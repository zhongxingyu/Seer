 package engine;
 
 import java.awt.Point;
 import java.util.ArrayList;
 
 import util.Vector2D;
 
 /*
  * A field stores the Level information. It is separated in square Tiles.
  * (0,0) is bottom left, (1,1) is bottom right.
  * At this point a Tile with value 0 is empty  and a tile with value 1 an obstacle
  */
 
 
 public class Field {
 	public int tiles[][];
 	public int tilesX=0;
 	public int tilesY=0;
 	public int tileSize;
 	
 	public Field(int numX, int numY, int tS) {
 		tiles = new int[numX][numY];
 		tilesX = numX;
 		tilesY = numY;
 		tileSize = tS;
 	}
 	
 	public void createCricle(Vector2D pos, double r) {
 		for(int x=0; x<tilesX; x++) {
 			for(int y=0; y<tilesY; y++) {
 				//System.out.println(pos.distance(getWorldPos(new Point(x,y))));
 				if(pos.distance(getWorldPos(new Point(x,y))) < r)
 					tiles[x][y] = 1;
 			}
 		}
 	}
 	
 	//Returns the position of the center of a Tile
 	public Vector2D getWorldPos(Point pos) {
 		/*return new Vector2D(
 				(double)pos.x/(double)tilesX+tileSize/2,
 				(double)pos.y/(double)tilesY+tileSize/2);*/
 		return new Vector2D(
 				(double)pos.x+0.5,
 				(double)pos.y+0.5);
 	}
 	
 	public void setTile(Vector2D pos, int value) {
 		Point p = tileIndexAt(pos);
 		tiles[p.x][p.y] = value;
 	}
 	
 	public Point tileIndexAt(Vector2D pos) {
 		return new Point(
 				(int)(pos.x()),
 				(int)(pos.y()));
 	}
 	
 	public int tileValueAt(Vector2D pos) {
 		Point tile = tileIndexAt(pos);
 		if(tile.x>=tilesX || tile.x < 0
 		 ||tile.y>=tilesY || tile.y < 0)
 			return 1;
 		return tiles[tile.x][tile.y];
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
			Vector2D p =tiles.get(i);
			if(p.x() < 0 || p.x() > 1.0 ||
			   p.y() < 0 || p.y() > 1.0) {
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
