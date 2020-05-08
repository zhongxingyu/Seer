 package swp_compiler_ss13.fuc.parser;
 
 import static org.junit.Assert.fail;
 import static swp_compiler_ss13.fuc.parser.GrammarTestHelper.tokens;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.log4j.BasicConfigurator;
 import org.junit.Test;
 
 import swp_compiler_ss13.common.lexer.TokenType;
 import swp_compiler_ss13.common.report.ReportType;
 import swp_compiler_ss13.fuc.errorLog.LogEntry;
 import swp_compiler_ss13.fuc.errorLog.LogEntry.Type;
 import swp_compiler_ss13.fuc.errorLog.ReportLogImpl;
 import swp_compiler_ss13.fuc.lexer.token.TokenImpl;
 import swp_compiler_ss13.fuc.parser.parser.ParserException;
 
 public class M1MultiplePlusesInExpTest {
 	static {
 		BasicConfigurator.configure();
 	}
 
 //	@Test
 //	public void testErrorMultiplePlusesInExp() {
 //
 //		// String input = "# return 27\n"
 //		// + "long l;\n"
 //		// + "l = 10 +\n"
 //		// + "23 # - 23\n"
 //		// + "- 23\n"
 //		// + "+ 100 /\n"
 //		// + "\n"
 //		// + "2\n"
 //		// + "- 30\n"
 //		// + "- 9 / 3;\n"
 //		// + "return l;\n";
 //		// Generate parsing table
 //		Grammar grammar = new ProjectGrammar.Complete().getGrammar();
 //		ALRGenerator<LR0Item, LR0State> generator = new LR0Generator(grammar);
 //		LRParsingTable table = generator.getParsingTable();
 //
 //		// Simulate input
 //		Lexer lexer = new TestLexer(
 //				new TestToken("long", TokenType.LONG_SYMBOL), id("l"), t(sem),
 //				id("l"), t(assignop), num(10), t(plus), num(23), t(minus),
 //				num(23), t(plus), num(100), t(div), num(2), t(minus), num(30),
 //				t(minus), num(9), t(div), num(3), t(sem), t(returnn), id("l"),
 //				t(sem), t(Terminal.EOF));
 //	
 //		ReportLog reportLog = new ReportLogImpl();
 //	
 //		// Check output
 //		try {
 //			GrammarTestHelper.parseToAst(lexer, reportLog);
 //			fail("Expected invalid ids error!");
 //		} catch (ParserException err) {
 //			// Check for correct error
 //			GrammarTestHelper.compareReportLogEntries(createExpectedEntries(), reportLog.getEntries(), false);
 //		}
 //	}
 
 	@Test
 	public void testErrorMultiplePlusesInExpOrgLexer() throws Exception {
 		String input = "# error: too many pluses in an expression\n"
 				+ "long foo;\n"
 				+ "long bar;\n"
 				+ "foo = 3;\n"
 				+ "bar = foo ++ 1;\n";
 		
 		ReportLogImpl reportLog = new ReportLogImpl();
 
 		// Check output
 		try {
 			GrammarTestHelper.parseToAst(input, reportLog);
 			fail("Expected invalid ids error!");
 		} catch (ParserException err) {
 			// Check for correct error
 			GrammarTestHelper.compareReportLogEntries(createExpectedEntries(), reportLog.getEntries());
 		}
 	}
 	
 	private static List<LogEntry> createExpectedEntries() {
 		// Expected entries
 		List<LogEntry> expected = new LinkedList<>();
		expected.add(new LogEntry(Type.ERROR, ReportType.UNRECOGNIZED_TOKEN, tokens(new TokenImpl("++", TokenType.NOT_A_TOKEN, 5, 11)), ""));
 		return expected;
 	}
 }
