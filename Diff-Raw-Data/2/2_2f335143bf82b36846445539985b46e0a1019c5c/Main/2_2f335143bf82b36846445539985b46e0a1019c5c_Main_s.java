 
 package ca.arentz;
 
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
 import org.eclipse.jetty.spdy.http.HTTPSPDYServerConnector;
 import org.eclipse.jetty.util.ssl.SslContextFactory;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 public class Main
 {
     public static void main(String[] args) throws Exception
     {
         Server server = new Server();
 
         SelectChannelConnector connector0 = new SelectChannelConnector();
         connector0.setPort(8080);
 
         SslContextFactory contextFactory = new SslContextFactory();
         contextFactory.setKeyStorePath("jetty.keystore");
         contextFactory.setKeyStorePassword("q1w2e3");
 
        SslSelectChannelConnectors connector1 = new SslSelectChannelConnector(contextFactory);
         connector1.setPort(8443);
 
         HTTPSPDYServerConnector connector2 = new HTTPSPDYServerConnector(contextFactory);
         connector2.setPort(9443);
 
         server.setConnectors(new Connector[]{connector0, connector1, connector2});
 
         WebAppContext webAppContext = new WebAppContext();
         webAppContext.setContextPath("/");
         webAppContext.setWar("webapps/test.war");
         server.setHandler(webAppContext);
 
         server.start();
         server.join();
     }
 }
 
