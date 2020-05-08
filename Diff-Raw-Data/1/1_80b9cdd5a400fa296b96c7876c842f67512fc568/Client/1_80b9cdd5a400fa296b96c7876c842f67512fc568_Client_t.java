 package client;
 
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import server.packet.LoginAcknowledgementType;
 import server.packet.ServerPacket;
 import server.packet.impl.CurrentGameStatePacket;
 import server.packet.impl.CurrentUsersListPacket;
 import server.packet.impl.GameResultPacket;
 import server.packet.impl.IllegalMovePacket;
 import server.packet.impl.LoginAcknowledgementPacket;
 import server.packet.impl.PlayRequestAcknowledgementPacket;
 import server.packet.impl.PlayRequestPacket;
 
 import common.Payload;
 import common.User;
 
 import exception.BadPacketException;
 
 /**
  * Tic Tac Toe client
  *
  * @author evan
  *
  */
 public class Client {
     /**
      * Creates a client and runs it.
      *
      * @param args
      */
     public static void main(String[] args) {
         //check arguments
         if (args.length != 2) {
             System.out.println("usage: client <server-ip> <server-port>");
             return;
         }
 
         try {
             (new Client(args[0], Integer.valueOf(args[1]))).run();
         } catch (SocketException e) {
             System.out.println("Could not connect to socket.");
         }
     }
 
     private final DatagramSocket socket;
     private final ExecutorService pool;
     private User currentUser;
     private final String receiverIP;
 
     private final int receiverPort;
 
     /**
      * Creates client that sends packets to specified ip and port
      *
      * @param serverIp
      * @param serverPort
      * @throws SocketException
      */
     public Client(String serverIp, int serverPort) throws SocketException {
         this.receiverIP = serverIp;
         this.receiverPort = serverPort;
         socket = new DatagramSocket();
         pool = Executors.newFixedThreadPool(2);
     }
 
     /**
      * @return user that client is currently logged in as
      */
     public User getCurrentUser() {
         return currentUser;
     }
 
     /**
      * @return local ip address of the client
      */
     public String getIP() {
         return socket.getLocalAddress().getHostAddress();
     }
 
     /**
      * @return local port of client
      */
     public int getPort() {
         return socket.getLocalPort();
     }
 
     /**
      * respond to server packet by figuring out that type of packet it is and calling the appropriate response method.
      *
      * @param packet
      */
     private void handle(ServerPacket packet) {
         switch (packet.getPacketType()) {
             case ACK:
                 return;
             case CURRENT_GAME_STATE:
                 handleCurrentGameState((CurrentGameStatePacket) packet);
                 return;
             case CURRENT_USERS_LIST:
                 handleUserList((CurrentUsersListPacket) packet);
                 return;
             case GAME_RESULT:
                 handleGameResult((GameResultPacket) packet);
                 return;
             case ILLEGAL_MOVE:
                 handleIllegalMove((IllegalMovePacket) packet);
                return;
             case LOGIN_ACK:
                 handleLoginAck((LoginAcknowledgementPacket) packet);
                 return;
             case PLAY_REQUEST_ACK:
                 handlePlayRequestAck((PlayRequestAcknowledgementPacket) packet);
                 return;
             case PLAY_REQUEST:
                 handlePlayRequest((PlayRequestPacket) packet);
                 return;
             default:
                 throw new BadPacketException("Unrecognized packet format");
         }
     }
 
     private void handleIllegalMove(IllegalMovePacket packet) {
         System.out.println(packet.toFormattedString());
     }
 
     /**
      * respond to current game state packet by printing out the tic tac toe board
      *
      * @param packet
      */
     private void handleCurrentGameState(CurrentGameStatePacket packet) {
         System.out.println(packet.toFormattedString());
     }
 
     /**
      * respond to game result packet by printing out a message to the console
      *
      * @param packet
      */
     private void handleGameResult(GameResultPacket packet) {
         String message = currentUser.getUsername();
         switch (packet.getResult()) {
             case DRAW:
                 message += " draw";
                 break;
             case LOSS:
                 message += " lose";
                 break;
             case WIN:
                 message += " win";
                 break;
             default:
                 break;
         }
         System.out.println(message);
     }
 
     /**
      * respond to a login acknowledgement packet
      *
      * @param packet
      * @return
      */
     private void handleLoginAck(LoginAcknowledgementPacket packet) {
         //print login message to console
         String message = "login ";
         message += packet.getAcktype() == LoginAcknowledgementType.SUCCESS ? "success " : "failure ";
         message += currentUser.getUsername();
         System.out.println(message);
     }
 
     /**
      * log out user and print message
      */
     public void handleLogout() {
         System.out.println(currentUser.getUsername() + " logout");
         currentUser = null;
     }
 
     /**
      * respond to a play request packet by printing out info to client
      *
      * @param packet
      */
     private void handlePlayRequest(PlayRequestPacket packet) {
         System.out.println(packet.toFormattedString());
     }
 
     /**
      * respond to a play request ack packet by printing out info to client
      *
      * @param packet
      */
     private void handlePlayRequestAck(PlayRequestAcknowledgementPacket packet) {
         System.out.println(packet.toFormattedString());
     }
 
     /**
      * respond to a current users list packet by printing the list to the console
      *
      * @param packet
      * @return
      */
     private void handleUserList(CurrentUsersListPacket packet) {
         System.out.println(packet.getUsers().toFormattedString());
     }
 
     /**
      * sets the current user variable
      *
      * @param username
      * @param ip
      * @param port
      */
     public void login(String username, String ip, int port) {
         currentUser = new User(username, ip, port);
     }
 
     /**
      * runs a client UDPReceiver that receives packets from the server
      *
      * @throws SocketException
      */
     public void recieve() throws SocketException {
         pool.execute(new UDPReciever(socket, this));
     }
 
     /**
      * respond to a datagram packet
      *
      * @param p
      * @throws UnknownHostException
      */
     public void respond(DatagramPacket p) throws UnknownHostException {
         Payload payload = new Payload(new String(p.getData(), 0, p.getLength()));
         ServerPacket sp = ServerPacket.fromPayload(payload);
         handle(sp);
     }
 
     /**
      * puts UDPSender and receiver in thread pool and runs them
      *
      * @throws SocketException
      */
     private void run() throws SocketException {
         send();
         recieve();
     }
 
     /**
      * runs a client UDPSender that takes console input and sends it, waiting for acks
      *
      * @throws SocketException
      */
     public void send() throws SocketException {
         pool.execute(new UDPSender(new DatagramSocket(), receiverIP, receiverPort, this));
     }
 }
