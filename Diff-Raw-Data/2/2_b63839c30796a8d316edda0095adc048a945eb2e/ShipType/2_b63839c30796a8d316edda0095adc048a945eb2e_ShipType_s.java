 // $codepro.audit.disable largeNumberOfParameters
 /**
  * Create Ship type
  * 
  * @author apham9
  * @date 10/10/2012
  */
 
 package com.canefaitrien.spacetrader.models;
 
 import java.util.Random;
 
 /**
  */
 public enum ShipType {
 	// speed of 14 parsecs, 1 weapon, 1 gadget, 15 cargo hold
 	/**
 	 * Field GNAT.
 	 */
 	GNAT(GNAT_DISTANCE, GNAT_HULL_STRENGTH, GNAT_WEAPON_SLOTS, GNAT_GADGET_SLOTS, GNAT_SHIELD_SLOTS, GNAT_CARGO_HOLD, GNAT_CREW, GNAT_MERCENARY, "Gnat"),//14, 100, 1, 1, 0, 15, 0, 0, "Gnat"),
 	// small, few cargo holds, a weak hull and no equipment, 20 parsecs/tank, 
 	// can be converted from a escape pod
 	/**
 	 * Field FLEA.
 	 */
 	FLEA(FLEA_DISTANCE, FLEA_HULL_STRENGTH, FLEA_WEAPON_SLOTS, FLEA_GADGET_SLOTS, FLEA_SHIELD_SLOTS, FLEA_CARGO_HOLD, FLEA_CREW, FLEA_MERCENARY, "Flea"),//17, 100, 1, 1, 1, 20, 0, 0, "Flea"),
 	// cheap, 1 of each equipment type slot, 17 parsecs/tank
 	// 20 cargo hold
 	/**
 	 * Field FIREFLY.
 	 */
 	FIREFLY(FIREFLY_DISTANCE, FIREFLY_HULL_STRENGTH, FIREFLY_WEAPON_SLOTS, FIREFLY_GADGET_SLOTS, FIREFLY_SHIELD_SLOTS, FIREFLY_CARGO_HOLD, FIREFLY_CREW, FIREFLY_MERCENARY, "Firefly"),//17, 200, 1, 1, 1, 20, 0, 0, "Firefly"),
 	// 13 parsec/tank, strong hull, 2 weapon, 1 shield, 1 gadget, 15 cargo hold
 	/**
 	 * Field MOSQUITO.
 	 */
 	MOSQUITO(MOSQUITO_DISTANCE, MOSQUITO_HULL_STRENGTH, MOSQUITO_WEAPON_SLOTS, MOSQUITO_GADGET_SLOTS, MOSQUITO_SHIELD_SLOTS, MOSQUITO_CARGO_HOLD, MOSQUITO_CREW, MOSQUITO_MERCENARY, "Mosquito"),//15, 300, 2, 1, 1, 15, 0, 0, "Mosquito"),
 	//20 cargo hold, hull = fifrefly, 2 shield, 2 gadget, mercernary available 15/tank
 	/**
 	 * Field BUMBLEBEE.
 	 */
 	BUMBLEBEE(BUMBLEBEE_DISTANCE, BUMBLEBEE_HULL_STRENGTH, BUMBLEBEE_WEAPON_SLOTS, BUMBLEBEE_GADGET_SLOTS, BUMBLEBEE_SHIELD_SLOTS, BUMBLEBEE_CARGO_HOLD, BUMBLEBEE_CREW, BUMBLEBEE_MERCENARY, "Bublebee"),//15, 200, 0, 2, 2, 20, 0, 5, "Bumblebee"),
 	// weak hull no weapon, 1 shield, 1 gadget, 50 cargo, 14 parsec/tank
 	/**
 	 * Field BEETLE.
 	 */
 	BEETLE(BEETLE_DISTANCE, BEETLE_HULL_STRENGTH, BEETLE_WEAPON_SLOTS, BEETLE_GADGET_SLOTS, BEETLE_SHIELD_SLOTS, BEETLE_CARGO_HOLD, BEETLE_CREW, BEETLE_MERCENARY, "Beetle"),//14, 100, 0, 1, 1, 50, 0, 0, "Beetle"),
 	// Strong hull, 3 weapon, 2 shield, 1 gadget, 20 cargo, 16/tank
 	/**
 	 * Field HORNET.
 	 */
 	HORNET(HORNET_DISTANCE, HORNET_HULL_STRENGTH, HORNET_WEAPON_SLOTS, HORNET_GADGET_SLOTS, HORNET_SHIELD_SLOTS, HORNET_CARGO_HOLD, HORNET_CREW, HORNET_MERCENARY, "Hornet"),//16, 300, 3, 1, 2, 20, 0, 0, "Hornet"),
 	// 30 cargo/ 2 weapons, 2 shield, 3 gadget, 3 crew, 15/tank
 	/**
 	 * Field GRASSHOPPER.
 	 */
 	GRASSHOPPER(GRASSHOPPER_DISTANCE, GRASSHOPPER_HULL_STRENGTH, GRASSHOPPER_WEAPON_SLOTS, GRASSHOPPER_GADGET_SLOTS, GRASSHOPPER_SHIELD_SLOTS, GRASSHOPPER_CARGO_HOLD, GRASSHOPPER_CREW, GRASSHOPPER_MERCENARY, "Grasshopper"),//15, 200, 2, 3, 2, 30, 3, 0, "Grasshopper"),
 	// Strong hull, 3 shield, 2 gadgets, 3 crew, 60 cargo, 1 weapon, 13/tank
 	/**
 	 * Field TERMITE.
 	 */
 	TERMITE(TERMITE_DISTANCE, TERMITE_HULL_STRENGTH, TERMITE_WEAPON_SLOTS, TERMITE_GADGET_SLOTS, TERMITE_SHIELD_SLOTS, TERMITE_CARGO_HOLD, TERMITE_CREW, TERMITE_MERCENARY, "Termite"),//13, 300, 1, 2, 3, 60, 3, 0, "Termite"),
 	// Utimalte private ship, strong hull, 3 weapons, 2 shield, 2 gadget, 3 crew, 14/tank, 35 cargo
 	/**
 	 * Field WASP.
 	 */
 	WASP(WASP_DISTANCE, WASP_HULL_STRENGTH, WASP_WEAPON_SLOTS, WASP_GADGET_SLOTS, WASP_SHIELD_SLOTS, WASP_CARGO_HOLD, WASP_CREW, WASP_MERCENARY, "Wasp"),//14, 300, 3, 2, 2, 35, 3, 0, "Wasp")
 	;
 	
 	// Ship constants fill out and move to ShipConstants.java
 	public static final int 
 		GNAT_DISTANCE = 14, 
 		GNAT_HULL_STRENGTH = 100, 
 		GNAT_WEAPON_SLOTS = 1, 
 		GNAT_GADGET_SLOTS = 1, 
 		GNAT_SHIELD_SLOTS = 0, 
 		GNAT_CARGO_HOLD = 15, 
 		GNAT_CREW = 0, 
 		GNAT_MERCENARY = 0;//14, 100, 1, 1, 0, 15, 0, 0, "Gnat"),
 	
 	public static final int 
 		FLEA_DISTANCE, 
 		FLEA_HULL_STRENGTH, 
 		FLEA_WEAPON_SLOTS, 
 		FLEA_GADGET_SLOTS, 
 		FLEA_SHIELD_SLOTS, 
 		FLEA_CARGO_HOLD, 
 		FLEA_CREW, 
 		FLEA_MERCENARY;
 		//17, 100, 1, 1, 1, 20, 0, 0, "Flea"),
 	
 	public static final int
 		FIREFLY_DISTANCE, 
 		FIREFLY_HULL_STRENGTH, 
 		FIREFLY_WEAPON_SLOTS, 
 		FIREFLY_GADGET_SLOTS, 
 		FIREFLY_SHIELD_SLOTS, 
 		FIREFLY_CARGO_HOLD, 
 		FIREFLY_CREW, 
 		FIREFLY_MERCENARY;//17, 200, 1, 1, 1, 20, 0, 0, "Firefly"),
 	
 	public static final int
 		MOSQUITO_DISTANCE, 
 		MOSQUITO_HULL_STRENGTH, 
 		MOSQUITO_WEAPON_SLOTS, 
 		MOSQUITO_GADGET_SLOTS, 
 		MOSQUITO_SHIELD_SLOTS, 
 		MOSQUITO_CARGO_HOLD, 
 		MOSQUITO_CREW, 
 		MOSQUITO_MERCENARY;//15, 300, 2, 1, 1, 15, 0, 0, "Mosquito"),
 	
 	public static final int
 		BUMBLEBEE_DISTANCE, 
 		BUMBLEBEE_HULL_STRENGTH, 
 		BUMBLEBEE_WEAPON_SLOTS, 
 		BUMBLEBEE_GADGET_SLOTS, 
 		BUMBLEBEE_SHIELD_SLOTS, 
 		BUMBLEBEE_CARGO_HOLD, 
 		BUMBLEBEE_CREW, 
 		BUMBLEBEE_MERCENARY;//15, 200, 0, 2, 2, 20, 0, 5, "Bumblebee"),
 	
 	public static final int
 		BEETLE_DISTANCE, 
 		BEETLE_HULL_STRENGTH, 
 		BEETLE_WEAPON_SLOTS, 
 		BEETLE_GADGET_SLOTS, 
 		BEETLE_SHIELD_SLOTS, 
 		BEETLE_CARGO_HOLD, 
 		BEETLE_CREW, 
 		BEETLE_MERCENARY;//14, 100, 0, 1, 1, 50, 0, 0, "Beetle"),
 	
 	public static final int 
 		HORNET_DISTANCE, 
 		HORNET_HULL_STRENGTH, 
 		HORNET_WEAPON_SLOTS, 
 		HORNET_GADGET_SLOTS, 
 		HORNET_SHIELD_SLOTS, 
 		HORNET_CARGO_HOLD, 
 		HORNET_CREW, 
 		HORNET_MERCENARY;//16, 300, 3, 1, 2, 20, 0, 0, "Hornet"),
 	
 	public static final int 
 		GRASSHOPPER_DISTANCE, 
 		GRASSHOPPER_HULL_STRENGTH,
 		GRASSHOPPER_WEAPON_SLOTS, 
 		GRASSHOPPER_GADGET_SLOTS, 
 		GRASSHOPPER_SHIELD_SLOTS, 
 		GRASSHOPPER_CARGO_HOLD, 
 		GRASSHOPPER_CREW, 
 		GRASSHOPPER_MERCENARY;//15, 200, 2, 3, 2, 30, 3, 0, "Grasshopper")
 	
 	public static final int 
 		TERMITE_DISTANCE, 
 		TERMITE_HULL_STRENGTH, 
 		TERMITE_WEAPON_SLOTS, 
 		TERMITE_GADGET_SLOTS, 
 		TERMITE_SHIELD_SLOTS, 
 		TERMITE_CARGO_HOLD, 
 		TERMITE_CREW, 
 		TERMITE_MERCENARY;//13, 300, 1, 2, 3, 60, 3, 0, "Termite"),
 	
 	public static final int 
 		WASP_DISTANCE, 
 		WASP_HULL_STRENGTH, 
 		WASP_WEAPON_SLOTS, 
 		WASP_GADGET_SLOTS, 
 		WASP_SHIELD_SLOTS, 
 		WASP_CARGO_HOLD, 
 		WASP_CREW, 
 		WASP_MERCENARY;//14, 300, 3, 2, 2, 35, 3, 0, "Wasp")
 	
 	
 	
 	// ShipType info
 	/**
 	 * Field maxWeaponSlots.
 	 */
 	public final int maxWeaponSlots;
 
 	/**
 	 * Field maxGadgetSlots.
 	 */
 	public final int maxGadgetSlots;
 
 	/**
 	 * Field maxShieldSlots.
 	 */
 	public final int maxShieldSlots;
 
 	/**
 	 * Field maxDistance.
 	 */
 	public final int maxDistance;
 
 	/**
 	 * Field maxCargoHold.
 	 */
 	public final int maxCargoHold;
 
 	/**
 	 * Field maxCrew.
 	 */
 	public final int maxCrew;
 
 	/**
 	 * Field maxMercenary.
 	 */
 	public final int maxMercenary;
 
 	/**
 	 * Field maxHullStrength.
 	 */
 	public final int maxHullStrength;
 
 	/**
 	 * Field name.
 	 */
 	public final String name;
 	
 	/**
 	 * Constructor for ShipType
 	 * @param maxDistance
 	 * @param maxHullStrength
 	 * @param maxWeaponSlots
 	 * @param maxGadgetSlots
 	 * @param maxShieldSlots
 	 * @param maxCargoHold
 	 * @param maxCrewMembers
 	 * @param maxMercernary
 	 * @param name
 	 */
	private ShipType (int maxDistance, int maxHullStrength, 
 			int maxWeaponSlots, int maxGadgetSlots, int maxShieldSlots,
 			int maxCargoHold, int maxCrewMembers, int maxMercernary, String name) {
 		
 		this.maxWeaponSlots = maxWeaponSlots;
 		this.maxGadgetSlots = maxGadgetSlots;
 		this.maxShieldSlots = maxShieldSlots;
 		this.maxDistance = maxDistance;
 		this.maxCargoHold = maxCargoHold;
 		this.maxCrew = maxCrewMembers;
 		this.maxMercenary = maxMercernary;
 		this.maxHullStrength = maxHullStrength;
 		this.name = name;
 	}
 	
 	/**
 	 * Method toString.
 	 * @return String
 	 */
 	public String toString() {
 		return name;
 	}
 	
 	/**
 	 * Method to get a random ShipType
 	
 	 * @return random ShipType */
 	public static ShipType getAShip() {
 		final int pick = new Random().nextInt(ShipType.values().length);
 	    return ShipType.values()[pick];
 	}
 }
