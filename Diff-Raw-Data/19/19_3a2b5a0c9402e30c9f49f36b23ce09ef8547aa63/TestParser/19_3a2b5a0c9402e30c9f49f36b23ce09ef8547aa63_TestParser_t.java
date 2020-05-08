 package cs4120.der34dlc287lg342.xi.tests;
 
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.ArrayList;
 
 import cs4120.der34dlc287lg342.xi.XiParser;
 
 import edu.cornell.cs.cs4120.util.VisualizableTreeNode;
 import edu.cornell.cs.cs4120.xi.AbstractSyntaxNode;
 import edu.cornell.cs.cs4120.xi.CompilationException;
 
 import edu.cornell.cs.cs4120.xi.parser.Parser;
 
 import junit.framework.TestCase;
 
 public class TestParser extends TestCase {
 	
 	public ArrayList<String> traverse(VisualizableTreeNode node, ArrayList<String> acc){
 		acc.add(node.label());
 		for (VisualizableTreeNode next : node.children()){
 			acc = traverse(next, acc);
 		}
 		return acc;
 	}
 	
 	public void checkType(Parser p, String[] expected){
 		AbstractSyntaxNode program = p.parse();
 		ArrayList<String> observed = traverse(program, new ArrayList<String>());
 		
 		for (String label : expected){
 			assertFalse(observed.isEmpty());
 			assertEquals(label, observed.remove(0));
 		}
 		
 		assertTrue(observed.isEmpty());
 	}
 	
 	public Parser gen(String code){
 		Reader reader = new StringReader(code);
 		return new XiParser(reader);
 	}
 	
 	public void testParser(){
 		checkType(gen("main(){}"), new String[]{"PROGRAM","FUNCDECL","ID(main)","BLOCK"});
 	}
 	
 	public void testParserExpr(){
 		checkType(gen("main(){a=3}"), new String[]{"PROGRAM","FUNCDECL","ID(main)","BLOCK","ASSIGNMENT","ID(a)","3"});
 	}
 	
 	public void testParserExprBinOp(){
 		checkType(gen("main(){a=1+3}"), new String[]{"PROGRAM","FUNCDECL","ID(main)","BLOCK","ASSIGNMENT","ID(a)","BIN(PLUS)","1","3"});
 	}
 	
 	public void testParserExprUnOp(){
 		checkType(gen("main(){a = !3}"), new String[]{"PROGRAM","FUNCDECL","ID(main)","BLOCK","ASSIGNMENT","ID(a)","UNOT","3"});
 	}
 	
 	public void testParserExprCall(){
 		checkType(gen("main(){a = f(1,(2,3,4),\"a\")}"), new String[]{
 			"PROGRAM","FUNCDECL","ID(main)","BLOCK","ASSIGNMENT","ID(a)","CALL(3)",
 			"ID(f)","1","ARRAY(3)","2","3","4","\"a\""});
 	}
 	
 	public void testParserExprChar(){
 		checkType(gen("main(){a = 'a'}"), new String[]{
 			"PROGRAM","FUNCDECL","ID(main)","BLOCK","ASSIGNMENT","ID(a)","97"});
 	}
 	
 	public void testParserExprString(){
 		checkType(gen("main(){a = \"yay\\\"\"}"), new String[]{
 			"PROGRAM","FUNCDECL","ID(main)","BLOCK","ASSIGNMENT","ID(a)","\"yay\\\"\""});
 	}
 	
 	public void testParserExprIndex(){
 		checkType(gen("main(){a = a[i+3]}"), new String[]{
 			"PROGRAM","FUNCDECL","ID(main)","BLOCK","ASSIGNMENT","ID(a)",
 			"ARR-INDEX", "ID(a)", "BIN(PLUS)", "ID(i)", "3"
 		});
 	}
 	
 	public void testParserExprArr(){
 		checkType(gen("main(){a = ((1,2),(1,),())}"), new String[]{
 			"PROGRAM","FUNCDECL","ID(main)","BLOCK","ASSIGNMENT","ID(a)",
 			"ARRAY(3)", "ARRAY(2)", "1", "2", "ARRAY(1)", "1", "ARRAY(0)"
 		});
 	}
 	
 	public void testParserExprParen(){
 		checkType(gen("main(){a = (1)}"), new String[]{
 			"PROGRAM","FUNCDECL","ID(main)","BLOCK","ASSIGNMENT","ID(a)",
 			"1"
 		});
 	}
 	
 	public void testParserStmtWhile(){
 		checkType(gen("main(){while (1 < 2) a = 3;}"), new String[]{
 			"PROGRAM","FUNCDECL","ID(main)","BLOCK",
 			"WHILE", "EQ(LT)", "1", "2", "ASSIGNMENT", "ID(a)", "3"
 		});
 	}
 	
 	public void testParserStmtWhileBlock(){
 		checkType(gen("main(){while (1 < 2){ a = 3;}}"), new String[]{
 			"PROGRAM","FUNCDECL","ID(main)","BLOCK",
 			"WHILE", "EQ(LT)", "1", "2", "BLOCK", "ASSIGNMENT", "ID(a)", "3"
 		});
 	}
 	
 	public void testParserStmtIf(){
 		checkType(gen("main(){if (1 < 2) a = 3;}"), new String[]{
 			"PROGRAM","FUNCDECL","ID(main)","BLOCK",
 			"IF", "EQ(LT)", "1", "2", "ASSIGNMENT", "ID(a)", "3"
 		});
 	}
 	
 	public void testParserStmtIfBlock(){
 		checkType(gen("main(){if (1 < 2){ a = 3;}}"), new String[]{
 			"PROGRAM","FUNCDECL","ID(main)","BLOCK",
 			"IF", "EQ(LT)", "1", "2", "BLOCK", "ASSIGNMENT", "ID(a)", "3"
 		});
 	}
 	
 	public void testParserStmtIfElse(){
 		checkType(gen("main(){if (1 < 2){ a = 3;} else b = 3}"), new String[]{
 			"PROGRAM","FUNCDECL","ID(main)","BLOCK",
 			"IF-ELSE", "EQ(LT)", "1", "2", "BLOCK", "ASSIGNMENT", "ID(a)", "3",
 			"ASSIGNMENT", "ID(b)", "3"
 		});
 	}
 	
 	public void testParserStmtIfElseBreak(){
 		checkType(gen("main(){if (1 < 2){ a = 3;} else break;}"), new String[]{
 			"PROGRAM","FUNCDECL","ID(main)","BLOCK",
 			"IF-ELSE", "EQ(LT)", "1", "2", "BLOCK", "ASSIGNMENT", "ID(a)", "3",
 			"BREAK"
 		});
 	}
 	
 	public void testParserStmtProcedure(){
 		checkType(gen("main(){lol();}"), new String[]{
 			"PROGRAM","FUNCDECL","ID(main)","BLOCK",
 			"PROCEDURE", "CALL(0)", "ID(lol)"
 		});
 	}
 	
 	public void testParserStmtDecl(){
 		checkType(gen("main(){a:int;}"), new String[]{
 			"PROGRAM","FUNCDECL","ID(main)","BLOCK",
 			"DECL", "ID(a)"
 		});
 	}
 	
 	public void testParserStmtInst(){
 		checkType(gen("main(){a:int[][] = 3;}"), new String[]{
 			"PROGRAM","FUNCDECL","ID(main)","BLOCK",
 			"INST", "DECL", "ID(a)", "3"
 		});
 	}
 	
 	public void testParserStmtInstTuple(){
 		checkType(gen("main(){a:int, b:bool = f();}"), new String[]{
 			"PROGRAM","FUNCDECL","ID(main)","BLOCK",
 			"INST", "DECL", "ID(a)", "DECL", "ID(b)", "CALL(0)", "ID(f)"
 		});
 	}
 	
 	public void testParserStmtInstTupleUnderscore(){
 		checkType(gen("main(){a:int, _ = f();}"), new String[]{
 			"PROGRAM","FUNCDECL","ID(main)","BLOCK",
 			"INST", "DECL", "ID(a)", "UNDERSCORE", "CALL(0)", "ID(f)"
 		});
 	}
 	
 	public void testParserStmtMult(){
 		checkType(gen("main(){a:int = 3;f(1,2)}"), new String[]{
 			"PROGRAM","FUNCDECL","ID(main)","BLOCK",
 			"INST", "DECL", "ID(a)", "3", "PROCEDURE", "CALL(2)", "ID(f)", "1", "2"
 		});
 	}
 	
 	public void testParserPrgmUse(){
 		checkType(gen("use io main(){a:int, _ = f();}"), new String[]{
 			"PROGRAM","USE", "ID(io)", "FUNCDECL","ID(main)","BLOCK",
 			"INST", "DECL", "ID(a)", "UNDERSCORE", "CALL(0)", "ID(f)"
 		});
 	}
 	
 	public void testParserPrgmUseFuncs(){
 		checkType(gen("use io main(){a:int, _ = f();} f(){}"), new String[]{
 			"PROGRAM","USE", "ID(io)", "FUNCDECL","ID(main)","BLOCK",
 			"INST", "DECL", "ID(a)", "UNDERSCORE", "CALL(0)", "ID(f)",
 			"FUNCDECL", "ID(f)", "BLOCK"
 		});
 	}
 	
 	public void testParserErrorEof(){
 		try{
 			checkType(gen("main(){a = 'a'"), new String[]{
 			"PROGRAM","FUNCDECL","ID(main)","BLOCK","ASSIGNMENT","ID(a)","97"});
 			fail("Did not catch syntax error");
 		} catch (CompilationException e){
 			assertTrue(e.getMessage() == "Syntax Error: Not expecting EOF");
 		}
 	}
 	
 	public void testParserErrorArrIndex(){
 		try{
 			checkType(gen("main(){a = 3[i]"), new String[]{
 			});
 			fail("Did not catch syntax error");
 		} catch (CompilationException e){
 			assertEquals(e.getMessage(), "Syntax Error: Not expecting EOF");
 		}
 	}
 	
 	public void testParserErrorChar(){
 		try{
 			checkType(gen("main(){a = 'aa'"), new String[]{
 			});
 			fail("Did not catch syntax error");
 		} catch (CompilationException e){
 			assertEquals(e.getMessage(), "Illegal character <'>");
 		}
 	}
 	
 	public void testParserPrgmReturns(){
 		checkType(gen("use io main(){a:int, _ = f();} \nf(){return x}"), new String[]{
 			"PROGRAM","USE", "ID(io)", "FUNCDECL","ID(main)","BLOCK",
 			"INST", "DECL", "ID(a)", "UNDERSCORE", "CALL(0)", "ID(f)",
 			"FUNCDECL", "ID(f)", "BLOCK", "RETURN", "ID(x)"
 		});
 	}
 	
 	public void testParserPrgmReturnsNull(){
 		checkType(gen("use io main(){a:int, _ = f();} \nf(){return}"), new String[]{
 			"PROGRAM","USE", "ID(io)", "FUNCDECL","ID(main)","BLOCK",
 			"INST", "DECL", "ID(a)", "UNDERSCORE", "CALL(0)", "ID(f)",
 			"FUNCDECL", "ID(f)", "BLOCK", "RETURN"
 		});
 	}
 	
 	public void testParserPrgmReturnInvalid(){
 		try{
 			checkType(gen("use io main(){a:int, _ = f();} \nf(){return; print()}"), new String[]{
 			});
 			fail("Did not catch syntax error");
 		} catch (CompilationException e){
 			assertEquals(e.getMessage(), "Syntax Error: Not expecting token IDENTIFIER(print)");
 		}
 	}
 	
 	public void testPositionForInvalidReturnType() {
 		try {
 			gen("func() int { return 1 }").parse();
 			fail("Did not catch compilation error");
 		} catch (CompilationException e) {
 			assertEquals("((1, 8), (1, 10))",e.getPosition().toString());
 		}
 	}
 	
 	public void testInvalidDeclarationstatement() {
 		try {
 			gen("func() { a:int = b:int }").parse();
 			fail("Did not catch compilation error");
 		} catch (CompilationException e) {
 			assertEquals("((1, 19), (1, 19))", e.getPosition().toString());
 		}
 	}
 	
 	public void testInvalidUnderscoreDecalration() {
 		try {
 			gen("func() { _ = false  }").parse();
 			fail("Did not catch compilation error");
 		} catch (CompilationException e) {
 			assertEquals("((1, 12), (1, 12))", e.getPosition().toString());
 			assertEquals("Syntax Error: Not expecting token GETS(=)", e.getMessage());
 		}	
 	}
 	
 	public void testInvalidReturnAndPosition() {
 		try {
 			gen("func(): int { \nreturn 1; a:int = 3").parse();
 			fail("Did not catch compilation error");
 		} catch (CompilationException compEx) {
 			assertEquals("((2, 11), (2, 11))", compEx.getPosition().toString());
 			assertEquals("Syntax Error: Not expecting token IDENTIFIER(a)", compEx.getMessage());
 		}
 	
 	}
 	
 	public void testReturnBlockWithinBlock() {
 		try {
 			gen("main(x: int[][]) {{return;}}").parse();
 			
 		} catch (CompilationException compEx) {
 			assertEquals("((1, 19), (1, 19))", compEx.getPosition().toString());
 			assertEquals("Syntax Error: Not expecting token OPEN_BRACE({)", compEx.getMessage());
 			System.out.println("exception caught");
 		}
 	}
 	
 	public void testArrayDeclarationWithNonConstant() {
 		try {
 			checkType(gen("func() { \nb:int \na:int[b] }"), new String[]{
 					"PROGRAM","FUNCDECL","ID(func)","BLOCK",
 					"DECL", "ID(b)", "DECL", "ID(a)"
 				});
 		} catch (Exception ex) {
 			fail();
 		}
 	}
 	
 	public void testNestedBlocks(){
 		checkType(gen("func(){{return}}"), new String[]{
 			"PROGRAM","FUNCDECL","ID(func)","BLOCK",
 			"BLOCK", "RETURN"});
 	}
 	
 	public void testUseSemi(){
 		checkType(gen("use io; func(){{return}}"), new String[]{
 			"PROGRAM","USE", "ID(io)", "FUNCDECL","ID(func)","BLOCK",
 			"BLOCK", "RETURN"});
 	}
	
	public void testPA4Issue() {
		checkType(gen("main() {b:bool = 3+-3*40/4/5*2 == -9}"), new String[] {
			"PRORGAM", "FUNCDECL", "ID(main)", "BLOCK", 
		});
	
	}
 }
