 package malice_grammar;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 
import malice_grammar.malice_grammarParser.program_return;

 import org.antlr.runtime.ANTLRFileStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.TokenStream;
 import org.antlr.runtime.tree.Tree;
 import org.junit.Test;
 
 import symbol_table.ExpressionChecker;
 import symbol_table.SymbolTable;
 
 
 public class TestSuiteForMainProgram
 {
 	private boolean runProgram(String filepath) throws IOException, RecognitionException
 	{
 		new ExpressionChecker();
 		SymbolTable table = new SymbolTable();
 		System.out.println(filepath + " started");
 		CharStream input = new ANTLRFileStream(filepath); 
 		malice_grammarLexer lexer = new malice_grammarLexer(input );
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		if (lexer.getNumberOfSyntaxErrors()==0)
		{
			malice_grammarParser parser = new malice_grammarParser(tokens ) ;
			program_return prog = parser.program();
			if (parser.getNumberOfSyntaxErrors()==0) {
				Tree tree =  (Tree) prog.getTree() ;
				System.out.println(tree.toStringTree());
				SemanticVerifier.checkProgramSemantics(tree, table);
			}
 		}
 		System.out.println(filepath + " done");
 		return true;
 	}
 	
 //	@Test
 //	public void testForValidPrograms( ) throws IOException, RecognitionException
 //	{
 //		String filepath = "c:/Users/varun/Documents/Malice/" +
 //				"malice_examples/my_tests/test_scope.g";
 //		assertTrue( runProgram(filepath));
 //		String[] ps = ("test01.alice test02.alice test03.alice " +
 //				"test04.alice test05.alice test06.alice test07.alice test08.alice test09.alice " +
 //				"test10.alice test11.alice test12.alice test13.alice test14.alice test15.alice vectorFunctions.alice").split(" ") ;
 //		for(String p: ps) {
 //			assertTrue( runProgram("c:/Users/varun/Documents/Malice/malice_examples/valid/" + p));
 //		}
 //	}
 	
 	@Test
 	public void testAllInvalidPrograms( ) 
 		throws IOException, RecognitionException 
 	{
 		String[] ps = ("test01.alice test02.alice test03.alice test04.alice" +
 				" test05.alice test06.alice test07.alice test08.alice " +
 				"test09.alice test10.alice test11.alice test12.alice " +
 				"test13.alice test14.alice test15.alice test16.alice " +
 				"test17.alice test18.alice test19.alice test20.alice " +
 				"test21.alice test22.alice test23.alice test24.alice " +
 				"test25.alice test26.alice test27.alice test28.alice " +
 				"test29.alice test30.alice test31.alice test32.alice " +
 				"test33.alice test34.alice test35.alice " +
 				"test36.alice").split(" ") ;
		assertTrue( runProgram("c:/Users/varun/Documents/Malice" +
				"/malice_examples/invalid/" + "test01.alice"));
//		for(String p : ps) {
//			assertTrue( runProgram("c:/Users/varun/Documents/Malice" +
//					"/malice_examples/invalid/" + p));
//		}
 	}
 }
