 package dk.aau.cs.giraf.cars.gamecode.GameObjects;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import android.graphics.Color;
 import android.graphics.Point;
 import android.graphics.Rect;
 
 import dk.aau.cs.giraf.cars.R;
 import dk.aau.cs.giraf.cars.gamecode.GameInfo;
 import dk.aau.cs.giraf.cars.gamecode.GameObject;
 import dk.aau.cs.giraf.cars.gamecode.GameRenderer;
 import dk.aau.cs.giraf.cars.gamecode.IDrawable;
 import dk.aau.cs.giraf.cars.gamecode.IWorkable;
 import dk.aau.cs.giraf.cars.gamecode.MapDivider;
 
 public class Car extends GameObject implements IWorkable, IDrawable {
 	protected float xOffset = -MapDivider.obstacleWidth;
 	protected int yOffset = 0;
 	private final int mLowFreq;
 	private final int mHighFreq;
 	Point[] collisionBox;
 	private boolean updateCarCollisionBox = true;
 	private float carSpeedAsFloat;
 	public float carScaling = 0.7F;
 	private int halfObstacleHeight = (int)(MapDivider.obstacleHeight * carScaling) / 2;
 	private int[] colors;
 	private int[] bitmapIds;
 	private boolean[] closedColors;
 	private int currentColor;
 	
 	public Car(int y, float carSpeed, int[] colors, int[] bitmapIds) {
 		carSpeedAsFloat = carSpeed;
 		this.yOffset = y;
 
 		mLowFreq = GameInfo.getLowFreq();
 		mHighFreq = GameInfo.getHighFreq();
 		
 		collisionBox = new Point[4];
 		collisionBox[0] = new Point(0,0);
 		collisionBox[1] = new Point(0,0);
 		collisionBox[2] = new Point(0,0);
 		collisionBox[3] = new Point(0,0);
 		
 		this.colors = colors;
 		this.bitmapIds = bitmapIds;
 		closedColors = new boolean[colors.length];
 		Random rand = new Random();
 		currentColor = rand.nextInt(colors.length);
 	}
 	
 	@Override
 	public void Draw(GL10 gl, GameRenderer spriteBatcher) {
 		if (colors[currentColor] != Color.WHITE) {
 			spriteBatcher.draw(gl, bitmapIds[currentColor], new Rect(0, 0, 898, 348), new Rect( (int)xOffset, yOffset - halfObstacleHeight, (int)(MapDivider.obstacleWidth * carScaling) + (int)xOffset, yOffset + halfObstacleHeight));
 		}
 		else {
 			spriteBatcher.draw(gl, R.drawable.car, new Rect(0, 0, 898, 348), new Rect( (int)xOffset, yOffset - halfObstacleHeight, (int)(MapDivider.obstacleWidth * carScaling) + (int)xOffset, yOffset + halfObstacleHeight));
 		}
 	}
 
 	@Override
 	public void PerformWork() {
 		updateCarCollisionBox = true;
 		if (GameInfo.win == false && GameInfo.garageClosing == false && GameInfo.pause == false){
 			xOffset = xOffset + carSpeedAsFloat;
 		}
 		int currFreq = GameInfo.getCurrFreq();
 		
 		if (currFreq > 0 && xOffset > -(MapDivider.obstacleWidth / 2)) {
 			if (currFreq > mHighFreq && yOffset - halfObstacleHeight > MapDivider.mapYStart) {
 				yOffset -= 2;
 			} else if (currFreq < mLowFreq && yOffset + halfObstacleHeight < MapDivider.mapYEnd) {
 				yOffset += 2;
 			}
  		} else {
  			int closestLane = 0;
  			for (int i = 1; i < MapDivider.lanes; i++) {
  				if (Math.abs(MapDivider.laneCenters[i] - yOffset) <
  					Math.abs(MapDivider.laneCenters[closestLane] - yOffset)) {
  					closestLane = i;
  				}
  			}
  			int offset = MapDivider.laneCenters[closestLane] - yOffset;
  			if (offset != 0) {
  				if (offset > 0) {
  					yOffset++;
  				} else if (offset < 0) {
  					yOffset--;
  				}
  			}
  		}
 	}
 	
 	public boolean CalculateCollisions(Point[] form) {
 		if (updateCarCollisionBox) {
 			int widthScaled = (int)(MapDivider.obstacleWidth * carScaling);
 			
 			collisionBox[0].x = (int)xOffset;
 			collisionBox[0].y = yOffset - halfObstacleHeight;
 			collisionBox[1].x = (int)xOffset;
 			collisionBox[1].y = yOffset + halfObstacleHeight;
 			collisionBox[2].x = widthScaled + (int)xOffset;
 			collisionBox[2].y = yOffset + halfObstacleHeight;
 			collisionBox[3].x = widthScaled + (int)xOffset;
 			collisionBox[3].y = yOffset - halfObstacleHeight;
 			
 			updateCarCollisionBox = false;
 		}
 		if (collisionBox[0].y > form[2].y || collisionBox[1].y < form[0].y) {
 			return false;
 		}
 		if (collisionBox[0].x > form[3].x || collisionBox[2].x < form[1].x){
 			return false;
 		}
 		
 		for (int i = 0; i < 3; i++) {
 			for (int j = 0; j < 3; j++) {
 				if (doesLineCrossLine(collisionBox[i], collisionBox[(i + 1) % 4],
 									  form[j], form[(j + 1) % 4])) {
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	private boolean doesLineCrossLine(Point line1Start, Point line1End,
 									  Point line2Start, Point line2End) {
 		float crossProduct1 = crossProduct(line1Start, line1End, line2Start);
 		float crossProduct2 = crossProduct(line1Start, line1End, line2End);
 		if ((crossProduct1 < 0 && crossProduct2 < 0) ||
 				(crossProduct1 > 0 && crossProduct2 > 0)) {
 			return false;
 		}
 		else {
 			float crossProduct3 = crossProduct(line2Start, line2End, line1Start);
 			float crossProduct4 = crossProduct(line2Start, line2End, line1End);
 			
 			if ((crossProduct3 < 0 && crossProduct4 < 0) ||
 					(crossProduct3 > 0 && crossProduct4 > 0)) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	private float crossProduct(Point line1Start, Point line1End,
 							   Point line2Point) {
 		return (line1End.x - line1Start.x) * (line2Point.y - line1End.y) -
 			   (line1End.y - line1Start.y) * (line2Point.x - line1End.x);
 	}
 	
 	public void resetPosition() {
 		yOffset = MapDivider.mapYStart + MapDivider.totalObstacleHeight / 2 + MapDivider.totalObstacleHeight;
 		xOffset = -MapDivider.obstacleWidth;
 	}
 	
 	public int getColor() {
 		return colors[currentColor];
 	}
 	
 	public void closeColor() {
 		closedColors[currentColor] = true;
 		
 		newColor();
 	}
 	
 	public void newColor() {
 		ArrayList<Integer> openColors = new ArrayList<Integer>();
 		
 		if (closedColors[0] == false) {
 			openColors.add(0);
 		}
 		if (closedColors[1] == false) {
 			openColors.add(1);
 		}
 		if (closedColors[2] == false) {
 			openColors.add(2);
 		}
 		
		if (openColors.size() > 0) {
			Random rand = new Random();
			currentColor = openColors.get(rand.nextInt(openColors.size()));
		}
 	}
 }
