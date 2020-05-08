 package scripting;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.concurrent.ConcurrentSkipListSet;
 
 import javax.script.Bindings;
 import javax.script.Invocable;
 import javax.script.ScriptContext;
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 
 import org.eclipse.swt.SWT;
 import org.pircbotx.hooks.Event;
 
 import connection.KEllyBot;
 
 import lombok.Getter;
 import lombok.Setter;
 
 import shared.NSAlertBox;
 import sun.org.mozilla.javascript.internal.NativeArray;
 
 public final class Script implements Comparable<Script> {
 	
 	public static final int JAVASCRIPT = 1;
 	public static final int RUBY = 2;
 	
 	@Getter
 	private int scriptType=0;
 	
 	@Getter @Setter
 	private boolean inUse = true;
 	@Getter
 	private String script;
 	@Getter
 	private File reference;
 	@Getter
 	private ConcurrentSkipListSet<String> functions = new ConcurrentSkipListSet<String>();
 	@Getter
 	private ConcurrentSkipListSet<String> descriptFunctions = new ConcurrentSkipListSet<String>();
 	
 	private ScriptEngineManager manager = new ScriptEngineManager();
 	private ScriptEngine jsEngine = manager.getEngineByName("JavaScript");
 	private ScriptEngine rbEngine = manager.getEngineByName("jruby");
 	private Invocable jsInvocable = (Invocable) jsEngine;
 	private Invocable rbInvocable = (Invocable) rbEngine;
 	private Bindings bindings = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);
 	
 	private String rbBase = "require 'java'\n";
 	
 	private String jsBase = "importClass(org.eclipse.swt.SWT);\n" +
 							"importClass(Packages.hexapixel.notifier.NotificationType);\n" +
 							"importClass(Packages.org.pircbotx.Colors);\n" +
 							"importClass(java.lang.Thread);\n" +
							"importClass(Packages.connection.KEllyBot);\n" +
 							"importPackage(org.pircbotx);\n" +
 							"importPackage(java.util);\n" +
 							"importPackage(java.lang);\n";
 	
 	public Script(File f) {
 		this.reference = f;
 		
 		String s = reference.getName().substring(reference.getName().indexOf('.'));
 
 		if(s.equals(".js")){
 			scriptType=JAVASCRIPT;
 		} else if(s.equals(".rb")){
 			scriptType=RUBY;
 		}
 		
 		reset();
 	}
 
 	public void reset() {
 		readScript();
 		
 		initBindings();
 		
 		initialize();
 	}
 
 	private void initialize() {
 		try {
 			switch(scriptType){
 			case JAVASCRIPT:
 				jsEngine.eval(jsBase+script);
 				break;
 			case RUBY:
 		        rbEngine.eval(rbBase+script);
 		        break;
 			}
 		} catch (ScriptException e) {
 
 			new NSAlertBox("Script Read Error", reference.getName()+" has an error. Due to error reporting methods, I can not help you narrow down the issue.", SWT.ICON_ERROR);
 
 			org.apache.log4j.Logger fLog = org.apache.log4j.Logger.getLogger("log.script.scripts");
 			fLog.error("Script initialization failed: "+reference.getName()+" at line #"+e.getLineNumber());
 		}
 	}
 
 	private void initBindings() {
 		bindings.put("global", new ScriptVars());
 		bindings.put("util", new ScriptFunctions());
 		bindings.put("gui", new ScriptGUI());
 		bindings.put("sound", new SoundData());
 	}
 
 	public void readScript() {
 
 		//reset the functions list
 		functions.clear();
 		descriptFunctions.clear();
 		
 		//re-parse the script
 		StringBuffer contents = new StringBuffer();
 		
 		BufferedReader reader = null;
 		if(!reference.exists()) return;
 
 		try {
 			reader = new BufferedReader(new FileReader(reference));
 			String text = null;
 			while((text = reader.readLine())!=null){
 				contents.append(text).append(System.getProperty("line.separator"));
 				parseFunction(text);
 			}
 			reader.close();
 		} catch (Exception e) {
 			org.apache.log4j.Logger fLog = org.apache.log4j.Logger.getLogger("log.script.scripts");
 			fLog.error("Script reading failed.", e);
 		}
 		
 		script = contents.toString();
 	}
 	
 	//check line-by-line for a function name
 	private void parseFunction(String text) {
 		if(text.toLowerCase().contains("meta")) {
 			parseMeta(text);
 		}
 		switch(scriptType){
 		case JAVASCRIPT:
 			if(!text.contains("function ")) return;
 			String elFunction = text.substring(9).split("[{]")[0].trim();
 			descriptFunctions.add(elFunction);
 			String[] array = text.replaceAll("[(]", " ").split(" ");
 			//function, onFunctionName, event), {
 			functions.add(array[1].trim());
 			break;
 		case RUBY:
 			if(!text.contains("def ")) return;
 			String rbFunction = text.substring(4);
 			descriptFunctions.add(rbFunction);
 			String[] rbarray = text.replaceAll("[(]", " ").split(" ");
 			//def function(var) 
 			functions.add(rbarray[1].trim());
 			break;
 		}
 	}
 	
 	private void parseMeta(String text){
 		//META<inuse=false>
 		String[] cleanMeta = text.replaceAll("[<>]", " ").split(" ")[1].split("=");
 		if(cleanMeta[0].equals("inuse")){
 			this.inUse = Boolean.parseBoolean(cleanMeta[1]);
 		}
 	}
 	
 	//event invocation
 	public void invoke(String function, Event<KEllyBot> e){
 		if(!inUse)return;
 		try {
 			switch(scriptType){
 			case JAVASCRIPT:
 				jsInvocable.invokeFunction(function, e);
 				break;
 			case RUBY:
 				rbInvocable.invokeFunction(function, e);
 				break;
 			}
 		} catch (NoSuchMethodException e1) {
 			org.apache.log4j.Logger fLog = org.apache.log4j.Logger.getLogger("log.script.scripts");
 			fLog.error("Script invocation failed.", e1);
 		} catch (ScriptException e1) {
 			org.apache.log4j.Logger fLog = org.apache.log4j.Logger.getLogger("log.script.scripts");
 			fLog.error("Script invocation failed.", e1);
 		}
 	}
 
 	//open-ended invocation
 	public void invoke(String command, Object... args) { 
 		if(!inUse)return;
 		try {
 			switch(scriptType){
 			case JAVASCRIPT:
 				jsInvocable.invokeFunction(command, args);
 				break;
 			case RUBY:
 				rbInvocable.invokeFunction(command, args);
 				break;
 			}
 		} catch (NoSuchMethodException e1) {
 			org.apache.log4j.Logger fLog = org.apache.log4j.Logger.getLogger("log.script.scripts");
 			fLog.error("Script invocation failed.", e1);
 		} catch (ScriptException e1) {
 			org.apache.log4j.Logger fLog = org.apache.log4j.Logger.getLogger("log.script.scripts");
 			fLog.error("Script invocation failed.", e1);
 		}
 	}
 	
 	public Object[] invoke(String command) {
 		Object[] rv = null;
 		try {
 			switch(scriptType){
 			case JAVASCRIPT:
 				NativeArray arr = (NativeArray) jsInvocable.invokeFunction(command);
 				rv = new Object[(int)arr.getLength()];
 				
 				//get as object
 				for(Object o : arr.getIds()) {
 					int index = (Integer) o;
 					rv[index] = arr.get(index, null);
 				}
 				break;
 			case RUBY:
 				rv = (Object[]) rbInvocable.invokeFunction(command);
 			}
 		} catch (NoSuchMethodException e) {
 			org.apache.log4j.Logger fLog = org.apache.log4j.Logger.getLogger("log.script.scripts");
 			fLog.error("Script invocation failed.", e);
 		} catch (ScriptException e) {
 			org.apache.log4j.Logger fLog = org.apache.log4j.Logger.getLogger("log.script.scripts");
 			fLog.error("Script invocation failed.", e);
 		}
 		return rv;
 	}
 	
 	@Override
 	public int compareTo(Script o) {
 		return reference.getName().compareTo(o.reference.getName());
 	}
 }
