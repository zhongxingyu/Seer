 package world;
 
 /**
  * @author jpoley15 Creates a new LivingThing, stores stats 
  *
  */
 
 public class LivingThing extends Thing {
 
	
	private int MaxHp;
	private int MaxStrength;
	private int MaxIntelligence;
	private int MaxDexterity;
	private int MaxSpeed;
	private int MaxMana;
 	
 	private String name;
	private int hp;
 	private int strength;
 	private int intelligence;
 	private int dexterity;
 	private int speed;
 	private int mana;
 
 	public LivingThing(boolean b) {
 		super(b);
 	}
 	
 	public LivingThing(String name, int strength, int intelligence, int dexterity, boolean solid){
 		super(solid);
 		this.name = name;
 		this.strength = strength;
 		this.intelligence = intelligence;
 		this.dexterity = dexterity;
 		hp = 20;
 		mana = (intelligence * 5) + 1;
 		speed = (dexterity * 5) + 1;
 		
 	}
 
 	public int getHp() {
 		return hp;
 	}
 
 	public void setHp(int hp) {
 		this.hp = hp;
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
 	public void updateStrength(int Strength){
 		strength += Strength;
 	}
 
 	public void updateIntelligence(int Intelligence){
 		intelligence += Intelligence;
 		updateMana(0);
 	}
 	
 
 	public void updateDexterity(int Dexterity){
 		dexterity += Dexterity;
 		updateSpeed(0);
 	}
 	
 
 	public void updateSpeed(int increase){
 		speed = (dexterity * 5) + increase + 1;
 	}
 	
 
 	public void updateMana(int increase){
 		mana = (intelligence * 5) + increase + 1;
 	}
 	
 	public void updateHp(int increase){
 		hp += increase;
 	}
 
 }
