 package com.dat255_group3.io;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.utils.GdxRuntimeException;
 
 public class IOHandler {
 
	private static FileHandle handle = Gdx.files.external("io/io.txt");
 	private static String [] levelData;
 
 
 	public IOHandler() {
 	}
 
 	public static void saveScore(int levelNr, int score){
 		try{
 			handle.writeString("level" + levelNr + ":" + score + ":", true);
 			Gdx.app.log("IOHandler", "Name: " + handle.name());
 			readScore();
 			Gdx.app.log("IOHandler", "Level: " + levelNr);
 			Gdx.app.log("IOHandler", "Read higScore: " + getScore(levelNr));
 			Gdx.app.log("IOHandler", "LevelData: " + levelData[0]);
 		} catch (GdxRuntimeException e){
 			Gdx.app.log("IOHandler", "Exception", e);
 		}catch (Exception e) {			
 		}
 	
 	}
 
 	public static void saveNewHigscore(int levelNr, int score) {
 		for(int i = 0; i<levelData.length; i=i+2) { //only every second string will contain a level name therefore increase i by two
 			if (levelData[i].contains("level" + levelNr)) {
 				levelData[i]= "level" + levelNr + ":";
 				Gdx.app.log("IOHandler", "levelData level:" + levelData[i]);		
 				levelData[i+1] = score + ":";
 				Gdx.app.log("IOHandler", "levelData score:" + levelData[i+1]);	
 				Gdx.app.log("IOHandler", "New HIgh score");				
 				
 			}
 		}
 		String text = "";
 		for (int i=0; i<levelData.length; i++) {
 			text = text + levelData[i];
 			
 		}
 		Gdx.app.log("IOHandler", "levelData text:" + text);	
 		Gdx.app.log("IOHandler", "Level: " + levelNr);
 		try{
 			handle.writeString(text, false);
 			readScore();
 			Gdx.app.log("IOHandler", "Read new higScore: " + getScore(levelNr));
 		} catch (GdxRuntimeException e){
 
 		}
 	}
 
 	public static void readScore(){
 //		try {
 //	        InputStream in = (InputStream) Gdx.files.internal("data/cube.obj").read();
 //	        try {
 //	          in.close();
 //	        } catch (IOException e) {
 //	        }
 		try{
 			String text = handle.readString();
 			levelData = text.split(":");		
 		} catch (GdxRuntimeException e){
 			Gdx.app.log("IOHandler", "Exception", e);
 		}
 	}
 
 	public static int getScore(int levelNr) {
 		try {
 			for(int i = 0; i<levelData.length; i=i+2) { //only every second string will contain a level name therefore increase i by two
 				if (levelData[i].contains("level" + levelNr)) {
 					return Integer.parseInt(levelData[i+1].trim());
 				}
 			}
 		}
 		catch (Exception e) {
 			// No old high score
 		}
 		return 0;
 	}
 	
 	public static boolean contains(String level) {
 		boolean foundMatch = false;
 		if (levelData != null) {
 			for (int i=0; i<levelData.length; i++) {
 				if (levelData[i].contains(level)) {
 					foundMatch = true;
 				}
 			}
 		}
 		
 		return foundMatch;
 	}
 	
 	public static String[] getLevelData(){
 		return levelData;
 	}
 }
