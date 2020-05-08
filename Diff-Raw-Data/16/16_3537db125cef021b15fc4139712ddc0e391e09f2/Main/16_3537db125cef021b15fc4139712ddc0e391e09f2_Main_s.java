 package edu.ntnu.brunson.manager;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import edu.ntnu.brunson.cards.Deck;
 import edu.ntnu.brunson.cards.Pile;
 import edu.ntnu.brunson.player.AIPlayer;
 import edu.ntnu.brunson.player.Phase1Player;
 import edu.ntnu.brunson.player.Phase2Player;
 import edu.ntnu.brunson.preflop.*;
 import java.io.PrintStream;
 import java.io.File;
 
 public class Main {
 
 	private Main(){};
 	
 	private void run(){
 		GameManager gameManager = new GameManager();
 		try{
			AIPlayer.loadPFT(6, "preflop2.txt");
 		}
 		catch(Exception e) {
 			System.exit(1);
 		}
 		gameManager.addPlayer(new Phase1Player(100, 50, 75, 50));
 		gameManager.addPlayer(new Phase1Player(100, 50, 75, 50));
 		gameManager.addPlayer(new Phase1Player(100, 50, 75, 50));
 		gameManager.addPlayer(new Phase1Player(100, 50, 75, 50));
 		gameManager.addPlayer(new Phase1Player(100, 50, 75, 50));
		gameManager.addPlayer(new Phase2Player("Bob", 100, 50, 75, 50));
 		gameManager.playGames(1500);
 	}
 	
 	public static void main(String[] args) {
 		Main main = new Main();
 		main.run();
 	}
 }
