 package com.turpgames.ichigu.model.game;
 
import com.turpgames.framework.v0.util.Utils;
 
 public abstract class CardDealer {
	protected final Card[] deck;
 	protected ICardDealerListener listener;
 
 	protected CardDealer() {
		deck = Card.newDeck();
 	}
 
 	public abstract void deal();
 	public abstract void abortDeal();
 
 	public void reset() {
		for (int i = 0; i < deck.length; i++) {
			deck[i].deselect();
			deck[i].deactivate();
			deck[i].getColor().a = 1;
		}
		Utils.shuffle(deck);
 	}
 
 	public void setListener(ICardDealerListener listener) {
 		this.listener = listener;
 	}
 
 	protected void notifyDealEnd() {
 		if (listener != null)
 			listener.onDealEnd();
 	}
 }
