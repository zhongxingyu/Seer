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
 		bombThread bt;
 		clientThread(MyConnection con) {
 			this.con = con;
 		}
 		
 		class bombThread extends Thread {
 			int loc;
 			String name;
 			bombThread(int loc, String name) {
 				this.loc = loc;
 				this.name = name;
 			}
 			public void run() {
 				try {
 					b.updateBomb(loc);
 					Thread.sleep(3000);
 					int bombLen;
 					if(name.equals(playerMe.name)) {
 						b.playerMe.bombCount++;
 						bombLen = b.playerMe.bombLen;
 					}
 					else {
 						b.playerOpp.bombCount++;
 						bombLen = b.playerOpp.bombLen;
 					}
 					b.fire(loc, bombLen);
 					Thread.sleep(2000);
					b.removeBomb(loc, bombLen);
 					Thread.sleep(1000);
 				} catch (Exception e) {
 					
 				}
 			}
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
 						String[] parts = msg.split(" ");
 						b = new Bomberman(con, playerMe, playerOpp, parts[1], parts[2]);
 						b.setTitle(playerMe.name + " - Bomberman");
 						b.repaint();
 						System.out.println(msg.substring(5));
 					}
 					else if( msg.equals("/display") ){
 						b.setVisible(true);
 					}
 					else if (msg.startsWith("/startGame")) {
 						b.startGame();
 					}
 					else if (msg.startsWith("/startTime")) {
 						b.startTime();
 					}
 					else if (msg.startsWith("/playerMoveLeft ")) {
 						System.out.println("LEFT:" + msg.substring(16,21));
 						String[] loc = msg.split(" ");
 						if (msg.substring(16,21).equals(playerMe.name)) {
 							b.updateBoard();
 						} else if (msg.substring(16,21).equals(playerOpp.name)) {
 							b.updateOpponent(Integer.parseInt(loc[2]), 0);
 						}
 						System.out.println(msg);
 					}
 					else if (msg.startsWith("/playerMoveRight ")){
 						System.out.println("RIGHT:" + msg.substring(17,22));
 						String[] loc = msg.split(" ");
 						if (msg.substring(17,22).equals(playerMe.name)) {
 							b.updateBoard();
 						} else if (msg.substring(17,22).equals(playerOpp.name)) {
 							b.updateOpponent(Integer.parseInt(loc[2]), 1);
 						}
 						System.out.println(msg);						
 					}
 					else if (msg.startsWith("/playerMoveUp ")){
 						System.out.println("UP:" + msg.substring(14,19));
 						String[] loc = msg.split(" ");
 						if (msg.substring(14,19).equals(playerMe.name)) {
 							b.updateBoard();
 						} else if (msg.substring(14,19).equals(playerOpp.name)) {
 							b.updateOpponent(Integer.parseInt(loc[2]), 2);
 						}
 						System.out.println(msg);						
 					}
 					else if (msg.startsWith("/playerMoveDown ")){
 						System.out.println("DOWN:" + msg.substring(16,21));
 						String[] loc = msg.split(" ");
 						if (msg.substring(16,21).equals(playerMe.name)) {
 							b.updateBoard();
 						} else if (msg.substring(16,21).equals(playerOpp.name)) {
 							b.updateOpponent(Integer.parseInt(loc[2]), 3);
 						}
 						System.out.println(msg);						
 					}
 					else if (msg.startsWith("/playerBomb ")) {
 						String[] loc = msg.split(" ");
 						int bombLoc = Integer.parseInt(loc[2]);
 						bt = new bombThread(bombLoc, loc[1]);
 						bt.start();
 					}
 					else if (msg.startsWith("/dead ")) {
 						String[] name = msg.split(" ");
 						if(name[1].equals(playerMe.name)) {
 							JOptionPane.showMessageDialog(b, "GAME OVER! YOU DIED :(");
 							playerMe.piece.setVisible(false);
 							playerMe.dead = true;
 						} else {
 							JOptionPane.showMessageDialog(b, "GAME OVER! YOU WIN! :D");
 							playerOpp.piece.setVisible(false);
 							playerOpp.dead = true;
 						}
 					}
 					else {
 						System.out.println(msg);
 					}
 					msg = "";
 				}
 				if(msg.equals("/quit")) {
 					System.exit(0);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
