 /**
  * 
  */
 package client.controller;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 
 import client.model.Card;
 import client.model.CardComparator;
 import client.model.CommunityCards;
 import client.model.Hand;
 import client.model.Player;
 
 
 /**
  * @author Vernon Schwehr
  *
  */
 public class HandController {
 	
 	public static final int ROYAL_FLUSH = 10;
 	public static final int STRAIGHT_FLUSH = 9;
 	public static final int FOUR_OF_A_KIND = 8;
 	public static final int FULL_HOUSE = 7;
 	public static final int FLUSH = 6;
 	public static final int STRAIGHT = 5;
 	public static final int THREE_OF_A_KIND = 4;
 	public static final int TWO_PAIR = 3;
 	public static final int PAIR = 2;
 	public static final int HIGH_CARD = 1; 
 
 	private final HashMap<Player, Hand> hands;
 	private final CommunityCards cCards;
 	private final ArrayList<Player> players;
 	private int points;
 	private int playerPoints;
 	private int cCardPoints;
 	
 	public HandController(ArrayList<Player> players, CommunityCards cCards) {
 		this.players = players;
 		hands = new HashMap<Player, Hand>();
 		for(Player player : this.players) {
 			hands.put(player, player.getHand());
 		}
 		this.cCards = cCards;
 	}
 	
 	public void doAnalyzeCCards() {
 		ArrayList<Card> cards = new ArrayList<Card>();
 		if(cCards.size() == 3) {
 			cards.add(cCards.get(0));
 			cards.add(cCards.get(1));
 			cards.add(cCards.get(2));
 			
 			setcCardPoints(checkHighCard(cards));
 			setcCardPoints(checkValueEquality(cards));
 		} 
 		if(cCards.size() == 4) {
 			cards.add(cCards.get(0));
 			cards.add(cCards.get(1));
 			cards.add(cCards.get(2));
 			cards.add(cCards.get(3));
 			
 			setcCardPoints(checkHighCard(cards));
 			setcCardPoints(checkValueEquality(cards));
 		} 
 		if(cCards.size() == 5) {
 			cards.add(cCards.get(0));
 			cards.add(cCards.get(1));
 			cards.add(cCards.get(2));
 			cards.add(cCards.get(3));
 			cards.add(cCards.get(4));
 			
 			if(points == 0)
 				setcCardPoints(checkRoyalFlush(cards));
 			if(points == 0)
 				setcCardPoints(checkStraightFlush(cards));
 			if(points == 0)
 				setcCardPoints(checkFullHouse(cards));
 			if(points == 0)
 				setcCardPoints(checkFlush(cards));
 			if(points == 0)
 				setcCardPoints(checkStraight(cards));
 			if(points == 0)
 				setcCardPoints(checkValueEquality(cards));
 			if(points == 0)
 				setcCardPoints(checkHighCard(cards));
 		}
 	}
 
 	private int checkRoyalFlush(ArrayList<Card> cards) {
 		// TODO Auto-generated method stub
 		
 		return points;
 	}
 
 	private int checkStraightFlush(ArrayList<Card> cards) {
 		// TODO Auto-generated method stub
 		
 		return points;
 	}
 
 	private int checkFullHouse(ArrayList<Card> cards) {
 		// TODO Auto-generated method stub
 		
 		return points;
 	}
 
 	private int checkFlush(ArrayList<Card> cards) {
 		// TODO Auto-generated method stub
 		ArrayList<Card> sameSuit = new ArrayList<Card>();
 		
 		boolean isFlush = false;
 		if(cards.size() >= 5) {
 			for(int i = 0; i < cards.size()-1; i++) {
 				String suit1 = cards.get(i).getSuit();
 				for(int j = i+1; j < cards.size(); j++) {
 					String suit2 = cards.get(j).getSuit();
 					if(suit1.equals(suit2) 
 							&& !sameSuit.contains(cards.get(j))) {
 						if(!sameSuit.contains(cards.get(i))) {
 							sameSuit.add(cards.get(i));
 						}
 						sameSuit.add(cards.get(j));
 					} 
 				}
 				if(sameSuit.size() > 0 && sameSuit.size() < 5) {
 					sameSuit.clear();
 				}
 				if(sameSuit.size() >= 5) {
 					isFlush = true;
 					break;
 				}
 			}
 		}
 		
 		if(isFlush) {
 			points = FLUSH;
 		}
 		
 		return points;		
 	}
 
 	/**
 	 * For checking a straight in the cards given.
 	 */
 	private int checkStraight(ArrayList<Card> cards) {
 		// TODO Auto-generated method stub
 		Collections.sort(cards, new CardComparator());
 		
 		for(int i = 0; i < cards.size()-1; i++) {
 			int firstVal = cards.get(i).getValue();
 			int nextVal = cards.get(i+1).getValue();
			int count = 0;
			while((firstVal == (nextVal-1)) || ((firstVal-1) == nextVal)) {
 				count++;
				firstVal = nextVal;
				nextVal = nextVal++;
 			}
 			if(count == 5) {
 				points = STRAIGHT;
 				break;
 			}
 		}
 		
 		return points;
 	}
 
 	/**
 	 * For checking for pair, two pair, 3 of a kind, 4 of a kind, or 
 	 * full house in the community cards.
 	 */
 	//TODO: Check for Full House (value equality)
 	private int checkValueEquality(ArrayList<Card> cards) {
 		ArrayList<Card> residual = new ArrayList<Card>();
 		ArrayList<Card> pair1 = new ArrayList<Card>();
 		ArrayList<Card> pair2 = new ArrayList<Card>();
 		
 		if(cards.size() >= 4) {
 			for(int i = 0; i < cards.size()-1; i++) {
 				for(int j = i+1; j < cards.size(); j++) {
 					if(!cards.get(i).equals(cards.get(j)) && 
 							cards.get(i).getValue() == cards.get(j).getValue()) {
 						points = PAIR;
 						pair1.add(cards.get(i));
 						pair1.add(cards.get(j));
 						break;
 					}
 				}
 				if(pair1.size() == 2) {
 					break;
 				}
 			}
 			for(Card card : cards) {
 				if(!pair1.contains(card)) {
 					residual.add(card);
 				}
 			}
 			
 			if(points == PAIR) {
 				for(int i = 0; i < residual.size()-1; i++) {
 					for(int j = i+1; j < residual.size(); j++) {
 						if(!residual.get(i).equals(residual.get(j)) &&
 								residual.get(i).getValue() == residual.get(j).getValue()) {
 							points = TWO_PAIR;
 							pair2.add(residual.get(i));
 							pair2.add(residual.get(j));
 							break;
 						}
 					}
 				}
 				if(points == PAIR) {
 					for(int i = 0; i < residual.size(); i++) {
 						if(residual.get(i).getValue() 
 								== pair1.get(0).getValue()) {
 							points = THREE_OF_A_KIND;
 						}
 					}
 				}
 			}
 			
 			if(points == TWO_PAIR) {
 				for(int i = 0; i < pair1.size(); i++) {
 					if(pair1.get(i).getValue() == pair2.get(i).getValue()) {
 						points = FOUR_OF_A_KIND;
 					}	
 				}
 			}
 		}
 		
 		return points;
 	}
 
 	/**
 	 * For checking high cards in the community cards.
 	 */
 	private int checkHighCard(ArrayList<Card> cards) {
 		if(points != 0)
 			points = 0;
 		
 		int max = 0;
 		for(int i = 0; i < cards.size(); i++) {
 			if(cards.get(i).getValue() > max) {
 				max = cards.get(i).getValue();
 				points = HIGH_CARD;
 			}
 		}
 		return points;
 	}
 
 	/**
 	 * @return the cc
 	 */
 	public CommunityCards getCc() {
 		return cCards;
 	}
 
 	/**
 	 * @return the players
 	 */
 	public ArrayList<Player> getPlayers() {
 		return players;
 	}
 
 	/**
 	 * @param cCardPoints the cCardPoints to set
 	 */
 	public void setcCardPoints(int cCardPoints) {
 		this.cCardPoints = cCardPoints;
 	}
 
 	/**
 	 * @return the cCardPoints
 	 */
 	public int getcCardPoints() {
 		return cCardPoints;
 	}
 	
 }
