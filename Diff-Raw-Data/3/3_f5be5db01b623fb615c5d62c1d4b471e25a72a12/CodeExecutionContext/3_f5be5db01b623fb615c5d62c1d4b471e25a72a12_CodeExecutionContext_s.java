 package net.sf.javascribe.api;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class CodeExecutionContext {
 	private HashMap<String,String> variables = new HashMap<String,String>();
 	private TypeResolver types = null;
 	
 	public List<String> getVariableNames() {
 		List<String> ret = new ArrayList<String>();
 		
 		for(String k : variables.keySet()) {
 			ret.add(k);
 		}
 		
 		return ret;
 	}
 	
 	public CodeExecutionContext(CodeExecutionContext parent,TypeResolver types) {
 		this.types = types;
 		if (parent!=null) {
 			variables.putAll(parent.variables);
 		}
 	}
 	
 	public void addVariable(String name,String type) {
 		variables.put(name,type);
 	}
 	
 	public TypeResolver getTypes() {
 		return types;
 	}
 	
 	public VariableType getType(String name) {
 		return types.getType(name);
 	}
 	
 	public String getVariableType(String var) {
 		return variables.get(var);
 	}
 	
 	public VariableType getTypeForVariable(String var) {
 		String type = variables.get(var);
 		if (type!=null) {
 			return types.getType(type);
 		}
 
 		return null;
 	}
 	
 }
 
