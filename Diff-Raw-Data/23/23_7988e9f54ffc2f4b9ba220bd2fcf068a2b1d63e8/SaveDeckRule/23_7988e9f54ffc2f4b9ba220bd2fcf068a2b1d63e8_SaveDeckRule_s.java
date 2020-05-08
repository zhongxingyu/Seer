 package de.nordakademie.nakjava.server.internal.actionRules.uniqueModeActionRules;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.nordakademie.nakjava.gamelogic.stateMachineEvenNewer.states.State;
 import de.nordakademie.nakjava.server.internal.Player;
 import de.nordakademie.nakjava.server.internal.actionRules.NonSimulationStateRule;
 import de.nordakademie.nakjava.server.internal.model.EditDeckSpecificModel;
 import de.nordakademie.nakjava.server.shared.proxy.actions.editdeck.SaveDeckAction;
 import de.nordakademie.nakjava.server.shared.serial.ActionContext;
 
 public class SaveDeckRule extends NonSimulationStateRule {
 
 	@Override
 	public List<ActionContext> applyRule(long sessionId, Player player) {
 		List<ActionContext> actions = new ArrayList<>();
 		actions.add(new SaveDeckAction(getBatch(sessionId), sessionId));
 		return actions;
 	}
 
 	@Override
 	protected boolean isRuleApplicableImpl(long sessionId, Player player) {
 		String currentPartOfDeckName = ((EditDeckSpecificModel) getStateSpecificModel(
 				sessionId, player)).getCurrentPartOfDeckName();
		return currentPartOfDeckName != ""
				&& currentPartOfDeckName != "StandardDeck";
 	}
 
 	@Override
 	public State getState() {
 		return State.EDITDECK;
 	}
 
 }
