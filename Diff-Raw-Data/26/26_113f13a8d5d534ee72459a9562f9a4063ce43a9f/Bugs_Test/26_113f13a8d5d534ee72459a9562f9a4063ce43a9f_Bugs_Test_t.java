 package descent.tests.mars;
 
 import descent.core.dom.AST;
 import descent.core.dom.Block;
 import descent.core.dom.CallExpression;
 import descent.core.dom.CompilationUnit;
 import descent.core.dom.DotIdentifierExpression;
 import descent.core.dom.ExpressionStatement;
 import descent.core.dom.FunctionDeclaration;
 
 
 public class Bugs_Test extends Parser_Test {
 	
 	// Used to fail in data[i].p
 	public void testBug1() {
 		String s = " void bla() { if (data[i].p == p) { } }";
 		getCompilationUnit(s);
 	}
 	
 	// Used to fail in (Pool).sizeof
 	public void testBug2() {
 		String s = " void bla() { (Pool).sizeof = 2; }";
 		getCompilationUnit(s);
 	}
 	
 	// Used to not parse correctly the asm statement
 	public void testBug3() {
 		String s = " void bla() { asm { pushad; } }";
 		CompilationUnit cu = getCompilationUnit(s);
 		assertEquals(1, cu.declarations().size());
 	}
 	
 	public void testBug4() {
 		String s = " a b ((void));";
 		getCompilationUnit(s);
 	}
 	
 	public void testBug5a() {
 		String s = " void bla() { return std.string.toString(buffer).dup; }";
 		CompilationUnit cu = getCompilationUnit(s);
 		assertEquals(1, cu.declarations().size());
 	}
 	
 	public void testBug5b() {
 		String s = " void bla() { return std.string.toString--.dup; }";
 		CompilationUnit cu = getCompilationUnit(s);
 		assertEquals(1, cu.declarations().size());
 	}
 	
 	public void testBug5c() {
 		String s = " void bla() { return std.string.toString++.dup; }";
 		CompilationUnit cu = getCompilationUnit(s);
 		assertEquals(1, cu.declarations().size());
 	}
 	
 	public void testBug6() {
 		String s = " void bla() { return isNumeric(va_arg!(char[])(_argptr)); }";
 		CompilationUnit cu = getCompilationUnit(s);
 		assertEquals(1, cu.declarations().size());
 	}
 	
 	public void testBug7() {
 		String s = " const char[16] hexdigits = \"0123456789ABCDEF\\n\";			/// 0..9A..F";
 		CompilationUnit cu = getCompilationUnit(s);
 		assertEquals(1, cu.declarations().size());
 	}
 	
 	// Dont give error on UTF characters
 	public void testBugUTF() {
 		String s = " // ";
 		CompilationUnit unit = getCompilationUnit(s);
 		assertEquals(0, unit.getProblems().length);
 	}
 	
 	public void testBug8() {
 		String s = " /** hola */ void bla() { }";
 		CompilationUnit unit = getCompilationUnit(s);
 		assertEquals(1, unit.declarations().get(0).preDDocs().size());
 	}
 	
 	public void testBug9() {
 		String s = " package";
 		getCompilationUnit(s);
 	}
 	
 	public void testBug10() {
 		String s;
 		CompilationUnit unit;
 		
 		s = "void bla() { Thread[] buf = new Thread[sm_tlen]; }";
 		unit = getCompilationUnit(s);
 		assertEquals(0, unit.getProblems().length);
 
 		s = "void bla() { Thread[] buf = new Thread[sm_tlen.b.c]; }";
 		unit = getCompilationUnit(s);
 		assertEquals(0, unit.getProblems().length);
 		
 		s = "void bla() { Thread[] buf = new Thread[Foo!(int).bar]; }";
 		unit = getCompilationUnit(s);
 		assertEquals(1, unit.getProblems().length);
 	}
 	
 	public void testBug11() {
 		String s = "void bla() { typeof(mangF).mangleof[3]; }";
 		getCompilationUnit(s);
 	}
 	
 	public void testBug12() {
 		String s = "void bla() { typeid(K).getHash; }";
 		getCompilationUnit(s);
 	}
 	
 	// error parsing the token 0_
 	public void testBug13() {
 		String s = "void bla() { h4 = (0_) % 256; }";
 		assertEquals(0, getCompilationUnit(s).getProblems().length);
 	}
 	
 	public void testBug14() {
 		String s = "int suffix; // inclusive of leftmost '.'\n" +
 				   "/** */\n" +
 				   "int x;";
 		assertEquals(0, getCompilationUnit(s).getProblems().length);
 	}
 	
 	public void testBug15() {
 		String s = "this ";
 		getCompilationUnit(s);
 	}
 	
 	public void testBug16() {
 		String s = "class ____C{void ____m(){\r\n" + 
 				"	invariant {\r\n" + 
 				"		\r\n" + 
 				"	}\r\n" + 
 				"}}";
 		CompilationUnit unit = getCompilationUnit(s);
 		assertEquals(1, unit.declarations().size());
 	}
 	
 	public void testBug17() {
 		String s = "void main() { Stdout.formatln(x); }";
 		CompilationUnit unit = getCompilationUnit(s);
 		assertEquals(1, unit.declarations().size());
 		
 		FunctionDeclaration func = (FunctionDeclaration) unit.declarations().get(0);
 		Block block = (Block) func.getBody();
 		ExpressionStatement stm = (ExpressionStatement) block.statements().get(0);
 		CallExpression callExp = (CallExpression) stm.getExpression();
 		DotIdentifierExpression dotId = (DotIdentifierExpression) callExp.getExpression();
 		assertPosition(dotId, 14, 15);
 	}
 	
 	public void testBug18() {
 		String s = "void main() { .formatln(x); }";
 		CompilationUnit unit = getCompilationUnit(s);
 		assertEquals(1, unit.declarations().size());
 		
 		FunctionDeclaration func = (FunctionDeclaration) unit.declarations().get(0);
 		Block block = (Block) func.getBody();
 		ExpressionStatement stm = (ExpressionStatement) block.statements().get(0);
 		CallExpression callExp = (CallExpression) stm.getExpression();
 		DotIdentifierExpression dotId = (DotIdentifierExpression) callExp.getExpression();
 		assertPosition(dotId, 14, 9);
 	}
 	
 	public void testBug19() {
 		for(String typeName : new String[] { "class", "struct", "union", "interface", "enum", "template" }) {
 			String s = "void main() { " + typeName +  " }";
 			CompilationUnit unit = getCompilationUnit(s);
 			FunctionDeclaration func = (FunctionDeclaration) unit.declarations().get(0);
 			Block block = (Block) func.getBody();
 			assertEquals(0, block.statements().size());
 		}
 	}
 	
 	public void testBug20() {
 		String s = "void bla() { foreach }";
 		getCompilationUnit(s);
 	}
 	
 	public void testBug21() throws Exception {
 		String s = "void a(X)() { }\r\n" + 
 				"	/** */\r\n" + 
 				"	void b(X)() { }";
 		getCompilationUnit(s);
 	}
 		
 	public void testBug22() {
 		String s = 
 			"version(Win32)\r\n" + 
 			"	    bool _blocking = false;	/// Property to get or set whether the socket is blocking or nonblocking.\r\n" + 
 			"	\r\n" + 
 			"	\r\n" + 
 			"	// For use with accepting().\r\n" + 
 			"	protected this()\r\n" + 
 			"	{\r\n" + 
 			"	}";
 		
 		getCompilationUnit(s);		
 	}
 	
 	public void testBug23() {
 		String s = 
 			"extern(System)\r\n" + 
 			"	    bool _blocking = false;	/// Property to get or set whether the socket is blocking or nonblocking.\r\n" + 
 			"	\r\n" + 
 			"	\r\n" + 
 			"	// For use with accepting().\r\n" + 
 			"	protected this()\r\n" + 
 			"	{\r\n" + 
 			"	}";
 		
 		getCompilationUnit(s, AST.D2);		
 	}
 	
 	public void testMissingIfThenBody() {
 		String s = "void foo() { if(true) }";
 		getCompilationUnit(s);	
 	}
 	
 	public void testMissingTryBody() {
 		String s = "void foo() { try finally { } }";
 		getCompilationUnit(s);	
 	}
 	
	public void testCPlusPlus() {
		String s = "void foo() { App::main(); }";
		getCompilationUnit(s);	
	}
	
 	public void testDstress_run_t_typeof_16_A() {
 		String s = "char[] name = (typeof(o)).classinfo.name;";
 		getCompilationUnit(s);	
 	}
 	
 	public void testDstress_nocompile_t_this_08_B() {
 		String s = "C(int i, int j) : this(i){ } this(int i){ }";
 		getCompilationUnit(s);
 	}
 	
 	public void testDstress_nocompile_new_21() {
 		String s = "class MyClass{ unittest{ new(size_t s){ void* v; return v; } } }";
 		getCompilationUnit(s);
 	}
 	
 	public void testDstress_nocompile_n_new_27_B() {
 		String s = "class A{ void test(){ } } class B : A{ public new void test(){ } }";
 		getCompilationUnit(s);
 	}
 	
 	public void testDstress_compile_extern_07() {
 		String s = "glFunctionFoo = cast(extern(C) int function()) wglGetProcAddress(\"glFunctionFoo\");";
 		getCompilationUnit(s);
 	}
 	
 	public void testDstress_nocompile_foreach_18_D() {
 		String s = "int main(){ char[] string; foreach(out char c; string){ } return 0; }";
 		getCompilationUnit(s);
 	}
 	
 	public void testDstress_nocompile_a_array_initialization_22_F() {
 		String s = "char[.] x;";
 		getCompilationUnit(s);
 	}
 	
 	public void testDstress_nocompile_a_alias_37_D() {
 		String s = "alias short foo(byte);";
 		getCompilationUnit(s);
 	}
 
 }
