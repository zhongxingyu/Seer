 package ch.bfh.monopoly.common;
 
 import java.util.Locale;
 
 import ch.bfh.monopoly.network.Network;
 
 
 public class GameClient {
 	
 	Player localPlayer;
 	Player currentPlayer;
 	Network communicate;
 	Locale loc;
 	Board board;
 	
 	public GameClient (Locale loc) {
 		
 		//loc should be received from a netmessage sent when the server calls startGame();
 		this.loc = loc;
 		
 		//TODO this list must be received from a netMessage when the game starts
 		String[] playerNames = {"Justin","Giuseppe","Damien","Cyril","Elie"};
		
 	}
 	
 	public void initBoard(String[] playerNames){
 		//a net message should come with the list of player names
 		//until then we have a mock object full of player names
 		board.createPlayers(playerNames, loc);
 	}
 
 	public Locale getLoc(){
 		return loc;
 	}
 
 	public Board getBoard(){
 		return this.board;
 	}
 	
 	public Player getCurrentPlayer() {
 		return currentPlayer;
 	}
 	
 	public void buyHouse(int tileID){
 		board.buyHouse(tileID);
 	}
 	
 	public void transferPropertyForPrice(String fromName, String toName, int propertyID, int price){
 		board.transferProperty(fromName, toName, propertyID);
 		board.transferMoney(fromName, toName, price);
 	}
 
 	
 }
