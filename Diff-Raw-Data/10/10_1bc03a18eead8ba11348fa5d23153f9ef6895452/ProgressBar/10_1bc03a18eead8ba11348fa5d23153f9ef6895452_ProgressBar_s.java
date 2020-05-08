 package com.testgame.sprite;
 
 import org.andengine.entity.primitive.Line;
 import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.Sprite;
 import org.andengine.util.adt.color.Color;
 
import android.util.Log;

 import com.testgame.scene.GameScene;
 
 public class ProgressBar extends Rectangle {
 	// ===========================================================
 	// Constants          
 	// ===========================================================
 	private static final float FRAME_LINE_WIDTH = 1f;
 	// ===========================================================          
 	// Fields         
 	// =========================================================== 
 	private final Line[] mFrameLines = new Line[4];
 	private final Rectangle mBackgroundRectangle;
 	private final Rectangle mProgressRectangle;
 	
 	private float mPixelsPerPercentRatio;
 
 	private final static int height = 64;
 	private final static int width = 50;
 	
 	private final float maxValue;
 	
 	// ===========================================================          
 	// Constructors          
 	// =========================================================== 
 	public ProgressBar(final GameScene gameScene, final float pX, final float pY, final float maxValue) {
 		super(pX, pY, width, height, gameScene.vbom);
 		setOffsetCenter(0, 0);
 		
 		this.mBackgroundRectangle = new Rectangle(0, 0, width, height, gameScene.vbom);
 		this.mBackgroundRectangle.setColor(Color.WHITE);
 		mBackgroundRectangle.setOffsetCenter(0, 0);
 		
 		this.mFrameLines[0] = new Line(0, 0, width, 0, FRAME_LINE_WIDTH, gameScene.vbom); //Top line.
 		this.mFrameLines[1] = new Line(width, 0, width, height, FRAME_LINE_WIDTH, gameScene.vbom); //Right line.
 		this.mFrameLines[2] = new Line(width, height, 0, height, FRAME_LINE_WIDTH, gameScene.vbom); //Bottom line.
 		this.mFrameLines[3] = new Line(0, height, 0, 0, FRAME_LINE_WIDTH, gameScene.vbom); //Left line.
 		
 		this.mProgressRectangle = new Rectangle(0, 0, width, height, gameScene.vbom);
 		mProgressRectangle.setOffsetCenter(0, 0);
 		
 		attachChild(this.mBackgroundRectangle); //This one is drawn first.
 		attachChild(this.mProgressRectangle); //The progress is drawn afterwards.
 		for(int i = 0; i < this.mFrameLines.length; i++) {
 			this.mFrameLines[i].setColor(Color.BLACK);
 			attachChild(this.mFrameLines[i]); //Lines are drawn last, so they'll override everything.
 		}
 		
 		this.maxValue = maxValue;
 		
 		this.mPixelsPerPercentRatio = (float) (height * 1f  / this.maxValue);
 		
 	}
 	// ===========================================================          
 	// Getter & Setter          
 	// =========================================================== 
 	public void setBackColor(final float pRed, final float pGreen, final float pBlue, final float pAlpha) {
 		this.mBackgroundRectangle.setColor(pRed, pGreen, pBlue, pAlpha);
 	}
 	public void setFrameColor(final float pRed, final float pGreen, final float pBlue, final float pAlpha) {
 		for(int i = 0; i < this.mFrameLines.length; i++)
 			this.mFrameLines[i].setColor(pRed, pGreen, pBlue, pAlpha);
 	}
 	public void setProgressColor(final float pRed, final float pGreen, final float pBlue, final float pAlpha) {
 		this.mProgressRectangle.setColor(pRed, pGreen, pBlue, pAlpha);
 	}
 	/**
 	 * Set the current progress of this progress bar.
 	 * @param pProgress is <b> BETWEEN </b> 0 - 100.
 	 */
 	public void setProgress(final float pProgress) {
 		if(pProgress < 0)
 			this.mProgressRectangle.setHeight(0); //This is an internal check for my specific game, you can remove it.
 		this.mProgressRectangle.setHeight(this.mPixelsPerPercentRatio * pProgress);
 	}
 	// ===========================================================          
 	// Methods for/from SuperClass/Interfaces          
 	// ===========================================================  
 	
 	// ===========================================================          
 	// Methods          
 	// ===========================================================  
 	
 	// ===========================================================          
 	// Inner and Anonymous Classes          
 	// ===========================================================  
 	
 }
