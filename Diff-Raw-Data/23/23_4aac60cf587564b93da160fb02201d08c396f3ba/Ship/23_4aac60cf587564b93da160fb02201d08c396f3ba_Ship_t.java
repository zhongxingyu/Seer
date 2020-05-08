 package sst;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 public class Ship {
 	public static final int _DEFAULT_INITIAL_ENERGY = 50000;
 	private String registration = null;
 	int energy = 0;
 	private HashMap<String, SubSystem> listOfSubSystems = new HashMap<String, SubSystem>();
 	boolean docked = false;
 
 	public Ship(String registration, HashMap<String, SubSystem> listOfSubSystems) {
 		this.registration = registration;
 		this.energy = _DEFAULT_INITIAL_ENERGY;
 		this.listOfSubSystems = listOfSubSystems;
 	}
 
 	public Ship(String registration, int initialEnergy,
 			HashMap<String, SubSystem> listOfSubSystems) {
 		this.registration = registration;
 		this.energy = initialEnergy;
 		this.listOfSubSystems = listOfSubSystems;
 	}
 	
 	private ShieldControl getShieldControl() {
 		return (ShieldControl) listOfSubSystems.get("ShieldControl");
 	}
 
 	public String getRegistration() {
 		return registration;
 	}
 
 	public int getShieldLevel() {
 		return getShieldControl().getEnergyLevel();
 	}
 
 	public int getEnergyLevel() {
 		return this.energy;
 	}
 
 	/**
 	 * 
 	 * @param energy
 	 *            to transfer
 	 * @return energy left over from the transfer
 	 */
 	public int transferEnergyToShields(int energy) {
 		int transferAmount = energy;
 		int deficitEnergy = 0;
 		if (transferAmount > getEnergyLevel()) {
 			transferAmount = getEnergyLevel();
 			deficitEnergy = energy - transferAmount;
 		}
 		this.energy -= transferAmount;
 		int extraEnergy = getShieldControl().transferEnergy(transferAmount);
 		this.energy += extraEnergy;
 		return extraEnergy - deficitEnergy;
 	}
 
 	public void takeDamage(int damage) {
 
 		int overflow = getShieldControl().takeShieldDamage(damage);
 		if (overflow > 0) {
 			SubSystem subSystem = pickRandomSubSystem();
 			subSystem.damage(overflow);
 		}
 	}
 
 	public SubSystem pickRandomSubSystem() {
 		Set key = listOfSubSystems.keySet();
 		int i = 0;
 		int mapSize = listOfSubSystems.size();
 		int randomIndex = rnd(mapSize);
 		SubSystem subSystem = null;
 		for (Iterator iterator = key.iterator(); iterator.hasNext();) {
 			subSystem = listOfSubSystems.get(iterator.next());
 			if (i == randomIndex) {
 				break;
 			}
 			i++;
 		}
 		return subSystem;
 	}
 
 	public static Random generator = new Random();
 
 	public static int rnd(int maximum) {
 		return generator.nextInt(maximum);
 	}
 
 	public boolean isSubSystemDamaged() {
 		boolean isDamaged = false;
 		Iterator it = this.listOfSubSystems.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry subSystem = (Map.Entry) it.next();
 			SubSystem subSystemEntry = (SubSystem) subSystem.getValue();
 			if (subSystemEntry.isDamaged()) {
 				isDamaged = true;
 				break;
 			}
 		}
 		return isDamaged;
 	}
 
 	public void rest(int tenthsOfStarDays) {
 		int effectiveTenthsOfStarDays = tenthsOfStarDays;
 		if (docked)	{
 			effectiveTenthsOfStarDays *= 2;
 		}
 		Iterator it = this.listOfSubSystems.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry subSystem = (Map.Entry) it.next();
 			SubSystem subSystemEntry = (SubSystem) subSystem.getValue();
 			subSystemEntry.repairDamage(effectiveTenthsOfStarDays);
 		}		
 	}
 	
 	public boolean dock(boolean starbaseNearby) {
 		if (starbaseNearby && !docked) {
 			docked = true;
 		}
 		return docked;
	}
 
 	/**
 	 * 
 	 * @param sectors number of sectors to move
 	 * @return number of stardates it takes for the move
 	 */
 	public void moveSectors(int sectors) {
 		int timeSpentInTenthsOfStarDays = sectors * 100;
 		rest(timeSpentInTenthsOfStarDays);
 	}
 }
