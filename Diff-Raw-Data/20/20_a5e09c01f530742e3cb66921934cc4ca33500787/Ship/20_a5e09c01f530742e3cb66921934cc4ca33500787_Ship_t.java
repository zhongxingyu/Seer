 package model;
 
 /**
  * Ship class for holding info on the ship
  * 
  * @author apham9
  * @version 1.0
  */
 public class Ship {
 	
 	// Ship info
 	private ShipType type;
 	private int hullStrength;
 	private int currentCargoHold;
 	private int[] cargo;
 	private int fuel;
 //	private Weapons[] weapons;
 //	private Shield[] shields;
 //	private Gadget[] gadgets;
 	
	public static final int MPG = 10;
 	
 	/**
 	 * Constructor for new Ship
 	 */
 	public Ship(ShipType type) {
 		this(type, type.MAX_HULL_STRENGTH, 0, new int[TradeGood.values().length], type.MAX_DISTANCE);
 	}
 	
 	/**
 	 * Constructor for load Ship
 	 */
 	public Ship(ShipType type, int hullStrength, int currentCargoHold, int[] cargo, int fuel) {
 		this.type = type;
 		this.hullStrength = hullStrength;
 		this.currentCargoHold = currentCargoHold;
 		this.cargo = cargo;
 		this.fuel = fuel;
 	}
 	
 	/**
 	 * Adds a good to the cargo
 	 * 
 	 * @param good good to add to cargo
 	 * @throws Exception if no room in cargo
 	 */
 	public void addGood(TradeGood good) throws Exception {
 		if (currentCargoHold == type.MAX_CARGO_HOLD) {
 			throw new Exception("No more room in the cargo, captain!");
 		} else {
 			cargo[good.ordinal()]++;
 			currentCargoHold++;
 		}
 	}
 	
 	/**
 	 * Removes a good from the cargo
 	 * 
 	 * @param good good to remove from cargo
 	 * @throws Exception if no good in cargo
 	 */
 	public void removeGood(TradeGood good) throws Exception {
 		if (cargo[good.ordinal()] == 0) {
 			throw new Exception("No such item in the cargo, captain!");
 		} else {
 			cargo[good.ordinal()]--;
 			currentCargoHold--;
 		}
 	}
 	
 	public ShipType getType() {
 		return type;
 	}
 	
 	public int[] getCargo() {
 		return cargo;
 	}
 
 	public int getFuel() {
 		return fuel;
 	}
 
 	public void setFuel(int fuel) {
 		this.fuel = fuel;
 	}
 	
 	public int getHullStrength() {
 		return hullStrength;
 	}
 	public int getMaxSpeed() {
 		return type.getMaxSpeed();
 	}
 }
