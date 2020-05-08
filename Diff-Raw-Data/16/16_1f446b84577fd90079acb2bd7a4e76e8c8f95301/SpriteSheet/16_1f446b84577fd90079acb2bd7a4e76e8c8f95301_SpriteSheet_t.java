 package com.javagroup.game.graphics.image;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 public class SpriteSheet {
 
 	private BufferedImage sheet;
 	
 	private int cellSize;
 	
 	
 	/**
 	 * 
 	 * @param path File path of the image
 	 * @param cellSize Size of the cell
 	 * @param color Color to set transparent
 	 */
 	public SpriteSheet(String path, int cellSize, int color) {
 		this.cellSize = cellSize;
 		try {
 			sheet = ImageIO.read(SpriteSheet.class.getResource(path));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		for (int y = 0; y < sheet.getHeight(); y++) {
 			for (int x = 0; x < sheet.getWidth(); x++) {
 				if (sheet.getRGB(x, y) == color) {
 					sheet.setRGB(x, y, 0x00000000);
 				}
 			}
 		}
 	}
 	
 	public SpriteSheet(String path, int cellSize) {
 		this.cellSize = cellSize;
 		try {
 			sheet = ImageIO.read(SpriteSheet.class.getResource(path));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 
 	}
 	
 	/**
 	 * 
 	 * @return Returns the full SpriteSheet image
 	 */
 	public BufferedImage getSpriteSheetImage() {
 		return sheet;
 	}
 	
 	
 	/**
 	 * 
 	 * @param x X coordinate of the subimage
 	 * @param y Y coordinate of the subimage
 	 * @param width width of the subimage
 	 * @param height height of the subimage
 	 * @return Returns a subimage specified by the parameters
 	 */
 	public BufferedImage getImage(int x, int y, int width, int height) {
 		BufferedImage image = Art.toCompatibleBufferedImage(sheet.getSubimage(x, y, width, height));
 		return image;
 	}
 	
 	/**
 	 * 
 	 * @param cell The cell number to return
 	 * @return Returns a subimage specified by the cell
 	 */
 	public BufferedImage getCell(int cell) {
		int sheetWidth = sheet.getWidth()/cellSize;
 		
		int cellX = cellSize * (cell%sheetWidth);
		int cellY = cellSize * (cell/sheetWidth);
		
		BufferedImage image = Art.toCompatibleBufferedImage(sheet.getSubimage(cellX , cellY , cellSize, cellSize));
 		return image;
 	}
 	
 }
