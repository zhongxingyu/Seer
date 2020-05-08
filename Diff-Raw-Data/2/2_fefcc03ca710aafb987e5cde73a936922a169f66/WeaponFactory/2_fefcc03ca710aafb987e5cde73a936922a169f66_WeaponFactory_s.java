 package model.items.weapons;
 
 import java.util.Random;
 
 /**
  * Creates different types of weapons.
  * @author Vidar Eriksson
  *
  */
 public class WeaponFactory {
 	
 	/**
 	 * 
 	 * @author Vidar
 	 *
 	 */
 	public static enum Type{
 		//Range
 		PISTOL (0.75f,5,50f,6,6000,1000,10, "Pistol", Weapon.Type.GUN),
 		SHOTGUN (0.45f, 10, 50f, 6, 10000, 3000, 11, "Shotgun", Weapon.Type.GUN),
 //		REVOLVER,
 //		HUNTING_RIFLE,
 		SUB_MACHINEGUN(0.55f, 3, 50f, 20, 7000, 250, 12, "Sub Machinegun", Weapon.Type.GUN),
 //		MINIGUN,
 //		ROCKET_LAUNCHER,
 //TODO more weapons
 		//Melee
 		FISTS (1.8f, 1, 0.3f, Weapon.UNLIMITED_AMMO, 0, 1000, 0, "Fists", Weapon.Type.FISTS),
 //		POCKET_KNIFE,
 //		MACHETTE,
 		BAT(0.35f, 4, 0.5f, Weapon.UNLIMITED_AMMO, 0, 700, 1, "Bat", Weapon.Type.MELEE),
 //		PIPE,
 		
 		TEST_WEAPON (1.0f,2,100f,100000000,5,1000,4, "Test weapon", Weapon.Type.GUN);
 		
 		private final float projectileSpeed;
 		private final int damage;
 		private final float range;
 		private final int magazineCapacity;
 		private final int reloadTime;
 		private final int rateOfFire;
 		private final int iconNumber;
 		private final String name;
 		private final Weapon.Type weaponType;
 		
 		/**
 		 * Creates a new type.
 		 * @param projectileSpeed the speed of the projectile this weapon use.
 		 * @param damage the damage each projectile inflicts.
 		 * @param range the range a projectile can travel.
 		 * @param magazineCapacity the number of bullets the weapon can hold when fully loaded.
 		 * @param reloadTime the time it takes to reload the weapon.
 		 * @param rateOfFire the time between each shot.
 		 * @param iconNumber the number of the image to use.
 		 * @param name the name of the weapon.
 		 */
 		Type(float projectileSpeed,
 				int damage,
 				float range,
 				int magazineCapacity,
 				int reloadTime,
 				int rateOfFire,
 				int iconNumber,
 				String name, 
 				Weapon.Type type){
 			this.projectileSpeed = projectileSpeed;
 			this.damage = damage;
 			this.range = range;
 			this.magazineCapacity = magazineCapacity;
 			this.reloadTime = reloadTime;
 			this.rateOfFire=rateOfFire;
 			this.iconNumber=iconNumber;
 			this.name=name;
 			this.weaponType = type;
 		}
 
 		/**
 		 * Gives the speed of the projectile used by this weapon.
 		 * @return the speed of the projectile used by this weapon.
 		 */
 		public float getProjectileSpeed() {
 			return projectileSpeed;
 		}
 
 		/**
 		 * Gives the damage the weapon's projectile inflicts.
 		 * @return the damage the weapon's projectile inflicts.
 		 */
 		public int getDamage() {
 			return damage;
 		}
 		
 		/**
 		 * Gives the type of the weapon.
 		 * @return the type of the weapon.
 		 */
 		public Weapon.Type getWeaponType() {
 			return weaponType;
 		}
 
 		/**
 		 * The range a projectile can travel.
 		 * @return range a projectile can travel.
 		 */
 		public float getRange() {
 			return range;
 		}
 
 		/**
 		 * The number of bullets the weapon can hold when fully loaded.
 		 * @return the number of bullets the weapon can hold when fully loaded.
 		 */
 		public int getMagazineCapacity() {
 			return magazineCapacity;
 		}
 
 		/**
 		 * Gives the time it takes to reload the weapon.
 		 * @return the time it takes to reload in ms.
 		 */
 		public int getReloadTime() {
 			return reloadTime;
 		}
 
 		/**
 		 * The time between each shot.
 		 * @return the time between each shot in ms.
 		 */
 		public int getRateOfFire() {
 			return rateOfFire;
 		}
 
 		/**
 		 * Gives the number of the image used by the weapon.
 		 * @return the number of the image used by the weapon.
 		 */
 		public int getIconNumber() {
 			return iconNumber;
 		}
 		
 		@Override
 		public String toString() {
 			return name;
 		}		
 	}
 	
 	/**
 	 * 
 	 * @author Vidar
 	 *
 	 */
 	public static enum Level{
 		RUSTY (1, "Rusty"),
 		NORMAL (3, "Normal"),
 		LARGE (5, "Large"),
 		BADASS (7, "Badass"),
 		EPIC (10, "Epic");
 		
 		private final int multiplier;
 		private final String name;
 		
 		/**
 		 * Creates a new level constant.
 		 * @param multiplier the multiplier to the weapon.
 		 * @param name the name of the level.
 		 */
 		Level (int multiplier, String name){
 			this.multiplier = multiplier;
 			this.name = name;
 		}
 		
 		/**
 		 * Gives the multiplier of this level.
 		 * @return the multiplier of this level.
 		 */
 		public int multiplier() {
 			return multiplier;
 		}
 		
 		@Override
 		public String toString() {
 			return name;
 		}
 	}
 		
 	/**
 	 * Creates a new weapon with the specified type, level and if its droppable or not.
 	 * @param type the type of the weapon.
 	 * @param level the level of the weapon.
 	 * @return a new weapon.
 	 */
 	public static Weapon createWeapon(Type type, Level level) {
		boolean droppable = (type.getWeaponType() == Weapon.Type.FISTS) ? true : false;
 		return new Weapon(
 				type.getProjectileSpeed(),
 				type.getDamage()*level.multiplier(),
 				type.getRange(),
 				type.getMagazineCapacity(),
 				type.getReloadTime()/level.multiplier(),
 				type.getRateOfFire(),
 				type.getIconNumber(),
 				level.toString() + " " + type.toString(),
 				droppable,
 				type.getMagazineCapacity(),
 				type.getWeaponType()
 				);
 	}
 
 	/**
 	 * Creates a test weapon for debugging.
 	 * @return a test weapon for debugging.
 	 */
 	public static Weapon createTestWeapon() {
 		return createWeapon(Type.PISTOL, Level.EPIC);
 		//TODO ta bort?
 	}
 	public static Weapon createRandomWeapon(){
 		int typeLength = Type.values().length;
 		int LevelLentgh = Level.values().length;
 		Random random = new Random();
 		return createWeapon(Type.values()[random.nextInt(typeLength)], 
 				Level.values()[random.nextInt(LevelLentgh)]);
 	}
 	/**
 	 * Creates a test weapon for debugging.
 	 * @return a test weapon for debugging.
 	 */
 	public static Weapon createTestWeapon2(){
 		return createWeapon(Type.PISTOL, Level.NORMAL);
 		//TODO ta bort?
 	}
 	
 	/**
 	 * Gives the weapon the player uses bu default.
 	 * @return the weapon the player uses by default.
 	 */
 	public static Weapon createPlayerDefaultWeapon() {
 		//TODO bestm vapen
 		return createEnemyMeleeWeapon();
 	}
 	
 	/**
 	 * Creates the weapon enemies uses
 	 * @return the weapon enemies uses.
 	 */
 	public static Weapon createEnemyMeleeWeapon(){
 		return createWeapon(Type.FISTS, Level.RUSTY);
 	}
 	
 	/**
 	 * Gives an instance of the default weapon used by the player.
 	 * @return an instance of the default weapon used by the player.
 	 */
 	public static Weapon getDefaultWeapon() {
 		return createEnemyMeleeWeapon();
 	}
 	
 	/**
 	 * Gives a new weapon based on the data provided.
 	 * NOTE: This is used instead of the <code>restore(String[] data)</code> in <code>Weapon</code>.
 	 * @param data the data to base the new weapon on.
 	 * @return a new weapon based on the data provided.
 	 */
 	public static Weapon loadWeapon(String[] data) {
 		return new Weapon(
 				Float.parseFloat(data[6]),
 				Integer.parseInt(data[1]),
 				Float.parseFloat(data[7]),
 				Integer.parseInt(data[4]),
 				Integer.parseInt(data[9]),
 				Integer.parseInt(data[8]),
 				Integer.parseInt(data[3]),
 				data[5],
 				Boolean.parseBoolean(data[3]),
 				Integer.parseInt(data[0]),
 				Weapon.Type.fromString(data[10]));
 	}
 }
