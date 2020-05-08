 package isr;
 
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 
 class EnemyShip
 {
	public static final int DEPARTURE = -700;
 	public static final int BASE_SPEED_PER_TURN = 40;
 	
 	private static EnemyShip instance;
 	private static Sound alarm;
 	
 	public static EnemyShip get()
 	{
 		if(instance == null)
 			instance = new EnemyShip();
 		return instance;
 	}
 	
 	private int progress;
 	
 	private EnemyShip()
 	{
 		progress = DEPARTURE;
 	}
 	
 	public static void loadContent() throws SlickException
 	{
 		alarm = new Sound(Game.ASSETS_DIR + "alarme_sub_ennemi.ogg");
 	}
 
 	public void init()
 	{
 		progress = DEPARTURE;
 	}
 	
 	public int getProgressMiles()
 	{
 		return progress;
 	}
 	
 	public float getProgressRatio()
 	{
 		return (float)progress / (float)Ship.TRIP_DISTANCE;
 	}
 	
 	public boolean reachedPlayer()
 	{
 		return progress >= Ship.get().getProgressMiles();
 	}
 	
 	public void advance()
 	{
 		advance(BASE_SPEED_PER_TURN);		
 	}
 
 	public void advance(int distanceMiles)
 	{
 		int lastProgress = progress;
 		progress += distanceMiles;
 		// The enemy cannot overtake the player's ship
 		if(progress > Ship.get().getProgressMiles())
 			progress = Ship.get().getProgressMiles();
 		if(lastProgress < 0 && progress >= 0)
 			alarm.play();
 	}
 	
 }
 
