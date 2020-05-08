 package world;
 
 /**
  * @author jpoley15 Creates a new character, allows for stat implementation 
  *
  */
 public class Character extends LivingThing {
 
 	private int xp;
 	private int level;
 
 	public Character(boolean solid) {
 		super(solid);
 	}
 
 	public Character(String name, int strength, int intelligence, int dexterity, boolean solid) {
 		super(name, strength, intelligence, dexterity, solid);
 		xp = 0;
 		level = 0;
 	}
 
 	public int getXp() {
 		return xp;
 	}
 
 	public void setXp(int xp) {
 		this.xp = xp;
 	}
 	
 	public void levelUp(){
 		if(xp < (750 * level) + (250 * level)){
 			level = level;
 		} else {
 			level++;
			updateDexterity(getDexterity()/3);
			updateIntelligence(getIntelligence()/3);
			updateStrength(getStrength()/3);
 			updateSpeed();
 			updateMana();
			updateHp();
 		}
 	}
 
 }
