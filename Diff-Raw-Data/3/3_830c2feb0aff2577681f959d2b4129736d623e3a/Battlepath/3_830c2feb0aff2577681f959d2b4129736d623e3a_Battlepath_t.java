 /**
  * Copyright (c) 2011-2012 Henning Funke.
  * 
  * This file is part of Battlepath.
  *
  * Battlepath is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * Battlepath is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package main;
 import game.Game;
 import game.Team;
 import game.View;
 import interaction.Input;
 import interaction.OpenGLRenderer;
 import interaction.BFrame;
 
 import java.awt.Dimension;
 import java.awt.Point;
import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import collision.CollisionSystem;
 
 import util.Vector2D;
 
 import engine.Field;
 import engine.MainLoop;
import engine.MapCodec;
 import engine.Pathplanner;
 import engine.Tile;
 import entities.Entity;
 import entities.Tower;
 
 /**
  * Main class of the Battlepath game.
  * @author Battlepath team
  * @version 0
  */
 public class Battlepath {
 	static Random rand = new Random();
 	
 	/**
 	 * Main method
 	 * @param args Command line arguments
 	 */
 	public static void main(String[] args) {
 		
 		int tileSize = 20;
 		int fieldWidth = 100;
 		int fieldHeight = 100;
 		Dimension windowSize = new Dimension(1000,800);
 		Field f = new Field(fieldWidth, fieldHeight, "random");
 		randomCircles(f, fieldWidth*fieldHeight/50, 3);
 		
 		//f.setTile(new Vector2D(40.5,40.5), 4);
 		//f.setTile(new Vector2D(40.5,41.5), 4);
 		
 		Vector2D start = findStartPos(f);
 		
 		/*byte[] fi = null;
 		try {
 			fi = Util.readFile("testmap");
 		} catch (IOException e) {
 			System.out.println("I/O error.");
 		}
 		f = MapCodec.decode(fi);
 		*/
 		/*byte[] m = MapCodec.encode(f);
 		
 		try {
 			Util.writeFile("testmap", m);
 		} catch (IOException e) {
 			System.out.println("I/O error.");
 		}*/
 		
 		ArrayList<Team> teams = new ArrayList<Team>();
 		teams.add(new Team("SWAR", 0));
 		teams.add(new Team("sWARm", 1));
 		
 		Game game = new Game(start, teams, 0);
 		
 		BFrame frame = new BFrame(windowSize);
 		Dimension paneSize = frame.getContentPane().getSize();
 		
 		OpenGLRenderer renderer = new OpenGLRenderer(game,tileSize,frame);
 		game.field = f;
 		game.input = new Input(frame, game);
 		game.pathPlanner =  new Pathplanner(f);
 		game.collisionSystem = new CollisionSystem(f,game);
 		//game.entitySystem.addAll(randomTowers(f, 30, game,teams));
 		game.setView(new View(paneSize, tileSize, game));
 		
 		game.initialize();
 		
 		MainLoop.startLoop(game, renderer, frame);
 	}
 	/**
 	 * Finds valid start position for the Unit
 	 * @param f Used field
 	 * @return Valid position
 	 */
 	public static Vector2D findStartPos(Field f) {
 		Point start = new Point(rand.nextInt(f.getTilesX()), rand.nextInt(f.getTilesY()));
 		Tile[][] tiles = f.getTiles();
 		while(tiles[start.x][start.y].getValue() == 1)
 			start = new Point(rand.nextInt(f.getTilesX()), rand.nextInt(f.getTilesY()));
 		return f.getWorldPos(start);
 	}
 	
 	/**
 	 * Creates random circles on a map. Currently the only map generation algorithm.
 	 * @param f Used field
 	 * @param n Number of circles
 	 * @param maxr Maximal radius of circles
 	 */
 	public static void randomCircles(Field f, int n, double maxr) {
 		for(int i=0; i<n; i++) {
 			f.createCircle(new Vector2D(rand.nextDouble()*f.getTilesX(), rand.nextDouble()*f.getTilesY()), rand.nextDouble()*maxr);
 		}
 	}
 	
 	/**
 	 * Generates randomly spread towers on a map
 	 * @param f Used field
 	 * @param n Number of towers
 	 * @param g Used game
 	 * @return List of towers
 	 */ 	
 	public static ArrayList<Entity> randomTowers(Field f, int n, Game g, ArrayList<Team> teams) {
 		ArrayList<Entity> list = new ArrayList<Entity>();
 		Tile[][] tiles = f.getTiles();
 		for(int i=0; i<n;i++) {
 			Point tower = new Point(rand.nextInt(f.getTilesX()), rand.nextInt(f.getTilesY()));
 			while(tiles[tower.x][tower.y].getValue() == 1)
 				tower = new Point(rand.nextInt(f.getTilesX()), rand.nextInt(f.getTilesY()));
 			
 			int team = rand.nextInt(teams.size());
 			list.add(new Tower(f.getWorldPos(tower), g, teams.get(team)));
 		}
 		return list;
 	}
 }
