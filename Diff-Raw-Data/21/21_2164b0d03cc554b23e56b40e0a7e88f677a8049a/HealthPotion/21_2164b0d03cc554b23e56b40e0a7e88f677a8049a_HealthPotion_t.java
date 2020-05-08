 package edu.gatech.CS2340.GrandTheftPoke.backend.Items;
 
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.Person;
 
 /**
  * Represents a HealthPotion
  * 
  * @author Team Rocket
  * 
  */
 public class HealthPotion extends Item implements Usable {
 	@Override
 	public void use(Person person) {
 		int health = person.getHealth();
 		int maxHealth = person.getMaxHealth();
 		if (health <= (maxHealth - 20)) {
 			person.setHealth(health + 20);
 		} else {
 			person.setHealth(maxHealth);
 		}
 	}
 
 	@Override
 	public void unUse(Person person) {
		return;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Health Potion";
 	}
 
 	@Override
 	public String getDescription() {
 		return "Health Potion restores your health to \n"
 				+ "to the maximum. This is allows you\n"
 				+ "stay alive longer.\n" + "Health = max";
 
 	}
 
 	@Override
 	public String getFileName() {
 		return "potion1";
 	}
 }
