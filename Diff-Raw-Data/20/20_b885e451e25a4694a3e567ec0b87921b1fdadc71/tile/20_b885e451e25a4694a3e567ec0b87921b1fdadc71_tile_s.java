 import java.awt.Dimension;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 
 public class tile {
 	BufferedImage sprite;
 	int value;
	static String[] file_names = {"tunnel.png", "dirt.png", "iron.png", "gold.png", "silver.png", "diamond.png", "emerald.png"};
 	public tile(Dimension size, int number){
 		sprite = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
 		try {
			sprite = ImageIO.read(new File("tiles/"+file_names[number]));
 		} catch (IOException e) {
 			e.printStackTrace();}
 	}
 	public BufferedImage getSprite() {
 		return sprite;
 	}
 	public int getValue() {
 		return value;
 	}
 	
 }
