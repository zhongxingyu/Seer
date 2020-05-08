 package org.libj.xquery.lexer;
 
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.ArrayList;
 
 import static org.libj.xquery.lexer.Token.t;
 import static org.libj.xquery.lexer.TokenType.*;
 import static java.lang.Character.isWhitespace;
 
 public class Lexer extends LL1Reader {
 
     public Lexer(Reader reader) throws IOException {
         super(reader);
         consume();
     }
 
     public Lexer(String text) throws IOException {
         this(new StringReader(text));
     }
 
     private ArrayList<Integer> stack = new ArrayList<Integer>();
     private static final int IN_CODE = 0;
     private static final int IN_NODE = 1;
     private static final int IN_APOS = 2;
     private static final int IN_QUOTE = 3;
     private static final int IN_TEXT = 4;
 
     public void enter(int x) {
         stack.add(x);
     }
     public void exit(int x) {
         if (stack.remove(stack.size() - 1) != x) {
             throw new RuntimeException("Wrong nested structure");
         }
     }
     private int currentMode() {
         return stack.isEmpty() ? IN_CODE : stack.get(stack.size()-1);
     }
     public boolean in(int x) {
         return currentMode() == x;
     }
 
 
     private void enterNode() {
         enter(IN_NODE);
     }
     private void exitNode() {
         exit(IN_NODE);
     }
 
     private void enterCode() {
         enter(IN_CODE);
     }
     private void exitCode() {
         exit(IN_CODE);
     }
     private boolean inNode() {
         return in(IN_NODE);
     }
     private boolean inCode() {
         return in(IN_CODE);
     }
 
     public Token nextToken() throws IOException {
         switch (currentMode()) {
             case IN_CODE:
                 return nextCodeToken();
             case IN_NODE:
                 return nextNodeToken();
             case IN_APOS:
                 return nextQuoteToken('\'');
             case IN_QUOTE:
                 return nextQuoteToken('"');
             case IN_TEXT:
                 return nextTextToken();
             default:
                 throw new RuntimeException("Not Implemented: "+currentMode());
         }
     }
 
     public Token nextCodeToken() throws IOException {
         while (!eof()) {
             switch (c) {
                 case ' ': case '\t': case '\n': case '\r':
                     skipWhitespaces();
                     continue;
                 case '(':
                     consume();
                     if (c == ':') {
                         consume();
                         endComment();
                         continue;
                     }
                     else {
                         return t(LPAREN, "(");
                     }
                 case ')':
                     consume();
                     return t(RPAREN, ")");
                 case '{':
                     enterCode();
                     consume();
                     return t(LBRACK, "{");
                 case '}':
                     exitCode();
                     consume();
                     return t(RBRACK, "}");
                 case '[':
                     consume();
                     return t(LBRACKET, "[");
                 case ']':
                     consume();
                     return t(RBRACKET, "]");
                 case '+':
                     consume();
                     return t(PLUS, "+");
                 case '-':
                     consume();
                     return t(MINUS, "-");
                 case '*':
                     consume();
                     return t(MULTIPLY, "*");
                 case '/':
                     consume();
                     return t(XPATH, "/");
                 case '@':
                     consume();
                     return t(ATTR_AT, "@");
                 case '=':
                     consume();
                     return t(EQ, "=");
                 case '!':
                     consume();
                     match('=');
                     return t(NE, "!=");
                 case ':':
                     consume();
                     match('=');
                     return t(ASSIGN, ":=");
                 case ',':
                     consume();
                     return t(COMMA, ",");
                 case ';':
                     consume();
                     return t(SEMI, ";");
                 case '\'':
                     return readSingleString();
                 case '"':
                     return readDoubleString();
                 case '$':
                     return readVariable();
                 case '<':
                     consume();
                     if (isQName()) {
                         return readTag();
                     }
                     else {
                         return t(LT, "<");
                     }
                 case '>':
                     consume();
                     if (c == '=') {
                         consume();
                         return t(GE, ">=");
                     }
                     else {
                         return t(GT, ">");
                     }
                 default:
                     if (isWordStart()) {
                         return readWord();
                     }
                     else if ('0' <= c && c <= '9') {
                         return readNumber();
                     }
                     else {
                         throw new LexerException(("invalid character: " + (char)c));
                     }
             }
         }
         return t(EOF, "<EOF>");
     }
 
     public Token nextNodeToken() throws IOException {
         skipWhitespaces();
         if (c == -1) {
             throw new RuntimeException("Broken XML");
         }
         if (isWordStart()) {
             return t(WORD, readRawWord());
         }
         else if (c == '=') {
             consume();
             return t(EQ, "=");
         }
         else if (c == '>') {
             consume();
             enter(IN_TEXT);
             return t(TAGOPENEND);
         }
         else if (c == '/') {
             consume();
             match('>');
             exitNode();
             return t(TAGCLOSE, "/>");
         }
         else if (c == '\'') {
             consume();
             enter(IN_APOS);
             return t(ATTROPEN, "'");
         }
         else if (c == '"') {
             consume();
             enter(IN_QUOTE);
             return t(ATTROPEN, "\"");
         }
         else {
             throw new LexerException(("invalid character: " + (char)c));
         }
     }
 
     private Token nextQuoteToken(char q) throws IOException {
         if (c == -1) {
             throw new RuntimeException("Broken attribute!");
         }
         if (c == '{') {
             consume();
             enterCode();
             return t(LBRACK, "{");
         } else if (c == q) {
             consume();
             exit(q == '"' ? IN_QUOTE : IN_APOS);
             return t(ATTRCLOSED, "'");
         } else {
             StringBuilder builder = new StringBuilder();
             while (c != -1 && c != q && c != '{') {
                 builder.append((char) c);
                 consume();
             }
             if (c == -1) {
                 throw new RuntimeException("Broken attribute!");
             }
             return t(TEXT, builder.toString());
         }
     }
 
     private Token nextTextToken() throws IOException {
         if (c == -1) {
             throw new RuntimeException("Broken XML!");
         }
         if (c == '{') {
             consume();
             enterCode();
             return t(LBRACK, "{");
         } else if (c == '<') {
             consume();
             if (c == '/') {
                 return readClosedTag();
             }
             else if (isQName()) {
                 return readTag();
             }
             else {
                 throw new RuntimeException("Not Implemented!");
             }
         } else {
             StringBuilder builder = new StringBuilder();
             while (c != -1 && c != '<' && c != '{') {
                 builder.append((char) c);
                 consume();
             }
             if (c == -1) {
                 throw new RuntimeException("Broken XML!");
             }
             return t(TEXT, builder.toString());
         }
     }
 
     private String readRawWord() throws IOException {
         StringBuilder builder = new StringBuilder();
         do {
             builder.append((char)c);
             consume();
         } while (isWordPart());
         return builder.toString();
     }
 
     private Token readWord() throws IOException {
         String text = readRawWord();
         TokenType type = WORD;
         if (text.equals("declare")) {
             type = DECLARE;
         }
         else if (text.equals("let")) {
             type = LET;
         }
         else if (text.equals("for")) {
             type = FOR;
         }
         else if (text.equals("in")) {
             type = IN;
         }
         else if (text.equals("as")) {
             type = AS;
         }
         else if (text.equals("if")) {
             type = IF;
         }
         else if (text.equals("then")) {
             type = THEN;
         }
         else if (text.equals("else")) {
             type = ELSE;
         }
         else if (text.equals("group")) {
             type = GROUP;
         }
         else if (text.equals("order")) {
             type = ORDER;
         }
         else if (text.equals("by")) {
             type = BY;
         }
         else if (text.equals("where")) {
             type = WHERE;
         }
         else if (text.equals("return")) {
             type = RETURN;
         }
         else if (text.equals("and")) {
             type = AND;
         }
         else if (text.equals("or")) {
             type = OR;
         }
         else if (text.equals("div")) {
             type = DIV;
         }
         else if (text.equals("mod")) {
             type = MOD;
         }
         else if (text.equals("to")) {
             type = TO;
         }
         else if (text.equals("namespace")) {
             type = NAMESPACE;
         }
         return t(type, text);
     }
 
     private Token readNumber() throws IOException {
         StringBuilder builder = new StringBuilder();
         do {
             builder.append((char)c);
             consume();
         } while (('0' <= c && c <= '9') || c == '.');
         return t(NUMBER, builder.toString());
     }
     
     private Token readSingleString() throws IOException {
         consume();
         StringBuilder builder = new StringBuilder();
         while (c != -1 && c != '\'') {
             builder.append((char)c); // TODO: FIXME: escaping!
             consume();
         }
         match('\'');
         return t(STRING, builder.toString());
     }
 
     private Token readDoubleString() throws IOException {
         consume();
         StringBuilder builder = new StringBuilder();
         while (c != -1 && c != '\"') {
             builder.append((char)c); // TODO: FIXME: escaping!
             consume();
         }
         match('"');
         return t(STRING, builder.toString());
     }
 
     private Token readVariable() throws IOException {
         StringBuilder builder = new StringBuilder();
         builder.append((char)c);
         consume();
         if (!isWordStart()) {
             throw new LexerException("Invalid variable name start: "+(char)c);
         }
         builder.append((char)c);
         consume();
         while (isWordPart()) {
             builder.append((char)c);
             consume();
         }
         return t(VARIABLE, builder.toString());
     }
 
     private Token readTag() throws IOException {
         enterNode();
         return t(TAGOPEN,  readRawWord());
     }
 
     private Token readClosedTag() throws IOException {
         exit(IN_TEXT);
         exitNode();
         Token t = t(TAGCLOSE, readRawWord());
         skipWhitespaces();
         match('>');
         return t;
     }
 
     private void skipWhitespaces() throws IOException {
         while (isWhitespace(c)) {
             consume();
         }
     }
 
     private void endComment() throws IOException {
         while (c != -1) {
             if (c == ':') {
                 do {
                     consume();
                 } while (c == ':');
                 if (c == ')') {
                     consume();
                     return;
                 }
                 else {
                     consume();
                 }
             }
             else {
                 consume();
             }
         }
     }
 
     private boolean isWordStart() {
         return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
     }
 
     private boolean isWordPart() {
         return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9') || c == ':' || c == '_' || c == '-' || c == '.';
     }
 
     private boolean isQName() {
         return isWordPart(); // TODO: FIXME: not correct
     }
 
 }
