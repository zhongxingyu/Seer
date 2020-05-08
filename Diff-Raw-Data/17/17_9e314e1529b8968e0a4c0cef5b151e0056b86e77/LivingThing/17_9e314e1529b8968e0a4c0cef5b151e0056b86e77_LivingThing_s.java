 package world;
 
 /**
  * @author jpoley15 Creates a new LivingThing, stores stats 
  *
  */
 
 public class LivingThing extends Thing {
 
 	private int hp;
 	private String name;
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
		updateMana();
 	}
 	
 
 	public void updateDexterity(int Dexterity){
 		dexterity += Dexterity;
		updateSpeed();
 	}
 	
 
	public void updateSpeed(){
		speed = (dexterity * 5) + 1;
 	}
 	
 
	public void updateMana(){
		mana = (intelligence * 5) + 1;
 	}
 	
 	public void updateHp(int increase){
 		hp += increase;
 	}
 
 }
