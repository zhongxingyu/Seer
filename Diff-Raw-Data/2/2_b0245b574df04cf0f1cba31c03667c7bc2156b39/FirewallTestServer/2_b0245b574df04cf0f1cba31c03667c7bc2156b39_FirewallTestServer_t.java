 import java.io.*;
 import java.net.*;
 
 /**
  * Usage: FirewallTestServer [LISTENING_PORT]
  *
  * Listens for incoming UDP connection and responds. After accepting a UDP
  * message and before responding it sets up TCP server for incoming connection
  * and responds to that to. After that it repeats the process.
  */
 class FirewallTestServer {
   private static int LISTENING_PORT = 9876;
   private static int MAX_DATA_LEN = 1024;
 
   public static void main(String args[]) throws Exception {
     int serverPort = LISTENING_PORT;
     if (args.length > 0) {
       try {
         serverPort = Integer.parseInt(args[0]);
       } catch (NumberFormatException e) {
         System.out.println("Given arg is not a correct port number.");
       }
     }
 
     try {
       DatagramSocket serverSocket = new DatagramSocket(serverPort);
 
       byte[] receiveData = new byte[MAX_DATA_LEN];
       byte[] sendData  = new byte[MAX_DATA_LEN];
 
       receiveData = new byte[MAX_DATA_LEN];
 
       DatagramPacket receivePacket =
         new DatagramPacket(receiveData, receiveData.length);
 
       System.out.println("Waiting for datagram packet");
       serverSocket.receive(receivePacket);
 
       String sentence = new String(receivePacket.getData());
 
       InetAddress IPAddress = receivePacket.getAddress();
       int port = receivePacket.getPort();
 
       System.out.println ("From: " + IPAddress + ":" + port);
       System.out.println ("Message: " + sentence);
 
       DatagramPacket sendPacket =
         new DatagramPacket(receivePacket.getData(),
             receivePacket.getData().length, IPAddress, port);
 
       ServerSocket tcpServerSocket =  setUpTCPListener(serverPort);
 
       serverSocket.send(sendPacket);
 
       listenOnTCP(tcpServerSocket);
 
     } catch (SocketException ex) {
       System.out.println("UDP Port 9876 is occupied.");
       System.exit(1);
     }
 
 
   }
 
   static ServerSocket setUpTCPListener(int serverPort) throws Exception {
     ServerSocket serverSocket = null;
 
     try {
       serverSocket = new ServerSocket(serverPort);
     } catch (IOException e) {
      System.err.println("Could not listen on port: " + serverPort + ". Due to: " + e); 
       System.exit(1);
     }
 
     return serverSocket;
   }
 
   static void listenOnTCP(ServerSocket serverSocket)  throws Exception {
     Socket clientSocket = null;
     System.out.println ("Waiting for connection.....");
 
     try {
       clientSocket = serverSocket.accept();
     } catch (IOException e) {
       System.err.println("Accept failed.");
       System.exit(1);
     }
 
     System.out.println ("Connection successful");
     System.out.println ("Waiting for input.....");
 
     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),
         true);
     BufferedReader in = new BufferedReader(
         new InputStreamReader(clientSocket.getInputStream()));
 
     String inputLine = in.readLine();
     if (inputLine == null) {
       System.err.println("Received null inputline");
       System.exit(1);
     }
 
     System.out.println ("Server: " + inputLine);
     out.println(inputLine);
 
     out.close();
     in.close();
     clientSocket.close();
     serverSocket.close();
   }
 }
 
