 package edu.tum.lua.stdlib.table;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import edu.tum.lua.Preconditions;
 import edu.tum.lua.exceptions.LuaBadArgumentException;
 import edu.tum.lua.exceptions.LuaRuntimeException;
 import edu.tum.lua.operator.list.LengthOperator;
 import edu.tum.lua.operator.relational.LTOperator;
 import edu.tum.lua.types.LuaFunction;
 import edu.tum.lua.types.LuaFunctionNative;
 import edu.tum.lua.types.LuaTable;
 import edu.tum.lua.types.LuaType;
 
 public class Sort extends LuaFunctionNative {
 
 	private static final LuaType[][] expectedTypes = { { LuaType.TABLE } };
 
 	private static class Comp implements Comparator<Object> {
 
 		private LuaFunction comp;
 		private final int type;
 
 		public Comp() {
 			type = 1;
 		}
 
 		public Comp(LuaFunction f) {
 			type = 2;
 			comp = f;
 		}
 
 		@Override
 		public int compare(Object o1, Object o2) {
 			boolean b1;
 			boolean b2;
 			if (type == 1) {
 				LTOperator op = new LTOperator();
 				b1 = op.apply(o1, o2);
 				b2 = op.apply(o2, o1);
 			} else {
 				b1 = true;
 				b2 = true;
 				if (comp.apply(o1, o2).get(0) == null
 						|| (LuaType.getTypeOf(comp.apply(o1, o2).get(0)) == LuaType.BOOLEAN && !((boolean) comp.apply(
 								o1, o2).get(0))))
 					b1 = false;
 				if (comp.apply(o2, o1).get(0) == null
 						|| (LuaType.getTypeOf(comp.apply(o2, o1).get(0)) == LuaType.BOOLEAN && !((boolean) comp.apply(
 								o2, o1).get(0))))
 					b2 = false;
 			}
			if (!b1) {
				if (!b2)
					return 0;
				else
					return 1;
			}
			return -1;
 
 		}
 	}
 
 	@Override
 	public List<Object> apply(List<Object> arguments) {
 		Preconditions.checkArguments("table.sort", arguments, expectedTypes);
 		Comp comp = null;
 		LuaTable table = (LuaTable) arguments.get(0);
 
 		if (arguments.size() > 1) {
 			if (LuaType.getTypeOf(arguments.get(1)) != LuaType.FUNCTION)
 				throw new LuaBadArgumentException(2, "table.sort", "function", LuaType.getTypeOf(arguments.get(1))
 						.toString());
 			comp = new Comp((LuaFunction) arguments.get(1));
 		}
 
 		double length = 0.0;
 		LengthOperator op = new LengthOperator();
 		try {
 			length = (double) op.apply(table);
 		} catch (LuaRuntimeException e) {
 		}
 		if (table.keySet().isEmpty())
 			return Collections.emptyList();
 		if (length != 0.0) {
 			LuaType type = LuaType.getTypeOf(table.get(1.0));
 			if (comp == null)
 				comp = new Comp();
 			Object[] t = new Object[(int) length];
 			for (double pos = 1.0; pos <= length; pos = pos + 1) {
 				if (LuaType.getTypeOf(table.get(pos)) != type)
 					throw new LuaRuntimeException("attempt to compare " + type.toString() + " with "
 							+ LuaType.getTypeOf(table.get(pos)).toString());
 				t[(int) pos - 1] = table.get(pos);
 			}
 			Arrays.sort(t, comp);
 			for (int i = 0; i < t.length; i++) {
 				table.set((double) (i + 1), t[i]);
 			}
 		}
 		return Collections.emptyList();
 	}
 
 }
