 package fr.umlv.escape.weapon;
 
 
 /**
  * Factory of {@link Weapon}
  */
 public class WeaponsFactory {
 	private static WeaponsFactory theWeaponsFactory;
 
 	private WeaponsFactory(){
 	}
 
 	public enum Weapons {
		MISSILELAUNCHER,
 		FLAMETHROWER,
		SHIBOLEETTHROWER,
		LASERBEAM
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
		System.out.println(weaponName);
 		Weapons currentWeapon = Weapons.valueOf(weaponName.toUpperCase());
 		switch(currentWeapon){
		case MISSILELAUNCHER : return new MissileLauncher(qty);
 		case FLAMETHROWER : return new FlameThrower(qty);
		case SHIBOLEETTHROWER : return new ShiboleetThrower(qty);
		case LASERBEAM : return new LaserBeam(qty);
 		default : throw new IllegalArgumentException("WeaponName: "+ weaponName +" isn't a legal weapon");
 		}
 	}
 }
