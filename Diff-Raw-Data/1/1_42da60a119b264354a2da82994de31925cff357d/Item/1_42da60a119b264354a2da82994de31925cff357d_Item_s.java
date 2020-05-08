 package orig;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import org.newdawn.slick.Image;
 
 
 import orig.Attack.AttackDirection;
 import orig.Attack.AttackPattern;
 import orig.Creature.cStats;
 import orig.Creature.sVal;
 import util.ImageUtil;
 
 public class Item implements Serializable {
 	
 	public enum iType {
 		HEAD, ARMOR, BOOTS, HAND, TOTAL;
 		
 		public String toString() {
 			return this.name();
 		}
 	}
 	
 	public enum AttackType {
 		BUFF, PHYS, RANGED, NONE
 	}
 	
 	//private UniversalElements UE = null;
 
 	private Element[] consists = null; //need to know proportions
 	private Element[] repairs = null; //need to know proportions
 	//private Element[] improve;
 
 	private int level = 1; // item level
 	private int hands = 1; //number of hands required to wield
 	private int techRequired = 1;
 	private double phys_tech_ratio = 0; //0 is pure physical, 1 is pure tech (projectile)
 	private double baseDmg = 0; //base value for attacking/defending
 	private iType type = null;
 	private String name = "";
 	private double weight = 0.0;
 	private double health = 0.0;
 	private boolean attack = false; //whether it is an attack or defense item
 	private ArrayList<Effect> effects = null; //does item pass on any additional effects
 	private AttackPattern pattern = AttackPattern.POINT; // how does this item attack, defaults to a point
 	private AttackType atype = AttackType.PHYS; // defaults to physical attack
 	private int attackSize = 2; // defaults to attacking 2 spacing (except for point attacks)
 	private boolean equipped = false;
 	
 	private String imgname = "";
 	Image looks = null;
 	int tileSize = 32;
 //	private double wearDmg = 0.0;
 
 	
 	public Item() {
 		this.name = new Language().generate();
 		iType types[] = iType.values();
 		this.type = types[(int) (Math.random()*iType.TOTAL.ordinal())];
 		//generate random elements for consists and repairs
 		this.consists = new Element[(int) Math.max((1.0/(Math.random()+0.1)),1)]; //create a new element array with random number  of slots, weighted toward fewer slots (having at least 1)
 		for(int i=0; i<this.consists.length; i++) {
 			this.consists[i] = UET.getUET().getElementList().get((int) (Math.min((UET.TOTAL)*Math.random(),UET.TOTAL-1)));
 		}
 		this.repairs = new Element[(int) Math.max((1.0/(Math.random()+0.1)),1)]; //create a new element array with random number  of slots, weighted toward fewer slots (having at least 1)
 		for(int i=0; i<this.repairs.length; i++) {
 			this.repairs[i] = UET.getUET().getElementList().get((int) (Math.min((UET.TOTAL)*Math.random(),UET.TOTAL-1)));
 		}
 		this.level = (int) (10*Math.random());
 		if(this.type == iType.HAND) {
 			this.attack = (Math.random() < .85); // 85% chance of being weapon (as opposed to shield)
 			this.hands = (int) Math.max((4*Math.random()),1); //generate random number of hands (at least 1 hand though)
 			this.baseDmg = ((this.level/(Math.random()+.3)))/100.0*this.hands; //generate random damage (assuming more hands means a more powerful weapon)
 		}
 		else {
 			this.attack = false;
 			this.hands = 0;
 			this.baseDmg = this.level/(Math.random()+.1); //generate random damage
 		}
 		this.techRequired = (int) (100*Math.random()); //generate tech level required
 		this.phys_tech_ratio = Math.random(); //generate random ratio of physical and tech
 		this.weight = 1/(.7*Math.random()+.2);
 		this.health = 5/(Math.random()+0.01);
 		this.effects = new ArrayList<Effect>(0);
 		while(Math.random() < .7) {
 			this.effects.add(new Effect());
 		}
 		AttackPattern ap[] = AttackPattern.values();
 		this.pattern = ap[(int) (AttackPattern.CIRCLE.ordinal()*Math.random())];
 		AttackType at[] = AttackType.values();
 		this.atype = at[(int) (AttackType.NONE.ordinal()*Math.random())];
 		this.attackSize = (int) Math.max((1/(Math.random()+.1)),1);
 		
 		this.imgname = generateImgname();
 	}
 	
 	public Item(Element[] consists, Element[] repairs, int hands, int techRequired, double phys_tech_ratio, double baseDmg, iType type, double weight) {
 		if(consists != null) {
 			this.consists = consists;
 		}
 		else this.consists = new Element[0];
 		if(repairs != null) {
 			this.repairs = repairs;
 		}
 		else this.repairs = new Element[0];
 		this.hands = hands;
 		this.techRequired = techRequired;
 		this.phys_tech_ratio = phys_tech_ratio;
 		this.baseDmg = baseDmg;
 		this.type = type;
 		this.weight = weight;
 		this.effects = new ArrayList<Effect>();
 		while(Math.random() < .3) this.effects.add(new Effect());
 	}
 	
 	public Element[] getConsists() {
 		return this.consists;
 	}
 	
 	public Element[] getRepairs() {
 		return this.repairs;
 	}
 	
 	public int getHands() {
 		return this.hands;
 	}
 	
 	public int getTechRequired() {
 		return this.techRequired;
 	}
 	
 	public double getPhysTechRatio() {
 		return this.phys_tech_ratio;
 	}
 	
 	public double getBaseDmg() {
 		return this.baseDmg;
 	}
 
 	public static Item unarmed(Creature c) {
 		Item i = new Item(c.getConsists(), null, 1, 0, 0.0,.01,iType.HAND,0);
 		i.attack = true;
 		i.attackSize = 1;
 		i.atype = AttackType.PHYS;
 		i.health = 9999999; //your hands shouldn't break
 		i.name = "Bare Hand";
 		i.pattern = AttackPattern.POINT;
 		i.effects = new ArrayList<Effect>(); //no effects
 		return i;
 	}
 	
 	public static Item healthPotion(double strength) {
 		Item i = new Item(null, null,0,0,0.0,0,iType.HAND,.1);
 		i.attack = false;
 		i.effects = new ArrayList<Effect>();
 		i.effects.add(new Effect(cStats.STAM_HEALTH,sVal.CURRENT,strength,1,true));
 		return i;
 	}
 	
 	public static Item offHand(Item i) {
 		Item newItem = new Item(null, null, 0,0,0.0,0,i.getType(),0);
 		String temp[] = i.getName().split("[()<>]");
 		newItem.name = temp[0];
 		newItem.attack = false;
 		newItem.effects = new ArrayList<Effect>(0);
 		return newItem;
 	}
 
 	public iType getType() {
 		return this.type;
 	}
 	
 	public String getName(){
 		String str = this.name;
 		if(this.hands > 1) str += "(" + this.hands + " hands)";
 		if(!this.equipped)	return str;
 		if(this.type == iType.HAND) {
 			return str+ "<WIELDING>";
 		}
 		return str+ "<" + this.type + ">";
 	}
 	
 	public  double getWeight() {
 		return this.weight;
 	}
 	
 	public double getHealth() {
 		return this.health;
 	}
 	
 	public AttackPattern getAttackPattern() {
 		return this.pattern;
 	}
 	
 	public int getAttackSize() {
 		return this.attackSize;
 	}
 	
 	public boolean canAttack() {//returns whether it is weapon or defensive item
 		return this.attack;
 	}
 	
 	public boolean canEquip(int techLevel) {
 		return (techLevel >= this.techRequired);
 	}
 	
 	public ArrayList<Effect> getEffects() {
 		return this.effects;
 	}
 	
 	public double getPhysTech() {
 		return this.phys_tech_ratio;
 	}
 	
 	private String generateImgname(){
 		String str;
 		if(type!=iType.HAND){
 			str = type.toString();
 		}
 		else{
 			str = atype.toString();
 		}
 		return str;
 	}
 	
 ////////////////////////////////Stuff for attacking////////////////////////////////////	
 	public Attack attack(int x, int y, AttackDirection ad, Creature c) {
 		int phys = c.getEffective(cStats.STR_PHYS_ATTACK, sVal.CURRENT);
 		int tech = c.getEffective(cStats.TECH_WEAPON, sVal.CURRENT);
 		double damage = this.baseDmg*((1-phys_tech_ratio)*phys + phys_tech_ratio*tech);
 		Attack a = new Attack(x,y,damage,this,c,ad);
 		return a;
 	}
 	
 	public AttackType getAttackType() {
 		return this.atype;
 	}
 	
 	public void takeAttack(Attack a,double atStr) {
 		double dmg = 0, count = 0;
 		for(Element ae : a.getWeapon().getConsists()) {
 			for(Element me : this.consists) {
 				dmg += atStr*UET.getUET().getDmg(ae, me);
 				count++;
 			}
 		}
 		dmg /= count;
 		this.health -= dmg;
 		if(this.health < 0) this.health = 0;
 		
 		if(a.getWeapon().getAttackType() == AttackType.PHYS){
 			ArrayList<Element> elements = new ArrayList<Element>(0);
 			for(Element e : this.consists) {
 				elements.add(e);
 			}
 			AttackResults ar = new AttackResults(this.effects, this, dmg);
 			a.getWeapon().takeAttackResults(ar); 
 		}
 	}
 	
 	public void takeAttackResults(AttackResults ar) {
 		if(ar != null) {//takes reflected damaged
 			double dmg = 0, count = 0, atStr = ar.getAttackStrength();
 			for(Element ae : ar.getConsists()) {
 				for(Element me : this.consists) {
					if(ae!=null&&me!=null) //you get the picture
 					dmg += atStr*UET.getUET().getDmg(ae, me);
 					count++;
 				}
 			}
 			dmg /= count;
 			this.health -= dmg;
 			if(this.health < 0) this.health = 0;
 			
 		}
 		else {//need to figure out how much firing damage it should take
 			//damage should be proportional to damage dealt, but inversely to skill (know how to take care of weapon)
 		}
 	}
 	
 	public void equip() {
 		this.equipped = true;
 	}
 	
 	public void unequip() {
 		this.equipped = false;
 	}
 	
 	public String toString() {
 		String str = getName();
 		String temp[] = str.split("[<>]");
 		str = temp[0];
 		str+= "\nThis is a ";
 		if(type==iType.HAND){
 			str+=atype.toString().toLowerCase()+" ";
 		}
 		str+= type.toString().toLowerCase();
 		str += "\nBase strength : " + (int)this.baseDmg + "\nWeight : " + (int)this.weight + "\nItem health : " + (int)this.health;
 		if(this.effects != null) {
 			for(int i=0; i<this.effects.size(); i++) {
 				str += "\n[" + this.effects.get(i).toString() + "]";
 			}
 		}
 		return str;
 	}
 	
 	public void draw(int xPos, int yPos){
 		if (looks == null) {
 			looks = ImageUtil.getImage(this.imgname);
 		}
 		looks.draw((xPos)*tileSize, (yPos)*tileSize);
 	}
 
 }
