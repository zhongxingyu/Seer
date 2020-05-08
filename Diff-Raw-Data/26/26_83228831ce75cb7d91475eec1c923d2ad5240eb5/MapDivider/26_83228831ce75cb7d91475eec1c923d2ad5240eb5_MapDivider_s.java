 package dk.aau.cs.giraf.cars.gamecode;
 
 import android.graphics.Rect;
 
 public final class MapDivider {
 
 	public static int lanes = 3;
 	public static int width = 1;
 	public static int height = 2;
 	public static int totalObstacleWidth;
 	public static int totalObstacleHeight;
 	public static int obstacleWidth;
 	public static int obstacleHeight;
 	public static int obstacleSpace;
 	public static int mapHeight;
 	public static int mapYStart;
 	public static int mapYEnd;
 	public static int[] laneCenters;
 	
 	private MapDivider() {}
 	
 	public static void CalculateConstants(GameView gameView) {
 		width = gameView.getWidth();
 		height = gameView.getHeight();
 		
 		totalObstacleWidth = (int) (width/7.5);
 		totalObstacleHeight = totalObstacleWidth;
 		obstacleSpace = (int) (totalObstacleWidth*0.025);
 		obstacleWidth = (int) (totalObstacleWidth*0.95);
 		obstacleHeight = obstacleWidth;
 		mapHeight = totalObstacleWidth*3;
 		mapYStart = (height - mapHeight) / 2;
 		mapYEnd = mapHeight + mapYStart;
 		
 		int laneHeight = mapHeight / 3;
 		laneCenters = new int[lanes];
 		for (int i = 0; i < lanes; i++) {
 			laneCenters[i] = mapYStart + laneHeight / 2 + laneHeight * i;
 		}
 	}
 	
 	public static void CalculateConstants(int Height, int carHeight) {
		mapHeight = Height;
 		obstacleHeight = carHeight;
		int laneHeight = mapHeight / 3;
 		laneCenters = new int[lanes];
 		
 		for (int i = 0; i < lanes; i++) {
 			laneCenters[i] = laneHeight * i + laneHeight / 2;
 		}
 	}
 	
 	public static Rect CalculateObstacle(int lane, int coloumn){
 		int x1;
 		int x2;
 		int y1;
 		int y2;
 		y1 = mapYStart + (lane - 1) * totalObstacleHeight + obstacleSpace;
 		y2 = y1 + obstacleHeight;
 		x1 = (int) (totalObstacleWidth * 1.5 + totalObstacleWidth * (coloumn - 1) + obstacleSpace);
 		x2 = x1 + obstacleWidth;
 		
 		return new Rect(x1, y1, x2, y2);
 	}

//	static int laneOne = 0;
//	static int laneTwo = height/3;
//	static int laneThree = (height/3)*2;
//	static double ratio = width/height;
	
 }
