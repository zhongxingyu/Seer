 package ogo.spec.game.multiplayer.client;
 
 import java.io.*;
 import java.util.*;
 import java.net.*;
 import java.util.concurrent.*;
 
 import ogo.spec.game.multiplayer.*;
 import ogo.spec.game.multiplayer.PeerInfo;
 import ogo.spec.game.multiplayer.client.peer.*;
 import ogo.spec.game.multiplayer.GameProto.*;
 
 /**
  * Client for network communication.
  */
 public class Client {
 
     public final static int PORT = 25765;
     public final static int INIT_PORT = 25745; // this is a UDP port
     public final static int INIT_LISTEN_PORT = 4444; // this is a UDP port
     public final static String BROADCAST_IP = "192.168.1.255";
 
     public final static int WAIT_SERVER_TIMEOUT = 1000;
 
     /**
      * Runnable for receiving UDP packets.
      */
     class DatagramReceiverRunnable implements Runnable
     {
         DatagramSocket sock;
 
         ConcurrentLinkedQueue<DatagramPacket> buffer = new ConcurrentLinkedQueue<DatagramPacket>();
 
         boolean read;
 
         DatagramReceiverRunnable(DatagramSocket sock)
         {
             this.sock = sock;
             this.read = true;
         }
 
         public void stop(){
             read = false;
         }
 
         public void run()
         {
             try {
                 while (read) {
                     DatagramPacket p = new DatagramPacket(new byte[1], 1);
 
                     sock.receive(p);
 
                     buffer.add(p);
                 }
             } catch (IOException e) {
                 if(read){
                     System.err.println("I/O Error");
                     e.printStackTrace();
                     System.exit(1);
                 }
             }
         }
     }
 
     /**
      * Thread to wait for a server connection.
      */
     class ServerConnectionListenRunnable implements Runnable
     {
         PeerServer server;
 
         ServerConnectionListenRunnable(PeerServer server)
         {
             this.server = server;
         }
 
         public void run()
         {
             try {
                 server.accept();
             } catch (IOException e) {
                 System.err.println("I/O not working");
                 e.printStackTrace();
                 System.exit(-1);
             }
         }
     }
 
     class StartConnectionRunnable implements Runnable
     {
         InitServer initServer;
 
         StartConnectionRunnable(InitServer init){
             initServer = init;
         }
 
         public void run(){
 
         }
     }
 
 
     // API methods
     protected DatagramSocket udpSock;
     protected TokenChangeListener tokenChangeListener;
     protected PeerServer server;
     protected PeerClient client;
 
     protected InitServer init;
     protected DatagramReceiverRunnable run;
 
     protected String nickname;
 
     public void close() throws IOException
     {
         run.stop();
         udpSock.close();
         init.close();
     }
 
     /**
      * Constructor.
      */
     public Client(String name) throws SocketException
     {
         udpSock = new DatagramSocket();//INIT_LISTEN_PORT);
         nickname = name;
     }
 
     public Client() throws SocketException
     {
         udpSock = new DatagramSocket();//INIT_LISTEN_PORT);
         nickname = "JeMoeder";
     }
 
 
     /**
      * Find servers on the network.
      */
     public List<PeerInfo> findServers() throws UnknownHostException, IOException, InterruptedException
     {
         sendBroadcast();
 
         return getServerList();
     }
 
     /**
      * Set the token change listener.
      */
     public void setTokenChangeListener(TokenChangeListener listener)
     {
         tokenChangeListener = listener;
     }
 
     /**
      * Connect to the given server, and wait for it to start the token ring.
      *
      * Please note that this method will only terminate when the connection is closes.
      * Thus, you should only call this from a thread that doesn't do anything else.
      */
     public void connectToInitServer(PeerInfo serv) throws IOException, UnknownHostException
     {
         init = new InitServer(serv.ip, serv.port);
     }
 
     PeerInfo info;
     public void connectToPeer() throws IOException, UnknownHostException, InterruptedException
     {
         // find initialization port
         int port = init.getPort();
 
         // create a local server with the given port
 
         server = new PeerServer(port);
 
         // tell the server we got the message and started a server
         init.reply(true);
 
         // create a connection to the given peer
         info = init.getConnectTo();
 
         new Thread(new ServerConnectionListenRunnable(server)).start();
 
         client = new PeerClient(info);
 
         // wait until we are connected
 
         while (!server.isConnected()) {
             Thread.sleep(100);
         }
 
         init.reply(true);
     }
 
     public void startTokenRing() throws Exception{
         if (info.init) {
             init();
         }
 
         while (true) {
             // get the next token and give it to the TokenChangeListener
             Token token = tokenChangeListener.tokenChanged(getToken());
 
             sendToken(token);
             //i++;
         }
     }
 
     // protected methods
     /**
      * Broadcast to find servers.
      */
     protected void sendBroadcast() throws UnknownHostException, IOException
     {
         DatagramPacket packet = new DatagramPacket(new byte[]{1}, 1, InetAddress.getByName(BROADCAST_IP), INIT_PORT);
         udpSock.send(packet);
     }
 
     /**
      * Get the server list.
      */
     protected List<PeerInfo> getServerList() throws InterruptedException
     {
         run = new DatagramReceiverRunnable(udpSock);
         new Thread(run).start();
 
         Thread.sleep(WAIT_SERVER_TIMEOUT);
         DatagramPacket packet = new DatagramPacket(new byte[]{1}, 1);
 
         ArrayList<PeerInfo> peers = new ArrayList<PeerInfo>();
 
         while ((packet = run.buffer.poll()) != null) {
             peers.add(new PeerInfo(PORT, packet.getAddress(), false));
         }
 
         return peers;
     }
 
     /**
      * Get the token.
      */
     protected Token getToken() throws IOException
     {
         try {
             return Token.parseFrom(server.read());
         } catch (com.google.protobuf.InvalidProtocolBufferException e) {
             return null;
         }
     }
 
     /**
      * Send the token.
      */
     protected void sendToken(Token token)
     {
         client.write(token);
     }
 
     /**
      * Send the token.
      */
     protected void init()
     {
         Token token = Token.newBuilder()
             .setLastId(0)
            .setTick(0)
             .build();
         sendToken(token);
     }
 
     public void setReady(GameProto.IsReady ready){
         init.sendReady(ready);
     }
 
     public GameProto.InitialGameState receiveInitialGameState() throws Exception{
         return init.receiveInitialGameState();
     }
 }
