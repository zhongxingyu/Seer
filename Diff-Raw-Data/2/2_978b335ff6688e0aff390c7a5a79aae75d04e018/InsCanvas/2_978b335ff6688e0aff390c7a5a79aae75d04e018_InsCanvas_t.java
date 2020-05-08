 
 package com.inspedio.system.core;
 
 import javax.microedition.lcdui.Font;
 import javax.microedition.lcdui.Graphics;
 import javax.microedition.lcdui.Image;
 import javax.microedition.lcdui.game.GameCanvas;
 import javax.microedition.lcdui.game.Sprite;
 
 import com.inspedio.enums.InputType;
 import com.inspedio.enums.ScreenOrientation;
 import com.inspedio.system.helper.extension.InsPointerEvent;
 
 /**
  * <code>InsCanvas</code> is how engine render its graphic.<br>
  * It also function as an input detector.<br>
  * It is automatically created when you initialize <code>InsGame</code> instance<br>.
  * Please note that <code>InsCanvas</code> is a singleton class, which means it can have only one object<br>
  * 
  * @author Hyude
  * @version 1.0
  */
 public class InsCanvas extends GameCanvas{
 
 	private static InsCanvas instance = null;
 	
 	public static final int COLOR_BLACK = 0x000000;
 	public static final int COLOR_RED = 0xFF0000;
 	public static final int COLOR_GREEN = 0x00FF00;
 	public static final int COLOR_BLUE = 0x0000FF;
 	public static final int COLOR_WHITE = 0xFFFFFF;
 	public static final int COLOR_YELLOW = 0xCCCC33;
 	
 	private Image bufferImage;
 	public Graphics bufferGraphics;
 	
 	private ScreenOrientation displayMode;
 	private boolean rotateCanvas = false;
 	
 	public static final Font defaultFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
 	public static final Font infoFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
 	
 	public InsGame game;
 	protected Graphics graphic;
 	public int deviceWidth;
 	public int deviceHeight;
 	public ScreenOrientation deviceOrientation;
 	
 	public static InsCanvas getInstance(){
 		try
 		{
 			if(instance == null){
 				throw new Exception("InsCanvas instance is not initialized");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return instance;
 	}
 	
 	public static InsCanvas init(InsGame Game, ScreenOrientation Mode){
 		try
 		{
 			if(instance == null){
 				instance = new InsCanvas(Game, Mode);;
 			} else {
 				throw new Exception("InsCanvas instance already initialized");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return instance;
 	}
 	
 	
 	private InsCanvas(InsGame Game, ScreenOrientation Mode) {
 		super(false);
 		this.setFullScreenMode(true);
 		this.game = Game;
 		this.displayMode = Mode;
 		InsGlobal.hasTouchScreen = this.hasPointerEvents();
 		if(InsGlobal.hasTouchScreen){
 			InsGlobal.inputType = InputType.TOUCH;
 		} else {
 			InsGlobal.inputType = InputType.KEYPAD;
 		}
 		this.setScreenSize(getWidth(), getHeight());
 	}
 	
 	protected void sizeChanged(int width, int height)
 	{
		//this.setScreenSize(width, height);
 	}
 	
 	protected void setScreenSize(int width, int height)
 	{
 		this.rotateCanvas = false;
 		if(this.displayMode == ScreenOrientation.PORTRAIT){
 			if(width > height){
 				this.rotateCanvas = true;
 			}
 		} else if(this.displayMode == ScreenOrientation.LANDSCAPE){
 			if(width < height){
 				this.rotateCanvas = true;
 			}
 		} else if(this.displayMode == ScreenOrientation.DYNAMIC){
 			if(width <= height){
 				this.displayMode = ScreenOrientation.PORTRAIT;
 			} else {
 				this.displayMode = ScreenOrientation.LANDSCAPE;
 			}
 		}
 		
 		if(rotateCanvas){
 			InsGlobal.screenWidth = this.deviceWidth = height;
 			InsGlobal.screenHeight = this.deviceHeight = width;
 		} else {
 			InsGlobal.screenWidth = this.deviceWidth = width;
 			InsGlobal.screenHeight = this.deviceHeight = height;
 		}
 		
 		InsGlobal.screenOrientation = this.deviceOrientation = this.displayMode;
 		
 		this.bufferImage = Image.createImage(this.deviceWidth, this.deviceHeight);
 		this.bufferGraphics = this.bufferImage.getGraphics();
 		this.graphic = getGraphics();
 		
 		
 		InsGlobal.middleX = InsGlobal.screenWidth / 2;
 		InsGlobal.middleY = InsGlobal.screenHeight / 2;
 		
 		System.out.println("Screen Width : " + InsGlobal.screenWidth);
 		System.out.println("Screen Height : " + InsGlobal.screenHeight);
 		
 	}
 	
 	/**
 	 * Transform Coordinate touched depend on Orientation
 	 */
 	protected InsPointerEvent transformCoordinate(InsPointerEvent e){
 		if(rotateCanvas){
 			int tmpX = e.x;
 			int tmpY = e.y;
 			
 			e.x = tmpY;
 			e.y = this.deviceHeight - tmpX;
 		}
 		//System.out.println("Event at (" + e.x + "," + e.y + ")");
 		
 		return e;
 	}
 	
 	protected void pointerPressed(int X, int Y){
 		InsGlobal.pointer.addEvent(transformCoordinate(new InsPointerEvent(X, Y, InsPointerEvent.PRESSED)));
 	}
 	
 	protected void pointerReleased(int X, int Y){
 		InsGlobal.pointer.addEvent(transformCoordinate(new InsPointerEvent(X, Y, InsPointerEvent.RELEASED)));
 	}
 	
 	protected void pointerDragged(int X, int Y){
 		InsGlobal.pointer.addEvent(transformCoordinate(new InsPointerEvent(X, Y, InsPointerEvent.DRAGGED)));
 	}
 	
 	/**
 	 * Clear Screen. Self explanatory
 	 */
 	public void clearScreen()
 	{
 		this.bufferGraphics.setColor(InsGlobal.BG_COLOR);
 		this.bufferGraphics.fillRect(0, 0, this.deviceWidth, this.deviceHeight);
 	}
 	
 	/**
 	 * Draw current FPS into screen. Used only for debugging 
 	 */
 	public void drawFPS()
 	{
 		int offset = InsCanvas.defaultFont.getHeight();
 		this.bufferGraphics.setColor(InsGlobal.FPS_COLOR);
 		this.bufferGraphics.setFont(InsCanvas.defaultFont);
 		this.bufferGraphics.drawString("FPS : " + InsGlobal.stats.currentUPS + " / " + InsGlobal.stats.currentFPS + " / " + InsGlobal.stats.currentFrameSkip, 5, deviceHeight - (4 * offset), (Graphics.TOP|Graphics.LEFT));
 		this.bufferGraphics.drawString("Process : " + InsGlobal.stats.currentUpdateTime + " / " + InsGlobal.stats.currentRenderTime, 5, deviceHeight - (3 * offset), (Graphics.TOP|Graphics.LEFT));
 		this.bufferGraphics.drawString("Sleep : " + InsGlobal.stats.currentSleepTime, 5, deviceHeight - (2 * offset), (Graphics.TOP|Graphics.LEFT));
 		this.bufferGraphics.drawString("Time : " + InsGlobal.stats.currentGameTime, 5, deviceHeight - offset, (Graphics.TOP|Graphics.LEFT));
 	}
 	
 	protected void hideNotify()
 	{
 		InsGlobal.hideGame();
 	}
 	
 	protected void showNotify()
 	{
 		InsGlobal.showGame();
 	}
 	
 	protected void keyPressed(int keyCode)
 	{
 		super.keyPressed(keyCode);
 		switch (keyCode)
 		{
 			case -6:
 				InsGlobal.onLeftSoftKey();
 				break;
 			case -7:
 				InsGlobal.onRightSoftKey();
 				break;
 		}
 	}
 	
 	public void flushGraphics(){
 		if(rotateCanvas){
 			this.graphic.drawRegion(this.bufferImage, 0, 0, deviceWidth, deviceHeight, Sprite.TRANS_ROT90, 0, 0, Graphics.LEFT | Graphics.TOP);
 		} else{
 			this.graphic.drawRegion(this.bufferImage, 0, 0, deviceWidth, deviceHeight, Sprite.TRANS_NONE, 0, 0, Graphics.LEFT | Graphics.TOP);
 			//this.bufferGraphics.drawImage(this.bufferImage, 0, 0, Graphics.LEFT | Graphics.TOP);
 		}
 		super.flushGraphics();
 	}
 	
 	
 	
 
 }
