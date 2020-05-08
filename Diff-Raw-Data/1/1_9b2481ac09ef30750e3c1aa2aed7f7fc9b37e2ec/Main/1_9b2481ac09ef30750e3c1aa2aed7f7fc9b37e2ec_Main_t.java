 package myClasses;
 
 import java.awt.Dimension;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.swing.JFrame;
 
 public class Main {
 	private static GameInstance game;
 	private static JFrame contentPane;
 	private static MenuInterface menu;
 	private static Player winner;
 	private static Options options;
 	private static String lbLoc = "src/files/leaderboard.txt";
 
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
 		
 		while(gameStatus != 0 && gameStatus != 1){
 			// Wait until game done
 			do {
 				// Cause it doesn't work with out print statement? wtf...
 				System.out.print("");
 				gameStatus = game.getGameStatus();
 			} while (gameStatus == -1);
 			// Determine how game ended
 			if (gameStatus == 0 || gameStatus == 3) {
 				// Game ended normally.
 				// Get Winner
 				winner = game.getWinner();
 				// Update Leaderboard
 				updateLeaderboard(winner);	
 			} 
 			if(gameStatus == 2 || gameStatus == 3){
 				gameStatus = -1;
 				game = new GameInstance(options, contentPane);
 			} //else Player quit game. 
 		}
 		return gameStatus;
		
 	}
 
 	public static void updateLeaderboard(Player winner) {
 		// Variables
 		FileReader fr;
 		FileWriter fw;
 		BufferedReader br;
 		PrintWriter pw;
 		int lbLength;
 		int winnerPos = 0;
 		boolean winPosFound = false;
 		String[] data;
 
 		try {
 			fr = new FileReader(lbLoc);
 		} catch (FileNotFoundException e) {
 			fr = null;
 			System.out.println("File Not Read.... Incorrect File Name");
 		}
 		br = new BufferedReader(fr);
 		try {
 			lbLength = Integer.parseInt(br.readLine());
 		} catch (NumberFormatException e) {
 			System.out.println("Number Format Incorrect in Main");
 			lbLength = 0;
 		} catch (IOException e) {
 			System.out.println("Error with Number of Entries");
 			lbLength = 0;
 		}
 
 		// Run through the entries in Text File & Check against Winner Num Turns
 		data = new String[lbLength];
 		for (int i = 0; i < lbLength; i++) {
 			try {
 				data[i] = br.readLine();
 			} catch (IOException e) {
 				System.out.println("Could not read line Number: " + (i + 1));
 				data[i] = null;
 			}
 
 			String[] line;
 			if (data[i] != null) {
 				// Check to see if player has less turns than players in data
 				line = data[i].split(",");
 				if (Integer.parseInt(line[1]) > winner.getNumMoves() && !winPosFound) {
 					// If less moves
 					winnerPos = i;
 					winPosFound = true;
 				}
 			}
 		}
 		try {
 			fr.close();
 			br.close();
 		} catch (IOException e) {
 			System.out.println("Could not Close File after Reading");
 		}
 		
 		// Write back to text file
 		try {
 			fw = new FileWriter(lbLoc);
 		} catch (IOException e) {
 			fw = null;
 			System.out.println("File Not Read.... Incorrect File Name");
 		}
 		pw = new PrintWriter(fw);
 		pw.println(lbLength + 1);
 		for (int i = 0; i < lbLength + 1; i++) {
 			if (i < winnerPos) {
 				pw.println(data[i]);
 			} else if (i == winnerPos) {
 				pw.println(winner.getName().toUpperCase() + "," + winner.getNumMoves());
 			} else {
 				pw.println(data[i-1]);
 			}
 		}
 		try {
 			fw.close();
 		} catch (IOException e) {
 			System.out.println("Could not Close File after Writing");
 		}
 	}
 	
 }
