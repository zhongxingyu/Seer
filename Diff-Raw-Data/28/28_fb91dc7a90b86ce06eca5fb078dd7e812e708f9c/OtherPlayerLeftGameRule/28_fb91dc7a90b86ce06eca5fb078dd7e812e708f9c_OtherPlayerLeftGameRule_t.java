 package de.nordakademie.nakjava.server.internal.actionRules.uniqueModeActionRules;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.nordakademie.nakjava.gamelogic.stateMachineEvenNewer.states.State;
 import de.nordakademie.nakjava.server.internal.Player;
 import de.nordakademie.nakjava.server.internal.actionRules.NonSimulationStateRule;
 import de.nordakademie.nakjava.server.shared.proxy.actions.PlayAgainAction;
 import de.nordakademie.nakjava.server.shared.serial.ActionContext;
 
 public class OtherPlayerLeftGameRule extends NonSimulationStateRule {
 
 	@Override
 	public List<ActionContext> applyRule(long sessionId, Player player) {
 		List<ActionContext> actions = new ArrayList<>();
 		actions.add(new PlayAgainAction(sessionId));
 		return actions;
 	}
 
 	@Override
 	public State getState() {
 		return State.OTHERPLAYERLEFTGAME;
 	}
 
 }
