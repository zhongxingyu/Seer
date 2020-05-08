 package com.javagroup.game.map;
 import java.awt.Graphics;
 import java.util.Random;
 
 import com.javagroup.game.graphics.BasicGame;
 import com.javagroup.game.graphics.image.Art;
import com.javagroup.game.map.Tile.TileType;
 
 
 public class Map {
 
 	public static final float tileSize = 32;
 	public static final float chunkSize = 16;
 	
 	private String name;
 	private Tile[][] tiles;
 	private int width;
 	private int height;
 	private Random r = new Random();
 	
 	public Map(String name, int width, int height) {
 		this.name = name;
 		tiles = new Tile[width][height];
 		this.width = width;
 		this.height = height;
 		loadTiles();
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void loadTiles() {
 		
 		
 		int setUnCol = 2;
 		int Cd = 0;
 		int Ce = 0;
 		
 		for (int y = 0; y < height; y++) {
 			for (int x = 0; x < width; x++) {
 				
 				
 			
 				
 				if(setUnCol != 0){
 				
 					setUnCol -= 1;
 					
 					int R = r.nextInt(2);
 					
 					if(R == 1){
 					tiles[y][x] = new Tile(0, x*tileSize, y*tileSize);
 					}
 					if(R == 0){
 					tiles[y][x] = new Tile(4, x*tileSize, y*tileSize);
 					}
 				}
 				if(setUnCol == 0){
 					
 				
 					tiles[y][x] = new Tile(r.nextInt(3)+1, x*tileSize, y*tileSize);
 					
 					
 					if(r.nextInt(2) == 0){
 					if(tiles[y][x].getID() < 4 && r.nextInt(20) < 15){
 						
 						
 										
 						setUnCol += r.nextInt(10);
 					}
 					}else{
 						
 						if(tiles[y][x].getID() < 4 && r.nextInt(20) >= 10){
 							
 							setUnCol += r.nextInt(20) + 1;
 						}
 						
 					}
 				}
 				
 			
 				
 					//tiles[x][y] = new Tile(r.nextInt(5), x * tileSize, y * tileSize);
 					}
 		}
 		
 		determineTileType();
 	}
 	
 	public void determineTileType() {
 		
 		for (int y = 0; y < height; y++) {
 			for (int x = 0; x < width; x++) {
 				Tile t = tiles[x][y];
 				
 			}
 		}
 	}
 	
 	public void render(Graphics g, float xOffset, float yOffset) {
 		for (int y = 0; y < height; y++) {
 			for (int x = 0; x < width; x++) {
 				if(tiles[x][y].getY()+yOffset+Art.getTileSheet().getCellSize() < 0 || tiles[x][y].getY()+yOffset > BasicGame.size.height)
 					continue;
 				else if(tiles[x][y].getX()+xOffset+Art.getTileSheet().getCellSize() < 0 || tiles[x][y].getX()+xOffset > BasicGame.size.width)
 					continue;
 				g.drawImage(tiles[x][y].getTexture().getImage(), (int)(xOffset + tiles[x][y].getX()), (int)(yOffset + tiles[x][y].getY()), (int)tileSize, (int)tileSize, null);
 			}
 		}
 		
 	}
 	
 }
