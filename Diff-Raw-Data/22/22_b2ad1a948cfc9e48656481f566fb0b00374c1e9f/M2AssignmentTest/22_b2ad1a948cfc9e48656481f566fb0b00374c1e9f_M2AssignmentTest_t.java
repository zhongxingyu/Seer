 package swp_compiler_ss13.fuc.parser;
 
 import static org.junit.Assert.assertNotNull;
 
 import java.io.ByteArrayInputStream;
 
 import org.apache.log4j.BasicConfigurator;
 import org.junit.Test;
 
 import swp_compiler_ss13.common.ast.AST;
 import swp_compiler_ss13.common.ast.nodes.binary.BinaryExpressionNode.BinaryOperator;
 import swp_compiler_ss13.common.lexer.Lexer;
 import swp_compiler_ss13.common.report.ReportLog;
 import swp_compiler_ss13.common.types.primitive.LongType;
 import swp_compiler_ss13.fuc.ast.ASTFactory;
 import swp_compiler_ss13.fuc.errorLog.ReportLogImpl;
 import swp_compiler_ss13.fuc.lexer.LexerImpl;
 import swp_compiler_ss13.fuc.parser.generator.ALRGenerator;
 import swp_compiler_ss13.fuc.parser.generator.LR1Generator;
 import swp_compiler_ss13.fuc.parser.generator.items.LR1Item;
 import swp_compiler_ss13.fuc.parser.generator.states.LR1State;
 import swp_compiler_ss13.fuc.parser.grammar.Grammar;
 import swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar;
 import swp_compiler_ss13.fuc.parser.parser.LRParser;
 import swp_compiler_ss13.fuc.parser.parser.LexerWrapper;
 import swp_compiler_ss13.fuc.parser.parser.tables.LRParsingTable;
 
 public class M2AssignmentTest {
 	static {
 		BasicConfigurator.configure();
 	}
 
 //	@Test
 //	public void testAssignment() {
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
 //		// Run LR-parser with table
 //		LRParser lrParser = new LRParser();
 //		LexerWrapper lexWrapper = new LexerWrapper(lexer, grammar);
 //		ReportLog reportLog = new ReportLogImpl();
 //		AST ast = lrParser.parse(lexWrapper, reportLog, table);
 //
 //		checkAst(ast);
 //	}
 
 	private static void checkAst(AST ast) {
 		assertNotNull(ast);
 		ASTFactory factory = new ASTFactory();
 		factory.addDeclaration("a", new LongType());
 		factory.addDeclaration("b", new LongType());
 		factory.addDeclaration("c", new LongType());
 		factory.addAssignment(factory.newBasicIdentifier("a"), factory.newLiteral("4", new LongType()));
 		factory.addAssignment(factory.newBasicIdentifier("b"), factory.newLiteral("3", new LongType()));
 		factory.addAssignment(factory.newBasicIdentifier("c"), factory.newLiteral("2", new LongType()));
 		
 		factory.addAssignment(factory.newBasicIdentifier("a"),
 					factory.newAssignment(factory.newBasicIdentifier("b"), 
 					factory.newLiteral("4", new LongType())));
 		
 		factory.addAssignment(factory.newBasicIdentifier("c"),
 				factory.newBinaryExpression(BinaryOperator.ADDITION,
						factory.newBinaryExpression(BinaryOperator.ADDITION, factory.newBasicIdentifier("a"), factory.newBasicIdentifier("b")),
						factory.newBasicIdentifier("c")));
 		factory.addReturn(factory.newBasicIdentifier("c"));
 		
 		AST expectedAst = factory.getAST();
 		ASTComparator.compareAST(expectedAst, ast);
 	}
 
  
 	@Test
 	public void testAssignmentOrgLexer() throws Exception {
 		String input = "# returns 10\n"
 				+ "# prints nothing\n"
 				+ "long a;\n"
 				+ "long b;\n"
 				+ "long c;\n"
 				+ "\n"
 				+ "\n"
 				+ "a = 4;\n"
 				+ "b = 3;\n"
 				+ "c = 2;\n"
 				+ "\n"
 				+ "a = b = 4;\n"
 				+ "c = a + b + c;\n"
 				+ "\n"
 				+ "return c;\n";
 //		String input = GrammarTestHelper.loadExample("test2.prog");
 //		String input = "\"test1\ntest2\"";
 //		String input = "string s;\ns = \"Hallo\nWelt\";";
 		
 		// Generate parsing table
 		Grammar grammar = new ProjectGrammar.Complete().getGrammar();
 		ALRGenerator<LR1Item, LR1State> generator = new LR1Generator(grammar);
 		LRParsingTable table = generator.getParsingTable();
 
 		// Simulate input
 		Lexer lexer = new LexerImpl();
 		lexer.setSourceStream(new ByteArrayInputStream(input.getBytes()));
 
 		// Run LR-parser with table
 		LRParser lrParser = new LRParser();
 		LexerWrapper lexWrapper = new LexerWrapper(lexer, grammar);
 		ReportLog reportLog = new ReportLogImpl();
 		AST ast = lrParser.parse(lexWrapper, reportLog, table);
 		checkAst(ast);
 	}
 }
