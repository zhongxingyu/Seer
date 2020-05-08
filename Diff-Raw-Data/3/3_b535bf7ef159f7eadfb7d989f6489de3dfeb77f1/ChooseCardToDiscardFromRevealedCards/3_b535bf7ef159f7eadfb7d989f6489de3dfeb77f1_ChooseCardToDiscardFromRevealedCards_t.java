 package org.jdominion.decisions.promo;
 
import java.util.ArrayList;
 import java.util.List;
 
 import org.jdominion.Card;
 import org.jdominion.CardList;
 import org.jdominion.Hand;
 import org.jdominion.Supply;
 import org.jdominion.Turn;
 import org.jdominion.Card.Type;
 import org.jdominion.decisions.ChooseFromRevealedCards;
 import org.jdominion.decisions.revealedCards.Discard;
 import org.jdominion.decisions.revealedCards.Option;
 import org.jdominion.decisions.revealedCards.PutInHand;
 import org.jdominion.decisions.revealedCards.RevealedCard;
 
 public class ChooseCardToDiscardFromRevealedCards extends ChooseFromRevealedCards {
 
 	public ChooseCardToDiscardFromRevealedCards(List<RevealedCard> revealedCards) {
 		super("Choose a card for your opponent to discard", false, revealedCards);
 	}
 
 	@Override
 	public void changeOption(RevealedCard card, Option newOption) {
 		// Because there has to be one card to discard you can't switch from Discard => PutInHand
 		// Otherwise we wouldn't know which card to discard
 		if (newOption == PutInHand.getInstance()) {
 			return;
 		}
 		// make sure that exactly one card gets discarded
 		for (RevealedCard otherCard : this.getRevealedCards()) {
 			if (otherCard != card) {
 				if (otherCard.getChoosenOption() == Discard.getInstance()) {
 					otherCard.setChoosenOption(PutInHand.getInstance());
 				}
 			}
 		}
 		card.setChoosenOption(newOption);
 	}
 
 	@Override
 	public void chooseDefaultAnswer(Hand hand, Turn currentTurn, Supply supply) {
 		CardList revealedCards = getCardListFromRevealedCards(getRevealedCards());
 		Card cardToDiscard = findCardToDiscard(currentTurn, revealedCards);
 		for (RevealedCard revealedCard : getRevealedCards()) {
 			if (revealedCard.getRevealedCard().equals(cardToDiscard)) {
 				revealedCard.setChoosenOption(Discard.getInstance());
 			} else {
 				revealedCard.setChoosenOption(PutInHand.getInstance());
 			}
 		}
		this.setAnswer(new ArrayList<RevealedCard>(getRevealedCards()));
 	}
 
 	private Card findCardToDiscard(Turn currentTurn, CardList revealedCards) {
 		// discard the most expensive action card if the player has actions left
 		// TODO: should know about golem or double-throne room which could provide actions
 		if (currentTurn.getAvailableActions() > 0 && revealedCards.contains(Type.ACTION)) {
 			return revealedCards.getCardsOfType(Type.ACTION).getMostExpensiveCard();
 		}
 		// discard the most expensive treasure
 		if (revealedCards.contains(Type.TREASURE)) {
 			return revealedCards.getCardsOfType(Type.TREASURE).getMostExpensiveCard();
 		}
 
 		// no treasure nor actions: just discard the most expensive card
 		return revealedCards.getMostExpensiveCard();
 	}
 
 	@Override
 	public boolean isValidAnswer(List<RevealedCard> answer) {
 		int cardsToDiscard = 0;
 		for (RevealedCard revealedCard : answer) {
 			if (revealedCard.getChoosenOption() == Discard.getInstance()) {
 				cardsToDiscard++;
 			}
 		}
 		if (cardsToDiscard != 1) {
 			return false;
 		}
 		return super.isValidAnswer(answer);
 	}
 
 }
