 package com.multimedia.slidepuzzel;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.hardware.Camera.Size;
 import android.util.Log;
 import android.view.MotionEvent;
 
 import com.multimedia.slidepuzzel.gamelogic.Game;
 
 public class DrawGame{
 	public Size imageSize;
 
 	private Game game;
 	private Paint p;
 	
 	private int[] rgb;
 	private Bitmap bitmap;
 	
 	private int tileSize;
 	private Rect[][] defaultRect;
 	
 	private int swapX;
 	private int swapY;
 	private int animX;
 	private int animY;
 	private int anim;
 	private Rect animRect;
 	
	private float scaleFactor;
	
 	public Context mContext;
 
 	public DrawGame(Activity activity){
 		game = ((GameActivity) activity).getGame();
 		
 		
 		game.getField().swapTile(game.getField().getNullX(), game.getField().getNullY() - 1);
 		// Inactive animation state
 		anim = -1;
 		swapX = -1;
 		swapY = -1;
 		
 		//game.shuffle();
 		
 		p = new Paint();
 		
 		mContext = ((GameActivity) activity).gContext();
 	}
 
 	public void imageReceived(byte[] data) {
 		// Allocate the image as an array of integers if needed.
 		// Then, decode the raw image data in YUV420SP format into a red-green-blue array (rgb array)
 		
 		if(rgb == null){
 			int arraySize = imageSize.width*imageSize.height;
 			rgb = new int[arraySize];
 			
 			// Calculate tile length & height by the height of the camera image
 			tileSize = imageSize.height / game.getSize();
 		
 			// Make default mapping rectangles
 			defaultRect = new Rect[game.getSize()][game.getSize()];
 			for(int x = 0; x < game.getSize(); x++){
 				for(int y = 0; y < game.getSize(); y++){
 					defaultRect[x][y] = new Rect(
 							(x * tileSize), (y * tileSize), 
 							((x + 1) * tileSize), ((y + 1) * tileSize)
 						);
 					
 					game.setTile(defaultRect[x][y], game.getDefaultField().getTileIdx(x, y));
 				}
 			}
 		}
 		decodeYUV420SP(rgb, data);
 		
 		// Create bitmap
 		// TODO reuse bitmap
 		bitmap = Bitmap.createBitmap(rgb, imageSize.width, imageSize.height, Bitmap.Config.ARGB_8888);
 		// Apply rotation
 		bitmap = game.getRotation().apply(bitmap);
 	}
 
 	public void draw(Canvas c){
 		if(anim > 0){
 			anim--;
 			// Move animating rectangle
 			animRect.offset(animX, animY);
 		}else if(anim == 0){	
 			// Swap the actual tile
 			game.getField().swapTile(swapX, swapY);
 			
 			// Last swap solved the puzzle
 			if(game.checkPuzzleSolved()){
 				game.getSound().playSound(game.getSound().win);
 				
 				int time = (int) (game.getGameTime().getTimeElapsed() / 1000);
 				Intent intent = new Intent().setClass(mContext, WinActivity.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				intent.putExtra("time", time);
 	            mContext.startActivity(intent);
 			}
 			
 			// Reset
 			anim = -1;
 			swapX = -1;
 			swapY = -1;
 			animRect = null;
 		}
 		p.setColor(combine(0, 0, 0));
 		
 		// Scale
		scaleFactor = (float) c.getHeight() / (float) (game.getSize() * tileSize);
		c.scale(scaleFactor, scaleFactor, 0, 0);
 		
 		// Draw the empty square black
 		c.drawRect(defaultRect[game.getField().getNullX()][game.getField().getNullY()], p);
 		
 		Rect t;
 		int tIdx;
 		for(int x = 0; x < game.getSize(); x++){
 			for(int y = 0; y < game.getSize(); y++){
 				tIdx = game.getField().getTileIdx(x, y);
 				
 				// Skip empty square
 				if(tIdx == 0){
 					continue;
 				}
 				
 				t = game.getTile(tIdx);
 				
 				if(x != swapX || y != swapY){
 					// Draw rectangle t of the bitmap into rectangle defaultRectangle[x][y]
 					c.drawBitmap(bitmap, t, defaultRect[x][y], null);		
 				}else if(animRect != null){
 					// Draw place from where swapped black
 					c.drawRect(defaultRect[x][y], p);
 					// Draw in animate rectangle, paint over black (sliding animation)
 					c.drawBitmap(bitmap, t, animRect, null);
 				}
 				
 				// Draw surrounding lines
 				c.drawLine((x * tileSize), (y * tileSize), ((x + 1) * tileSize), (y * tileSize), p);
 				c.drawLine((x * tileSize), ((y + 1) * tileSize), ((x + 1) * tileSize), ((y + 1) * tileSize), p);
 				
 				c.drawLine((x * tileSize), (y * tileSize), (x * tileSize), ((y + 1) * tileSize), p);
 				c.drawLine(((x + 1) * tileSize), (y * tileSize), ((x + 1) * tileSize), ((y + 1) * tileSize), p);
 				
 			}
 		}
 		
 		// Draw solved each time
 		/*if(game.isSolved()){
 			//p.setColor(combine(255, 0, 0));
 			//c.drawText("You have solved the puzzle", 50, 50, p);
 			//c.drawText("Time: " + (game.getGameTime().getTimeElapsed() / 1000) + " sec", 50, 70, p);
 			int time = (int) (game.getGameTime().getTimeElapsed() / 1000);
 			Intent intent = new Intent().setClass(mContext, WinActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			intent.putExtra("time", time);
             mContext.startActivity(intent);
 		}*/
 	}
 	
 	public void onTouchEvent(MotionEvent event){
 		Log.d("TouchEvent", "Raw touch event on " + event.getX() + ", " + event.getY());
 		if(anim == -1 && !game.isSolved()){		
			float x =  event.getX();
			float y =  event.getY();
			Log.d("TouchEvent", "Touch on unscaled " + x + ", " + y);
			x /= scaleFactor;
			y /= scaleFactor;
			swapX = (int) Math.round(x);
			swapY = (int) Math.round(y);
			Log.d("TouchEvent", "Touch on scaled " + swapX + ", " + swapY + " tilesize " + tileSize);
 			swapX /= tileSize;
 			swapY /= tileSize;
 			Log.d("TouchEvent", "Touch gives " + swapX + ", " + swapY);
 			
 			if(game.getField().validSwap(swapX, swapY)){
 				Log.d("TouchEvent", "Starting anim on " + swapX + ", " + swapY);
 				// Animation timer
 				anim = tileSize / 20;
 				
 				animRect = new Rect(game.getTile(game.getDefaultField().getTileIdx(swapX, swapY)));
 				
 				// Shift of rectangle each draw
 				animX = game.getField().getNullX() - swapX;
 				animY = game.getField().getNullY() - swapY;
 				animX *= 20;
 				animY *= 20;
 				
 				game.getSound().playSound(game.getSound().swap);
 			}else{
 				swapX = -1;
 				swapY = -1;
 			}
 		}
 	}
     
     // Combine red, green and blue into a single color int
     private int combine(int r, int g, int b) {
     	 return 0xff000000 | (r << 16) | (g << 8) | b;
     }
     
     /*
      * Decode the incoming data (YUV format) to a red-green-blue format
      */
 	private void decodeYUV420SP(int[] rgb, byte[] yuv420sp) {
 		final int width = imageSize.width;
 		final int height = imageSize.height;
     	final int frameSize = width * height;
     	
     	for (int j = 0, yp = 0; j < height; j++) {
     		int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
     		for (int i = 0; i < width; i++, yp++) {
     			int y = (0xff & ((int) yuv420sp[yp])) - 16;
     			if (y < 0) y = 0;
     			if ((i & 1) == 0) {
     				v = (0xff & yuv420sp[uvp++]) - 128;
     				u = (0xff & yuv420sp[uvp++]) - 128;
     			}
     			
     			int y1192 = 1192 * y;
     			int r = (y1192 + 1634 * v);
     			int g = (y1192 - 833 * v - 400 * u);
     			int b = (y1192 + 2066 * u);
     			
     			if (r < 0) r = 0; else if (r > 262143) r = 262143;
     			if (g < 0) g = 0; else if (g > 262143) g = 262143;
     			if (b < 0) b = 0; else if (b > 262143) b = 262143;
     			
     			rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
     		}
     	}
     }
 }
