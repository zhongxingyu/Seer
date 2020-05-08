 package myClasses;
 
 import java.awt.Dimension;
 
 import javax.swing.JFrame;
 
 public class Main {
 	private static GameInstance game;
 	private static JFrame contentPane;
 	private static MenuInterface menu;
 	private static Player winner;
 	private static Options options;
 
 	public static void main(String[] args) {
 		// Instantiate new GUI
 		// InterfaceTest gui = new InterfaceTest();
 
 		// Initialize contentPane
 		contentPane = new JFrame();
 		contentPane.setVisible(true);
 		contentPane.setSize(new Dimension(1120, 700));
 		contentPane.setResizable(false);
 		contentPane.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		contentPane.setLayout(null);
 
 		// Forever until user quit on close
 		while (true) {
 			// Instantiate Menu Gui
 			menu = new MenuGUI(contentPane);
 			
 			// Wait Until Game Ready
 			while (true) {
 				// Cause it doesn't work with out print statement? wtf...
 				System.out.print("");
 				if (menu.isGameReady() == true) {
 					options = menu.getOptions();
 					break;
 				}
 			}
 			
 			while (true) {
 				// Play Game
 				playGame();
 				// Game over so open Menu.
 				menu = new MenuGUI(contentPane, options);
 				// Wait Until Game Ready
 				while (true) {
 					// Cause it doesn't work with out print statement? wtf...
 					System.out.print("");
 					if (menu.isGameReady() == true) {
 						options = menu.getOptions();
 						break;
 					}
 				}
 			}
 			
 			
 		}
 	}
 
 	public static int playGame() {
 		// Initiate Game
 		int gameStatus = -1;
 		game = new GameInstance(options, contentPane);
 
 		// Wait until game done
 		do {
 			// Cause it doesn't work with out print statement? wtf...
 			System.out.print("");
			gameStatus = game.getGameStatus();
 		}while (gameStatus == -1);
 		// Determine how game ended
 		if(gameStatus == 0){
 			// Game ended normally.
 			// Get Winner
 			winner = game.getWinner();
 			// Update Leaderboard
 			updateLeaderboard(winner);
 		} //else Player quit game.		}
 		return gameStatus;
 
 	}
 
 	public static void updateLeaderboard(Player winner) {
 
 	}
 }
