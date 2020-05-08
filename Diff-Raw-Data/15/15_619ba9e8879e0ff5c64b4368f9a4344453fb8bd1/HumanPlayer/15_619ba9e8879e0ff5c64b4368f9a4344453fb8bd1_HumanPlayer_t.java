 package game;
 
 import utils.ui.UserUtil;
 import game.play.Board;
 import game.play.Card;
 import game.play.Player;
 
 public class HumanPlayer extends Player {
 
 	public HumanPlayer(String name) {
 		super(name);
 	}
 
 	@Override
 	public int bid(int tricksThisHand, int tricksRemaining, boolean restricted) {
		System.out.println("Hello, "+this.getName()+". There are " + tricksThisHand + " tricks to take this hand, " + (tricksThisHand - tricksRemaining) + " have already been bid on.");
 		if(restricted)
 		{
 			System.out.println("You are the dealer, and therefore cannot bid " + tricksRemaining);
 		}
 		Integer bidRequest = null;
 		do
 		{
 			bidRequest = Integer.parseInt(UserUtil.ask("How many would you like to bid?"));
		} while ( !acceptable(bidRequest, tricksThisHand, tricksRemaining, restricted));
 		return bidRequest;
 	}
 
	private boolean acceptable(Integer bidRequest, int tricksThisHand, int tricksRemaining, boolean restricted) {
 		if (bidRequest == null)
 		{
 			return false;
 		}
 		if (bidRequest < 0) // dont bid negative
 		{
 			return false;
 		}
 		if (bidRequest > tricksThisHand) // dont bid more than the total number of tricks
 		{
 			return false;
 		}
 		if (restricted && bidRequest==tricksRemaining) // you're the dealer, cant make total bid = number of tricks.
 		{
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public Card playCard(Board board) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
