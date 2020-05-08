 package liarsDiceModel;
 import java.util.ArrayList;
 import java.util.List;
 
 import programmerTournamentModel.Bot;
 import programmerTournamentModel.Game;
 import programmerTournamentModel.GameFactory;
 
 /**
  * This class is a factory which returns an instance of the LiarsDiceGame class (and the players associated with it.
  */
 public class LiarsDiceGameFactory implements GameFactory {
 	private String name = "Liar's Dice";
 	
 	/**
 	 * Constructor. (Does nothing.)
 	 */
 	public LiarsDiceGameFactory(){
 		
 	}
 	
 	/**
 	 * @param players A list of players who will be included in the current Game instance.
 	 * @return An instance of LiarsDiceGame set up with the given players.
 	 */
 	public Game getGameInstance(List<Player> players) {
 		ArrayList<LiarsDicePlayer> liarsDicePlayers = new ArrayList<LiarsDicePlayer>();
 		for(Player p : players){
 			liarsDicePlayers.add((LiarsDicePlayer)p);
 		}
 		resetDice(liarsDicePlayers);
 		return new LiarsDiceGame(liarsDicePlayers);
 	}
 
 	/**
 	 * Resets the dice of each player in the given list of LiarsDicePlayers
 	 * @param liarsDicePlayers List of players which need their dice reset.
 	 */
 	private void resetDice(ArrayList<LiarsDicePlayer> liarsDicePlayers) {
 		for(LiarsDicePlayer p : liarsDicePlayers){
 			p.resetDice();
 		}
 	}
 
 	/**
 	 * Uses reflection to get a List of all the submitted bots in the "/bots" folder.
 	 * @return List of Players made from bots contained in the "/bots" folder.
 	 */
 	public List<Player> getPlayers() {
 		// TODO Reflection goes here - for now can hardcode here
 		ArrayList<Bot> bots = new ArrayList<Bot>();
 		bots.add(new TestBot0());
 		bots.add(new TestBot1());
 //		bots.add(new TestBot2());
 //		bots.add(new TestBot3());
		bots.add(new TestBot4());
//		bots.add(new TestBot5());
 		
 		//wrap each bot in a Player object
 		int playerNumber = 1;
 		ArrayList<Player> players = new ArrayList<Player>();
 		for (Bot bot : bots)
 		{
 			players.add(new LiarsDicePlayer((LiarsDiceBot) bot, playerNumber++));
 		}
 		
 		return players;
 	}
 
 	/**
 	 * @return The name of this Game implementation: "Liar's Dice"
 	 */
 	public String getGameName() {
 		return name;
 	}
 
 }
