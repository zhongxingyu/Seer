 package mines.thread;
 
 /**
  * ServerUdpThread class
  * <p/>
  * This class is used to ...
  *
  * @author <a href="mailto:nnombela@gmail.com">Nicolas Nombela</a>
  * @since 09-jul-2006
  */
 
 import java.net.*;
 import java.util.*;
 import java.io.IOException;
 import mines.model.*;
 import mines.net.*;
 
 
 public class ServerThread extends UdpThread implements EventReceiver, EventSender {
     private Map<SocketAddress, Player> players = new Hashtable<SocketAddress, Player>();
     private Map<Player, GameSession> sessions = new Hashtable<Player, GameSession>();
     private Timer timer = new Timer();
 
     public ServerThread(int port) throws IOException {
         super(port, 1);
     }
 
     public static void main(String[] args) {
         try {
             int port = args.length == 1? Integer.parseInt(args[0]) : SERVER_DEFAULT_PORT;
             UdpThread server = new ServerThread(port);
             server.start();
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
     
     public void process(InetSocketAddress address)  {
         byte event = buffer.get();
         //System.out.println("Received event " + event);
 
         Player from = getPlayer(event, address);
         if (from == null) {
             sendError(new Player(address, "error client"), Event.NOT_LOGGED_ERROR_CODE);
             return;
         }
 
         from.updateLastEventTime();
         
         switch(event) {
             case Event.UNCOVER_CELL:
                 receiveUncoverCell(from);
                 break;
             case Event.SET_FLAG:
                 receiveSetFlag(from);
                 break;
             case Event.TALK_MESSAGE:
                 receiveTalkMessage(from);
                 break;
             case Event.PLAYER_READY:
                 receivePlayerReady(from);
                 break;
             case Event.INIT_BOARD:
                 receiveInitBoard(from);
                 break;
             case Event.JOIN_SESSION:
                 receiveJoinSession(from);
                 break;
             case Event.LOGIN_SERVER:
                 receiveLoginServer(from);
                 break;
             case Event.LOGOUT_SERVER:
                 receiveLogoutServer(from);
                 break;
             case Event.CREATE_SESSION:
                 receiveCreateSession(from);
                 break;
             case Event.SESSION_LIST:
                 receiveSessionList(from);
                 break;
             case Event.PLAYER_LIST:
                 receivePlayerList(from);
                 break;
             case Event.QUIT_SESSION:
                 receiveQuitSession(from);
                 break;
             case Event.HEART_BEAT:
                 receiveHeartbeat(from);
                 break;
             case Event.ABOUT:
                 receiveAbout(from);
                 break;
 
             default:
                 System.err.println("Unknown event " + buffer.receiver() + " from player " + from);
         }
     }
 
     private Player getPlayer(byte event, InetSocketAddress address) {
         Player from = players.get(address);
         if (from == null) {
             if (event == Event.LOGIN_SERVER) {
                 from = new Player(address, "client");
                 players.put(address, from);
             } else if (event == Event.ABOUT) {
                 from = new Player(address, "about client");
             } else if (event == Event.PLAYER_LIST) {
                 from = new Player(address, "list client");
             }
         }
         return from;
     }
 
     //-----------------------------------------------------
 
     public void sendError(Player to, String message) {
         buffer.put(Event.ERROR_SERVER);
         buffer.putString(message);
         send(to.getAddress());
     }
 
     public void sendHeartbeat(Player to) {
         buffer.put(Event.HEART_BEAT);
         send(to.getAddress());
     }
 
     public void sendUncoverCell(Player to, Cell cell) {
         buffer.put(Event.UNCOVER_CELL);
         buffer.putInt(cell.getX());
         buffer.putInt(cell.getY());
         buffer.putInt(cell.getUncoveredBy().getId());
         send(to.getAddress());
     }
 
     public void sendSetFlag(Player to, Cell cell) {
         buffer.put(Event.SET_FLAG);
         buffer.putInt(cell.getX());
         buffer.putInt(cell.getY());
         buffer.putInt(cell.getSetFlagBy().getId());
         send(to.getAddress());
     }
 
     public void sendPlayerList(Player to) {
         buffer.put(Event.PLAYER_LIST);
         buffer.putInt(players.size());
         for(Player player: players.values()) {
             buffer.putPlayer(player);
             if (player.getSession() != null) {
                 buffer.putString(player.getSession().getName());
             } else {
                 buffer.putString("NULL");
             }
         }
         send(to.getAddress());
     }
 
 
     public void sendSessionList(Player to) {
         buffer.put(Event.SESSION_LIST);
         buffer.putInt(sessions.size());
         for(GameSession session : sessions.values()) {
             buffer.putString(session.getName());
             buffer.putPlayer(session.getCreator());
             buffer.putInt(session.getNumPlayers());
             buffer.putBoolean(session.hasStarted());
         }
         send(to.getAddress());
     }
 
     public void sendLoginServer(Player to, String username) {
         buffer.put(Event.LOGIN_SERVER);
         buffer.putString(username);
         send(to.getAddress());
     }
 
     public void sendLogoutServer(Player to) {
         buffer.put(Event.LOGOUT_SERVER);
         send(to.getAddress());
     }
 
     public void sendGameOver(Player to, Player winner) {
         buffer.put(Event.GAME_OVER);
         buffer.putInt(winner.getId());
         send(to.getAddress());
     }
 
     public void sendFinishGame(Player to, Player looser) {
         buffer.put(Event.FINISH_GAME);
         buffer.putInt(looser.getId());
         send(to.getAddress());
     }
 
     public void sendPlayerReady(Player to, Player player) {
         buffer.put(Event.PLAYER_READY);
         buffer.putInt(player.getId());
         buffer.putInt(player.getScore());
         send(to.getAddress());
     }
 
     public void sendCountdownGame(Player to) {
         buffer.put(Event.COUNTDOWN_GAME);
         send(to.getAddress());
     }
 
     public void sendJoinSession(Player to, Player player) {
         buffer.put(Event.JOIN_SESSION);
         buffer.putPlayer(player);
         buffer.putBoolean(player.isReady());
         buffer.putInt(player.getGamesWon());
         send(to.getAddress());
     }
 
     public void sendCreateSession(Player to, Player creator, String sessionName, int gameMode, boolean autoFlags) {
         buffer.put(Event.CREATE_SESSION);
         buffer.putPlayer(to);
         buffer.putPlayer(creator);
         buffer.putString(sessionName);
         buffer.putInt(gameMode);
         buffer.putBoolean(autoFlags);
         send(to.getAddress());
     }
 
     public void sendQuitSession(Player to, Player player) {
         buffer.put(Event.QUIT_SESSION);
         buffer.putInt(player.getId());
         send(to.getAddress());
     }
 
     public void sendRestartBoard(Player to, Board board) {
         buffer.put(Event.RESTART_BOARD);
 
         buffer.putInt(board.getMines().size());
         for(Cell cell : board.getMines()) {
             buffer.putInt(cell.getX());
             buffer.putInt(cell.getY());
         }
         buffer.putInt(board.getUncovered().size());
         for(Cell cell : board.getUncovered()) {
             buffer.putInt(cell.getX());
             buffer.putInt(cell.getY());
             buffer.putInt(cell.getUncoveredBy().getId());
         }
         buffer.putInt(board.getNumFlags());
         for(Cell cell : board.getMines()) {
             if (cell.hasFlag()) {
                 buffer.putInt(cell.getX());
                 buffer.putInt(cell.getY());
                 buffer.putInt(cell.getSetFlagBy().getId());
             }
         }
         send(to.getAddress());
     }
 
     public void sendInitBoard(Player to, Board board) {
         buffer.put(Event.INIT_BOARD);
         buffer.putInt(board.getXSize());
         buffer.putInt(board.getYSize());
         buffer.putInt(board.getNumMines());
         send(to.getAddress());
     }
 
     public void sendTalkMessage(Player to, Player from, String message) {
         buffer.put(Event.TALK_MESSAGE);
         buffer.putInt(from.getId());
         buffer.putString(message);
         send(to.getAddress());
     }
 
     public void sendAbout(Player to) {
         buffer.put(Event.ABOUT);
         buffer.putMessage(new String[] {
                 "Multiplayer Minesweeper (M2) v0.9", "by nnombela@gmail.com", "Have Fun!"});
         send(to.getAddress());
     }
 
     // ------------------------------------------
 
 
     public void receiveLoginServer(Player from) {
         //System.out.println("LOGIN SERVER EVENT");
         String username = buffer.getString();
         from.setUsername(username);
         sendLoginServer(from, username);
     }
 
     public void receiveLogoutServer(Player from) {
         //System.out.println("LOGOUT SERVER EVENT");
         removeSession(from);
         players.remove(from.getAddress());
         sendLogoutServer(from);
     }
 
     private void removeSession(Player player) {
         GameSession session = player.getSession();
         if (session != null) {
             session.quit(player);
             if (session.getCreator().equals(player)) {
                 removeSession(session);
             }
         }
     }
 
     private void putNewSession(GameSession session) {
         timer.schedule(((ServerSession)session).getTimerTask(), 0, 1000);
         sessions.put(session.getCreator(), session);
     }
 
     private void removeSession(GameSession session) {
         sessions.remove(session.getCreator());
         ((ServerSession)session).getTimerTask().cancel();
         timer.purge();
     }
 
     public void receiveSessionList(Player from) {
         //System.out.println("SESSION LIST EVENT");
         purgeDeadSessions();
         sendSessionList(from);
     }
 
     private void purgeDeadSessions() {
         for(GameSession session : new ArrayList<GameSession>(sessions.values())) {
             if (session.getNumPlayers() == 0) {
                 removeSession(session);
             }
         }
     }
 
     public void receivePlayerList(Player from) {
         //System.out.println("PLAYER LIST EVENT");
         sendPlayerList(from);
     }
 
     public void receiveHeartbeat(Player from) {
         // Do nothing
     }
 
     public void receiveAbout(Player from) {
         sendAbout(from);
     }
 
     public void receiveErrorServer(Player from) {
         sendError(from, Event.GENERAL_ERROR_CODE);
     }
 
     public void receiveJoinSession(Player from) {
         //System.out.println("JOIN SESSION EVENT");
         Player creator = buffer.getPlayer(true);
         GameSession session = sessions.get(creator);
         if (session != null) {
             session.join(from);
         } else {
             sendError(from, Event.SESSION_DOES_NOT_EXITS_ERROR_CODE);
         }
     }
 
     public void receiveTalkMessage(Player from) {
         //System.out.println("TALK MESSAGE EVENT");
         String message = buffer.getString();
         GameSession session = from.getSession();
         if (session != null) {
             session.talk(from, message);
         } else {
             sendError(from, Event.SESSION_DOES_NOT_EXITS_ERROR_CODE);
         }
     }
 
 
     public void receiveCreateSession(Player from) {
        System.out.println("CREATE SESSION EVENT , ACTIVE SESSIONS: " + sessions.size());
         Player self = buffer.getPlayer(false);
         String name = buffer.getString();
         int gameMode = buffer.getInt();
         boolean autoFlags = buffer.getBoolean();
         removeSession(from);
         if (sessions.size() > 20) {
             sendError(from, Event.TOO_MANY_ACTIVE_SESSIONS_ERROR_CODE);
             purgeDeadSessions();
         } else {
             GameSession session = new ServerSession(this);
             session.init(self, from, name, gameMode, autoFlags);
             putNewSession(session);
         }
     }
 
     public void receiveFinishGame(Player from) {
         sendError(from, Event.GENERAL_ERROR_CODE);
    }
 
     public void receiveGameOver(Player from) {
         sendError(from, Event.GENERAL_ERROR_CODE);
     }
 
     public void receiveQuitSession(Player from) {
         System.out.println("QUIT SESSION EVENT");
         Player player = buffer.getPlayer(true);
         Player quitPlayer = players.get(player.getAddress());
         if (quitPlayer == null || quitPlayer.getSession() == null) {
             sendError(from, Event.CAN_NOT_QUIT_SESSION_ERROR_CODE + player.getUsername());
         } else if (from.equals(quitPlayer) || from.equals(quitPlayer.getSession().getCreator())) {
             removeSession(quitPlayer);
         } else {
             sendError(from, Event.CAN_NOT_QUIT_SESSION_ERROR_CODE + quitPlayer.getUsername());
         }
     }
 
     public void receiveRestartBoard(Player from) {
         sendError(from, Event.GENERAL_ERROR_CODE);
     }
 
     public void receivePlayerReady(Player from) {
         //System.out.println("PLAYER READY EVENT");
         if (from.getSession() != null) {
             from.getSession().ready(from);
         } else {
             sendError(from, Event.SESSION_DOES_NOT_EXITS_ERROR_CODE);
         }
     }
 
     public void receiveCountdownGame(Player from) {
         sendError(from, Event.GENERAL_ERROR_CODE);
     }
 
     public void receiveUncoverCell(Player from) {
         //System.out.println("UNCOVER CELL EVENT");
         int x = buffer.getInt();
         int y = buffer.getInt();
         int id = buffer.getInt(); // id must be player.getId()
 
         GameSession session = from.getSession();
         if (session != null) {
             if (from.isReady() == false) {
                 sendError(from, Event.NOT_READY_ERROR_CODE);
             } else if (session.hasStarted() == false) {
                 sendError(from, Event.GAME_HAS_NOT_STARTED_CODE);
             } else {
                 session.uncoverCell(x, y, from);
             }
         } else {
             sendError(from, Event.GAME_HAS_NOT_STARTED_CODE);
             System.err.println("Received uncover cell from " + from.getUsername() + ", but has no session");
         }
     }
 
     public void receiveSetFlag(Player from) {
         //System.out.println("SET FLAG EVENT");
         int x = buffer.getInt();
         int y = buffer.getInt();
         int id = buffer.getInt(); // id must be player.getId()
 
         GameSession session = from.getSession();
         if (session != null) {
             if (session.hasStarted() == false || from.isReady() == false) {
                 sendError(from, Event.NOT_READY_ERROR_CODE + from.getUsername());
             } else {
                 session.setFlag(x, y, from);
             }
         } else {
             sendError(from, Event.NOT_READY_ERROR_CODE + from.getUsername());
             System.err.println("Received set flag from " + from.getUsername() + ", but has no session");
         }
     }
     
     public void receiveInitBoard(Player from) {
         //System.out.println("INIT BOARD EVENT");
         int xSize = buffer.getInt();
         int ySize = buffer.getInt();
         int numMines = buffer.getInt();
         if (from.getSession() != null) {
             from.getSession().initBoard(xSize, ySize, numMines);
         } else {
             sendError(from, Event.SESSION_DOES_NOT_EXITS_ERROR_CODE);
         }
     }
 }
