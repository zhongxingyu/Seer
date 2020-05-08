 /**
  * 
  */
 package cello.alt.servlet.scripting;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.ImporterTopLevel;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 
 import cello.alt.servlet.RhinoServlet;
 
 /**
  * Top-level scope object for RhinoServlet.  This defines the standard objects,
  *  a global static "Rhino" object that gives access for controlling 
  *  RhinoServlet.  It also adds a self-pointing global property "global", and
  *  makes Rhino.require a protected global variable.
  *  
  * @author Marcello
  *
  */
 public class GlobalScope extends ImporterTopLevel {
 
     /** For Eclipse warning */
     private static final long serialVersionUID = 3328680909322034652L;
     private Map<String,Scriptable> moduleScopes;
     
     /**
      * Constructs a new GlobalScope object referencing a particular RhinoServlet
      * @param server  the server
      */
     public GlobalScope(RhinoServlet server) {
         Context cx = Context.enter();
         cx.initStandardObjects(this,true);
         Context.exit();
         
         moduleScopes = new HashMap<String,Scriptable>();
 
         // Two properties of global: a self pointer
         defineProperty("global", this, RhinoServlet.PROTECTED);
         //  and a "static" class for managing Rhino
         RhinoClass rhinoClass = new RhinoClass(server);
         defineProperty("Rhino", rhinoClass, RhinoServlet.PROTECTED);
         // Add require as a global method
         defineProperty("require", rhinoClass.get("require",rhinoClass),
                 RhinoServlet.PROTECTED);
     }
    
 
     /**
      * Each module has its own scope from which its scripts are executed. This
      *  method returns that scope (creating it if necessary).  It also creates
      *  the necessary object hierarchy.  For example, if this method were called
      *  with the argument "module1.module2.Script", a global.module1 would be
      *  created, and within that a module2, global.module1.module2.  
      *  
      * @param name  the module/script name (or .*)
      * @return the scope associated with this module/script name
      */
     public Scriptable getModuleScope(String name) {
         int index = name.lastIndexOf('.');
         if (index<=0)
             return this;
         
         // Strip off the script name
         String modulePath = name.substring(0,index); 
             
         // Check if this module has been cached
         if (moduleScopes.containsKey(modulePath))
             return moduleScopes.get(modulePath);
         
         // Start with the global object as the base object and walk down the
         // module tree
         Scriptable scope = this;
         for (String moduleName : modulePath.split("\\.")) {
             // Check if there already exists an object with this module name
             Object o = scope.get(moduleName, scope);
             // If not, create the new scope.
             if (o == Scriptable.NOT_FOUND) {
                 ScriptableObject child = new NamedScriptableObject("Module "+moduleName);
                 // Prototype the child scope with the global scope so it has
                 // direct access to all its values, to read them, anyway 
                 child.setPrototype(this);
                 
                 // Create a self-pointer
                 child.defineProperty("module", child, RhinoServlet.PROTECTED);
 
                 // We want "threadScope" to be a new top-level scope, so set its parent 
                 // scope to null. This means that any variables created by assignments
                 // will be properties of "threadScope".
                 child.setParentScope(null);                
 
                 // Add the child scope as a member of the parent
                 scope.put(moduleName, scope, child);
                 
                 scope = child;
             } else {
                 scope = (Scriptable)o;
             }
                 
         }
         
         // Add to cache
         moduleScopes.put(modulePath, scope);
         System.out.println("getModuleScope("+name+") : "+scope);
         
         return scope;
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
                                     "addScriptPath",
                                     "getResource",
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
            String scriptName = (String)args[0];
             
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
             s.update(cx, (GlobalScope)cx.getThreadLocal("globalScope"));
 
             return false;
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
            String scriptName = (String)args[0];
             
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
             s.evaluate(cx, scope);
             
             return false;
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
          */
         public static Object getResource(Context cx, Scriptable thisObj, 
                 Object[] args, Function funObj) {
 
             // Read arguments
             String resourceName = (String)args[0];
             
             // Get the current script (the one that called require())
             JavaScript currentScript = RhinoServlet.getCurrentScript(cx);
 
             // This should only be null if this method is called from outside of
             // a JavaScript evaluate() method
             if (currentScript == null)
                 throw new RuntimeException("Current script is undefined!");
             
             // Get its loader and then the resource
             ScriptLoader loader = currentScript.getScriptLoader();
             
             return loader.getResource(resourceName);
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
                 server.setScriptPath((ScriptLoader)path);
             else if (path instanceof String)
                 server.addScriptPath((String)path);
             else
                 throw new IOException("Invalid ScriptLoader "+path);
         }
         
         /**
          * The JavaScript function "log"
          * @param msg
          */
         public void log(Object msg) {
             System.out.println("Rhino.log: "+msg.toString());
         }
 
     }
     
 
 }
