 package asteroids;
 import java.awt.geom.Rectangle2D;
 import processing.core.*;
 
 /**
  *
  * @author Zachary Stewart
  */
 class Ship extends SpaceThing {
     private boolean accelerating;
     private boolean rotatingLeft;
     private boolean rotatingRight;
     private int nukes;
     public int incShips;
     
     public Ship(PApplet papp) {
         super(papp);
         locationX = (float) canvas.width/2;
         locationY = (float) canvas.height/2;
 
         speed = (float) 0.0;
         size = 10;
         bounds = new Rectangle2D.Float(locationX, locationY, size*2, size*2);
     }
 
     @Override
     public void draw() {
         update();
         super.draw();
         if(explode) canvas.stroke(255, 0, 0);
         else canvas.stroke(255);
         canvas.fill(255);
         if (rotatingLeft) this.rotate((float) 5.0);
         if (rotatingRight) this.rotate((float) -5.0);
 
 
         canvas.pushMatrix();
         canvas.translate(locationX, locationY);
         canvas.rectMode(PApplet.CENTER);
         canvas.rotate(PApplet.radians(direction));
         canvas.fill(0);
         canvas.stroke(200,200,255);
         canvas.triangle(0, -size, -size/3*2, size, size/3*2, size);
         if (accelerating) drawJet();
         canvas.popMatrix();
     }
 
     @Override
     public void update() {
         super.update();
         if(accelerating) {
             if (speed < 4) {
                 speed += .1;
             }
             deltaX += PApplet.cos(PApplet.radians(direction-90)) * 0.1;
             deltaY += PApplet.sin(PApplet.radians(direction-90)) * 0.1;
         } else {
             if(speed > 0) {
                 speed -= 0.01;
             }
         }
         if(bounds.getMinX() > canvas.width) {
             locationX = (float) 0.0;
         } else if(bounds.getMaxX() < 0) {
             locationX = (float) canvas.width;
         } else {
             locationX += deltaX;
         }
         if(bounds.getMinY() > canvas.height) {
             locationY = (float) 0.0;
         } else if(bounds.getMaxY() < 0) {
             locationY = (float) canvas.height;
         } else {
             locationY += deltaY;
         }
     }
 
     public int getNukes() {
         return nukes;
     }
 
     private float shipCos() {
         return (float) PApplet.cos(PApplet.radians(direction));
     }
 
     private float shipSin() {
         return (float) PApplet.sin(PApplet.radians(direction));
     }
 
     private float shipAngle() {
         return (float) PApplet.radians(direction);
     }
 
     private void drawJet() {
         canvas.stroke(255,0,0);
         canvas.triangle(0, 16, -3, 10, 3, 10);
     }
 
     private void rotate(Float angle) {
         direction -= angle;
         direction = direction % 360;
     }
 
     public void setAccelerating(boolean moving) {
         if(moving) accelerating = true;
         else accelerating = false;
     }
 
     public void setRotatingLeft(boolean rotating) {
         if(rotating) rotatingLeft = true;
         else rotatingLeft = false;
     }
 
     public void setRotatingRight(boolean rotating) {
         if(rotating) rotatingRight = true;
         else rotatingRight = false;
     }
 
     public void firePrimary() {
         createable = new SpaceThing[1];
         createable[0] = new Bullet(canvas, locationX+12*shipSin(), locationY-12*shipCos(), direction, Math.abs(deltaX) + Math.abs(deltaY));
     }
 
     public void fireNuke() {
        if(nukes > 1) {
             createable = new SpaceThing[1];
             createable[0] = new Nuke(canvas, locationX+12*shipSin(), locationY-12*shipCos(), direction, canvas.mouseX, canvas.mouseY);
             nukes -= 1;
         }
     }
 
     public void fireNuke(SpaceThing target) {
         if(nukes > 0) {
             createable = new SpaceThing[1];
             createable[0] = new Nuke(canvas, locationX+12*shipSin(), locationY-12*shipCos(), direction, target);
             nukes -= 1;
         }
     }
 
     @Override
     public void explode() {
         incShips = -1;
         createable = new SpaceThing[2];
         createable[0] = new Ship(canvas);
         createable[1] = new Debris(canvas, locationX, locationY);
         remove = true;
     }
 
     void addPowerUp(PowerUp powerUp) {
         if (powerUp.getType() == NUKE) {
             nukes += 1;
         } else if (powerUp.getType() == SHIP) {
             incShips = 1;
         }
     }
 
 
 }
