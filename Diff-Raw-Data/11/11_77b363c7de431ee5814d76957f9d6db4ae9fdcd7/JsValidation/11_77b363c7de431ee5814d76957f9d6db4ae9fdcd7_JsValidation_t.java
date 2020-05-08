 package com.choas.jsvalidator.validate;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.script.Compilable;
 import javax.script.CompiledScript;
 import javax.script.ScriptContext;
 import javax.script.ScriptEngineManager;
 import javax.script.SimpleScriptContext;
 import javax.script.*;
 
 /**
  * This is a sample class for JavaScript validation used inside of Java.
  * 
  * TODO license
  * 
  * @author gregoril
  * 
  */
 public class JsValidation {
 
 	private CompiledScript javaWrapperScript = null;
 
 	public void run(Object obj, List<String> validateMsgs) {
 		ScriptEngineManager mgr = new ScriptEngineManager();
 		Compilable ce = (Compilable) mgr.getEngineByName("ECMAScript");
 		try {
 			String script = readJSfiles().toString();
 			this.javaWrapperScript = ce.compile(script);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		int SCOPE = ScriptContext.ENGINE_SCOPE;
 		ScriptContext context = new SimpleScriptContext();
 		context.setAttribute("obj", obj, SCOPE);
 		context.setAttribute("validateMsgs", validateMsgs, SCOPE);
 
 		try {
 			this.javaWrapperScript.eval(context);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private StringBuffer readJSfiles() throws IOException {
 		final StringBuffer buffer = new StringBuffer();
 		List<String> jsfiles = new ArrayList<String>();
		jsfiles.add("src/main/resources/js/validate.js");
		jsfiles.add("src/main/resources/js/javaWrapper.js");
 
 		String newline = System.getProperty("line.separator");
 		for (String jsfile : jsfiles) {
 			System.out.println("read " + jsfile);
 			FileReader fr = new FileReader(jsfile);
 			BufferedReader bufRead = new BufferedReader(fr);
 			String line;
 			while ((line = bufRead.readLine()) != null) {
 				buffer.append(line).append(newline);
 			}
 		}
 		return buffer;
 	}
 
 	public void runWithDebug(Object obj, List<String> validateMsgs) {
 		// http://blogs.oracle.com/sundararajan/entry/javascript_debugging_tips_for_mustang
 
 		// create a new ScriptEngineManager
 		ScriptEngineManager m = new ScriptEngineManager();
 		// get JavaScript engine instance
 		ScriptEngine jsEngine = m.getEngineByName("javascript");
 		// set a "debug" bindings for global variables
 		jsEngine.setBindings(new DebugBindings(), ScriptContext.ENGINE_SCOPE);
 		// eval code
 		jsEngine.put("obj", obj);
 		jsEngine.put("validateMsgs", validateMsgs);
 		try {
 			String script = readJSfiles().toString();
 			jsEngine.eval(script);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public class DebugBindings extends SimpleBindings {
 		@Override
 		public Object put(String name, Object value) {
 			Object res = super.put(name, value);
 			System.out.println("Global assign: " + name);
 			return res;
 		}
 
 		@Override
 		public Object get(Object key) {
 			Object res = super.get(key);
 			System.out.println("Global access: " + key);
 			return res;
 		}
 	}
 }
