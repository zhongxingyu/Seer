 package edu.tum.lua.junit;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import org.junit.Before;
 import org.junit.Test;
 
import util.ParserUtil;
 import edu.tum.lua.Environment;
import edu.tum.lua.LuaInterpreter;
 import edu.tum.lua.StatementVisitor;
 import edu.tum.lua.ast.Asm;
 import edu.tum.lua.ast.Binop;
import edu.tum.lua.ast.Block;
 import edu.tum.lua.ast.ExpList;
 import edu.tum.lua.ast.NumberExp;
 import edu.tum.lua.ast.Op;
 import edu.tum.lua.ast.VarList;
 import edu.tum.lua.ast.Variable;
 
 public class StatementVisitorTest {
 
 	private StatementVisitor visitor;
 	private Environment environment;
 
 	@Before
 	public void setUp() throws Exception {
 		environment = new Environment();
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

		block = ParserUtil.loadString("print(\"a\")");
		LuaInterpreter.eval(block, environment);
 	}
 
 	@Test
 	public void testVisitDoExp() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	@Test
 	public void testVisitWhileExp() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	@Test
 	public void testVisitRepeatUntil() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	@Test
 	public void testVisitIfThenElse() {
 		fail("Not yet implemented"); // TODO
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
 	public void testVisitFunctionDef() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	@Test
 	public void testVisitLocalFuncDef() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	@Test
 	public void testVisitLocalDecl() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	@Test
 	public void testVisit() {
 		fail("Not yet implemented"); // TODO
 	}
 
 }
