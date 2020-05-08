 package cpsc471.ftp.client.control;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Logger;
 
 import java.io.*;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 /**
  * Implementation of the ControlClient interface
  */
 public class ControlClientImpl implements ControlClient {
 
     private Logger logger = Logger.getLogger(this.getClass());
 
     private String domainName;
 
     private short port;
 
     private Socket socket;
 
     private PrintWriter socketWriter;
 
     private BufferedReader socketReader;
 
     /**
      * Create a ControlClient
      * @param domainName domainName name of server to which to connect
      * @param port port on server to which to connect
      * @throws IllegalArgumentException if domainName name isn't found
      */
     public ControlClientImpl(String domainName, short port)
             throws UnknownHostException, IOException {
 
         this.domainName = domainName;
         this.port = port;
 
         setSocket(new Socket(domainName, port));
     }
 
     /**
      * Basic constructor, for testing
      */
     public ControlClientImpl() {
 
         BasicConfigurator.configure(); // configure log4j
     }
 
     @Override
     public void ls() {
 
         socketWriter.println("ls");
         socketWriter.flush();
 
         // todo add data transfer
     }
 
     @Override
     public void put(String localFile)
         throws FileNotFoundException {
 
         put(new File(localFile));
     }
 
     /**
      * Upload a file to the server
      * @param localFile the file to upload
      * @throws FileNotFoundException file to upload
      */
     public void put(File localFile)
             throws FileNotFoundException {
 
         if(localFile.exists()) {
             socketWriter.println("put");
             socketWriter.println(localFile.getName());
             socketWriter.flush();
         }
         else {
             throw new FileNotFoundException(
                     "File \"" + localFile.getName() + "\" does not exist"
             );
         }
     }
 
     @Override
     public void get(String remoteFile)
         throws FileNotFoundException {
 
         // send get request to server
         socketWriter.println("get");
         socketWriter.println(remoteFile);
         socketWriter.flush();
 
         String serverResponse = null;
         try {
             serverResponse = socketReader.readLine();
         }
         catch (IOException e) {
             logger.warn("Unable to read from socket: " + e.getMessage(), e);
         }
 
        if(serverResponse != "connecting") {
             throw new FileNotFoundException("Server could not find file.");
         }
     }
 
     @Override
     public void quit() {
 
         try {
             socket.close();
         }
         catch (IOException e) {
             logger.warn("Unable to close socket:" + e.getMessage(), e);
         }
     }
 
     // region Getters and Setters
 
     public String getDomainName() {
         return domainName;
     }
 
     public short getPort() {
         return port;
     }
 
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
         socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
     }
     // endregion
 }
