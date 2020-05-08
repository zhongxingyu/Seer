 package se.chalmers.dat255.risk.model;
 
 import se.chalmers.dat255.risk.model.TurnAndPhaseManager;
 import se.chalmers.dat255.risk.model.TurnAndPhaseManager.Phase;
 
 public class ClickHandler {
 	TurnAndPhaseManager phaseHandler;
 	
 	IProvince oldClickedProvince = null;
 	boolean movedTroops = false;
 	boolean firstProvinceConqueredThisTurn = true;
 	
 	public ClickHandler(){
 		phaseHandler=new TurnAndPhaseManager();
 	}
 	
 	
 	/*
 	 * Tells PhaseHandler to changePhase.
 	 * 
 	 * Return is 2 if if a new bonus shall be computed. == 0 for F0
 	 * Return is 1 if a change of phase has taken place.
 	 * Return is 0 if a new turn has begun.
 	 * Return is -1 if phase didn't change.
 	 */
 	public int handlePhaseClick(Player currentPlayer, int bonusUnitsLeft, Player[] players) {
 		// TODO Auto-generated method stub
 		// Ska kolla så att spelaren är klar med alla sina
 		Phase currentPhase = phaseHandler.getPhase();
 		
 		if (currentPhase == Phase.FBuild || currentPhase == Phase.F1) {
 			// CHECKS IF I'M ALLOWED TO PRESS CHANGE PHASE
 			if (bonusUnitsLeft == 0 && currentPlayer.getCards().size() < 5) {
 				return phaseHandler.changePhase(currentPlayer, players);
 			}
 		}
 		int result = phaseHandler.changePhase(currentPlayer, players);
		resetVariables();
 		return result;
 			// "actions" och kan byta fas.
 		
 		// När du är i FBuild, så måste du kolla så att det är tomt
 		// i bonus innan du "byter fas" = kör changePhase.
 
 	}

 	private void resetVariables(){
 		oldClickedProvince = null;
 		movedTroops = false;
 		firstProvinceConqueredThisTurn = true;
 	}
 	
 }
