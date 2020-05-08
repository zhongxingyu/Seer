 import static org.junit.Assert.*;
 
 import java.io.File;
 
 import org.junit.Test;
 
 /**
  * LL1 Parser JUnit Tests
  */
 public class LL1ParserTest {
 	@Test
 	public void testLL1ParserTest1() throws LL1ParseException {
 		testGrammar("tests/test1/grammar.txt", "tests/test1/test.txt");
 	}
 	
 	@Test
 	public void testLL1ParserTest2() throws LL1ParseException {
 		testGrammar("tests/test2/grammar.txt", "tests/test2/test.txt");
 	}
 	
 	@Test
 	public void testLL1ParserBalancedParentheses() throws LL1ParseException {
 		testGrammar("tests/balanced_parentheses/grammar.txt", "tests/balanced_parentheses/test.txt");
 	}
 	
 	@Test
 	public void testLL1ParserTiny() throws LL1ParseException {
 		testGrammar("tests/tiny/grammar.txt", "tests/tiny/test.txt");
 	}
 	
 	@Test
 	public void testLL1ParserTinyPrecedence() throws LL1ParseException {
 		testGrammar("tests/tiny_precedence/grammar.txt", "tests/tiny_precedence/test.txt");
 	}
 
 	/**
 	 * Quickly create an LL1 Parser with the given file paths and assert the result of the parse.
 	 * @param grammarPath Path to the grammar file
 	 * @param pathToParse Path to the file to parse
 	 * @throws LL1ParseException
 	 */
 	public void testGrammar(String grammarPath, String pathToParse) throws LL1ParseException {
 		LL1Parser parser = new LL1Parser(new File(grammarPath), new File(pathToParse));
 		assertTrue(parser.parse());
 	}
 }
