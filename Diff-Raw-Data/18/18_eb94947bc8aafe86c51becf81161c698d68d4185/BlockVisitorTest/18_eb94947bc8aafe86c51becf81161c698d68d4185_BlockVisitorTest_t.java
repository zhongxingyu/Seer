 package edu.tum.lua.junit;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import util.ParserUtil;
 import edu.tum.lua.BlockVisitor;
 import edu.tum.lua.LocalEnvironment;
 import edu.tum.lua.LuaInterpreter;
 import edu.tum.lua.ast.Asm;
 import edu.tum.lua.ast.Binop;
 import edu.tum.lua.ast.Block;
 import edu.tum.lua.ast.DoExp;
 import edu.tum.lua.ast.ExpList;
 import edu.tum.lua.ast.IfThenElse;
 import edu.tum.lua.ast.LastBreak;
 import edu.tum.lua.ast.LocalDecl;
 import edu.tum.lua.ast.LocalFuncDef;
 import edu.tum.lua.ast.Name;
 import edu.tum.lua.ast.NameList;
 import edu.tum.lua.ast.NumberExp;
 import edu.tum.lua.ast.Op;
 import edu.tum.lua.ast.PreExp;
 import edu.tum.lua.ast.PrefixExpVar;
 import edu.tum.lua.ast.StatList;
 import edu.tum.lua.ast.VarList;
 import edu.tum.lua.ast.Variable;
 import edu.tum.lua.types.LuaTable;
 import edu.tum.lua.types.LuaType;
 
 public class BlockVisitorTest {
 
 	private BlockVisitor visitor;
 	private LocalEnvironment environment;
 
 	@Before
 	public void setUp() throws Exception {
 		environment = new LocalEnvironment();
 		visitor = new BlockVisitor(environment);
 	}
 
 	@Test
 	public void testVisitAsm() {
 		Asm asm = new Asm(new VarList(new Variable("a")), new ExpList(new Binop(new NumberExp(1.0), Op.ADD,
 				new NumberExp(1.0))));
 
 		assertEquals(null, environment.get("a"));
 		visitor.visit(asm);
 		assertEquals(2.0, environment.get("a"));
 	}
 
 	@Test
 	public void testVisitFuncCallStmt() throws Exception {
 		Block block = ParserUtil.loadString("a=1+2");
 
 		assertEquals(null, environment.get("a"));
 		LuaInterpreter.eval(block, environment);
 		assertEquals(3.0, environment.get("a"));
 
 		block = ParserUtil.loadString("b=type(type(a))");
 
 		assertEquals(null, environment.get("b"));
 		LuaInterpreter.eval(block, environment);
 		assertEquals("string", environment.get("b"));
 
 		block = ParserUtil.loadString("c='a'");
 
 		assertEquals(null, environment.get("c"));
 		LuaInterpreter.eval(block, environment);
 		assertEquals("a", environment.get("c"));
 	}
 
 	@Test
 	public void testVisitIf() throws Exception {
 
 		environment = new LocalEnvironment();
 		visitor = new BlockVisitor(environment);
 
 		// > a = 1+1 >> a=2 // > if a == 2 then b=1 else b=0 end >> b=1
 		// > if b!= 1 then c=1 else c=0 end >> c=0
 
 		Asm asm = new Asm(new VarList(new Variable("a")), new ExpList(new Binop(new NumberExp(1.0), Op.ADD,
 				new NumberExp(1.0))));
 
 		// > a = 1+1 assertEquals(null, environment.get("a"));
 		visitor.visit(asm);
 		assertEquals(2.0, environment.get("a"));
 
 		// > if a == 2 then b=1 else b=0 end >> b=1
 		assertEquals(null, environment.get("b"));
 		IfThenElse ifstatement = new IfThenElse(new Binop(new PreExp(new PrefixExpVar(new Variable("a"))), Op.EQ,
 				new NumberExp(2.0)), new Block(new StatList(new Asm(new VarList(new Variable("b")), new ExpList(
 				new NumberExp(1.0)))), null), new Block(new StatList(new Asm(new VarList(new Variable("b")),
 				new ExpList(new NumberExp(2.0)))), null));
 
 		visitor = new BlockVisitor(environment);
 		visitor.visit(ifstatement);
 		assertEquals(1.0, environment.get("b"));
 
 		// > if a == 2 then b=1 else b=0 end >> b=1
 
 		Block block2 = ParserUtil.loadString("if b~=1 then c=1 else c=0 end");
 
 		assertEquals(null, environment.get("c"));
 		LuaInterpreter.eval(block2, environment);
 		assertEquals(0.0, environment.get("c"));
 
 	}
 
 	@Test
 	public void testVisitDoExp() throws Exception {
 		Block block = ParserUtil.loadString("a=1");
 		DoExp doExp = new DoExp(block);
 
 		assertEquals(null, environment.get("a"));
 		visitor.visit(doExp);
 		assertEquals(1.0, environment.get("a"));
 	}
 
 	@Test
 	public void testVisitWhileExp() throws Exception {
 		Block block = ParserUtil.loadString("a=1");
 
 		assertEquals(null, environment.get("a"));
 		LuaInterpreter.eval(block, environment);
 		assertEquals(1.0, environment.get("a"));
 
 		block = ParserUtil.loadString("while a<4 do a=a+1 b=2 end");
 
 		LuaInterpreter.eval(block, environment);
 		assertEquals(4.0, environment.get("a"));
 		assertEquals(2.0, environment.get("b"));
 	}
 
 	@Test
 	public void testVisitRepeatUntil() throws Exception {
 		Block block = ParserUtil.loadString("a='a'");
 
 		assertEquals(null, environment.get("a"));
 		LuaInterpreter.eval(block, environment);
 		assertEquals("a", environment.get("a"));
 
 		block = ParserUtil.loadString("repeat a=a..'a' b=2 until a=='aaa'");
 
 		LuaInterpreter.eval(block, environment);
 		assertEquals("aaa", environment.get("a"));
 		assertEquals(2.0, environment.get("b"));
 	}
 
 	@Test
 	public void testVisitIfThenElse() throws Exception {
 		Block block = ParserUtil.loadString("a=false if a then b=2 elseif a then b=3 else b=1 end");
 		assertEquals(null, environment.get("a"));
 		LuaInterpreter.eval(block, environment);
 		assertFalse((boolean) environment.get("a"));
 		assertEquals(1.0, environment.get("b"));
 	}
 
 	@Test
 	public void testVisitForExp() throws Exception {
 
 		Block block1, block2;
 
 		// Normal iteration setUp();
 		block1 = ParserUtil.loadString("b=0");
 		block2 = ParserUtil.loadString("for a = 1,3,0.5 do b=a end");
 
 		LuaInterpreter.eval(block1, environment);
 		assertEquals(0.0, environment.get("b"));
 		LuaInterpreter.eval(block2, environment);
 		assertEquals(3.0, environment.get("b"));
 
 		// Negative iteration
 
 		setUp();
 		block1 = ParserUtil.loadString("b=0");
 		block2 = ParserUtil.loadString("for a = 3,1,-0.5 do b=a end");
 
 		LuaInterpreter.eval(block1, environment);
 		assertEquals(0.0, environment.get("b"));
 		LuaInterpreter.eval(block2, environment);
 		assertEquals(1.0, environment.get("b"));
 
 		// No step given
 
 		setUp();
 		block1 = ParserUtil.loadString("b=0");
 		block2 = ParserUtil.loadString("for a = 1,3 do b=b+1 end");
 
 		LuaInterpreter.eval(block1, environment);
 		assertEquals(0.0, environment.get("b"));
 		LuaInterpreter.eval(block2, environment);
 		assertEquals(3.0, environment.get("b"));
 
 		// Do not change
 
 		// Do not change "a" // > a = 1 // > for a = 1,3 do print(a) end
 		// > print(a) >> 1
 		block1 = ParserUtil.loadString("a=1");
 		block2 = ParserUtil.loadString("for a = 1,3,0.5 do b=a end");
 
 		assertEquals(null, environment.get("a"));
 		LuaInterpreter.eval(block1, environment);
 		LuaInterpreter.eval(block2, environment);
 		assertEquals(1.0, environment.get("a"));
 
 	}
 
 	@Test
 	public void testVisitForExp2() throws Exception {
 
 		Block block1 = ParserUtil.loadString("a=1");
 		Block block2 = ParserUtil.loadString("for a = 1,3,0.5 do print(a) end");
 
 		assertEquals(null, environment.get("a"));
 		LuaInterpreter.eval(block1, environment);
 		LuaInterpreter.eval(block2, environment);
 		assertEquals(1.0, environment.get("a"));
 
 	}
 
 	@Test
 	public void testVisitForIn() throws Exception {
 
 		Block block = ParserUtil.loadString("a='a'");
 
 		assertEquals(null, environment.get("a"));
 		LuaInterpreter.eval(block, environment);
 		assertEquals("a", environment.get("a"));
 
 		LuaTable table = new LuaTable();
 		table.set("one", 1.0);
 		table.set("two", 2.0);
 		table.set("three", 3.0);
 
 		environment.set("t", table);
 		LuaTable t = (LuaTable) environment.get("t");
 		assertEquals(1.0, t.get("one"));
 		assertEquals(2.0, t.get("two"));
 		assertEquals(3.0, t.get("three"));
 
 		/*
 		 * Asm asm = new Asm(new VarList(new Variable("a")), new ExpList(new
 		 * Binop(new PreExp(new PrefixExpVar(new Variable("a"))), Op.CONCAT, new
 		 * PreExp(new PrefixExpVar(new Variable("k")))))); PrefixExpFuncCall
 		 * pefc = new PrefixExpFuncCall(new FunctionCall(new PrefixExpVar(new
 		 * Variable("pairs")), new ExpList(new PreExp(new PrefixExpVar(new
 		 * Variable("t")))))); block = new Block(new StatList(new ForIn(new
 		 * NameList(new Name("k")), new ExpList(new PreExp(pefc)), new Block(new
 		 * StatList(asm), null))), null);
 		 */
 		// TODO: Read statement
 
 		LuaInterpreter.eval(block, environment);
 		// assertEquals("atwoonethree,", environment.get("a"));
 	}
 
 	@Test
 	public void testVisitFunctionDef() throws Exception {
 		Block block = ParserUtil.loadString("a={}");
 
 		assertEquals(null, environment.get("a"));
 		LuaInterpreter.eval(block, environment);
 		assertEquals(LuaType.TABLE, LuaType.getTypeOf(environment.get("a")));
 
		block = ParserUtil.loadString("function a.foo() end");
 
		// FunctionExp foo = new FunctionExp(new NameList(), false, new
		// Block(new StatList(), null));
		// VarTabIndex location = new VarTabIndex(new PrefixExpVar(new
		// Variable("a")), new TextExp("foo"));
		// block = new Block(new StatList(new Asm(new VarList(location), new
		// ExpList(foo))), null);
 		LuaInterpreter.eval(block, environment);
 
 		assertEquals(LuaType.FUNCTION, LuaType.getTypeOf(((LuaTable) environment.get("a")).get("foo")));
 	}
 
 	@Test
 	public void testVisitLocalFuncDef() throws Exception {
 		// Block block =
 		// ParserUtil.loadString("x=0 local function foo() x=1 end foo()");
 		Block block = ParserUtil.loadString("x=0");
 		assertEquals(null, environment.get("x"));
 		LuaInterpreter.eval(block, environment);
 		assertEquals(0.0, environment.get("x"));
 		Block body = ParserUtil.loadString("x=1");
 		LocalFuncDef foo = new LocalFuncDef("foo", new NameList(), false, body);
 		block = new Block(new StatList(foo), new LastBreak());
 
 		LuaInterpreter.eval(block, environment);
 		// TODO call the function when it will be implemented in the parser
 	}
 
 	@Test
 	public void testVisitLocalDecl() throws Exception {
 		// Block block =ParserUtil.loadString("local a = 1 x = a+1");
 		LocalDecl l = new LocalDecl(new NameList(new Name("a")), new ExpList(new NumberExp(1.0)));
 		Asm asm = new Asm(new VarList(new Variable("x")), new ExpList(new Binop(new NumberExp(1.0), Op.ADD, new PreExp(
 				new PrefixExpVar(new Variable("a"))))));
 		StatList statList = new StatList(l);
 		statList.append(asm);
 		Block block = new Block(statList, new LastBreak());
 		assertEquals(null, environment.get("a"));
 		LuaInterpreter.eval(block, environment);
 		assertEquals(null, environment.get("a"));
 		assertEquals(2.0, environment.get("x"));
 
 	}
 
 }
