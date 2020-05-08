 package core;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 
 public abstract class Renderer{
 	public static final byte WALLS = 0, POWERUPS = 16, PLAYERS = 32;
 
 	int width, height;
 	int tileSize;
 
 	public BufferedImage buffer;
 	public Graphics g;
 
 	public Renderer(int width, int height, int tileSize){
 		this.width = width;
 		this.height = height;
 	        this.tileSize = tileSize;
		buffer = new BufferedImage(width * tileSize, height * tileSize, BufferedImage.TYPE_INT_RGB);
 		g = buffer.getGraphics();
 	}
 
 	public abstract void clear();
 	public abstract void drawElement(int x, int y, int frame, byte elementType);
 	public void renderMap(byte[][] map, int frame){
 		int xM = Math.min(map.length, width);
 		int yM = Math.min(map[0].length, height);
 		for(int x = 0; x < xM; x++)
 			for(int y = 0; y < yM; y++)
 				drawElement(x, y, frame, map[x][y]);
 	}
 }
