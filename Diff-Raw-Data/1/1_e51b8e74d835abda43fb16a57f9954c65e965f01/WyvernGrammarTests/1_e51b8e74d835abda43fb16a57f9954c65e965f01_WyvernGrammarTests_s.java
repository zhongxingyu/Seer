 /**
  * 
  */
 package compiler.test;
 
 import static compiler.wyvern.WyvernLexer.*;
 import static compiler.wyvern.WyvernParser.*;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import compiler.Symbol;
 import compiler.SymbolType;
 import compiler.Tuples;
 import compiler.Utils;
 import compiler.parse.Parser;
 import compiler.wyvern.WyvernComments;
import compiler.wyvern.WyvernParser;
 
 /**
  * @author Michael
  * 
  */
 public class WyvernGrammarTests {
 	private static class TestCase extends
 			Tuples.Trio<String, SymbolType[], SymbolType> {
 		public TestCase(String item1, SymbolType[] item2, SymbolType item3) {
 			super(item1, item2, item3);
 		}
 
 		public static TestCase make(String program, SymbolType overallType,
 				SymbolType... tokenTypes) {
 			return new TestCase(program, tokenTypes.length > 0 ? tokenTypes
 					: null, overallType);
 		}
 	}
 
 	private static final List<TestCase> TEST_CASES;
 
 	static {
 		List<TestCase> testCases = new ArrayList<TestCase>();
 
 		// lexical tests
 		testCases.addAll(Utils.set(TestCase.make("int", null, INT_ALIAS),
 				TestCase.make("obj 2 + 20.25 i280.2", null, OBJECT_ALIAS, INT,
 						PLUS, REAL, IDENTIFIER, REAL),
 				TestCase.make("'a''\\n'/*x/**/*/", null, CHAR,
 						CHAR, COMMENT_START,
 						COMMENT_TEXT, COMMENT_START,
 						COMMENT_END, COMMENT_END),
 				TestCase.make("\n\n\t \" \\\"\"", null,
 						STRING_TERMINATOR, STRING_TEXT,
 						ESCAPE, STRING_TEXT,
 						STRING_TERMINATOR), TestCase.make(
 						"a.b.C, str", null, IDENTIFIER,
 						ACCESS, IDENTIFIER,
 						ACCESS, TYPE_IDENTIFIER,
 						COMMA, STRING_ALIAS)));
 
 		TEST_CASES = Collections.unmodifiableList(testCases);
 	}
 
 	public static void lexerTests() {
 		Utils.check(LEXER != null, "lexer DNE");
 
 		for (TestCase testCase : TEST_CASES) {
 			if (testCase.item2() != null) {
 				List<Symbol> symbols = Utils.toList(LEXER
 						.lex(new StringReader(testCase.item1())));
 				List<SymbolType> types = new ArrayList<SymbolType>(
 						symbols.size());
 				for (Symbol symbol : symbols) {
 					if (!symbol.type().equals(CONTEXT.eofType())) {
 						types.add(symbol.type());
 					}
 				}
 
 				Utils.check(types.equals(Arrays.asList(testCase.item2())),
 						types + " != " + Arrays.asList(testCase.item2()));
 			}
 		}
 	}
 	
 	public static void parserTests() {
 		Utils.check(PARSER != null, "parser DNE");
 		
 		for (TestCase testCase : TEST_CASES) {
 			if (testCase.item3() != null) {
 				List<Symbol> tokens = Utils.toList(LEXER
 						.lex(new StringReader(testCase.item1())));
 				Map<Symbol, Symbol> map = new HashMap<Symbol, Symbol>();
 				List<Symbol> withoutComments = WyvernComments.stripComments(tokens, map);
 				Parser.Result result = PARSER.parse(withoutComments.iterator());
 				
 				Utils.check(result.succeeded());
 				Utils.check(result.parseTree().type().equals(testCase.item3()));
 			}
 		}
 	}
 
 	public static void commentTests() {
 		String prog;
 		Map<Symbol, Symbol> comments;
 		List<Symbol> tokens;
 
 		prog = "/*text*/int a";
 		comments = new LinkedHashMap<Symbol, Symbol>();
 		tokens = WyvernComments.stripComments(
 				Utils.toList(LEXER.lex(new StringReader(prog))),
 				comments);
 		Utils.check(tokens.size() == 3);
 		Utils.check(tokens.get(0).type().equals(INT_ALIAS));
 		Utils.check(comments.size() == 1);
 		Utils.check(comments.values().iterator().next() == tokens.get(0));
 
 		prog = "/**//*/*/text**/int a*/";
 		comments = new LinkedHashMap<Symbol, Symbol>();
 		tokens = WyvernComments.stripComments(
 				Utils.toList(LEXER.lex(new StringReader(prog))),
 				comments);
 		Utils.check(tokens.size() == 1);
 		Utils.check(tokens.get(0).type().equals(CONTEXT.eofType()));
 		Utils.check(comments.size() == 2);
 		Utils.check(comments.values().iterator().next() == null);
 		Utils.check(comments.keySet().iterator().next().text().equals("/**/"));
 	}
 
 	public static void main(String[] args) {
 		lexerTests();
 
 		commentTests();
 
 		parserTests();
 		
 		System.out.println("All wyvern grammar tests passed!");
 	}
 }
