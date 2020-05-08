 package fr.umlv.escape.front;
 
 import fr.umlv.escape.Objects;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Rect;
 
 /**
   * Class that manage a scrolling background. The backGroundScroller can scroll an image in
   * the vertical or horizontal way.
   */
 public class BackGroundScroller {
 	Bitmap backgroundImage;
 	Rect screenRect;
 	Rect backgroundRect;
 	private final int backgroundHeight; // Optimization for not using getter
	private float scaleBackground;		// To adapt different ratio screen
 	
 	/**Constructor
 	 * @param heightScreen The height of the screen.
 	 * @param backGroundName The name of the background
 	 */
 	public BackGroundScroller(int widthScreen, int heightScreen,Bitmap backGroundImage){
 		if(heightScreen<0){
 			throw new IllegalArgumentException("height screen can't be negative");
 		}
 		Objects.requireNonNull(backGroundImage);
 		
 		this.backgroundImage=backGroundImage;
 		this.backgroundHeight = backGroundImage.getHeight();
		this.scaleBackground = heightScreen/widthScreen;
 		this.screenRect = new Rect(0,0,widthScreen,heightScreen);
 		this.backgroundRect = new Rect(0, backgroundHeight-heightScreen, backGroundImage.getWidth(), backgroundHeight);
 	}
 
 	/** Applies a vertical scroll step to the backGround.
 	  */
 	public void verticalScroll(){
 		backgroundRect.top-=2;
 		backgroundRect.bottom-=2;
 
 		if(backgroundRect.top<=0){
 			backgroundRect.top+=backgroundHeight-screenRect.bottom;
 			backgroundRect.bottom=backgroundHeight;
 		}
 	}
 	
 	public void onDrawBackground(Canvas canvas){
 		canvas.drawBitmap(backgroundImage, backgroundRect, screenRect, null);
 	}
 	
 	public void updateScreenSizes(int width, int height){
 		this.screenRect = new Rect(0,0,width,height);
 		this.backgroundRect = new Rect(0, backgroundHeight-height, this.backgroundImage.getWidth(), this.backgroundHeight);
 	}
 }
