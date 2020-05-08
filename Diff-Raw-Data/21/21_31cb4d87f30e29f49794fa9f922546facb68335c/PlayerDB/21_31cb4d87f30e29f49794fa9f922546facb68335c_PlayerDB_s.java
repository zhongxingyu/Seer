 /* memory
  * de.thm.ateam.memory.engine.type
  * PlayerDB.java
  * 04.06.2012
  *
  * by Frank Kevin Zey
  */
 package de.thm.ateam.memory.engine.type;
 
 import android.content.ContentValues;
 
 /**
  * @author Frank Kevin Zey
  *
  */
 public class PlayerDB {
 
 	public static final String TABLE_NAME = "player";
 	public static final String ID 		  = "_id";
 	public static final String NICK 	  = "nick";
 	public static final String WIN 		  = "win";
 	public static final String LOSE 	  = "lose";
 	public static final String DRAW 	  = "draw";
 	public static final String HIT 		  = "hit";
	public static final String TURN	  = "shot";
 	
 	/**
 	 * Erzeugt ein neues ContentValues-Objekt mit dem Inhalt des übergebenen
 	 * Gamer-Objekts.
 	 * 
 	 * @param player Spieler, dessen Informationen gespeichert werden sollen
 	 * 
 	 * @return ContentValues Gibt ein ContentValues-Objekt zurück mit Inhalt des Spielers, wenn
 	 *    gamer==null, Rückgabe = null
 	 */
 	protected final ContentValues createContentValues(Player player) {
 		if (player == null)
 			return null;
 		
 		ContentValues cv = new ContentValues();
 		
 		cv.put(NICK, player.getNick());
 		cv.put(WIN,  player.getWin());
 		cv.put(LOSE, player.getLose());
 		cv.put(DRAW, player.getDraw());
 		cv.put(HIT,  player.getHits());
 		cv.put(TURN, player.getShots());
 		
 		return cv;
 	}
 
 }
