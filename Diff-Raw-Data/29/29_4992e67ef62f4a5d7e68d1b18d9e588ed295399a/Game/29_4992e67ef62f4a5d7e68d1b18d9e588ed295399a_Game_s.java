 package game;
 
 import frontend.GameController;
 import structures.Point;
 
 public class Game {
 	private Board board;
 	private transient GameController observer;
 	private Strategy strategy;
 	private Computer computer;
 	private Human human;
 	
 	public Game(){
 		this.computer = new Computer();
 		this.human = new Human();	
 		this.strategy = new BlobStrategy(human, computer);
 		this.board = strategy.startingBoard();
 	}
 	
 	public Game(char[] charBoard){
 		this();
 		this.board = new Board(strategy, human, computer, charBoard);
 	}
 	
 	public Board getBoard(){
 		return board;
 	}
 
 
	public int getBoardHeight() {
 		return Board.SIZE;
 	}
 
	public int getBoardWidth() {
 		return Board.SIZE;
 	}
 	
 	
 	public void start(GameController observer) {
 		this.observer = observer;
 		this.strategy.setObserver(observer);
 		printBoard();
 	}
 	
 	public boolean humanMove(Movement move){
 		if (board.getTileOwner(move.source) == human){
 			return move(move);
 		}
 		return false;
 	}
 	
 	public boolean move(Movement move){
 		if (strategy.isValid(board, move)){
 			this.board = strategy.move(board, board.getTileOwner(move.source), move);
 			return true;
 		}
 		return false;
 	}
 	
 	private void printBoard() {
 		for (int i = 0; i < getBoardHeight(); i++){
 			for (int j = 0; j < getBoardWidth(); j++){
 				Point source = Point.getInstance(j, i);
 				if (board.getTile(source) != ' '){
 					observer.onTileUpdate(i, j, board.getTile(source));
 				}
 			}
 		}
 	}
 	
 	public Strategy getStrategy(){
 		return strategy;
 	}
 	
 	public Computer getComputer(){
 		return computer;
 	}
 	
 	public Human getHuman(){
 		return human;
 	}
 	
 	public Player getOpponent(Player player){
 		return player.equals(human) ? computer : human;
 	}
 	
 	public boolean hasWin(Player player){
 		int opponentTiles = this.board.countTilesForPlayer(getOpponent(player));
 		
 		if (opponentTiles == 0){
 			return true;
 		}
 		
 		if (!this.board.hasAvailableMoves(player)){
 			return this.board.countTilesForPlayer(player) > opponentTiles;
 		}
 		
 		return false;
 	}
 	
 }
