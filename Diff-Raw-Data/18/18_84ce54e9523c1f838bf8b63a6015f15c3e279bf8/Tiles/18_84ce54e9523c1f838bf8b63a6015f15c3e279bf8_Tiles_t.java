 package environment;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import javax.imageio.ImageIO;
 
 import Core.Canvas;
 import Core.GUI;
 import Items.Item;
 
 public class Tiles {
 
 	private List<RoadTile> tiles;
 	public final static int TILE_HT = 500;
 	public final static int TILE_OFFSET = 160;
 	public static BufferedImage image;
 	public static BufferedImage imageFirst;
 	public static List<String> items;
 	public static Map<String,String> commonItems;
 	public static Map<String,String> rareItems;
 	public final static int IMG_WD = 32;
	public Canvas canvas;
 	
 	public Tiles(String filepath, GUI parent) {
 		tiles = new ArrayList<RoadTile>();
 		try {
 			this.image = ImageIO.read(new FileInputStream("src"
 					+ File.separatorChar + "data" + File.separatorChar
 					+ filepath));
 			this.imageFirst = ImageIO.read(new FileInputStream("src" + File.separatorChar + "data" + File.separatorChar + "NRoad.png"));
 		} catch (Exception e) {}
 	
 		this.items = new ArrayList<String>();
 		items.add("Beer");
 		items.add("Whiskey");
 		items.add("Burger");
 		items.add("Vodka");
 		items.add("Shrooms");
 		
 		this.commonItems = new HashMap<String,String>();
 		commonItems.put("Beer","Beer.png");
 		commonItems.put("Whiskey", "Whiskey.png");
 		commonItems.put("Burger","Burger.png");
 		commonItems.put("Vodka", "Vodka.png");
 		
 		this.rareItems = new HashMap<String,String>();
 		rareItems.put("Shrooms","Shrooms.png");
 	}
 
 	public void addTile(){
 		tiles.add(new RoadTile(tiles.size(),this));
 	}
 	
 	public void setCanvas(Canvas cv){
 		this.canvas = cv;
 	}
 	
 	public RoadTile getTile(int index){
 		return tiles.get(index);
 	}
 	
 	public void draw(Graphics g, int depth){
 		
 		int x = canvas.SCREEN_WIDTH/2 - image.getWidth()/2;
 		int ht = image.getHeight();
 		int start = canvas.getHeight();
 		int startIndex = (int)(depth/TILE_HT);
 
 		
 		for (int i = startIndex; i < tiles.size(); i++){
 			
 			//debugging:
 			//Random rand = new Random();
 			//g.setColor(new Color(rand.nextFloat(),rand.nextFloat(),rand.nextFloat()));
 			//g.drawRect(x,start-ht*i-ht+depth,TILE_HT,TILE_HT);
 			
 			if (i == 0) g.drawImage(this.imageFirst, x, start - ht*i - ht + depth, null);
 			else g.drawImage(this.image, x, start - ht*i - ht + depth,null);
 			
 			Map<Item,Point> items = tiles.get(i).getMap();
 
 			
 			for (Map.Entry<Item,Point> entry : items.entrySet()){
 				Point p = entry.getValue();
 				
 				int itemX = x + p.x;
 				int itemY = start - ht*i - ht + depth + p.y;
 				
 				entry.getKey().draw(g,itemX,itemY);
 				
 				//debugging: bounding boxes
 				//g.drawRect(itemX,itemY,entry.getKey().getImage().getHeight(),entry.getKey().getImage().getHeight());
 				
 			}
 		}
 		
 		//check if the top of the last one is below the top of the screen
 		int lastHt = start - ht*tiles.size()-1 + depth;
 		if (lastHt > 0){
 			tiles.add(new RoadTile(tiles.size(),this));
 		}
 				
 	}
 	
 	public int numTiles(){
 		return this.tiles.size();
 	}
 }
