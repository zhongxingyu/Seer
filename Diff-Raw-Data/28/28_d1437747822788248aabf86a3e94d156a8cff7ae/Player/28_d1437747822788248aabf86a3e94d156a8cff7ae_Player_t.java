 /* memory
  * de.thm.ateam.memory.engine.type
  * Player.java
  * 03.06.2012
  *
  * by Frank Kevin Zey
  */
 package de.thm.ateam.memory.engine.type;
 
 
 import de.thm.ateam.memory.engine.*;
 import android.database.Cursor;
 import android.util.Log;
 import android.widget.BaseAdapter;
 import de.thm.ateam.memory.engine.interfaces.PlayerDAO;
 import de.thm.ateam.memory.game.PlayerList;
 
 
 /**
  * @author fast nur Frank Kevin Zey
  * 
  */
 public class Player implements Comparable<Player>{
 
 	private final String tag = "Player";
 
 
 	/* 
 	 * Die private Geschichten sind zwar aus Softwaresicht sinnig
 	 * geh���ren aber unter Android spart man sich oft die Methodenaufrufe
 	 * aus Geschwindigkeitsgr���nden.
 	 * Falls ich damit was kaputt gemacht hab, es tut mir leid ;)
 	 */
 
 
	public long id;
 	public String nick;
 	public int win, lose, draw, hit, turn, roundHits, roundTurns; 
 	public boolean roundWin, roundLose, roundDraw; 
 	/*renamed shot to turn, removing the private modifyer*/
 
 	public Player(Cursor c) {
 		this.id = c.getInt(0);
 		this.nick = c.getString(1);
 		this.win = c.getInt(2);
 		this.lose = c.getInt(3);
 		this.draw = c.getInt(4);
 		this.hit = c.getInt(5);
 		this.turn = c.getInt(6);
 	}
 
 	protected Player(){}
 
 	public Player(String nick) {
 		this.nick = nick;
 		this.win  = 0;
 		this.lose = 0;
 		this.draw = 0;
 		this.hit  = 0;
 		this.turn = 0;
 		this.roundDraw = false;
 		this.roundLose = false;
 		this.roundWin = false;
 	}
 
 	public Player myTurn(){
 		Log.i(tag, nick+": it's your turn!");
 		return this;
 	}
 
 	public int hit(){
 		return ++roundHits;
 	}
 
 	public int turn(){
 		return ++roundTurns;
 	}
 
 	public void onChange(){
 
 	}
 
 
 	/**
 	 * updatePlayer ver��ndert die Angaben des Spielers in der Datenbank.
 	 * 
 	 * 
 	 */
 	public final void updatePlayer(PlayerDAO database) {
 
 		if (database.updatePlayer(this))
 			Log.i(PlayerDAO.LOG_TAG, "Database updated");
 		else
 			Log.i(PlayerDAO.LOG_TAG, "Database not updated");
 	}
 
 	/**
 	 * getNick gibt den Nick des jeweiligen Spielers zur��ck.
 	 * 
 	 * @return String Nickname des Spielers
 	 */
 	public final String getNick() {
 		return this.nick;
 	}
 
 	/**
 	 * getWin gibt die Anzahl der Siege des Spielers zur��ck.
 	 * 
 	 * @return int Anzahl der Siege
 	 */
 	public final int getWin() {
 		return this.win;
 	}
 
 	/**
 	 * getLose gibt die Anzahl der Niederlagen des Spielers zur��ck.
 	 * 
 	 * @return int Anzahl der Niederlagen
 	 */
 	public final int getLose() {
 		return this.lose;
 	}
 
 	/**
 	 * getDraw gibt die Anzahl der Unentschieden des Spielers zur��ck.
 	 * 
 	 * @return int Anzahl der Unentschieden
 	 */
 	public final int getDraw() {
 		return this.draw;
 	}
 
 	/**
 	 * getAverageWinRate gibt einen Prozentfaktor des durchschnittlichen
 	 * Siegrate des Spielers zur��ck.
 	 * 
 	 * @return float Siegrate des Spielers
 	 */
 	public final float getAverageWinRate() {
 		float f = 0.0f;
 
 		f = win / Float.valueOf(this.getGameNumber());
 
 		return f;
 	}
 
 	/**
 	 * getGameNumber gibt die Anzahl aller gespielten Spiele des Spielers
 	 * zur��ck.
 	 * 
 	 * @return int Anzahl aller gespielten Spiele
 	 */
 	public final int getGameNumber() {
 		return (this.win + this.lose + this.draw);
 	}
 
 	/**
 	 * getShots die Anzahl aller Kartenz��ge zur��ck.
 	 * 
 	 * @return int Anzahl aller Kartenz��ge
 	 */
 	public final int getShots() {
 		return this.turn;
 	}
 
 	/**
 	 * getGameNumber gibt die Anzahl der getroffenen Kartenpaaren zur��ck.
 	 * 
 	 * @return int Anzahl aller getroffenen Kartenpaaren
 	 */
 	public final int getHits() {
 		return this.hit;
 	}
 
 	/**
 	 * getID gibt die ID des Spielers zur��ck
 	 * 
 	 * @return int Die ID des Spielers, gleich der in der Datenbank
 	 */
 	public final long getID() {
 		return this.id;
 	}
 
 	/**
 	 * hashCode generiert einen Hashcode des Objekts anhand der ID und des Nicks. Sollte
 	 * der Nick 'null' oder ein leerer String sein, wird stattdessen 0 gew��hlt.
 	 * 
 	 * @return int Generierter Hash
 	 */
 	@Override
 	public int hashCode() {
 		/* this should be sufficiently enough
 		 * result = 42
 		 */
 		int result = 42;
 
 		/* using only the lowest 32 bit of ID */
 		result = 31 * result + (int) this.getID();
 		result = 31 * result
 				+ (( this.getNick() == null || this.getNick().equals("") ) ? 0 : this.getNick().hashCode());
 
 		return result;
 	}
 
 	/**
 	 * setID setzt die ID des Spielers
 	 * 
 	 * @param id Die neue ID des Spielers
 	 */
 	public void setID(long id) {
 		this.id = id;
 	}
 
 	/**
 	 * for printing the object
 	 * 
 	 * @return The nickname of the user
 	 */
 	public String toString(){
 		return this.nick;
 	}
 	
 	/**
 	 * removes a player
 	 * 
 	 * @returns success or failure
 	 */
 	public boolean remove(BaseAdapter adapter){
 		PlayerList.getInstance().session.remove(this);
 		PlayerList.getInstance().players.remove(this);
 		
 		try {
 			if(adapter!= null)adapter.notifyDataSetChanged();
 			//TODO: Das funktioniert so nicht.
 			MemoryPlayerDAO.getInstance().removePlayer(this);
 		} catch (Exception e) {
 			Log.e(tag, "", e);
 			return false;
 		}
 		return true;
 	}
 	
 	
 	public int compareTo(Player another) throws ClassCastException{
 		if(!(another instanceof Player)){
 			throw new ClassCastException();
 		}
 		return (int) (this.getAverageWinRate() - another.getAverageWinRate());
 	}
 }
