 import java.util.Random;
 
 public class RandomPlayer extends SimPlayer {
 	private Random random;
 
 	public RandomPlayer (String startXO, boolean startOnZeroSide) {
 		super(startXO, startOnZeroSide);
 		this.random = new Random();
 	}
 
 	public Move takeTurn(Game g) {
		Move[] moves = this.rankBestMoves(g, 6);
 		Move m = moves[this.random.nextInt(Math.min(2, moves.length))];
 		this.performMove(m);
 		return m;
 	}
 }
