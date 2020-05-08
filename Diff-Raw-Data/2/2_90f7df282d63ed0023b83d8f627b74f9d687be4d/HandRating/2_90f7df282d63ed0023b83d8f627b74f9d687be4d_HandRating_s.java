 package edu.ntnu.brunson.manager;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Map;
 
 import edu.ntnu.brunson.cards.Card;
 import edu.ntnu.brunson.cards.Deck;
 import edu.ntnu.brunson.cards.Pile;
 import edu.ntnu.brunson.cards.Suit;
 import edu.ntnu.brunson.cards.Value;
 import edu.ntnu.brunson.util.PileUtil;
 
 public class HandRating implements Comparable<HandRating>{
 
 	private PokerHands type;
 	private Value[] tieInfo;
 	
 	
 	public static void main(String[] args) {
 		int numCards = 5;
 		long start = System.currentTimeMillis();
 		int[] numHands = new int[PokerHands.values().length];
 		for(Pile pile : PileUtil.combinations(Deck.fullDeck(),numCards))
 			numHands[rate(pile).type.ordinal()]++;
 		long stop = System.currentTimeMillis();
 		long time = stop - start;
 		System.out.printf("numCards: %d, took: %d s%n",numCards,time/1000);
 		for(PokerHands hand : PokerHands.values())
 			System.out.printf("%s: %d hands.%n",hand.name(),numHands[hand.ordinal()]);
 		
 	}
 	
 	private HandRating(PokerHands type, Value[] tieInfo){
 		this.type = type;
 		this.tieInfo = tieInfo;
 	}
 	
 	public static HandRating rate(Pile... piles){
 		Pile pile = new Pile(piles);
 		if(pile.size() < 5 || pile.size() > 7)
 			throw new IllegalArgumentException();
 		List<Pile> partitions = partition(pile);
 		Pile flush,straight;
 		
 		if((flush = findFlush(pile)) != null && (straight = findStraight(flush)) != null){
 			return straightFlush(straight);
 		} else if(partitions.get(0).size() == 4){
 			return quad(partitions);
 		} else if(partitions.get(0).size() == 3 && partitions.get(1).size() >= 2){
 			return boat(partitions);
 		} else if(flush != null){
 			return flush(flush);
 		} else if((straight = findStraight(pile)) != null){
 			return straight(straight);
 		} else if(partitions.get(0).size() == 3){
 			return trips(partitions);
 		} else if(partitions.get(0).size() == 2){
 			if(partitions.get(1).size() == 2){
 				return twopair(partitions);
 			}
 			return onepair(partitions);
 		}
 		return highcard(partitions);
 	}
 
 	private static Pile findFlush(Pile hand) {
 		Map<Suit,Pile> map = new EnumMap<Suit,Pile>(Suit.class);
 		for(Card card : hand){
 			Pile pile = map.get(card.getSuit());
 			if(pile == null)
 				map.put(card.getSuit(), new Pile(card));
 			else
 				pile.add(card);
 		}
 		
 		for(Pile pile : map.values()){
 			if(pile.size() >= 5)
 				return pile;
 		}
 		return null;
 	}
 
 	private static Pile findStraight(Pile hand) {
 		hand.sort();
 		Card[] straight = new Card[5];
 		int i = 0;
 		straight[i] = hand.getCard(0);
 		for (Card card : hand) {
 			int comp = straight[i].compareTo(card);
 			if(comp == -1){
 				straight[++i] = card;
 				if(i == 4)
 					return new Pile(straight);
 			} else if (comp < -1){
 				straight[i = 0] = card;
 			}
 		}
 		if(i == 3 && straight[0].getValue() == Value.FIVE && (straight[4] = hand.getCard(0)).getValue() == Value.ACE)
 			return new Pile(straight);
 		return null;
 	}
 	
 	private static List<Pile> partition(Pile hand) {
 		Map<Value,Pile> map = new EnumMap<Value,Pile>(Value.class);
 		for(Card card : hand){
 			Pile pile = map.get(card.getValue());
 			if(pile == null)
 				map.put(card.getValue(), new Pile(card));
 			else
 				pile.add(card);
 		}
 		List<Pile> list = new ArrayList<Pile>(map.values());
 		Collections.sort(list);
 		return list;
 	}
 
 	private static HandRating straightFlush(Pile straight) {
 		Value[] tie = new Value[1];
 		tie[0] = straight.getCard(0).getValue();
 		return new HandRating(PokerHands.STRAIGHTFLUSH,tie);
 	}
 
 	private static HandRating quad(List<Pile> partitions) {
 		Value[] tie = new Value[2];
 		setTieInfo(tie,partitions);
 		return new HandRating(PokerHands.QUAD,tie);
 	}
 	
 	private static HandRating boat(List<Pile> partitions) {
 		Value[] tie = new Value[2];
 		setTieInfo(tie,partitions);
 		return new HandRating(PokerHands.BOAT,tie);
 	}
 	
 	private static HandRating flush(Pile flush) {
 		flush.sort();
 		Value[] tie = new Value[5];
 		for(int i = 0; i < tie.length; i++)
 			tie[i] = flush.getCard(i).getValue();
 		return new HandRating(PokerHands.FLUSH,tie);
 	}
 	
 	private static HandRating straight(Pile straight){ 
 		Value[] tie = new Value[1];
 		tie[0] = straight.getCard(0).getValue();
 		return new HandRating(PokerHands.STRAIGHT,tie);
 	}
 	
 	private static HandRating trips(List<Pile> partitions) {
 		Value[] tie = new Value[3];
 		setTieInfo(tie,partitions);
 		return new HandRating(PokerHands.TRIPS,tie);
 	}
 	
 	private static HandRating twopair(List<Pile> partitions) {
 		Value[] tie = new Value[3];
 		setTieInfo(tie,partitions);
 		return new HandRating(PokerHands.TWOPAIR,tie);
 	}
 	
 	private static HandRating onepair(List<Pile> partitions) {
 		Value[] tie = new Value[4];
 		setTieInfo(tie,partitions);
 		return new HandRating(PokerHands.ONEPAIR,tie);
 	}
 	
 	private static HandRating highcard(List<Pile> partitions) {
 		Value[] tie = new Value[5];
 		setTieInfo(tie,partitions);
 		return new HandRating(PokerHands.HIGHCARD,tie);
 		
 	}
 	
 	private static void setTieInfo(Value[] tie,List<Pile> partitions){
 		for(int i = 0; i < tie.length; i++)
 			tie[i] = partitions.get(i).getCard(0).getValue();
 	}
 
 
 	
 	/**
 	 * dirty dirty hack, returns the power rating in the int[] format. 
 	 * @return lawl i are return
 	 */
 	@Deprecated
 	public int[] asIntArray(){
 		int[] rating = new int[tieInfo.length + 1];
 		rating[0] = type.ordinal() + 1;
 		for(int i = 1; i < tieInfo.length; i++)
 			rating[i] = tieInfo[i-1].ordinal() + 2;
 		return rating;
 	}
 	
 	@Override
 	public int compareTo(HandRating that) {
 		int diff = this.type.ordinal() - that.type.ordinal();
 		if(diff != 0)
 			return diff;
 		for(int i = 0; i < tieInfo.length; i++){
 			diff = this.tieInfo[i].ordinal() - that.tieInfo[i].ordinal();
 			if(diff != 0) 
 				return diff;
 		}
 		return 0;
 	}
 	
 	//TODO: Implement this.
 	public boolean inPercentile(Pile hand, int players, int percentile) {
 		return true;
 	}
 	
 	public String toString(){
 		Value[] t = tieInfo;
 		switch(type){
 		
 		case HIGHCARD: 
 			return String.format("Highcards %c %c %c %c %c",t[0].pip,t[1].pip,t[2].pip,t[3].pip,t[4].pip);
 		case ONEPAIR: 
 			return String.format("Pair of %s, %c %c %c kickers",t[0].plural,t[1].pip,t[2].pip,t[3].pip);
 		case TWOPAIR: 
 			return String.format("Two pair %s and %s, %c kicker",t[0].plural,t[1].plural,t[2].pip);
 		case TRIPS:
 			return String.format("Three of a Kind %s, %c %c kickers",t[0].plural,t[1].pip,t[2].pip);
 		case STRAIGHT:
 			return String.format("Straight %s high",t[0].singular);
 		case FLUSH:
 			return String.format("Flush %s high, %s %s %s %s kickers",t[0].singular,t[1].pip,t[2].pip,t[3].pip,t[4].pip);
 		case BOAT: 
 			return String.format("Full House %s full of %s",t[0].plural,t[1].plural);
 		case QUAD:
 			return String.format("Four of a Kind %s, %c kicker",t[0].plural,t[1].pip);
 		case STRAIGHTFLUSH:
 			if(t[0] == Value.ACE)
 				return "Royal Flush";
 			return String.format("Straight Flush %s high",t[0].singular);
 		}
 		throw new RuntimeException("This shouldn't happen!");
 	}
 	
 	static enum PokerHands {
 		HIGHCARD,ONEPAIR,TWOPAIR,TRIPS,STRAIGHT,FLUSH,BOAT,QUAD,STRAIGHTFLUSH;
 	}
 	
	public static HandRating pair(Value kicker1, Value kicker2, Value kicker3, Value kicker4, Value kicker5) {
 		Value[] tieInfo = {kicker1, kicker2, kicker3, kicker4, kicker5};
 		return new HandRating(PokerHands.HIGHCARD, tieInfo);
 	}
 	
 	public static HandRating pair(Value pair, Value kicker1, Value kicker2, Value kicker3) {
 		Value[] tieInfo = {pair, kicker1, kicker2, kicker3};
 		return new HandRating(PokerHands.ONEPAIR, tieInfo);
 	}
 	
 	public static HandRating twoPair(Value pair, Value pair2, Value kicker) {
 		Value[] tieInfo = {pair, pair2, kicker};
 		return new HandRating(PokerHands.TWOPAIR, tieInfo);
 	}
 	
 	public static HandRating trips(Value trip, Value kicker1, Value kicker2) {
 		Value[] tieInfo = {trip, kicker1, kicker2};
 		return new HandRating(PokerHands.TRIPS, tieInfo);
 	}
 	
 	public static HandRating straight(Value highCard) {
 		Value[] tieInfo = {highCard};
 		return new HandRating(PokerHands.STRAIGHT, tieInfo);
 	}
 	
 	public static HandRating Flush(Value highCard, Value kicker1, Value kicker2, Value kicker3, Value kicker4) {
 		Value[] tieInfo = {highCard, kicker1, kicker2, kicker3, kicker4};
 		return new HandRating(PokerHands.FLUSH, tieInfo);
 	}
 	
 	public static HandRating boat(Value trips, Value pair, Value kicker) {
 		Value[] tieInfo = {trips, pair};
 		return new HandRating(PokerHands.BOAT, tieInfo);
 	}
 	
 	public static HandRating quads(Value quad, Value kicker) {
 		Value[] tieInfo = {quad, kicker};
 		return new HandRating(PokerHands.QUAD, tieInfo);
 	}
 	
 	public static HandRating straightFlush(Value highCard) {
 		Value[] tieInfo = {highCard};
 		return new HandRating(PokerHands.STRAIGHTFLUSH, tieInfo);
 	}
 	
 	public PokerHands getType() {
 		return type;
 	}
 	
 	public boolean isPair() {
 		return type == PokerHands.ONEPAIR;
 	}
 	
 	public boolean isTwoPair() {
 		return type == PokerHands.TWOPAIR;
 	}
 	
 	public boolean isTrips() {
 		return type == PokerHands.TRIPS;
 	}
 	
 	public boolean isStraight() {
 		return type == PokerHands.STRAIGHT;
 			
 	}
 
 	public boolean isFlush() {
 		return type == PokerHands.FLUSH;
 			
 	}
 	
 	public boolean isBoat() {
 		return type == PokerHands.BOAT;
 			
 	}
 	
 	public boolean isQuad() {
 		return type == PokerHands.QUAD;
 			
 	}
 	
 	public boolean isStraightFlush() {
 		return type == PokerHands.STRAIGHTFLUSH;		
 	}
 }
