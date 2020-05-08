 package com.example.wecharades.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import android.content.Context;
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
 
 	private static DataController dc = null; //TODO this is high coulpling... CODE SMELL
 	private Model m;
 	private IDatabase db;
 	private DataController(Context context){
 		m = Model.getModelInstance(context);
 		db = Database.getDatabaseInstance(context);
 		db.setConverter(this);
 		db.addObserver(this);
 	}
 
 	public static DataController getDataController(Context context){
 		if(dc == null){
 			dc = new DataController(context);
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
 		if(db.getClass().equals(Database.class)
 				& obj != null){
 			if(obj.getClass().equals(DatabaseException.class)){
 				setChanged();
 				notifyObservers((DatabaseException)obj);
 			} else if(obj instanceof TreeMap){
 				ArrayList<Game> gameList = retrieveUpdatedGameList((TreeMap<Game, ArrayList<Turn>>) obj);
 				setChanged();
 				notifyObservers(gameList);
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
 		m.logOutCurrentPlayer(context);
 		db.logOut();
 		//Dereference the model
 		m = null;
 	}
 
 	/**
 	 * returns the current user
 	 * @return
 	 */
 	public Player getCurrentPlayer(){
 		if(m.getCurrentPlayer() == null){
 			m.setCurrentPlayer(db.getCurrentPlayer());
 		}
 		return m.getCurrentPlayer();
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
 	 * @param p2 - palyer 2
 	 * @throws DatabaseException - if the connection to the database fails
 	 */
 	public void createGame(Player p1, Player p2) throws DatabaseException{
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
 	/*
 	 * This is one of the core methods of this application.
 	 * 	This method will sync the database with the model!
 	 * 	//TODO This MIGHT have problems with it and is untested
 	 */
 	private ArrayList<Game> retrieveUpdatedGameList(TreeMap<Game, ArrayList<Turn>> dbGames) {
 		Game localGame;
 		for(Map.Entry<Game, ArrayList<Turn>> gameMap : dbGames.entrySet()){
 			localGame = m.getGame(gameMap.getKey().getGameId());
 			if(localGame == null || m.getTurns(localGame) == null){
 				//If the local game does not exist, or does not have any turns
 				m.putGame(gameMap.getKey());
 				m.putTurns(gameMap.getValue());
 			} else if(Game.hasChanged(localGame, gameMap.getKey())){
 				Log.d("WORKS?", "YES!");
 				if(localGame.getTurnNumber() < gameMap.getKey().getTurnNumber()){
 					Log.d("DC: update", "Run if the local turn is older than the db one");
 					//Run if the local turn is older than the db one.
 					//It can then be deduced that the local turns are out-of-date.
 					//Because of the saveEventually, we do not have to check the other way around.
 					m.putGame(gameMap.getKey());
 					m.putTurns(gameMap.getValue());
 				} else if(localGame.isFinished() 
 						&& !localGame.getCurrentPlayer().equals(getCurrentPlayer())){ 
 					Log.d("DC: update", "This code deletes games and turns after they are finished!");
 
 					//This code deletes games and turns after they are finished!
 					//This code is only reachable for the receiving player
 					db.removeGame(localGame);
 				} else if(!localGame.getCurrentPlayer().equals(gameMap.getKey().getCurrentPlayer())){
 					//If current player of a game is different, we must check the turns
 					Log.d("DC: update", "If current player of a game is different, we must check the turns");
 					Turn localTurn = m.getCurrentTurn(localGame);
 					Turn dbTurn = gameMap.getValue().get(gameMap.getKey().getTurnNumber()-1);
 					if(localTurn.getState() > dbTurn.getState()){
 						//Update db.turn if local version is further ahead
 						db.updateTurn(localTurn);
 					} else {
 						//If something is wrong, allways use the "Golden master" - aka. the database
 						m.putTurn(dbTurn);
 					}
 				}
 			}
 		}
 		removeOldGames();
 
 		return m.getGames();
 	}
 	/*
 	 * This part removes any games that are "to old".
 	 */
 	private void removeOldGames(){
 		ArrayList<Game> finishedGames = new ArrayList<Game>(); 
 		for(Game locGame : m.getGames()){
 			if(locGame.isFinished())
 				finishedGames.add(locGame);
 		}
 		if(finishedGames.size() > 0){
 			//Sort the games using a cusom time-comparator
 			Collections.sort(finishedGames, new Comparator<Game>(){
 				@Override
 				public int compare(Game g1, Game g2) {
 					return (int) (g1.getLastPlayed().getTime() - g2.getLastPlayed().getTime()); 
 				}
 			});
 			//Removes games that are to old - also with a number restriction.
 			//The newest gemes are preferred (which is why we sort the list)
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
 			for(int i=0; i < game.getTurnNumber(); i++){
 				currentTurn = turnList.get(i);
 				p1s += currentTurn.getPlayerScore(p1);
 				p2s += currentTurn.getPlayerScore(p2);
 			}
 			returnMap.put(p1, p1s);
 			returnMap.put(p2, p2s);
 		} else{
 			returnMap.put(game.getPlayer1(), 0);
 			returnMap.put(game.getPlayer2(), 0);
 		}
 		return returnMap;
 	}
 
 	/**
 	 * Updates the database for the game. 
 	 * 	if the turn is finished, this will also be set here.
 	 * @param game - the game to be updated
 	 * @throws DatabaseException
 	 */
 	private void updateGame(Game game) throws DatabaseException{
 		if(isFinished(game)){
 			game.setFinished();
 		}
 		db.updateGame(game);
		db.updateTurn(m.getCurrentTurn(game)); //Put this above the switch clause in updateTurn() and it should work.
 	}
 	/*
 	 * Helper method for updateGame()
 	 */
 	private boolean isFinished(Game game){
 		return (game.getTurnNumber() == 6) && (m.getCurrentTurn(game).getState() == Turn.FINISH);
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
 
 	public void updateTurn(Turn turn) throws DatabaseException{
 		m.putTurn(turn);
 		Game game = m.getGame(turn.getGameId());
 		switch(turn.getState()){
 		case Turn.INIT : 	game.setCurrentPlayer(turn.getRecPlayer());
 		break;
 		case Turn.VIDEO : 	game.setCurrentPlayer(turn.getAnsPlayer());
 		break;
 		case Turn.FINISH : 	game.incrementTurn();
 		break;
 		}
 		game.setLastPlayed(new Date());
 		updateGame(game);
 	}
 
 
 	//Invitation -----------------------------------------------------------
 
 	/**
 	 * A method to get all current invitations from the database
 	 * @return
 	 * @throws DatabaseException
 	 */
 	public ArrayList<Invitation> getInvitations() throws DatabaseException{
 		ArrayList<Invitation> invitations = db.getInvitations(getCurrentPlayer());
 		Date currentTime = new Date();
 		long timeDifference;
 		ArrayList<Invitation> oldInvitations = new ArrayList<Invitation>();
 		for(Invitation inv : invitations){
 			timeDifference = (currentTime.getTime() - inv.getTimeOfInvite().getTime()) / (1000L*3600L);
 			if(timeDifference > Model.INVITATIONS_SAVETIME){ //if the invitations are considered to old
 				oldInvitations.add(inv);
 				invitations.remove(inv);
 			}
 		}
 		db.removeInvitations(oldInvitations);
 		return invitations;
 	}
 
 	/**
 	 * Retrieves a list of all invitations sent form this device.
 	 * @return An ArrayList containing Invitations
 	 */
 	public ArrayList<Invitation> getSentInvitations(){
 		return m.getSentInvitations();
 	}
 
 	/**
 	 * Returns a set with all players the current player has sent invitations to.
 	 * @return A TreeSet containing String (natural)usernames
 	 */
 	public TreeSet<String> getSentInvitationsAsUsernames(){
 		TreeSet<String> usernames = new TreeSet<String>();
 		ArrayList<Invitation> invitations = getSentInvitations();
 		for(Invitation invitation : invitations){
 			usernames.add(invitation.getInvitee().getName());
 		}
 		return usernames;
 	}
 
 	/**
 	 * Send an invitation to another player
 	 * @param invitation
 	 */
 	public void sendInvitation(Invitation invitation){
 		if(!m.getSentInvitations().contains(invitation)){
 			m.setSentInvitation(invitation);
 			db.sendInvitation(invitation);
 		}
 	}
 
 	/**
 	 * Send an invitation to another Player (based on the Player class)
 	 * @param player The player-representation of the player
 	 */
 	public void sendInvitation(Player player){
 		sendInvitation(new Invitation(getCurrentPlayer(), player, new Date()));
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
 
 }
