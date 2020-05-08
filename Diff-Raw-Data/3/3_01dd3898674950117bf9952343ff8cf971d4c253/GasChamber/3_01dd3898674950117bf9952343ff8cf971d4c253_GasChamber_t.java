 /**
  * Programmers: Ed Broxson & Chase McCowan 
  * Date: 02/20/2013 
  * Purpose: Create and draw multiple particles for use 
  *          in Chemistry Diffusion example.
  */
 package layout;
 
 import processing.core.PApplet;
 import processing.core.PFont;
 import processing.core.PVector;
 
 public class GasChamber extends PApplet {
 
     int numParts = 40;
     Element[] particles = new Element[numParts];
     int[] color1 = new int[3];
     int[] color2 = new int[3];
     float x, y;
     float diameter;
     boolean gateOpen = false;
     int mw1, mw2;
     double time1, time2;
     String gas1, gas2;
     PFont myFont, delinFont;
     boolean bStop;
         
 
     /**
      * method to create size of sketch and particles
      */
     @Override
     public void setup() {
         size(775, 200);
         myFont = createFont("sans-serif", 24);
         delinFont = createFont("sans-serif", 14);
     }
 
     /**
      * method to draw particles, runs continously
      */
     @Override
     public void draw() {
 //        frameRate(31);  // AEB another way to change the speed of particles,
                           // may be usefull in figuring out the particles
                           // bouncing beyond Gate Line after a minute or two in the box
         
         int gateX = 200;
         int gateY = 100;
         int finishX = 750;
         int finishY = 100;
         int delinX = 220;
         int delinY = 0;
 
         background(170);
 
         if (!gateOpen) {
             line(200, 0, 200, height);
             line(200, 25, 225, 25);
             line(225, 25, 225, 175);
             line(200, 175, 225, 175);
 
             textFont(myFont);
             textAlign(CENTER, BOTTOM);
             pushMatrix();
             fill(0, 0, 255);
             translate(gateX, gateY);
             rotate(HALF_PI);
             text("Gate", 0, 0);
             popMatrix();
         }
 
         textFont(myFont);
         textAlign(CENTER, BOTTOM);
         pushMatrix();
        fill(0, 0, 255);
         translate(finishX, finishY);
         rotate(HALF_PI);
         text("Finish Line", 0, 0);
         popMatrix();
         
         textFont(delinFont);
         textAlign(CENTER, TOP);
         pushMatrix();
        fill(0, 0, 255);
         translate(delinX, delinY);
         text("-------------------------------------------------- 50 Meters ---------------------------------------------------", 265, 0);
         popMatrix();
 
         for (int i = 0; i < numParts; i++) {
             particles[i].update();
             particles[i].checkEdges();
         }
         stroke(0);
         fill(175);
 
         for (int j = 0; j < particles.length; j++) {
             x = particles[j].location.x;
             y = particles[j].location.y;
             diameter = particles[j].diameter;
             
 
             if (j % 2 == 0) {
                 fill(color1[0], color1[1], color1[2]);
                 ellipse(x, y, diameter, diameter);
             } else {
                 fill(color2[0], color2[1], color2[2]);
                 ellipse(x, y, diameter, diameter);
             }
         }
     }
 
     /**
      * method to pause and restart the drawing of this PApplet
      */
     @Override
     public void keyPressed() {
         bStop = !bStop;
         if (bStop) {
             noLoop();
         } else {
             loop();
         }
     }
 
     /**
      * method to fill the start box with 20 of each element to be used in
      * simulation
      */
     public void particleFill(int part1, int part2) {
 
         float vel1 = 0;
         float diam1 = 0;
         float vel2 = 0;
         float diam2 = 0;
         float rate = 2;
 
         switch (part1) {
             case 0:
                 vel1 = rate;
                 diam1 = 2;
                 mw1 = 4;
                 time1 = 1;
                 gas1 = "Helium";
                 color1[0] = 0;  // black
                 color1[1] = 0;
                 color1[2] = 0;
                 break;
             case 1:
                 vel1 = rate / sqrt(5);
                 diam1 = 5;
                 mw1 = 20;
                 time1 = 1;
                 gas1 = "Neon";
                 color1[0] = 0;  // green
                 color1[1] = 255;
                 color1[2] = 0;
                 break;
             case 2:
                 vel1 = rate / sqrt(10);
                 diam1 = 6;
                 mw1 = 40;
                 time1 = 1;
                 gas1 = "Argon";
                 color1[0] = 255;  // red
                 color1[1] = 0;
                 color1[2] = 0;
         }
 
         switch (part2) {
             case 0:
                 vel2 = rate;
                 diam2 = 2;
                 mw2 = 4;
                 time2 = 1;
                 gas2 = "Helium";
                 color2[0] = 255;  // white
                 color2[1] = 255;
                 color2[2] = 255;
                 break;
             case 1:
                 vel2 = rate / sqrt(5);
                 diam2 = 5;
                 mw2 = 20;
                 time2 = 1;
                 gas2 = "Neon";
                 color2[0] = 0;  // dk green
                 color2[1] = 100;
                 color2[2] = 0;
                 break;
             case 2:
                 vel2 = rate / sqrt(10);
                 diam2 = 6;
                 mw2 = 40;
                 time2 = 1;
                 gas2 = "Argon";
                 color2[0] = 100;  // dk red
                 color2[1] = 0;
                 color2[2] = 0;
                 break;
             case 3:
                 vel2 = rate / sqrt(20);
                 diam2 = 9;
                 mw2 = 80;
                 time2 = 1;
                 gas2 = "Unknown1";
                 color2[0] = 255;  // purple
                 color2[1] = 0;
                 color2[2] = 255;
                 break;
             case 4:
                 vel2 = rate / sqrt(16);
                 diam2 = 7;
                 mw2 = 64;
                 time2 = 1;
                 gas2 = "Unknown2";
                 color2[0] = 255;  // yellow
                 color2[1] = 255;
                 color2[2] = 0;
                 break;
             case 5:
                 vel2 = rate / sqrt(4);
                 diam2 = 4;
                 mw2 = 16;
                 time2 = 1;
                 gas2 = "Unknown3";
                 color2[0] = 0;  // blue
                 color2[1] = 0;
                 color2[2] = 255;
         }
 
         for (int i = 0; i < numParts; i++) {
             if (i % 2 == 0) {
                 particles[i] = new Element(random(0, 180), random(0, height), vel1, diam1, i, particles);
             } else {
                 particles[i] = new Element(random(0, 180), random(0, height), vel2, diam2, i, particles);
             }
         }
     }
 
     /**
      * method to remove gate from display and start race
      */
     public void setGateOpen(boolean gateOpen) {
         this.gateOpen = gateOpen;
         Element.setGateOpen(gateOpen);
     }
 }
