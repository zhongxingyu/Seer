 /**
  * 
  */
 package game;
 
 import general.Config;
 
 /**
  * @author Michi
  * Bildet genau einen Satz eines Vier-Gewinnt-Spiels ab
  */
 public class Set {
 	private Player startingPlayer;
 	private Player currentPlayer;
 	private Player ourPlayer;
 	private Player enemyPlayer;
 	private Board board;
 	
 	public Set(Player ourPlayer) {
 		board = new Board(Config.COLS, Config.ROWS);
 		this.setOurPlayer(ourPlayer);
 	}
 	
 	public Player getStartingPlayer() {
 		return startingPlayer;
 	}
 	
 	public void setStartingPlayer(Player startingPlayer) {
 		this.startingPlayer = startingPlayer;
 	}
 
 	public Player getWinningPlayer() {
 		return this.board.findWinner();
 	}
 	
	//TODO Winning Player manuell setzen
	
	
 	public Player getCurrentPlayer() {
 		return currentPlayer;
 	}
 	
 	public Board getBoard() {
 		return board;
 	}
 
 	public Player getOurPlayer() {
 		return ourPlayer;
 	}
 
 	/**
 	 * Fixiert unseren Spieler und impliziet den der Gegnerpartei
 	 * @param ourPlayer Die "Farbe", die unser Spieler bekommt
 	 */
 	public void setOurPlayer(Player ourPlayer) {
 		this.ourPlayer = ourPlayer;
 		this.enemyPlayer = (ourPlayer == Player.X) ? Player.O : Player.X;
 	}
 
 	public Player getEnemyPlayer() {
 		return enemyPlayer;
 	}
 
 	public void setEnemyPlayer(Player enemyPlayer) {
 		this.enemyPlayer = enemyPlayer;
 	}
 	
 	
 	
 }
