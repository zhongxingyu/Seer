 package com.evervoid.state.action.ship;
 
 import com.evervoid.json.Json;
 import com.evervoid.state.EVGameState;
 import com.evervoid.state.action.IllegalEVActionException;
 import com.evervoid.state.action.PropAction;
 import com.evervoid.state.prop.Ship;
 
 public abstract class ShipAction extends PropAction
 {
 	public ShipAction(final Json j, final EVGameState state) throws IllegalEVActionException
 	{
 		super(j, state);
 		if (!(getProp() instanceof Ship)) {
 			throw new IllegalEVActionException("Prop is not a ship");
 		}
 	}
 
 	public ShipAction(final Ship ship, final EVGameState state) throws IllegalEVActionException
 	{
 		super(ship.getPlayer(), ship, state);
 	}
 
 	public Ship getShip()
 	{
 		return (Ship) getProp();
 	}
 
 	/**
 	 * Check if this ShipAction is valid. Calls the template method isValidShipAction iff ship is valid in the first place.
 	 * Subclasses should only override isValidShipAction, hence the "final" keyword on this method.
 	 */
 	@Override
	protected final boolean isValidPropAction()
 	{
 		return getProp() instanceof Ship && !getShip().isDead() && isValidShipAction();
 	}
 
 	protected abstract boolean isValidShipAction();
 }
