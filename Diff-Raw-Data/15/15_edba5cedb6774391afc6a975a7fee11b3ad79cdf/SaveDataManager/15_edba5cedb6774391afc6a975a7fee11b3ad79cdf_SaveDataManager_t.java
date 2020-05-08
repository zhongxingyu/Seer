 package com.gildorym.basicchar;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Map;
 
 public class SaveDataManager {
 	
 	public static void saveClassData(BasicChar plugin) {
 		try {
 			if (!plugin.getDataFolder().exists()) {
 				plugin.getDataFolder().mkdir();
 			}
 			File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "classes.dat");
 			file.delete();
 			file.createNewFile();
 			FileOutputStream fos = new FileOutputStream(plugin.getDataFolder().getAbsolutePath() + File.separator + "classes.dat");
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 			oos.writeObject(plugin.classes);
			oos.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static void loadClassData(BasicChar plugin) {
 		try {
 			File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "classes.dat");
 			if (file.exists()) {
 				FileInputStream fis = new FileInputStream(plugin.getDataFolder().getAbsolutePath() + File.separator + "classes.dat");
 				ObjectInputStream ois = new ObjectInputStream(fis);
 				plugin.classes.putAll((Map<String, CharacterClass>) ois.readObject());
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
 	
 	public static void saveProfessionData(BasicChar plugin) {
 		try {
 			if (!plugin.getDataFolder().exists()) {
 				plugin.getDataFolder().mkdir();
 			}
 			File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "professions.dat");
 			file.delete();
 			file.createNewFile();
 			FileOutputStream fos = new FileOutputStream(plugin.getDataFolder().getAbsolutePath() + File.separator + "professions.dat");
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 			oos.writeObject(plugin.professions);
			oos.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static void loadProfessionData(BasicChar plugin) {
 		try {
 			File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "professions.dat");
 			if (file.exists()) {
 				FileInputStream fis = new FileInputStream(plugin.getDataFolder().getAbsolutePath() + File.separator + "professions.dat");
 				ObjectInputStream ois = new ObjectInputStream(fis);
 				plugin.professions.putAll((Map<String, CharacterProfession>) ois.readObject());
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
 	
 	public static void saveLevelData(BasicChar plugin) {
 		try {
 			if (!plugin.getDataFolder().exists()) {
 				plugin.getDataFolder().mkdir();
 			}
 			File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "levels.dat");
 			file.delete();
 			file.createNewFile();
 			FileOutputStream fos = new FileOutputStream(plugin.getDataFolder().getAbsolutePath() + File.separator + "levels.dat");
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 			oos.writeObject(plugin.levels);
			oos.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static void loadLevelData(BasicChar plugin) {
 		try {
 			File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "levels.dat");
 			if (file.exists()) {
 				FileInputStream fis = new FileInputStream(plugin.getDataFolder().getAbsolutePath() + File.separator + "levels.dat");
 				ObjectInputStream ois = new ObjectInputStream(fis);
 				plugin.levels.putAll((Map<String, Integer>) ois.readObject());
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
 	
 	public static void saveExperienceData(BasicChar plugin) {
 		try {
 			if (!plugin.getDataFolder().exists()) {
 				plugin.getDataFolder().mkdir();
 			}
 			File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "experience.dat");
 			file.delete();
 			file.createNewFile();
 			FileOutputStream fos = new FileOutputStream(plugin.getDataFolder().getAbsolutePath() + File.separator + "experience.dat");
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 			oos.writeObject(plugin.experience);
			oos.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static void loadExperienceData(BasicChar plugin) {
 		try {
 			File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "experience.dat");
 			if (file.exists()) {
 				FileInputStream fis = new FileInputStream(plugin.getDataFolder().getAbsolutePath() + File.separator + "experience.dat");
 				ObjectInputStream ois = new ObjectInputStream(fis);
 				plugin.experience.putAll((Map<String, Integer>) ois.readObject());
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
