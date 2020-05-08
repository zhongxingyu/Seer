 /*******************************************************************************
  * Copyright (c) 2012 GamezGalaxy.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 package com.gamezgalaxy.GGS.world;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import com.gamezgalaxy.GGS.iomodel.Player;
 import com.gamezgalaxy.GGS.server.Server;
 import com.gamezgalaxy.GGS.server.Tick;
 
 public class LevelHandler {
 	// TODO: System of detected unloaded. Someone else do that please.
 
 	public List<Level> levels = new CopyOnWriteArrayList<Level>();
 	
 	private Server server;
 	
 	public LevelHandler(Server server) {
 		this.server = server;
 		server.Add(new Saver());
 	}
 
 	public void newLevel(String name, short width, short height, short length)
 	{
 		if(!new File("levels/" + name).exists())
 		{
 			Level level = new Level(width, height, length);
 
 			level.name = name;
 			level.FlatGrass();
 			try {
 				level.Save();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			levels.add(level);
 		}
 	}
 	
 	public Level findLevel(String name) {
 		Level temp = null;
 		for (int i = 0; i < levels.size(); i++) {
 			if ((levels.get(i).name).equalsIgnoreCase(name))
 				return levels.get(i);
 			if ((levels.get(i).name).contains(name) && temp == null)
 				temp = levels.get(i);
 			else if ((levels.get(i).name).contains(name) && temp != null)
 				return null;
 		}
 		return temp;
 	}
 	
 	public ArrayList<Player> getPlayers(Level level) {
 		ArrayList<Player> temp = new ArrayList<Player>();
 		for (int i = 0; i < server.players.size(); i++)
 			if (server.players.get(i).getLevel() == level)
 				temp.add(server.players.get(i));
 		return temp;
 	}
 
 	public void loadLevels()
 	{
 		levels.clear();
 		File levelsFolder = new File("levels");
 		File[] levelFiles = levelsFolder.listFiles();
 		for(File f : levelFiles)
 			loadLevel(levelsFolder.getPath() + "/" + f.getName());
 	}
 	
 	public Level loadLevel(String filename) {
 		Level l = null;
 		try {
 			l = Level.Load(filename);
 		} catch (ClassNotFoundException e) {
 			server.Log("ERROR LOADING LEVEL!");
 			e.printStackTrace();
 		} catch (IOException e) {
 			server.Log("ERROR LOADING LEVEL!");
 			e.printStackTrace();
 		}
 		if (l != null)
 			levels.add(l);
 		return l;
 	}
 	public void unloadLevel(Level level) {
		unloadLevel(level, true);
	}
	public void unloadLevel(Level level, boolean save) {
 		if (!levels.contains(level))
 			return;
 		try {
			level.Unload(server, save);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		levels.remove(level);
 	}
 	
 	private class Saver implements Tick {
 		int temp = 6000;
 		@Override
 		public void Tick() {
 			if (temp > 0) {
 				temp--;
 				return;
 			}
 			temp = 6000;
 			for (int i = 0; i < levels.size(); i++) {
 				try {
 					levels.get(i).Save();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 }
