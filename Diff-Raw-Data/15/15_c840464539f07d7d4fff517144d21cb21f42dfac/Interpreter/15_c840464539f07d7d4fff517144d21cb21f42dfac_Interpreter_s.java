 package fmv.tools;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 public class Interpreter extends ExternalTool {
 	private String command = "/usr/bin/java";
 
 	public Interpreter(String cmd) {
 		command = cmd;
 	}
 	public Interpreter(){};
 
 	@Override
 	protected Collection<String> getCommand() {
 		return Arrays.asList(command);
 	}
 
 	@Override
 	protected String getConfig(String key, String value) {
 		String config = null;
 		if (!value.equals("none") && !value.equals("")) {
			if (key.equals("classpath")) {
				config = "-classpath ! " + value.replaceAll(" ", " ! ");
 			} else if (key.equals("jar") && value.equals("true")) {
 				config = "-jar";
 			} else {
 				throw new IllegalArgumentException(key + " : " + value);
 			}
 		}
 		return config;
 	}
 
 	public static void main(String[] args) {
 		ExternalTool java = new Interpreter();
 		java.configure("config/interpreter.config");
 		System.out.println(java.run(null, "fmv.tools.FindBugs"));
 	}
 
 }
