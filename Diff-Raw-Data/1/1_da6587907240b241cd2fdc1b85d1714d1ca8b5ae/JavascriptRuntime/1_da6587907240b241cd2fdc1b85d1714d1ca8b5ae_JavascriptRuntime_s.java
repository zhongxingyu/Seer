 package org.intellij.ideajs.runconfiguration;
 
 import com.intellij.openapi.project.Project;
 import org.dynjs.Config;
 import org.dynjs.runtime.DynJS;
 import org.dynjs.runtime.JSFunction;
 import org.dynjs.runtime.Types;
 import org.dynjs.runtime.builtins.Require;
 
 public class JavascriptRuntime {
     private final DynJS runtime;
 
     public JavascriptRuntime(Project project) {
         Config config = new Config(this.getClass().getClassLoader());
         config.setCompileMode(Config.CompileMode.OFF);
         this.runtime = new DynJS(config);
        this.runtime.clearModuleCache();
         ((Require)runtime.getExecutionContext().getGlobalObject().get("require"))
                 .addLoadPath(project.getBasePath());
     }
 
     public Object call(String script, Object... args) {
         JSFunction function = (JSFunction)runtime.evaluate(script);
         Object result = runtime.getExecutionContext().call(function, (Object)null, args);
         if(Types.NULL.equals(result) || Types.UNDEFINED.equals(result)) {
             return null;
         } else {
             return result;
         }
     }
 }
