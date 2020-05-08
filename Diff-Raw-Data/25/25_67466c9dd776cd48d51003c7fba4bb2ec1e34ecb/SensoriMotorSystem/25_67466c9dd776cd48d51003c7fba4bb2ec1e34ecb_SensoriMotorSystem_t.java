 package com.beartronics.jschema;
 // Basde on code examples from The Nature of Code
 // <http://www.shiffman.net/teaching/nature>
 // Spring 2011
 // PBox2D example
 
 // Basic example of falling rectangles
 
 import pbox2d.*;
 import org.jbox2d.collision.shapes.*;
 import org.jbox2d.common.*;
 import org.jbox2d.dynamics.*;
 import java.util.*;
 
 import org.jbox2d.dynamics.joints.*;
 import org.jbox2d.collision.shapes.Shape;
 
 import processing.core.*;
 
 
 public class SensoriMotorSystem {
 
 
     public JSchema app;
 
     // Object planes
     public Plane plane0; // back plane
     public Plane plane1; // front plane
 
     int WORLD_WIDTH = 4096;
     int WORLD_HEIGHT = 800;
 
     // Position of body in world
     public float xpos;
     public float ypos;
 
     float bodyWidth = 600;
     float bodyHeight = 400;
 
     // horizontal scroll speed for debugging
     float scrollspeed = 25.0f;
 
     ////////////////////////////////////////////////////////////////
     // Head and Eyes Controls
 
     // Computed from head gross and fine angles
     public float gazeXpos = 0;
     public float gazeYpos = 0;
     
     int headAzimuthAngle = 0;
     int headElevationAngle = 0;
 
     public Hand hand1;
     public Hand hand2;
 
     // max number of gross and fine motor steps that arms can take
     public int reachX = 10;
     public int reachY = 10;
     // arm motor step size
     public int dGross = 100;
     public int dFine = 10;
 
     public SensoriMotorSystem(JSchema a) {
         this.app = a;
         System.out.println("SensoriMotorSystem constructor this.app = "+this.app);
     }
 
     Object2D findObj(int index) {
         for (Plane plane: planes) {
             Object2D o = plane.findObj(index);
             if (o != null) { return o; }
         }
         return null;
     }
 
     Object2D findObjAt(float x, float y) {
         for (Plane plane: planes) {
             Object2D o = plane.findObjAt(x, y);
             if (o != null) { return o; }
         }
         return null;
     }
 
 
     PFont font;
     ArrayList<Plane> planes = new ArrayList<Plane>();
     Plane currentPlane;
 
     int marker_color;
 
     void setupDisplay() {
         // Initial body position
         xpos = app.width/2;
         ypos = app.height/2;
 
 
         // Initialize box2d physics and create the world
         plane0 = new Plane(app, app.color(255, 55, 55));
         plane1 = new Plane(app, app.color(0,0,0));
 
         marker_color = app.color(189,22,198,128);
         // Initialize box2d physics and create the world
         planes.add(plane0);
         planes.add(plane1);
 
         currentPlane = plane1;
         
         for (Plane plane: planes) {
             plane.setup();
         }
 
 
         app.smooth();
 
         font = app.createFont("Monospaced", 12);
         app.textFont(font);
 
         initialBoundaries(plane0);
         initialBoundaries(plane1);
 
         makeNavigationMarkers(plane0);
 
         initialPhysobjs(plane1);
 
         // hands start out in plane1
         hand1 = plane1.addHand( xpos, ypos, 32, 32, 5, app.color(0,255,0));
         hand2 = plane1.addHand( xpos, ypos, 32, 32, 5, app.color(255,0,0));
         hand1.alpha = 255;
         hand2.alpha = 255;
         hand1.hjog(-1,0);
         hand2.hjog(1,0);
 
         hand1.updatePosition(xpos,ypos);
         hand2.updatePosition(xpos,ypos);
 
 
         plane0.addBox(800, 100, 100, 100, 4, app.color(255,40,30));
         plane0.addBox(1000, 100, 50, 50, 8, app.color(87,191,22));
 
         worldState = new WorldState();
 
         // move body into initial position
         moveBody(xpos,ypos);
 
     }
 
     void setTranslations(float x, float y) {
         for (Plane plane: planes) {
             plane.setTranslation(x,y);
         }
     }
 
 
     void initialBoundaries(Plane p) {
         // Add a bunch of fixed boundaries
         // floor
         p.addBoundary(WORLD_WIDTH/2, app.height-5, WORLD_WIDTH, 10f );
         // left wall
         p.addBoundary(5,           app.height-200, 10f, 1200f );
         // right wall
         p.addBoundary(WORLD_WIDTH-5, app.height-200, 10f, 1200f );
 
 
         p.addBoundary(100, app.height-50, 10f, 100f );
         p.addBoundary(160, app.height-50, 10f, 100f );
         p.addBoundary(900, app.height-80, 10f, 160f );
         p.addBoundary(1200, app.height-100, 10f, 200f );
     }
 
     void makeNavigationMarkers(Plane p) {
         // add markers to help locate position
 
         for (int x = 200; x < WORLD_WIDTH; x+=200) {
             p.addBoundary(x, app.height-400, 20+x/100f, 20+x/50f, marker_color );
         }
     }
 
     void initialPhysobjs(Plane p) {
         int bottom = app.height;
         p.addBox(500, bottom -10, 64, 64, 1);
         p.addBox(500, bottom-10, 64, 64, 2);
         p.addBox(800, bottom-10, 32, 32, 2);
         p.addBox(1200, bottom-10, 64, 64, 2);
         p.addBox(1500, bottom-10, 64, 64, 1);
         p.addBox(2000, bottom-10, 64, 64, 10);
         p.addBox(300, bottom-200, 200, 5, 6);
         p.addBox(300, bottom-200, 400, 5, 6);
         p.addBall(1000, bottom-100, 40);
         p.addBall(200, bottom-100, 40);
         p.addBall(250, bottom-100, 40);
         p.addBall(500, bottom-100, 30);
         p.addCustomShape1(50,bottom-100,app.color(123,201,122));
     }
 
     String showHandInfo(Hand h) {
         Vec2 f = h.getJointForce();
         String info = String.format("grossX=%.1f,%.1f fineX=%.1f,%.1f F=(%.1f, %.1f) %s",
                                     h.grossX, h.grossY,
                                     h.fineX, h.fineY,
                                     f.x*100, f.y*100,
                                     h.touchString());
         return info;
     }
 
 
     void draw() {
 
         app.rectMode(PConstants.CORNER);
         app.background(255);
         app.fill(0);
 
         app.text("alt-click to create box, click to grasp, ctrl-click to lift, left and right arrow to rotate, shift for transparent", 20,12);
         app.text("plane="+planes.indexOf(currentPlane), 20,22);
         app.text("xpos="+xpos+ "   ypos="+ypos,20,32);
         app.text("hand1 "+showHandInfo(hand1),20,42);
         app.text("hand2 "+showHandInfo(hand2),20,52);
         app.text("gazeX="+gazeXpos+" gazeY="+gazeYpos, 20, 62);
 
 
         for (Plane plane: planes) {
             plane.draw();
         }
 
 
         // draw viewport and gaze location
         drawViewPort();
 
         computeWorldState();
 
     }
 
     // screen is always drawn such that body is located at horizontal center
     void drawViewPort() {
         // draw where the gaze is centered
         float dx, dy;
         dx = gazeXpos;
         dy = gazeYpos;
         float cx = app.width/2;
         float cy = ypos;
 
         app.pushMatrix();
         app.pushStyle();
         app.rectMode(PConstants.CENTER);
 
 
         // draw yellow circle at body (xpos,ypos)
         app.strokeWeight(3);
         app.stroke(177,177,102);
         app.noFill();
         app.rect(cx, ypos, 40, 40);
         app.ellipse(cx,ypos,20,20);
         app.strokeWeight(1);
         // draw X at gaze position
         app.stroke(app.color(128,128,128,200));
         app.line(cx + dx - 50, cy-dy-50,
                  cx + dx + 50, cy-dy+50);
         app.line(cx + dx + 50, cy-dy-50,
                  cx + dx - 50, cy-dy+50);
         
 
         // draw the max range the hands can move
         app.rect(cx, ypos, reachX*dGross, reachY*dGross);
         app.popStyle();
         app.popMatrix();
 
 
         
 
 
     }
 
 
     int downKeys[] = new int[1024];
 
     Plane nextPlane() {
         int idx = planes.indexOf(currentPlane);
         idx = (idx+1) % planes.size();
         return planes.get(idx);
     }
 
     Plane prevPlane() {
         int idx = planes.indexOf(currentPlane);
         idx = (idx-1);
         if (idx < 0) {
             idx = planes.size()-1;
         }
         return planes.get(idx);
     }
 
     final int MAX_HAND_FORCE = 100;
 
     /** Moves the body position.
      * checks the hand forces, and if too large, will not move the body, thus preventing
      * moving too far from the hands
      */
     void jogBody(float dx, float dy) {
         Vec2 f1 = hand1.getJointForce();
         Vec2 f2 = hand2.getJointForce();
         if (dx < 0) {
             if ((f1.x > -MAX_HAND_FORCE) && (f2.x > -MAX_HAND_FORCE)) {
                 xpos += dx;
             } else {
                 app.println(String.format("HAND X FORCE h1x=%f h2x=%f TOO LARGE, CANNOT MOVE BODY dx=%f", f1.x, f2.x, dx));
             }
         }
 
         if (dx > 0) {
             app.println(String.format("** dx=%f f1.x=%f f2.x=%f", dx, f1.x, f2.x));
             if ((f1.x < MAX_HAND_FORCE) && (f2.x < MAX_HAND_FORCE)) {
                 xpos += dx;
             } else {
                 app.println(String.format("HAND X FORCE h1x=%f h2x=%f TOO LARGE, CANNOT MOVE BODY dx=%f", f1.x, f2.x, dx));
             }
         }
 
 
         if (dy < 0) {
             if ((f1.y > -MAX_HAND_FORCE) && (f2.y > -MAX_HAND_FORCE)) {
                 ypos += dy;
             } else {
                 app.println(String.format("HAND Y FORCE h1y=%f h2y=%f TOO LARGE, CANNOT MOVE BODY dy=%f", f1.y, f2.y, dy));
             }
         }
 
         if (dy > 0) {
             if ((f1.y < MAX_HAND_FORCE) && (f2.y < MAX_HAND_FORCE)) {
                 ypos += dy;
             } else {
                 app.println(String.format("HAND Y FORCE h1y=%f h2y=%f TOO LARGE, CANNOT MOVE BODY dy=%f", f1.y, f2.y, dy));
             }
         }
 
         moveBody(xpos, ypos);
     }
 
     void moveBody(float x, float y) {
         xpos = x;
         xpos = (float)Math.max(0, xpos);
         xpos = (float)Math.min(WORLD_WIDTH,xpos);
 
         ypos = y;
         ypos = (float)Math.min(WORLD_HEIGHT,ypos);
         ypos = (float)Math.max(0,ypos);
 
         hand1.updatePosition(xpos,ypos);
         hand2.updatePosition(xpos,ypos);
 
         setTranslations(xpos,ypos);
     }
 
     public void keyPressed() {
         downKeys[app.keyCode] = 1;
         if (app.keyCode == PConstants.LEFT) {
             jogBody(-scrollspeed,0);
         } else if (app.keyCode == PConstants.RIGHT) {
             jogBody(scrollspeed,0);
         } else if (app.keyCode == PConstants.UP || app.keyCode == PConstants.DOWN) {
             // If we're interactively grasping an object with the mouse, move it to next plane
             Plane next = app.keyCode == PConstants.UP ? nextPlane() : prevPlane();
             if (currentPlane.pickedThing != null) {
                 Object2D obj = currentPlane.pickedThing;
                 currentPlane.mouseDropObject();
                 obj.moveToPlane(next);
                 next.mouseGraspObject(obj);
             }
             currentPlane = next;
         }
         currentPlane.keyPressed();
         if (app.keyCode == PConstants.SHIFT) {
             for (Plane plane: planes) {
                 plane.setTransparent(true);
             }
         }
 
 
     }
         
     public void keyReleased() {
         downKeys[app.keyCode] = 0;
         currentPlane.keyReleased();
         if (app.keyCode == PConstants.SHIFT) {
             for (Plane plane: planes) {
                 plane.setTransparent(false);
             }
         }
 
     }
     
     boolean isKeyDown(int k) {
         return downKeys[k] == 1;
     }
 
     void mouseReleased() {
         currentPlane.mouseReleased();
     }
 
     void mousePressed() {
         currentPlane.mousePressed();
     }
 
     WorldState worldState;
 
     public WorldState getWorldState() {
         return worldState;
     }
 
     /** Reads primitive actions from worldstate and performs them.
      */
     public void processActions(WorldState w) {
        //app.println("SensoriMotorSystem.processActions() not yet implemented");
     }
 
     public int sensorID = 0;
 
     /// Fills in the sensory input values
     public WorldState computeWorldState() {
         sensorID = 0;
         computeTouchSensors();
         computeVisionSensor();
         computeAudioSensors();
         
         return worldState;
     }
 
     void computeAudioSensors() {
     }
 
     boolean isObjectAtGaze() {
         Object2D obj = findObjAt(xpos+gazeXpos,ypos+gazeYpos);
         return (obj != null);
     }
 
 
     boolean isSolidObjectAtGaze() {
         Object2D obj = findObjAt(xpos+gazeXpos,ypos+gazeYpos);
         if (obj == null) {
             return false;
         } else {
             return obj.isSolid();
         }
     }
 
     boolean isHollowObjectAtGaze() {
         Object2D obj = findObjAt(xpos+gazeXpos,ypos+gazeYpos);
         if (obj == null) {
             return false;
         } else {
             return obj.isHollow();
         }
     }
 
     Object2D objectAtGaze() {
         return findObjAt(xpos+gazeXpos,ypos+gazeYpos);
     }
 
     void computeVisionSensor() {
         // is fovea seeing a solid object?
         worldState.setSensorInput("vision.fovea.object", sensorID++, isObjectAtGaze());
         worldState.setSensorInput("vision.fovea.solid_object", sensorID++, isSolidObjectAtGaze());
         worldState.setSensorInput("vision.fovea.hollow_object", sensorID++, isHollowObjectAtGaze());
 
         for (int x = -5; x < 6; x++) {
             for (int y = -5; y < 6; y++) {
                 worldState.setSensorInput("vision.peripheral.obj."+x+"."+y, sensorID++, objectAtQuadrant(x,y));
             }
         }
 
         // Look for closed and open boundary objects
         //worldState.setSensorInput("vision.fovea.closed_object", sensorID++, closedObjectAt(0,0));
         
         //worldState.setSensorInput("vision.peripheral.obj."+x+"."+y, sensorID++, objectAtQuadrant(x,y));
         
 
     }
     static final int QUADRANT_SIZE = 100;
 
     boolean objectAtQuadrant(float qx, float qy) {
         Object2D obj = findObjAt(xpos + qx*QUADRANT_SIZE, ypos+qy*QUADRANT_SIZE);
         return obj != null;
     }
 
     // Vision primitives
     void gazeAt(Object2D thing) {
         Vec2 pos = thing.getPosition();
         gazeXpos = pos.x - xpos;
         gazeYpos = ypos - pos.y;
     }
 
     // Includes proprioceptive sensors
     void computeTouchSensors() {
         // update joint position sensors
         for (int i = -5; i < 6; i++) {
             worldState.setSensorInput("hand1.gross.x."+i, sensorID++, hand1.grossX == i);
             worldState.setSensorInput("hand1.gross.y."+i, sensorID++, hand1.grossY == i);
             worldState.setSensorInput("hand1.fine.x."+i,  sensorID++, hand1.fineX == i);
             worldState.setSensorInput("hand1.fine.y."+i,  sensorID++, hand1.fineY == i);
             worldState.setSensorInput("hand2.gross.x."+i, sensorID++, hand2.grossX == i);
             worldState.setSensorInput("hand2.gross.y."+i, sensorID++, hand2.grossY == i);
             worldState.setSensorInput("hand2.fine.x."+i,  sensorID++, hand2.fineX == i);
             worldState.setSensorInput("hand2.fine.y."+i,  sensorID++, hand2.fineY == i);
         }
 
         // gaze angle sensor
         for (int i = -5; i < 6; i++) {
             worldState.setSensorInput("gaze.gross.x"+i, sensorID++, Math.round(gazeXpos/50) == i);
             worldState.setSensorInput("gaze.gross.y"+i, sensorID++, Math.round(gazeYpos/50) == i);
         }
 
         // update joint force sensors
         for (int i = -5; i < 6; i++) {
             Vec2 f1 = hand1.getJointForce();
             worldState.setSensorInput("hand1.force.x."+i, sensorID++, (i-1) < f1.x && f1.x < i);
             worldState.setSensorInput("hand1.force.y."+i, sensorID++, (i-1) < f1.y && f1.y < i);
             Vec2 f2 = hand2.getJointForce();
             worldState.setSensorInput("hand2.force.x."+i, sensorID++, (i-1) < f2.x && f2.x < i);
             worldState.setSensorInput("hand2.force.y."+i, sensorID++, (i-1) < f2.y && f2.y < i);
         }
 
         // update gripper touch and force sensors
         int t1 = hand1.touchingSides();
         worldState.setSensorInput("hand1.touch.left", sensorID++, (t1 & Hand.TOUCH_LEFT) != 0);
         worldState.setSensorInput("hand1.touch.right", sensorID++, (t1 & Hand.TOUCH_RIGHT) != 0);
         worldState.setSensorInput("hand1.touch.top", sensorID++, (t1 & Hand.TOUCH_TOP) != 0);
         worldState.setSensorInput("hand1.touch.bottom", sensorID++, (t1 & Hand.TOUCH_BOTTOM) != 0);
 
         
         int t2 = hand2.touchingSides();
         worldState.setSensorInput("hand1.touch.left", sensorID++, (t2 & Hand.TOUCH_LEFT) != 0);
         worldState.setSensorInput("hand1.touch.right", sensorID++, (t2 & Hand.TOUCH_RIGHT) != 0);
         worldState.setSensorInput("hand1.touch.top", sensorID++, (t2 & Hand.TOUCH_TOP) != 0);
         worldState.setSensorInput("hand1.touch.bottom", sensorID++, (t2 & Hand.TOUCH_BOTTOM) != 0);
         
     }
 
 
 
 
 }
 
 
