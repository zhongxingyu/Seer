 package com.example.wecharades.model;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.TreeSet;
 
 import android.content.Context;
 
 
 /**
  * This is a class intended as an interface to the model and database.
  * 	All requests for DATA should be run through this class, which will handle
  * 	the logic of fetching cached data and retrieving form the database.
  * 	This class will also house the logic for pushing and updating data to both  
  * @author Anton Dahlstrm
  *
  */
 public class DataController {
 
 	private static DataController dc = null; //TODO this is high coulpling... CODE SMELL
 	private Model m;
 	private IDatabase db;
 	private DataController(Context context){
 		m = Model.getModelInstance(context);
 		db = Database.getDatabaseInstance(context);
 		db.setConverter(this);
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
 
 	/**
 	 * delete the account responding to the current user
 	 */
 	public void deleteAccount() {
 		//TODO: implement
 		m.deleteAccount();
 		db.deleteAccount();
 	}
 
 	//Players -----------------------------------------------------------
 
 	/**
 	 * Get a user by its ParseId
 	 * @param parseId - the players ParseId
 	 * @return A player
 	 * @throws DatabaseException - if the connection to the database fails
 	 */
 	public Player getPlayerById(String parseId) throws DatabaseException {
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
 		ArrayList<Player> players = db.getPlayers();
 		m.putPlayers(players);
 		TreeSet<String> nameList = new TreeSet<String>(new Comparator<String>() {
 			public int compare(String s1, String s2){
 				return s1.compareToIgnoreCase(s2);
 			}
 		});
 		for(Player p : players){
 			nameList.add(p.getName());
 		}
 		nameList.remove(getCurrentPlayer().getName());
 		return nameList;
 	}
 
 	/**
 	 * Returns a list with all player names. This list will also be cached locally.
 	 * @return an ArrayList containing 
 	 * @throws DatabaseException - if the connection to the database fails
 	 */
 	public TreeSet<String> getAllOtherPlayerNames() throws DatabaseException {
 		ArrayList<Player> players = db.getPlayers();
 		m.putPlayers(players);
 		TreeSet<String> nameList = new TreeSet<String>(new Comparator<String>() {
 			public int compare(String s1, String s2){
 				return s1.compareToIgnoreCase(s2);
 			}
 		});
 		for(Player p : players){
 			nameList.add(p.getName());
 		}
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
 	public ArrayList<Game> getGames() throws DatabaseException{
 		ArrayList<Game> games = db.getGames(getCurrentPlayer());
 		Game localGame;
 		for(Game game : games){
 			localGame = m.getGame(game.getGameId());
 			//If the local game hasn't been created locally, fetch all turns and create the game
 			if(localGame == null){
 				m.putGame(game);
 			} 
 			if (m.getTurns(game) == null){
 				m.putTurns(db.getTurns(game));
 			} else if(Game.hasChanged(game, localGame)){
 				//Updates the current turn from the database
 				m.putTurn(db.getTurn(game, game.getTurn()));
 				//If the last turn was finished
 				if(game.getTurn() > localGame.getTurn()){
 					m.putTurn(db.getTurn(game, game.getTurn()-1));
 				}
 			}
 		}
 		//m.putGameList(games);
 		return m.getGames();
 	}
 
 	/**
 	 * Returns a game from its parseId
 	 * @param parseId - the games parseId
 	 * @return A Game, or null if no such game exist
 	 * @throws DatabaseException - if the connection to the database fails
 	 */
 	public Game getGame(String parseId) throws DatabaseException{
 		Game game = m.getGame(parseId);
 		if(game == null){
 			game = db.getGame(parseId);
 			m.putGame(game);
 		}
 		return game;
 	}
 
 	/**
 	 * Updates the database for the game. 
 	 * 	CALL THIS METHOD BEFORE INCREMENTING THE GAME TURN!
 	 * 	if the turn is finished, this will also be set here.
 	 * @param game - the game to be updated
 	 * @throws DatabaseException
 	 */
 	public void updateGame(Game game) throws DatabaseException{
 		if(isFinished(game)){
 			game.setFinished();
 		}
 		db.updateGame(game);
 		db.updateTurn(m.getCurrentTurn(game));
 	}
 	/*
 	 * Helper method for updateGame()
 	 */
 	private boolean isFinished(Game game){
 		return (game.getTurn() == 6) && (m.getCurrentTurn(game).getState() == Turn.FINISH);
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
 			if(timeDifference > 72){ //if the invitations are considered to old
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
 		m.setSentInvitation(invitation);
 		db.sendInvitation(invitation);
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
