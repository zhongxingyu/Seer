 package asteroids;
 import processing.core.*;
 import java.awt.*;
 import java.awt.geom.Rectangle2D;
 import java.util.Random;
 
 /**
  *
  * @author Zachary Stewart
  */
 public class Asteroid extends SpaceThing {
     int points;
     int generation;
     int color = canvas.color(255);
     int[] x;
     int[] y;
     int[] avgLocation;
     Random rand = new Random();
     Polygon poly;
 
    public Asteroid(PApplet papp, float speed, float initX, float initY, int initGeneration) {
         super(papp);
         locationX = initX;
         locationY = initY;
         generation = initGeneration;
         init();
     }
     
    public Asteroid(PApplet papp, float speed) {
         super(papp);
         generation = 3;
         locationX = (float) rand.nextInt(canvas.width);
         locationY = (float) rand.nextInt(canvas.height);
         init();
     }
 
     /**
      * This determines the size of the asteroid, based on its 'generation'
      * each one varies a little. Then, it generates 10-15 points for a
      * polygon, each varying a little. Unique asteroids FTW!
      */
     private void init() {
         rand = new Random();
         direction = rand.nextInt(360);
         switch (generation) {
             case 3:
                 size = rand.nextInt(10)+90;
                 break;
             case 2:
                 size = rand.nextInt(10)+40;
                 break;
             case 1:
                 size = rand.nextInt(10)+15;
                 break;
         }
         speed = (float) 2.0;
         points = rand.nextInt(5)+10;
         x = new int[points];
         y = new int[points];
         for (int i=0; i<points; i++) {
             x[i] = (int) ((locationX + rand.nextInt(20)-10 + size
                     * PApplet.cos(PApplet.radians(360/points*i))));
             y[i] = (int) ((locationY + rand.nextInt(20)-10 + size
                     * PApplet.sin(PApplet.radians(360/points*i))));
         }
         poly = new Polygon(x, y, points);
 
         bounds = poly.getBounds();
     }
 
     /**
      * Here I translate the entire polygon according to its direction.
      * when going of the screen at any side, it reappears on the opposite
      * side.
      */
     @Override
     public void update() {
         int dx;
         int dy;
 
         if(bounds.getMinX() > canvas.width) {
             dx = -canvas.width - (int) bounds.getWidth();
         } else if(bounds.getMaxX() < 0) {
             dx = canvas.width + (int) bounds.getWidth();
         } else {
             dx = (int) (speed*deltaX());
         }
 
         if(bounds.getMinY() > canvas.height) {
             dy = -canvas.height - (int) bounds.getHeight();
         } else if(bounds.getMaxY() < 0) {
             dy = canvas.height + (int) bounds.getHeight();
         } else {
             dy = (int) (speed*deltaY());
         }
         poly.translate(dx, dy);
         bounds = poly.getBounds();
     }
 
     /**
      * This loops through all points on the polygon and draws them as
      * a processing shape. Then it calls the update method.
      */
     @Override
     public void draw() {
         super.draw();
         canvas.noFill();
         canvas.stroke(color);
         canvas.beginShape();
         for(int i=0; i<poly.npoints; i++) {
             canvas.vertex(poly.xpoints[i], poly.ypoints[i]);
         }
         canvas.endShape(PApplet.CLOSE);
         update();
     }
 
 
     /**
      * This checks for Asteroid collisions with Ships and Bullets.
      * @param other
      * @return
      */
     public boolean collides(Object other) {
         if(other instanceof Bullet && poly.intersects(((Bullet) other).getBounds())) {
             return true;
         } else if ((other instanceof Nuke) && poly.intersects(((Nuke) other).getBounds())) {
             return true;
         } else if ((other instanceof Ship) && poly.intersects(((Ship) other).getBounds())) {
             return true;
         } else if (other instanceof Rectangle2D && poly.intersects(((Rectangle2D) other))) {
             return true;
         } else {
             return false;
         }
     }
 
     public boolean intersectsPoint(int xPoint, int yPoint) {
         return poly.contains(xPoint, yPoint);
     }
 
     @Override
     public void explode() {
         if(generation > 1) {
             createable = new SpaceThing[2];
             for(int i=0; i<createable.length; i++) {
                 createable[i] = new Asteroid(
                         canvas,
                        speed,
                         (float) bounds.getCenterX(),
                         (float) bounds.getCenterY(),
                         generation-1);
             }
             remove = true;
         } else {
             remove = true;
         }
     }
 
 }
