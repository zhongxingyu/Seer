 /*
  * This file is part of MineQuest, The ultimate MMORPG plugin!.
  * MineQuest is licensed under GNU General Public License v3.
  * Copyright (C) 2012 The MineQuest Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.theminequest.MineQuest.Quest;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Random;
 
 import org.apache.commons.io.FileUtils;
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 
 public class QuestWorldManip {
 
 	private static Random randGen = new Random();
 	
 	public static World copyWorld(World w) throws IOException{
 		String newname;
 		File newdirectory;
 		do {
 			newname = "mqinstance_"+randGen.nextLong();
 			newdirectory = new File(newname);
 		}while (newdirectory.exists());
 		FileUtils.copyDirectory(w.getWorldFolder(), newdirectory);
 
 		File uid = new File(newdirectory + File.separator + "uid.dat");
 		if (uid.exists()) uid.delete();
 		
 		WorldCreator tmp = new WorldCreator(newname);
 		tmp.copy(w);
 		return Bukkit.createWorld(tmp);
 	}
 	
 	public static void removeWorld(World w) throws IOException{
 		String removename = w.getName();
 		if (Bukkit.unloadWorld(w, false))
			FileUtils.deleteDirectory(new File(removename));
 	}
 	
 }
