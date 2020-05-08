 package edu.tum.lua.stdlib;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import edu.tum.lua.LuaRuntimeException;
 import edu.tum.lua.Preconditions;
 import edu.tum.lua.types.LuaFunctionNative;
 import edu.tum.lua.types.LuaType;
 
 public class ToNumber extends LuaFunctionNative {
 
 	LuaType[][] expectedTypesOne = { { LuaType.STRING, LuaType.NUMBER } };
 	LuaType[][] expectedTypesTwo = { { LuaType.STRING, LuaType.NUMBER }, { LuaType.NUMBER } };
 
 	@Override
 	public List<Object> apply(List<Object> arguments) {
 		if (arguments.size() == 1)
 			Preconditions.checkArguments("tonumber", arguments, expectedTypesOne);
 		else
 			Preconditions.checkArguments("tonumber", arguments, expectedTypesTwo);
 
 		List<Object> list = new LinkedList<Object>();
 		Object o = arguments.get(0);
 
 		if (arguments.size() == 1) {
 			switch (LuaType.getTypeOf(o)) {
 			case NUMBER:
 				list.add(o);
 				break;
 			case STRING:
				list.add(new Double(Double.parseDouble((String) o)));
 				break;
 			default:
 				list.add(null);
 			}
 
 			return list;
 		}
 
 		Object b = arguments.get(1);
 
 		if (LuaType.getTypeOf(b) != LuaType.NUMBER || (int) b >= 36 || (int) b <= 2) {
 			throw new LuaRuntimeException("base expected as Number between 2 and 36");
 		}
 
 		if (LuaType.getTypeOf(o) == LuaType.NUMBER) {
 			return this.apply(Double.toString((double) o), b);
 		}
 
 		if (LuaType.getTypeOf(o) != LuaType.STRING) {
 			list.add(null);
 			return list;
 		}
 
		list.add(new Double(Integer.parseInt((String) o, (int) b)));
 		return list;
 	}
 }
