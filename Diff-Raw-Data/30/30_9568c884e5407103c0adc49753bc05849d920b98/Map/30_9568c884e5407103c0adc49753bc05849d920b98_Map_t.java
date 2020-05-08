 package snake;
 
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
 		placeFood(-1, -1);
 	}
 	
 	// (x0, y0) is where tail used to be.
 	// (x1, y1) is where head now is.
 	public static void MoveFromTo(int x0, int y0, int x1, int y1, Snake s) {
 		tiles[x0][y0] = BLANK;
 		if (tiles[x1][y1] != BLANK) {
 			s.Collide(tiles[x1][y1]);
 			SnakeMain.Collide(x1, y1, s.idx, tiles[x1][y1]);
		} else {
			tiles[x1][y1] = SNAKE;
 		}
 	}
 	
 	public static void Remove(int x, int y) {
 		tiles[x][y] = BLANK;
 	}
 	
 	public static void SetSnake(int x, int y) {
 		tiles[x][y] = SNAKE;
 	}
 	
 	static void initTiles() {
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				if (((i & 1) != 0 || (j & 1) != 0)) {
 					tiles[i][j] = BLANK;
 				} else {
 					tiles[i][j] = BLANK;
 				}
 			}
 		}
 		// randPath(0, 0, 100);
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
 
 	static boolean rand(float chance) { return (Math.random() < chance); }
 	
 	// Passing (-1, -1) indicates place but don't erase
 	static void placeFood(int x, int y) {
 		if (x != -1 && y != -1 && tiles[x][y] == FOOD)
 			tiles[x][y] = BLANK;
 		while (true) {
 			int x1 = (int)(Math.random() * width);
 			int y1 = (int)(Math.random() * height);
 			if (tiles[x1][y1] == BLANK) {
 				tiles[x1][y1] = FOOD;
 				break;
 			}
 		}
 	}
 }
