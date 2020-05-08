 package nd.regex;
 
 import static nd.regex.Token.Type;
 
 /**
  *
  */
 final class LexerImpl implements Lexer {
 
     static private final char EOF = (char) -1;
     private final String pattern;
     private int currentIndex = 0;
     private char current;
 
 
     LexerImpl(String pattern) {
         this.pattern = pattern;
        if (pattern.length() > 0) {
            this.current = pattern.charAt(0);
        } else {
            this.current = EOF;
        }
     }
 
     @Override
     public Token nextToken() {
         if (current != EOF) {
             if (isSpecialCharacter(current)) {
                 return parseSpecialCharacter();
             } else {
                 Token token = new Token(Type.CHARACTER, String.valueOf(current));
                 consume();
                 return token;
             }
         } else {
             return new Token(Type.EOF, "EOF");
         }
     }
 
 
     private boolean isSpecialCharacter(char c) {
         return c == '[' ||
                c == ']' ||
                c == '^' ||
                c == '.' ||
                c == '?' ||
                c == '*' ||
                c == '+' ||
                c == '\\'||
                c == '{' ||
                c == '}' ||
                c == '(' ||
                c == ')' ||
                c == '$' ||
                c == '-' ||
                c == '|';
     }
 
     private Token parseSpecialCharacter() {
         char c = current;
         consume();
         switch (c) {
             case '[' :
                 if (current == '^') {
                     consume();
                     return new Token(Type.LEFT_BRACKET_CARET, "[^");
                 } else {
                     return new Token(Type.LEFT_BRACKET, "[");
                 }
             case ']' : return new Token(Type.RIGHT_BRACKET, "]");
             case '^' : return new Token(Type.CARET, "^");
             case '$' : return new Token(Type.DOLLAR, "$");
             case '.' : return new Token(Type.CLASS_ANY_CHARACTER, ".");
             case '?' : return new Token(Type.ZERO_OR_ONE, "?");
             case '*' : return new Token(Type.ZERO_OR_MORE, "*");
             case '+' : return new Token(Type.ONE_OR_MORE, "+");
             case '-' : return new Token(Type.CHARACTER, "-");
             case '\\': return parseSlashCombination();
             case '{' : return new Token(Type.LEFT_CURLY_BRACKET, "{");
             case '}' : return new Token(Type.RIGHT_CURLY_BRACKET, "}");
             case '(' : return new Token(Type.LEFT_PAREN, "(");
             case ')' : return new Token(Type.RIGHT_PAREN, ")");
             case '|' : return new Token(Type.OR, "|");
             default  : throw new LexerException("Unexpected character " + c);
         }
     }
 
     private Token parseSlashCombination() {
         char c = current;
         consume();
         if (isSpecialCharacter(c)) {
             return new Token(Type.CHARACTER, String.valueOf(c));
         } else {
             switch (c) {
                 case 'd': return new Token(Type.CLASS_DIGIT, "d");
                 case 'D': return new Token(Type.CLASS_NON_DIGIT, "D");
                 case 's': return new Token(Type.CLASS_WHITESPACE, "s");
                 case 'S': return new Token(Type.CLASS_NON_WHITESPACE, "S");
                 case 'w': return new Token(Type.CLASS_WORD_CHARACTER, "w");
                 case 'W': return new Token(Type.CLASS_NON_WORD_CHARACTER, "W");
                 case 'b': return new Token(Type.WORD_BOUNDARY, "b");
                 case 'B': return new Token(Type.NON_WORD_BOUNDARY, "B");
                 default : throw new LexerException("Unexpected character " + c);
             }
         }
     }
 
 
     @Override
     public boolean hasNext() {
         return currentIndex < pattern.length();
     }
 
     private void consume() {
         currentIndex++;
         if (currentIndex >= pattern.length()) {
             current = EOF;
         } else {
             current = pattern.charAt(currentIndex);
         }
     }
 
     private static class LexerException extends Error {
         public LexerException(String message) {
             super(message);
         }
     }
 }
