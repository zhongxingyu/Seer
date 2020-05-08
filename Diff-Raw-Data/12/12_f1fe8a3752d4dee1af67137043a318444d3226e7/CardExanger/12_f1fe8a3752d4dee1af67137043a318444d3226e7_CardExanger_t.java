 package se.chalmers.dat255.risk.model;
 
 import java.util.ArrayList;
 
 public class CardExanger {
 	private ICard card1 = null;
 	private ICard card2 = null;
 
 	public CardExanger() {
 
 	}
 
 	/**
 	 * Checks if the player has clicked cards before. If this is the third card
 	 * that is clicked
 	 * 
 	 * @param The
 	 *            last card clicked.
 	 * @param The
 	 *            current player.
 	 * @return
 	 */
 	public ArrayList<String> makeExange(ICard card, Player currentPlayer) {
 		if (!handledBefore(card)) {
 			if (card2 != null) {
 				card.setActive(true);
 				if (currentPlayer.exchangeCard(card1, card2, card)) {
 					ArrayList<String> cardProvinces = new ArrayList<String>();
 					cardProvinces.add(card.getName());
 					cardProvinces.add(card1.getName());
 					cardProvinces.add(card2.getName());
 					card1 = null;
 					card2 = null;
 					return cardProvinces;
 				}
 				card1 = null;
 				card2 = null;
 				return null;
 			} else {
 				if (card1 == null) {
 					card1 = card;
 					card1.setActive(true);
 				} else {
 					card2 = card;
 					card2.setActive(true);
 				}
 				return null;
 			}
 		}
 		return null;
 	}
 
 	/*
 	 * If a card has already been activated then you want to deactivate and
 	 * remove it. If two cards activated and you press the first card then the
 	 * second card reference is moved to the first card variable, and second
 	 * card is then set to null. Otherwise the card variable is just set to
 	 * null;
 	 */
 	private boolean handledBefore(ICard card) {
 		if (card.isActive()) {
 			if (card.equals(card2)) {
 				card2 = null;
 			} else if (card.equals(card1)) {
				card1=card2;
				card2=null;
 			}
			card.setActive(false);
 			return true;
 		}
 		return false;
 	}
 	
 	public void flushCards(){
 		if(card1 != null){
 			card1.setActive(false);
 		}
 		if(card2 != null){
 			card2.setActive(false);
 		}
 		
 		card1 = null;
 		card2 = null;
 		
 	}
 	
 	/*
 	 * Only for testing
 	 */
 	public ICard getCard1(){
 		return card1;
 	}
 	/*
 	 * Only for testing
 	 */
 	public ICard getCard2(){
 		return card1;
 	}
 	
 	
 }
