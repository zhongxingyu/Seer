 package manager;
 
 import cards.*;
 
 import java.util.*;
 
 public class Utility {
 	
 	private Utility(){}
 	
 	public static int[] calcCardPower(Pile... piles) {
 		Pile pile = new Pile(piles);
 		
 		//Pair
 		
 		if(onePair(pile)) {
 			return onePairRating(pile);
 		}
 		//Two pairs		
 		else if(twoPair(pile)) {
 			return twoPairRating(pile);
 		}
 		//Three of a kind.
 		else if(trips(pile)) {
 			return tripsRating(pile);
 		}
 		
 		//Straight flush
 		else if(isStraight(pile) && isFlush(pile)) {
 			return straightFlushRating(pile);
 		}
 		//straight
 		else if(isStraight(pile)) {
 			return straightRating(pile);
 		}
 		
 		//Flush
 		else if(isFlush(pile)) {
 			return flushRating(pile);
 		}
 		
 		//Boat
 		else if(boat(pile)) {
 			return boatRating(pile);
 		}
 		
 		//Quads
 		else if(quads(pile)) {
 			return quadsRating(pile);
 		}
 		return highCardRating(pile);
 	}
 	
 
 	private static int[] highCardRating(Pile pile) {
 		ArrayList<Integer> values = new ArrayList<Integer>();
 		for(int i = 0; i < pile.getCardCount(); i++) {
 			values.add(pile.getCard(i).getIntValue());
 		}
 		Collections.sort(values);
 		Collections.reverse(values);
 		int[] v = new int[pile.getCardCount() + 1];
 		v[0] = 1;
 		for(int i = 1; i < values.size() + 1; i++) {
 			v[i] = values.get(i-1);
 		}
 		return v;
 		
 	}
 	
 	private static int[] valueSort(Pile pile) {
 		ArrayList<Integer> values = new ArrayList<Integer>();
 		for(int i = 0; i < pile.getCardCount(); i++) {
 			values.add(pile.getCard(i).getIntValue());
 		}
 		Collections.sort(values);
 		Collections.reverse(values);
 		int[] v = new int[pile.getCardCount()];
 		for(int i = 0; i < v.length; i++) {
 			v[i] = values.get(i);
 		}
 		return v;
 	}
 	
 	private static boolean isFlush(Pile pile) {
 		int c = 0;
 		int h = 0;
 		int s = 0;
 		int d = 0;
 		
 		for(int i = 0; i < pile.getCardCount(); i++) {
 			switch(pile.getCard(i).getSuit()) {
 			case CLUBS: c++; continue;
 			case HEARTS:  h++; continue;
 			case SPADES: s++; continue;
 			case DIAMONDS: d++; continue;
 			}
 		}
 		if(s > 4 || c > 4 || h > 4 || d > 4) {
 			return true;
 		}
 		return false;
 	}
 	
 	private static int[] flushRating(Pile pile) {
 		int[] rating = new int[6];
 		rating[0] = 6;
 		Pile flushPile = new Pile();
 		int count=0;
 		for(Card card : pile) {
 			for(Card card2 : pile) {
 				if(card.getSuit() == card2.getSuit()) {
 					count++;
 				}
 				if(count >2) {
 					flushPile.add(card);
 					continue;
 				}
 			}
 			count=0;
 		}
 		//Remove the cards which are of dissimilar suit.
 		int[] values = valueSort(flushPile);
 		int index =0;
 		for(int i = 1; i < rating.length; i++) {
 			rating[i] = values[index];
 			index++;
 		}
 		return rating;
 	}
 	
 	private static boolean isStraight(Pile pile) {
 		int straighteningCards = pile.getCardCount();
 		int[] values = valueSort(pile);
 		boolean flag=true;
 		for(int i = 0; i < pile.getCardCount() - 1; i++) {
 			if(!(values[i] == values [i+1] - 1 | values[i] == values[i+1] + 1)) {
 				straighteningCards--;
 			}
 			if(straighteningCards < 5) {
 				flag = false;
 				break;
 			}
 		}
 		
 		//Change any ace to 1 and try again
 		for(int i=0; i < values.length; i++) {
 			if(values[i] == 14) {
 				values[i] = 1;
 			}
 		}
 		
 		//sort array again
 		values=sort(values);
 		straighteningCards= pile.getCardCount();
 		for(int i = 0; i < pile.getCardCount() - 1; i++) {
 			if(!(values[i] == values [i+1] - 1 | values[i] == values[i+1] + 1)) {
 				straighteningCards--;
 			}
 			if(straighteningCards < 5) {
 				return flag | false;
 			}
 		}
 				
 		return true;
 	}
 	
 	private static int[] straightRating(Pile pile) {
 		int[] rating = new int[2];
 		int[] values = valueSort(pile);
 		rating[0] = 5;
 		for(int i = 0; i < values.length - 4; i++) {
 			if(values[i] == values[i+1] + 1 && values[i] == values[i+2] +2 && values[i] == values[i+3] + 3 && values[i] == values[i+4] + 4) {
 				rating[1] = values[i];
 				return rating;
 			}
 		}
 		
 		//Change any ace to 1 and try again
 		for(int i=0; i < values.length; i++) {
 			if(values[i] == 14) {
 				values[i] = 1;
 			}
 		}
 		//sort array again
 		values=sort(values);
 		
 		for(int i = 0; i < values.length - 4; i++) {
 			if(values[i] == values[i+1] + 1 && values[i] == values[i+2] +2 && values[i] == values[i+3] + 3 && values[i] == values[i+4] + 4) {
 				rating[1] = values[i];
 				return rating;
 			}
 		}
 		
 		throw new IllegalArgumentException();
 	}
 	
 	private static boolean onePair(Pile pile){
 		if(isFlush(pile) || isStraight(pile)) {
 			return false;
 		}
 		int[] values = valueSort(pile);
 		int equalCards = 0;
 		for(int i = 0; i < values.length - 1; i++) {
 			
 			if(values[i] == values[i+1]) {
 				equalCards++;
 			}
 		}
 		//two pairs, full house, quads
 		if(equalCards > 1) {
 			return false;
 		}
 		return equalCards > 0;
 	}
 	
 	private static int[] onePairRating(Pile pile) {
 		int[] rating = new int[5];
 		rating[0] = 2;
 		int index = 0;
 		int rIndex = 0;
 		int[] values = valueSort(pile);
 		for(int i = 0; i < values.length - 1; i++) {
 			if(values[i] == values[i+1]) {
 				rating[1] = values[i];
 				index = i;
 			}
 		}
 		for(int i =0; i< values.length; i++) {
 			if(i == index | i == index + 1) {
 				continue;
 			}
 			if(rIndex +2 == rating.length) {
 				return rating;
 			}
 			else {
 			rating[2 + rIndex] = values[i];
 			rIndex++;
 			}
 			
 		}
 		return rating;
 	}
 	
 	public static boolean twoPair(Pile pile) {
 		int[] rating = new int[5];
 		rating[0] = 3;
 		int[] values = valueSort(pile);
 		int pairCount = 0;
 		for(int i = 0; i < values.length - 2; i++) {
 			//trips, boat, quads
 			if(values[i] == values[i+1] && values[i]== values[i+2]) {
 				return false;
 			}
 		}
 		for(int i = 0; i < values.length - 1; i++) {
 			if(values[i] == values[i+1]) {
 				pairCount++;
 			}
 		}
 		return pairCount > 1;
 	}
 	
 	public static int[] twoPairRating(Pile pile) {
 		int[] rating = new int[4];
 		rating[0] = 3;
 		int[] values = valueSort(pile);
 		int highPair = 0;
 		int lowPair = 0;
 		
 		//Set high pair
 		for(int i = 0; i < values.length - 1; i++) {
 			if(values[i] == values[i+1]) {
 				rating[1] = values[i];
 				highPair = i;
 			}
 		}
 		//Set second pair
 		for(int i = highPair; i < values.length - 1; i++) {
 			if(values[i] == values[i+1]) {
 				rating[1] = values[i];
 				lowPair = i;
 			}
 		}
 		for(int j = 0; j < values.length; j++) {
 			if(j==highPair || j==highPair + 1 || j==lowPair ||j==lowPair+1) {
 				continue;
 			}
			rating[4] = values[j];
 		}					
 		return rating;
 	}
 	
 	public static boolean trips(Pile pile) {
 		int[] values = valueSort(pile);
 		int tripIndex = -1 ;
 		if(isFlush(pile) || isStraight(pile)) {
 			return false;
 		}
 		for(int i = 0; i < values.length -3; i++) {
 			//quads
 			if(values[i] == values[i+1] && values[i]== values[i+2] && values [i] == values[i+3]) {
 				return false;
 			}
 		}
 		
 		for(int i = 0; i < values.length - 2; i++) {
 			//trips
 			if(values[i] == values[i+1] && values[i]== values[i+2]) {
 				tripIndex = i;
 			}
 		}
 		//if we find trips we have to make sure we cannot find another pair as this would mean a full house.
 		for(int i = 0; i < values.length; i++) {
 			//we found trips, skip index containing this value, check for pair.
 			if(tripIndex != - 1 && i != tripIndex && i != tripIndex + 1 && i != tripIndex + 2) {
 				if(values[i] == values[i+1]) {
 					return false;
 				}
 			}
 		}
 		return tripIndex > 0;
 	}
 	
 	public static int[] tripsRating(Pile pile) {
 		int[] values = valueSort(pile);
 		int[] rating = {4, 0, 0, 0};
 		int tripIndex = -1;
 		int rIndex =0;
 		for(int i = 0; i < values.length-1; i++) {
 			if(values[i] == values[i+1]) {
 				tripIndex = i;
 				rating[1] = values[i];
 				break;
 			}
 		}
 		
 		for(int i = 0; i < values.length; i++) {
 			if( i== tripIndex | i == tripIndex + 1 | i == tripIndex +2) {
 				continue;
 			}
 			if(rIndex +2 == rating.length) {
 				return rating;
 			}
 			rating[2+rIndex] = values[i];
 			rIndex++;
 		}
 		return rating;
 	}
 	
 	public static boolean boat(Pile pile) {
 		int[] values = valueSort(pile);
 		int tripIndex = -1 ;
 		if(isFlush(pile) || isStraight(pile)) {
 			return false;
 		}
 		for(int i = 0; i < values.length -3; i++) {
 			//quads
 			if(values[i] == values[i+1] && values[i]== values[i+2] && values [i] == values[i+3]) {
 				return false;
 			}
 		}
 		
 		for(int i = 0; i < values.length-2; i++) {
 			//trips
 			if(values[i] == values[i+1] && values[i]== values[i+2]) {
 				tripIndex = i;
 			}
 		}
 		//if we find trips we have to make sure we find another pair as this would mean a full house.
 		for(int i = 0; i < values.length; i++) {
 			//we found trips, skip index containing this value, check for pair.
 			if(tripIndex != - 1 && i != tripIndex && i != tripIndex + 1 && i != tripIndex + 2) {
 				if(values[i] == values[i+1]) {
 					return true;
 				}
 			}
 		}
 		return false;		
 	}
 	
 	public static int[] boatRating(Pile pile) {
 		int[] values = valueSort(pile);
 		int[] rating = {7, 0, 0};
 		int tripIndex = -1;
 		for(int i = 0; i < values.length -2; i++) {
 			if(values[i] == values[i+1] && values[i] == values[i+2]) {
 				tripIndex = i;
 				rating[1] = values[i];
 				break;
 			}
 		}
 		
 		for(int i = 0; i < values.length -1; i++) {
 			if(i == tripIndex | i == tripIndex + 1 | i == tripIndex + 2) {
 				continue;
 			}
 			if(values[i] == values[i+1]) {
 				rating[2] = values[i];
 			}
 		}
 		return rating;
 	}
 	
 	public static boolean quads(Pile pile) {
 		int[] values = valueSort(pile);
 		for(int i = 0; i < values.length -3; i++) {
 			//quads
 			if(values[i] == values[i+1] && values[i]== values[i+2] && values [i] == values[i+3]) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public static int[] quadsRating(Pile pile) {
 		int[] values = valueSort(pile);
 		int[] rating = {8, 0, 0};
 		int index = 0;
 		for(int i = 0; i < values.length -3; i++) {
 			if(values[i] == values[i+1] && values[i]== values[i+2] && values [i] == values[i+3]) {
 				rating[1] = values[i];
 				index =i;
 			} 
 		}
 		
 		for(int i =0; i< values.length; i++) {
 			if(i == index | i == index + 1 | i==index +2 | i == index + 3) {
 				continue;
 			}
 			rating[2] = values[i];
 			return rating;
 		}
 		throw new IllegalArgumentException();
 	}
 	
 	public static int[] straightFlushRating(Pile pile){
 		int[] rating = {9, 0};
 		Pile flushPile = new Pile();
 		int count=0;
 		
 		//Create a new pile containing only the cards of correct suit. 5 + 2 cards when flush is known, add to flushpile if card has same suit as > 2 other cards.
 		for(Card card : pile) {
 			for(Card card2 : pile) {
 				if(card.getSuit() == card2.getSuit()) {
 					count++;
 				}
 				if(count >2) {
 					flushPile.add(card);
 					break;
 				}
 			}
 			count=0;
 		}
 		//The straight flush rating is now the straight rating of the flush pile
 		rating[1] = straightRating(flushPile)[1];
 		return rating;
 	}
 	
 	private static int[] sort(int[] array) {
 		Arrays.sort(array);
 		int[] a = new int[array.length];
 		int index = 0;
 		for(int i = array.length -1; i> -1; i--) {
 			a[index] = array[i];
 			index++;
 		}
 		return a;
 		
 	}
 }
 	
 	
