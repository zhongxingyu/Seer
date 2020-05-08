 package dk.aau.cs.giraf.cars.objects;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import android.graphics.Rect;
 import dk.aau.cs.giraf.cars.R;
 import dk.aau.cs.giraf.cars.gamecode.GameObjects.Car;
 import dk.aau.cs.giraf.cars.gamecode.GameInfo;
 import dk.aau.cs.giraf.cars.gamecode.GameRenderer;
 import dk.aau.cs.giraf.cars.gamecode.MapDivider;
 
 public class MicCar extends Car {
 	private final int mViewWidth;
 	private final int mViewHeight;
 	private final int mImgWidth;
 	private final int mImgHeight;
 	private int mStartY;
 	private int mEndY;
 	private int mLowFreq;
 	private int mHighFreq;
 	private int mCarScaledWidth;
 	private int mCarScaledHeight;
 	
 	public MicCar(int viewWidth, int viewHeight, int carWidth, int carHeight, int lowFreq, int highFreq) {
 		super(0, 0, null, null);   //Second argument is the speed of the car in the X direction
 		mViewWidth = viewWidth;
 		mViewHeight = viewHeight;
 		mImgWidth = carWidth;
 		mImgHeight = carHeight;
 		mLowFreq = lowFreq;
 		mHighFreq = highFreq;
 	}
 
 	@Override
 	public void draw(GL10 gl, GameRenderer spriteBatcher) {
 	
		mStartY = yOffset - (mViewWidth / 2);
 		mEndY = mStartY + mViewWidth;
 		mCarScaledWidth = mViewWidth;
 		mCarScaledHeight = mViewWidth;
 		
 		spriteBatcher.draw(gl, R.drawable.car, new Rect(0, 0, mImgWidth, mImgHeight), new Rect(0, mStartY , mCarScaledWidth, mEndY));
 
 	}
 
 	@Override
 	public void performWork() {		
 		int currFreq = GameInfo.getCurrFreq();
 		
 		if (currFreq > 0) {
 			if (currFreq > mHighFreq && yOffset - (mCarScaledHeight / 2) > MapDivider.mapYStart) {
 				yOffset -= 2;
 			} else if (currFreq < mLowFreq && yOffset + (mCarScaledHeight / 2) < MapDivider.mapYEnd) {
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
 
 }
