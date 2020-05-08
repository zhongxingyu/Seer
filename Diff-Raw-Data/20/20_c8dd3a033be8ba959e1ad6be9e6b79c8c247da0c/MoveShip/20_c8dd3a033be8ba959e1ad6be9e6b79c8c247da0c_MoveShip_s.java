 package com.evervoid.state.action.ship;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.evervoid.json.Json;
 import com.evervoid.state.EVContainer;
 import com.evervoid.state.EVGameState;
 import com.evervoid.state.SolarSystem;
 import com.evervoid.state.geometry.GridLocation;
 import com.evervoid.state.geometry.Point;
 import com.evervoid.state.player.Player;
 import com.evervoid.state.prop.Pathfinder;
 import com.evervoid.state.prop.Prop;
 import com.evervoid.state.prop.Ship;
 
 public class MoveShip extends ShipAction
 {
 	private final GridLocation aDestination;
 	/**
 	 * null path means not computed yet
 	 */
 	private List<GridLocation> aPath = new ArrayList<GridLocation>();
 
 	public MoveShip(final Json j, final EVGameState state)
 	{
 		super(j, state);
 		for (final Json step : j.getListAttribute("path")) {
 			aPath.add(new GridLocation(step));
 		}
 		aDestination = new GridLocation(j.getAttribute("destination"));
 	}
 
 	public MoveShip(final Player player, final Ship ship, final Point destination)
 	{
 		super(player, "MoveShip", ship);
 		aDestination = new GridLocation(destination, ship.getData().getDimension());
 	}
 
 	private void computePath()
 	{
 		if (aPath == null || aPath.isEmpty()) {
 			aPath = new Pathfinder().findPath(aShip, aDestination);
 		}
 	}
 
 	@Override
 	public void execute()
 	{
 		aShip.move(aPath);
 	}
 
 	public List<GridLocation> getPath()
 	{
 		computePath();
 		return aPath;
 	}
 
 	@Override
 	public boolean isValid()
 	{
 		final EVContainer<Prop> container = aShip.getContainer();
 		if (!(container instanceof SolarSystem)) {
 			// Ship not in solar system
 			return false;
 		}
 		final boolean valid = aShip.getValidDestinations().contains(aDestination);
 		if (valid) {
 			// If it's valid, it's worth computing the path right now
 			computePath();
			System.out.println("valid move");
 		}
 		return valid;
 	}
 
 	@Override
 	public Json toJson()
 	{
 		return super.toJson().setListAttribute("path", aPath).setAttribute("destination", aDestination);
 	}
 }
