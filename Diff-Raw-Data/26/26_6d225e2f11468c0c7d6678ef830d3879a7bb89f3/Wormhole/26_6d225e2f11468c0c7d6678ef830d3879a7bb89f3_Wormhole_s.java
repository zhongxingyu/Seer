 package com.evervoid.state;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.evervoid.client.graphics.geometry.MathUtils;
 import com.evervoid.json.Json;
 import com.evervoid.json.Jsonable;
 import com.evervoid.state.observers.WormholeObserver;
 import com.evervoid.state.prop.Portal;
 import com.evervoid.state.prop.Prop;
 import com.evervoid.state.prop.Ship;
 
 public class Wormhole implements EVContainer<Prop>, Jsonable, Comparable<Wormhole>
 {
 	/**
 	 * The number of turns to cross this wormhole will be computed by (distance between solar systems) * this multiplier
 	 */
 	private static final double sDistanceToTurnMultiplier = 0.0005;
 	/**
 	 * The maximum number of turns any wormhole takes to cross
 	 */
 	private static final int sMaximumTurns = 8;
 	/**
 	 * The minimum number of turns any wormhole takes to cross
 	 */
 	private static final int sMinimumTurns = 1;
 	private final int aID;
 	private final Set<WormholeObserver> aObserverSet;
 	/**
 	 * A Map for a Ship to it's progress along the wormhole
 	 */
 	private final Map<Ship, Integer> aShipSet = new HashMap<Ship, Integer>();
 	private final SolarSystem aSolarSystem1;
 	private final SolarSystem aSolarSystem2;
 	private final EVGameState aState;
 	/**
 	 * The number of turns it takes to cross this wormhole
 	 */
 	private final int aTurns;
 
 	Wormhole(final Json j, final EVGameState state)
 	{
 		aState = state;
 		aSolarSystem1 = aState.getSolarSystem(j.getIntAttribute("ss1"));
 		aSolarSystem2 = aState.getSolarSystem(j.getIntAttribute("ss2"));
 		aTurns = j.getIntAttribute("turns");
 		aID = j.getIntAttribute("id");
 		for (final Json wormship : j.getListAttribute("ships")) {
 			aShipSet.put(new Ship(wormship.getAttribute("ship"), state.getPlayerByName(j.getStringAttribute("player")), state),
 					wormship.getIntAttribute("progress"));
 		}
 		aObserverSet = new HashSet<WormholeObserver>();
 	}
 
 	Wormhole(final SolarSystem ss1, final SolarSystem ss2, final float length, final EVGameState state)
 	{
 		aState = state;
 		aID = state.getNextWormholeID();
 		aSolarSystem1 = ss1;
 		aSolarSystem2 = ss2;
 		aTurns = MathUtils.clampInt(sMinimumTurns, (int) (length * sDistanceToTurnMultiplier), sMaximumTurns);
 		aObserverSet = new HashSet<WormholeObserver>();
 	}
 
 	@Override
 	public boolean addElem(final Prop p)
 	{
 		if (!(p instanceof Ship)) {
 			return false;
 		}
 		final Ship s = (Ship) p;
 		if (aShipSet.containsKey(s)) {
 			return false;
 		}
 		aShipSet.put(s, 0);
 		return true;
 	}
 
 	@Override
 	public int compareTo(final Wormhole other)
 	{
 		return 1000 * (getSolarSystem1().getID() - other.getSolarSystem1().getID())
 				+ (getSolarSystem2().getID() - other.getSolarSystem2().getID());
 	}
 
 	/**
 	 * @param ss1
 	 *            One of the two solar systems
 	 * @param ss2
 	 *            One of the two solar systems
 	 * @return Whether this wormhole connects the specified solar systems or not
 	 */
 	public boolean connects(final SolarSystem ss1, final SolarSystem ss2)
 	{
 		return (getSolarSystem1().equals(ss1) && getSolarSystem2().equals(ss2))
 				|| (getSolarSystem1().equals(ss2) && getSolarSystem2().equals(ss1));
 	}
 
 	@Override
 	public boolean containsElem(final Prop p)
 	{
 		if (!(p instanceof Ship)) {
 			return false;
 		}
 		final Ship s = (Ship) p;
 		return aShipSet.containsKey(s);
 	}
 
 	public void deregisterObserver(final WormholeObserver wObserver)
 	{
 		aObserverSet.remove(wObserver);
 	}
 
 	@Override
	public Iterable<Prop> elemIterator()
 	{
		// TODO Auto-generated method stub
		return null;
		// aShipSet.keySet();
 	}
 
 	@Override
 	public boolean equals(final Object other)
 	{
 		if (other == null || !(other instanceof Wormhole)) {
 			return false;
 		}
 		final Wormhole o = (Wormhole) other;
 		return o.connects(getSolarSystem1(), getSolarSystem2());
 	}
 
 	@Override
 	public int getID()
 	{
 		return aID;
 	}
 
 	public Portal getPortal1()
 	{
 		return aSolarSystem1.getPortalTo(aSolarSystem2);
 	}
 
 	public Portal getPortal2()
 	{
 		return aSolarSystem2.getPortalTo(aSolarSystem2);
 	}
 
 	public SolarSystem getSolarSystem1()
 	{
 		return aSolarSystem1;
 	}
 
 	public SolarSystem getSolarSystem2()
 	{
 		return aSolarSystem2;
 	}
 
 	public void moveShip(final Ship s)
 	{
 		aShipSet.put(s, aShipSet.get(s) + 1);
 	}
 
 	public void registerObserver(final WormholeObserver wObserver)
 	{
 		aObserverSet.add(wObserver);
 	}
 
 	@Override
 	public boolean removeElem(final Prop p)
 	{
 		if (!(p instanceof Ship)) {
 			return false;
 		}
 		final Ship s = (Ship) p;
 		if (aShipSet.containsValue(s)) {
 			aShipSet.remove(s);
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 
 	@Override
 	public Json toJson()
 	{
 		final List<Json> ships = new ArrayList<Json>(aShipSet.size());
 		for (final Ship s : aShipSet.keySet()) {
 			ships.add(new Json().setIntAttribute("progress", aShipSet.get(s)).setAttribute("ship", s));
 		}
 		final Json j = new Json();
 		j.setIntAttribute("ss1", aSolarSystem1.getID());
 		j.setIntAttribute("ss2", aSolarSystem2.getID());
 		j.setIntAttribute("turns", aTurns);
 		j.setListAttribute("ships", ships);
 		j.setIntAttribute("id", aID);
 		return j;
 	}
 }
