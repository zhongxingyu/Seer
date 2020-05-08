 package world;
 
 public class Character extends LivingThing {
 
 	private String name;
 	private int strength;
 	private int intelligence;
 	private int dexterity;
 	private int speed;
 	private int mana;
 	private int xp;
 
 	// Constructs a default character with name "name" and stats all set to 10
 
 	/**
	 * @param solid this is a boolean indicating whether something is solid (true) or not (false)
 	 */
 	public Character(boolean solid) {
 
 		super(solid);
 		name = "name";
 		strength = 10;
 		intelligence = 10;
 		dexterity = 10;
 
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
 
 		super(solid);
 		this.name = name;
 		this.strength = strength;
 		this.intelligence = intelligence;
 		this.dexterity = dexterity;
 
 		setHp(strength * 5);
 		speed = dexterity * 5;
 		mana = intelligence * 5;
 		xp = 0;
 
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public int getStrength() {
 		return strength;
 	}
 
 	public void setStrength(int strength) {
 		this.strength = strength;
 	}
 
 	public int getIntelligence() {
 		return intelligence;
 	}
 
 	public void setIntelligence(int intelligence) {
 		this.intelligence = intelligence;
 	}
 
 	public int getDexterity() {
 		return dexterity;
 	}
 
 	public void setDexterity(int dexterity) {
 		this.dexterity = dexterity;
 	}
 
 	public int getSpeed() {
 		return speed;
 	}
 
 	public void setSpeed(int speed) {
 		this.speed = speed;
 	}
 
 	public int getMana() {
 		return mana;
 	}
 
 	public void setMana(int mana) {
 		this.mana = mana;
 	}
 
 	public int getXp() {
 		return xp;
 	}
 
 	public void setXp(int xp) {
 		this.xp = xp;
 	}
 	
 	public void updateStrength(int Strength){
 		strength += Strength;
 	}
 	
 	public void updateIntelligence(int Intelligence){
 		intelligence += Intelligence;
 	}
 	
 	public void updateDexterity(int Dexterity){
 		dexterity += Dexterity;
 	}
 	
 	public void updateSpeed(){
 		speed = dexterity * 5;
 	}
 	
 	public void updateMana(){
 		mana = intelligence * 5;
 	}
 	
 	public void updateHp(){
 		setHp(strength * 5);
 	}
 
 }
