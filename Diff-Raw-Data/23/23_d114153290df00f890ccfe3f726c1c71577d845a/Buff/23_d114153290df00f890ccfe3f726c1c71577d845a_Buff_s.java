 package hunternif.mc.dota2items.core.buff;
 
 
 public class Buff {
 	public final String name;
 	public final int id;
 	
 	public boolean disableAttack;
 	public boolean disableMove;
 	public boolean disableItems;
 	
 	public boolean magicImmune;
 	public boolean invulnerable;
 	
 	public int health = 0;
 	public float healthRegen = 0;
 	public int mana = 0;
 	public float manaRegen = 0;
 	public int manaRegenPercent = 0;
 	public int movementSpeed = 0;
 	public int attackSpeed = 0;
 	public int armor = 0;
 	public int damage = 0;
 	public int damagePercentMelee = 0;
 	public int damagePercentRanged = 0;
 	
 	public int intelligence = 0;
 	public int strength = 0;
 	public int agility = 0;
 	
 	public boolean stacks = true;
 	
 	
 	public static final Buff[] buffList = new Buff[1024];
 	
 	public static final Buff tangoActive = new Buff(1, "Tango active").setHealthRegen(115f/16f).setDoesNotStack();
 	public static final Buff quell = new Buff(2, "Quell").setDamagePercentMelee(32).setDamagePercentRanged(16).setDoesNotStack();
 	public static final Buff eulsScepter = new Buff(3, "Eul's Scepter").setMovementSpeed(30).setIntelligence(10).setManaRegenPercent(150);
	public static final Buff inCyclone = new Buff(4, "Swept up in Cyclone").setDisableAttack().setDisableItems().setDisableMove().setInvulnerable();
 	public static final Buff ringOfProtection = new Buff(5, "Ring of Protection").setArmor(2);
 	public static final Buff bootsOfSpeed = new Buff(6, "Boots of Speed").setMovementSpeed(50);
 	
 	
 	public Buff(int id, String name) {
 		this.id = id;
 		this.name = name;
 		buffList[id] = this;
 	}
 	
 	public Buff setDisableAttack() {
 		disableAttack = true;
 		return this;
 	}
 	
 	public Buff setDisableMove() {
 		disableMove = true;
 		return this;
 	}
 	
 	public Buff setDisableItems() {
 		disableItems = true;
 		return this;
 	}
 	
 	public Buff setMagicImmune() {
 		magicImmune = true;
 		return this;
 	}
 	
 	public Buff setInvulnerable() {
 		invulnerable = true;
 		return this;
 	}
 	
 	public Buff setHealth(int value) {
 		health = value;
 		return this;
 	}
 	
 	public Buff setHealthRegen(float value) {
 		healthRegen = value;
 		return this;
 	}
 	
 	public Buff setMana(int value) {
 		mana = value;
 		return this;
 	}
 	
 	public Buff setManaRegen(float value) {
 		manaRegen = value;
 		return this;
 	}
 	
 	public Buff setManaRegenPercent(int value) {
 		manaRegenPercent = value;
 		return this;
 	}
 	
 	public Buff setMovementSpeed(int value) {
 		movementSpeed = value;
 		return this;
 	}
 	
 	public Buff setAttackSpeed(int value) {
 		attackSpeed = value;
 		return this;
 	}
 	
 	public Buff setArmor(int value) {
 		armor = value;
 		return this;
 	}
 	
 	public Buff setDamage(int value) {
 		damage = value;
 		return this;
 	}
 	
 	public Buff setDamagePercentMelee(int value) {
 		damagePercentMelee = value;
 		return this;
 	}
 	
 	public Buff setDamagePercentRanged(int value) {
 		damagePercentRanged = value;
 		return this;
 	}
 	
 	public Buff setStrength(int value) {
 		strength = value;
 		return this;
 	}
 	
 	public Buff setIntelligence(int value) {
 		intelligence = value;
 		return this;
 	}
 	
 	public Buff setAgility(int value) {
 		agility = value;
 		return this;
 	}
 	
 	public Buff setDoesNotStack() {
 		stacks = false;
 		return this;
 	}
 	
 	@Override
 	public String toString() {
 		return name;
 	}
 }
