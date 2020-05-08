 package edu.tum.lua.stdlib;
 
 import java.util.Collections;
 import java.util.List;
 
 import edu.tum.lua.LocalEnvironment;
import edu.tum.lua.LuaRuntimeException;
 import edu.tum.lua.Preconditions;
 import edu.tum.lua.types.LuaFunctionInterpreted;
 import edu.tum.lua.types.LuaFunctionNative;
 import edu.tum.lua.types.LuaType;
 
 public class LoadString extends LuaFunctionNative {
 
 	private static final LuaType[][] expectedTypes = { { LuaType.STRING } };
 
 	@Override
 	public List<Object> apply(List<Object> arguments) {
 		Preconditions.checkArguments("loadstring", arguments, expectedTypes);
 		try {
			LuaFunctionInterpreted function = new LuaFunctionInterpreted(null, new LocalEnvironment());
 			return Collections.singletonList((Object) function);
		} catch (LuaRuntimeException e) {
 			return Collections.singletonList(null);
 		}
 	}
 
 }
