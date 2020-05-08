 package edu.tum.lua.stdlib;
 
 import java.util.Collections;
 import java.util.List;
 
 import edu.tum.lua.LuaRuntimeException;
 import edu.tum.lua.Preconditions;
 import edu.tum.lua.types.LuaFunction;
 import edu.tum.lua.types.LuaFunctionNative;
 import edu.tum.lua.types.LuaType;
 
 public class Load extends LuaFunctionNative {
 
 	private static final LuaType[][] expectedTypes = { { LuaType.FUNCTION } };
 
 	@Override
 	public List<Object> apply(List<Object> arguments) {
 		Preconditions.checkArguments("load", arguments, expectedTypes);
 		LuaFunction f = (LuaFunction) arguments.get(0);
 		List<Object> iter = f.apply(Collections.emptyList());
 		String result = "";
 
 		while (iter != null && !iter.isEmpty()) {
 			if (LuaType.getTypeOf(iter.get(0)) != LuaType.STRING)
				throw new LuaRuntimeException("The function doesn't return a string");
 			if (iter.get(0).toString().length() == 0)
 				break;
 			result = result.concat(iter.get(0).toString());
 			iter = f.apply(Collections.emptyList());
 		}
 
 		LoadString l = new LoadString();
 		return l.apply(result);
 	}
 
 }
