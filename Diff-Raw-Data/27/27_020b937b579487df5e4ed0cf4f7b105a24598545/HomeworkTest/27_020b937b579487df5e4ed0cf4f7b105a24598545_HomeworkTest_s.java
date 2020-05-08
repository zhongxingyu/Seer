 package edu.tum.lua.junit.homework;
 
 import java.io.FileNotFoundException;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import util.ParserUtil;
 import edu.tum.lua.GlobalEnvironment;
 import edu.tum.lua.LocalEnvironment;
 import edu.tum.lua.LuaInterpreter;
 import edu.tum.lua.ast.Block;
 
 public class HomeworkTest {
 
 	private LocalEnvironment environment;
 
 	@Before
 	public void setUp() throws Exception {
 		environment = new LocalEnvironment();
 	}
 
 	@Test
 	public void testMatthias() throws FileNotFoundException, Exception {
 
 		/*
 		 * Run the knapsack lua file, expected return value is 32
 		 */
 
 		Block block = ParserUtil.loadFile("../Frontend/testinput/homework/matthias_knapsack_interpretable.lua");
 		LuaInterpreter.eval(block, environment);
 
 	}
 
 	@Test
 	public void testJohannes() throws FileNotFoundException, Exception {
 		Block block = ParserUtil.loadFile("../Frontend/testinput/homework/johannes_parser_test.lua");
 		LuaInterpreter.eval(block, environment);
 	}
 
 	@Test
 	public void testLisa() throws FileNotFoundException, Exception {
 		Block block = ParserUtil.loadFile("../Frontend/testinput/homework/lisa_avltree/test.lua");
 		GlobalEnvironment ge = new GlobalEnvironment();
 		ge.getLuaTable("package").set("path",
 				ge.getLuaTable("package").get("path") + ";../Frontend/testinput/homework/lisa_avltree/?.lua");
 		LuaInterpreter.eval(block, ge);
 	}
 
 }
