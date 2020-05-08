 package edu.tum.lua.stdlib;
 
 import static edu.tum.lua.Preconditions.checkArguments;
 
 import java.util.Collections;
 import java.util.List;
 
 import edu.tum.lua.types.LuaFunctionNative;
 import edu.tum.lua.types.LuaTable;
 import edu.tum.lua.types.LuaType;
 
 public class GetMetatable extends LuaFunctionNative {
 
	private static final LuaType[][] types = { { LuaType.TABLE }, { LuaType.TABLE, LuaType.NIL } };
 
 	@Override
 	public List<Object> apply(List<Object> arguments) {
		checkArguments("setmetatable", arguments, types);

 		LuaTable table = (LuaTable) arguments.get(0);
		LuaTable metaTable = (LuaTable) arguments.get(1);

		table.setMetatable(metaTable);

		return Collections.singletonList((Object) table);
 	}
 }
