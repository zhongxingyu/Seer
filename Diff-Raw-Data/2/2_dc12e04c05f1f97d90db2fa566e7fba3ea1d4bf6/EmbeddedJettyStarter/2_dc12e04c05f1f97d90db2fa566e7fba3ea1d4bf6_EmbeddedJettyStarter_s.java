 package util;
 
 import org.mortbay.jetty.Connector;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.handler.ContextHandlerCollection;
 import org.mortbay.jetty.nio.BlockingChannelConnector;
 import org.mortbay.jetty.webapp.WebAppContext;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.BindException;
 import java.net.ServerSocket;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 
 public class EmbeddedJettyStarter
 {
 
     public static void start(){
         new EmbeddedJettyStarter().startJettyWithDefaultServlets("/", 8080);
     }
 
 
     public static void main(String[] args) {
         new EmbeddedJettyStarter().startJettyWithDefaultServlets("/", 8080);
     }
 
 
     public void startJettyWithDefaultServlets(String contextPath, int port) {
         try {
             dieIfRunning(port);
 
             Server server = createServer( port );
 
             final File webappRootLocation = getWebappRotLocation();
 
             WebAppContext appContext = createAppContext(contextPath, webappRootLocation);
 
             appContext.setInitParams(Collections.singletonMap("useFileMappedBuffer", "false"));
 
             ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
             server.setHandler(contextHandlerCollection);
             contextHandlerCollection.addHandler(appContext);
 
             server.start();
             // server.join();
 
         } catch (Exception e) {
             e.printStackTrace();
             System.exit(-1);
         }
     }
 
 
 
     private WebAppContext createAppContext(String contextPath, File file) throws IOException {
         return new WebAppContext(file.getAbsolutePath(), contextPath);
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     private WebAppContext getTestAppContext(String contextPath, File file) {
         String testLocation = file.getAbsolutePath().replace("main", "test");
         final File testScope = new File(testLocation);
         System.out.println("testContext.getAbsolutePath() = " + testScope.getAbsolutePath());
         return new WebAppContext(testScope.getAbsolutePath(), contextPath);
     }
 
 
     private File getWebappRotLocation() throws IOException {
         final File file = computeWebappRootLocation(this.getClass());
         if (null == file) {
             throw new IllegalStateException("The application must " + "have the module path as the working directory, was ["
                     + new File("./").getCanonicalPath() + "]");
         }
         System.out.println("webapp.getAbsolutePath() = " + file.getAbsolutePath());
         return file;
     }
 
     private Server createServer(int port) {
         Server server = new Server();
         server.setStopAtShutdown(true);
         Connector connector = createConnector();
         connector.setPort(port);
         connector.setHost("0.0.0.0");
         connector.setHeaderBufferSize(8192);
         server.addConnector(connector);
         return server;
     }
 
     protected Connector createConnector() {
         return new BlockingChannelConnector();
     }
 
 
     private static File computeWebappRootLocation(Class<? extends EmbeddedJettyStarter> webAppClass) {
         File projectPath;
         URL resource = webAppClass.getResource(webAppClass.getSimpleName() + ".class");
         String path = resource.getPath();
         System.out.println("path = " + path);
         int targetDirIdx = path.indexOf("/target");
         if (targetDirIdx < 0) {
             /*
             * this is really no better but I need to find a better way...
             */
            resource = webAppClass.getResource("/applicationContext.xml");
             path = resource.getPath();
             System.out.println("path = " + path);
             targetDirIdx = path.indexOf("/target");
 
         }
         projectPath = new File(path.substring(0, targetDirIdx));
         String relativePath = "src/main/webapp";
         List<File> webAppFoldersToTry = Arrays.asList(new File(relativePath), new File(projectPath, relativePath));
         for (File folder : webAppFoldersToTry) {
             if (folder.exists()) {
                 return folder;
             }
         }
         return null;
     }
 
     private static void dieIfRunning(int port) throws IOException {
         ServerSocket socket = null;
         try {
             socket = new ServerSocket(port);
         } catch (BindException e) {
             throw new RuntimeException("Server is already running on port [" + port + "]");
         } finally {
             if (socket != null) {
                 socket.close();
             }
         }
     }
 
 }
