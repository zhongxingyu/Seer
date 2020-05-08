 package http.server;
 
 import http.request.Request;
 import http.response.Response;
 import http.router.Router;
 import http.server.logger.SystemLogger;
 import http.server.logger.SystemLoggerFactory;
 import http.server.serverSocket.ServerSocketFactory;
 import http.server.serverSocket.WebServerSocket;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 public class Server {
   public File workingDirectory;
   public WebServerSocket theServerSocket;
 
   public Server(int port, String publicDirectory, String env, String routesFilePath, String htAccessFilePath) throws Exception {
     workingDirectory = new File(System.getProperty("user.dir"));
     ServerSocketFactory theServerSocketFactory = new ServerSocketFactory();
     theServerSocket = theServerSocketFactory.get(port, env);
     run(theServerSocket, port, publicDirectory, env, routesFilePath, htAccessFilePath);
   }
 
   public void run(WebServerSocket theServerSocket, int port, String publicDirectory, String env, String routesFilePath, String htAccessFilePath) throws IOException {
     SystemLoggerFactory theSystemLoggerFactory = new SystemLoggerFactory();
     SystemLogger logger = theSystemLoggerFactory.get(env);
 
     Request request = new Request();
     Router router = new Router(publicDirectory, routesFilePath, htAccessFilePath);
     Response response = new Response();
 
     logger.logMessage("Ninja Server is running.  WOOT!");
     logger.logMessage("Port: "                   + port);
     logger.logMessage("Environment: "            + env);
    logger.logMessage("Now serving files from: " + new File(workingDirectory + publicDirectory).toString());
    logger.logMessage("Routes Filename: "        + new File(workingDirectory + routesFilePath).toString());
    logger.logMessage("htaccess Filename: "      + new File(workingDirectory + htAccessFilePath).toString());
 
     while (true) {
       theServerSocket.connect();
 
       HashMap receivedRequest = request.get(theServerSocket);
       logger.logMessage("REQUEST: http://" + receivedRequest.get("Host") + receivedRequest.get("url"));
 
       ArrayList routeInfo = router.getRouteInfo(receivedRequest);
 
       response.send(theServerSocket, receivedRequest, routeInfo);
       File routeFile = (File)routeInfo.get(0);
       String routeFilePath = routeFile.getAbsolutePath();
       logger.logMessage("RENDERED: http://" + receivedRequest.get("Host") + "/" + subtractPath(routeFilePath, new File(workingDirectory + publicDirectory)));
 
       theServerSocket.closeConnection();
     }
   }
 
   public static void main(String[] args) throws Exception {
     int portIndex = Arrays.asList(args).indexOf("-p");
     Integer port = Integer.parseInt(args[portIndex + 1]);
 
     int publicDirectoryIndex = Arrays.asList(args).indexOf("-d");
     String publicDirectory = args[publicDirectoryIndex + 1];
 
     int envIndex = Arrays.asList(args).indexOf("-e");
     String env = args[envIndex + 1];
 
     int routesFileIndex = Arrays.asList(args).indexOf("-r");
     String routesFilePath = args[routesFileIndex + 1];
 
     int htAccessFileIndex = Arrays.asList(args).indexOf("-h");
     String htAccessFilePath = args[htAccessFileIndex + 1];
 
     new Server(port, publicDirectory, env, routesFilePath, htAccessFilePath);
   }
 
   private String subtractPath(String routeFilePath, File publicDirectoryFullPath) {
     return routeFilePath.replace(publicDirectoryFullPath.toString(), "");
   }
 }
