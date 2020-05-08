 package fc.lexer;
 
 import java.text.CharacterIterator;
 import java.text.StringCharacterIterator;
 
 /**
  * Implementation of a simple lexical analyzer based on CharacterIterator and
  * StringBuilder.
  */
 public class Lexer {
     private final CharacterIterator charIterator;
     private final StringBuilder stringBuilder = new StringBuilder();
 
     public Lexer(String input) {
         charIterator = new StringCharacterIterator(input);
     }
 
     /**
      * Return the next token from the input.
      * 
      * @return next identifiable Token from the input string.
      * @throws LexerException
      *             if no valid token can be identified.
      */
     public Token nextToken() throws LexerException {
         Token result;
 
         // Consume leading whitespace
         scanWhitespace(null);
 
         // Over in JavaCC the token positions seem to be 1-based.
         int tokenPosition = charIterator.getIndex() + 1;
 
         switch (charIterator.current()) {
         case CharacterIterator.DONE:
             result = new Token(tokenPosition, Symbol.END);
             charIterator.next();
             break;
 
         case '=':
             result = new Token(tokenPosition, Symbol.EQUAL);
             charIterator.next();
             break;
 
         case '(':
             result = new Token(tokenPosition, Symbol.LPAREN);
             charIterator.next();
             break;
 
         case ')':
             result = new Token(tokenPosition, Symbol.RPAREN);
             charIterator.next();
             break;
 
         case '+':
             result = new Token(tokenPosition, Symbol.PLUS);
             charIterator.next();
             break;
 
         case '-':
             result = new Token(tokenPosition, Symbol.MINUS);
             charIterator.next();
             break;
 
         case '*':
             result = new Token(tokenPosition, Symbol.MULTIPLY);
             charIterator.next();
             break;
 
         case '/':
             result = new Token(tokenPosition, Symbol.DIVIDE);
             charIterator.next();
             break;
 
         default:
             // clear string buffer
             stringBuilder.delete(0, stringBuilder.length());
 
             if (scanNumber(stringBuilder) > 0) {
                 // Successfully scanned a dotted decimal number
                 result = new Token(tokenPosition, Symbol.NUMBER,
                         stringBuilder.toString());
             }
             else if (scanIdentifier(stringBuilder) > 0) {
                 // Successfully scanned an identifier
                 String identifier = stringBuilder.toString();
 
                 // Decide if we have a keyword here
                 if (identifier.equals("let")) {
                     result = new Token(tokenPosition, Symbol.LET);
                 }
                 else if (identifier.equals("exit")) {
                     result = new Token(tokenPosition, Symbol.EXIT);
                 }
                 else {
                     // Otherwise it must be a variable name
                     result = new Token(tokenPosition, Symbol.IDENTIFIER,
                             identifier);
                 }
             }
             else {
                 // If we got have a number nor a keyword or identifier we
                 // stumbled upon something funny... Tell them.
                 throw new LexerException("Unexpected symbol '"
                         + charIterator.current() + "'", tokenPosition);
             }
 
             break;
         }
 
         return result;
     }
 
     /**
      * Append characters from charIterator matching the condition to
      * stringBuilder and return how many characters were processed.
      * 
      * @param charIterator
      *            A CharIterator over the input string/stream/etc
      * @param condition
      *            The condition every char must match
      * @param stringBuilder
      *            On return the StringBuilder contains the matched characters if
      *            any. Pass null if the matched character sequence is not of
      *            interest.
      * @return The number of processed chars
      */
     private int scan(CharacterIterator charIterator, CharMatcher condition,
             StringBuilder stringBuilder) {
         return scan(charIterator, condition, 0, stringBuilder);
     }
 
     /**
      * Append characters from charIterator matching the condition up to the
      * given limit to stringBuilder and return how many characters were
      * processed.
      * 
      * @param charIterator
      *            A CharIterator over the input string/stream/etc
      * @param condition
      *            The condition every char must match
      * @param limit
      *            A number specifying how many chars will be processed at
      *            maximum. Pass 0 for no limit.
      * @param stringBuilder
      *            On return the StringBuilder contains the matched characters if
      *            any. Pass null if the matched character sequence is not of
      *            interest.
      * @return The number of processed chars
      */
     private int scan(CharacterIterator charIterator, CharMatcher condition,
             int limit, StringBuilder stringBuilder) {
         int count = 0;
 
         for (char c = charIterator.current(); c != CharacterIterator.DONE
                 && condition.match(c) && (limit == 0 || count < limit);
                 c = charIterator.next()) {
             if (stringBuilder != null) {
                 stringBuilder.append(c);
             }
             count++;
         }
 
         return count;
     }
 
     /**
      * Advance charIterator until it points to a non-whitespace character.
      *
      * @return The number of processed chars
      */
     private int scanWhitespace(StringBuilder stringBuilder) {
         final CharMatcher matcher = new CharMatcher() {
             @Override
             public boolean match(char c) {
                 return Character.isWhitespace(c);
             }
         };
 
         return scan(charIterator, matcher, null);
     }
 
     /**
      * Scan a dotted decimal number into stringBuilder
      *
      * @return The number of processed chars
      */
     private int scanNumber(StringBuilder stringBuilder) {
         final CharMatcher dot = new CharMatcher() {
             @Override
             public boolean match(char c) {
                 return c == '.';
             }
         };
 
         final CharMatcher digit = new CharMatcher() {
             @Override
             public boolean match(char c) {
                 return Character.isDigit(c);
             }
         };
 
        int digitcount;
        int dotcount;

        // Scan zero or more digits
        digitcount = scan(charIterator, digit, stringBuilder);
        // Scan zero or one dot
        dotcount = scan(charIterator, dot, 1, stringBuilder);
        // Scan zero or more digits
        digitcount += scan(charIterator, digit, stringBuilder);

        // If we've only scanned a dot we have to back out.
        if (digitcount == 0 && dotcount == 1) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            charIterator.previous();
            dotcount--;
        }

        return digitcount + dotcount;
     }
 
     /**
      * Scan an identifier into stringBuilder
      *
      * @return The number of processed chars
      */
     private int scanIdentifier(StringBuilder stringBuilder) {
         final CharMatcher matcher = new CharMatcher() {
             @Override
             public boolean match(char c) {
                 return Character.isLetter(c);
             }
         };
 
         return scan(charIterator, matcher, stringBuilder);
     }
 }
