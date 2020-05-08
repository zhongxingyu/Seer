 //File: GameGraphics.java
 //Purpose: Test graphics rendering class, as well as imitate a game engine.
 //Note: This class will just be renamed GameEngine at this point.
 
 package edu.sru.andgate.bitbot.graphics;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import edu.sru.andgate.bitbot.Bot;
 import edu.sru.andgate.bitbot.R;
 import edu.sru.andgate.bitbot.gametypes.DungeonCrawl;
 import edu.sru.andgate.bitbot.interpreter.InstructionLimitedVirtualMachine;
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
 
 public class GameActivity extends Activity
 {	
 	private GameView glSurfaceView;
 	GlRenderer gameRenderer;
 	DrawableParticleEmitter particleEmitter;
 	Bot loadedBot;
 	DungeonCrawl dc;
 	MediaPlayer mp;
 	DrawableBot test;
 	DrawableBot test2;
 	DrawableBot test3;
 	DrawableGun testGun;
 	//Drawable bot4,bot5,bot6,bot7,bot8,bot9,bot10;
 	DrawableBot enemyBot, enemyBot2, enemyBot3, enemyBot4;
 	BotLayer enemyTurret, enemyTurret2, enemyTurret3, enemyTurret4;
 	BotLayer testTurret;
 	BotLayer test2Turret;
 	int[][] drawList;
 	int drawListPointer = 0;
 	boolean gameLoop = true;
 	boolean touchEventFired = false;
 	float touchX = 0;
 	float touchY = 0;
 	float previousTouchX = 0;
 	float previousTouchY = 0;
 	float touchXLoc = 0;
 	float touchYLoc = 0;
 	
 	int MAX_OBJECTS = 250;
 	
 	DrawableBot[] testBotArray;
 	int NUM_TEST_BOTS = 0;
 	
 	final int TYPE_BOT = 0;
 	final int TYPE_GUN = 1;
 	
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
         
         testBotArray = new DrawableBot[NUM_TEST_BOTS];
         
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
         
         //Declare Tile Map
         testMap = new TileMap();
         
         //Declare Collision Manager
         collisionManager = new CollisionManager(testMap);
         
         //Declare Particle Emitter
         particleEmitter = new DrawableParticleEmitter();
         
         //Attach emitter to CollisionManager
         collisionManager.setParticleEmitter(particleEmitter);
 		
         //Declare Draw List
         drawList = new int[2][MAX_OBJECTS];       
         
         //Test Bot 1
         test = new DrawableBot();
         test.setTranslation(-3.5f,0.1f,-5.0f);
         test.addTexture(R.drawable.sand);	//TextureID = 0
         collisionManager.addCollisionDetector(test);
         
         
         //Test Bot 1 Turret Layer
         testTurret = new BotLayer(test);
         testTurret.addTexture(R.drawable.sandturret);
         testTurret.setRotationAngle(test.parameters[3]);
         //testTurret.setTranslation(0.0f,0.0f,-6.0f);
         
         //Test Bot 2
         test2 = new DrawableBot();
         test2.attachCollisionSound(this.getBaseContext(), R.raw.bot_wall_collision);
         test2.setTranslation(3.5f,0.1f,-5.0f);
         test2.setRotation(180.0f,0.0f,0.0f,-5.0f);
         test2.addTexture(R.drawable.adambot);	//TextureID = 0
         test2.moveStepSize = 0.10f;
         collisionManager.addCollisionDetector(test2);        
         
         //Test Bot 2 Turret Layer
         test2Turret = new BotLayer(test2);
         test2Turret.addTexture(R.drawable.adamturret);
         test2Turret.setRotationAngle(test2.parameters[3]);
         
         //Test gun
         testGun = new DrawableGun(test2,test2Turret);
         testGun.addTexture(R.drawable.bulletnew);
         collisionManager.addCollisionDetector(testGun);
         
         
         //More test bots
         enemyBot = new DrawableBot();
         enemyBot.setTranslation(7.5f,3.0f,-5.0f);
         enemyBot.addTexture(R.drawable.adambot);
         collisionManager.addCollisionDetector(enemyBot);
         enemyTurret = new BotLayer(enemyBot);
         enemyTurret.addTexture(R.drawable.adamturret);
         enemyTurret.setRotationAngle(enemyBot.parameters[3]);
         enemyBot.addTexture(R.drawable.adambotd1);	//1
         enemyBot.addTexture(R.drawable.adambotd2);	//2
         enemyBot.addDamageTextureToSequence(1, 50);
         enemyBot.addDamageTextureToSequence(2, 25);
         
         enemyBot2 = new DrawableBot();
         enemyBot2.setTranslation(5.5f,2.5f,-5.0f);
         enemyBot2.addTexture(R.drawable.adambot);
         collisionManager.addCollisionDetector(enemyBot2);
         enemyTurret2 = new BotLayer(enemyBot2);
         enemyTurret2.addTexture(R.drawable.adamturret);
         enemyTurret2.setRotationAngle(enemyBot2.parameters[3]);
         enemyBot2.addTexture(R.drawable.adambotd1);	//1
         enemyBot2.addTexture(R.drawable.adambotd2);	//2
         enemyBot2.addDamageTextureToSequence(1, 50);
         enemyBot2.addDamageTextureToSequence(2, 25);
         
         enemyBot3 = new DrawableBot();
         enemyBot3.setTranslation(10.5f,-4.5f,-5.0f);
         enemyBot3.addTexture(R.drawable.adambot);
         collisionManager.addCollisionDetector(enemyBot3);
         enemyTurret3 = new BotLayer(enemyBot3);
         enemyTurret3.addTexture(R.drawable.adamturret);
         enemyTurret3.setRotationAngle(enemyBot3.parameters[3]);
         enemyBot3.addTexture(R.drawable.adambotd1);	//1
         enemyBot3.addTexture(R.drawable.adambotd2);	//2
         enemyBot3.addDamageTextureToSequence(1, 50);
         enemyBot3.addDamageTextureToSequence(2, 25);
         
         enemyBot4 = new DrawableBot();
         enemyBot4.setTranslation(-11.5f,4.5f,-5.0f);
         enemyBot4.addTexture(R.drawable.adambot);
         collisionManager.addCollisionDetector(enemyBot4);
         enemyTurret4 = new BotLayer(enemyBot4);
         enemyTurret4.addTexture(R.drawable.adamturret);
         enemyTurret4.setRotationAngle(enemyBot4.parameters[3]);
         enemyBot4.addTexture(R.drawable.adambotd1);	//1
         enemyBot4.addTexture(R.drawable.adambotd2);	//2
         enemyBot4.addDamageTextureToSequence(1, 50);
         enemyBot4.addDamageTextureToSequence(2, 25);
               
         gameRenderer = new GlRenderer(this.getBaseContext());
 
         try{
         	String botFile = getIntent().getExtras().getString("Bot");
         	Log.v("BitBot", botFile);
 	        loadedBot = Bot.CreateBotFromXML(this.getBaseContext(), botFile);
 	        addBotToWorld(loadedBot);
 	        addToDrawList(loadedBot);
 	        ilvm.addInterpreter(loadedBot.getInterpreter());
 	        Log.v("BitBot", "Got Here 1");
 			
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
         }catch (Exception e){
         	Log.v("BitBot", "Bot not loaded");
         }
         
         
         //Set renderer to be the main renderer with the current activity context
         glSurfaceView.setEGLConfigChooser(false);
         glSurfaceView.setRenderer(gameRenderer);
         glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
         
         //Mass Test Bots
         for(int i=0;i<NUM_TEST_BOTS;i++)
         {
         	testBotArray[i] = new DrawableBot();
         	testBotArray[i].addTexture(R.drawable.red);
         	testBotArray[i].setTranslation(2.0f,0.0f,-5.0f);
         	gameRenderer.addObjectToWorld(testBotArray[i]);
         }
         
 //        /* Comment out to turn off TileMap()
        // testMap = new TileMap();
         testMap.loadMapFile("testarena.map", this.getBaseContext());  
         
         //game types test
         dc = new DungeonCrawl(this.getBaseContext(), 4, "testarena.map", "enemy_bot.zml");
         dc.Initialize();
         
         gameRenderer.setTileMap(testMap);
         
         //testMap.addTexture(R.drawable.stone);
         //gameRenderer.setTileMap(testMap);
         //*/
         
         //Add test objects to world
         gameRenderer.addObjectToWorld(testTurret);
         gameRenderer.addObjectToWorld(test);
         gameRenderer.addObjectToWorld(test2);
         gameRenderer.addObjectToWorld(test2Turret);
         gameRenderer.addObjectToWorld(testGun);
         gameRenderer.addObjectToWorld(enemyTurret);
         gameRenderer.addObjectToWorld(enemyBot);
         gameRenderer.addObjectToWorld(enemyTurret2);
         gameRenderer.addObjectToWorld(enemyBot2);
         gameRenderer.addObjectToWorld(enemyTurret3);
         gameRenderer.addObjectToWorld(enemyBot3);
         gameRenderer.addObjectToWorld(enemyTurret4);
         gameRenderer.addObjectToWorld(enemyBot4);
         //gameRenderer.addObjectToWorld(test3);
                 
         //Connect draw list to Renderer
         //gameRenderer.setDrawList(drawList);
         
 		//Tell camera to continuously update so it can follow user bot
 		gameRenderer.continuousCameraUpdate = true;
 		
 		gameRenderer.setParticleEmitter(particleEmitter);
         
         preloadTextures();							//Should always be called before game starts
         
         //Mass of bots
         
         for(int i=0;i<NUM_TEST_BOTS;i++)
         {
         	addToDrawList(TYPE_BOT,testBotArray[i].ID);
         }
         
         gameRenderer.startSimulation();
         gameLoop();
     }
     
     public void addBotToWorld(Bot bot)
     {
     	gameRenderer.addObjectToWorld(bot);
     	collisionManager.addCollisionDetector(loadedBot);
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
     		//Proof of concept variables
     		float move = 0.01f;
     		float rotate = 1.0f;
     		int shotCount = 0;
     		boolean goinUp = true;
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
     					Log.v("bitbot", Float.toString(touchX) + " , " + Float.toString(touchY));
     				}
     				previousTouchX = touchX;
     				previousTouchY = touchY;
     				
     				//Handle 360 Degree Turret Rotation
     				rotate += 1.0f;
     				if(rotate >= 360)
     				{
     					rotate = 0.0f;
     				}
     				
     				//Make some other stuff happen
     	    		test.setTranslation(-3.5f,(0.01f + move),-5.0f);
     	    		//test2.setTranslation(3.5f,(-1*(0.01f + move)),-5.0f);
     	    		test2.move();
     	    		//test2.move(45.0f, 0.1f);
     	    		test2Turret.setRotationAngle(rotate);
     	    		
     	    		testGun.update();
     	    		if(shotCount >= 10)
     	    		{
     	    			testGun.fire();
     	    			shotCount = 0;
     	    		}    	    		
     	    		shotCount++;
     	    		
     	    		if(goinUp)
     	    		{
     	    			move += 0.03f;
     	    			if(move >= 0.0f)
     	    			{
     	    				goinUp = false;
     	    				test.setRotation(180.0f,0.0f,0.0f,-5.0f);
     	    				testTurret.setRotationAngle(test.parameters[3]);
     	    				//test2.setRotation(0.0f,0.0f,0.0f,-5.0f);
     	    				//test3.setSelectedTexture(0);
     	    			}
     	    		}
     	    		else
     	    		{
     	    			move -= 0.03f;
     	    			if(move <= -5.5f)
     	    			{
     	    				goinUp = true;
     	    				test.setRotation(0.0f,0.0f,0.0f,-5.0f);
     	    				testTurret.setRotationAngle(test.parameters[3]);
     	    				//test2.setRotation(180.0f,0.0f,0.0f,-5.0f);
     	    				//test3.setSelectedTexture(1);
     	    			}
     	    		}
     	    		
     				//Collision Detection Updater
     				collisionManager.update();
     				
     				//Particle Emitter Updater
     				particleEmitter.update();
     	    		
     				//Camera Stuff
     				gameRenderer.cameraX = test2.parameters[0];
     				gameRenderer.cameraY = test2.parameters[1];
     	            
     	            for(int i=0;i<NUM_TEST_BOTS;i++)
     	            {
     	            	addToDrawList(TYPE_BOT,testBotArray[i].ID);
     	            }
     				
     	            if(test.isAlive)
     	            {
     	            	addToDrawList(TYPE_BOT,test.ID);
     	            	addToDrawList(TYPE_BOT,testTurret.ID);
     	            }
     	            if(enemyBot.isAlive)
     	            {
     	            	addToDrawList(TYPE_BOT,enemyBot.ID);
     	            	addToDrawList(TYPE_BOT,enemyTurret.ID);
     	            }
     	            if(enemyBot2.isAlive)
     	            {
     	            	addToDrawList(TYPE_BOT,enemyBot2.ID);
     	            	addToDrawList(TYPE_BOT,enemyTurret2.ID);
     	            }
     	            if(enemyBot3.isAlive)
     	            {
     	            	addToDrawList(TYPE_BOT,enemyBot3.ID);
     	            	addToDrawList(TYPE_BOT,enemyTurret3.ID);
     	            }
     	            if(enemyBot4.isAlive)
     	            {
     	            	addToDrawList(TYPE_BOT,enemyBot4.ID);
     	            	addToDrawList(TYPE_BOT,enemyTurret4.ID);
     	            }
     	            
     		        addToDrawList(TYPE_BOT,test2.ID);
     		        addToDrawList(TYPE_BOT,test2Turret.ID);
     		        addToDrawList(TYPE_GUN, testGun.ID); 	
     		        
     		        /*
    		        //Nick loaded bot
    		        try{
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
