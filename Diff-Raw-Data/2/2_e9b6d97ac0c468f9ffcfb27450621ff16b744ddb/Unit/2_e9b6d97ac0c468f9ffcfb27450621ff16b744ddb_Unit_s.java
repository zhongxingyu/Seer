 package model.units;
 
 import model.attack.IAttack;
 import model.exception.DeadBossException;
 import model.exception.DeadUnitException;
 import view.Tile;
 
 /**
  * @author Aurel
  * 
  */
 public abstract class Unit {
 
     public enum Weapon {
 	SWORD, LANCE, AXE, BOW, DAGGER, HEROIC, UNKNOWN
     }
 
     protected IAttack attack;
     protected int hp;
     protected int maxHp;
     protected int dmg;
     protected int hit;
     protected int move;
     protected int def;
     protected int crit;
     protected int mat;
     protected Weapon wep;
     protected String name;
     protected Tile tile;
     protected boolean pow;
     protected int turnsCripple;
     protected int turnsPoisoned;
     protected boolean attackedPrevious;
 
     /**
      * @param attack
      *            behavior of attack
      * @param hp
      *            life points
      * @param dmg
      *            damage
      * @param hit
      *            probability of hitting (%)
      * @param move
      *            number of moves
      * @param def
      *            damage reduction (%)
      * @param crit
      *            probability of critical attack (%)
      * @param mat
      *            number of moves after attack
      * @param wep
      *            type of weapon
      * @param name
      *            name of unit
      */
     public Unit(IAttack attack, int hp, int dmg, int hit, int move, int def,
 	    int crit, int mat, Weapon wep, String name) {
 	this.attack = attack;
 	this.hp = hp;
 	this.maxHp = hp;
 	this.dmg = dmg;
 	this.hit = hit;
 	this.move = move;
 	this.def = def;
 	this.crit = crit;
 	this.mat = mat;
 	this.wep = wep;
 	this.name = name;
 	this.turnsCripple = 0;
 	this.turnsPoisoned = 0;
 	this.attackedPrevious = false;
     }
 
     public abstract void activatePower();
 
     /**
      * Heal a unit.
      * 
      */
     public void addRegeneration() {
 	System.out.println("Nothing to do.");
 	// let empty
     }
 
     /**
      * Heal a unit on a Forest.
      * 
      */
     public void addRegenerationForest() {
 	System.out.println("Nothing to do.");
 	// let empty
     }
 
     /**
      * Heal a unit on a Fort.
      * 
      */
     public void addRegenerationFort() {
	this.hp += 5;
 	if (this.hp > this.maxHp)
 	    this.hp = this.maxHp;
     }
 
     /**
      * Attack an other unit.
      * 
      * @param u
      *            the defender
      * @return result of the fight
      * @throws DeadUnitException
      *             if the defender is dead
      */
     public String attack(Unit u, boolean tank) throws DeadUnitException {
 	return this.attack.attack(this, u, tank);
     }
 
     /**
      * Returns if a unit can attack from a certain range.
      * 
      * @param i
      *            the range
      * @return true if the unit can attack or false otherwise
      */
     public boolean canAttackFromRange(int i) {
 	return this.attack.canAttackFromRange(i);
     }
 
     /**
      * Returns the probability of critical attack.
      * 
      * @return the probability of critical attack
      */
     public int getCrit() {
 	return crit;
     }
 
     /**
      * Returns the damage reduction.
      * 
      * @return the damage reduction
      */
     public int getDef() {
 	return def;
     }
 
     /**
      * Returns the number of damages.
      * 
      * @return the damages
      */
     public int getDmg() {
 	return dmg;
     }
 
     /**
      * Returns the probability of hit another unit.
      * 
      * @return the probability of hit
      */
     public int getHit() {
 	return hit;
     }
 
     /**
      * Returns the number of life points.
      * 
      * @return the number of hp
      */
     public int getHp() {
 	return hp;
     }
 
     /**
      * Returns the number of movements after attack.
      * 
      * @return the number of movements after attack
      */
     public int getMat() {
 	return mat;
     }
 
     /**
      * Returns the number of movements.
      * 
      * @return the number of movements
      */
     public int getMove() {
 	return move;
     }
 
     /**
      * Returns the name.
      * 
      * @return the name
      */
     public String getName() {
 	return name;
     }
 
     /**
      * Returns the range of attack.
      * 
      * @return the range
      */
     public int getRange() {
 	return attack.getRange();
     }
 
     /**
      * Returns the Tile of the unit.
      * 
      * @return the tile
      */
     public Tile getTile() {
 	return tile;
     }
 
     /**
      * Returns the number of turns crippled left.
      * 
      * @return number of turn cripples
      */
     public int getTurnsCripple() {
 	return turnsCripple;
     }
 
     /**
      * Returns the number of turns poisoned left.
      * 
      * @return number of turn poisoned
      */
     public int getTurnsPoisoned() {
 	return turnsPoisoned;
     }
 
     /**
      * Returns the kind of weapon.
      * 
      * @return the weapon
      */
     public Weapon getWep() {
 	return wep;
     }
 
     /**
      * Returns if the unit has attacked previous turn.
      * 
      * @return true if the unit has attacked previous turn false otherwise
      */
     public boolean hasAttackedPrevious() {
 	return attackedPrevious;
     }
 
     /**
      * Returns the possibility of the unit to MaT.
      * 
      * @return the true if the unit can MaT.
      */
     public boolean hasMat() {
 	return mat > 0;
     }
 
 
     /**
      * Returns if the power of the unit is activate.
      * 
      * @return true if activate false otherwise
      */
     public boolean isPowActivate() {
 	return pow;
     }
 
     /**
      * Reduce the number of life points.
      * 
      * @param dmg
      *            the damages
      * @throws DeadUnitException
      *             if the unit is dead
      * @throws DeadBossException
      *             if the boss is dead
      * 
      */
     public void receiveDmg(int dmg) throws DeadUnitException, DeadBossException {
 	this.hp -= dmg;
 	if (this.hp <= 0) {
 	    if (this.name.equals("Pegasus") || this.name.equals("Dragon"))
 		throw new DeadBossException(this.name, dmg);
 	    throw new DeadUnitException(this.name, dmg);
 	}
     }
 
     /**
      * Reduce the number of life points dur to poison.
      * 
      * @throws DeadUnitException
      *             if the unit is dead
      * @throws DeadBossException
      *             if the boss is dead
      */
     public void receivePoisonedDmg() throws DeadUnitException,
 	    DeadBossException {
 	this.receiveDmg(5);
     }
 
     /**
      * Set if a unit has attacked previous turn.
      * 
      * @param attackedPrevious
      *            true if the unit has attacked previous turn false otherwise
      */
     public void setAttackedPrevious(boolean attackedPrevious) {
 	this.attackedPrevious = attackedPrevious;
     }
 
     /**
      * Set a new behavior of attack.
      * 
      * @param a
      *            behavior of attack
      */
     public void setIAttack(IAttack a) {
 	this.attack = a;
     }
 
     /**
      * Set a new Tile on which is the unit.
      * 
      * @param tile
      *            the tile on which is the unit
      */
     public void setTile(Tile tile) {
 	this.tile = tile;
     }
 
     /**
      * Set number of turns cripple.
      * 
      * @param turnsCripple
      */
     public void setTurnsCripple(int turnsCripple) {
 	this.turnsCripple = turnsCripple;
     }
 
     /**
      * Set number of turns poisoned.
      * 
      * @param turnsPoisoned
      */
     public void setTurnsPoisoned(int turnsPoisoned) {
 	this.turnsPoisoned = turnsPoisoned;
     }
 
 }
