 package com.detour.raw;
 
 import android.content.Context;
 
 public class GameManager {
 	
 	private static GameManager gameManager = new GameManager();
 	
 	Camera camera;
 	Input input;
 	HUD mHUD;
 	
 	Level mLevel;
 	
 	LevelLoader levelLoader;
 	SpriteBatch spriteBatch;
 	Texture mHeroTexture;
 	
 	Tile[] tileMap;
 	Hero hero;
 	
 	boolean levelLoaded = false;
 	
 	private GameManager(){
 		input = new Input(this);
 		camera = new Camera();
 		mHUD = new HUD(camera);
 		
 	}
 	
 	public static synchronized GameManager getGameManager(){ //TODO synchronized?
 		return gameManager;
 	}
 	
 	public void update(float deltaTime){
 		if(levelLoaded){
 			camera.update((float)hero.getX(), (float)hero.getY());
 			mHUD.update();
 			mLevel.update(deltaTime, 8, 3);
 		}
 	}
 	
 	public void draw(){
 		
 		if(levelLoaded){
 			
 			spriteBatch.begin(mHeroTexture);
 			for(int i=0;i<tileMap.length;i++){
 				tileMap[i].draw(spriteBatch);
 			}
 			mHUD.draw(spriteBatch);
 			hero.draw(spriteBatch);
 			spriteBatch.end(camera);
			//mLevel.drawDebug(camera);
 		}
 		
 	}
 	
 	public void loadLevel(Context context, int level){
 		
 		levelLoaded = false;
 		
 		//<Practice> TODO
 		mLevel = new Level(context);
 		
 		mHeroTexture = new Texture(context, Animation.HERO_TEXTURE, 3, new int[]{3,1,5}, new int[]{8,8,4,0,7,16,16,16,16}, new int[]{128,1024,64}, new int[]{128,320,64});
 		if(!levelLoaded){
 			levelLoader = new LevelLoader(context, level, mLevel);
 			tileMap = levelLoader.getTileMap();
 			
 			hero = new Hero();
 			mLevel.create(hero, 2, 3, 2, 2, true);
 			
 			spriteBatch = new SpriteBatch(1600, SpriteBatch.SPRITE_SHADER, camera.getScreenRatio(), context);
 			
 			levelLoaded = true;
 		}
 		
 		System.gc();
 	}
 	
 	public HUD getHUD(){
 		return mHUD;
 	}
 	
 	public Input getInput(){
 		return input;
 	}
 	
 	public Camera getCamera(){
 		return camera;
 	}
 	
 	public Hero getHero(){
 		return hero;
 	}
 	
 }
