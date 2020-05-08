 /* 
  * File Name: Map.java
  * Contributors:	Jonathan Bradley 	- 7/18/2013
  * 					Ryan Meier			- 
  * 					Ben Emrick			-
  * 
  * Purpose: This class creates a map grid to keep track of each game tile
  * 
  * Future Goals: change constructor to read-in a text file for different
  * 		levels once we have the different types of tiles developed
  */
 package td.map;
 
 import java.awt.Image;
 


 public class Map {
 	private boolean hasChanged;
 	private int mapWidth, mapHeight;
 	private Tile mapGrid[][];
 	private Image map;
 	private int TileID;
 	
 	public Map(int width, int height) {
 		Tile mapGrid[][] = new Tile[height][width];
 		TileID = 0;
 		for (int i = 0; i < width; i++) {
 			for (int k = 0; k < height; k++)	 {
 				mapGrid[i][k] = new Tile(
						"../artAssets/mapTile_Grid.png", 
 						TileID++);
 			}
 		}
 		createBufferedImage();
 				
 	}
 	
 	public int getHeight() {
 		return mapHeight;
 	}
 	
 	public int getWidth() {
 		return mapWidth;
 	}
 	
 	public void tick() {
 		
 	}
 	
 	public void render() {
 		if(hasChanged) {
 			createBufferedImage();
		} else {
			
		}
 	}
 	
 	private void createBufferedImage() {
 //		Graphics g;
 //		Graphics2D g2d = (Graphics2D)g;
 //		Tile tempTile;
 //		for (int i = 0; i <= mapHeight; i++){
 //			for (int k = 0; i<= mapWidth; i++){
 //				tempTile = mapGrid[i][k];
 //				g2d.drawImage(tempTile.getTileImage(),
 //						k * tempTile.getTileWidth(),
 //						i * tempTile.getTileHeight(),
 //						null);
 //			}
 //		}
 		
 		hasChanged = false;
 	}
 	
 	public void changeTile(String img, int ID){
 		// use black magic to find the location in the grid
 		mapGrid	[(int)(Math.floor(ID/mapWidth))]
 				[(ID % mapWidth)] 
 			    = new Tile(img, ID);
 		hasChanged = true;
 	}
 }
