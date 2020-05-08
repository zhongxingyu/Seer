 package swp_compiler_ss13.fuc.parser;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 import static swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar.Complete.assign;
 import static swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar.Complete.factor;
 import static swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar.Complete.id;
 import static swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar.Complete.loc;
 import static swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar.Complete.sem;
 import static swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar.Complete.type;
 
 import org.junit.Test;
 
 import swp_compiler_ss13.common.ast.AST;
 import swp_compiler_ss13.common.parser.Parser;
 import swp_compiler_ss13.fuc.parser.generator.ALRGenerator;
 import swp_compiler_ss13.fuc.parser.generator.LR0Generator;
 import swp_compiler_ss13.fuc.parser.generator.items.LR0Item;
 import swp_compiler_ss13.fuc.parser.generator.states.LR0State;
 import swp_compiler_ss13.fuc.parser.grammar.Grammar;
 import swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar;
 
 public class ParserImplTest {
 	@Test
 	public void testParsingComplete() {
		/*
 		Parser parser = new ParserImpl();
 		parser.setLexer(new TestLexer(type, id, sem, loc, assign, factor));
 
 		AST ast = parser.getParsedAST();
 		assertNotNull(ast);
		*/
 	}
 
 	@Test
 	public void testLR0ShiftReduceOnWholeGrammar() {
 		// Generate parsing table
 		Grammar grammar = new ProjectGrammar.Complete().getGrammar();
 		ALRGenerator<LR0Item, LR0State> generator = new LR0Generator(grammar);
 		try {
 			generator.getParsingTable();
 			fail("Expected shift-reduce exception in whole grammar!");
 		} catch (RuntimeException err) {
 			// Expected
 		}
 	}
 }
