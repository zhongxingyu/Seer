 package fc.lexer;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import fc.lexer.Lexer;
 import fc.lexer.Symbol;
 import fc.lexer.Token;
 
 public class LexerTest {
     @Test
     public void testEmptyString() throws LexerException {
         final Lexer lexer = new Lexer("");
         Token t = lexer.nextToken();
         assertEquals(Symbol.END, t.getSymbol());
     }
     
     @Test
     public void testWhitespaceOnly() throws LexerException {
         final Lexer lexer = new Lexer(" \t\n \r");
         Token t = lexer.nextToken();
         assertEquals(Symbol.END, t.getSymbol());
     }
 
     @Test
     public void testSimpleOperators() throws LexerException {
         final Lexer lexer = new Lexer("=()+-*/");
         Token token;
 
         token = lexer.nextToken();
         assertEquals(Symbol.EQUAL, token.getSymbol());
         assertEquals(1, token.getPosition());
         token = lexer.nextToken();
         assertEquals(Symbol.LPAREN, token.getSymbol());
         assertEquals(2, token.getPosition());
         token = lexer.nextToken();
         assertEquals(Symbol.RPAREN, token.getSymbol());
         assertEquals(3, token.getPosition());
         token = lexer.nextToken();
         assertEquals(Symbol.PLUS, token.getSymbol());
         assertEquals(4, token.getPosition());
         token = lexer.nextToken();
         assertEquals(Symbol.MINUS, token.getSymbol());
         assertEquals(5, token.getPosition());
         token = lexer.nextToken();
         assertEquals(Symbol.MULTIPLY, token.getSymbol());
         assertEquals(6, token.getPosition());
         token = lexer.nextToken();
         assertEquals(Symbol.DIVIDE, token.getSymbol());
         assertEquals(7, token.getPosition());
         token = lexer.nextToken();
         assertEquals(Symbol.END, token.getSymbol());
         assertEquals(8, token.getPosition());
     }
 
     @Test
     public void testScanLet() throws LexerException {
         final Lexer lexer = new Lexer("let");
         Token token;
         
         token = lexer.nextToken();
         assertEquals(Symbol.LET, token.getSymbol());
         assertEquals(1, token.getPosition());
         token = lexer.nextToken();
         assertEquals(Symbol.END, token.getSymbol());
         assertEquals(4, token.getPosition());
     }
 
     @Test
     public void testScanExit() throws LexerException {
         final Lexer lexer = new Lexer("exit");
         Token token;
         
         token = lexer.nextToken();
         assertEquals(Symbol.EXIT, token.getSymbol());
         assertEquals(1, token.getPosition());
         token = lexer.nextToken();
         assertEquals(Symbol.END, token.getSymbol());
         assertEquals(5, token.getPosition());
     }
 
     @Test
     public void testScanName() throws LexerException {
         final Lexer lexer = new Lexer("x");
         Token token;
         
         token = lexer.nextToken();
         assertEquals(Symbol.IDENTIFIER, token.getSymbol());
         assertEquals("x", token.getStringValue());
         assertEquals(1, token.getPosition());
         token = lexer.nextToken();
         assertEquals(Symbol.END, token.getSymbol());
         assertEquals(2, token.getPosition());
     }
 
     @Test
     public void testScanConstantNumber() throws LexerException {
         Lexer lexer;
         Token token;
 
         lexer = new Lexer("42");
         token = lexer.nextToken();
         assertEquals(Symbol.NUMBER, token.getSymbol());
         assertEquals(42, Double.parseDouble(token.getStringValue()), 0.0);
         assertEquals(1, token.getPosition());
         token = lexer.nextToken();
         assertEquals(Symbol.END, token.getSymbol());
         assertEquals(3, token.getPosition());
 
         lexer = new Lexer("2.89");
         token = lexer.nextToken();
         assertEquals(Symbol.NUMBER, token.getSymbol());
         assertEquals(2.89, Double.parseDouble(token.getStringValue()), 0.0);
         assertEquals(1, token.getPosition());
         token = lexer.nextToken();
         assertEquals(Symbol.END, token.getSymbol());
         assertEquals(5, token.getPosition());
     }
 
     @Test
     public void testScanWhitoutWhitespace() throws LexerException {
         Lexer lexer;
         Token token;
 
         lexer = new Lexer("42/2+6-4*1");
         token = lexer.nextToken();
         assertEquals(Symbol.NUMBER, token.getSymbol());
         assertEquals(42, Double.parseDouble(token.getStringValue()), 0.0);
         assertEquals(1, token.getPosition());
 
         token = lexer.nextToken();
         assertEquals(Symbol.DIVIDE, token.getSymbol());
         assertEquals(3, token.getPosition());
 
         token = lexer.nextToken();
         assertEquals(Symbol.NUMBER, token.getSymbol());
         assertEquals(2, Double.parseDouble(token.getStringValue()), 0.0);
         assertEquals(4, token.getPosition());
        
         token = lexer.nextToken();
         assertEquals(Symbol.PLUS, token.getSymbol());
         assertEquals(5, token.getPosition());
 
         token = lexer.nextToken();
         assertEquals(Symbol.NUMBER, token.getSymbol());
         assertEquals(6, Double.parseDouble(token.getStringValue()), 0.0);
         assertEquals(6, token.getPosition());
 
         token = lexer.nextToken();
         assertEquals(Symbol.MINUS, token.getSymbol());
         assertEquals(7, token.getPosition());
 
         token = lexer.nextToken();
         assertEquals(Symbol.NUMBER, token.getSymbol());
         assertEquals(4, Double.parseDouble(token.getStringValue()), 0.0);
         assertEquals(8, token.getPosition());
 
         token = lexer.nextToken();
         assertEquals(Symbol.MULTIPLY, token.getSymbol());
         assertEquals(9, token.getPosition());
 
         token = lexer.nextToken();
         assertEquals(Symbol.NUMBER, token.getSymbol());
         assertEquals(1, Double.parseDouble(token.getStringValue()), 0.0);
         assertEquals(10, token.getPosition());
 
         token = lexer.nextToken();
         assertEquals(Symbol.END, token.getSymbol());
         assertEquals(11, token.getPosition());
     }
 
     @Test(expected=LexerException.class)
     public void testUnexpectedSymbol() throws LexerException {
         Lexer lexer = new Lexer("%");
         lexer.nextToken();
     }
 }
