 package edu.tum.lua.stdlib;
 
 import static edu.tum.lua.Preconditions.checkArguments;
 
 import java.util.Collections;
 import java.util.List;
 
 import edu.tum.lua.types.LuaFunctionNative;
 import edu.tum.lua.types.LuaTable;
 import edu.tum.lua.types.LuaType;
 
 public class GetMetatable extends LuaFunctionNative {
 
 	private static final LuaType[][] types = { { LuaType.TABLE } };
 
 	@Override
 	public List<Object> apply(List<Object> arguments) {
 		checkArguments("getmetatable", arguments, types);
 		LuaTable table = (LuaTable) arguments.get(0);
		return Collections.singletonList(table.getMetatable());
 	}
 }
