 package com.evervoid.state;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.evervoid.state.player.Player;
 
 public class EverVoidGameState
 {
 	public static Galaxy createRandomGalaxy()
 	{
 		final Map<Point3D, SolarSystem> tempMap = new HashMap<Point3D, SolarSystem>();
 		final Galaxy tempGalaxy = new Galaxy(tempMap);
 		return tempGalaxy;
 	}
 
 	private final Galaxy fGalaxy;
 	private final List<Player> fPlayerList;
 
 	/**
 	 * Default constructor, simply creates a brand new galaxy with solar systems and planets in.
 	 */
 	public EverVoidGameState()
 	{
 		fPlayerList = new ArrayList<Player>();
 		fPlayerList.add(new Player("EverVoidGame"));
 		fGalaxy = createRandomGalaxy();
 	}
 
 	public EverVoidGameState(final List<Player> playerList, final Galaxy galaxy)
 	{
 		fGalaxy = galaxy.copy();
 		// create a new ArrayList and copy all of playerList into it
 		fPlayerList = new ArrayList<Player>();
 		fPlayerList.addAll(playerList);
 	}
 
 	@Override
 	public EverVoidGameState clone()
 	{
 		// TODO actually clone
 		return this;
 	}
 
 	public SolarSystem getSolarSystem(final Point3D point)
 	{
 		if (point == null) {
 		}
 		// TODO make return correct solar system
 		return fGalaxy.getSolarSystem(point);
 	}
 }
