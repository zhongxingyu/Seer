 package wci.frontend.java;
 
 import wci.frontend.*;
 import wci.frontend.java.tokens.*;
 
 import static wci.frontend.Source.EOF;
 import static wci.frontend.Source.EOL;
 import static wci.frontend.java.JavaTokenType.*;
 import static wci.frontend.java.JavaErrorCode.*;
 
 /**
  * <h1>JavaScanner</h1>
  *
  * <p>The Java scanner.</p>
  *
  * <p>Copyright (c) 2009 by Ronald Mak</p>
  * <p>For instructional purposes only.  No warranties.</p>
  */
 public class JavaScanner extends Scanner
 {
     /**
      * Constructor
      * @param source the source to be used with this scanner.
      */
     public JavaScanner(Source source)
     {
         super(source);
     }
 
     /**
      * Extract and return the next Java token from the source.
      * @return the next token.
      * @throws Exception if an error occurred.
      */
     protected Token extractToken()
         throws Exception
     {
         skipWhiteSpace();
 
         Token token;
         char currentChar = currentChar();
 
         // Construct the next token.  The current character determines the
         // token type.
         if (currentChar == EOF) {
             token = new EofToken(source);
         }
         else if (Character.isLetter(currentChar)) {
             token = new JavaWordToken(source);
         }
         else if (Character.isDigit(currentChar)) {
             token = new JavaNumberToken(source);
         }
         else if (currentChar == '\'') {
             token = new JavaCharToken(source);
         }
         else if (currentChar == '"') {
             token = new JavaStringToken(source);
         }
         else if (JavaTokenType.SPECIAL_SYMBOLS
                  .containsKey(Character.toString(currentChar))) {
             token = new JavaSpecialSymbolToken(source);
         }
         else {
             token = new JavaErrorToken(source, INVALID_CHARACTER,
                                          Character.toString(currentChar));
             nextChar();  // consume character
         }
 
         return token;
     }
 
     /**
      * Skip whitespace characters by consuming them.  A comment is whitespace.
      * @throws Exception if an error occurred.
      */
     private void skipWhiteSpace()
         throws Exception
     {
         char currentChar = currentChar();
         boolean foundAsterisk = false; // flag to detect closing */ in comments
 
         while (Character.isWhitespace(currentChar) || (currentChar == '/')) 
         {   // Start of a comment?
             if (currentChar == '/')
             {
                currentChar = nextChar();
                 
                 // Consume comments with '//'
                if (currentChar == '/')
                 {
                     do currentChar = nextChar();
                     while (currentChar != EOL && currentChar != EOF);
                     
                     if (currentChar == EOL)
                         currentChar = nextChar();
                 }
                 // Consume comments with '/* */'
                else if (currentChar == '*')
                 {
                     do 
                     { 
                         currentChar = nextChar();
                         if (foundAsterisk && currentChar == '/')
                             break;
                         else 
                             foundAsterisk = currentChar == '*';
                     } while (currentChar != EOF);
                     
                     if (currentChar == '/')
                         currentChar = nextChar();
                 }
             }
             // Not a comment.
             else {
                currentChar = nextChar();  // consume whitespace character
             }
         }
     }
 }
