 package org.weiqi;
 
 import java.util.ArrayDeque;
 import java.util.Deque;
 
 import org.weiqi.Weiqi.Occupation;
 
 public class MovingGameSet extends GameSet {
 
 	private Deque<Move> moves = new ArrayDeque<>();
 
 	public MovingGameSet() {
 		super();
 	}
 
 	public MovingGameSet(GameSet gameSet) {
 		super(gameSet);
 	}
 
 	public MovingGameSet(Board board, Occupation nextPlayer) {
 		super(board, nextPlayer);
 	}
 
 	@Override
 	public Move play(Coordinate coord) {
 		Move move = super.play(coord);
 		moves.push(move);
 		return move;
 	}
 
 	@Override
 	public void pass() {
 		super.pass();
 	}
 
 	public void undo() {
		super.undo(moves.pop());
 	}
 
 }
