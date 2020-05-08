 package sst;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 public class Ship {
 	public static final int _DEFAULT_INITIAL_ENERGY = 50000;
 	private String registration = null;
 	private ShieldControl shield = null;
 	int energy = 0;
 	private HashMap<String, SubSystem> listOfSubSystems = new HashMap<String, SubSystem> ();
 	
 	public Ship(String registration) {
 		this.registration = registration;
 		this.shield = new ShieldControl();
 		this.energy = _DEFAULT_INITIAL_ENERGY;
 		initializeSubSystems();
 	}
 	
 	public Ship(String registration, int initialEnergy) {
 		this.registration = registration;
 		this.shield = new ShieldControl();
 		this.energy = initialEnergy;
 		initializeSubSystems();		
 	}
 	
 	private void initializeSubSystems() {
 		this.listOfSubSystems.put("Weapons", new SubSystem(2000, 200));
 		this.listOfSubSystems.put("Engine", new SubSystem(1500, 100));
 		this.listOfSubSystems.put("LifeSupport", new SubSystem(1000, 100));
 	}
 
 	public String getRegistration() {
 		return registration;
 	}
 	
 	public int getShieldLevel() {
 		return this.shield.getEnergyLevel();
 	}
 
 	public int getEnergyLevel() {
 		return this.energy;
 	}
 
 	/**
 	 * 
 	 * @param energy to transfer
 	 * @return energy deficit that ship was unable to transfer
 	 */
 	public int transferEnergyToShields(int energy) {
 		int transferAmount = energy;
 		int deficitEnergy = 0;
 		if (transferAmount > getEnergyLevel()) {
 			transferAmount = getEnergyLevel();
 			deficitEnergy = energy - transferAmount;
 		}
 		if (shield.getEnergyLevel() + energy > ShieldControl._MAX_SHIELD_ENERGY) {
 			transferAmount = shield.getEnergyLevel() + energy - ShieldControl._MAX_SHIELD_ENERGY;
 		}
 		shield.transferEnergy(transferAmount);
 		this.energy -= transferAmount;
 		return deficitEnergy;
 	}
 	
 	public void takeDamage(int damage) {
		int overflow = shield.takeDamage(damage);
		//TODO: damage a subsystem
 	}
 	
 	public boolean isSubSystemDamaged() {
 		boolean isDamaged = false;
 	    Iterator it = this.listOfSubSystems.entrySet().iterator();
 	    while (it.hasNext()) {
 	        Map.Entry subSystem = (Map.Entry)it.next();
 	        SubSystem subSystemEntry = (SubSystem) subSystem.getValue();
 	        if (subSystemEntry.isDamaged()) {
 	        	isDamaged = true;
 	        	break;
 	        }
 	    }
 	    return isDamaged;
 	}
 }
