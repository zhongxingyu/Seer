 package edu.tum.lua.types;
 
 import static edu.tum.lua.ast.LegacyAdapter.convert;
 
 import java.util.Arrays;
 import java.util.List;
 
 import edu.tum.lua.Environment;
 import edu.tum.lua.LuaInterpreter;
import edu.tum.lua.ast.Block;
 import edu.tum.lua.ast.FunctionDef;
 
 public class LuaFunctionInterpreted implements LuaFunction {
 
 	private Environment environment;
 	private final List<String> argumentNames;
	private final Block block;
 
 	public LuaFunctionInterpreted(FunctionDef node, Environment e) {
 		environment = e;
 		argumentNames = convert(node.args);
		block = node.block;
 	}
 
 	@Override
 	public List<Object> apply(List<Object> arguments) {
 		Environment currentEnvironment = new Environment(environment);
 
 		for (int i = 0; i < Math.min(argumentNames.size(), arguments.size()); i++) {
 			currentEnvironment.set(argumentNames.get(i), arguments.get(i));
 		}
 
		return LuaInterpreter.eval(block, currentEnvironment);
 	}
 
 	@Override
 	public List<Object> apply(Object... arguments) {
 		return apply(Arrays.asList(arguments));
 	}
 
 	public Environment getEnvironment() {
 		return environment;
 	}
 
 	public void setEnvironment(Environment e) {
 		environment = e;
 	}
 }
