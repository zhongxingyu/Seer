 package world;
 
 /**
  * @author jpoley15 Creates a new character, allows for stat implementation 
  *
  */
 public class Character extends LivingThing {
 
 	private int xp;
 
 	public Character(boolean solid) {
 		super(solid);
 	}
 
 	public Character(String name, int strength, int intelligence, int dexterity, boolean solid) {
 		super(name, strength, intelligence, dexterity, solid);
 	}
 
 	public int getXp() {
 		return xp;
 	}
 
 	public void setXp(int xp) {
 		this.xp = xp;
 	}
 	
 
 }
