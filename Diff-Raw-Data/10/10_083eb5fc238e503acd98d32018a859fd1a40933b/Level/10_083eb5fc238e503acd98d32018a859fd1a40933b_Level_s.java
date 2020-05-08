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
     Hero hero;
     
 	public Level(){
 		create();
 	}
 	
 	public void create(){
 		System.out.println("In create");
 		map = TiledLoader.createMap(Gdx.files.internal("data/level/test2.tmx"));
 		System.out.println("Tiles loaded");
 	    atlas = new SimpleTileAtlas(map, Gdx.files.internal("data/"));
 	    System.out.println("atlas made");
 	    populate();
 	    
 	}
 
 	
 	private void addEntity(Entity ent){
 		entities.add(ent);
 	}
 	
 	private boolean removeEntity(Entity ent){
 		return entities.removeValue(ent, true);
 	}
 	
 	public Hero getHero(){
 		return hero;
 	}
 	
 	// Populates entity list
 	private void populate(){
<<<<<<< HEAD
		addEntity(new Hero(1, 1, "data/entity/Hero.png")); // temporary entity gen
		addEntity(new Entity(1, 2, "data/entity/Enemy.png"));
		addEntity(new Entity(2, 1, "data/entity/Enemy.png"));
		addEntity(new Entity(2, 2, "data/entity/Hero.png"));	
=======
 		addEntity(new Hero(1, 1, "Hero")); // temporary entity gen
 		addEntity(new Entity(1, 2, "Enemy"));
 		addEntity(new Entity(2, 1, "Enemy"));
 		addEntity(new Entity(2, 2, "Hero"));	
>>>>>>> aspect ratio stuff
 	}
 	
 	public Array<Entity> getEntities(){
 		return this.entities;
 	}
 }
