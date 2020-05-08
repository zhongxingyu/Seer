 package graphics;
 
 import game.Debug;
 import game.Main;
 
 import java.awt.image.BufferedImage;
 
 import javax.imageio.ImageIO;
 
 public class Sprite {
 
 	public static Image[][] load(String filename, int w, int h) {
 		try {
			BufferedImage image = ImageIO.read(Main.class
					.getResource("/ressources/" + filename));
 			int partsX = image.getWidth() / w;
 			int partsY = image.getHeight() / h;
 			Image[][] parts = new Image[partsX][partsY];
 			for (int x = 0; x < partsX; x++) {
 				for (int y = 0; y < partsY; y++) {
 					parts[x][y] = new Image(w, h);
 					image.getRGB(x * w, y * h, w, h, parts[x][y].pixels, 0, w);
 				}
 			}
 			return parts;
 		} catch (Exception e) {
 			Debug.log(Debug.ERROR, "[Sprite]-Error: Can't load " + filename);
 			return null;
 		}
 	}
 }
