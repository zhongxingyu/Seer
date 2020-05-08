 package com.evervoid.state.prop;
 
 import com.evervoid.json.Json;
 import com.evervoid.state.EVGameState;
 import com.evervoid.state.SolarSystem;
 import com.evervoid.state.geometry.Dimension;
 import com.evervoid.state.geometry.GridLocation;
 import com.evervoid.state.geometry.Point;
 import com.evervoid.state.player.Player;
 
 public class Portal extends Prop
 {
 	public static enum GridEdge
 	{
 		BOTTOM, LEFT, RIGHT, TOP;
 	}
 
 	private final SolarSystem aDestinationSS;
 	private final GridEdge aOrientation;
 
 	public Portal(final int id, final Player player, final GridLocation location, final SolarSystem local,
 			final SolarSystem dest)
 	{
 		super(id, player, location, "portal");
 		aContainer = local;
 		aDestinationSS = dest;
 		if (location.getY() == local.getHeight() - 1 && location.getHeight() == 1) {
 			aOrientation = GridEdge.TOP;
 		}
 		else if (location.getX() == local.getWidth() - 1 && location.getWidth() == 1) {
 			aOrientation = GridEdge.RIGHT;
 		}
 		else if (location.getY() == 0 && location.getHeight() == 1) {
 			aOrientation = GridEdge.BOTTOM;
 		}
 		else {
 			aOrientation = GridEdge.LEFT;
 		}
 	}
 
 	public Portal(final Json j, final EVGameState state)
 	{
 		super(j, state.getNullPlayer(), "portal", state);
 		aDestinationSS = state.getSolarSystem(j.getIntAttribute("dest"));
 		enterContainer(aContainer);
 		aOrientation = GridEdge.values()[j.getIntAttribute("orient")];
 	}
 
 	@Override
 	public SolarSystem getContainer()
 	{
 		return (SolarSystem) aContainer;
 	}
 
 	public SolarSystem getDestination()
 	{
 		return aDestinationSS;
 	}
 
 	public int getHeight()
 	{
 		return aLocation.getHeight();
 	}
 
 	public Point getJumpingLocation(final Dimension dim)
 	{
 		Point jumpingPoint = aLocation.origin;
 		switch (aOrientation) {
 			case TOP:
				jumpingPoint = jumpingPoint.add(0, -1);
				break;
 			case RIGHT:
				jumpingPoint = jumpingPoint.add(-1, 0);
				break;
 			case BOTTOM:
				jumpingPoint = jumpingPoint.add(0, 1);
				break;
 			case LEFT:
				jumpingPoint = jumpingPoint.add(1, 0);
 		}
 		return jumpingPoint;
 	}
 
 	@Override
 	public Json toJson()
 	{
 		final Json j = super.toJson();
 		j.setIntAttribute("dest", aDestinationSS.getID());
 		j.setIntAttribute("orient", aOrientation.ordinal());
 		return j;
 	}
 }
