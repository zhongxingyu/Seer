 package com.adaptionsoft.games.uglytrivia;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 public class Game {
     ArrayList players = new ArrayList();
     int[] places = new int[6];
     int[] purses  = new int[6];
     boolean[] inPenaltyBox  = new boolean[6];
     int[] highscores= new int[6];
 
     LinkedList popQuestions = new LinkedList();
     LinkedList scienceQuestions = new LinkedList();
     LinkedList sportsQuestions = new LinkedList();
     LinkedList rockQuestions = new LinkedList();
     
     int currentPlayer = 0;
     boolean isGettingOutOfPenaltyBox;
     
     public  Game(){
     	for (int i = 0; i < 50; i++) {
 			popQuestions.addLast("Pop Question " + i);
 			scienceQuestions.addLast(("Science Question " + i));
 			sportsQuestions.addLast(("Sports Question " + i));
 			rockQuestions.addLast(createRockQuestion(i));
     	}
     }
 
 	public String createRockQuestion(int index){
 		return "Rock Question " + index;
 	}
 
 	public boolean add(String playerName) {
 		
 		
 	    players.add(playerName);
 	    places[howManyPlayers()] = 0;
 	    purses[howManyPlayers()] = 0;
 	    inPenaltyBox[howManyPlayers()] = false;
 	    
 	    System.out.println(playerName + " was added");
 	    System.out.println("They are player number " + players.size());
 		return true;
 	}
 	
 	public int howManyPlayers() {
		return players.size()-1;
 	}
 
 	public void roll(int roll) {
 		System.out.println(players.get(currentPlayer) + " is the current player");
 		System.out.println("They have rolled a " + roll);
 		
 		if (inPenaltyBox[currentPlayer]) {
 			if (roll % 2 != 0) {
 				isGettingOutOfPenaltyBox = true;
 				
 				System.out.println(players.get(currentPlayer) + " is getting out of the penalty box");
 				moveForwardCurrentPlayer(roll);
 				askQuestion();
 			} else {
 				System.out.println(players.get(currentPlayer) + " is not getting out of the penalty box");
 				isGettingOutOfPenaltyBox = false;
 				}
 			
 		} else {
 		
 			moveForwardCurrentPlayer(roll);
 			askQuestion();
 		}
 		
 	}
 
 	private void moveForwardCurrentPlayer(int roll) {
 		places[currentPlayer] = places[currentPlayer] + roll;
 		if (places[currentPlayer] > 11) places[currentPlayer] = places[currentPlayer] - 12;
 		
 		System.out.println(players.get(currentPlayer) 
 				+ "'s new location is " 
 				+ places[currentPlayer]);
 		System.out.println("The category is " + currentCategory());
 	}
 
 	private void askQuestion() {
 		if (currentCategory() == "Pop")
 			System.out.println(popQuestions.removeFirst());
 		if (currentCategory() == "Science")
 			System.out.println(scienceQuestions.removeFirst());
 		if (currentCategory() == "Sports")
 			System.out.println(sportsQuestions.removeFirst());
 		if (currentCategory() == "Rock")
 			System.out.println(rockQuestions.removeFirst());		
 	}
 	
 	// randomly return a category
 	private String currentCategory() {
 		if (places[currentPlayer] == 0) return "Pop";
 		if (places[currentPlayer] == 4) return "Pop";
 		if (places[currentPlayer] == 8) return "Pop";
 		if (places[currentPlayer] == 1) return "Science";
 		if (places[currentPlayer] == 5) return "Science";
 		if (places[currentPlayer] == 9) return "Science";
 		if (places[currentPlayer] == 2) return "Sports";
 		if (places[currentPlayer] == 6) return "Sports";
 		if (places[currentPlayer] == 10) return "Sports";
 		return "Rock";
 	}
 
 	public boolean wasCorrectlyAnswered() {
 		if (inPenaltyBox[currentPlayer]){
 			if (isGettingOutOfPenaltyBox) {
 				System.out.println("Answer was correct!!!!");
 				purses[currentPlayer]++;
 				System.out.println(players.get(currentPlayer) 
 						+ " now has "
 						+ purses[currentPlayer]
 						+ " Gold Coins.");
 				
 				boolean winner = didPlayerWin();
 				nextPlayer();
 				
 				return winner;
 			} else {
 				nextPlayer();
 				return true;
 			}
 			
 			
 			
 		} else {
 		
 			System.out.println("Answer was corrent!!!!");
 			purses[currentPlayer]++;
 			System.out.println(players.get(currentPlayer) 
 					+ " now has "
 					+ purses[currentPlayer]
 					+ " Gold Coins.");
 			
 			boolean winner = didPlayerWin();
 			nextPlayer();
 			
 			return winner;
 		}
 	}
 
 	private void nextPlayer() {
 		currentPlayer++;
 		if (currentPlayer == players.size()) currentPlayer = 0;
 	}
 	
 	public boolean wrongAnswer(){
 		System.out.println("Question was incorrectly answered");
 		System.out.println(players.get(currentPlayer)+ " was sent to the penalty box");
 		inPenaltyBox[currentPlayer] = true;
 		
 		nextPlayer();
 		return true;
 	}
 
 	/**
 	 * Tells if the last player won.
 	 */
 	private boolean didPlayerWin() {
 		return !(purses[currentPlayer] == 6);
 	}
 }
