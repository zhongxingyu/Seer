 package edu.tum.lua.stdlib.math;
 
 import java.util.Arrays;
 import java.util.List;
 
 import edu.tum.lua.Preconditions;
 import edu.tum.lua.types.LuaFunctionNative;
 import edu.tum.lua.types.LuaType;
 
public class Exp extends LuaFunctionNative {
 
 	// TODO the expected types are not right
 	// max(v ..) they all need to be numbers
 	LuaType[][] expectedTypes = { { LuaType.NUMBER } };
 
 	@Override
 	public List<Object> apply(List<Object> arguments) {
 		Preconditions.checkArguments("max", arguments, expectedTypes);
 		double max = (double) arguments.get(0);
 		for (Object argument : arguments) {
 			if ((double) argument > max) {
 				max = (double) argument;
 			}
 		}
 		return Arrays.asList((Object) max);
 	}
 }
