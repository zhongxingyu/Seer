 package pl.edu.agh.to1.dice.logic;
 
 import java.util.Scanner;
 
 public class ConsoleGameConfigurator implements GameConfigurator {
 	private GameBuilder gameBuilder = new GameBuilder();
 	private Scanner scanner = new Scanner(System.in);
 	public DiceGame loadConfiguration() {
 		System.out.println("Welcome to DiceGame!");
 		DiceGame game = null;
 		printHelp();
 		String cmd;
 		do {
 			cmd = scanner.next();
 			if(cmd.equals("player")) {
 				gameBuilder.addPlayer(scanner.next());
 			} else if(cmd.equals("bot")) {
 				gameBuilder.addBot(scanner.next());
			} else if(cmd.equals("normal")) {
				gameBuilder.setScoresPerCategory(gameBuilder.getDefaultScoresPerCategory());
			} else if(cmd.equals("triple")) {
				gameBuilder.setScoresPerCategory(3);
 			} else if(cmd.equals("start")) {
 				game = gameBuilder.create();
 			} else if(cmd.equals("-h")) {
 				printHelp();
 			} else {
 				System.out.println("Unsupported operation! Type -h to get help.");
 			}
 		} while(!cmd.equals("start"));
 
 		return game;
 	}
 
 	private void printHelp() {
 		System.out.println("player player_name - adds a player with player_name name");
 		System.out.println("bot bot_name - adds a bot with bot_name name");
		System.out.println("normal - set normal dice game (default)");
		System.out.println("triple - set triple dice game");
 		System.out.println("start - to start the game.");
 	}
 
 }
