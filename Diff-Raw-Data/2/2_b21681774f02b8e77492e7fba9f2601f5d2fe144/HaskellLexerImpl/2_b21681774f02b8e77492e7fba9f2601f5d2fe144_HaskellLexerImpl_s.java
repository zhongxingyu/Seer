 package ideah.lexer;
 
 import java.util.*;
 
 final class HaskellLexerImpl implements HaskellTokenTypes {
 
     private static final String SPECIALS = "(),;[]`{}";
     private static final String ASC_SYMBOLS = "!#$%&*+./<=>?@\\^|-~:";
     private static final Set<String> KEYWORDS = new HashSet<String>(Arrays.asList(
         "case", "class", "data", "default", "deriving", "do", "else",
         "foreign", "if", "import", "in", "infix", "infixl", "infixr",
         "instance", "let", "module", "newtype", "of", "then", "type", "where", "_"
     ));
     private static final Set<String> RESERVED_OPS = new HashSet<String>(Arrays.asList(
         "..", ":", "::", "=", "\\", "|", "<-", "->", "@", "~", "=>"
     ));
     private static final List<String> ESCAPES = Arrays.asList(
         "NUL", "SOH", "STX", "ETX", "EOT", "ENQ", "ACK",
         "BEL", "BS", "HT", "LF", "VT", "FF", "CR", "SO", "SI", "DLE",
         "DC1", "DC2", "DC3", "DC4", "NAK", "SYN", "ETB", "CAN",
         "EM", "SUB", "ESC", "FS", "GS", "RS", "US", "SP", "DEL"
     );
 
     private final LookaheadBuffer la;
 
     HaskellLexerImpl() {
         la = new LookaheadBuffer(3);
     }
 
     public void init(CharSequence source, int startIndex, int endIndex) {
         la.init(source, startIndex, endIndex);
     }
 
     private static void append(StringBuilder buf, int c) {
         buf.append((char) c);
     }
 
     private boolean is2(int c1, int c2, StringBuilder buf) {
         if (la.peek(0) == c1 && la.peek(1) == c2) {
             if (buf != null) {
                 append(buf, c1);
                 append(buf, c2);
             }
             la.next();
             la.next();
             return true;
         } else {
             return false;
         }
     }
 
     private boolean isCommentStart(StringBuilder buf) {
         return is2('{', '-', buf);
     }
 
     private boolean isCommentEnd(StringBuilder buf) {
         return is2('-', '}', buf);
     }
 
     private HaskellToken mlComment(int start) {
         StringBuilder buf = new StringBuilder();
         if (isCommentStart(buf)) {
             int count = 1;
             while (true) {
                 if (isCommentStart(buf)) {
                     count++;
                 } else if (isCommentEnd(buf)) {
                     count--;
                     if (count <= 0)
                         break;
                 } else {
                     int c = la.peek();
                     if (c < 0)
                         break;
                     append(buf, c);
                     la.next();
                 }
             }
             return new HaskellToken(ML_COMMENT, buf.toString(), start);
         } else {
             return null;
         }
     }
 
     private static boolean isAscSmall(int c) {
         return c >= 'a' && c <= 'z';
     }
 
     private static boolean isUniSmall(int c) {
         if (c < 0)
             return false;
         return Character.isLowerCase((char) c);
     }
 
     private static boolean isSmall(int c) {
         return isAscSmall(c) || isUniSmall(c) || c == '_';
     }
 
     private static boolean isAscLarge(int c) {
         return c >= 'A' && c <= 'Z';
     }
 
     private static boolean isUniLarge(int c) {
         if (c < 0)
             return false;
         char ch = (char) c;
         return Character.isUpperCase(ch) || Character.isTitleCase(ch);
     }
 
     private static boolean isLarge(int c) {
         return isAscLarge(c) || isUniLarge(c);
     }
 
     private static boolean isUniSymbol(int c) {
         return false; // todo: unicode symbols
     }
 
     private static boolean isSpecial(int c) {
         return SPECIALS.indexOf(c) >= 0;
     }
 
     private static boolean isSymbol(int c) {
         if (ASC_SYMBOLS.indexOf(c) >= 0) {
             return true;
         } else {
             return isUniSymbol(c) && !(isSpecial(c) || "_\"'".indexOf(c) >= 0);
         }
     }
 
     private static boolean isAscDigit(int c) {
         return c >= '0' && c <= '9';
     }
 
     private static boolean isUniDigit(int c) {
         if (c < 0)
             return false;
         return Character.isDigit((char) c);
     }
 
     private static boolean isDigit(int c) {
         return isAscDigit(c) || isUniDigit(c);
     }
 
     private HaskellToken comment(int start) {
         StringBuilder buf = new StringBuilder();
         if (is2('-', '-', buf)) {
             while (true) {
                 if (!la.match('-'))
                     break;
                 buf.append('-');
             }
             if (isSymbol(la.peek())) {
                 StringR rest = restSym(buf.toString(), start);
                 if (rest.reserved != null) {
                     return rest.reserved;
                 } else {
                     return new HaskellToken(VAR_SYM, rest.str, start);
                 }
             } else {
                 while (true) {
                     int c = la.peek();
                     if (c < 0)
                         break;
                     if (is2('\r', '\n', buf)) {
                         break;
                     } else if (c == '\r' || c == '\n' || c == '\f') {
                         append(buf, c);
                         la.next();
                         break;
                     } else {
                         append(buf, c);
                         la.next();
                     }
                 }
                 return new HaskellToken(COMMENT, buf.toString(), start);
             }
         } else {
             return null;
         }
     }
 
     private static final class StringR {
 
         final String str;
         final HaskellToken reserved;
 
         private StringR(String str, HaskellToken reserved) {
             this.str = str;
             this.reserved = reserved;
         }
     }
 
     private String restId(int c) {
         StringBuilder buf = new StringBuilder();
         append(buf, c);
         la.next();
         while (true) {
             int c1 = la.peek();
            if (isSmall(c1) || isLarge(c1) || c1 == '\'') {
                 append(buf, c1);
                 la.next();
             } else {
                 break;
             }
         }
         return buf.toString();
     }
 
     private StringR varId(int coords) {
         int c = la.peek();
         if (isSmall(c)) {
             String str = restId(c);
             if (KEYWORDS.contains(str)) {
                 return new StringR(str, new HaskellToken(KEYWORD, str, coords));
             } else {
                 return new StringR(str, null);
             }
         } else {
             return null;
         }
     }
 
     private String conId() {
         int c = la.peek();
         if (isLarge(c)) {
             return restId(c);
         } else {
             return null;
         }
     }
 
     private StringR restSym(String pred, int coords) {
         StringBuilder buf = new StringBuilder();
         buf.append(pred);
         while (true) {
             int c1 = la.peek();
             if (isSymbol(c1)) {
                 append(buf, c1);
                 la.next();
             } else {
                 break;
             }
         }
         String str = buf.toString();
         if (RESERVED_OPS.contains(str)) {
             return new StringR(str, new HaskellToken(KEY_OP, str, coords));
         } else {
             return new StringR(str, null);
         }
     }
 
     private StringR restSym(int c, int coords) {
         la.next();
         String pred = String.valueOf((char) c);
         return restSym(pred, coords);
     }
 
     private StringR varSym(int coords) {
         int c = la.peek();
         if (isSymbol(c) && c != ':') {
             HaskellToken comment = comment(coords);
             if (comment != null) {
                 if (comment.type == COMMENT || comment.type == KEY_OP) {
                     return new StringR(null, comment);
                 } else {
                     return new StringR(comment.text, null);
                 }
             } else {
                 return restSym(c, coords);
             }
         } else {
             return null;
         }
     }
 
     private StringR conSym(int coords) {
         int c = la.peek();
         if (c == ':') {
             return restSym(c, coords);
         } else {
             return null;
         }
     }
 
     private static final class RestId {
 
         final StringR str;
         final HaskellTokenType kind;
 
         private RestId(StringR str, HaskellTokenType kind) {
             this.str = str;
             this.kind = kind;
         }
     }
 
     private RestId idEnd(int coords) {
         StringR varId = varId(coords);
         if (varId != null) {
             return new RestId(varId, VAR_ID);
         } else {
             StringR varSym = varSym(coords);
             if (varSym != null) {
                 return new RestId(varSym, VAR_SYM);
             } else {
                 StringR conSym = conSym(coords);
                 if (conSym != null) {
                     return new RestId(conSym, CON_SYM);
                 } else {
                     return null;
                 }
             }
         }
     }
 
     private static HaskellToken consToken(StringBuilder buf, int start) {
         return new HaskellToken(CON_ID, buf.toString(), start);
     }
 
     private List<HaskellToken> id(int start) {
         StringBuilder buf = new StringBuilder();
         int count = 0;
         while (true) {
             int dotPos;
             if (count > 0) {
                 dotPos = la.getCoords();
                 if (!la.match('.')) {
                     return Collections.singletonList(consToken(buf, start));
                 }
             } else {
                 dotPos = start;
             }
             String conId = conId();
             if (conId == null) {
                 int restPos = la.getCoords();
                 RestId restId = idEnd(restPos);
                 if (restId == null || restId.str.reserved != null) {
                     List<HaskellToken> tokens = new ArrayList<HaskellToken>(3);
                     if (count > 0) {
                         tokens.add(consToken(buf, start));
                         if (restId == null) {
                             StringR restSym = restSym('.', dotPos);
                             if (restSym.reserved != null) {
                                 tokens.add(restSym.reserved);
                             } else {
                                 tokens.add(new HaskellToken(VAR_SYM, restSym.str, dotPos));
                             }
                         } else {
                             tokens.add(new HaskellToken(VAR_SYM, ".", dotPos));
                             tokens.add(restId.str.reserved);
                         }
                     } else {
                         if (restId != null) {
                             tokens.add(restId.str.reserved);
                         }
                     }
                     return tokens;
                 } else {
                     if (count > 0) {
                         buf.append('.');
                     }
                     buf.append(restId.str.str);
                     return Collections.singletonList(new HaskellToken(restId.kind, buf.toString(), start));
                 }
             } else {
                 if (count > 0) {
                     buf.append('.');
                 }
                 buf.append(conId);
                 count++;
             }
         }
     }
 
     private String decimal(int digit) {
         StringBuilder buf = new StringBuilder();
         append(buf, digit);
         decimal(buf);
         return buf.toString();
     }
 
     private boolean decimal(StringBuilder buf) {
         int count = 0;
         while (true) {
             int c1 = la.peek();
             if (!isDigit(c1))
                 break;
             append(buf, c1);
             la.next();
             count++;
         }
         return count > 0;
     }
 
     private boolean exponent(StringBuilder buf) {
         int c = la.peek();
         if (c == 'e' || c == 'E') {
             la.next();
             append(buf, c);
             int c1 = la.peek();
             if (c1 == '+' || c1 == '-') {
                 la.next();
                 append(buf, c1);
                 return decimal(buf);
             } else {
                 return decimal(buf);
             }
         } else {
             return true;
         }
     }
 
     private boolean octal(StringBuilder buf) {
         int count = 0;
         while (true) {
             int c = la.peek();
             if (c >= '0' && c <= '7') {
                 append(buf, c);
                 la.next();
                 count++;
             } else {
                 break;
             }
         }
         return count > 0;
     }
 
     private boolean hex(StringBuilder buf) {
         int count = 0;
         while (true) {
             int c = la.peek();
             if (c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F') {
                 append(buf, c);
                 la.next();
                 count++;
             } else {
                 break;
             }
         }
         return count > 0;
     }
 
     private HaskellToken number(int start) {
         StringBuilder buf = new StringBuilder();
         if (is2('0', 'o', buf) || is2('0', 'O', buf)) {
             if (!octal(buf)) {
                 return new HaskellToken(ERROR_NUMBER, buf.toString(), start);
             }
             return new HaskellToken(OCTAL, buf.toString(), start);
         } else if (is2('0', 'x', buf) || is2('0', 'X', buf)) {
             if (!hex(buf)) {
                 return new HaskellToken(ERROR_NUMBER, buf.toString(), start);
             }
             return new HaskellToken(HEX, buf.toString(), start);
         } else {
             int c = la.peek();
             if (isDigit(c)) {
                 la.next();
                 String intPart = decimal(c);
                 buf.append(intPart);
                 boolean error = false;
                 if (la.match('.')) {
                     buf.append('.');
                     if (!decimal(buf)) {
                         error = true;
                     }
                 }
                 if (!exponent(buf)) {
                     error = true;
                 }
                 if (error) {
                     return new HaskellToken(ERROR_NUMBER, buf.toString(), start);
                 } else {
                     if (buf.length() == intPart.length()) {
                         return new HaskellToken(INTEGER, intPart, start);
                     } else {
                         return new HaskellToken(FLOAT, buf.toString(), start);
                     }
                 }
             } else {
                 return null;
             }
         }
     }
 
     private static boolean isWhitechar(int c) {
         return c >= 0 && c <= ' ';
     }
 
     private void skipSpaces(StringBuilder buf) {
         while (true) {
             int c = la.peek();
             if (isWhitechar(c)) {
                 la.next();
                 append(buf, c);
             } else {
                 break;
             }
         }
     }
 
     private HaskellToken whitespace(int start) {
         if (isWhitechar(la.peek())) {
             StringBuilder buf = new StringBuilder();
             skipSpaces(buf);
             return new HaskellToken(WHITESPACE, buf.toString(), start);
         } else {
             return null;
         }
     }
 
     private boolean escape(StringBuilder buf, boolean amp) {
         int c = la.peek();
         if (isWhitechar(c)) {
             // gap
             skipSpaces(buf);
             return la.match('\\');
         } else {
             if ("abfnrtv\\\"'".indexOf(c) >= 0 || (amp && c == '&')) {
                 // single-char escape
                 append(buf, c);
                 la.next();
                 return true;
             } else if (c == 'o') {
                 // octal escape
                 la.next();
                 return octal(buf);
             } else if (c == 'x') {
                 // hex escape
                 la.next();
                 return hex(buf);
             } else if (isDigit(c)) {
                 // decimal escape
                 la.next();
                 buf.append(decimal(c));
                 return true;
             } else if (c == '^') {
                 append(buf, c);
                 la.next();
                 int c1 = la.peek();
                 if (c1 >= 'A' && c1 <= 'Z' || "@[\\]^_".indexOf(c1) >= 0) {
                     append(buf, c1);
                     la.next();
                     return true;
                 } else {
                     return false;
                 }
             } else {
                 // control escapes
                 if (c >= 'A' && c <= 'Z') {
                     for (String escape : ESCAPES) {
                         boolean match = true;
                         for (int j = 0; j < escape.length(); j++) {
                             if (la.peek(j) != escape.charAt(j)) {
                                 match = false;
                                 break;
                             }
                         }
                         if (match) {
                             for (int j = 0; j < escape.length(); j++) {
                                 la.next();
                             }
                             buf.append(escape);
                             return true;
                         }
                     }
                 }
                 return false;
             }
         }
     }
 
     private HaskellToken string(int start) {
         if (la.match('"')) {
             StringBuilder buf = new StringBuilder();
             buf.append('"');
             boolean error = false;
             while (true) {
                 int c = la.peek();
                 if (c == '\\') {
                     buf.append('\\');
                     la.next();
                     if (!escape(buf, true)) {
                         error = true;
                     }
                 } else if (c >= ' ' && c != '"') {
                     append(buf, c);
                     la.next();
                 } else {
                     break;
                 }
 
             }
             if (!la.match('"')) {
                 error = true;
             } else {
                 buf.append('"');
             }
             if (error) {
                 return new HaskellToken(ERROR_STRING, buf.toString(), start);
             } else {
                 return new HaskellToken(STRING, buf.toString(), start);
             }
         } else {
             return null;
         }
     }
 
     private HaskellToken chr(int start) {
         if (la.match('\'')) {
             StringBuilder buf = new StringBuilder();
             buf.append('\'');
             int c = la.peek();
             boolean error = false;
             if (c == '\\') {
                 buf.append('\\');
                 la.next();
                 if (!escape(buf, false)) {
                     error = true;
                 }
             } else if (c >= ' ' && c != '\'') {
                 append(buf, c);
                 la.next();
             } else {
                 error = true;
             }
             if (!la.match('\'')) {
                 error = true;
             } else {
                 buf.append('\'');
             }
             if (error) {
                 return new HaskellToken(ERROR_STRING, buf.toString(), start);
             } else {
                 return new HaskellToken(CHAR, buf.toString(), start);
             }
         } else {
             return null;
         }
     }
 
     private final Deque<HaskellToken> tokenQueue = new ArrayDeque<HaskellToken>();
 
     private void output(HaskellToken t) {
         tokenQueue.addLast(t);
     }
 
     private void output(List<HaskellToken> tokens) {
         for (HaskellToken token : tokens) {
             output(token);
         }
     }
 
     private boolean nextTokens() {
         if (la.eof())
             return false;
         int start = la.getCoords();
         HaskellToken t = whitespace(start);
         if (t != null) {
             output(t);
             return true;
         }
         t = comment(start);
         if (t != null) {
             output(t);
             return true;
         }
         t = mlComment(start);
         if (t != null) {
             output(t);
             return true;
         }
         List<HaskellToken> tokens = id(start);
         if (tokens.size() > 0) {
             output(tokens);
             return true;
         }
         t = number(start);
         if (t != null) {
             output(t);
             return true;
         }
         t = string(start);
         if (t != null) {
             output(t);
             return true;
         }
         t = chr(start);
         if (t != null) {
             output(t);
             return true;
         }
         int c = la.peek();
         la.next();
         String str = String.valueOf((char) c);
         if (isSpecial(c)) {
             output(new HaskellToken(specialType(c), str, start));
         } else {
             output(new HaskellToken(ERROR_UNDEFINED, str, start));
         }
         return true;
     }
 
     private static HaskellTokenType specialType(int c) {
         switch (c) {
         case '(': return L_PAREN;
         case ')': return R_PAREN;
         case '[': return L_SQUARE;
         case ']': return R_SQUARE;
         case '{': return L_CURLY;
         case '}': return R_CURLY;
         case ',': return COMMA;
         }
         return SPECIAL;
     }
 
     public HaskellToken nextToken() {
         if (tokenQueue.isEmpty()) {
             if (!nextTokens())
                 return null;
         }
         return tokenQueue.removeFirst();
     }
 
     public static void main(String[] args) {
         HaskellLexerImpl lexer = new HaskellLexerImpl();
         String str =
             "Xyzzy.Fyva.add\n" +
             "Xyzzy.Abba.+--\n" +
             "Xyzzy.Abba.+-->\n" +
             "Xyzzy.Abba.-->\n" +
             "Xyzzy.Abba.--\n" +
             "Xyzzy.Abba.\n" +
             "Xyzzy.Abba..\n" +
             "Xyzzy.Abba...\n" +
             "1234\n" +
             "1234.567\n" +
             "123.456E10\n" +
             "123.456E+10\n" +
             "123.456E-10\n" +
             "0123E10\n" +
             "\"xyzzy^A\"\n" +
             "\"xyzzy\\r\\nabba\"\n" +
             "'\\f'\n" +
             "'X'";
         lexer.init(str, 0, str.length());
         while (true) {
             HaskellToken t = lexer.nextToken();
             if (t == null)
                 break;
             System.out.println(t);
         }
     }
 }
