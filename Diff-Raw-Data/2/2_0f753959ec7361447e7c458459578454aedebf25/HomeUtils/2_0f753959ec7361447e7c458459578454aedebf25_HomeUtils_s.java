 package de.lycake.CakeHomePlugin;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public class HomeUtils {
 
 	/**
 	 * Loads the homes from file
 	 */
 	public static HashMap<String,double[]> loadHomes(){
 		HashMap<String, double[]> homes = null;
 		try {
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream("Plugins/CakePlugins/homes.cak"));
 			homes = (HashMap<String, double[]>) ois.readObject();
 			ois.close();
 		} catch (FileNotFoundException f){
 			homes = new HashMap<String, double[]>();
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 		return homes;
 	}
 	
 	/**
 	 * Saves the homes to file
 	 */
 	public static void saveHomes(HashMap<String, double[]> homes){
 		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("homes.cak"));
 			oos.writeObject(homes);
 			oos.flush();
 			oos.close();
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 }
