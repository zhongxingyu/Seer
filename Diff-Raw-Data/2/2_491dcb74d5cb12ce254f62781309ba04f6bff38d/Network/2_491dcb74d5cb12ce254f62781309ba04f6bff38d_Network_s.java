 package baller.client.net;
 
 import baller.client.gui.Square;
 
 import java.io.*;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Logger;
 
 public class Network {
 
     private static final Logger log = Logger.getLogger(Network.class.getName());
 
     private int clientID;
 
     private Socket pushSocket;
     private Socket readSocket;
     private BufferedReader reader;
     private PrintWriter pusher;
 
     private final String hostAdr;
     private final int readPort;
     private final int pushPort;
 
     private boolean ready;
 
     public Network(String host, int readPort, int pushPort) {
         this.hostAdr = host;
         this.readPort = readPort;
         this.pushPort = pushPort;
 
         ready = false;
     }
 
     public Network() {
         this ("localhost", 1234, 4321);
     }
 
     public void setUp() {
         try {
             setUpReadSocket();
             setUpPushSocket();
         } catch (IOException e) {
             log.info(e.toString());
             close();
             reader = null;
             pusher = null;
         }
 
         ready = true;
     }
 
     public void closeNetwork() {
         close();
     }
 
     private void checkReady() {
         if (!ready) {
             throw new IllegalStateException("Network not ready");
         }
     }
 
     public BufferedReader getReader() {
         checkReady();
         return reader;
     }
 
     public PrintWriter getPusher() {
         checkReady();
         return pusher;
     }
 
     public int getClientID() {
         checkReady();
         return clientID;
     }
 
     public List<Square> waitForStartUp() {
         try {
             String starting = reader.readLine();
             assert starting.equals("starting");
 
             String idString = reader.readLine();
             String[] ids = idString.split(":");
             List<Square> squares = new ArrayList<Square>();
             for(String id : ids) {
                 squares.add(new Square(Integer.parseInt(id)));
             }
             return squares;
         } catch (IOException e) {
             log.info(e.toString());
             return Collections.emptyList();
         }
     }
 
 
     private void setUpReadSocket() throws IOException {
         readSocket = new Socket(hostAdr, readPort);
         reader = new BufferedReader(new InputStreamReader(readSocket.getInputStream()));
         clientID = Integer.parseInt(reader.readLine());
         log.info("got id: " + clientID);
     }
 
     private void setUpPushSocket() throws IOException {
         pushSocket = new Socket(hostAdr, pushPort);
        pusher = new PrintWriter(pushSocket.getOutputStream());
     }
 
 
     private void close() {
         closeSocket(readSocket, reader);
         closeSocket(pushSocket, pusher);
     }
 
     private void closeSocket(Socket socket, Closeable closeable) {
         try {
             closeable.close();
             socket.close();
         } catch (IOException e) {
             log.info(e.toString());
         }
     }
 }
