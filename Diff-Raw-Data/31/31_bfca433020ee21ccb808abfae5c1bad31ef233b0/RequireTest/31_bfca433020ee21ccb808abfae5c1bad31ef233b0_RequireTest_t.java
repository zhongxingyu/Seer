 package edu.tum.lua.junit.stdlib;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 
 import util.ParserUtil;
 import edu.tum.lua.GlobalEnvironment;
 import edu.tum.lua.LocalEnvironment;
 import edu.tum.lua.LuaInterpreter;
 import edu.tum.lua.ast.Block;
 import edu.tum.lua.types.LuaTable;
 
 public class RequireTest {
 
 	@Rule
 	public TemporaryFolder folder = new TemporaryFolder();
 
 	private LocalEnvironment environment;
 	private GlobalEnvironment g;
 
 	@Before
 	public void setUp() throws Exception {
 		environment = new LocalEnvironment();
 		g = GlobalEnvironment.getGlobalEnvironment();
 
 	}
 
 	@Test
 	public void testRequire() throws Exception {
 
 		// Create mymodule.lua
 		File file = folder.newFile("mymodule.lua");
 		BufferedWriter out = new BufferedWriter(new FileWriter(file));
 		out.write("t={} \n");
 		out.write("return t \n");
 		out.close();
 
 		// Push the temporary folder on the path
 		String path = GlobalEnvironment.getGlobalEnvironment().getLuaTable("package").getString("path");
 		String fileAbsolutePath = file.getAbsolutePath();
 		String filePath = fileAbsolutePath.substring(0, fileAbsolutePath.lastIndexOf(File.separator));
 		GlobalEnvironment.getGlobalEnvironment().getLuaTable("package").set("path", path + ";" + filePath + "/?.lua");
 
 		Block block1 = ParserUtil.loadString("m=require(\"mymodule\")");
 		Block block2 = ParserUtil.loadString("n=2");
 
 		assertEquals(null, environment.get("m"));
 		LuaInterpreter.eval(block1, environment);
 		LuaInterpreter.eval(block2, environment);
 		assertEquals(LuaTable.class, environment.get("m").getClass());
 		assertEquals(LuaTable.class, g.getLuaTable("package").getLuaTable("loaded").get("mymodule").getClass());
 
 	}
 }
