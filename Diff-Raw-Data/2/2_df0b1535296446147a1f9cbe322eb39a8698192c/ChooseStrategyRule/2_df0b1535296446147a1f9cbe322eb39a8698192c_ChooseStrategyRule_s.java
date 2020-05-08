 package de.nordakademie.nakjava.server.internal.actionRules.uniqueModeActionRules;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.nordakademie.nakjava.gamelogic.stateMachineEvenNewer.states.State;
 import de.nordakademie.nakjava.gamelogic.stateMachineEvenNewer.winstrategies.WinStrategies;
 import de.nordakademie.nakjava.server.internal.Player;
 import de.nordakademie.nakjava.server.internal.actionRules.NonSimulationStateRule;
 import de.nordakademie.nakjava.server.shared.proxy.actions.settingupgame.SelectWinStrategy;
 import de.nordakademie.nakjava.server.shared.serial.ActionContext;
 
 public class ChooseStrategyRule extends NonSimulationStateRule {
 
 	@Override
 	protected boolean isRuleApplicableImpl(long sessionId, Player player) {
		return getSession(sessionId).getModel().getStrategy() != null;
 	}
 
 	@Override
 	public List<ActionContext> applyRule(long sessionId, Player player) {
 		List<ActionContext> actions = new ArrayList<>();
 		long batch = getBatch(sessionId);
 		for (String winstrategy : WinStrategies.getInstance().getStrategies()) {
 			actions.add(new SelectWinStrategy(winstrategy, batch, sessionId));
 		}
 		return actions;
 	}
 
 	@Override
 	public State getState() {
 		return State.CONFIGUREGAME;
 	}
 
 }
