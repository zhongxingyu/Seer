 package com.musicgame.PumpAndJump;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Files;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.assets.loaders.*;
 
 public class LevelInterpreter
 {
 	/**
 	 * Returns the GameObject specified in inputLine
 	 * If inputLine is an invalid format then returns null
 	 * If inputLine is null throws runtimeException
 	 * @param inputLine
 	 * @return
 	 */
 	public static GameObject getNextObject(String inputLine){
 		String jumpPattern="j \\d+(\\.\\d+)?\\s*";
 		String slidePattern="s \\d+(\\.\\d+)? \\d+(\\.\\d+)?\\s*";
 		if(inputLine.matches(jumpPattern)){
 			String[] input=inputLine.split(" ");
 			double startTime=Double.parseDouble(input[1]);
 			return new JumpObstacle(startTime);
 		}
 		if(inputLine.matches(slidePattern)){
 			String[] input=inputLine.split(" ");
 			double startTime=Double.parseDouble(input[1]);
 			double endTime=Double.parseDouble(input[2]);
 			return new DuckObstacle(startTime,endTime);
 		}
 		return null;
 	}
 	/**
 	 * Returns the list of the GameObjects that were loaded from the level
 	 * Will return an empty array list if no file is found.
 	 * Precondition: The first txt file in the resources folder is the level data
 	 * @return
 	 * @throws FileNotFoundException
 	 */
 	public static ArrayList<GameObject> loadLevel() throws FileNotFoundException
 	{
 		//Gdx.files.internal("Something in assets");
 		ArrayList<GameObject> Level=new ArrayList <GameObject>();
		FileHandle dir= Gdx.files.internal("Skrillex_Cinema.wav");
 		Scanner LevelIn = new Scanner(dir.reader());
 		while(LevelIn.hasNextLine()){
 			GameObject obstacle=getNextObject(LevelIn.nextLine());
 			if(obstacle!=null)
 				Level.add(obstacle);
 		}
 		LevelIn.close();
 		return Level;
 	}
 }
