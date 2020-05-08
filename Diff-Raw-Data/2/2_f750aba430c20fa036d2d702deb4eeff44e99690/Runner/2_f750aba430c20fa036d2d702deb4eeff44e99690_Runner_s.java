 package gino;
 
 import java.io.IOException;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ContextFactory;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.ImporterTopLevel;
 import org.mozilla.javascript.ScriptableObject;
 
 public class Runner {
 
   public static Context enterContext(ClassLoader classLoader) {
     Context cx = ContextFactory.getGlobal().enterContext();
     cx.setLanguageVersion(Context.VERSION_1_8);
     cx.setApplicationClassLoader(classLoader);
     return cx;
   }
 
   public static ScriptableObject createScope(Context cx, Object logger) throws IOException {
     ScriptableObject scope = new ImporterTopLevel(cx);
     if (logger != null)
       scope.put("logger", scope, Context.javaToJS(logger, scope));
     Functions.defineFunctions(scope);
     Functions.loadScript(cx, scope, "gino/scope.js", true);
     return scope;
   }
 
   public static void exitContext() {
     Context.exit();
   }
 
   public static Object run(String scriptFileName, Object[] args, ClassLoader classLoader, Object logger) throws IOException {
     Object result;
     Context cx = enterContext(classLoader);
     try {
       ScriptableObject scope = createScope(cx, logger);
       Object bootFunc = Functions.loadScript(cx, scope, "gino/boot.js", true);
       if (!(bootFunc instanceof Function))
         throw new RuntimeException("result of boot script is expected to be a function");
      Object[] bootArgs = new Object[] { Context.javaToJS(scriptFileName, scope), args };
       result = ((Function) bootFunc).call(cx, scope, null, bootArgs);
     } finally {
       exitContext();
     }
     return result;
   }
 }
