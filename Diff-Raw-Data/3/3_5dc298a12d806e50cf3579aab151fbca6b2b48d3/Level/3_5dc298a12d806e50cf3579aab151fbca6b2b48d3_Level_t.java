 package com.me.Roguish.Model;
 import com.me.Roguish.Controller.AbilityController;
 import com.badlogic.gdx.utils.Array;
 import java.util.Random;
 import com.badlogic.gdx.maps.MapProperties;
 import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
 import com.badlogic.gdx.maps.tiled.TmxMapLoader;
 import com.badlogic.gdx.maps.tiled.TiledMapTile;
 
 public class Level{
 	public Array<Entity> entities = new Array<Entity>();
 	public TurnQueue queue;
 	public AbilityController ability = new AbilityController();
     public TiledMap map;
     //public SimpleTileAtlas atlas;
     public HeroUnit hero;
     private Random Dice = new Random();
     public MonsterUnit rat1 = new MonsterUnit(1, 2, "E_Rat", new Array<Integer>(), MonsterUnit.RAT);
     public MonsterUnit rat2 = new MonsterUnit(2, 1, "E_Rat", new Array<Integer>(), MonsterUnit.RAT);
     public MonsterUnit rat3 = new MonsterUnit(2, 2, "E_Rat", new Array<Integer>(), MonsterUnit.RAT);
     public MonsterUnit bat1 = new MonsterUnit(1, 4, "E_Bat", new Array<Integer>(), MonsterUnit.BAT);
     public MonsterUnit spider1 = new MonsterUnit(1, 6, "E_Spider", new Array<Integer>(), MonsterUnit.SPIDER);
     public MonsterUnit shadow1 = new MonsterUnit(1, 7, "E_Shadow", new Array<Integer>(), MonsterUnit.SHADOW);
     public Entity winChest = new Entity(4,13,"Chest", new Array<Integer>());
     private MonsterUnit mon;
     
     private TiledMapTileLayer backgroundLayer;
     public int columns;
     public int rows;
     
 	public Level(ClassCard cCard){
 		hero =  new HeroUnit(5, 5, cCard.getClassName(), new Array<Integer>(), cCard);
 		create();
 		setStats();
 		populate();
 		queue = new TurnQueue(getEntities());
 		
 		backgroundLayer = (TiledMapTileLayer)map.getLayers().get(0);
 		columns = backgroundLayer.getWidth();
 		rows = backgroundLayer.getHeight();
 	}
 	
 	public Level(ClassCard cCard, Array<Integer> abilities) {
 		for(int i = 0; i < abilities.size; i++)
 			System.out.println(abilities.get(i));
 		hero =  new HeroUnit(5, 5, cCard.getClassName(),  new Array<Integer>(), cCard);
 		for(int i = 0; i < abilities.size; i++ )
 			hero.addAbility(abilities.get(i));
 		create();
 		setStats();
 		populate();
 		queue = new TurnQueue(getEntities());
		backgroundLayer = (TiledMapTileLayer)map.getLayers().get(0);
		columns = backgroundLayer.getWidth();
		rows = backgroundLayer.getHeight();
 	}
 	
 	
 	// The below will work once the tileOpen() method is added to level.
 	
 	/*
 	
 	private void generateEntities(){
 		// This can be scaled as the level difficulty
 		int area = map.height * map.width;
 		int totalMon = (int) area / 5;
 		
 		for(int i = 0; i < totalMon; i++){
 			
 			// Rewrite once level controller gets refactored
 			int[] x = findOpenXY();
 			
 			//Update as new units are created
 			int monType = Dice.nextInt(6);
 			switch(monType){
 			case MonsterUnit.RAT: mon = new MonsterUnit(x[0], x[1], "E_Rat", new Array<Integer>(), MonsterUnit.RAT); break;
 			case MonsterUnit.BAT: mon = new MonsterUnit(x[0], x[1], "E_Bat", new Array<Integer>(), MonsterUnit.BAT); break;
 			case MonsterUnit.SPIDER: mon = new MonsterUnit(x[0], x[1], "E_Spider", new Array<Integer>(), MonsterUnit.BAT); break;
 			case MonsterUnit.SHADOW: mon = new MonsterUnit(x[0], x[1], "E_Monster", new Array<Integer>(), MonsterUnit.SHADOW); break;
 			}
 			
 			mon.setId(1 + i);
 			addEntity(mon);
 		
 			
 		}
 	}
 	*/
 
 	private void setStats() {
 		 hero.setId(0);
 		 rat1.setId(1);
 		 rat2.setId(2);
 		 rat3.setId(3);
 		 bat1.setId(4);
 		 spider1.setId(5);
 		 shadow1.setId(6);
 		 winChest.setId(1337);
 		 winChest.setAlive(false);
 		
 	}
 
 	public void create(){
 		System.out.println("In create");
 		map = new TmxMapLoader().load("data/level/test_FoV.tmx");
 		System.out.println("Tiles loaded");
 	   
 	}
 
 	
 	private void addEntity(Entity ent){
 		entities.add(ent);
 	}
 	
 	private boolean removeEntity(Entity ent){
 		return entities.removeValue(ent, true);
 	}
 	
 	public HeroUnit getHero(){
 		return hero;
 	}
 		// Populates entity list
 	private void populate(){
 
 		addEntity(hero); 
 		addEntity(rat1);
 		addEntity(rat2);
 		addEntity(rat3);
 		addEntity(bat1);
 		addEntity(spider1);
 		addEntity(shadow1);
 		addEntity(winChest);
 	}
 	
 	public Array<Entity> getEntities(){
 		return this.entities;
 	}
 	
 	public boolean tilePropCheck(int x, int y, String property){
 		MapProperties temp = getTile(x,y).getProperties();
 		if(temp.containsKey(property)){
 			return !Boolean.parseBoolean(temp.get(property, String.class));
 		}
 		else return true;
 	}
 
 	public boolean tilePropCheck(int x, int y, int layer, String property){
 		MapProperties temp = getTile(x,y,layer).getProperties();
 		if(temp.containsKey(property))
 			return !Boolean.parseBoolean(temp.get(property, String.class));
 		else return true;
 	}
 	
 	public boolean tileExists(int x, int y, int layer){
 		try{
 			backgroundLayer.getCell(x,y).getTile();
 			return true;
 		}catch(ArrayIndexOutOfBoundsException e){
 			return false;
 		}
 	}
 	
 	public TiledMapTile getTile(int x, int y, int layer){
 		TiledMapTileLayer temp = (TiledMapTileLayer)map.getLayers().get(layer);
 		return temp.getCell(x,y).getTile();
 	}
 	
 	public TiledMapTile getTile(int x, int y){
 		try{
 			return backgroundLayer.getCell(x,y).getTile();
 		}
 		catch(NullPointerException e){
 			return null;
 		}
 	}
 	
 }
