 package front_end;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import java_cup.runtime.Symbol;
 
 /**
  * Tests Lexer's performance on decomposing different inputs into the correct sequence of tokens.
  * 
  * @author jason, sam
  *
  */
 public class LexerTester {
 
 
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
 
 	/**
 	 * Tests for correct parsing of addition operator and its operands.
 	 * 
 	 * Specifically, ensures that Lexer produces token streams of ID PLUS ID for strings like
 	 * "a + b" and INT_CONST PLUS INT_CONST for strings like "1 + 2".
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void additionSymbolTest1() throws IOException {
 
 		String text = "2 + 5";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be an INT_CONST", sym.INT_CONST,
 				tokenList.get(0).intValue());
 		assertEquals("The second token should be a PLUS", sym.PLUS, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be an INT_CONST", sym.INT_CONST,
 				tokenList.get(2).intValue());
 		
 	}
 	
 	/**
 	 * Tests for correct parsing of addition operator and its operands.
 	 * 
 	 * Specifically, ensures that Lexer produces token streams of ID PLUS ID for strings like
 	 * "a + b" and INT_CONST PLUS INT_CONST for strings like "1 + 2".
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void additionSymbolTest2() throws IOException {
 
 		String text = "a + b";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be an ID", sym.ID,
 				tokenList.get(0).intValue());
 		assertEquals("The second token should be a PLUS", sym.PLUS, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be an ID", sym.ID,
 				tokenList.get(2).intValue());
 		
 	}
 	
 	/**
 	 * Tests for correct parsing of subtraction operator and its operands.
 	 * 
 	 * Specifically, ensures that Lexer produces token streams of ID MINUS ID for strings like
 	 * "a - b" and INT_CONST MINUS INT_CONST for strings like "1 - 2".
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void subtractionSymbolTest1() throws IOException {
 
 		String text = "a - b";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be an ID", sym.ID,
 				tokenList.get(0).intValue());
 		assertEquals("The second token should be a MINUS", sym.MINUS, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be an ID", sym.ID,
 				tokenList.get(2).intValue());
 		
 	}
 	
 	/**
 	 * Tests for correct parsing of subtraction operator and its operands.
 	 * 
 	 * Specifically, ensures that Lexer produces token streams of ID MINUS ID for strings like
 	 * "a - b" and INT_CONST MINUS INT_CONST for strings like "1 - 2".
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void subtractionSymbolTest2() throws IOException {
 
 		String text = "1 - 2";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be an INT_CONST", sym.INT_CONST,
 				tokenList.get(0).intValue());
 		assertEquals("The second token should be a MINUS", sym.MINUS, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be an INT_CONST", sym.INT_CONST,
 				tokenList.get(2).intValue());
 		
 	}
 	
 	/**
 	 * Tests for correct parsing of division operator and its operands.
 	 * 
 	 * Specifically, ensures that Lexer produces token streams of ID DIVIDE ID for strings like
 	 * "a / b" and INT_CONST DIVIDE INT_CONST for strings like "1 / 2".
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void divisionSymbolTest1() throws IOException {
 
 		String text = "1 / 2";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be an INT_CONST", sym.INT_CONST,
 				tokenList.get(0).intValue());
 		assertEquals("The second token should be a DIVIDE", sym.DIVIDE, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be an INT_CONST", sym.INT_CONST,
 				tokenList.get(2).intValue());
 		
 	}
 
 	/**
 	 * Tests for correct parsing of division operator and its operands.
 	 * 
 	 * Specifically, ensures that Lexer produces token streams of ID DIVIDE ID for strings like
 	 * "a / b" and INT_CONST DIVIDE INT_CONST for strings like "1 / 2".
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void divisionSymbolTest2() throws IOException {
 
 		String text = "a / b";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be an ID", sym.ID,
 				tokenList.get(0).intValue());
 		assertEquals("The second token should be a DIVIDE", sym.DIVIDE, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be an ID", sym.ID,
 				tokenList.get(2).intValue());
 		
 	}
 	
 	/**
 	 * Tests for correct parsing of multiplication operator and its operands.
 	 * 
 	 * Specifically, ensures that Lexer produces token streams of ID TIMES ID for strings like
 	 * "a * b" and INT_CONST TIMES INT_CONST for strings like "1 * 2".
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void multiplicationSymbolTest1() throws IOException {
 
 		String text = "1 * 2";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be an INT_CONST", sym.INT_CONST,
 				tokenList.get(0).intValue());
 		assertEquals("The second token should be a TIMES", sym.TIMES, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be an INT_CONST", sym.INT_CONST,
 				tokenList.get(2).intValue());
 		
 	}
 
 	/**
 	 * Tests for correct parsing of multiplication operator and its operands.
 	 * 
 	 * Specifically, ensures that Lexer produces token streams of ID TIMES ID for strings like
 	 * "a * b" and INT_CONST TIMES INT_CONST for strings like "1 * 2".
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void multiplicationSymbolTest2() throws IOException {
 
 		String text = "a * b";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be an ID", sym.ID,
 				tokenList.get(0).intValue());
 		assertEquals("The second token should be a TIMES", sym.TIMES, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be an ID", sym.ID,
 				tokenList.get(2).intValue());
 		
 	}
 	
 	/**
 	 * Tests for correct parsing of a parenthesized expression.
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of L_PAREN ID R_PAREN for 
 	 * strings like "(a)"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void parenthesesSymbolTest() throws IOException {
 
 		String text = "(a)";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be an L_PAREN", sym.L_PAREN,
 				tokenList.get(0).intValue());
 		assertEquals("The second token should be a ID", sym.ID, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be an R_PAREN", sym.R_PAREN,
 				tokenList.get(2).intValue());
 		
 	}
 	
 	/**
 	 * Tests for correct parsing of the postfix increment operator
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of ID INCR for 
 	 * strings like "a++"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void incrementSymbolTest() throws IOException {
 
 		String text = "a++";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 2 tokens for the string '"
 				+ text + "'", 2, tokenList.size());
 		assertEquals("The first token should be a ID", sym.ID, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a INCR", sym.INCR, tokenList
 				.get(1).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the postfix decrement operator
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of ID DECR for 
 	 * strings like "a--"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void decrementSymbolTest() throws IOException {
 
 		String text = "a--";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 2 tokens for the string '"
 				+ text + "'", 2, tokenList.size());
 		assertEquals("The first token should be a ID", sym.ID, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a DECR", sym.DECR, tokenList
 				.get(1).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with the logical 'and' operator
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of BOOL_CONST AND BOOL_CONST
 	 * for strings like "true and false" and a token stream of ID AND ID for strings like
 	 * "variable and variable"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void andSymbolTest1() throws IOException {
 
 		String text = "true and false";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be a BOOL_CONST", sym.BOOL_CONST, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a AND", sym.AND, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a BOOL_CONST", sym.BOOL_CONST, tokenList
 				.get(2).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with the logical 'and' operator
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of BOOL_CONST AND BOOL_CONST
 	 * for strings like "true and false" and a token stream of ID AND ID for strings like
 	 * "variable and variable"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void andSymbolTest2() throws IOException {
 
 		String text = "variable and variable";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be a ID", sym.ID, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a AND", sym.AND, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a ID", sym.ID, tokenList
 				.get(2).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with the logical 'or' operator
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of BOOL_CONST OR BOOL_CONST
 	 * for strings like "true or false" and a token stream of ID OR ID for strings like
 	 * "variable or variable"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void orSymbolTest1() throws IOException {
 
 		String text = "true or false";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be a BOOL_CONST", sym.BOOL_CONST, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a OR", sym.OR, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a BOOL_CONST", sym.BOOL_CONST, tokenList
 				.get(2).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with the logical 'or' operator
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of BOOL_CONST OR BOOL_CONST
 	 * for strings like "true or false" and a token stream of BOOL OR BOOL for strings like
 	 * "bool or bool"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void orSymbolTest2() throws IOException {
 
 		String text = "variable or variable";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be a ID", sym.ID, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a OR", sym.OR, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a ID", sym.ID, tokenList
 				.get(2).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with the logical 'not' operator
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of NOT BOOL_CONST
 	 * for strings like "not true"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void notSymbolTest() throws IOException {
 
 		String text = "not true";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 2 tokens for the string '"
 				+ text + "'", 2, tokenList.size());
 		assertEquals("The first token should be a NOT", sym.NOT, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a BOOL", sym.BOOL_CONST, tokenList
 				.get(1).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with the 'greater than' comparison operator
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of INT_CONST GRTR INT_CONST
 	 * for strings like "2 > 1" and a token stream of ID GRTR ID for strings like
 	 * "number > number"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void greaterThanSymbolTest1() throws IOException {
 
 		String text = "2 > 1";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a GRTR", sym.GRTR, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(2).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with the 'greater than' comparison operator
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of INT_CONST GRTR INT_CONST
 	 * for strings like "2 > 1" and a token stream of ID GRTR ID for strings like
 	 * "number > number"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void greaterThanSymbolTest2() throws IOException {
 
 		String text = "number > number";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be a ID", sym.ID, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a GRTR", sym.GRTR, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a ID", sym.ID, tokenList
 				.get(2).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with the 'greater than or equal to' comparison
 	 * operator
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of INT_CONST GRTR_EQL INT_CONST
 	 * for strings like "2 >= 1" and a token stream of ID GRTR_EQL ID for strings like
 	 * "number >= number"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void greaterThanOrEqualSymbolTest1() throws IOException {
 
 		String text = "2 >= 1";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a GRTR_EQL", sym.GRTR_EQL, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(2).intValue());
 	}
 	
 
 	/**
 	 * Tests for correct parsing of the statements with the 'greater than or equal to' comparison
 	 * operator
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of INT_CONST GRTR_EQL INT_CONST
 	 * for strings like "2 >= 1" and a token stream of ID GRTR_EQL ID for strings like
 	 * "number >= number"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void greaterThanOrEqualSymbolTest2() throws IOException {
 
 		String text = "number >= number";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be a ID", sym.ID, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a GRTR_EQL", sym.GRTR_EQL, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a ID", sym.ID, tokenList
 				.get(2).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with the 'less than' comparison operator
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of INT_CONST LESS INT_CONST
 	 * for strings like "2 < 1" and a token stream of ID LESS ID for strings like
 	 * "number < number"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void lessThanSymbolTest1() throws IOException {
 
 		String text = "2 < 1";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a LESS", sym.LESS, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(2).intValue());
 	}
 
 	/**
 	 * Tests for correct parsing of the statements with the 'less than' comparison operator
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of INT_CONST LESS INT_CONST
 	 * for strings like "2 < 1" and a token stream of ID LESS ID for strings like
 	 * "number < number"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void lessThanSymbolTest2() throws IOException {
 
 		String text = "number < number";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be a ID", sym.ID, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a LESS", sym.LESS, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a ID", sym.ID, tokenList
 				.get(2).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with the 'less than or equal to' comparison
 	 * operator
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of INT_CONST LESS_EQL INT_CONST
 	 * for strings like "2 <= 1" and a token stream of ID LESS_EQL ID for strings like
 	 * "number <= number"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void lessThanOrEqualSymbolTest1() throws IOException {
 
 		String text = "2 <= 1";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a LESS_EQL", sym.LESS_EQL, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(2).intValue());
 	}
 	
 
 	/**
 	 * Tests for correct parsing of the statements with the 'less than or equal to' comparison
 	 * operator
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of INT_CONST LESS_EQL INT_CONST
 	 * for strings like "2 <= 1" and a token stream of ID LESS_EQL ID for strings like
 	 * "number <= number"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void lessThanOrEqualSymbolTest2() throws IOException {
 
 		String text = "number <= number";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be a ID", sym.ID, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a LESS_EQL", sym.LESS_EQL, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a ID", sym.ID, tokenList
 				.get(2).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with the 'not equal to' comparison operator
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of INT_CONST NOT_EQLS INT_CONST
 	 * for strings like "2 != 1" and a token stream of ID NOT_EQLS ID for strings like
 	 * "number != number"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void notEqualSymbolTest1() throws IOException {
 
 		String text = "2 != 1";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a NOT_EQLS", sym.NOT_EQLS, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(2).intValue());
 	}
 	
 
 	/**
 	 * Tests for correct parsing of the statements with the 'not equal to' comparison operator
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of INT_CONST NOT_EQLS INT_CONST
 	 * for strings like "2 != 1" and a token stream of ID NOT_EQLS ID for strings like
 	 * "number != number"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void notEqualSymbolTest2() throws IOException {
 
 		String text = "number != number";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be a ID", sym.ID, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a NOT_EQLS", sym.NOT_EQLS, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a ID", sym.ID, tokenList
 				.get(2).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with the 'equal to' comparison operator
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of INT_CONST DBL_EQLS INT_CONST
 	 * for strings like "2 == 1" and a token stream of ID DBL_EQLS ID for strings like
 	 * "number == number"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void equalSymbolTest1() throws IOException {
 
 		String text = "2 == 1";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a DBL_EQLS", sym.DBL_EQLS, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(2).intValue());
 	}
 	
 
 	/**
 	 * Tests for correct parsing of the statements with the 'equal to' comparison operator
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of INT_CONST DBL_EQLS INT_CONST
 	 * for strings like "2 == 1" and a token stream of ID DBL_EQLS ID for strings like
 	 * "number == number"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void equalSymbolTest2() throws IOException {
 
 		String text = "number == number";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be a ID", sym.ID, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a DBL_EQLS", sym.DBL_EQLS, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a ID", sym.ID, tokenList
 				.get(2).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with 'if'
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of IF L_PAREN ID GRTR ID R_PAREN
 	 * for strings like "if(number > number)"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void ifSymbolTest() throws IOException {
 
 		String text = "if(number > number)";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 6 tokens for the string '"
 				+ text + "'", 6, tokenList.size());
 		assertEquals("The first token should be a IF", sym.IF, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a L_PAREN", sym.L_PAREN, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a ID", sym.ID, tokenList
 				.get(2).intValue());
 		assertEquals("The fourth token should be a GRTR", sym.GRTR, tokenList
 				.get(3).intValue());
 		assertEquals("The fifth token should be a ID", sym.ID, tokenList
 				.get(4).intValue());
 		assertEquals("The sixth token should be a R_PAREN", sym.R_PAREN, tokenList
 				.get(5).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements surrounded by braces '{' and '}'
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of 
 	 * L_BRACE INT ID R_BRACE for strings like "{  int variable }"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void braceSymbolTest() throws IOException {
 
 		String text = "{  int variable }";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 4 tokens for the string '"
 				+ text + "'", 4, tokenList.size());
 		assertEquals("The first token should be a L_BRACE", sym.L_BRACE, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a INT", sym.INT, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a ID", sym.ID, tokenList
 				.get(2).intValue());
 		assertEquals("The fourth token should be a R_BRACE", sym.R_BRACE, tokenList
 				.get(3).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with an 'else' clause
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of IF L_PAREN ID GRTR ID R_PAREN
 	 * L_BRACE INT ID R_BRACE ELSE L_BRACE INT ID R_BRACE
 	 * for strings like "if(number > numberTwo){  int variable } else {  int other }"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void elseSymbolTest() throws IOException {
 
 		String text = "if(number > numberTwo){  int variable } else {  int other }";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 15 tokens for the string '"
 				+ text + "'", 15, tokenList.size());
 		assertEquals("The first token should be a IF", sym.IF, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a L_PAREN", sym.L_PAREN, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a ID", sym.ID, tokenList
 				.get(2).intValue());
 		assertEquals("The fourth token should be a GRTR", sym.GRTR, tokenList
 				.get(3).intValue());
 		assertEquals("The fifth token should be a ID", sym.ID, tokenList
 				.get(4).intValue());
 		assertEquals("The sixth token should be a R_PAREN", sym.R_PAREN, tokenList
 				.get(5).intValue());
 		assertEquals("The seventh token should be a L_BRACE", sym.L_BRACE, tokenList
 				.get(6).intValue());
 		assertEquals("The eighth token should be a INT", sym.INT, tokenList
 				.get(7).intValue());
 		assertEquals("The ninth token should be a ID", sym.ID, tokenList
 				.get(8).intValue());
 		assertEquals("The tenth token should be a R_BRACE", sym.R_BRACE, tokenList
 				.get(9).intValue());
 		assertEquals("The eleventh token should be a ELSE", sym.ELSE, tokenList
 				.get(10).intValue());
 		assertEquals("The twelfth token should be a L_BRACE", sym.L_BRACE, tokenList
 				.get(11).intValue());
 		assertEquals("The thirteenth token should be a INT", sym.INT, tokenList
 				.get(12).intValue());
 		assertEquals("The fourteenth token should be a ID", sym.ID, tokenList
 				.get(13).intValue());
 		assertEquals("The fifteenth token should be a R_BRACE", sym.R_BRACE, tokenList
 				.get(14).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with an 'elseif' clause
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of IF L_PAREN ID GRTR ID R_PAREN
 	 * L_BRACE INT ID SEMICOL R_BRACE ELSEIF L_PAREN ID DBL_EQLS ID R_PAREN L_BRACE INT ID SEMICOL R_BRACE 
 	 * ELSE L_BRACE INT ID SEMICOL R_BRACE
 	 * for strings like 
 	 * "if(number > numberTwo){  int variable; } elseif(number == numberTwo) {  int next; }
 	 * else {  int other; }"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void elseIfSymbolTest() throws IOException {
 
 		String text = "if(number > numberTwo){  int variable; } elseif (number == numberTwo)" +
 				"{  int next; } else {  int other; }";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 28 tokens for the string '"
 				+ text + "'", 28, tokenList.size());
 		assertEquals("The first token should be a IF", sym.IF, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a L_PAREN", sym.L_PAREN, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a ID", sym.ID, tokenList
 				.get(2).intValue());
 		assertEquals("The fourth token should be a GRTR", sym.GRTR, tokenList
 				.get(3).intValue());
 		assertEquals("The fifth token should be a ID", sym.ID, tokenList
 				.get(4).intValue());
 		assertEquals("The sixth token should be a R_PAREN", sym.R_PAREN, tokenList
 				.get(5).intValue());
 		assertEquals("The seventh token should be a L_BRACE", sym.L_BRACE, tokenList
 				.get(6).intValue());
 		assertEquals("The eighth token should be a INT", sym.INT, tokenList
 				.get(7).intValue());
 		assertEquals("The ninth token should be a ID", sym.ID, tokenList
 				.get(8).intValue());
 		assertEquals("The tenth token should be a SEMICOL", sym.SEMICOL, tokenList
 				.get(9).intValue());
 		assertEquals("The eleventh token should be a R_BRACE", sym.R_BRACE, tokenList
 				.get(10).intValue());
 		assertEquals("The twelfth token should be a ELSEIF", sym.ELSEIF, tokenList
 				.get(11).intValue());
 		assertEquals("The thirteenth token should be a L_PAREN", sym.L_PAREN, tokenList
 				.get(12).intValue());
 		assertEquals("The fourteenth token should be a ID", sym.ID, tokenList
 				.get(13).intValue());
 		assertEquals("The fifteenth token should be a DBL_EQLS", sym.DBL_EQLS, tokenList
 				.get(14).intValue());
 		assertEquals("The sixteenth token should be a ID", sym.ID, tokenList
 				.get(15).intValue());
 		assertEquals("The seventeenth token should be a R_PAREN", sym.R_PAREN, tokenList
 				.get(16).intValue());
 		assertEquals("The eighteenth token should be a L_BRACE", sym.L_BRACE, tokenList
 				.get(17).intValue());
 		assertEquals("The nineteenth token should be a INT", sym.INT, tokenList
 				.get(18).intValue());
 		assertEquals("The twentieth token should be a ID", sym.ID, tokenList
 				.get(19).intValue());
 		assertEquals("The twenty first token should be a SEMICOL", sym.SEMICOL, tokenList
 				.get(20).intValue());
 		assertEquals("The twenty second token should be a R_BRACE", sym.R_BRACE, tokenList
 				.get(21).intValue());
 		assertEquals("The twenty third token should be a ELSE", sym.ELSE, tokenList
 				.get(22).intValue());
 		assertEquals("The twenth fourth token should be a L_BRACE", sym.L_BRACE, tokenList
 				.get(23).intValue());
 		assertEquals("The twenty fifth token should be a INT", sym.INT, tokenList
 				.get(24).intValue());
 		assertEquals("The twenty sixth token should be a ID", sym.ID, tokenList
 				.get(25).intValue());
 		assertEquals("The twenty seventh token should be a SEMICOL", sym.SEMICOL, tokenList
 				.get(26).intValue());
 		assertEquals("The twenty eighth token should be a R_BRACE", sym.R_BRACE, tokenList
 				.get(27).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with the 'assignment' symbol
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of INT ID ASSIGN INT_CONST
 	 * for strings like "int variable = 1"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void assignmentSymbolTest1() throws IOException {
 
 		String text = "int variable = 1";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 4 tokens for the string '"
 				+ text + "'", 4, tokenList.size());
 		assertEquals("The first token should be a INT", sym.INT, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a ID", sym.ID, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a ASSIGN", sym.ASSIGN, tokenList
 				.get(2).intValue());
 		assertEquals("The fourth token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(3).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with the 'assignment' symbol
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of BOOL ID ASSIGN BOOL_CONST
 	 * for strings like "bool variable = true"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void assignmentSymbolTest2() throws IOException {
 
 		String text = "bool variable = true";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 4 tokens for the string '"
 				+ text + "'", 4, tokenList.size());
 		assertEquals("The first token should be a BOOL", sym.BOOL, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a ID", sym.ID, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a ASSIGN", sym.ASSIGN, tokenList
 				.get(2).intValue());
 		assertEquals("The fourth token should be a BOOL_CONST", sym.BOOL_CONST, tokenList
 				.get(3).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with the 'assignment' symbol
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of REAL ID ASSIGN REAL_CONST
 	 * for strings like "bool variable = true"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void assignmentSymbolTest3() throws IOException {
 
 		String text = "real variable = 1.3";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 4 tokens for the string '"
 				+ text + "'", 4, tokenList.size());
 		assertEquals("The first token should be a REAL", sym.REAL, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a ID", sym.ID, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a ASSIGN", sym.ASSIGN, tokenList
 				.get(2).intValue());
 		assertEquals("The fourth token should be a REAL_CONST", sym.REAL_CONST, tokenList
 				.get(3).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with the 'assignment' symbol
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of TEXT ID ASSIGN TEXT_LITERAL
 	 * for strings like "TEXT variable = friend"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void assignmentSymbolTest4() throws IOException {
 
		String text = "text variable = 'friend'";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 4 tokens for the string '"
 				+ text + "'", 4, tokenList.size());
 		assertEquals("The first token should be a TEXT", sym.TEXT, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a ID", sym.ID, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a ASSIGN", sym.ASSIGN, tokenList
 				.get(2).intValue());
 		assertEquals("The fourth token should be a TEXT_LITERAL", sym.TEXT_LITERAL, tokenList
 				.get(3).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with 'while'
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of WHILE L_PAREN ID 
 	 * LESS INT_CONST R_PAREN for strings like "while(count < 10)"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void whileSymbolTest() throws IOException {
 
 		String text = "while(count < 10)";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 6 tokens for the string '"
 				+ text + "'", 6, tokenList.size());
 		assertEquals("The first token should be a WHILE", sym.WHILE, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a L_PAREN", sym.L_PAREN, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a ID", sym.ID, tokenList
 				.get(2).intValue());
 		assertEquals("The fourth token should be a LESS", sym.LESS, tokenList
 				.get(3).intValue());
 		assertEquals("The fifth token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(4).intValue());
 		assertEquals("The sixth token should be a R_PAREN", sym.R_PAREN, tokenList
 				.get(5).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with the 'return' symbol
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of RETURN ID
 	 * for strings like "return variable"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void returnSymbolTest() throws IOException {
 
 		String text = "return variable";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 2 tokens for the string '"
 				+ text + "'", 2, tokenList.size());
 		assertEquals("The first token should be a RETURN", sym.RETURN, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a ID", sym.ID, tokenList
 				.get(1).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with brackets '[' and ']'
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of 
 	 * ID L_BRKT INT_CONST R_BRKT for strings like "a[2]"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void bracketSymbolTest() throws IOException {
 
 		String text = "a[2]";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 4 tokens for the string '"
 				+ text + "'", 4, tokenList.size());
 		assertEquals("The first token should be a ID", sym.ID, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a L_BRKT", sym.L_BRKT, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(2).intValue());
 		assertEquals("The fourth token should be a R_BRKT", sym.R_BRKT, tokenList
 				.get(3).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the statements with 'switch' statements
 	 * Also tests for the correct parsing of 'case' and 'default' since
 	 * they are inherent to switch statements
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of 
 	 * SWITCH L_PAREN ID R_PAREN CASE INT_CONST COL ID ASSIGN INT_CONST
 	 * CASE INT_CONST COL ID ASSIGN INT_CONST DEFAULT COL ID ASSIGN INT_CONST
 	 * for strings like "switch(test) case 1: variable=1 case 2: variable=2 default: variable=10"
 	 * @throws IOException
 	 */
 	@Test
 	public void switchSymbolTest() throws IOException {
 
 		String text = "switch(test) case 1: variable=1 case 2: variable=2 default: variable=10";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 21 tokens for the string '"
 				+ text + "'", 21, tokenList.size());
 		assertEquals("The first token should be a SWITCH", sym.SWITCH, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a L_PAREN", sym.L_PAREN, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a ID", sym.ID, tokenList
 				.get(2).intValue());
 		assertEquals("The fourth token should be a R_PAREN", sym.R_PAREN, tokenList
 				.get(3).intValue());
 		assertEquals("The fifth token should be a CASE", sym.CASE, tokenList
 				.get(4).intValue());
 		assertEquals("The sixth token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(5).intValue());
 		assertEquals("The seventh token should be a COL", sym.COL, tokenList
 				.get(6).intValue());
 		assertEquals("The eighth token should be a ID", sym.ID, tokenList
 				.get(7).intValue());
 		assertEquals("The ninth token should be a ASSIGN", sym.ASSIGN, tokenList
 				.get(8).intValue());
 		assertEquals("The tenth token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(9).intValue());
 		assertEquals("The eleventh token should be a CASE", sym.CASE, tokenList
 				.get(10).intValue());
 		assertEquals("The twelfth token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(11).intValue());
 		assertEquals("The thirteenth token should be a COL", sym.COL, tokenList
 				.get(12).intValue());
 		assertEquals("The fourteenth token should be a ID", sym.ID, tokenList
 				.get(13).intValue());
 		assertEquals("The fifteenth token should be a ASSIGN", sym.ASSIGN, tokenList
 				.get(14).intValue());
 		assertEquals("The sixteenth token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(15).intValue());
 		assertEquals("The seventeenth token should be a DEFAULT", sym.DEFAULT, tokenList
 				.get(16).intValue());
 		assertEquals("The eighteenth token should be a COL", sym.COL, tokenList
 				.get(17).intValue());
 		assertEquals("The nineteenth token should be a ID", sym.ID, tokenList
 				.get(18).intValue());
 		assertEquals("The twentieth token should be a ASSIGN", sym.ASSIGN, tokenList
 				.get(19).intValue());
 		assertEquals("The twenty first token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(20).intValue());
 		
 	}
 	
 	/**
 	 * Tests for correct parsing of the 'list' symbol
 	 * 
 	 * Specifically, ensures that Lexer produces a token stream of 
 	 * LIST LESS INT GRTR ID L_PAREN INT_CONST R_PAREN  
 	 * for strings like "list<int> myList(5)"
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void listSymbolTest() throws IOException {
 
 		String text = "list<int> myList(5)";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 8 tokens for the string '"
 				+ text + "'", 8, tokenList.size());
 		assertEquals("The first token should be a LIST", sym.LIST, tokenList
 				.get(0).intValue());
 		assertEquals("The second token should be a LESS", sym.LESS, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be a INT", sym.INT, tokenList
 				.get(2).intValue());
 		assertEquals("The fourth token should be a GRTR", sym.GRTR, tokenList
 				.get(3).intValue());
 		assertEquals("The first token should be a ID", sym.ID, tokenList
 				.get(4).intValue());
 		assertEquals("The second token should be a L_PAREN", sym.L_PAREN, tokenList
 				.get(5).intValue());
 		assertEquals("The third token should be a INT_CONST", sym.INT_CONST, tokenList
 				.get(6).intValue());
 		assertEquals("The fourth token should be a R_PAREN", sym.R_PAREN, tokenList
 				.get(7).intValue());
 	}
 	
 	/**
 	 * Tests for correct parsing of the '@Main' symbol
 	 * 
 	 * Specifically, ensures that Lexer produces token streams of 
 	 * MAIN L_BRACE R_BRACE  for strings like "@Main{}" 
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void mainSymbolTest() throws IOException {
 
 		String text = "@Main{}";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be an MAIN", sym.MAIN,
 				tokenList.get(0).intValue());
 		assertEquals("The second token should be a L_BRACE", sym.L_BRACE, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be an R_BRACE", sym.R_BRACE,
 				tokenList.get(2).intValue());
 		
 	}
 	
 	/**
 	 * Tests for correct parsing of the '@Functions' symbol
 	 * 
 	 * Specifically, ensures that Lexer produces token streams of 
 	 * FUNCTIONS L_BRACE R_BRACE  for strings like "@Functions{}" 
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void functionsSymbolTest() throws IOException {
 
 		String text = "@Functions{}";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be an FUNCTIONS", sym.FUNCTIONS,
 				tokenList.get(0).intValue());
 		assertEquals("The second token should be a L_BRACE", sym.L_BRACE, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be an R_BRACE", sym.R_BRACE,
 				tokenList.get(2).intValue());
 		
 	}
 	
 	/**
 	 * Tests for correct parsing of the '@Map' symbol
 	 * 
 	 * Specifically, ensures that Lexer produces token streams of 
 	 * MAP L_BRACE R_BRACE  for strings like "@Map{}" 
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void mapSymbolTest() throws IOException {
 
 		String text = "@Map{}";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be an MAP", sym.MAP,
 				tokenList.get(0).intValue());
 		assertEquals("The second token should be a L_BRACE", sym.L_BRACE, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be an R_BRACE", sym.R_BRACE,
 				tokenList.get(2).intValue());
 		
 	}
 	
 	/**
 	 * Tests for correct parsing of the '@Reduce' symbol
 	 * 
 	 * Specifically, ensures that Lexer produces token streams of 
 	 * REDUCE L_BRACE R_BRACE  for strings like "@Reduce{}" 
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void reduceSymbolTest() throws IOException {
 
 		String text = "@Reduce{}";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 3 tokens for the string '"
 				+ text + "'", 3, tokenList.size());
 		assertEquals("The first token should be an REDUCE", sym.REDUCE,
 				tokenList.get(0).intValue());
 		assertEquals("The second token should be a L_BRACE", sym.L_BRACE, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be an R_BRACE", sym.R_BRACE,
 				tokenList.get(2).intValue());
 		
 	}
 	
 	/**
 	 * Tests for correct parsing of the 'arrow' symbol
 	 * 
 	 * Specifically, ensures that Lexer produces token streams of 
 	 * MAP L_PAREN INT ID COMMA REAL ID R_PAREN ARROW L_PAREN INT COMMA
 	 * INT R_PAREN L_BRACE R_BRACE
 	 * for strings like "@Map(int num, real numTwo) -> (int, int){}" 
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void arrowSymbolTest() throws IOException {
 
 		String text = "@Map(int num, real numTwo) -> (int, int){}";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 16 tokens for the string '"
 				+ text + "'", 16, tokenList.size());
 		assertEquals("The first token should be an MAP", sym.MAP,
 				tokenList.get(0).intValue());
 		assertEquals("The second token should be a L_PAREN", sym.L_PAREN, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be an INT", sym.INT,
 				tokenList.get(2).intValue());
 		assertEquals("The fourth token should be an ID", sym.ID,
 				tokenList.get(3).intValue());
 		assertEquals("The fifth token should be a COMMA", sym.COMMA, tokenList
 				.get(4).intValue());
 		assertEquals("The sixth token should be an REAL", sym.REAL,
 				tokenList.get(5).intValue());
 		assertEquals("The seventh token should be an ID", sym.ID,
 				tokenList.get(6).intValue());
 		assertEquals("The eighth token should be a R_PAREN", sym.R_PAREN, tokenList
 				.get(7).intValue());
 		assertEquals("The ninth token should be an ARROW", sym.ARROW,
 				tokenList.get(8).intValue());
 		assertEquals("The tenth token should be an L_PAREN", sym.L_PAREN,
 				tokenList.get(9).intValue());
 		assertEquals("The eleventh token should be a INT", sym.INT, tokenList
 				.get(10).intValue());
 		assertEquals("The twelfth token should be an COMMA", sym.COMMA,
 				tokenList.get(11).intValue());
 		assertEquals("The thirteenth token should be an INT", sym.INT,
 				tokenList.get(12).intValue());
 		assertEquals("The fourteenth token should be a R_PAREN", sym.R_PAREN, tokenList
 				.get(13).intValue());
 		assertEquals("The fifteenth token should be an L_BRACE", sym.L_BRACE,
 				tokenList.get(14).intValue());
 		assertEquals("The sixteenth token should be an R_BRACE", sym.R_BRACE,
 				tokenList.get(15).intValue());
 		
 	}
 	
 	/**
 	 * Tests for correct parsing of the 'foreach' and 'in' symbols
 	 * 
 	 * Specifically, ensures that Lexer produces token streams of 
 	 * FOREACH ID IN ID  for strings like "foreach variable in myList" 
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void foreachSymbolTest() throws IOException {
 
 		String text = "foreach variable in myList";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 4 tokens for the string '"
 				+ text + "'", 4, tokenList.size());
 		assertEquals("The first token should be an FOREACH", sym.FOREACH,
 				tokenList.get(0).intValue());
 		assertEquals("The second token should be a ID", sym.ID, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be an IN", sym.IN,
 				tokenList.get(2).intValue());
 		assertEquals("The third token should be an ID", sym.ID,
 				tokenList.get(3).intValue());
 		
 	}
 	
 	/**
 	 * Tests for correct parsing of the 'for' statements
 	 * 
 	 * Specifically, ensures that Lexer produces token streams of 
 	 * FOR L_PAREN INT ID ASSIGN INT_CONST SEMICOL ID LESS INT_CONST
 	 * SEMICOL ID INCR R_PAREN
 	 * for strings like "for (int i = 0; i < 10; i++)" 
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void forSymbolTest() throws IOException {
 
 		String text = "for (int i = 0; i < 10; i++)";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 14 tokens for the string '"
 				+ text + "'", 14, tokenList.size());
 		assertEquals("The first token should be an FOR", sym.FOR,
 				tokenList.get(0).intValue());
 		assertEquals("The second token should be a L_PAREN", sym.L_PAREN, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be an INT", sym.INT,
 				tokenList.get(2).intValue());
 		assertEquals("The fourth token should be an ID", sym.ID,
 				tokenList.get(3).intValue());
 		assertEquals("The fifth token should be an ASSIGN", sym.ASSIGN,
 				tokenList.get(4).intValue());
 		assertEquals("The sixth token should be an INT_CONST", sym.INT_CONST,
 				tokenList.get(5).intValue());
 		assertEquals("The seventh token should be an SEMICOL", sym.SEMICOL,
 				tokenList.get(6).intValue());
 		assertEquals("The eighth token should be an ID", sym.ID,
 				tokenList.get(7).intValue());
 		assertEquals("The ninth token should be an LESS", sym.LESS,
 				tokenList.get(8).intValue());
 		assertEquals("The tenth token should be an INT_CONST", sym.INT_CONST,
 				tokenList.get(9).intValue());
 		assertEquals("The eleventh token should be an SEMICOL", sym.SEMICOL,
 				tokenList.get(10).intValue());
 		assertEquals("The twelfth token should be an ID", sym.ID,
 				tokenList.get(11).intValue());
 		assertEquals("The thirteenth token should be an INCR", sym.INCR,
 				tokenList.get(12).intValue());
 		assertEquals("The fourteenth token should be an R_PAREN", sym.R_PAREN,
 				tokenList.get(13).intValue());
 		
 	}
 	
 	/**
 	 * Tests for correct parsing of a program with the 
 	 * @Main, @Map, @Reduce and @Functions sections
 	 * 
 	 * Specifically, ensures that Lexer produces token streams of 
 	 * FUNCTIONS L_BRACE R_BRACE MAP L_PAREN INT ID COMMA REAL ID R_PAREN ARROW L_PAREN INT COMMA
 	 * INT R_PAREN L_BRACE R_BRACE 
 	 * REDUCE L_PAREN INT ID COMMA REAL ID R_PAREN ARROW L_PAREN INT COMMA
 	 * INT R_PAREN L_BRACE R_BRACE
 	 * MAIN L_BRACE R_BRACE
 	 * for strings like "@Functions{}@Map(int num, real numTwo) -> (int, int){}
 	 * @Reduce(int num, real numTwo) -> (int, int){}@Main{}" 
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void programStructureTest() throws IOException {
 
 		String text = "@Functions{}@Map(int num, real numTwo) -> (int, int){}" +
 						"@Reduce(int num, real numTwo) -> (int, int){}@Main{}";
 		StringReader stringReader = new StringReader(text);
 		Lexer lexer = new Lexer(stringReader);
 		List<Integer> tokenList = new ArrayList<Integer>();
 		Symbol token = lexer.next_token();
 		
 		while (token.sym != sym.EOF) {
 			tokenList.add(token.sym);
 			token = lexer.next_token();
 		}
 
 		assertEquals("It should produce 38 tokens for the string '"
 				+ text + "'", 38, tokenList.size());
 		assertEquals("The first token should be an FUNCTIONS", sym.FUNCTIONS,
 				tokenList.get(0).intValue());
 		assertEquals("The second token should be a L_BRACE", sym.L_BRACE, tokenList
 				.get(1).intValue());
 		assertEquals("The third token should be an R_BRACE", sym.R_BRACE,
 				tokenList.get(2).intValue());
 		assertEquals("The fourth token should be an MAP", sym.MAP,
 				tokenList.get(3).intValue());
 		assertEquals("The fifth token should be a L_PAREN", sym.L_PAREN, tokenList
 				.get(4).intValue());
 		assertEquals("The sixth token should be an INT", sym.INT,
 				tokenList.get(5).intValue());
 		assertEquals("The seventh token should be an ID", sym.ID,
 				tokenList.get(6).intValue());
 		assertEquals("The eighth token should be a COMMA", sym.COMMA, tokenList
 				.get(7).intValue());
 		assertEquals("The ninth token should be an REAL", sym.REAL,
 				tokenList.get(8).intValue());
 		assertEquals("The tenth token should be an ID", sym.ID,
 				tokenList.get(9).intValue());
 		assertEquals("The eleventh token should be a R_PAREN", sym.R_PAREN, tokenList
 				.get(10).intValue());
 		assertEquals("The twelfth token should be an ARROW", sym.ARROW,
 				tokenList.get(11).intValue());
 		assertEquals("The thirteenth token should be an L_PAREN", sym.L_PAREN,
 				tokenList.get(12).intValue());
 		assertEquals("The fourteenth token should be a INT", sym.INT, tokenList
 				.get(13).intValue());
 		assertEquals("The fifteenth token should be an COMMA", sym.COMMA,
 				tokenList.get(14).intValue());
 		assertEquals("The sixteenth token should be an INT", sym.INT,
 				tokenList.get(15).intValue());
 		assertEquals("The seventeenth token should be a R_PAREN", sym.R_PAREN, tokenList
 				.get(16).intValue());
 		assertEquals("The eighteenth token should be a L_BRACE", sym.L_BRACE, tokenList
 				.get(17).intValue());
 		assertEquals("The nineteenth token should be an R_BRACE", sym.R_BRACE,
 				tokenList.get(18).intValue());
 		assertEquals("The twentieth token should be an REDUCE", sym.REDUCE,
 				tokenList.get(19).intValue());
 		assertEquals("The twenty first token should be a L_PAREN", sym.L_PAREN, tokenList
 				.get(20).intValue());
 		assertEquals("The twenty second token should be an INT", sym.INT,
 				tokenList.get(21).intValue());
 		assertEquals("The twenty third token should be an ID", sym.ID,
 				tokenList.get(22).intValue());
 		assertEquals("The twenty fourth token should be a COMMA", sym.COMMA, tokenList
 				.get(23).intValue());
 		assertEquals("The twenty fifth token should be an REAL", sym.REAL,
 				tokenList.get(24).intValue());
 		assertEquals("The twenty sixth token should be an ID", sym.ID,
 				tokenList.get(25).intValue());
 		assertEquals("The twenty seventh token should be a R_PAREN", sym.R_PAREN, tokenList
 				.get(26).intValue());
 		assertEquals("The twenty eighth token should be an ARROW", sym.ARROW,
 				tokenList.get(27).intValue());
 		assertEquals("The twenty ninth token should be an L_PAREN", sym.L_PAREN,
 				tokenList.get(28).intValue());
 		assertEquals("The thirtieth token should be a INT", sym.INT, tokenList
 				.get(29).intValue());
 		assertEquals("The thirty first token should be an COMMA", sym.COMMA,
 				tokenList.get(30).intValue());
 		assertEquals("The thirty second token should be an INT", sym.INT,
 				tokenList.get(31).intValue());
 		assertEquals("The thirty third token should be a R_PAREN", sym.R_PAREN, tokenList
 				.get(32).intValue());
 		assertEquals("The thirty fourth token should be a L_BRACE", sym.L_BRACE, tokenList
 				.get(33).intValue());
 		assertEquals("The thirty fifth token should be an R_BRACE", sym.R_BRACE,
 				tokenList.get(34).intValue());
 		assertEquals("The thirty sixth token should be an MAIN", sym.MAIN,
 				tokenList.get(35).intValue());
 		assertEquals("The thirty seventh token should be a L_BRACE", sym.L_BRACE, tokenList
 				.get(36).intValue());
 		assertEquals("The thirty eighth token should be an R_BRACE", sym.R_BRACE,
 				tokenList.get(37).intValue());
 		
 	}
 }
