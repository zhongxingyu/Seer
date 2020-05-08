 package frontend.tokens;
 
 import frontend.Source;
 import frontend.Token;
 import frontend.TokenType;
 
 import java.io.IOException;
 
 public class WordToken extends Token {
     public WordToken(Source source) throws IOException {
         super(source);
     }
 
     protected void extract() throws IOException {
         StringBuilder textBuffer = new StringBuilder();
         char currentChar = currentChar();
 
         // Get the word characters (letter or digit).  The scanner has
         // already determined that the first character is a letter.
         while (Character.isLetterOrDigit(currentChar)
                 || currentChar == '-' || currentChar == '?')
         {
             textBuffer.append(currentChar);
             currentChar = nextChar();  // consume character
         }
 
         text = textBuffer.toString();
         type = TokenType.RESERVED_WORDS.get(text);
 
         if (text.equals("let") && currentChar() == '*') {
             text = "let*";
             type = TokenType.ALL_SYMBOLS.get(text);
         }
         else if (text.equals("null") && currentChar() == '?') {
             text = "null?";
             type = TokenType.ALL_SYMBOLS.get(text);
         }
         else if (type == null) {
             type = TokenType.REGULAR_SYMBOL;
         }
         else {
             // Error here. This is for Assignment 6.
         }
     }
 }
