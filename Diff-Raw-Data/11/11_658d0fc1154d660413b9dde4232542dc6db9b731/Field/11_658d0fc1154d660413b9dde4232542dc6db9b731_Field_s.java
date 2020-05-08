 package engine;
 
 import java.awt.Point;
 import java.util.ArrayList;
 
 import util.Line2D;
 import util.Util;
 import util.Vector2D;
 
 /*
  * A field stores the Level information. It is separated in square Tiles.
  * (0,0) is bottom left, (1,1) is bottom right.
  * At this point a Tile with value 0 is empty  and a tile with value 1 an obstacle
  */
 
 
 public class Field {
 	public Tile tiles[][];
 	public int tilesX=0;
 	public int tilesY=0;
 	private ArrayList<Line2D> boundingFrame = new ArrayList<Line2D>();
 	
 	public Field(int numX, int numY) {
 		tiles = new Tile[numX][numY];
 		tilesX = numX;
 		tilesY = numY;
 		
 		for(int x=0;x<tilesX;x++) {
 			for(int y=0;y<tilesY;y++) {
 				tiles[x][y] = new Tile(new Point(x,y),0);
 			}
 		}
 		calcBoundingFrame();
 	}
 	
 	public void createCricle(Vector2D pos, double r) {
 		for(int x=0; x<tilesX; x++) {
 			for(int y=0; y<tilesY; y++) {
 				//System.out.println(pos.distance(getWorldPos(new Point(x,y))));
 				if(pos.distance(getWorldPos(new Point(x,y))) < r)
 					tiles[x][y].setType(1);
 			}
 		}
 	}
 	
 	public void movePointIntoIndexBounds(Point p) {
 		if(p.x >= tilesX) p.x = tilesX-1;
 		if(p.x < 0) p.x = 0;
 		if(p.y >= tilesY) p.y = tilesY-1;
 		if(p.y < 0) p.y = 0;
 	}
 	
 	private void calcBoundingFrame() {
 		boundingFrame.clear();
 		double sx = tilesX;
 		double sy = tilesY;
 		boundingFrame.add(new Line2D(new Vector2D(0,sy), new Vector2D(0,0)));
 		boundingFrame.add(new Line2D(new Vector2D(0,0), new Vector2D(sx,0)));
 		boundingFrame.add(new Line2D(new Vector2D(sx,0), new Vector2D(sx,sy)));
 		boundingFrame.add(new Line2D(new Vector2D(sx,sy), new Vector2D(0,sy)));
 	}
 
 	public ArrayList<Line2D> getBoundingFrame() {
 		return boundingFrame;
 	}
 	
 	//Returns the position of the center of a Tile
 	public Vector2D getWorldPos(Point pos) {
 		return new Vector2D(
 				(double)pos.x+0.5,
 				(double)pos.y+0.5);
 	}
 	
 	public void setTile(Vector2D pos, int value) {
 		Point p = tileIndexAt(pos);
 		tiles[p.x][p.y].setType(value);
 	}
 	
 	public Point tileIndexAt(Vector2D pos) {
 		return new Point(
 				(int)(pos.x()),
 				(int)(pos.y()));
 	}
 	
 	public Tile tileAt(Vector2D pos) {
 		Point p = tileIndexAt(pos);
 		return tileAt(p);
 	}
 	
 	public Tile tileAt(Point index) {
 		if(Util.isValueInBounds(0, index.x, tilesX-1) &&
 			Util.isValueInBounds(0, index.y, tilesY-1)) {
 			return tiles[index.x][index.y];
 		}
		else return null;	
 	}
 	
 	public Tile tileAt(int x,int y) {
 		return tileAt(new Point(x,y));
 	}
 	
 	public int tileValueAt(Vector2D pos) {
 		Point tile = tileIndexAt(pos);
 		if(tile.x>=tilesX || tile.x < 0
 		 ||tile.y>=tilesY || tile.y < 0)
 			return 1;
 		return tiles[tile.x][tile.y].getType();
 	}
 	
 	public ArrayList<Vector2D> neighbours(Vector2D pos) {
 		ArrayList<Vector2D> result = new ArrayList<Vector2D>();
 		
 		Point tile = tileIndexAt(pos);
 		
 		Vector2D left,right,top,bottom;
 		left = getWorldPos(new Point(tile.x-1,tile.y));
 		right = getWorldPos(new Point(tile.x+1,tile.y));
 		top = getWorldPos(new Point(tile.x,tile.y-1));
 		bottom = getWorldPos(new Point(tile.x,tile.y+1));
 		
 		if(tileValueAt(left) != 1 && tileValueAt(top) != 1)
 			result.add(getWorldPos(new Point(tile.x-1,tile.y-1)));
 		if(tileValueAt(right) != 1 && tileValueAt(top) != 1)
 			result.add(getWorldPos(new Point(tile.x+1,tile.y-1)));
 		if(tileValueAt(left) != 1 && tileValueAt(bottom) != 1)
 			result.add(getWorldPos(new Point(tile.x-1,tile.y+1)));
 		if(tileValueAt(right) != 1 && tileValueAt(bottom) != 1)
 			result.add(getWorldPos(new Point(tile.x+1,tile.y+1)));
 		
 		result.add(left);
 		result.add(right);
 		result.add(top);
 		result.add(bottom);
 		
 		
 		result = removeInvalidTiles(result);
 		
 		
 		return result;
 		
 	}
 	
 	public void setTilesTo(ArrayList<Vector2D> tiles, int value) {
 		for(Vector2D t : tiles) {
 			setTile(t, value);
 		}
 	}
 	
 	public ArrayList<Vector2D> removeInvalidTiles(ArrayList<Vector2D> tiles) {
 		for(int i=0; i<tiles.size();i++) {
 			Vector2D p = tiles.get(i);
 			if(p.x() < 0 || p.x() > tilesX ||
 			   p.y() < 0 || p.y() > tilesY) {
 				tiles.remove(i);
 				i--;
 			}
 		}
 		return tiles;
 	}
 	
 	public ArrayList<Vector2D> removeNotOnes(ArrayList<Vector2D> tiles) {
 		for(int i=0; i<tiles.size();i++) {
 			Vector2D p = tiles.get(i);
 			if(this.tileValueAt(p) == 1) {
 				tiles.remove(i);
 				i--;
 			}
 				
 		}
 		return tiles;
 	}
 	public ArrayList<Vector2D> freeneighbours(Vector2D pos) {
 		ArrayList<Vector2D> result = neighbours(pos);
 		
 		result = removeNotOnes(result);
 		
 		return result;
 	}
 	
 	public boolean sameTile(Vector2D a, Vector2D b) {
 		return this.tileIndexAt(a).equals(this.tileIndexAt(b));
 	}
 	
 	public void printToConsole() {
 		for(int y=0; y<tilesY; y++) {
 			for(int x=0;x<tilesX; x++) {
 				System.out.print(tiles[x][y]);
 			}
 			System.out.println();
 		}
 		
 	}
 	
 	
 	
 }
