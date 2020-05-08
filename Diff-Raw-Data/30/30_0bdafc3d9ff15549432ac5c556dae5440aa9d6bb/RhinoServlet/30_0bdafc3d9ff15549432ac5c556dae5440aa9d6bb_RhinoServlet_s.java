 package cello.alt.servlet;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ContextFactory;
 import org.mozilla.javascript.RhinoException;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 
 import cello.alt.servlet.scripting.DirectoryScriptLoader;
 import cello.alt.servlet.scripting.DynamicFactory;
 import cello.alt.servlet.scripting.GlobalScope;
 import cello.alt.servlet.scripting.JarScriptLoader;
 import cello.alt.servlet.scripting.JavaScript;
 import cello.alt.servlet.scripting.NamedScriptableObject;
 import cello.alt.servlet.scripting.ScriptLoader;
 
 /**
  * The main Servlet class for serving up JavaScript-based web pages.
  * 
  * @author Marcello
  *
  */
 
 public class RhinoServlet extends HttpServlet {
     
     private static final long serialVersionUID = 2280866936332806360L;
     private ScriptLoader loader = null;
    
     private GlobalScope globalScope;
     
     /** Version string */
     public static final String VERSION = "RhinoServlet v0.02 alpha";
     
     private String entryPoint = "Main";
     /** 
      * Protected access from ScriptableObject (Don't enum, read-only, and
      *  permanent).
      */
     public static final int PROTECTED = ScriptableObject.DONTENUM |
                                          ScriptableObject.PERMANENT |
                                          ScriptableObject.READONLY;
 
     /**
      * Constructs a new RhinoServer
      */
     public RhinoServlet() {
        ContextFactory.initGlobal(new DynamicFactory());
         globalScope = new GlobalScope(this);
     }
     
 
     /**
      * Initialize this RhinoServlet.
      * @param config the Servlet configuration object
      * @throws ServletException if there was a problem initializing
      */
     @Override
     public void init(ServletConfig config) throws ServletException {
         try {
             addScriptPath(config.getInitParameter("scriptpath"));
             String s = config.getInitParameter("entryPoint");
             if (s!=null)
                 entryPoint = s;
         } catch (IOException ex) {
             throw new ServletException("Error initializing",ex);
         }
     }
     
     /**
      * Adds a string-based script path to list of supported script paths. This
      *  may construct a DirectoryScriptLoader or JarScriptLoader depending on the
      *  type of the file.
      * @param path  the path to add
      * @throws IOException  if there is an error reading the path
      */
     public void addScriptPath(String path) throws IOException {
         File file = new File(path);
         if (file.isDirectory()) {
             loader = new DirectoryScriptLoader(loader, file);
         } else if (file.isFile()) {
             String lowercase = path.toLowerCase();
             if (lowercase.endsWith(".jar") || lowercase.endsWith(".zip")) 
                 loader = new JarScriptLoader(loader, file);
         } else {
             throw new IOException("Cannot add scriptpath : "+path);
         }
     }
     /**
      * Adds a ScriptLoader object to the list of script paths.  New scripts are 
      *  loaded by traversing the script path in order.  
      * @param sl  the ScriptLoader to add
      */
     public void setScriptPath(ScriptLoader sl) {
         if (sl.getParentLoader() != loader)
             throw new RuntimeException("Cannot replace loader that does not "+
                                          "parent the current loader");
         loader = sl;
         System.out.println("added scriptpath: "+sl);
     }
     /**
      * Clears the list of script paths (this does not unload any classes).
      */
     public void clearScriptPath() {
         loader = null;
     }
     /**
      * Adds a folder to the class path loader.
      * @param path
      */
     public void addClassPath(String path) {
         throw new RuntimeException("Cannot add classpath : "+path);
     }
     
     /**
      * Gets the current thread's RhinoServer from a Context
      * @param cx  the javascript Context
      * @return  the RhinoServer, or null
      */
     public static RhinoServlet getServlet(Context cx) {
         Object o = cx.getThreadLocal("rhinoServer");
         if (o==null || !(o instanceof RhinoServlet)) return null;
         return (RhinoServlet)o;
     }
     /**
      * Gets the current thread's script from a Context.
      * @param cx  the javascript Context
      * @return the current JavaScript, or null
      */
     public static JavaScript getCurrentScript(Context cx) {
         Object o = cx.getThreadLocal("currentScript");
         if (o==null || !(o instanceof JavaScript)) return null;
         return (JavaScript)o;
     }
     /**
      * Sets the current JavaScript and returns the old one.
      * @param cx  the javascript Context
      * @param script  the JavaScript
      * @return  the previous JavaScript
      */
     public static JavaScript setCurrentScript(Context cx, JavaScript script) {
         JavaScript oldScript = getCurrentScript(cx);
         if (script != null)
             cx.putThreadLocal("currentScript",script);
         return oldScript;
     }
 
     /**
      * Helper function to print out error messages.
      * @param resp  the HTTP Server response object
      * @param ex  the error to handle
      */
     private void handleError(HttpServletResponse resp, Throwable ex) {
         try {
             if (resp.isCommitted()) {
                 PrintWriter out = resp.getWriter();
                 ex.printStackTrace(out);
                 return;
             }
             resp.setStatus(500);
             resp.setContentType("text/html");
             PrintWriter out = resp.getWriter();
             out.println("<html>");
             out.println(" <head><title>Execution Error: 500</title></head>");
             out.println(" <body>");
             out.println("  <h2>Execution Error: "+ex.getClass().getSimpleName()+"</h2>");
             out.println("  <p>Stack trace:</p>");
             out.print("  <blockquote><pre>");
             if (ex instanceof RhinoException) {
                 RhinoException rex = (RhinoException)ex;
                 rex.printStackTrace(out);
             } else {
                 ex.printStackTrace(out);
             }
             out.println("</pre></blockquote>");
             out.println(" <p><address>Powered by Alt Framework</address></p>");
             out.println(" </body>");
             out.println("</html>");
         } catch (IOException ex2) {
             ex2.printStackTrace(System.err);
         }
     }
     
     
     /**
      * Constructs a child scope from another scope.  
      * 
      * @param name  the name of the scope
      * @param scope  
      * @return the new scope
      */
     private ScriptableObject makeChildScope(String name, Scriptable scope) {
         // This code is based on sample code from Rhino's website:
         //  http://www.mozilla.org/rhino/scopes.html
         
         // I changed it to use a ScriptableObject instead of Context.newObject()
         ScriptableObject child = new NamedScriptableObject(name);
         
         // Set the prototype to the scope (so we have access to all its methods 
         // and members). 
         child.setPrototype(scope);
 
         // We want "threadScope" to be a new top-level scope, so set its parent 
         // scope to null. This means that any variables created by assignments
         // will be properties of "threadScope".
         child.setParentScope(null);
         
         return child;
     }
     
     /**
      * The main Servlet method. 
      * @param request  the HTTP request object
      * @param response  the HTTP response object
      */
     @Override
     public void service(HttpServletRequest request, 
             HttpServletResponse response) {
         Context cx = Context.enter();
         //cx.setOptimizationLevel(-1);
         cx.putThreadLocal("rhinoServer",RhinoServlet.this);
         try {
 
             // retrieve JavaScript...
             JavaScript s = loader.loadScript(entryPoint);
             
             ScriptableObject threadScope = makeChildScope("RequestScope", 
                     globalScope.getModuleScope(entryPoint));
             
             cx.putThreadLocal("globalScope", globalScope);
             // Define thread-local variables
             threadScope.defineProperty("request", request, PROTECTED);
             threadScope.defineProperty("response", response, PROTECTED);
             
             // Evaluate the script in this scope
             s.evaluate(cx, threadScope);
         } catch (Throwable ex) {
             handleError(response,ex);
         } finally {
             Context.exit();
         }
     }
 
 
 }
