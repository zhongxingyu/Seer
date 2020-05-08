 package com.teamBasics.CollegeTD;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.Log;
 
 import com.teamBasics.framework.Game;
 import com.teamBasics.framework.Graphics;
 import com.teamBasics.framework.Image;
 import com.teamBasics.framework.Input.TouchEvent;
 import com.teamBasics.framework.Screen;
 
 public class LevelSelectScreen extends Screen {
 
 	// Variable Setup
 	Paint paint, paint2;
 	public static String mapPath;
 	public int row1, row2;
 	//row1 = 200;
 	//row2 = 325;
 
 	public LevelSelectScreen(Game game) {
 		super(game);
 		
 		// Defining a paint object
 		paint = new Paint();
 		paint.setTextSize(25);
 		paint.setTextAlign(Paint.Align.CENTER);
 		paint.setAntiAlias(true);
 		paint.setColor(Color.GRAY);
 
 		// Back button (options menu)
 		paint2 = new Paint();
 		paint2.setTextSize(100);
 		paint2.setTextAlign(Paint.Align.CENTER);
 		paint2.setAntiAlias(true);
 		paint2.setColor(Color.LTGRAY);
 
 	}
 
 	public static String getMapPath() {
 		return mapPath;
 	}
 
 	public static void setMapPath(String mapPath) {
 		LevelSelectScreen.mapPath = mapPath;
 	}
 
 	@Override
 	public void update(float deltaTime) {
 		Graphics g = game.getGraphics();
 		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
 
 		int len = touchEvents.size();
 		for (int i = 0; i < len; i++) {
 			TouchEvent event = touchEvents.get(i);
 			if (event.type == TouchEvent.TOUCH_UP) {
 
 				// Level 1 Select
 				if (inBounds(event, 50-12, 200, 100, 100)) {
 					loadMapPath(1);
 					game.setScreen(new GameScreen(game, 1));
 				}
 				
 				// Level 2 Select
 				else if (inBounds(event, 175-12, 200, 100, 100)) {
 					loadMapPath(2);
 					game.setScreen(new GameScreen(game, 2));
 				}				
 
 				// Level 3 Select
 				else if (inBounds(event, 300-12, 200, 100, 100)) {
 					loadMapPath(3);
 					game.setScreen(new GameScreen(game, 3));
 				}
 				
 				// Level 4 Select
 				else if (inBounds(event, 425-12, 200, 100, 100)) {
 					loadMapPath(4);
 					game.setScreen(new GameScreen(game, 4));
 				}	
 				
 				// Level 5 Select
 				else if (inBounds(event, 550-12, 200, 100, 100)) {
 					loadMapPath(5);
 					game.setScreen(new GameScreen(game, 5));
 				}
 				
 				// Level 6 Select
				else if (inBounds(event, 675-12, 200, 100, 100)) {
 					loadMapPath(6);
 					game.setScreen(new GameScreen(game, 6));
 				}	
 				
 				// Level 7 Select
 				else if (inBounds(event, 50-12, 325, 100, 100)) {
 					loadMapPath(7);
 					game.setScreen(new GameScreen(game, 7));
 				}
 				
 				// Level 8 Select
 				else if (inBounds(event, 175-12, 325, 100, 100)) {
 					loadMapPath(8);
 					game.setScreen(new GameScreen(game, 8));
 				}
 				
 				// Level 9 Select
 				else if (inBounds(event, 300-12, 325, 100, 100)) {
 					loadMapPath(9);
 					game.setScreen(new GameScreen(game, 9));
 				}
 				
 				// Level 10 Select
 				else if (inBounds(event, 425-12, 325, 100, 100)) {
 					loadMapPath(10);
 					game.setScreen(new GameScreen(game, 10));
 				}					
 				
 				// Level 11 Select
 				else if (inBounds(event, 550-12, 325, 100, 100)) {
 					loadMapPath(11);
 					game.setScreen(new GameScreen(game, 11));
 				}
 				
 				// Level 12 Select
 				else if (inBounds(event, 675-12, 325, 100, 100)) {
					loadMapPath(12);
					game.setScreen(new GameScreen(game, 12));
 				}
 				
 			}
 		}
 	}
 
 	public static void loadMapPath(int currentL){
 		mapPath = convertStreamToString(SampleGame.getInputSP(currentL));
 	}
 	
 	private static String convertStreamToString(InputStream is) {
 
 		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 		StringBuilder sb = new StringBuilder();
 
 		String line = null;
 		try {
 			while ((line = reader.readLine()) != null) {
 				sb.append((line + "\n"));
 			}
 		} catch (IOException e) {
 			Log.w("LOG", e.getMessage());
 		} finally {
 			try {
 				is.close();
 			} catch (IOException e) {
 				Log.w("LOG", e.getMessage());
 			}
 		}
 		return sb.toString();
 	}
 	
 	private boolean inBounds(TouchEvent event, int x, int y, int width, int height) {
 		if (event.x > x && event.x < x + width - 1 && event.y > y
 				&& event.y < y + height - 1)
 			return true;
 		else
 			return false;
 	}
 
 	@Override
 	public void paint(float deltaTime) {
 		Graphics g = game.getGraphics();
 		row1 = 200;
 		row2 = 325;
 		g.drawImage(Assets.levelbackground, 0, 0);
 		
 		// Row 1
 		// Level 1
 		g.drawImage(Assets.levelSelect_box, 50-12, row1);
 		g.drawImage(Assets.level1, 55-12, 220);
 		
 		// Level 2
 		g.drawImage(Assets.levelSelect_box, 175-12, row1);
 		g.drawImage(Assets.level2, 180-12, 220);
 		
 		// Level 3
 		g.drawImage(Assets.levelSelect_box, 300-12, row1);
 		g.drawImage(Assets.level3, 305-12, 220);
 			
 		// Level 4
 		g.drawImage(Assets.levelSelect_box, 425-12, row1);
 		g.drawImage(Assets.level4, 430-12, 220);
 				
 		// Level 5
 		g.drawImage(Assets.levelSelect_box, 550-12, row1);
 		g.drawImage(Assets.level5, 555-12, 220);
 				
 		// Level 6
 		g.drawImage(Assets.levelSelect_box, 675-12, row1);
 		g.drawImage(Assets.level6, 680-12, 220);
 
 		// Row 2
 		// Level 7
 		g.drawImage(Assets.levelSelect_box, 50-12, row2);
 		g.drawImage(Assets.level7, 55-12, 345);
 		
 		// Level 8
 		g.drawImage(Assets.levelSelect_box, 175-12, row2);
 		g.drawImage(Assets.level8, 180-12, 345);
 		
 		// Level 9
 		g.drawImage(Assets.levelSelect_box, 300-12, row2);
 		g.drawImage(Assets.level9, 305-12, 345);
 		
 		// Level 10
 		g.drawImage(Assets.levelSelect_box, 425-12, row2);
 		g.drawImage(Assets.level10, 430-12, 345);
 		
 		// Level 11
 		g.drawImage(Assets.levelSelect_box, 550-12, row2);
 		g.drawImage(Assets.level11, 555-12, 345);
 			
 		// Level 12
 		g.drawImage(Assets.levelSelect_box, 675-12, row2);
		g.drawImage(Assets.level12, 680-12, 345);
 		
 		// Text
 		g.drawString("Level 1", 100-12, 290, paint);
 		g.drawString("Level 2", 225-12, 290, paint);
 		g.drawString("Level 3", 350-12, 290, paint);
 		g.drawString("Level 4", 475-12, 290, paint);
 		g.drawString("Level 5", 600-12, 290, paint);
 		g.drawString("Level 6", 725-12, 290, paint);
 		
 		g.drawString("Level 7", 100-12, 415, paint);
 		g.drawString("Level 8", 225-12, 415, paint);
 		g.drawString("Level 9", 350-12, 415, paint);
 		g.drawString("Level 10", 475-12, 415, paint);
 		g.drawString("Level 11", 600-12, 415, paint);
 		g.drawString("Level 12", 725-12, 415, paint);
 	}
 
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void backButton() {
 		game.setScreen(new MainMenuScreen(game));
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 	}
 
 }
