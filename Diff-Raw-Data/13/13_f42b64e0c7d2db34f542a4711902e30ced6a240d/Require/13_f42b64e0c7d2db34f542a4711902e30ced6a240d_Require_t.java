 package edu.tum.lua.stdlib;
 
 import static edu.tum.lua.Preconditions.checkArguments;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import util.ParserUtil;
 import edu.tum.lua.GlobalEnvironment;
 import edu.tum.lua.LuaInterpreter;
 import edu.tum.lua.exceptions.LuaIOException;
 import edu.tum.lua.types.LuaFunctionNative;
 import edu.tum.lua.types.LuaType;
 
 public class Require extends LuaFunctionNative {
 
 	LuaType[][] expectedTypes = { { LuaType.STRING } };
 	String modulename;
 	GlobalEnvironment g;
 	LoadFile loadfile;
 
 	public Require(GlobalEnvironment ge) {
 		this.g = ge;
 	}
 
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
 		loadfile = new LoadFile();
 
 		// 1) Search _G.package.loaded[modulename]
 		if (g.getLuaTable("package").getLuaTable("loaded").get(modulename) != null) {
 			return Arrays.asList(g.getLuaTable("package").getLuaTable("loaded").get(modulename));
 		}
 
 		// 2) Look in _G.package.path for the file modulename
 		String path = g.getLuaTable("package").getString("path");
 
 		if (path == null) {
			throw new LuaIOException("File Not Found, because path is empty");
 		}
 
 		path = path.replace("?", modulename);
 		String[] filestrings = path.split(";");
 		for (String filestring : filestrings) {
 			File file = new File(filestring);
 
 			if (file.isFile() && file.canRead()) {
 				List<Object> returned_value_list = new ArrayList<>();
 				try {
 					returned_value_list = LuaInterpreter.eval(ParserUtil.loadFile(file), g);
 				} catch (FileNotFoundException e) {
 					throw new LuaIOException("File " + filestring + " not found");
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 				// Get the first return value, omit all others
 				Object returned_value = returned_value_list.isEmpty() ? true : returned_value_list.get(0);
 
 				// Register to _G.package.loaded
 				g.getLuaTable("package").getLuaTable("loaded").set(modulename, returned_value);
 				return Arrays.asList(returned_value);
 			}
 		}
 
		String errorstring = "module '" + modulename + "' not found:";
		errorstring = errorstring.concat("\n\tno field package.preload['" + modulename + "']");
		for (String filestring : filestrings) {
			errorstring = errorstring.concat("\n\tno file " + filestring);
		}
		throw new LuaIOException(errorstring);
 	}
 }
