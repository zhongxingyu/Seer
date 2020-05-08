 package edu.tum.lua.stdlib;
 
 import static edu.tum.lua.Preconditions.checkArguments;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import edu.tum.lua.GlobalEnvironment;
import edu.tum.lua.exceptions.LuaRuntimeException;
 import edu.tum.lua.types.LuaFunction;
 import edu.tum.lua.types.LuaFunctionNative;
 import edu.tum.lua.types.LuaType;
 
 public class Require extends LuaFunctionNative {
 
 	LuaType[][] expectedTypes = { { LuaType.STRING } };
 	String modulename;
 	GlobalEnvironment g;
 	LoadFile loadfile;
 
 	@Override
 	public List<Object> apply(List<Object> arguments) {
 		/*
 		 * Load a package: require(String modulename)
 		 * 
 		 * 1) Search _G.package.loaded if there is already a key "modulename"
 		 * and if yes return that value.
 		 * 
 		 * 2) Search in _G.package.path if file "modulename" exists, evaluate it
 		 * using loadfile, register it to _G.package.loaded, and return the
 		 * evaluated value.
 		 */
 
 		checkArguments("assert", arguments, expectedTypes);
 		modulename = (String) arguments.get(0);
 		g = GlobalEnvironment.getGlobalEnvironment();
 		loadfile = new LoadFile();
 
 		// 1) Search _G.package.loaded[modulename]
 		if (g.getLuaTable("package").getLuaTable("loaded").get(modulename) != null) {
 			return Arrays.asList(g.getLuaTable("package").getLuaTable("loaded").get("modulename"));
 		}
 
 		// 2) Look in _G.package.path for the file modulename
 		String path = g.getLuaTable("package").getString("path");
 		if (path != null) {
 			path = path.replace("?", modulename);
 			String[] filestrings = path.split(";");
 			for (String filestring : filestrings) {
 				File file = new File(filestring);
 				if (file.isFile() && file.canRead()) {
 
 					LuaFunction interpretedfunction = (LuaFunction) loadfile.apply(Arrays.asList(filestring)).get(0);
 					List<Object> returned_value_list = interpretedfunction.apply(Collections.emptyList());
 
 					// Get the first return value, omit all others
 					Object returned_value = returned_value_list.get(0);
 
 					// Register to _G.package.loaded
 					g.getLuaTable("package").getLuaTable("loaded").set(modulename, returned_value);
 					return Arrays.asList(returned_value);
 
 				}
 			}
 		}
 
		throw new LuaRuntimeException("File Not Found");
 	}
 }
