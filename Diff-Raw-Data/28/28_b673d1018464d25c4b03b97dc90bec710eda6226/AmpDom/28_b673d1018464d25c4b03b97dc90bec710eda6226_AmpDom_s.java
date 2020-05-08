 package com.me.ampdom;
 
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Files.FileType;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.physics.box2d.World;
 
 public class AmpDom implements ApplicationListener {
 	/**
 	 * The time the last frame was rendered, used for throttling frame rate
 	 */
 	Sound damage;
 	Sound flyjar;
 	private long lastRender;
 	
 	private TiledMapHelper tiledMapHelper;
 	private Alabaster frog;
    
 	private World world;
 	boolean hit = false;
 	EnemyContact detect;
 	private Box2DDebugRenderer debugRenderer;
     public static final float PIXELS_PER_METER = 60.0f;
     private int screenWidth; // width of game screen
 	private int screenHeight; // width of game height
 	private LevelMap level;
 	
 	//menu 
 	private Menu m;
 	private OrthographicCamera camera;
 	public static int state = 0;
 	Sprite shellSprite;
 	Texture shellText;
 	SpriteBatch batch;
 	
 	// HUD
 	float shellElapsedTime;
 	float shoutElapsedTime;
 	float spitElapsedTime;
 	long shellBeginTime;
 	long shoutBeginTime; 
 	long spitBeginTime; 
 	float hitElapsedTime;
 	long hitBeginTime;
 
 	// detect keyboard
 	MyInputProcessor input = new MyInputProcessor();
 	float ElapsedTime;
 	long BeginTime;
 	
 	//background tex
 	Texture backgroundTex;
 
 	public AmpDom() {
 		super();
 
 		// Defer until create() when Gdx is initialized.
 		screenWidth = -1;
 		screenHeight = -1;
 	}
 
 	public AmpDom(int width, int height) {
 		super();
 
 		screenWidth = width;
 		screenHeight = height;
 	}
 
 	@Override
 	public void create() {
 		
 		Gdx.input.setInputProcessor(input);
 		
 		/**
 		 * If the viewport's size is not yet known, determine it here.
 		 */
 		if (screenWidth == -1) {
 			screenWidth = Gdx.graphics.getWidth();
 			screenHeight = Gdx.graphics.getHeight();
 		}
 		//batch
 		batch = new SpriteBatch();
 		//menu
 		camera = new OrthographicCamera(1, 600);
 		m = new Menu(camera);
 	
 		world = new World(new Vector2(0.0f, -10.0f), true);
 		detect = new EnemyContact();
 		world.setContactListener(detect);
 		
 		level = new LevelMap();		
 		level.create(world, level.currentLevel, screenWidth, screenHeight,detect);
 		
 		frog = new Alabaster(world, 1.0f, 5.0f);
         lastRender = System.nanoTime();
         debugRenderer = new Box2DDebugRenderer();		
 		
 		damage = Gdx.audio.newSound(Gdx.files.getFileHandle("data/sounds/hit.wav", FileType.Internal));
 		flyjar = Gdx.audio.newSound(Gdx.files.getFileHandle("data/sounds/jar.wav", FileType.Internal));
 		
 		tiledMapHelper = level.getMap();				
 		
 		//load background
 		backgroundTex = new Texture(Gdx.files.internal("data/background.png"));
 		
 		/*************************************************************/
 		//--read in enemy types and locations
 		/*MapBodyManager mapBodyManager = new MapBodyManager(world, PIXELS_PER_METER, "data/materials.xml", Logger.DEBUG);
 		mapBodyManager.createPhysics(tiledMapHelper.getMap());*/
 		
 		/*************************************************************/        
 	}
 
 	@Override
 	public void resume() {
 	}
 
 	// blink baster
 	public void getHit() {
 		hitBeginTime = System.nanoTime();
 		hitElapsedTime = (System.nanoTime() - hitBeginTime)/1000000000.0f;
 
 		//if(hitElapsedTime > 0 && hitElapsedTime < 3) {
 			//if((int)hitElapsedTime%2 != 0 )
 				frog.sprite.setColor(1f, 1f, 1f, 0.2f);
 			//else
 				//frog.sprite.setColor(1f, 1f, 1f, 1f);
 			hitBeginTime = System.nanoTime();
 		//}
 		
 	}
 	
 	@Override
 	public void render() {		
 		
 		//clear buffer to run faster
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 	
 		if(state >= 0 && state%2 == 0 && level.currentLevel%2 == 0){
 
 			//if(level.test.isPlaying() == false)
 			//level.test.play();
 			
 			//load background
 			batch.begin();
 			batch.draw(backgroundTex, 0, 0);
 			batch.end();
 
 			long now = System.nanoTime();			
 			
 			frog.move(input);
 			input.tick();
 			
 
 
 			//System.out.println(frog.entity.getPosition());
 
 			world.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);
 
 			//System.out.println(frog.entity.getPosition());
 			//camera follows player
 			tiledMapHelper.getCamera().position.x = PIXELS_PER_METER
 					* frog.entity.getPosition().x;
 			if(frog.entity.getPosition().y > 680) {
 				tiledMapHelper.getCamera().position.y = PIXELS_PER_METER
 						* frog.entity.getPosition().y;
 			}
 			
 			if(detect.enemyDmg && detect.isHit){
 				if(frog.shell) {
 					if(frog.shellCharge-1 >= 0) {
 						frog.shellCharge -= 1;
 						System.out.println("SHELL HEALTH: " + frog.shellCharge);
 						
 						// initiate timer
 						if(frog.shellCharge == 0)
 							shellBeginTime = System.nanoTime();
 					}
 				} else {
 					frog.takeDamage(10);
 					//frog.sprite.setColor(Color.RED);
 					
 					//frog.sprite.setColor(1f, 1f, 1f, .2f);
 					//long BeginTime = System.nanoTime();
 					//float ElapsedTime = (System.nanoTime() - BeginTime)/1000000000.0f;
 
 					//if(ElapsedTime >0 && ElapsedTime <5) {
 					//hitElapsedTime = System.nanoTime();
 					//if(hitElapsedTime > 0 && hitElapsedTime < 3)
 						getHit();//BeginTime = System.nanoTime();
 					//}
 					frog.icon.setColor(Color.RED);
 					detect.isHit = false;
 					damage.play();
 				}
 			}
 			
 			//System.out.println(frog.entity.getLinearVelocity().y);
 			if(detect.obstacleDmg && detect.isHit){
 				frog.takeDamage(100);	
 				detect.isHit = false;
 				damage.play();
 				System.out.println("youve been hit by spikes!"+ frog.getHealth());
 				}
 			
 			if(detect.collectJar){
 				 System.out.println("jar----"+LevelMap.jar.sprite.toString());
 				frog.incHealth(20);
 				flyjar.play();
 				LevelMap.jar.entity.setActive(false);
 				LevelMap.jar.sprite.setScale(0f,0f);
 				detect.collectJar= false;
 			}
 			
 			if(frog.getHealth()<=0){
 				frog.die();
 				world.destroyBody(frog.entity);
 				frog = new Alabaster(world, 1.0f, 5.0f);
 				frog.shell = false;
 				frog.shout = false;
 			}
 			
 			if(!frog.shell) {
 				//if(frog.shellHealth == 0) {
 					shellElapsedTime = (System.nanoTime() - shellBeginTime)/1000000000.0f;
 
 					if(shellElapsedTime > 2 && shellElapsedTime < 5 && frog.shellCharge <100) {
 						frog.shellCharge += 10;
 						shellBeginTime = System.nanoTime();
 					}
 			}
 			
 			if(frog.shout) {
 				if(frog.shoutCharge-20 >= 0) {
 					frog.shoutCharge -= 20;
 					System.out.println("SHOUT HEALTH: " + frog.shoutCharge);
 
 					// initiate timer
 					if(frog.shoutCharge == 0)
 						shoutBeginTime = System.nanoTime();
 				}
 			}
 
 			if(!frog.shout) {
 					shoutElapsedTime = (System.nanoTime() - shoutBeginTime)/1000000000.0f;
 
 					if(shoutElapsedTime > 5 && frog.shoutCharge <100) {
 						frog.shoutCharge += 20;
 						shoutBeginTime = System.nanoTime();
 						//shoutElapsedTime = (System.nanoTime() - shoutBeginTime)/1000000000.0f;
 					}
 			}
 
 			if(frog.spit) {
 				if(frog.spitCharge-20 >= 0) {
 					frog.spitCharge -= 20;
 
 					System.out.println("SPIT HEALTH: " + frog.spitCharge);
 
 					// initiate timer
 					if(frog.spitCharge == 0)
 						spitBeginTime = System.nanoTime();
 				}
 			}
 
 			if(!frog.spit) {
 					spitElapsedTime = (System.nanoTime() - spitBeginTime)/1000000000.0f;
 
 					if(spitElapsedTime > 5 && frog.spitCharge <100) {
 						frog.spitCharge += 20;
 						spitBeginTime = System.nanoTime();
 						//spitElapsedTime = (System.nanoTime() - spitBeginTime)/1000000000.0f;
 					}
 			}
 
 
 
 			
 			/**
 			 * Ensure that the camera is only showing the map, nothing outside.
 			 */
 			if (tiledMapHelper.getCamera().position.x < Gdx.graphics.getWidth() / 2) {
 				tiledMapHelper.getCamera().position.x = Gdx.graphics.getWidth() / 2;
 			}
 			if (tiledMapHelper.getCamera().position.x >= tiledMapHelper.getWidth()
 					- Gdx.graphics.getWidth() / 2) {
 				tiledMapHelper.getCamera().position.x = tiledMapHelper.getWidth()
 						- Gdx.graphics.getWidth() / 2;
 			}
 			
 			if (tiledMapHelper.getCamera().position.y < Gdx.graphics.getHeight() / 2) {
 				tiledMapHelper.getCamera().position.y = Gdx.graphics.getHeight() / 2;
 			}
 			if (tiledMapHelper.getCamera().position.y >= tiledMapHelper.getHeight()
 					- Gdx.graphics.getHeight() / 2) {
 				tiledMapHelper.getCamera().position.y = tiledMapHelper.getHeight()
 						- Gdx.graphics.getHeight() / 2;
 			}
 
			// need some array to load all creatures or something
 			tiledMapHelper.getCamera().update();
 			tiledMapHelper.render();
 
 
 			for(FlyingEnemy f : LevelMap.flyers){
 				if(detect.enemPos == f.entity.getPosition() && detect.spitEnem)
 				{
 					f.die();
 					detect.spitEnem = false;
 	          	}
 				
 
 				f.directPos = frog.entity;
 				if(Math.sqrt( Math.pow( frog.entity.getPosition().x - f.entity.getPosition().x , 2 )
 						+ Math.pow(frog.entity.getPosition().y - f.entity.getPosition().y , 2 ) ) <= 4) {
 					f.directMove = true;
 					f.spawnX = frog.entity.getPosition().x;
 					f.spawnY = frog.entity.getPosition().y;
 					
 				}
 				else {
 					f.directMove = false;
 					f.spawnX = f.spawnX0;
 					f.spawnY = f.spawnY0;
 				}
 				f.move();
 				f.batchRender(tiledMapHelper);
 			}
 			
 			for(Enemy e : LevelMap.enemies)
 			{
 				if(detect.enemPos == e.entity.getPosition())
 				{					
 					if(detect.spitEnem)
 					{
 						detect.spitEnem = false;
 						e.die();
 					}
 					
 					//alter enem vel
 					if(detect.shoutEnem)
 					{
 						detect.shoutEnem = false;
 						if(frog.facingRight)
 						{
 							e.entity.applyForce(new Vector2(5.0f, 0.0f), new Vector2(e.entity.getPosition().x, e.entity.getPosition().y));
 						}
 						else
 						{
 							e.entity.applyForce(new Vector2(-5.0f, 0.0f), new Vector2(e.entity.getPosition().x, e.entity.getPosition().y));
 						}
 					}
 				}
 				// ram movement
 //				if(Math.sqrt( Math.pow( frog.entity.getPosition().x - e.entity.getPosition().x , 2 )
 //						+ Math.pow(frog.entity.getPosition().y - e.entity.getPosition().y , 2 ) ) <= 4) {
 //						e.ramMoveRight();
 //				}
 				e.move();
 				e.batchRender(tiledMapHelper);
 			}
 			
 			for(Obstacle s : LevelMap.spikes){
 				s.move();
 				s.batchRender(tiledMapHelper);
 				s=null;
 			}
 			
 			for(MovingPlat m : LevelMap.plats){
 				m.move();
 				m.batchRender(tiledMapHelper);
 		
 			}
 			
 			frog.batchRender(tiledMapHelper);
 
 			if(level.currentLevel==0){//?
 			LevelMap.endlevelpt1.batchRender(tiledMapHelper);
 			
 			LevelMap.jar.batchRender(tiledMapHelper);
 			}
 			if(detect.endLevel){
 				  System.out.println("iio");
 				  detect.endLevel = false;
 				  try {
 					  Thread.sleep(1000);
 				} catch (InterruptedException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 				state++;
 				reset();
 				 
 			}
 			
 			now = System.nanoTime();
 			if (now - lastRender < 30000000) { // 30 ms, ~33FPS
 				try {
 					Thread.sleep(30 - (now - lastRender) / 1000000);
 				} catch (InterruptedException ie) {
 				}
 			}
 			else {
 				frog.sprite.setColor(1f, 1f, 1f, 1f);
 				//frog.sprite.setColor(Color.WHITE);
 				frog.icon.setColor(Color.WHITE);
 			}
 
 			//debugger
 		     debugRenderer.render(world,tiledMapHelper.getCamera().combined.scale(
 		    		 AmpDom.PIXELS_PER_METER,
 		    		 AmpDom.PIXELS_PER_METER,
 		    		 AmpDom.PIXELS_PER_METER));
 			lastRender = now;
 			
 			frog.displayHUD();
 			
 		}
 		
 
 		 m.display();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void dispose() {
 		m.dispose();
 		frog.dispose();
 	}
 	
 	public void reset(){		
 		level.currentLevel = state;
 		
 		level.deleteLevel();
 		
 	    world.destroyBody(frog.entity);
 
 	    world.destroyBody(LevelMap.jar.entity);
 	    world.destroyBody(LevelMap.endlevelpt1.entity);
 	  // world.destroyBody(LevelMap.plat.entity);
 	    if(state >= 0 && state%2 == 1 && level.currentLevel%2 == 1){    	
 		    level = new LevelMap();
 		    level.currentLevel = state + 1;
 		    world = new World(new Vector2(0.0f, -10.0f), true);
 			level.create(world, level.currentLevel, screenWidth, screenHeight,detect);
 	        tiledMapHelper = level.getMap();		
 	        frog = new Alabaster(world, 1.0f, 10.0f);
 	        lastRender = System.nanoTime();
 	    }
 	}
 }
