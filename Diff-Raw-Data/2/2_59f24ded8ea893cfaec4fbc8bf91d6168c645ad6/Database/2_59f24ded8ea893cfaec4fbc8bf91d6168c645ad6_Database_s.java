 package com.example.wecharades.model;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Stack;
 import java.util.TreeMap;
 import java.util.regex.Pattern;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.nfc.NfcAdapter.CreateNdefMessageCallback;
 import android.util.Log;
 
 import com.example.wecharades.presenter.StartPresenter;
 import com.parse.FindCallback;
 import com.parse.GetCallback;
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 
 /**
  * This class is intended as the interface against the server and database of this game.
  *  
  * @author Anton Dahlstrm
  *
  */
 @SuppressLint("DefaultLocale")
 public class Database extends Observable implements IDatabase {
 
 	//TODO change how dbexceptions are sent
 
 	//This is used to avoid problems with using plain strings when calling the database.
 	public static final String
 	WORDLIST				= "WordList",
 	WORDLIST_WORD			= "word",
 	GAME 					= "Game",
 	GAME_PLAYER_1 			= "player1",
 	GAME_PLAYER_2 			= "player2",
 	GAME_PLAYER_CURRENT 	= "currentPlayer",
 	GAME_TURN 				= "turn",
 	GAME_FINISH 			= "finished",
 	TURN					= "Turn",
 	TURN_GAME				= "game",
 	TURN_TURN				= "turn",
 	TURN_STATE				= "state",
 	TURN_WORD				= "word",
 	TURN_VIDEOLINK			= "videoLink",
 	TURN_PLAYER_REC			= "recPlayer",
 	TURN_PLAYER_REC_SCORE	= "recPlayerScore",
 	TURN_PLAYER_ANS			= "ansPlayer",
 	TURN_PLAYER_ANS_SCORE	= "ansPlayerScore",
 	PLAYER					= "_User",
 	PLAYER_USERNAME			= "username",
 	PLAYER_USERNAME_NATURAL	= "naturalUsername",
 	PLAYER_GLOBALSCORE		= "globalScore",
 	PLAYER_GAMES_PLAYED		= "gamesPlayed",
 	PLAYER_GAMES_LOST		= "gamesLost",
 	PLAYER_GAMES_DRAW		= "gamesDraw",
 	RANDOMQUEUE				= "RandomQueue",
 	RANDOMQUEUE_PLAYER		= "player",
 	INVITE 					= "invite",
 	INVITE_INVITER 			= "inviter",
 	INVITE_INVITEE 			= "invitee";
 
 	private static IDatabase singleton;
 	private DatabaseConverter dbc;
 
 	private Database(Context context){
 		Parse.initialize(context.getApplicationContext(), "p34ynPRwEsGIJ29jmkGbcp0ywqx9fgfpzOTjwqRF", "RZpVAX3oaJcZqTmTwLvowHotdDKjwsi6kXb4HJ0R");
 	}
 
 	public static IDatabase getDatabaseInstance(Context context){
 		if(singleton == null)
 			singleton = new Database(context);
 		return singleton;
 	}
 
 	/**
 	 * Sets the converter for this database.
 	 * @param dc - the Datacontroller
 	 */
 	@Override
 	public void setConverter(DataController dc){
 		dbc = new DatabaseConverter(dc);
 	}
 
 	/**
 	 * Randomly get 6 unique word from the database 
 	 * @return an ArrayList with 6 words 
 	 */
 	private void getWords(final Player player1, final Player player2){
 		final Database db = this;
 		ParseQuery query = new ParseQuery(WORDLIST);
 		query.findInBackground(new FindCallback(){
 			public void done(List<ParseObject> dblist, ParseException e){
 				if(e == null && db != null){
 					Stack<String> wordList = new Stack<String>();
 					for(ParseObject word : dblist){
 						wordList.add(word.getString("word"));
 					}
 					Collections.shuffle(wordList);
 					db.createGameInBackground(player1, player2, wordList);
 				} else{
 					Log.d("Database", "Failed to find word");
 				}
 			}
 		});
 	}
 
 	//Games -----------------------------------------------------------------------------------------//	
 
 	/* (non-Javadoc)
 	 * @see com.example.wecharades.model.IDatabase#createGame(com.example.wecharades.model.Player, com.example.wecharades.model.Player)
 	 */
 	@Override
 	public void createGame(Player player1, Player player2){
 		//Fetch the list of words - on Callback, the rest of the game creation will take place
 		getWords(player1, player2);
 	}
 	/*
 	 * Helper methdo for createGame - called after callback from fetching wordlist
 	 */
 	private void createGameInBackground(Player player1, Player player2, Stack<String> wordList){
 		LinkedList<ParseObject> parseList = new LinkedList<ParseObject>();
 
 		ParseObject newGame = new ParseObject(GAME);
 		newGame.put(GAME_PLAYER_1, player1.getParseId());
 		newGame.put(GAME_PLAYER_2, player2.getParseId());
 		newGame.put(GAME_PLAYER_CURRENT, player1.getParseId());
 		newGame.put(GAME_TURN, 1);
 		newGame.put(GAME_FINISH, false);
 		parseList.add(newGame);
 		//Adds all the six turns
 		String recP, ansP;
 		for(int i=1; i <= 6 ; i++){
 			if(i%2 == 0){
 				recP = player2.getParseId();
 				ansP = player1.getParseId();
 			} else{
 				recP = player1.getParseId();
 				ansP = player2.getParseId();
 			}
 			parseList.add(createTurn(newGame, i, wordList.pop(), recP, ansP));
 		}
 		ParseObject.saveAllInBackground(parseList);
 	}
 
 	/**
 	 * Method to delete a game in background
 	 */
 	public void removeGame(Game game){
 		ParseQuery query = new ParseQuery(GAME);
 		query.getInBackground(game.getGameId(), new GetCallback(){
 			public void done(ParseObject game, ParseException e){
 				if(e == null){
 					removeTurns(game);
 					game.deleteEventually();
 				} else{
 					setChanged();
 					notifyObservers(
 							new DBMessage(
 									DBMessage.ERROR
 									, new DatabaseException(e.getCode(), e.getMessage())
 									)
 							);
 				}
 			}
 		});
 	}
 	/*
 	 * Helper method to removeTurns - called when the game has been fetched from the db.
 	 */
 	private void removeTurns(ParseObject game){
 		ParseQuery turnQuery = new ParseQuery(TURN);
 		turnQuery.whereEqualTo(TURN_GAME, game);
 		turnQuery.findInBackground(new FindCallback(){
 			public void done(List<ParseObject> list, ParseException e){
 				if(e == null){
 					for(ParseObject turn : list){
 						turn.deleteEventually();
 					}
 				} else{
 					setChanged();
 					notifyObservers(
 							new DBMessage(
 									DBMessage.ERROR
 									, new DatabaseException(e.getCode(), e.getMessage())
 									)
 							);
 				}
 			}
 		});
 	}
 
 	/* (non-Javadoc)
 	 * @see com.example.wecharades.model.IDatabase#getGame(java.lang.String)
 	 */
 	@Override
 	public Game getGame(String gameId) throws DatabaseException{
 		Game game;
 		try {
 			ParseQuery query = new ParseQuery(GAME);
 			ParseObject dbGame = query.get(gameId);
 			game = dbc.parseGame(dbGame);
 		} catch (ParseException e) {
 			Log.d("Database",e.getMessage());
 			throw new DatabaseException(1001,"Failed to fetch game data");
 		}
 		return game;
 	}
 
 	private ParseObject getGameParseObject(String gameId) throws DatabaseException {
 		ParseObject object = null;
 		ParseQuery query = new ParseQuery("Game");
 		try {
 			object = query.get(gameId);
 		} catch(ParseException e){
 			Log.d("Database", e.getMessage());
 			throw new DatabaseException(1002, "Failed to get ParseObject");
 		}
 
 		return object;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.example.wecharades.model.IDatabase#getGames(com.example.wecharades.model.Player)
 	 */
 	@Override
 	public void fetchGames(Player player){
 		final Database db = this;
 
 		ArrayList<ParseQuery> queries = new ArrayList<ParseQuery>();
 		ParseQuery query1 = new ParseQuery(GAME);
 		query1.whereContains(GAME_PLAYER_1, player.getParseId());
 		ParseQuery query2 = new ParseQuery(GAME);
 		query2.whereContains(GAME_PLAYER_2, player.getParseId());
 
 		queries.add(query1);
 		queries.add(query2); 
 
 		ParseQuery mainQuery = ParseQuery.or(queries);
 
 		mainQuery.findInBackground(new FindCallback(){
 			public void done(List<ParseObject> dbResult, ParseException e){
 				if(e == null){
 					if (db != null){
 						db.getTurnsInBackgrund(dbResult);
 					}
 				} else{
 					setChanged();
 					notifyObservers(
 							new DBMessage(
 									DBMessage.ERROR
 									, new DatabaseException(e.getCode(), e.getMessage())
 									)
 							);
 				}
 			}
 		});
 	}
 	/*
 	 * Helper method to fetch games. Games and turns are fetched in background.
 	 */
 	private void getTurnsInBackgrund(final List<ParseObject> gameList){
 		if(gameList.isEmpty()){
 			//If there are no games, we should still update screen to remove old games!
 			setChanged();
 			notifyObservers(new DBMessage(DBMessage.GAMELIST, new TreeMap<Game, ArrayList<Turn>>()));
 		} else{
 			LinkedList<ParseQuery> gameQueries = new LinkedList<ParseQuery>();
 			for(ParseObject game : gameList){
 				gameQueries.add((new ParseQuery(TURN)).whereEqualTo(TURN_GAME, game));
 			}
 			ParseQuery masterQuery = ParseQuery.or(gameQueries);
 			masterQuery.findInBackground(new FindCallback(){
 				public void done(List<ParseObject> resultList, ParseException e){
 					if(e == null){
 						if(!resultList.isEmpty()){
 							try{
 								ArrayList<Game> games = new ArrayList<Game>();
 								for(ParseObject obj : gameList){
 									games.add(dbc.parseGame(obj)); //TODO This should be fixed later
 								}
 								//First, we create a TreeMap with the games, and an index for reference:
 								TreeMap<Game, ArrayList<Turn>> map = new TreeMap<Game, ArrayList<Turn>>();
 								TreeMap<String, Game> idList = new TreeMap<String, Game>();
 								for(Game game : games){
 									map.put(game, new ArrayList<Turn>());
 									idList.put(game.getGameId(), game);
 								}
 								//Then, we must parse the ParseObjects to turns and add them to the correct list
 								for(ParseObject obj : resultList){
 									Turn turn = dbc.parseTurn(obj); //TODO This should also be fixed.
 									Game g = idList.get(turn.getGameId());
 									ArrayList<Turn> tl = map.get(g);
 									tl.add(turn);
 								}
 								setChanged();
 								notifyObservers(new DBMessage(DBMessage.GAMELIST, map));
 							} catch(DatabaseException e2){
 								setChanged();
 								notifyObservers(new DBMessage(DBMessage.ERROR, e2));
 							}
 						}
 					} else{
 						setChanged();
 						notifyObservers(new DBMessage(DBMessage.ERROR
 								, new DatabaseException(e.getCode(), e.getMessage())));
 					}
 				}
 			});
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.example.wecharades.model.IDatabase#updateGame(com.example.wecharades.model.Game)
 	 */
 	@Override
 	public void updateGame(Game theGame) {
 		final Game game = theGame;
 		ParseQuery query = new ParseQuery(GAME);
 		query.getInBackground(theGame.getGameId(), new GetCallback() {
 			public void done(ParseObject object, ParseException e){
 				if(e == null){
 					//Updates the game on the server with the latest info
 					object.put(GAME_PLAYER_CURRENT, game.getCurrentPlayer().getParseId());
 					object.put(GAME_TURN, game.getTurnNumber());
 					object.saveEventually();
 				} else{
 					Log.d("Database",e.getMessage());
 				}
 			}
 		});
 	}
 
 	//Turn -----------------------------------------------------------------------------------------//	
 
 	/**
 	 * A PRIVATE method to create a new ParseObject-Turn. Not pushed to db.
 	 * 	This is a helper class for create game.
 	 * @param game - the Game ParseObject
 	 * @param turnNumber - an integer representation of the turn number
 	 * @param word - the word of the turn
 	 * @param recPlayer - the parseId of the player that should record
 	 * @param ansPlayer - the parseId of the player that should answer
 	 */
 	private ParseObject createTurn(ParseObject game, int turnNumber, String word, String recPlayer, String ansPlayer) {
 		ParseObject newTurn = new ParseObject(TURN);
 		newTurn.put(TURN_GAME,game);
 		newTurn.put(TURN_TURN,turnNumber);
 		newTurn.put(TURN_STATE,Turn.INIT);
 		newTurn.put(TURN_WORD,word);
 		newTurn.put(TURN_VIDEOLINK,"");
 		newTurn.put(TURN_PLAYER_REC,recPlayer);
 		newTurn.put(TURN_PLAYER_REC_SCORE,0);
 		newTurn.put(TURN_PLAYER_ANS,ansPlayer);
 		newTurn.put(TURN_PLAYER_ANS_SCORE,0);
 		return newTurn;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.example.wecharades.model.IDatabase#getTurn(com.example.wecharades.model.Game, int)
 	 */
 	@Override
 	public Turn getTurn(Game game, int turnNumber) throws DatabaseException{
 		ParseQuery query = new ParseQuery(TURN);
 		query.whereEqualTo(TURN_GAME, getGameParseObject(game.getGameId()));
 		query.whereEqualTo(TURN_TURN, turnNumber);
 		ParseObject turn = null;
 		try {
 			turn = query.getFirst();
 		} catch (ParseException e) {
 			Log.d("Database",e.getMessage());
 			throw new DatabaseException(1004, "Failed to get turn");
 		}
 		return dbc.parseTurn(turn);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.example.wecharades.model.IDatabase#getTurns(com.example.wecharades.model.Game)
 	 */
 	@Override
 	public ArrayList<Turn> getTurns(Game game) throws DatabaseException{
 		ParseQuery query = new ParseQuery(TURN);
 		//We have to do this, as the turn is linked to a parse object
 		query.whereEqualTo(TURN_GAME, getGameParseObject(game.getGameId()));
 		query.addAscendingOrder(TURN_TURN);
 
 		List<ParseObject> dbList = null;
 		try{
 			dbList = query.find();
 		} catch(ParseException e){
 			Log.d("Database", e.getMessage());
 			throw new DatabaseException(1005, "Failed to get turns");
 		}
 		ArrayList<Turn> turnList = new ArrayList<Turn>();
 		for(ParseObject turn : dbList){
 			turnList.add(dbc.parseTurn(turn));
 		}
 		return turnList;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.example.wecharades.model.IDatabase#updateTurn(com.example.wecharades.model.Turn)
 	 */
 	@Override
 	public void updateTurn(Turn theTurn){
 		final Turn turn = theTurn;
 		ParseQuery query = new ParseQuery(TURN);
 		query.whereEqualTo(TURN_GAME, ParseObject.createWithoutData(GAME, turn.getGameId()));
 		/*try { //TODO background activity
 			query.whereEqualTo(TURN_GAME, getGameParseObject(turn.getGameId()));
 		} catch (DatabaseException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}*/
 		query.whereEqualTo(TURN_TURN, turn.getTurnNumber());
 		query.getFirstInBackground(new GetCallback() {
 			public void done(ParseObject dbTurn, ParseException e){
 				if(e == null){
 					dbTurn.put(TURN_STATE,turn.getState());
 					dbTurn.put(TURN_VIDEOLINK, turn.getVideoLink());
 					dbTurn.put(TURN_PLAYER_REC_SCORE, turn.getRecPlayerScore());
 					dbTurn.put(TURN_PLAYER_ANS_SCORE, turn.getAnsPlayerScore());
 					dbTurn.saveEventually();
 				} else{
 					Log.d("Database",e.getMessage());
 				}
 			}
 		});
 	}
 
 	//Players -----------------------------------------------------------------------------------------//	
 
 	/* (non-Javadoc)
 	 * @see com.example.wecharades.model.IDatabase#getPlayer(java.lang.String)
 	 */
 	@Override
 	public Player getPlayer(String playerName) throws DatabaseException {
 		ParseQuery query = ParseUser.getQuery();
 		query.whereEqualTo(PLAYER_USERNAME, playerName.toLowerCase());
 		ParseObject dbPlayer;
 		try {
 			dbPlayer = query.getFirst();
 		} catch (ParseException e) {
 			Log.d("Database", e.getMessage());
 			throw new DatabaseException(1006,"Failed to fetch user");
 		}
 		return dbc.parsePlayer(dbPlayer);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.example.wecharades.model.IDatabase#getPlayerById(java.lang.String)
 	 */
 	@Override
 	public Player getPlayerById(String parseId) throws DatabaseException {
 		return dbc.parsePlayer(getPlayerObject(parseId));
 	}
 
 	/**
 	 * 
 	 * @param parseId
 	 * @return
 	 * @throws DatabaseException
 	 */
 	private ParseObject getPlayerObject(String parseId) throws DatabaseException {
 		try {
 			return ParseUser.getQuery().get(parseId);
 		} catch (ParseException e) {
 			Log.d("Database", e.getMessage());
 			throw new DatabaseException(1007,"Failed to fetch user");
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.example.wecharades.model.IDatabase#getPlayers()
 	 */
 	@Override
 	public ArrayList<Player> getPlayers() throws DatabaseException {
 		ArrayList<Player> players = new ArrayList<Player>();
 		ParseQuery query = ParseUser.getQuery();
 
 		try {
 			List<ParseObject> dbResult = query.find();
 			for(ParseObject player : dbResult) {
 				players.add(dbc.parsePlayer(player));
 			}
 		} catch (ParseException e) {
 			Log.d("Database", e.getMessage());
 			throw new DatabaseException(1008,"Failed to fetch players");
 		}
 		return players;
 	}
 
 	public void updatePlayer(final Player player){
 		ParseQuery query = ParseUser.getQuery();
 		query.getInBackground(player.getParseId(), new GetCallback(){
 			public void done(ParseObject obj, ParseException e){
 				if(e == null){
 					obj.put(PLAYER_GAMES_DRAW, player.getDrawGames());
 					obj.put(PLAYER_GAMES_LOST, player.getLostGames());
 					obj.put(PLAYER_GAMES_PLAYED, player.getPlayedGames());
 					obj.put(PLAYER_GLOBALSCORE, player.getGlobalScore());
 					obj.saveEventually();
 				} else{
 					setChanged();
 					notifyObservers(new DBMessage(DBMessage.ERROR, new DatabaseException(e.getCode(), e.getMessage())));
 				}
 			}
 		});
 	}
 
 	/**
 	 * Generates a list with the 10 players with best global score
 	 * @return a list with top 10 players based on their global score
 	 * @throws DatabaseException
 	 */
 	public ArrayList<Player> getTopTenPlayers() throws DatabaseException {
 		ArrayList<Player> players = new ArrayList<Player>();
 		ParseQuery query = ParseUser.getQuery();
 		query.addDescendingOrder("globalScore");
 		query.setLimit(10);
 
 		try {
 			List<ParseObject> dbResult = query.find();
 			for(ParseObject player : dbResult) {
 				players.add(dbc.parsePlayer(player));
 			}
 		} catch (ParseException e) {
 			Log.d("Database", e.getMessage());
 			throw new DatabaseException(1008,"Failed to fetch players");
 		}
 		return players;
 
 	}
 
 	//Invitations -----------------------------------------------------------------------------------------
 
 	/* (non-Javadoc)
 	 * @see com.example.wecharades.model.IDatabase#putIntoPlayerQueue(com.example.wecharades.model.Player)
 	 */
 	@Override
 	public void putIntoRandomQueue(final Player player){
 		final Database db = this;
 
 		ParseQuery query = new ParseQuery(RANDOMQUEUE);
 		query.findInBackground(new FindCallback(){
 			public void done(List<ParseObject> queryList, ParseException e){
 				if(e == null){
 					if(db != null){
 						if(queryList.isEmpty()){
 							db.putRandom(player);
 						} else{
 							Collections.shuffle(queryList);
 							try {
 								Player p2 = getPlayerById(queryList.get(0).getString(RANDOMQUEUE_PLAYER));
 								db.createGame(player, p2);
 								db.removeRandom(p2);
 							} catch (DatabaseException e1) {
 								setChanged();
 								notifyObservers(new DBMessage(DBMessage.ERROR
 										, e1));
 							}
 						}
 					}
 				} else{
 					setChanged();
 					notifyObservers(new DBMessage(DBMessage.ERROR
 							, new DatabaseException(e.getCode(), e.getMessage())));
 				}
 			}
 		});
 	}
 	/*
 	 * Helper method for putInRandomQueue
 	 */
 	private void putRandom(Player player){
 		ParseObject queue = new ParseObject(RANDOMQUEUE);
 		queue.put(RANDOMQUEUE_PLAYER, player.getParseId());
 		queue.saveEventually();
 	}
 	public void removeRandom(Player player){
 		ParseQuery query = new ParseQuery(RANDOMQUEUE);
 		query.whereEqualTo(RANDOMQUEUE_PLAYER, player.getParseId());
 		//First fetches the player from the randomqueue, then deletes the player. All on speparate thread.
 		query.findInBackground(new FindCallback(){
 			public void done(List<ParseObject> thePlayer, ParseException e){
 				if(e == null){
 					//Delete all instanses of a player.
 					for(ParseObject p : thePlayer){
 						p.deleteEventually();
 					}
 				} else{
 					Log.d("Database", "Could not remove random player");
 				}
 			}
 		});
 	}
 
 	/* (non-Javadoc)
 	 * @see com.example.wecharades.model.IDatabase#sendInvitation(com.example.wecharades.model.Invitation)
 	 */
 	@Override
 	public void sendInvitation(Invitation inv){
 		ParseObject invite = new ParseObject(INVITE);
 		invite.put(INVITE_INVITER, inv.getInviter().getParseId());
 		invite.put(INVITE_INVITEE, inv.getInvitee().getParseId());
 		invite.saveInBackground();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.example.wecharades.model.IDatabase#getInvitations(com.example.wecharades.model.Player)
 	 */
 	@Override
 	public void getInvitations(Player player) throws DatabaseException {
 		ParseQuery query = new ParseQuery(INVITE);
 		query.whereContains(INVITE_INVITEE, player.getParseId());
 		query.findInBackground(new FindCallback(){
 			public void done(List<ParseObject> result, ParseException e){
 				if(e == null){
 					ArrayList<Invitation> invList = new ArrayList<Invitation>();
 					try{
 						if(!result.isEmpty()){
 							for(ParseObject obj : result){
 								invList.add(dbc.parseInvitation(obj));
 							}
 						}
 						setChanged();
 						notifyObservers(new DBMessage(DBMessage.INVITATIONS, invList));
 					} catch(DatabaseException e2){
 						sendError(e2);
 					}
 				} else{
 					sendError(new DatabaseException(e.getCode(), e.getMessage()));
 				}
 			}
 			private void sendError(DatabaseException e){
 				setChanged();
 				notifyObservers(new DBMessage(DBMessage.ERROR, e));
 			}
 		});
 	}
 
 	/* (non-Javadoc)
 	 * @see com.example.wecharades.model.IDatabase#removeInvitation(com.example.wecharades.model.Invitation)
 	 */
 	@Override
 	public void removeInvitation(Invitation inv) throws DatabaseException{
 		try{
 			ParseQuery query = new ParseQuery(INVITE);
 			query.whereEqualTo(INVITE_INVITER, inv.getInviter().getParseId());
 			query.whereEqualTo(INVITE_INVITEE, inv.getInvitee().getParseId());
 			List<ParseObject> objectList = query.find();
 			for(ParseObject object : objectList){
 				object.delete();
 			}
 		} catch(ParseException e){
 			Log.d("Database", e.getMessage());
 			throw new DatabaseException(1010,"Error removing player from queue");
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.example.wecharades.model.IDatabase#removeInvitations(java.util.Collection)
 	 */
 	@Override
 	public void removeInvitations(Collection<Invitation> inv) throws DatabaseException{
 		for(Invitation invite : inv){
 			removeInvitation(invite);
 		}
 	}
 
 	//User login, registration and logout -----------------------------------------------------------------------------
 
 
 	/* (non-Javadoc)
 	 * @see com.example.wecharades.model.IDatabase#registerPlayer(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public void registerPlayer(
 			String inputNickname, 
 			String inputEmail, 
 			String inputPassword, 
 			String inputRepeatPassword
 			) throws DatabaseException{
 
 		//Some checks that are done locally
 		if(inputNickname == null || !Pattern.compile("^[A-Za-z0-9_]{2,16}$").matcher(inputNickname).matches()) {
 			throw new DatabaseException(2001,"Invalid nickname. \n - It should be between 2 and 16 characters.\n - It should only contain A-Z, a-z, 0-9 and underline");
			//		} else if (!Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$;").matcher(inputEmail).matches()) {
			//			throw new DatabaseException(2002,"Invalid e-mail.");
 		} else if (inputEmail == null || inputEmail.length() == 0) {
 			throw new DatabaseException(125, "Invalid e-mail address.");
 		} else if( inputPassword == null || inputPassword.length() <5 ){
 			throw new DatabaseException(2003,"Weak password");
 		} else if(!inputPassword.equals(inputRepeatPassword)){
 			throw new DatabaseException(2004,"Unrepeated password");
 		}
 
 		ParseUser user = new ParseUser();
 		user.setUsername(inputNickname.toLowerCase());
 		user.put(PLAYER_USERNAME_NATURAL, inputNickname);	//to keep the input username, e.g capital letter
 		user.put(PLAYER_GLOBALSCORE, 0); //globalScore is set to zero when register
 		user.setPassword(inputPassword);
 		user.setEmail(inputEmail);
 		try {
 			user.signUp();
 		} catch (ParseException e) {
 			Log.d("Database", e.getMessage());
 			throw new DatabaseException(e.getCode(), e.getMessage());
 		}
 	}
 
 
 	/* (non-Javadoc)
 	 * @see com.example.wecharades.model.IDatabase#loginPlayer(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public void loginPlayer(String username, String password) throws DatabaseException{
 		//login through parse.com's standard function
 		//Using lowercase at login and registration to avoid case sensitivity problem
 		try {
 			ParseUser.logIn(username.toLowerCase(), password);
 		} catch (ParseException e) {
 			Log.d("Database",e.getMessage() + ". Code: " + Integer.toString(e.getCode()));
 			throw new DatabaseException(e.getCode(), e.getMessage());
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.example.wecharades.model.IDatabase#getCurrentPlayer()
 	 */
 	@Override
 	public Player getCurrentPlayer(){
 		return dbc.parsePlayer(ParseUser.getCurrentUser());
 	}
 
 	/* (non-Javadoc)
 	 * @see com.example.wecharades.model.IDatabase#resetPassword(java.lang.String)
 	 */
 	@Override
 	public void resetPassword(String email) throws DatabaseException {
 		try {
 			ParseUser.requestPasswordReset(email);
 		} catch (ParseException e) {
 			throw new DatabaseException(e.getCode(), e.getMessage());
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.example.wecharades.model.IDatabase#logOut()
 	 */
 	@Override
 	public void logOut(){
 		ParseUser.logOut();
 	}
 
 }
