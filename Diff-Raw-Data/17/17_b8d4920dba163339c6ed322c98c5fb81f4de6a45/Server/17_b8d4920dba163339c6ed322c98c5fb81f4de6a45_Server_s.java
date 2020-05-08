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
 //    private int numUsers;
     private final EditController editCont;
     private static Map<String, ServerDocument> docList = new HashMap<String, ServerDocument>();
     private final int CAPACITY = 10;
     private static Map<Socket, String> socketMap = new HashMap<Socket, String>();
 
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
         //adding in another thread
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
                 // TODO Auto-generated catch block
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
                 System.out.println("closed? 3: "+socket.isClosed());
                 
                 //error happens somewhere in or before the following line
                 for (String line = in.readLine(); line!=null; line=in.readLine()) {
                     System.out.println("beginning of handleConnection for loop");
                     if (!socketMap.containsKey(socket)) {
                         String[] name = line.split(" ");
                         socketMap.put(socket, name[0]);
                     }
                     System.out.println("input from GUI: " + line);
                     //String output = editCont.putOnQueue(line);
                     editCont.putOnQueue(line);
                     //System.out.println("output from server: " + output);
                     
                     while (!editCont.getQueue().isEmpty()) {
                         String output = editCont.takeFromQueue();
                         System.out.println("output from server: "+output);
                         if(output != null) {
                             String[] outTokens = output.split(" ");
 
                             /**
                              * Floods update messages to the sockets with messages according to the following:
                              * If something was successful: send the original client the success message and send
                              * the rest of the clients an update message.
                              * If something was unsuccessful: update messages to all of the clients including the 
                              * original one.
                              * If something is a message that only the original client cares about, send the message to
                              * that client only.
                              */
 
                             //out.println(output);
                             //out.flush();
                             
                             if (outTokens[0].equals("InvalidInput")) {
                                 // do nothing, skip this loop for indexing's sake
                             } 
                             else {
                                 System.out.println("FLOODING:");
                                 for (Socket soc : socketMap.keySet()) {
                                     if (!soc.equals(socket)) {
                                           PrintWriter tempOut = new PrintWriter(soc.getOutputStream(), true);
                                           tempOut.println(output);
                                           tempOut.flush();
                                           System.out.println("sent: "+output+" ...to "+socketMap.get(soc));
                                   } else {
                                           out.println(output);
                                           out.flush();
                                           System.out.println("sent: "+output+" ...to "+socketMap.get(socket));
                                   }
                                 }
                             }
 //                            else if (outTokens[2].equals("new") || outTokens[2].equals("getDocNames") || 
 //                                    outTokens[2].equals("checkNames") || outTokens[2].equals("save") || 
 //                                    outTokens[2].equals("open") || outTokens[2].equals("cursorMoved")) {
 //                                // These are outgoing messages that only the original client cares about
 //                                
 //                                
 //                                for (Socket soc : socketMap.keySet()) {
 //                                    if (!soc.equals(socket)) {
 //                                        if (socketMap.get(soc).equals(outTokens[0])) {
 //                                            PrintWriter tempOut = new PrintWriter(soc.getOutputStream(), true);
 //                                            tempOut.println(output);
 //                                            tempOut.flush();
 //                                        }
 //                                    } else {
 //                                        if (socketMap.get(socket).equals(outTokens[1])) {
 //                                            out.println(output);
 //                                            out.flush();
 //                                        }
 //                                    }
 //                                }
 //                            } else {
 //                                String linesAndContent = docList.get(outTokens[1]).getDocContent();
 //                                String update = outTokens[0] + " " + outTokens[1] + " update " + linesAndContent;
 //                                
 //                                for (Socket soc : socketMap.keySet()) {
 //                                    if (!soc.equals(socket)) {
 //                                            PrintWriter tempOut = new PrintWriter(soc.getOutputStream(), true);
 //                                            tempOut.println(update);
 //                                            tempOut.flush();
 //                                    } else {
 //                                        if (socketMap.get(socket).equals(outTokens[1])) {
 //                                            out.println(update);
 //                                            out.flush();
 //                                        }
 //                                    }
 //                                }
 //                            }
                             
                             // TODO: make it not crash when a GUI exits
                         }
                     }
                     
                     
                 } 
             } finally { 
                 out.close();
                 in.close();
                 System.out.println("at end of handleconnection for "+socketMap.get(socket));
                 // check this line if multithreading is wrong
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
