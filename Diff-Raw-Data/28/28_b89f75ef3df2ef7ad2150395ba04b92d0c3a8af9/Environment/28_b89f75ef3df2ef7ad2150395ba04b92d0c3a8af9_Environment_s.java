 package marco.internal;
 
 import marco.MarcoBindingError;
 import marco.MarcoException;
 import marco.lang.MarcoNil;
 import marco.macros.def;
 import marco.macros.function;
 import marco.lang.MarcoObject;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class Environment {
     private Map<String, MarcoObject> env = new HashMap<>();
 
     public Environment() {
        add("def", new def());
        add("function", new function());
        add("nil", new MarcoNil());
     }
 
     public Environment(Map env) {
         this.env = env;
     }
 
    public void add(String var, MarcoObject value) {
         if (env.containsKey(var)) {
             throw new MarcoBindingError(var, value, env.get(var));
         } else {
             env.put(var, value);
         }
     }
 
     public MarcoObject lookUp(String var) {
         if (env.containsKey(var)) {
             return env.get(var);
         } else {
             throw new MarcoException("Undefined variable: " + var);
         }
     }
 
     public Environment duplicate() {
         Map newEnv = new HashMap();
         newEnv.putAll(env);
         return new Environment(newEnv);
     }
 }
