 /**
  * 
  */
 package poker.hand_card;
 
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * 
  * 
  */
public class CheckerImpl implements Checker {
 	
 	private List<CheckResult> results = new LinkedList<CheckResult>();
 
 	@Override
 	public CheckResult check(Hand hand) {
 		results.clear(); //to allow checker reuse
 		
		boolean straight = checkStraight(hand);	//adds to results if true
		boolean flush = checkFlush(hand);	//adds to results if true
		if(straight && flush) {
 			hand = hand.sortByRank();
			return new CheckResult(ConditionType.StraightFlush, hand);
 		}
 
 		MultiplesChecker multiplesChecker = MultiplesCheckerFactory.getInstance().getMultiplesChecker();
 		results.add(multiplesChecker.checkMultiples(hand));
 		
 		return Collections.max(results, CheckResult.getComparator());
 	}
 
 	private boolean checkStraight(Hand hand) {
		if (checkWheelStraight(hand)) {
			return true;
		}
 		
 		hand = hand.sortByRank();
 		Iterator<Card> it = hand.iterator();
 		
 		Card curr = it.next();
 		Card next = null;
 		while (it.hasNext()) {
 			next = it.next();
 			if ((curr.getRank().ordinal()-1)!=next.getRank().ordinal()) {
 				return false;
 			}
 			curr = next;
 		}
 		results.add( new CheckResult(ConditionType.Straight,hand));
 		return true;
 	}
 		
 	private boolean checkWheelStraight(Hand hand) {
 		Map<Rank, List<Card>> rankMap = Util.rankMap(hand);
 		
 		for (Entry<Rank,List<Card>> entry : rankMap.entrySet()) {
 			if ((entry.getKey()==Rank.Ace)||(entry.getKey().compareTo(Rank.Six)<0)) {
 				if (entry.getValue().size()<1) {return false;}
 			}
 		}
 		
 		List<Card> list = hand.getCards();
 		Card cardToMove = rankMap.get(Rank.Ace).get(0);		// to sort better ..?
 		list.remove(cardToMove);
 		list.add(cardToMove);
 		
 		results.add( new CheckResult(ConditionType.Straight, hand));
 		return true;
 	}
 
 	private boolean checkFlush(Hand hand) {
 		Map<Suit,List<Card>> suitMap = Util.suitMap(hand);
 		hand = hand.sortByRank();
 		for (List<Card> list : suitMap.values()) {
 			if (list.size()==5) {
 				results.add( new CheckResult(ConditionType.Flush, hand));
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	
 }
