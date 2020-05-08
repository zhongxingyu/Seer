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
 	public static HashMap<Player, Location> loadHomes(){
 		HashMap<Player, Location> homes = null;
 		try {
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream("homes.cak"));
 			homes = (HashMap<Player, Location>) ois.readObject();
 			ois.close();
		} catch (FileNotFoundException f){
			homes = new HashMap<Player, Location>();
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 		return homes;
 	}
 	
 	/**
 	 * Saves the homes to file
 	 */
 	public static void saveHomes(HashMap<Player, Location> homes){
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
