 package com.turpgames.ichigu.model.game;
 
 
 public abstract class CardDealer {
	protected Card[] deck;
 	protected ICardDealerListener listener;
 
 	protected CardDealer() {
		reset();
 	}
 
 	public abstract void deal();
 	public abstract void abortDeal();
 
 	public void reset() {
		deck = Card.newDeck();
 	}
 
 	public void setListener(ICardDealerListener listener) {
 		this.listener = listener;
 	}
 
 	protected void notifyDealEnd() {
 		if (listener != null)
 			listener.onDealEnd();
 	}
 }
