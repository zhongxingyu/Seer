 /*
  *  lemonjuice - Java Template Engine.
  *  Copyright (C) 2009 Manuel Tomis support@pagegoblin.com
  *
  *  This library is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with this library.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.codegremlins.lemonjuice.engine;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 class Scanner {
     private static String[] KEYWORDS = {"if", "then", "else", "else", "macro", "for", "in", "do", "end", "set", "true", "false"};
     private enum Type {NUMBER, STRING, NAME, SYMBOL, TEXT, KEYWORD, BUILTIN, END};
     
     private static final Set<String> BUILTINS = new HashSet<String>();
     
     static {
         String[] builtins = {
                 "append", "equal", "head", "tail", "join", "length",
                 "shuffle", "strip", "lookup", "include", "flatten",
                 "xml", "url", "split", "reverse", "sort", "range"
                 };
         
         for (String key : builtins) {
             BUILTINS.add(key);
         }
     }
     
     
     private PeekReader in;
     private StringBuffer out = new StringBuffer();
     private String token;
     private Type type;
     private boolean inTag = false;
     private boolean beginTag = false;
     private boolean cached = false;
     
     private int column = 1;
     
     public Scanner(PeekReader in) {
         this.in = in;
     }
 
     public void peek() throws IOException {
         if (!cached) {
             next();
         }
         cached = true;
     }
     
     public void read() throws IOException {
         if (!cached) {
             next();
         }
         cached = false;
     }
 
     private void next() throws IOException {
         out.setLength(0);
 
         if (beginTag) {
             beginTag = false;
             token = "${";
             type = Type.SYMBOL;
             return;
         }
 
         type = null;
         token = null;
 
         int c = in.peek();
         if (c == -1) {
             type = Type.END;
             return;
         }
         
         if (!inTag) {
             type = Type.TEXT;
             parseText();
             column = in.column() - 1;
             return;
         }
         
         clearSpace();
         column = in.column() - 1;
         c = in.peek();
         
         if (c == -1) {
             type = Type.END;
             return;
         } else if ("/+:(){}!,.;=[]?&|@$".indexOf(c) > -1) {
             type = Type.SYMBOL;
             out.append((char)in.read());
             
             if (c == '&' || c == '|' || c == '.') {
                 int d = in.peek();
                 if (d == c) {
                     out.append((char)in.read());
                 }
             }
             
             if (c == '/') {
                 out.setLength(0);
                 out.append("end");
             } else if (c == '$') {
                 out.setLength(0);
                 out.append("macro");
             }
             
             if (c == '}') {
                 inTag = false;
             }
         } else if (c == '"') {
             type = Type.STRING;
             parseString('"');
         } else if (c == '\'') {
             type = Type.STRING;
             parseString('\'');
         } else if (Character.isDigit(c)) {
             type = Type.NUMBER;
             parseNumber();
         } else if (Character.isLetter(c) || c == '_') {
             type = Type.NAME;
             parseName();
             
             String t = getToken();
             for (String key : KEYWORDS) {
                 if (key.equals(t)) {
                     type = Type.SYMBOL;
                     return;
                 }
             }
         } else {
             error("Invalid character `" + (char)c + "'");
         }
     }
     
     private void error(String text) throws IOException {
         throw new IOException("Line " + in.line() + "," + (in.column() - 1) + ": " + text);
     }
 
     private void clearSpace() throws IOException {
         for (;;) {
             int c = in.peek();
             while (Character.isWhitespace(c)) {
                 in.read();
                 c = in.peek();
             }
 
             if (!clearComment()) {
                 return;
             }
         }
     }
 
     private boolean clearComment() throws IOException {
         int c = in.peek();
         if (c == '#') {
             for (;;) {
                 c = in.read();
                 if (c == -1 || c == '\n' || c == '\r') {
                     return true;
                 }
             }
         } else {
             return false;
         }
     }
 
     private void parseName() throws IOException {
         int c = in.peek();
         while (Character.isLetterOrDigit(c) || c == '_' || c == '-') {
             out.append((char)in.read());
             c = in.peek();
         }
         
         clearSpace();
         if (in.peek() == ':') {
             in.read();
             type = Type.KEYWORD;
         } else if (BUILTINS.contains(out.toString())) {
             type = Type.BUILTIN;
         }
     }
 
     private void parseNumber() throws IOException {
         int c = in.peek();
         while (Character.isDigit(c) || c == '_' || c == '-') {
             out.append((char)in.read());
             c = in.peek();
         }
     }
     
     private void parseString(char end) throws IOException {
         in.read();
         
         int c = in.read();
         while (c != end) {
             if (c == '\n' || c == -1) {
                 error("Unterminated string");
             }
             
             if (c == '\\') {
                 c = in.read();
                 switch (c) {
                 case '\'': c = '\''; break; 
                 case '"': c = '"'; break; 
                 case 'b': c = '\b'; break; 
                 case 'f': c = '\f'; break; 
                 case 'n': c = '\n'; break; 
                 case 'r': c = '\r'; break; 
                 case 't': c = '\t'; break; 
                 case '0': c = '\0'; break;
                 case 'u': 
                     c = parseHex();
                     break;
                 case -1: error("Unterminated string");
                 }
             }
             out.append((char)c);
             c = in.read();
         }
     }
 
     private int parseHex() throws IOException {
         int r = 0;
         for (int i = 0; i < 4; i++) {
             r <<= 4;
             int c = in.read();
             
             if (c >= 'A' && c <= 'F') {
                 c = 10 + (c - 'A');
             } else if (c >= 'a' && c <= 'f') {
                 c = 10 + (c - 'a');
             } else if (c >= '0' && c <= '9') {
                 c = c - '0';
             } else {
                 error("Unexpected character `" + (char)c + "'");
             }
             
             r |= c;
         }
         return r;
     }
     
     private void parseText() throws IOException {
         for (;;) {
             int c = in.read();
             
             if (c == -1) {
                 return;
             } else if (c == '$') {
                 c = in.read();
                 if (c == '$') {
                     out.append('$');
                 } else if (c == '{') {
                     c = in.peek();
                     
                     if (c == '*') {
                         in.read();
                         for (;;) {
                             c = in.read();
                             if (c == -1) {
                                 return;
                             } else if (c == '*' && in.peek() == '}') {
                                 in.read();
                                 return;
                             }
                         }
 
                     } else {
                         beginTag = true;
                         inTag = true;
                     }
                     return;
                 } else {
                     out.append('$');
                     out.append((char)c);
                 }
             } else {
                 out.append((char)c);
             }
         }
     }
     
     public String getToken() {
         if (token == null) {
             token = out.toString();
         }
         return token;
     }
     
     public boolean isNumber() {
         return type == Type.NUMBER;
     }
     
     public boolean isString() {
         return type == Type.STRING;
     }
 
     public boolean isName() {
         return type == Type.NAME;
     }
 
     public boolean isEnd() {
         return type == Type.END;
     }
 
     public boolean isText() {
         return type == Type.TEXT;
     }
 
     public boolean isKeyword() {
         return type == Type.KEYWORD;
     }
 
     public boolean isBuiltin() {
         return type == Type.BUILTIN;
     }
 
     public boolean isSymbol(String text) {
         return type == Type.SYMBOL && getToken().equals(text);
     }
 
     public int line() {
         return in.line();
     }
 
     public int column() {
         return column;
     }
 }
