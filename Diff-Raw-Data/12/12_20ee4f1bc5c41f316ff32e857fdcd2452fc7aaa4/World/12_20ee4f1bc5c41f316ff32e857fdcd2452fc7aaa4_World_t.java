 package core;
 
 import helpers.Delegate;
 import helpers.Helper;
 import objects.*;
 
 import java.awt.*;
 import java.util.*;
 import java.util.List;
 
 /**
  * The world class represents the container for all elements of the game.
  * Has a constant step of 35 ms and updates all entities once per tick.
  * Handles entity movement, some AI, block destruction, and powerup spawning.
  */
 public class World implements Runnable {
     public boolean exists;
     private int[][] map;
     private Delegate delegate;
     private Player player;
     private List<Enemy> enemies;
     private List<Bomb> bombs;
     private List<Explosion> explosions;
     private HashMap<Point, Powerup> powerups;
     private int enemyMoveCounter;
 
     /**
      * Creates a new world object
      * @param m
      * @param d
      */
     public World(int[][] m, Delegate d) {
         map = m;
         delegate = d;
         enemies = new ArrayList<Enemy>();
         bombs = new ArrayList<Bomb>();
         explosions = new ArrayList<Explosion>();
         powerups = new HashMap<Point, Powerup>();
         exists = true;
         enemyMoveCounter = 0;
         setPlayer();
         addEnemy(new Enemy(map[0].length - 1, map.length - 1));
         addEnemy(new Enemy(0, map.length - 1));
         addEnemy(new Enemy(map[0].length - 1, 0));
     }
 
     public Player getPlayer() {
         return player;
     }
 
     // Resizes sprites to block size and sets player
     public void setPlayer() {
         player = new Player();
         player.sprite = Helper.resizeImage(player.sprite);
     }
 
     public List<Enemy> getEnemies() {
         return enemies;
     }
 
     // Resizes sprite to block size and adds enemy to list
     public void addEnemy(Enemy e) {
         e.sprite = Helper.resizeImage(e.sprite);
         enemies.add(e);
     }
 
     public List<Bomb> getBombs() {
         return bombs;
     }
 
     // Resizes sprite to block size and adds item to list
     public void addBomb(Bomb b) {
         b.sprite = Helper.resizeImage(b.sprite);
         map[b.getY()][b.getX()] = 6;
         bombs.add(b);
     }
 
     public List<Explosion> getExplosions() {
         return explosions;
     }
 
     public void addExplosion(Explosion e) {
         e.sprite = Helper.resizeImage(e.sprite);
         explosions.add(e);
     }
 
     public Collection<Powerup> getPowerups() {
         return powerups.values();
     }
 
     public void addPowerup(Powerup p) {
         p.sprite = Helper.resizeImage(p.sprite);
         powerups.put(new Point(p.getX(), p.getY()), p);
     }
 
     /**
      * Moves the player and allows checking of the environment before movement.
      * @param action
      * @param activeMap
      */
     public void playerAction(int action, int[][] activeMap) {
         int y = player.getY();
         int x = player.getX();
 
         if (activeMap[y][x] == -2 || activeMap[y][x] == -3) {
             player.kill();
             exists = false;
             player.sprite = Thing.playerDeadImg;
             player.sprite = Helper.resizeImage(player.sprite);
             return;
         }
 
         if (activeMap[y][x] == -1) {
             Powerup p = powerups.remove(new Point(x, y));
             if (p.getPower() == Powerup.Power.BombUp) {
                 player.numBombs++;
             } else {
                 player.firePower++;
             }
         }
 
         switch (action) {
             // Left
             case 1:
                 if (x > 0 && activeMap[y][x - 1] <= 0)
                     player.move(1);
                 break;
             // Up
             case 2:
                 if (y > 0 && activeMap[y - 1][x] <= 0)
                     player.move(2);
                 break;
             // Right
             case 3:
                 if (x < activeMap[y].length - 1 && activeMap[y][x + 1] <= 0)
                     player.move(3);
                 break;
             // Down
             case 4:
                 if (y < activeMap.length - 1 && activeMap[y + 1][x] <= 0)
                     player.move(4);
                 break;
             // Bomb
             case 5:
                 Bomb b = player.setBomb();
                 if (b != null)
                     addBomb(b);
                 break;
         }
     }
 
     /**
      * Moves the enemy and allows checking of the environment before movement.
      * @param enemy
      * @param action
      * @param enemyGo
      * @param activeMap
      */
     public void enemyAction(Enemy enemy, int action, boolean enemyGo, int[][] activeMap) {
         int y = enemy.getY();
         int x = enemy.getX();
 
         if (activeMap[y][x] == -3) {
             enemy.kill();
             enemy.sprite = Thing.enemyDeadImg;
             enemy.sprite = Helper.resizeImage(enemy.sprite);
             return;
         }
 
         //TODO: Fix enemy AI to use multi bombs and fire upgrades
 //        if(activeMap[y][x] == -1) {
 //            Powerup p = powerups.remove(new Point(x, y));
 //            if(p.getPower() == Powerup.Power.BombUp) {
 //                enemy.numBombs++;
 //            } else {
 //                enemy.firePower++;
 //            }
 //        }
 
         if (!enemyGo)
             return;
 
         switch (action) {
             // Left
             case 1:
                 if (x == 0 || activeMap[y][x - 1] < -1 || activeMap[y][x - 1] > 1)
                     return;
                 if (activeMap[y][x - 1] == 1)
                     break;
                 enemy.move(1);
                 activeMap[y][x - 1] = -2;
                 return;
             // Up
             case 2:
                 if (y == 0 || activeMap[y - 1][x] < -1 || activeMap[y - 1][x] > 1)
                     return;
                 if (activeMap[y - 1][x] == 1)
                     break;
                 enemy.move(2);
                 activeMap[y - 1][x] = -2;
                 return;
             // Right
             case 3:
                 if (x == activeMap[y].length - 1 || activeMap[y][x + 1] < -1 || activeMap[y][x + 1] > 1)
                     return;
                 if (activeMap[y][x + 1] == 1)
                     break;
                 enemy.move(3);
                 activeMap[y][x + 1] = -2;
                 return;
             // Down
             case 4:
                 if (y == activeMap.length - 1 || activeMap[y + 1][x] < -1 || activeMap[y + 1][x] > 1)
                     return;
                 if (activeMap[y + 1][x] == 1)
                     break;
                 enemy.move(4);
                 activeMap[y + 1][x] = -2;
                 return;
         }
         if (!nextToEnemy(x, y, activeMap)) {
             Bomb b = enemy.setBomb();
             if (b != null)
                 addBomb(b);
         }
     }
 
     /**
      * If the enemy is next to another enemy, they should not lay a bomb.
      * @param x
      * @param y
      * @param activeMap
      * @return
      */
     public boolean nextToEnemy(int x, int y, int[][] activeMap) {
         return ((y != activeMap.length - 1 && activeMap[y + 1][x] == -2) ||
                 (y != 0 && activeMap[y - 1][x] == -2) ||
                 (x != activeMap[0].length - 1 && activeMap[y][x + 1] == -2) ||
                (x != 0 && activeMap[y][x - 1] == -2));
     }
 
     /**
      * Destroys world blocks based on the points affected by the bomb's explosion
      * @param points
      */
     public void explode(List<Point> points) {
         for (Point p : points) {
             if (map[p.y][p.x] == 1) {
                 Random rand = new Random();
                 int choice = rand.nextInt(16);
                 if (choice < 2) {
                     addPowerup(new Powerup(choice, p.x, p.y));
                 }
             }
             map[p.y][p.x] = 0;
             addExplosion(new Explosion(p.x, p.y));
         }
     }
 
     /**
      * Gets the map with dynamic information about "things" on the map visible
      * @return
      */
     public int[][] getActiveMap() {
         int[][] activeMap = new int[map.length][map[0].length];
 
         for (int i = 0; i < map.length; i++) {
             System.arraycopy(map[i], 0, activeMap[i], 0, map[i].length);
         }
 
         for (Powerup p : powerups.values()) {
             activeMap[p.getY()][p.getX()] = -1;
         }
 
         for (Enemy e : enemies) {
             activeMap[e.getY()][e.getX()] = -2;
         }
 
         for (Explosion e : explosions) {
             activeMap[e.getY()][e.getX()] = -3;
         }
 
         return activeMap;
     }
 
     /**
      * The world step method.
      * Updates all "things" in the world and then repaints.
      */
     public void step() {
         int[][] activeMap = getActiveMap();
         boolean mapChanged = false;
         enemyMoveCounter++;
 
         for (int i = 0; i < explosions.size(); i++) {
             Explosion e = explosions.get(i);
             if (e.isDestroyed()) {
                 explosions.remove(i);
                 i--;
             }
         }
 
         playerAction(delegate.getInput(), activeMap);
 
         boolean enemyGo = enemyMoveCounter == 10;
 
         if (enemyGo)
             enemyMoveCounter = 0;
 
         for (int i = 0; i < enemies.size(); i++) {
             Enemy e = enemies.get(i);
             if (e.isDestroyed()) {
                 enemies.remove(i);
                 i--;
                 continue;
             }
             enemyAction(e, e.calcPath(player.getX(), player.getY(), bombs, activeMap), enemyGo, activeMap);
         }
 
 
         for (int i = 0; i < bombs.size(); i++) {
             Bomb b = bombs.get(i);
             if (b.isDestroyed() || activeMap[b.getY()][b.getX()] == -3) {
                 mapChanged = true;
                 explode(b.blowUp(map));
                 bombs.remove(i);
                 i--;
             }
         }
 
         if (mapChanged)
             delegate.setBackgroundImage(map);
 
         delegate.repaint();
     }
 
     /**
      * World will run indefinitely until it no longer exists.
      */
     @Override
     public void run() {
         while (exists) {
             try {
                 Thread.sleep(35);
             } catch (InterruptedException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
             step();
         }
     }
 }
