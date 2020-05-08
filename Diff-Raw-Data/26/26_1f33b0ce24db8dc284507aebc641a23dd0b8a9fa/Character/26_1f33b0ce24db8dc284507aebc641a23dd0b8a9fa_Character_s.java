 package world;
 
 /**
  * @author jpoley15 Creates a new character, allows for stat implementation 
  *
  */
 public class Character extends LivingThing {
 
 	private int xp;
 
	// Constructs a default character with name "name" and stats all set to 10

	/**
	 * @param solid is a boolean indicating whether something is solid (true) or not (false)
	 */
 	public Character(boolean solid) {

 		super(solid);


 	}
 
	// Constructs a character with user specified name and stats //

	/**
	 * @param name is a String that stores the name of the character
	 * @param strength is an int that stores the character's strength
	 * @param intelligence is an int that stores the character's intelligence
	 * @param dexterity is an int that stores the character's dexterity 
	 * @param solid is a boolean indicating whether the character is solid
	 */
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
