 package game;
 
 import game.play.Board;
 import game.play.Card;
 import game.play.Player;
 
 public class PassivePlayer extends Player {
 
 	public PassivePlayer(String name) {
 		super(name);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
	public int bid(int tricksRemaining, boolean restricted) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public Card playCard(Board board) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
