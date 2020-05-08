 package org.libj.xquery.lexer;
 
 import java.io.IOException;
 
 import static org.libj.xquery.lexer.TokenType.EOF;
 
 public class XPathLexer {
     private final LL1Reader reader;
     private StringBuffer builder = new StringBuffer();
 
     public XPathLexer(LL1Reader reader) throws IOException {
         this.reader = reader;
         readXPath();
     }
 
     public void readXPath() throws IOException {
         while (reader.c != EOF) {
             switch (reader.c) {
                 case '\'':
                     readSingleString();
                     break;
                 case '\"':
                     readDoubleString();
                     break;
                 case '[':
                     readBracket();
                     break;
                 case '(':
                     readParen();
                     break;
                 case ' ':
                 case '\t':
                 case '\n':
                 case '\r':
                 case ']':
                 case ')':
                 case '}':
                 case ',':
                     return;
                 default:
                     append();
                     reader.consume();
             }
         }
     }
 
     public void readPart() throws IOException {
         while (reader.c != EOF) {
             switch (reader.c) {
                 case '\'':
                     readSingleString();
                     break;
                 case '\"':
                     readDoubleString();
                     break;
                 case '[':
                     readBracket();
                     break;
                 case '(':
                     readParen();
                     break;
                 case ']':
                 case ')':
                     return;
                 default:
                     append();
             }
             reader.consume();
         }
     }
 
     private void readSingleString() throws IOException {
         match('\'');
         while (reader.c != EOF && reader.c != '\'') {
             // TODO: escape
             consume();
         }
         match('\'');
     }
 
     private void readDoubleString() throws IOException {
         match('"');
         while (reader.c != EOF && reader.c != '"') {
             // TODO: escape
             consume();
         }
         match('"');
     }
 
     private void readBracket() throws IOException {
         match('[');
         readPart();
         match(']');
     }
 
     private void readParen() throws IOException {
         match('(');
         readPart();
         match(')');
     }
 
     private void append() {
         builder.append((char) reader.c);
     }
 
     private void consume() throws IOException {
         append();
         reader.consume();
     }
 
     private void match(char c) throws IOException {
         builder.append((char)reader.c);
         reader.match(c);
     }
 
     public String getXPath() {
         return builder.toString();
     }
 
 }
