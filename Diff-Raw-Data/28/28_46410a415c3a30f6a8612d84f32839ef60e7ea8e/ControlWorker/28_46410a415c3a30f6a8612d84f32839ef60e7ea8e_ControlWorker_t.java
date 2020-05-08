 package cpsc471.ftp.server.control;
 
 import cpsc471.ftp.data.DataChannelClient;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Logger;
 
 import java.io.*;
 import java.net.Socket;
 
 /**
  * Handles client requests
  */
 public class ControlWorker implements Runnable {
 
     private Logger logger = Logger.getLogger(ControlWorker.class);
 
     private PrintWriter socketWriter;
 
     private BufferedReader socketReader;
 
     private Socket socket;
 
     public static final String CONNECTING_MESSAGE = "connecting";
     public static final String INSUFFICIENT_ARGS_MESSAGE = "insufficient arguments";
     public static final String FILE_NOT_FOUND_MESSAGE = "not found";
 
     /**
      * Create a worker to handle a connection
      * @param socket a socket representing the connection for this
      *               worker to handle
      */
     public ControlWorker(Socket socket) throws IOException {
 
         setSocket(socket);
     }
 
     /**
      * For testing purposes
      */
     public ControlWorker() {
 
         BasicConfigurator.configure(); // configure log4j
     }
 
     @Override
     public void run() {
         logger.info("Servicing connection");
 
         while(!socket.isClosed()) {
             handleCmd();
         }
     }
 
     public void handleCmd() {
         try {
             String cmd = socketReader.readLine();
             logger.info("Read " + cmd);
 
             switch(cmd) {
                 case "ls":
                     ls();
                     break;
                 case "put":
                     put();
                     break;
                 case "get":
                     get();
                     break;
                 case "quit":
                     quit();
                     break;
                 default:
                     break;
             }
         }
         catch (IOException e) {
             logger.warn("Nothing in input buffer");
         }
     }
 
     /**
      * Handle quit command
      */
     public void quit() {
 
         logger.info("Closing connection");
         try {
             socket.close();
         }
         catch (IOException e) {
             logger.warn("Unable to close socket: " + e.getMessage(), e);
         }
     }
 
     /**
      * Handle the ls command
      */
     public void ls() {
 
         logger.info("handling \"ls\"");
 
         int port = nextNum();
         if(port == 0) {
             logger.warn("Insufficient arguments supplied to ls command");
             insufficientArgs();
             return;
         }
         connect(null); // send message "connecting" to client
 
         // transfer data
         try {
             DataChannelClient dataChannel =
                     new DataChannelClient(socket.getInetAddress(), port);
             // todo figure out how to do ls from java
             dataChannel.upload("list of file contents");
             dataChannel.close();
            System.out.println("ls succeeded");
         }
         catch(IOException e) {
            System.err.println("ls failed: " + e.getMessage());
         }
     }
 

    /**
     * Perform the ls command
     * @return a string containing the results of the `ls`
     */
    private String doLs() {

        // todo invoke the ls command
        return "this is the result of ls";
    }

     /**
      * Handle a request to download a file
      */
     public void get() {
 
         String fileName = nextArg();
         int port = nextNum();
         if(fileName == null  || port == 0) {
             logger.warn("Insufficient arguments supplied to get command");
             insufficientArgs();
             return;
         }
         else if(!isValidFile(fileName)) {
             logger.warn("File \"" + fileName + "\" not found");
             notFound();
             return;
         }
 
         logger.info("handling \"get\" " + fileName);
         // respond to request with "connecting"
         File file = new File(fileName);
         String[] args = {Long.toString(file.length())};
         connect(args);
 
         // "upload" file to client
         try {
             DataChannelClient dataChannel =
                     new DataChannelClient(socket.getInetAddress(), port);
             dataChannel.upload(file);
             dataChannel.close();
            System.out.println("get \"" + fileName + "\" succeeded");
         }
         catch(IOException e) {
            System.err.println("get \"" + fileName + "\" failed: "
                    + e.getMessage());
         }
     }
 
     /**
      * Handle a request to upload a file
      */
     public void put() {
 
         String fileName = nextArg();
         int fileSize = nextNum();
         int port = nextNum();
         if(fileName == null || fileSize == 0 || port == 0) {
             logger.warn("Insufficient arguments supplied to put command");
             insufficientArgs();
             return;
         }
 
         // fileName will always be the second line
         logger.info("handling \"put " + fileName + "\"");
         connect(null);
         File file = new File(fileName);
         // open socket connection to client
         // download file from client
         try {
             DataChannelClient dataChannel =
                     new DataChannelClient(socket.getInetAddress(), port);
             // todo figure out how to do ls from java
             dataChannel.download(file, fileSize);
             dataChannel.close();
            System.out.println("put \"" + fileName + "\" succeeded");
         }
         catch(IOException e) {
            System.err.println("put \"" + fileName + "\" failed: "
                    + e.getMessage());
         }
     }
 
     /**
      * Extract fileName from stream
      * @return string representing the next argument passed to the server
      * null if none
      */
     private String nextArg() {
 
         String arg;
 
         try {
             arg = socketReader.readLine();
 
             if(arg == null || arg.length() == 0) {
                 throw new IllegalArgumentException("argument omitted");
             }
         }
         catch (IllegalArgumentException | IOException e) {
             return null;
         }
         return arg;
     }
 
     /**
      * Extracts a port number from the argument list
      * @return port number or 0 on failure
      */
     private int nextNum() {
 
         String rawArgument = nextArg();
         int port = 0;
 
         if(rawArgument != null) {
             try {
                 port = Integer.parseInt(rawArgument);
             }
             catch(NumberFormatException e) {
                 port = 0;
             }
         }
 
         return port;
     }
 
     /**
      * Checks if a file exists
      * @param fileName name of file
      * @return true if file exists
      */
     private boolean isValidFile(String fileName) {
 
         return new File(fileName).exists();
     }
 
     /**
      * Send a "connecting" response to client
      * @param args Other arguments to send
      */
     private void connect(String[] args) {
 
         if(args != null) {
             for(String s : args) {
                 socketWriter.println(s);
             }
         }
 
         socketWriter.println(CONNECTING_MESSAGE);
         socketWriter.flush();
     }
 
     /**
      * Send a "not found" message to client
      */
     private void notFound() {
 
         socketWriter.println(FILE_NOT_FOUND_MESSAGE);
         socketWriter.flush();
     }
 
     /**
      * Send an insufficient args response to client
      */
     private void insufficientArgs() {
 
         socketWriter.println(INSUFFICIENT_ARGS_MESSAGE);
         socketWriter.flush();
     }
 
     // region Setters and Getters
 
     public Socket getSocket() {
         return socket;
     }
 
     /**
      * Sets socket and streams that read from socket
      * @param socket socket to set
      * @throws IOException if unable to open a stream into the socket
      */
     public void setSocket(Socket socket) throws IOException {
 
         this.socket = socket;
         socketWriter = new PrintWriter(socket.getOutputStream());
         socketReader = new BufferedReader(
                 new InputStreamReader(socket.getInputStream())
         );
     }
 
     // endregion
 }
