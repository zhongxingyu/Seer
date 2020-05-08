 package com.detourgames.raw;
 
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 
 public class GameManager {
 
 	private static GameManager gameManager = new GameManager();
 
 	private Camera mCamera;
 	private HUD mHUD;
 	// Input input;
 	// HUD mHUD;
 
 	Level mLevel;
 
 	LevelLoader levelLoader;
 	SpriteBatch spriteBatch;
 	Box2DDebugRenderer debug;
 	
 	boolean levelLoaded = false;
 
 	private GameManager() {
 		// input = new Input(this);
 		// camera = new Camera();
 		// mHUD = new HUD(camera);
 		
 	}
 
 	public static GameManager getGameManager() { // TODO synchronized?
 		return gameManager;
 	}
 
 	public void update(float deltaTime) {
 		if (levelLoaded) {
 			mCamera.update(mLevel.getHero());
 			// (float)mLevel.getHero().getY());
 			// mHUD.update();
 			EventQueue.getEventQueue().processAndRemoveAllEvents();
 			mLevel.update(deltaTime, 8, 3);
 			// TODO update camera AFTER level?
 		}
 	}
 	
 	public void draw(long nanoTime) {
 		
 		if (levelLoaded) {
 			spriteBatch.setProjectionMatrix(mCamera.getCamera().combined);
 			spriteBatch.begin();
 			// mHUD.draw(spriteBatch);
 			mLevel.draw(spriteBatch, nanoTime);
 			//mHUD.draw(spriteBatch, nanoTime);
 			spriteBatch.end();
			debug.render(mLevel.getWorld(), mCamera.getCamera().combined);
 			//mLevel.drawDebug(mCamera);
 		}
 
 	}
 
 	public void loadLevel(int level) {
 
 		levelLoaded = false;
 
 		mLevel = new Level();
 		spriteBatch = new SpriteBatch(1600);
 
 		//mHeroTexture = new TextureAtlas();// new Texture(context,
 											// Animation.HERO_TEXTURE, 3, new
 											// int[]{3,1,5}, new
 											// int[]{8,8,4,0,7,16,16,16,16}, new
 											// int[]{128,1024,64}, new
 											// int[]{128,320,64});
 		// mHeroTexture.
 		if (!levelLoaded) {
 			levelLoader = new LevelLoader(mLevel);
 			levelLoader.createLevelFromFile(level);
 
 			// spriteBatch = new SpriteBatch(1600, SpriteBatch.SPRITE_SHADER,
 			// context, camera);
 
 			levelLoaded = true;
 		}
 		
 		debug = new Box2DDebugRenderer(true, true,true,true);
 
 		// System.gc();
 	}
 
 	/*
 	 * return mHUD; }
 	 * 
 	 * public Input getInput(){ return input; }
 	 * 
 	 * public Camera getCamera(){ return camera; }
 	 */
 
 	public Level getLevel() {
 		return mLevel;
 	}
 	
 	public void createCamera(float viewportWidth, float viewportHeight, int pixelWidth, int pixelHeight){
 		mCamera = new Camera(viewportWidth, viewportHeight);
 		mCamera.setScreenSizePixels(pixelWidth, pixelHeight);
 		mHUD = new HUD(mCamera);
 	}
 	
 	public Camera getCamera(){
 		return mCamera;
 	}
 	
 	public HUD getHUD(){
 		return mHUD;
 	}
 	
 }
