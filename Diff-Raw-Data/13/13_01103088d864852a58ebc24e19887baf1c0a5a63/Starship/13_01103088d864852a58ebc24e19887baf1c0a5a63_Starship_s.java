 package StarTrekV2;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Starship {
 
 	private static final Randomizer randomizer = new Randomizer();
 
 	public Shield shield = new Shield(5000);
 	public List<Subsystem> subSystems;
 	public SystemRestore restoreSource = new SystemRestore();
 	
 	public Starship() {
 		subSystems = new ArrayList<Subsystem>();
 
 		subSystems.add(new Engine(5000));
 		subSystems.add(new Weapon(5000));
 		subSystems.add(new LifeSupport(5000));
 	}
 
 	/**
 	 * Selects randomly which subsystem to hit after the shield is depleted. 
 	 * @return
 	 */
 	private Subsystem selectSystemToHit() {
		int subSystemIndex = randomizer.nextInt(subSystems.size());
 		return subSystems.get(subSystemIndex);
 	}
 
 	/**
 	 * Subsystem checks energy level of the shield
 	 * 		If shield energy > 0, absorb the energy hit
 	 * 		If shield energy <= 0, send energy hit to Strategic subsystem
 	 * Check if shield energy = 0, deplete the shield
 	 * Check if the Strategic subsystem is damaged or not, report subsystem status
 	 * 
 	 * @param energyHit
 	 */
 	public void hitStarship(
 			int energyHit) 
 	{
 		int unabsorbedEnergy = energyHit;
 
 		if (!shield.isDepleted()) {
 			// Hit shield first
 			unabsorbedEnergy = shield.absorb(energyHit);
 		}
 
 		if (unabsorbedEnergy > 0) {
 			// Select randomly which system to hit
 			Subsystem subSystem = selectSystemToHit();
 			subSystem.takeDamage(unabsorbedEnergy);
 		}
 	}
 	
 	public void repairStarship() {
 		for (Subsystem subSystem : subSystems) {
 			
 			if (subSystem.isDamaged()) {
 				if (subSystem.isRepairable()) {
 					restoreSource.fixSubsystem(subSystem);
 				}
 				else {
 					System.err.println("Starship lost its subsystem");
 				}
 			}
 		}
 	}
 }
