 package org.jdominion.effects.darkAges;
 
 import org.jdominion.Card;
 import org.jdominion.Card.Type;
 import org.jdominion.Player;
 import org.jdominion.Supply;
 import org.jdominion.Turn;
 import org.jdominion.effects.CardEffectAction;
 
 public class VagrantEffect extends CardEffectAction {
 
 	@Override
 	public boolean execute(Player activePlayer, Turn currentTurn, Supply supply) {
 		Card revealedCard = activePlayer.revealCard();
		if (revealedCard == null) {
			return false;
		}
 		if (revealedCard.isOfType(Type.CURSE) || revealedCard.isOfType(Type.RUINS) || revealedCard.isOfType(Type.SHELTER)
 				|| revealedCard.isOfType(Type.VICTORY)) {
 			activePlayer.getHand().add(revealedCard);
 		} else {
 			activePlayer.placeOnDeck(revealedCard);
 		}
 		return true;
 	}
 
 }
