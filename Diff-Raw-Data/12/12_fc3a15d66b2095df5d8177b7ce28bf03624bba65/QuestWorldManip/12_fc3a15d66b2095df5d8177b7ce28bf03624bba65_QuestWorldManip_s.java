 /**
  * This file, QuestWorldManip.java, is part of MineQuest:
  * A full featured and customizable quest/mission system.
  * Copyright (C) 2012 The MineQuest Team
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  **/
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
		Bukkit.unloadWorld(w, false);
		FileUtils.deleteDirectory(new File(removename));
 	}
 	
 }
