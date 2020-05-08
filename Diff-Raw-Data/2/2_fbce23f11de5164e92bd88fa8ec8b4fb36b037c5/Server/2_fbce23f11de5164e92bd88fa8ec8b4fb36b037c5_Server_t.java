 package jnet;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Map;
 
 
 public class Server implements Runnable {
     private final ServerSocket serverSocket;
     private final ServerListener listener;    
     private final Map<String, ServerThread> clients;
     private final Thread thread;
     
     /**
      * Creates a new Server and begins listening on the specified port.
      * 
      * @param portNumber the port number to listen on
      * @param listener an object that will be notified when a message is received
      * @throws IOException if the server fails to open a socket
      */
     public Server(int portNumber, ServerListener listener) throws IOException {
         serverSocket = new ServerSocket(portNumber);
         this.listener = listener;
         clients = new HashMap<String, ServerThread>();
         thread = new Thread(this);
         thread.start();
     }
     
     /**
      * Disconnects all clients and closes down the server.
      * 
      * @throws IOException if an error occurs when closing the server
      */
     public synchronized void close() throws IOException {
         for (String clientName : clients.keySet())
             disconnect(clientName);
         
         serverSocket.close();
     }
     
     /**
      * Sends a message to the specified client.
      * 
      * @param clientName the name of the client to send to (the same name passed into {@link ServerListener#messageReceived(Server, String, String)})
      * @param message the message to send
      * @return true if the clientName was valid and the message sent, false otherwise
      */
     public boolean send(String clientName, String message) {
         ServerThread client = clients.get(clientName);
         if (client == null)
             return false;
         
         client.send(message);
         return true;
     }
 
     /**
      * Disconnects the specified client.
      * 
      * @param clientName the name of the client to disconnect (the same name passed into {@link ServerListener#messageReceived(Server, String, String)})
      * @returns true if the clientName was valid and the client disconnected, false otherwise
      * @throws IOException if an error occurs when disconnecting the client
      */
     public synchronized boolean disconnect(String clientName) throws IOException {
          ServerThread client = clients.remove(clientName);
          if (client == null)
              return false;
          
          client.close();
          return true;
     }
     
    void clientDisconnnected(String clientName) {
         try {
             disconnect(clientName);
             listener.clientDisconnected(this, clientName);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     void handle(String clientName, String message) {
         listener.messageReceived(this, clientName, message);
     }
     
     @Override
     public void run() {
         while (true) {
             Socket clientSocket;
             try {
                 clientSocket = serverSocket.accept();
             } catch (IOException e) {
                 break;
             }
             
             ServerThread newClient;
             try {
                 newClient = new ServerThread(this, clientSocket);
                 clients.put(newClient.getClientName(), newClient);
                 newClient.start();
                 listener.clientConnected(this, newClient.getClientName());
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
     
     public static void main(String[] args) {
         try {
             new Server(8000, new ServerListener() {                
                 @Override
                 public synchronized void messageReceived(Server s, String clientName, String message) {
                     System.out.println("Received: " + message + " from " + clientName);
                     for (int i=1; i<=5; i++) {
                         System.out.println("Waiting " + i + "...");
                         try {
                             Thread.sleep(1000);
                         } catch (InterruptedException e) {
                             e.printStackTrace();
                         }
                     }
                     s.send(clientName, message + message);
                 }
 
                 @Override
                 public synchronized void clientConnected(Server server, String clientName) {
                 }
 
                 @Override
                 public synchronized void clientDisconnected(Server server, String clientName) {
                 }
             });
             while (true) {
                 
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
