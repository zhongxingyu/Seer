 package com.grundyboy34.utils;
 
 import java.io.File;
 import java.io.IOException;
 import com.grundyboy34.SideScroller;
 import com.grundyboy34.io.LevelXMLHandler;
 import com.grundyboy34.level.Level;
 
 public class LevelDataLoader {
 
 	private static void loadLevel(File file) {
 
 		Level level = null;
 		String filePath = file.getAbsolutePath();
 		String levelResourcePath = file.getParentFile().getAbsolutePath();
 		try {
 			level = LevelXMLHandler.fromXML(filePath);
			System.out.println(levelResourcePath);
 			level.setResourceManager(new ResourceManager(levelResourcePath));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		if (level != null) {
 			SideScroller.getWorld().addLevel(level);
 		}
 	}
 
 	public static void loadLevels() {
 		File lvlDir = new File(SideScroller.getLevelsDirectory());
 
 		for (File lvl : lvlDir.listFiles()) {
 			for (File lvlFile : lvl.listFiles()) {
 
 				if (lvlFile.isFile() && lvlFile.getName().equals("level.xml")) {
 					loadLevel(lvlFile);
 				}
 			}
 		}
 
 	}
 
 }
