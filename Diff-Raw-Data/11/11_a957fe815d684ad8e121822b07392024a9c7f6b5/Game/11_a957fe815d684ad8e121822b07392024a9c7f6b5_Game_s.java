 import java.util.ArrayList;
 
 public class Game {
 
 	private Board gameBoard;
 	private Robot gameRobot;
 	private Player player1;
 	private Player player2;
 	private ArrayList<int[][]> lastFewMoves;
 	private int movesSinceEvent;
 	private int numberOfKings;
 
 	public Game (Player p1, Player p2) {
 		this.gameBoard = new Board(new Player[]{p1, p2});
 		this.player1 = p1;
 		this.player2 = p2;
 		this.player1.setBoard(gameBoard);
 		this.player2.setBoard(gameBoard);
 		this.lastFewMoves = new ArrayList<int[][]>();
 		this.movesSinceEvent = 0;
 		this.numberOfKings = 0;
 	}
 	
 	public Game (Player p1, Player p2, Robot startGameRobot) {
 		this(p1,p2);
 		this.gameRobot = startGameRobot;
 	}
 
 	public Game(Player p1, Player p2, int[][] p1Locations, int[][] p2Locations, int[][] p1kings, int[][] p2kings) {
 		this.gameBoard = new Board(new Player[]{p1, p2}, p1Locations, p2Locations, p1kings, p2kings);
 		this.player1 = p1;
 		this.player2 = p2;
 		this.player1.setBoard(this.gameBoard);
 		this.player2.setBoard(this.gameBoard);
 		this.lastFewMoves = new ArrayList<int[][]>();
 		this.movesSinceEvent = 0;
 		this.numberOfKings = 0;
 	}
 	
 	public Game (Game oldG, Move m) {
 		this.player1 = oldG.getPlayers()[0];
 		this.player2 = oldG.getPlayers()[1];
 		this.lastFewMoves = ArraysHelper.asArrayList(oldG.getLastFewMoves());
 		this.lastFewMoves.add(m.getWaypoints());
 		if (this.lastFewMoves.size()>12) {
 			this.lastFewMoves.remove(0);
 		}
 		this.movesSinceEvent = oldG.getMovesSinceEvent();
 		this.numberOfKings = oldG.getNumberOfKings();
 		this.gameBoard = new Board(oldG.getGameBoard(), m);
 
 		this.movesSinceEvent++;
 		if (this.lastFewMoves.get(this.lastFewMoves.size()-1).length > 2) {
 			this.movesSinceEvent = 0;
 		}
 		if (this.gameBoard.kingsLeft(this.player1)+this.gameBoard.kingsLeft(player2)!=this.numberOfKings) {
 			this.movesSinceEvent = 0;
 			this.numberOfKings = this.gameBoard.kingsLeft(this.player1)+this.gameBoard.kingsLeft(player2);
 		}
 	}
 
 	public Player play() {
 		while (player1.getAllMoves(this.gameBoard).length>0 && player2.getAllMoves(this.gameBoard).length>0 && !this.isDraw()) {
 			this.lastFewMoves.add(this.player1.takeTurn(this).getWaypoints());
 			if (this.lastFewMoves.size()>12) {
 				this.lastFewMoves.remove(0);
 			}
 
 			this.movesSinceEvent++;
 			if (this.lastFewMoves.get(this.lastFewMoves.size()-1).length > 2) {
 				this.movesSinceEvent = 0;
 			}
 			if (this.gameBoard.kingsLeft(this.player1)+this.gameBoard.kingsLeft(player2)!=this.numberOfKings) {
 				this.movesSinceEvent = 0;
 				this.numberOfKings = this.gameBoard.kingsLeft(this.player1)+this.gameBoard.kingsLeft(player2);
 			}

 			this.gameBoard.printBoard();
 
 			if (player1.getAllMoves(this.gameBoard).length>0 && player2.getAllMoves(this.gameBoard).length>0 && !this.isDraw()) {
 				this.lastFewMoves.add(this.player2.takeTurn(this).getWaypoints());
 				if (this.lastFewMoves.size()>12) {
 					this.lastFewMoves.remove(0);
 				}
 
 				this.movesSinceEvent++;
 				if (this.lastFewMoves.get(this.lastFewMoves.size()-1).length > 2) {
 					this.movesSinceEvent = 0;
 				}
 				if (this.gameBoard.kingsLeft(this.player1)+this.gameBoard.kingsLeft(player2)!=this.numberOfKings) {
 					this.movesSinceEvent = 0;
 					this.numberOfKings = this.gameBoard.kingsLeft(this.player1)+this.gameBoard.kingsLeft(player2);
 				}
 
 				this.gameBoard.printBoard();
 			}
 		}
 		if (player1.getAllMoves(this.gameBoard).length==0) {
 			return this.player2;
 		}
 		else if (player2.getAllMoves(this.gameBoard).length==0) {
 			return this.player1;
 		} else {
 			return null;
 		}
 	}
 
 	public Board getGameBoard () {
 		return this.gameBoard;
 	}
 
 	public Player[] getPlayers () {
 		return new Player[]{this.player1,this.player2};
 	}
 
 	public Player getOtherPlayer(Player p) {
 		if (this.player1==p) {
 			return this.player2;
 		} else {
 			return this.player1;
 		}
 	}
 
 	public int[][][] getLastFewMoves() {
 		return this.lastFewMoves.toArray(new int[this.lastFewMoves.size()][][]);
 	}
 
 	public int getMovesSinceEvent() {
 		return this.movesSinceEvent;
 	}
 
 	public int getNumberOfKings() {
 		return this.numberOfKings;
 	}
 
 	public boolean isDraw() {
 		//returns a draw if no captures or kings have occurred in the last 40 moves for each player
 		if (this.movesSinceEvent>=80) {
 			return true;
 		}
 
 		//returns false if there have been fewer than 12 moves
 		if (this.lastFewMoves.size()<12) {
 			return false;
 		}
 		//creates array to hold triplets of moves which should be identical
 		int[][][] identicals;
 		//iterates over each set of three moves
 		for (int moveIndex=0; moveIndex<4; moveIndex++) {
 			//sets identicals to three moves which should be equal
 			identicals = new int[][][] {this.lastFewMoves.get(moveIndex), this.lastFewMoves.get(moveIndex+4), this.lastFewMoves.get(moveIndex+8)};
 			//returns false if the moves do not have the same number of waypoints
 			if (identicals[0].length!=identicals[1].length || identicals[1].length!=identicals[2].length) {
 				return false;
 			}
 			//iterates over all waypoints and returns false if any of them are not the same for all three moves
 			for (int i=0; i<identicals[0].length; i++) {
 				if (!(identicals[0][i][0]==identicals[1][i][0] && identicals[0][i][1]==identicals[1][i][1]) || !(identicals[1][i][0]==identicals[2][i][0] && identicals[1][i][1]==identicals[2][i][1])) {
 					return false;
 				}
 			}
 		}
 		//the game must be a draw because no discrepancies were found in the moves
 		return true;
 	}
 }
