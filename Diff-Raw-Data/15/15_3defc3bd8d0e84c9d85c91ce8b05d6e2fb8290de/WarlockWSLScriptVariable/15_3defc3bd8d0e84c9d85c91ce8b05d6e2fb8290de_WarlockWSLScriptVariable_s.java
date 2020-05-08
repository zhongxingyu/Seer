 package cc.warlock.script.wsl;
 
 import java.util.Map;
 
 public class WarlockWSLScriptVariable extends WarlockWSLScriptArg {
 	private String variableName;
 	
 	public WarlockWSLScriptVariable(String var) {
 		variableName = var;
 	}
 	
 	@Override
 	public String getString(Map<String, String> variables) {
		/*for(String name : variables.keySet()) {
 			System.out.println("var: \"" + name + "\"");
 		}
 		System.out.println("variableName: \"" + variableName + "\"");
		*/
 		String value = variables.get(variableName);
 		if(value == null) return "";
 		return value;
 	}
 
 }
