 package org.mozilla.javascript;
 
 import java.io.*;
 
 /**
  * This class implements the JavaScript scanner.
  *
  * It is based on the C source files jsscan.c and jsscan.h
  * in the jsref package.
  *
  * @see org.mozilla.javascript.Parser
  *
  * @author Mike McCabe
  * @author Brendan Eich
  */
 
 public class TokenStream
 {
     /*
      * For chars - because we need something out-of-range
      * to check.  (And checking EOF by exception is annoying.)
      * Note distinction from EOF token type!
      */
     private final static int
         EOF_CHAR = -1;
 
     private final static char BYTE_ORDER_MARK = '\uFEFF';
 
     TokenStream(Reader sourceReader, String sourceString,
                 int lineno)
     {
         this.lineno = lineno;
         if (sourceReader != null) {
         	this.sourceReader = sourceReader;
             this.sourceBuffer = new char[512];
             this.sourceEnd = 0;
         } else {
             this.sourceString = sourceString;
             this.sourceEnd = sourceString.length();
         }
         this.sourceCursor = this.cursor = 0;
     }
 
     final String getSourceString() { return sourceString; }
     
     final int getLineno() { return lineno; }
 
     public final String getString() { return string; }
 
     final char getQuoteChar() {
         return (char) quoteChar;
     }
 
     final double getNumber() { return number; }
     final boolean isNumberOctal() { return isOctal; }
 
     final boolean eof() { return hitEOF; }
 
     public final int getToken() throws IOException
     {
         int c;
 
     retry:
         for (;;) {
             // Eat whitespace, possibly sensitive to newlines.
             for (;;) {
                 c = getChar();
                 if (c == EOF_CHAR) {
                     tokenBeg = cursor - 1;
                     tokenEnd = cursor;
                     return Token.EOF;
                 } else if (c == '\n') {
                     dirtyLine = false;
                     tokenBeg = cursor - 1;
                     tokenEnd = cursor;
                     return Token.EOL;
                 } else if (!isJSSpace(c)) {
                     if (c != '-') {
                         dirtyLine = true;
                     }
                     break;
                 }
             }
 
             // Assume the token will be 1 char - fixed up below.
             tokenBeg = cursor - 1;
             tokenEnd = cursor;
 
             // identifier/keyword/instanceof?
             // watch out for starting with a <backslash>
             boolean identifierStart;
             boolean isUnicodeEscapeStart = false;
             if (c == '\\') {
                 c = getChar();
                 if (c == 'u') {
                     identifierStart = true;
                     stringBufferTop = 0;
                 } else {
                     identifierStart = false;
                     ungetChar(c);
                     c = '\\';
                 }
             } else {
                 identifierStart = Character.isJavaIdentifierStart((char)c);
                 if (identifierStart) {
                     stringBufferTop = 0;
                     addToString(c);
                 }
             }
 
             if (identifierStart) {
                 boolean containsEscape = isUnicodeEscapeStart;
                 for (;;) {
                     if (isUnicodeEscapeStart) {
                         // strictly speaking we should probably push-back
                         // all the bad characters if the <backslash>uXXXX
                         // sequence is malformed. But since there isn't a
                         // correct context(is there?) for a bad Unicode
                         // escape sequence in an identifier, we can report
                         // an error here.
                         int escapeVal = 0;
                         for (int i = 0; i != 4; ++i) {
                             c = getChar();
                             escapeVal = Kit.xDigitToInt(c, escapeVal);
                             // Next check takes care about c < 0 and bad escape
                             if (escapeVal < 0) { break; }
                         }
                         if (escapeVal < 0) {
                             return Token.ERROR;
                         }
                         addToString(escapeVal);
                         isUnicodeEscapeStart = false;
                     } else {
                         c = getChar();
                         if (c == '\\') {
                             c = getChar();
                             if (c == 'u') {
                                 isUnicodeEscapeStart = true;
                                 containsEscape = true;
                             } else {
                                 return Token.ERROR;
                             }
                         } else {
                             if (c == EOF_CHAR || c == BYTE_ORDER_MARK
                                 || !Character.isJavaIdentifierPart((char)c))
                             {
                                 break;
                             }
                             addToString(c);
                         }
                     }
                 }
                 ungetChar(c);
 
                 String str = getStringFromBuffer();
                 this.string = (String)allStrings.intern(str);
                 return Token.NAME;
             }
 
             // is it a number?
             if (isDigit(c) || (c == '.' && isDigit(peekChar()))) {
                 isOctal = false;
                 stringBufferTop = 0;
                 int base = 10;
 
                 while ('0' <= c && c <= '9') {
                     addToString(c);
                     c = getChar();
                 }
 
                 boolean isInteger = true;
                 ungetChar(c);
                 String numString = getStringFromBuffer();
                 this.string = numString;
 
                 double dval;
                 if (!isInteger) {
                     try {
                         // Use Java conversion to number from string...
                         dval = Double.valueOf(numString).doubleValue();
                     }
                     catch (NumberFormatException ex) {
                         return Token.ERROR;
                     }
                 } else {
                     dval = stringToNumber(numString, 0, base);
                 }
 
                 this.number = dval;
                 return Token.NUMBER;
             }
 
             switch (c) {
             case ';': return Token.SEMI;
             case '[': return Token.LB;
             case ']': return Token.RB;
             case '{': return Token.LC;
             case '}': return Token.RC;
             case '(': return Token.LP;
             case ')': return Token.RP;
             case ',': return Token.COMMA;
             case '?': return Token.HOOK;
             case ':':
                 if (matchChar(':')) {
                     return Token.COLONCOLON;
                 } else {
                     return Token.COLON;
                 }
             case '.':
                 if (matchChar('.')) {
                     return Token.DOTDOT;
                 } else if (matchChar('(')) {
                     return Token.DOTQUERY;
                 } else {
                     return Token.DOT;
                 }
 
             case '|':
                 if (matchChar('|')) {
                     return Token.OR;
                 } else if (matchChar('=')) {
                     return Token.ASSIGN_BITOR;
                 } else {
                     return Token.BITOR;
                 }
 
             case '^':
                 if (matchChar('=')) {
                     return Token.ASSIGN_BITXOR;
                 } else {
                     return Token.BITXOR;
                 }
 
             case '&':
                 if (matchChar('&')) {
                     return Token.AND;
                 } else if (matchChar('=')) {
                     return Token.ASSIGN_BITAND;
                 } else {
                     return Token.BITAND;
                 }
 
             case '=':
                 if (matchChar('=')) {
                     if (matchChar('=')) {
                         return Token.SHEQ;
                     } else {
                         return Token.EQ;
                     }
                 } else {
                     return Token.ASSIGN;
                 }
 
             case '!':
                 if (matchChar('=')) {
                     if (matchChar('=')) {
                         return Token.SHNE;
                     } else {
                         return Token.NE;
                     }
                 } else {
                     return Token.NOT;
                 }
 
             case '<':
                 if (matchChar('<')) {
                     if (matchChar('=')) {
                         return Token.ASSIGN_LSH;
                     } else {
                         return Token.LSH;
                     }
                 } else {
                     if (matchChar('=')) {
                         return Token.LE;
                     } else {
                         return Token.LT;
                     }
                 }
 
             case '>':
                 if (matchChar('>')) {
                     if (matchChar('>')) {
                         if (matchChar('=')) {
                             return Token.ASSIGN_URSH;
                         } else {
                             return Token.URSH;
                         }
                     } else {
                         if (matchChar('=')) {
                             return Token.ASSIGN_RSH;
                         } else {
                             return Token.RSH;
                         }
                     }
                 } else {
                     if (matchChar('=')) {
                         return Token.GE;
                     } else {
                         return Token.GT;
                     }
                 }
 
             case '*':
                 if (matchChar('=')) {
                     return Token.ASSIGN_MUL;
                 } else {
                     return Token.MUL;
                 }
 
             case '/':
                 if (matchChar('=')) {
                     return Token.ASSIGN_DIV;
                 } else {
                     return Token.DIV;
                 }
 
             case '%':
                 if (matchChar('=')) {
                     return Token.ASSIGN_MOD;
                 } else {
                     return Token.MOD;
                 }
 
             case '~':
                 return Token.BITNOT;
 
             case '+':
                 if (matchChar('=')) {
                     return Token.ASSIGN_ADD;
                 } else if (matchChar('+')) {
                     return Token.INC;
                 } else {
                     return Token.ADD;
                 }
 
             case '-':
                 if (matchChar('=')) {
                     c = Token.ASSIGN_SUB;
                 } else if (matchChar('-')) {
                     c = Token.DEC;
                 } else {
                    c = Token.SUB;
                 }
                 return c;
 
             default:
                 System.out.println("Illegal character");
                 return Token.ERROR;
             }
         }
     }
 
     private static boolean isAlpha(int c)
     {
         // Use 'Z' < 'a'
         if (c <= 'Z') {
             return 'A' <= c;
         } else {
             return 'a' <= c && c <= 'z';
         }
     }
 
     static boolean isDigit(int c)
     {
         return '0' <= c && c <= '9';
     }
 
     /* As defined in ECMA.  jsscan.c uses C isspace() (which allows
      * \v, I think.)  note that code in getChar() implicitly accepts
      * '\r' == \u000D as well.
      */
     static boolean isJSSpace(int c)
     {
         if (c <= 127) {
             return c == 0x20 || c == 0x9 || c == 0xC || c == 0xB;
         } else {
             return c == 0xA0 || c == BYTE_ORDER_MARK
                 || Character.getType((char)c) == Character.SPACE_SEPARATOR;
         }
     }
 
     private static boolean isJSFormatChar(int c)
     {
         return c > 127 && Character.getType((char)c) == Character.FORMAT;
     }
 
     private String getStringFromBuffer()
     {
         tokenEnd = cursor;
         return new String(stringBuffer, 0, stringBufferTop);
     }
 
     private void addToString(int c)
     {
         int N = stringBufferTop;
         if (N == stringBuffer.length) {
             char[] tmp = new char[stringBuffer.length * 2];
             System.arraycopy(stringBuffer, 0, tmp, 0, N);
             stringBuffer = tmp;
         }
         stringBuffer[N] = (char)c;
         stringBufferTop = N + 1;
     }
     
     private boolean canUngetChar() {
         return ungetCursor == 0 || ungetBuffer[ungetCursor - 1] != '\n';
     }
 
     private void ungetChar(int c)
     {
         // can not unread past across line boundary
         if (ungetCursor != 0 && ungetBuffer[ungetCursor - 1] == '\n')
             Kit.codeBug();
         ungetBuffer[ungetCursor++] = c;
         cursor--;
     }
 
     private boolean matchChar(int test) throws IOException
     {
         int c = getCharIgnoreLineEnd();
         if (c == test) {
             tokenEnd = cursor;
             return true;
         } else {
             ungetCharIgnoreLineEnd(c);
             return false;
         }
     }
 
     private int peekChar() throws IOException
     {
         int c = getChar();
         ungetChar(c);
         return c;
     }
 
     private int getChar() throws IOException
     {
         if (ungetCursor != 0) {
             cursor++;
             return ungetBuffer[--ungetCursor];
         }
 
         for(;;) {
             int c;
             if (sourceString != null) {
                 if (sourceCursor == sourceEnd) {
                     hitEOF = true;
                     return EOF_CHAR;
                 }
                 cursor++;
                 c = sourceString.charAt(sourceCursor++);
             } else {
                 if (sourceCursor == sourceEnd) {
                     if (!fillSourceBuffer()) {
                         hitEOF = true;
                         return EOF_CHAR;
                     }
                 }
                 cursor++;
                 c = sourceBuffer[sourceCursor++];
             }
 
             if (lineEndChar >= 0) {
                 if (lineEndChar == '\r' && c == '\n') {
                     lineEndChar = '\n';
                     continue;
                 }
                 lineEndChar = -1;
                 lineStart = sourceCursor - 1;
                 lineno++;
             }
 
             if (c <= 127) {
                 if (c == '\n' || c == '\r') {
                     lineEndChar = c;
                     c = '\n';
                 }
             } else {
                 if (c == BYTE_ORDER_MARK) return c; // BOM is considered whitespace
                 if (isJSFormatChar(c)) {
                     continue;
                 }
                 if (isJSLineTerminator(c)) {
                     lineEndChar = c;
                     c = '\n';
                 }
             }
             return c;
         }
     }
 
     private int getCharIgnoreLineEnd() throws IOException
     {
         if (ungetCursor != 0) {
             cursor++;
             return ungetBuffer[--ungetCursor];
         }
 
         for(;;) {
             int c;
             if (sourceString != null) {
                 if (sourceCursor == sourceEnd) {
                     hitEOF = true;
                     return EOF_CHAR;
                 }
                 cursor++;
                 c = sourceString.charAt(sourceCursor++);
             } else {
                 if (sourceCursor == sourceEnd) {
                     if (!fillSourceBuffer()) {
                         hitEOF = true;
                         return EOF_CHAR;
                     }
                 }
                 cursor++;
                 c = sourceBuffer[sourceCursor++];
             }
 
             if (c <= 127) {
                 if (c == '\n' || c == '\r') {
                     lineEndChar = c;
                     c = '\n';
                 }
             } else {
                 if (c == BYTE_ORDER_MARK) return c; // BOM is considered whitespace
                 if (isJSFormatChar(c)) {
                     continue;
                 }
                 if (isJSLineTerminator(c)) {
                     lineEndChar = c;
                     c = '\n';
                 }
             }
             return c;
         }
     }
 
     private void ungetCharIgnoreLineEnd(int c)
     {
         ungetBuffer[ungetCursor++] = c;
         cursor--;
     }
 
     private void skipLine() throws IOException
     {
         // skip to end of line
         int c;
         while ((c = getChar()) != EOF_CHAR && c != '\n') { }
         ungetChar(c);
         tokenEnd = cursor;
     }
 
     /**
      * Returns the offset into the current line.
      */
     final int getOffset()
     {
         int n = sourceCursor - lineStart;
         if (lineEndChar >= 0) { --n; }
         return n;
     }
 
     final String getLine()
     {
         if (sourceString != null) {
             // String case
             int lineEnd = sourceCursor;
             if (lineEndChar >= 0) {
                 --lineEnd;
             } else {
                 for (; lineEnd != sourceEnd; ++lineEnd) {
                     int c = sourceString.charAt(lineEnd);
                     if (isJSLineTerminator(c)) {
                         break;
                     }
                 }
             }
             return sourceString.substring(lineStart, lineEnd);
         } else {
             // Reader case
             int lineLength = sourceCursor - lineStart;
             if (lineEndChar >= 0) {
                 --lineLength;
             } else {
                 // Read until the end of line
                 for (;; ++lineLength) {
                     int i = lineStart + lineLength;
                     if (i == sourceEnd) {
                         try {
                             if (!fillSourceBuffer()) { break; }
                         } catch (IOException ioe) {
                             // ignore it, we're already displaying an error...
                             break;
                         }
                         // i recalculuation as fillSourceBuffer can move saved
                         // line buffer and change lineStart
                         i = lineStart + lineLength;
                     }
                     int c = sourceBuffer[i];
                     if (isJSLineTerminator(c)) {
                         break;
                     }
                 }
             }
             return new String(sourceBuffer, lineStart, lineLength);
         }
     }
 
     private boolean fillSourceBuffer() throws IOException
     {
         if (sourceString != null) Kit.codeBug();
         if (sourceEnd == sourceBuffer.length) {
             if (lineStart != 0) {
                 System.arraycopy(sourceBuffer, lineStart, sourceBuffer, 0,
                                  sourceEnd - lineStart);
                 sourceEnd -= lineStart;
                 sourceCursor -= lineStart;
                 lineStart = 0;
             } else {
                 char[] tmp = new char[sourceBuffer.length * 2];
                 System.arraycopy(sourceBuffer, 0, tmp, 0, sourceEnd);
                 sourceBuffer = tmp;
             }
         }
         int n = sourceReader.read(sourceBuffer, sourceEnd,
                                   sourceBuffer.length - sourceEnd);
         if (n < 0) {
             return false;
         }
         sourceEnd += n;
         return true;
     }
 
     /**
      * Return the current position of the scanner cursor.
      */
     public int getCursor() {
         return cursor;
     }
 
     /**
      * Return the absolute source offset of the last scanned token.
      */
     public int getTokenBeg() {
         return tokenBeg;
     }
 
     /**
      * Return the absolute source end-offset of the last scanned token.
      */
     public int getTokenEnd() {
         return tokenEnd;
     }
 
     /**
      * Return tokenEnd - tokenBeg
      */
     public int getTokenLength() {
         return tokenEnd - tokenBeg;
     }
      
      static double stringToNumber(String s, int start, int radix) {
          char digitMax = '9';
          int len = s.length();
          int end;
          double sum = 0.0;
          for (end=start; end < len; end++) {
              char c = s.charAt(end);
              int newDigit;
              if ('0' <= c && c < digitMax)
                  newDigit = c - '0';
              else
                  break;
              sum = sum*radix + newDigit;
          }
          if (start == end) {
              return NaN;
          }
          if (sum >= 9007199254740992.0) {
              if (radix == 10) {
                  /* If we're accumulating a decimal number and the number
                   * is >= 2^53, then the result from the repeated multiply-add
                   * above may be inaccurate.  Call Java to get the correct
                   * answer.
                   */
                  try {
                      return Double.valueOf(s.substring(start, end)).doubleValue();
                  } catch (NumberFormatException nfe) {
                      return NaN;
                  }
              }
          }
          return sum;
      }
      
      public static boolean isJSLineTerminator(int c)
      {
          // Optimization for faster check for eol character:
          // they do not have 0xDFD0 bits set
          if ((c & 0xDFD0) != 0) {
              return false;
          }
          return c == '\n' || c == '\r' || c == 0x2028 || c == 0x2029;
      }
 
     // stuff other than whitespace since start of line
     private boolean dirtyLine;
 
     // Set this to an initial non-null value so that the Parser has
     // something to retrieve even if an error has occurred and no
     // string is found.  Fosters one class of error, but saves lots of
     // code.
     private String string = "";
     private double number;
     private boolean isOctal;
 
     // delimiter for last string literal scanned
     private int quoteChar;
 
     private char[] stringBuffer = new char[128];
     private int stringBufferTop;
     private ObjToIntMap allStrings = new ObjToIntMap(50);
 
     // Room to backtrace from to < on failed match of the last - in <!--
     private final int[] ungetBuffer = new int[3];
     private int ungetCursor;
 
     private boolean hitEOF = false;
 
     private int lineStart = 0;
     private int lineEndChar = -1;
     int lineno;
 
     private String sourceString;
     private Reader sourceReader;
     private char[] sourceBuffer;
     private int sourceEnd;
 
     // sourceCursor is an index into a small buffer that keeps a
     // sliding window of the source stream.
     int sourceCursor;
 
     // cursor is a monotonically increasing index into the original
     // source stream, tracking exactly how far scanning has progressed.
     // Its value is the index of the next character to be scanned.
     int cursor;
 
     // Record start and end positions of last scanned token.
     int tokenBeg;
     int tokenEnd;
     
     public static final double NaN = Double.longBitsToDouble(0x7ff8000000000000L);
 }
