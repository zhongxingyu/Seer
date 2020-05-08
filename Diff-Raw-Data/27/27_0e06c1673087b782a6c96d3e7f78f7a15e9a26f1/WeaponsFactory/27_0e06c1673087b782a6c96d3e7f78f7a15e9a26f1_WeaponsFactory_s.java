 package fr.umlv.escape.weapon;
 
 
 /**
  * Factory of {@link Weapon}
  */
 public class WeaponsFactory {
 	private static WeaponsFactory theWeaponsFactory;
 
 	private WeaponsFactory(){
 	}
 
 	public enum Weapons {
		MISSILE_LAUNCHER,
 		FLAMETHROWER,
		SHIBOLEET_THROWER,
		LASER_BEAM
 	}
 	
 	/**
 	 * @return the singleton of Weapon Factory
 	 */
 	public static WeaponsFactory getTheWeaponsFactory(){
 		if(WeaponsFactory.theWeaponsFactory==null){
 			WeaponsFactory.theWeaponsFactory = new WeaponsFactory();
 		}
 		return WeaponsFactory.theWeaponsFactory;
 	}
 	
 	/**
 	 * @param weaponName
 	 * @param qty
 	 * @return a weapon with a quantity of bullet in function of string weaponName
 	 */
 	public Weapon createWeapon(String weaponName, int qty){
 		Weapons currentWeapon = Weapons.valueOf(weaponName.toUpperCase());
 		switch(currentWeapon){
		case MISSILE_LAUNCHER : return new MissileLauncher(qty);
 		case FLAMETHROWER : return new FlameThrower(qty);
		case SHIBOLEET_THROWER : return new ShiboleetThrower(qty);
		case LASER_BEAM : return new LaserBeam(qty);
 		default : throw new IllegalArgumentException("WeaponName: "+ weaponName +" isn't a legal weapon");
 		}
 	}
 }
