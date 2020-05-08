 package edu.tum.lua.stdlib;
 
 import java.io.FileNotFoundException;
 import java.util.Collections;
 import java.util.List;
 
 import util.ParserUtil;
 import edu.tum.lua.GlobalEnvironment;
 import edu.tum.lua.LuaInterpreter;
 import edu.tum.lua.Preconditions;
 import edu.tum.lua.ast.Block;
import edu.tum.lua.exceptions.LuaRuntimeException;
 import edu.tum.lua.parser.exception.SyntaxError;
 import edu.tum.lua.types.LuaFunctionNative;
 import edu.tum.lua.types.LuaType;
 
 public class DoFile extends LuaFunctionNative {
 
 	private static final LuaType[][] expectedTypes = { { LuaType.STRING } };
 	private final GlobalEnvironment globalEnvironment;
 
 	public DoFile(GlobalEnvironment ge) {
 		this.globalEnvironment = ge;
 	}
 
 	@Override
 	public List<Object> apply(List<Object> arguments) {
 		Preconditions.checkArguments("dofile", arguments, expectedTypes);
 		String file = (String) arguments.get(0);
 
 		try {
 			Block block = ParserUtil.loadFile(file);
 			return LuaInterpreter.eval(block, globalEnvironment);
 		} catch (FileNotFoundException e) {
 			System.out.println("Cannot open " + file + ": No such file or directory");
 		} catch (SyntaxError se) {
 			System.out.println("Syntax error in file " + file);
 			se.printStackTrace();
		} catch (LuaRuntimeException e) {
			throw e;
 		} catch (Exception e) {
 			System.out.println("Error while parsing file " + file);
 			e.printStackTrace();
 		}
 
 		return Collections.emptyList();
 	}
 
 }
