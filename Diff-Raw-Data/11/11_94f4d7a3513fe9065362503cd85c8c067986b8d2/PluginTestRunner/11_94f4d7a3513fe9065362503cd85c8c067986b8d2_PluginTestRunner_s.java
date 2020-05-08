 package cn.orz.pascal.cui.utils;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.List;
 import java.util.Map;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 
 import cn.orz.pascal.phonegap.MorphologicalAnalysiser;
 
 public class PluginTestRunner {
 	ScriptEngineManager manager = new ScriptEngineManager();
 	ScriptEngine engine = manager.getEngineByName("JavaScript");
 
 	public static void main(String[] args) throws Exception {
 		new PluginTestRunner().run(args);
 	}
 
 	public void run(String[] args) throws Exception {
 		if (engine != null) {
 			try {
 				this.engine.put("PluginTestRunner", this);
 				this.engine
 						.eval("function load(filename) { PluginTestRunner.load(filename); }");
 				this.engine.eval("window = {};");
 				this.engine
 						.eval("console = {log:function(message){print(message + \"\\n\")}};");
 				this.engine
 						.eval("function exec(action, args, callbackId){ return PluginTestRunner.exec(action, args, callbackId) };");
 				engine.eval(new FileReader(args[0]));
 			} catch (ScriptException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public Result exec(String action, Object[] args, String callbackId) {
 		String text = args[0].toString();
 		MorphologicalAnalysiser obj = new MorphologicalAnalysiser();
 		Result result = new Result("OK", obj.analyse(text));
 
 		return result;
 	}
 
	public void load(String filename) throws ScriptException {
 		try {
			this.engine.eval(new FileReader(filename));
 		} catch (FileNotFoundException e) {
 			throw new RuntimeException("Error loading javascript file: "
 					+ filename, e);
 		}
 	}
 }
