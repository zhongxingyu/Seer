 package no.shhsoft.basus.language.parser;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import no.shhsoft.basus.language.OperatorType;
 import no.shhsoft.basus.language.Reserved;
 import no.shhsoft.basus.utils.ErrorUtils;
 import no.shhsoft.basus.utils.TextLocation;
 import no.shhsoft.basus.value.IntegerValue;
 import no.shhsoft.basus.value.NumericValue;
 import no.shhsoft.basus.value.RealValue;
 import no.shhsoft.basus.value.StringValue;
 
 /**
  * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
  */
 public final class BasusTokenizer
 implements Tokenizer {
 
     private static final char EOF = Character.MIN_VALUE;
     private final char[] chars;
     private boolean commentsAreTokens;
     private int idx;
     private int line;
     private int col;
     private int nextLine;
     private int nextCol;
     private final List<Token> tokenStack = new ArrayList<Token>();
 
     private TextLocation getCurrentTextLocation() {
         return new TextLocation(line, col, idx);
     }
 
     private void error(final String key, final Object... args) {
        throw new ParserException(ErrorUtils.getMessage(key, null, args), getCurrentTextLocation());
     }
 
     private char nextChar() {
         line = nextLine;
         col = nextCol;
         if (idx >= chars.length - 1) {
             ++idx;
             return EOF;
         }
         final char c = chars[++idx];
         ++nextCol;
         if (c == '\n') {
             ++nextLine;
             nextCol = 1;
         }
         return c;
     }
 
     private char peekNextChar() {
         if (idx >= chars.length - 1) {
             return EOF;
         }
         return chars[idx + 1];
     }
 
     private char currChar() {
         if (idx >= chars.length) {
             return EOF;
         }
         return chars[idx];
     }
 
     private boolean isIdentifierStart(final char c) {
         return c == '_' || Character.isLetter(c);
     }
 
     private boolean isIdentifierPart(final char c) {
         return isIdentifierStart(c) || (c >= '0' && c <= '9');
     }
 
     private void skipWhitespace() {
         for (;;) {
             final char c = currChar();
             if (!commentsAreTokens && c == '/') {
                 final char nc = peekNextChar();
                 if (nc == '/') {
                     while (currChar() != EOF && currChar() != '\n') {
                         nextChar();
                     }
                 } else if (nc == '*') {
                     nextChar();
                     nextChar();
                     while (currChar() != EOF) {
                         if (currChar() == '*' && peekNextChar() == '/') {
                             break;
                         }
                         nextChar();
                     }
                     nextChar();
                 } else {
                     break;
                 }
             } else if (!Character.isWhitespace(c)) {
                 break;
             }
             nextChar();
         }
     }
 
     private String scanComment() {
         final StringBuilder sb = new StringBuilder();
         sb.append('/');
         sb.append(currChar());
         final char cc = currChar();
         if (cc == '/') {
             while (currChar() != EOF && currChar() != '\n') {
                 sb.append(nextChar());
             }
         } else if (cc == '*') {
             sb.append(nextChar());
             sb.append(nextChar());
             while (currChar() != EOF) {
                 if (currChar() == '*' && peekNextChar() == '/') {
                     break;
                 }
                 sb.append(nextChar());
             }
             sb.append(nextChar());
         } else {
             throw new RuntimeException("Expected comment start.");
         }
         nextChar();
         return sb.toString();
     }
 
     private String scanIdentifier() {
         final StringBuilder sb = new StringBuilder();
         sb.append(currChar());
         char c;
         while ((c = nextChar()) != EOF) {
             if (!isIdentifierPart(c)) {
                 break;
             }
             sb.append(c);
         }
         return sb.toString();
     }
 
     private NumericValue scanNumber() {
         double number = 0.0;
         double divider = 0.1;
         boolean dotSeen = false;
         boolean isReal = false;
         char c;
         while ((c = currChar()) != EOF) {
             if (c == '.') {
                 dotSeen = true;
                 isReal = true;
             } else if (c == 'e' || c == 'E') {
                 isReal = true;
                 c = nextChar();
                 if (c == EOF) {
                     error("err.danglingExpChar");
                 }
                 double sign = 1.0;
                 if (c == '-') {
                     sign = -1.0;
                     c = nextChar();
                     if (c == EOF) {
                         error("err.danglingNegExp");
                     }
                 }
                 final double exp = scanNumber().getValueAsDouble();
                 number = number * Math.pow(10.0, sign * exp);
                 break;
             } else if (c >= '0' && c <= '9') {
                 final double digit = c - '0';
                 if (dotSeen) {
                     number += digit * divider;
                     divider /= 10.0;
                 } else {
                     number = number * 10.0 + digit;
                 }
             } else {
                 break;
             }
             nextChar();
         }
         if (!isReal && (number > Integer.MAX_VALUE || number < Integer.MIN_VALUE)) {
             isReal = true;
         }
         if (isReal) {
             return new RealValue(number);
         }
         return IntegerValue.get((int) number);
     }
 
     private StringValue scanString() {
         final StringBuilder sb = new StringBuilder();
         boolean endSeen = false;
         char c;
         while ((c = nextChar()) != EOF) {
             if (c == '\\') {
                 c = nextChar();
                 if (c == EOF) {
                     error("err.unexpectedEndOfInput");
                 }
                 switch (c) {
                     case 'r':
                         sb.append('\r');
                         break;
                     case 'n':
                         sb.append('\n');
                         break;
                     case 'b':
                         sb.append('\b');
                         break;
                     case 't':
                         sb.append('\t');
                         break;
                     default:
                         sb.append(c);
                 }
             } else if (c == '"') {
                 endSeen = true;
                 nextChar();
                 break;
             } else if (c == '\r' || c == '\n') {
                 error("err.multilineString");
             } else {
                 sb.append(c);
             }
         }
         if (!endSeen) {
             error("err.stringEndOfInput");
         }
         return new StringValue(sb.toString());
     }
 
     private IntegerValue scanCharacter() {
         IntegerValue value = null;
         char c = nextChar();
         if (c == EOF) {
             error("err.charNotTerminated");
         }
         if (c == '\\') {
             c = nextChar();
             if (c == EOF) {
                 error("err.unexpectedEndOfInput");
             }
             switch (c) {
                 case 'r':
                     value = IntegerValue.getFromCharacterConstant('\r');
                     break;
                 case 'n':
                     value = IntegerValue.getFromCharacterConstant('\n');
                     break;
                 case 'b':
                     value = IntegerValue.getFromCharacterConstant('\b');
                     break;
                 case 't':
                     value = IntegerValue.getFromCharacterConstant('\t');
                     break;
                 default:
                     value = IntegerValue.getFromCharacterConstant(c);
             }
         } else {
             value = IntegerValue.getFromCharacterConstant(c);
         }
         c = nextChar();
         if (c != '\'') {
             error("err.charNotTerminated");
         }
         nextChar();
         return value;
     }
 
     public BasusTokenizer(final String s) {
         chars = s.replaceAll("\r\n", "\n").replaceAll("\r", "\n").toCharArray();
         idx = -1;
         nextLine = 1;
         nextCol = 1;
         nextChar();
     }
 
     public BasusTokenizer(final String s, final boolean commentsAreTokens) {
         this(s);
         this.commentsAreTokens = commentsAreTokens;
     }
 
     @Override
     @SuppressWarnings("boxing")
     public Token nextToken() {
         if (tokenStack.size() > 0) {
             return tokenStack.remove(tokenStack.size() - 1);
         }
         skipWhitespace();
         final TextLocation startLocation = getCurrentTextLocation();
         final char c = currChar();
         if (c == EOF) {
             return null;
         }
         if (isIdentifierStart(c)) {
             final String word = scanIdentifier();
             final Reserved reserved = Reserved.getReservedEnum(word);
             if (reserved != null) {
                 if (reserved == Reserved.AND) {
                     return new Token(OperatorType.AND, startLocation, getCurrentTextLocation());
                 }
                 if (reserved == Reserved.OR) {
                     return new Token(OperatorType.OR, startLocation, getCurrentTextLocation());
                 }
                 if (reserved == Reserved.NOT) {
                     return new Token(OperatorType.NOT, startLocation, getCurrentTextLocation());
                 }
                 return new Token(reserved, startLocation, getCurrentTextLocation());
             }
             return new Token(word, startLocation, getCurrentTextLocation());
         }
         if (c == '.' || (c >= '0' && c <= '9')) {
             return new Token(scanNumber(), startLocation, getCurrentTextLocation());
         }
         if (c == '"') {
             return new Token(scanString(), startLocation, getCurrentTextLocation());
         }
         if (c == '\'') {
             return new Token(scanCharacter(), startLocation, getCurrentTextLocation());
         }
         nextChar();
         if (c == '(') {
             return new Token(OperatorType.LEFT_PAREN, startLocation, getCurrentTextLocation());
         }
         if (c == ')') {
             return new Token(OperatorType.RIGHT_PAREN, startLocation, getCurrentTextLocation());
         }
         if (c == '[') {
             return new Token(OperatorType.LEFT_BRACKET, startLocation, getCurrentTextLocation());
         }
         if (c == ']') {
             return new Token(OperatorType.RIGHT_BRACKET, startLocation, getCurrentTextLocation());
         }
         if (c == '+') {
             return new Token(OperatorType.PLUS, startLocation, getCurrentTextLocation());
         }
         if (c == ',') {
             return new Token(OperatorType.COMMA, startLocation, getCurrentTextLocation());
         }
         if (c == '-') {
             return new Token(OperatorType.MINUS, startLocation, getCurrentTextLocation());
         }
         if (c == '*') {
             return new Token(OperatorType.MULTIPLY, startLocation, getCurrentTextLocation());
         }
         if (c == '/') {
             if (commentsAreTokens && (currChar() == '/' || currChar() == '*')) {
                 return Token.commentToken(scanComment(), startLocation, getCurrentTextLocation());
             }
             return new Token(OperatorType.DIVIDE, startLocation, getCurrentTextLocation());
         }
         if (c == '%') {
             return new Token(OperatorType.MODULUS, startLocation, getCurrentTextLocation());
         }
         if (c == '^') {
             return new Token(OperatorType.EXPONENTIATE, startLocation, getCurrentTextLocation());
         }
         if (c == ';') {
             return new Token(OperatorType.SEMICOLON, startLocation, getCurrentTextLocation());
         }
         if (c == '=') {
             if (currChar() == '=') {
                 nextChar();
                 return new Token(OperatorType.EQUAL, startLocation, getCurrentTextLocation());
             }
             return new Token(OperatorType.ASSIGN, startLocation, getCurrentTextLocation());
         }
         if (c == '!') {
             if (currChar() == '=') {
                 nextChar();
                 return new Token(OperatorType.NOT_EQUAL, startLocation, getCurrentTextLocation());
             }
         }
         if (c == '<') {
             if (currChar() == '=') {
                 nextChar();
                 return new Token(OperatorType.LESS_OR_EQUAL, startLocation,
                                  getCurrentTextLocation());
             }
             return new Token(OperatorType.LESS, startLocation, getCurrentTextLocation());
         }
         if (c == '>') {
             if (currChar() == '=') {
                 nextChar();
                 return new Token(OperatorType.GREATER_OR_EQUAL, startLocation,
                                  getCurrentTextLocation());
             }
             return new Token(OperatorType.GREATER, startLocation, getCurrentTextLocation());
         }
         error("err.unexpectedChar", c);
         /* return statement just to please the compiler */
         return null;
     }
 
     @Override
     public void pushBack(final Token token) {
         tokenStack.add(token);
     }
 
 }
