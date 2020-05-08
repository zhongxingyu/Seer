 package cards;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import cards.activators.CardParams;
 import cards.activators.SicariusParams;
 
 import roma.Game;
 import roma.GameVisor;
 import enums.*;
 
 public class CardSicarius extends Card {
 
 	public CardNames getID() {
 		return CardNames.Sicarius;
 	}
 
 	public String getName() {
 		return "Sicarius";
 	}
 	
 	public int getCostToPlay() {
 		return 9;
 	}
 
 	public int getDiceToActivate() {
 		return 1;
 	}
 
 	public boolean isBuilding() {
 		return false;
 	}
 
 
 	public String getDescription() {
 		return "Eliminates an opposing, face-up character card. " +
 				"The opposing card and the Sicarius are both discarded";
 	}
 
 	public int getDefense() {
 		return 2;
 	}
 
 	@Override
 	public CardParams getParams() {
 		return new SicariusParams();
 	}
 
 	@Override
 	public boolean performEffect(GameVisor g, int pos, CardParams a) {
 		boolean performed = false;
 		SicariusParams myParams = (SicariusParams) a;
 		int enemyPos = (g.whoseTurn() + 1) % Game.MAX_PLAYERS;
 		Card targetCard = g.getField().getCard(enemyPos, myParams.getTargetPos());
 		
 		// Sicarius attacks the enemy for 9999 (ties in the Kat)
		if (targetCard != null && !targetCard.isBuilding() && targetCard.onAttacked(g, this, myParams.getTargetPos(), 9999)) {
 			performed = true;
 			g.getField().setCard(g.whoseTurn(), pos-1, null);
 			g.discard(this);			
 			
 		} else {
 			
 		}
 		
 		return performed;
 	}
 
 }
