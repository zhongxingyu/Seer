 package edu.tum.lua.junit.exceptions;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Test;
 
 import util.ParserUtil;
 import edu.tum.lua.GlobalEnvironment;
 import edu.tum.lua.LuaInterpreter;
 import edu.tum.lua.ast.Block;
 import edu.tum.lua.exceptions.LuaRuntimeException;
 import edu.tum.lua.exceptions.PrettyPrinter;
 import edu.tum.lua.parser.exception.SyntaxError;
 
 public class PrettyPrinterTest {
 
 	@Test
 	public void testPrint() throws SyntaxError, IOException {
 		Map<Block, File> map = new HashMap<>();
 
 		File crash = new File("crash.lua");
 		Block block = ParserUtil.loadFile(crash);
 
 		map.put(block, crash);
		PrettyPrinter pp = new PrettyPrinter(map);
 		try {
 			LuaInterpreter.eval(block, new GlobalEnvironment());
 		} catch (LuaRuntimeException ex) {
 			pp.print(ex);
 		}
 	}
 }
