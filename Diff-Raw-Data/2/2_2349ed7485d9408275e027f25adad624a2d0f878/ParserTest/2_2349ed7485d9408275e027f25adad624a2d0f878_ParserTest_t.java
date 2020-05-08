 package edu.kit.pp.minijava;
 
 import edu.kit.pp.minijava.ast.*;
 import edu.kit.pp.minijava.tokens.*;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.mockito.stubbing.OngoingStubbing;
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 public class ParserTest {
 
 	public ParserTest() {
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
 	public void parseClassMember() throws Exception {
		Parser p= createParserFromTokensWithEOF(K("class"), I("classic"), O("{"), 
 				K("public"), K("int"), I("i"), O(";"), 
 				K("public"), K("int"), O("["), O("]"), I("intArray"), O(";"),
 				O("}"));
 		p.parseProgram();
 	}
 
 	@Test
 	public void parseAdditionExpression() throws Exception {
 		Parser p = createParserFromTokens(I("a"), O("+"), I("b"), O("+"), I("c"));
 		BinaryExpression e = (BinaryExpression) p.parseExpression();
 		assertEquals("+", e.getToken().getValue());
 		assertEquals("+", e.getLeft().getToken().getValue());
 		assertEquals("a", ((BinaryExpression) e.getLeft()).getLeft().getToken().getValue());
 		assertEquals("b", ((BinaryExpression) e.getLeft()).getRight().getToken().getValue());
 		assertEquals("c", e.getRight().getToken().getValue());
 	}
 
 	@Test
 	public void parseAdditionAndMultiplicationExpression() throws Exception {
 		Parser p = createParserFromTokens(I("a"), O("+"), I("b"), O("*"), I("c"));
 		BinaryExpression e = (BinaryExpression) p.parseExpression();
 		assertEquals("+", e.getToken().getValue());
 		assertEquals("a", e.getLeft().getToken().getValue());
 		assertEquals("*", e.getRight().getToken().getValue());
 		assertEquals("b", ((BinaryExpression) e.getRight()).getLeft().getToken().getValue());
 		assertEquals("c", ((BinaryExpression) e.getRight()).getRight().getToken().getValue());
 	}
 
 	@Test
 	public void parseShouldNotThrowExceptionForSimpleTokenStream() throws Exception {
 		Parser p = createParserFromTokensWithEOF(K("class"), I("Muh"), O("{"), O("}"));
 		try {
 			p.parseProgram();
 		} catch (Parser.UnexpectedTokenException e) {
 			fail("Did throw UnexpectedTokenException");
 		}
 	}
 
 	@Test
 	public void parseShouldFailIfNoClass() throws Exception {
 		Parser p = createParserFromTokensWithEOF(K("ssalc"), I("Muh"), O("{"), O("}"));
 		try {
 			p.parseProgram();
 			fail("Did accept program without class.");
 		} catch (Parser.UnexpectedTokenException e) {
 
 		}
 	}
 
 	@Test
 	public void parseSimpleClassWithField() throws Exception {
 		Parser p = createParserFromTokensWithEOF(
 				K("class"), I("Muh"), O("{"),
 				K("public"), I("String"), I("name"), O(";"), O("}"));
 		try {
 			p.parseProgram();
 		} catch (Parser.UnexpectedTokenException e) {
 			fail("Did not accept valid program with class field.");
 		}
 	}
 
 	@Test
 	public void parseSimpleClassWithInvalidField1() throws Exception {
 		Parser p = createParserFromTokensWithEOF(
 				K("class"), I("Muh"), O("{"),
 				K("public"), I("String"), O("]"), O("]"), I("name"), O(";"), O("}"));
 		try {
 			p.parseProgram();
 			fail("Did accept program with invalid class field.");
 		} catch (Parser.UnexpectedTokenException e) {
 
 		}
 	}
 
 	@Test
 	public void parseSimpleClassWithInvalidField2() throws Exception {
 		Parser p = createParserFromTokensWithEOF(
 				K("class"), I("Muh"), O("{"),
 				K("public"), I("String"), O("["), O("["), I("name"), O(";"), O("}"));
 		try {
 			p.parseProgram();
 			fail("Did accept program with invalid class field.");
 		} catch (Parser.UnexpectedTokenException e) {
 
 		}
 	}
 
 	private Parser createParserFromTokens(Token ... tokens) throws Exception {
 		Lexer l = mock(Lexer.class);
 		OngoingStubbing stub = when(l.next());
 		for (Token token : tokens) {
 			stub = stub.thenReturn(token);
 		}
 		return new Parser(l);
 	}
 
 	private Parser createParserFromTokensWithEOF(Token ... tokens) throws Exception {
 		Token[] ts = new Token[tokens.length + 1];
 		System.arraycopy(tokens, 0, ts, 0, tokens.length);
 		ts[tokens.length] = EOF();
 		return createParserFromTokens(ts);
 	}
 
 	private Token I() {
 		return I("generated_identifier");
 	}
 
 	private Token I(String s) {
 		return new Identifier(s);
 	}
 
 	private Token O(String s) {
 		return new Operator(s);
 	}
 
 	private Token K(String s) {
 		return new Keyword(s);
 	}
 
 	private Token EOF() {
 		return new edu.kit.pp.minijava.tokens.Eof();
 	}
 }
