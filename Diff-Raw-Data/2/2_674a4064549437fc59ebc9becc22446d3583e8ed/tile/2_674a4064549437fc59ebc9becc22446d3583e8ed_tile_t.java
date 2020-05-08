 import java.awt.Dimension;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 
 public class tile {
 	BufferedImage sprite;
 	int value;
 	int fuelCost;
 	String name;
 	static String[] fileNames = {"tunnel.png", "dirt.png", "iron.png", "gold.png", "silver.png", "diamond.png", "emerald.png"};
 	static int[] moneyValues = {0,0,1,3,2,5,4};
 	static int[] fuelConsumption = {1,2,4,3,3,6,5};
 	public tile(Dimension size, int number){
 		value = moneyValues[number];
 		fuelCost = fuelConsumption[number];
		name = fileNames[number].split("\\.")[0];
 		sprite = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
 		try {
 			sprite = ImageIO.read(new File("tiles/"+fileNames[number]));
 		} catch (IOException e) {
 			e.printStackTrace();}
 	}
 	public BufferedImage getSprite() {
 		return sprite;
 	}
 	public int getValue() {
 		return value;
 	}
 	public int getFuelCost(){
 		return fuelCost;
 	}
 	public String getName(){
 		return name;
 	}
 	
 }
