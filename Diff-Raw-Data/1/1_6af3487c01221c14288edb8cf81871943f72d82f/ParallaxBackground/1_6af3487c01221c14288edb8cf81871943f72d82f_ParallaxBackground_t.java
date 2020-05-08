 package circlesvssquares;
 
 import org.jbox2d.common.Vec2;
 
 import processing.core.PConstants;
 import processing.core.PImage;
 
 public class ParallaxBackground {
 
     private static int LAYERS = 4;
     private static int BUILDINGS_PER_LAYER = 15;
 
     private static int MIN_BUILDING_HEIGHT = 100;
     private static int MAX_BUILDING_HEIGHT = 1000;
 
     private static int MIN_BUILDING_WIDTH = 60;
     private static int MAX_BUILDING_WIDTH = 120;
 
     class Building {
         int height, width;
         float xpos, ypos;
         private CirclesVsSquares app;
         public Building(CirclesVsSquares app) {
             this.app = app;
             height = (int)app.random(MIN_BUILDING_HEIGHT, MAX_BUILDING_HEIGHT);
             width = (int)app.random(MIN_BUILDING_WIDTH, MAX_BUILDING_WIDTH);
             xpos = app.random(-width/2, app.width + width/2);
             ypos = app.height;
         }
         
         void show(int depth) {
             app.noStroke();
             app.fill(64+(depth*32));
             app.rect(xpos, ypos + 500, width, height + 1000);
         }
         void update(float parallax, float dx, float dy) {
             xpos += -parallax * dx;
             ypos += -parallax * dy;
 
             if (xpos < 0 - width) {
                 xpos = app.width + width;
             } else if (xpos > app.width + width) {
                 xpos = 0 - width;
             }
         }
     }
 
     private CirclesVsSquares app;
     private PImage backgroundGradient;
     private Building buildings[] = new Building[BUILDINGS_PER_LAYER * LAYERS];
     private Vec2 lastPos;
 
     public ParallaxBackground(CirclesVsSquares app) {
         this.app = app;
         this.reset();
     }
 
     public void reset() {
         for (int i = 0; i < BUILDINGS_PER_LAYER * LAYERS; i++) {
             buildings[i] = new Building(app);
         }
         // create bg gradient
         backgroundGradient = this.app.createImage(app.width, app.height, PImage.RGB);
         for (int i = 0; i < backgroundGradient.pixels.length; i++) {
             int row = i / backgroundGradient.width;
             float color = (float)row / (float)backgroundGradient.height;
             backgroundGradient.pixels[i] = app.color(100.0f, 100.0f, 150.0f + color * 100.0f);
         }
        lastPos = null;
     }
 
     public void display(float screenWidth, float screenHeight, Vec2 playerPos) {
         Vec2 posChange = new Vec2(0, 0);
         if (this.lastPos != null) {
             posChange = playerPos.sub(this.lastPos);
         }
         this.lastPos = playerPos;
 
         this.display(screenWidth, screenHeight, posChange.x, posChange.y);
     }
 
     public void display(float screenWidth, float screenHeight, float dx, float dy) {
         this.app.pushStyle();
         this.app.rectMode(PConstants.CENTER);
         this.app.image(backgroundGradient, 0, 0);
 
         for (int i = 0; i < LAYERS; i++) {
             for (int j = BUILDINGS_PER_LAYER*i; j < BUILDINGS_PER_LAYER*(i+1); j++) {
                 buildings[j].show(i);
                 buildings[j].update(0.3f + i/15.0f, dx, dy);
             }
         }
         this.app.popStyle();
     }
 }
 
 
 
 
 
 
