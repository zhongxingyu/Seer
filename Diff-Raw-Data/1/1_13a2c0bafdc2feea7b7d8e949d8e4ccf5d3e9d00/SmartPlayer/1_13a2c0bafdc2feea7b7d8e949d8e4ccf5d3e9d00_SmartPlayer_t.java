 package com.rachum.amir.skyhiking.players;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.rachum.amir.skyhiking.Card;
 import com.rachum.amir.skyhiking.Game;
 import com.rachum.amir.skyhiking.Move;
 import com.rachum.amir.skyhiking.MoveHandler;
 import com.rachum.amir.skyhiking.PayHandler;
 import com.rachum.amir.util.range.Range;
 
 public class SmartPlayer extends Player {
 	int cardsPerDie;
 
 	public SmartPlayer(String name) {
 		super(name);
 		List<Integer> options = Arrays.asList(3,3,3,4,4,4,5);
 		Collections.shuffle(options);
 		cardsPerDie = options.get(0);
 	}
 
 	@Override
 	public void play(MoveHandler handler, Game context) {
 		// TODO Auto-generated method stub
 		List<Move> options = new LinkedList<Move>();
 		int toPay = 0;
 		if (context.pilot != this) {
 			toPay = context.diceRoll.size();
 		} else {
 			toPay = context.level.getDiceNumber() - 1;
 		}
 		if (this.getScore() + context.level.getScore() >= 50) {
 			handler.move(Move.LEAVE);
 			return;
 		}
 		int handSize = context.pilot.getHand().getCards().size();
 		int added = 0;
 		while (added <= toPay) {
 			if (handSize >= cardsPerDie) {
 				options.add(Move.STAY);
 				handSize -= cardsPerDie;
 				added += 1;
 			} else {
 				break;
 			}
 		}
 		if (added < toPay) { // not "enough" cards.
 			for (int i : new Range(toPay*cardsPerDie - handSize)) {
 				options.add(Move.LEAVE);
 			}
 		} else {
 			for (int i : new Range(handSize)) {
 				options.add(Move.STAY);
 			}
 		}
 		Collections.shuffle(options);
 		handler.move(options.get(0));
 	}
 
 	@Override
 	public void pay(PayHandler handler, Game context) {
 	    if (hand.contains(context.diceRoll)) {
 	    	hand.discard(context.diceRoll);
 	        handler.pay(true, context.diceRoll);
 	        return;
 	    } else if (hand.contains(Card.WILD)) {
 	    	List<Boolean> options = new LinkedList<Boolean>();
 	    	if (context.level.getScore() >= 9) {
	    		options.add(true);
 	    		for (int i : new Range(context.level.getScore()-9)) {
 	    			options.add(true);
 	    		}
 	    		for (int i : new Range((context.remainingPlayers.size() - 1) * 2)) {
 	    			options.add(false);
 	    		}
 	    		Collections.shuffle(options);
 	    		if (options.get(0)) {
 	    			hand.discard(Card.WILD);
 	    			handler.pay(true, Arrays.asList(Card.WILD));
 	    			return;
 	    		}
 	    	}
 	    }
 	    handler.pay(false, null);
 	}
 }
