 /**
  *
  * Game System Interprets Game Commands!
  *
  * @author Casey DeLorme
  * @version 05-14-2012
  *
  */
 
 
 // Imports
 import java.util.Hashtable;
 import java.util.Arrays;
 
 
 public class GameSystem implements Interpreter {
 
 
 	/* Static */
 
 	private static String SYSTEM = "GAME";
 
 
 	/* Properties */
 
 	private UserManager um;
 	private GameFactory gf;
 
 
 	/* Constructors */
 
 	public GameSystem(UserManager aUM) {
 
 		// Set User Manager
 		setUM(aUM);
 
 		// Create GameFactory
 		setGF(new GameFactory());
 
 	}
 
 
 	/* Custom Methods */
 
 	public void interpret(Hashtable aCommand, User aUser) {
 
 		// Obtain Command
 		String command = (String) aCommand.get("COMMAND");
 
 		// Check/Identify Command
 		if (command.equals("NEWGAME")) {
 
 			// Grab User Names
 			String challengerName = (String) aCommand.get("CHALLENGER");
 			String recipientName = (String) aCommand.get("RECIPIENT");
 
 			// Identify & Set Users
 			User aChallenger = null;
 			User aRecipient = null;
 			for (User u : getUM().getUsers()) {
 
 				if (u.getUserName().equals(challengerName)) aChallenger = u;
 				if (u.getUserName().equals(recipientName)) aRecipient = u;
 
 			}
 
 			// If both a Challenger & Recipient were found
 			if (aChallenger != null && aRecipient != null) {
 
 				// Send addGame command to GameFactory
 				getGF().addGame(aChallenger, aRecipient);
 
 			}
 
 		} else if (command.equals("SHOW")) {
 
 			// Parse GameID, Click, and X & Y coordinates
 			int gameID = Integer.parseInt((String) aCommand.get("GAMEID"));
 			int theX = Integer.parseInt((String) aCommand.get("X"));
 			int theY = Integer.parseInt((String) aCommand.get("Y"));
 
 			// Game TMP
 			Game tmp = null;
 
 			// Send Three Options to get By ID
 			for (Game g : getGF().getGames()) {
 
 				// If Match Found
 				if (g.getGameID() == gameID) tmp = g;
 
 			}
 
 			// Perform Separately to avoid ConcurrentModificationException
 			if (tmp != null) {
 
 				// Send ShowTile Command
 				tmp.showTile(aUser, theX, theY);
 
 			}
 
 		} else if (command.equals("DISCONNECT")) {
 
 			// User has Disconnected, need to inform all
 			// of their game instances opponents
 			// and kill the game server side too
 
 			// Prepare a Hashtable for Command Sending
			Hashtable<String, String> aCommand2 = new Hashtable<String, String>();
 
 			// Cycle all Games and match user instances
 			for (int x = (getGF().getGames().size() - 1); x >= 0 ; x--) {
 
 				// If user is in this game, create command for opponent user
 				if (Arrays.asList(getGF().getGames().get(x).getPlayers()).contains(aUser)) {
 
 					// Send whoever is not the user a KILL command
 					aCommand2.put("SYSTEM", "GAME");
 					aCommand2.put("COMMAND", "KILL");
 					aCommand2.put("GAMEID", Integer.toString(getGF().getGames().get(x).getGameID()));
 
 					// Cycle Players
 					for (User u : getGF().getGames().get(x).getPlayers()) {
 
 						// If not quitter
 						if (!u.equals(aUser)) {
 
 							// Send Kill to opponent
 							u.sendCommand(aCommand2);
 						}
 
 					}
 
				}
 
				// Kill Instance Server-Side
				getGF().killGame(getGF().getGames().get(x));
 
 			}
 
 		}
 
 	}
 
 
 	public User getUserByName(String aUserName) {
 
 		User aUser = null;
 
 		// Cycle Users
 		for (User u : getUM().getUsers()) {
 
 			// Set Match if Found
 			if (u.getUserName().equals(aUserName)) aUser = u;
 
 		}
 
 		return aUser;
 
 	}
 
 
 
 
 	/* Mutators */
 
 	private void setUM(UserManager aUM) {
 		um = aUM;
 	}
 
 	private void setGF(GameFactory aGF) {
 		gf = aGF;
 	}
 
 
 	/* Accessors */
 
 	public UserManager getUM() {
 		return um;
 	}
 
 	public String getSystem() {
 		return SYSTEM;
 	}
 
 	private GameFactory getGF() {
 		return gf;
 	}
 
 
 }
