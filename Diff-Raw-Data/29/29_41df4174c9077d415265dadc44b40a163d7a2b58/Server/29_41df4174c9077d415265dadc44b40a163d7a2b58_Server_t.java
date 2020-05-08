 package backend;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
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
  *
  */
 public class Server {
     private ServerSocket serverSocket = null;
     private int numUsers;
     private final EditController editCont;
     private static Map<String, ServerDocument> docList  = new HashMap<String, ServerDocument>();
 
     /**
      * Makes Server that listens for connections on port.
      * @param port port number, requires 0 <= port <= 65535
      * @throws IOException 
      */
     public Server (int port) throws IOException {
         serverSocket = new ServerSocket(port);
         numUsers = 0;
         editCont = new EditController(new EditQueue(), docList);
     }
 
     /**
      * Run the server, listening for client connections and handling them.
      * Never returns 
      * @author User
      *
      */
     private Socket socket;
     public void serve() throws IOException {
         while (true) {
             System.out.println("youre before socket");
             // block until a client connects
             System.out.println(serverSocket.toString());
             socket = serverSocket.accept();
             System.out.println("youve accepted the socket");
             // makes threads
             Thread clientThread = new Thread(new Runnable() {
                 public void run() {
                         try {
                             handleConnection(socket);
                         } catch (IOException e) {
                             e.printStackTrace();
                         } finally {
                             try {
                                 socket.close();
                                 //serverSocket.close();
                             } catch (IOException e) {
                                 e.printStackTrace();
                             }
                         }
                     
                 }
                 /**
                  * Handles a single client connection. Returns when client disconnects.
                  * @param socket socket where the client is connected
                  * @throws IOException if connection has an error or terminates unexpectedly
                  */
                 private void handleConnection(Socket socket) throws IOException {                    
                     numUsers++;                   
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     //out.println("Welcome! There are currently "+numUsers+" other clients connected.");
                     //out.flush();
                     try {
                         System.out.println("1");
                         for (String line =in.readLine(); line!=null; line=in.readLine()) {
                             System.out.println(line);
                             System.out.println("2");
                             String output = handleRequest(line);
                             System.out.println("3");
                             System.out.println(output);
                             if(output != null) {
                                 out.print(output);
                                 out.flush();
                                 if (output.equals("Exit")) {
                                     numUsers--;
                                     return;
                                 } 
                             } 
                         }
                     } finally {   
                         System.out.println("4");
                         out.close();
                         in.close();
                         System.out.println("closed");
                     }
                 }
 
                 /**
                  * Handler for client input
                  * 
                  * make requested mutations on game state if applicable, then return
                  * appropriate message to user
                  * @param input
                  *
                  */
                 private String handleRequest(String input) {
                     String[] tokens = input.split(" ");
                     System.out.println("5");
                     if (tokens.length > 1 && tokens[1].equals("NewDoc")) { 
                        synchronized (this) {
                            System.out.println("new doc");
                            // If creating a new document
                            String title = tokens[2];
                            if (docList.containsKey(title)) {
                                System.out.println("6");
                                return "new invalid";
                            } else {
                                docList.put(title, new ServerDocument(title));
                                
                                System.out.println("7");
                                return "new success";
                            }
                         }
                        
                     } else if (tokens.length > 0 && tokens[0].equals("getDocNames")) {
                         System.out.println("getdocnames");
                         // If asking for list of document names
                         String names = "";
                         for (String key: docList.keySet()) {
                             names += key;
                             names += " ";
                         }
                         return names.substring(0, names.length() - 1);
                     } else if (tokens.length > 0 && tokens[0].equals("update")) {
                         System.out.println("update");
                         ServerDocument doc = docList.get(tokens[1]);
                         if (doc == null) {
                             return "update fail";
                         } else {
                             String contents = doc.getDocContent();
                             return "update " + tokens[1] + " " + contents;
                         }
                     } else if (tokens.length > 0 && tokens[0].equals("open")) {
                        synchronized (this) {
                         System.out.println("open");
                         ServerDocument doc = docList.get(tokens[2]);
                         if (doc == null) {
                             return "open fail";
                         } else {
                             String contents = doc.getDocContent();
                             return "open " + tokens[1] + " " + contents;
                         }
                        }
                     } else {
                         System.out.println("edit msg");
                         // Gives all the edit messages to the edit controller to deal with, including:
                         // save, insert, remove, space entered, cursor moved
                         if (editCont.putOnQueue(input)) {
                             return "success";
                         } else { 
                             ServerDocument doc = docList.get(tokens[1]);
                             if (doc == null) {
                                 return tokens[2] + " " + "fail";
                             } else {
                                 String contents = doc.getDocContent();
                                 return tokens[2] + " fail " + contents;
                             }
                         }
                     }
                 }
 
             });
             clientThread.start();
         }
     }
 
     
     /**
      * Start a Server running on the default port (4444).
      * 
      // TODO: state and explain creation of server here
      * 
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
     
     /**
      * Returns the titles of all the documents
      * @return The titles of the documents
      */
     public static ArrayList<String> getDocs() { 
         ArrayList<String> titleList = new ArrayList<String>();
         Set<String> keys = docList.keySet();
         if (!keys.isEmpty()){
             titleList = new ArrayList<String>(keys);
         }
         return titleList; 
     }
     
     public static boolean docListEmptyCheck(){
         if (docList == null){
             return true;
         }
         return false;
     }
     
     public static void addDocument(String title){
         docList.put(title, new ServerDocument(title));
         System.out.println("doclist after adding" + docList.toString());
     }
     /**
      * Returns the document with the given title or null if there is nothing with that title.
      * @param title The title
      */
     public static ServerDocument getDocument(String title) {
         return docList.get(title);
     }
 }
