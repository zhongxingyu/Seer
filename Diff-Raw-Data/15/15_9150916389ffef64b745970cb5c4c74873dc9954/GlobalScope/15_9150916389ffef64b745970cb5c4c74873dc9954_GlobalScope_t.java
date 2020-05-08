 /**
  * 
  */
 package cello.alt.servlet.js;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.ImporterTopLevel;
 import org.mozilla.javascript.NativeJavaObject;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 
 import cello.alt.servlet.RhinoServlet;
 import cello.alt.servlet.resource.ResourceException;
 import cello.alt.servlet.script.JavaScript;
 import cello.alt.servlet.script.ScriptLoader;
 import cello.alt.servlet.script.ScriptNotFoundException;
 
 /**
  * Top-level scope object for RhinoServlet.  This defines the standard objects,
  *  a global static "Rhino" object that gives access for controlling 
  *  RhinoServlet.  It also adds a self-pointing global property "global", and
  *  makes Rhino.require a protected global variable.
  *  
  * @author Marcello
  *
  */
 public class GlobalScope extends ImporterTopLevel implements ModuleProvider {
 
     /** For Eclipse warning */
     private static final long serialVersionUID = 3328680909322034652L;
     private Map<String,Module> moduleScopes = new HashMap<String,Module>();
     private RootModule rootModule;
     
     /**
      * Constructs a new GlobalScope object referencing a particular RhinoServlet
      * @param server  the server
      */
     public GlobalScope(RhinoServlet server) {
         Context cx = Context.enter();
         cx.initStandardObjects(this);
         Context.exit();
 
         // Two properties of global: a self pointer
         defineProperty("global", this, RhinoServlet.PROTECTED);
         //  and a "static" class for managing Rhino
         RhinoClass rhinoClass = new RhinoClass(server);
         defineProperty("Rhino", rhinoClass, RhinoServlet.PROTECTED);
         
         // Static class for managing servlet
         ServletClass servletClass = new ServletClass(server);
         defineProperty("Servlet", servletClass, RhinoServlet.PROTECTED);
         
         // Add require as a global method
         defineProperty("require", rhinoClass.get("require",rhinoClass),
                 RhinoServlet.PROTECTED);
 
         rootModule = new RootModule(this);
     }
     /**
      * Converts wrapped Java objects to their native object. 
      * @param o 
      * @return the potentially unwrapped object, or the object itself
      */
     public static Object getJavaObject(Object o) {
         if (o instanceof NativeJavaObject)
             return ((NativeJavaObject)o).unwrap();
         return o;
     }
 
     /**
      * @see ModuleProvider#getScriptModule(java.lang.String)
      */
     public Module getScriptModule(String name) {
         int index = name.lastIndexOf('.');
         if (index<=0)
             return rootModule;
         
         // Strip off the script name
         String modulePath = name.substring(0,index); 
             
         // Check if this module has been cached
         if (moduleScopes.containsKey(modulePath))
             return moduleScopes.get(modulePath);
         
         // Start with the global object as the base object and walk down the
         // module tree
         Module module = rootModule;
         for (String moduleName : modulePath.split("\\.")) {
             // Check if there already exists an object with this module name
             Object o = ScriptableObject.getProperty(module,moduleName);
 
             // If not, create the new scope.
             module = (o == Scriptable.NOT_FOUND) ? new Module(this, module,
                     moduleName) : (Module) o;
                 
         }
         
         // Add to cache
         moduleScopes.put(modulePath, module);
         
         return module;
     }
     private static class ServletClass extends ScriptableObject {
         /** For Eclipse warning */
         private static final long serialVersionUID = 7263015886240684296L;
 
         /**
          * Constructs a new ServletClass object
          * @param server  the RhinoServlet
          */
         private ServletClass(RhinoServlet server) {
             defineProperty("context", new NativeJavaInterface(this, 
                     server.getServletContext(), ServletContext.class), 
                     RhinoServlet.PROTECTED);
             defineProperty("config", new NativeJavaInterface(this, 
                     server.getServletConfig(), ServletConfig.class), 
                     RhinoServlet.PROTECTED);
    
         }
 
         /**
          * @see org.mozilla.javascript.ScriptableObject#getClassName()
          */
         @Override
         public String getClassName() {
             return "ServletClass";
         }
         
     }
     private static class RhinoClass extends ScriptableObject {
         /** For Eclipse warning */
         private static final long serialVersionUID = 4816919208983818311L;
         private RhinoServlet server;
         
         /**
          * Constructs a new RhinoClass object
          * @param server
          */
         private RhinoClass(RhinoServlet server) {
             this.server = server;
             defineFunctionProperties( 
                     new String[] {  "require",
                                     "evaluate",
                                     "eval",
                                     "addScriptPath",
                                     "getResource",
                                     "getRequestScope",
                                     "synchronize",
                                     "debug",
                                     "log"},
                                     RhinoClass.class,
                                     RhinoServlet.PROTECTED);
                                     
         }
         
         /**
          * @see ScriptableObject#getClassName()
          */
         @Override
         public String getClassName() {
             return "Rhino";
         }
 
         /**
          * JavaScript function "require".  In order to access the current 
          *  JavaScript scope and Context, this static form needs to be used.
          * <ul>
          *  <li>require(scriptname)</li>
          *  <li>require(scriptname,cascadeReload)</li>
          * </ul> 
          * @param cx  javascript Context
          * @param thisObj  javascript "this" object
          * @param args  the arguments passed to the function
          * @param funObj  the function object associated with this method
          * @return  the return value of the JavaScript function
          * @see ScriptableObject#defineFunctionProperties(java.lang.String[], java.lang.Class, int) 
          * @throws ScriptNotFoundException  if the script could not be found
          * @throws IOException if there was an error loading the script
          */
         public static Object require(Context cx, Scriptable thisObj, 
                 Object[] args, Function funObj) throws ScriptNotFoundException,
                 IOException {
 
             // Read arguments
             String scriptName = Context.toString(args[0]);
             
             boolean cascade = false;
             if (args.length>=2)
                 cascade = (Boolean)args[1];
 
 
             // Get the current script (the one that called require())
             JavaScript currentScript = RhinoServlet.getCurrentScript(cx);
 
             // This should only be null if this method is called from outside of
             // a JavaScript evaluate() method
             if (currentScript == null)
                 throw new RuntimeException("Current script is undefined!");
             
             // Get its loader and load the script
             ScriptLoader loader = currentScript.getScriptLoader();
             
             JavaScript s = loader.loadScript(scriptName);
             
             // Add the dependency
             currentScript.addDependency(s, cascade);
             // Update the dependency
             s.update(cx);
 
             return s;
         }
         /**
          * JavaScript function "evaluate".  In order to access the current 
          *  JavaScript scope and Context, this static form needs to be used.
          * <ul>
          *  <li>evaluate(scriptname)</li>
          *  <li>evaluate(scriptname,scope)</li>
          * </ul> 
          * @param cx  javascript Context
          * @param thisObj  javascript "this" object
          * @param args  the arguments passed to the function
          * @param funObj  the function object associated with this method
          * @return  the return value of the JavaScript function
          * @see ScriptableObject#defineFunctionProperties(java.lang.String[], java.lang.Class, int) 
          * @throws ScriptNotFoundException if the specified script could not be found
          * @throws IOException  if there was a problem evaluating the script
          */
         public static Object evaluate(Context cx, Scriptable thisObj, 
                 Object[] args, Function funObj) throws ScriptNotFoundException,
                 IOException {
 
             // Read arguments
             if (args.length==0) return false;
             String scriptName = Context.toString(args[0]);
             
             // Get scope
             Scriptable scope = thisObj;
             if (args.length>=2 && args[1] instanceof Scriptable)
                 scope = (Scriptable)args[1];
 
             // Get the current script (the one that called evaluate())
             JavaScript currentScript = RhinoServlet.getCurrentScript(cx);
             
             // This should never be null
             if (currentScript == null)
                 throw new RuntimeException("Current script is undefined!");
             
             // Load and evaluate the script in the current scope
             ScriptLoader loader = currentScript.getScriptLoader();
             JavaScript s = loader.loadScript(scriptName);
             
            return s.evaluate(cx, scope);
         }
         /**
          * JavaScript function "evaluate".  In order to access the current 
          *  JavaScript scope and Context, this static form needs to be used.
          * <ul>
          *  <li>evaluate(scriptname)</li>
          *  <li>evaluate(scriptname,scope)</li>
          * </ul> 
          * @param cx  javascript Context
          * @param thisObj  javascript "this" object
          * @param args  the arguments passed to the function
          * @param funObj  the function object associated with this method
          * @return  the return value of the JavaScript function
          * @see ScriptableObject#defineFunctionProperties(java.lang.String[], java.lang.Class, int) 
          */
         public static Object eval(Context cx, Scriptable thisObj, 
                 Object[] args, Function funObj) {
 
             // Read arguments
             if (args.length==0) return false;
             String source = Context.toString(args[0]);
             
             // Get scope
             Scriptable scope = thisObj;
             if (args.length>=2 && args[1] instanceof Scriptable)
                 scope = (Scriptable)args[1];
 
             // Get the current script (the one that called evaluate())
             JavaScript currentScript = RhinoServlet.getCurrentScript(cx);
             
             // This should never be null
             if (currentScript == null)
                 throw new RuntimeException("Current script is undefined!");
             
             
             return cx.evaluateString(scope,source,currentScript.getName()+"(eval)",1,null);
         }
 
         /**
          * JavaScript function "getResource".  In order to access the current 
          *  JavaScript scope and Context, this static form needs to be used.
          * <ul>
          *  <li>getResource(resourcename)</li>
          * </ul> 
          * @param cx  javascript Context
          * @param thisObj  javascript "this" object
          * @param args  the arguments passed to the function
          * @param funObj  the function object associated with this method
          * @return  the return value of the JavaScript function
          * @see ScriptableObject#defineFunctionProperties(String[], Class, int)
          * @throws ResourceException if there was a problem getting the resource
          */
         public static Object getResource(Context cx, Scriptable thisObj, 
                 Object[] args, Function funObj) throws ResourceException {
 
             // Read arguments
             String resourceName = Context.toString(args[0]);
             
             // Get the current script (the one that called require())
             JavaScript currentScript = RhinoServlet.getCurrentScript(cx);
 
             // This should only be null if this method is called from outside of
             // a JavaScript evaluate() method
             if (currentScript == null)
                 throw new RuntimeException("Current script is undefined!");
             
             // Get the resource relative to the current script
             return Context.javaToJS(currentScript.getResource(resourceName),
                     thisObj);
         }        
         /**
          * JavaScript function "getRequestScope".  This returns the current
          *  scope of execution, unique to every request. 
          *   
          * @param cx  javascript Context
          * @param thisObj  javascript "this" object
          * @param args  the arguments passed to the function
          * @param funObj  the function object associated with this method
          * @return  the return value of the JavaScript function
          * @see ScriptableObject#defineFunctionProperties(String[], Class, int)
          */
         public static Object getRequestScope(Context cx, Scriptable thisObj, 
                 Object[] args, Function funObj) {
 
             return RhinoServlet.getRequestScope(cx);
         }        
         /**
          * The JavaScript function "addScriptPath" has two formats:
          * <ul>
          *  <li>addScriptPath(string)</li>
          *  <li>addScriptPath({@link ScriptLoader})</li>
          * </ul>
          * @param path  the path object
          * @throws IOException  if there was an error adding the script path
          */
         public void addScriptPath(Object path) throws IOException {
             if (path instanceof ScriptLoader)
                 server.addScriptLoader((ScriptLoader)path);
             else
                 server.addScriptPath(Context.toString(path));
         }
         /**
          * Starts the Rhino debugger
          */
         public void debug() {
             server.startDebugger();
         }
         /**
          * The JavaScript function "log"
          * @param msg
          */
         public void log(Object msg) {
             System.out.println("Rhino.log: "+Context.toString(msg));
         }
         
         /**
          * The JavaScript function "log"
          * @param cx 
          * @param thisObj 
          * @param args 
          * @param funObj 
          * @return the lock object
          */
         public static Object synchronize(Context cx, Scriptable thisObj, 
                 Object[] args, Function funObj) {
             Object lock = args[0];
             Function func = (Function)args[1];
             
             synchronized (lock) {
                 func.call(cx, func.getParentScope(), thisObj, new Object[]{cx,lock,func,funObj,thisObj});
             }
             
             return lock;
         }
 
 
     }
     
 
 }
