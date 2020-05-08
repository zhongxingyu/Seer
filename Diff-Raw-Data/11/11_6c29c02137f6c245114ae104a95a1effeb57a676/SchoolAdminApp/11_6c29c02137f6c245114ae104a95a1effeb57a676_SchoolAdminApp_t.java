 package org.sukrupa.app;
 
 import org.sukrupa.app.config.AppConfig;
 import org.sukrupa.platform.server.DbServer;
 import org.sukrupa.platform.server.WebServer;
 
 import static org.sukrupa.platform.logging.ConsoleLog4jLogging.configureLogging;
 import static org.sukrupa.platform.text.StringManipulation.join;
 
 public class SchoolAdminApp {
 
     private static final int HTTP_PORT = 8080;
     private static final String WEB_APP_CONTEXT = "/sukrupa";
    private static final String DEFAULT_WEB_ROOT = "../../src/web";
 
     private DbServer dbServer;
     private WebServer webServer;
 
    public SchoolAdminApp(String webRoot) {
         dbServer = new DbServer(rootDir(), dbName());
        webServer = new WebServer(webRoot, HTTP_PORT, WEB_APP_CONTEXT);
     }
 
     public void start() {
         configureLogging();
         dbServer.start();
         webServer.start();
     }
 
     public void shutdown() {
         dbServer.shutDown();
     }
 
     private String rootDir() {
         return join(userHome(), property("db.root.dir"));
     }
 
     private String dbName() {
         return property("db.name");
     }
 
     private String userHome() {
         return System.getProperty("user.home");
     }
 
     private String property(String key) {
         return new AppConfig().properties().getProperty(key);
     }
 
     public static void main(String[] args) throws Exception {
         SchoolAdminApp schoolAdminApp = new SchoolAdminApp(warDirectoryNameFrom(args));
         try {
             schoolAdminApp.start();
         } finally {
             schoolAdminApp.shutdown();
         }
     }
 
     private static String warDirectoryNameFrom(String[] args) {
        return args.length > 0 ? args[0] : DEFAULT_WEB_ROOT;
     }
 }
