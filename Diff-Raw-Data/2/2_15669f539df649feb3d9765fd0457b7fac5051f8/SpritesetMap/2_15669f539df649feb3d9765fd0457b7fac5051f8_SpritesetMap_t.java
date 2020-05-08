 package com.me.mygdxgame.sprite;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Random;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Intersector;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.math.collision.Ray;
 import com.me.mygdxgame.game.Game;
 import com.me.mygdxgame.game.GameEvent;
 import com.me.mygdxgame.game.GameMap;
 import com.me.mygdxgame.mgr.SpriteMgr;
 import com.me.mygdxgame.mgr.WindowMgr;
 import com.me.mygdxgame.utils.Cst;
 import com.me.mygdxgame.utils.Point2i;
 
 public class SpritesetMap {
 
 	private SpriteBatch batch;
 
 	private byte[][] tilemap;
 
 	public SpritesetMap(){
 		createSpriteBatches();
 		createTilemap();
 	}
 
 	private void createSpriteBatches(){
 		batch = new SpriteBatch();
 	}
 
 	private void createTilemap(){
 		tilemap = new byte[Game.map.getMapSize().x][Game.map.getMapSize().y];
 
 		Random rand = new Random();
 
 		for(int i=0; i<tilemap.length; i++){
 			for(int j=0; j<tilemap[0].length; j++){
 				int n = rand.nextInt(255);
 				if(n < 200){
 				tilemap[i][j] = Cst.FLOOR;
 				} else{
 					tilemap[i][j] = Cst.WALL;
 				}
 			}
 		}
 	}
 
 	public void highlightTile(float x, float y) {
 		//highlightedI = isoToI(x,y);
 		//highlightedJ = isoToJ(x,y);
 	}
 
 
 	public void update(){
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		batch.setProjectionMatrix(Game.cam.combined);
 		batch.begin();
 
 		Vector3 inter = new Vector3();
 
 		Ray pickRay = Game.cam.getPickRay(0, 0);
 		Intersector.intersectRayPlane(pickRay, Cst.XY_PLANE, inter);
 
 		float sx = inter.x;
 		float sy = inter.y;
 
 		pickRay = Game.cam.getPickRay(Gdx.graphics.getWidth(), 0);
 		Intersector.intersectRayPlane(pickRay, Cst.XY_PLANE, inter);
 		float sw = inter.x;
 
 		pickRay = Game.cam.getPickRay(0, Gdx.graphics.getHeight());
 		Intersector.intersectRayPlane(pickRay, Cst.XY_PLANE, inter);
 		float sh = inter.y;
 
 		Point2i pos = new Point2i(0, 0);
 		SpriteTile spriteTile;
 		SpriteStatic spriteStatic;
 		List<GameEvent> events;
 		List<Sprite> list = new ArrayList<Sprite>();
 		
 		int iStart = (int) (sx/Cst.TILE_W) - 1;
 		int jStart = (int) (sy/Cst.TILE_HH) - 1;
 		int iEnd = (int) (sw/Cst.TILE_W) + 1;
 		int jEnd = (int) (sh/Cst.TILE_HH) + 1;
 		
 		iStart = Math.max(iStart, 0);
 		jStart = Math.max(jStart, 0);
 		iEnd = Math.min(iEnd, Game.map.getMapSize().x);
 		jEnd = Math.min(jEnd, Game.map.getMapSize().y);
 		
 		int nbrendered = 0;
 		
 		for(int j=jStart; j < jEnd; j++) {
 			for(int i=iStart; i < iEnd; i++) {
 			
 				pos.y = j*Cst.TILE_HH; //+cam y
 				if (j % 2 == 0) {
 					pos.x = i*Cst.TILE_W; //+cam x
 				} else {
 					pos.x = i*Cst.TILE_W + Cst.TILE_HW; //+cam x
 				}
 				
 				
 				spriteTile = SpriteMgr.getTile(tilemap[i][j]);
 
 				spriteTile.setPosition(pos.x, pos.y - spriteTile.getElevation());
 				spriteTile.draw(batch);
 				nbrendered++;
 				
 				
 				
 				events = Game.map.eventsAt(i,j);
 				if(events != null){
 					for(GameEvent event : events){
 						spriteStatic = SpriteMgr.getStatic(event.getId());
 						spriteStatic.setElevation(spriteTile.getElevation());
 						spriteStatic.update(event);
 						list.add(spriteStatic);
 					}
 				}
 				
 				
 				Collections.sort(list, new Comparator<Sprite>() {
 					@Override public int compare(Sprite s1, Sprite s2) {
 						return (int) (s1.getY() - s2.getY());
 					}
 				});
 
 				for(Sprite spr : list){
 					spr.draw(batch);
 					nbrendered++;
 				}
 				
				list.clear();
				
 			}
 		}
 		
 		WindowMgr.spriteNumberLabel.setText("Draw number: " + nbrendered);
 		
 		/*
 		int iStart = isoToI(sx, sy) - 2;
 		int jStart = isoToJ(sx + sw, sy) - 2;
 		int iMax = isoToI(sx + sw, sy + sh) + 2;
 		int jMax = isoToJ(sx, sy + sh) + 2;
 
 		iStart = Math.max(iStart, 0);
 		jStart = Math.max(jStart, 0);
 		iMax = Math.min(iMax, Game.map.getWidthInTiles());
 		jMax = Math.min(jMax, Game.map.getHeightInTiles());
 		
 		SpriteTile spriteTile;
 		SpriteStatic spriteStatic;
 		List<GameEvent> events;
 		List<Sprite> list = new ArrayList<Sprite>();
 		
 		for (int i = iStart; i < iMax; i++) {
 			for (int j = jStart; j < jMax; j++) {
 				
 				spriteTile = TileMgr.get(tilemap[i][j]);
 
 				int x = i * Cst.TILE_HW - j * Cst.TILE_HW;
 				int y = i * Cst.TILE_HH + j * Cst.TILE_HH - spriteTile.getElevation();
 				
 				if(i == highlightedI && j == highlightedJ) {
 					highlightedTile = SpriteTile.getHighlightedTile(spriteTile);
 					highlightedTile.setPosition(x, y);
 					highlightedTile.draw(batch);
 				} else {
 					spriteTile.setPosition(x, y);
 					spriteTile.draw(batch);
 				}
 				
 				events = Game.map.eventsAt(i,j);
 				if(events != null){
 					for(GameEvent event : events){
 						spriteStatic = StaticMgr.get(event.getId());
 						spriteStatic.setElevation(spriteTile.getElevation());
 						spriteStatic.update(event);
 						list.add(spriteStatic);
 					}
 				}
 				
 				Collections.sort(list, new Comparator<Sprite>() {
 					@Override public int compare(Sprite s1, Sprite s2) {
 						return (int) (s1.getY() - s2.getY());
 					}
 				});
 
 				for(Sprite spr : list){
 					spr.draw(batch);
 				}
 			}
 		}
 		*/
 		batch.end();
 
 	}
 
 }
