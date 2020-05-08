 package polly.porat.network;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.concurrent.locks.ReentrantLock;
 
 import polly.network.events.ConnectionListener;
 import polly.network.events.NetworkEvent;
 import polly.network.events.ObjectReceivedEvent;
 import polly.network.events.ObjectReceivedListener;
 import polly.network.protocol.Constants;
 import polly.network.protocol.Request;
 import polly.network.protocol.Constants.RequestType;
 
 
 public class ClientProtocolHandler implements ConnectionListener, ObjectReceivedListener {
     
     
     
     public static void main(String[] args) {
         System.setProperty("javax.net.ssl.trustStore", "cfg/SSLKeyStore");
         System.setProperty("javax.net.ssl.trustStorePassword", "blabla");
         
         ClientProtocolHandler cph = new ClientProtocolHandler();
         
         try {
            cph.connect(InetAddress.getByName("192.168.0.20"), 24500, "C0mb4t", 
                 "nichtpenner");
             System.out.println("connected");
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     
 
     private ClientConnection connection;
     private ReentrantLock connectionLock;
     
     
     
     public ClientProtocolHandler() {
         this.connectionLock = new ReentrantLock();
     }
     
     
     
     public void connect(InetAddress host, int port, String userName, String password) 
             throws IOException {
         
         try {
             this.connectionLock.lock();
             
             if (this.connection != null) {
                 throw new IllegalStateException("connection already active");
             }
             
             this.connection = new ClientConnection(host, port);
             this.connection.addConnectionListener(this);
             this.connection.addObjectReceivedListener(this);
             
             Request login = new Request(RequestType.LOGIN);
             login.getPayload().put(Constants.USER_NAME, userName);
             login.getPayload().put(Constants.PASSWORD, password);
             this.connection.send(login);
             
         } finally {
             this.connectionLock.unlock();
         }
     }
     
     
     
     public void disconnect() {
         try {
             this.connectionLock.lock();
             
             if (this.connection == null) {
                 throw new IllegalStateException("connection not active");
             }
             
             Request request = new Request(RequestType.LOGOUT);
             this.connection.send(request);
             
             this.connection.close();
             this.connection = null;
         } finally {
             this.connectionLock.unlock();
         }
     }
 
 
 
     @Override
     public void connectionAccepted(NetworkEvent e) {}
 
 
 
     @Override
     public void connectionClosed(NetworkEvent e) {
         this.disconnect();
     }
 
 
 
     @Override
     public void objectReceived(ObjectReceivedEvent e) {
     }
 }
