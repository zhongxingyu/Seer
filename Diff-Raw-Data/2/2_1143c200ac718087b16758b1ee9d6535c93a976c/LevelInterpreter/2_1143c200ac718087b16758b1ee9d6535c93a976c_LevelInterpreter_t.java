 package com.musicgame.PumpAndJump.Util;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Files;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.assets.loaders.*;
 import com.musicgame.PumpAndJump.Beat;
 import com.musicgame.PumpAndJump.DuckObstacle;
 import com.musicgame.PumpAndJump.GameObject;
 import com.musicgame.PumpAndJump.JumpObstacle;
 
 public class LevelInterpreter
 {
 	/**
 	 * Returns the GameObject specified in inputLine
 	 * If inputLine is an invalid format then returns null
 	 * If inputLine is null throws runtimeException
 	 * @param inputLine
 	 * @return
 	 */
 	public static GameObject getNextObjectPattern(String inputLine){
 		String jumpPattern="j \\d+(\\.\\d+)?\\s*";
 		String slidePattern="d \\d+(\\.\\d+)? \\d+(\\.\\d+)?\\s*";
 		if(inputLine.matches(jumpPattern)){
 			String[] input=inputLine.split(" ");
 			double startTime=Double.parseDouble(input[1]);
 			return new JumpObstacle((float)startTime,0);
 		}
 		if(inputLine.matches(slidePattern)){
 			String[] input=inputLine.split(" ");
 			double startTime=Double.parseDouble(input[1]);
 			double endTime=Double.parseDouble(input[2]);
 			return new DuckObstacle((float)startTime,(float)endTime);
 		}
 		System.out.println(inputLine);
 		return null;
 	}
 
 	public static GameObject getNextObject(String inputLine)
 	{
 		Scanner s = new Scanner(inputLine);
 		if(!s.hasNext())
 			return null;
 		String type = s.next();
 		if(!s.hasNextDouble())
 			return null;
 		double start = s.nextDouble();
 		if( type.equals( "b" ) )
 			return new Beat( (float)start );
 		if(!s.hasNextDouble())
 			return null;
 		double end = s.nextDouble();
 		GameObject obj;
 		if(type.equalsIgnoreCase("j"))
 		{
 			return new JumpObstacle((float)start,(float)end);
 		}else if(type.equalsIgnoreCase("d"))
 		{
 			return new DuckObstacle((float)start,(float)end);
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the list of the GameObjects that were loaded from the level
 	 * Will return an empty array list if no file is found.
 	 * If the file Level1.txt doesn't exist returns null
 	 * @return
 	 * @throws FileNotFoundException
 	 */
 	public static ArrayList<GameObject> loadLevel() throws FileNotFoundException
 	{
 		//Gdx.files.internal("Something in assets");
 		ArrayList<GameObject> Level;
		FileHandle dir= Gdx.files.internal("level1.txt");
 
 		if(!dir.exists())
 		{
 			return null;
 		}
 
 		Scanner LevelIn = new Scanner(dir.reader());
 		Level=loadFromScanner(LevelIn);
 		LevelIn.close();
 		return Level;
 	}
 	/**
 	 * Returns the array for the given scanner
 	 * Will throw runtime error if scan is null
 	 * @return
 	 */
 	public static ArrayList<GameObject> loadFromScanner(Scanner scan){
 		ArrayList<GameObject> Level=new ArrayList<GameObject>();
 		while(scan.hasNextLine()){
 			GameObject obstacle=getNextObject(scan.nextLine());
 
 			if(obstacle!=null)
 				Level.add(obstacle);
 		}
 		return Level;
 	}
 }
