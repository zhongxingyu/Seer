 package server;
 
 import exception.BadPacketException;
 import exception.IllegalMoveException;
 import game.Game;
 import game.GameResultType;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import server.packet.IllegalMoveType;
 import server.packet.LoginAcknowledgementType;
 import server.packet.PlayRequestAcknowledgementStatus;
 import server.packet.ServerPacket;
 import server.packet.impl.AcknowledgementPacket;
 import server.packet.impl.CurrentGameStatePacket;
 import server.packet.impl.CurrentUsersListPacket;
 import server.packet.impl.GameResultPacket;
 import server.packet.impl.IllegalMovePacket;
 import server.packet.impl.LoginAcknowledgementPacket;
 import server.packet.impl.PlayRequestAcknowledgementPacket;
 import server.packet.impl.PlayRequestPacket;
 import client.packet.ClientPacket;
 import client.packet.impl.AcceptRequestPacket;
 import client.packet.impl.ChoosePlayerPacket;
 import client.packet.impl.DenyRequestPacket;
 import client.packet.impl.LoginPacket;
 import client.packet.impl.LogoutPacket;
 import client.packet.impl.PlayGamePacket;
 
 import common.Payload;
 import common.User;
 import common.UserList;
 import common.UserState;
 
 /**
  * Tic tac toe server
  *
  * @author evan
  *
  */
 public class Server {
     /**
      * Creates a server and runs it.
      *
      * @param args
      * @throws SocketException
      * @throws IOException
      */
     public static void main(String[] args) {
         //check arguments
         if (args.length != 1) {
             System.out.println("usage: server <server-port>");
             return;
         }
         try {
             (new Server(Integer.valueOf(args[0]))).recieve();
         } catch (SocketException e) {
             System.out.println("couldn't connect to socket! exiting....");
         } catch (IOException e) {
             System.out.println("couldn't connect to server! exiting....");
         }
     }
 
     private final DatagramSocket socket;
     private final ExecutorService pool;
     private UserList currentUsers;
     private ArrayList<Game> currentGames;
 
     /**
      * Creates a server that listens on the specified port
      *
      * @param port
      * @throws IOException
      */
     private Server(int port) throws IOException {
         this.socket = new DatagramSocket(port);
         this.pool = Executors.newFixedThreadPool(1);
         this.currentGames = new ArrayList<Game>();
         this.currentUsers = new UserList();
     }
 
     /**
      * Sends an ack in response to the input packet
      *
      * @param cp
      * @param p
      */
     private void ack(ClientPacket cp, DatagramPacket p) {
         AcknowledgementPacket ack = new AcknowledgementPacket(cp.getPacketId());
         sendPacket(ack, p.getAddress(), p.getPort());
     }
 
     /**
      * respond to client packet by figuring out that type of packet it is and calling the appropriate handler method.
      *
      * @param packet
      * @param pkt
      * @return
      */
     private ServerPacket getResponse(ClientPacket packet, DatagramPacket pkt) {
         switch (packet.getPacketType()) {
             case LOGIN:
                 return handleLogin((LoginPacket) packet, pkt.getAddress().getHostAddress());
             case QUERY_LIST:
                 return new CurrentUsersListPacket(currentUsers);
             case CHOOSE_PLAYER:
                 return handlePlayRequest((ChoosePlayerPacket) packet);
             case ACCEPT_REQUEST:
                 return handleAcceptRequest((AcceptRequestPacket) packet);
             case DENY_REQUEST:
                 return handleDenyRequest((DenyRequestPacket) packet);
             case PLAY_GAME:
                 return handlePlay((PlayGamePacket) packet);
             case LOGOUT:
                 return handleLogout((LogoutPacket) packet);
             default:
                 throw new BadPacketException("Unrecognized packet format");
         }
     }
 
     /**
      * Responds to a play request acceptance by initiating a game between the two users and sending appropriate packets
      * out
      *
      * @param packet
      * @return null (packet is sent to other user instead)
      */
     private ServerPacket handleAcceptRequest(AcceptRequestPacket packet) {
         //find users in list
         User sender = currentUsers.get(packet.getSender());
         User receiver = currentUsers.get(packet.getReciever());
 
         //set states to busy
         sender.setState(UserState.BUSY);
         receiver.setState(UserState.BUSY);
 
         //send ackchoose packet to original user
         try {
             sendPacket(new PlayRequestAcknowledgementPacket(sender.getUsername(),
                     PlayRequestAcknowledgementStatus.ACCEPTED), receiver);
         } catch (UnknownHostException e) {
             e.printStackTrace();
         }
 
         //initiate game
         Game g = new Game(receiver, sender);
         currentGames.add(g);
 
         //send game state packet to player 1
         try {
             sendPacket(new CurrentGameStatePacket(g), receiver);
         } catch (UnknownHostException e) {
             e.printStackTrace();
         }
         return null;
     }
 
 
     /**
      * Responds to play request denial by setting states to free and sending appropriate packets
      *
      * @param packet
      * @return null (packet is sent to other user instead)
      */
     private ServerPacket handleDenyRequest(DenyRequestPacket packet) {
         //look up users
         User sender = currentUsers.get(packet.getSender());
         User receiver = currentUsers.get(packet.getReciever());
 
         //set states to free
         sender.setState(UserState.FREE);
         receiver.setState(UserState.FREE);
 
         //send ackchoose packet to original requester
         try {
             sendPacket(
                     new PlayRequestAcknowledgementPacket(sender.getUsername(), PlayRequestAcknowledgementStatus.DENY),
                     receiver);
         } catch (UnknownHostException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     /**
      * handle login packet by registering user and setting state to FREE
      *
      * @param packet
      * @return acklogin packet to be sent back to user
      */
     private LoginAcknowledgementPacket handleLogin(LoginPacket packet, String ip) {
         // try to find a user with the input username
         User u = currentUsers.get(packet.getUsername());
 
         // register user if no user exists with this username
         if (u == null) {
             u = new User(packet.getUsername(), UserState.OFFLINE, ip, packet.getPort());
             currentUsers.put(u.getUsername(), u);
         }
 
         if (u.getState() == UserState.OFFLINE) {
             //check for maximum of five online from same IP, fail if so
             if (currentUsers.moreThanNOnlineWithSameIp(5, ip)) {
                 return new LoginAcknowledgementPacket(LoginAcknowledgementType.FAILURE);
             }
             // login success for offline/newly registered user
             u.setState(UserState.FREE);
             return new LoginAcknowledgementPacket(LoginAcknowledgementType.SUCCESS);
 
         } else if (u.getState() == UserState.BUSY || u.getState() == UserState.DECISION
                 || u.getState() == UserState.FREE) {
             // login failure for online user
             return new LoginAcknowledgementPacket(LoginAcknowledgementType.FAILURE);
 
         } else {
             // this should not be reachable
             throw new IllegalStateException();
         }
     }
 
     /**
      * handle logout packet by setting user state to offline
      *
      * @param packet
      * @return null (no packet sent back)
      */
     private ServerPacket handleLogout(LogoutPacket packet) {
         //get user
         User u = currentUsers.get(packet.getUsername());
 
         //null check
         if (u == null)
             return null;
 
         //end any games
         Game g = u.getCurrentGame();
         if (g != null) {
             u.getCurrentGame().terminate();
             currentGames.remove(g);
         }
 
         //set user state
         u.setState(UserState.OFFLINE);
 
         return null;
     }
 
     /**
      * Handle play game packet by attempting to make a move on the tic tac toe board
      *
      * @param packet
      * @return IllegalMovePacket or GameResultPacket
      */
     private ServerPacket handlePlay(PlayGamePacket packet) {
         User u = currentUsers.get(packet.getUsername());
 
         // make sure user is playing a game and it's their turn
         if (u == null || u.getCurrentGame() == null || !u.getCurrentGame().isTurn(u)) {
             return new IllegalMovePacket(IllegalMoveType.OUT_OF_TURN);
         }
 
         // check for illegal move
         if (packet.getCellNumber() < 1 || packet.getCellNumber() > 9) {
             return new IllegalMovePacket(IllegalMoveType.OCCUPIED);
         }
 
         //now we can attempt to make the move
         Game g = u.getCurrentGame();
         try {
             //different packets sent on different game results
             switch (g.play(packet.getCellNumber())) {
                 case WIN:
                     g.terminate();
                     currentGames.remove(g);
                     try {
                         sendPacket(new GameResultPacket(GameResultType.LOSS), g.otherPlayer(u));
                     } catch (UnknownHostException e) {
                         e.printStackTrace();
                     }
                     return new GameResultPacket(GameResultType.WIN);
                 case DRAW:
                     g.terminate();
                     currentGames.remove(g);
                     try {
                         sendPacket(new GameResultPacket(GameResultType.DRAW), g.otherPlayer(u));
                     } catch (UnknownHostException e) {
                         e.printStackTrace();
                     }
                     return new GameResultPacket(GameResultType.DRAW);
                 case IN_PROGRESS:
                     try {
                         sendPacket(new CurrentGameStatePacket(g), g.otherPlayer(u));
                     } catch (UnknownHostException e) {
                         e.printStackTrace();
                     }
                     return null;
                 default:
                     break;
             }
         } catch (IllegalMoveException e) {
             return new IllegalMovePacket(IllegalMoveType.OCCUPIED);
         }
 
         return null;
 
     }
 
     /**
      * Handles a play request packet by setting user states and sending appropriate packets
      * @param packet
      * @return
      */
     private ServerPacket handlePlayRequest(ChoosePlayerPacket packet) {
         // check state of sender
         User sender = currentUsers.get(packet.getSender());
         if (sender == null || sender.getState() != UserState.FREE) {
             return new PlayRequestAcknowledgementPacket(sender == null ? "" : sender.getUsername(),
                     PlayRequestAcknowledgementStatus.FAILURE);
         }
 
         // sender is free, check state of receiver
         User receiver = currentUsers.get(packet.getReciever());
         if (receiver == null || receiver.getState() != UserState.FREE || receiver == sender) {
             return new PlayRequestAcknowledgementPacket(packet.getReciever(), PlayRequestAcknowledgementStatus.FAILURE);
         }
 
         // receiver is free, set states into decision
         sender.setState(UserState.DECISION);
         receiver.setState(UserState.DECISION);
 
         // send choose message to client two
         try {
             sendPacket(new PlayRequestPacket(sender.getUsername()), receiver);
         } catch (UnknownHostException e) {
             return new PlayRequestAcknowledgementPacket(receiver.getUsername(),
                     PlayRequestAcknowledgementStatus.FAILURE);
         }
 
         return null;
     }
 
     /**
      * Starts up the server
      * @throws SocketException
      */
     public void recieve() throws SocketException {
         pool.execute(new UDPReciever(socket, this));
     }
 
     /**
      * responds to the input datagram packet
      * @param p
      * @throws UnknownHostException
      */
     public void respond(DatagramPacket p) throws UnknownHostException {
         Payload payload = new Payload(new String(p.getData(), 0, p.getLength()).trim());
         ClientPacket cp = ClientPacket.fromPayload(payload);
 
         ack(cp, p);
 
         ServerPacket response = getResponse(cp, p);
         sendPacket(response, currentUsers.get(cp.getUsername()));
     }
 
     /**
      * sends a packet from server to specified address and port
      *
      * @param p
      * @param addr
      * @param port
      */
     public void sendPacket(ServerPacket p, InetAddress addr, int port) {
         if (p == null)
             return;
 
         byte[] payload = p.toPayload().getBytes();
         DatagramPacket sendPacket;
         sendPacket = new DatagramPacket(payload, payload.length, addr, port);
 
         try {
             socket.send(sendPacket);
         } catch (IOException e) {
             socket.close();
             e.printStackTrace();
             return;
         }
 
         System.out.println("[" + Calendar.getInstance().getTimeInMillis() + "] Sent to client: (IP: "
                 + addr.getHostName() + ", Port: " + String.valueOf(port) + "): " + p.toPayload());
     }
 
     /**
      * sends packet to specified user
      *
      * @param response
      * @param u
      * @throws UnknownHostException
      */
     private void sendPacket(ServerPacket response, User u) throws UnknownHostException {
         sendPacket(response, InetAddress.getByName(u.getIp()), u.getPort());
     }
 }
