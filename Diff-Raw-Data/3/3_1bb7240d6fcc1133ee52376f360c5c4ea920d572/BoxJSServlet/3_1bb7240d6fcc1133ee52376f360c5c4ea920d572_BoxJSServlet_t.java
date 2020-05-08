 package sbx.boxjs;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.naming.InitialContext;
 import javax.servlet.GenericServlet;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.sql.DataSource;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ContextFactory;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 
 import sun.misc.IOUtils;
 
 public class BoxJSServlet extends GenericServlet {
 
     private static final long serialVersionUID = -1L;
     private static final Logger log = Logger.getLogger(BoxJSServlet.class.getName());
     Map<String, String> cache = new HashMap<String, String>();
     Scriptable rootScope;
     DataSource ds;
     Function service;
 
     @Override
     public void service(ServletRequest rrequest, ServletResponse rresponse)
             throws ServletException, IOException {
         HttpServletRequest request = (HttpServletRequest) rrequest;
         HttpServletResponse response = (HttpServletResponse) rresponse;
         Scriptable scope;
 
         try {
             Context jcx = ContextFactory.getGlobal().enterContext();
             scope = jcx.newObject(rootScope);
             scope.setPrototype(rootScope);
             scope.setParentScope(null);
             ScriptableObject.putProperty(scope, "scope", Context.javaToJS(scope, scope));
             ScriptableObject.putProperty(scope, "jscontext", Context.javaToJS(jcx, scope));
             ScriptableObject.putProperty(scope, "request", Context.javaToJS(request, scope));
             ScriptableObject.putProperty(scope, "response", Context.javaToJS(response, scope));
             Object functionArgs[] = {request, response};
             service.call(jcx, scope, scope, functionArgs);
         } catch (Exception e) {
             log.severe("Servlet Error (" + e.getClass() + ")" + e.getMessage() + "\nuri: " + request.getRequestURI());
             e.printStackTrace();
             response.getWriter().println(
                     "<html><body><b>Servlet Error (" + e.getClass()
                     + ")</b><xmp>" + e.getMessage() + "</xmp></body></html>");
         } finally {
             Context.exit();
         }
     }
 
     public Object require(Object fileName) {
         String filename = fileName.toString();
         Scriptable newScope = null;
         String applicationRoot = null;
 
         try {
             Context cx = org.mozilla.javascript.ContextFactory.getGlobal().enterContext();
             synchronized (rootScope) {
                 Object config = org.mozilla.javascript.ScriptableObject.getProperty(rootScope, "config");
                 applicationRoot = (String) ((ScriptableObject) config).get("applicationRoot", rootScope);
                 newScope = cx.newObject(rootScope);
                 newScope.setPrototype(rootScope);
             }
             ScriptableObject.putProperty(newScope, "scope", newScope);
             ScriptableObject.putProperty(newScope, "jscontext", cx);
 
             evaluateJavascriptFile(cx, newScope, applicationRoot + filename);
             return org.mozilla.javascript.ScriptableObject.getProperty(newScope, "exports");
         } catch (Exception e) {
             System.out.println("Error: " + e.getMessage());
         } finally {
             Context.exit();
         }
         return null;
     }
 
     public Object runScript(String filename, HttpServletRequest request, Scriptable scope) throws Exception {
         Object ret = null;
         String srcName = null;
         Context ctx = null;
 
         try {
             if (isDeveloperMode() || cache.get(filename) == null) {
                 String filePath = "/boxjs" + filename;
                 filePath = request.getServletContext().getRealPath(filePath);
                 srcName = filePath.replaceAll(".*?\\\\(\\w+\\.\\w+)", "$1");
                 System.out.println("loading(" + filename + ")....................................................");
                 cache.put(filename, new String(IOUtils.readFully(new FileInputStream(filePath), -1, true)));
             }
             ctx = Context.enter();
             ret = ctx.evaluateString(scope, cache.get(filename), srcName, 1, null);
 
         } catch (Exception e) {
             e.printStackTrace();
             throw new Exception(e);
         } finally {
             Context.exit();
         }
 
         return org.mozilla.javascript.Context.toString(ret);
     }
 
     @SuppressWarnings("rawtypes")
     private boolean isDeveloperMode() {
         Map config = (Map) rootScope.get("config", rootScope);
         Boolean developerMode = (Boolean) config.get("developerMode");
         return developerMode != null && developerMode;
     }
 
     public Object evaluateJavascriptFile(Context ctx, Scriptable scope, String path) throws FileNotFoundException, IOException {
         String filename = this.getServletConfig().getServletContext().getRealPath("/boxjs" + path);
         String srcName = filename.replaceAll(".*?\\\\(\\w+\\.\\w+)", " $1");
         FileReader rd = null;
         Object r = null;
 
         if (new File(filename).exists()) {
             r = ctx.evaluateReader(scope, rd = new FileReader(filename), srcName, 1, null);
         }
         if (rd != null) {
             rd.close();
         }
         return r;
     }
 
     @SuppressWarnings("rawtypes")
     private void loadDatabaseModule(Map config, Context ctx) throws Exception {
         Map dbmap = (Map) config.get("database");
         String datasource = (String) dbmap.get("datasource");
         if (datasource != null) {
             log.info("initializing db connection pool ...");
 
             InitialContext initContext = new InitialContext();
			DataSource ds = (DataSource)initContext.lookup(datasource);
 
             ScriptableObject.putProperty(rootScope, "ds", Context.javaToJS(ds, rootScope));
             log.info("db connection pool initialized.");
 
             log.info("loading database.pool.js ...");
             evaluateJavascriptFile(ctx, rootScope, "/modules/database.js");
         } else {
             log.info("loading database.js ...");
             java.lang.Class.forName(dbmap.get("driver").toString());
             evaluateJavascriptFile(ctx, rootScope, "/modules/database.without.pool.js");
         }
     }
 
     @Override
     protected void finalize() throws Throwable {
         System.out.println("finalize .......................................................\n");
         Context.exit();
     }
     
 
     @Override
     @SuppressWarnings({ "rawtypes", "unchecked" })
     public void init() throws ServletException {
         log.info("boxJS initializing ...\n");
 
         Map config;
         Context ctx = ContextFactory.getGlobal().enterContext();
         rootScope = ctx.initStandardObjects();
 
         ScriptableObject.putProperty(rootScope, "servlet", Context.javaToJS(this, rootScope));
         ScriptableObject.putProperty(rootScope, "jscontext", Context.javaToJS(ctx, rootScope));
         ScriptableObject.putProperty(rootScope, "rootScope", Context.javaToJS(rootScope, rootScope));
         ScriptableObject.putProperty(rootScope, "scope", Context.javaToJS(rootScope, rootScope));
         ScriptableObject.putProperty(rootScope, "log", Context.javaToJS(log, rootScope));
 
         try {
             log.info("loading config.js ...");
             evaluateJavascriptFile(ctx, rootScope, "/config.js");
 
             log.info("loading platform.js ...");
             evaluateJavascriptFile(ctx, rootScope, "/platform.js");
             service = (Function) rootScope.get("service", rootScope);
 
             config = (Map) rootScope.get("config", rootScope);
 
 			List<String> modules = (List<String>) config.get("modules");
 
             for(String mod : modules) {
                 if (mod.equals("database")) {
                     loadDatabaseModule(config, ctx);
                 } else {
                     log.info("loading io.js ...");
                     evaluateJavascriptFile(ctx, rootScope, "/modules/" + mod + ".js");
                 }
             }
             /*
             loadBinaryModule(config, ctx);
             loadIOModule(config, ctx);
             loadWebServiceModule(config, ctx);
 
             loadLibs(config, ctx);
             */
             
             log.info("loading application.js ...");
             String appPath = (String) (config).get("entryPoint");
             appPath = (appPath == null || appPath.isEmpty()) ? "/application.js" : appPath;
             evaluateJavascriptFile(ctx, rootScope, appPath);
             log.info(appPath + ".js files loaded.");
 
         } catch (Exception e) {
             OutputStream out = new ByteArrayOutputStream();
             e.printStackTrace(new PrintStream(out));
             log.severe(e.getMessage() + ": \n" + out.toString());
             e.printStackTrace();
         } finally {
             Context.exit();
         }
         log.info("boxJS initialed!\n=========================================================================================\n");
     }
 }
