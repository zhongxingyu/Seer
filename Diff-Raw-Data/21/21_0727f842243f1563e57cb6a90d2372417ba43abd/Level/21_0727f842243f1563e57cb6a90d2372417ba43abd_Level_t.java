 //This class will contain a number of enemy waves and a particular map
 
 package com.teamBasics.CollegeTD;
 
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import com.teamBasics.framework.Graphics;
 
 public class Level {
 	private ArrayList<EnemyWave> level = new ArrayList<EnemyWave>();;
 	private ArrayList<BorderTile> tilearrayBorder;
 	private ArrayList<PathTile> tilearrayPath;
 
 	//Later in the constructor this class can just be given the map number
 	//and it can generate the map itself. That way each level can have a
 	//different map
 	public Level(ArrayList<PathTile> tilearrayPath) {
 		//Instantiation of 1 wave of enemies
 		EnemyWave wave1 = new EnemyWave();
 		ArrayList<Enemy> group1 = new ArrayList<Enemy>();
 		ArrayList<Enemy> group2 = new ArrayList<Enemy>();
 		
 		Assignment b = new Assignment(tilearrayPath);
 		Assignment c = new Assignment(tilearrayPath);
 		Assignment d = new Assignment(tilearrayPath);
 		Assignment e = new Assignment(tilearrayPath);
		Assignment f = new Assignment(tilearrayPath);
 		group1.add(b);
 		group1.add(c);
		group1.add(d);
 		group2.add(e);
		group2.add(f);
 		
 		for(int i=0; i<10; i++) {
			Assignment a = new Assignment(tilearrayPath);
			group1.add(a);
 		}
 		for(int i=0; i<5; i++) {
			Assignment a = new Assignment(tilearrayPath);
			group2.add(a);
 		}
 		
 		if(group1 != null && group2 != null) {
 			wave1.addEnemyGroup(group1, Integer.valueOf(0), Integer.valueOf(8));
 			wave1.addEnemyGroup(group2, Integer.valueOf(200), Integer.valueOf(20));
 			level.add(wave1);
 		}	
 	}
 	
 	//Update 1 wave of the level at a time. Update the first wave you
 	//come to that is not complete.
 	public void update() {
 		EnemyWave waveToUpdate = null;
 		for(int i=0; i<level.size(); i++) {
 			if(!(level.get(i).isComplete())) {
 				waveToUpdate = level.get(i);
				break;
 			}
 		}
 		if(waveToUpdate != null) {
 			waveToUpdate.update();
 		}
 		else {
 			//give user some kind of screen that they won the level
 		}
 	}
 	
 	public void draw(Graphics g) {
 		for(int i=0; i<level.size(); i++) {
 			level.get(i).draw(g);
 		}	
 	}
 
 
 	//Will be used later on to load the map for each level
 	/*
 	// Load map border
 	private void loadMapBorder() {
 		ArrayList<String> lines = new ArrayList<String>();
 		int width = 0;
 		int height = 0;
 
 		Scanner scanner = new Scanner(SampleGame.mapBorder);
 		while (scanner.hasNextLine()) {
 			String line = scanner.nextLine();
 
 			// no more lines to read
 			if (line == null) {
 				break;
 			}
 
 			if (!line.startsWith("!")) {
 				lines.add(line);
 				width = Math.max(width, line.length());
 			}
 		}
 		height = lines.size();
 
 		// j = y value
 		for (int j = 0; j < height; j++) {
 			String line = (String) lines.get(j);
 			for (int i = 0; i < width; i++) {
 				if (i < line.length()) {
 					char ch = line.charAt(i);
 					BorderTile t = new BorderTile(i, j, Character.getNumericValue(ch));
 					tilearrayBorder.add(t);
 				}
 			}
 		}
 	}
 
 
 	// Load walkway-path
 	private void loadMapPath() {
 		ArrayList<String> lines = new ArrayList<String>();
 		int width = 0;
 		int height = 0;
 
 		Scanner scanner = new Scanner(SampleGame.mapPath);
 		while (scanner.hasNextLine()) {
 			String line = scanner.nextLine();
 			
 			// no more lines to read
 			if (line == null) {
 				break;
 			}
 
 			if (!line.startsWith("!")) {
 				lines.add(line);
 				width = Math.max(width, line.length());
 			}
 		}
 		height = lines.size();
 
 		// j = y value
 		for (int j = 0; j < height; j++) {
 			String line = (String) lines.get(j);
 			for (int i = 0; i < width; i++) {
 
 				if (i < line.length()) {
 					char ch = line.charAt(i);
 					PathTile t = new PathTile(i, j, ch);
 					tilearrayPath.add(t);
 				}
 			}
 		}
 
 	}
 	*/	
 }
