 package main;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.net.SocketException;
 import java.sql.SQLException;
 
 public class AcceptPlayerCoordinates {
 	public static void acceptCoordinates(ObjectInputStream inputStream) throws SQLException, ClassNotFoundException {
 		System.out.println("Accepting player coordinates..");
 		try {
 			int counter = 0;
 			// not actually player's name but infact the listPosition is passed
 			// as second parameter (hack of course)
 			while (true) {
 				Player player = (Player) inputStream.readObject();
 				int position = Integer.parseInt(player.getName());
 				// System.out.println("Received updates from client..");
 
 				synchronized (Player.onlinePlayers) {
					// DB transaction to update player
 					if (Player.onlinePlayers.size() > position) {
 						Player.onlinePlayers.get(position).update(player.getX(), player.getY());
 						if (counter >= 100) { // DB transaction is more costly -- do it infrequently
 							DatabaseHandler.updateCoordinates(player.getId(), player.getX(), player.getY());
 							counter = 0;
 						}
 						counter++;
 					}
 					else if (Player.onlinePlayers.get(position-1).getId() == player.getId()) {
						Player.onlinePlayers.get(position).update(player.getX(), player.getY());
 						if (counter >= 100) { // DB transaction is more costly -- do it infrequently
 							DatabaseHandler.updateCoordinates(player.getId(), player.getX(), player.getY());
 							counter = 0;
 						}
 						counter++;
 					}
 					else {
 						System.out.println("OUT OF BOUNDS! Position: " + position + " List size: " + Player.onlinePlayers.size());
 						return; // prevent out of bounds (player index not in list or moved)
 					}
 				}
 			}
 		}
 		catch (SocketException e) { // read failed because client closed connection
 			return;
 		}
 		catch (IOException e) { // read failed because client closed connection
 			return;
 		}
 	}
 }
