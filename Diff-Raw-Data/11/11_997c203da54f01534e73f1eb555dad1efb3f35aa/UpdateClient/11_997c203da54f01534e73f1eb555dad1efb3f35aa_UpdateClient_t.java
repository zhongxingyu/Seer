 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.net.SocketException;
 import java.sql.SQLException;
 
 public class UpdateClient {
 
 	public static void sendOnlinePlayers(Socket connection, BufferedOutputStream bos) throws InterruptedException, SQLException {
 		int position = 0;
 		int id = GameServer.getPlayerId();
 		try {
 			ObjectOutputStream outputStream = new ObjectOutputStream(bos);
 			while (true) {
 				// 3 second pause between updates
 				Thread.sleep(3000);
 				
 				position = Player.getPosition(id);
 				// player has to actually be in the ArrayList
 				if (position < 0) {
 					System.exit(1); // should never get here
 				}
 				System.out.println("Player ID: " + id + " is at position: " + position);
 				
 				// write player position in list to client
				outputStream.writeInt(position);
				outputStream.flush();
 				outputStream.reset();
 				outputStream.writeObject(Player.onlinePlayers);
 				outputStream.flush();
 			}
 		}
 		catch (SocketException e) {
 			try {
 				// hack .. fix this if possible 
 				// throws socket exception indicating closed/lost connection from client
 				System.out.println("\nPlayer " + id + " has disconnected.");
 				DatabaseHandler.removeOnline(id, position);
 				connection.shutdownOutput();
 				connection.close();
 			} catch (IOException e1) {
 			}
 			
 		}
 		catch (IOException ioe) {
 		}
 	}
 }
