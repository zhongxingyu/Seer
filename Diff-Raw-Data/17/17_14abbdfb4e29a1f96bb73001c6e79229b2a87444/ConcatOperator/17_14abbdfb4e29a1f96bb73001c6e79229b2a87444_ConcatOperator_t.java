 package edu.tum.lua.operator.list;
 
 import javax.naming.OperationNotSupportedException;
 
 import edu.tum.lua.exceptions.LuaArithmeticOperationNotSupportedException;
 import edu.tum.lua.operator.BinaryOperator;
import edu.tum.lua.stdlib.ToString;
 import edu.tum.lua.types.LuaFunction;
 import edu.tum.lua.types.LuaType;
 
 public class ConcatOperator extends BinaryOperator {
 
 	protected static String convert(Object object) throws OperationNotSupportedException {
 		if (LuaType.getTypeOf(object) == LuaType.STRING) {
 			return (String) object;
 		} else if (LuaType.getTypeOf(object) == LuaType.NUMBER) {
			return ToString.toString(object);
 		}
 		throw new LuaArithmeticOperationNotSupportedException(LuaType.getTypeOf(object).toString());
 	}
 
 	@Override
 	public Object apply(Object op1, Object op2) {
 		try {
 			return applyString(convert(op1), convert(op2));
 		} catch (OperationNotSupportedException e) {
 			LuaFunction handler = getHandler("__concat", op1, op2);
 			return handler.apply(op1, op2);
 		}
 
 	}
 
 	private String applyString(String op1, String op2) {
 		return op1.concat(op2);
 	}
 }
