 package com.evervoid.state;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import com.evervoid.client.graphics.geometry.MathUtils;
 import com.evervoid.json.Json;
 import com.evervoid.json.Jsonable;
 import com.evervoid.state.geometry.Dimension;
 import com.evervoid.state.geometry.Point3D;
 import com.evervoid.state.prop.Portal;
 
 /**
  * This class represents a physical galaxy consisting of Solar Systems, Wormholes, etc.
  */
 public class Galaxy implements Jsonable
 {
 	private final ArrayList<Point3D> aPlanarSSRepresentation = new ArrayList<Point3D>();
 	private int aSize = 0;
 	private final Map<Integer, SolarSystem> aSolarSystems = new HashMap<Integer, SolarSystem>();
 	private final EVGameState aState;
 	/**
 	 * Temporary solar system; remove!
 	 */
 	private SolarSystem aTempSolarSystem = null;
 	private final Map<Integer, Wormhole> aWormholes = new HashMap<Integer, Wormhole>();
 	private final int ssPadding = 50;
 
 	protected Galaxy(final EVGameState state)
 	{
 		aState = state;
 	}
 
 	protected Galaxy(final Json j, final EVGameState state)
 	{
 		this(state);
 		state.aGalaxy = this;
 		// Step 1: Create wormholes
 		for (final Json wormhole : j.getListAttribute("wormholes")) {
 			final Wormhole tempWormhole = new Wormhole(wormhole, state);
 			addWormhole(tempWormhole);
 		}
 		// Step 2: Create empty solar systems
 		final Json solarsystems = j.getAttribute("solarsystems");
 		for (final String ss : solarsystems.getAttributes()) {
 			final SolarSystem tempSystem = new SolarSystem(solarsystems.getAttribute(ss), state);
 			addSolarSystem(tempSystem);
 		}
 		// Step 3: Populate solar systems
 		for (final SolarSystem ss : aSolarSystems.values()) {
 			ss.populate(solarsystems.getAttribute(String.valueOf(ss.getID())));
 		}
 	}
 
 	private void addPortals(final Wormhole wormhole, final SolarSystem ss1, final SolarSystem ss2, final EVGameState state)
 	{
 		if (ss1.equals(ss2)) {
 			return;
 		}
 		if (addWormhole(wormhole)) {
 			// Only create Portals if the wormhole is valid!
 			final Portal portal1 = new Portal(wormhole.getPortalID1(), state.getNullPlayer(), ss1.getWormholeLocation(), ss1,
 					wormhole);
 			final Portal portal2 = new Portal(wormhole.getPortalID2(), state.getNullPlayer(), ss2.getWormholeLocation(), ss2,
 					wormhole);
 			aState.addProp(portal1, portal1.getContainer());
 			aState.addProp(portal2, portal2.getContainer());
 		}
 	}
 
 	/**
 	 * Add a solar system to the galaxy
 	 * 
 	 * @param pSolar
 	 *            Solar system to add.
 	 */
 	private void addSolarSystem(final SolarSystem pSolar)
 	{
 		if (aTempSolarSystem == null) {
 			aTempSolarSystem = pSolar;
 		}
 		aSolarSystems.put(pSolar.getID(), pSolar);
 		// if new solar system is out of bounds, resize
 		aSize = Math.max(aSize, pSolar.getRadius() + (int) pSolar.getPoint3D().getDistanceToOrigin());
 	}
 
 	/**
 	 * Adds an empty (no ships) wormhole going from a solar system to another. If the two specified solar systems were already
 	 * connected by a wormhole, the existing wormhole is not overwritten (nothing happens)
 	 * 
 	 * @param wormhole
 	 *            The wormhole to add
 	 */
 	private boolean addWormhole(final Wormhole wormhole)
 	{
 		if (wormhole.isRecursive() || aWormholes.containsValue(wormhole)) {
 			return false;
 		}
 		else {
 			aWormholes.put(wormhole.getID(), wormhole);
 			return true;
 		}
 	}
 
 	/**
 	 * @param ss1
 	 *            One of the two solar systems
 	 * @param ss2
 	 *            One of the two solar systems
 	 * @return The wormhole connecting the specified solar systems, or null if there is no connection
 	 */
 	public Wormhole getConnection(final SolarSystem ss1, final SolarSystem ss2)
 	{
 		if (ss1.equals(ss2)) {
 			// Can't connect a solar system to itself
 			return null;
 		}
 		for (final Wormhole w : aWormholes.values()) {
 			if (w.connects(ss1.getID(), ss2.getID())) {
 				return w;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @return A new, unused solar system ID
 	 */
 	public int getNextSolarID()
 	{
 		// If we have no solar system, then ID 0 is not taken
 		if (aSolarSystems.isEmpty()) {
 			return 0;
 		}
 		// If we have solar systems, iterate over them, get the max, and return max+1
 		// because that ID is certainly not taken
 		int maxId = Integer.MIN_VALUE;
 		for (final Integer id : aSolarSystems.keySet()) {
 			maxId = Math.max(maxId, id);
 		}
 		return maxId + 1;
 	}
 
 	public int getNextWormholeID()
 	{
 		// TODO Auto-generated method stub
 		int maxId = -1;
 		for (final Integer id : aWormholes.keySet()) {
 			maxId = Math.max(maxId, id);
 		}
 		return maxId + 1;
 	}
 
 	/**
 	 * Finds a random, non-overlapping Point3D in space
 	 * 
 	 * @param radius
 	 *            The radius of the sphere occupying the Point3D
 	 * @return A Point3D at which a sphere of the given radius would not overlap with anything
 	 */
 	private Point3D getRandomSolarPoint(final int radius)
 	{
 		// Might want to change the min/max values here
 		Point3D point = null;
 		while (point == null || isOccupied(point, radius)) {
 			point = new Point3D(MathUtils.getRandomIntBetween(-500, 500), MathUtils.getRandomIntBetween(-500, 500),
 					MathUtils.getRandomIntBetween(-400, 400));
 		}
 		return point;
 	}
 
 	/**
 	 * @return The size of the galaxy.
 	 */
 	public int getSize()
 	{
 		return aSize;
 	}
 
 	/**
 	 * @return The set of all Point3D of each solar system
 	 */
 	public Set<Point3D> getSolarPoints()
 	{
 		final Set<Point3D> points = new HashSet<Point3D>();
 		for (final SolarSystem ss : aSolarSystems.values()) {
 			points.add(ss.getPoint3D());
 		}
 		return points;
 	}
 
 	/**
 	 * @param id
 	 *            The ID of the solar system
 	 * @return The solar system of the specified ID.
 	 */
 	public SolarSystem getSolarSystem(final int id)
 	{
 		return aSolarSystems.get(id);
 	}
 
 	/**
 	 * @param point
 	 * @return
 	 */
 	public SolarSystem getSolarSystemByPoint3D(final Point3D point)
 	{
 		for (final SolarSystem ss : aSolarSystems.values()) {
 			if (ss.getPoint3D().sameAs(point)) {
 				return ss;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param ss1
 	 *            First solar system
 	 * @param ss2
 	 *            Second solar system
 	 * @return Spatial distance between the two specified solar systems
 	 */
 	public float getSolarSystemDistance(final SolarSystem ss1, final SolarSystem ss2)
 	{
 		if (aSolarSystems.containsValue(ss1) && aSolarSystems.containsValue(ss2)) {
 			return ss1.getPoint3D().distanceTo(ss2.getPoint3D());
 		}
 		return 0;
 	}
 
 	/**
 	 * @return All solar systems
 	 */
 	public Collection<SolarSystem> getSolarSystems()
 	{
 		return aSolarSystems.values();
 	}
 
 	SolarSystem getTempSolarSystem()
 	{
 		return aTempSolarSystem;
 	}
 
 	public Wormhole getWormhole(final int id)
 	{
 		return aWormholes.get(id);
 	}
 
 	/**
 	 * @return The set of wormholes in this galaxy
 	 */
 	public Iterable<Wormhole> getWormholes()
 	{
 		return aWormholes.values();
 	}
 
 	/**
 	 * Finds if a hypothetical sphere at the given Point3D with the given radius would collide with the existing solar systems
 	 * 
 	 * @param point
 	 *            The center of the sphere
 	 * @param radius
 	 *            The radius of the sphere
 	 * @return Whether the given sphere would collide or not
 	 */
 	public boolean isOccupied(final Point3D point, final int radius)
 	{
 		final Point3D origin = new Point3D(point.x, point.y, 0);
 		Point3D currentPoint = null;
 		for (final SolarSystem ss : aSolarSystems.values()) {
 			currentPoint = new Point3D(ss.getPoint3D().x, ss.getPoint3D().y, 0);
 			if (currentPoint.distanceTo(origin) <= radius + ss.getRadius() + ssPadding) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean isPathClear(final SolarSystem ss1, final SolarSystem ss2)
 	{
 		final Point3D ss1Loc = ss1.getPoint3D();
 		final Point3D ss2Loc = ss2.getPoint3D();
 		return false;
 	}
 
 	/**
 	 * Randomly adds solar systems and wormholes to this galaxy
 	 */
 	public void populateRandomly(final EVGameState state)
 	{
 		for (int i = 0; i < 6; i++) {
 			final int width = MathUtils.getRandomIntBetween(32, 128);
 			final int height = MathUtils.getRandomIntBetween(24, 72);
 			final Point3D origin = getRandomSolarPoint(Math.max(width, height));
 			final Dimension ssDimension = new Dimension(width, height);
 			final SolarSystem tSolar = new SolarSystem(getNextSolarID(), ssDimension, origin, state.getRandomStar(ssDimension),
 					state);
 			addSolarSystem(tSolar);
 		}
 		final SolarSystem previousSS = (SolarSystem) MathUtils.getRandomElement(aSolarSystems.values());
 		// This loop ensures graph connectivity (builds a path using every node).
 		for (final SolarSystem ss : aSolarSystems.values()) {
 			final Wormhole tempWormhole = new Wormhole(state.getNextPropID(), state.getNextPropID() + 1, previousSS
 					.getPoint3D().distanceTo(ss.getPoint3D()), state);
 			addPortals(tempWormhole, previousSS, ss, state);
 		}
 		for (int i = 0; i < 5; i++) {
 			final SolarSystem ss1 = (SolarSystem) MathUtils.getRandomElement(aSolarSystems.values());
 			final SolarSystem ss2 = (SolarSystem) MathUtils.getRandomElement(aSolarSystems.values());
 			if (ss1.equals(ss2)) {
 				i--;
 				continue;
 			}
 			final Wormhole tempWormhole = new Wormhole(state.getNextPropID(), state.getNextPropID() + 1, ss1.getPoint3D()
 					.distanceTo(ss2.getPoint3D()), state);
			addPortals(tempWormhole, ss1, ss1, state);
 		}
 		for (final SolarSystem ss : aSolarSystems.values()) {
 			ss.populateRandomly();
 		}
 	}
 
 	@Override
 	public Json toJson()
 	{
 		return new Json().setIntMapAttribute("solarsystems", aSolarSystems).setListAttribute("wormholes", aWormholes.values());
 	}
 }
