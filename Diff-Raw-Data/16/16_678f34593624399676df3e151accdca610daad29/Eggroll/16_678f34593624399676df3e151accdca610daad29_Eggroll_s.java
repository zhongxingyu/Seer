 /*Author: Alejandro Munoz-McDonald
  *Date Created: 4/12/2013
  **/
 
 package Java;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Scanner;
 
 public class Eggroll implements Play {
 	private static Scanner scanner = new Scanner(System.in);
 	private static ArrayList<Player> players;
 	private static Deck deck;
 	private static Deck pile;
 	private static String input;
 	private boolean finished;
 	private int amountOfPlayers;
 	ArrayList<String> statuses;
 	
 	public Eggroll(){
 		amountOfPlayers=4;
 		playGame();
 	}
 	
 	public Eggroll(int numOfPlayers){
 		amountOfPlayers=numOfPlayers;
 		playGame();
 	}
 	
 	public void explainRules() {
 		System.out.println("Try harder...");
 	}
 	public void setUp() {
 		statuses = new ArrayList<String>(Arrays.asList("Egg","Vice Egg","Vice President","President")); //Currently only up to four players can play, need more labels for more.
 		players = new ArrayList<Player>();
 		for(int i=0;i<amountOfPlayers;i++){
 			players.add(new Player());
 		}
 		while(statuses.size()-players.size()!=0){ //Remove unneeded statuses
 			statuses.remove(statuses.size()/2);
 		}
 		pile = new Deck(); //Trash pile. Used to check if players move was legal.
 		pile.clearDeck();
 		pile.addCard(new Card("3 of Clubs"));
 		deck = new Deck();
 		deck.takeCardOutOfDeck(new Card("3 of Clubs"));
 		deck.shuffle();
 	}
 	private void dealDeck(){ //Deals out entire deck to all players
 		int count=0;
 		while(!deck.isEmpty()){
 			players.get(count%players.size()).addCard(deck.deal());
 			count++;
 		}
 	}
 	
 	private Card getTopCard(){
 		return pile.getCards().get(pile.getCards().size()-1);
 	}
 	
 	private boolean isValidMove(Card a){
 		if(pile.getCards().size()>0){
 			if(getTopCard().getValue()<=a.getValue()||a.getValue()==2){
 				return true;
 			}
 		}return false;
 	}
 	
 
 	@Override
 	public void playGame() {
 		int cardPlayLimit=1;
 		int passCount=1;
 		int turns=0;
 		int sameCardCount=1;
 		
 		setUp();
 		
 		dealDeck();
 		/*for(int i=0;i<players.size();i++){
 			System.out.println(players.get(i).toString());
 		}*/
 
 		//players.get(0).getHand().clearDeck();
 		players.get(0).addCard(new Card("3 of clubs"));
 		while(!finished){
 			if(pile.getCards().size()>=4){
			for(int i=0;i<3;i++){
				if(!(pile.getCards().get(pile.getCards().size()-1-i).getValue()==pile.getCards().get(pile.getCards().size()-i-2).getValue()))
					break;
				sameCardCount=4;
			}}
 			boolean pass=false;
 			int numCardsPlayed=0;
 			String input=null;
 			Card[] inputCard = new Card[0];

 			if(players.get(turns%players.size()).getHand().isEmpty()){ //Skip players that have no cards
 				turns++;
 			}
 			else{ //Continue regular gameplay
 				inputCard = new Card[1];
 				if(sameCardCount==4){ //Check for quadruple sets
 					System.out.println("Everyone drop a card!");
 					pile.clearDeck();
 					for(int i=0;i<players.size();i++){
 						if(!players.get(turns%players.size()).getHand().isEmpty()){
 							boolean validCard=false;
 							System.out.println("Player #"+(turns%players.size()+1)+" drop one card.\nHand:"+players.get(turns%players.size()).getHand().toString());
 							while(!validCard){	//Take input from the user
 								input = scanner.nextLine();
 								inputCard[numCardsPlayed] = new Card(input);
 								if(players.get(turns%players.size()).getHand().hasCard(inputCard[numCardsPlayed])){
 									players.get(turns%players.size()).removeCard(inputCard[numCardsPlayed]);
 									validCard=true;
 								}else{
 									System.out.println("You don't have that card!");
 								}
 							}
 						}turns++;
 					}
 					sameCardCount=1;
 				}
 				if(players.get(turns%players.size()).getHand().isEmpty())
 					break;
 				if(passCount>players.size()){ //Check if all players have passed, reset pile
 					System.out.println("All players have passed!");
 					pile.clearDeck();
 				}
 				
 				if(pile.isEmpty()){ //If pile is empty, need to place starting cards
 					passCount=2;
 					System.out.println("Player #"+(turns%players.size()+1)+" start pile.\nHand:"+players.get(turns%players.size()).getHand().toString());
 					boolean done=false;
 					inputCard=new Card[4];
 					while(!done){	//Take input from the user
 						input = scanner.nextLine();
 						if(input.toLowerCase().equals("done")){ //Said when user is done inputing cards(for doubles/triples)
 							if(numCardsPlayed==0){
 								System.out.println("Input at least one card!");
 							}else{
 								done=true;
 								cardPlayLimit=numCardsPlayed;
 								for(int i=0;i<numCardsPlayed;i++){
 									pile.addCard(inputCard[i]);
 									players.get(turns%players.size()).removeCard(inputCard[i]);
 								}
 								turns++;
 							}
 						}else{ //Else, check if card is valid move, and for doubles, if cards are same
 							inputCard[numCardsPlayed] = new Card(input);
 							if(players.get(turns%players.size()).getHand().hasCard(inputCard[numCardsPlayed])){
 								if(numCardsPlayed>0)
 									if(inputCard[0].getValue()==inputCard[numCardsPlayed].getValue()){ //Check if input cards are same
 										numCardsPlayed++;
 									}
 									else{
 										System.out.println("You have to input the same card!");
 										numCardsPlayed=0;
 									}
 								else{
 									numCardsPlayed++;
 								}
 							}else{
 								System.out.println("You don't have that card!");
 							}
 						}
 					}
 				}
 				//Main Gameplay
 				inputCard=new Card[cardPlayLimit];
 				while(numCardsPlayed<cardPlayLimit){ //While the player has not put in the maximum cards neeeded
 					
 					System.out.print("Pile:"); //Show pile
 					for(int i=pile.getCards().size()-1;i>pile.getCards().size()-1-cardPlayLimit;i--){
 						System.out.print(pile.getCards().get(i).toString()+","); 
 					}System.out.println();
 					boolean validCard=false;
 
 					System.out.println("Player #"+(turns%players.size()+1)+" turn.\nHand:"+players.get(turns%players.size()).getHand().toString());
 					while(!validCard){	//Take input from the user, regular gameplay here
 						input = scanner.nextLine();
 						if(input.toLowerCase().equals("pass")){ //Check if player passes, skip turn
 							pass=true;
 							passCount++;
 							turns++;
 							break;
 						}else{
 							passCount=2; //reset pass count
 							inputCard[numCardsPlayed] = new Card(input);
 							if(players.get(turns%players.size()).getHand().hasCard(inputCard[numCardsPlayed])){ //If player hasCard
 								if(numCardsPlayed==cardPlayLimit-2&&inputCard[numCardsPlayed].getValue()==2){
 									numCardsPlayed=cardPlayLimit;
 									break;
 								}
 								else if(numCardsPlayed>0){
 									if(inputCard[0].getValue()==inputCard[numCardsPlayed].getValue()){
 										validCard = true;
 										numCardsPlayed++;
 									}
 									else{ //Checking for double/triple sets
 										System.out.println("You have to input the same card!");
 										numCardsPlayed=0;
 									}
 								}
 								else{
 									validCard=true;
 									numCardsPlayed++;
 								}
 							}else{
 								System.out.println("You don't have that card!");
 							}
 						}
 					}if(pass) //Used so turn is not executed if player passes
 						break;
 					if(numCardsPlayed==cardPlayLimit){ //If player has entered enough cards to move on, execute card removal+pile addition
 						if(isValidMove(inputCard[0])){ //Make move if legal
 							players.get(turns%players.size()).removeCard(inputCard[0]);
 							if(inputCard[0].getValue()==getTopCard().getValue()){ //Skip player if same card
 								sameCardCount++;
 								if(sameCardCount<4){
 									turns+=2;
 									passCount++;
 								}
 								for(int i=0;i<numCardsPlayed;i++){
 									pile.addCard(inputCard[numCardsPlayed-1-i]);
 								}
 							}else if(inputCard[0].getValue()==2){
 								pile.clearDeck();
 							}
 							else{
 								sameCardCount=1;
 								for(int i=0;i<numCardsPlayed;i++){
 									pile.addCard(inputCard[numCardsPlayed-1-i]);
 								}
 								turns++;
 							}
 						}else
 							System.out.println("Not a valid move!");
 					}else{
 						if(isValidMove(inputCard[numCardsPlayed-1])) //If multiple cards needed, prompt player for more cards
 							System.out.println("Input next card");
 						else{
 							System.out.println("Not a valid move!");
 							numCardsPlayed=0;
 						}
 					}
 				}
 				
 				//Check for empty hand/winner
 				for(int i=0;i<players.size();i++){
 					if(players.get(i).getHand().size()==0){
 						System.out.println("Player "+(i+1)+" is "+statuses.get(players.size()-1));
 					}
 				}if(players.size()==1){
 					finished=true;
 				}//
 				System.out.println();
 			}
 		}
 		
 	}
 	
 	public void gameOver(String winner) {
 		
 	}
 
 }
