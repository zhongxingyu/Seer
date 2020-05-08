 package linewars.init;
 
 import java.util.ArrayList;
 
 /**
  * For testing only.
  * @author Taylor Bergquist
  *
  */
 public class SinglePlayerDriverProxy {
 	public static void main(String[] args){
		String mapDefinitionURI = "resources/maps/Less_Maximally_Curvy_Map.cfg";
 		int numPlayers = 1;
 		String serverAddress = "127.0.0.1";
 		String raceURI1 = "resources/races/thatOneRace.cfg";
 		String playerName1 = "Knexer";
 		String playerAddress1 = "127.0.0.1";
 		ArrayList<String> raceURIs = new ArrayList<String>();
 		raceURIs.add(raceURI1);
 		ArrayList<String> playerNames = new ArrayList<String>();
 		playerNames.add(playerName1);
 		ArrayList<String> playerAddresses = new ArrayList<String>();
 		
 		Game toStart = new Game(mapDefinitionURI, numPlayers, serverAddress, raceURIs, playerNames, playerAddresses);
 		toStart.initialize();
 		toStart.run();
 	}
 }
