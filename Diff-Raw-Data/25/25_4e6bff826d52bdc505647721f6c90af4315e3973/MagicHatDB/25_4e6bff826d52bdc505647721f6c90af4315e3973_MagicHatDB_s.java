 package com.magichat;
 
 import java.io.File;
 import java.util.Date;
 import java.util.List;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 
 public class MagicHatDB {
 
 	private static final String DB_PATH = "/data/data/com.magichat/databases/";
 	protected static final String MH_DB_NAME = "MagicHatDB";
 	private static final int MH_DB_VERSION = 1;
 
 	private MagicHatDbHelper mhHelper;
 	private SQLiteDatabase mhDb;
 	private final Context context;
 
 	// ///////////////////////////////MAIN SETUP//////////////////////////////
 
 	public MagicHatDB(Context c) {
 		context = c;
 	}
 
 	public MagicHatDB openWritableDB() {
 		mhHelper = new MagicHatDbHelper(context, MH_DB_NAME, MH_DB_VERSION);
 		try {
 			mhDb = mhHelper.getWritableDatabase();
 		} catch (SQLiteException e) {
 			e.printStackTrace();
 		}
 
 		return this;
 	}
 
 	public MagicHatDB openReadableDB() {
 		mhHelper = new MagicHatDbHelper(context, MH_DB_NAME, MH_DB_VERSION);
 		try {
 			mhDb = mhHelper.getReadableDatabase();
 		} catch (SQLiteException e) {
 			e.printStackTrace();
 		}
 
 		return this;
 	}
 
 	public void closeDB() {
 		try {
 			mhHelper.close();
 		} catch (SQLiteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected static boolean isCreated() {
 		File dbFile = new File(DB_PATH + MH_DB_NAME);
 		return dbFile.exists();
 	}
 
 	protected boolean isUpgrade() {
		mhHelper = new MagicHatDbHelper(context, MH_DB_NAME, MH_DB_VERSION);
 		boolean result = false;
 
 		try {
			mhDb = mhHelper.getReadableDatabase();
 			result = mhDb.getVersion() == MH_DB_VERSION ? false : true;
			mhDb.close();
 		} catch (SQLiteException e) {
 			e.printStackTrace();
 		}
 
 		return result;
 	}
 
 	// //////////////////////////////// DECKS //////////////////////////////////
 
 	public void addDeck(String name, int OwnerId, Integer active) {
 		mhHelper.addDeck(name, OwnerId, active, mhDb);
 	}
 
 	public void updateDeck(String owner, String oldDeckName,
 			String newDeckName, boolean newActive) {
 		mhHelper.updateDeck(owner, oldDeckName, newDeckName, newActive, mhDb);
 	}
 
 	public void deleteDecks(int[] id) {
 		mhHelper.deleteDecks(id, mhDb);
 	}
 
 	public Deck getDeck(int deckId) {
 		return mhHelper.getDeck(deckId, mhDb);
 	}
 
 	public Deck getDeck(String sDeckName, String sOwnerName) {
 		return mhHelper.getDeck(sDeckName, sOwnerName, mhDb);
 	}
 
 	public List<Deck> getAllDecks() {
 		return mhHelper.getAllDecks(mhDb);
 	}
 
 	public List<Deck> getAllActiveDecks() {
 		return mhHelper.getAllActiveDecks(mhDb);
 	}
 
 	public List<Deck> getAllManualDecks() {
 		return mhHelper.getAllManualDecks(mhDb);
 	}
 
 	public List<Deck> getDeckList(Player p) {
 		return mhHelper.getDeckList(p, mhDb);
 	}
 
 	public int getDeckId(String sDeckName, String sOwnerName) {
 		return mhHelper.getDeckId(sDeckName, sOwnerName, mhDb);
 	}
 
 	public boolean deckExists(Deck d) {
 		return mhHelper.deckExists(d, mhDb);
 	}
 
 	// ////////////////////PLAYERS/////////////OWNERS///////////////////////////////
 
 	public Player getOwner(int ownerId) {
 		return mhHelper.getOwner(ownerId, mhDb);
 	}
 
 	public Player getPlayer(int playerId) {
 		return mhHelper.getPlayer(playerId, mhDb);
 	}
 
 	public Player getOwner(String name) {
 		return mhHelper.getOwner(name, mhDb);
 	}
 
 	public Player getPlayer(String name) {
 		return mhHelper.getPlayer(name, mhDb);
 	}
 
 	public int getPlayerId(String name) {
 		return mhHelper.getPlayerId(name, mhDb);
 	}
 
 	public List<Player> getActivePlayers() {
 		return mhHelper.getActivePlayers(mhDb);
 	}
 
 	public List<Player> getAllPlayers() {
 		return mhHelper.getAllPlayers(mhDb);
 	}
 
 	public List<Player> getAllOwners() {
 		return mhHelper.getAllOwners(mhDb);
 	}
 
 	/*
 	 * public Cursor getCursorForAllOwners() { return
 	 * ourDatabase.query(DATABASE_TABLE_ALLPLAYERS, new String[] {
 	 * KEY_PLAYER_ROWID, KEY_PLAYER_NAME, KEY_PLAYER_ACTIVE }, null, null, null,
 	 * null, null); }
 	 */
 
 	public Player flipActiveStatus(Player pFake) {
 		return mhHelper.flipActiveStatus(pFake, mhDb);
 	}
 
 	public Deck flipActiveStatus(String deckName, String ownerName) {
 		return mhHelper.flipActiveStatus(deckName, ownerName, mhDb);
 	}
 
 	// /////////////////// GAMES ///////////////////////////////////////////
 
 	public void addGameResult(List<Player> Players, List<Deck> gameDecks,
 			Player pWinner, Date gameDate) {
 		mhHelper.addGameResult(Players, gameDecks, pWinner, gameDate, mhDb);
 	}
 
 	public List<Game> getAllGames() {
 		return mhHelper.getAllGames(mhDb);
 	}
 
 	public List<Game> getGames(Player p) {
 		return mhHelper.getGames(p, mhDb);
 	}
 
 	public List<Game> getGames(Deck d) {
 		return mhHelper.getGames(d, mhDb);
 	}
 }
