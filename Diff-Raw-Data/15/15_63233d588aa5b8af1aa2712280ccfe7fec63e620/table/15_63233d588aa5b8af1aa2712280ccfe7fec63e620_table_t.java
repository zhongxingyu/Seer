 import java.io.IOException;
 
 public class table {
 	static int numOfPlayers = 0;
 	static int numOfDecks = 0;
 	static IOHandler ioh;
 	
 	public static void main(String[] args) throws IOException{
 		ioh = IOHandler.getInstance();
		numOfPlayers = (int) Double.parseDouble(ioh.queryInput("How many players to start?"));
		numOfDecks = (int) Double.parseDouble(ioh.queryInput("How many decks to start?"));
 		Game game = new Game(numOfPlayers, numOfDecks);
 		game.play();
		ioh.print("The game has finished!");
 	}
 }
