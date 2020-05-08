 import java.io.*;
 import java.net.*;
 import java.util.Scanner;
 import javax.swing.*;
 
 public class MyClient {
 	String msg;
 	Bomberman b;
 	Player playerMe;
 	Player playerOpp;
 	String playerName;
 	
 	public static void main (String args[]) {
 		new MyClient();
 	}
 	
 	public MyClient() {
 		try {
 			System.out.println("\nClient: Connecting to server...");
 			Socket socket = new Socket("127.0.0.1",8080);
 			
 			MyConnection con = new MyConnection(socket);
 			System.out.println("Client: I connected! ^_^\n");
 			
 			clientThread getThread = new clientThread(con);
 			getThread.start();
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	class clientThread extends Thread {
 		MyConnection con;
 		clientThread(MyConnection con) {
 			this.con = con;
 		}
 		
 		public void run() {
 			try {
 				while(!(msg=con.getMessage()).equals("/quit")) {
 					if (msg.startsWith("/thisisme ")) {
 						playerMe = new Player(msg.substring(10));
 						System.out.println(playerMe.name);
 					} 
 					else if (msg.startsWith("/startpos ")) {
 						int startMe = Integer.parseInt(msg.substring(10));
 						playerMe.startPos = startMe;
 					} 
 					else if (msg.startsWith("/thisisopp ")) {
 						playerOpp = new Player(msg.substring(11));
 						System.out.println(playerOpp.name);
 					} 
 					else if (msg.startsWith("/opponentstartpos ")) {
 						int startOpp = Integer.parseInt(msg.substring(18));
 						playerOpp.startPos = startOpp;
 					} 
 					else if (msg.startsWith("/map ")) {
 						b = new Bomberman(con, playerMe, playerOpp, msg.substring(5));
 						b.setVisible(true);
 						b.setTitle(playerMe.name + " - Bomberman");
 						b.repaint();
 						System.out.println(msg.substring(5));
 					}
 					else if (msg.startsWith("/startGame")) {
 						b.startGame();
 					}
 					else if (msg.startsWith("/playerMoveLeft ")) {
 // 						int newLoc = b.player.moveLeft(b.board, Integer.parseInt(msg.substring(16)));
 // 						JPanel panel = (JPanel) b.board.getComponent(newLoc);
 // 						b.player = new Player("data/"+player.name+"_left.gif");
 // 						panel.add(player);
 // 						b.validate();
 // 						b.repaint();
						if (msg.substring(16,21) == playerMe.name) {
							int newLoc = playerMe.moveLeft(b.board, Integer.parseInt(msg.substring(22)));
 							JPanel panel = (JPanel) b.board.getComponent(newLoc);
							playerMe = new Player("data/" + playerMe.name + "_left.gif");
 							panel.add(playerMe);
 							b.validate();
 							b.repaint();
						} else if (msg.substring(16,21) == playerOpp.name) {
							int newLoc = playerOpp.moveLeft(b.board, Integer.parseInt(msg.substring(22)));
 							JPanel panel = (JPanel) b.board.getComponent(newLoc);
							playerMe = new Player("data/" + playerOpp.name + "_left.gif");
 							panel.add(playerOpp);
 							b.validate();
 							b.repaint();
 						}
 						System.out.println(msg);
 					}
 					else {
 						System.out.println(msg);
 					}
 					msg = "";
 				}
 				if(msg.equals("/quit")) {
 					//c.setVisible(false);
 					System.exit(0);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
