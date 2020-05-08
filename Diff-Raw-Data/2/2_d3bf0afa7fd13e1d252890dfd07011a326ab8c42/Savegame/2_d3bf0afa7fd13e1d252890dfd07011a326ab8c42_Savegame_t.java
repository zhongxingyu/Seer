 package main;
 
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.HashMap;
 
 import org.newdawn.slick.util.Log;
 
 /**
  * Savegame class , specially made for IGM2E . 
  * Replaces Slick2D's savegames because they use Slick2D's state method , 
  * this class just holds fields as cache to save them or load them later on . 
  * 
  * Examples : 
  * - Instances can hold player position , player progress or other stuff 
  * - Instances can be saved and be loaded later on when in pause menu
  *	
  */
 public class Savegame implements Serializable {
 
 	public static String name = "SAVEGAME";
 	public String song = "";
 	public float songpos = 0f;
 	public HUD tmphud;
 	
 	public Savegame() {
 	}
 
 	public void saveLevel(Level level) {
 		try {
 			saveLevel(level, name);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public Level loadLevel() {
 		try {
 			return loadLevel(name);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public void saveLevel(Level level, String fileName) throws IOException {
 		level.prepareSave();
 		level.uninit();
 		
 		File file = Options.getSavesDir();
 		file = new File(file, fileName+".lvl");
 		if (file.exists()) file.delete();
 		if (!file.exists()) file.createNewFile();
 		
 		FileOutputStream fos = new FileOutputStream(file);
 		
 		java.security.AccessController.doPrivileged(new SetSystemPropAction("sun.io.serialization.extendedDebugInfo", "true"));
 		
 		ObjectOutputStream oos = new ObjectOutputStream(fos);
 		
 		oos.writeObject(level);
 		
 		oos.close();
 		
 		java.security.AccessController.doPrivileged(new SetSystemPropAction("sun.io.serialization.extendedDebugInfo", "false"));
 		
 		level.reinit();
 		level.unprepareSave();
 	}
 	
 	public Level loadLevel(String fileName) throws IOException {
 		Level level = new Level();
 		
 		File file = Options.getSavesDir();
 		file = new File(file, fileName+".lvl");
 		
 		if (file.exists()) {
 			try {
 				FileInputStream fis = new FileInputStream(file);
 				ObjectInputStream ois = new ObjectInputStream(fis);
 				
 				level = (Level) ois.readObject();
 				
 				ois.close();
 				
 			} catch (EOFException e) {
 				// End of the file reached, do nothing
 			} catch (ClassNotFoundException e) {
 				throw new IOException("Failed to pull state from store - class not found");
 			}
 		}
 		
 		level.reinit();
 		level.unprepareSave();
 		
 		return level;
 	}
 
 	public void saveGame(String fileName) throws IOException {
		if (IGM2E.bgm != null) {
 			songpos = IGM2E.bgm.getPosition();
 		} else {
 			songpos = 0;
 		}
 		tmphud = IGM2E.hud;
 		
 		File file = Options.getSavesDir();
 		file = new File(file, fileName+".sav");
 		if (file.exists()) file.delete();
 		if (!file.exists()) file.createNewFile();
 		
 		FileOutputStream fos = new FileOutputStream(file);
 		ObjectOutputStream oos = new ObjectOutputStream(fos);
 		
 		// save hashMap
 		oos.writeObject(this);
 		
 		oos.close();
 	}
 
 	public static Savegame loadGame(String fileName) throws IOException {
 		Savegame savegame = new Savegame();
 		
 		File file = Options.getSavesDir();
 		file = new File(file, fileName+".sav");
 		
 		if (file.exists()) {
 			try {
 				FileInputStream fis = new FileInputStream(file);
 				ObjectInputStream ois = new ObjectInputStream(fis);
 				
 				savegame = (Savegame) ois.readObject();
 				
 				ois.close();
 				
 			} catch (EOFException e) {
 				// End of the file reached, do nothing
 			} catch (ClassNotFoundException e) {
 				throw new IOException("Failed to pull state from store - class not found");
 			}
 		}
 		
 		if (!IGM2E.songname.equals(savegame.song)) {
 			IGM2E.playBGM(savegame.song);
 			IGM2E.bgm.setPosition(savegame.songpos);
 		}
 		
 		IGM2E.hud = savegame.tmphud;
 		savegame.tmphud = null;
 		
 		return savegame;
 	}
 	
 }
