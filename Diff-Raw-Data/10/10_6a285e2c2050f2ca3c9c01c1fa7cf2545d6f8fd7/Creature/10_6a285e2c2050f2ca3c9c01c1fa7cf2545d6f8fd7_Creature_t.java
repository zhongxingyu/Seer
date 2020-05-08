 package orig;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 
 import orig.Attack.AttackDirection;
 import orig.Item.AttackType;
 
 import orig.Item.iType;
 
 
 public class Creature implements Serializable {
 	public enum cStats {
 		STR_PHYS_ATTACK, SPEED_MOVE, SPEED_ATTACK, DETECT_SIGHT, 
 		STEALTH_SIGHT, TECH_WEAPON, TECH_ARMOR, STAM_HEALTH, TOTAL;
 		
 		public String toString() {
 			return this.name();
 		}
 	}
 	
 	public enum bStats {
 		STRENGTH,SPEED,DETECTION,STEALTH,TECH,STAMINA,TOTAL;
 		
 		public String toString() {
 			return this.name();
 		}
 	}
 	
 	public enum sVal {
 		LEVEL,XP,MAX,CURRENT,TOTAL;
 		
 		public String toString() {
 			return this.name();
 		}
 	}
 
 	private String name; 
 	private String rName;
 
 	//from Race
 	//private String name;
 //	private int diet;  //How it eats, not what it eats
 //	private Element consumes; //what it eats
 //	private Element produces; //what it poops
 	private Element casing; //the flesh
 	private Element fluid; //the blood
 	private Element organs;
 //	private boolean genders; //true if the Race is not a hermaphodite
 	private int numArms;
     private int numLegs;
     private int raceKey;
 	
 	private int[][] cStat;
 	private int[][] bStat;
 	
 	private ArrayList<Item> inventory;
 	private Item[] equipped; // which items (non-hand)
 	private Item[] weilding; // which items (hands)
 	private int MaxInventory = 10;
 	private int availHands;
 	private double weight;
 	private ArrayList<Effect> effects;
 	private ArrayList<String> friendly;
 	
 	
 	// behind the scenes
 	final double base_creat_ratio = .2; //race stats * .2 + creat stats = effective
 	final double mul = 10;
 	final double rate = 1.15;
 	final int slots = 3;
 	final int lvlGain = 1;
 	private int maxLvl = 100;
 	private int baseGain;
 	private int initCap = 5*cStats.TOTAL.ordinal();
 	private Item unarmed; // = Item.unarmed(this);
 	
 
 	/*
 	public Creature(String name, String rName, int diet, Element consumes, Element produces, Element casing, Element fluid, Element organs, boolean genders, int numArms, int numLegs, int[][] cStat, int[][] bStat, ArrayList<Item> inventory, Item[] equipped, ArrayList<Item> weilding, int MaxInventory, int availHands, double weight, int initCap) {
 		this.name = name;
 		this.rName = rName;
 //		this.diet = diet;
 //		this.consumes = consumes;
 //		this.produces = produces;
 		this.casing = casing;
 		this.fluid = fluid;
 		this.organs = organs;
 //		this.genders = genders;
 		this.numArms = numArms;
 		this.cStat = new int[cStats.TOTAL.ordinal()][sVal.TOTAL.ordinal()];
 		for(int i=0; i<cStats.TOTAL.ordinal(); i++) {
 			for(int j=0; j<sVal.TOTAL.ordinal(); j++) {
 				this.cStat[i][j] = cStat[i][j];
 			}
 		}
 		this.bStat = new int[bStats.TOTAL.ordinal()][sVal.TOTAL.ordinal()];
 		for(int i=0; i<bStats.TOTAL.ordinal(); i++) {
 			for(int j=0; j<sVal.TOTAL.ordinal(); j++) {
 				this.bStat[i][j] = bStat[i][j];
 			}
 		}
 		if(inventory != null) this.inventory = inventory;
 		else this.inventory = new ArrayList<Item>(0);
 		if(equipped != null) this.equipped = equipped;
 		else this.equipped = new Item[slots];
 		if(weilding != null) this.weilding = weilding;
 		else this.weilding = new ArrayList<Item>(0);
 		this.MaxInventory = MaxInventory;
 		if(availHands <= numArms && availHands >= 0) this.availHands = availHands;
 		else availHands = numArms;
 		this.weight = weight;
 		this.initCap = initCap;
 	}
 	*/
 	public Creature (Race r) {
 		this(r, r.getL().generate());
 	}
 	
 	public Creature(Race r, String pName) {
 		cStats[] c = cStats.values();
 		bStats[] b = bStats.values();
 		sVal[] v = sVal.values();
 		
 		this.name = pName;
 		this.rName = r.getName();
 		this.casing = r.getCasing();
 		this.fluid = r.getFluid();
 		this.organs = r.getOrgans();
 		this.numArms = r.getNumArms();
 		this.numLegs = r.getNumLegs();
 		this.raceKey = r.getRaceKey();
 		this.cStat = new int[cStats.TOTAL.ordinal()][sVal.TOTAL.ordinal()];
 		for(int i=0; i<cStats.TOTAL.ordinal(); i++) {
 			for(int j=0; j<sVal.TOTAL.ordinal(); j++) {
 				this.cStat[i][j] = r.get(c[i],v[j]);
 			}
 		}
 		this.bStat = new int[bStats.TOTAL.ordinal()][sVal.TOTAL.ordinal()];
 		for(int i=0; i<bStats.TOTAL.ordinal(); i++) {
 			for(int j=0; j<sVal.TOTAL.ordinal(); j++) {
 				this.bStat[i][j] = r.get(b[i],v[j]);
 			}
 		}
 		
 		this.inventory = new ArrayList<Item>(0);
 		this.equipped = new Item[slots];
 		this.weilding = new Item[this.numArms];
 		this.MaxInventory += 100;
 		this.availHands = this.numArms;
 		this.friendly = new ArrayList<String>(r.getFriendly().size());
 		for(int i=0; i<r.getFriendly().size(); i++) {
 			this.friendly.add(r.getFriendly().get(i).getName());
 		}
 		this.effects = new ArrayList<Effect>(r.getEffects().size());
 		for(int i=0; i<r.getEffects().size();i++) {
 			this.effects.add(new Effect(r.getEffects().get(i)));
 		}
 		this.unarmed = Item.unarmed(this);
 	}
 	
 	public Element[] getConsists() {
 		Element consists[] = {this.casing, this.fluid, this.organs};
 		return consists;
 	}
 	
 	public int get(cStats s, sVal v) {
 		return cStat[s.ordinal()][v.ordinal()];//[n];
 	}
 	
 	public void set(cStats s,sVal v, int n) {
 		cStat[s.ordinal()][v.ordinal()] = n;
 		if(v == sVal.LEVEL) {
 			this.cStat[s.ordinal()][sVal.MAX.ordinal()] = lvlGain*this.cStat[s.ordinal()][sVal.LEVEL.ordinal()];
 			// current = max (unless temp effect has it above max already)
 			if(this.cStat[s.ordinal()][sVal.CURRENT.ordinal()] < this.cStat[s.ordinal()][sVal.MAX.ordinal()]) {
 				this.cStat[s.ordinal()][sVal.CURRENT.ordinal()] = this.cStat[s.ordinal()][sVal.MAX.ordinal()];
 			}
 		}
 
 	}
 
 	public int get(bStats s, sVal v) {
 		return bStat[s.ordinal()][v.ordinal()];//[n];
 	}
 	
 	public void set(bStats s,sVal v, int n) {
 		bStat[s.ordinal()][v.ordinal()] = n;
 		if(v == sVal.LEVEL) {
 			this.bStat[s.ordinal()][sVal.MAX.ordinal()] = lvlGain*this.bStat[s.ordinal()][sVal.LEVEL.ordinal()];
 			// current = max (unless temp effect has it above max already)
 			if(this.bStat[s.ordinal()][sVal.CURRENT.ordinal()] < this.bStat[s.ordinal()][sVal.MAX.ordinal()]) {
 				this.bStat[s.ordinal()][sVal.CURRENT.ordinal()] = this.bStat[s.ordinal()][sVal.MAX.ordinal()];
 			}
 		}
 	}	
 	
 	public String getName() {
 		return this.name;
 	}
 	
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public int getRaceKey(){
 		return this.raceKey;
 	}
 	
 	public String getRName() {
 		return this.rName;
 	}
 	
 	public void setRName(String rName) {
 		this.rName = rName;
 	}
 	
 	public int getNumArms() {
 		return this.numArms;
 	}
 	
 	public int getNumLegs(){
 		return numLegs;
 	}
 	
 	public void setNumArms(int n) {
 		this.numArms = n;
 	}
 	
 	public int getEffective(cStats s, sVal v) {
 		//returns effective value for stat (useful for current/max)
 		bStats b = decode(s);
 		int eff = (int) (base_creat_ratio*bStat[decode(s).ordinal()][v.ordinal()] + cStat[s.ordinal()][v.ordinal()]);
 		if(this.effects != null) {
 			for(int i=0; i<effects.size(); i++) {
 				if(!effects.get(i).isBase()) {
 					if((effects.get(i).getCStat() == s) && (effects.get(i).getSVal() == v)) {  
 						eff += effects.get(i).getValue();
 					}
 				}
 				else {
 					if((effects.get(i).getBStat() == b) && (effects.get(i).getSVal() == v)) {
 						eff += effects.get(i).getValue();
 					}
 				}
 			}			
 		}
 		//if over encumbered, everything is less effective
 		//if((s == cStats.SPEED_MOVE) && ((v == sVal.CURRENT) || (v == sVal.MAX) )){
 			eff *= (1-Math.max(0,Math.min(1,(this.weight-this.MaxInventory)/(this.MaxInventory))));
 		//}
 		return eff;
 	}
 	
 //Decrement and remove Effects
 	public void update() {
 		Effect e;
 		for(int i=0; i<effects.size(); i++) {
 			e = effects.get(i);
 			double value = e.getValue(), dmg = e.getValue();
 			if(e.isElemental()&&e!=null) {
 				dmg = value*UET.getUET().getDmg(e.getElement(),this.casing);
 				dmg += value*UET.getUET().getDmg(e.getElement(),this.fluid);
 				dmg += value*UET.getUET().getDmg(e.getElement(),this.organs);
 			}
 			if(e.isTemp()) {
 				//do nothing, just leave in effects and it modifies effective value
 			}
 			else {
 				if(e.isBase()) {
 					this.bStat[e.getBStat().ordinal()][e.getSVal().ordinal()] += dmg;
 				}
 				else {
 					this.cStat[e.getCStat().ordinal()][e.getSVal().ordinal()] += dmg;
 				}
 			}
 			//decrements steps remaining
 			effects.get(i).update();
 			//removes expired effects and stays at current count (effect was deleted, therefore everything shifted)
 			if(effects.get(i).getSteps() <= 0) {
 				effects.remove(i);
 				i--;
 			}
 		}
 	}
 	
 //Random Leveling//////////////////////////	
 	public void randomInitXP() {
 		int total = 0, xp, min = cStat[0][sVal.LEVEL.ordinal()], max = cStat[0][sVal.LEVEL.ordinal()], stat;
 		cStats c[] = cStats.values();
 		for(int i = 0; i < cStats.TOTAL.ordinal(); i++) {
 			total += cStat[i][sVal.LEVEL.ordinal()]; 
 			if(min > cStat[i][sVal.LEVEL.ordinal()]) {
 				min = cStat[i][sVal.LEVEL.ordinal()];
 			}
 			if(max < cStat[i][sVal.LEVEL.ordinal()]) {
 				max = cStat[i][sVal.LEVEL.ordinal()];
 			}
 		}
 		while(total < initCap) {
 			xp = (int) (mul*Math.pow((min+max)/2+1,rate)*Math.random());// can't level up by anymore than 1 level (lowest level)
 			stat = (int) (cStats.TOTAL.ordinal()*Math.random());
 			gain(c[stat],sVal.XP,xp);
 			total = 0;
 			for(int i = 0; i < cStats.TOTAL.ordinal(); i++) {
 				min = cStat[i][sVal.LEVEL.ordinal()];
 				total += cStat[i][sVal.LEVEL.ordinal()]; 
 				if(min > cStat[i][sVal.LEVEL.ordinal()]) {
 					min = cStat[i][sVal.LEVEL.ordinal()];
 				}
 				if(max < cStat[i][sVal.LEVEL.ordinal()]) {
 					max = cStat[i][sVal.LEVEL.ordinal()];
 				}
 			}
 		}
 	}
 	
 	public void randomInitPoints() {
 		int total = 0, stat;
 		cStats c[] = cStats.values();
 		for(int i = 0; i < cStats.TOTAL.ordinal(); i++) {
 			total += cStat[i][sVal.LEVEL.ordinal()]; // = cStats.values()[i].ordinal();
 		}
 		for(int i = total; i <= initCap; i++) {
 			stat = (int) (cStats.TOTAL.ordinal()*Math.random());
 			gain(c[stat],sVal.LEVEL,1);
 		}
 	}
 
 	public void autoLvlPointsBy(int levels) {
 		int stat;
 		cStats c[] = cStats.values();
 		for(int i = 0; i <= levels; i++) {
 			stat = (int) (cStats.TOTAL.ordinal()*Math.random());
 			gain(c[stat],sVal.LEVEL,1);
 		}
 	}
 
 	public void autoLvlPointsTo(int levels) {
 		int total = 0, stat;
 		cStats c[] = cStats.values();
 		for(int i = 0; i < cStats.TOTAL.ordinal(); i++) {
 			total += cStat[i][sVal.LEVEL.ordinal()]; 
 		}
 		for(int i = total; i <= levels; i++) {
 			stat = (int) (cStats.TOTAL.ordinal()*Math.random());
 			gain(c[stat],sVal.LEVEL,1);
 		}
 	}
 
 	public void autoLvlXP(int xp) {
 		// randomly allocating chunks of xp/100 
 		int div = 100;
 		int total = 0, stat, givenXP = 0;
 		cStats c[] = cStats.values();
 		while(total < xp) {
 			givenXP = (int) ((10*Math.random())*div*(Math.random()));// can't level up by anymore than 1 level (lowest level)
 			if(givenXP > (xp - total)) givenXP = (xp-total);
 			
 			stat = (int) (cStats.TOTAL.ordinal()*Math.random());
 			if(givenXP < (xp - total)) {
 				gain(c[stat],sVal.XP,givenXP);
 				total += givenXP;
 			}
 			else {
 				gain(c[stat],sVal.XP,xp-total);
 				total = xp;
 			}
 		}
 	}
 
 //Gaining stats/leveling up///////////////////////////////////////////////
 	//Gain/lose health,energy,stats,XP etc
 	public int gain(cStats s, sVal v, int n) {
 		if(v == sVal.XP) { 
 			bStats b = decode(s);
 			gain(b, v, n/baseGain); //Base stats gain XP in corresponding stat, divided by number of cstats that map to it
 			this.cStat[s.ordinal()][v.ordinal()] += n;
 		}
 		else {
 			this.cStat[s.ordinal()][v.ordinal()] += n;
 			if(v == sVal.LEVEL) {
 				if(cStat[s.ordinal()][v.ordinal()] > maxLvl) cStat[s.ordinal()][sVal.LEVEL.ordinal()] = maxLvl;
 				
 				this.cStat[s.ordinal()][sVal.MAX.ordinal()] = lvlGain*this.cStat[s.ordinal()][sVal.LEVEL.ordinal()];
 				// current = max (unless temp effect has it above max already)
 				if(this.cStat[s.ordinal()][sVal.CURRENT.ordinal()] < this.cStat[s.ordinal()][sVal.MAX.ordinal()]) {
 					this.cStat[s.ordinal()][sVal.CURRENT.ordinal()] = this.cStat[s.ordinal()][sVal.MAX.ordinal()];
 				}	
 			}
 		}
 		return checkXP(s);
 	}
 
 	//Same as above but for base stats
 	private void gain(bStats s, sVal v, int n) {
 		if(v == sVal.XP) { 
 			this.bStat[s.ordinal()][v.ordinal()] += n;
 		}
 		else {
 			this.bStat[s.ordinal()][v.ordinal()] += n;
 			if(v == sVal.LEVEL) {
 				if(bStat[s.ordinal()][v.ordinal()] > maxLvl) bStat[s.ordinal()][sVal.LEVEL.ordinal()] = maxLvl;
 				
 				this.bStat[s.ordinal()][sVal.MAX.ordinal()] = lvlGain*this.bStat[s.ordinal()][sVal.LEVEL.ordinal()];
 				// current = max (unless temp effect has it above max already)
 				if(this.bStat[s.ordinal()][sVal.CURRENT.ordinal()] < this.bStat[s.ordinal()][sVal.MAX.ordinal()]) {
 					this.bStat[s.ordinal()][sVal.CURRENT.ordinal()] = this.bStat[s.ordinal()][sVal.MAX.ordinal()];
 				}	
 			}
 
 		}
 		checkXP(s);
 	}
 	
 	//If XP >= mul*(lvl)^rate, then lvlUP
 	private int checkXP(cStats s) {
 		int temp = 0;
 		//while XP >= max for level, level up
 		if(cStat[s.ordinal()][sVal.LEVEL.ordinal()] >= maxLvl) {
 			cStat[s.ordinal()][sVal.LEVEL.ordinal()] = maxLvl;
 			temp = cStat[s.ordinal()][sVal.XP.ordinal()];
 			cStat[s.ordinal()][sVal.XP.ordinal()] = 0;
 			return temp;
 		}
 		while( (this.cStat[s.ordinal()][sVal.XP.ordinal()]) >= ((int) mul*Math.pow(this.cStat[s.ordinal()][sVal.LEVEL.ordinal()],rate)) ){
 			if(cStat[s.ordinal()][sVal.LEVEL.ordinal()] >= maxLvl) { //it is about to level up
 				cStat[s.ordinal()][sVal.LEVEL.ordinal()] = maxLvl;
 				temp = cStat[s.ordinal()][sVal.XP.ordinal()];
 				cStat[s.ordinal()][sVal.XP.ordinal()] = 0;
 				return temp;
 			}
 			else {
 				lvlUp(s);
 			}
 		}
 		return temp;
 	}
 
 	//If XP >= mul*(lvl)^rate, then lvlUP
 	private void checkXP(bStats s) {
 		//while XP >= max for level, level up
 		while( this.bStat[s.ordinal()][sVal.XP.ordinal()] >= ((int) mul*Math.pow(this.bStat[s.ordinal()][sVal.LEVEL.ordinal()],rate)) ){
 			if(bStat[s.ordinal()][sVal.LEVEL.ordinal()] >= maxLvl) { //it is about to level up past maxLvl
 				bStat[s.ordinal()][sVal.LEVEL.ordinal()] = maxLvl;
 				bStat[s.ordinal()][sVal.XP.ordinal()] = 0;
 			}
 			else {
 				lvlUp(s);
 			}
 		}
 	}
 	
 	private void lvlUp(cStats s) {
 		//Incrementlevel and decrement XP by XP cap for level;
 		this.cStat[s.ordinal()][sVal.XP.ordinal()] -= ((int) mul*Math.pow(this.cStat[s.ordinal()][sVal.LEVEL.ordinal()],rate));
 		this.cStat[s.ordinal()][sVal.LEVEL.ordinal()]++;
 		this.cStat[s.ordinal()][sVal.MAX.ordinal()] += lvlGain;
 		// current = max (unless temp effect has it above max already)
 		if(this.cStat[s.ordinal()][sVal.CURRENT.ordinal()] < this.cStat[s.ordinal()][sVal.MAX.ordinal()]) {
 			this.cStat[s.ordinal()][sVal.CURRENT.ordinal()] = this.cStat[s.ordinal()][sVal.MAX.ordinal()];
 		}
 	}
 
 	private void lvlUp(bStats s) {
 		//Incrementlevel and decrement XP by XP cap for level;
 		this.bStat[s.ordinal()][sVal.XP.ordinal()] -= ((int) mul*Math.pow(this.bStat[s.ordinal()][sVal.LEVEL.ordinal()],rate));
 		this.bStat[s.ordinal()][sVal.LEVEL.ordinal()]++;
 		this.bStat[s.ordinal()][sVal.MAX.ordinal()] += lvlGain;
 		// current = max (unless temp effect has it above max already)
 		if(this.bStat[s.ordinal()][sVal.CURRENT.ordinal()] < this.bStat[s.ordinal()][sVal.MAX.ordinal()]) {
 			this.bStat[s.ordinal()][sVal.CURRENT.ordinal()] = this.bStat[s.ordinal()][sVal.MAX.ordinal()];
 		}
 	}
 	
 //Decode cStats into the corresponding bStats //////////////////////////////////
 	public bStats decode(cStats s) {
 		String temp[], str[] = s.toString().split("_");
 		bStats names[] = bStats.values();
 		cStats cnames[] = cStats.values();
 		this.baseGain = 0;
 		int rNum = -1;
 		for(int i=0; i<bStats.TOTAL.ordinal(); i++) {
 			//if(str[0].compareTo(names[i].toString()) == 0) {
 			if((str[0].substring(0, 3)).compareTo((names[i].name().substring(0, 3))) == 0) {
 				rNum = i;
 				for(int j=0; j<cStats.TOTAL.ordinal(); j++) {
 					temp = cnames[j].toString().split("_");
 					//compares the first 3 characters between 
 					if(str[0].substring(0, 3).compareTo(temp[0].substring(0, 3)) == 0) {
 						this.baseGain++;
 					}
 				}
 			}
 		}
 		//System.out.println(str[0]);
 		if( (rNum > bStats.TOTAL.ordinal()) || (rNum < 0)) {
 			return null;
 		}
 		return names[rNum];
 	}
 	
 	public int getBaseGain() {
 		return this.baseGain;
 	}
 
 //Dealing with attacking///////////////////////////////////////////////////////////
 	public boolean isFriendly(Race r) {
 		for(int i=0; i<this.friendly.size();i++) {
 			if(this.friendly.get(i).equalsIgnoreCase(r.getName())) return true;
 		}
 		return false;
 	}
 
 	public boolean isFriendly(Creature c) {
 		for(int i=0; i<this.friendly.size();i++) {
 			if(this.friendly.get(i).equalsIgnoreCase(c.getRName())) return true;
 		}
 		return false;
 	}
 
 	public boolean isFriendly(String str) {
 		for(int i=0; i<this.friendly.size();i++) {
 			if(this.friendly.get(i).equalsIgnoreCase(str)) return true;
 		}
 		return false;
 	}
 	
 	public void addFriendly(Race r) {
 		for(int i=0; i<this.friendly.size();i++) {
 			if(this.friendly.get(i).equalsIgnoreCase(r.getName())) return;
 		}
 		this.friendly.add(r.getName());
 	}
 	
 	public void addFriendly(Creature c) {
 		for(int i=0; i<this.friendly.size();i++) {
 			if(this.friendly.get(i).equalsIgnoreCase(c.getRName())) return;
 		}
 		this.friendly.add(c.getRName());
 	}
 	
 	public ArrayList<String> getFriendly() {
 		return this.friendly;
 	}
 
 	
 ////////////////////////////// Attacking ////////////////////////////////////////////////////////////////////
 	//TODO: relabel this as a melee attack only, and somehow let the creature be able to know
 	// what styles of attack it can do, so that if it has a ranged attack, it tries to attack
 	// from a range instead of just running towards an enemy
 	//TODO: link this with OnScreenChar, so that functions called to OSC ask the base creature to do the methods
 	public ArrayList<Attack> attack(int x, int y, AttackDirection ad) {//the attacking starts here
 		ArrayList<Attack> att = new ArrayList<Attack>(0);
 		for(int i=0; i<this.getNumArms(); i++) {
 			if(this.weilding[i] != null && i<this.weilding.length)	att.add(this.weilding[i].attack(x, y, ad, this));
 			else {
 				att.add(unarmed.attack(x, y, ad, this));
 			}
 		}
 		return att;
 	}
 	
 	public void takeAttack(Attack a) {
 		//get resistances from univeral reaction table
 		double atStr = a.getAttackStrength();
 		ArrayList<Effect> attEff;
 		for(int i=0; i<this.equipped.length; i++) {
 			if(this.equipped[i] != null) {
 				this.equipped[i].takeAttack(a,atStr);
 				atStr -= this.equipped[i].getAttackSize();
 			}
 			
 			/*
 			attEff = new ArrayList<Effect>(0);
 			for(int j=0; j<this.equipped[i].getEffects().size(); j++){
 				if(this.equipped[i].getEffects().get(j).isAttack()) {
 					attEff.add(this.equipped[i].getEffects().get(j));
 				}
 			}
 			a.getWeapon().takeAttackResults(new AttackResults(attEff, this.equipped[i],fullStr));
 			attEff = null;
 			*/
 		}
 		if(atStr > 0) {
 			double dmg = 0;
 			for(int i=0; i<a.getWeapon().getConsists().length; i++) {
 				dmg += atStr*UET.getUET().getDmg(a.getWeapon().getConsists()[i],this.casing);
 				dmg += atStr*UET.getUET().getDmg(a.getWeapon().getConsists()[i],this.fluid);
 				dmg += atStr*UET.getUET().getDmg(a.getWeapon().getConsists()[i],this.organs);
 			}
 			dmg /= a.getWeapon().getConsists().length*3;
 			dmg = Math.min(dmg, this.cStat[cStats.STAM_HEALTH.ordinal()][sVal.CURRENT.ordinal()]);
 			this.cStat[cStats.STAM_HEALTH.ordinal()][sVal.CURRENT.ordinal()] -= dmg;
			/*System.out.printf("Current health of %s: %d/%d\n", this.name,
					this.cStat[cStats.STAM_HEALTH.ordinal()][sVal.CURRENT.ordinal()],
					this.cStat[cStats.STAM_HEALTH.ordinal()][sVal.MAX.ordinal()]);*/
 			attEff = new ArrayList<Effect>(0);
 			for(int i=0; i<this.effects.size(); i++) {
 				if(this.effects.get(i).isAttack()) {
 					attEff.add(this.effects.get(i));
 				}
 			}
 			//adds XP to strength and tech (proportional to weapon)
 			attEff.add(new Effect(cStats.STR_PHYS_ATTACK,sVal.XP,(1.0-a.getWeapon().getPhysTech())*dmg,true));
 			attEff.add(new Effect(cStats.TECH_WEAPON,sVal.XP,(1.0-a.getWeapon().getPhysTech())*dmg,true));
 			if(a.getWeapon().getType() == iType.HAND && a.getWeapon().getAttackType() == AttackType.PHYS) {
 				AttackResults ar = new AttackResults(attEff,null,dmg);
 				a.getAttacker().takeAttackResults(ar);
 			}
 			
 		}
 		//apply effects
 		for(int i=0; i<a.getEffects().size(); i++) {
 			attackEffect(a.getEffects().get(i));
 		}
 	}
 	
 	public void takeAttackResults(AttackResults ar) {
 		//get resistances from univeral reaction table
 		//double atStr = ar.getAttackStrength();
 		for(Effect e: ar.getEffects()) {
 			attackEffect(e);
 		}
 	}
 
 	
 	public boolean isDead() {
 		return (getEffective(cStats.STAM_HEALTH,sVal.CURRENT) <= 0);
 	}
 	
 	public void attackEffect(Effect e) {
 		if(e != null && e.isAttack()) { //if it is an attack
 			effects.add(new Effect(e));
 		}
 	}
 	
 	public void defenseEffect(Effect e) {
 		if(e != null && !e.isAttack()) { //if it is an attack 
 			effects.add(new Effect(e));
 		}
 		
 	}
 	
 //Dealing with inventory
 	/*
 	public boolean isEquipped(Item i) {
 		for(int n=0; n<equipped.length; n++) {
 			if(i == equipped[n]) return true;
 		}
 		for(int n=0; n<weilding.size(); n++) {
 			if(i == weilding.get(n)) return true;
 		}
 		return false;
 	}
 	*/
 	
 	
 	public boolean equip(Item i) {
 		//equip if has required tech and enough hands (if applicable)
 		if(i.getType() == iType.HAND) {
 			if(availHands >= i.getHands()) {
 			//if(availHands >= i.getHands() && getEffective(cStats.TECH_WEAPON,sVal.CURRENT) >= i.getTechRequired()) {
 				for(int j=0; j<weilding.length; j++) {
 					if(weilding[j] == null) {
 						weilding[j] = i;
 						//if i is a multiple hand weapon, store the offHand holder in next spots
 						for(int k=1; k<i.getHands(); k++) {
 							weilding[j+k] = Item.offHand(i);
 						}
 						availHands -= i.getHands();
 						i.equip();
 						return true;
 					}
 					else if(weilding[j] == i) {
 						return true;
 					}
 				}
 				return false;
 			}
 			else {
 				return false;
 			}
 		}
 		else {
 		//else if(getEffective(cStats.TECH_ARMOR,sVal.CURRENT) >= i.getTechRequired()){
 			if(equipped[i.getType().ordinal()] != null) equipped[i.getType().ordinal()].unequip();
 			equipped[i.getType().ordinal()] = i;
 			i.equip();
 			return true;
 		}
 		//return false;
 	}
 	
 	public boolean unequip(Item i) {
 		if(i.getType() == iType.HAND) {
 			for(int j=0; j<weilding.length; j++) {
 				if(weilding[j] == i) {
 					weilding[j] = null;
 					int hands = i.getHands();
 					availHands += hands;
 					i.unequip();
 					//remove offhand versions
 					for(int k=1; k<hands; k++) {
 						weilding[j+k] = null;
 					}
 					consolidateWeilding();
 					return true;
 				}
 			}
 			return false;
 		}
 		else {
 			equipped[i.getType().ordinal()] = null;
 			i.unequip();
 			return true;
 		}
 	}
 	
 	public boolean unequip(int n) {
 		if(n < 0) return false;
 		else if(n < this.slots){
 			if(this.equipped[n] != null) {
 				this.equipped[n].unequip();
 				this.equipped[n] = null;
 				return true;
 			}
 		}
 		else if(n<this.slots+this.weilding.length) {
 			if(this.weilding[n-this.slots] != null && this.weilding[n-this.slots].getHands() > 0) {
 				int hands = this.weilding[n-this.slots].getHands();
 				this.weilding[n-this.slots].unequip();
 				this.weilding[n-this.slots] = null;
 				//unequip all offhand items linked to this item
 				for(int j=1; j<hands; j++) {
 					this.weilding[n-this.slots + j] = null;
 				}
 				availHands += hands;
 				consolidateWeilding();
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void pickup(Item i) {
 		this.inventory.add(i);
 		weight += i.getWeight();
 	}
 	
 	public String getEquippedLess(int i){
 		String str = getEquipped(i);
 		String r = "";
 		for(int j=0;j<str.length();j++){
 			if(str.charAt(j)!='<'){
 				r+=str.charAt(j);
 			}
 			else break;
 		}
 		return r;
 	}
 	
 	public void pickAndEquip(Item i){
 		pickup(i);
 		equip(i);
 	}
 	
 	public Item drop(Item i) {
 		this.inventory.remove(i);
 		unequip(i);
 		weight -= i.getWeight();
 		return i;
 	}
 	
 	public ArrayList<Item> getInventory() {
 		return this.inventory;
 	}
 
 	public String getEquipped(int i){
 		if(i>=(this.equipped.length+this.weilding.length) || i < 0) return "None";
 		else if(i < this.equipped.length) {
 			if(equipped[i] == null) return "None";			
 			return equipped[i].getName();
 		}
 		else {
 			if(weilding[i-this.equipped.length] == null) return "None";
 			return this.weilding[i-this.equipped.length].getName();
 		}
 	}
 	
 	public Item getEquippedFull(int i){
 		if(i>=(this.equipped.length+this.weilding.length) || i < 0) return null;
 		else if(i < this.equipped.length) {
 			if(equipped[i] == null) return null;			
 			return equipped[i];
 		}
 		else {
 			if(weilding[i-this.equipped.length] == null) return null;
 			return this.weilding[i-this.equipped.length];
 		}
 	}
 	
 	private void consolidateWeilding() {
 		int offset = 0;
 		for(int i=0; i<weilding.length; i++) {
 			if(this.weilding[i] == null) offset++;
 			else if(offset > 0) {
 				this.weilding[i-offset] = this.weilding[i];
 				this.weilding[i] = null;
 			}
 		}
 	}
 	/*
 	public String getEquipped(iType i, int n) {
 		if(i != iType.HAND) {
 			if(i == iType.TOTAL) return "Don't pass TOTAL to this";
 			
 			if(equipped[i.ordinal()] == null) return "None";
 			else return equipped[i.ordinal()].getName();
 		}
 		else {
 			return getEquipped(n); //get weapons
 		}
 	}
 	*/
 /*
 	public String getItemName(int n) {
 		if(n < 0 || n >= this.inventory.size()) return "None";
 		if(isEquipped(this.inventory.get(n))) return this.inventory.get(n).getName() + "<E>";
 		return this.inventory.get(n).getName();
 	}
 	*/
 	
 	public boolean detects(int myX, int myY, int x, int y, Creature c) {
 		double distance = Math.sqrt(Math.pow((myX-x),2) + Math.pow((myY-y),2));
 		double stealthScore = this.getEffective(cStats.STEALTH_SIGHT,sVal.CURRENT);
 		double detectScore = c.getEffective(cStats.DETECT_SIGHT,sVal.CURRENT);
 		//stealth increases with distance 
 		// stealth and detect both get 50% of their value plus up to 50% (random)
 		if(stealthScore*Math.pow(distance, 1.25)*(.5+.5*Math.random()) > detectScore*(.5+.5*Math.random())) {
 			// not detected 
 			this.gain(cStats.STEALTH_SIGHT,sVal.XP,(int) Math.floor(detectScore/distance));
 			return false;
 		}
 		else {
 			//detected
 			c.gain(cStats.DETECT_SIGHT,sVal.XP,(int) Math.floor(stealthScore*distance));
 			return true;
 		}
 	}
 }
