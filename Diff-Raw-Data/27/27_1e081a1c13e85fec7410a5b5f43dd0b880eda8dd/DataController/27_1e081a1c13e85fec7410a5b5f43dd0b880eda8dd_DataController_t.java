 package com.example.wecharades.model;
 
 import java.io.IOException;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPConnectionClosedException;
 import org.apache.commons.net.io.CopyStreamException;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 
 import com.parse.ParseUser;
 
 
 /**
  * This is a class intended as an interface to the model and database.
  * 	All requests for DATA should be run through this class, which will handle
  * 	the logic of fetching cached data and retrieving form the database.
  * 	This class will also house the logic for pushing and updating data to both  
  * @author Anton Dahlstrm
  *
  */
 public class DataController extends Observable implements Observer{
 
 	//This variable is set when a user logs out. We cannot simply delete these references, as
 	// we might have to wirte stuff to the database.
 	private static boolean RECREATE = false;
 
 	private static DataController dc;
 	private Model m;
 	private IDatabase db;
 	private DataController(Context context){
 		//Get references to the model and the database - both singletons
 		m = Model.getModelInstance(context);
 		db = Database.getDatabaseInstance(context);
 		db.setConverter(this);
 		db.addObserver(this);
 	}
 
 	/**
 	 * Get a reference to the datacontroller
 	 * @param context - the context
 	 * @return A dataController
 	 */
 	public static DataController getDataController(Context context){
 		/*
 		 * This is a singleton. This is done, as we never want to have several instances of this class
 		 * 	We will recreate the instance of this class whenever a user logs out and in again.
 		 */
 		if(dc == null || RECREATE){
 			dc = new DataController(context);
 			RECREATE = false;
 		}
 		return dc;
 	}
 
 	/**
 	 * Call this method to save the state of the model.
 	 * @param context - the current context.
 	 */
 	public void saveState(Context context){
 		if(m != null)
 			m.saveModel(context);
 	}
 
 	/**
 	 * This method is called when the database has finished fetching turn and game data.
 	 * 	This is part of the observer pattern.
 	 */
 	@SuppressWarnings("unchecked") //Se comment bellow
 	@Override
 	public void update(Observable db, Object obj) {
 		/*
 		 * All messages that are sent from the database should contain a DBMessage.
 		 * 	This procedure is in place to reduce the level of type-checking needed here.
 		 */
 		if(obj != null && obj.getClass().equals(DBMessage.class)){
 			DBMessage dbm = (DBMessage) obj;
 			if(dbm.getMessage() == DBMessage.ERROR){
 				//If we get an error from the database, print message and stackTrace in logcat,
 				// and send a modified error on to the user.
 				DatabaseException e = (DatabaseException) dbm.getData();
 				Log.d("DatabaseError", "Code: " + e.getCode() + "   Message: " + e.getMessage());
 				e.printStackTrace();
 				setChanged();
 				notifyObservers(new DCMessage(DCMessage.ERROR, e.prettyPrint()));
 			} else if(dbm.getMessage() == DBMessage.MESSAGE){
 				//If we receive a message from the database, we should display this in a different manner to the user.
 				setChanged();
 				notifyObservers(new DCMessage(DCMessage.MESSAGE, (String) dbm.getData())); 
 			} else if(dbm.getMessage() == DBMessage.GAMELIST){
 				//If we receive a gameList from the database, we should compare this and send it forth to the user.
 				ArrayList<Game> gameList = parseGameList((TreeMap<Game, ArrayList<Turn>>) dbm.getData());
 				setChanged();
 				notifyObservers(new DCMessage(DCMessage.DATABASE_GAMES, gameList));
 			} else if(dbm.getMessage() == DBMessage.INVITATIONS){
 				//When we get an updated list of invitations, we parse this and send it forth.
 				List<Invitation> invList = parseDbInvitations((List<Invitation>) dbm.getData());
 				setChanged();
 				notifyObservers(new DCMessage(DCMessage.INVITATIONS, invList));
 			}
 		}
 	}
 
 	//Session handling -----------------------------------------------------------
 
 	/**
 	 * Log in a player
 	 * @param username - The username (case insensitive)
 	 * @param password - The password
 	 * @throws DatabaseException - if the connection to the database fails 
 	 */
 	public void loginPlayer(Context context, String username, String password) throws DatabaseException{
 		m = Model.getModelInstance(context);
 		db = Database.getDatabaseInstance(context);
 		db.loginPlayer(username, password);
 		m.setCurrentPlayer(db.getCurrentPlayer());
 	}
 
 	/**
 	 * Log out the current player
 	 */
 	public void logOutPlayer(Context context){
 		db.removePushNotification(context);
 		m.logOutCurrentPlayer(context);
 		db.logOut();
 		//Recreates DataController on next log in or app restart.
 		RECREATE = true;
 	}
 
 	/**
 	 * returns the current user
 	 * @return the Player that is the current user.
 	 */
 	public Player getCurrentPlayer(){
 		//Return the current user stats from the model, but first checks if the user is logged in.
 		if(ParseUser.getCurrentUser() != null){
 			if(m.getCurrentPlayer() == null){
 				m.setCurrentPlayer(db.getCurrentPlayer());
 			}
 			return m.getCurrentPlayer();
 		} else{
 			return null;
 		}
 	}
 
 	//TODO Should errors be sent to listening classes insead of thrown? We can use boolean-returns if necessary.
 
 	/**
 	 * Register a player
 	 * @param inputNickname - The player
 	 * @param inputEmail - The registered email
 	 * @param inputPassword - the password
 	 * @param inputRepeatPassword - the password, repeated
 	 * @throws DatabaseException - if the connection fails
 	 */
 	public void registerPlayer(
 			String inputNickname, 
 			String inputEmail, 
 			String inputPassword, 
 			String inputRepeatPassword) throws DatabaseException{
 		db.registerPlayer(inputNickname, inputEmail, inputPassword,inputRepeatPassword);
 	}
 
 	/**
 	 * Resets the password connected to the provided email address
 	 * @param email - The email address connected to an account.
 	 * @throws DatabaseException - if the connection fails 
 	 */
 	public void resetPassword(String email) throws DatabaseException{
 		db.resetPassword(email);
 	}
 
 	//Players -----------------------------------------------------------
 
 	/**
 	 * Get a user by its ParseId - fetched locally if possible
 	 * @param parseId - the players ParseId
 	 * @return A player
 	 * @throws DatabaseException - if the connection to the database fails
 	 */
 	public Player getPlayerById(String parseId) throws DatabaseException {
 		Player p = m.getPlayerById(parseId);
 		if(p == null){
 			//If the player is not present in the model, we must call the database.
 			p = db.getPlayerById(parseId);
 			m.putPlayer(p);
 		}
 		return p;
 	}
 	//As both the parseId and the username are unique for each player, both of these methods can be nice to have.
 	/**
 	 * Get a user by its username
 	 * @param username - the players username
 	 * @return A player
 	 * @throws DatabaseException - if the connection to the database fails
 	 */
 	public Player getPlayer(String username) throws DatabaseException {
 		Player p = m.getPlayer(username);
 		if(p == null){
 			p = db.getPlayer(username);
 			m.putPlayer(p);
 		}
 		return p;
 	}
 	/**
 	 * Return an updated instace of the provided player
 	 * @param player - the player to update
 	 * @return an updated Player object
 	 */
 	public Player getUpdatePlayer(Player player){
 		//TODO implement this on account screen
 		if(player != null){
 			try{
 				return db.getPlayerById(player.getParseId());
 			} catch(DatabaseException e){
 				setChanged();
 				notifyObservers(new DCMessage(DCMessage.ERROR, e.prettyPrint()));
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns a list of all players as objects from the database. 
 	 * 	The local list of players is updated as well.
 	 * @return An ArrayList with Players
 	 * @throws DatabaseException
 	 */
 	public ArrayList<Player> getAllPlayerObjects() throws DatabaseException {
 		//Get all players from the database
 		ArrayList<Player> players = db.getPlayers();
 		//Save all players locally (Will hopefully save some requests)
 		m.putPlayers(players);
 		return players;
 	}
 
 	/**
 	 * Returns a list with all player names. The players will also be cached locally.
 	 * @return an ArrayList containing 
 	 * @throws DatabaseException - if the connection to the database fails
 	 */
 	public TreeSet<String> getAllPlayerNames() throws DatabaseException {
 		//Gets all players as player objects and retrieves a list of usernames from these
 		ArrayList<Player> players = getAllPlayerObjects();
 		TreeSet<String> nameList = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
 		for(Player p : players){
 			nameList.add(p.getName());
 		}
 		return nameList;
 	}
 
 	/**
 	 * Returns a list with all player names, except the current user. All players will be cached locally
 	 * @return an ArrayList containing names
 	 * @throws DatabaseException - if the connection to the database fails
 	 */
 	public TreeSet<String> getAllOtherPlayerNames() throws DatabaseException {
 		TreeSet<String> nameList = getAllPlayerNames();
 		nameList.remove(getCurrentPlayer().getName());
 		return nameList;
 	}
 
 	//Games -----------------------------------------------------------
 
 	/**
 	 * Puts the current user in the random queue. The player is only allowed to
 	 * 	have one place in this queue at a time.
 	 */
 	public void putInRandomQueue(){
 		db.putIntoRandomQueue(getCurrentPlayer());
 	}
 	/**
 	 * Create a game. The local storage will not be updated, but fetched during next update.
 	 * @param p1 - player 1
 	 * @param p2 - player 2
 	 * @throws DatabaseException - if the connection to the database fails
 	 */
 	public void createGame(Player p1, Player p2){
 		db.createGame(p1, p2);
 	}
 
 	/**
 	 * Return the local version of the game by its gameId
 	 * @param gameId - the GameId
 	 * @return A Game, or null if it doesn't exist
 	 */
 	public Game getGame(String gameId){
 		return m.getGame(gameId);
 	}
 
 	/**
 	 * Gets a list of current cached games 
 	 */
 	public ArrayList<Game> getGames(){
 		return m.getGames();
 	}
 
 	/**
 	 * Fetches games from the database (updates model on reply)
 	 */
 	public void fetchGames(){
 		//Fetches the db-list of current games
 		db.fetchGames(getCurrentPlayer());
 	}
 
 	/**
 	 * This method compares the local cashed games against the fetched games from the database.
 	 * 	The game which is ahead is updated in both positions.  
 	 * @param dbGames
 	 * @return an ArrayList with games
 	 */
 	private ArrayList<Game> parseGameList(TreeMap<Game, ArrayList<Turn>> dbGames){
 		Game localGame;
 		for(Map.Entry<Game, ArrayList<Turn>> dbGame : dbGames.entrySet()){
 			//Fetch the local version of the game
 			localGame = m.getGame(dbGame.getKey().getGameId());
 			//If this game doesn't exist, create it
 			if(localGame == null){
 				m.putGame(dbGame.getKey());
 				m.putTurns(dbGame.getValue());
 				if(dbGame.getKey().isFinished() 
 						&& dbGame.getKey().getPlayer2().equals(getCurrentPlayer())){
 					//If the current player is p2 receives 
 					updatePlayerStats(getCurrentPlayer(), dbGame.getKey());
 					db.removeGame(dbGame.getKey());
 					removeVideofromServer(dbGame.getKey());
 				}
 			} else if(localGame.aheadOf(dbGame.getKey())){
 				//Updating the database is done in updateTurn as well, although we cannot
 				// rely solely on that method, as it might cause sync problems.
 				db.updateGame(localGame);
 				db.updateTurns(m.getTurns(localGame));
 			} else if(dbGame.getKey().aheadOf(localGame)){
 				//Update local - Also check for finish an delete db from db.
 				m.putGame(dbGame.getKey());
 				m.putTurns(dbGame.getValue());
 				if(dbGame.getKey().isFinished() 
 						&& dbGame.getKey().getPlayer2().equals(getCurrentPlayer())){
 					updatePlayerStats(getCurrentPlayer(), dbGame.getKey());
 					db.removeGame(dbGame.getKey());
 					removeVideofromServer(dbGame.getKey());
 				}
 			} 
 			/*
 			 * This is done to ensure that there are no errors in the current game
			 */
			else {
				/*
				 * Test to see if a received game does not have any turns.
				 * 	In that event, delete the local turn and try to fetch next time.
				 * 	TODO If this is repeated three times, we delete the object from the database. 
				 */
				if(dbGame.getValue() == null || dbGame.getValue().isEmpty()){
					m.removeGame(dbGame.getKey());
				} 
				/*
				 * 	If there is a missmatch between current player and turn number/state, the local games are considered
				 * 	as corrupted, and will be replaced by the database-games.
				 *  This test should not be done on finished games, as it then deletes this game from local storage.	
				 */	
				else if (
 						((localGame.getCurrentPlayer().equals(m.getCurrentTurn(localGame).getRecPlayer()) && m.getCurrentTurn(localGame).getState() != Turn.INIT)
 								||
 								(localGame.getCurrentPlayer().equals(m.getCurrentTurn(localGame).getAnsPlayer()) && m.getCurrentTurn(localGame).getState() != Turn.VIDEO))
 								&& (!localGame.isFinished())
 						){
 					m.putGame(dbGame.getKey());
 					m.putTurns(dbGame.getValue());
				}
 			}
 		}
 		removeOldGames(new ArrayList<Game>(dbGames.keySet()));
 		return m.getGames();
 	}

 	/**
 	 * This part removes any games that are "to old".
 	 * @param dbGames - the list of games received form the database.
 	 */
 	private void removeOldGames(ArrayList<Game> dbGames){
 		ArrayList<Game> finishedGames = new ArrayList<Game>();
 		for(Game locGame : m.getGames()){
 			if(locGame.isFinished()){
 				//Add all finished games to a list
 				finishedGames.add(locGame);
 			} else if(!dbGames.contains(locGame)){
 				//remove the local game if it is not found on the server 
 				// games can then be deleted from the database and updated on each device
 				m.removeGame(locGame);
 				// Also remves the turns associated with this game.
 				db.removeTurnsOfGame(locGame);
 			}
 		}
 		/*
 		 * We only want to display the 10 first games (defined in static variable in method)
 		 * 	Any older games are removed. We also delete games that have been finished for more then 1 week 
 		 */
 		if(finishedGames.size() > 0){
 			//Sort the games using a custom comparator, based on time.
 			//	The newest games are preferred in the final list, which is why we sort the list
 			Collections.sort(finishedGames, new Comparator<Game>(){
 				@Override
 				public int compare(Game g1, Game g2) {
 					return (int) (g1.getLastPlayed().getTime() - g2.getLastPlayed().getTime()); 
 				}
 			});
 			long timeDiff;
 			int numberSaved = 0;
 			for(Game game : finishedGames){
 				//All games that are older then the 10th game is removed.
 				if(numberSaved > Model.FINISHEDGAMES_NUMBERSAVED){
 					m.removeGame(game);
 				} else{
 					//Compare the timestamps: if its older then 168h (1 week), we delete i
 					timeDiff =  ((new Date()).getTime() - game.getLastPlayed().getTime()) 
 							/ (1000L * 3600L);
 					if(timeDiff > 168){
 						m.removeGame(game);
 					} else{
 						numberSaved ++;
 					}
 				}
 			}
 		}
 	}
 	/**
 	 * Removes video on the FTP server from the finished games.
 	 */
 	private void removeVideofromServer(Game game) {
 		RemoveVideoFromServer remove = new RemoveVideoFromServer(game.getGameId());
 		remove.execute();
 	}
 
 	/**
 	 * Retrieves the gamescore for a game, divided by player.
 	 * @param game - the game to fetch
 	 * @return A TreeMap with players linked to scores. Use get(Player) to get the score.
 	 */
 	public TreeMap<Player, Integer> getGameScore(Game game){
 		TreeMap<Player, Integer> returnMap = new TreeMap<Player, Integer>();
 		ArrayList<Turn> turnList = null;
 		if(game != null){
 			turnList = getTurns(game);
 		} else{
 			return returnMap;
 		}
 		if(turnList != null){
 			Player p1 = game.getPlayer1();
 			Player p2 = game.getPlayer2();
 			int p1s = 0;
 			int p2s = 0;
 			for(Turn turn : turnList){
 				p1s += turn.getPlayerScore(p1);
 				p2s += turn.getPlayerScore(p2);
 			}
 			returnMap.put(p1, p1s);
 			returnMap.put(p2, p2s);
 		} else{
 			//If there are no turns
 			returnMap.put(game.getPlayer1(), 0);
 			returnMap.put(game.getPlayer2(), 0);
 		}
 		return returnMap;
 	}
 
 	//Turn -----------------------------------------------------------
 	/**
 	 * Get all turns cached turns of a game. These are updated from the database whenever getGames() is called.
 	 * @param game - The game who's turns to fetch
 	 * @return An ArrayList of turns
 	 */
 	public ArrayList<Turn> getTurns(Game game){
 		return m.getTurns(game);
 	}
 
 	/**
 	 * Update the game to the database and model. This method will update both games and turns.
 	 * @param turn - the current turn of the game.
 	 */
 	public void updateGame(Turn turn) {
 		//Save turn to database and model.
 		m.putTurn(turn);
 		db.updateTurn(turn);
 		//Fetch the local version of the game
 		Game game = m.getGame(turn.getGameId());
 		//Set player and turn number depending on the state of the turn.
 		switch(turn.getState()){
 		case Turn.INIT : 	game.setCurrentPlayer(turn.getRecPlayer());
 		break;
 		case Turn.VIDEO : 	game.setCurrentPlayer(turn.getAnsPlayer());
 		break;
 		case Turn.FINISH : 	game.incrementTurn();//Also sets game to finished!
 		break;
 		}
 		game.setLastPlayed(new Date());
 		if(game.isFinished()){
 			/*
 			 * Update player stats. Due to write restrictions of Parse, 
 			 * 	this method has to be called by the current, logged in, player. 
 			 */
 			updatePlayerStats(getCurrentPlayer(), game);
 		}
 		//Update the local and db game instace. 
 		db.updateGame(game);
 		m.putGame(game);
 	}
 
 	/**
 	 * Calculates and updates a players global statistics 
 	 * 	- this has do be done by the logged in player!
 	 * @param p - The player to update. This player has to be logged in.
 	 * @param g - The game to update score from
 	 */
 	private void updatePlayerStats(Player p, Game g){
 		TreeMap<Player, Integer> scoreMap = getGameScore(g);
 		Player p1 = p;
 		Player p2 = g.getOpponent(p);
 		//Get scores of the current games
 		int p1GS = scoreMap.get(p1);
 		int p2GS = scoreMap.get(p2);
 		int p1W = 0;
 		int p1L = 0;
 		int draw = 0;
 		if(p1GS > p2GS){
 			p1W++;
 		} else if(p2GS > p1GS){
 			p1L++;
 		} else{
 			draw++;
 		}
 		//Update db with stats
 		db.incrementPlayerStats(p1, p1GS, p1W, draw, p1L);
 	}
 
 
 	//Invitation -----------------------------------------------------------
 
 	/**
 	 * A method to get all current invitations from the database
 	 */
 	public void getInvitations(){
 		db.getInvitations(getCurrentPlayer());
 	}
 	/**
 	 * A private method to parse received database invitations.
 	 * @param dbInv - received invitations from database.
 	 * @return A List of current invitations. The list will be of size 0 if no elements are found.
 	 */
 	private List<Invitation> parseDbInvitations(List<Invitation> dbInv){
 		//The date and timedifference is used for comparison later
 		Date currentTime = new Date();
 		long timeDifference;
 		//These will be removed later
 		LinkedList<Invitation> oldInvitations = new LinkedList<Invitation>();
 		//A list of ALL current invitations
 		LinkedList<Invitation> currentInvitations = new LinkedList<Invitation>();
 		//A list of all sent invitations
 		LinkedList<Invitation> sentInvitations = new LinkedList<Invitation>();
 		//A list of all sent invitations
 		LinkedList<Invitation> receivedInvitations = new LinkedList<Invitation>();		
 		for(Invitation inv : dbInv){
 			timeDifference = (currentTime.getTime() - inv.getTimeOfInvite().getTime()) / (1000L*3600L);
 			//if the invitations are considered to old OR already in current games
 			if(timeDifference > Model.INVITATIONS_SAVETIME 
 					|| inCurrentGames(inv)){
 				oldInvitations.add(inv);
 			} else if(!currentInvitations.contains(inv)){
 				//We only allow one instace of an invitation to appear in the list
 				currentInvitations.add(inv);
 				if(inv.getInviter().equals(getCurrentPlayer())){
 					sentInvitations.add(inv);
 				} else{
 					receivedInvitations.add(inv);
 				}
 			}
 		}
 		//Remove old invitations from the database
 		db.removeInvitations(oldInvitations);
 		m.setSentInvitations(sentInvitations);
 		m.setReceivedInvitations(receivedInvitations);
 		return currentInvitations;
 	}
 	/**
 	 * Helper method to check if Invitation is in current games.
 	 * @param inv - Invitation to check validity of
 	 */
 	private boolean inCurrentGames(Invitation inv){
 		for(Game g : m.getGames()){ //Check if the invitation is in current (not finished) games
 			//If the game isn't finished AND that the combination of inviter/invitee doesn't exist in any games. 
 			if(		!g.isFinished() &&
 					(inv.getInviter().equals(g.getPlayer1()) 
 							&& inv.getInvitee().equals(g.getPlayer2()))
 							||
 							(inv.getInvitee().equals(g.getPlayer1())
 									&& inv.getInviter().equals(g.getPlayer2()))
 					){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Retrieves a list of all invitations sent form this device.
 	 * @return An ArrayList containing Invitations
 	 */
 	public List<Invitation> getSentInvitations(){
 		//The cache is updated on each getInvitations() call
 		return m.getSentInvitations();
 	}
 
 	/**
 	 * Returns a set with all players the current player has sent invitations to.
 	 * @return A TreeSet containing String (natural)usernames
 	 */
 	public TreeSet<String> getSentInvitationsAsUsernames(){
 		TreeSet<String> usernames = new TreeSet<String>();
 		List<Invitation> invitations = getSentInvitations();
 		for(Invitation invitation : invitations){
 			usernames.add(invitation.getInvitee().getName());
 		}
 		return usernames;
 	}
 
 	/**
 	 * Get all received invitations for the current player
 	 * @return A List with invitations
 	 */
 	public List<Invitation> getReceivedInvitations(){
 		return m.getReceivedInvitations();
 	}
 
 
 	/**
 	 * Send an invitation to another player
 	 * @param invitation - the invitation to send
 	 */
 	public void sendInvitation(Invitation invitation){
 		db.sendInvitation(invitation);
 	}
 
 	/**
 	 * Send an invitation to another Player (based on the Player class)
 	 * @param player The player-representation of the player
 	 */
 	public void sendInvitation(Player player){
 		Invitation inv = new Invitation(getCurrentPlayer(), player);
 		//Create an invitation instance and send it to the database.
 		sendInvitation(inv);
 		//Add a temp invitation in model
 		m.getSentInvitations().add(inv);
 	}
 
 	/**
 	 * Called in order to accept an invitation and automatically create a game.
 	 * @param invitation - The invitation to accept
 	 */
 	public void acceptInvitation(Invitation invitation){
 		createGame(invitation.getInvitee(), invitation.getInviter());
 		db.removeInvitation(invitation);
 	}
 
 	/**
 	 * Called to reject an invitation, which is then deleted form the database
 	 * @param invitation - The invitation to reject
 	 * @throws DatabaseException
 	 */
 	public void rejectInvitation(Invitation invitation){
 		db.removeInvitation(invitation);
 	}
 
 	/**
 	 * A private class to remove videos in an AsyncTask from the ftp.
 	 * 	Call whenever a video can be considered as consumed to save storage space.
 	 */
 	private class RemoveVideoFromServer extends AsyncTask <Void, Long, Boolean> {
 		String gameId;
 
 		public RemoveVideoFromServer(String game) {
 			/*
 			 * TODO ADAM - CHECK THIS!!! WAS PREVIOUSLY this.gameId = gameIs (was never set)
 			 * 	Was this intentional? Changed this now, but it is untested
 			 */
 			this.gameId = game;
 		}
 
 		@Override
 		protected void onPreExecute(){
 			//We do not have to check anything
 		}
 
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			boolean result = false;
 			FTPClient con = null;
 			try{
 				//Try and create a connection to the FPT client and delete the file.
 				con = new FTPClient();
 				con.connect("ftp.mklcompetencia.se", 21);
 				if (con.login("mklcompetencia.se", "ypkq4w")){ //PlainText is nice...
 					con.enterLocalPassiveMode();
 					result = con.deleteFile(gameId);
 					if (result) {
 						Log.v("deletion on FTP", "succeeded");
 					}
 					con.logout();
 					con.disconnect();
 				}
 			}
 			//Catch exceptions
 			catch (SocketException e){
 				Log.v("download result Socket", e.getMessage());
 				cancel(true);
 			}
 			catch (UnknownHostException e){
 				Log.v("download result Unknown", e.getMessage());
 				cancel(true);
 			}
 			catch (FTPConnectionClosedException e){
 				Log.v("download result FTP CONNECTIONCLOSED", e.getMessage());
 				cancel(true);
 			}
 			catch (CopyStreamException e){
 				Log.v("download result COPYSTREAM", e.getMessage());
 				cancel(true);
 			}
 			catch (IOException e){
 				Log.v("download result IOE", e.getMessage());
 				cancel(true);
 			}
 			catch (Exception e){
 				Log.v("download result just exception","failed " + e.getMessage());
 				cancel(true);
 			}
 			return null;	
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result){
 			//Nothing here as well
 		}
 	}
 }
