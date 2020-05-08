 package edu.berkeley.cs.cs162.Test;
 
 import static org.junit.Assert.*;
 
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Collections;
 import java.util.Random;
 import java.util.Vector;
 
 import org.junit.Test;
 
 import edu.berkeley.cs.cs162.Client.HumanPlayer;
 import edu.berkeley.cs.cs162.Server.Board;
 import edu.berkeley.cs.cs162.Server.BoardLocation;
 import edu.berkeley.cs.cs162.Server.ClientConnection;
 import edu.berkeley.cs.cs162.Writable.BoardInfo;
 import edu.berkeley.cs.cs162.Writable.ClientInfo;
 import edu.berkeley.cs.cs162.Writable.ClientMessages;
 import edu.berkeley.cs.cs162.Writable.GameInfo;
 import edu.berkeley.cs.cs162.Writable.Location;
 import edu.berkeley.cs.cs162.Writable.Message;
 import edu.berkeley.cs.cs162.Writable.MessageFactory;
 import edu.berkeley.cs.cs162.Writable.MessageProtocol;
 import edu.berkeley.cs.cs162.Writable.ResponseMessages;
 
 /**
  * Harvey C
  */
 
 public class HumanPlayerTest {
 	@Test
 	public void test() throws IOException {
 		final int TEST_PORT = 1235;
 		final String TEST_NAME = "TestHuman";
 		final String TEST_GAME_NAME = "TestGame";
 
 		BoardInfo boardInfo = MessageFactory.createBoardInfo(new Board(9));
 		GameInfo gameInfo = MessageFactory.createGameInfo(TEST_GAME_NAME);
 		ClientInfo blackPlayerInfo = MessageFactory.createHumanPlayerClientInfo("BlackPlayer");
 		ClientInfo whitePlayerInfo = MessageFactory.createHumanPlayerClientInfo("WhitePlayer");
 		
 		ServerSocket server = new ServerSocket(TEST_PORT);
 		Thread hThread = new Thread() {
 			public void run()
 			{
 				HumanPlayer.main(new String[] {"localhost",String.valueOf(TEST_PORT),TEST_NAME});
 			}
 		};
 		hThread.start();
 		
 		ClientConnection connection;
 		{
 			Socket c1 = server.accept();
 			c1.setSoTimeout(3000);
 			Socket c2 = server.accept();
 			c2.setSoTimeout(3000);
 			int syn_id = (new DataInputStream(c1.getInputStream())).readInt();
 			int syn_id2 = (new DataInputStream(c2.getInputStream())).readInt();
 			assertEquals(syn_id,syn_id2);
 			connection = new ClientConnection(c1, c2, syn_id);
 		
 		}
 		
 		connection.receive3WayHandshake(new Random());
 		Message connectMsg = connection.readFromClient();
 		assertEquals(connectMsg.getMsgType(), MessageProtocol.OP_TYPE_CONNECT);
 		
 		ClientInfo cInfo = ((ClientMessages.ConnectMessage)connectMsg).getClientInfo();
 		assertEquals(cInfo.getName(), TEST_NAME);
 		assertEquals(cInfo.getPlayerType(), MessageProtocol.TYPE_HUMAN);
 		System.out.println("Connect To Server Worked");
 		
 		//Wait for game
 		Message connected = MessageFactory.createStatusOkMessage();
 		connection.sendReplyToClient(connected);
 		Message waitMessage = connection.readFromClient();
 		assertEquals(waitMessage.getMsgType(), MessageProtocol.OP_TYPE_WAITFORGAME);
 		System.out.println("Wait For Game Worked");
 		connection.sendReplyToClient(MessageFactory.createStatusOkMessage());
 //		ClientMessages.JoinMessage joinMessage = (ClientMessages.JoinMessage) connection.readFromClient();
 //		assertEquals(joinMessage.getGameInfo().getName(), TEST_GAME_NAME);
 
 		
 		
 		//Game start 
 		Message gameStartMsg = MessageFactory.createGameStartMessage(gameInfo, boardInfo, cInfo, whitePlayerInfo);
 		connection.sendToClient(gameStartMsg);
 		assertTrue(connection.readReplyFromClient(gameStartMsg).isOK());
 		System.out.println("Game Start Worked");
 		
 		
 		//Get move and stone moved response
 		Message getMoveMsg = MessageFactory.createGetMoveMessage();
 		connection.sendToClient(getMoveMsg);
 //		Message movedMsg = connection.readFromClient();
 		System.out.println("Please enter location waka waka waka");
 		Message moved = connection.readReplyFromClient(getMoveMsg);
 	    ResponseMessages.GetMoveStatusOkMessage movedMsg = (ResponseMessages.GetMoveStatusOkMessage) moved;
 		Location loc = movedMsg.getLocation();
 		assertTrue(movedMsg.isOK());
 		assertEquals(movedMsg.getMsgType(), MessageProtocol.OP_STATUS_OK);
 		assertEquals(movedMsg.getMoveType(), MessageProtocol.MOVE_STONE);
 		System.out.println("Get Move Response Worked");
 		
 		
 		//Make move and response	
 		Vector<BoardLocation> boardPieces = new Vector<BoardLocation>();
 		BoardLocation bLoc = loc.makeBoardLocation();
 		Message makeMoveMsg = MessageFactory.createMakeMoveMessage(gameInfo, cInfo, movedMsg.getMoveType(), bLoc, boardPieces);
         connection.sendToClient(makeMoveMsg);
         Message makeMoveReply = connection.readReplyFromClient(makeMoveMsg);
         assertTrue(makeMoveReply.isOK());
 //		connection.sendReplyToClient(MessageFactory.createMakeMoveMessage(gameInfo, cInfo, MessageProtocol.MOVE_STONE, bLoc,  boardPieces));
 //		Message okResp = connection.readFromClient();
 //		assertEquals(okResp.getMsgType(), MessageProtocol.OP_STATUS_OK);
 		System.out.println("Make Move Response Worked");
 		
 		
 		//Get move and pass response
 		Message newGetMoveMsg = MessageFactory.createGetMoveMessage();
 		connection.sendToClient(newGetMoveMsg);
 		System.out.println("Please make a pass");
 		Message movedPass = connection.readReplyFromClient(newGetMoveMsg);
 		ResponseMessages.GetMoveStatusOkMessage movedPassMsg = (ResponseMessages.GetMoveStatusOkMessage) movedPass;
 		Location nullLoc = movedPassMsg.getLocation();		
		assertEquals(movedPassMsg.getMoveType(), MessageProtocol.MOVE_PASS);
 		System.out.println("Pass Move Worked");
 		bLoc = nullLoc.makeBoardLocation();
 		connection.sendReplyToClient(MessageFactory.createMakeMoveMessage(gameInfo, cInfo, MessageProtocol.MOVE_PASS, bLoc,  boardPieces));
 		
 		
 		//Game over and response		
 	    Message gameOverMsg = MessageFactory.createGameOverMessage(gameInfo, 1.0, 0.5, cInfo);
 	    connection.sendToClient(gameOverMsg);
 	    assertTrue(connection.readReplyFromClient(gameOverMsg).isOK());
 	    System.out.println("Game Over Worked");
 	    
 	    
 	    //Wait for next game
 	    Message waitForNextGameMsg = connection.readFromClient();
 	    assertEquals(MessageProtocol.OP_TYPE_WAITFORGAME, waitForNextGameMsg.getMsgType());
 	    System.out.println("Wait For Next Game Worked");
 	    
 	}
 
 }
