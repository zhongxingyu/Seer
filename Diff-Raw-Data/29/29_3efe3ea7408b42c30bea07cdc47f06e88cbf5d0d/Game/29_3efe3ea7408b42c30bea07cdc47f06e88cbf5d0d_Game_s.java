 import java.io.Serializable;
 
 
 /**
  * @author vitalema and hannantt
  *
  *This class represents the current state of the game
  */
 public class Game implements Serializable {
 	Player currentPlayer;
 	View currentView;
 	boolean isMuted;
 	boolean isPaused;
 	String language;
 	
 	/**
 	 * @param p - the player who is playing the game.
 	 * @param v - the starting view 
 	 * 
 	 * The constructor for making a new game instance.
 	 */
 	public Game(Player p, View v){
 		language = "english";
 		currentPlayer = p;
 		currentView = v;
 		isMuted = false;
 		isPaused = false;
 	}
 	
	public void changeLanguage() {
		if (this.language == "english") {
			this.language = "french";
		}
		else {
			this.language = "english";
		}
 	}
 	/**
 	 * @return the current view
 	 */
 	public View getCurrentView() {
 		return this.currentView;
 	}
 	
 	public String getLanguage() {
 		return this.language;
 	}
 	/**
 	 * @return the current player
 	 */
 	public Player getCurrentPlayer() {
 		return this.currentPlayer;
 	}
 	
 	void changeView(View v){
 		currentView = v;
 		}
 	void pauseGame(){
 		isPaused = true;
 	}
 	void unpauseGame(){
 		isPaused = false;
 	}
 	void muteGame(){
 		isMuted = true;
 	}
 	void unmuteGame(){
 		isMuted = false;
 	}
 	
 	Boolean isMuted(){
 		return isMuted;
 	}
 	
 	Boolean isPaused(){
 		return isPaused;
 	}
 }
