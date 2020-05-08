 package lexer;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PushbackInputStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import Utils.Util;
 
 public class Lexer {
 
     public Lexer(String file_name) throws FileNotFoundException, IOException {
         this.file_name = file_name;
         in = new PushbackInputStream(new FileInputStream(file_name), 2);
 
         str_to_type.put(";", TokenType.SEMICOLON);
         str_to_type.put(".", TokenType.POINT);
         str_to_type.put(",", TokenType.COMMA);
         str_to_type.put("(", TokenType.L_PARENTHESIS);
         str_to_type.put(")", TokenType.R_PARENTHESIS);
         str_to_type.put("[", TokenType.L_BRAKET);
         str_to_type.put("]", TokenType.R_BRAKET);
         str_to_type.put("{", TokenType.L_BRACE);
         str_to_type.put("}", TokenType.R_BRACE);
         str_to_type.put("/", TokenType.DIV);
         str_to_type.put("/=", TokenType.DIV_ASSIGN);
         str_to_type.put("+", TokenType.PLUS);
         str_to_type.put("-", TokenType.MINUS);
         str_to_type.put("*", TokenType.MUL);
         str_to_type.put("%", TokenType.MOD);
         str_to_type.put("+=", TokenType.PLUS_ASSIGN);
         str_to_type.put("-=", TokenType.MINUS_ASSIGN);
         str_to_type.put("*=", TokenType.MUL_ASSIGN);
         str_to_type.put("%=", TokenType.MOD_ASSIGN);
         str_to_type.put("!", TokenType.NOT);
         str_to_type.put("~", TokenType.NOT_B);
         str_to_type.put("|", TokenType.OR_B);
         str_to_type.put("&", TokenType.AND_B);
         str_to_type.put("^", TokenType.XOR);
         str_to_type.put("!=", TokenType.NE);
         str_to_type.put("&=", TokenType.AND_ASSIGN);
         str_to_type.put("|=", TokenType.OR_ASSIGN);
         str_to_type.put("==", TokenType.EQ);
         str_to_type.put("||", TokenType.OR);
         str_to_type.put("&&", TokenType.AND);
         str_to_type.put("=", TokenType.ASSIGN);
         str_to_type.put("++", TokenType.INC);
         str_to_type.put("--", TokenType.DEC);
         str_to_type.put(">>", TokenType.SHR);
         str_to_type.put("<<", TokenType.SHL);
         str_to_type.put(">>=", TokenType.SHR_ASSIGN);
         str_to_type.put("<<=", TokenType.SHL_ASSIGN);
         str_to_type.put(">=", TokenType.GE);
         str_to_type.put("<=", TokenType.LE);
         str_to_type.put(">", TokenType.GT);
         str_to_type.put("<", TokenType.LT);
         str_to_type.put(":", TokenType.COLON);
         str_to_type.put("->", TokenType.ARROW);
         str_to_type.put("\"", TokenType.STRING);
         str_to_type.put("\'", TokenType.CHAR_CONST);
         str_to_type.put("?", TokenType.QUESTION);
 
         key_words.add("break");
         key_words.add("char");
         key_words.add("continue");
         key_words.add("do");
         key_words.add("double");
         key_words.add("else");
         key_words.add("float");
         key_words.add("for");
         key_words.add("if");
         key_words.add("int");
         key_words.add("long");
         key_words.add("return");
         key_words.add("struct");
         key_words.add("void");
         key_words.add("while");
 
     }
 
     String file_name;
     private int p = 0;
     private int l = 0;
     PushbackInputStream in;
     char curr = 1;
     Token token;
     HashMap<String, TokenType> str_to_type = new HashMap<>();
     HashSet<String> key_words = new HashSet<>();
 
     char getNextChar() throws IOException {
         int res = in.read();
         if (res ==  -1){
             return 0;
         }
         return (char) res;
     }
 
     char getNextChar(int idx) throws IOException{
         byte[] arr = new byte[idx];
         in.read(arr, 0, arr.length);
         in.unread(arr);
         return (char)arr[idx - 1];
     }
 
     String buildStringWithCh() throws IOException{
         StringBuilder tmp = new StringBuilder();
         do {
             tmp.append(curr);
             curr = getNextChar();
         } while (Character.isLetterOrDigit(curr));
         in.unread(curr);
         return tmp.toString();
     }
 
     void throwException(String msg) throws LexerException {
         throw new LexerException(l + 1, p + 1, msg);
     }
 
     Token makeToken(Object val, String text, TokenType type){
         return new Token<>(p + 1, l + 1, val, text, type);
     }
 
     private void eatSpace() throws IOException{
         while ((curr = getNextChar()) != -1){
             if (curr == '\r' && getNextChar(1) == '\n'){
                 ++l;
                 p = 0;
                 curr = getNextChar();
                 continue;
             }
             if (curr == '\t') {
                 p += 4;
                 continue;
             }
             if (!Character.isSpaceChar(curr)) {
                 break;
             }
             ++p;
         }
     }
 
     String eatComments() throws LexerException, IOException{
         if (getNextChar(1) == '/'){
             ++l;
             p = 0;
             do {
                 curr = getNextChar();
             } while (curr != '\n' && curr != 0);
             if (curr == 0){
                 return "eof";
             }
         }
         else
             if (getNextChar(1) == '*'){
                 p+= 2;
                 curr = getNextChar();
                 while ((curr = getNextChar()) != '*' && getNextChar(1) != '/' && curr != 0) {
                     if (curr == '\n'){
                         l++;
                         p = 0;
                     }
                     p++;
                 }
                 p += 2;
                 if (curr == 0){
                     ++p;
                     throwException("Unclosed multiline comment");
                 }
                 curr = getNextChar();
             }
             else
             {
                 if (getNextChar(1) == '=') {
                     return "/=";
                 }
                 else {
                     return "/";
                 }
             }
         return "";
     }
 
     public Token getToken(){
         return token;
     }
 
     Token getIdent() throws IOException {
         String s = buildStringWithCh();
         TokenType type = TokenType.VAR;
         if (key_words.contains(s)) {
             type = TokenType.KEY_WORD;
         }
         p += s.length();
         return makeToken(s, s, type);
     }
 
     Token getHexNumber() throws LexerException, IOException {
         String tmp = buildStringWithCh();
         Integer val = 0;
         try {
             val = Integer.parseInt(tmp.substring(2),  16);
         }
         catch (Exception e){
             throwException("Incorrect hex nubmer");
         }
         p += tmp.length() + 1;
         return makeToken(val, tmp, TokenType.INT);
     }
 
     Token getOctNumber() throws LexerException, IOException{
         StringBuilder tmp = new StringBuilder();
         do {
             tmp.append(curr);
             curr = getNextChar();
         } while (Character.isDigit(curr));
         Integer val = 0;
         try {
             val = Integer.parseInt(tmp.toString(), 8);
         }
         catch (Exception e){
             throwException("Incorrect oct number");
         }
         p += tmp.length() + 1;
         return makeToken(val, tmp.toString(), TokenType.INT);
     }
 
     Token getNumber(String ... args) throws LexerException, IOException{
         boolean was_point = false;
         boolean was_exp = false;
         boolean was_sign = false;
         String bonus = "";
         if (args.length != 0) {
             bonus = args[0];
             was_point = true;
         }
         StringBuilder tmp = new StringBuilder(bonus);
         do {
             tmp.append(curr);
             curr = Character.toLowerCase(getNextChar());
             if (curr == '.') {
                 if (was_point){
                     throwException("Incorrect float number");
                 }
                 else {
                     was_point = true;
                 }
             }
             if (curr == 'e'){
                 char next_ch = getNextChar();
                 if (was_exp && next_ch != '-' && next_ch != '+' && !Character.isDigit(next_ch) || was_sign){
                     throwException("Incorrect float number");
                 }
                 was_sign = Util.isIn(curr, '+' , '-');
                 was_exp = true;
                 tmp.append(curr);
                 curr = next_ch;
             }
 
        } while (Character.isDigit(curr) || was_exp || (was_exp && Util.isIn(curr, '+', '-')));
         in.unread(curr);
         String s = tmp.toString();
         Double dval;
         Integer val;
         try {
             if (was_point || was_exp){
                 dval = Double.parseDouble(s);
                 return makeToken(dval, s, TokenType.FLOAT);
             }
             else {
                 val = Integer.parseInt(s,  10);
                 return makeToken(val, s, TokenType.INT);
             }
         } catch (Exception e){
             throwException("Incorrect number");
         }
         p += tmp.length() + 1;
         return null;
     }
 
     Token getString(char arg) throws LexerException, IOException{
         StringBuilder tmp = new StringBuilder();
         tmp.append(arg);
         StringBuilder val = new StringBuilder();
         while ((curr = getNextChar()) != arg && curr != 0){
             if (curr == '\n'){
                 throwException("Unclosed string const");
             }
             if (curr == '\\'){
                 char tail = getNextChar();
                 String res = "";
                 if (Character.isDigit(tail)){
                     char t; int i;
                     for(i = 1; i < 3; ++i) {
                         t = getNextChar();
                         if (Character.isDigit(t)) {
                             res += t;
                         }
                         else {
                             in.unread(t);
                             break;
                         }
                     }
                     tmp.append("\\").append(tail).append(res);
                     try {
                         val.append((char)Integer.parseInt(tail + res, 8));
                     }
                     catch (Exception e) {
                         throwException("Bad char const");
                     }
                     continue;
                 }
                 if (tail == 'x'){
                     char t; int i;
                     for(i = 1; i < 4; ++i) {
                         t = getNextChar();
                         if (Character.isLetterOrDigit(t)) {
                             res += t;
                         }
                         else {
                             in.unread(t);
                             break;
                         }
                     }
                     tmp.append("\\x").append(res);
                     try {
                         val.append((char)Integer.parseInt(res, 16));
                     }
                     catch (Exception e) {
                         throwException("Bad char const");
                     }
                     continue;
                 }
                 switch(tail){
                     case '\\': {res = "\\"; break;}
                     case '\"': {res = "\""; break; }
                     case '\'': {res = "\'"; break; }
                     case 'n':  {res = "\n"; break; }
                     case 'r': {res = "\r"; break; }
                     case 'b': {res = "\b"; break; }
                     case 't': {res = "\t"; break; }
                     case 'f': {res = "\f"; break; }
                 }
                 if (res.length() == 0) {
                     res += tail;
                     val.append(tail);
                 }
                 else {
                     val.append(res);
                 }
                 tmp.append("\\").append(tail);
 
             }
             else {
                 val.append(curr);
                 tmp.append(curr);
             }
         }
         if (curr == 0){
             throwException("Unclosed string const");
         }
 
         if (arg == '\'' && val.length() > 3) {
             throwException("Incorrect char const");
         }
         tmp.append(arg);
         p += tmp.length() + 1;
         return makeToken(val.toString(), tmp.toString(), str_to_type.get(arg + ""));
     }
 
     Token getOperation() throws IOException {
         Token tmpToken;
         String to_str = curr + "";
         char next_ch = getNextChar(1);
         if (next_ch != 0){
             String concat = "" + curr;
             if ((curr == '-' && next_ch == '>')||( next_ch == '=' && curr != next_ch)) {
                 p += 2;
                 concat += next_ch;
             }
             if (next_ch == curr){
                 if (Util.isIn(curr, '>', '<') && getNextChar(2) == '='){
                     concat = concat + next_ch + "=";
                     p += 3;
                     getNextChar();
                 }
                 else {
                     p += 2;
                     concat += next_ch;
                 }
             }
             tmpToken = makeToken(concat, concat, str_to_type.get(concat));
             if (concat.equals(to_str)) {
                 ++p;
             }
             else {
                 getNextChar();
             }
         }
         else
         {
             tmpToken = makeToken(to_str, to_str, str_to_type.get(to_str));
 
         }
         return tmpToken;
     }
 
     public boolean next() throws LexerException, IOException{
         if (curr == 0){
             token = makeToken("EOF", "EOF", TokenType.EOF);
             return false;
         }
         eatSpace();
         while (curr == '/'){
             String t = eatComments();
             if (!"".equals(t)){
                 if ("eof".equals(t)) {
                     return false;
                 }
                 token = makeToken(t, t, str_to_type.get(t));
                 if ("/=".equals(t)) {
                     getNextChar();
                 }
                 return true;
             }
             eatSpace();
         }
 
         if (Character.isLetter(curr)) {
             token = getIdent();
             return true;
         }
 
         if (Util.isIn(curr, ';', ',', '.', '[', ']', '{', '}', '(', ')', ':', '?')){
             if (curr == '.' && (Character.isDigit(getNextChar(1)) || Character.toLowerCase(getNextChar(1)) == 'e')) {
                 token = getNumber("0");
             }
             else {
                 token = makeToken(curr, curr + "", str_to_type.get(curr + ""));
             }
             return true;
         }
 
         if (Util.isIn(curr, '+', '-', '*', '%', '~', '!', '&', '|', '=', '>', '<', '^')){
             token = getOperation();
             return true;
         }
 
         if (curr == '\"' || curr == '\''){
             token = getString(curr);
             return true;
         }
 
        if (Character.isDigit(curr)){
            if (curr == '0'){
                if (Character.toLowerCase(getNextChar(1)) == 'x') {
                         token = getHexNumber();
                         return true;
                    }
                 else {
                     if (Character.isDigit(getNextChar(1))){
                         token = getOctNumber();
                         return true;
                     }
                  }
             }
             token = getNumber();
             return true;
        }
 
         curr = 0;
         token = makeToken("EOF", "EOF", TokenType.EOF);
         return false;
     }
 
 }
