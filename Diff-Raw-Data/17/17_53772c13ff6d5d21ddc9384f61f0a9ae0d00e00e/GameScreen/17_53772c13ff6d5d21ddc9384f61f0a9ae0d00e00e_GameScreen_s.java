 package com.team14;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.Files.FileType;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.World;
 
 public class GameScreen implements Screen, InputProcessor
 {	
 	/* The time the last frame was rendered, used for throttling framerate */
 	private long lastRender;
 
 	private TiledMapHelper tiledMapHelper;
 
 	/* The libgdx SpriteBatch -- used to optimize sprite drawing. */
 	private SpriteBatch worldBatch;
 
 	/**
 	 * The libgdx SpriteBatch for the heads up display (score+lives)
 	 */
 	private SpriteBatch hudBatch;
 
 	/**
 	 * This is the main box2d "container" object. All bodies will be loaded in
 	 * this object and will be simulated through calls to this object.
 	 */
 	private World world;
 
 	/* This is the player character. It will be created as a dynamic object. */
 	public Razorback razorback;
 
 	/**
 	 * Box2d works best with small values. If you use pixels directly you will
 	 * get weird results -- speeds and accelerations not feeling quite right.
 	 * Common practice is to use a constant to convert pixels to and from
 	 * "meters".
 	 */
 	public static final float PIXELS_PER_METER = 60.0f;
 
 	/**
 	 * The screen's width and height. This may not match that computed by
 	 * libgdx's gdx.graphics.getWidth() / getHeight() on devices that make use
 	 * of on-screen menu buttons.
 	 */
 	private int screenWidth;
 	private int screenHeight;
 	private boolean gameOver = false;
 	private Game game;
 	private boolean initialized = false;
 	private GameInfo info;
 	public LifeLostScreen lifeLostScreen;
 	public GameOverScreen gameOverScreen;
 	private Music music;
 	private Music jumpSound;
 	private Music dashSound;
 	
 	/**
 	 * The camera responsible for showing the score and lives above the acutal game
 	 */
 	private HUDCamera hudCamera;
 	private BitmapFont font;
 	
 	public GameScreen(Game g, GameInfo i, Music m)
 	{
 		info = i;
 		game = g;
 		music = m;
 		screenWidth = -1;	// Defer until create() when Gdx is initialized.
 		screenHeight = -1;
 		
 		gameOverScreen = new GameOverScreen(game, info, music);
 		lifeLostScreen = new LifeLostScreen(game, info, music, this);
 	}
 	
 	public void updateWorld()
 	{
 		/**
 		 * Have box2d update the positions and velocities (and etc) of all
 		 * tracked objects. The second and third argument specify the number of
 		 * iterations of velocity and position tests to perform -- higher is
 		 * more accurate but is also slower.
 		 */
 		world.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);
 
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		// A nice(?), blue backdrop.
 		Gdx.gl.glClearColor(0, 0.5f, 0.9f, 0);
 
 		/** 
 		 * The camera is now controlled primarily by the position of the main
 		 * character, and secondarily by the map boundaries.
 		 */
 		tiledMapHelper.getCamera().position.x = PIXELS_PER_METER
 				* razorback.getXPosition() + 300;
 		
 		// Ensure that the camera is only showing the map, nothing outside.
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
 
 		tiledMapHelper.getCamera().update();
 		tiledMapHelper.render();
 		worldBatch.setProjectionMatrix(tiledMapHelper.getCamera().combined);		
 	}
 	
 	/**
 	 * Get new score and lives, update on HUD camera
 	 */
 	public void updateHUD()
 	{
 		hudCamera.getCamera().update();
 		hudCamera.render();
 		hudBatch.setProjectionMatrix(hudCamera.getCamera().combined);
 	}
 
 	@Override
 	public void render(float delta)
 	{
 		long now = System.nanoTime();
 		
 		/**
 		 * First we update the tilemap and razorback. Then we display them. 
 		 */ 
 		updateWorld();		// Update tilemap + camera position, HUD camera
 		worldBatch.begin();	// Prepare the world and razorback SpriteBatch for drawing.
 		razorback.move(worldBatch);
 		worldBatch.end();	// "Flush" the sprites to screen.
 		
 		/**
 		 * Now we update the score and lives, then display them.
 		 */
 		updateHUD();
 		hudBatch.begin();
 		float fps = 1f / Gdx.graphics.getDeltaTime();
 		CharSequence str = "Lives: " + info.lives() +  "  Score: " + Integer.toString((int) getScore()) + ", FPS: " + Integer.toString((int) fps);
 		font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
 		font.draw(hudBatch, str, 50, 550);
 		hudBatch.end();
 		
 		/**
 		 * Check for collision
 		 */
 		if (razorback.getXVelocity() <= 0.0f)
 		{
 			// Tell the GameInfo object that we're dead, and wish to record a new score 
 			info.loseLife((int) getScore());
 			
 			if (info.gameOver())
 			{
 				System.out.println("game over, man. game over.");
 				game.setScreen(gameOverScreen);
 			}
 			else
 			{
 				System.out.println("life lost, man. life lost.");
 				game.setScreen(lifeLostScreen);
 			}
 		}
 		
 		/**
 		 * Update render time
 		 */
 		now = System.nanoTime();
 		if (now - lastRender < 30000000) // 30 ms, ~33FPS
 		{
 			try
 			{
 				Thread.sleep(30 - (now - lastRender) / 1000000);
 			} catch (InterruptedException e)
 			{
 			}
 		}
 		lastRender = now;
 	}
 	
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void show()
 	{
 		/**
 		 * Only initialize on first call
 		 */
 		if (!initialized)
 		{
 			/**
 			 * If the viewport's size is not yet known, determine it here.
 			 */
 		
 			if (screenWidth == -1) {
 				screenWidth = Gdx.graphics.getWidth();
 				screenHeight = Gdx.graphics.getHeight();
 			}
 
 			tiledMapHelper = new TiledMapHelper();
 			tiledMapHelper.setPackerDirectory("assets/tiles");
 			tiledMapHelper.loadMap("assets/tiles/testmap.tmx");
 			tiledMapHelper.prepareCamera(screenWidth, screenHeight);
 
 			worldBatch = new SpriteBatch();
 
 			/**
 			 * You can set the world's gravity in its constructor. Here, the gravity
 			 * is negative in the y direction (as in, pulling things down).
 			 */
 			world = new World(new Vector2(0.0f, -10.0f), true);
 
 			razorback = new Razorback(world);
 			font = new BitmapFont();
 
 			tiledMapHelper.loadCollisions("assets/collisions.txt", world,
 					PIXELS_PER_METER);
 
 			lastRender = System.nanoTime();
 			Gdx.input.setInputProcessor(this);
 
 			/**
 			 * Initialize objects needed to draw score and lives
 			 */
 			hudCamera = new HUDCamera();
 			hudCamera.prepareCamera(screenWidth, screenHeight);
 			hudBatch = new SpriteBatch();
 			
 			/**
 			 * Initialize objects needed to play sounds
 			 */
 	        jumpSound = Gdx.audio.newMusic(Gdx.files.getFileHandle("assets/music/jump.wav", FileType.Internal));
 	        jumpSound.setLooping(false);
 	        jumpSound.setVolume(0.2f);	// jump.wav is pretty loud!

 			initialized = true;
 		}
 		
 		/**
 		 * Starts playing music after initialization. If we're already initialized
 		 * and are coming back to this screen, will play music from pause point.
 		 */
        music.play();
 	}
 
 	@Override public void hide() { }
 	@Override public void pause() { }
 	@Override public void resume() { }
 	@Override public void dispose() { }
 
 	/**
 	 * InputProcessor methods
 	 */
 	@Override
 	public boolean keyDown(int keycode)
 	{
 		boolean down = true;
 		switch (keycode)
 		{
 			case (Keys.DPAD_UP):
 				if (razorback.jump())
 					jumpSound.play();
 				break;
 			case (Keys.CONTROL_LEFT):
 				razorback.dash();
 				break;
 			case (Keys.X):
 				// For now, summons a new HelpScreen
 				// TODO: Create an actual PauseScreen class+image
 				game.setScreen(new HelpScreen(game, this));
 				break;
 			default:
 				down = false;
 				break;
 		}
 		return down;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int x, int y, int pointer, int button) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int x, int y, int pointer, int button) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int x, int y, int pointer) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchMoved(int x, int y) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public float getScore()
 	{
 		return PIXELS_PER_METER * razorback.getXPosition();
 	}
 }
