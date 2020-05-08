 package game;
 
 import java.util.Iterator;
 
 import structures.Pair;
 import structures.Point;
 import frontend.GameController;
 
 public class BlobStrategy implements Strategy{
 
 	Human human;
 	Computer computer;
 	GameController observer;
 	
 	public BlobStrategy(Human h, Computer c){
 		human = h;
 		computer = c;
 	}
 	
 	private class BlobStrategyIterable implements Iterable<Pair<Board, Movement>>{
 
 		private Board base;
 		private Point source;
 		private Player player;
 		
 		public BlobStrategyIterable(Board board, Point source){
 			Player player = board.getTileOwner(source);
 			
 			if (player == null){
 				throw new RuntimeException("Player is null");
 			}
 			
 			this.base = board;
 			this.player = player;
 			this.source = source;
 		}
 		
 		@Override
 		public Iterator<Pair<Board, Movement>> iterator() {
 			return new BlobStrategyIterator(base, source, player);
 		}
 		
 	}
 	
 	private class BlobStrategyIterator implements Iterator<Pair<Board, Movement>>{
 
 		private final Board base;
 		private Point source;
 		private Player player;
 		private Movement moves[] = new Movement[5 * 5 - 1];
 		private int moveIdx = 0;
 		private int top = 0;
 		
 		public BlobStrategyIterator(Board base, Point source, Player player){
 			this.base = base;
 			this.source = source;
 			this.player = player;
 			generateMoves();
 		}
 		
 		private void generateMoves(){
 			int idx = 0;
 			for (int dx = -2; dx < 3; dx++){
 				for (int dy = -2; dy < 3; dy++){
 					if (dx == 0 && dy == 0){
 						continue;
 					}
 					int x = (int) (source.getX() + dx);
 					int y = (int) (source.getY() + dy);
 					
 					if (x < 0 || y < 0 || x >= base.getWidth() || y >= base.getHeight()){
 						continue;
 					}
 					Point target = Point.getInstance(x, y);
 					if (base.getTileOwner(target) != null){
 						continue;
 					}
 					this.moves[idx++] = new Movement(source, target);
 				}
 			}
 			top = idx;
 			
 		}
 		
 		@Override
 		public boolean hasNext() {
 			return moveIdx < top;
 		}
 
 		@Override
 		public Pair<Board, Movement> next() {
 			Movement move = this.moves[moveIdx++];
 			Board board = (Board) base.clone();
 			
 			board = board.putBlob(player, move.target);
 			if (move.distance() == 2){
 				board = board.deleteBlob(player, source);
 			}
 			attack(player, board, move.target);
 			return new Pair<Board, Movement>(board, move);
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 	}
 	
 	
 	@Override
 	public boolean isValid(Board board, Movement move) {
 		Point source = move.source;
 		Point target = move.target;
 		
 		if (source == target || move.distance() > 2){
 			return false;
 		}
 		if (board.getTileOwner(source) == null || 
 				board.getTileOwner(target) != null){
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	/* ALWAYS for current player */
 	public double evaluateScore(Board board, Player p) {
 		double computerTiles = board.countTilesForPlayer(computer);
 		double humanTiles = board.countTilesForPlayer(human);
 		return board.countTilesForPlayer(p) / (computerTiles + humanTiles);
 	}
 
 	@Override
 	public Board move(Board board, Player player, Movement move) {
 			board.putBlob(player, move.target);
 			
			if (move.distance() == 2){
 				board.deleteBlob(player, move.source);
			}
			
			attack(player, board, move.target);
			
 			if (observer != null){
 				observer.onTileUpdate(move.source.getY(), move.source.getX(), board.getTile(move.source));
 				observer.onTileUpdate(move.target.getY(), move.target.getX(), board.getTile(move.target));
				refreshAdyacentTiles(board, move.target);
 			}
 			return board;
 	}
 	
 	private void refreshAdyacentTiles(Board board, Point source){
 		if (observer == null){
 			return;
 		}
 		
 		for (int dx = -1; dx < 2; dx++){
 			for (int dy = -1; dy < 2; dy++){
 				if (dx == 0 && dy == 0){
 					continue;
 				}
 				
 				int x = (int) (source.getX() + dx);
 				int y = (int) (source.getY() + dy);
 				
 				if (x < 0 || y < 0 || x >= board.getWidth() || y >= board.getHeight()){
 					continue;
 				}
 				
 				Point target = Point.getInstance(x, y);
 				
 				if (board.getTileOwner(target) != null){
 					observer.onTileUpdate(target.getY(), target.getX(), board.getTile(target));
 				}
 			}
 		}
 	}
 	
 	public Board startingBoard(){
 		Board board = new Board(this, human, computer);
 		board.putBlob(human, Point.getInstance(0, 7));
 		board.putBlob(human, Point.getInstance(0, 0));
 		board.putBlob(computer, Point.getInstance(7, 0));
 		board.putBlob(computer, Point.getInstance(7, 7));
 		return board;
 	}
 	
 	// Only method that modifies the board
 			private void attack(Player player, Board board, Point source){
 				for (int dx = -1; dx < 2; dx++){
 					for (int dy = -1; dy < 2; dy++){
 						if (dx == 0 && dy == 0){
 							continue;
 						}
 						
 						int x = (int) (source.getX() + dx);
 						int y = (int) (source.getY() + dy);
 						
 						if (x < 0 || y < 0 || x >= board.getWidth() || y >= board.getHeight()){
 							continue;
 						}
 						
 						Point target = Point.getInstance(x, y);
 						
 						if (board.getTileOwner(target) != null){
 							board.setTile(target, player);
 						}
 					}
 				}
 			}
 
 	@Override
 	public void setObserver(GameController observer) {
 		this.observer = observer;
 	}
 
 	@Override
 	public Iterable<Pair<Board, Movement>> boardsForMove(Board board, Point source) {
 		return new BlobStrategyIterable(board, source);
 	}
 }
