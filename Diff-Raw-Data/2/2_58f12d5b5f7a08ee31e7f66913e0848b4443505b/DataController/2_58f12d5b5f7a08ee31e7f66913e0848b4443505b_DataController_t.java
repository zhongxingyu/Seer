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
 
 
 /**
  * This is a class intended as an interface to the model and database.
  * 	All requests for DATA should be run through this class, which will handle
  * 	the logic of fetching cached data and retrieving form the database.
  * 	This class will also house the logic for pushing and updating data to both  
  * @author Anton Dahlstrm
  *
  */
 public class DataController extends Observable implements Observer{
 	//TODO Delete later
 	public Game getGame(String gameId){
 		return m.getGame(gameId);
 	}
 
 	private static boolean RECREATE = false;
 
 	private static DataController dc;
 	private Model m;
 	private IDatabase db;
 	private DataController(Context context){
 		m = Model.getModelInstance(context);
 		db = Database.getDatabaseInstance(context);
 		db.setConverter(this);
 		db.addObserver(this);
 	}
 
 	public static DataController getDataController(Context context){
 		if(dc == null || RECREATE){
 			dc = new DataController(context);
 			RECREATE = false;
 		}
 		return dc;
 	}
 
 	public void saveState(Context context){
 		if(m != null)
 			m.saveModel(context);
 	}
 
 	/**
 	 * This method is called when the database has finished fetching turn and game data.
 	 */
 	@Override
 	public void update(Observable db, Object obj) {
 		if(obj != null && obj.getClass().equals(DBMessage.class)){
 			DBMessage dbm = (DBMessage) obj;
 			if(dbm.getMessage() == DBMessage.ERROR){
 				setChanged();
 				notifyObservers(new DCMessage(DCMessage.ERROR, ((DatabaseException) dbm.getData()).prettyPrint()));
 			} else if(dbm.getMessage() == DBMessage.MESSAGE){
 				setChanged();
 				notifyObservers(new DCMessage(DCMessage.MESSAGE, (String) dbm.getData())); 
 			} else if(dbm.getMessage() == DBMessage.GAMELIST){
 				ArrayList<Game> gameList = parseGameList((TreeMap<Game, ArrayList<Turn>>) dbm.getData());
 				setChanged();
 				notifyObservers(new DCMessage(DCMessage.DATABASE_GAMES, gameList));
 			} else if(dbm.getMessage() == DBMessage.INVITATIONS){
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
 		RECREATE = true;
 	}
 
 	/**
 	 * returns the current user
 	 * @return
 	 */
 	public Player getCurrentPlayer(){
 		return db.getCurrentPlayer();
 	}
 
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
 			String inputRepeatPassword
 			) throws DatabaseException{
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
 	 * Get a user by its ParseId
 	 * @param parseId - the players ParseId
 	 * @return A player
 	 * @throws DatabaseException - if the connection to the database fails
 	 */
 	protected Player getPlayerById(String parseId) throws DatabaseException {
 		Player p = m.getPlayerById(parseId);
 		if(p == null){
 			p = db.getPlayerById(parseId);
 			m.putPlayer(p);
 		}
 		return p;
 	}
 
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
 	 * Returns a list of all players as objects
 	 * @return An ArrayList with players
 	 * @throws DatabaseException
 	 */
 	public ArrayList<Player> getAllPlayerObjects() throws DatabaseException {
 		ArrayList<Player> players = db.getPlayers();
 		m.putPlayers(players);
 		return players;
 	}
 
 	/**
 	 * Returns a list with all player names. This list will also be cached locally.
 	 * @return an ArrayList containing 
 	 * @throws DatabaseException - if the connection to the database fails
 	 */
 	public TreeSet<String> getAllPlayerNames() throws DatabaseException {
 		ArrayList<Player> players = getAllPlayerObjects();
 		TreeSet<String> nameList = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
 		for(Player p : players){
 			nameList.add(p.getName());
 		}
 		return nameList;
 	}
 
 	/**
 	 * Returns a list with all player names. This list will also be cached locally.
 	 * @return an ArrayList containing 
 	 * @throws DatabaseException - if the connection to the database fails
 	 */
 	public TreeSet<String> getAllOtherPlayerNames() throws DatabaseException {
 		TreeSet<String> nameList = getAllPlayerNames();
 		nameList.remove(getCurrentPlayer().getName());
 		return nameList;
 	}
 
 	/**
 	 * 
 	 * @return an ArrayList with Players
 	 */
 	public ArrayList<Player> getTopTenPlayers() throws DatabaseException {
 		return db.getTopTenPlayers();
 	}
 
 	//Games -----------------------------------------------------------
 	public void putInRandomQueue(){
 		db.putIntoRandomQueue(getCurrentPlayer());
 	}
 	/**
 	 * Create a game. The local storage will not be updated
 	 * @param p1 - player 1
 	 * @param p2 - player 2
 	 * @throws DatabaseException - if the connection to the database fails
 	 */
 	public void createGame(Player p1, Player p2){
 		db.createGame(p1, p2);
 	}
 
 	/**
 	 * Gets a list of current games. This should only be called from the StartPresenter,
 	 * 	as it updates the game-list from the database. If a game has changed, its current turn will be updated.
 	 * @throws DatabaseException - if the connection to the database fails
 	 */
 	public ArrayList<Game> getGames(){
 		//Fetches the db-list of current games
 		db.fetchGames(getCurrentPlayer());
 		return m.getGames();
 	}
 
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
 					db.removeGame(dbGame.getKey());
 					removeVideofromServer(dbGame.getKey());
 				}
 			} else if(localGame.aheadOf(dbGame.getKey())){
 				//This is also done in updateTurn, but for safety even here. Also updates ALL turns.
 				db.updateGame(localGame);
 				db.updateTurns(m.getTurns(localGame));
 			} else if(dbGame.getKey().aheadOf(localGame)){
 				//Update local - Also check for finish an delete db from db.
 				m.putGame(dbGame.getKey());
 				m.putTurns(dbGame.getValue());
 				if(dbGame.getKey().isFinished()){
 					db.removeGame(dbGame.getKey());
 					removeVideofromServer(dbGame.getKey());
 				}
 			} else{
 				if ( //If there is a missmatch between current player and turn number/state.
 						(localGame.getCurrentPlayer().equals(m.getCurrentTurn(localGame).getRecPlayer()) && m.getCurrentTurn(localGame).getState() != Turn.INIT)
 						||
 						(localGame.getCurrentPlayer().equals(m.getCurrentTurn(localGame).getAnsPlayer()) && m.getCurrentTurn(localGame).getState() != Turn.VIDEO)
 						){
 					m.putGame(dbGame.getKey());
 					m.putTurns(dbGame.getValue());
 				}
 			}
 		}
 		removeOldGames(new ArrayList<Game>(dbGames.keySet()));
 		return m.getGames();
 	}
 	/*
 	 * This part removes any games that are "to old".
 	 */
 	private void removeOldGames(ArrayList<Game> dbGames){
 		ArrayList<Game> finishedGames = new ArrayList<Game>();
 		for(Game locGame : m.getGames()){
 			if(locGame.isFinished()){
 				finishedGames.add(locGame);
 			} else if(!dbGames.contains(locGame)){
 				//remove the local game if it is not found on the server
 				m.removeGame(locGame);
 			}
 		}
 		if(finishedGames.size() > 0){
 			//Sort the games using a custom time-comparator
 			Collections.sort(finishedGames, new Comparator<Game>(){
 				@Override
 				public int compare(Game g1, Game g2) {
 					return (int) (g1.getLastPlayed().getTime() - g2.getLastPlayed().getTime()); 
 				}
 			});
 			//Removes games that are to old - also with a number restriction.
 			//The newest games are preferred (which is why we sort the list)
 			long timeDiff;
 			int numberSaved = 0;
 			for(Game game : finishedGames){
 				if(numberSaved > Model.FINISHEDGAMES_NUMBERSAVED){
 					m.removeGame(game);
 				} else{
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
 	/*
 	 * Removes video on the FTP server from the finished games.
 	 */
 	private void removeVideofromServer(Game game) {
 		RemoveVideoFromServer remove = new RemoveVideoFromServer(game.getGameId());
 		remove.execute();
 	}
 
 	public TreeMap<Player, Integer> getGameScore(Game game){
 		TreeMap<Player, Integer> returnMap = new TreeMap<Player, Integer>();
 		ArrayList<Turn> turnList = null;
 		if(game != null){
 			turnList = getTurns(game);
 		}
 		if(turnList != null){
 			Player p1 = game.getPlayer1();
 			Player p2 = game.getPlayer2();
 			int p1s = 0;
 			int p2s = 0;
 			Turn currentTurn;
 			for(Turn turn : turnList){
 				p1s += turn.getPlayerScore(p1);
 				p2s += turn.getPlayerScore(p2);
 			}
 			returnMap.put(p1, p1s);
 			returnMap.put(p2, p2s);
 		} else{
 			returnMap.put(game.getPlayer1(), 0);
 			returnMap.put(game.getPlayer2(), 0);
 		}
 		return returnMap;
 	}
 
 	//Turn -----------------------------------------------------------
 	/**
 	 * Get all turns for a game. These are all collected from the stored instance - updated at startscreen.
 	 * @param game - The game who's turns to fetch
 	 * @return An ArrayList of turns
 	 */
 	public ArrayList<Turn> getTurns(Game game){
 		return m.getTurns(game);
 	}
 
 	public void updateTurn(Turn turn) {
 		m.putTurn(turn);
 		db.updateTurn(turn);
 		Game game = m.getGame(turn.getGameId());
 		switch(turn.getState()){
 		case Turn.INIT : 	game.setCurrentPlayer(turn.getRecPlayer());
 		break;
 		case Turn.VIDEO : 	game.setCurrentPlayer(turn.getAnsPlayer());
 		break;
 		case Turn.FINISH : 	game.incrementTurn(); //Also sets game to finished!
 		break;
 		}
 		if(game.isFinished()){ //Update player stats
 			TreeMap<Player, Integer> scoreMap = getGameScore(game);
 			Player p1 = turn.getRecPlayer(); //This assignment is random, but it doesn't matter
 			Player p2 = turn.getAnsPlayer(); 
 			int p1GS = scoreMap.get(p1);
 			int p2GS = scoreMap.get(p2);
 			int p1W = 0;
 			int p2W = 0;
 			int p1L = 0;
 			int p2L = 0;
 			int draw = 0;
 			if(p1GS > p2GS){
 				p1W++;
 			} else if(p2GS > p1GS){
 				p2W++;
 			} else{
 				draw++;
 			}
 			db.incrementPlayerStats(p1, p1GS, p1W, draw, p1L);
 			db.incrementPlayerStats(p2, p2GS, p2W, draw, p2L);
 		}
 		db.updateGame(game);
 		m.putGame(game);
 	}
 
 
 	//Invitation -----------------------------------------------------------
 
 	/**
 	 * A method to get all current invitations from the database
 	 */
 	public void getInvitations(){
 		db.getInvitations(getCurrentPlayer());
 	}
 	/**
 	 * A method to parse received database invitations.
 	 * @param dbInv - received invitations from database.
 	 * @return A List of current invitations. The list will be of size 0 if no elements are found.
 	 */
 	private List<Invitation> parseDbInvitations(List<Invitation> dbInv){
 		Date currentTime = new Date();
 		long timeDifference;
 		LinkedList<Invitation> oldInvitations = new LinkedList<Invitation>();
 		LinkedList<Invitation> currentInvitations = new LinkedList<Invitation>();
 		LinkedList<Invitation> sentInvitations = new LinkedList<Invitation>();
 		LinkedList<Invitation> receivedInvitations = new LinkedList<Invitation>();		
 		for(Invitation inv : dbInv){
 			timeDifference = (currentTime.getTime() - inv.getTimeOfInvite().getTime()) / (1000L*3600L);
 			//if the invitations are considered to old OR already in current games
 			if(timeDifference > Model.INVITATIONS_SAVETIME 
 					|| inCurrentGames(inv)){
 				oldInvitations.add(inv);
 			} else if(!currentInvitations.contains(inv)){
 				currentInvitations.add(inv);
 				if(inv.getInviter().equals(getCurrentPlayer())){
 					sentInvitations.add(inv);
 				} else{
					receivedInvitations.add(inv);
 				}
 			}
 		}
 		db.removeInvitations(oldInvitations);
 		m.setSentInvitations(sentInvitations);
 		m.setReceivedInvitations(receivedInvitations);
 		return currentInvitations;
 	}
 	/*
 	 * Helper method to check if Invitation is in current games
 	 */
 	private boolean inCurrentGames(Invitation inv){
 		for(Game g : m.getGames()){ //Check if the invitation is in current games
 			if(	
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
 	public List<Invitation> getReceivedInvitations(){
 		return m.getReceivedInvitations();
 	}
 	
 
 	/**
 	 * Send an invitation to another player
 	 * @param invitation
 	 */
 	public void sendInvitation(Invitation invitation){
 		db.sendInvitation(invitation);
 	}
 
 	/**
 	 * Send an invitation to another Player (based on the Player class)
 	 * @param player The player-representation of the player
 	 */
 	public void sendInvitation(Player player){
 		sendInvitation(new Invitation(getCurrentPlayer(), player));
 	}
 
 	/**
 	 * Called in order to accept an invitation and automatically create a game.
 	 * @param invitation - The invitation to accept
 	 * @throws DatabaseException
 	 */
 	public void acceptInvitation(Invitation invitation) throws DatabaseException{
 		createGame(invitation.getInviter(), invitation.getInvitee());
 		db.removeInvitation(invitation);
 	}
 
 	/**
 	 * Called to reject an invitation, which is then deleted form the database
 	 * @param invitaiton - The invitation to reject
 	 * @throws DatabaseException
 	 */
 	public void rejectInvitation(Invitation invitaiton) throws DatabaseException{
 		db.removeInvitation(invitaiton);
 	}
 
 	private class RemoveVideoFromServer extends AsyncTask <Void, Long, Boolean> {
 
 		String gameId;
 
 		public RemoveVideoFromServer(String game) {
 			this.gameId = gameId;
 		}
 
 		@Override
 		protected void onPreExecute(){
 
 		}
 
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			boolean result = false;
 			FTPClient con = null;
 			try{
 				con = new FTPClient();
 				con.connect("ftp.mklcompetencia.se", 21);
 				if (con.login("mklcompetencia.se", "ypkq4w")){
 					con.enterLocalPassiveMode();
 					result = con.deleteFile(gameId);
 					if (result) {
 						Log.v("deletion on FTP", "succeeded");
 					}
 					con.logout();
 					con.disconnect();
 				}
 			}
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
 
 		}
 	}
 
 	//	public void subscribetoNotification(Context context) {
 	//		db.subscribetoNotification(context);
 	//	}
 
 }
