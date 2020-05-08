 package de.nordakademie.nakjava.server.internal.actionRules.uniqueModeActionRules;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.nordakademie.nakjava.gamelogic.stateMachineEvenNewer.states.State;
 import de.nordakademie.nakjava.server.internal.Player;
 import de.nordakademie.nakjava.server.internal.actionRules.Alphabet;
 import de.nordakademie.nakjava.server.internal.actionRules.NonSimulationStateRule;
 import de.nordakademie.nakjava.server.internal.model.LoginSpecificModel;
 import de.nordakademie.nakjava.server.shared.proxy.actions.login.TypePlayerNameAction;
 import de.nordakademie.nakjava.server.shared.serial.ActionContext;
 
 public class TypeSpecialCharactersForPlayerNameRule extends
 		NonSimulationStateRule {
 
 	@Override
 	public List<ActionContext> applyRule(long sessionId, Player player) {
 		List<ActionContext> actions = new ArrayList<>();
 		for (char specialCharacter : Alphabet.getNotAtTheBeginningLetters()) {
 			actions.add(new TypePlayerNameAction(specialCharacter,
 					getBatch(sessionId), sessionId));
 		}
 		return actions;
 	}
 
 	@Override
 	public State getState() {
 		return State.LOGIN;
 	}
 
 	@Override
 	protected boolean isRuleApplicableImpl(long sessionId, Player player) {
		return !((LoginSpecificModel) getStateSpecificModel(sessionId, player))
				.getCurrentPartOfName().equals("");
 	}
 
 }
