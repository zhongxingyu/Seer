 package Zegana;
 import magicGame.*;
 import java.util.*;
 public class Connect{
 
     public static int simulate(Deck a, Deck b){
 	magicGame.GameState gs = new magicGame.GameState(2);
 	ArrayList<String[]> players = new ArrayList<String[]>();
 	players.add(format(a));
 	players.add(format(b));
 	gs.makePlayers(players);
 	gs.initializeGame();
 	ArrayList<Object> result = gs.playGame("Com");
 	//System.out.println("Player " + ((magicGame.Player) result.get(1)).getPlayerNumber() + " wins in " + (int) result.get(0) + "turns.");
	if(result.get(1) == null){
	    System.out.println("Sim returned null player as victor");
	    System.out.println("Win turn: "+(int) result.get(0));
	    System.out.println(a);
	    System.out.println(b);
	}
 	return ((magicGame.Player) result.get(1)).getPlayerNumber();
     }
 
     //Turns a deck object as Zegana understands it into an
     //array of card names with repetition, which is the format
     //the simulator understands decks in
     private static String[] format(Deck in){
 	int size = 0;
 	
 	for(int i = 0; i<in.quantity.size(); i++){
 	    size += in.quantity.get(i);
 	}
 
 	String[] cards = new String[size];
 
 	int position = 0;
 	int count = 0;
 
 	for(int i = 0; i<size; i++){
 	    cards[i] = in.cards.get(position);
 	    count++;
 	    if(count==in.quantity.get(position)){
 		count = 0;
 		position++;
 	    }
 	}
 
 	return cards;
     }
     
     //Makes a deck of forests, formatted for the simulator
     private static String[] formatEmpty(){
 	String[] cards = new String[60];
 	for(int i=0; i<60; i++){
 	    cards[i]="Forest";
 	}
 	return cards;
     }
 }
