 package twoverse.object.applet;
 
 import processing.core.PApplet;
 import twoverse.object.Star;
 import twoverse.util.Point.TwoDimensionalException;
 
 @SuppressWarnings("serial")
 public class AppletStar extends Star implements AppletBodyInterface {
     private PApplet mParent;
 
     public AppletStar(PApplet parent, Star star) {
         super(star);
         mParent = parent;
     }
 
     public void display() throws TwoDimensionalException {
         if(getState() == 1) {
             drawPulsar();
         } else if(getState() == 2) {
             drawBlackHole();
         } else if(getState() == 3) {
             drawSupernova();
         } else if(getState() == 4) {
             drawInert();
         } else {
             drawFormation();
         }
     }
 
     private void drawPulsar() {
         // TODO
         drawFormation();
     }
 
     private void drawSupernova() {
         // TODO
         drawFormation();
     }
 
     private void drawInert() {
         // TODO
         drawFormation();
     }
 
     private void drawBlackHole() {
         // TODO
         drawFormation();
     }
 
     private void drawFormation() {
         mParent.pushMatrix();
         mParent.noStroke();
         try {
             mParent.translate((float) getPosition().getX(),
                     (float) getPosition().getY(),
                     (float) getPosition().getZ());
         } catch(TwoDimensionalException e) {
         }
         mParent.fill((float) getColorR(),
                 (float) getColorG(),
                 (float) getColorB());
         mParent.ellipse(0, 0, (float) getRadius(), (float) getRadius());
 
         for(int i = 2; i < 25; i++) {
             mParent.fill((float) getColorR(),
                     (float) getColorG(),
                     (float) getColorB(),
                     (float) (255.0 / i/2.0));
             mParent.ellipse(0, 0, (float) getRadius() + i, (float) getRadius()
                     + i);
         }
         mParent.popMatrix();
     }
 }
