 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Game;
 
 import Projectile.Projectile;
 import Spawn.Spawn;
 import Unit.Unit;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 
 /**
  *
  * @author michal
  */
 public class Game {
 
     /*
      * This kind of this is probably necessary - can store all of the stuff that
      * is on the screen - projectiles, ships and whatever else, and then get this
      * list and use it to draw all of the objects on the screen with whichever 
      * thing is going to be doing that - most likely a panel. GameObject is just
      * some way of referring to all of the stuff. We could always have two different
      * arrays for projectiles and ships, and grab them independently.
      */
     ArrayList<Unit> units;
     ArrayList<Projectile> projectiles;
     ArrayList<Spawn> spawns;
 
     public Game() {
         units = new ArrayList<Unit>();
         projectiles = new ArrayList<Projectile>();
         spawns = new ArrayList<Spawn>();
     }
 
     /**
      * Prunes the unit and projectile arrays to remove objects that are no longer
      * in the field of play.
      * @param frameSize The current size of the game frame.
      */
     public void pruneArrays(Dimension frameSize) {
         // TODO Decide on some kind of range outside the frame in which we keep objects
         int height = frameSize.height;
         int width = frameSize.width;
         System.out.println(height);
         System.out.println(width);
         pruneUnitArray(height, width);
         pruneProjectileArray(height, width);
     }
 
     public void doNaiveCollisionDetection(){
         long time = System.currentTimeMillis();
         Rectangle2D projBound;
         Rectangle2D unitBound;
        ArrayList<Projectile> toRemoveProj = new ArrayList<Projectile>();
        ArrayList<Unit> toRemoveUnit = new ArrayList<Unit>();
         for (Unit unit : units) {
             Point uPoint = unit.getLocation();
             unitBound = getCenteredBox(uPoint);
             for (Projectile p : projectiles) {
                 Point projPoint  = p.getLocation();
                 projBound = getCenteredBox(projPoint);
                 if (projBound.intersects(unitBound)){
                    toRemoveProj.add(p);
                    toRemoveUnit.add(unit);
                 }
             }
         }
        for (Unit unit : toRemoveUnit) {
            removeUnitFromArray(unit);
        }
        for (Projectile p : toRemoveProj) {
            removeProjectileFromArray(p);
        }
         long timeTaken = time - System.currentTimeMillis();
         if (timeTaken >= 20){
             System.out.printf("WARNING: COLLISION DETECTION TOOK %d MILLISECONDS!\n", timeTaken);
         }
     }
 
     /**
      * Gets a box of 5x5 pixels with the point passsed as the centre.
      * @param centre
      */
     public Rectangle2D getCenteredBox(Point centre){
          return new Rectangle2D.Double(centre.getX() - 2.5, centre.getY() - 2.5, 5, 5);
     }
 
     /**
      * Removes any units in the array that are outside a rectangle of the height
      * and width provided.
      * @param height Height of the rectangle.
      * @param width Width of the rectangle.
      */
     public void pruneUnitArray(int height, int width) {
         Rectangle2D bounds = new Rectangle2D.Double(0, 0, width, height);
         ArrayList<Unit> toRemove = new ArrayList<Unit>();
         for (Unit unit : units) {
             if (!bounds.contains(unit.getLocation())) {
                 toRemove.add(unit);
             }
         }
         for (Unit unit : toRemove) {
             units.remove(unit);
         }
     }
 
     public void pruneProjectileArray(int height, int width) {
         Rectangle2D bounds = new Rectangle2D.Double(0, 0, width, height);
         ArrayList<Projectile> toRemove = new ArrayList<Projectile>();
         for (Projectile projectile : projectiles) {
             if (!bounds.contains(projectile.getLocation())) {
                 toRemove.add(projectile);
             }
         }
         for (Projectile projectile : toRemove) {
             projectiles.remove(projectile);
         }
     }
 
     /**
      * Adds a unit to the units array.
      * @param addUnit The unit to add to the array.
      */
     public void addUnitToArray(Unit addUnit) {
         units.add(addUnit);
     }
     /**
      * Removes a unit from the array at the index specified
      * @param index
      */
     public void removeUnitfromArray(int index){
         units.remove(index);
     }
 
     /**
      * Removes a specified unit from the array.
      * @param unit
      */
     public void removeUnitFromArray(Unit unit){
         units.remove(unit);
     }
 
     public void removeProjectileFromArray(Projectile p){
         projectiles.remove(p);
     }
 
     public void removeProjectileFromArray(int index){
         projectiles.remove(index);
     }
 
     /**
      * Adds a new projectile to the projectiles array.
      * @param addProjectile Projectile to add the the array.
      */
     public void addProjectileToArray(Projectile addProjectile) {
         projectiles.add(addProjectile);
     }
 
     /**
      * Returns the list of units currently present in the game.
      * @return Arraylist of units.
      */
     public ArrayList<Unit> getUnitArray() {
         return units;
     }
 
     /**
      * Returns the list of projectiles currently present in the game.
      * @return Arraylist of projectiles.
      */
     public ArrayList<Projectile> getProjectileArray() {
         return projectiles;
     }
 
     /**
      * Returns the unit array size to be used in the game panel class
      * @return
      */
     public int getUnitArrayLength() {
         return units.size();
     }
 
     public void drawUnitArray(Graphics2D g2) {
         for (int i = 0; i < getUnitArrayLength(); i++) {
             getUnitArray().get(i).draw(g2);
         }
     }
 
     public void drawProjectileArray(Graphics2D g) {
         Graphics2D g_ = (Graphics2D) g;
         for (int i = 0; i < getProjectileArray().size(); i++) {
             getProjectileArray().get(i).draw(g_);
         }
     }
 }
