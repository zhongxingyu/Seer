 package com.sinius15.suite.io;
 
 import java.awt.Dimension;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import javax.imageio.ImageIO;
 
 import com.sinius15.suite.Lib;
 import com.sinius15.suite.entitys.Entity;
 import com.sinius15.suite.game.Level;
 
 public class LevelSaver {
 	
 	/*
 		YOLO :  you only live once   just testing
 	*/
 
 	public static void saveLevel(Level l, File f) throws IOException{
 		if(!f.exists())
 			f.createNewFile();
 		PrintWriter writer = new PrintWriter(f);
 		
 		writer.println("#This is a save file for the game 'Suite'. If you want to open this file, i reccommand downloading the game for free at ...");
 		writer.println("");
 		writer.println("#general:");
 		writer.println("levelName: \"" + l.name + "\"");		//handled
 		writer.println("levelWidht: " + l.w);					//
 		writer.println("levelHeight: " + l.h);					//	
 		writer.println("XScroll: " + l.xScroll);				//
 		writer.println("YScroll: " + l.yScroll);				//
 		//writer.println("Background: " + l.background.);		//?			
 		writer.println("");
 		writer.println("#positions: ");
 		ArrayList<Entity> ents;
 		for(int x = 0 ; x < l.w ; x++){
 			for(int y = 0 ; y < l.h ; y++){
 				ents = new ArrayList<>();
 				for(Entity e : l.entities)
 					if(e.x == x && e.y == y)
 						ents.add(e);
 				if(l.getTile(x, y).id != 0 && ents.size() == 0)
 					continue;
 				String builder = "pos: x:" + x + "y:" + y +  "t:" + l.getTile(x,y).id  + "(" + l.getData(x, y) + ") ";
 				for(Entity e : ents)
 					builder = builder + "e:" + e.getClass().getName() + "(" + e.toSave() + ")";
 				writer.println(builder);
 			}
 		}
 		
 		writer.close();
 		
 		
 	}
 	
 	public Level loadLevel(File f) throws IOException{
 		ArrayList<String> in = new ArrayList<>();
 		Scanner scan = new Scanner(f);
 		String line;
 		while((line = scan.nextLine()) != null)
 			if(line.substring(0, 2) != null && !line.substring(0, 2).equals("#"))
 				in.add(line);
 		scan.close();
 		
 		int w = -1, h = -1;
 		String name = null;
 		for(String s : in){
 			if(s.startsWith("levelWidht: "))
 				w = Integer.parseInt(s.replaceAll("levelWidht: ", ""));
 			else if(s.startsWith("levelHeight: "))
 				h = Integer.parseInt(s.replaceAll("levelHeight: ", ""));
 			else if(s.startsWith("levelName: "))
 				name = s.replace("levelName: ", "").replaceAll("\"", "");
 		}
		Level l = new Level(new Dimension(w, h), name, null);
 		for(String s : in){
 			if(s.startsWith("XScroll: "))
 				l.xScroll = Integer.parseInt(s.replace("XScroll: ", ""));
 			if(s.startsWith("YScroll: "))
 				l.yScroll = Integer.parseInt(s.replace("YScoll: ", ""));
 			if(s.startsWith("Background: "))
 				l.background = ImageIO.read(new File(s.replace("Background: ", "")));
 		}
 		return l;
 	}
 	
 	/*	This is how the files are going to look:
 	 * 
 	 *  general:
 	 *  levelname
 	 * 	LevelWidht: 100
 	 *  LevelHeight: 100
 	 *  LevelDificulty: 0 
 	 *  XScroll: 20
 	 *  YScroll: 20
 	 *  Background: images/cookeis.jpg
 	 *  StaticBackground: ?
 	 *  
 	 *  blocks:
 	 *  x:12, y:13, t:track(arguments) e:cookiemonster(argumetns) e:cloudmonster(arguments) 
 	 *  x:16, y:24, t:track(arguments) e:cookiemonster(argumetns) e:cloudmonster(arguments) 
 	 *  */
 	
 	/*
 	 *  t = tile    e = entity
 	 *  if a coord=xyz  is not saved,  it has no entitys and the tyle = air)
 	 *  The arguments are provided by the getArguments() method. The name is found by the class name.
 	 *  This means that all the entitys need all to be in the same package!!!
 	 *  
 	 *  the arguments are passed in as strings or integers in the constructor of the tile/entity
 	 *  
 	 * */
 }
