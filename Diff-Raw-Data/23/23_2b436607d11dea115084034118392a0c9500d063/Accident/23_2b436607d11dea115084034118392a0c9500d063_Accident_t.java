 package fr.lo02.model.card.HazardCards;
 
 import fr.lo02.model.Player;
 import fr.lo02.model.card.Card;
 import fr.lo02.model.card.SafetyCards.DrivingAce;
 import fr.lo02.model.card.remedyCards.GoRoll;
 
 public class Accident extends Card {
 
 	public Accident() {
 		this.setHazardCard(true);
 	}
 
 	public Player checkValidMove(Player activePlayer, Player targetPlayer) {
 		Player p = null;
 		// Si il possede pas la botte "Driving Ace" et que la derniere carte est
 		// type "RemedyCards"
		if (!(targetPlayer.isDrivingAce()) && targetPlayer.getLastCardFromBattle() instanceof GoRoll) {
 //		if (activePlayer.getLastCardFromBattle() instanceof GoRoll) {
 			p = targetPlayer;
 		}
 		return p;
 	}
 
 	@Override
 	public Player playThisCard(Player activePlayer, Player targetedPlayer) {
 		// TODO Auto-generated method stub
 		DrivingAce aDrivingAce = new DrivingAce();
 		super.playThisCard(activePlayer, targetedPlayer);
 		targetedPlayer.addToBattle(this);
 		if(targetedPlayer.getHand().contains(aDrivingAce)) { // NOK
 			targetedPlayer.coupFourre();
 			return targetedPlayer;
 		}
 		else
 			return null;
 	}
 
 }
