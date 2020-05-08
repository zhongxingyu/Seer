 package com.hawksman.unnamedgame;
 
 import java.util.Scanner;
 
 public class Fight {
 	
 	//Initialize the fight variables
 	public static String fightingChoice;
 	static Scanner sc = new Scanner(System.in);
 	public static Monster monster = new Monster(1);
 	public static Player player = Game.player;
 	public static String monsterChoice;
 	public static String getMonsterChoice;
 	public static int monsterInt;
 	
 	public static void start() {
 		
 		//Print the options for fightable monsters
 		MainGame.display.disp("What type of monster do you want to fight?:");
 		//Display the buttons for the choice of monster
 		MainGame.display.Enable(MainGame.display.warriorM, MainGame.display.fighterM);
 	}
 	
 	public static void getBattleChoice() {
 		
 		//Show the player the move options
 		MainGame.display.disp("What do you want to do?:");
 		MainGame.display.Enable(MainGame.display.attack, MainGame.display.defend, MainGame.display.heal);
 	}
 	
 	public static void battle(int choice) {
 		
 		//Declare variables for the moves of the player and monster
 		boolean monsterHeal = false;
 		boolean monsterDefend = false;
 		boolean monsterAttack = false;
 		boolean playerDefend = false;
 		boolean playerAttack = false;
 		boolean playerHeal = false;
 		
 		//Get the move the monster will make
 		int monsterAi = monster.getAiChoice();
 		
 		//Check what input the player has given
 		if(choice == 1) {
 			//Set the booleans according to the input for attack
 			playerAttack = true;
 			playerDefend = false;
 			playerHeal = false;
 		} else if(choice == 2) {
 			//Set the booleans according to the input for defend
 			playerDefend = true;
 			playerAttack = false;
 			playerHeal = false;
 		} else if(choice == 3) {
 			//Set the booleans according to the input for heal
 			playerAttack = false;
 			playerDefend = false;
 			playerHeal = true;
 		} else {
 			//Set the player not to do anything if the input is wrong
 			playerAttack = false;
 			playerDefend = false;
 			playerHeal = false;
 		}
 		
 		//Link the monster AI choice to a move
 		if(monsterAi == 1) {
 			//Set the booleans according to the AI for attack
 			monsterAttack = true;
 			monsterDefend = false;
 			monsterHeal = false;
 		} else if(monsterAi == 2) {
 			//Set the booleans according to the AI for defend
 			monsterAttack = true;
 			monsterDefend = false;
 			monsterHeal = false;
 		} else if(monsterAi == 3) {
 			//Set the booleans according to the AI for heal
 			monsterAttack = false;
 			monsterDefend = false;
 			monsterHeal = true;
 		}
 		
 		String pFirst = "";
 		String mFirst = "";
 		String mAttack = "";
 		String pAttack = "";
 		String pLife = "";
 		String mLife = "";
 		
 		//Player moves
 		if(playerHeal) {
 			//Heal the player by 10 life
 			player.Heal(10);
 			//Show a message saying the player was healed
 			pFirst = player.name + " healed 10 life! \n";
 		} else if(playerDefend) {
 			//Set the monster not to attack (do damage)
 			monsterAttack = false;
 			//Shows a message that the player has defended
 			pFirst = player.name + " defended and has got 0 damage from " + monster.name + "\n";
 		} else if(!playerAttack && !playerDefend && !playerHeal) {
 			//Show a message that the player did not do anything
 			pFirst = player.name + " did nothing. \n";
 		} 
 		
 		//Monster moves
 		if(monsterHeal) {
 			//heal the monster by 10 life
 			monster.Heal(10);
 			//Show a message that the monster was healed
 			mFirst = (monster.name + " healed 10 life! \n");
 		} else if(monsterDefend) {
 			//Set the player not to attack (do damage)
 			playerAttack = false;
 			//Show a message that the monster has defended
 			mFirst = monster.name + " defended and has got 0 damage from " + player.name + "\n";
 		}
 		
 		//Attack moves
 		if(playerAttack) {
 			//Lower the monsters life by the players power
 			monster.life -= player.strength;
 		} 
 		
 		if(monsterAttack) {
 			//Lower the players life by the monsters power
 			player.life -= monster.strength;
 		}
 		if(playerAttack) {
 			//Show a message that the player has attacked
 			pAttack = player.name + " hit " + monster.name + " for " + player.strength + " damage. \n";
 		}
 		if(monsterAttack) {
 			//Show a message that the monster has attacked
 			mAttack = monster.name + " hit " + player.name + " for " + monster.strength + " damage. \n";
 		}
 		
 		//Show the current life for the player and the monster
 		pLife = player.name + " does now have " + player.life + " life left. \n";
 		mLife = monster.name + " does now have " + monster.life + " life left. \n";
 		
 		//Print the moves message
 		MainGame.display.disp(pFirst + mFirst + pAttack + mAttack + pLife + mLife);
 		
 		//Check if the player is still alive
 		if(player.life <= 0) {
 			
 			//If the player has no life left, show him that he has lost
 			MainGame.display.disp("Too bad! You lost!" + "\n" + "Play again?");
 			
 			//Show the option to play again
 			MainGame.display.Enable(MainGame.display.playAgain);
 		}
 		
 		//Check if the monster is still alive
 		if(monster.life <= 0) {
 			
			MainGame.display.disp("You beat " + monster.name + "! \n" + "You got " + monster.giveXp + " XP!" + "\n" + "You now have " + player.xp + " XP!");
 			player.xp += monster.giveXp;
 			
 			MainGame.display.Enable(MainGame.display.continueStage);
 		}
 		
 		MainGame.display.Enable(MainGame.display.continueFight);
 	
 	}
 
 }
