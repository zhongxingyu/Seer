 package ch.zhaw.mcag;
 
 import ch.zhaw.mcag.level.Level;
 import ch.zhaw.mcag.model.Dimension;
 
 public final class Config {
 	//private static int level = Level.LEVEL_DEEPSEA;
	private static int level = Level.LEVEL_DEEPSEA;
 	private static Dimension boardDimension;
 
 	private static int lifes = 5;
 	private static int extraPoint = 2000;
 	private static int obstaclePoint = 1000;
 	private static int creaturePoint = 500;
 
 	// Speed settings
 	private static int gameSpeed = 15;
 	private static int initialSpeed = 10;
 	private static int movePixels = 2;
 
 	// Speed factors
 	private static int enemyFactor = 150;
 	private static int obstacleFactor = 150;
 	private static int shotFactor = 50;
 	private static int extraFactor = 200;
 
 	// interval in milliseconds
 	private static long shotInterval = 200;
 	private static int flickerTime = 500;
 
 	// Image path
 	private final static String imagePath = "/images/";
 
 	// Images
 	private final static String player = "Player.png";
 	private final static String friendlyShot = "Shot.png";
 	private final static String enemyShot = "Shot_Opponent.png";
 	private final static String background = "Background.png";
 	private final static String foreground = "Earth.png";
 	private final static String[] enemies = {"Alien.png", "Alien2.png"};
 	private final static String[] hardObstacles = {"HRTA.png", "HRTA2.png", "HRTA3.png", "HRTA4.png"};
 	private final static String[] softObstacles = {"Satelite.png"};
 	private final static String[] extras = {"alien.gif"};
 
 	public static int getLevel() {
 		return level;
 	}
 
 	public static void setLevel(int aLevel) {
 		level = aLevel;
 	}
 
 	public static Dimension getBoardDimension() {
 		return boardDimension;
 	}
 
 	public static void setBoardDimension(Dimension aBoardDimension) {
 		boardDimension = aBoardDimension;
 	}
 
 	public static int getLifes() {
 		return lifes;
 	}
 
 	public static void setLifes(int aLifes) {
 		lifes = aLifes;
 	}
 
 	public static int getExtraPoint() {
 		return extraPoint;
 	}
 
 	public static void setExtraPoint(int aExtraPoint) {
 		extraPoint = aExtraPoint;
 	}
 
 	public static int getObstaclePoint() {
 		return obstaclePoint;
 	}
 
 	public static void setObstaclePoint(int aObstaclePoint) {
 		obstaclePoint = aObstaclePoint;
 	}
 
 	public static int getCreaturePoint() {
 		return creaturePoint;
 	}
 
 	public static void setCreaturePoint(int aCreaturePoint) {
 		creaturePoint = aCreaturePoint;
 	}
 
 	public static int getGameSpeed() {
 		return gameSpeed;
 	}
 
 	public static void setGameSpeed(int aGameSpeed) {
 		gameSpeed = aGameSpeed;
 	}
 
 	public static int getInitialSpeed() {
 		return initialSpeed;
 	}
 
 	public static void setInitialSpeed(int aInitialSpeed) {
 		initialSpeed = aInitialSpeed;
 	}
 
 	public static int getMovePixels() {
 		return movePixels;
 	}
 
 	public static void setMovePixels(int aMovePixels) {
 		movePixels = aMovePixels;
 	}
 
 	public static int getEnemyFactor() {
 		return enemyFactor;
 	}
 
 	public static void setEnemyFactor(int aEnemyFactor) {
 		enemyFactor = aEnemyFactor;
 	}
 
 	public static int getObstacleFactor() {
 		return obstacleFactor;
 	}
 
 	public static void setObstacleFactor(int aObstacleFactor) {
 		obstacleFactor = aObstacleFactor;
 	}
 
 	public static int getShotFactor() {
 		return shotFactor;
 	}
 
 	public static void setShotFactor(int aShotFactor) {
 		shotFactor = aShotFactor;
 	}
 
 	public static int getExtraFactor() {
 		return extraFactor;
 	}
 
 	public static void setExtraFactor(int aExtraFactor) {
 		extraFactor = aExtraFactor;
 	}
 
 	public static long getShotInterval() {
 		return shotInterval;
 	}
 
 	public static void setShotInterval(long aShotInterval) {
 		shotInterval = aShotInterval;
 	}
 
 	public static int getFlickerTime() {
 		return flickerTime;
 	}
 
 	public static void setFlickerTime(int aFlickerTime) {
 		flickerTime = aFlickerTime;
 	}
 
 	public static String getImagePath() {
 		return imagePath;
 	}
 
 	public static String getPlayer() {
 		return player;
 	}
 
 	public static String getFriendlyShot() {
 		return friendlyShot;
 	}
 
 	public static String getEnemyShot() {
 		return enemyShot;
 	}
 
 	public static String getBackground() {
 		return background;
 	}
 
 	public static String getForeground() {
 		return foreground;
 	}
 
 	public static String[] getEnemies() {
 		return enemies;
 	}
 
 	public static String[] getHardObstacles() {
 		return hardObstacles;
 	}
 
 	public static String[] getSoftObstacles() {
 		return softObstacles;
 	}
 
 	public static String[] getExtras() {
 		return extras;
 	}
 }
