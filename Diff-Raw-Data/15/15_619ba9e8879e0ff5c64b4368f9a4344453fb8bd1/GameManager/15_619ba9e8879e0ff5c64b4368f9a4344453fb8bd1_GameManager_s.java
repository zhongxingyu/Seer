 package game.play;
 
 import game.score.HandRecord;
 import game.score.ScoreCard;
 
 import java.util.List;
 
 public class GameManager {
 
 	private ScoreCard scoreCard;
 	private Deck deck;
 	private int handNumber;
 	private HandRecord thisHand;
 	private List<Player> players;
 	private Player inLead;
 	private Board board;
 	
 	public GameManager()
 	{
 		
 		
 	}
 
 	public void play(List<Player> players)
 	{
 		this.players = players;
 		scoreCard = new ScoreCard(players.size());
 		
 		for (handNumber = 1; handNumber<=13; handNumber++)
 		{
 			inLead = players.get(0);
 			deck = new Deck();
 			deck.shuffle();
 			board = new Board();
 			playHand();
 		}
 	}
 	
 	private void playHand() {
 		board.clear();
 		dealCards();
 		
 		for(Player player: players)
 		{
 			System.out.println(player.getName()+"'s hand: " + player.getHand());
 		}
 		
 		bidRound();
 		
 		
 		for(int i=0;i<cardsThisHand();i++)
 		{
 			inLead = playTrick();
 		}
 		thisHand.assessScores();
 		scoreCard.recordHand(thisHand);
 	}
 
 	/*
 	 * returns the player that won the trick
 	 */
 	private Player playTrick() {
 		int leadOffset = players.indexOf(inLead);
 		Player theirTurn = inLead;
 		for(int turnIndex = 0; turnIndex<players.size(); turnIndex++)
 		{
 			theirTurn = players.get((turnIndex+leadOffset)%players.size());
 			board.put(theirTurn.playCard(board),theirTurn);
 		}
 		
 		return board.determineWinner();
 		
 	}
 
 	private void bidRound() {
 		int tricksRemaining = cardsThisHand();
 		int leadOffset = players.indexOf(inLead);
 		Player theirTurn = inLead;
 		for(int turnIndex = 0; turnIndex<players.size(); turnIndex++)
 		{
 			theirTurn = players.get((turnIndex+leadOffset)%players.size());
			tricksRemaining -= theirTurn.bid(cardsThisHand(),tricksRemaining,turnIndex==players.size());
 		}
 	}
 
 	private void dealCards() {
 		thisHand = new HandRecord(players);
 		thisHand.setBoard(board);
 		System.out.println("Dealing "+cardsThisHand()+" cards to everyone");
 		for (Player player : players)
 		{
 			player.setHand(new Hand(deck.drawCards(cardsThisHand())));
 		}
 		Card trump = deck.drawCard();
 		thisHand.getBoard().setTrump(trump.getSuit());
 	}
 
 	/*
 	 * reads the handNumberField and returns the number of cards in the given hand.
 	 * perhaps allow this to be overridden for different variations on the rules. (along with scoring)
 	 */
 	private int cardsThisHand()
 	{
 		return Math.abs(7-handNumber)+1;
 	}
 	
 }
