 package org.jdominion.effects.prosperity;
 
 import org.jdominion.Card;
 import org.jdominion.CardList;
 import org.jdominion.Player;
 import org.jdominion.Supply;
 import org.jdominion.Turn;
 import org.jdominion.effects.TrashCards;
 
 public class ForgeEffect extends TrashCards {
 
 	public ForgeEffect() {
 		super(0, Integer.MAX_VALUE);
 	}
 
 	@Override
 	public boolean execute(Player activePlayer, Turn currentTurn, Supply supply) {
 		CardList cardsToTrash = chooseCardsToTrash(activePlayer, currentTurn.getGame(), currentTurn, supply);

 		GainCardWhichCostsExactlyX gainEffect = new GainCardWhichCostsExactlyX(calculateTotalCost(cardsToTrash));
 
 		return gainEffect.execute(activePlayer, currentTurn, supply);
 	}
 
 	private int calculateTotalCost(CardList cardsToTrash) {
 		int totalCost = 0;
 		for (Card card : cardsToTrash) {
 			totalCost += card.getCost();
 		}
 		return totalCost;
 	}
 
 }
