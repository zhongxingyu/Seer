 package com.clusterflux.concentric;
 
 import android.graphics.Canvas;
 import android.view.View;
 import android.content.Context;
 import android.util.AttributeSet;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import java.util.*;
 import java.math.*;
 import java.io.FileInputStream;
 import java.io.ObjectInputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import android.util.Log;
 import android.widget.Toast;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.graphics.BitmapFactory;
 import android.view.SurfaceView;
 import android.view.SurfaceHolder;
 
 
 public class MapView extends SurfaceView implements SurfaceHolder.Callback {
 
 	protected Context context;
 	public World world;
 	public Map<Integer,Bitmap> TILE_MAP;
 	public Map<String,Bitmap> SHADOW;
 	public Bitmap SPRITE;
 	public Player player;
 	public Camera camera;
 	//public Bitmap overlayBitmap;
 	
 	//hardcoded parameters for testing
 	public int tile_width;
 	public int tile_height;
 	public int screen_width;
 	public int screen_height;
 	public int spriteX = 0;
 	public int spriteY = 0;
 
 	public MapThread mapThread; 
 	
 	public MapView(Context context, AttributeSet attrs) {
 	
 		super(context, attrs);
 		this.context = context;
 		Log.d("LOGCAT", "MapView created");	
 		
 		//get the tile map
 		WorldFeatures worldFeatures = new WorldFeatures(context);		
 		TILE_MAP = worldFeatures.TILE_MAP;
 		SHADOW = worldFeatures.SHADOW;
 		SPRITE = worldFeatures.SPRITE;
 		tile_height = worldFeatures.tile_height;
 		tile_width = worldFeatures.tile_width;
 		
 		Log.d("LOGCAT", "tile_height = " + tile_height);
 		Log.d("LOGCAT", "tile_width = " + tile_width);
 		
 		SurfaceHolder holder = getHolder();
 		holder.addCallback(this);
 		
 	}
 	
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }
 	
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) { 
 	
 		mapThread = new MapThread(holder, context, this);
 		mapThread.setRunning(true);
 		mapThread.start();
 		
 	}
 	
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) { 
 	
 		mapThread.setRunning(false);
 		boolean retry = true;
 		
 		while (retry) {
 		
 			try {
 				mapThread.join();
 				retry = false;
 			} catch (Exception e) {
 				Log.d("LOGCAT", e.getMessage());
 			}
 		
 		}
 		
 	}
 	
 	public void doDraw(Canvas canvas) {
 					
 		canvas.drawColor(Color.BLACK);
 		
 		canvas.translate(0, - tile_height/2); //account for null space at top of tiles
 
 		int screenX = 0; //reset screenX each loop - this is where we will add an if statement to draw one column only
 		
		for (int x = camera.x; x < camera.x + screen_height; x += 1, screenX += 1) {
 			
 			int screenY = 0; //reset screenY each loop - this is where we will add an if statement to draw one row only
 			
 			for (int y = camera.y; y < camera.y + screen_width; y += 1, screenY += 1) {
 			
 				//LAYER 1:
 				if (world.world_map[x][y] != 0) {
 					
 					//draw tile
 					canvas.drawBitmap(TILE_MAP.get(world.world_map[x][y]), screenY*tile_height , screenX*tile_width, null);
 				
 					if (player.x == x && player.y == y) { //if the player is standing here, draw the sprite
 						//draw sprite
 						
 						spriteX = player.movement;
 						
 						if (player.direction.equals("right")) { spriteY = 2; } 
 						else if (player.direction.equals("left")) { spriteY = 1; } 
 						else if (player.direction.equals("up")) { spriteY = 3; } 
 						else if (player.direction.equals("down")) { spriteY = 0; } 
 						
 						Rect src = new Rect((SPRITE.getHeight()/4)*spriteX, (SPRITE.getWidth()/3)*spriteY, 
 						  (SPRITE.getHeight()/4)*(spriteX+1), (SPRITE.getWidth()/3)*(spriteY+1));
 						Rect dest = new Rect(screenY*tile_height, screenX*tile_width + tile_width/4, 
 						  (screenY + 1)*tile_height, (screenX + 1)*tile_width + tile_width/3);
 						/*canvas.drawBitmap(SPRITE, screenY*tile_height + tile_height/8, screenX*tile_width + tile_width/4, null);*/
 						canvas.drawBitmap(SPRITE, src, dest, null);
 					}
 					
 					//draw shadows
 					if (y != world.world_height - 1 && x != world.world_width - 1) {
 					if (world.world_map2[x+1][y+1] != 0 && world.world_map2[x][y+1] == 0) {
 						canvas.drawBitmap(SHADOW.get("southeast"), screenY*tile_height , screenX*tile_width, null);
 					}}
 					if (x != world.world_width - 1) {
 					if (world.world_map2[x+1][y] != 0) {
 						canvas.drawBitmap(SHADOW.get("south"), screenY*tile_height , screenX*tile_width, null);
 					}}
 					if (x != world.world_width - 1 && y != 0) {
 					if (world.world_map2[x+1][y-1] != 0 && world.world_map2[x][y-1] == 0) {
 						canvas.drawBitmap(SHADOW.get("southwest"), screenY*tile_height , screenX*tile_width, null);
 					}}
 					if (y != world.world_height - 1) {
 					if (world.world_map2[x][y+1] != 0) {
 						canvas.drawBitmap(SHADOW.get("east"), screenY*tile_height , screenX*tile_width, null);
 					}}
 					if (y != 0) { 
 					if (world.world_map2[x][y-1] != 0) {
 						canvas.drawBitmap(SHADOW.get("west"), screenY*tile_height , screenX*tile_width, null);
 					}}
 					if (x != 0 && y != world.world_height - 1) {
 					if (world.world_map2[x-1][y+1] != 0 && world.world_map2[x-1][y] == 0 && world.world_map2[x][y+1] == 0) {
 						canvas.drawBitmap(SHADOW.get("northeast"), screenY*tile_height , screenX*tile_width, null);
 					}}
 					if (x != 0) {
 					if (world.world_map2[x-1][y] != 0) {
 						canvas.drawBitmap(SHADOW.get("north"), screenY*tile_height , screenX*tile_width, null);
 					}}
 					if (x != 0 && y != 0) {
 					if (world.world_map2[x-1][y-1] != 0 && world.world_map2[x][y-1] == 0 && world.world_map2[x-1][y] == 0) {
 						canvas.drawBitmap(SHADOW.get("northwest"), screenY*tile_height , screenX*tile_width, null);
 					}}
 					
 				}
 				
 				//LAYER 2:
 				if (world.world_map2[x][y] != 0) {
 				
 					//draw tile
 					canvas.drawBitmap(TILE_MAP.get(world.world_map2[x][y]), screenY*tile_height, screenX*tile_width - tile_width/2, null);
 					
 					//draw shadows
 					if (x != world.world_width - 1 && y != 0) {
 					if (world.world_map2[x+1][y-1] != 0 && world.world_map2[x+1][y] == 0) {
 						canvas.drawBitmap(SHADOW.get("sidewest"), screenY*tile_height , screenX*tile_width, null);
 					}}
 					if (x != 0) {
 					if (world.world_map2[x-1][y] == 0 && world.world_map[x-1][y] == 0) {
 						canvas.drawBitmap(SHADOW.get("south"), screenY*tile_height , screenX*tile_width, null);
 					}}
 					
 				}
 				
 			}
 			
 		}
 						
 	}
 	
 	public void setWorld(World world){
 		
 		this.world = world;
 		
 	}
 	
 	public void setPlayer(Player player) {
 	
 		this.player = player;
 		
 	}
 	
 	public void setCamera(Camera camera) {
 	
 		this.camera = camera;
 		
 	}
 	
 	public void setScreenSize(int screen_width, int screen_height) {
 	
 		this.screen_width = screen_width;
 		this.screen_height = screen_height;
 		
 	}
 
 }
