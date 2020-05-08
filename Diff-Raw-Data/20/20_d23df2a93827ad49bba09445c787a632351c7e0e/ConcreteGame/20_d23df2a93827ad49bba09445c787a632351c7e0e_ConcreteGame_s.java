 package com.github.pmcompany.pustomario.core;
 
 import com.github.pmcompany.pustomario.io.View;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import static com.github.pmcompany.pustomario.core.VectorDirection.*;
 
 /**
  * @author dector (dector9@gmail.com)
  */
 public class ConcreteGame implements EventHandler, DataProvider {
     private Map map;
     private Player player;
 
     private long updateTime;
 
     public ConcreteGame() {
     }
 
     public void initGame() {
         loadMap();
 
         updateTime = System.currentTimeMillis();
     }
 
     private void loadMap() {
         try {
             BufferedReader reader;
             reader = new BufferedReader(new FileReader("level.pml"));
 
             String[] levelParams = reader.readLine().split(" ");
 
             int levelWidth = Integer.parseInt(levelParams[0]);
             int levelHeight = Integer.parseInt(levelParams[1]);
             map = new Map(levelWidth, levelHeight);
 
             int tile;
             for (int j = levelHeight; j > 0; j--) {
                 String levelLine = reader.readLine();
                 for (int i = 1; i <= levelWidth; i++) {
                     tile = levelLine.charAt(i-1);
                     if (tile == '#') {
                         map.setAt(i, j, levelLine.charAt(i-1));
                     } else if (tile == '@') {
                         player = new Player((i-1) * View.TILE_WIDTH + 1,
                                 (j-1) * View.TILE_HEIGHT + 1);
                     }
                 }
             }
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         if (player == null) {
             player = new Player(1, 1);
         }
     }
 
     public void handleEvent(Event e) {
         switch (e.getType()) {
             case ACCELERATE_X_PLAYER: {
                 player.accelerateX(e.getFloatValue() * Physics.RUN_ACCELERATION_COEFFICIENT);
             } break;
             case ACCELERATE_Y_PLAYER: {
                 player.accelerateY(e.getFloatValue());
             } break;
 
             case RUN_RIGHT: {
                 player.accelerateX(Physics.RUN_SPEED);
             } break;
             case RUN_LEFT: {
                 player.accelerateX(-Physics.RUN_SPEED);
             } break;
             case JUMP: {
                 if (player.isCanJump()) {
                     player.accelerateY(Physics.JUMP_SPEED);
                     player.setCanJump(false);
                 }
             } break;
         }
     }
 
     public int getTileAt(int x, int y) {
         return map.getAt(x, y);
     }
 
     public Point countTileByAbs(int absX, int absY) {
         return new Point((absX-1) / View.TILE_WIDTH + 1, (absY-1) / View.TILE_HEIGHT + 1);
     }
 
     public Point countAbsByTile(int tX, int tY) {
         return new Point((tX - 1) * View.TILE_WIDTH + 1, (tY - 1) * View.TILE_HEIGHT + 1);
     }
 
     public boolean isTileBlocked(int x, int y) {
         return map.isBlocked(x, y);
     }
 
     public int getPlayerX() {
         return player.getX();
     }
 
     public int getPlayerY() {
         return player.getY();
     }
 
     public float getPlayerSpeedX() {
         return player.getSpeedX();
     }
 
     public float getPlayerSpeedY() {
         return player.getSpeedY();
     }
 
     public int getMapWidth() {
         return map.getWidth();
     }
 
     public int getMapHeight() {
         return map.getHeight();
     }
 
     public boolean isPlayerWatchingRight() {
         return player.isWatchingRight();
     }
 
     public void preUpdate() {
         long currTime = System.currentTimeMillis();
         long diff = currTime - updateTime;
 
         // Count dynamic objects position,
         // using physical laws and time value
 
         movePlayer();
 
         updateTime = currTime;
     }
 
     public void postUpdate() {
 //        player.setSpeedX(0);
 //        player.setSpeedY(0);
     }
 
     private void movePlayer() {
         player.accelerateY(Physics.GRAVITY_ACCELERATION);
         player.setSpeedX(player.getSpeedX() * Physics.GROUND_ONE_ON_FRICTION);
 
         int px = player.getX();
         int py = player.getY();
         float speedX = player.getSpeedX();
         float speedY = player.getSpeedY();
 
         // Move player
 
        Point newCrossedTile;
         List<Point> crossedTiles;
         boolean crosses = false;
 
         if (speedX != 0) {
             player.setX(px + (int)speedX);
             crossedTiles = getPlayerCrossedTiles();
 
             for (int i = 0; i < crossedTiles.size() && (! crosses); i++) {
                 newCrossedTile = crossedTiles.get(i);
 
                 if (isTileBlocked(newCrossedTile.getX(), newCrossedTile.getY())) {
                     crosses = true;
                 }
             }
 
             if (crosses) {
                 player.setX(px);
                 player.setSpeedX(0);
             }
         }
 
         crosses = false;
 
         if (speedY != 0) {
             player.setY(py + (int)speedY);
             crossedTiles = getPlayerCrossedTiles();
 
             for (int i = 0; i < crossedTiles.size() && (! crosses); i++) {
                 newCrossedTile = crossedTiles.get(i);
 
                 if (isTileBlocked(newCrossedTile.getX(), newCrossedTile.getY())) {
                     crosses = true;
                 }
             }
 
             if (crosses) {
                player.setY(py);
 
                 if (speedY < 0) {
                     player.setCanJump(true);
                 }
                 player.setSpeedY(0);
             }
         }
     }
 
     public List<Point> getPlayerCrossedTiles() {
         List<Point> tilesList = new LinkedList<Point>();
 
         int px = player.getX();
         int py = player.getY();
 
         int lx = 0;
         int ly = 0;
         int nx;
         int ny;
         int dx;
         int dy;
         Point p;
         for (int i = 0; i < 4; i++) {
             nx = px + lx * (View.TILE_WIDTH - 1);
             ny = py + ly * (View.TILE_HEIGHT - 1);
 
             dx = nx % View.TILE_WIDTH;
             dy = ny % View.TILE_HEIGHT;
 
             nx -= ((dx != 0)?dx:View.TILE_WIDTH) - 1;
             ny -= ((dy != 0)?dy:View.TILE_HEIGHT) - 1;
 
             p = countTileByAbs(nx, ny);
 
             if (! tilesList.contains(p)) {
                 tilesList.add(p);
             }
 
             if (i == 1) {
                 lx = 1;
             } else if (i == 0) {
                 ly = 1;
             } else if (i == 2) {
                 ly = 0;
             }
         }
 
         return tilesList;
     }
 
     /*
      *         1
      *         ^
      *     8 < . > 2
      *         V
      *         4
      */
     public List<Point> getPlayerNeighbourTiles(int direction) {
         List<Point> points = new LinkedList<Point>();
 
         List<Point> crossedTiles = getPlayerCrossedTiles();
 
         int lx;
         int ly;
 
         Point nP;
         Point crossedP;
         for (int i = 0; i < crossedTiles.size(); i++) {
             crossedP = crossedTiles.get(i);
 
             int k = 0;
             int directionMask;
             for (int j = 0; j < 4; j++) {
 
                 directionMask = 0;
 
                 if (direction != 0) {
                     if ((direction & UP) != 0) {
                         ly = 1;
                         directionMask |= UP;
                     } else if ((direction & DOWN) != 0) {
                         ly = -1;
                         directionMask |= DOWN;
                     } else {
                         ly = 0;
                     }
 
                     if ((direction & RIGHT) != 0) {
                         lx = 1;
                         directionMask |= RIGHT;
                     } else if ((direction & LEFT) != 0) {
                         lx = -1;
                         directionMask |= LEFT;
                     } else {
                         lx = 0;
                     }
 
                     nP = new Point(crossedP.getX() + lx, crossedP.getY() + ly);
 
                     if (! crossedTiles.contains(nP) && ! points.contains(nP)) {
                         points.add(nP);
                     }
                 }
 
                 k++;
             }
         }
 
         return points;
     }
 
 //    private int[][] getPlayerTiles() {
 //        int[][] pTiles = new int[4][2];
 //
 //        int px = player.getX();
 //        int py = player.getY();
 //        int rx = View.TILE_XRADIUS;
 //        int ry = View.TILE_YRADIUS;
 //
 //        pTiles[0][0] = px - rx;
 //        pTiles[0][1] = py + ry;
 //        pTiles[1][0] = px + rx;
 //        pTiles[1][1] = py + ry;
 //        pTiles[2][0] = px + rx;
 //        pTiles[2][1] = py - ry;
 //        pTiles[3][0] = px - rx;
 //        pTiles[3][1] = py - ry;
 //
 //        return pTiles;
 //    }
 }
