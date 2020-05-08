 package oop1;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 public class SimpleLexerTest {
 
 	@Test
 	public void simpleTest() {
 		String text = "42is thenumber";
		SimpleLexer lexer = new SimpleLexer(text, new IdentifierReader(), new IntReader(), new WhitespaceReader());
 		Token token;
		
 		token = lexer.readNextToken();
 		assertEquals("42", token.getText());
 
 		token = lexer.readNextToken();
 		assertEquals("is", token.getText());
 
 		token = lexer.readNextToken();
 		assertEquals(" ", token.getText());
 
 		assertTrue(lexer.hasNextTokens());
 
 		token = lexer.readNextToken();
 		assertEquals("thenumber", token.getText());
 
 		assertFalse(lexer.hasNextTokens());
 	}
 }
