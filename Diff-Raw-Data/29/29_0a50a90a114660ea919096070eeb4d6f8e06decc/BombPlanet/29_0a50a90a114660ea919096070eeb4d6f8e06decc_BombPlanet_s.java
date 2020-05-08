 package com.evervoid.state.action.ship;
 
 import com.evervoid.json.Json;
 import com.evervoid.state.EVGameState;
 import com.evervoid.state.action.IllegalEVActionException;
 import com.evervoid.state.player.Player;
 import com.evervoid.state.prop.Planet;
 import com.evervoid.state.prop.Prop;
 import com.evervoid.state.prop.Ship;
 
 public class BombPlanet extends ShipAction
 {
 	// FIXME - not 5
	private int aDamage = 5;
 	private final Planet aTargetPlanet;
 
 	public BombPlanet(final Json j, final EVGameState state) throws IllegalEVActionException
 	{
 		super(j, state);
 		aTargetPlanet = (Planet) state.getPropFromID(j.getIntAttribute("bombingTarget"));
 		aDamage = j.getIntAttribute("damage");
 	}
 
 	public BombPlanet(final Planet planet, final Ship ship) throws IllegalEVActionException
 	{
 		super(ship);
 		aTargetPlanet = planet;
 	}
 
 	@Override
 	protected void executeAction()
 	{
 		aTargetPlanet.takeDamange(aDamage);
 	}
 
 	public int getDamage()
 	{
 		return aDamage;
 	}
 
 	@Override
 	public String getDescription()
 	{
 		return "Bombing a planet";
 	}
 
 	public Prop getTarget()
 	{
 		return aTargetPlanet;
 	}
 
 	@Override
 	protected boolean isValidShipAction()
 	{
 		// 1. Planet is owned by enemy
 		// 2. Planet is not owned by nullplayer
 		// 3. is in the correct solar system
 		// 4. is within reach
 		final Player owner = aTargetPlanet.getPlayer();
 		if (owner.isNullPlayer() || owner.equals(getSender())) {
 			return false;
 		}
 		return getShip().getContainer().equals(aTargetPlanet.getContainer()) && getShip().canShoot(aTargetPlanet);
 	}
 
 	public void rollDamage()
 	{
 		// TODO: Do it
 	}
 
 	@Override
 	public Json toJson()
 	{
 		final Json j = super.toJson();
 		j.setIntAttribute("bombingTarget", aTargetPlanet.getID());
 		j.setIntAttribute("damage", aDamage);
 		return j;
 	}
 }
