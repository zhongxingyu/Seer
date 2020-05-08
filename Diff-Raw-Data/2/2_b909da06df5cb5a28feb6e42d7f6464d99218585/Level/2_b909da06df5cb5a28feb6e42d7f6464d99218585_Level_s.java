 package com.gamegear.firstwing.levels;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Filter;
 import com.gamegear.firstwing.ActorMgr;
 import com.gamegear.firstwing.TextureMgr;
 import com.gamegear.firstwing.actors.*;
 import com.gamegear.firstwing.actors.json.StaticActor;
 import com.gamegear.firstwing.levels.json.*;
 import com.gamegear.firstwing.screens.GameScreen;
 
 public class Level {
 
 	private int width;
 	private int height;
 	private ArrayList<Actor> staticActors;
 	private ArrayList<MoveableActor> dynamicActors;
 	private ArrayList<Spawner> spawners;
 	private ArrayList<Orb> collectables;
 	private Queue<SpeedPoint> speed;
 	private GameScreen game;
 	private int currentSpeed = 5;
 	private ArrayList<Sprite> background;
 	private Bob playerShip;
 	private LevelProperties properties;
 	private Texture bgTexture;
 
 	public int getWidth() {
 		return width;
 	}
 
 	public void setWidth(int width) {
 		this.width = width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	public void setHeight(int height) {
 		this.height = height;
 	}
 
 	public ArrayList<Actor> getStaticActors() {
 		return staticActors;
 	}
 	
 	public void addStaticActor(Actor actor){
 		this.staticActors.add(actor);
 	}
 	
 	public ArrayList<MoveableActor> getMoveableActors() {
 		return this.dynamicActors;
 	}
 	
 	public void addMoveableActor(MoveableActor enemy){
 		this.dynamicActors.add(enemy);
 	}
 	
 	public ArrayList<Spawner> getSpawners(){
 		return this.spawners;
 	}
 	
 	public ArrayList<Orb> getCollectables(){
 		return this.collectables;
 	}
 	
 	public void addCollectable(Orb orb){
 		this.collectables.add(orb);
 	}
 	
 	public void setBlocks(ArrayList<Actor> blocks) {
 		this.staticActors = blocks;
 	}
 	
 	public Bob getPlayer(){
 		return this.playerShip;
 	}
 
 	public Level(GameScreen game, String levelPath) {
 		this.game = game;
 		
 		this.staticActors = new ArrayList<Actor>();
 		this.dynamicActors = new ArrayList<MoveableActor>();
 		this.spawners = new ArrayList<Spawner>();
 		this.collectables = new ArrayList<Orb>();
 		this.speed = new LinkedList<SpeedPoint>();
 		this.background = new ArrayList<Sprite>();
 		
 		try
 		{
 			this.loadLevel(levelPath);
 		}
 		catch(Exception ex)
 		{
 			//TODO FIX ME
 		}
 	}
 	
 	public Actor get(int x) {
 		return staticActors.get(x);
 	}
 	
 	public LevelProperties getProperties(){
 		return this.properties;
 	}
 	
 	public float getSpeed(float cameraX)
 	{
 		if(!speed.isEmpty())
 		{
 			SpeedPoint tmpSpeed = speed.peek();
 			if(tmpSpeed.X < cameraX)
 			{
 				currentSpeed = (int) tmpSpeed.Speed;
 				speed.remove();
 			}
 		}
 		
 		return currentSpeed;
 	}
 	
 	public int getSpeed()
 	{
 		return currentSpeed;
 	}
 	
 	public ArrayList<Sprite> getBackground(){
 		return this.background;
 	}
 
 	public void loadLevel(String levelPath) {
 		
 		FileHandle fileHandle;
 		fileHandle = Gdx.files.internal("levels/" + levelPath + ".dat");
 		
		if(levelPath.isEmpty() || !fileHandle.exists())
 		{
 			this.properties = new JSONLoader().getLevel(Gdx.files.internal("levels/1.dat"));
 		}
 		else
 		{
 			//TODO FIX ME
 			this.properties = new JSONLoader().getLevel(Gdx.files.internal("levels/" + levelPath + ".dat"));
 		}
 		
 		Collections.sort(this.properties.Tiles);
 		Iterator<Tile> tiles = this.properties.Tiles.iterator();
 		Collections.sort(this.properties.Spawners);
 		Iterator<com.gamegear.firstwing.levels.json.Spawner> spawnerIt = this.properties.Spawners.iterator();
 		Filter filter = new Filter();
 		TextureMgr.initiateAtlas(this.properties.LevelColor);
 		
 		// TILES
 		while(tiles.hasNext()){
 			Tile tile = tiles.next();
 			
 			// Check level width
 			if(tile.X > width)
 			{
 				width = tile.X;
 			}
 			
 			if(tile.Name.contains("alienhead")){
 				staticActors.add(new AlienHead(new Vector2(tile.X, tile.Y), game.world, ActorMgr.getProperties(tile.Name, new StaticActor()), filter));
 			} else {
 				staticActors.add(new Block(new Vector2(tile.X, tile.Y), game.world, ActorMgr.getProperties(tile.Name, new StaticActor()), tile, filter, this.properties.LevelColor));
 			}
 			
 			tiles.remove();
 		}
 		
 		// SPAWNERS
 		while(spawnerIt.hasNext()){
 			com.gamegear.firstwing.levels.json.Spawner spawner = spawnerIt.next();
 			this.spawners.add(new Spawner(spawner, game.world, this));
 			spawnerIt.remove();
 		}
 		
 		if(bgTexture == null)
 		{
 			bgTexture = new Texture(Gdx.files.internal("images/" + this.properties.BackgroundName + ".png"));
 		}
 		
 		// BACKGROUND
 		float bgSize = 4f;
 		int xCeiling = (int) Math.ceil(this.properties.FinishX / (int)bgSize);
 		int yCeiling = 12 / (int)bgSize;
 		for(int x = 0; x < xCeiling; x++){
 			for(int y = 0; y < yCeiling; y++){
 				Sprite tempBg = new Sprite(bgTexture);
 				tempBg.setSize(bgSize, bgSize);
 				tempBg.setPosition((x * bgSize) - 0.5f, (y * bgSize) - 0.5f);
 				this.background.add(tempBg);
 			}
 		}
 		
 		// SPEED
 		if(this.properties.SpeedPoints != null)
 		{
 			Collections.sort(this.properties.SpeedPoints);
 		}
 		speed.add(new SpeedPoint(0, 1));
 		if(this.properties.SpeedPoints != null){
 			for(SpeedPoint sp : this.properties.SpeedPoints){
 				speed.add(sp);
 			}
 		}
 		speed.add(new SpeedPoint(this.properties.FinishX - 6f, 0));
 		
 		// PLAYER
 		this.playerShip = new Bob(new Vector2(this.properties.SpawnX, this.properties.SpawnY), game.world, new Filter());
 	}
 	
 	public void dispose()
 	{
 		staticActors.clear();
 		dynamicActors.clear();
 		properties = null;
 		
 		spawners.clear();
 		collectables.clear();
 		speed.clear();
 		background.clear();
 	}
 }
