 package eu.scapeproject;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 
 import org.simpleframework.transport.connect.SocketConnection;
 
 /**
  * Mock class for API testing
  * @author Frank Asseg
  *
  */
 public class ConnectorAPIMock implements Runnable{
 
     private final int port = 8783;
     private final String path;
     private SocketConnection conn;
     private volatile boolean running=false;
     private MockContainer container;
     
     public ConnectorAPIMock(){
     	this.path = System.getProperty("java.io.tmpdir") + "/scape-tck-" + System.getProperty("user.name");
     }
     
     public int getPort(){
         return this.port;
     }
     
     public void run(){
        this.running=true;
         try {
             this.startServer();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
     
     public void stop() throws IOException{
         if (this.conn == null){
             throw new IOException("Connection is null");
         }
         this.conn.close();
         this.running=false;
     }
     
     private void startServer() throws IOException{
     	this.container=new MockContainer(this.path);
         this.conn=new SocketConnection(this.container);
         this.conn.connect(new InetSocketAddress(this.port));
     }
     
     public synchronized boolean isRunning() {
         return this.running;
     }
     
     public void purgeStorage() throws Exception{
     	this.container.purgeStorage();
     }
 }
