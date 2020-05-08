 package chatter.client;
 
 import chatter.common.Constants;
 import chatter.common.*;
 
 import java.io.*;
 import java.net.*;
 import java.util.concurrent.TimeoutException;
 
 /**
  * @author Bharath Ravi
  * @author Kapil Gole
  * @author Alban Dumouilla
  *
  * The basic chatter.client algorithm is as follows:
  * 1. Create a Socket to the server
  * 2. Use the socket to create an EncryptedSocket.
  * 3. All further messaging should be done using only the encrypted socket.
  * 4. First authenticate using uname + passwd.
  * 5. Start listening for either:
  *     a. Chats from server
  *     b. Input from chatter.client
  * 6. If user says "\quit" then send a QUIT message to server and close.
  */
 public class ChatterClient extends Thread {
   private static Thread serverThread;
   private static EncryptedSocket connection;
 
   public ChatterClient() {}
 
   public void run() {
    try {
       InetAddress address = InetAddress.getByName(Constants.HOST);
       connection = new EncryptedSocket(new Socket(address, Constants.PORT));
     } catch (ConnectException e) {
       System.out.println(ErrorConstants.CONNECTION_REFUSED);
       return;
     } catch (UnknownHostException e) {
       System.out.println(ErrorConstants.UNKNOWN_HOST);
       e.printStackTrace();
       return;
     } catch (DiffieHellmanException e) {
       System.out.println(ErrorConstants.ERROR_ENCRYPTION_SETUP);
       e.printStackTrace();
       return;
     } catch (IOException e) {
       if (isInterrupted()) {
         System.out.println("Closing connections...");
       } else {
         System.out.println(ErrorConstants.ERROR_SERVER_DISCONNECT);
       }
       return;
     } catch (CryptoException e) {
       System.out.println(ErrorConstants.ERROR_ENCRYPTION_SETUP);
       e.printStackTrace();
       return;
     }
 
     try {
       if (authenticateClient()) {
         startChatting();
       }
     } catch (InvalidMessageException e) {
       System.out.println(ErrorConstants.INVALID_MESSAGE);
       e.printStackTrace();
     } catch (TimeoutException e) {
       System.out.println(ErrorConstants.ERROR_SERVER_TIMEOUT);
     } catch (IOException e) {
       if(!isInterrupted()) {
         System.out.println(ErrorConstants.ERROR_SERVER_DISCONNECT);
         //e.printStackTrace();
       }
     } catch (CryptoException e) {
       System.out.println(ErrorConstants.ERROR_ENCRYPTION);
       e.printStackTrace();
     }
   }
 
   private void startChatting() throws IOException, CryptoException {
     System.out.println("Logged in!");
     connection.setTimeout(Constants.CHAT_TIMEOUT);
 
     createServerListener();
     serverThread.start();
     InputStreamReader inputStreamReader = new InputStreamReader(System.in);
     BufferedReader readChat = new BufferedReader(inputStreamReader);
 
     while (true) {
       String chatText = readChat.readLine();
       if (chatText.equals(Constants.QUIT_MESSAGE)){
         shutdown();
         return;
       } else {
         connection.sendLine(Message.createChatMessage(chatText));
       }
     }
   }
 
   private void sendQuit() {
     // Quitting is a best effort operation. If there are errors
     // while sending a Quit message, they are simply ignored.
     // This is because the other end is fail proof with timeouts anyway,
     // and the QUIT is just a convenience.
     try {
       connection.sendLine(Message.createQuitMessage());
     } catch (IOException e) {
       // Ignore error
     } catch (CryptoException e) {
       // Ignore error
     }
   }
 
   private boolean authenticateClient() throws IOException, CryptoException,
       InvalidMessageException, TimeoutException {
     connection.setTimeout(Constants.AUTHENTICATION_TIMEOUT);
     InputStreamReader inputStreamReader = new InputStreamReader(System.in);
     BufferedReader readChat = new BufferedReader(inputStreamReader);
     System.out.print("UserName : ");
     String username = readChat.readLine();
     System.out.print("Password : ");
     String password = readChat.readLine();
 
     connection.sendLine(Message.createAuthMessage(username, password));
     String response = connection.readLine();
     Message msg = new Message(response);
 
     switch (msg.type) {
       case QUIT:
         System.out.println(ErrorConstants.ERROR_AUTH);
         return false;
       case OKAY:
         return true;
       case CHAT:
         System.out.println(msg.messageContent);
         return false;
       default:
         System.out.println(ErrorConstants.INVALID_MESSAGE);
         System.out.println(msg.type);
         return false;
     }
   }
 
   /**
    * Creates a thread that endlessly loops waiting for chat messages from
    * the server and printing them when it receives any.
    */
   private void createServerListener() {
     serverThread = new ServerListenerThread(connection);
   }
 
   public void interrupt() {
     shutdown();
   }
 
   private void shutdown() {
     super.interrupt();
 
     if(serverThread!=null) {
       serverThread.interrupt();
     }
 
     try {
       if (connection !=null && !connection.isClosed()) {
         sendQuit();
         connection.close();
       }
     } catch (IOException e) {
       // Ignore error while quitting
     }
   }
 }
