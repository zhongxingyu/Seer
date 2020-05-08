 package edu.tum.lua.stdlib;
 
 import static edu.tum.lua.Preconditions.checkArguments;
 
 import java.util.Arrays;
 import java.util.List;
 
 import edu.tum.lua.types.LuaFunctionNative;
 import edu.tum.lua.types.LuaTable;
 import edu.tum.lua.types.LuaType;
 
 public class IPairs extends LuaFunctionNative {
 
 	private class INext extends LuaFunctionNative {
 		LuaType[][] expectedTypes = { { LuaType.TABLE, LuaType.NUMBER } };
 
 		@Override
 		public List<Object> apply(List<Object> arguments) {
 			checkArguments("inext", arguments, expectedTypes);
 			LuaTable table = (LuaTable) arguments.get(0);
 			double index = (double) arguments.get(1);
 			double nextindex = index + 1;
 
 			if (table.get(nextindex) == null) {
 				// return nothing (empty list)
 				return Arrays.asList();
 			}
			return Arrays.asList(nextindex, table.get(nextindex));
 		}
 	}
 
 	LuaType[][] expectedTypes = { { LuaType.TABLE } };
 
 	@Override
 	public List<Object> apply(List<Object> arguments) {
 
 		checkArguments("ipairs", arguments, expectedTypes);
 
 		INext nextFunction = new INext();
 		LuaTable table = (LuaTable) arguments.get(0);
 
 		return Arrays.asList(nextFunction, table, 0.0);
 	}
 
 }
