 package swp_compiler_ss13.fuc.lexer.other;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.ByteArrayInputStream;
 import java.io.UnsupportedEncodingException;
 
 import junit.extensions.PA;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import swp_compiler_ss13.common.lexer.Lexer;
 import swp_compiler_ss13.common.lexer.Token;
 import swp_compiler_ss13.common.lexer.TokenType;
 import swp_compiler_ss13.fuc.lexer.LexerImpl;
 import swp_compiler_ss13.fuc.lexer.util.Constants;
 
 /**
  * Testclass for tokenizing of semicolons, comments and EOF
  * 
  * @author "Thomas Benndorf"
  * 
  */
 public class AdditionalSymbolTest {
 	private Lexer lexer;
 
 	@Before
 	public void setUp() throws Exception {
 		this.lexer = new LexerImpl();
 	}
 
 	/**
	 * Test for matching of semicolon, comment
 	 */
 	@Test
 	public void matchingSemicolonAndEOFTest() {
 		PA.setValue(this.lexer, "actualTokenValue", Constants.SEMICOLON);
 		PA.invokeMethod(this.lexer, "matchToken()");
 		assertEquals(TokenType.SEMICOLON,
 				PA.getValue(this.lexer, "actualTokenType"));
 
 		PA.setValue(this.lexer, "actualTokenValue", Constants.DOT);
 		PA.invokeMethod(this.lexer, "matchToken()");
 		assertEquals(TokenType.DOT, PA.getValue(this.lexer, "actualTokenType"));
 
 		/*
 		 * FIXME: PA.invokeMethode() throws IllegalArgumentException
 		 * 
 		 * PA.setValue(this.lexer, "actualTokenValue", Constants.COMMENT +
 		 * Constants.COMMENT_EXAMPLE); PA.invokeMethod(this.lexer,
 		 * "matchToken()"); assertEquals(TokenType.COMMENT,
 		 * PA.getValue(this.lexer, "actualTokenType"));
 		 */
 
 	}
 
 	/**
 	 * Test for tokenizing of semicolons, comments, dot and EOF with an empty
 	 * line
 	 * 
 	 * @throws UnsupportedEncodingException
 	 *             : UTF-8 encoding not supported
 	 */
 	@Test
 	public void simpleTokenizingOfSymbolsTest()
 			throws UnsupportedEncodingException {
 		String simpleSymbolString = Constants.ID1 + Constants.SEMICOLON + " "
 				+ Constants.DOT + "\n\n" + Constants.COMMENT + " "
 				+ Constants.COMMENT_EXAMPLE;
 
 		this.lexer.setSourceStream(new ByteArrayInputStream(simpleSymbolString
 				.getBytes("UTF-8")));
 
 		Token token = this.lexer.getNextToken();
 		assertEquals(Constants.ID1, token.getValue());
 		assertEquals(TokenType.ID, token.getTokenType());
 		assertEquals(1, token.getLine().intValue());
 		assertEquals(1, token.getColumn().intValue());
 
 		token = this.lexer.getNextToken();
 		assertEquals(Constants.SEMICOLON, token.getValue());
 		assertEquals(TokenType.SEMICOLON, token.getTokenType());
 		assertEquals(1, token.getLine().intValue());
 		assertEquals(3, token.getColumn().intValue());
 
 		token = this.lexer.getNextToken();
 		assertEquals(Constants.DOT, token.getValue());
 		assertEquals(TokenType.DOT, token.getTokenType());
 		assertEquals(1, token.getLine().intValue());
 		assertEquals(5, token.getColumn().intValue());
 
 		token = this.lexer.getNextToken();
 		assertEquals(Constants.COMMENT + " " + Constants.COMMENT_EXAMPLE,
 				token.getValue());
 		assertEquals(TokenType.COMMENT, token.getTokenType());
 		assertEquals(3, token.getLine().intValue());
 		assertEquals(1, token.getColumn().intValue());
 
 		token = this.lexer.getNextToken();
 		assertEquals("$", token.getValue());
 		assertEquals(TokenType.EOF, token.getTokenType());
 		assertEquals(4, token.getLine().intValue());
 		assertEquals(1, token.getColumn().intValue());
 
 		token = this.lexer.getNextToken();
 		assertEquals("$", token.getValue());
 		assertEquals(TokenType.EOF, token.getTokenType());
 		assertEquals(4, token.getLine().intValue());
 		assertEquals(1, token.getColumn().intValue());
 	}
 }
