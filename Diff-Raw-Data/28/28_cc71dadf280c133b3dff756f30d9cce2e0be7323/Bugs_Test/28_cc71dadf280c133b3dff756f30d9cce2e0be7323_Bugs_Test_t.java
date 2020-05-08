 package descent.tests.mars;
 
 import descent.core.compiler.IProblem;
 import descent.core.dom.AST;
 import descent.core.dom.Block;
 import descent.core.dom.CallExpression;
 import descent.core.dom.CompilationUnit;
 import descent.core.dom.Declaration;
 import descent.core.dom.DotIdentifierExpression;
 import descent.core.dom.ExpressionStatement;
 import descent.core.dom.FunctionDeclaration;
 import descent.core.dom.CompilationUnitResolver.ParseResult;
 import descent.internal.compiler.parser.ASTDmdNode;
 import descent.internal.compiler.parser.IntegerExp;
 import descent.internal.compiler.parser.Module;
 import descent.internal.compiler.parser.TypeSArray;
 import descent.internal.compiler.parser.VarDeclaration;
 
 
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
 		String s = " // �";
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
 	
 	public void testCaseOpen() {
 		String s = "void foo() { switch(1) case ";
 		getCompilationUnit(s);	
 	}
 	
 	public void testAutoMissingSemicolon() {
 		String s = "void foo() { auto x = 2 }";
 		CompilationUnit cu = getCompilationUnit(s);
 		assertEquals(1, cu.getProblems().length);
 		assertError(cu.getProblems()[0], IProblem.ParsingErrorInsertTokenAfter, 22, 1);
 	}
 	
 	public void testStrangeBug() {
 		String s = "struct foo { int a, b; } foo f = { ";
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
 	
 	public void testBugFoundByBrunoMedeiros() {
 		String s = 
 			"// Foo\r\n" + 
 			"\r\n" + 
 			"void foo() {\r\n" + 
 			"\t// bla\r\n" + 
 			"}\r\n" + 
 			"\r\n" + 
 			"class Bang {\r\n" + 
 			"}";
 		
 		ParseResult parseResult = getParseResult(s, AST.D1);
 		Module module = parseResult.module;
 		ASTDmdNode node = (ASTDmdNode) module.members.get(1); // SEMANTIC
 		assertNull(module.getPreComments(node));
 	}
 	
 	public void testBugFoundByBrunoMedeiros2() {
 		String s = 
 			"// Foo\r\n" + 
 			"\r\n" + 
 			"void foo() {\r\n" + 
 			"\t// bla\r\n" + 
 			"}\r\n" + 
 			"// a comment \r\n" + 
 			"class Bang {\r\n" + 
 			"}";
 		
 		ParseResult parseResult = getParseResult(s, AST.D1);
 		Module module = parseResult.module;
 		ASTDmdNode node = (ASTDmdNode) module.members.get(1); // SEMANTIC
 		assertEquals(1, module.getPreComments(node).size());
 	}
 	
 	public void testTicket25() {
 		String s;
 		
 		s = "char[] s = \"�a\";";
 		assertEquals(0, getCompilationUnit(s).getProblems().length);
 		
 		s = "char[] s = \"a�\";";
 		assertEquals(0, getCompilationUnit(s).getProblems().length);
 	}
 	
 	public void testTypeSArray1() {
 		String s = "bool[2][4] b;";
 		
 		Module module = getParseResult(s, AST.D1).module;
 		VarDeclaration var = (VarDeclaration) module.members.get(0);
 		TypeSArray ts = (TypeSArray) var.type;
 		assertEquals(4, ((IntegerExp) ts.dim).value.intValue());
 		ts = (TypeSArray) ts.next;
 		assertEquals(2, ((IntegerExp) ts.dim).value.intValue());
 	}
 	
 	public void testTypeSArray2() {
 		String s = "bool b[4][2];";
 		
 		Module module = getParseResult(s, AST.D1).module;
 		VarDeclaration var = (VarDeclaration) module.members.get(0);
 		TypeSArray ts = (TypeSArray) var.type;
 		assertEquals(4, ((IntegerExp) ts.dim).value.intValue());
 		ts = (TypeSArray) ts.next;
 		assertEquals(2, ((IntegerExp) ts.dim).value.intValue());
 	}
 	
 	public void testFunctionSourceRangeBugWithEnumBefore() {
 		String s = 
 			"enum {\r\n" + 
 			"	a, // x\r\n" + 
 			"}\r\n" + 
 			"\r\n" + 
 			"void b() {\r\n" + 
 			"	\r\n" + 
 			"}";
 		
 		CompilationUnit unit = getCompilationUnit(s);
 		Declaration decl = unit.declarations().get(1);
 		assertPosition(decl, 23, s.length() - 23);
 		
 		assertEquals(23, unit.getExtendedStartPosition(decl));
 		assertEquals(s.length() - 23, unit.getExtendedLength(decl));
 	}
 	
 	public void testFunctionSourceRangeBugWithEnumBefore2() {
 		String s = 
 			"enum {\r\n" + 
 			"	a, // x\r\n" + 
 			"}\r\n" + 
 			"\r\n" + 
 			"void b() {\r\n" + 
 			"	\r\n" + 
 			"}";
 		
 		ParseResult parseResult = getParseResult(s, AST.D1);
 		Module m = parseResult.module;
 		
 		ASTDmdNode node = (ASTDmdNode) m.members.get(1);
 		assertNull(m.getPreComments(node));
 	}
 	
 	public void testTicket108() {
 		String s = 
 			"char[] c(int v) {\r\n" + 
 			"    switch (v) {\r\n" + 
 			"    default:\r\n" + 
 			"        return b( a(\"test\" );\r\n" + 
 			"    }\r\n" + 
 			"}";
 		
 		Module m = getParseResult(s, AST.D1).module;
 		assertNotNull(m);
 	}
 	
 	public void testWrongEnum() {
 		String s = 
 			"enum { int a = 1; int b = 2; }";
 		
 		Module m = getParseResult(s, AST.D2).module;
 		assertNotNull(m);
 	}
 	
 	public void testAliasThisWrongOrder() {
 		String s = 
 			"struct S { alias this foo; }";
 		
 		Module m = getParseResult(s, AST.D2).module;
 		assertNotNull(m);
 	}
	
	public void testPrivateEnumMemberJavaHeapSapceD1() {
		String s = 
			"enum Color { private UNINITIALIZED = -1, RED, GREEN, BLUE }";
		
		getParseResult(s, AST.D1);
	}
	
	public void testPrivateEnumMemberJavaHeapSapceD2() {
		String s = 
			"enum Color { private UNINITIALIZED = -1, RED, GREEN, BLUE }";
		
		getParseResult(s, AST.D2);
	}
 
 }
