package com.stork.shoebill;

import static com.stork.shoebill.TokenType.*;

import java.util.ArrayList;
import java.util.List;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    // start & current are offsets that index into the strong
    private int start = 0; // points at the first char in the lexeme being scanned
    private int current = 0; // points at the char currently being considered
    private int line = 1; // keep tracks what source line `current` is

    public Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // At beginning of the next lexeme
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }
    
    private boolean isAtEnd() {
        return current >= source.length(); // all chars consumed
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    // A comment goes until the end of line
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                } else {
                    addToken(SLASH);
                }
                break;
            
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace
                break;
            
            case '\n':
                line++;
                break;

            case '"': // String literals
                string();
                break;

            default:
                Shoebill.error(line, "Unexpected character.");
                break;
        }
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            advance();
        }

        if (isAtEnd()) {
            Shoebill.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim surrounding quotes
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private char peek() { // unconsuming effect
        if (isAtEnd()) return '\0'; // C-like terminating str char
        return source.charAt(current);
    }

    private boolean match(char expected) { // consuming effect, conditional advance()
        // consume current char only if current is what we expect
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char advance() { // consuming effect, scanner input
        // return source.charAt(current) and then post-incr current
        return source.charAt(current++);
    }

    private void addToken(TokenType type) { // scanner output
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

}
