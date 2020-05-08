 /**
  * 
  */
 package model;
 
 /**
  * @author Aurel
  *
  */
 public abstract class DistanceUnit extends Unit {
 
 	protected int mat; // number of move after attack
 	public DistanceUnit(int hp, int dmg, int hit, int move, int def, int crit,
 			Weapon wep, String name, int mat) {
 		super(hp, dmg, hit, move, def, crit, wep, name);
 		this.mat = mat;
 		// TODO Auto-generated constructor stub
 	}
 
 }
