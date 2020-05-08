 package edu.kit.pp.minijava;
 
 import java.io.IOException;
 import java.io.StringReader;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import edu.kit.pp.minijava.tokens.Token;
 import static org.junit.Assert.*;
 
 public class LexerTest {
 
 	public LexerTest() {
 	}
 
 	@BeforeClass
 	public static void setUpClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() {
 	}
 
 	@After
 	public void tearDown() {
 	}
 
 	@Test
 	public void shouldHandleCommentsWithStars() throws Exception {
 		assertTrue(createLexer("/**\n Comment over multiple * lines.\n**/").next().isEof());
 	}
 
 	@Test
 	public void shouldNotIgnorePartsAfterCommentsWithStars() throws Exception {
 		assertEquals("class", createLexer("/*\n**/class").next().getValue());
 	}
 
 	@Test
 	public void shouldOutputErrorGivenNonAsciiCharacters() throws Exception {
 		assertTrue(createLexer("∞").next() instanceof edu.kit.pp.minijava.tokens.Error);
 	}
 
 	@Test
 	public void shouldNotAllowIntegersWithLeadingZeros() throws Exception {
 		Lexer lexer = createLexer("012");
 		assertEquals("0", lexer.next().getValue());
 		assertEquals("12", lexer.next().getValue());
 	}
 
 	private Lexer createLexer(String input) throws IOException {
 		return new Lexer(new StringReader(input));
 	}
 }
