 package edu.tum.lua.stdlib;
 
 import java.util.Collections;
import java.util.LinkedList;
 import java.util.List;
 
import util.ParserUtil;
 import edu.tum.lua.LocalEnvironment;
 import edu.tum.lua.Preconditions;
import edu.tum.lua.ast.Block;
 import edu.tum.lua.types.LuaFunctionInterpreted;
 import edu.tum.lua.types.LuaFunctionNative;
 import edu.tum.lua.types.LuaType;
 
 public class LoadString extends LuaFunctionNative {
 
 	private static final LuaType[][] expectedTypes = { { LuaType.STRING } };
 
 	@Override
 	public List<Object> apply(List<Object> arguments) {
 		Preconditions.checkArguments("loadstring", arguments, expectedTypes);
 		try {
			Block block = ParserUtil.loadString((String) arguments.get(0));
			LuaFunctionInterpreted function = new LuaFunctionInterpreted(new LinkedList<String>(), false, block,
					new LocalEnvironment());
 			return Collections.singletonList((Object) function);
		} catch (Exception e) {
 			return Collections.singletonList(null);
 		}
 	}
 
 }
