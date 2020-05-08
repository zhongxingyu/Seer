 package com.gildorym.charactercards;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Map;
 
 public class SaveDataManager {
 	
 	public static void saveData(CharacterCards plugin) {
 		try {
 			if (!plugin.getDataFolder().exists()) {
 				plugin.getDataFolder().mkdir();
 			}
 			File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "character-cards.dat");
 			file.delete();
 			file.createNewFile();
 			FileOutputStream fos = new FileOutputStream(plugin.getDataFolder().getAbsolutePath() + File.separator + "character-cards.dat");
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 			oos.writeObject(plugin.getCharacterCards());
			oos.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static void loadData(CharacterCards plugin) {
 		try {
 			File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "character-cards.dat");
 			if (file.exists()) {
 				FileInputStream fis = new FileInputStream(plugin.getDataFolder().getAbsolutePath() + File.separator + "character-cards.dat");
 				ObjectInputStream ois = new ObjectInputStream(fis);
 				plugin.getCharacterCards().putAll((Map<String, CharacterCard>) ois.readObject());
				ois.close();
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
