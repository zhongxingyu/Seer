 package uk.ac.aber.dcs.cs221.monstermash.data;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Observable;
 import java.util.Random;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 import uk.ac.aber.dcs.cs221.monstermash.util.Name;
 
 /**
  * 
  * @author Jacob Smith, jas32
  *
  */
 public class Monster implements Comparable<Monster> {
 	public static final int MAX_NUM_CHILDREN = 10;
 	
 	private static volatile long nextPrimaryKey = 1;
 	private static volatile boolean isInit = false;
 	
 	private volatile long primaryKey;
 	
 	private volatile UserAccount owner;
 	
 	private volatile String name;
 	
 	private volatile boolean forTupping = false;
 	private volatile boolean forSale = false;
 	private volatile boolean isDead = false;
 	
 	private volatile Date dateOfBirth;
 	
 	protected volatile double ageRate;
 	protected volatile int strengthCoefficient;
 	protected volatile int evadeCoefficient;
 	protected volatile int toughnessCoefficient;
 	
 	protected volatile double fertility;
 	
 	
 	protected volatile int injuryChance;
 	
 	protected static Name nameGen;
 	
 	private volatile int injuries;
 	
 	public synchronized static void init(long nextPrimaryKey) {
 		Monster.nextPrimaryKey = nextPrimaryKey;
 		Monster.isInit = true;
 	}
 	
 	/**
 	 * Public constructor.
 	 * 
 	 * Assigns the next available primary key and initialises the date of
 	 * birth field to this instant.
 	 */
 	public Monster() {
 		synchronized (getClass() ) {
			//if (isInit == false) { throw new RuntimeException(); }
 			primaryKey = nextPrimaryKey++;
 			if (nameGen == null) { nameGen = new Name(); }
 		}
 		
 		dateOfBirth = new Date();
 		
 		injuries = 0;
 	}
 	/**
 	 * Construct an uninitialise Monster with a particular primaryKey.
 	 * @param primaryKey
 	 */
 	protected Monster(long primaryKey) {
 		this.primaryKey = primaryKey;
 		
 		synchronized(getClass() ) {
 			// Infer what the next PrimaryKey should be.
 			if (primaryKey >= nextPrimaryKey) {
 				nextPrimaryKey = primaryKey + 1;
				this.isInit = true;
 			} 
 			
 		}
 	}
 	
 	/**
 	 * 
 	 * @return This monster's primary key.
 	 */
 	public long getUID() { return primaryKey; }
 	
 	/**
 	 * Creates a reciprocal association between this monster and an owning UserAccount.
 	 * @param owner The monster's owner.
 	 * @return This monster object.
 	 */
 	public synchronized Monster setOwner(UserAccount owner) {
 		if (this.owner != null) { this.owner.removeMonster(this); }
 		this.owner = owner;
 		this.owner.addMonster(this);
 		return this;
 		
 	}
 	/**
 	 * 
 	 * @return The monster's owner.
 	 */
 	public UserAccount getOwner() { return owner; }
 	
 	/**
 	 * Customise a monster with a name.
 	 * @param name The monster's new name.
 	 * @return The monster object.
 	 */
 	public synchronized Monster setName(String name) { this.name = name; return this; }
 	/**
 	 * 
 	 * @return The monster's name.
 	 */
 	public String getName() { return name; }
 	
 	/**
 	 * Sets the monster's gender and immediately gives the monster a random
 	 * gender appropriate name. {@link #Gender}
 	 * @param gender The monster's gender.
 	 * @return This monster object.
 	 */
 	public synchronized Monster setName() {
 		Random prng = new Random(); 
 		this.setName((prng.nextBoolean() ) ? nameGen.male(prng): nameGen.female(prng) );
 		return this; 
 	}
 	/**
 	 * 
 	 * @return The monster's age in seconds.
 	 */
 	public long getAge() {
 		long instantOfBirth = dateOfBirth.getTime();
 		long currentInstant = new Date().getTime();
 		return currentInstant - instantOfBirth;
 	}
 	/**
 	 * 
 	 * @return The monster's health, less than 1. If less than 0 this indicates the monster should be reaped.
 	 */
 	public double getHealth() {
 		if (isDead) {
 			return -1;
 		}
 		return 2 - injuries*0.1 - Math.exp(ageRate * getAge() ); //$2-e^{\lambda t}$
 	}
 	/**
 	 * 
 	 * @return The monster's strength score.
 	 */
 	public int getStrength() {
 		return (int) (strengthCoefficient*(Math.exp(ageRate * getAge() ) - 1 ) * getHealth());
 	}
 	/**
 	 * 
 	 * @return The monster's chance at evading an incoming blow.
 	 */
 	public int getEvade() {
 		return (int) (evadeCoefficient*(Math.exp(ageRate * getAge() ) - 1) * getHealth() );
 	}
 	/**
 	 * 
 	 * @return The monster's toughness score.
 	 */
 	public int getToughness() {
 		return (int) (toughnessCoefficient*(Math.exp(ageRate * getAge() ) - 1) * getHealth() );
 	}
 	
 	/**
 	 * Mutation function implemented using a binomial distribution with number of trials, n=93 and p=1/2.
 	 * @param The PRNG state object.
 	 * @return A random number with a binomial distribution on the interval (0,1)
 	 */
 	protected static double mutation(java.util.Random rand) {
 		final int ITERATIONS = 3;
 		final int WORDLENGTH = 31;
 		
 		int count = 0;
 		
 		for (int i = 0; i < ITERATIONS; ++i) {
 			int stream = rand.nextInt();
 			for (int mask = 1; mask >0; mask *= 2) {
 				count += (stream & mask)!=0 ? 1: 0; 
 			}
 		}
 		return (double)(count) / (WORDLENGTH*ITERATIONS);
 	}
 	
 	/**
 	 * Generates a new Monster populated with random attributes.
 	 * @return A new 'starter' Monster.
 	 */
 	public static Monster generateRandom() {
		
		Monster monster = new Monster();		
 		java.util.Random rand = new java.util.Random();
 		
 		monster.setName();
 		
 		
 		
 		monster.ageRate = mutation(rand) * 1.6e-6f;
 		monster.strengthCoefficient = (int) (mutation(rand) * 500);
 		monster.toughnessCoefficient = (int) (mutation(rand) * 500);
 		monster.evadeCoefficient = (int) (mutation(rand) * 500);
 		
 		monster.fertility =  Math.abs(rand.nextDouble() );
 		monster.injuryChance = (int) (mutation(rand) * 200);
 		
 		return monster;
 		
 	}
 	
 	
 	/**
 	 * Utility function for genetic crossover.
 	 * @param rand The PRNG state object.
 	 * @param a The first parent's gene.
 	 * @param b The second parent's gene.
 	 * @return One of the two genes, each with a 50/50 chance.
 	 */
 	protected static double crossover (java.util.Random rand, double a, double b) {
 		return (rand.nextBoolean() ) ? a: b;
 	}
 	
 	/**
 	 * Breeds two monsters together to produce a list of children. The children automatically belong
 	 * to the mother's owner.
 	 * @param father The father with which to breed.
 	 * @return A list of the new children produced.
 	 */
 	public ArrayList<Monster> breed (Monster father) {
 		java.util.Random rand = new java.util.Random(); 
 		int numChildren = (int) (Math.sqrt(this.fertility * father.fertility) * MAX_NUM_CHILDREN);
 		ArrayList<Monster> returnedChildren = new ArrayList<Monster>(numChildren);
 		
 		while (returnedChildren.size() < numChildren) {
 			Monster monster = new Monster();
 			
 			monster.setName();
 			monster.setOwner(this.getOwner() );
 			
 			monster.ageRate = crossover(rand, this.ageRate, father.ageRate);
 			monster.ageRate *= mutation(rand) + 0.5;
 			
 			monster.strengthCoefficient = (int) crossover(rand, this.strengthCoefficient, father.strengthCoefficient);
 			monster.strengthCoefficient *= mutation(rand) + 0.5;
 			
 			monster.evadeCoefficient = (int) crossover(rand, this.evadeCoefficient, father.evadeCoefficient);
 			monster.evadeCoefficient *= mutation(rand) + 0.5;
 			
 			monster.toughnessCoefficient = (int) crossover(rand, this.toughnessCoefficient, father.toughnessCoefficient);
 			monster.toughnessCoefficient *= mutation(rand) + 0.5;
 			
 			monster.fertility = crossover(rand, this.fertility, father.fertility);
 			monster.fertility *= mutation(rand) + 0.5;
 			
 			monster.injuryChance = (int) crossover(rand, this.injuryChance, father.injuryChance);
 			monster.injuryChance *= mutation(rand) + 0.5;
 			
 			returnedChildren.add(monster);
 		}
 		
 		return returnedChildren;
 		
 	}
 	
 	/**
 	 * Mark this monster for removal from the game (by removing it from the owning UserAccount).
 	 */
 	public synchronized void reap() {
 		this.isDead = true;
 		if (this.owner != null) { 
 			this.owner.removeMonster(this);
 		}
 	}
 	
 	@Override
 	public int compareTo(Monster arg0) {
 		long difference = this.getUID() - arg0.getUID();
 		if (difference > 0) {
 			return 1;
 		}
 		if (difference < 0) {
 			return -1;
 		}
 		return 0;
 	}		
 	
 	public synchronized JSONObject buildJSON() throws JSONException {
 		JSONObject json = new JSONObject();
 		
 		json.put("ageRate", ageRate);
 		json.put("dateOfBirth", dateOfBirth.getTime() );
 		json.put("evade", evadeCoefficient);
 		json.put("fertility", fertility);
 		json.put("injuries", injuries);
 		json.put("injuryChance", injuryChance);
 		json.put("name", name);
 		json.put("owner", owner.getUID() );
 		json.put("strength", strengthCoefficient);
 		json.put("toughness", toughnessCoefficient);		
 		json.put("primaryKey", getUID() );
 		json.put("forSale", this.forSale);
 		json.put("forTupping", this.forTupping);
 		
 		return json;
 	}
 	
 	public static Monster readJSON(UserAccount owner, JSONObject json) throws JSONException {
 		long primaryKey = json.getLong("primaryKey");
 		Monster monster = new Monster(primaryKey);
 		
 		monster.ageRate = json.getDouble("ageRate");
 		monster.dateOfBirth = new Date(json.getLong("dateOfBirth") );
 		monster.evadeCoefficient = json.getInt("evade");
 		monster.fertility = json.getDouble("fertility");
 		monster.injuries = json.getInt("injuries");
 		monster.injuryChance = json.getInt("injuryChance");
 		monster.name = json.getString("name");
 		monster.setOwner(owner);
 		monster.strengthCoefficient = json.getInt("strength");
 		monster.toughnessCoefficient = json.getInt("toughness");
 		
 		monster.forTupping = json.optBoolean("forTupping");
 		monster.forSale = json.optBoolean("forSale");
 		
 		return monster;
 		
 	}
 
 	public int worth() {
 		int accumulator = 0;
 		accumulator += this.getHealth();
 		accumulator += this.getStrength();
 		accumulator += this.getToughness();
 		accumulator += this.getEvade();
 		return accumulator;
 	}
 	
 	public boolean isForTupping() {
 		return this.forTupping;
 	}
 	public synchronized boolean isForTupping(boolean b) {
 		return this.forTupping = b;
 	}
 	
 	public boolean isForSale() {
 		return this.forSale;
 	}
 	public synchronized boolean isForSale(boolean b) {
 		return this.forSale = b;
 	}
 
 	public synchronized void checkForInjury(Random prng) {
 		if (prng.nextInt() % 100 < this.injuryChance) {
 			this.injuries += 1;
 		}
 	}
 		
 	public synchronized Monster ageCheat(long time) {
 		long instantOfBirth = dateOfBirth.getTime();
 		instantOfBirth -= time;
 		dateOfBirth = new Date(instantOfBirth);
 		return this;		
 	}
 		
 		
 	
 	
 
 }
