 package xhl.core;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static xhl.core.Token.TokenType.*;
 
 import java.io.StringReader;
 
 import org.junit.Test;
 
 /**
  * Test lexer module.
  *
  * @author Sergej Chodarev
  */
 public class LexerTest {
     @Test
     public void braces() throws Exception {
         String code = "{[()]}";
         Lexer l = new Lexer(new StringReader(code));
         assertEquals(BRACE_OPEN, l.nextToken().type);
         assertEquals(BRACKET_OPEN, l.nextToken().type);
         assertEquals(PAR_OPEN, l.nextToken().type);
         assertEquals(PAR_CLOSE, l.nextToken().type);
         assertEquals(BRACKET_CLOSE, l.nextToken().type);
         assertEquals(BRACE_CLOSE, l.nextToken().type);
     }
 
     @Test
     public void punctuation() throws Exception {
         String code = ",:";
         Lexer l = new Lexer(new StringReader(code));
         assertEquals(COMMA, l.nextToken().type);
         assertEquals(COLON, l.nextToken().type);
     }
 
     @Test
     public void symbols() throws Exception {
         String code = "abc ABc_98";
         Lexer l = new Lexer(new StringReader(code));
         Token t = l.nextToken();
         assertEquals(SYMBOL, t.type);
         assertEquals("abc", t.stringValue);
         t = l.nextToken();
         assertEquals(SYMBOL, t.type);
         assertEquals("ABc_98", t.stringValue);
     }
 
     @Test
     public void operators() throws Exception {
         String code = "+ ->>";
         Lexer l = new Lexer(new StringReader(code));
         Token t = l.nextToken();
         assertEquals(OPERATOR, t.type);
         assertEquals("+", t.stringValue);
         t = l.nextToken();
         assertEquals(OPERATOR, t.type);
         assertEquals("->>", t.stringValue);
     }
 
     @Test
     public void numbers() throws Exception {
         String code = "-42 3.14";
         Lexer l = new Lexer(new StringReader(code));
         Token t = l.nextToken();
         assertEquals(NUMBER, t.type);
         assertEquals(-42.0, t.doubleValue, 0);
         t = l.nextToken();
         assertEquals(NUMBER, t.type);
         assertEquals(3.14, t.doubleValue, 0);
     }
 
     @Test
     public void strings() throws Exception {
         String code = "\"hello\"";
         Lexer l = new Lexer(new StringReader(code));
         Token t = l.nextToken();
         assertEquals(STRING, t.type);
         assertEquals("hello", t.stringValue);
     }
 
     @Test
     public void keywords() throws Exception {
         String code = "true false none";
         Lexer l = new Lexer(new StringReader(code));
         assertEquals(TRUE, l.nextToken().type);
         assertEquals(FALSE, l.nextToken().type);
         assertEquals(NONE, l.nextToken().type);
     }
 
     @Test
     public void newline() throws Exception {
         String code = "a\nb\n";
         Lexer l = new Lexer(new StringReader(code));
         assertEquals(SYMBOL, l.nextToken().type);
         assertEquals(LINEEND, l.nextToken().type);
         assertEquals(SYMBOL, l.nextToken().type);
         assertEquals(LINEEND, l.nextToken().type);
     }
 
     @Test
     public void newlineInBraces() throws Exception {
         String code = "a\n(b\n[c\n]\n)\n";
         Lexer l = new Lexer(new StringReader(code));
         assertEquals(SYMBOL, l.nextToken().type);
         assertEquals(LINEEND, l.nextToken().type);
         assertEquals(PAR_OPEN, l.nextToken().type);
         assertEquals(SYMBOL, l.nextToken().type);
         assertEquals(BRACKET_OPEN, l.nextToken().type);
         assertEquals(SYMBOL, l.nextToken().type);
         assertEquals(BRACKET_CLOSE, l.nextToken().type);
         assertEquals(PAR_CLOSE, l.nextToken().type);
         assertEquals(LINEEND, l.nextToken().type);
     }
 
     @Test
     public void emptyLinesAndComments() throws Exception {
        String code = "a\n\n   #comment\nb";
         Lexer l = new Lexer(new StringReader(code));
         assertEquals(SYMBOL, l.nextToken().type);
         assertEquals(LINEEND, l.nextToken().type);
        assertEquals(SYMBOL, l.nextToken().type);
        assertEquals(LINEEND, l.nextToken().type);
         assertNull(l.nextToken());
     }
 
     @Test
     public void indentation() throws Exception {
         String code = "a:\n  b\nc\n    d";
         Lexer l = new Lexer(new StringReader(code));
         assertEquals(SYMBOL, l.nextToken().type);
         assertEquals(COLON, l.nextToken().type);
         assertEquals(LINEEND, l.nextToken().type);
         assertEquals(INDENT, l.nextToken().type);
         assertEquals(SYMBOL, l.nextToken().type);
         assertEquals(LINEEND, l.nextToken().type);
         assertEquals(DEDENT, l.nextToken().type);
         assertEquals(SYMBOL, l.nextToken().type);
         assertEquals(LINEEND, l.nextToken().type);
         assertEquals(INDENT, l.nextToken().type);
         assertEquals(SYMBOL, l.nextToken().type);
         assertEquals(LINEEND, l.nextToken().type);
         assertEquals(DEDENT, l.nextToken().type);
     }
 
     @Test
     public void combination() throws Exception {
         String code = "a \"hello\":\n  b [42++true,\n()]\n";
         Lexer l = new Lexer(new StringReader(code));
         assertEquals(SYMBOL, l.nextToken().type);
         assertEquals(STRING, l.nextToken().type);
         assertEquals(COLON, l.nextToken().type);
         assertEquals(LINEEND, l.nextToken().type);
         assertEquals(INDENT, l.nextToken().type);
         assertEquals(SYMBOL, l.nextToken().type);
         assertEquals(BRACKET_OPEN, l.nextToken().type);
         assertEquals(NUMBER, l.nextToken().type);
         assertEquals(OPERATOR, l.nextToken().type);
         assertEquals(TRUE, l.nextToken().type);
         assertEquals(COMMA, l.nextToken().type);
         assertEquals(PAR_OPEN, l.nextToken().type);
         assertEquals(PAR_CLOSE, l.nextToken().type);
         assertEquals(BRACKET_CLOSE, l.nextToken().type);
         assertEquals(LINEEND, l.nextToken().type);
         assertEquals(DEDENT, l.nextToken().type);
     }
 
     @Test
     public void eof() throws Exception {
         String code = "a";
         Lexer l = new Lexer(new StringReader(code));
         assertEquals(SYMBOL, l.nextToken().type);
         assertEquals(LINEEND, l.nextToken().type);
         assertNull(l.nextToken());
     }
 }
