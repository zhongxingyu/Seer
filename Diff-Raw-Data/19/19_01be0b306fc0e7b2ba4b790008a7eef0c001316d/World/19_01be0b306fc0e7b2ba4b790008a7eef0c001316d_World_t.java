 package core;
 
 import helpers.Delegate;
 import helpers.Helper;
 import objects.*;
 
 import java.awt.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: brad
  * Date: 4/5/13
  * Time: 2:26 PM
  * To change this template use File | Settings | File Templates.
  */
 public class World implements Runnable {
     public boolean exists;
     private int[][] map;
     private Delegate delegate;
     private Player player;
     private List<Enemy> enemies;
     private List<Bomb> bombs;
     private List<Explosion> explosions;
     private int enemyMoveCounter;
 
     public World(int[][] m, Delegate d) {
         map = m;
         delegate = d;
         enemies = new ArrayList<Enemy>();
         bombs = new ArrayList<Bomb>();
         explosions = new ArrayList<Explosion>();
         exists = true;
         enemyMoveCounter = 0;
         setPlayer();
        addEnemy(new Enemy(map[0].length - 2, map.length - 2));
        addEnemy(new Enemy(1, map.length - 2));
        addEnemy(new Enemy(map[0].length - 2, 1));
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
 
     public void playerAction(int action, int[][] activeMap) {
         int y = player.getY();
         int x = player.getX();
 
         if (activeMap[y][x] == -2 || activeMap[y][x] == -3) {
             player.kill();
             exists = false;
             player.sprite = Thing.deadImg;
             player.sprite = Helper.resizeImage(player.sprite);
             return;
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
                 addBomb(player.setBomb());
                 break;
         }
     }
 
     public void enemyAction(Enemy enemy, int action, int[][] activeMap) {
         int y = enemy.getY();
         int x = enemy.getX();
 
         if (activeMap[y][x] == -3) {
             enemy.kill();
             return;
         }
 
         switch (action) {
             // Left
             case 1:
                 if (x == 0 || activeMap[y][x - 1] < -1)
                     return;
                 if (activeMap[y][x - 1] > 0)
                     break;
                 enemy.move(1);
                 activeMap[y][x - 1] = -2;
                 return;
             // Up
             case 2:
                 if (y == 0 || activeMap[y - 1][x] < -1)
                     return;
                 if (activeMap[y - 1][x] > 0)
                     break;
                 enemy.move(2);
                 activeMap[y - 1][x] = -2;
                 return;
             // Right
             case 3:
                 if (x == activeMap[y].length - 1 || activeMap[y][x + 1] < -1)
                     return;
                 if (activeMap[y][x + 1] > 0)
                     break;
                 enemy.move(3);
                 activeMap[y][x + 1] = -2;
                 return;
             // Down
             case 4:
                 if (y == activeMap.length - 1 || activeMap[y + 1][x] < -1)
                     return;
                 if (activeMap[y + 1][x] > 0)
                     break;
                 enemy.move(4);
                 activeMap[y + 1][x] = -2;
                 return;
         }
         addBomb(enemy.setBomb());
     }
 
     public void explode(List<Point> points) {
         for (Point p : points) {
             map[p.y][p.x] = 0;
             addExplosion(new Explosion(p.x, p.y));
         }
     }
 
     public int[][] getActiveMap() {
         int[][] activeMap = new int[map.length][map[0].length];
 
         for (int i = 0; i < map.length; i++) {
             System.arraycopy(map[i], 0, activeMap[i], 0, map[i].length);
         }
 
         for (Enemy e : enemies) {
             activeMap[e.getY()][e.getX()] = -2;
         }
 
         for (Explosion e : explosions) {
             activeMap[e.getY()][e.getX()] = -3;
         }
 
         return activeMap;
     }
 
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
 
         for (int i = 0; i < bombs.size(); i++) {
             Bomb b = bombs.get(i);
             if (b.isDestroyed()) {
                 mapChanged = true;
                 explode(b.blowUp(map[0].length - 1, map.length - 1));
                 bombs.remove(i);
                 i--;
             }
         }
 
         playerAction(delegate.getInput(), activeMap);
 
         if (enemyMoveCounter == 10) {
             enemyMoveCounter = 0;
             for (int i = 0; i < enemies.size(); i++) {
                 Enemy e = enemies.get(i);
                 if (e.isDestroyed()) {
                     enemies.remove(i);
                     i--;
                     continue;
                 }
                 enemyAction(e, e.calcPath(player.getX(), player.getY(), activeMap), activeMap);
             }
         }
 
         if (mapChanged)
             delegate.setBackgroundImage(map);
 
         delegate.repaint();
     }
 
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
