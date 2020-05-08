 package world;
 
 import java.awt.Color;
 
 /**
  * @author jpoley15 Creates a new character, allows for stat implementation
  * 
  */
 public class Character extends LivingThing {
 
 	private int xp;
 	private int level;
 	private Weapon weapon;
 

 	public Character(boolean solid, Color c) {
 		super(solid, c);
 	}
 
 	public Character(String name, int strength, int intelligence, int dexterity, boolean solid, Color c) {
 		super(name, strength, intelligence, dexterity, solid, c);
 		xp = 0;
 		level = 0;
 	}
 
 	public int getXp() {
 		return xp;
 	}
 
 	public void setXp(int xp) {
 		this.xp = xp;
 	}
 	public Weapon getWeapon() {
 		return weapon;
 	}
 
 	public void setWeapon(Weapon weapon) {
 		this.weapon = weapon;
 	}
 
 	public void levelUp() {
		if (xp < (750 * level) + (250 * level)) {
			level = level;
		} else {
 			level++;
 			int points = getIntelligence();
 			updateMaxDexterity(points);
 			updateMaxIntelligence(points);
 			updateMaxStrength(points);
 			updateMaxSpeed(0);
 			updateMaxMana(0);
 			updateMaxHp(10);
 		}
 	}
 

 }
