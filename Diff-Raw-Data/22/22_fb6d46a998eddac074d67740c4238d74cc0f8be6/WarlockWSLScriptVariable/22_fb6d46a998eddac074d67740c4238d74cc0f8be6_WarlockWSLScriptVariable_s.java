 package cc.warlock.script.wsl;
 
 import java.util.Map;
 
 public class WarlockWSLScriptVariable extends WarlockWSLScriptArg {
 	private String variableName;
 	
 	public WarlockWSLScriptVariable(String var) {
 		variableName = var;
 	}
 	
 	@Override
 	public String getString(Map<String, String> variables) {
 		String value = variables.get(variableName);
 		if(value == null) return "";
 		return value;
 	}
 
 }
