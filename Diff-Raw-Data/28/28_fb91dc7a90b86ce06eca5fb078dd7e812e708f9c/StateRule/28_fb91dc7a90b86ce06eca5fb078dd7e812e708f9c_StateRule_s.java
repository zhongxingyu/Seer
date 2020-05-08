 package de.nordakademie.nakjava.server.internal.actionRules;
 
 import de.nordakademie.nakjava.gamelogic.shared.playerstate.PlayerState;
 import de.nordakademie.nakjava.gamelogic.stateMachineEvenNewer.states.State;
 import de.nordakademie.nakjava.server.internal.ActionRule;
 import de.nordakademie.nakjava.server.internal.Player;
 import de.nordakademie.nakjava.server.internal.Session;
 import de.nordakademie.nakjava.server.internal.Sessions;
 import de.nordakademie.nakjava.server.internal.model.StateSpecificModel;
 
 public abstract class StateRule implements ActionRule {
 
 	@Override
 	public boolean isRuleApplicable(long sessionId, Player player) {
 		Session session = Sessions.getInstance().getSession(sessionId);
 		return session.getPlayerStateForPlayer(player).getState() == getState()
 				&& isRuleApplicableImpl(sessionId, player);
 	}
 
 	protected boolean isRuleApplicableImpl(long sessionId, Player player) {
 		return true;
 	}
 
 	public abstract State getState();
 
 	public StateSpecificModel getStateSpecificModel(long sessionId,
 			Player player) {
 		return getPlayerState(sessionId, player).getStateSpecificModel();
 	}
 
 	public PlayerState getPlayerState(long sessionId, Player player) {
 		return getSession(sessionId).getPlayerStateForPlayer(player);
 	}
 
 	public Session getSession(long sessionId) {
 		return Sessions.getInstance().getSession(sessionId);
 	}
 
 }
