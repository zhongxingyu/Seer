 package ru.vkirilchuk.examples.spring.hessian.server;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 
 import ru.vkirilchuk.examples.spring.hessian.api.ScriptDTO;
 import ru.vkirilchuk.examples.spring.hessian.api.ScriptService;
 
 public class ScriptServiceImpl implements ScriptService {
 
     private ScriptEngineManager scriptEngineFactory;
     
     public ScriptServiceImpl(ScriptEngineManager scriptEngineFactory) {
         this.scriptEngineFactory = scriptEngineFactory;
     }
 
     public Object execute(ScriptDTO script) throws ScriptException {
         String type = script.getType();
         String text = script.getText();
 
        ScriptEngine engine = scriptEngineFactory.getEngineByName(type);
         return engine.eval(text);
     }
 }
