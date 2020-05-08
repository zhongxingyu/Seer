 package backend;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ArrayBlockingQueue;
 
 /**
  * Server for the realtime collaborative editor.
  * 
  * Thread-safe argument
  * --------------------
  * Edits are are sent over sockets to the server and are added to the
  * EditQueue so that they are dealt with one by one. The Edits are not
  * inserted using indexes; instead, they are inserted using the Cursor's
  * location for that client. This means that an edit will always be 
  * inserted correctly into the ServerDocument even if its index changes 
  * in between when it is sent to the server and when it is actually added
  * to the ServerDocument. Additionally, Edits belong to the original 
  * client until that client moves the Cursor or presses space or newline.
  * This helps narrow down the case where clients will try to edit right on
  * top of each other. They still type right next to each other,
  */
 public class Server {
     private ServerSocket serverSocket = null;
     private final EditController editCont;
     private static Map<String, ServerDocument> docList = new HashMap<String, ServerDocument>();
     private final int CAPACITY = 10;
     private static Map<Socket, String> socketMap = new HashMap<Socket, String>();
     //private final String SPLIT_CHAR = Character.toString((char) 0x2605);
     private final String SPLIT_CHAR = "|";
 
     /**
      * Makes a Server that listens for connections on port.
      * @param port The port number, requires 0 <= port <= 65535
      * @throws IOException 
      */
     public Server (int port) throws IOException {
         serverSocket = new ServerSocket(port);
         editCont = new EditController(new ArrayBlockingQueue<String>(CAPACITY), docList);
     }
 
     /**
      * Runs the server, listening for client connections and handling them.
      */
     public void serve() throws IOException {
         // A thread waiting for new connections
         Thread receiverThread = new Thread(new Runnable() {
             public void run() {
                 while (true) {
                     System.out.println("you're before socket");
                     // block until a client connects
                     System.out.println(serverSocket.toString());
                     Socket socket;
                     try {
                         socket = serverSocket.accept();
                         System.out.println("youve accepted the socket");
                         // makes threads
                         Runnable handler = new Handler(socket);
                         Thread clientThread = new Thread(handler);
                         clientThread.start();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }
         });
         receiverThread.start();
     }
     
     private class Handler implements Runnable {
         private final Socket socket;
         private Handler (Socket s) {
             socket = s;
         }
 
         @Override
         public void run() {
             try {
                 handleConnection();
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (InterruptedException e) {
                 e.printStackTrace();
             } finally {
                 try {
                     System.out.println("preclose");
                     socket.close();
                     //check the following line
                     System.out.println("postclose");
                     // not sure if needed:
                     //serverSocket.close();
                     } catch (IOException e) {
                       e.printStackTrace();
                 } finally {}
             }
         }
 
         /**
          * Handles a single client connection. Returns when client disconnects.
          * @throws IOException if connection has an error or terminates unexpectedly
          * @throws InterruptedException 
          */
         private void handleConnection() throws IOException, InterruptedException {
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             try {
                 synchronized (this) {
                 for (String line = in.readLine(); line!=null; line=in.readLine()) {
                     System.out.println("beginning of handleConnection for loop");
                     if (!socketMap.containsKey(socket)) {
                         String[] name = line.split(" ");
                         socketMap.put(socket, name[0]);
                     }
                     System.out.println("INPUT FROM GUI: " + line);
                     editCont.putOnQueue(line);
                     
                     while (!editCont.getQueue().isEmpty()) {
                         //synchronized (this) {
                             String output = editCont.takeFromQueue();
                             System.out.println("output from server: "+output);
                             if(output != null) {
                                 String[] outTokens = output.split("\\|");
                                 System.out.println("clientName: "+outTokens[0]);
                                 System.out.println("docName: "+outTokens[1]);
                                 /**
                                  * Floods update messages to the sockets with messages according to the following:
                                  * If something was successful: send the original client the success message and send
                                  * the rest of the clients an update message.
                                  * If something was unsuccessful: update messages to all of the clients including the 
                                  * original one.
                                  * If something is a message that only the original client cares about, send the message to
                                  * that client only.
                                  */                            
                                 if (outTokens[0].equals("InvalidInput")) {
                                     // do nothing, skip this loop for indexing's sake
                                 } 
                                 else {
                                     System.out.println("FLOODING:");
                                     String lineAndContent;
                                     if (outTokens.length > 1 && docList.get(outTokens[1]) != null && docList.get(outTokens[1]).getDocContent() != null) {
                                         lineAndContent =  docList.get(outTokens[1]).getDocContent();
                                     } else {
                                         lineAndContent = "";
                                     }
                                     String update = outTokens[0] + SPLIT_CHAR+outTokens[1]+SPLIT_CHAR+"update"+SPLIT_CHAR+ lineAndContent;
                                     for (Socket soc : socketMap.keySet()) {
                                         if (!soc.equals(socket)) {
                                             PrintWriter tempOut = new PrintWriter(soc.getOutputStream(), true);
                                             tempOut.println(output);
                                             tempOut.flush();
                                             System.out.println("sent: "+output+" ...to "+socketMap.get(soc));
                                            if (outTokens.length > 1 && !outTokens[1].equals("???")) {
                                                tempOut.println(update);
                                                tempOut.flush();
                                                System.out.println("update: "+update+" ...to "+socketMap.get(soc));
                                            }
                                         } else {
                                             out.println(output);
                                             out.flush();
                                             System.out.println("sent: "+output+" ...to "+socketMap.get(socket));
                                            if (outTokens.length > 1 && !outTokens[1].equals("???")) {
                                                out.println(update);
                                                out.flush();
                                                System.out.println("update: "+update+" ...to "+socketMap.get(socket));
                                            }                                            
                                         }
                                     }
                                 }
                            // }
                             // TODO: make it not crash when a GUI exits
                         }
                     }
                     
                     
                 }
                 }
             } finally { 
                 out.close();
                 in.close();
                 System.out.println("at end of handleconnection for "+socketMap.get(socket));
                 socketMap.remove(socket);
             }
         }
     }
         
     
     /**
      * Start a Server running on the default port (4444).
      * 
      * The server runs. It waits for clients to connect to the correct port using
      * sockets. It calls handleConnection to deal with the socket of each client, and 
      * handleConnection gives the input to the editController to put on the 
      * EditQueue. The EditQueue deals with messages in order so that everything is 
      * threadsafe.
      */
     public static void main(String[] args) {
         // Always uses the same port. Clients connect their GUIs
         // to this port and the host's IP address.
         final int port = 4444;
         try {
             Server server = new Server(port);
             server.serve();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
