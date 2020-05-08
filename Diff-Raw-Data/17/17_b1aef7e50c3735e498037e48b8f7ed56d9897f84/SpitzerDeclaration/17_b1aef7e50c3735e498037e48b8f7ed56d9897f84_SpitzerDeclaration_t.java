 package game;
 
 import game.cards.Card;
 import game.cards.Cards;
 import game.cards.Rank;
 import game.cards.SpitzerDeck;
 import game.cards.Suit;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 public enum SpitzerDeclaration
 {
 	CALL_FOR_ACE_CLUB(0),
 	CALL_FOR_ACE_SPADE(1),
 	CALL_FOR_ACE_HEART(2),
 	CALL_FOR_ACE_CLUB_RENOUNCED(3),
 	CALL_FOR_ACE_SPADE_RENOUNCED(4),
 	CALL_FOR_ACE_HEART_RENOUNCED(5),
 	CALL_FOR_FIRST_TRICK_WINNER(6),
 	SNEAKER(7),
 	ZOLA(8),
 	ZOLA_SCHNEIDER(9),
 	ZOLA_SCHNEIDER_SCHWARTZ(10),
 	SCHNEIDER(11),
 	NONE(12);
 		
 	public int id;
 	
 	private SpitzerDeclaration(int id)
 	{
 		this.id = id;
 	}
 	
 	public boolean isOutrankedBy(SpitzerDeclaration other)
 	{
 		if(this.equals(ZOLA_SCHNEIDER_SCHWARTZ))
 			return false;
 		
 		if(isZola(this) && isZola(other) && other.equals(ZOLA_SCHNEIDER_SCHWARTZ))
 			return true;
 		
 		return false;
 	}
 	
 	public static boolean isZola(SpitzerDeclaration declare)
 	{
 		switch(declare)
 		{
 		case ZOLA:
 		case ZOLA_SCHNEIDER:
 		case ZOLA_SCHNEIDER_SCHWARTZ:
 			return true;
 			default:
 				return false;
 		}
 	}
 	
 	public static boolean isSolo(SpitzerDeclaration declare)
 	{
 		switch(declare)
 		{
 		case SNEAKER:
 		case ZOLA:
 		case ZOLA_SCHNEIDER:
 		case ZOLA_SCHNEIDER_SCHWARTZ:
 			return true;
 		default:
 			return false;
 		}
 	}
 	
 	public static boolean isRenounced(SpitzerDeclaration declare)
 	{
 		switch(declare)
 		{
 		case CALL_FOR_ACE_CLUB_RENOUNCED:
 		case CALL_FOR_ACE_HEART_RENOUNCED:
 		case CALL_FOR_ACE_SPADE_RENOUNCED:
 			return true;
 			default:
 				return false;
 		}
 	}
 	
 	public static Suit getSuitOfDeclaration(SpitzerDeclaration declare)
 	{
 		switch(declare)
 		{
 		case CALL_FOR_ACE_CLUB:
 		case CALL_FOR_ACE_CLUB_RENOUNCED:
 			return Suit.CLUB;
 		case CALL_FOR_ACE_SPADE:
 		case CALL_FOR_ACE_SPADE_RENOUNCED:
 			return Suit.SPADE;
 		case CALL_FOR_ACE_HEART:
 		case CALL_FOR_ACE_HEART_RENOUNCED:
 			return Suit.HEART;
 			
 		}
 		
 		return null;
 	}
 	
 	public static Map<Suit, SpitzerDeclaration> getRenouncedDeclarationMap()
 	{
 		Map<Suit, SpitzerDeclaration> renounced = Maps.newHashMap();
 		renounced.put(Suit.SPADE, CALL_FOR_ACE_SPADE_RENOUNCED);
 		renounced.put(Suit.CLUB,  CALL_FOR_ACE_CLUB_RENOUNCED);
 		renounced.put(Suit.HEART, CALL_FOR_ACE_HEART_RENOUNCED);
 		return renounced;
 	}
 	
 	public static List<SpitzerDeclaration> buildFromHand(Cards hand, SpitzerGameState gameState)
 	{
 		List<SpitzerDeclaration> declarations = Lists.newArrayList();
 		
 		// If player has both queens, then give the partnership options
 		if(hand.hasBothQueens())
 		{
 			// Check for call ace of clubs
 			if(hand.hasFailOfSuit(Suit.CLUB) && !hand.hasCard(Card.ACE_OF_CLUBS))
 				declarations.add(CALL_FOR_ACE_CLUB);
 			if(hand.hasFailOfSuit(Suit.SPADE) && !hand.hasCard(Card.ACE_OF_SPADES))
 				declarations.add(CALL_FOR_ACE_SPADE);
 			if(hand.hasFailOfSuit(Suit.HEART) && !hand.hasCard(Card.ACE_OF_HEARTS))
 				declarations.add(CALL_FOR_ACE_HEART);
 			
 			// Check for renounced
 			Collection<Suit> failSuits = hand.getFailSuits();
 			Map<Suit, SpitzerDeclaration> renouncedMap = getRenouncedDeclarationMap();
 			for(Suit suit : failSuits)
 			{
				renouncedMap.remove(suit);
 			}
 
			// Renounced is only valid if you can't call normally
			if(declarations.isEmpty())
				declarations.addAll(renouncedMap.values());
 			
 			// Check for all three aces
 			if(hand.hasCard(Card.ACE_OF_CLUBS) && hand.hasCard(Card.ACE_OF_SPADES) && hand.hasCard(Card.ACE_OF_HEARTS) && !gameState.trickWinnerHistory.isEmpty())
 				declarations.add(CALL_FOR_FIRST_TRICK_WINNER);
 			
 			// Solo options
 			declarations.add(SNEAKER);
 		}
 		
 		// Anyone may declare zola
 		declarations.add(ZOLA);
 		declarations.add(ZOLA_SCHNEIDER);
 		declarations.add(ZOLA_SCHNEIDER_SCHWARTZ);
 		
 		// If this player doesn't have both queens, then the player may choose not to declare anything
 		if(!hand.hasBothQueens())
 			declarations.add(NONE);
 		
 		// Allow schneider?
 		if(SpitzerDeck.ALLOW_SCHNEIDER)
 			declarations.add(SCHNEIDER);
 		
 		return declarations;
 	}
 }
