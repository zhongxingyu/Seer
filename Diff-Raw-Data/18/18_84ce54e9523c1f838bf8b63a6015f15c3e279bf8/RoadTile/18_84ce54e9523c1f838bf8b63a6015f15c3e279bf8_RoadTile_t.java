 package environment;
 
 import Core.Canvas;
 import Items.*;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import javax.imageio.ImageIO;
 
 import Items.Item;
 
 public class RoadTile {
 
 	private int position;
 	private Map<Item, Point> items;
 
 	private Tiles parent;
 
 	public RoadTile(int position, Tiles parent) {
 		this.position = position;
 		items = new HashMap<Item, Point>();
 		this.parent = parent;
 		generateItems();
 	}
 
 	public void generateItems() {
 		//generate up to 4 boulders
 		if (position < 1) return;
 		
 		for (int i = 0; i < 4; i++){
 			double rand = Math.random();
 			if (rand < 0.7){
 				Point p = generateValidPoint(Tiles.IMG_WD*2);
 				if (p.x < 0) break;
 				String filepath = "src" + File.separatorChar + "data" + File.separatorChar + "Boulder.png";
 				BufferedImage image = null;
 				try{
 					image = ImageIO.read(new FileInputStream(filepath));
 				}
 				catch(IOException e){}
 				this.items.put(new Boulder(image), p);
 			}
 		}
 		
 		
 		// generate up to 5 items
 		for (int i = 0; i < 5; i++) {
 
 			List<String> items = parent.items;
 			
 			double rand = Math.random();
 			if (rand < 0.75) {
 				// generate a valid position for the item
 				int randIndex = (int) (Math.random() * (Tiles.items.size()));
 				Point p = generateValidPoint(Tiles.IMG_WD);
 				if (p.x < 0) break;
 				String filepath = "src" + File.separatorChar + "data" + File.separatorChar;
 
 				// randomly choose an item to spawn
 				String extension;
 				BufferedImage img = null;
 
 				// rare item list
 				if (rand < 0.05) {
 
 					switch (items.get(randIndex)) {
 					case "Shrooms":
 						extension = parent.rareItems.get("Shrooms");
 						try {
 							img = ImageIO.read(new FileInputStream(filepath
 									+ extension));
 						} catch (IOException e) {
 						}
 						this.items.put(new Shrooms(img), p);
 						break;
 					}
 				}
 
 				// common item list
 				else {
 
 					switch (items.get(randIndex)) {
 					case "Beer":
 						extension = parent.commonItems.get("Beer");
 						try {
 							img = ImageIO.read(new FileInputStream(filepath
 									+ extension));
 
 						} catch (IOException e) {
 						}
 						this.items.put(new Beer(img), p);
 						break;
 
 					case "Vodka":
 						extension = parent.commonItems.get("Vodka");
 						try {
 							img = ImageIO.read(new FileInputStream(filepath
 									+ extension));
 
 						} catch (IOException e) {
 						}
 
 						this.items.put(new Whiskey(img), p);
 						break;
 
 					case "Burger":
 						extension = parent.commonItems.get("Burger");
 						try {
 							img = ImageIO.read(new FileInputStream(filepath + extension));
 						} catch (IOException e) {
 					}
 						this.items.put(new Burger(img), p);
 						break;
 					}
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * Generate a random point, iterate through the map Check if it collides
 	 * with anything
 	 * @ IMG_WD: the width of the item to generate
 	 */
 	public Point generateValidPoint(int IMG_WD) {
 
 		int TILE_HT = Tiles.TILE_HT;
 		int offset = 160;
		
		//int lowerBound = parent.canvas.SCREEN_WIDTH/2 - TILE_HT/2 + offset;
		//int upperBound = 
		
		//int upperBound = TILE_HT - 2*offset - IMG_WD/2;
 		int lowerBound = offset;
 		int upperBound = TILE_HT - 2 * offset;
 		
 		Random rand = new Random();
 		boolean valid = true;
 		int count = 0;
 
 		while (true) {
 			valid = true;
			
			
 			int xPos = rand.nextInt(upperBound) + lowerBound;
 			int yPos = rand.nextInt(TILE_HT - IMG_WD);
			
			
 			Rectangle rect = new Rectangle(xPos, yPos, IMG_WD, IMG_WD);
 
 			//get width of all items, check for intersection
 			for (Map.Entry<Item,Point> me : items.entrySet()){
 				Point p = me.getValue();
 				Item itm = me.getKey();
 				int width = itm.getImage().getHeight();
 				Rectangle itemRect = new Rectangle(p.x,p.y,width,width);
 				
 				if (rect.intersects(itemRect)){
 					valid = false;
 					count++;
 					break;
 				}
 				
 			}
 
 			if (valid)
 				return new Point(xPos,yPos);
 			if (count > 4)
 				return new Point(-1,-1);
 			
 		}
 			
 	}
 
 	public Map<Item, Point> getMap() {
 		return this.items;
 	}
 
 }
