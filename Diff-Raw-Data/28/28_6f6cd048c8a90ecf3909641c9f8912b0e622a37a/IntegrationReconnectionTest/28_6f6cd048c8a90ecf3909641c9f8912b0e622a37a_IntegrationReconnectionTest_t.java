 package edu.berkeley.cs.cs162.Test;
 
 import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Vector;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.junit.Test;
 
 import edu.berkeley.cs.cs162.Server.BoardLocation;
 import edu.berkeley.cs.cs162.Server.ClientLogic;
 import edu.berkeley.cs.cs162.Server.DatabaseConnection;
 import edu.berkeley.cs.cs162.Server.Game;
 import edu.berkeley.cs.cs162.Server.GameServer;
 import edu.berkeley.cs.cs162.Server.GoBoard;
 import edu.berkeley.cs.cs162.Server.GoBoard.IllegalMoveException;
 import edu.berkeley.cs.cs162.Server.PlayerLogic;
 import edu.berkeley.cs.cs162.Server.Security;
 import edu.berkeley.cs.cs162.Server.ServerStateManager;
 import edu.berkeley.cs.cs162.Server.StoneColor;
 import edu.berkeley.cs.cs162.Synchronization.ReaderWriterLock;
 import edu.berkeley.cs.cs162.Writable.ClientInfo;
 import edu.berkeley.cs.cs162.Writable.Message;
 import edu.berkeley.cs.cs162.Writable.MessageFactory;
 import edu.berkeley.cs.cs162.Writable.MessageProtocol;
 
 public class IntegrationReconnectionTest {
 	protected static final int TEST_PORT = 1234;
 	protected static final String password = "plainTextPassword";
 	protected static final String hash = Security.computeHash(password);
 	protected static final String salt = "cs162project3istasty";
 	protected static final String database_name = "integration-reconnection-test.db";
 	
 	protected static final ClientInfo cinfo = MessageFactory.createClientInfo("TestPlayer", MessageProtocol.TYPE_MACHINE);
	private static final int NUM_GAMES = 50;
 	@Test
 	public void test() throws InterruptedException, IOException, SQLException, IllegalMoveException {
 		try {
 			//create the database and entries.
 			DatabaseConnection database_connection = new DatabaseConnection(database_name);
 			database_connection.wipeDatabase();
 			ServerStateManager sm = new ServerStateManager(database_connection);
 			for (int i = 0; i <NUM_GAMES; i++) {
 				String p1 = "TestPlayer"+ (i*2);
 				String p2 = "TestPlayer"+ (i*2+1);
 				
 				String c1 = "insert into clients (name, type, passwordHash) values ('"+ p1 +"', " 
 						+ MessageProtocol.TYPE_MACHINE + ", '" + Security.computeHashWithSalt(hash, salt) + "')";
 				String c2 = "insert into clients (name, type, passwordHash) values ('"+ p2 + "', " 
 						+ MessageProtocol.TYPE_MACHINE + ", '" + Security.computeHashWithSalt(hash, salt) + "')";
 				database_connection.initializeDatabase();
 				database_connection.startTransaction();
 				int p1id = database_connection.executeWriteQuery(c1);
 				int p2id = database_connection.executeWriteQuery(c2);
 				database_connection.finishTransaction();
 				assertTrue(p1id != -1);
 				assertTrue(p2id != -1);
 				PlayerLogic p1l = (PlayerLogic)ClientLogic.getClientLogicForClientType(null, p1, MessageProtocol.TYPE_MACHINE, null);
 				PlayerLogic p2l = (PlayerLogic)ClientLogic.getClientLogicForClientType(null, p2, MessageProtocol.TYPE_MACHINE, null);
 				p1l.setID(p1id);
 				p2l.setID(p2id);
 				
 				GoBoard board = new GoBoard(10);
 				Game currentGame = new Game("GN" + i, p1l, p2l, board);
 				currentGame.setGameID(sm.createGameEntry(currentGame));
 				
 				for (int j = 0; j < i+1; j++) {
 					int x = j%10;
 					int y = j/10;
 					board.makePassMove(StoneColor.BLACK);
 					sm.updateGameWithPass(currentGame, currentGame.getBlackPlayer());
 					Vector<BoardLocation> captured = board.makeMove(new BoardLocation(x, y), StoneColor.WHITE);
 					sm.updateGameWithMove(currentGame, currentGame.getWhitePlayer(), new BoardLocation(x, y), captured);
 				}
 			}
 			database_connection.close();
 		} catch (SQLException e) {
 			assertFalse(e.getMessage(), true);
 		}
 		
 		final GameServer server = new GameServer(database_name, 100, 5, new PrintStream(System.out));
 		
 		Thread t = new Thread() {
 			public void run() {
 				try {
 					server.waitForConnectionsOnPort(TEST_PORT, InetAddress.getByName("localhost"));
 				} catch (UnknownHostException e) {
 					throw new AssertionError(e);
 				}
 			}
 		};
 		
 		
 		t.start();
 		while (!server.isReady()) {
 			Thread.sleep(10);
 		}
 		
 		ReaderWriterLock lock = new ReaderWriterLock();
 		List<Thread> threads = new ArrayList<Thread>();
 		AtomicInteger sharedCount = new AtomicInteger(0);
 		
 		for (int i = 0; i < (NUM_GAMES * 2); i++)
 		{
 			List<Message> moves = Arrays.asList(MessageFactory.createGetMoveStatusOkMessage(MessageProtocol.MOVE_PASS, MessageFactory.createLocationInfo(0, 0)));
 			System.out.println(">>>> STARTING TestPlayer"+ i + ".");
 			threads.add(TestPlayer.runInstance("TestPlayer"+i, MessageProtocol.TYPE_MACHINE, moves, TEST_PORT, lock, sharedCount));
 		}
 		for (Thread thread : threads) {
 			thread.join();
 		}
 		
 		Thread.sleep(100);
 		
         try {
 			DatabaseConnection database_connection = new DatabaseConnection("integration-auth-test.db");
 			String getUnfinishedGamesQuery = "SELECT * FROM games WHERE winner IS NULL";
 			
 	        ResultSet results = database_connection.executeReadQuery(getUnfinishedGamesQuery);
 	        //there should be no unfinished games.
 	        assertFalse(results.next());
 
 	        database_connection.closeReadQuery(results);
 	        
 			database_connection.close();
 		} catch (SQLException e) {
 			assertFalse(e.getMessage(), true);
 		}
 	}
 }
