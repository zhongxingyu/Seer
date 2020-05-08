 package fr.lo02.model.card.HazardCards;
 
 import fr.lo02.model.Player;
 import fr.lo02.model.card.Card;
 import fr.lo02.model.card.remedyCards.GoRoll;
 
 public class OutOfGas extends Card {
 
 	public OutOfGas() {
 		this.setHazardCard(true);
 	}
 
 	public Player checkValidMove(Player activePlayer, Player targetPlayer) {
 		Player p = null;
 		// Si il possede pas la botte "ExtraTank" et que la derniere carte est
 		// de type "RemedyCards"
		if (!(targetPlayer.isExtraTank()) && activePlayer.getLastCardFromBattle() instanceof GoRoll) {
 			p = targetPlayer;
 		}
 		return p;
 	}
 
 	@Override
 	public Player playThisCard(Player activePlayer, Player targetedPlayer) {
 		super.playThisCard(activePlayer, targetedPlayer);
 		targetedPlayer.addToBattle(this);
 		return null;
 	}
 
 }
