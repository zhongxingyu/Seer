 /**
  * 
  */
 package model;
 
 /**
  * @author Aurel
  *
  */
 public abstract class Unit implements Soldier{
	protected int hp;		// Life points
	protected int dmg;		// Damage
	protected int hit;		// Probability of hitting (%)
	protected int move;		// Number of moves
	protected int def; 		// Damage reduction (%)
	protected int crit;		// Probability of critical attack (%)
	protected Weapon wep; 	// Type of Weapon
	protected String name; 	// Name
 	
 	public Unit(int hp, int dmg, int hit, int move, int def, int crit, Weapon wep, String name) {
 		this.hp = hp;
 		this.dmg = dmg;
 		this.hit = hit;
 		this.move = move;
 		this.def = def;
 		this.crit = crit;
 		this.wep = wep;
 		this.name = name;
 	}
 	
 	
 	/**
 	 * @param dmg
 	 */
 	public void receiveDmg(int dmg) throws DeadUnitException
 	{
 		this.hp -= dmg;
 		if(this.hp <= 0 ) throw new DeadUnitException(this.name);
 	}
 }
