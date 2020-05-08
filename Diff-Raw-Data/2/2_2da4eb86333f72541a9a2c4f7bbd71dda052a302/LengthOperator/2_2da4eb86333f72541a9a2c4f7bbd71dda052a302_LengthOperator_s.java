 package edu.tum.lua.operator.list;
 
 import edu.tum.lua.operator.Operator;
 import edu.tum.lua.stdlib.table.MaxN;
 import edu.tum.lua.types.LuaFunction;
 import edu.tum.lua.types.LuaTable;
 import edu.tum.lua.types.LuaType;
 
 public class LengthOperator extends Operator {
 
 	public Object apply(Object op) throws NoSuchMethodException {
 		if (LuaType.getTypeOf(op) == LuaType.STRING) {
 			return op.toString().length();
 		} else if (LuaType.getTypeOf(op) == LuaType.TABLE) {
 			return applyTable((LuaTable) op);
 		} else {
 			LuaFunction handler = getHandler("length", op);
 			return handler.apply(op);
 		}
 	}
 
 	private Object applyTable(LuaTable op) {
 		MaxN m = new MaxN();
 		return m.apply(op).get(0);
 	}
 
 }
