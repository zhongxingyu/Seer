 package org.eclipse.jetty.test;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URL;
 import java.util.Properties;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.eclipse.jetty.util.IO;
 import org.eclipse.jetty.util.UrlEncoded;
 import org.eclipse.jetty.util.log.Log;
 import org.eclipse.jetty.util.log.Logger;
 import org.eclipse.jetty.websocket.WebSocket;
 import org.eclipse.jetty.websocket.WebSocketClient;
 import org.eclipse.jetty.websocket.WebSocketClientFactory;
 
 public class TestClient
 {
     private static String getJettyVersion() throws IOException
     {
         String resource = "META-INF/maven/org.eclipse.jetty/jetty-websocket/pom.properties";
         URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
         if (url == null)
         {
             return "GitMaster";
            // TODO: use git to get actual branch name?
         }
 
         InputStream in = null;
         try
         {
             in = url.openStream();
             Properties props = new Properties();
             props.load(in);
             return props.getProperty("version");
         }
         finally
         {
             IO.close(in);
         }
     }
 
     public static void main(String[] args) throws Exception
     {
         if (args.length < 2)
         {
             System.err.println("ERROR: Hostname and port not specified");
             System.err.printf("USAGE: java -cp jetty-autobahn-websocket-client.jar %s [hostname] [port]%n",TestClient.class.getName());
             System.exit(-1);
         }
 
        System.setProperty("org.eclipse.jetty.websocket.STRICT","true");
        System.setProperty("org.eclipse.jetty.util.log.stderr.LONG","true");
         System.setProperty("org.eclipse.jetty.io.nio.LEVEL","INFO");
         System.setProperty("org.eclipse.jetty.util.component.LEVEL","INFO");
        System.setProperty("org.eclipse.jetty.LEVEL","DEBUG");
 
         String hostname = args[0];
         int port = Integer.parseInt(args[1]);
 
         int caseNumbers[] = null;
 
         if (args.length > 2)
         {
             int offset = 2;
             caseNumbers = new int[args.length - offset];
             for (int i = offset; i < args.length; i++)
             {
                 caseNumbers[i - offset] = Integer.parseInt(args[i]);
             }
         }
 
         TestClient client = null;
         try
         {
             String userAgent = "JettyWebsocketClient/" + getJettyVersion();
             client = new TestClient(hostname,port,userAgent);
 
             client.updateStatus("Running test suite...");
             client.updateStatus("Using Fuzzing Server: %s:%d",hostname,port);
             client.updateStatus("User Agent: %s",userAgent);
 
             if (caseNumbers == null)
             {
                 int caseCount = client.getCaseCount();
                 client.updateStatus("Will run all %d cases ...",caseCount);
                 for (int caseNum = 0; caseNum < caseCount; caseNum++)
                 {
                     client.updateStatus("Running case %d (of %d) ...",caseNum,caseCount);
                     client.runCaseByNumber(caseNum);
                 }
             }
             else
             {
                 client.updateStatus("Will run %d cases ...",caseNumbers.length);
                 for (int caseNum : caseNumbers)
                 {
                     client.runCaseByNumber(caseNum);
                 }
             }
             client.updateStatus("All test cases executed.");
             client.updateReports();
         }
         catch (Throwable t)
         {
             t.printStackTrace(System.err);
         }
         finally
         {
             if (client != null)
             {
                 client.shutdown();
             }
         }
         System.exit(0);
     }
 
     private Logger log;
     private URI baseWebsocketUri;
     private WebSocketClientFactory clientFactory;
     @SuppressWarnings("unused")
     private String hostname;
     @SuppressWarnings("unused")
     private int port;
     private String userAgent;
 
     public TestClient(String hostname, int port, String userAgent) throws Exception
     {
         this.log = Log.getLogger(this.getClass());
         this.hostname = hostname;
         this.port = port;
         this.userAgent = userAgent;
         this.baseWebsocketUri = new URI("ws://" + hostname + ":" + port);
         this.clientFactory = new WebSocketClientFactory();
         this.clientFactory.start();
     }
 
     public int getCaseCount() throws IOException, InterruptedException
     {
         URI wsUri = baseWebsocketUri.resolve("/getCaseCount");
         WebSocketClient wsc = clientFactory.newWebSocketClient();
         OnGetCaseCount onCaseCount = new OnGetCaseCount();
         wsc.open(wsUri,onCaseCount);
         onCaseCount.awaitMessage();
         if (onCaseCount.hasCaseCount())
         {
             return onCaseCount.getCaseCount();
         }
 
         throw new IllegalStateException("Unable to get Case Count");
     }
 
     public void runCaseByNumber(int caseNumber) throws IOException, InterruptedException
     {
         URI wsUri = baseWebsocketUri.resolve("/runCase?case=" + caseNumber + "&agent=" + UrlEncoded.encodeString(userAgent));
         log.debug("next uri - {}",wsUri);
         WebSocketClient wsc = clientFactory.newWebSocketClient();
         OnEchoMessage onEchoMessage = new OnEchoMessage(caseNumber);
         Future<WebSocket.Connection> conn = wsc.open(wsUri,onEchoMessage);
 
         try
         {
             conn.get(1,TimeUnit.SECONDS);
         }
         catch (ExecutionException e)
         {
             log.warn("Unable to connect to: " + wsUri,e);
             return;
         }
         catch (TimeoutException e)
         {
             log.warn("Unable to connect to: " + wsUri,e);
             return;
         }
 
         onEchoMessage.awaitClose();
     }
 
     public void shutdown()
     {
         try
         {
             this.clientFactory.stop();
         }
         catch (Exception e)
         {
             log.warn("Unable to stop WebSocketClientFactory",e);
         }
     }
 
     public void updateReports() throws IOException, InterruptedException
     {
         URI wsUri = baseWebsocketUri.resolve("/updateReports?agent=" + UrlEncoded.encodeString(userAgent));
         WebSocketClient wsc = clientFactory.newWebSocketClient();
         OnUpdateReports onUpdateReports = new OnUpdateReports();
         wsc.open(wsUri,onUpdateReports);
         onUpdateReports.awaitClose();
     }
 
     public void updateStatus(String format, Object... args)
     {
         log.info(String.format(format,args));
     }
 }
