 package edu.tum.lua.junit;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import util.ParserUtil;
 import edu.tum.lua.LocalEnvironment;
 import edu.tum.lua.LuaInterpreter;
 import edu.tum.lua.StatementVisitor;
 import edu.tum.lua.ast.Asm;
 import edu.tum.lua.ast.Binop;
 import edu.tum.lua.ast.Block;
 import edu.tum.lua.ast.ExpList;
 import edu.tum.lua.ast.IfThenElse;
 import edu.tum.lua.ast.NumberExp;
 import edu.tum.lua.ast.Op;
 import edu.tum.lua.ast.PreExp;
 import edu.tum.lua.ast.PrefixExpVar;
 import edu.tum.lua.ast.StatList;
 import edu.tum.lua.ast.VarList;
 import edu.tum.lua.ast.Variable;
 import edu.tum.lua.types.LuaTable;
 import edu.tum.lua.types.LuaType;
 
 public class StatementVisitorTest {
 
 	private StatementVisitor visitor;
 	private LocalEnvironment environment;
 
 	@Before
 	public void setUp() throws Exception {
 		environment = new LocalEnvironment();
 		visitor = new StatementVisitor(environment);
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
 		visitor = new StatementVisitor(environment);
 
 		// > a = 1+1 >> a=2
 		// > if a == 2 then b=1 else b=0 end >> b=1
 		// > if b != 1 then c=1 else c=0 end >> c=0
 
 		Asm asm = new Asm(new VarList(new Variable("a")), new ExpList(new Binop(new NumberExp(1.0), Op.ADD,
 				new NumberExp(1.0))));
 
 		// > a = 1+1
 		assertEquals(null, environment.get("a"));
 		visitor.visit(asm);
 		assertEquals(2.0, environment.get("a"));
 
 		// > if a == 2 then b=1 else b=0 end >> b=1
 		assertEquals(null, environment.get("b"));
 		IfThenElse ifstatement = new IfThenElse(new Binop(new PreExp(new PrefixExpVar(new Variable("a"))), Op.EQ,
 				new NumberExp(2.0)), new Block(new StatList(new Asm(new VarList(new Variable("b")), new ExpList(
 				new NumberExp(1.0)))), null), new Block(new StatList(new Asm(new VarList(new Variable("b")),
 				new ExpList(new NumberExp(2.0)))), null));
 
 		visitor = new StatementVisitor(environment);
 		visitor.visit(ifstatement);
 		assertEquals(1.0, environment.get("b"));
 
 		// > if a == 2 then b=1 else b=0 end >> b=1
 
		Block block2 = ParserUtil.loadString("if b!=1 then c=1 else c=0 end");
 
 		assertEquals(null, environment.get("c"));
 		LuaInterpreter.eval(block2, environment);
 		assertEquals(0.0, environment.get("c"));
 
 	}
 
 	@Test
 	public void testVisitDoExp() {
 		fail("Not yet implemented"); // TODO
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
 		Block block = ParserUtil.loadString("local a=true if not a then b=2 elseif not a then b=3 else b=1 end");
 
 		assertEquals(null, environment.get("a"));
 		LuaInterpreter.eval(block, environment);
 		assertEquals(null, environment.get("a"));
 		assertEquals(1.0, environment.get("b"));
 	}
 
 	@Test
 	public void testVisitForExp() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	@Test
 	public void testVisitForIn() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	@Test
 	public void testVisitFunctionDef() throws Exception {
 		Block block = ParserUtil.loadString("a={}");
 
 		assertEquals(null, environment.get("a"));
 		LuaInterpreter.eval(block, environment);
 		assertEquals(LuaType.TABLE, LuaType.getTypeOf(environment.get("a")));
 
 		block = ParserUtil.loadString("function a.foo() end'");
 
 		LuaInterpreter.eval(block, environment);
 		assertEquals(LuaType.FUNCTION, LuaType.getTypeOf(((LuaTable) environment.get("a")).get("foo")));
 	}
 
 	@Test
 	public void testVisitLocalFuncDef() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	@Test
 	public void testVisitLocalDecl() {
 		fail("Not yet implemented"); // TODO
 	}
 
 }
