 package com.infinitron.rpg;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.view.SurfaceView;
 import android.view.SurfaceHolder;
 
 public class Game extends SurfaceView implements SurfaceHolder.Callback {
 
 	private SurfaceHolder surfaceHolder;
 	private GameThread gameThread;
 	private GameRenderer renderer;
 	
 	// TESTING SPRITE RENDERING
	//Player sprite rendering
 	public GridSpriteSheet elaine = new GridSpriteSheet(BitmapFactory.decodeResource(getResources(), R.drawable.walk_elaine), 30, 47);
 	public Sprite elaineSprite = new Sprite(elaine, 2);
 	public GameObject elaineObject = new GameObject("Elaine", elaineSprite, 20, 20);
 	
	// Monster sprite rendering
	public GridSpriteSheet monster1 = new GridSpriteSheet(BitmapFactory.decodeResource(getResources(), R.drawable.monster_inc), 100, 100);
	public Sprite monster1Sprite = new Sprite(monster1, 2);
	public GameObject monster1Object = new GameObject("Monster1", monster1Sprite, 110, 50);
	
 	public Bitmap map = BitmapFactory.decodeResource(getResources(), R.drawable.map);
 	public GridSpriteSheet map_tilesheet = new GridSpriteSheet(BitmapFactory.decodeResource(getResources(), R.drawable.map_tilesheet), 16, 16); 
 	public Level level = new Level(map, map_tilesheet, 60, 60);
 	
 	Monster[] monsterCollection = new Monster[10];
 	Item[][] itemCollection = new Item[3][10];
 	
 	public int fps = 0;
 	
 	public Game(Context context) {
 		super(context);
 		init();
 	}
 	
 	public void init() {
 		surfaceHolder = getHolder();
 		surfaceHolder.addCallback(this);
 		setFocusable(true);
 		
 		renderer = new GameRenderer();
 		renderer.addRenderable(elaineObject);
		renderer.addRenderable(monster1Object);
 		
 		gameThread = new GameThread(this, renderer, level);
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder surfaceHolder) {
 		gameThread.surfaceCreated(surfaceHolder);
 		gameThread.start();
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
 		gameThread.surfaceDestroyed(surfaceHolder);
 		
 		boolean retry = true;
 		while (retry) {
 			try {
 				gameThread.join();
 				retry = false;
 			} catch (InterruptedException e) {
 				
 			}
 		}
 	}
 	
 	@Override
 	public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
 		gameThread.surfaceChanged(surfaceHolder, format, width, height);
 	}
 	
 	public void update() {
 		
 	}
 	
 	// 1d array full of monster objects 
 	public void createMonsters(Sprite sprite){
 		for(int i = 0; i < monsterCollection.length; i++){
 			monsterCollection[i] = new Monster("The Thing", sprite, 0, 0, 0, 0, 0, 0);
 		}
 	}
 	
 	// 2d array of items, where the each row is a different type of item and the each column represents a different object of type item where the greater
 	// the column index the greater the item level
	public void createItems(Sprite itemSprites[][]){
 		for(int j = 0; j < itemCollection.length; j++){
 			for(int i = 0; i < itemCollection[j].length; i++){
 				if(j == 0){
					itemCollection[j][i] = (Item) new Weapon("An Item", itemSprites[j][i], 0, 0, 0, 0, 0, 0);
 				}else if(j == 1){
					itemCollection[j][i] = (Item) new Armor("A piece of Armour", itemSprites[j][i], 0, 0, 0, 0, 0, 0);
 				}else if(j == 2){
					itemCollection[j][i] = (Item) new Potion("A Potion", itemSprites[j][i], 0, 0, 0, 0, 0, 0);
 				}
 			}
 		}
 	}
 }
