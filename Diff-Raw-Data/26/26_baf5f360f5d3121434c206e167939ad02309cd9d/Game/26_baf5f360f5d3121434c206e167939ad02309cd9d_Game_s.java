 public class Game {
 
 	private Board gameBoard;
 	private Robot gameRobot;
 	private Player player1;
 	private Player player2;
 
 	public Game (Player p1, Player p2) {
 		this.gameBoard = new Board(new Player[]{p1, p2});
 		this.player1 = p1;
 		this.player2 = p2;
 		this.player1.setBoard(gameBoard);
 		this.player2.setBoard(gameBoard);
 	}
 	
 	public Game (Player p1, Player p2, Robot startGameRobot) {
 		this(p1,p2);
 		this.gameRobot = startGameRobot;
 	}
 
 	public Player play() {
 		while (this.gameBoard.totalPiecesLeft(this.player1)>0 && this.gameBoard.totalPiecesLeft(this.player2)>0) {
 			this.player1.takeTurn(this);
 			this.player2.takeTurn(this);
 		}
 		if (this.gameBoard.totalPiecesLeft(player1)>0) {
 			return this.player1;
 		}
 		else {
 			return this.player2;
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
 }
