 //File: GameGraphics.java
 //Purpose: Test graphics rendering class, as well as imitate a game engine.
 //Note: This class will just be renamed GameEngine at this point.
 
 package edu.sru.andgate.bitbot.graphics;
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.media.MediaPlayer;
 import android.opengl.GLSurfaceView;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import edu.sru.andgate.bitbot.Bot;
 import edu.sru.andgate.bitbot.R;
 import edu.sru.andgate.bitbot.gametypes.BotVsBot;
 import edu.sru.andgate.bitbot.gametypes.DungeonCrawl;
 import edu.sru.andgate.bitbot.gametypes.GameTypes;
 import edu.sru.andgate.bitbot.gametypes.TutorialTesting;
 import edu.sru.andgate.bitbot.interpreter.InstructionLimitedVirtualMachine;
 
 public class NickGameActivity extends Activity
 {	
 	private GameView glSurfaceView;
 	private Timer t;
 	private GlRenderer gameRenderer;
 	private DrawableParticleEmitter particleEmitter;
 	private MediaPlayer mp;
 	private int[][] drawList;
 	private int drawListPointer = 0;
 	private boolean gameLoop = true;
 	private String botFile, mapFile;
 	private float touchX = 0;
 	private float touchY = 0;
 	private float previousTouchX = 0;
 	private float previousTouchY = 0;
 	private ArrayList<DrawableBot> notifyOnTouchList;
 	
 	private GameTypes gameType;
 	public int numShotsFired, numBulletsContact, kills;
 	
 	private int MAX_OBJECTS = 250;
 	private final int TYPE_BOT = 0;
 	private final int TYPE_GUN = 1;
 	public String missionType;
 	private int viewType = 0;
 	
 	//TileMap testMap;
 	private CollisionManager collisionManager;
 	
 	private TextView codeTxt;
 	private ScrollView codeScroll;
 	
 	private InstructionLimitedVirtualMachine ilvm = new InstructionLimitedVirtualMachine();
 	
 	// Used to globably access the current game.
 	public static NickGameActivity currentGame = null;
 	public NickGameActivity()
 	{
 		currentGame = this;
 	}
 	
 	
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         kills = 0;
         numBulletsContact = 0;
         numShotsFired = 0;
         
         
         //figure out what type of Game this is
         missionType = getIntent().getExtras().getString("GameType");
         botFile = getIntent().getExtras().getString("Bot");
         mapFile = getIntent().getExtras().getString("MapFile");
         viewType = getIntent().getExtras().getInt("ViewType", 0);
 
         if(missionType.equalsIgnoreCase("BOT versus BOT")){
         	gameType = new BotVsBot(this,mapFile, botFile);
         }else if(missionType.equalsIgnoreCase("Dungeon Crawl")){
         	gameType = new DungeonCrawl(this, mapFile, botFile);
         }else if(missionType.equalsIgnoreCase("Tutorial")){
         	int numOfBots = getIntent().getExtras().getInt("BotNum");
         	gameType = new TutorialTesting(this, numOfBots, botFile);
         }
         
         gameType.Initialize(this);
              
         // making it full screen
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         // Keep screen from shutting off
         getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         // requesting to turn the title OFF
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         
         mp = MediaPlayer.create(this.getBaseContext(), R.raw.neverland);
         mp.start();
         mp.setLooping(true);
         
         notifyOnTouchList = new ArrayList<DrawableBot>(MAX_OBJECTS);
         
         if(viewType == 0)
         {
         	setContentView(R.layout.game_activity);
         	glSurfaceView = (GameView) findViewById(R.id.game_view);
         }
         else if(viewType == 1)
         {
         	setContentView(R.layout.game_activity_scrollview);
         	glSurfaceView = (GameView) findViewById(R.id.game_view);
         	codeTxt = (TextView) findViewById(R.id.code_txt);
         	codeScroll = (ScrollView) findViewById(R.id.code_scroll);
         	gameType.getBot().getInterpreter().setOutputTextView(codeTxt);
 	    	codeTxt.setText(gameType.getBot().getInterpreter().getBotLog());
         }
                
         notifyOnTouchList.add(gameType.getBot().getDrawableBot());
         
         //Declare Collision Manager
         collisionManager = new CollisionManager(gameType.getMap());
                 
         //Declare Particle Emitter
         particleEmitter = new DrawableParticleEmitter();
         
         //Attach emitter to CollisionManager
         collisionManager.setParticleEmitter(particleEmitter);
 		
         //Declare Draw List
         drawList = new int[2][MAX_OBJECTS];       
         
         gameRenderer = new GlRenderer(this);
         
         ilvm.addInterpreter(gameType.getBot().getInterpreter());
 		
 //        // Run the vm every second.
 //		t = new Timer();
 //		t.schedule(new TimerTask()
 //		{
 //			@Override
 //			public void run()
 //			{
 //				ilvm.resume(4);
 //			}
 //		}, 50, 50);
 		
         //Set renderer to be the main renderer with the current activity context
         glSurfaceView.setEGLConfigChooser(false);
         glSurfaceView.setRenderer(gameRenderer);
         glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
  
         gameRenderer.setTileMap(gameType.getMap());
         
         //Nick
         for(int i = 0; i < gameType.getBots().length; i++){
         	addBotToWorld(gameType.getBots()[i]);
         }
         
         addBotToWorld(gameType.getBot());
         
 		//Tell camera to continuously update so it can follow user bot
 		gameRenderer.continuousCameraUpdate = true;
 		
 		gameRenderer.setParticleEmitter(particleEmitter);
         
         preloadTextures();							//Should always be called before game starts
      
         gameRenderer.startSimulation();
         startGameLoop();
     }
     
     public void addBotToWorld(Bot bot)
     {
     	gameRenderer.addObjectToWorld(bot);
     	collisionManager.addCollisionDetector(bot);
     }
     
     public void preloadTextures()
     {
     	gameRenderer.setPreloadTextureFlag(true);
     	glSurfaceView.requestRender();
     }
     
     public void addToDrawList(int type, int objectID)
     {
     	drawList[0][drawListPointer] = type;		//Set Object Type
     	drawList[1][drawListPointer] = objectID;	//Set Object ID
     	drawListPointer++;							//Increment Draw List Pointer / (Size-1)
     }
       
     public void addToDrawList(Bot bot)
     {
     	drawList[0][drawListPointer] = TYPE_BOT;
     	drawList[1][drawListPointer] = bot.getDrawableBot().ID;
     	drawListPointer++;
     	
     	drawList[0][drawListPointer] = TYPE_BOT;
     	drawList[1][drawListPointer] = bot.getBotLayer().ID;
     	drawListPointer++;
     	
     	drawList[0][drawListPointer] = TYPE_GUN;
     	drawList[1][drawListPointer] = bot.getDrawableGun().ID;
     	drawListPointer++;
     }
     
     public void startGameLoop()
     {
     	Runnable gR = new Runnable()
     	{
     		//Proof of concept variables
     		int shotCount = 0;
     		boolean thisFrameDrawn = false;
     		
     		//Testing FPS Only
     		long startTime = 0;
     		long endTime = 0;
     		long timeCount = 0;
     		int frameCount = 0;
     		
     		
     		//Game Loop
     		public void run()
     		{
     			while(gameLoop)
     			{
     				startTime = System.currentTimeMillis();
     				//IMPORTANT VARIABLE FOR RENDERER SYNCHRONIZATION
     				thisFrameDrawn = false;
     				
     				//Handle touch events
     				HandleTouchEvents();
     				
     				//check victory conditions
 
     				gameType.Update();
     				   			    				
     				//gameType.getBot().getDrawableBot().moveByTouch(0.1f);
     				gameType.getBot().getDrawableBot().move();
     				//gameType.getBot().getBotLayer().setRotationAngle(gameType.getBot().getDrawableBot().moveAngle-90);
     				
     				// Run Interpreter
 //    				ilvm.resume(30);
    				ilvm.resume(4);			
 
     				//update players bot's gun
     	    		if(gameType.getBot().getDrawableBot().isAlive)
     	    		{
     	    			gameType.getBot().getDrawableGun().update();
     	    		}
     	    		
     	    		//update all other bot's gun's
     	    		for(int i = 0; i < gameType.getBots().length; i++)
     	    		{
     	    			if(gameType.getBots()[i].getDrawableBot().isAlive)
     	    			{
     	    				gameType.getBots()[i].getDrawableGun().update();
     	    			}
     	    		}
     	    		
 //    	    		//no?
 //    	    		if(shotCount >= 10)
 //    	    		{
 //    	    			if(gameType.getBot().getDrawableBot().isAlive)
 //    	    			{
 //    	    				gameType.getBot().getDrawableGun().fire();
 //    	    			}
 //    	    			for(int i = 0; i < gameType.getBots().length; i++)
 //    	    			{
 //    	    				if(gameType.getBots()[i].getDrawableBot().isAlive)
 //    	    				{
 //    	    					gameType.getBots()[i].getDrawableGun().fire();
 //    	    				}
 //        	    		}
 //    	    			numShotsFired++;
 //    	    			shotCount = 0;
 //    	    		}    	    		
 //    	    		shotCount++;
 
     	    		    	    		
     				//Collision Detection Updater
     				collisionManager.update();
     				
     				//Particle Emitter Updater
     				particleEmitter.update();
     	    		
     				//Camera Stuff
     				gameRenderer.cameraX = gameType.getBot().getDrawableBot().parameters[0];
     				gameRenderer.cameraY = gameType.getBot().getDrawableBot().parameters[1];
     	            	        
     		        // Add bots to the drawlist if they are alive
     		        for (int i = 0; i < gameType.getBots().length; i++)
     		        	if(gameType.getBots()[i].getDrawableBot().isAlive)
     		        		addToDrawList(gameType.getBots()[i]);
     		        
     		        // Add the player to the drawlist if he is alive.
     		        if (gameType.getBot().getDrawableBot().isAlive)
 		        		addToDrawList(gameType.getBot());
     		        
     	            //Renderer Synchronization / Draw Frame Request
     	    		while (!thisFrameDrawn && gameLoop)
     	    		{
 	    	    		if(gameRenderer.getFrameDrawn())
 	    	    		{
 	    	    			gameRenderer.drawListSize = drawListPointer;
 	    	    			
 	    	    			for(int i=0;i<2;i++)
 	    	    				for(int j=0;j<drawListPointer;j++)
 	    	    					gameRenderer.drawList[i][j] = drawList[i][j];
 	    	    			
 	    	    			//gameRenderer.setFrameDrawn(false);
 	    	    			gameRenderer.frameDrawn = false;
 	    	    			glSurfaceView.requestRender();
 	    	    			thisFrameDrawn = true;
 	    	    			drawListPointer = 0;
 	    	    		}
 	    	    		
 	    	    		// If we're waiting on the gameRenderer, should the thread pause
 	    	    		// to let other stuff happen?
 //	    	    		try {Thread.sleep(1);}
 //	    	    		catch (InterruptedException e) {}
     	    		}
     	    		
     	    		// Count up the number of frames and every second print that number
     	    		// out and reset the count.
     	    		endTime = System.currentTimeMillis();
     	    		timeCount += (endTime-startTime);
     	    		frameCount++;
     	    		if(timeCount >= 1000.0)
     	    		{
     	    			Log.v("bitbot", "FPS: " + frameCount);
     	    			frameCount = 0;
     	    			timeCount = 0;
     	    		}
     	    		
     			}
     		}
     	};
     	
     	Thread gT = new Thread(gR);
     	gT.setName("Game Thread: " + gT.getName());
     	gT.start();
     }
     
     /**
      * This method is called once every frame and handles touch events (obviously)
      */
     private void HandleTouchEvents()
     {
     	if (glSurfaceView.touchNeedsToBeHandled)
     	{
 			//Touch Event Vars
 			float xWaypoint = 0.0f;
 			float yWaypoint = 0.0f;
 			float xPercentage = 0.0f;
 			float yPercentage = 0.0f;
 			
 	    	touchX = glSurfaceView.touchX;
 			touchY = glSurfaceView.touchY;
 			
 			if(true || touchX != previousTouchX || touchY != previousTouchY)
 			{
 				//Convert to game world waypoints
 				xPercentage = (touchX/gameRenderer.screenWidth);
 				yPercentage = ((touchY/gameRenderer.screenHeight));
 				
 				xWaypoint = (gameRenderer.drawLeft - (xPercentage * (gameRenderer.drawLeft - gameRenderer.drawRight)));
 				yWaypoint = (gameRenderer.drawTop - (yPercentage * (gameRenderer.drawTop - gameRenderer.drawBottom)));
 				
 				//Notify bots that require it
 				for(int i=0;i<notifyOnTouchList.size();i++)
 				{
 					notifyOnTouchList.get(i).onTouchEvent(xWaypoint, yWaypoint);
 				}
 			}
 			
 			previousTouchX = touchX;
 			previousTouchY = touchY;
 			
 			// Flag that we handled this touch event.
 			glSurfaceView.touchNeedsToBeHandled = false;
     	}
     }
     
     
     public GameTypes getGameType(){
     	return this.gameType;
     }   
     
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		glSurfaceView.onResume();
 		preloadTextures(); //Reload textures on resume to prevent missing texture / white square problem.
 	}
 
 	@Override
 	protected void onPause()
 	{
 		super.onPause();
 		glSurfaceView.onPause();
 	}
 	
 	@Override
 	protected void onDestroy()
 	{
 		super.onDestroy();
 		
 		if (mp != null)
 			mp.release();
 		
 		gameLoop = false;
 		
 		if (t != null)
 			t.cancel();
 		
 		if (ilvm != null)
 			ilvm.stop();
 		
 		finish();
 	}
 }
