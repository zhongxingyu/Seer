 package com.hawksman.unnamedgame;
 
 
 public class Game {
 	
 	//Initialize the Game variables
 	public static Player player;
 	
 	//Ask the player for a name and set the standard stats
 	public static void play() {
 		
 		//Asks the player for his name
 		MainGame.display.disp("What is your name?: ");
 		MainGame.display.DisAll();
 		MainGame.display.nameField.setVisible(true);
 		MainGame.display.nameField.setEnabled(true);
 	
 	}
 	
 	//Show the help menu
 	public static void showHelp() {
 		
 		//Prints the help menu and then goes back to the game
 		String help = " -------------------" +
 						"\n" +
 						" Help:" +
 						"\n" +
 						" Fight!!" +
 						"\n" +
 						"-------------------";
 		
 		MainGame.display.disp(help);
 		MainGame.display.Enable(MainGame.display.backToMain);
 		
 	}
 	
 	//Ask if the player wants to fight, exit of check his/her level
 	public static void nextStage() {
 		
 		MainGame.display.disp("What do you want to do?");
 		
		MainGame.display.Enable(MainGame.display.fight, MainGame.display.level, MainGame.display.mainExit);
 	}
 
 }
