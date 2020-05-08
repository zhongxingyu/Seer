 package com.gamegear.firstwing.screens;
 
 import java.util.Iterator;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.input.GestureDetector;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.Filter;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.Joint;
 import com.badlogic.gdx.physics.box2d.Manifold;
 import com.badlogic.gdx.physics.box2d.ContactImpulse;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.actions.Actions;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.Window;
 import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.Timer;
 import com.badlogic.gdx.utils.Timer.Task;
 import com.gamegear.firstwing.ActorMgr;
 import com.gamegear.firstwing.BobController;
 import com.gamegear.firstwing.FirstWing;
 import com.gamegear.firstwing.WorldRenderer;
 import com.gamegear.firstwing.actors.*;
 import com.gamegear.firstwing.actors.Actor.ActorState;
 import com.gamegear.firstwing.actors.json.StaticActor;
 import com.gamegear.firstwing.helper.Helper;
 import com.gamegear.firstwing.levels.Level;
 import com.gamegear.firstwing.levels.Spawner;
 import com.gamegear.firstwing.levels.json.LevelProperties.ColorEnum;
 
 public class GameScreen extends MenuScreen {
 
 	public enum GameState{
 		Begin,
 		Paused,
 		Running
 	}
 	
 	public World 			world;
 	public Level			level;
 	public WorldRenderer 	renderer;
 	public BobController	controller;
 	public GestureDetector 	gestureDetector;
 	
 	//Interface
 	public Texture 			interfaceTexture;
 	public ShapeRenderer 	interfaceRenderer;
 	public BitmapFont 		font;
 	public BitmapFont 		popupFont;
 	public String			notificationMessage;
 	public boolean			notificationEnabled = false;
 	
 	public boolean			markedForRestart = false;
 	public FreeTypeFontGenerator 	fontGenerator;
 	public Array<Actor>		actorsForRemoval;
 	public int				levelPath;
 	
 	private boolean 		loaded = false;
 	
 	// Bullets
 	private Array<Bullet> 	bullets;
 	long					timeSinceLastBullet;
 	
 	long					timeSinceDamage = 0;
 	
 	InputMultiplexer im;
 	
 	private int width, height;
 	private boolean finished;
 	private GameState currentState;
 	
 	public GameScreen(FirstWing game, int levelPath)
 	{
 		super(game);
 		this.levelPath = levelPath;
 		FirstWing.stats.levelID = levelPath;
 		load(levelPath);
 	}
 	
 	@Override
 	public void show() {
 		super.show();
 		
 		// Input
 		Gdx.input.setCatchBackKey(true);
 		controller = new BobController(this, width, height);
 		gestureDetector = new GestureDetector(20, 0.5f, 1, 0.15f, controller);
 		im = new InputMultiplexer(controller, gestureDetector);
 		Gdx.input.setInputProcessor(im);
 		
 		if (!FirstWing.audio.isEnabled()) {
 			FirstWing.audio.enableMusic();
 		}
 	}
 	
 	public void load(int levelPath)
 	{
 		Gdx.app.log("GameLoad", "Started to load");
 		this.currentState = GameState.Begin;
 		this.levelPath = levelPath;
 		FirstWing.stats.levelID = levelPath;
 		// Bullet array
 		bullets = new Array<Bullet>();
 		bullets.ensureCapacity(20);
 
 		// Rendering
 		createWorld(String.valueOf(levelPath));
 		//firstWing.setScreen(firstWing.extLevelScreen);
 
 		renderer = new WorldRenderer(this, false);
 
 		fontGenerator = new FreeTypeFontGenerator(
 				Gdx.files.internal("ui/TiresiasScreenfont.ttf"));
 		font = fontGenerator
 				.generateFont((int) (16 * Gdx.graphics.getDensity()));
 		popupFont = fontGenerator.generateFont((int) (40 * Gdx.graphics
 				.getDensity()));
 		fontGenerator.dispose();
 
 		interfaceTexture = new Texture(Gdx.files.internal("images/dpad.png"));
 		interfaceRenderer = new ShapeRenderer();
 		
 		// Contact listener
 		createCollisionListener();
 		this.actorsForRemoval = new Array<Actor>();
 		loaded = true;
 		Gdx.app.log("GameLoad", "Finished loading");
 	}
 	
 	public void loadLevel(int levelPath)
 	{
 		this.levelPath = levelPath;
 		FirstWing.stats.levelID = levelPath;
 		this.currentState = GameState.Begin;
 		
 		// Bullet array
 		bullets.clear();
 		this.actorsForRemoval.clear();
 
 		// Rendering
 		//world.dispose();
 		try{
 			createWorld(String.valueOf(levelPath));
 			Gdx.input.setInputProcessor(this.im);
 		}
 		catch(Exception ex)
 		{
 			Gdx.app.log("From game", ex.getMessage());
 			firstWing.setScreen(firstWing.extLevelScreen);
 		}
 		renderer.reset();
 
 		// Contact listener
 		createCollisionListener();
 		System.gc();
 		
 		FirstWing.stats.currentColor = ColorEnum.none;
 		renderer.changeAfterBurnerColor(FirstWing.stats.currentColor);
 	}
 	
 	public void createWorld(String levelPath) {
 		if (world == null && level == null) {
 			this.world = new World(new Vector2(0, 0), true);
 			this.level = new Level(this, levelPath);
 		} else {
 			//clearWorld();
 			this.level.dispose();
 			world.setContactListener(null);
 			this.world.dispose();
 			this.world = new World(new Vector2(0, 0), true);
 			this.level.loadLevel(levelPath);
 		}
 		System.gc();
 	}
 	
 	public void clearWorld()
 	{	       
 		Iterator<Joint> joints = world.getJoints();
 		while (joints.hasNext()) {
 			Joint j = joints.next();
 			if (j != null)
 				world.destroyJoint(j);
 		}
 
 		Iterator<Body> bodies = world.getBodies();
 		while (bodies.hasNext()) {
 			Body b = bodies.next();
 			if (b != null)
 				b.setUserData(null);
 			world.destroyBody(b);
 		}
 		world.setContactListener(null);
 	}
 	
 	public GameState getCurrentState(){
 		return this.currentState;
 	}
 	
 	public void setCurrentState(GameState state){
 		this.currentState = state;
 	}
 
 	@Override
 	public void render(float delta) {
 		super.render(delta);
 		//Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
 		//Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		removeBodies();
 		if(this.finished){
 			this.finished = false;
 			
 			if(this.firstWing.platformInterface.getSignedIn())
 			{
 				// Completed first level
 				if(this.levelPath == 1)
 				{
 					this.firstWing.platformInterface.unlockAchievement("CgkIhpLNkp8BEAIQBA");
 				}
 				
 				FirstWing.stats.checkColorAchievement();
 			}
 			
 			FirstWing.stats.checkStars(level.getProperties().StarOne, level.getProperties().StarTwo);
 			this.stage.addActor(this.getVictoryWindow());
 			Gdx.input.setInputProcessor(stage);
 			FirstWing.stats.resetScore(true);
 			this.currentState = GameState.Paused;
 		}
 		
 		if(this.markedForRestart)
 		{
 			this.stage.addActor(this.getDeathWindow());
 			Gdx.input.setInputProcessor(stage);
 			this.currentState = GameState.Paused;
 			this.markedForRestart = false;
 		}
 		
 		if(this.currentState == GameState.Running){
 			
 			//Update input
 			this.controller.update(delta);
 			this.checkPlayerBounds();
 			
 			//Update Bob speed
 			this.level.getPlayer().getBody().setLinearVelocity(controller.linImpulseX + level.getSpeed(),controller.linImpulseY);
 			
 			//Update bullets
 			this.checkBulletFire();
 			this.moveEnemies();
 		}
 			
 		this.stage.getSpriteBatch().setProjectionMatrix(this.renderer.getCam().combined);
 		
 		//Render frame
 		this.renderer.render(this.currentState, this.stage.getSpriteBatch());
 	
 		this.stage.draw();
 		
 		//Render interface
 		this.renderInterface(this.stage.getSpriteBatch(), false);
 		//renderFPS();
 		
 		//Handle playlist
 		if(FirstWing.audio.isEnabled())
 		{
 			FirstWing.audio.handleMusic();
 		}
 	}
 	
 	public void moveEnemies() {
 		Iterator<Spawner> it = level.getSpawners().iterator();
 		while (it.hasNext()) {
 			Spawner spawner = it.next();
 			if (spawner.getPosition().x - 6 < renderer.cameraX) {
 				spawner.Spawn();
 			} else {
 				return;
 			}
 
 			if (spawner.getPosition().x < renderer.cameraX) {
 				it.remove();
 			}
 		}
 	}
 	
 	public void checkPlayerBounds()
 	{
 		if(renderer.cameraX - 4f >= level.getPlayer().getPosition().x && -(controller.dpadCenterX - controller.dpadX) < 0){ 
 			controller.linImpulseX = 0;
 		}
 		else if(renderer.cameraX - 5f >= level.getPlayer().getPosition().x){
 			markedForRestart = true;
 		}
 		else{
 			//controller.linImpulseX = 0;
 		}
 		if(renderer.cameraX + 4f <= level.getPlayer().getPosition().x && -(controller.dpadCenterX - controller.dpadX) > 0){
 			controller.linImpulseX = 0;
 		}
 		else if(renderer.cameraX + 5f <= level.getPlayer().getPosition().x){
 			markedForRestart = true;
 		}
 		else{
 			//controller.linImpulseX = 0;
 		}
 	}
 	
 	public void renderInterface(SpriteBatch batch, boolean showFPS)
 	{
 		interfaceRenderer.begin(ShapeType.FilledRectangle);
 			interfaceRenderer.setColor(Color.DARK_GRAY);
 			interfaceRenderer.filledRect(0, height - (30 * Gdx.graphics.getDensity()), width, height);
 			interfaceRenderer.setColor(Helper.colorEnumToColor(FirstWing.stats.currentColor));
 			interfaceRenderer.filledRect(this.stage.getWidth() * 0.6f, height - (30 * Gdx.graphics.getDensity()), ((renderer.cameraX + 5)/level.getProperties().FinishX)*this.stage.getWidth()*0.2f+5, height);
 			interfaceRenderer.setColor(Helper.colorEnumToColor(ColorEnum.none));
 			interfaceRenderer.filledRect(this.stage.getWidth() * 0.8f, height - (30 * Gdx.graphics.getDensity()), 5, height);
 		interfaceRenderer.end();
 		
 		interfaceRenderer.begin(ShapeType.Line);
 			interfaceRenderer.setColor(Helper.colorEnumToColor(FirstWing.stats.currentColor));
 			interfaceRenderer.line(0, height - (30 * Gdx.graphics.getDensity()), width, height - (30 * Gdx.graphics.getDensity()));
 		interfaceRenderer.end();
     
 		batch.begin();
 		font.setScale(1);
 		font.draw(batch, "Score: " + FirstWing.stats.getScore(), 10, height -10);
 		font.draw(batch, "Highscore: " + FirstWing.stats.getHighScore(levelPath), this.stage.getWidth() * 0.2f, height -10);
 		float shipHealth = (this.level.getPlayer().getHealth() / this.level.getPlayer().getInitialHealth()) * 100;
 		font.draw(batch, "Health: " + (int)shipHealth + "%", this.stage.getWidth() * 0.4f, height -10);
 		
 		if(this.currentState == GameState.Begin){
 			this.popupFont.draw(batch, "Tap to start", (this.stage.getWidth() * 0.5f) - (this.popupFont.getSpaceWidth() * 6f), this.stage.getHeight() * 0.75f);
 		}
 		
 		if(notificationEnabled)
 		{
 			popupFont.setColor(Helper.colorEnumToColor(ColorEnum.none));
 			popupFont.draw(batch, notificationMessage,(int) (width / 2 - popupFont.getBounds(notificationMessage).width / 2), height / 1.2f);
 		}
 		
 		if(showFPS)
 		{
 			font.draw(batch, "FPS:" + Gdx.graphics.getFramesPerSecond(), width - 50, height -10);
 		}
 		
 		if(controller.getDpadCenterX() > 0 && this.currentState == GameState.Running)
 		{
 			batch.draw(interfaceTexture, controller.getDpadCenterX() - interfaceTexture.getWidth()/4, controller.getDpadCenterY() - interfaceTexture.getHeight()/4, interfaceTexture.getWidth()/2, interfaceTexture.getHeight()/2);
 			batch.draw(interfaceTexture, controller.getDpadX() - interfaceTexture.getWidth()/2, controller.getDpadY() - interfaceTexture.getHeight()/2, interfaceTexture.getWidth(), interfaceTexture.getHeight());
 		}
 		
 		batch.end();
 	}
 	
 	public void setNotification(String message, int durationInSeconds)
 	{
 		//Set message
 		notificationMessage = message;
 		notificationEnabled = true;
 		
 		//Stop previous timer
 		Timer.instance.clear();
 		
 		//Hide notification after set time
 		Timer.schedule(new Task(){
 		    @Override
 		    public void run() {
 		        notificationEnabled = false;
 		    }
 		}, durationInSeconds);
 	}
 	
 	public void checkBulletFire()
 	{
 		int maxBullets = 10;
 		
 		// Maximum bullets on screen
         if(bullets.size > maxBullets)
         {
         	this.actorsForRemoval.add(bullets.get(0));
         	bullets.removeIndex(0);
         }
         
         // Remove bullets off screen
         for(Bullet b : bullets)
         {
         	if(b.getBody().getWorldCenter().x > renderer.cameraX + (renderer.getCam().viewportWidth / 2))
         	{
         		this.actorsForRemoval.add(b);
        		b.setState(ActorState.DYING, true);
         		bullets.removeValue(b, true);
         	}
         }
 		
 		//Bullet delay in seconds
 		float bulletDelay = 0.8f;
 		
 		Filter filter = new Filter();
 		filter.categoryBits = 1;
 		filter.groupIndex = -2;
 		
 		float elapsedTime = (System.nanoTime() - timeSinceLastBullet) / 1000000000.0f;
         if(elapsedTime > bulletDelay){
         	timeSinceLastBullet = System.nanoTime();
         	Bullet temp = new Bullet(this.level.getPlayer().getBody().getWorldPoint(new Vector2(0.8f,0)), world, filter, FirstWing.stats.currentColor);
         	temp.getBody().setBullet(true);
         	temp.getBody().setLinearVelocity(10,0);
         	bullets.add(temp);
         	FirstWing.audio.sounds.get("laser").play(FirstWing.options.getVolume());
         }
 	}
 	
 	private void createCollisionListener() {
         world.setContactListener(new ContactListener() {
             @Override
             public void beginContact(Contact contact) {
                 Fixture fixtureA = contact.getFixtureA();
                 Fixture fixtureB = contact.getFixtureB();
                 Enemy collisionEnemy = null;
                 Bullet collisionBullet = null;
                 Orb collisionOrb = null;
                 Bob collisionBob = null;
                 Block collisionBlock = null;
                 AlienHead collisionAlienHead = null;
                 
                 Actor actorA = (Actor)fixtureA.getBody().getUserData();
                 Actor actorB = (Actor)fixtureB.getBody().getUserData();
                 if(actorA instanceof Enemy){
                 	collisionEnemy = (Enemy)actorA;
                 } else if (actorB instanceof Enemy){
                 	collisionEnemy = (Enemy)actorB;
                 }
                 
                 if(actorA instanceof Orb){
                 	collisionOrb = (Orb)actorA;
                 } else if (actorB instanceof Orb){
                 	collisionOrb = (Orb)actorB;
                 }
                 
                 if(actorA instanceof Bullet){
                 	collisionBullet = (Bullet)actorA;
                 } else if (actorB instanceof Bullet){
                 	collisionBullet = (Bullet)actorB;
                 }
                 
                 if(actorA instanceof Bob){
                 	collisionBob = (Bob)actorA;
                 } else if(actorB instanceof Bob){
                 	collisionBob = (Bob)actorB;
                 }
                 
                 if(actorA instanceof Block){
                 	collisionBlock = (Block)actorA;
                 } else if(actorB instanceof Block){
                 	collisionBlock = (Block)actorB;
                 }
                 
                 if(actorA instanceof AlienHead){
                 	collisionAlienHead = (AlienHead)actorA;
                 } else if(actorB instanceof AlienHead){
                 	collisionAlienHead = (AlienHead)actorB;
                 }
                 
                 // Als een bullet in aanraking komt
                 if(collisionBullet != null)
                 {
                 	if(collisionEnemy != null){
                 		collisionEnemy.setHealth(collisionEnemy.getHealth() - 5);
                 		if(collisionEnemy.getHealth() <= 0)
                     	{
                     		if(!actorsForRemoval.contains(collisionEnemy, true))
                         	{
                     			renderer.callParticleSystem(collisionEnemy.getBody().getWorldCenter().x, collisionEnemy.getBody().getWorldCenter().y);
                     			FirstWing.audio.sounds.get("explosion").play(FirstWing.options.getVolume());
                     			FirstWing.stats.addScore(10f);
                     			collisionEnemy.setState(ActorState.DYING, true);
                     			actorsForRemoval.add(collisionEnemy);
                         	}
                     	}
                 	} else if(collisionBlock != null && collisionBlock.isBreakable()){
                 		collisionBlock.setHealth(collisionBlock.getHealth() - 2f, collisionBullet.getColor());
                 		if(collisionBlock.getHealth() <= 0){
                     		if(!actorsForRemoval.contains(collisionBlock, true))
                         	{
                     			collisionBlock.setState(ActorState.DYING, true);
                     			actorsForRemoval.add(collisionBlock);
                     			FirstWing.audio.sounds.get("explosion").play(FirstWing.options.getVolume());
                         	}
                     	}
                 	}
                 	
                 	if(!actorsForRemoval.contains(collisionBullet, true))
                 	{
                		collisionBullet.setState(ActorState.DYING, true);
                 		actorsForRemoval.add(collisionBullet);
                 	}
                 }
                 
                 // Als het schip in aanraking komt
                 if(collisionBob != null){
                 	if(collisionBlock != null){
                 		switch(((StaticActor)collisionBlock.getProperties()).Type){
 							case Breakable:
 								collisionBob.setHealth(0f);
 								break;
 							case Finish:
 								finished = true;
 								break;
 							case Tile:
 								if(timeSinceDamage + 100 < System.currentTimeMillis()){
 									collisionBob.setHealth(collisionBob.getHealth() - 1f);
 									timeSinceDamage = System.currentTimeMillis();
 								}
 								break;
 							default:
 								break;
         				}
                 	} else if(collisionOrb != null){
                 		if(FirstWing.stats.addScore(collisionOrb.getColor(), collisionOrb.getPoints()))
                 		{
                 			renderer.changeAfterBurnerColor(collisionOrb.getColor());
                 			setNotification("Changed color!", 2);
                 		}
                 		else
                 		{
                 			setNotification("Combo x" + FirstWing.stats.comboOrbs + "!" , 2);
                 		}
                 		
                 		if(!actorsForRemoval.contains(collisionOrb, true)){
                 			collisionOrb.setState(ActorState.DYING, true);
                     		actorsForRemoval.add(collisionOrb);
                     	}
                 	} else if(collisionOrb == null && collisionBullet == null && collisionAlienHead == null && timeSinceDamage + 1000 < System.currentTimeMillis()){
                 		timeSinceDamage = System.currentTimeMillis();
                 		collisionBob.setHealth(collisionBob.getHealth() - 5);
                 	} else if(collisionAlienHead != null){
                 		FirstWing.stats.setTrophy(true);
                 		if(!actorsForRemoval.contains(collisionAlienHead, true)){
                 			collisionAlienHead.setState(ActorState.DYING, true);
                 			actorsForRemoval.add(collisionAlienHead);
                 		}
                 	}
                 	
                 	if(collisionBob.getHealth() <= 0){
                 		markedForRestart = true;
                 		FirstWing.audio.sounds.get("explosion").play(FirstWing.options.getVolume());
                 	}
                 }
             }
 
             @Override
             public void endContact(Contact contact) {
                 //Fixture fixtureA = contact.getFixtureA();
                 //Fixture fixtureB = contact.getFixtureB();
                 //Gdx.app.log("endContact", "between " + fixtureA.toString() + " and " + fixtureB.toString());
             }
 
             @Override
             public void preSolve(Contact contact, Manifold oldManifold) {
             }
 
             @Override
             public void postSolve(Contact contact, ContactImpulse impulse) {
             }
 
         });
     }
 	
 	public void removeBodies()
 	{
 		if(!world.isLocked())
 		{
 			for(int i = 0; i < this.actorsForRemoval.size; i++){
 				Actor actor = this.actorsForRemoval.pop();
 				if(actor.getState() == ActorState.DEAD){
 					if(actor instanceof Enemy){
 						Enemy enemy = (Enemy) actor;
 						Filter filter = new Filter();
 						filter.categoryBits = 4;
 						filter.groupIndex = -2;
 						Orb toDrop = new Orb(enemy.getPosition(), enemy.getWorld(), ActorMgr.getProperties("orb", new StaticActor()), enemy.getColor(), filter);
 						level.getMoveableActors().remove(enemy);
 						level.addCollectable(toDrop);
 					} else if(actor instanceof Bullet){
 						Bullet bullet = (Bullet) actor;
 						bullets.removeValue(bullet, true);
 					} else if(actor instanceof Orb){
 						Orb orb = (Orb) actor;
 						level.getCollectables().remove(orb);
 					} else if(actor instanceof AlienHead){
 						AlienHead alienHead = (AlienHead) actor;
 						level.getStaticActors().remove(alienHead);
 					} else if(actor instanceof Block){
 						Block block = (Block) actor;
 						level.getStaticActors().remove(block);
 					}
 					
 					try{
 						this.world.destroyBody(actor.getBody());
 					} catch (NullPointerException ex){
 						Gdx.app.log("Destroy Body", ex.getMessage());
 						return;
 					}
 				} else {
 					this.actorsForRemoval.add(actor);
 				}
 			}
 		}
 		else
 		{
 			try {
 				Thread.sleep(5);
 				removeBodies();
 			} catch (InterruptedException e) {}
 		}
 	}
 	
 	@Override
 	public void resize(int width, int height) {
 		super.resize(width, height);
 		renderer.setSize(width, height);
 		this.width = width;
 		this.height = height;
 		controller.width = width;
 		controller.height = height;
 	}
 
 	@Override
 	public void hide() {
 		FirstWing.audio.disableMusic();
 	}
 
 	@Override
 	public void pause() {
 		super.pause();
 		currentState = GameState.Paused;
 	}
 
 	@Override
 	public void resume() {
 		super.resume();
 		getWindow();
 		
 	}
 
 	@Override
 	public void dispose() {
 		Gdx.input.setInputProcessor(null);
 		interfaceRenderer.dispose();
 	}
 	
 	private Window getDeathWindow(){
 		final Window window = this.getStandardWindow("You Failed!");
 		TextButton btnRetry = new TextButton("Yes", this.getSkin());
 		btnRetry.addListener(new ClickListener(){
 			@Override
 			public void clicked(InputEvent event, float x, float y){
 				stage.getActors().removeValue(window, true);
 				FirstWing.stats.resetScore(false);
 				loadLevel(levelPath);
 			}
 		});
 		btnRetry.pad(0f, 20f, 0f, 20f);
 		
 		TextButton btnMenu = new TextButton("No", this.getSkin());
 		btnMenu.addListener(new ClickListener(){
 			@Override
 			public void clicked(InputEvent event, float x, float y){
 				stage.getActors().removeValue(window, true);
 				firstWing.setScreen(new MainMenu(firstWing));
 			}
 		});
 		btnMenu.pad(0f, 20f, 0f, 20f);
 		
 		window.add(new Label("You're destroyed!", this.getSkin())).expandX().colspan(2);
 		window.row();
 		window.add(new Label("Would you like to retry?", this.getSkin())).expandX().colspan(2).spaceBottom(10f);
 		window.row();
 		window.add(btnRetry).expandX();
 		window.add(btnMenu).expandX();
 		return window;
 	}
 	
 	public Window getVictoryWindow(){
 		final Window window = this.getStandardWindow("Victory!");
 		window.setSize(this.stage.getWidth() / 1.5f, this.stage.getHeight() / 1.5f);
 		window.setPosition(this.stage.getWidth() / 2 - window.getWidth() / 2, this.stage.getHeight() / 2 - window.getHeight() / 2);
 		TextButton btnReplay = new TextButton("Replay", this.getSkin());
 		btnReplay.addListener(new ClickListener(){
 			@Override
 			public void clicked(InputEvent event, float x, float y){
 				stage.getActors().removeValue(window, true);
 				loadLevel(levelPath);
 			}
 		});
 		
 		TextButton btnMenu = new TextButton("Back to menu", this.getSkin());
 		btnMenu.addListener(new ClickListener(){
 			@Override
 			public void clicked(InputEvent event, float x, float y){
 				Gdx.app.log("Window", "Back to menu pressed");
 				firstWing.setScreen(firstWing.mainScreen);
 				stage.getActors().removeValue(window, true);
 			}
 		});
 		
 		
 		TextButton btnNextLevel = new TextButton("Next level", this.getSkin());
 		btnNextLevel.addListener(new ClickListener(){
 			@Override
 			public void clicked(InputEvent event, float x, float y){
 				stage.getActors().removeValue(window, true);
 				//Change level
 				levelPath += 1;
 				FirstWing.stats.changeLevel(levelPath);
 				loadLevel(levelPath);
 			}
 		});
 		
 		int stars = FirstWing.stats.getStars(levelPath);
 		Table starTable = new Table();
 		starTable.defaults().pad(5);
 		if (stars >= 0) {
 			for (int star = 0; star < 3; star++) {
 				if (stars > star) {
 					starTable.add(new Image(getSkin().getDrawable("star-filled"))).width(20).height(20);
 				} else {
 					starTable.add(new Image(getSkin().getDrawable("star-unfilled"))).width(20).height(20);
 				}
 			}			
 		}
 		
 		if(stars < 1)
 		{
 			window.add(new Label("Ouch, not enough points!", this.getSkin())).colspan(3);
 			window.row();
 			window.add(new Label("Try to get orbs of the same color", this.getSkin())).colspan(3);
 		}
 		else
 		{
 			window.add(new Label("Congratulations!", this.getSkin())).colspan(3);
 		}
 		
 		window.row();
 		window.add(new Label("You scored: " + FirstWing.stats.getScore(), this.getSkin())).colspan(3);
 		window.row();
 		if(FirstWing.stats.getTrophy())
 		{
 			window.add(new Label("You got the trophy!", this.getSkin())).colspan(3);
 		}
 		else
 		{
 			window.add(new Label("You didn't get the trophy!", this.getSkin())).colspan(3);
 		}
 		window.row();
 		window.add(starTable).height(30).colspan(3);
 		window.row();
 		window.add(btnReplay);
 		window.add(btnMenu);
 		if(stars > 0 || FirstWing.stats.getUnlockedLevels() > levelPath)
 		{
 			window.add(btnNextLevel);
 		}
 		return window;
 	}
 	
 	public Window getPauseWindow(){
 		final Window window = this.getStandardWindow("Paused!");
 		window.setSize(this.stage.getWidth() / 1.5f, this.stage.getHeight() / 1.5f);
 		window.setPosition(this.stage.getWidth() / 2 - window.getWidth() / 2, this.stage.getHeight() / 2 - window.getHeight() / 2);
 		TextButton btnReplay = new TextButton("Restart", this.getSkin());
 		btnReplay.addListener(new ClickListener(){
 			@Override
 			public void clicked(InputEvent event, float x, float y){
 				stage.getActors().removeValue(window, true);
 				loadLevel(levelPath);
 			}
 		});
 		
 		TextButton btnMenu = new TextButton("Back to menu", this.getSkin());
 		btnMenu.addListener(new ClickListener(){
 			@Override
 			public void clicked(InputEvent event, float x, float y){
 				Gdx.app.log("Window", "Back to menu pressed");
 				firstWing.setScreen(firstWing.mainScreen);
 				FirstWing.stats.resetScore(false);
 				stage.getActors().removeValue(window, true);
 			}
 		});
 		
 		
 		TextButton btnResume = new TextButton("Resume", this.getSkin());
 		btnResume.addListener(new ClickListener(){
 			@Override
 			public void clicked(InputEvent event, float x, float y){
 				stage.getActors().removeValue(window, true);
 				//Resume level
 				currentState = GameState.Running;
 				Gdx.input.setInputProcessor(im);
 			}
 		});
 		
 		
 		window.add(new Label("Game is paused!", this.getSkin())).colspan(3);
 		window.row();
 		window.add(btnReplay);
 		window.add(btnMenu);
 		window.add(btnResume);
 		return window;
 	}
 	
 	private Window getStandardWindow(String title){
 		Window window = new Window(title, this.getSkin());
 		//window.debug();
 		window.addAction(Actions.fadeIn(0.5f));
 		WindowStyle windowStyle = this.getSkin().get("default", WindowStyle.class); 
 		windowStyle.titleFont = this.popupFont;
 		window.padTop(this.popupFont.getCapHeight() + 30f);
 		window.setStyle(windowStyle);
 		window.setSize(this.stage.getWidth() / 2f, this.stage.getHeight() / 2.5f);
 		window.setPosition(this.stage.getWidth() / 2 - window.getWidth() / 2, this.stage.getHeight() / 2 - window.getHeight() / 2);
 		window.setMovable(false);
 		return window;
 	}
 	
 	public void getWindow()
 	{
 		this.stage.addActor(this.getPauseWindow());
 		Gdx.input.setInputProcessor(stage);
 		this.currentState = GameState.Paused;
 	}
 
 	public boolean isLoaded() {
 		return loaded;
 	}
 }
