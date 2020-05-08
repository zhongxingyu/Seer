 package jp.angelforest.arrow.particlesystem;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import jp.arrow.angelforest.engine.core.AngelForest2DEngine;
 import jp.arrow.angelforest.engine.core.SquarePolygon;
 
 public class Particle {
     public static int PARTICLE_TYPE_SQUARE = 0;
     public static int PARTICLE_TYPE_2SQUARESTAR = 1;
 
     public static int PARTICLE_STATUS_INIT = 0;
     public static int PARTICLE_STATUS_FINISHED = 1;
 
     //current status
     private int size;
     private int angle;
     private int x;
     private int y;
     private int alpha = AngelForest2DEngine.one;
     private int velocity;
 
     //per step
     private double vx;
     private double vy;
     private double vangle;
     private int valpha;
     private int step;
 
     //goals
     private int gx;
     private int gy;
     private int gangle;
 
     private int type;
 
     private int status = PARTICLE_STATUS_INIT;
 
     private SquarePolygon square1;
     private SquarePolygon square2;
 
     private int[] white;
 
     public Particle(int size) {
         white = new int[] {
                 AngelForest2DEngine.one, AngelForest2DEngine.one, AngelForest2DEngine.one, AngelForest2DEngine.one, //3
                 AngelForest2DEngine.one, AngelForest2DEngine.one, AngelForest2DEngine.one, AngelForest2DEngine.one, //7
                 AngelForest2DEngine.one, AngelForest2DEngine.one, AngelForest2DEngine.one, AngelForest2DEngine.one, //11
                 AngelForest2DEngine.one, AngelForest2DEngine.one, AngelForest2DEngine.one, AngelForest2DEngine.one  //15
         };
         this.size = size;
         velocity = 3;
         square1 = new SquarePolygon(white);
         square2 = new SquarePolygon(white);
     }
 
     public void draw(GL10 gl) {
         if(status != PARTICLE_STATUS_FINISHED) {
             square1.draw(x, y, size, size, angle);
             square2.draw(x, y, size, size, angle+45);
         }
     }
 
     public void move() {
         if(x != gx || y != gy) {
             x += vx;
             y += vy;
             angle += vangle;
             changeAlpha();
         }
         //esle delete it
         else {
             status = PARTICLE_STATUS_FINISHED;
         }
     }
 
     private void changeAlpha(){
        white[3] /= valpha;
        white[7] /= valpha;
        white[11] /= valpha;
        white[15] /= valpha;

         square1.changeColor(white);
         square2.changeColor(white);
     }
 
     public void setGoal(int gx, int gy, int gangle) {
         this.gx = gx;
         this.gy = gy;
         this.gangle = gangle;
 
         double rad = Math.atan2(gy-y, gx-x);
         double frad = (rad+2*Math.PI)%(2*Math.PI);
         vx = velocity*Math.cos(frad);
         vy = velocity*Math.sin(frad);
 
         //get step
         step = (int)(Math.abs(gx-x)/vx);
         vangle = (gangle-angle)/step;
         valpha = AngelForest2DEngine.one/step;
     }
 
     public int getX() {
         return x;
     }
 
     public void setX(int x) {
         this.x = x;
     }
 
     public int getY() {
         return y;
     }
 
     public void setY(int y) {
         this.y = y;
     }
 
     public int getAngle() {
         return angle;
     }
 
     public void setAngle(int angle) {
         this.angle = angle;
     }
 }
