 /* author: Christopher Farm */
 
 package com.appordinance.ImagePuzzle;
 
 import com.appordinance.ImagePuzzle.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TableRow.LayoutParams;
 import android.widget.*;
 
 public class GamePlay extends Activity implements OnClickListener {
 	ImageView[][] image_array;
 
 	// create shared preferences string
 	final String GAME_SETTINGS = "mySettings";
 	
 	//set integers for each of the levels
 	final int EASY = 3;
 	final int MEDIUM = 4;
 	final int HARD = 5;
 	int level;
 	
 	//keep track of the moves
 	int moves = 0;
 	
 	//create a table layout
 	TableLayout tl;
 	
 	//create parameters for the table and the images 
 	LayoutParams layout = new LayoutParams(LayoutParams.WRAP_CONTENT,
 			LayoutParams.MATCH_PARENT);
 	LayoutParams layout_image = new LayoutParams(LayoutParams.WRAP_CONTENT,
 			LayoutParams.WRAP_CONTENT);
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		//get resources for the shared preferences
 		SharedPreferences settings = getSharedPreferences(GAME_SETTINGS, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		
 		// set default level if there is nothing in the shared preferences
 		if (settings.contains("lvl") == false) {
 			level = EASY;
 		}
 
 		// if there are settings for the level stored then get the settings for
 		// the level that are stored -> default also set to EASY
 		if (settings.contains("lvl") == true) {
 			level = settings.getInt("lvl", EASY);
 		}
 
 		// get screen width and height
 		Display display = getWindowManager().getDefaultDisplay();
 		Point size = new Point();
 		display.getSize(size);
 		final double SCREEN_WIDTH = size.x;
 		final double SCREEN_HEIGHT = size.y;
 
 		// create a new TableLayout so I can put images into the table
 		tl = new TableLayout(this);
 
 		// place the table in the center of the Activity
 		tl.setGravity(Gravity.CENTER);
 
 		// set the layout parameters for the Table
 		tl.setLayoutParams(layout);
 
 		// counter will keep track of the tile number
 		int tile_counter = 0;
 
 		// use easy for now to keep track of the number of tiles in the table
 		// Create Tile array for the solution and for the puzzle
 		image_array = new ImageView[level][level];
 
 		// get the clicked drawable in the list and set it as the bitmap
 		Bundle extra = getIntent().getExtras();
 
 		try {
 			// Create a scaled bitmap
 			Bitmap bmap = BitmapFactory.decodeResource(getResources(),
 					extra.getInt("pictureRef"));
 			
 			//save the reference to the image used and commit the changes
 			editor.putInt("imageUsed", extra.getInt("pictureRef"));
 			editor.commit();
 			
 			//define the dimensions and keep track of the aspect ratio
 			double bmap_width = bmap.getWidth();
 			double bmap_height = bmap.getHeight();
 			double bmap_aspectRatio = bmap_width / bmap_height;
 
 			// while the image is larger than the screen, shrink the size
 			if (SCREEN_WIDTH < bmap_width) {
 				// then use the screen_width as the limiting factor
 				bmap_width = SCREEN_WIDTH;
 				bmap_height = bmap_width / bmap_aspectRatio;
 			}
 
 			else if (SCREEN_HEIGHT < bmap_height) {
 				// then use the screen_height as the limiting factor
 				bmap_height = SCREEN_HEIGHT;
 				bmap_width = bmap_height * bmap_aspectRatio;
 			}
 
 			/*
 			 * CREATE THE INCREMENTS FOR WIDTH AND HEIGHT - NEED TO USE THIS TO
 			 * CREATE SEVERAL BITMAPS
 			 */
 			int width_increment = 0;
 			int height_increment = 0;
 			if (level == EASY) {
 				width_increment = (int) bmap_width / EASY;
 				height_increment = (int) bmap_height / EASY;
 			}
 			if (level == MEDIUM) {
 				width_increment = (int) bmap_width / MEDIUM;
 				height_increment = (int) bmap_height / MEDIUM;
 			}
 			if (level == HARD) {
 				width_increment = (int) bmap_width / HARD;
 				height_increment = (int) bmap_height / HARD;
 			}
 
 			// scaled full size bitmap
 			Bitmap bmapScaled = Bitmap.createScaledBitmap(bmap,
 					(int) bmap_width, (int) bmap_height, true);
 
 			// create place holders for each of the increments
 			int startX, startY, width, height;
 			
 			//dividers for each of the tiles
 			int padding = 1;
 
 			// create table of tiles to display the solution
 			for (int i = 0; i < level; i++) {
 
 				for (int j = 0; j < level; j++) {
 					tile_counter++;
 					// create a new image
 					ImageView new_image = new ImageView(this);
 
 					new_image.setLayoutParams(layout_image); // set the layout
 					
 					//keep track of the x and y positions
 					startX = j * width_increment;
 					startY = i * height_increment;
 					width = width_increment;
 					height = height_increment;
 					// create bmap for each of the tiles
 					Bitmap temp_bmp = Bitmap.createBitmap(bmapScaled, startX,
 							startY, width, height);
 
 					new_image.setPadding(padding, padding, padding, padding);
 					new_image.setImageBitmap(temp_bmp);
 
 					// create a new Tile in the array
 					image_array[i][j] = new ImageView(this);
 
 					// store the tiles in an array
 					image_array[i][j] = new_image;
 					image_array[i][j].setId(tile_counter);
 
 				}
 
 			}
 
 			// create last image and set the ID to the last cell
 			ImageView lastImg = new ImageView(this);
 			lastImg.setImageBitmap(Bitmap.createBitmap(width_increment,
 					height_increment, Bitmap.Config.ARGB_8888));
 			image_array[level - 1][level - 1] = lastImg;
 
 			//set the id for the last to make sure it's in the right place
 			image_array[level - 1][level - 1].setId(level * level);
 
 			// if the game has been played before then keep track of the moves from where it last was -> default set to 0
 			if(settings.getBoolean("hasPlayed", false)==true){
 				moves = settings.getInt("numMoves", 0);
 			}
 			
 			//if it's an EASY level and has been played then place tiles on the board based on shared preferences
 			if (level == EASY && settings.getBoolean("hasPlayed", false) == true) {
 				
 				ImageView[][] imageArray = new ImageView[level][level];
 				// find the ID in the image_array and put in the respective
 				// position
 				for (int i = 0; i < level; i++) {
 					for (int j = 0; j < level; j++) {
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos1e", 0)) {
 							imageArray[0][0] = image_array[i][j];
 
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos2e", 0)) {
 							imageArray[0][1] = image_array[i][j];
 
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos3e", 0)) {
 							imageArray[0][2] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos4e", 0)) {
 							imageArray[1][0] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos5e", 0)) {
 							imageArray[1][1] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos6e", 0)) {
 							imageArray[1][2] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos7e", 0)) {
 							imageArray[2][0] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos8e", 0)) {
 							imageArray[2][1] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos9e", 0)) {
 							imageArray[2][2] = image_array[i][j];
 						}
 					}
 				}
 				
 				//set the global image array and the click listeners
 				for (int i = 0; i < level; i++) {
 					for (int j = 0; j < level; j++) {
 						image_array[i][j] = imageArray[i][j];
 						image_array[i][j].setOnClickListener(this);
 					}
 				}
 				
 				// set up the table with rows and insert the images
 				for (int i = 0; i < level; i++) {
 					TableRow new_tr = new TableRow(this);
 					new_tr.setLayoutParams(layout_image);
 					for (int j = 0; j < level; j++) {
 						new_tr.addView(image_array[i][j]);
 					}
 					tl.addView(new_tr);
 				}
 
 				// set the content to the view
 				setContentView(tl);
 				
 
 			}
 			
 			//now do the same thing if the level is at medium
 			if (level == MEDIUM && settings.getBoolean("hasPlayed", false) == true) {
 
 				ImageView[][] imageArray = new ImageView[level][level];
 				// find the ID in the image_array and put in the respective
 				// position
 				for (int i = 0; i < level; i++) {
 					for (int j = 0; j < level; j++) {
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos1m", 0)) {
 							imageArray[0][0] = image_array[i][j];
 
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos2m", 0)) {
 							imageArray[0][1] = image_array[i][j];
 
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos3m", 0)) {
 							imageArray[0][2] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos4m", 0)) {
 							imageArray[0][3] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos5m", 0)) {
 							imageArray[1][0] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos6m", 0)) {
 							imageArray[1][1] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos7m", 0)) {
 							imageArray[1][2] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos8m", 0)) {
 							imageArray[1][3] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos9m", 0)) {
 							imageArray[2][0] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos10m", 0)) {
 							imageArray[2][1] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos11m", 0)) {
 							imageArray[2][2] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos12m", 0)) {
 							imageArray[2][3] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos13m", 0)) {
 							imageArray[3][0] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos14m", 0)) {
 							imageArray[3][1] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos15m", 0)) {
 							imageArray[3][2] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos16m", 0)) {
 							imageArray[3][3] = image_array[i][j];
 						}
 					}
 				}
 
 				for (int i = 0; i < level; i++) {
 					for (int j = 0; j < level; j++) {
 						image_array[i][j] = imageArray[i][j];
 						image_array[i][j].setOnClickListener(this);
 					}
 				}
 				
 				// set up the table with rows and insert the images
 				for (int i = 0; i < level; i++) {
 					TableRow new_tr = new TableRow(this);
 					new_tr.setLayoutParams(layout_image);
 					for (int j = 0; j < level; j++) {
 						new_tr.addView(image_array[i][j]);
 					}
 					tl.addView(new_tr);
 				}
 
 				// set the content to the view
 				setContentView(tl);
 				
 
 			}
 			
 			//do the same thing if the level is at hard
 			if (level == HARD && settings.getBoolean("hasPlayed", false) == true) {
 
 				ImageView[][] imageArray = new ImageView[level][level];
 				// find the ID in the image_array and put in the respective
 				// position
 				for (int i = 0; i < level; i++) {
 					for (int j = 0; j < level; j++) {
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos1h", 0)) {
 							imageArray[0][0] = image_array[i][j];
 
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos2h", 0)) {
 							imageArray[0][1] = image_array[i][j];
 
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos3h", 0)) {
 							imageArray[0][2] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos4h", 0)) {
 							imageArray[0][3] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos5h", 0)) {
 							imageArray[0][4] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos6h", 0)) {
 							imageArray[1][0] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos7h", 0)) {
 							imageArray[1][1] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos8h", 0)) {
 							imageArray[1][2] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos9h", 0)) {
 							imageArray[1][3] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos10h", 0)) {
 							imageArray[1][4] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos11h", 0)) {
 							imageArray[2][0] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos12h", 0)) {
 							imageArray[2][1] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos13h", 0)) {
 							imageArray[2][2] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos14h", 0)) {
 							imageArray[2][3] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos15h", 0)) {
 							imageArray[2][4] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos16h", 0)) {
 							imageArray[3][0] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos17h", 0)) {
 							imageArray[3][1] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos18h", 0)) {
 							imageArray[3][2] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos19h", 0)) {
 							imageArray[3][3] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos20h", 0)) {
 							imageArray[3][4] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos21h", 0)) {
 							imageArray[4][0] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos22h", 0)) {
 							imageArray[4][1] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos23h", 0)) {
 							imageArray[4][2] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos24h", 0)) {
 							imageArray[4][3] = image_array[i][j];
 						}
 						if (image_array[i][j].getId() == settings.getInt(
 								"pos25h", 0)) {
 							imageArray[4][4] = image_array[i][j];
 						}
 					}
 				}
 
 				for (int i = 0; i < level; i++) {
 					for (int j = 0; j < level; j++) {
 						image_array[i][j] = imageArray[i][j];
 						image_array[i][j].setOnClickListener(this);
 					}
 				}
 				
 				// set up the table with rows and insert the images
 				for (int i = 0; i < level; i++) {
 					TableRow new_tr = new TableRow(this);
 					new_tr.setLayoutParams(layout_image);
 					for (int j = 0; j < level; j++) {
 						new_tr.addView(image_array[i][j]);
 					}
 					tl.addView(new_tr);
 				}
 
 				// set the content to the view
 				setContentView(tl);
 				
 
 			}
 			
 			//if the game hasn't been played before then just draw the board in it's original form
 			else{
 				// set up the table with rows and insert the images
 				for (int i = 0; i < level; i++) {
 					TableRow new_tr = new TableRow(this);
 					new_tr.setLayoutParams(layout_image);
 					for (int j = 0; j < level; j++) {
 						new_tr.addView(image_array[i][j]);
 					}
 					tl.addView(new_tr);
 				}
 
 				// set the content to the view
 				setContentView(tl);
 				
 				// rearrange Tiles after 3 seconds
 				Handler handler = new Handler();
 				handler.postDelayed(new Runnable() {
 					public void run() {
 
 						image_array = rearrangeImages(image_array, level);
 
 					}
 				}, 3000);
 
 				Toast.makeText(this, "Play!", Toast.LENGTH_SHORT).show();
 				editor.putBoolean("hasPlayed", true);
 				editor.commit();
 			}
 			
 				
 			
 		} catch (Exception e) {
 			//Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
 		}
 
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		//get resources to save information
 		SharedPreferences settings = getSharedPreferences(GAME_SETTINGS, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		
 		//save the number of moves
 		editor.putInt("numMoves", moves).commit();
 		
 		//keep a position id array
 		int[][] posID_array = new int[level][level];
 		
 		//initially keep track of the ids in each of the x and y positions
 		for (int i = 0; i < level; i++) {
 			for (int j = 0; j < level; j++) {
 				posID_array[i][j] = image_array[i][j].getId();
 			}
 
 		}
 
 		if (level == EASY) {
 			// SAVE THE STATE OF THE BOARD AT THE EASY LEVEL
 			int pos1e = posID_array[0][0];
 			int pos2e = posID_array[0][1];
 			int pos3e = posID_array[0][2];
 			int pos4e = posID_array[1][0];
 			int pos5e = posID_array[1][1];
 			int pos6e = posID_array[1][2];
 			int pos7e = posID_array[2][0];
 			int pos8e = posID_array[2][1];
 			int pos9e = posID_array[2][2];
 			editor.putInt("pos1e", pos1e);
 			editor.putInt("pos2e", pos2e);
 			editor.putInt("pos3e", pos3e);
 			editor.putInt("pos4e", pos4e);
 			editor.putInt("pos5e", pos5e);
 			editor.putInt("pos6e", pos6e);
 			editor.putInt("pos7e", pos7e);
 			editor.putInt("pos8e", pos8e);
 			editor.putInt("pos9e", pos9e);
 			editor.commit();
 		}
 		
 		if (level == MEDIUM){
 			int pos1m = posID_array[0][0];
 			int pos2m = posID_array[0][1];
 			int pos3m = posID_array[0][2];
 			int pos4m = posID_array[0][3];
 			int pos5m = posID_array[1][0];
 			int pos6m = posID_array[1][1];
 			int pos7m = posID_array[1][2];
 			int pos8m = posID_array[1][3];
 			int pos9m = posID_array[2][0];
 			int pos10m = posID_array[2][1];
 			int pos11m = posID_array[2][2];
 			int pos12m = posID_array[2][3];
 			int pos13m = posID_array[3][0];
 			int pos14m = posID_array[3][1];
 			int pos15m = posID_array[3][2];
 			int pos16m = posID_array[3][3];
 			editor.putInt("pos1m", pos1m);
 			editor.putInt("pos2m", pos2m);
 			editor.putInt("pos3m", pos3m);
 			editor.putInt("pos4m", pos4m);
 			editor.putInt("pos5m", pos5m);
 			editor.putInt("pos6m", pos6m);
 			editor.putInt("pos7m", pos7m);
 			editor.putInt("pos8m", pos8m);
 			editor.putInt("pos9m", pos9m);
 			editor.putInt("pos10m", pos10m);
 			editor.putInt("pos11m", pos11m);
 			editor.putInt("pos12m", pos12m);
 			editor.putInt("pos13m", pos13m);
 			editor.putInt("pos14m", pos14m);
 			editor.putInt("pos15m", pos15m);
 			editor.putInt("pos16m", pos16m);
 			editor.commit();
 		}
 		
 		if (level == HARD){
 			int pos1h = posID_array[0][0];
 			int pos2h = posID_array[0][1];
 			int pos3h = posID_array[0][2];
 			int pos4h = posID_array[0][3];
 			int pos5h = posID_array[0][4];	
 			int pos6h = posID_array[1][0];
 			int pos7h = posID_array[1][1];
 			int pos8h = posID_array[1][2];
 			int pos9h = posID_array[1][3];
 			int pos10h = posID_array[1][4];
 			int pos11h = posID_array[2][0];
 			int pos12h = posID_array[2][1];
 			int pos13h = posID_array[2][2];
 			int pos14h = posID_array[2][3];
 			int pos15h = posID_array[2][4];
 			int pos16h = posID_array[3][0];
 			int pos17h = posID_array[3][1];
 			int pos18h = posID_array[3][2];
 			int pos19h = posID_array[3][3];
 			int pos20h = posID_array[3][4];
 			int pos21h = posID_array[4][0];
 			int pos22h = posID_array[4][1];
 			int pos23h = posID_array[4][2];
 			int pos24h = posID_array[4][3];
 			int pos25h = posID_array[4][4];
 			editor.putInt("pos1h", pos1h);
 			editor.putInt("pos2h", pos2h);
 			editor.putInt("pos3h", pos3h);
 			editor.putInt("pos4h", pos4h);
 			editor.putInt("pos5h", pos5h);
 			editor.putInt("pos6h", pos6h);
 			editor.putInt("pos7h", pos7h);
 			editor.putInt("pos8h", pos8h);
 			editor.putInt("pos9h", pos9h);
 			editor.putInt("pos10h", pos10h);
 			editor.putInt("pos11h", pos11h);
 			editor.putInt("pos12h", pos12h);
 			editor.putInt("pos13h", pos13h);
 			editor.putInt("pos14h", pos14h);
 			editor.putInt("pos15h", pos15h);
 			editor.putInt("pos16h", pos16h);
 			editor.putInt("pos17h", pos17h);
 			editor.putInt("pos18h", pos18h);
 			editor.putInt("pos19h", pos19h);
 			editor.putInt("pos20h", pos20h);
 			editor.putInt("pos21h", pos21h);
 			editor.putInt("pos22h", pos22h);
 			editor.putInt("pos23h", pos23h);
 			editor.putInt("pos24h", pos24h);
 			editor.putInt("pos25h", pos25h);
 			editor.commit();
 		}
 		
 	}
 
 	public ImageView[][] rearrangeImages(ImageView[][] imageArray, int lvl) {
 
 		// on the rearrangement set the on click listeners so that it isn't
 		// click-able while showing the solution
 		for (int i = 0; i < level; i++) {
 			for (int j = 0; j < level; j++) {
 				imageArray[i][j].setOnClickListener(this);
 			}
 		}
 
 		if (lvl == 3) {
 			// rearrange the tiles for EASY
 			imageArray = switchTile(imageArray, 0, 0, 2, 1);
 			imageArray = switchTile(imageArray, 0, 1, 2, 0);
 			imageArray = switchTile(imageArray, 0, 2, 1, 2);
 			imageArray = switchTile(imageArray, 1, 0, 1, 1);
 
 		}
 
 		if (lvl == 4) {
 			// rearrange the tiles for MEDIUM
 			imageArray = switchTile(imageArray, 0, 0, 3, 1);
 			imageArray = switchTile(imageArray, 0, 1, 3, 2);
 			imageArray = switchTile(imageArray, 0, 2, 3, 0);
 			imageArray = switchTile(imageArray, 0, 3, 2, 3);
 			imageArray = switchTile(imageArray, 1, 0, 2, 2);
 			imageArray = switchTile(imageArray, 1, 1, 2, 1);
 			imageArray = switchTile(imageArray, 1, 2, 2, 0);
 			imageArray = switchTile(imageArray, 0, 0, 0, 1);
 		}
 
 		if (lvl == 5) {
 			// rearrange the tiles for HARD
 			imageArray = switchTile(imageArray, 0, 0, 4, 3);
 			imageArray = switchTile(imageArray, 0, 1, 4, 2);
 			imageArray = switchTile(imageArray, 0, 2, 4, 1);
 			imageArray = switchTile(imageArray, 0, 3, 4, 0);
 			imageArray = switchTile(imageArray, 0, 4, 3, 4);
 			imageArray = switchTile(imageArray, 1, 0, 3, 3);
 			imageArray = switchTile(imageArray, 1, 1, 3, 2);
 			imageArray = switchTile(imageArray, 1, 2, 3, 1);
 			imageArray = switchTile(imageArray, 1, 3, 3, 0);
 			imageArray = switchTile(imageArray, 1, 4, 2, 4);
 			imageArray = switchTile(imageArray, 2, 0, 2, 3);
 			imageArray = switchTile(imageArray, 2, 1, 2, 2);
 
 		}
 
 		return imageArray;
 	}
 
 	public ImageView[][] switchTile(ImageView[][] imageArray, int pos_x1,
 			int pos_y1, int pos_x2, int pos_y2) {
 
 		//set drawables that are for the two images that will be swapped
 		Drawable d1 = imageArray[pos_x1][pos_y1].getDrawable();
 		Drawable d2 = imageArray[pos_x2][pos_y2].getDrawable();
 		imageArray[pos_x1][pos_y1].setImageDrawable(d2);
 		imageArray[pos_x2][pos_y2].setImageDrawable(d1);
 
 		//switch the ID of each tile
 		int temp_ID = imageArray[pos_x1][pos_y1].getId();
 		imageArray[pos_x1][pos_y1].setId(imageArray[pos_x2][pos_y2].getId());
 		imageArray[pos_x2][pos_y2].setId(temp_ID);
 
 		return imageArray;
 	}
 
 	@Override
 	public void onClick(View v) {
 		
 		//increment the number of moves
 		moves++;
 		
 		// retrieve the coordinates of the blank square and the clicked square
 		int x = 0, y = 0, xblank = 0, yblank = 0;
 		for (int i = 0; i < level; i++) {
 			for (int j = 0; j < level; j++) {
 				if (image_array[i][j].getId() == v.getId()) {
 					x = i;
 					y = j;
 				}
 
 				if (image_array[i][j].getId() == level * level) {
 					xblank = i;
 					yblank = j;
 				} else {
 					// do nothing
 				}
 			}
 		}
 		// if the blank is 1 unit away in the y direction xor the x direction,
 		// then swap the tiles
 		if ((x == xblank && (y == yblank + 1 || y == yblank - 1))
 				|| (y == yblank && (x == xblank + 1 || x == xblank - 1))) {
 			image_array = switchTile(image_array, x, y, xblank, yblank);
 
 		}
 
 		// else call an illegal movement
 		else {
 			// do nothing
 			Toast.makeText(this, "That was an illegal move.", Toast.LENGTH_SHORT).show();
 		}
 
 		// see if solved puzzle then start a new activity if true
 		boolean isDone = checkSolution(image_array);
 
 		if (isDone == true) {
 			Toast.makeText(this, "Congrats!", Toast.LENGTH_LONG).show();
 			Intent intent = new Intent(this, YouWin.class);
 			startActivity(intent);
 			finish();
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		//inflate the menu
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	public void redrawBoard(int level) {
 		finish();
 		
 		//get resources to reset the board
 		Bundle extra = getIntent().getExtras();
 		SharedPreferences settings = getSharedPreferences(GAME_SETTINGS, 0);
 		SharedPreferences.Editor editor = settings.edit();
 
 		//keep track of the level and also reset the hasplayed to hasn't played
 		editor.putInt("lvl", level);
 		editor.putBoolean("hasPlayed", false);
 		editor.commit();
 		
 		//reset the moves
 		moves = 0;
 		
 		//access the same activity and send back the same picture
 		Intent i = new Intent(this, GamePlay.class);
 		i.putExtra("pictureRef", extra.getInt("pictureRef"));
 		startActivity(i);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection and declare shared preferences resources
 		Bundle extra = getIntent().getExtras();
 		SharedPreferences settings = getSharedPreferences(GAME_SETTINGS, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		switch (item.getItemId()) {
 		case R.id.easy:
 			// redraw board
 			redrawBoard(EASY);
 			return true;
 
 		case R.id.medium:
 			// redraw
 			redrawBoard(MEDIUM);
 			return true;
 
 		case R.id.hard:
 			// redraw board save
 			redrawBoard(HARD);
 			return true;
 
 		case R.id.quit:
 			finish();
 			
 			//reset the moves and the hasplayed
 			moves = 0;
 			editor.putBoolean("hasPlayed", false).commit();
			startActivity(new Intent(this, ImageSelection.class));
 			return true;
 
 		case R.id.shuffle:
 			finish();
 			//clear all the data so that you can shuffle
 			editor.clear(); 
 			editor.commit();
 			
 			//reset the moves and the hasplayed
 			moves = 0;
 			editor.putBoolean("hasPlayed", false);
 			editor.putInt("lvl", level);
 			editor.commit();
 			
 			Intent i = new Intent(this, GamePlay.class); //calls a new activity and fires the onPause event
 			i.putExtra("pictureRef", extra.getInt("pictureRef"));
 			startActivity(i);
 			return true;
 
 		default:
 			return true;
 		}
 	}
 
 	public boolean checkSolution(ImageView[][] image_array) {
 
 		int tileCounter = 1;
 		
 		//check to see if all the pieces are in the right place 
 		for (int i = 0; i < level; i++) {
 			for (int j = 0; j < level; j++) {
 				if (image_array[i][j].getId() != tileCounter) {
 					return false;
 				}
 
 				else {
 					// do nothing
 
 				}
 				tileCounter++;
 			}
 		}
 
 		return true;
 	}
 
 }
