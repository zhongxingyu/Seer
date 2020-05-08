 package com.jclark.microxml.tree;
 
 import sun.nio.cs.StreamDecoder;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.Arrays;
 
 /**
  * Front end of MicroXML parsing.
  * This makes calls into {@link TokenHandler}.
  * @author James Clark
  */
 
 class Tokenizer<TExc extends Throwable> {
     private TokenHandler<TExc> handler;
 
     private LineMap lineMap;
 
     private char[] buf;
     // index into buf of next text character
     private int nextIndex = 0;
     // index past last available character in buf
     private int limit;
     // char 0 in buf corresponds to this position in the input
     private int bufStartPosition = 0;
     private Reader reader;
     // Make this twice the size of the default buffer for BufferedReader
     static final int DEFAULT_BUF_SIZE = 16 * 1024;
     // Minimum characters to read() at a time.
     static final int MIN_READ = 1024;
 
     enum MarkupCharType {
         /** Valid whitespace characters according to MicroXML */
         WHITESPACE(Flag.WHITESPACE),
         /** ASCII Form Feed */
         FF(Flag.WHITESPACE|Flag.FORBIDDEN),
         /** Forbidden characters with code points < 0x80 */
         ASCII_FORBIDDEN(Flag.FORBIDDEN),
         LT,
         GT,
         AMP,
         /** Single or double quote */
         QUOTE,
         EQUALS,
         SLASH,
         HASH,
         SEMI,
         BANG,
         MINUS(Flag.VALID_NAME|Flag.EXTENDED_NAME),
         // ASCII characters other than the above that are not treated as extended name characters
         DELIMITER,
         /** Valid name start characters */
         NAME_START(Flag.VALID_NAME_START|Flag.EXTENDED_NAME_START|Flag.VALID_NAME|Flag.EXTENDED_NAME),
         /** Valid ASCII name characters that are not name start characters */
         ASCII_NAME(Flag.VALID_NAME|Flag.EXTENDED_NAME),
         /**
          * Characters with code >= 0x80 that are valid name characters but not valid name start characters
          */
         UNICODE_NAME(Flag.VALID_NAME|Flag.EXTENDED_NAME|Flag.EXTENDED_NAME_START),
         /**
          *  Characters with code >= 0x80 that are forbidden by MicroXML
          */
         UNICODE_FORBIDDEN(Flag.FORBIDDEN|Flag.EXTENDED_NAME_START|Flag.EXTENDED_NAME),
         /**
          * Characters that MicroXML allows in documents but does not allow in names, but that we allow in names
          */
         EXTENDED_NAME(Flag.EXTENDED_NAME|Flag.EXTENDED_NAME_START);
 
         private static class Flag {
             // not allowed anywhere in a document according to MicroXML spec; CR is not included here
             static final int FORBIDDEN = 0x80;
             // allowed at the start of a name according to MicroXML spec
             static final int VALID_NAME = 0x100;
             // allowed as the second and subsequent characters of a name
             static final int VALID_NAME_START = 0x200;
             // allowed in a name for error handling purposed
             static final int EXTENDED_NAME = 0x400;
             // allowed at the start of a name for error handling purposes
             static final int EXTENDED_NAME_START = 0x800;
             // whitespace
             static final int WHITESPACE = 0x1000;
         }
 
         private int flags;
 
         private MarkupCharType() {
             flags = 0;
         }
 
         private MarkupCharType(int flags) {
             this.flags = flags;
         }
 
         final boolean isNameStart() {
             return (flags & Flag.VALID_NAME_START) != 0;
         }
 
         final boolean isNameChar() {
             return (flags & Flag.VALID_NAME) != 0;
         }
 
         final boolean isForbidden() {
             return (flags & Flag.FORBIDDEN) != 0;
         }
 
         final boolean isWhitespace() {
             return (flags & Flag.WHITESPACE) != 0;
         }
     }
 
     static private final MarkupCharType[] latin1CharTable = new MarkupCharType[256];
 
     // Initialize latin1CharTable;
     static {
         final String[] pairs = new String[] {
                 "\u0000\u001F\u007F\u007F",
                 "\u0021\u007E",
                 "\u0080\u009F",
                 "\u00A0\u00FF",
                 "\t\r\n ",
                 "AZaz__\u00C0\u00D6\u00D8\u00F6\u00F8\u00FF",
                 "09--..",
         };
         final MarkupCharType[] pairsType = new MarkupCharType[] {
                 MarkupCharType.ASCII_FORBIDDEN,
                 MarkupCharType.DELIMITER,
                 MarkupCharType.UNICODE_FORBIDDEN,
                 MarkupCharType.EXTENDED_NAME,
                 MarkupCharType.WHITESPACE,
                 MarkupCharType.NAME_START,
                 MarkupCharType.ASCII_NAME
         };
 
         for (int i = 0; i < pairs.length; i++) {
             for (int j = 0, n = pairs[i].length(); j < n; j += 2)
                 for (char ch = pairs[i].charAt(j), last = pairs[i].charAt(j + 1); ch <= last; ch++)
                     latin1CharTable[ch] = pairsType[i];
         }
 
         latin1CharTable[0xB7] = MarkupCharType.UNICODE_NAME;
         latin1CharTable[':'] = MarkupCharType.EXTENDED_NAME;
         latin1CharTable['$'] = MarkupCharType.EXTENDED_NAME;
 
         final String delims = "<>&\"'=/#;!-";
         final MarkupCharType[] delimType = new MarkupCharType[] {
                 MarkupCharType.LT,
                 MarkupCharType.GT,
                 MarkupCharType.AMP,
                 MarkupCharType.QUOTE,
                 MarkupCharType.QUOTE,
                 MarkupCharType.EQUALS,
                 MarkupCharType.SLASH,
                 MarkupCharType.HASH,
                 MarkupCharType.SEMI,
                 MarkupCharType.BANG,
                 MarkupCharType.MINUS
         };
 
         for (int i = 0; i < delims.length(); i++)
             latin1CharTable[delims.charAt(i)] = delimType[i];
     }
 
     static private final char REPLACEMENT_CHAR = 0xFFFD;
     static private final MarkupCharType REPLACEMENT_TYPE = MarkupCharType.NAME_START;
 
     Tokenizer(Reader reader, LineMap lineMap, TokenHandler<TExc> handler) {
         this(lineMap, handler);
         buf = new char[DEFAULT_BUF_SIZE];
         limit = 0;
         this.reader = reader;
     }
 
     Tokenizer(String source, LineMap lineMap, TokenHandler<TExc> handler) {
         this(lineMap, handler);
         if (source.length() <= DEFAULT_BUF_SIZE) {
             buf = source.toCharArray();
             limit = buf.length;
             reader = null;
         }
         else {
             reader = new StringReader(source);
             limit = 0;
         }
     }
 
     private Tokenizer(LineMap lineMap, TokenHandler<TExc> handler) {
         this.lineMap = lineMap;
         this.handler = handler;
     }
 
     void parse() throws TExc, IOException {
         while (hasNextChar()) {
             switch(buf[nextIndex]) {
             case '<':
                 parseLt();
                 break;
             case '&':
                 parseCharRef();
                 break;
             case '\r':
                 parseCr();
                 break;
             default:
                 parseText((char)0);
                 break;
             }
         }
         handler.end(bufStartPosition + limit);
     }
 
     enum TextType {
         NORMAL,
         FORBIDDEN,
         SPECIAL,
         SURROGATE1,
         SURROGATE2,
     }
 
     static private final TextType[] textTypeTable = new TextType[256];
     static {
         Arrays.fill(textTypeTable, TextType.NORMAL);
         Arrays.fill(textTypeTable, 0, 0x20, TextType.FORBIDDEN);
         Arrays.fill(textTypeTable, 0x7F, 0xA0, TextType.FORBIDDEN);
         textTypeTable['\t'] = TextType.NORMAL;
         String special = "\r\n<>&\"'";
         for (int i = 0; i < special.length(); i++)
             textTypeTable[special.charAt(i)] = TextType.SPECIAL;
     }
 
     private TextType textType(char c) {
         if (c <= 0xFF)
             return textTypeTable[c];
         if (c < 0xD800)
             return TextType.NORMAL;
         if (c < 0xE000)
             return c < 0xDC00 ? TextType.SURROGATE1 : TextType.SURROGATE2;
         if (c >= 0xFDD0 && (c <= 0xFDEF || c >= 0xFFFE))
             return TextType.FORBIDDEN;
         return TextType.NORMAL;
     }
 
     private void parseCr() throws TExc, IOException {
         if ((nextIndex + 1 < limit || fillBuf()) && buf[nextIndex + 1] == '\n') {
             lineMap.addLineStart(bufStartPosition + nextIndex + 2);
             handler.crLf(bufStartPosition + nextIndex);
             nextIndex += 2;
         }
         else {
             lineMap.addLineStart(bufStartPosition + nextIndex + 1);
             buf[nextIndex] = '\n';
             handler.literalChars(bufStartPosition + nextIndex, buf, nextIndex, 1);
             nextIndex++;
         }
     }
 
     private void parseText(char quote) throws TExc, IOException {
         int startIndex = nextIndex;
     loop:
         for (;;) {
             char c = buf[nextIndex];
             TextType t = textType(c);
             if (t != TextType.NORMAL) {
                 switch (c) {
                 case '\n':
                     lineMap.addLineStart(bufStartPosition + nextIndex + 1);
                     break;
                 case '\r':
                     if (nextIndex + 1 < limit) {
                         if (buf[nextIndex + 1] == '\n')
                             break loop;
                         buf[nextIndex] = '\n';
                         lineMap.addLineStart(bufStartPosition + nextIndex + 1);
                         break;
                     }
                     break loop;
                 case '>':
                     error(ParseError.UNESCAPED_LT);
                     break;
                 case '<':
                     if (quote == 0)
                         break loop;
                     error(ParseError.UNESCAPED_GT);
                     break;
                 case '&':
                     break loop;
                 default:
                     if (t == TextType.SPECIAL) {
                         if (c == quote)
                             break loop;
                         break;
                     }
                     if (t == TextType.SURROGATE1) {
                         if (nextIndex + 1 == limit) {
                            if (startIndex > 0 || nextIndex > startIndex)
                                 break loop; // deal with this in the next invocation of parseText
                             if (!fillBuf()) {
                                 error(ParseError.ISOLATED_SURROGATE);
                                 buf[nextIndex] = REPLACEMENT_CHAR;
                                 break;
                             }
                             startIndex = 0;
                         }
                         if (isSurrogate2(buf[nextIndex + 1])) {
                             if (isForbiddenSurrogatePair(buf[nextIndex], buf[nextIndex + 1])) {
                                 error(nextIndex, nextIndex + 2, ParseError.INVALID_CODE_POINT);
                                 buf[nextIndex] = REPLACEMENT_CHAR;
                                 buf[nextIndex + 1] = REPLACEMENT_CHAR;
                             }
                             ++nextIndex;
                             break;
                         }
                         error(ParseError.ISOLATED_SURROGATE);
                         buf[nextIndex] = REPLACEMENT_CHAR;
                         break;
                     }
                     if (t == TextType.FORBIDDEN) {
                         error(ParseError.INVALID_CODE_POINT);
                         buf[nextIndex] = REPLACEMENT_CHAR;
                         break;
                     }
                     if (t == TextType.SURROGATE2) {
                         error(ParseError.ISOLATED_SURROGATE);
                         buf[nextIndex] = REPLACEMENT_CHAR;
                         break;
                     }
                     break;
                 }
             }
             if (++nextIndex == limit)
                 break;
         }
         handler.literalChars(bufStartPosition + startIndex,
                              buf,
                              startIndex,
                              nextIndex - startIndex);
     }
 
 
 
     /*
     How to handle reparsing:
     Cannot give any errors except illegal chars before giving up.
 
     Have special reparsing routine that treats it as text.
     Need to generate ignoredLf
     Cannot have & or >.
     Handle illegal chars in first parse.
     Replacement chars parsed as replacement.
     FF treated as whitespace.
     Control characters treated as delimiters.
 
     Prescan to handle missing attribute closing quotes? If not followed by whitespace, /> or >
 
 
     When do we commit to treating it as a start-tag?
     See = or > or /.
     Need to handle <x foo bar baz="..."
     Otherwise if x<y and y<z
     Commit on =
     <x x="stuf
     <y y-="stuff
 
     */
 
     // Index of next character to be considered by getMarkup().
     private int markupIndex = -1;
 
     void parseLt() throws TExc, IOException {
         markupIndex = nextIndex + 1;
         try {
             MarkupCharType m = getMarkup();
             if (m.isNameStart())
                 parseStartTag();
             else if (m == MarkupCharType.SLASH)
                 parseEndTag();
             else if (m == MarkupCharType.BANG)
                 parseDecl();
             else
                 giveUp(m);
         }
         catch (MarkupException e) {
             error(ParseError.UNESCAPED_LT);
             reparseAsText();
         }
         nextIndex = markupIndex;
     }
 
     private final char[] charRef1 = new char[1];
     private final char[] charRef2 = new char[2];
 
     private void parseCharRef() throws TExc, IOException {
         markupIndex = nextIndex + 1;
         try {
             MarkupCharType m = getMarkup();
             if (m == MarkupCharType.HASH)
                 parseNumericCharRef();
             else
                 parseNamedCharRef(m);
         }
         catch (MarkupException e) {
             error(ParseError.UNESCAPED_AMP);
             reparseAsText();
         }
         nextIndex = markupIndex;
     }
 
     private void parseNamedCharRef(MarkupCharType m) throws TExc, IOException, MarkupException {
         if (!m.isNameStart())
             giveUp(m);
         do {
             m = getMarkup();
         } while (m.isNameChar());
         if (m != MarkupCharType.SEMI)
             giveUp(m);
         int ch = lookupCharName(nextIndex + 1, markupIndex - nextIndex - 2);
         if (ch < 0) {
             error(nextIndex + 1, markupIndex - 1, ParseError.UNKNOWN_CHAR_NAME);
             // Maybe better to treat the whole reference as text
             ch = REPLACEMENT_CHAR;
         }
         charRef1[0] = (char)ch;
         handler.charRef(nextIndex, markupIndex - nextIndex, charRef1);
     }
 
     private int lookupCharName(int offset, int length) {
         switch (length) {
         case 2:
             if (buf[offset + 1] == 't') {
                 if (buf[offset] == 'l')
                     return '<';
                 if (buf[offset] == 'g')
                     return '>';
             }
             break;
         case 3:
             if (buf[offset] == 'a' && buf[offset + 1] == 'm' && buf[offset + 2] == 'p')
                 return '&';
         case 4:
             if (buf[offset + 2] == 'o') {
                 if (buf[offset] == 'q') {
                     if (buf[offset + 1] == 'u' && buf[offset + 3] == 't')
                         return '"';
                 }
                 else if (buf[offset] == 'a') {
                     if (buf[offset + 1] == 'p' && buf[offset + 3] == 's')
                         return '\'';
                 }
             }
             break;
         }
         return -1;
     }
 
     private void parseNumericCharRef() throws TExc, IOException, MarkupException {
         char c = getMarkupChar();
         if (c != 'x')
             giveUp(c);
         c = getMarkupChar();
         int codePoint = hexWeight(c);
         if (codePoint < 0)
             giveUp(c);
         for (;;) {
             c = getMarkupChar();
             if (c == ';')
                 break;
             int weight = hexWeight(c);
             if (weight < 0)
                 giveUp(c);
             // Avoid overflow
             if (codePoint <= Character.MAX_CODE_POINT)
                 codePoint = codePoint * 16 + weight;
         }
         if (codePoint > Character.MAX_CODE_POINT) {
             error(nextIndex + 3, markupIndex - 1, ParseError.REF_CODE_POINT_TOO_BIG);
             codePoint = REPLACEMENT_CHAR;
         }
         else if (isCodePointForbidden(codePoint)) {
             error(nextIndex + 3, markupIndex - 1, ParseError.FORBIDDEN_CODE_POINT_REF);
             if (replaceCodePoint(codePoint))
                 codePoint = REPLACEMENT_CHAR;
         }
         char[] ref;
         if (codePoint <= 0xFFFF) {
             charRef1[0] = (char)codePoint;
             ref = charRef1;
         }
         else {
             Character.toChars(codePoint, charRef2, 0);
             ref = charRef2;
         }
         handler.charRef(bufStartPosition + nextIndex, bufStartPosition + markupIndex - nextIndex, ref);
     }
 
     static private boolean isCodePointForbidden(int codePoint) {
         if (codePoint <= 0xFF)
             return codePoint == 0xD || latin1CharTable[codePoint].isForbidden();
         if (codePoint < 0xD800)
             return false;
         if (codePoint < 0xE000)
             return true;
         if (codePoint < 0xFDD0)
             return false;
         if (codePoint < 0xFDF0)
             return true;
         return (codePoint & 0xFFFE) == 0xFFFE;
     }
 
     static private int hexWeight(char c) {
         switch (c) {
         case '0':
             return 0;
         case '1':
             return 1;
         case '2':
             return 2;
         case '3':
             return 3;
         case '4':
             return 4;
         case '5':
             return 5;
         case '6':
             return 6;
         case '7':
             return 7;
         case '8':
             return 8;
         case '9':
             return 9;
         case 'A':
         case 'a':
             return 0xA;
         case 'B':
         case 'b':
             return 0xB;
         case 'C':
         case 'c':
             return 0xC;
         case 'D':
         case 'd':
             return 0xD;
         case 'E':
         case 'e':
             return 0xE;
         case 'F':
         case 'f':
             return 0xF;
         }
         return -1;
     }
 
     private void parseStartTag() throws TExc, IOException, MarkupException {
         // At this point, we have already got the name start
         assert markupIndex == nextIndex + 2;
         MarkupCharType m = getMarkup();
         while (m.isNameChar())
             m = getMarkup();
         String name = new String(buf, nextIndex + 1, markupIndex - nextIndex - 2);
         boolean opened = false;
         try {
             for (;;) {
                 while (m.isWhitespace())
                     m = getMarkup();
                 if (m == MarkupCharType.SLASH) {
                     m = getMarkup();
                     if (m != MarkupCharType.GT)
                         giveUp(m);
                     if (!opened) {
                         opened = true;
                         handler.startTagOpen(bufStartPosition + nextIndex, name);
                     }
                     handler.emptyElementTagClose(bufStartPosition + markupIndex);
                     nextIndex = markupIndex;
                     return;
                 }
                 if (m == MarkupCharType.GT) {
                     if (!opened) {
                         opened = true;
                         handler.startTagOpen(bufStartPosition + nextIndex, name);
                     }
                     handler.startTagClose(bufStartPosition + markupIndex);
                     nextIndex = markupIndex;
                     return;
                 }
                 if (!m.isNameStart())
                     giveUp(m);
                 int attrNamePosition = bufStartPosition + markupIndex - 1;
                 int attrNameLength = 1;
                 for (;;) {
                     m = getMarkup();
                     if (!m.isNameChar())
                         break;
                     attrNameLength++;
                 }
                 String attrName = new String(buf, markupIndex - attrNameLength - 1, attrNameLength);
                 while (m.isWhitespace())
                     m = getMarkup();
                 if (m != MarkupCharType.EQUALS)
                     giveUp(m);
                 if (!opened) {
                     opened = true;
                     handler.startTagOpen(bufStartPosition + nextIndex, name);
                     nextIndex = attrNamePosition - bufStartPosition;
                 }
                 m = getMarkup();
                 while (m.isWhitespace())
                     m = getMarkup();
                 if (m != MarkupCharType.QUOTE)
                     giveUp(m);
                 handler.attributeOpen(attrNamePosition, bufStartPosition + markupIndex, attrName);
                 // switch to text mode
                 nextIndex = markupIndex;
                 if (!parseAttributeValue(buf[markupIndex - 1])) {
                     handler.startTagClose(bufStartPosition + nextIndex);
                     return;
                 }
                 // switch back to markup mode
                 markupIndex = nextIndex;
                 // TODO if this gets EOF it will produce a misleading error
                 m = getMarkup();
                 // need to check for space before next attribute
             }
         }
         catch (MarkupException e) {
             if (!opened)
                 throw e;
             error(markupIndex - 1, markupIndex, ParseError.MISSING_QUOTE);
             handler.startTagClose(nextIndex);
             reparseAsText();
         }
     }
 
     private boolean parseAttributeValue(char quote) throws TExc, IOException {
         while (hasNextChar()) {
             char c = buf[nextIndex];
             if (c == quote) {
                 ++nextIndex;
                 handler.attributeClose();
                 return true;
             }
             else if (c == '&')
                 parseCharRef();
             else if (c == '\r')
                 parseCr();
             else
                 parseText(quote);
         }
         error(ParseError.MISSING_QUOTE);
         return false;
     }
 
     private void parseEndTag() throws TExc, MarkupException, IOException {
         // At this point we just got the slash
         assert markupIndex == nextIndex + 2;
         MarkupCharType m = getMarkup();
         if (!m.isNameStart())
             giveUp(m);
         do {
             m = getMarkup();
         } while (m.isNameChar());
         String name = new String(buf, nextIndex + 2, markupIndex - nextIndex - 3);
         while (m.isWhitespace())
             m = getMarkup();
         if (m != MarkupCharType.GT)
             giveUp(m);
         handler.endTag(bufStartPosition + nextIndex, bufStartPosition + markupIndex, name);
     }
 
     private void parseDecl() throws TExc, MarkupException, IOException {
         MarkupCharType m = getMarkup();
         if (m != MarkupCharType.MINUS)
             giveUp(m);
         m = getMarkup();
         if (m != MarkupCharType.MINUS)
             giveUp(m);
         // TODO Avoid buffering up the whole comment
         try {
             for (;;) {
                 m = getMarkup();
                 if (m == MarkupCharType.MINUS) {
                     m = getMarkup();
                     if (m == MarkupCharType.MINUS) {
                         m = getMarkup();
                         if (m == MarkupCharType.GT)
                             break;
                         error(markupIndex - 1, markupIndex, ParseError.DOUBLE_MINUS_IN_COMMENT);
                     }
                 }
             }
         }
         catch (MarkupException e) {
             // Cannot reparseAsText because the comment may contain < and &
             error(nextIndex, markupIndex, ParseError.UNTERMINATED_COMMENT);
         }
     }
 
     private void giveUp(MarkupCharType m) throws MarkupException {
         // We cannot back up past arbitrary characters because this might give us a double error.
         // So only backup past the characters we have to.
         switch (m) {
         case LT:
         case GT:
         case AMP:
             --markupIndex;
         }
         throw new MarkupException();
     }
 
     private void giveUp(char c) throws MarkupException {
         switch (c) {
         case '<':
         case '>':
         case '&':
             --markupIndex;
         }
         throw new MarkupException();
     }
 
     // Reparse from [nextIndex,markupIndex) as text
     // May get an exception from making calls on handler.
     private void reparseAsText() throws TExc {
         // TODO handle mapping lineEndings to LF including generating ignoredLf
         // If we end with a CR, then there should be a following LF in the buffer (unless there's no LF)
         // Checking of invalid chars has already been done.
         handler.literalChars(bufStartPosition + nextIndex, buf, nextIndex, markupIndex - nextIndex);
     }
 
     static class MarkupException extends Exception {
     }
 
     private char getMarkupChar() throws TExc, IOException, MarkupException {
         getMarkup();
         return buf[markupIndex - 1];
     }
 
     // handle lineMap, illegal chars
     MarkupCharType getMarkup() throws IOException, TExc, MarkupException {
         if (markupIndex == limit) {
             if (!markupFillBuf())
                 throw new MarkupException();
         }
         char ch = buf[markupIndex++];
         if (ch <= 0xFF) {
             MarkupCharType t = latin1CharTable[ch];
             if (t.isForbidden()) {
                 error(markupIndex - 1, markupIndex, ParseError.INVALID_CODE_POINT);
                 if (replaceCodePoint(ch)) {
                     buf[markupIndex] = REPLACEMENT_CHAR;
                     return REPLACEMENT_TYPE;
                 }
             }
             else if (ch == '\n'
                      || (ch == '\r' && ((markupIndex == limit && !markupFillBuf()) || buf[markupIndex] != '\n')))
                 lineMap.addLineStart(bufStartPosition + markupIndex + 1);
             return t;
         }
         switch (ch >>> 12) {
         case 0:
             if (ch == 0x37E)
                 return MarkupCharType.EXTENDED_NAME;
             if (0x300 <= ch && ch <= 0x36F)
                 return MarkupCharType.UNICODE_NAME;
             break;
         case 0x2:
             if (ch >= 0x2190) {
                if (ch < 0x2C00 || ch > 0x2FEF)
                  return MarkupCharType.EXTENDED_NAME;
             }
             else if (ch < 0x2070) {
                 switch (ch) {
                 case 0x200C:
                 case 0x200D:
                     break;
                 case 0x203F:
                 case 0x2040:
                     return MarkupCharType.UNICODE_NAME;
                 default:
                     return MarkupCharType.EXTENDED_NAME;
                 }
             }
             break;
         case 0x3:
             if (ch == 0x3000)
                 return MarkupCharType.EXTENDED_NAME;
             break;
         case 0xD:
             if (ch >= 0xD800)
                 return handleSurrogate(ch);
             break;
         case 0xE:
             return MarkupCharType.EXTENDED_NAME;
         case 0xF:
             if (ch < 0xF900)
                 return MarkupCharType.EXTENDED_NAME;
             if (ch >= 0xFDD0 && (ch <= 0xFDEF || ch >= 0xFFFE)) {
                 error(markupIndex - 1, markupIndex, ParseError.INVALID_CODE_POINT);
                 if (replaceCodePoint(ch)) {
                     buf[markupIndex] = REPLACEMENT_CHAR;
                     return REPLACEMENT_TYPE;
                 }
                 return MarkupCharType.UNICODE_FORBIDDEN;
             }
             break;
         }
         return MarkupCharType.NAME_START;
     }
 
     private boolean replaceCodePoint(int codePoint) {
         return true;
     }
 
     private MarkupCharType handleSurrogate(char ch) throws TExc, IOException {
         if (isSurrogate2(ch)) {
             if (!isSurrogate1(buf[markupIndex - 2])) {
                 error(markupIndex - 1, markupIndex, ParseError.ISOLATED_SURROGATE);
                 if (replaceCodePoint(ch)) {
                     buf[markupIndex - 1] = REPLACEMENT_CHAR;
                     return REPLACEMENT_TYPE;
                 }
                 return MarkupCharType.UNICODE_FORBIDDEN;
             }
             return MarkupCharType.UNICODE_NAME;
         }
         char ch2;
         if ((markupIndex == limit && !markupFillBuf()) || !isSurrogate2(ch2 = buf[markupIndex])) {
             error(markupIndex - 1, markupIndex, ParseError.ISOLATED_SURROGATE);
             if (replaceCodePoint(ch)) {
                 buf[markupIndex - 1] = REPLACEMENT_CHAR;
                 return REPLACEMENT_TYPE;
             }
             return MarkupCharType.UNICODE_FORBIDDEN;
         }
         // We have a surrogatePair.
         if (isForbiddenSurrogatePair(ch, ch2))
             return MarkupCharType.UNICODE_FORBIDDEN;
         // Test for code points in the range F0000 and 10FFFF
         // Equivalent top bits of hi surrogate must be 110110111
         if ((ch & 0xFF80) == 0xDB80)
             return MarkupCharType.EXTENDED_NAME;
         return MarkupCharType.NAME_START;
     }
 
     boolean markupFillBuf() throws IOException {
         int n = markupIndex - nextIndex;
         boolean ret = fillBuf();
         markupIndex = nextIndex + n;
         return ret;
     }
 
     // keep characters between nextIndex and limit
     boolean fillBuf() throws IOException {
         if (reader == null)
             return false;
         int nKeep = limit - nextIndex;
         if (buf.length - nKeep >= MIN_READ) {
             // No need to resize
             // If there's a small amount left over, then always copy to the beginning of the buffer
             if (nKeep < 8) {
                 for (int i = 0; i < nKeep; i++)
                     buf[i] = buf[nextIndex + i];
                 bufStartPosition += nextIndex;
                 nextIndex = 0;
                 limit = nKeep;
             }
             // Otherwise if there's enough space in the buffer, avoid copying
             else if (buf.length - limit < MIN_READ) {
                 System.arraycopy(buf, nextIndex, buf, 0, nKeep);
                 bufStartPosition += nextIndex;
                 limit = nKeep;
                 nextIndex = 0;
             }
         }
         else {
             char[] newBuf = new char[buf.length * 2];
             if (nKeep > 0)
                 System.arraycopy(buf, nextIndex, newBuf, 0, nKeep);
             limit = nKeep;
             bufStartPosition += nextIndex;
             nextIndex = 0;
             buf = newBuf;
         }
         assert limit < buf.length;
         int nRead = reader.read(buf, limit, buf.length - limit);
         assert nRead != 0;
         if (nRead < 0) {
             reader = null;
             return false;
         }
         limit += nRead;
         return true;
     }
 
     private boolean hasNextChar() throws IOException {
         return nextIndex < limit || fillBuf();
     }
 
     private static boolean isSurrogate1(char ch) {
         return Character.isHighSurrogate(ch);
     }
 
     private static boolean isSurrogate2(char ch) {
         return Character.isLowSurrogate(ch);
     }
 
     private static boolean isForbiddenSurrogatePair(char ch1, char ch2) {
         return (ch2 & 0x3FE) == 0x3FE && (ch1 & 0x3F) == 0x3F;
     }
 
     void error(ParseError err, Object... args) throws TExc {
         handler.error(bufStartPosition + nextIndex,
                       bufStartPosition + (nextIndex == limit ? nextIndex : nextIndex + 1),
                       err,
                       args);
     }
 
     void error(int startIndex, int endIndex, ParseError err, Object... args) throws TExc {
         handler.error(bufStartPosition + startIndex, bufStartPosition + endIndex, err, args);
     }
 }
