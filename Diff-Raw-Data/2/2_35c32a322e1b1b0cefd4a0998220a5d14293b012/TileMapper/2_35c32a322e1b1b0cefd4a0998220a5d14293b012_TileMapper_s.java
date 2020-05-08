 package com.example.lightdetector;
 
 import java.util.ArrayList;
 
 import org.opencv.core.Point;
 import org.opencv.core.Size;
 
 /**
  * This class maps point from image to given category.
  * Consider situation below
  * -------------------------
  * |          |            |
  * |          |     X      |
  * |          |            |
  * -------------------------
  *  X means point to be categorized
  *  tilesCountX = 2
  *  tilesCountY = 1
  *  After mapping, result should be (1, 0), because X is in Second box in
  *  horizontal and in first box in vertical.
  * @author Tomas Dohnalek
  *
  */
 public class TileMapper {
 	
 	private int tilesCountX;
 	private int tilesCountY;
 	
	private Boolean debug = true;
 
 	/**
 	 * Creates TileMapping class specifying tiles
 	 * @param tilesCountX
 	 * @param tilesCountY
 	 */
 	public TileMapper(int tilesCountX, int tilesCountY) {
 		setTileSize(tilesCountX, tilesCountY);
 	}
 
 	public void setTileSize(int tileCountX, int tileCountY) {
 		this.tilesCountX = tileCountX;
 		this.tilesCountY = tileCountY;
 	}
 	
 	/**
 	 * Maps a whole bunch of points, see map() below
 	 * @param imageSize
 	 * @param listOfPoints
 	 * @return List of categories
 	 */
 	public ArrayList<Point> mapList(Size imageSize, ArrayList<Point> listOfPoints) {
 		ArrayList<Point> result = new ArrayList<Point>();
 		
 		for (Point point: listOfPoints) {
 			result.add(map(imageSize, point));
 		}
 		
 		return result;
 	}
 
 	/**
 	 * Maps single point according to image size
 	 * @param imageSize
 	 * @param point
 	 * @return pair of x category and y category
 	 */
 	public Point map(Size imageSize, Point point) {
 		if (debug) {
 			System.out.print("TileMapper::map:");
 			System.out.print("imageSize = " + imageSize.width + "x" + imageSize.height);
 		}
 		
 		Point tile = new Point();
 		
 		tile.x = mapToBox(tilesCountX, imageSize.width, point.x);
 		tile.y = mapToBox(tilesCountY, imageSize.height, point.y);
 		
 		if (debug) {
 			System.out.print("TileMapper::map:");
 			System.out.print(" imageSize = " + imageSize.width + "x" + imageSize.height);
 			System.out.print(" point = " + point.x + " " + point.y);
 			System.out.print(" => " + tile.x + " " + tile.y);
 			System.out.println();
 		}
 		
 		return tile;
 	}
 	
 	private int mapToBox(int boxes, double size, double position) {
 		double boxSize = size/boxes; 
 		double sum = 0;
 		int index = 0;
 		while (sum < position) {
 			sum += boxSize;
 			index++;
 		}
 		
 		if (index > 0)
 			index -= 1; // correction 
 		
 		return index;
 	}
 	
 	public void setDebug(Boolean flag) {
 		debug = flag;
 	}
 }
