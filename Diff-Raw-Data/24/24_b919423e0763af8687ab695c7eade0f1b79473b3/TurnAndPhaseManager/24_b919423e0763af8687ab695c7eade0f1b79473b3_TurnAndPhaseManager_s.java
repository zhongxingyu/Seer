 package se.chalmers.dat255.risk.model;
 
 import java.util.List;
 
 public class TurnAndPhaseManager {
 	private int activePlayer;
 	private Phase currentPhase;
 
 	public TurnAndPhaseManager() {
 		currentPhase = Phase.FBuild;
 		activePlayer = 0;
 	}
 
 	public static enum Phase {
 		FBuild("Deployment"), F1("Rienforcement"), F2("Attack"), F3("Movment");
 		private String phase;
 		Phase(String p){
 			phase = p;
 		}
 		public String getPhase(){
 			return phase;
 		}
 	}
 
 	public Phase getPhase() {
 		return currentPhase;
 	}
 
 	/*
 	 * Changing phase and then pokes on other methods.
 	 * 
 	 * Return is ComputeBonusForF0 if if a new bonus shall be computed in F0. 
 	 * Return is ComputeBonusForF1 is 0 if a new turn has begun (and not in F0).
 	 * Return is ChangedPhase if a change of phase has taken place.  
 	 * Return is DoNothing if the phase didn't change. (Currently not in use here. Is instead controlled in EventHandler)
 	 */
 
 	public ResultType changePhase(Player currentPlayer, List<Player> players) {
 		if (currentPhase == Phase.FBuild) {
 			if (currentPlayer == players.get(players.size() - 1)) { //Sista spelaren i F0
 				changeTurn(players);
 				currentPhase = Phase.F1;
 				return ResultType.ComputeBonusForF1; // Special, no need to compute troops
 			} else {
 				changeTurn(players);
 				return ResultType.ComputeBonusForF0;
 			}
 		} else if (currentPhase == Phase.F3) {
 			changeTurn(players);
 			currentPhase = Phase.F1;
 			return ResultType.ComputeBonusForF1;
 		} else if (currentPhase == Phase.F1) {
 			currentPhase = Phase.F2;
 		} else {
 			currentPhase = Phase.F3;
 		}
 		return ResultType.ChangedPhase;
 	}
 
 	public static enum ResultType {ComputeBonusForF0, ComputeBonusForF1, ChangedPhase, DoNothing}
 
 	private void changeTurn(List<Player> players) {
 		activePlayer = (activePlayer + 1) % players.size();
 		System.out.println("Changed Turn, new player is: " + activePlayer);
 	}
 
 	public int getActivePlayer() {
 		return activePlayer;
 	}
 	
 	
 	public void removePlayer(int pos){
 		if(pos < activePlayer){
 			activePlayer -= 1;
 		}
 	}
 
 	/**
 	 * called when a player gives up
 	 * 
 	 * @param players
 	 *            the list of players left in the game
 	 */
 	public boolean surrender(List<Player> players) {
 		if (players.size() != 1) {
 			if (currentPhase == Phase.FBuild) {
 				if (activePlayer == players.size()) {
 					changeTurn(players);
 					currentPhase = Phase.F1;
 					return true;
 				}
				return false;
 			} else {
 				currentPhase = Phase.F1;
				return false;
 			}
 		} else {
 			activePlayer = 0;//safety messure as to not crash the game
 			currentPhase = Phase.F1;
 			return false;
 		}
 	}
 }
