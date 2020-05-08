 package bg.alex_iii.FinalCommand;
 
 public class GameSettings {
 	public static final float TARGET_RADIUS = 1;
 	public static final float TARGET_HEIGHT = 1;
 	
 	public static final float BASE_RADIUS = 1;
 	public static final float BASE_HEIGHT = 1.33f;
 	
	public static final float EXPLOSION_RADIUS = 3;
 	public static final float EXPLOSION_DURATION = 1;
 	public static final float EXPLOSION_SPEED = EXPLOSION_RADIUS / EXPLOSION_DURATION;
 	
 	public static final float MISSILE_SPEED = 1.5f;
 	public static final float MISSILE_START_ALTITUDE = 10;
 	public static final float MISSILE_SPAWN_TIME = 3;
 	public static final float MISSILE_SPAWN_VARIATION = 0.3f;
 	
 	public static final float SAM_SPEED = 8;
 	
 	public static final float SAM_EXPLOSION_RADIUS = 2;
 	public static final float SAM_EXPLOSION_DURATION = 1.5f;
 	public static final float SAM_EXPLOSION_SPEED = SAM_EXPLOSION_RADIUS / SAM_EXPLOSION_DURATION;
 }
