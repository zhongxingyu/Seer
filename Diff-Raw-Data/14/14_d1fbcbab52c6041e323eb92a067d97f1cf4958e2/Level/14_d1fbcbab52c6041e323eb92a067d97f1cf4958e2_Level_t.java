 package com.me.Roguish.Model;
 import com.me.Roguish.Model.Entity;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.graphics.g2d.tiled.SimpleTileAtlas;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledLoader;
 import com.badlogic.gdx.graphics.g2d.tiled.TileAtlas;
 
 public class Level{
 	private static int maxEntity = 16;
 	private Array<Entity> entities = new Array<Entity>();
 
 	
     public TiledMap map;
     public SimpleTileAtlas atlas;
    private Entity hero = new Entity(5, 5, "Hero");
 
     
 	public Level(){
 		create();
 		populate();
 	}
 	
 	public void create(){
 		System.out.println("In create");
 		map = TiledLoader.createMap(Gdx.files.internal("data/level/test3.tmx"));
 		System.out.println("Tiles loaded");
 	    atlas = new SimpleTileAtlas(map, Gdx.files.internal("data/"));
 	    System.out.println("atlas made");  
 	}
 
 	
 	private void addEntity(Entity ent){
 		entities.add(ent);
 	}
 	
 	private boolean removeEntity(Entity ent){
 		return entities.removeValue(ent, true);
 	}
 	
 	public Entity getHero(){
 		return hero;
 	}
 	
 	// Populates entity list
 	private void populate(){
 
 		addEntity(hero); 
 		addEntity(new Entity(1, 2, "Enemy"));
 		addEntity(new Entity(2, 1, "Enemy"));
 		addEntity(new Entity(2, 2, "Enemy"));	
 
 	}
 	
 	public Array<Entity> getEntities(){
 		return this.entities;
 	}
 	
 	public boolean tilePropCheck(int x, int y, String property){
		return ("true".equals(map.getTileProperty(getTile(x,y), property)));
 	}
 	public int getTile(int x, int y, int layer){
 		return map.layers.get(layer).tiles[y][x];
 	}
 	
 	public int getTile(int x, int y){
 		return map.layers.get(0).tiles[y][x];
 	}
 }
