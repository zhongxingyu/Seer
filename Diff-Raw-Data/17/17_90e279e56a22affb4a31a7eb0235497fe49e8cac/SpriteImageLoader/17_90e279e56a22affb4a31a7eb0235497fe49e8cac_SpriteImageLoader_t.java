 package edu.gatech.cs2340.io;
 
 import java.awt.Color;
 import java.io.File;
import java.io.Serializable;
 import java.util.HashMap;
 
 import javax.swing.ImageIcon;
 
 import edu.gatech.cs2340.data.Player;
 
 /**
  * 
  * @author Stephen Conway Function group: View
  * 			created for M8. 
  * 
  * 
  * 
  *         Purpose: Holds all sprite images. Gives them out based on Player object data.
  */
public class SpriteImageLoader extends ImageLoader implements Serializable {
 	private static final Color[] colors = { Color.BLUE, Color.YELLOW, Color.GREEN, Color.RED };
 	private static final String[] races = {"bonzoid", "buzzite", "flapper", "human", "ugaite"};
 	private static final String[] color_png = {"_blue.png", "_gold.png", "_green.png", "_red.png"};
 	
 	private static HashMap<String, HashMap<Color, ImageIcon>> images;
 	private static boolean loaded;
 	
 	/**
 	 * Method to load sprite images from files. Runs loads asynchronously.
 	 */
 	public void loadImages() {
 		if (loaded) {
 			throw new RuntimeException("SpriteImageLoader already loaded images!");
 		}
 		images = new HashMap<String, HashMap<Color, ImageIcon>>();
 		Thread thread = new Thread(this);
 		thread.start();
 	}
 
 	/**
 	 * Method to load sprite images asynchronously.
 	 */
 	@Override
 	public void run() {		
 		for (String race : races) {
 			HashMap<Color, ImageIcon> raceImages = new HashMap<Color, ImageIcon>();
 			for (int i=0; i<4; i++) {
 				File imageFile = new File(String.format("%s/edu.gatech.cs2340.res.%s/%s%s",resourcesPath,race,race,color_png[i]));
 				if (!imageFile.exists()) {
 					throw new RuntimeException("Cannot locate " + imageFile.getAbsolutePath() + ".");
 				}
 				ImageIcon ii = new ImageIcon(imageFile.getAbsolutePath());
 				raceImages.put(colors[i], ii);
 			}
 			images.put(race, raceImages);
 		}
 		loaded = true;
 	}
 	
 	/**
 	 * Method to get sprite image data based on the data in the input Player object.
 	 * 
 	 * @param player
 	 * @return
 	 */
 	public ImageIcon getImage(Player player) {
 		if (!loaded) {
 			throw new RuntimeException("Must initialize SpriteImageLoader first!");
 		}
 		
 		String race = player.getRace().toLowerCase();
 		Color color = player.getPlayerColor();
 		HashMap<Color, ImageIcon> colorMap = images.get(race);
 		if (colorMap == null) {
 			throw new RuntimeException("Cannot find race '" + race + "'");
 		}
 		ImageIcon image = colorMap.get(color);
 		if (image == null) {
 			throw new RuntimeException(String.format("Cannot find color '%s' in race '%s'",color, race));
 		}
 		return image;
 	}
 	
 	
 }
