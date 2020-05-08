 /*
 	Author: Croller
 	Date: 10/2013
 	View README for more Info!
 */
 
 import java.util.*;
 import java.util.concurrent.TimeUnit;
import java.io.*;
 
 public class BlackJack {
 
 
 	private int numberOfPlayers = 0; //How many players are playing between 1-4 inclusive
 
 	private Player [] peoplePlaying; //Array of Player objects ; stores player information
 
 	private Player dealer;
 
 	private Deck deck; //Deck object to construct a deck of cards and return a random card
 
 	private int gamesPlayed = 0; //Keeps track of how many games have been played
 
 	private Scanner sc; //Scanner to read user input 
 
 	/*
 	Overview: Constructor for BlackJack, determines if a game is starting or already going on
 	Pre-Conditions: main constructs BlackJack object
 	Post-Conditions: A game of Black-Jack
 	*/
 	public BlackJack(){
 		sc = new Scanner(System.in);
		if(gamesPlayed == 0){
 			intro();
			File stats = new File("Stats.txt");
		}else
 			game();
 	}
 
 	/*
 	//OverView: Intoduction of the game: to consult the user for number of players and put you into a game!
 	//Pre-Conditions: if gamesPlayed = 0, this method will consult the user for information to play
 	//Post-Conditions: Game On!
 	*/
 	private void intro(){
 		System.out.println("Welcome to BlackJack!");
 		System.out.print("How many players are playing? (1-5) ");
 		String p = sc.nextLine();
 		while(!p.equals("1") && !p.equals("2") && !p.equals("3") && !p.equals("4") && !p.equals("5")){
 				System.out.println(p + " is not a valid number of players!");
 				System.out.print("How many players are playing (1-5) ");
 				p = sc.nextLine();
 			}
 		numberOfPlayers = Integer.parseInt(p);
 		peoplePlaying = new Player [numberOfPlayers];
 		System.out.println("Congrats! " + numberOfPlayers + " are selected to play!");
 		int count = 0;
 		while(count < numberOfPlayers){
 			System.out.print("Player " + (count+1) + ", Enter a name to play with: ");
 			String name = sc.nextLine(); //Prompting Player for name
 			peoplePlaying[count] = new Player(true, name);
 			System.out.println("Player " + (count+1) + ", You've sucessfully named yourself " + name);
 			count++;
 		}
 		for(int i=0; i<numberOfPlayers; i++){
 			if(i == numberOfPlayers-1)
 				System.out.print("and " + peoplePlaying[i].getName() + " get ready!!!");
 			else
 				System.out.print(peoplePlaying[i].getName() + " ");
 		}
 		System.out.println("Game Commencing in :");
 		int time = 5;
 		while(time > 0){
 			System.out.print(time + "..\n");
 			try{
 				TimeUnit.SECONDS.sleep(1);
 			}catch (Exception e){}
 			time--;
 		}
 		gamesPlayed++;
 	}
 
 	/*
 	Overview: Method that ensures that input from user is correct 
 	Pre-Conditions: A Scanner asking for input
 	Post-Conditions: A guarentee that the correct input was given from the user
 	*/
 	private int correctInput(){
 		System.out.print("What would you like to do? (1-Hit, 2-Stay, 3-Double, 4-Split) : ");
 		String s = sc.nextLine();
 		while(!s.equals("1") && !s.equals("2") && !s.equals("3") && !s.equals("4")){
 			System.out.println(s + " is not an integer between 1-4");
 			System.out.print("What would you like to do? (1-Hit, 2-Stay, 3-Double, 4-Split) : ");
 			s = sc.nextLine();
 		}
 		return Integer.parseInt(s);
 
 	}
 
 	/*
 	Overview: Method that plays the game of BlackJack
 			1)Deals init cards to dealer and players
 			2)loops through players moves
 			3)loops through dealers moves (AI)
 			4)evaluates who won and who lost
 			5)finds out who wants to play again and who doesn't
 			6)once player leave a game, statsics for games played are given
 	*/
 	public void game(){
 		deck = new Deck(); //creates a new deck with 52 cards (eventually to be more once betting is implemented)
 		dealer = new Player(true, "Dealer"); //creates Player Dealer for dealer cards
 		
 		boolean stillPlaying = true;
 		while(stillPlaying){ //Loop that iterates until all players are done playing
 
			System.out.println("\nRound " + gamesPlayed + "\n"); //Tells the current round

 			//Dealing init cards to players / dealer
 			for(int i = 0; i < numberOfPlayers; i++)
 				peoplePlaying[i].addCard(deck.randomCard());
 			dealer.addCard(deck.randomCard());
 			for(int i = 0; i < numberOfPlayers; i++)
 				peoplePlaying[i].addCard(deck.randomCard());
 			dealer.addCard(deck.randomCard());
 		
 		//First Default deal is out, First card is Shown, Second card is Hidden
 			System.out.println("Dealers Hand : " + dealer.getCard(0) + " X");
 
 			//Players turn...
 			int opt = 0; //Users Option...
 			for(int i = 0; i < numberOfPlayers; i++){
 				System.out.println(peoplePlaying[i]);
 				opt = correctInput();
 				if(opt == 1){
 					peoplePlaying[i].addCard(deck.randomCard());
 					System.out.println("You got a : " + peoplePlaying[i].getCard(peoplePlaying[i].getSize()-1));
 					if(bust(i)) //if player busts then move to next player..
 						continue;
 					else{ //otherwise if player doesn't bust then ask player what they want to do
 						i--;
 						continue;
 					}
 				}else if (opt == 2) //Does nothing...
 					continue;
 				else if (opt == 3){ //Doubles the bet but does nothing yet
 					System.out.println("Hey were given option double, which doesent do anything yet");
 					i--;
 				}else if(opt == 4){ //Splits the cards but does nothing yet
 					System.out.println("Hey were given option split, which doesent do anythitng yet");
 					i--;
 				}
 			}
 
 			//Dealers turn...
 			int cardSum = 0; 
 			int aces = 0;
 			boolean done=false;
 			while(!done){ 
 				System.out.println(dealer);
 				cardSum = 0;
 				for(int i=0; i<dealer.getSize(); i++){
 					if(dealer.getCard(i).contains("A")) 
 						aces++;
 					else
 						cardSum	+= cardValue(dealer.getCard(i));
 				}
 				while(aces != 0){
 					cardSum += cardValueA(cardSum);
 					aces--;
 				}
 				if(cardSum <= 16){
 					dealer.addCard(deck.randomCard());
 				}else if (cardSum > 16 && cardSum < 22){
 					done = true;
 				}else if (cardSum > 21){
 					System.out.println("DEALER BUSTED!!! with " + cardSum);
 					done = true;
 				}
 			}
 
 			eval(cardSum); //evaluates who won, who lost, who tied
 
 			if(!afterGame())  //if there are no players playing anymore.. then exit while loop and game
 				stillPlaying = false;
 			else { //otherwise keep playing
 				dealer.clear(); //clears the dealers deck...
 				continue;
 			}
 		}
 
 		System.out.println("\nThank You for playing BlackJack - by Croller\n");
 
 	}
 	/*
 	Overview: After each round, this method asks players if they want to keep playing or not
 			  If they do, the method will return true, Otherwise false
 	Pre-Conditions: A game must complete
 	Post-Conditions: A new game or exit the program
 	*/ 
 	private boolean afterGame(){
		gamesPlayed++; //one game has played... so increment gamesPlayed
 		int numberOfPlayersDone = 0; //tallys the number of players who don't want to play anymore
 		for(int i = 0; i<numberOfPlayers; i++){
 			System.out.print(peoplePlaying[i].getName() + ", do you want to play again? (Y/N) :");
 			String ans = sc.nextLine().toLowerCase();
 			while(!ans.equals("y") && !ans.equals("n")){
 				System.out.print("This is not Y/N... please try again... :");
 				ans = sc.nextLine().toLowerCase();
 			}
 			if(ans.equals("n")){
 				peoplePlaying[i].looser(false);
 				System.out.println("Thanks for playing " + peoplePlaying[i].getName());
 				//statistics(i) //Will give player results of his games
 				numberOfPlayersDone++;
 			}else
 				peoplePlaying[i].clear(); //We know player is playing.. so clear his current cards
 		}
 		if(numberOfPlayersDone != 0){
			for(int i=0; i<numberOfPlayers; i++){
 				if(!peoplePlaying[i].isPlaying())
 					remove(i); //removes player from [] peoplePlaying
 			}
 		}
 		if(numberOfPlayers == 0)
 			return false; //game over!
 		else
 			return true; //game still on for remaining players
 	}
 
 	//Overview: removes player at index n from peoplePlaying[]
 	private void remove(int n){
		for(int i=n; i<numberOfPlayers-1; i++)
			peoplePlaying[i] = peoplePlaying[i+1];
 		numberOfPlayers--;
 	}
 
 	/*
 	Overview: evaluates the outcome of all players vs dealer
 	Pre-Conditions: 
 	Post-Conditions: 
 	*/
 	private void eval(int dsum){
 		System.out.println("\n..........Evaluation..........");
 		System.out.println(dealer); //Shows dealers hand
 		for(int i=0; i<numberOfPlayers; i++){ 
 			if(bust(i))
 				continue;
 			else{
 				int psum = 0;
 				int ace = 0;
 				for(int j=0; j<peoplePlaying[i].getSize(); j++){
 					if(peoplePlaying[i].getCard(j).contains("A"))
 						ace++;
 					else
 						psum += cardValue(peoplePlaying[i].getCard(j));
 				}while(ace != 0){
 					psum += cardValueA(psum);
 					ace--;
 				}
 				if(dsum > 21)
 					System.out.println(peoplePlaying[i].getName() + " wins with a sum of " + psum + " dealer busted...");
 				else if(dsum < psum)
 					System.out.println(peoplePlaying[i].getName() + " wins with a sum of " + psum + " vs " + dsum);
 				else if(dsum == psum)
 					System.out.println(peoplePlaying[i].getName() + " Push.... " + psum + " vs " + dsum);
 				else 
 					System.out.println(peoplePlaying[i].getName() + " looses to the dealer ....." + psum + " vs " + dsum);
 			}
 		}
 	}
 
 	/*
 	Overview Returns True: if player busts
 					 False: Otherwise
 	Parameters: int n: index of player
 	Pre-Conditions: A player makes a move
 	*/
 	private boolean bust(int n){
 		int sum = 0;
 		int ace = 0;
 		ArrayList<String> temp = peoplePlaying[n].getCards(); //Temp ArrayList to tally up card values
 		for(int i=0; i<temp.size(); i++){
 			if(temp.get(i).contains("A")) //Tallys aces to compare to the whole some of othercards
 				ace++;
 			else
 				sum += cardValue(temp.get(i)); //Tallys up everyother card value into sum
 		}
 		while(ace != 0){ //decides what an ace or aces will be counted as
 			sum += cardValueA(sum);
 			ace--;
 		}
 		if(sum > 21){ //You busted...
 			System.out.println(peoplePlaying[n].getName() + " BUSTED!!!" + sum + "..."); //Busted...
 			peoplePlaying[n].looser(false); //Change Boolean of player isPlaying ... to false using mutator method loose
 			return true;
 		}
 		else if(sum<21){ //Still alive!
 			System.out.println(peoplePlaying[n].getName() + " Didnt Bust! " + sum); //Didnt Bust
 			return false;
 		}else { //BLACKJACK!
 			System.out.println("BLACKJACK!!!! " + sum + " Baby!!!"); //BlackJack
 			return false;
 		}
 	}
 
 	//Overview: Returns card values for all cards but Aces
 	private int cardValue(String s){
 		if(s.contains("J") || s.contains("Q") || s.contains("K") || s.contains("1"))
 			return 10; 
 		else
 			return Integer.parseInt(s.substring(0,1));
 		
 	}
 	//Overview: Returns card value for Ace depending on the sum of the other cards in play
 	private int cardValueA(int sum){
 		if (sum <= 10)
 			return 11;
 		else 
 			return 1;
 	}
 	//Overview: Shows statistics for player that is leaving...
 	private void statistics(int n){
 
 	}
 
 	//main to simulate game
 	public static void main(String [] args){
 		BlackJack b = new BlackJack();
 		//System.out.println("\nYes we're going into a game!"); //Testing...
 		b.game();
 	}
 }
