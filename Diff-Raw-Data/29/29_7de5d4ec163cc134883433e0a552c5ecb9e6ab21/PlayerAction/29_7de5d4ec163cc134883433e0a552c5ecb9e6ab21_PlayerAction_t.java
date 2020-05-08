 package com.evervoid.state.action.player;
 
 import com.evervoid.json.Json;
 import com.evervoid.state.EVGameState;
 import com.evervoid.state.action.Action;
 import com.evervoid.state.action.IllegalEVActionException;
 import com.evervoid.state.player.Player;
 
 /**
  * Represents an action executed to a player. Note: All actions have a Player (the creator of the Action); however, this does
  * not always correspond to the target of the Action, hence this class
  */
 public abstract class PlayerAction extends Action
 {
 	private final Player aTargetPlayer;
 
 	public PlayerAction(final Json j, final EVGameState state) throws IllegalEVActionException
 	{
 		super(j, state);
 		aTargetPlayer = aState.getPlayerByName(j.getStringAttribute("targetPlayer"));
 	}
 
 	public PlayerAction(final Player creator, final String actionType, final Player target, final EVGameState state)
 			throws IllegalEVActionException
 	{
 		super(creator, actionType, state);
 		aTargetPlayer = target;
 	}
 
 	public Player getTargetPlayer()
 	{
 		return aTargetPlayer;
 	}
 
 	/**
 	 * Check if this PlayerAction is valid. Calls the template method isValidPlayerAction iff target player is valid in the
 	 * first place. Subclasses should only override isValidPlayerAction, hence the "final" keyword on this method.
 	 */
 	@Override
 	protected final boolean isValidAction()
 	{
 		return aTargetPlayer != null && aState.getPlayerByName(aTargetPlayer.getName()) != null && isValidPlayerAction();
 	}
 
 	protected abstract boolean isValidPlayerAction();
 
 	@Override
 	public Json toJson()
 	{
		return super.toJson().setStringAttribute("targetPlayer", aTargetPlayer.getName());
 	}
 }
