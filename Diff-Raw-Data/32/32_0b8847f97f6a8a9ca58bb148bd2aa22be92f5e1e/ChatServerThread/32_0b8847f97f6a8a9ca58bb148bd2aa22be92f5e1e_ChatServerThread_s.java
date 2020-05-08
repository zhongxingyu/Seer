 package Server;
 import java.net.*;
 import java.util.Scanner;
 import java.io.*;
 import Common.Encryptor;
 
 public class ChatServerThread extends Thread {
     /* The client socket and IO we are going to handle in this thread */
     protected Socket         socket;
     protected PrintWriter    out;
     protected BufferedReader in;
 
     //the user this thread corresponds to
     private UserAccount user;
     
     //chat room this belongs to
     private ChatServerChatRoom chatRoom;
     //server this belongs to
     private ChatServerMain chatServer;
 
    public int getID(){ return user.getID(); }
    public void setID(int a){ user.setID(a); }
 
     public String getUserName(){ return user.getName(); }
     public void setUserName(String n){ user.setName(n); }
 
     public ChatServerChatRoom getChatRoom(){ return chatRoom; }
 
     public ChatServerThread(Socket socket, UserAccount account, ChatServerChatRoom  chatRoom, ChatServerMain chatServer){
         user = account;
         this.chatRoom = chatRoom;
         this.chatServer = chatServer;
 
         /* Create the I/O variables */
         try {
             this.out = new PrintWriter(socket.getOutputStream(), true);
             this.in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 
             /* Debug */
             System.out.println("Client handler thread created.");
         } catch (IOException e) {
             System.out.println("IOException: " + e);
         }
     }
     
     private void send(String a){
         a = Encryptor.encrypt(a, 5); //encrypt it 5 times
         this.out.println(a);
     }
     
     private String receive(){
         try{
       return Encryptor.decrypt(this.in.readLine(), 5); //decrypt it 5 times
     } catch (Exception e) {}
     return null;
     }
 
     @Override
     public void run() {
         //ask for login credentials
         send("Enter username (will be created if doesn't exist).");
         String name = receive();
         send("Enter your password (or desired password for new account).");
         String password = receive();
         user = chatServer.handleLogin(name, password);
         if (user==null){ send("Invalid password or unavailable new username, disconnecting!"); forceDisconnect(); }
         //else{ System.out.println("Unknown error run ChatServerThread run from handling login!!!"); this.out.println("Technical difficulties, disconnecting."); forceDisconnect(); }
         
         Scanner scanner; //for analyzing text
         while (true) {
             try {
                 /* Get string from client */
                 String fromClient = receive();
 
                 /* If null, connection is closed, so just finish */
                 if (fromClient == null) {
                     disconnect(null);
                 }
 
                 /* Handle the text. */
                 if (fromClient.length()>0){ 
                     if (fromClient.charAt(0)=='/'){ //if it's a command
                         scanner = new Scanner(fromClient);
                         String firstWord = scanner.next();
                         if (firstWord.equalsIgnoreCase("/whisper")){ //to tell a user a private message
                             if (!scanner.hasNext()) send("You must specify the target's name.");
                             String target = scanner.next();
                             if (!scanner.hasNext()) send("You must specify a message.");
                             String message = scanner.next();
                             if (!chatServer.tellUser(user.getName(), target, message)) send("User not found online.");
                         }
                         else if (firstWord.equalsIgnoreCase("/nick")){ //to change a user's name
                             if (!scanner.hasNext()) send("You must specify a name.");
                             else if (!chatServer.changeUserName(user.getName(), scanner.next())) send("Name already taken or invalid.");
                         }
                         else if (firstWord.equalsIgnoreCase("/disconnect")){ //to disconnect gracefully
                             disconnect(scanner.next());
                         }
                   /*      else if (firstWord.equalsIgnoreCase("/changeroom")){
                             if (!scanner.hasNext()) send("You must specify a room name.");
                             else if (!chatServer.changeRoom(scanner.next(), user.getName())) send("Invalid room name specified. ");   TODO
                         } */
                         else send("Invalid command.");
                         scanner.close();
                     }
 
                     else chatRoom.tellEveryone(name, fromClient);
                 }
 
             } catch (Exception e) {
                 /* On exception, stop the thread */
                 return;
             }
         }
     }
     
     public void disconnect(String message){ //called when disconnecting from server
         System.out.println("Client "+user.getName()+ " disconnected");
         if (message!=null && chatRoom!=null) chatRoom.tellEveryone(user.getName(), message);
         if (chatRoom!=null) chatRoom.tellEveryone("Server Message", ""+user.getName()+" disconnected."); //server message
         try{
         this.in.close();
         this.out.close();
         this.socket.close();
         } catch (Exception e) { /* do nothing */ }
     }
     
     public void forceDisconnect(){ //just disconnect, sending no messages
         System.out.println("Client "+user.getName()+ " disconnected forcibly");
         try{
         this.in.close();
         this.out.close();
         this.socket.close();
         } catch (Exception e) { /* do nothing */ }
     }
 
     public void tell(String user, String message){
         if (message==null || message.length()<=0) return;
         send(user+": "+message);
     }
 }
