 package com.eddygao;
 
 import android.view.View;
 
 public class Action {
 	public enum ActionType {
 		move, flip, dump;
 	}
 	private ActionType action;
 	private Deck src, dst;
 	private Card card;
 	private MainView parentView;
 	
 	public Action(Deck src, Card card, MainView parent) {
 		action = ActionType.move;
 		this.card = card;
 		this.src = src;
 		this.parentView = parent;
 	}
 	
 	public Action(Card card, MainView parent) {
 		action = ActionType.flip;
 		this.card = card;
 		this.parentView = parent;
 	}
 	
 	public Action(Deck src, Deck dst, MainView parent) {
 		action = ActionType.dump;
 		this.src = src;
 		this.dst = dst;
 		this.parentView = parent;
 	}
 	
 	public void undo() {
 		if (action == ActionType.move) { 
 			Deck parent = card.getParent();
 			parent.removeCard(card);
 			parentView.moveToDeck(parent, src, card); 
			parentView.invalidate();
 		}
 		if (action == ActionType.flip) {
 			card.setReveal(!card.isRevealed());
 			card.setImage(parentView.cardImage(card));
 			parentView.invalidate();
 		}
 		if (action == ActionType.dump) {
 			while (!dst.isEmpty()) {
 				src.addCard(dst.topDeck());
 				dst.removeCard(dst.topDeck());
 			}
			parentView.invalidate();
 		}
 	}
 }
