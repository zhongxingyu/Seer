 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Spawn;
 
 import GUIComponents.BaseFrame;
 import Path.ZigZagPath;
 import Weapon.*;
 import Unit.*;
 import java.awt.Color;
 import java.awt.Point;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Random;
 
 /**
  *
  * @author danielcecil
  */
 public class Spawn implements Serializable{
 
     private static int width = 800;
     private static int height = 600;
     private static int maxX, maxY;
     private static Point sp1, sp2, sp3, sp4, sp5, sp6, sp7;
     private static Point[] spawnPoints;
 
     public Spawn() {
         try {
             width = BaseFrame.getWindowSize().width;
             height = BaseFrame.getWindowSize().height;
         } catch (Exception e) {
             System.err.println(e);
         }
             spawnPoints = new Point[7];
             spawnPoints[0] = new Point(0, 100);
             spawnPoints[1] = new Point(0, 0);
             spawnPoints[2] = new Point((int) (width * 0.25), 0);
             spawnPoints[3] = new Point((int) (width * 0.5), 0);
             spawnPoints[4] = new Point((int) (width * 0.75), 0);
             spawnPoints[5] = new Point(width -1, 0);
             spawnPoints[6] = new Point(width -1, 100);
     }
 
     /**
      * setRandPosition method sets the Unit's x/y coordinates to a random position
      * within set bounds.
      * @param unit the Unit class to be modified
      * @return enemy at random location
      */
     public Point setRandPosition() {
         Random r = new Random();
         int randX = r.nextInt(width);
         // int randY = r.nextInt(maxY);
         int randY = r.nextInt(200);
         Point p = new Point(randX, randY);
         return p;
     }
 
     /**
      * newEnemy creates a new default unit at a random position
      * @return unit at random position
      */
     public Unit newDefaultUnit() {
         Unit en = new DefaultUnit(setRandPosition().x, setRandPosition().y);
         return en;
     }
 
     public Unit newEnemyUnit(Point spPoint) {
        Unit en = new Enemy(100, 100, new BasicWeapon(), new ZigZagPath(ZigZagPath.Direction.LEFT), 100, spPoint.x, spPoint.y, Color.cyan);
         return en;
     }
 
 
     public ArrayList<Unit> spawnAtLoc(int spawnPoint) {
         ArrayList<Unit> arr = new ArrayList();
             for (int i = 0; i < 8; i++) {
                 if (i == spawnPoint) {
                     arr.add(newEnemyUnit(spawnPoints[i])); 
             }
         }
         return arr;
     }
 
     /**
      * spawnRandomSame spawns the given number of units at a random point (all at the same point)
      * @param n - the number of units to be spawned
      * @return
      */
     public ArrayList<Unit> spawnRandomSame(int n){
         ArrayList<Unit> arr = new ArrayList();
         Random r = new Random();
         int point = r.nextInt(7);
         for(int i = 0; i < n; i++){
             arr.add(spawnAtLoc(point).get(0));
         }
         return arr;
     }
     
     /**
      * spawnRandom spawns the given number of units at random points (all different)
      * @param n - the number of units to be spawned
      * @return
      */
         public ArrayList<Unit> spawnRandom(int n){
         ArrayList<Unit> arr = new ArrayList();
         Random r = new Random();
         for(int i = 0; i < n; i++){
             arr.add(spawnAtLoc(r.nextInt(7)).get(0));
         }
         return arr;
     }
 
     /**
      * spawnAtSame method spawns the given number of units to be spawned at the given position
      * @param n - the number of units to be spawned
      * @param position - the position (0-6) of where to spawn the units
      * @return
      */
         public ArrayList<Unit> spawnAtSame(int n, int position){
         ArrayList<Unit> arr = new ArrayList();
         for(int i = 0; i < n; i++){
             arr.add(spawnAtLoc(position).get(0));
         }
         return arr;
     }
 }
