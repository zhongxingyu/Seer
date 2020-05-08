 package pl.edu.agh.student.pathfinding.map;
 
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 public class BitmapMap implements IMap {
 	
 	private static final int START_COLOR = 0x0000FF00; // red
 	private static final int FINISH_COLOR = 0x00FF0000; // green
 	
 	private int width;
 	private int height;
 	private int[][] costsTable;
 	private Point start;
 	private Point finish;
 	
 	public BitmapMap(File bitmap) throws IOException {
		BufferedImage image = ImageIO.read(bitmap);
 		parseBitmap(image);
 		validateBitmap();
 	}
 
 	private void validateBitmap() throws IOException {
 		if(start == null) {
 			throw new IOException("Bitmap does not contain starting point.");
 		}
 		if(finish == null) {
 			throw new IOException("Bitmap does not contain finish point.");
 		}
 	}
 
 	private void parseBitmap(BufferedImage image) {
 		width = image.getWidth();
 		height = image.getHeight();
 		costsTable = new int[width][height];
 		for(int x=0; x<width; x++) {
 			for(int y=0; y<height; y++) {
 				int rgb = image.getRGB(x, y);
 				costsTable[x][y] = getCost(rgb);
 				if((rgb & 0x00FFFFFF) == START_COLOR) {
 					start = new Point(x, y);
 				} else if((rgb & 0x00FFFFFF) == FINISH_COLOR) {
 					finish = new Point(x, y);
 				}
 			}
 		}
 	}
 
 	private int getCost(int rgb) {
 		int red = 255 - ((rgb >> 16) & 0x000000FF);
 		int green = 255 - ((rgb >> 8 ) & 0x000000FF);
 		int blue = 255 - ((rgb) & 0x000000FF);
 		return red + green + blue;
 	}
 
 	@Override
 	public Point getStartingPoint() {
 		return start;
 	}
 
 	@Override
 	public Point getFinishPoint() {
 		return finish;
 	}
 
 	@Override
 	public int getWidth() {
 		return width;
 	}
 
 	@Override
 	public int getHeight() {
 		return height;
 	}
 
 	@Override
 	public boolean isAccessible(Point point) {
 		return getCost(point) < 255 + 255 + 255;
 	}
 
 	@Override
 	public int getCost(Point point) {
 		return costsTable[point.x][point.y];
 	}
 
 }
