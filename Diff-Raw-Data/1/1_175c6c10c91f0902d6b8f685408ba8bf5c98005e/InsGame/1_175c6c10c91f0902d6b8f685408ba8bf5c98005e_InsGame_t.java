 
 package com.inspedio.system.core;
 
 import java.util.Random;
 
 import javax.microedition.midlet.MIDlet;
 
 import com.inspedio.entity.InsState;
 import com.inspedio.enums.ScreenOrientation;
 import com.inspedio.system.helper.InsCache;
 import com.inspedio.system.helper.InsCamera;
 import com.inspedio.system.helper.InsKeys;
 import com.inspedio.system.helper.InsPointer;
 import com.inspedio.system.helper.InsSave;
 import com.inspedio.system.helper.InsStats;
 
 /**
  * <code>InsGame</code> is the core of Inspedio Engine.<br>
  * <code>InsGame</code> is the core machine that makes the whole engine running.<br>
  * To use it, call init() method to specify argument, and use getInstance() method to get its object.<br>
  * Please note that <code>InsGame</code> is a singleton class, which means it can have only one object<br>
  * 
  * @author Hyude
  * @version 1.0
  */
 public class InsGame implements Runnable {
 	
 	/**
 	 * Singleton Instance for InsGame Class
 	 */
 	private static InsGame instance = null;
 	
 	/**
 	 * Current game state.
 	 */
 	public InsState state;
 	/**
 	 * Current game canvas.
 	 */
 	public InsCanvas canvas;
 	/**
 	 * Default Loader State
 	 */
 	public InsLoader loader;
 	/**
 	 * Save Load helper
 	 */
 	public InsSave saveload;
 	/**
 	 * Set this to TRUE to force close the game
 	 */
 	public boolean stop;	
 	/**
 	 * If a state change was requested, the new state object is stored here until we switch to it
 	 */
 	public InsState requestedState;
 	/**
 	 * Total number of milliseconds elapsed since the game start
 	 */
 	public long totalTime;
 	/**
 	 * Time stamp marking when game start
 	 */
 	public long beginTime;
 	/**
 	 * How much update you want each second. Standard = 25 fps
 	 */
 	public int idealFPS;
 	/**
 	 * Milliseconds of time per frame of game loop. 25 FPS = 40ms
 	 */
 	protected int framePeriod;
 	/**
 	 * Maximum render skipped
 	 */
 	protected int maxframeSkip;
 	/**
 	 * A flag for keeping track of whether a game reset was requested or not.
 	 */
 	public boolean requestedReset = false;;
 	/**
 	 * Marker for detecting LeftsoftKey Press
 	 */
 	public boolean leftSoftKeyPressed = false;
 	/**
 	 * Marker for detecting RightSoftKey Press
 	 */
 	public boolean rightSoftKeyPressed = false;
 	/**
 	 * Marker for detecting SwitchState request
 	 */
 	public boolean switchStateRequested = false;
 	/**
 	 * Whether SwitchState should use Loader
 	 */
 	public boolean switchStateUseLoader = false;
 	
 	
 	public static InsGame getInstance(){
 		try
 		{
 			if(instance == null){
 				throw new Exception("InsGame instance is not initialized");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return instance;
 	}
 		
 	/**
 	 * Initialize InsGame, InsCanvas, InsGlobal, and InsState
 	 * 
 	 * @param	Midlet				Reference to Main Class (extends midlet), useful for exit and command
 	 * @param	InitialState		Initial State to begin game (usually MenuState, or IntroState)
 	 * @param	FPS					Frame Per Second. How much time the game updated each second (standard = 30 update per second)
 	 * @param	MaxFrameSkip		Maximum frame skip allowed. Standard = 5
 	 * @param	Loader				<code>InsLoader</code> instance used for Assets loading when creating state
 	 * @param	SaveLoad			<code>InsSave</code> instance used for save load data into RecordStore	
 	 * @param	ScreenOrientation	Either PORTRAIT or LANDSCAPE
 	 * 
 	 * @return	Reference to <code>InsGame</code> instance
 	 */
 	public static InsGame init(MIDlet Midlet, InsState InitialState, int FPS, int MaxFrameSkip, InsLoader Loader, InsSave SaveLoad, ScreenOrientation Mode){
 		try
 		{
 			if(instance == null){
 				instance = new InsGame(Midlet, InitialState, FPS, MaxFrameSkip, Loader, SaveLoad, Mode);
 			} else {
 				throw new Exception("InsGame instance already initialized");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return instance;
 	}
 	
 	
 	private InsGame(MIDlet Midlet, InsState InitialState, int FPS, int MaxFrameSkip, InsLoader Loader, InsSave SaveLoad, ScreenOrientation Mode)
 	{
 		initGame(Mode);
 		InsGlobal.midlet = Midlet;
 		InsGlobal.save = SaveLoad;
 		this.requestedState = InitialState;
 		this.loader = Loader;
		this.loader.create();
 		this.loader.init();
 		
 		this.idealFPS = FPS;
 		this.framePeriod = (int) (1000 / this.idealFPS);
 		
 		this.totalTime = 0;
 		
 		this.maxframeSkip = MaxFrameSkip;
 
 		this.requestedReset = false;
 		this.leftSoftKeyPressed = false;
 		this.rightSoftKeyPressed = false;
 		this.stop = false;
 		
 		this.switchState(false);
 	}
 	
 	/**
 	 * 
 	 */
 	private void initGame(ScreenOrientation Mode)
 	{
 		this.canvas = InsCanvas.init(this, Mode);
 		InsGlobal.game = this;
 		InsGlobal.canvas = this.canvas;
 		InsGlobal.graphic = this.canvas.bufferGraphics;
 		InsGlobal.keys = new InsKeys();
 		InsGlobal.pointer = new InsPointer();	
 		InsGlobal.camera = new InsCamera();
 		InsGlobal.cache = new InsCache();
 		InsGlobal.stats = InsStats.getInstance();
 		InsGlobal.randomizer = new Random(System.currentTimeMillis());
 		InsGlobal.initGlobal();
 	}
 	
 	/**
 	 * This method invoke thread and calling run. Call this method from your main
 	 */
 	public void start()
 	{	
 		Thread t = new Thread(this);
 		t.start();
 		
 		System.out.println("Thread Started");
 	}
 	
 	
 	
 	/**
 	 * This is game loop, which executed every frames.<br>
 	 * To makes performance stable, the game loop implemented using.<br>
 	 * constant game speed with maximum FPS paradigm.<br>
 	 * It means, it will update X times per second constantly, while render whenever there are enough time.<br>
 	 * So the rendering can be less depends on how heavy the update are.<br>
 	 * 
 	 * Note : You can set <code>running</code> to false to force close the game anytime
 	 */
 	public void run() {
 		long elapsedTime = 0;
 		int surplusTime = 0;				// Positive if process on time or have surplus time. Negative when late
 		int frameLate = 0;					// How much Frame Cycle is currently being late
 		this.beginTime = System.currentTimeMillis();
 		
 		while(!this.stop){
 			try
 			{
 				// Mark the Beginning of Cycle
 				this.beginTime = System.currentTimeMillis();
 					
 				// Check whether Cycle in On time or being Late
 				if(surplusTime >= 0){
 					// Cycle is On time, sleep equal to surplus time
 					if(surplusTime > 0){
 						InsGlobal.stats.sleepCount += surplusTime;
 						Thread.sleep(surplusTime);
 					}
 					this.update();
 					this.render();
 					this.nextCycle();	
 				} else {
 					// Cycle is being late, check whether cycle is late more than 1 frame
 					frameLate = surplusTime / this.framePeriod;
 					
 					if ((frameLate >= 1) && (frameLate < this.maxframeSkip)) {	
 						// Frame is late more than 1 Frame, but lower than Max Frame Skip
 						// update equal to frame late
 						for(int i = 0; i < frameLate; i++){
 							this.update();
 							this.nextCycle();
 						}
 						InsGlobal.stats.frameSkipCount += frameLate;
 						surplusTime -= frameLate * this.framePeriod;
 						System.out.println("FRAME LATE BY " + frameLate);
 					} else if(frameLate >= this.maxframeSkip){
 						// Frame is late more than Max Frame Skip. Force skip frame up to MAXFRAMESKIP
 						// Reset any debtTime no matter how much
 						frameLate = this.maxframeSkip;
 						for(int i = 0; i < frameLate; i++){
 							this.update();
 							this.nextCycle();
 						}
 						InsGlobal.stats.frameSkipCount += frameLate;
 						surplusTime = 0;
 						System.out.println("FRAME LATE BY " + frameLate);
 					}
 					this.update();
 					this.render();
 					this.nextCycle();
 				}
 					
 				elapsedTime = System.currentTimeMillis() - this.beginTime;
 				surplusTime += (this.framePeriod - elapsedTime);
 				
 				
 			}catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 	}
 	
 	/**
 	 * Game update involving in updateInput from user, and calling update for current game state
 	 */
 	public void update()
 	{
 		try
 		{
 			long curtime = System.currentTimeMillis();
 			this.updateKeyState();
 			this.updatePointerState();
 			if(!this.state.deleted)
 			{
 				if(this.switchStateRequested)
 				{
 					this.switchState(this.switchStateUseLoader);
 					this.switchStateRequested = false;
 					this.switchStateUseLoader = false;
 				}
 				if(this.leftSoftKeyPressed)
 				{
 					this.state.onLeftSoftKey();
 					this.leftSoftKeyPressed = false;
 				}
 				if(this.rightSoftKeyPressed)
 				{
 					this.state.onRightSoftKey();
 					this.rightSoftKeyPressed = false;
 				}
 				this.state.preUpdate();
 				this.state.update();
 				this.state.postUpdate();
 			}
 			InsGlobal.stats.updateCount++;
 			InsGlobal.stats.updateTime += (System.currentTimeMillis() - curtime);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Update KeyEvent State (Pressed, Released, etc)
 	 */
 	public void updateKeyState()
 	{
 		InsGlobal.keys.updateKeyState(InsGlobal.canvas.getKeyStates());
 	}
 	
 	/**
 	 * Update PointerEvent State (Pressed, Released, etc)
 	 */
 	public void updatePointerState()
 	{
 		InsGlobal.pointer.updatePointerState();
 	}
 	
 	/**
 	 * rendering all needed graphic for current gamestate
 	 */
 	public void render()
 	{
 		try
 		{
 			long curtime = System.currentTimeMillis();
 			this.canvas.clearScreen();
 			if(!this.state.deleted)
 			{
 				this.state.draw(InsGlobal.graphic);
 			}
 			if(InsGlobal.displayFPS)
 			{
 				this.canvas.drawFPS();
 			}
 			this.canvas.flushGraphics();
 			InsGlobal.stats.renderCount++;
 			InsGlobal.stats.renderTime += (System.currentTimeMillis() - curtime);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Switch state into given state.
 	 * If useLoader is TRUE, didn't create the state (let the loader create it)
 	 */
 	public void switchState(boolean useLoader)
 	{
 		if(useLoader)
 		{
 			this.state.destroy();
 			this.state = null;
 			this.loader.start();
 			this.state = this.loader;
 		}
 		else
 		{
 			this.requestedState.create();
 			this.requestedState.finishCreate();
 			this.state = this.requestedState;
 			this.requestedState = null;
 		}
 	}
 	
 	/**
 	 * Attempt to go to next cycle
 	 * Calculating scheduled time, realFPS, etc
 	 * 
 	 * Displaying current UPS, FPS, and GameTime
 	 */
 	protected void nextCycle()
 	{
 		InsGlobal.cycleCount++;
 		long curtime = System.currentTimeMillis();
 		
 		if((curtime - InsGlobal.stats.gameTimeMark) >= 1000)
 		{
 			InsGlobal.stats.gameTimeMark += 1000;
 			//InsGlobal.stats.gameTimeMark = curtime;
 			InsGlobal.stats.timeCount++;
 			
 			InsGlobal.stats.calculateStats();
 			InsGlobal.stats.resetStats();
 		}
 	}
 	
 		
 	
 	
 	
 	
 }
