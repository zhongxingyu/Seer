 //File: GameGraphics.java
 //Purpose: Test graphics rendering class, as well as imitate a game engine.
 //Note: This class will just be renamed GameEngine at this point.
 
 package edu.sru.andgate.bitbot.graphics;
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import edu.sru.andgate.bitbot.Bot;
 import edu.sru.andgate.bitbot.R;
 import edu.sru.andgate.bitbot.gametypes.DungeonCrawl;
 import edu.sru.andgate.bitbot.gametypes.GameTypes;
 import edu.sru.andgate.bitbot.interpreter.InstructionLimitedVirtualMachine;
 import edu.sru.andgate.bitbot.tools.Constants;
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
 
 public class NickGameActivity extends Activity
 {	
 	private GameView glSurfaceView;
 	GlRenderer gameRenderer;
 	DrawableParticleEmitter particleEmitter;
 	Bot loadedBot;
 	MediaPlayer mp;
 	int[][] drawList;
 	int drawListPointer = 0;
 	boolean gameLoop = true;
 	String botFile, mapFile;
 	boolean touchEventFired = false;
 	float touchX = 0;
 	float touchY = 0;
 	float previousTouchX = 0;
 	float previousTouchY = 0;
 	float touchXLoc = 0;
 	float touchYLoc = 0;
 	ArrayList<DrawableBot> notifyOnTouchList;
 	
 	GameTypes gt;
 	public int kills;
 	public double accuracy;
 	
 	int MAX_OBJECTS = 250;
 	int NUM_TEST_BOTS = 0;
 	final int TYPE_BOT = 0;
 	final int TYPE_GUN = 1;
 	final int USER_BOT = 1;
 	final int ENEMY_BOT = 0;
 	public String missionType;
 	int viewType = 0;
 	
 	TileMap testMap;
 	CollisionManager collisionManager;
 	
 	TextView codeTxt;
 	ScrollView codeScroll;
 	
 	InstructionLimitedVirtualMachine ilvm = new InstructionLimitedVirtualMachine();
 		
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         kills = 0;
         accuracy = 0.0;
         //figure out what type of Game this is
         missionType = getIntent().getExtras().getString("GameType");
         botFile = getIntent().getExtras().getString("Bot");
         mapFile = getIntent().getExtras().getString("Map");
         if(missionType.equalsIgnoreCase("Dungeon Crawl")){
         	gt = new DungeonCrawl(this.getBaseContext(), testMap, mapFile, botFile);
         }
              
         // making it full screen
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         // Keep screen from shutting off
         getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         // requesting to turn the title OFF
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         
         mp = MediaPlayer.create(this.getBaseContext(), R.raw.neverland);
         mp.start();
         mp.setLooping(true);
         
         // Set the layout.
         //setContentView(R.layout.game_activity);
         
         notifyOnTouchList = new ArrayList<DrawableBot>(MAX_OBJECTS);
         
         //game types test
         gt.Initialize(this);
         testMap = gt.getMap();
         notifyOnTouchList.add(gt.getBot().getDrawableBot());
         
         // Initiate the Open GL view and create an instance with this activity
         //glSurfaceView = new GLSurfaceView(this);
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
         }
         else if(viewType == 2)
         {
         	setContentView(R.layout.game_activity_scrollview);
         	glSurfaceView = (GameView) findViewById(R.id.game_view);
         	codeTxt = (TextView) findViewById(R.id.code_txt);
 			codeScroll = (ScrollView) findViewById(R.id.code_scroll);
 			
     		Timer t = new Timer();
     		t.schedule(new TimerTask()
     		{
     			@Override
     			public void run()
     			{
     				codeTxt.post(new Runnable()
     				{
     					@Override
     					public void run()
     					{
     						codeTxt.append("\nmore code");
     						codeScroll.fullScroll(View.FOCUS_DOWN); 
     					}
     				});
     			}
     		}, 1000, 1000);
         }
                
         //Declare Collision Manager
         collisionManager = new CollisionManager(testMap);
                 
         //Declare Particle Emitter
         particleEmitter = new DrawableParticleEmitter();
         
         //Attach emitter to CollisionManager
         collisionManager.setParticleEmitter(particleEmitter);
 		
         //Declare Draw List
         drawList = new int[2][MAX_OBJECTS];       
         
                    
         gameRenderer = new GlRenderer(this.getBaseContext());
         
         ilvm.addInterpreter(gt.getBot().getInterpreter());			
 		// Run the vm every second.
		Timer t = new Timer();
 		t.schedule(new TimerTask()
 		{
 			@Override
 			public void run()
 			{
 				ilvm.resume(4);
 			}
 		}, 50, 50);
 		
         //Set renderer to be the main renderer with the current activity context
         glSurfaceView.setEGLConfigChooser(false);
         glSurfaceView.setRenderer(gameRenderer);
         glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
  
         gameRenderer.setTileMap(testMap);
         
         //Nick
         for(int i = 0; i < gt.getBots().length; i++){
         	addBotToWorld(gt.getBots()[i]);
         }
         
         addBotToWorld(gt.getBot());
         
 		//Tell camera to continuously update so it can follow user bot
 		gameRenderer.continuousCameraUpdate = true;
 		
 		gameRenderer.setParticleEmitter(particleEmitter);
         
         preloadTextures();							//Should always be called before game starts
         
         //Mass of bots
         
              
         gameRenderer.startSimulation();
         gameLoop();
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
     
     public void gameLoop()
     {
     	Runnable gR = new Runnable()
     	{
     		//Touch Event Vars
     		float xWaypoint = 0.0f;
     		float yWaypoint = 0.0f;
     		float xPercentage = 0.0f;
     		float yPercentage = 0.0f;
     		
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
     				touchX = glSurfaceView.touchX;
     				touchY = glSurfaceView.touchY;
     				if(touchX != previousTouchX || touchY != previousTouchY)
     				{
     					touchEventFired = true;
     					
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
     				
     				//check victory conditions
     				gt.Update();
     			    				
    				//gt.getBot().getDrawableBot().moveByTouch(0.1f);
    				gt.getBot().getDrawableBot().move();
     				//test2.move(0.0f, 0.05f);
     	    		gt.getBot().getBotLayer().setRotationAngle(gt.getBot().getDrawableBot().moveAngle-90);
     				
     	    		gt.getBot().getDrawableGun().update();
     	    		if(shotCount >= 10)
     	    		{
     	    			gt.getBot().getDrawableGun().fire();
     	    			shotCount = 0;
     	    		}    	    		
     	    		shotCount++;
     	    		
     				//Collision Detection Updater
     				collisionManager.update();
     				
     				//Particle Emitter Updater
     				particleEmitter.update();
     	    		
     				//Camera Stuff
     				gameRenderer.cameraX = gt.getBot().getDrawableBot().parameters[0];
     				gameRenderer.cameraY = gt.getBot().getDrawableBot().parameters[1];
     	            	        
     		        //Nick
     		        for(int i = 0; i < gt.getBots().length; i++){
     		        	if(gt.getBots()[i].getDrawableBot().isAlive)
     		        		addToDrawList(gt.getBots()[i]);
     		        }
     		        if(gt.getBot().getDrawableBot().isAlive)
 		        		addToDrawList(gt.getBot());
     		        
     		      /*
     		        //Nick loaded bot
 	    		        try{
 	    		        	if(loadedBot.getDrawableBot().isAlive)
 	    		        		addToDrawList(loadedBot);
 	    		        }catch (Exception e){
 	    		        	Log.v("BitBot", "Adding to drawlist failed");
 	    		        }
     		     */
     		        
     		        
     	            //Renderer Synchronization / Draw Frame Request
     	    		while(!thisFrameDrawn && gameLoop)
     	    		{
 	    	    		if(gameRenderer.getFrameDrawn())
 	    	    		{
 	    	    			gameRenderer.drawListSize = drawListPointer;
 	    	    			for(int i=0;i<2;i++)
 	    	    			{
 	    	    				for(int j=0;j<drawListPointer;j++)
 	    	    				{
 	    	    					gameRenderer.drawList[i][j] = drawList[i][j];
 	    	    				}
 	    	    			}
 	    	    			//gameRenderer.setFrameDrawn(false);
 	    	    			gameRenderer.frameDrawn = false;
 	    	    			glSurfaceView.requestRender();
 	    	    			thisFrameDrawn = true;
 	    	    			drawListPointer = 0;
 	    	    		}
     	    		}
     	    		
     	    		
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
 		mp.release();
 		gameLoop = false;
 		finish();
 	}
 }
