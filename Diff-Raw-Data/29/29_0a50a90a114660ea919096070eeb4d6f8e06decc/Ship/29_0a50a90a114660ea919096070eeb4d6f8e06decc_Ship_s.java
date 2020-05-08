 package com.evervoid.state.prop;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import com.evervoid.json.Json;
 import com.evervoid.state.Color;
 import com.evervoid.state.EVContainer;
 import com.evervoid.state.EVGameState;
 import com.evervoid.state.SolarSystem;
 import com.evervoid.state.data.ShipData;
 import com.evervoid.state.data.SpriteData;
 import com.evervoid.state.data.TrailData;
 import com.evervoid.state.data.WeaponData;
 import com.evervoid.state.geometry.GridLocation;
 import com.evervoid.state.geometry.Point;
 import com.evervoid.state.observers.ShipObserver;
 import com.evervoid.state.player.Player;
 import com.evervoid.state.player.ResourceAmount;
 import com.evervoid.utils.MathUtils;
 
 public class Ship extends Prop implements EVContainer<Prop>
 {
 	private final ShipData aData;
 	private int aHealth;
 	private final Set<ShipObserver> aObserverList;
 	private int aRadiation;
 	private int aShields;
 	private final Set<Ship> aShipCargo;
 
 	public Ship(final int id, final Player player, final EVContainer<Prop> container, final GridLocation location,
 			final String shipType, final EVGameState state)
 	{
 		super(id, player, location, "ship", state);
 		aData = aPlayer.getRaceData().getShipData(shipType);
 		// Overwrite GridLocation dimension with data from ship data
 		aLocation.dimension = aData.getDimension();
 		aObserverList = new HashSet<ShipObserver>();
 		aContainer = container;
 		aHealth = aData.getHealth(player.getResearch());
 		aShields = aData.getShields(player.getResearch());
 		aRadiation = aData.getRadiation(player.getResearch());
 		aShipCargo = new HashSet<Ship>();
 	}
 
 	public Ship(final Json j, final EVGameState state)
 	{
 		super(j, "ship", state);
 		aData = aPlayer.getRaceData().getShipData(j.getStringAttribute("shiptype"));
 		// Overwrite GridLocation dimension with data from ship data
 		aLocation.dimension = aData.getDimension();
 		aObserverList = new HashSet<ShipObserver>();
 		aHealth = j.getIntAttribute("health");
 		aShields = j.getIntAttribute("shields");
 		aRadiation = j.getIntAttribute("radiation");
 		aShipCargo = new HashSet<Ship>();
 		final List<Json> cargo = j.getListAttribute("shipCargo");
 		for (final Json ship : cargo) {
 			aShipCargo.add(new Ship(ship, aState));
 		}
 		aState.registerProp(this, aContainer);
 	}
 
 	@Override
 	public boolean addElem(final Prop e)
 	{
 		if (!(e instanceof Ship)) {
 			return false;
 		}
 		final Ship s = (Ship) e;
 		if (canHold(s) && s.getCurrentCargoSize() == 0) {
 			// check that this ship won't put us over capacity
 			// check that the ship isn't carrying anything; no recursion
 			return aShipCargo.add(s);
 		}
 		// ship didn't mean criteria
 		return false;
 	}
 
 	public void addHealth(final int amount)
 	{
 		aHealth = MathUtils.clampInt(0, aHealth + amount, getMaxHealth());
 		for (final ShipObserver observer : aObserverList) {
 			observer.shipHealthChanged(this, aHealth);
 		}
 		if (aHealth <= 0) { // ded; not beeg soorprize.
 			die();
 		}
 	}
 
 	public void addRadiation(final int amount)
 	{
 		aRadiation = MathUtils.clampInt(0, aRadiation + amount, getMaxRadiation());
 	}
 
 	public void addShields(final int amount)
 	{
 		aShields = MathUtils.clampInt(0, aShields + amount, getMaxShields());
 		for (final ShipObserver observer : aObserverList) {
 			observer.shipShieldsChanged(this, aShields);
 		}
 	}
 
 	public boolean canHold(final Ship ship)
 	{
 		return ship.getDockingSize() + getCurrentCargoSize() <= getCargoCapacity();
 	}
 
 	public boolean canJump()
 	{
 		return aRadiation >= aState.getJumpCost();
 	}
 
 	public boolean canShoot()
 	{
 		// If we have some kind of research which can make a ship shoot, this should be handled here
 		return aData.canShoot();
 	}
 
 	public boolean canShoot(final GridLocation location)
 	{
 		if (!canShoot()) {
 			return false;
 		}
 		// TODO check that the line isn't obstructed by something (like another prop)
 		return distanceTo(location) <= getSpeed();
 	}
 
 	public boolean canShoot(final Prop prop)
 	{
 		return canShoot(prop.getLocation());
 	}
 
 	public void capturePlanet(final Planet targetPlanet, final ShipPath underlyingMove)
 	{
 		targetPlanet.changeOwner(getPlayer());
 		for (final ShipObserver observer : aObserverList) {
 			observer.shipCapturedPlanet(this, targetPlanet, underlyingMove);
 		}
 	}
 
 	@Override
 	public boolean containsElem(final Prop e)
 	{
 		return aShipCargo.contains(e);
 	}
 
 	public void deregisterObserver(final ShipObserver observer)
 	{
 		aObserverList.remove(observer);
 	}
 
 	public void die()
 	{
 		// warn all observers
 		for (final ShipObserver observer : aObserverList) {
 			observer.shipDestroyed(this);
 		}
 		// Note, you do not need to remove yourself from your container. The Container should observe the ship, and so it will
 		// remove it
 		aState.deregisterProp(aID);
 	}
 
 	public float distanceTo(final GridLocation location)
 	{
 		final GridLocation myLocation = getLocation();
 		return myLocation.distanceTo(location);
 	}
 
 	public float distanceTo(final Prop prop)
 	{
 		return distanceTo(prop.getLocation());
 	}
 
 	@Override
 	public Iterable<? extends Prop> elemIterator()
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void enterCargo(final Ship containerShip, final ShipPath shipPath)
 	{
 		leaveContainer();
 		enterContainer(containerShip);
 		for (final ShipObserver observer : aObserverList) {
 			observer.shipEnteredCargo(this, containerShip, shipPath);
 		}
 		// Also notify observers of the target ship that we've entered it
 		for (final ShipObserver observer2 : containerShip.aObserverList) {
 			observer2.shipEnteredThis(containerShip, this);
 		}
 	}
 
 	@Override
 	public boolean enterContainer(final EVContainer<Prop> container)
 	{
 		if (super.enterContainer(container)) {
 			if (container instanceof ShipObserver) {
 				// if the container cares, register it
 				aObserverList.add((ShipObserver) container);
 			}
 			return true;
 		}
 		// failed to enter container
 		return false;
 	}
 
 	public void enterContainer(final EVContainer<Prop> container, final GridLocation destination)
 	{
 		aLocation = destination;
 		enterContainer(container);
 	}
 
 	public Set<Ship> getCargo()
 	{
 		return aShipCargo;
 	}
 
 	public int getCargoCapacity()
 	{
 		return aData.getCargoCapacity(aPlayer.getResearch());
 	}
 
 	public Color getColor()
 	{
 		return aPlayer.getColor();
 	}
 
 	public ResourceAmount getCost()
 	{
 		return aData.getBaseCost();
 	}
 
 	public int getCurrentCargoSize()
 	{
 		int count = 0;
 		for (final Ship s : aShipCargo) {
 			count += s.getDockingSize();
 		}
 		return count;
 	}
 
 	public ShipData getData()
 	{
 		return aData;
 	}
 
 	private int getDockingSize()
 	{
 		return aData.getDockingSize();
 	}
 
 	public int getHealth()
 	{
 		return aHealth;
 	}
 
 	public int getHealthRegenRate()
 	{
 		return aData.getHealthRegenRate(aPlayer.getResearch());
 	}
 
 	public int getMaxDamage()
 	{
 		return aData.getDamage(aPlayer.getResearch());
 	}
 
 	public int getMaxHealth()
 	{
 		return aData.getHealth(aPlayer.getResearch());
 	}
 
 	public int getMaxRadiation()
 	{
 		return aData.getRadiation(aPlayer.getResearch());
 	}
 
 	public int getMaxShields()
 	{
 		return aData.getShields(aPlayer.getResearch());
 	}
 
 	public int getRadiation()
 	{
 		return aRadiation;
 	}
 
 	public int getRadiationRate()
 	{
 		if (!(getContainer() instanceof SolarSystem)) {
 			return 0;
 		}
 		final SolarSystem ss = (SolarSystem) getContainer();
 		final Star star = ss.getStar();
 		final float distance = getLocation().distanceTo(star.getLocation());
 		return (int) (10 * (star.getRadiationLevel() / distance));
 		// FIXME - do correctly. Logarithmically based on distance (with a cap)
 	}
 
 	public int getShieldRegenRate()
 	{
 		return aData.getShieldRegenRate(aPlayer.getResearch());
 	}
 
 	public int getShields()
 	{
 		return aShields;
 	}
 
 	public float getShieldsFloat()
 	{
 		if (getMaxShields() == 0) {
 			return 0; // Prevent silent division by 0
 		}
 		return (float) getShields() / (float) getMaxShields();
 	}
 
 	public SpriteData getShieldSprite()
 	{
 		return aData.getShieldSprite(aPlayer.getResearch());
 	}
 
 	public String getShipType()
 	{
 		return aData.getTitle();
 	}
 
 	public int getSpeed()
 	{
 		// TODO: Get speed multiplier from research
 		return aData.getSpeed(aPlayer.getResearch());
 	}
 
 	public SpriteData getSprite()
 	{
 		return aData.getBaseSprite();
 	}
 
 	public TrailData getTrailData()
 	{
 		// TODO: Make this depend on research
 		return aPlayer.getRaceData().getTrailData("engine_0");
 	}
 
 	public Set<GridLocation> getValidDestinations()
 	{
 		return new Pathfinder().getValidDestinations(this);
 	}
 
 	public WeaponData getWeaponData()
 	{
 		// TODO: Make this depend on research
 		return aPlayer.getRaceData().getWeaponData("weapon_0");
 	}
 
 	public List<Point> getWeaponSlots()
 	{
 		return aData.getWeaponSlots();
 	}
 
 	public List<SpriteData> getWeaponSprites()
 	{
 		final List<Point> slots = getWeaponSlots();
 		final List<SpriteData> list = new ArrayList<SpriteData>(slots.size());
 		final WeaponData weapon = getWeaponData();
 		int index = 0;
 		for (final Point p : slots) {
 			list.add(new SpriteData(weapon.getTurretSprite(index), p.x, p.y));
 			index++;
 		}
 		return list;
 	}
 
 	public boolean isAtMaxHealth()
 	{
 		return aHealth == getMaxHealth();
 	}
 
 	public boolean isAtMaxRadiation()
 	{
 		return aRadiation == getMaxRadiation();
 	}
 
 	public boolean isAtMaxShields()
 	{
 		return aShields == getMaxShields();
 	}
 
 	public boolean isDead()
 	{
 		return aHealth <= 0;
 	}
 
 	public void jumpToSolarSystem(final SolarSystem ss, final GridLocation jumpLocation, final List<GridLocation> leavingMove,
 			final GridLocation destinationLocation)
 	{
 		removeRadiation(aState.getJumpCost());
 		final ShipPath path = new ShipPath(aLocation, jumpLocation, leavingMove);
 		for (final ShipObserver observer : aObserverList) {
 			// FIXME jumping into a solar system should be done from wormholes, not solar systems
 			observer.shipJumped(this, aContainer, path.clone(), ss);
 		}
 		leaveContainer();
 		aLocation = destinationLocation;
 		enterContainer(ss);
 	}
 
 	@Override
 	public void leaveContainer()
 	{
 		leaveContainer(null);
 	}
 
 	public void leaveContainer(final ShipPath exitPath)
 	{
 		final EVContainer<Prop> oldContainer = aContainer;
 		super.leaveContainer();
		aContainer = null;
 		// clone list so that observers may remove themselves
 		final Set<ShipObserver> oldObservers = new HashSet<ShipObserver>(aObserverList);
 		for (final ShipObserver observer : oldObservers) {
 			observer.shipLeftContainer(this, oldContainer, exitPath);
 		}
 	}
 
 	public void move(final GridLocation destination, final ShipPath path)
 	{
 		final GridLocation oldLocation = aLocation;
 		aLocation = destination;
 		for (final ShipObserver observer : aObserverList) {
 			observer.shipMoved(this, oldLocation, path.clone());
 		}
 	}
 
 	public void registerObserver(final ShipObserver sObserver)
 	{
 		aObserverList.add(sObserver);
 	}
 
 	@Override
 	public boolean removeElem(final Prop e)
 	{
 		return aShipCargo.remove(e);
 	}
 
 	private void removeHealth(final int amount)
 	{
 		addHealth(-amount);
 	}
 
 	private void removeRadiation(final int amount)
 	{
 		addRadiation(-amount);
 	}
 
 	private void removeShields(final int amount)
 	{
 		addShields(-amount);
 	}
 
 	/**
 	 * Simple function needed to pass the event to all of the ship's observers
 	 * 
 	 * @param targetShip
 	 */
 	public void shoot(final Ship targetShip)
 	{
 		for (final ShipObserver observers : aObserverList) {
 			observers.shipShot(this, targetShip.getLocation());
 		}
 	}
 
 	public void takeDamage(final int damage)
 	{
 		removeHealth(Math.max(0, damage - aShields));
 		removeShields(Math.min(aShields, damage));
 	}
 
 	@Override
 	public Json toJson()
 	{
 		final Json j = super.toJson();
 		j.setIntAttribute("health", aHealth);
 		j.setIntAttribute("radiation", aRadiation);
 		j.setIntAttribute("shields", aShields);
 		j.setStringAttribute("shiptype", aData.getType());
 		j.setListAttribute("shipCargo", aShipCargo);
 		return j;
 	}
 }
