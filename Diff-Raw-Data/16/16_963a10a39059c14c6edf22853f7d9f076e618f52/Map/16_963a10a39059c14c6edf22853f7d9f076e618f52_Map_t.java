 import java.awt.Color;
 import java.awt.Graphics;
 
 public class Map {
 	public static final byte BLANK = 0, WALL = 1, FOOD = 2, SNAKE = 100;
 	
 	static byte[][] tiles;
 	public static int width;
 	public static int height;
 	
	public static void Init(int width, int height) {
 		Map.width = width;
 		Map.height = height;
 		
 		tiles = new byte[width][height];
 		initTiles();
 	}
 	
 	public static void MoveTo(int x, int y, Snake s) {
 		if (tiles[x][y] != BLANK) {
 			s.Collide(tiles[x][y]);
 			SnakeMain.Collide(x, y, s.idx, tiles[x][y]);
 		}
 	}
 	
 	static void initTiles() {
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				if (((i & 1) != 0 || (j & 1) != 0)) {
 					tiles[i][j] = WALL;
 				} else {
 					tiles[i][j] = BLANK;
 				}
 			}
 		}
 		randPath(0, 0, 100);
 	}
 	
 	static void randPath(int i, int j, int depth) {
         if (depth <= 0) {
             return;
         }
         tiles[i][j] = BLANK;
         if (rand(0.5f)) {
             randPath((i + 1) % width, j, depth - 1);
         }
         if (rand(0.5f)) {
             randPath(i, (j + 1) % height, depth - 1);
         }
     }
 
 	static boolean rand(float chance) { return (Math.random() < chance);
 	}
 }
