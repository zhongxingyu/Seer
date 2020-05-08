 package com.evervoid.state.action;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.evervoid.json.Json;
 import com.evervoid.json.Jsonable;
 import com.evervoid.state.EVGameState;
 import com.evervoid.state.action.planet.ConstructShip;
import com.evervoid.state.action.player.ReceiveIncome;
 import com.evervoid.state.action.ship.JumpShipIntoPortal;
 import com.evervoid.state.action.ship.MoveShip;
 import com.evervoid.state.action.ship.ShootShip;
 
 /**
  * Represents a game turn. Holds a list of actions.
  */
 public class Turn implements Jsonable
 {
 	/**
 	 * The list of actions played during this turn
 	 */
 	private final List<Action> aActions = new ArrayList<Action>();
 
 	/**
 	 * Constructor; does nothing right now...
 	 */
 	public Turn()
 	{
 		// Nothing
 	}
 
 	/**
 	 * JSON constructor; rebuilds a Turn instance from a Json object
 	 * 
 	 * @param j
 	 *            The Json representation of a Turn
 	 */
 	public Turn(final Json j, final EVGameState state)
 	{
 		try {
 			for (final Json action : j.getListAttribute("turns")) {
 				final String type = action.getStringAttribute("actiontype");
 				if (type.equals("MoveShip")) {
 					aActions.add(new MoveShip(action, state));
 				}
 				else if (type.equals("JumpShip")) {
 					aActions.add(new JumpShipIntoPortal(action, state));
 				}
 				else if (type.equals("ConstructShip")) {
 					aActions.add(new ConstructShip(action, state));
 				}
 				else if (type.equals("ShootShip")) {
 					aActions.add(new ShootShip(action, state));
 				}
				else if (type.equals("ReceiveIncome")) {
					aActions.add(new ReceiveIncome(action, state));
				}
 			}
 		}
 		catch (final IllegalEVActionException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Adds an action to the list of actions during this turn
 	 * 
 	 * @param action
 	 *            The action to add
 	 */
 	public void addAction(final Action action)
 	{
 		aActions.add(action);
 	}
 
 	public void addAllActions(final List<Action> list)
 	{
 		for (final Action a : list) {
 			addAction(a);
 		}
 	}
 
 	public void delAction(final Action action)
 	{
 		aActions.remove(action);
 	}
 
 	public List<Action> getActions()
 	{
 		return aActions;
 	}
 
 	public List<Action> getActionsOfType(final String... types)
 	{
 		final List<Action> actions = new ArrayList<Action>(aActions.size() / 2 + 1);
 		for (final Action act : aActions) {
 			for (final String type : types) {
 				if (type.equals(act.getActionType())) {
 					actions.add(act);
 				}
 			}
 		}
 		return actions;
 	}
 
 	@Override
 	public Json toJson()
 	{
 		final Json j = new Json();
 		j.setListAttribute("turns", aActions);
 		return j;
 	}
 
 	@Override
 	public String toString()
 	{
 		String s = "";
 		final String sep = "\n~ THEN ~\n";
 		for (final Action a : aActions) {
 			s += a + sep;
 		}
 		return s.substring(0, Math.max(0, s.length() - sep.length()));
 	}
 }
