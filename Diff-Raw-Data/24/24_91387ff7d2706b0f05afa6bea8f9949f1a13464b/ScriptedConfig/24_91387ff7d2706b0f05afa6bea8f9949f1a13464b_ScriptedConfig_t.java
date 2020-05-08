 package app.config;
 
 import common.util.ResourceLoader;
 
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
 import java.io.IOException;
 
 public class ScriptedConfig {
 
     public static void load(String scriptName, ScriptServices scriptServices) throws ScriptException, IOException {
 
         ScriptEngineManager factory = new ScriptEngineManager();
 
         String[] tuple = scriptName.split("\\.");
         assert tuple.length == 2;
 
         ScriptEngine engine = factory.getEngineByExtension(tuple[1]);
         engine.getContext().setAttribute("app", scriptServices, ScriptContext.ENGINE_SCOPE);
 
         String script = ResourceLoader.loadResource(scriptName);
         engine.eval(script);
     }
 
 }
