 package com.clusterflux.concentric;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.content.Intent;
 import android.support.v4.app.NavUtils;
 import android.view.MenuItem;
 import java.io.*;
 import android.util.Log;
 import android.widget.Toast;
 import android.graphics.Bitmap;
 import java.util.*;
 import android.view.View.OnTouchListener;
 import java.math.*;
 import android.view.Display;
 import android.graphics.Point;
 import android.view.MotionEvent;
 import java.lang.Math;
 import android.widget.ImageView;
 
 public class GameActivity extends Activity {
 	
 	public MapView mapView;
 	public ImageView dPadView;
 	public World world;
 	public Player player;
 	public Camera camera;
 	
 	//hardcoded parameters for testing
 	public int screen_width;
 	public int screen_height;
 	public int tile_width;
 	public int tile_height;
 	public int cameraOffsetX;
 	public int cameraOffsetY;
					
				int count = 0;
	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 	
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.game);	
 		Log.d("LOGCAT", "GameActivity Started");
 		
 		//get the world_name from MenuActivity
 		Intent intent = getIntent();
 		String world_name = intent.getStringExtra(MenuActivity.EXTRA_MESSAGE);
 		
 		//load the world
 		try {
 			world = new World(this, world_name);
 		} catch (IOException e) {
 			//do nothing
 		} catch (ClassNotFoundException e) {
 			//do nothing
 		}
 		
 		//get the tile sizes
 		WorldFeatures worldFeatures = new WorldFeatures(this);		
 		tile_height = worldFeatures.tile_height;
 		tile_width = worldFeatures.tile_width;
 		
 		//get the window size and determine tiles per screen
 		Display display = getWindowManager().getDefaultDisplay();
 		Point size = new Point();
 		display.getSize(size);
 		screen_width = (size.y / tile_width) + 1;
 		screen_height = (size.x / tile_height) + 1;
 
 		//load the player
 		player = new Player(world.world_height/2, world.world_width/2); //hardcoded spawn point!
 		
 		//determine camera offset
 		if ((screen_width & 1) == 0) { //even
 			cameraOffsetX = screen_width/2 - 1;
 		} else { //odd
 			cameraOffsetX = screen_width/2;
 		}
 		
 		if ((screen_height & 1) == 0) { //even
 			cameraOffsetY = screen_height/2 - 1;
 		} else { //odd
 			cameraOffsetY = screen_height/2;
 		}
 		
 		//load the camera relative to the player
 		camera = new Camera(player.x - cameraOffsetX, player.y - cameraOffsetY);
 		
 		//Create a reference to the MapView object. set world, player, camera, tile sizes.
 		mapView = (MapView) findViewById(R.id.map_view);
 		mapView.setWorld(world);
 		mapView.setPlayer(player);
 		mapView.setCamera(camera);
 		mapView.setScreenSize(screen_height, screen_width);
 		
 		//Create a reference to the DPadView object, implement OnTouchListener
 		dPadView = (ImageView) findViewById(R.id.dpad_view);
 		//dPadView.setAlpha(127); //make it transparent
 		
 		dPadView.setOnTouchListener(new View.OnTouchListener() {
  
 			public boolean onTouch(View v, MotionEvent event) {
 				
 				//get DPad center coordinates - how to get this out of here?
 				int dPadCenter = dPadView.getHeight()/2;
 			
 				//get DPad touch coordinates
 				float touchX = event.getX();
 				float touchY = event.getY();
 								
 				if (event.getAction() == MotionEvent.ACTION_DOWN ) {
 						
 						//update based on DPad usage
 						float diffX = touchX - dPadCenter;
 						float diffY = touchY - dPadCenter;
 						//Log.d("LOGCAT", "diffX = " + diffX + "; diffY = " + diffY);
 
 						if (Math.abs(diffX) > Math.abs(diffY)) {
 							if (touchX > dPadCenter) { update(0,1,"right");  }
 							if (touchX < dPadCenter) { update(0,-1,"left"); }
 						}
 					
 						if (Math.abs(diffY) > Math.abs(diffX)) {
 							if (touchY > dPadCenter) { update(1,0,"down");  }
 							if (touchY < dPadCenter) { update(-1,0,"up"); }
 						}
 						
 				/*} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
 					
 					//update based on DPad usage
 					float diffX = touchX - dPadCenter;
 					float diffY = touchY - dPadCenter;
 					//Log.d("LOGCAT", "diffX = " + diffX + "; diffY = " + diffY);
 
 					if (Math.abs(diffX) > Math.abs(diffY)) {
 						if (touchX > dPadCenter) { update(0,1);  }
 						if (touchX < dPadCenter) { update(0,-1); }
 					}
 					
 					if (Math.abs(diffY) > Math.abs(diffX)) {
 						if (touchY > dPadCenter) { update(1,0);  }
 						if (touchY < dPadCenter) { update(-1,0); }
 					}*/
 						
 				} else if (event.getAction() == MotionEvent.ACTION_UP) {
 				
 					//reset coordinates when done touching
 					touchX = 0f;
 					touchY = 0f;
 				}
 			
 				return true;
 			}
 					
 		});
 		
 	}
 	
 	public void update(int moveX, int moveY, String direction) {
 		
 		int newPlayerX = player.x + moveX;
 		int newPlayerY = player.y + moveY;
 		int newCameraX = camera.x + moveX;
 		int newCameraY = camera.y + moveY;
 		
 		//set direction
 		player.changeDirection(direction);
 		
 		//check if player is trying to go out of bounds
 		if ( newPlayerY > world.world_width - 1 || newPlayerX > world.world_height - 1 || newPlayerY < 0 || newPlayerX < 0) {
 		
 			Log.d("LOGCAT", "Player attempting to go out of bounds");
 			
 		} else { //check for collisions
 		
 			if (world.world_map2[newPlayerX][newPlayerY] != 0) { //there's a block on 2nd layer
         
             Log.d("LOGCAT", "Trying to cross 2nd layer. Cancel move");
             
 			} else { //update player and camera positions
 				//Log.d("LOGCAT", "screenX,Y = (" + screen_height + "," + screen_width + ")");
 
 				player.move(moveX, moveY);
 				
 				//camera out of bounds condition
 				if ( ( moveX == 1  && newCameraX < world.world_height - screen_width + 1 && newPlayerX > 			 
 				     cameraOffsetX )||
 				     ( moveY == 1  && newCameraY < world.world_width - screen_height + 1 && newPlayerY >          cameraOffsetY )||
 				     ( moveX == -1 && newCameraX >= 0  && newPlayerX < world.world_height - (screen_width/2 + 1)       )||	 
 				     ( moveY == -1 && newCameraY >= 0  && newPlayerY < world.world_width - (screen_height/2 + 1)       )) 
 				{
 					camera.move(moveX, moveY);	
 				}
 				
 			}
 				
 		}
 		
 	}
 	
 	public void updateWorld(View view) {
 	
 		Point block = player.isFacing();
 		
		if ( world.world_map2[block.x][block.y] != 0 ) {
 		
 			world.world_map2[block.x][block.y] = 0;
 			
 		} else {
 		
 			Log.d("LOGCAT", "no block to break");
 			
 		}
 		
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
