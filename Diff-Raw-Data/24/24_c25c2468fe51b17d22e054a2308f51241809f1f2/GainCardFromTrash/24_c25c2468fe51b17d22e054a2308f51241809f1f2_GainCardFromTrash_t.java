 package org.jdominion.effects.darkAges;
 
 import org.jdominion.Card;
 import org.jdominion.CardList;
 import org.jdominion.Player;
 import org.jdominion.Supply;
 import org.jdominion.Turn;
 import org.jdominion.decisions.darkAges.ChooseCardToGainFromTrash;
 import org.jdominion.effects.CardEffectAction;
 import org.jdominion.location.TopOfDeck;
 
 public class GainCardFromTrash extends CardEffectAction {
 
 	@Override
 	public boolean execute(Player activePlayer, Turn currentTurn, Supply supply) {
 		CardList cardsToGain = new CardList();
 		for(Card cardInTrash: currentTurn.getGame().getTrash()) {
 			if (cardInTrash.getCost() >= 3 && cardInTrash.getCost() <= 6) {
 				cardsToGain.add(cardInTrash);
 			}
 		}
 		if (cardsToGain.isEmpty()) {
 			return false;
 		}
 
 		ChooseCardToGainFromTrash decision = new ChooseCardToGainFromTrash(cardsToGain);
 
 		activePlayer.decide(decision, this);
 
 		Card choosenCard = decision.getChoosenCard();
 
		currentTurn.getGame().getTrash().gainFromTrash(activePlayer, choosenCard, currentTurn, supply);
 
 		activePlayer.gainCard(choosenCard, new TopOfDeck(), currentTurn, supply);
 
 		return true;
 	}
 
 	@Override
 	public String getText() {
 		return "Gain a card from the trash costing from 3 to 6 putting it on top of your deck";
 	}
 }
