 package swp_compiler_ss13.common.lexer;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.ByteArrayInputStream;
 import java.io.UnsupportedEncodingException;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public abstract class LexerContractTest<L extends Lexer> {
 	public L lexer;
 
 	@Before
 	public final void setup() {
 		lexer = getLexerInstance();
 	}
 
 	protected abstract L getLexerInstance();
 
 	@Test
 	public void nonInitializedLexerReturnsEofToken() {
 		assertEquals(TokenType.EOF, lexer.getNextToken().getTokenType());
 		assertEquals("EOF should be returns till reinitialization",
 				TokenType.EOF, lexer.getNextToken().getTokenType());
 	}
 	
 	@Test
 	public void withNullInitializedLexerBehavesAsIfNotInitialized() {
 		lexer.setSourceStream(null);
 		assertEquals(TokenType.EOF, lexer.getNextToken().getTokenType());
 		assertEquals("EOF should be returns till reinitialization",
 				TokenType.EOF, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void emptyStreamInitializedLexerReturnsEofToken() {
 		prepareLexer("");
 		assertEquals(TokenType.EOF, lexer.getNextToken().getTokenType());
 		assertEquals("EOF should be returns till reinitialization",
 				TokenType.EOF, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerReturnsExpectedTokenAfterInitialization() {
 		prepareLexer("testIdToken");
 		assertEquals(TokenType.ID, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerReturnsEofTokenAtEndOfInput() {
 		prepareLexer("testIdToken");
 		assertEquals(TokenType.ID, lexer.getNextToken().getTokenType());
 		assertEquals(TokenType.EOF, lexer.getNextToken().getTokenType());
 		assertEquals("EOF should be returns till reinitialization",
 				TokenType.EOF, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerIsReinitializable() {
 		prepareLexer("testIdToken");
 		assertEquals(TokenType.ID, lexer.getNextToken().getTokenType());
 		assertEquals(TokenType.EOF, lexer.getNextToken().getTokenType());
 		prepareLexer("2535");
 		assertEquals(TokenType.NUM, lexer.getNextToken().getTokenType());
 		assertEquals(TokenType.EOF, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenNum() {
 		Token token = null;
 		prepareLexer("12312");
 		assertEquals(TokenType.NUM,
 				(token = lexer.getNextToken()).getTokenType());
 		assertTrue(token instanceof NumToken);
 		assertThat(((NumToken) token).getLongValue(), is(12312L));
 		prepareLexer("12312e11");
 		assertEquals(TokenType.NUM,
 				(token = lexer.getNextToken()).getTokenType());
 		assertTrue(token instanceof NumToken);
 		assertThat(((NumToken) token).getLongValue(), is(1231200000000000L));
 		prepareLexer("12312E10");
 		assertEquals(TokenType.NUM,
 				(token = lexer.getNextToken()).getTokenType());
 		assertTrue(token instanceof NumToken);
 		assertThat(((NumToken) token).getLongValue(), is(123120000000000L));
 	}
 	
 	@Test
 	public void lexerHandlesMultipleMinusInNumAsNotAToken() {
 		prepareLexer("1e--1");
 		assertEquals(TokenType.NOT_A_TOKEN,
 				(lexer.getNextToken()).getTokenType());
 	}
 	
 	@Test
 	public void lexerHandlesMultipleMinusInRealAsNotAToken() {
 		prepareLexer("1.0e--1");
 		assertEquals(TokenType.NOT_A_TOKEN,
 				(lexer.getNextToken()).getTokenType());
 	}
 	
 	@Test
 	public void lexerRecognizeTokenReal() {
 		Token token = null;
 		prepareLexer("12312.0");
 		assertEquals(TokenType.REAL,
 				(token = lexer.getNextToken()).getTokenType());
 		assertTrue(token instanceof RealToken);
 		assertThat(((RealToken) token).getDoubleValue(), is(12312.0));
 		prepareLexer("12312.0e1");
 		assertEquals(TokenType.REAL,
 				(token = lexer.getNextToken()).getTokenType());
 		assertTrue(token instanceof RealToken);
 		assertThat(((RealToken) token).getDoubleValue(), is(123120.0));
 		prepareLexer("12312.0E1");
 		assertEquals(TokenType.REAL,
 				(token = lexer.getNextToken()).getTokenType());
 		assertTrue(token instanceof RealToken);
 		assertThat(((RealToken) token).getDoubleValue(), is(123120.0));
 	}
 
 	@Test
 	public void lexerRecognizeTokenString() {
 		Token token = null;
 		prepareLexer("\"test-string$$\"");
 		assertEquals(TokenType.STRING,
 				(token = lexer.getNextToken()).getTokenType());
 		assertEquals("\"test-string$$\"", token.getValue());
 	}
 
 	@Test
 	public void lexerRecognizeTokenTrue() {
 		Token token = null;
 		prepareLexer("true");
 		assertEquals(TokenType.TRUE,
 				(token = lexer.getNextToken()).getTokenType());
 		assertTrue(token instanceof BoolToken);
 		assertTrue(((BoolToken) token).getBooleanValue());
 	}
 
 	@Test
 	public void lexerRecognizeTokenFalse() {
 		Token token = null;
 		prepareLexer("false");
 		assertEquals(TokenType.FALSE,
 				(token = lexer.getNextToken()).getTokenType());
 		assertTrue(token instanceof BoolToken);
 		assertFalse(((BoolToken) token).getBooleanValue());
 	}
 
 	@Test
 	public void lexerRecognizeStringsWithEscapedQuotes() {
 		Token token = null;
 		prepareLexer("\"test-string\\\"\"");
 		assertEquals(TokenType.STRING,
 				(token = lexer.getNextToken()).getTokenType());
 		assertEquals("\"test-string\\\"\"", token.getValue());
 	}
 
 	@Test
 	public void lexerEndsStringsWithCorrectEscapingAtCorrectPosition() {
 		Token token = null;
 		prepareLexer("\"test-string\\\"\"\"test\"");
 		assertEquals(TokenType.STRING,
 				(token = lexer.getNextToken()).getTokenType());
 		assertEquals("\"test-string\\\"\"", token.getValue());
 		assertEquals(TokenType.STRING,
 				(token = lexer.getNextToken()).getTokenType());
 		assertEquals("\"test\"", token.getValue());
 	}
 
 	@Test
 	public void lexerRecognizeIdToken() {
 		Token token = null;
 		prepareLexer("a");
 		assertEquals(TokenType.ID,
 				(token = lexer.getNextToken()).getTokenType());
 		assertEquals("a", token.getValue());
 		prepareLexer("and");
 		assertEquals(TokenType.ID,
 				(token = lexer.getNextToken()).getTokenType());
 		assertEquals("and", token.getValue());
 		prepareLexer("aComplexIdTypeWith100Meanings");
 		assertEquals(TokenType.ID,
 				(token = lexer.getNextToken()).getTokenType());
 		assertEquals("aComplexIdTypeWith100Meanings", token.getValue());
 	}
 
 	@Test
 	public void lexerRecognizeTokenMinus() {
 		prepareLexer("-");
 		assertEquals(TokenType.MINUS, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenPlus() {
 		prepareLexer("+");
 		assertEquals(TokenType.PLUS, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenTimes() {
 		prepareLexer("*");
 		assertEquals(TokenType.TIMES, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenDivide() {
 		prepareLexer("/");
 		assertEquals(TokenType.DIVIDE, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenAssign() {
 		prepareLexer("=");
 		assertEquals(TokenType.ASSIGNOP, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenEquals() {
 		prepareLexer("==");
 		assertEquals(TokenType.EQUALS, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenGreater() {
 		prepareLexer(">");
 		assertEquals(TokenType.GREATER, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenGreaterEqual() {
 		prepareLexer(">=");
 		assertEquals(TokenType.GREATER_EQUAL, lexer.getNextToken()
 				.getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenLess() {
 		prepareLexer("<");
 		assertEquals(TokenType.LESS, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenLessOrEqual() {
 		prepareLexer("<=");
 		assertEquals(TokenType.LESS_OR_EQUAL, lexer.getNextToken()
 				.getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenNot() {
 		prepareLexer("!");
 		assertEquals(TokenType.NOT, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenNotEquals() {
 		prepareLexer("!=");
 		assertEquals(TokenType.NOT_EQUALS, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenOr() {
 		prepareLexer("||");
 		assertEquals(TokenType.OR, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenAnd() {
 		prepareLexer("&&");
 		assertEquals(TokenType.AND, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenBreak() {
 		prepareLexer("break");
 		assertEquals(TokenType.BREAK, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenDo() {
 		prepareLexer("do");
 		assertEquals(TokenType.DO, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenIf() {
 		prepareLexer("if");
 		assertEquals(TokenType.IF, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenElse() {
 		prepareLexer("else");
 		assertEquals(TokenType.ELSE, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenWhile() {
 		prepareLexer("while");
 		assertEquals(TokenType.WHILE, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenPrint() {
 		prepareLexer("print");
 		assertEquals(TokenType.PRINT, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenLongSymbol() {
 		prepareLexer("long");
 		assertEquals(TokenType.LONG_SYMBOL, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenDoubleSymbol() {
 		prepareLexer("double");
 		assertEquals(TokenType.DOUBLE_SYMBOL, lexer.getNextToken()
 				.getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenBoolSymbol() {
 		prepareLexer("bool");
 		assertEquals(TokenType.BOOL_SYMBOL, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenStringSymbol() {
 		prepareLexer("string");
 		assertEquals(TokenType.STRING_SYMBOL, lexer.getNextToken()
 				.getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenLeftParan() {
 		prepareLexer("(");
 		assertEquals(TokenType.LEFT_PARAN, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenRightParan() {
 		prepareLexer(")");
 		assertEquals(TokenType.RIGHT_PARAN, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenLeftBrace() {
 		prepareLexer("{");
 		assertEquals(TokenType.LEFT_BRACE, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenRightBrace() {
 		prepareLexer("}");
 		assertEquals(TokenType.RIGHT_BRACE, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenLeftBracket() {
 		prepareLexer("[");
 		assertEquals(TokenType.LEFT_BRACKET, lexer.getNextToken()
 				.getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenRightBracket() {
 		prepareLexer("]");
 		assertEquals(TokenType.RIGHT_BRACKET, lexer.getNextToken()
 				.getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenSemicolon() {
 		prepareLexer(";");
 		assertEquals(TokenType.SEMICOLON, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerRecognizeTokenComment() {
 		prepareLexer("#hello\n");
 		assertEquals(TokenType.COMMENT, lexer.getNextToken().getTokenType());
 		prepareLexer("#hello");
 		assertEquals("Comments at document end may have no newline-character",
 				TokenType.COMMENT, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerIsCaseSensitive() {
 		prepareLexer("True TRUE False FALse Do DO Break If iF IF ElSe");
 		for (int i = 0; i < 11; i++)
 			assertEquals(TokenType.ID, lexer.getNextToken().getTokenType());
 		assertEquals(TokenType.EOF, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerReturnsForNonTokenizableCharactersNotAToken() {
 		prepareLexer("not $ $ print");
 		assertEquals(TokenType.ID, lexer.getNextToken().getTokenType());
 		assertEquals(TokenType.NOT_A_TOKEN, lexer.getNextToken().getTokenType());
 		assertEquals(TokenType.PRINT, lexer.getNextToken().getTokenType());
 		assertEquals(TokenType.EOF, lexer.getNextToken().getTokenType());
 	}
 	
 	@Test
 	public void lexerRecognizeNotATokenAlsoAtEndOfStream() {
 		prepareLexer("not $ $");
 		assertEquals(TokenType.ID, lexer.getNextToken().getTokenType());
 		assertEquals(TokenType.NOT_A_TOKEN, lexer.getNextToken().getTokenType());
 		assertEquals(TokenType.EOF, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerDoesNotMisrecognizeIdsStartingLikeReservedWords() {
 		prepareLexer("true88 printer");
 		assertEquals(TokenType.ID, lexer.getNextToken().getTokenType());
 		assertEquals(TokenType.ID, lexer.getNextToken().getTokenType());
 		assertEquals(TokenType.EOF, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerIgnoresAdditionalWhitespaces() {
 		prepareLexer("ids     \t\n  =   \t\r\n\t\t    42\n\n;");
 		assertEquals(TokenType.ID, lexer.getNextToken().getTokenType());
 		assertEquals(TokenType.ASSIGNOP, lexer.getNextToken().getTokenType());
 		assertEquals(TokenType.NUM, lexer.getNextToken().getTokenType());
 		assertEquals(TokenType.SEMICOLON, lexer.getNextToken().getTokenType());
 		assertEquals(TokenType.EOF, lexer.getNextToken().getTokenType());
 	}
 
 	@Test
 	public void lexerMatchLongerStatements() {
 		prepareLexer("distance=a*b;\nstr=\"Hello World\";#same message as ever\nif(i<10){\n}gate[9] = 2.67;");
 		TokenType[] expectedTokens = { TokenType.ID, TokenType.ASSIGNOP,
 				TokenType.ID, TokenType.TIMES, TokenType.ID,
 				TokenType.SEMICOLON, TokenType.ID, TokenType.ASSIGNOP,
 				TokenType.STRING, TokenType.SEMICOLON, TokenType.COMMENT,
 				TokenType.IF, TokenType.LEFT_PARAN, TokenType.ID,
 				TokenType.LESS, TokenType.NUM, TokenType.RIGHT_PARAN,
 				TokenType.LEFT_BRACE, TokenType.RIGHT_BRACE, TokenType.ID,
 				TokenType.LEFT_BRACKET, TokenType.NUM, TokenType.RIGHT_BRACKET,
 				TokenType.ASSIGNOP, TokenType.REAL, TokenType.SEMICOLON,
 				TokenType.EOF };
 
 		for (int i = 0; i < expectedTokens.length; i++)
 			assertEquals("Token " + i, expectedTokens[i], lexer.getNextToken()
 					.getTokenType());
 	}
 
 	@Test
 	public void lexerCountsColumnInLineCorrectly() {
 		prepareLexer("ids     \t  =   \t\r\t\t    42;");
 		assertThat(lexer.getNextToken().getColumn(), is(1));
 		assertThat(lexer.getNextToken().getColumn(), is(12));
 		assertThat(lexer.getNextToken().getColumn(), is(24));
 		assertThat(lexer.getNextToken().getColumn(), is(26));
 	}
 
 	@Test
 	public void lexerResetsColumnCounterAfterNewline() {
 		prepareLexer("ids =\n  42\n;");
 		assertThat(lexer.getNextToken().getColumn(), is(1));
 		assertThat(lexer.getNextToken().getColumn(), is(5));
 		assertThat(lexer.getNextToken().getColumn(), is(3));
 		assertThat(lexer.getNextToken().getColumn(), is(1));
 	}
 
 	@Test
 	public void lexerCountsLinesCorrectly() {
 		prepareLexer("id\r\n=42\r\n;");
 		assertThat(lexer.getNextToken().getLine(), is(1));
 		assertThat(lexer.getNextToken().getLine(), is(2));
 		assertThat(lexer.getNextToken().getLine(), is(2));
 		assertThat(lexer.getNextToken().getLine(), is(3));
 	}
 
 	/**
 	 * set testString as input for the parser
 	 */
 	private void prepareLexer(String testString) {
 		try {
 			lexer.setSourceStream(new ByteArrayInputStream(testString
 					.getBytes("UTF-8")));
 		} catch (UnsupportedEncodingException e) {
 			fail("UTF-8 is unknown ?!");
 		}
 	}
 }
