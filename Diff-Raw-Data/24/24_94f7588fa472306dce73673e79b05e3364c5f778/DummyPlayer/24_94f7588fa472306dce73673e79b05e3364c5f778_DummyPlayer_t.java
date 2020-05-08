 package skatgame;
 
 import java.util.Random;
 
 
 
 /**
  * 
  * A dummy player is a representation of a Skat player who plays valid cards with no AI behind it.<br>
  *
  */
 public class DummyPlayer implements IPlayer {
 
 	@Override
 	public void assignIndex(int PlayerIndex) {
 		// We would store our index here as a global variable so we know who we are.
 	}
 	
 	@Override
 	public Card playTurn(Pile hand, Pile playedCards,
 			int playerIndexOfWhoStarted) {
 		// TODO (right now, for the purpose of testing, return any card in hand)
 		// [Strategy]
 		// Examine what Cards have been played, and by which Player
 		// (playedCards is designed such that the first Card in the Pile is the card that was played first)
 		// Examine the Cards in your hand, and return the Card you decide to play.
 		// Keep GameType variables in mind in order to pick valid Cards.
 		
 		// (This can be used to test the error for a user not having a card they played).
 		//if(true)
 		//	return new Card(Card.CARD_SUIT.CLUBS, Card.FACE_VALUE.ACE);
 		
 		// If we're not leading suit..
 		if(playedCards.getNumCards() > 0) {
 			// Get the leading suit
 			Card.CARD_SUIT leadingSuit = playedCards.getCard(0).getSuit();
 			
 			// If we can find a leading suit card, let's play it, this way it will be a valid play.
 			for(int i = 0; i < hand.getNumCards(); i++)
 				if(hand.getCard(i).getSuit() == leadingSuit) {
 					return hand.getCard(i);
 				}
 		}
 		
 		//  Play a random card
 		Random r = new Random();
 		int cardIndex = r.nextInt(hand.getNumCards());
 		return hand.getCard(cardIndex);
 	}
 
 	@Override
 	public void endTrick(Pile trick, int winnerIndex) {
 		// TODO
 		// See who won the trick this round.
 		// See which Cards were won in the trick
 		// [Strategy] Useful for counting cards, etc.
 	}
 
 	@Override
 	public boolean decideTakeSkat(Pile hand) {
 		// TODO
 		// Examine your hand.
 		// [Strategy] Decide whether or not to view the skat.
 		// Create a boolean retVal. Set as True to view the skat, False otherwise.
 		
 		// Make it random
 		Random r = new Random();
 		return r.nextBoolean();
 	}
 
 	@Override
	public Pile giveSkat(Pile hand, Pile skat) {
 		// TODO
 		// Add the two cards in the skat to your hand.
 		// [Strategy] Choose any two cards to discard.
 		// Remove discard cards from your hand, put them in the skat.
 		// Return the skat. 
 		
 		// Decide how many cards to take.
 		Random r = new Random();
 		int swapAmt =  r.nextInt(3); // 0-2 cards.
 		
 		// Just swap them linearly (first skat card swap with first hand card, etc).
 		for(int i = 0; i < swapAmt; i++) {
 			Card skatCard = skat.getCard(i);
 			Card handCard = hand.getCard(i);
 			skat.replaceCards(skatCard, handCard);
 			hand.replaceCards(handCard, skatCard);
 		}
 		
 		return skat;
 	}
 
 	@Override
 	public boolean bid(int currentHighestBid, Pile hand, boolean canMatch) {
 		// TODO
 		// Examine canMatch -- determine whether or not you may match the current highest bid,
 		// or if you must bid higher.
 		// Examine your hand.
 		// [Strategy] Decide whether to bid/match, or pass.
 		// Return true if you want to bid/match, or false if you want to pass.
 		
 		// Make it random
 		Random r = new Random();
 		return r.nextBoolean();
 	}
 
 	@Override
 	public GameTypeOptions decideGameType(Pile hand) {
 		// Examine your hand.
 		// [Strategy] Decide which type of game to play.
 		// Create the corresponding type of GameTypeOptions object, and return it.
 		// Keep in mind, only certain combinations of game type variables are valid.
 		// (For example, you cannot have a game that is both Skat and Schneider)
 		
 		// Our implementation will return random data.
 		Random r = new Random();
 		int indexHand = r.nextInt(GameTypeOptions.SkatHandType.values().length);
 		int indexSuit = r.nextInt(GameTypeOptions.GameType.values().length);
 		int indexTrump = r.nextInt(GameTypeOptions.TrumpSuit.values().length);
 		boolean iOuvert = r.nextBoolean();
 		boolean iSchwarz = r.nextBoolean();
 		boolean iSchneider = r.nextBoolean();
 		
 		return new GameTypeOptions(GameTypeOptions.SkatHandType.values()[indexHand], GameTypeOptions.GameType.values()[indexSuit], GameTypeOptions.TrumpSuit.values()[indexTrump], iOuvert, iSchneider, iSchwarz);
 	}
 
 	@Override
 	public void setGameType(GameTypeOptions gameType, int declarerIndex) {
 		// TODO
 		// Keep a record of the current game type, to use when determining strategy and valid card choices.
 		// Keep a record of the index of the declarer. By process of elimination (you already have your own playerIndex),
 		// you can figure out the index of each player.
 		// [Strategy] This is useful when choosing which Card to play during your turn so that you can attempt to
 		// beat the declarer, and so you don't accidentally trump the other defender if their card has already beaten the declarer.
 		// Note: if you ARE the declarer, you will already have a record of your own index, so the second parameter in this method
 		// is not important to you. Furthermore, the difference between the two defenders is irrelevant to the declarer, so it is not
 		// necessary that the declarer knows the individual indexes of the two defenders.
 		
 		// Store game-type.
 		// Store declarer index.
 	}
 
 	@Override
 	public void endGameInfo(boolean decWon, int[] scores) {
 		// TODO
 		// If desired, keep a record of your (and the other players') current game score.
 		// Note: This method isn't particularly important, it simply provides the player with information
 		// a real human being would naturally acquire while playing a game of skat.
 	}
 
 	@Override
 	public void giveDeclarersHand(Pile declHand) {
 		// TODO
 		// Keep a record of the declarer's hand.
 		// [Strategy] Use your knowledge of the declarer's cards in order to choose which cards you play.
 	}
 
 	
 }
