 package com.teamBasics.CollegeTD;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import android.util.Log;
 
 import com.teamBasics.CollegeTD.R;
 import com.teamBasics.CollegeTD.GameScreen;
 import com.teamBasics.framework.Screen;
 import com.teamBasics.framework.implementation.AndroidGame;
 import com.teamBasics.CollegeTD.SaveState;
 
 public class SampleGame extends AndroidGame {
 
 	public static String mapPath;
 	public ArrayList<InputStream> inputSB;
 	public static ArrayList<InputStream> inputSP; 
 	boolean firstTimeCreate = true;
 	private static int currentLevel;
 	
 
 	public int getCurrentLevel() {
 		return currentLevel;
 	}
 
 	public static void setCurrentLevel(int currentLevelNew) {
 		currentLevel = currentLevelNew;
 	}
 
 	@Override
 	public Screen getInitScreen() {
 
 		if (firstTimeCreate) {
 			Assets.load(this);
 			SaveState.load(getFileIO());
 			//currentLevel=SaveState.currentLevel;
 			firstTimeCreate = false;
 		}
 
 		//inputSB = new ArrayList<InputStream>(20);
 		inputSP = new ArrayList<InputStream>(12);
 		
 		// Need to set new level number when user has defeated all enemies on selected map.
		currentLevel = 11; 
 		
 		// Adding Path Map Tile Conversions
 		inputSP.add(0, getResources().openRawResource(R.raw.pathmap1));
 		inputSP.add(1, getResources().openRawResource(R.raw.pathmap2));
 		inputSP.add(2, getResources().openRawResource(R.raw.pathmap3));
 		inputSP.add(3, getResources().openRawResource(R.raw.pathmap4));
 		inputSP.add(4, getResources().openRawResource(R.raw.pathmap5));
 		inputSP.add(5, getResources().openRawResource(R.raw.pathmap6));
 		inputSP.add(6, getResources().openRawResource(R.raw.pathmap7));
 		inputSP.add(7, getResources().openRawResource(R.raw.pathmap8));
 		inputSP.add(8, getResources().openRawResource(R.raw.pathmap9));
 		inputSP.add(9, getResources().openRawResource(R.raw.pathmap10));
 		inputSP.add(10, getResources().openRawResource(R.raw.pathmap11));
 		//inputSP.add(11, getResources().openRawResource(R.raw.pathmap12));
 
 		
 		// Border and Path Tile Selection based on Level
 		//mapBorder = convertStreamToString(inputSB.get(currentLevel-1));
 		loadMapPath(currentLevel);		
 
 		return new SplashLoadingScreen(this);
 	}
 
 	public static void loadMapPath(int currentL){
 		mapPath = convertStreamToString(inputSP.get(currentL-1));
 	}
 	
 	@Override
 	public void onBackPressed() {
 		getCurrentScreen().backButton();
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
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		Assets.theme.play();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		Assets.theme.pause();
 	}
 	
 }
