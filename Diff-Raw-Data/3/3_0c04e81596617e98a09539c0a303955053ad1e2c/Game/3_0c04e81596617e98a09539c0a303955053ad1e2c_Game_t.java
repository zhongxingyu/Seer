 package Cluedo;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.InputMismatchException;
 import java.util.List;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.Set;
 
 import board.*;
 
 public class Game {
 
 	private Board board;
 	private Player currentPlayer;
 	private int currentPlayerInt;
 	private int numPlayers = 0;
 	private int playersLeft;
 	private int numDice = 2;
 	private Random rand;
 	private List<Card> remainingCards;
 	private List<Card> leftoverCards;
 	private List<Player> players;
 	private Scanner input;
 	private Solution solution;
 	private boolean playing;
 	private boolean canSuggest;
 	private boolean canAccuse;
 	private boolean canUsePassage;
 
 	public Game(){
 		// Initialise fields
 		this.rand = new Random();
 		this.players = new ArrayList<Player>();
 		this.remainingCards = new ArrayList<Card>();
 		this.leftoverCards = new ArrayList<Card>();
 
 		makeCards();
 		makeSolution();
 		input = new Scanner(System.in);
 		while(this.numPlayers < 3 || this.numPlayers > 6){
 			System.out.println("How many players? (3 to 6):");
 			try{
 				this.numPlayers = input.nextInt();
 			} catch (InputMismatchException e){
 				this.numPlayers =-1;
 			}
 			this.playersLeft = this.numPlayers;
 			input.nextLine();
 		}
 		makePlayers(this.numPlayers);
 		this.board = new Board(players);
 		this.board.createBoard();
 
 		deal();
 
 		currentPlayerInt = 0;
 		currentPlayer = players.get(currentPlayerInt);
 
 		play();	
 	}
 
 	/**
 	 * Set up the game and start main loop
 	 */
 	public void play(){
 		this.playing = true;
 		// Main loop
 		while(playing){
 			boolean done = false;
 			// First check if everyone is out. If everyone is out, end the game.
 			if(this.playersLeft == 0){
 				System.out.println("Everyone lost.");
 				this.playing = false;
 				break;
 			}
 			// This while loop represents one turn
 			int movesLeft = roll();
 			System.out.println(currentPlayer+"'s turn to move. "+currentPlayer+" rolled a "+movesLeft);
 			while(!done){
 				// Movement phase
 				while(movesLeft > 0){
 					System.out.println(board.toString());
 					String inRoom = board.getRoom(currentPlayer);
 					if(inRoom!=null){
 						System.out.println(currentPlayer+" entered the "+inRoom);
 					}
 					System.out.println("(N)orth, (E)ast, (S)outh, or (W)est? (F) to finish moving. "+movesLeft+" moves left.");
 					String dir = input.nextLine().trim().toLowerCase();
 					if(dir.equals("f")){ movesLeft = 0; }
 					else if(move(dir)){
 						movesLeft--;
 					}
 					else if(!move(dir)){
 						System.out.println("Can't move in that direction or invalid input.");
 					}
 				}
 				// Suggest/Accuse/End turn
 				System.out.println(printOptions());
 				String choice = input.nextLine().trim().toLowerCase();
 				if(choice.equals("e")){ 
 					endTurn();
 					done = true;
 				}
 				else if(choice.equals("v")){
 					System.out.println("Your cards:\n"+currentPlayer.handToString());
 				}
 				else if(choice.equals("s") && canSuggest){
 					makeSuggestion();
 					endTurn();
 					done = true;
 				}
 				else if(choice.equals("a") && canAccuse){
 					makeAccusation();
 					endTurn();
 					done = true;
 				}
 				else if(choice.equals("p") && canUsePassage){
 					board.useSecretPassage(currentPlayer);
 					endTurn();
 					done = true;
 				}
 				else{
 					System.out.println("Invalid choice.");
 				}
 			}
 		}
 		System.out.println("Game over.");
 		input.close();
 	}
 	/**
 	 * Builds a String of your available options at the end of a turn.
 	 * @return
 	 */
 	public String printOptions(){
 		StringBuilder opts = new StringBuilder();
 		opts.append("(V)iew hand, ");
 		String currentRoom = board.getRoom(currentPlayer);
 		if(currentRoom != null){
 			opts.append("Make a (S)uggestion, ");
 			this.canSuggest = true;
 			if(currentRoom.equals("Pool")){
 				opts.append("Make an (A)ccusation, ");
 				this.canAccuse = true;
 			}
 		}
 		if(board.canUseSecretPassage(currentPlayer)){
 			opts.append("Use the secret (P)assage, ");
 			this.canUsePassage = true;
 		}
 		opts.append("(E)nd turn.");
 		return opts.toString();
 	}
 	/**
 	 * Make a suggestion.
 	 * Suggestion is then checked against other players' hands and refuted if they have a matching card.
 	 */
 	public void makeSuggestion(){
 		int choice = -1;
 		while(choice < 0 || choice >= characterNames.length){
 			System.out.println("Choose a character:\n"+charactersString);
 			choice = input.nextInt()-1;
 		}
 		Character sugChar = new Character(characterNames[choice]);
 		choice = -1;
 		Room sugRoom = new Room(board.getRoom(currentPlayer));
 		while(choice < 0 || choice >= weaponNames.length){
 			System.out.println("Choose a weapon:\n"+weaponsString);
 			choice = input.nextInt()-1;
 		}
 		Weapon sugWeap = new Weapon(weaponNames[choice]);
 		Suggestion suggestion = new Suggestion(sugChar, sugRoom, sugWeap);
 		board.evaluateSuggestions(currentPlayer, suggestion);
 
 		// Refuting: just check if card is still in remainingCards as cards are only removed from there to be put into the solution
 		if(remainingCards.contains(sugChar)){
 			System.out.println("Your suggestion was refuted: "+sugChar+" was not the murderer");
 		} else if(remainingCards.contains(sugRoom)){
 			System.out.println("Your suggestion was refuted: The murder didn't occur in "+sugRoom);
 		} else if(remainingCards.contains(sugWeap)){
 			System.out.println("Your suggestion was refuted: "+sugWeap+" wasn't the murder weapon");
 		}
 	}
 	/**
 	 * Make an accusation.
 	 * Accusation is then checked against other player's hands, 
 	 * currentPlayer is removed from game if the accusation is refuted.
 	 */
 	public void makeAccusation(){
 		int choice = -1;
 		while(choice < 0 || choice >= characterNames.length){
 			System.out.println("Choose a character:\n"+charactersString);
 			choice = input.nextInt()-1;
 		}
 		Character accChar = new Character(characterNames[choice]);
 		choice = -1;
 		while(choice < 0 || choice >= roomNames.length){
 			System.out.println("Choose a room:\n"+roomsString);
 			choice = input.nextInt()-1;
 		}
 		Room accRoom = new Room(roomNames[choice]);
 		choice = -1;
 		while(choice < 0 || choice >= weaponNames.length){
 			System.out.println("Choose a weapon:\n"+weaponsString);
 			choice = input.nextInt()-1;
 		}
 		Weapon accWeap = new Weapon(weaponNames[choice]);
 		Accusation accusation = new Accusation(accChar, accRoom, accWeap);
 		if(solution.checkAccusation(accusation)){
 			System.out.println(currentPlayer+" wins! The solution was:\n"+solution.toString());
 			endTurn();
 			playing = false;
 		} else {
 			System.out.println(currentPlayer+"'s accusation was wrong. "+currentPlayer+" is out of the game!");
 			currentPlayer.lose();
 			playersLeft--;
 			endTurn();
 		}
 	}
 	/**
 	 * Resets fields for suggesting/accusing and moves to next player
 	 */
 	public void endTurn(){
 		canSuggest = false;
 		canAccuse = false;
 		canUsePassage = false;
 		if(currentPlayerInt < numPlayers-1){
 			currentPlayer = players.get(++currentPlayerInt);
 		}
 		else {
 			currentPlayerInt = 0;
 			currentPlayer = players.get(currentPlayerInt);
 		}
 		if(currentPlayer.isOut()){
 			endTurn();
 		}
 	}
 	/**
 	 * Roll the dice
 	 * @return value rolled
 	 */
 	public int roll(){
 		int dice = 0;
 		for(int i = 0; i < this.numDice; i++){
 			dice += 1 + rand.nextInt(6);
 		}
 		return dice;
 	}
 	/**
 	 * Shuffle the cards and deal them one by one to each player.
 	 * Then sort player's hands.
 	 */
 	public void deal(){
 		Collections.shuffle(remainingCards);
 		int p = 0;
 		for(Card c : remainingCards){
 			players.get(p).giveCard(c);
 			if(p == numPlayers-1){
 				p = 0;
 			}else{ 
 				p++; 
 			}
 		}
 		for(Player pl : players){
 			pl.sort();
 		}
 		Collections.sort(remainingCards, new CardComparator());
 	}
 	/**
 	 * Tell the board to move the current player in a direction.
 	 * Returns false if player couldn't move in that direction -> ask for a different direction.
 	 * Returns true if the player was moved in that direction. 
 	 * @param direction The compass direction to move the current player in.
 	 * @return true of player was moved, false if player couldn't move in that direction.
 	 */
 	public boolean move(String direction){
 		String movedir = "z";
 		if(direction.equals("n")){ movedir = "north"; }
 		else if(direction.equals("e")){ movedir = "east"; }
 		else if(direction.equals("s")){ movedir = "south"; }
 		else if(direction.equals("w")){ movedir = "west"; }
 		else if(direction.equals("z")){ return false; }
 		return board.move(currentPlayer, movedir);
 	}
 	/**
 	 *TODO: TEST METHODS - DELETE BEFORE SUBMITTING***************
 	 */
 	public void printHands(){
 		for(Player p : players){
 			System.out.println(p.getName()+": "+p.handToString());
 		}
 		System.out.println(leftoverCards.size());
 		for(Card c : leftoverCards){
 			System.out.println(c.toString()+", ");
 		}
 	}
 
 	/**
 	 * Private methods for setting up the game below here
 	 */
 	/**
 	 * Create players based on the number of players entered.
 	 * @param numPlayers Number of players to create.
 	 */
 	private void makePlayers(int numPlayers){
 		ArrayList<String> names = new ArrayList<String>();
 		int choice = -1;
 		for(int i = 0; i < characterNames.length; i++){
 			names.add(characterNames[i]);
 		}
 		for(int i = 1; i <= numPlayers; i++){
 			while(choice < 1 || choice > names.size()){
 				choice = -1;
 				System.out.println("Player "+i+" choose a character:");
 				for(int j = 0; j < names.size(); j++){
 					System.out.print((j+1)+": "+names.get(j)+", ");
 				}
 				System.out.print("\n");
 				try{
 					choice = input.nextInt();
 				} catch (InputMismatchException e) {
 					choice = -1;
 				}
 				input.nextLine();
 			}
 			players.add(new Player(names.get(choice-1)));
 			names.remove(choice-1);
 			choice = -1;
 		}
 	}
 	/**
 	 * Create all cards for the game and put them into remainingCards
 	 */
 	private void makeCards(){
 		// Create weapon cards
 		for(int i = 0; i < weaponNames.length; i++){
 			remainingCards.add(new Weapon(weaponNames[i]));
 		}
 		// Create character cards
 		for(int i = 0; i < characterNames.length; i++){
 			remainingCards.add(new Character(characterNames[i]));
 		}
 		// Create room cards
 		for(int i = 0; i < roomNames.length; i++){
 			remainingCards.add(new Room(roomNames[i]));
 		}
 	}
 	/**
 	 * Create a random solution for this game.
 	 */
 	private void makeSolution() {
 		// To begin with, weapons occupy 0-8 in remainingCards,
 		// characters are 9-14, and rooms are 15-24.
 		int w = rand.nextInt(9);
 		int c = 9 + rand.nextInt(14 - 9 + 1);
 		int r = 15 + rand.nextInt(24 - 15 + 1);
 		// Get cards for solution.
 		Weapon sWeapon = (Weapon)remainingCards.get(w);
 		Character sCharacter = (Character)remainingCards.get(c);
 		Room sRoom = (Room)remainingCards.get(r);
 		// Remove cards from remainingCards.
 		remainingCards.remove(sWeapon);
 		remainingCards.remove(sCharacter);
 		remainingCards.remove(sRoom);
 		// Create new solution.
 		solution = new Solution(sWeapon, sCharacter, sRoom);
 		System.out.println(solution); // TODO: delete this test
 	}
 
 	/**
 	 * String arrays for card names.
 	 */
 	private String[] weaponNames = { "Rope", "Candlestick", "Knife", "Pistol", "Baseball Bat", "Dumbbell", "Trophy", "Poison", "Axe" };
 	private String[] characterNames = { "Kasandra Scarlett", "Jack Mustard", "Diane White", "Jacob Green", "Eleanor Peacock", "Victor Plum" };
 	private String[] roomNames = { "Spa", "Theatre", "Living Room", "Conservatory", "Patio", "Hall", "Kitchen", "Dining Room", "Guest House", "Pool" };
 	/**
 	 * Strings of card names to print out when making suggestions or accusations.
 	 * This is just to save printing each one out in a for loop ever time.
 	 */
 	private String charactersString = "1: Kasandra Scarlett, 2: Jack Mustard 3: Diane White, 4: Jacob Green, 5: Eleanor Peacock, 6: Victor Plum";
 	private String roomsString = "1: Spa, 2: Theatre, 3: Living Room, 4: Conservatory, 5: Patio, 6: Hall, 7: Kitchen, 8: Dining Room, 9: Guest House, 10: Pool";
 	private String weaponsString = "1: Rope, 2: Candlestick, 3: Knife, 4: Pistol, 5: Baseball Bat, 6: Dumbbell, 7: Trophy, 8: Poison, 9:Axe";
 }
