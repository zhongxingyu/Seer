 package jlogic.read;
 
 public final class Lexer {
     private String file;
     private int line = 0;
     private int column = 0;
 
     private String code;
     private char current;
     private int codeIndex = 0;
 
     public Lexer(String file, String code) {
         this.file = file;
         this.code = code;
 
         if (code.length() > 0)
             current = code.charAt(0);
     }
 
     public String getFile() {
         return file;
     }
 
     public Token read() throws ReadException {
         assert codeIndex >= 0 && codeIndex <= code.length();
 
         if (codeIndex == code.length())
             return new Token(createLocation(), TokenType.EndOfFile, "");
 
         Token token = null;
         if (Character.isLetter(current)) {
             token = readIdentifier();
         } else if (current == '(') {
             token = new Token(createLocation(), TokenType.LeftParen, "(");
             advance();
         } else if (current == ')') {
             token = new Token(createLocation(), TokenType.RightParen, ")");
             advance();
         } else if (current == '_') {
             token = new Token(createLocation(), TokenType.Underscore, ")");
             advance();
         } else if (current == ',') {
             token = new Token(createLocation(), TokenType.Comma, ",");
             advance();
         }
 
         if (token == null)
             throw new ReadException(createLocation(), "Unknown character: " + current);
 
         skipWhitespace();
 
         return token;
     }
 
     private void advance() {
         ++codeIndex;
         ++column;
 
         if (!isEndOfFile())
             current = code.charAt(codeIndex);
     }
 
     private boolean isEndOfFile() {
         return codeIndex == code.length();
     }
 
     private Location createLocation() {
         return new Location(file, line, column);
     }
 
     private Token readIdentifier() {
         Location location = createLocation();
 
         int start = codeIndex;
         while (!isEndOfFile() &&
                 (Character.isLetter(current) || Character.isDigit(current) ||
                 current == '_')) {
             advance();
         }
 
         return new Token(location, TokenType.Identifier, code.substring(start, codeIndex));
     }
 
     private void skipWhitespace() {
        while (!isEndOfFile() && Character.isWhitespace(current)) {
             if (current == '\n') {
                 advance();
                 column = 0;
                 ++line;
             }
             else
                 advance();
         }
     }
 }
