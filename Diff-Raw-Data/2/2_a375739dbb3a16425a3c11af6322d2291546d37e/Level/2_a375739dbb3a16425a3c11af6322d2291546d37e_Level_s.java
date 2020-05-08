 package com.me.Roguish.Model;
 
 import com.me.Roguish.Model.Entity;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.graphics.g2d.tiled.*;
 
 public class Level{
 	private static int maxEntity = 16;
 	private Array<Entity> entities = new Array<Entity>();
 	
     public TiledMap map = TiledLoader.createMap(Gdx.files.internal("data/test.tmx"));
    public TileAtlas atlas = new TileAtlas(map, Gdx.files.internal("data/levels/pack"));
 
     
 	public Level(){
 		
 	
 	}
 	
 
 }
