 package com.example.SubGameObj;
 
 
 import java.util.HashSet;
 import java.util.Set;
 
 import com.example.SubGameObj.Entity.EnemyShip;
 import com.example.SubGameObj.Entity.Ship;
 import com.example.SubGameObj.Event.Event;
 import com.example.SubGameObj.Utils.ObjectListener;
 import com.example.SubGameObj.Weapon.Weapon;
 
 
 /**
  * This class represents the Map on which Ships and Events occur.
  * This is also the entry point for the onTurn method and holds the set of ships and events.
  * Generic T holds the Object type that will be the canvas for drawing.
  * @author anshirtz
  *
  */
 public class GameMap implements ObjectListener {
 	
 	private Set <Ship> mActiveShips = null;
 	private Set <Weapon> mWeaponsActive = null;
 	private Set <Event> mCurrentEvents = null;
 	
 	private Set <Ship> shipsToRemove = null;
 	private Set <Weapon> weaponsToRemove = null;
 	
 	public static final int xSize = 1000;
 	public static final int ySize = 1000;
 	
 	public static GameMap instance = null;
 	
 	public static GameMap getInstance() {
 		if (instance == null) {
 			instance = new GameMap();
 		}
 		return instance;
 	}
 	
 	protected static void destroyGame () {
 		instance = null;
 	}
 	
 	private GameMap () {
 		this.mActiveShips = new HashSet<Ship>();
 		this.mCurrentEvents = new HashSet<Event>();
 		this.mWeaponsActive = new HashSet<Weapon>();
 		this.shipsToRemove = new HashSet<Ship>();
 		this.weaponsToRemove = new HashSet<Weapon>();
 	}
 	
 	protected void onTurn () {
 		this.weaponOnTurn();
 		this.shipOnTurn();
 		this.eventActions();
 		this.purgeSets();
 	}
 
 	private void purgeSets() {
 		this.mCurrentEvents.removeAll(mCurrentEvents);
 		this.mActiveShips.removeAll(shipsToRemove);
 		this.mWeaponsActive.removeAll(weaponsToRemove);
 		this.shipsToRemove.removeAll(shipsToRemove);
 		this.weaponsToRemove.removeAll(weaponsToRemove);
 	}
 	
 	private void eventActions() {
 		for (Event event : this.mCurrentEvents) {
 			for (Ship ship : this.mActiveShips) {
 				event.eventAction(ship);
 			}
 		}
 	}
 
 	private void weaponOnTurn() {
 		for (Weapon weapon : this.mWeaponsActive) {
 			weapon.onTurn();
 		}
 	}
 	
 	private void shipOnTurn() {
 		for (Ship ship : this.mActiveShips) {
 			ship.onTurn();
 		}
 	}
 	
 	protected void drawMap(ObjectDrawer objectDrawer) {
 		for (Ship ship : this.getmActiveShips()) {
 			ship.onDraw(objectDrawer);
 		}
 		for (Weapon weapon : this.getmWeaponsActive()) {
 			weapon.onDraw(objectDrawer);
 		}
 		for (Event event : this.getmCurrentEvents()) {
 			event.onDraw(objectDrawer);
 		}
 	}
 	
 	/*
 	 *********************
 	 * Getters & Setters *
 	 *********************
 	 */
 	
 	public Set<Ship> getmActiveShips() {
 		return mActiveShips;
 	}
 
 	public void setmActiveShips(Set<Ship> mActiveShips) {
 		this.mActiveShips = mActiveShips;
 	}
 
 	public Set<Weapon> getmWeaponsActive() {
 		return mWeaponsActive;
 	}
 
 	public void setmWeaponsActive(Set<Weapon> mWeaponsActive) {
 		this.mWeaponsActive = mWeaponsActive;
 	}
 
 	public Set<Event> getmCurrentEvents() {
 		return mCurrentEvents;
 	}
 
 	public void setmCurrentEvents(Set<Event> mCurrentEvents) {
 		this.mCurrentEvents = mCurrentEvents;
 	}
 	
 	/*
 	 **************************
 	 * ObjectListener Methods *
 	 **************************
 	 */
 	
 	@Override
 	public void createShip(Ship ship) {
 		this.mActiveShips.add(ship);
 	}
 	
 	@Override
 	public void removeShip(Ship ship) {
 		this.shipsToRemove.add(ship);
 	}
 
 	@Override
 	public void createEvent(Event event) {
 		this.mCurrentEvents.add(event);
 	}
 
 	@Override
 	public void removeEvent(Event event) {
 		if (this.mCurrentEvents.contains(event)) {
 			this.mCurrentEvents.remove(event);
 		}
 	}
 
 	@Override
 	public void createWeapon(Weapon weapon) {
 		this.mWeaponsActive.add(weapon);
 	}
 
 	@Override
 	public void removeWeapon(Weapon weapon) {
 		this.weaponsToRemove.add(weapon);
 	}
 
 }
