 package dk.aau.cs.giraf.cars.gamecode.GameObjects;
 
 import javax.microedition.khronos.opengles.GL10;
 
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
	public int xOffset = 0;
 	protected int yOffset = 0;
 	private final int mLowFreq;
 	private final int mHighFreq;
 	Point[] collisionBox;
 	private boolean updateCarCollisionBox = true;
 	
 	public Car(int y) {
 		// TODO Auto-generated constructor stub
 		this.yOffset = y;
 
 		mLowFreq = GameInfo.getLowFreq();
 		mHighFreq = GameInfo.getHighFreq();
 		
 		collisionBox = new Point[4];
 		collisionBox[0] = new Point(0,0);
 		collisionBox[1] = new Point(0,0);
 		collisionBox[2] = new Point(0,0);
 		collisionBox[3] = new Point(0,0);
 	}
 	
 	@Override
 	public void Draw(GL10 gl, GameRenderer spriteBatcher) {
 		spriteBatcher.draw(gl, R.drawable.car, new Rect(0, 0, 898, 348), new Rect( xOffset, yOffset, MapDivider.obstacleWidth + xOffset, MapDivider.obstacleHeight + yOffset));
 	}
 
 	@Override
 	public void PerformWork() {
 		// TODO Auto-generated method stub
 		updateCarCollisionBox = true;
 		xOffset++;
 		
 		int currFreq = GameInfo.getCurrFreq();
 		
 		if (currFreq > 0) {
 			if (currFreq > mHighFreq && yOffset > 0) {
 				yOffset--;
 				//System.out.println("Going up");
 			} else if (currFreq < mLowFreq && (yOffset < (MapDivider.mapHeight - MapDivider.obstacleHeight))) {
 				yOffset++;
 				//System.out.println("Going down");
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
 	
 	//�NDRE TIL PASSENDE FORM
 	public boolean CalculateCollisions(Point[] form) {
 		if (updateCarCollisionBox) {
 			collisionBox[0].x = xOffset;
 			collisionBox[0].y = yOffset;
 			collisionBox[1].x = xOffset;
 			collisionBox[1].y = MapDivider.obstacleHeight + yOffset;
 			collisionBox[2].x = MapDivider.obstacleWidth + xOffset;
 			collisionBox[2].y = MapDivider.obstacleHeight + yOffset;
 			collisionBox[3].x = MapDivider.obstacleWidth + xOffset;
 			collisionBox[3].y = yOffset;
 			
 			updateCarCollisionBox = false;
 		}
 		if (collisionBox[0].y > form[2].y || collisionBox[1].y < form[0].y) {
 			return false;
 		}
 		if (collisionBox[0].x > form[3].x || collisionBox[2].x < form[1].x){
 			return false;
 		}  
 		
 		return true; 
 		
 	}
 	public void resetPosition(){
 		yOffset = MapDivider.mapYStart + MapDivider.obstacleSpace + MapDivider.totalObstacleHeight;
 		xOffset = 0;
 	}
 }
