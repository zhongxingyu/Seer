 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Random;
 
 public class Map {
 
     private Tile[] tiles;
     private int mapNum;
     public static final int WIDTH = 9;
     public static final int HEIGHT = 5;
 
     public Map(int mapNum) {
         tiles = new Tile[WIDTH * HEIGHT];
         this.mapNum = mapNum;
         initializeMap();
     }
 
     private void initializeMap() {
         int numTiles;
         if (mapNum == 1) {
             numTiles = setMap1();
         }
         else if (mapNum == 2) {
             numTiles = setMap2();
         }
         else if (mapNum == 3) {
             numTiles = setMap3();
         }
         else if (mapNum == 4) {
             numTiles = setMap4();
         }
         else {
             numTiles = setMap5(); 
         } 
 
         // this will need to be changed, I'm just guessing
         Random rand = new Random();
         int numMount1 = (rand.nextInt() % 2) + 2;
         int numMount2 = (rand.nextInt() % 2) + 2;
         int numMount3 = (rand.nextInt() % 2) + 2;
 
         int numElementsToAdd = WIDTH * HEIGHT - numTiles; // enough mountains and plains to fill the rest of the map
         ArrayList<Tile> newTiles = new ArrayList<Tile>();
         addMountains(1, numMount1, newTiles);
         addMountains(2, numMount2, newTiles);
         addMountains(3, numMount3, newTiles);
 
         numElementsToAdd -= (numMount1 + numMount2 + numMount3); // this is how many plains we need to add
         while (numElementsToAdd > 0) {
             newTiles.add(new PlainTile());
             numElementsToAdd -= 1;
         }
 
         Collections.shuffle(newTiles);
 
         // populate the map
         for (int i = 0; i < WIDTH * HEIGHT; i++) {
             if (tiles[i] == null) {
                 tiles[i] = newTiles.get(0);
                 newTiles.remove(0);
             }
         }
     }
 
 
     public Tile[] getTiles() {
         return tiles;
     }
 
     public int getMapNum() {
         return mapNum;
     }
 
     public Tile getTile(int row, int column) {
         if (row < 0 || row >= HEIGHT || column < 0 || column >= WIDTH) {
             return null;
         }
 
         return tiles[coordToLinear(row, column)];
     }
 
     public void setTile(int row, int column, Tile tile) {
         if (row < 0 || row >= HEIGHT || column < 0 || column >= WIDTH) {
             return;
         }
         tiles[coordToLinear(row, column)] = tile;
     }
 
     private int coordToLinear(int row, int column) {
         return row * WIDTH + column;
     }
 
     private void addMountains(int mountainNum, int numToAdd, ArrayList<Tile> tiles) {
         while (numToAdd > 0) {
             tiles.add(new MountainTile(mountainNum));
             numToAdd -= 1;
         }
         return;
     }
 
     private int setMap1() {
         tiles[coordToLinear(0, 4)] = new RiverTile();
         tiles[coordToLinear(1, 4)] = new RiverTile();
         tiles[coordToLinear(2, 4)] = new TownTile();
         tiles[coordToLinear(3, 4)] = new RiverTile();
         tiles[coordToLinear(4, 4)] = new RiverTile();
         return 5;
     }
 
     private int setMap2() {
         tiles[coordToLinear(0, 2)] = new RiverTile();
         tiles[coordToLinear(1, 2)] = new RiverTile();
         tiles[coordToLinear(1, 3)] = new RiverTile();
         tiles[coordToLinear(1, 4)] = new RiverTile();
         tiles[coordToLinear(2, 4)] = new TownTile();
         tiles[coordToLinear(3, 4)] = new RiverTile();
         tiles[coordToLinear(3, 5)] = new RiverTile();
         tiles[coordToLinear(3, 6)] = new RiverTile();
         tiles[coordToLinear(3, 7)] = new RiverTile();
         tiles[coordToLinear(3, 8)] = new RiverTile();
         return 10;
     }
 
     private int setMap3() {
         tiles[coordToLinear(0, 6)] = new RiverTile();
         tiles[coordToLinear(1, 6)] = new RiverTile();
         tiles[coordToLinear(2, 6)] = new RiverTile();
         tiles[coordToLinear(3, 6)] = new RiverTile();
         tiles[coordToLinear(3, 5)] = new RiverTile();
         tiles[coordToLinear(3, 4)] = new RiverTile();
         tiles[coordToLinear(2, 4)] = new TownTile();
         tiles[coordToLinear(1, 4)] = new RiverTile();
         tiles[coordToLinear(1, 3)] = new RiverTile();
         tiles[coordToLinear(1, 2)] = new RiverTile();
         tiles[coordToLinear(2, 2)] = new RiverTile();
         tiles[coordToLinear(3, 2)] = new RiverTile();
         tiles[coordToLinear(4, 2)] = new RiverTile();
         return 13;
     }
 
     private int setMap4() {
         tiles[coordToLinear(4, 2)] = new RiverTile();
         tiles[coordToLinear(3, 2)] = new RiverTile();
         tiles[coordToLinear(2, 2)] = new RiverTile();
         tiles[coordToLinear(1, 2)] = new RiverTile();
         tiles[coordToLinear(1, 3)] = new RiverTile();
         tiles[coordToLinear(1, 4)] = new RiverTile();
         tiles[coordToLinear(1, 5)] = new RiverTile();
         tiles[coordToLinear(2, 5)] = new RiverTile();
         tiles[coordToLinear(3, 5)] = new RiverTile();
         tiles[coordToLinear(4, 5)] = new RiverTile();
         tiles[coordToLinear(2, 4)] = new TownTile();
         return 11;
     }
 
     private int setMap5() {
         tiles[coordToLinear(1, 0)] = new RiverTile();
         tiles[coordToLinear(1, 1)] = new RiverTile();
         tiles[coordToLinear(1, 2)] = new RiverTile();
         tiles[coordToLinear(2, 2)] = new RiverTile();
         tiles[coordToLinear(3, 2)] = new RiverTile();
         tiles[coordToLinear(3, 3)] = new RiverTile();
         tiles[coordToLinear(3, 4)] = new RiverTile();
         tiles[coordToLinear(3, 5)] = new RiverTile();
         tiles[coordToLinear(3, 6)] = new RiverTile();
         tiles[coordToLinear(2, 6)] = new RiverTile();
         tiles[coordToLinear(1, 6)] = new RiverTile();
         tiles[coordToLinear(0, 6)] = new RiverTile();
         tiles[coordToLinear(2, 4)] = new TownTile();
         return 13;
     }
 }
 
