 package game.cards;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.Nullable;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import game.SpitzerDeclaration;
 import game.cards.Rank;
 
 public class SpitzerDeck extends Deck {
 	public static final Boolean ALLOW_SCHNEIDER = false;
 
 	public static final ImmutableMap<Rank, Integer> RANK_POINTS = ImmutableMap.<Rank, Integer>builder()
 													.put(Rank.ACE, 11)
 													.put(Rank.TEN, 10)
 													.put(Rank.KING, 4)
 													.put(Rank.QUEEN, 3)
 													.put(Rank.JACK, 2)
 													.put(Rank.NINE, 0)
 													.put(Rank.EIGHT, 0)
 													.put(Rank.SEVEN, 0)
 													.build();
 	
 	public static final ImmutableMap<Rank, Integer> FAIL_RANK_PRIORITIES = ImmutableMap.<Rank, Integer>builder()
 													.put(Rank.ACE, 10)
 													.put(Rank.TEN, 9)
 													.put(Rank.KING, 8)
 													.put(Rank.NINE, 7)
 													.put(Rank.EIGHT, 6)
 													.put(Rank.SEVEN, 7)
 													.build();
 	
 	public static final ImmutableMap<Card, Integer> TRUMP_PRIORITIES = ImmutableMap.<Card, Integer>builder()
 													.put(Card.QUEEN_OF_CLUBS, 20)
 													.put(Card.SEVEN_OF_DIAMONDS, 19)
 													.put(Card.QUEEN_OF_SPADES, 18)
 													.put(Card.QUEEN_OF_HEARTS, 17)
 													.put(Card.QUEEN_OF_DIAMONDS, 16)
 													.put(Card.JACK_OF_CLUBS, 15)
 													.put(Card.JACK_OF_SPADES, 14)
 													.put(Card.JACK_OF_HEARTS, 13)
 													.put(Card.JACK_OF_DIAMONDS, 12)
 													.put(Card.ACE_OF_DIAMONDS, 11)
 													.put(Card.TEN_OF_DIAMONDS, 10)
 													.put(Card.KING_OF_DIAMONDS, 9)
 													.put(Card.NINE_OF_DIAMONDS, 8)
 													.put(Card.EIGHT_OF_DIAMONDS, 7)
 													.build();
 
 	public static final Set<Card> FAIL_CARDS = Sets.newHashSet(
 				Card.ACE_OF_CLUBS,
 				Card.TEN_OF_CLUBS,
 				Card.KING_OF_CLUBS,
 				Card.NINE_OF_CLUBS,
 				Card.EIGHT_OF_CLUBS,
 				Card.SEVEN_OF_CLUBS,
 				Card.ACE_OF_SPADES,
 				Card.TEN_OF_SPADES,
 				Card.KING_OF_SPADES,
 				Card.NINE_OF_SPADES,
 				Card.EIGHT_OF_SPADES,
 				Card.SEVEN_OF_SPADES,
 				Card.ACE_OF_HEARTS,
 				Card.TEN_OF_HEARTS,
 				Card.KING_OF_HEARTS,
 				Card.NINE_OF_HEARTS,
 				Card.EIGHT_OF_HEARTS,
 				Card.SEVEN_OF_HEARTS
 			);
 	public static final Set<Card> TRUMP_CARDS = Sets.newHashSet(
 				Card.QUEEN_OF_CLUBS,
 				Card.QUEEN_OF_DIAMONDS,
 				Card.QUEEN_OF_HEARTS,
 				Card.QUEEN_OF_SPADES,
 				Card.SEVEN_OF_DIAMONDS,
 				Card.EIGHT_OF_DIAMONDS,
 				Card.NINE_OF_DIAMONDS,
 				Card.TEN_OF_DIAMONDS,
 				Card.KING_OF_DIAMONDS,
 				Card.ACE_OF_DIAMONDS,
 				Card.JACK_OF_CLUBS,
 				Card.JACK_OF_SPADES,
 				Card.JACK_OF_HEARTS,
 				Card.JACK_OF_DIAMONDS
 			);
 	
 	public static final Predicate<Card> IS_FAIL = new Predicate<Card>(){
 		@Override
 		public boolean apply(@Nullable Card arg0) {
 			return FAIL_CARDS.contains(arg0);
 		}
 	};
 	
 	public static final Predicate<Card> IS_TRUMP = new Predicate<Card>(){
 
 		@Override
 		public boolean apply(@Nullable Card arg0) {
 			return TRUMP_CARDS.contains(arg0);
 		}
 	};
 	
 	public static boolean isFail(Card c)
 	{
 		return IS_FAIL.apply(c);
 	}
 	
 	public static boolean isTrump(Card c)
 	{
 		return IS_TRUMP.apply(c);
 	}
 	
 	public static Collection<Card> getValidCardsForTrick(SpitzerDeclaration declaration, Integer declarePlayer, Integer player, Cards playerHand, List<Card> orderedTrick)
 	{
 		if(declaration != null && declaration.equals(SpitzerDeclaration.NONE))
 			declaration = null;
 		
 		// If this is the first play and no declarations were made (or solo was called), then everything is valid
 		if(orderedTrick.isEmpty() && (declaration == null || SpitzerDeclaration.isSolo(declaration)))
 			return playerHand;
 		
 		Suit declareSuit = declaration == null ? null : SpitzerDeclaration.getSuitOfDeclaration(declaration);
 		
 		// If you have the ace that was called and you lead, you may play any card in that suit
 		if(declareSuit != null && orderedTrick.isEmpty() && playerHand.hasCard(Rank.ACE, declareSuit))
 		{
			return playerHand.getFailCardsOfSuit(declareSuit);
 		}
 		
 		// If you declared and it is the first card in the trick you can play anything
 		if(declareSuit != null && declarePlayer.equals(player) && orderedTrick.isEmpty())
 			return playerHand;
 		
 		// Beyond this point, you can play anything if it's the first card
 		if(orderedTrick.isEmpty())
 			return playerHand;
 		
 		// Get lead card
 		Card firstCard = orderedTrick.get(0);
 		
 		// If you have the called ace, you do not lead, and the lead suit is the called suit, then you must play the ace
 		if(declareSuit != null && !orderedTrick.isEmpty() 
 				&& firstCard.getSuit().equals(declareSuit)
 				&& isFail(firstCard)
 				&& playerHand.hasCard(Rank.ACE, declareSuit))
 		{
 			return playerHand.getCardsOfRankAndSuit(Rank.ACE, declareSuit);
 		}
 		
 		// If you do not have the called ace, you are the calling player, and it is a renounced call, and the ace has been played 
 		// then you must TRUMP the trick
 		if(declareSuit != null && SpitzerDeclaration.isRenounced(declaration) && player.equals(declarePlayer)
 				&& orderedTrick.contains(Card.fromRankAndSuit(Rank.ACE, declareSuit)))
 		{
 			// Only trump cards
 			return playerHand.getTrumps();
 		}
 		
 		if(isFail(firstCard))
 		{
 			// If the player has a fail of the same suit as lead, then they can only be played
 			if(playerHand.hasFailOfSuit(firstCard.getSuit()))
 				return playerHand.getFailCardsOfSuit(firstCard.getSuit());
 			
 			// Player must play trump, if he has one
 			if(playerHand.hasTrump())
 				return playerHand.getTrumps();
 			
 			// Otherwise, the player can play anything
 			return playerHand;
 		}
 		else if(isTrump(firstCard))
 		{
 			// If the player has trump, then he must play it
 			if(playerHand.hasTrump())
 				return playerHand.getTrumps();
 			
 			// Otherwise, play whatever
 			return playerHand;
 		}
 		
 		return Collections.EMPTY_LIST;
 	}
 	
 	
 	
 	public static Integer getPointsForCards(Collection<Card> cards)
 	{
 		Integer points = 0;
 		
 		for(Card card : cards)
 		{
 			points += SpitzerDeck.RANK_POINTS.get(card.getRank());
 		}
 		
 		return points;
 	}
 	
 	public static Map<Card, Integer> getPointsPerCards(Collection<Card> cards)
 	{
 		Map<Card, Integer> points = Maps.newHashMap();
 		
 		for(Card card : cards)
 		{
 			points.put(card, SpitzerDeck.RANK_POINTS.get(card.getRank()));
 		}
 		
 		return points;
 	}
 	
 	public static Card getWinningCard(Collection<Card> cards)
 	{
 		Card winner = null;
 		
 		for(Card card : cards)
 		{
 			winner = getWinningCard(winner, card);
 		}
 		
 		return winner;
 	}
 	
 	public static Card getWinningCard(Card a, Card b)
 	{
 		if(a == null)
 			return b;
 		if(b == null)
 			return a;
 		
 		if(isFail(a) && isFail(b))
 		{
 			if(FAIL_RANK_PRIORITIES.get(a.getRank()) > FAIL_RANK_PRIORITIES.get(b.getRank()))
 				return a;
 			else
 				return b;
 		}
 		
 		if(isTrump(a) && isFail(b))
 			return a;
 		
 		if(isTrump(b) && isFail(a))
 			return b;
 		
 		// Both are trump
 		return TRUMP_PRIORITIES.get(a) > TRUMP_PRIORITIES.get(b) ? a : b;
 	}
 	
 	@Override
 	protected void buildDeck()
 	{
 		// Played with 32 cards, 7-Ace of each suit
 		cards.addAll(Card.getRange(Rank.SEVEN, Rank.KING, Suit.DIAMOND));
 		cards.addAll(Card.getRange(Rank.SEVEN, Rank.KING, Suit.CLUB));
 		cards.addAll(Card.getRange(Rank.SEVEN, Rank.KING, Suit.HEART));
 		cards.addAll(Card.getRange(Rank.SEVEN, Rank.KING, Suit.SPADE));
 		
 		// Add Aces
 		cards.addAll(Card.getAces());
 	}
 }
