 package battleship;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.InputMismatchException;
 import java.util.Scanner;
 
 import battleship.Map.MapSize;
 
 /*
  * Minimum:
  * TODO: Comment. Oh THE COMMENT!
  *  
  * TODO: Make the game follow teh rulez
  * [X] boats at least placed 1 square away from each other - fixed for human and computer alike
  * 	[X] Create a way to compare two coordinates
  * 		*** Created the isCoordinate (better name?) functions in coordinate ***
  * 
  * TODO: more rules?
  * 		alternatives:
  * 		[ ] you may fire once per ship in your fleet that has yet to be damaged, or once if all ship has been damaged
  * 		[ ] may fire once per unsunk ship
  * 		[ ] may fire three times per turn, but may not know what shot hit what (like "you fired at (0,0),(1,1),(2,2). You had 2 hits and 1 miss")
  * 		[ ] if you sink a ship you may fire again
  * 
  * TODO: super: Player
  * [X] draw board for players
  * [X] MakeMove - a super one - should probably be the same for both human and computer
  * 		***functions for the human player***
  * 		***computerplayer got a randomized version***
  * 
  * 
  * 
  * TODO: Human Player:
  *	[X] Draw player entered ship on board.
  * 	[X]: Make a move
   	* 	[X] Handle collisions (if we with collision meant "the same player hitting on the same square twice" or something like that)
  * 		[X] Basic textinput (x,y)
  * 			[X] incorrect input should be fixed
  * 			(TODO: Advanced placement w keyarrows)
  * 	  	TODO: Make support for another human player
  * 
  * TODO: Computer player.
  * 	[X] Place ships (random)
  * 
  * 	TODO: Make a move (pure random, peeking or something with memory?)
  * 		[X] random
  * 
  * TODO: Create a functioning game-loop ((preferably one that can be used both locally and over intewebs))
  * 		***should probably not be a static thing. Alpha down below in main.
  * 
  * TODO: Determine winner.
  * TODO: Count misses
  * TODO: Count hits.
  * 	(Keep track of number of # hits and # turns? )
  * 
  * TODO: Design
  *	TODO: Make UML
  * 
  * 
  * If time allows for it:
  * TODO: Add support for more shiptypes
  * TODO: Network-play !!! Will probably have to rewrite a lot !!!
  * TODO: Highscore
  * 	TODO: Store permanently.
  * 		TODO: Encryption
  * 			TODO: Store on server. SSL. Mums!
  * TODO: Graphics
  * 	TODO: Tweak the drawing of game board for better placements.
 
  */
 
 
 /**
  * Battleship, the game based on the movie with the same name.
  * 
  * @author Victor,Wiktor
  *
  */
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		int size = 10;
 		int nofHumanPlayers = 0;
 		int nofComputerPlayers = 0;
 		int computerSkill = 0; // Easy = 1, Medium = 2, Hard = 3. Set indivudually for each computer opponent?
 		
 		 
 		
 		Player player1 = new ComputerPlayer("HAL");
 		Player player2 = new ComputerPlayer("BMO");
		System.out.println("  ____        _   _   _       __ _      _     _ ");
		System.out.println(" | __ )  __ _| |_| |_| | ___ / _(_) ___| | __| |");
		System.out.println(" |  _ \\ / _` | __| __| |/ _ \\ |_| |/ _ \\ |/ _` |");
		System.out.println(" | |_) | (_| | |_| |_| |  __/  _| |  __/ | (_| |");
		System.out.println(" |____/ \\__,_|\\__|\\__|_|\\___|_| |_|\\___|_|\\__,_|");
		System.out.println("  ");
 		boolean quit = false;
 		while(!quit){
 			System.out.println("1. Ändra storlek på spelplan. Nuvarande: " + size);
 			System.out.println("2. Ändra antal mänskliga spelare. Nuvarande: "+ nofHumanPlayers);
 			System.out.println("3. Ändra antal datorspelare. Nuvarande: " + nofComputerPlayers);
 			System.out.println("4. Lägg till skepp. Nuvarande: NA");
 			System.out.println("5. Lista skepp. Nuvarande: NA");
 			System.out.println("6. Se highscore.");
 			System.out.println("7. Starta spelet!");
 			System.out.println("8. Avsluta.");
 			System.out.println("Ange ditt val som ett heltal, bekräfta med enter:");
 			
 			try{
 				Scanner scanner = new Scanner(System.in);
 				int inNumber = scanner.nextInt();
 				//scanner.close(); // Will close close ALL system.in reading??
 				switch (inNumber) {
 				case 1:
 					System.out.println("Ange ny storlek på spelplan: ");
 					try{
 						// Need local instance of scanner so that the main-scanner won't 
 						// think of this input as an unvalid menu-option
 						Scanner intScanner = new Scanner(System.in);
 						size = intScanner.nextInt();
 						//intScanner.close();
 					}
 					catch(InputMismatchException err){
 						System.out.println("Du måste ange ett giltigt nummer, var god försök igen.");
 					}
 					break;
 				case 2:
 					System.out.println("Ange antal mänskliga spelare: ");
 					try{
 						Scanner intScanner = new Scanner(System.in);
 						nofHumanPlayers = intScanner.nextInt();
 					}
 					catch(InputMismatchException err){
 						System.out.println("Du måste ange ett giltigt nummer, var god försök igen.");
 					}
 					break;
 				case 3:
 					System.out.println("Ange antal datorspelare: ");
 					try{
 						Scanner intScanner = new Scanner(System.in);
 						nofComputerPlayers = intScanner.nextInt();
 					}
 					catch(InputMismatchException err){
 						System.out.println("Du måste ange ett giltigt nummer, var god försök igen.");
 					}
 					break;
 				case 4:
 					System.out.println("Lägg till skepp. NOT IMPLEMENTED!!");
 					break;
 				case 5:
 					System.out.println("Visa skepp. NOT IMPLEMENTED!!");
 					break;
 				case 6:
 					System.out.println("Visa highscore. NOT IMPLEMENTED!!");
 					break;
 				case 7:
 					// Non-interactive way of starting the game. Just does not care about 
 					// menu-options..
 					GameLoop game = new GameLoop(size, player1, player2);
 					break;
 				case 8:
 					System.out.println("Tråkigt att du vill avsluta, bye!");
 					quit = true;
 					break;
 					
 				// Wrong menu-option (as integer)
 				default:
 					System.out.println("Du skrev in i ett ogiltigt menyval. Försök igen!");
 					break;
 				}
 			}
 			catch(InputMismatchException err){
 				// Not a number entered as menu-option
 				System.out.println("Du måste ange ett val som ett heltal. Försök igen!");
 			}
 			
 		}
 		
 	}
 	
 	
 
 }
