 package nl.dirkgroenen.jokeren;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 public class GameData implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3796450525724090900L;
 	
 	private Hand playerHand, oppHand;
 	private ArrayList<Hand> playersInOrder;
 	private Deck deck;
 	private boolean gameInProgress,grabbedCard,playerMustThrow,firstRun;
 	private ArrayList<PlayedSet> playedSets; 
 	private Turn<Hand> turn;
 	
 	private static GameData instance = new GameData();
 	
 	public GameData(){
 		// Do nothing
 	}
 	
 	public static GameData getInstance(){
 		return instance;
 	}
 	
 	public void init(Hand playerHand, Hand oppHand, ArrayList<Hand> playersInOrder, Deck deck, ArrayList<PlayedSet> playedSets, Turn<Hand> turn) {
 		this.playerHand = playerHand;
 		this.playersInOrder = playersInOrder;
 		this.oppHand = oppHand;
 		this.deck = deck;
 		this.grabbedCard = false;
 		this.playerMustThrow = false;
 		this.playedSets = playedSets;
 		this.firstRun = false;
 		this.turn = turn;
 	}
 	
 	public Hand getPlayerHand(){
 		return playerHand;
 	}
 	public Hand getOppHand(){
 		return oppHand;
 	}
 	public Deck getDeck(){
 		return deck;
 	}
 	public ArrayList<Hand> getPlayersInOrder(){
 		return playersInOrder;
 	}
 	public void setGrabbedCard(boolean set){
 		this.grabbedCard = set;
 	}
 	public boolean getGrabbedCard(){
 		return grabbedCard;
 	}
 
 	public void setGameInProgress(boolean progress) {
 		this.gameInProgress = progress;
 	}
 	
 	public boolean isGameInProgress(){
 		return gameInProgress;
 	}
 	
 	public void createNewPlaySet(PlayedSet newSet){
 		playedSets.add(newSet);
 	}
 	
 	public ArrayList<PlayedSet> getAllPlayedSets(){
 		return playedSets;
 	}
 	
 	public void setPlayerCanThrow(boolean set){
 		this.playerMustThrow = set;
 	}
 	
 	public boolean canPlayerThrow(){
 		return playerMustThrow;
 	}
 	
 	public boolean getFirstRun(){
 		return firstRun;
 	}
 	
 	public void setFirstRun(boolean set){
 		this.firstRun = set;
 	}
 	
 	public Turn<Hand> getTurn(){
 		return turn;
 	}
 	
 }
