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
 import org.jbox2d.collision.AABB;
 
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
 
 
     // Map from color to object. Each physobj has a unique color, to
     // give some assistance to the visual system in tagging objects for
     // gaze control.
     public HashMap<Integer,Object2D> objColorMap = new HashMap<Integer,Object2D>();
     public int objCounter = 0;
 
     void addObjectColorMapping(Object2D obj, int color) {
         objColorMap.put(color, obj);
     }
 
     public int getNextObjectColor() {
         if (objCounter > 255) {
             throw new RuntimeException("cannot have more than 255 objects");
         } else {
             int c = ObjectColors.objcolors[objCounter++];
             return c;
         }
     }
 
     public Object2D findObjByColor(int color) {
         return objColorMap.get(color);
     }
 
     public Object2D testfind() {
         return findObjByColor(-14277082);
     }
 
     ////////////////////////////////////////////////////////////////
     // Head and Eyes Controls
 
     // Relative offset from body xpos,ypos.
     // Computed from head gross and fine angles
     public float gazeXpos = 0;
     public float gazeYpos = 0;
     
     public Hand hand1;
     public Hand hand2;
 
     // max number of gross and fine motor steps that arms can take
     public int reachX = 5;
     public int reachY = 5;
     // arm motor step size
     public int dGross = 100;
     public int dFine = 20;
 
     // Used to sense objects in the visual field
     VisualRegion vision;
 
     boolean showWorldState = false;
 
     Object2D foveatedObject = null;
     // An image context to render the retina view into
     PGraphics retina = null;
 
     public SensoriMotorSystem(JSchema a, PGraphics retina) {
         this.app = a;
         this.retina = retina;
         System.out.println("SensoriMotorSystem constructor this.app = "+this.app);
     }
 
     Object2D findObj(int index) {
         for (Plane plane: planes) {
             Object2D o = plane.findObj(index);
             if (o != null) { return o; }
         }
         return null;
     }
 
     Object2D findObjAt(Vec2 pos) {
         return findObjAt(pos.x, pos.y);
     }
 
     ArrayList<Object2D> findObjectsAt(Vec2 pos) {
         ArrayList<Object2D> items = new ArrayList<Object2D>();
         for (Plane plane: planes) {
             ArrayList<Object2D> objs = plane.findObjectsAt(pos.x, pos.y);
             items.addAll(objs);
         }
         return items;
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
 
     static final int RETINA_SIZE = 1000;
 
     void setupDisplay() {
         // Initial body position
         xpos = app.width/2;
         ypos = app.height/2+100;
 
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
 
         vision = new VisualRegion(this);
         vision.init(NQUADRANT_X, NQUADRANT_Y, QUADRANT_SIZE);
 
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
         p.addBoundary(WORLD_WIDTH/2, app.height+30, WORLD_WIDTH, 100f );
         // left wall
         p.addBoundary(-40,           app.height-200, 50f, 2000f );
         // right wall
         p.addBoundary(WORLD_WIDTH+40, app.height-200, 50f, 2000f );
 
 
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
         p.addBox(500, bottom -10, 64, 64, 1);
         p.addBox(500, bottom -10, 64, 64, 1);
         p.addBox(500, bottom -10, 64, 64, 1);
         p.addBox(500, bottom -10, 64, 64, 1);
         p.addBox(500, bottom -10, 64, 64, 1);
 
         /*        p.addBox(500, bottom-10, 64, 64, 2);
         p.addBox(800, bottom-10, 32, 32, 2);
         p.addBox(1200, bottom-10, 64, 64, 2);
 
         p.addBox(1500, bottom-10, 64, 64, 1);
         p.addBox(2000, bottom-10, 64, 64, 10);
 
         p.addBox(300, bottom-200, 200, 5, 6);
         */
         /*
         p.addBox(300, bottom-200, 400, 5, 6);
         p.addBox(280, bottom-200, 20, 10, 8);
         p.addBox(260, bottom-200, 20, 20, 8);
         p.addBox(240, bottom-200, 30, 20, 8);
 
         */
         p.addBall(1000, bottom-100, 40);
         p.addBall(200, bottom-100, 40);
 
 
         /*
           p.addBall(250, bottom-100, 40);
         p.addBall(500, bottom-100, 30);
         p.addCustomShape1(50,bottom-100,app.color(123,201,122));
         */
 
     }
 
     String showHandInfo(Hand h) {
         StringBuilder touchList = new StringBuilder();
         for (Object2D obj: h.touchingObjects()) {
             touchList.append(" "+obj.index);
         }
 
         StringBuilder graspList = new StringBuilder();
         for (Object2D obj: h.getWeldedObjects()) {
             graspList.append(" "+obj.index);
         }
 
 
         Vec2 jf = h.getJointForce();
 
         float torque = h.getJointTorque();
 
         String info = String.format("grossX=(%.1f,%.1f) fineX=(%.1f,%.1f) FJoint=(%.1f, %.1f) TRQ=%.1f %s [touching %s] [grasp %s]",
                                     h.grossX, h.grossY,
                                     h.fineX, h.fineY,
                                     jf.x*100, jf.y*100,
                                     torque,
                                     h.touchString(), touchList, graspList);
         return info;
     }
 
 
     void draw() {
         if (run || singleStep) {
             singleStep = false;
 
             app.rectMode(PConstants.CORNER);
             if (planes.indexOf(currentPlane) == 0) {
                 app.background(255,225,225);
             } else {
                 app.background(255);
             }
 
             for (Plane plane: planes) {
                 plane.draw();
             }
 
             // draw viewport and gaze location
             drawViewPort();
 
             displayRetinaView();
 
             computeWorldState();
 
             // Display info text 
             displayInfoText();
 
             if (showWorldState) {
                 displayWorldState(worldState);
             }
 
 
 
         }
     }
 
     void displayInfoText() {
         app.fill(0);
         app.text("alt-click to create box, click to grasp, ctrl-click to lift, L and R key to rotate grip, shift for transparent, space/enter toggle single-step", 20,12);
         app.text("clock = "+app.stage.clock + " plane="+planes.indexOf(currentPlane), 20,22);
         app.text("xpos="+xpos+ "   ypos="+ypos,20,32);
         app.text("hand1 "+showHandInfo(hand1),20,42);
         app.text("hand2 "+showHandInfo(hand2),20,52);
         app.text("gazeX="+gazeXpos+" gazeY="+gazeYpos, 20, 62);
         app.text( String.format("%d schemas, %d items, %d actions", 
                                 app.stage.schemas.size(),
                                 app.stage.items.size(),
                                 app.stage.actions.size()), 20, 72);
         app.text(String.format("last actions: %s", worldState.actions), 20, 82);
     }
 
     void displayRetinaView() {
         retina.beginDraw();
         retina.background(255);
         for (Plane plane: planes) {
             
             plane.drawRetina(retina);
         }
         retina.endDraw();
     }
 
     void displayWorldState(WorldState w) {
 
         app.pushMatrix();
         app.pushStyle();
         app.rectMode(PConstants.CENTER);
         app.translate(app.width-250,20);
 
         for (Map.Entry<String, SensorInput> entry : w.inputs.entrySet())
         {
             SensorInput s = entry.getValue();
             int id = s.id;
             boolean v = s.value;
 
             float cx = (id % 20) * 12;
             float cy = (id / 20) * 12;
 
             // draw the max range the hands can move
             if (v) {
                 app.fill(0);
             } else {
                 app.fill(225);
             }
 
             app.rect(cx, cy, 10,10);
         }
 
         // draw the max range the hands can move
 
         app.popStyle();
         app.popMatrix();
         vision.display();
 
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
         app.line(cx + dx - 50, cy+dy-50,
                  cx + dx + 50, cy+dy+50);
         app.line(cx + dx + 50, cy+dy-50,
                  cx + dx - 50, cy+dy+50);
         
 
         // draw the max range the hands can move
         app.rect(cx, ypos, reachX*dGross + reachX*dFine, reachY*dGross+ reachY*dFine);
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
 
         plane0.updateHeadPosition(xpos,ypos);
         plane1.updateHeadPosition(xpos,ypos);
 
         hand1.updatePosition(xpos,ypos);
         hand2.updatePosition(xpos,ypos);
 
         setTranslations(xpos,ypos);
     }
 
     boolean run = true;
     boolean singleStep = false;
 
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
 
         if (app.key == 'w') {
             showWorldState = !showWorldState;
         }
 
         if (app.key == 'e') {
             gazeUp(GAZE_INCR);
         } else if (app.key == 'c') {
             gazeDown(GAZE_INCR);
         } else if (app.key == 's') {
             gazeLeft(GAZE_INCR);
         } else if (app.key == 'f') {
             gazeRight(GAZE_INCR);
         } else if (app.key == ' ') {
             run = false;
             singleStep = true;
         } else if (app.keyCode == PConstants.ENTER) {
             run = true;
             singleStep = false;
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
         //HashMap<String,Action> outputList
         for (Action action : w.actions) {
             // CODE HERE To execute actions
             switch (action.type) {
               case CENTER_GAZE:
                 // Set the gaze to the body/head center position
                 gazeAt(new Vec2(xpos,ypos));
                 break;
               case MOVE_LEFT:
                 break;
               case MOVE_RIGHT:
                 break;
               case MOVE_UP:
                 break;
               case MOVE_DOWN:
                 break;
               case GAZE_LEFT:
                 gazeLeft(GAZE_INCR);
                 break;
               case GAZE_RIGHT:
                 gazeRight(GAZE_INCR);
                 break;
               case GAZE_UP:
                 gazeUp(GAZE_INCR);
                 break;
               case GAZE_DOWN:
                 gazeDown(GAZE_INCR);
                 break;
               case FOVEATE_NEXT_MOTION:
                 foveateNextMovingObject(gazeAbsPosition());
                 break;
               case FOVEATE_NEXT_OBJECT_LEFT:
                 foveateNextObjectLeft();
                 break;
               case FOVEATE_NEXT_OBJECT_RIGHT:
                 foveateNextObjectRight();
                 break;
               case FOVEATE_NEXT_OBJECT_UP:
                 foveateNextObjectUp();
                 break;
               case FOVEATE_NEXT_OBJECT_DOWN:
                 foveateNextObjectDown();
                 break;
               case HAND2_LEFT:  // move right hand, gross motor
                 hand2.hjog(-1,0);
                 break;
               case HAND2_RIGHT:
                 hand2.hjog(1,0);
                 break;
               case HAND2_UP:
                 hand2.vjog(-1, 0);
                 break;
               case HAND2_DOWN:
                 hand2.vjog(1,0);
                 break;
               case HAND2_FINE_LEFT: // move right hand, fine motor
                 hand2.hjog(0,-1);
                 break;
               case HAND2_FINE_RIGHT:
                 hand2.hjog(0,1);
                 break;
               case HAND2_FINE_UP:
                 hand2.vjog(0,-1);
                 break;
               case HAND2_FINE_DOWN:
                 hand2.vjog(0,1);
                 break;
 
               case HAND1_LEFT:  // move left hand, gross motor
                 hand1.hjog(-1,0);
                 break;
               case HAND1_RIGHT:
                 hand1.hjog(1,0);
                 break;
               case HAND1_UP:
                 hand1.vjog(-1, 0);
                 break;
               case HAND1_DOWN:
                 hand1.vjog(1,0);
                 break;
               case HAND1_FINE_LEFT: // move left hand, fine motor
                 hand1.hjog(0,-1);
                 break;
               case HAND1_FINE_RIGHT:
                 hand1.hjog(0,1);
                 break;
               case HAND1_FINE_UP:
                 hand1.vjog(0,-1);
                 break;
               case HAND1_FINE_DOWN:
                 hand1.vjog(0,1);
                 break;
 
 
 
 
               case HAND1_GRASP:
                 hand1.weldContacts();
                 break;
               case HAND1_UNGRASP:
                 hand1.removeWeldJoints();
                 break;
               case HAND2_GRASP:
                 hand2.weldContacts();
                 break;
               case HAND2_UNGRASP:
                 hand2.removeWeldJoints();
                 break;
               case HAND1_WELD:
                 hand1.weldGraspedObjects();
                 break;
               case HAND2_WELD:
                 hand2.weldGraspedObjects();
                 break;
               case HAND1_UNWELD:
                 hand1.unWeldGraspedObjects();
                 break;
               case HAND2_UNWELD:
                 hand2.unWeldGraspedObjects();
                 break;
               default:
                 app.println("unknown Action type "+action.type);
             }
         }
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
 
 
     void computeVisionSensor() {
         // copies the bitmap into the pixels[] array for retina
         retina.loadPixels();
         // is fovea seeing a solid object?
         worldState.setSensorInput("vision.fovea.object", sensorID++, vision.isObjectAtGaze(gazeAbsPosition()));
         worldState.setSensorInput("vision.fovea.solid_object", sensorID++, vision.isSolidObjectAtGaze(gazeAbsPosition()));
         worldState.setSensorInput("vision.fovea.hollow_object", sensorID++, vision.isHollowObjectAtGaze(gazeAbsPosition()));
         worldState.setSensorInput("vision.fovea.round_object", sensorID++, vision.isRoundObjectAtGaze(gazeAbsPosition()));
         worldState.setSensorInput("vision.fovea.flat_object", sensorID++, vision.isFlatObjectAtGaze(gazeAbsPosition()));
 
         worldState.setSensorInput("vision.fovea.flat_object", sensorID++, vision.isRedObjectAtGaze(gazeAbsPosition()));
         worldState.setSensorInput("vision.fovea.flat_object", sensorID++, vision.isBlueObjectAtGaze(gazeAbsPosition()));
         worldState.setSensorInput("vision.fovea.flat_object", sensorID++, vision.isGreenObjectAtGaze(gazeAbsPosition()));
 
         worldState.setSensorInput("vision.fovea.flat_object", sensorID++, vision.isDarkObjectAtGaze(gazeAbsPosition()));
         worldState.setSensorInput("vision.fovea.flat_object", sensorID++, vision.isLightObjectAtGaze(gazeAbsPosition()));
 
         for (int angle = 0 ; angle < 180; angle+= 10) {
             worldState.setSensorInput("vision.fovea.angle."+angle, sensorID++,
                                       vision.isGazeObjectAtAngle(gazeAbsPosition(), angle-10, angle));
         }
 
         // Look for closed and open boundary objects
         //worldState.setSensorInput("vision.fovea.closed_object", sensorID++, closedObjectAt(0,0));
         
         computeMotionSensors();
         for (int x = 0; x < NQUADRANT_X; x++) {
             for (int y = 0; y < NQUADRANT_Y; y++) {
                 worldState.setSensorInput("vision.peripheral.obj."+x+"."+y, sensorID++, vision.peripheralObjectAtQuadrant(x,y));
 
                 VisualCell cell = vision.getCell(x,y);
                 worldState.setSensorInput("vision.peripheral.motion."+x+"."+y, sensorID++, cell.motionSensed);
                 worldState.setSensorInput("vision.peripheral.pos_x_motion."+x+"."+y, sensorID++, cell.motion.x > 0);
                 worldState.setSensorInput("vision.peripheral.neg_x_motion."+x+"."+y, sensorID++, cell.motion.x < 0);
                 worldState.setSensorInput("vision.peripheral.pos_y_motion."+x+"."+y, sensorID++, cell.motion.y > 0);
                 worldState.setSensorInput("vision.peripheral.neg_y_motion."+x+"."+y, sensorID++, cell.motion.y < 0);
             }
         }
     }
 
     static final double MOTION_THRESHOLD = 1;
     void computeMotionSensors() {
         vision.clearMotionSensors();
         for (Plane plane: planes) {
             for (Object2D obj: plane.physobjs) {
                 Fixture f = obj.body.getFixtureList();
                 AABB bbox = f.getAABB(0);
                 Vec2 lb = plane0.box2d.coordWorldToPixels(bbox.lowerBound);
                 Vec2 ub = plane0.box2d.coordWorldToPixels(bbox.upperBound);
 
                 Vec2 vmotion = obj.body.getLinearVelocity();
 
                 // translate these coordinates to retina frame of reference
                 Vec2 offset = new Vec2(xpos-app.width/2, ypos-app.width/2);
                 offset.x -= gazeXpos;
                 offset.y += gazeYpos;
                 lb.subLocal(offset);
                 ub.subLocal(offset);
                 
                 int x1 = (int) Math.floor(lb.x / QUADRANT_SIZE);
                 int x2 = (int) Math.floor(ub.x / QUADRANT_SIZE);
 
                 int y1 = (int) Math.floor(lb.y / QUADRANT_SIZE);
                 int y2 = (int) Math.floor(ub.y / QUADRANT_SIZE);
 
                 int xmin = Math.min(x1, x2);
                 int xmax = Math.max(x1, x2);
                 int ymin = Math.min(y1, y2);
                 int ymax = Math.max(y1, y2);
 
                 if (vmotion.lengthSquared() > MOTION_THRESHOLD) {
                     for (int x = xmin; x <= xmax; x++) {
                         for (int y = ymin; y <= ymax; y++) {
                             vision.setMotionAtQuadrant(x, y, vmotion);
                         }
                     }
                 }
 
             }
         }
     }
 
 
 
     static final int QUADRANT_SIZE = 200;
     static final int NQUADRANT_X = 5; // 10x10 field
     static final int NQUADRANT_Y = 5; // 
 
     static final int GAZE_INCR = 50;
     static final int GAZE_MAX_XOFFSET = 650;
     static final int GAZE_MAX_YOFFSET = 350;
 
     boolean objectAtQuadrant(float qx, float qy) {
         Object2D obj = findObjAt(xpos + qx*QUADRANT_SIZE, ypos+qy*QUADRANT_SIZE);
         return obj != null;
     }
 
     // Vision motor primitives
     /**
        send the gaze to center on this object
        @return dx,dy of gaze motion
      */
     Vec2 gazeAt(Object2D thing) {
         return gazeAt(thing.getPosition());
     }
 
 /**
    This moves the gaze to abs position pos, and has side effect of
    setting the proprioceptive sense gazeMotion to tell how far the gaze moved.
  */
     Vec2 gazeAt(Vec2 pos) {
         float oldgx = gazeXpos;
         float oldgy = gazeYpos;
         gazeXpos = limit(pos.x - xpos, -GAZE_MAX_XOFFSET, GAZE_MAX_XOFFSET);
         gazeYpos = limit(pos.y - ypos, -GAZE_MAX_YOFFSET, GAZE_MAX_YOFFSET);
         gazeMotion = new Vec2(gazeXpos - oldgx, gazeYpos-oldgy);
         return gazeMotion;
     }
 
     // tracks how far the gaze moved
     Vec2 gazeMotion = new Vec2();
 
     float limit(float val, float min, float max) {
         if (val < min) {
             return min;
         } else if (val > max) {
             return max;
         } else {
             return val;
         }
     }
 
     void gazeLeft(int n) {
         float oldgx = gazeXpos;
         gazeXpos = limit(gazeXpos - n, -GAZE_MAX_XOFFSET, GAZE_MAX_XOFFSET);
         gazeMotion =  new Vec2(gazeXpos - oldgx, 0);
     }
 
     void gazeRight(int n) {
         gazeLeft(-n);
     }
 
     void gazeUp(int n) {
         float oldgy = gazeYpos;
         gazeYpos = limit(gazeYpos - n, -GAZE_MAX_YOFFSET, GAZE_MAX_YOFFSET);
         gazeMotion =  new Vec2(0, gazeYpos - oldgy);        
     }
 
     void gazeDown(int n) {
         gazeUp(-n);
     }
 
     void setGazePosition(float x, float y) {
         gazeXpos = x;
         gazeYpos = y;
     }
 
     Vec2 gazeAbsPosition() {
         return new Vec2(xpos+gazeXpos, ypos+gazeYpos);
     }
 
     Object2D objectAtGaze() {
         return findObjAt(gazeAbsPosition());
     }
 
 
     /** Moves the gaze to center on the next item in the fovea. */
     void gazeNext() {
         ArrayList<Object2D> items = findObjectsAt(gazeAbsPosition());
         // NYI
     }
 
 
     /**
        sorts objects list by proximity to position
      */
     public ArrayList<Object2D> sortPhysobjsByDistance(final Vec2 position) {
         ArrayList<Object2D> items = new ArrayList<Object2D>(plane0.physobjs);
         items.addAll(plane1.physobjs);
 
         // sort items by horizontal (x) position
         Collections.sort(items, new Comparator<Object2D>() {
                 public int compare(Object2D o1, Object2D o2) {
                     float a = o1.getPosition().sub(position).length();
                     float b = o2.getPosition().sub(position).length();
                     return Integer.signum(Math.round(a - b));
                 }
             });
 
         sortedItems = items;
         return items;
     }
     /**
        Foveate on the closest moving object to the current gaze
      */
     public Object2D foveateNextMovingObject(Vec2 position) {
         ArrayList<Object2D> items = sortPhysobjsByDistance(position);
         //app.println("sorted by dist from "+position+" items = "+items.toString());
         //app.println("foveateNextMovingObject foveatedObject = "+foveatedObject);
         int idx = -1;
         // find the index of the closest moving object to the 
 
         for (int i = 0; i < items.size(); i++) {
             Object2D obj = items.get(i);
             //app.println(i + " checking object "+obj +" isMoving "+obj.isMoving());
             // if we're already looking at this obj, go to the next one
             if (obj == foveatedObject) continue;
 
             if (obj.isMoving()) {
                 idx = i;
                 break;
             }
         }
 
         if (idx == -1) {
             app.println("Error in foveateNextMovingObject, could not find any object!");
             return null;
         } else {
             foveatedObject = items.get(idx);
             gazeAt(foveatedObject);
             return foveatedObject;
         }
 
     }
 
 
     /*
       FOVEATE_NEXT_OBJECT_LEFT,
       FOVEATE_NEXT_OBJECT_RIGHT,
       FOVEATE_NEXT_OBJECT_UP,
       FOVEATE_NEXT_OBJECT_DOWN
     */
     /**
        Move gaze to center of nearest object whose center of mass is to the left.
      */
     public ArrayList<Object2D> sortedItems = null;
 
     ArrayList<Object2D> sortPhysobjsHorizontal() {
         ArrayList<Object2D> items = new ArrayList<Object2D>(plane0.physobjs);
         items.addAll(plane1.physobjs);
 
         // sort items by horizontal (x) position
         Collections.sort(items, new Comparator<Object2D>() {
                 public int compare(Object2D o1, Object2D o2) {
                     Vec2 a = o1.getPosition();
                     Vec2 b = o2.getPosition();
                     return Integer.signum(Math.round(a.x - b.x));
                 }
             });
 
         sortedItems = items;
         return items;
     }
 
     ArrayList<Object2D> sortPhysobjsVertical() {
         ArrayList<Object2D> items = new ArrayList<Object2D>(plane0.physobjs);
         items.addAll(plane1.physobjs);
 
         // sort items by horizontal (x) position
         Collections.sort(items, new Comparator<Object2D>() {
                 public int compare(Object2D o1, Object2D o2) {
                     Vec2 a = o1.getPosition();
                     Vec2 b = o2.getPosition();
                     return Integer.signum(Math.round(a.y - b.y));
                 }
             });
 
         sortedItems = items;
         return items;
     }
 
     public Object2D foveateNextObjectLeft() {
         ArrayList<Object2D> items = sortPhysobjsHorizontal();
         
         Vec2 gaze = gazeAbsPosition();
         //app.println("gazeAbsPosition = "+gaze);
 
         int idx = -1;
 
         // find the index of the closest object to the left of the gaze position
 
         for (int i = items.size()-1; i >= 0; i--) {
             Object2D obj = items.get(i);
             if (obj.getPosition().x < gaze.x) {
                 idx = i;
                 break;
             }
         }
         //app.println("...idx = "+idx);
 
         if (idx == -1) {
             app.println("Error in foveateNextObjectLeft, could not find any object!");
             return null;
         } else {
             foveatedObject = items.get(idx);
             gazeAt(foveatedObject);
             return foveatedObject;
         }
     }
 
     public Object2D foveateNextObjectRight() {
         ArrayList<Object2D> items = sortPhysobjsHorizontal();
         Vec2 gaze = gazeAbsPosition();
         app.println("gazeAbsPosition = "+gaze);
 
         int idx = -1;
 
         // find the index of the closest object to the left of the gaze position
 
         for (int i = 0; i < items.size(); i++) {
             Object2D obj = items.get(i);
             if (obj.getPosition().x > gaze.x) {
                 idx = i;
                 break;
             }
         }
         app.println("...idx = "+idx);
 
         if (idx == -1) {
             app.println("Error in foveateNextObjectRight, could not find any object!");
             return null;
         } else {
             foveatedObject = items.get(idx);
             gazeAt(foveatedObject);
             return foveatedObject;
         }
     }
 
 
     public Object2D foveateNextObjectUp() {
         ArrayList<Object2D> items = sortPhysobjsHorizontal();
         
         Vec2 gaze = gazeAbsPosition();
         app.println("gazeAbsPosition = "+gaze);
 
         int idx = -1;
 
         // find the index of the closest object to the left of the gaze position
 
         for (int i = items.size()-1; i >= 0; i--) {
             Object2D obj = items.get(i);
             if (obj.getPosition().y < gaze.y) {
                 idx = i;
                 break;
             }
         }
         app.println("...idx = "+idx);
 
         if (idx == -1) {
             app.println("Error in foveateNextObjectLeft, could not find any object!");
             return null;
         } else {
             foveatedObject = items.get(idx);
             gazeAt(foveatedObject);
             return foveatedObject;
         }
     }
 
 
     public Object2D foveateNextObjectDown() {
         ArrayList<Object2D> items = sortPhysobjsHorizontal();
         Vec2 gaze = gazeAbsPosition();
         app.println("gazeAbsPosition = "+gaze);
 
         int idx = -1;
 
         // find the index of the closest object to the left of the gaze position
 
         for (int i = 0; i < items.size(); i++) {
             Object2D obj = items.get(i);
             if (obj.getPosition().y > gaze.y) {
                 idx = i;
                 break;
             }
         }
         app.println("...idx = "+idx);
 
         if (idx == -1) {
             app.println("Error in foveateNextObjectRight, could not find any object!");
             return null;
         } else {
             foveatedObject = items.get(idx);
             gazeAt(foveatedObject);
             return foveatedObject;
         }
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
         Vec2 f1 = hand1.getJointForce();
         f1.mulLocal(100.0f);
         float trq1 = hand1.getJointTorque();
         Vec2 f2 = hand2.getJointForce();
         f2.mulLocal(100.0f);
         float trq2 = hand2.getJointTorque();
         // forces should be normalized to range [-1.0, 1.0]
 
         float f = -1.0f;
         for (int i = 0; i < 2; i++, f+=2.0) {
             worldState.setSensorInput("hand1.force.x."+i, sensorID++, f1.x < f);
             worldState.setSensorInput("hand1.force.y."+i, sensorID++, f1.y < f);
             worldState.setSensorInput("hand1.torque."+i, sensorID++,  trq1 < f);            
             worldState.setSensorInput("hand2.force.x."+i, sensorID++, f2.x < f);
             worldState.setSensorInput("hand2.force.y."+i, sensorID++, f2.y < f);
             worldState.setSensorInput("hand2.torque."+i, sensorID++,  trq2 < f);
         }
 
         // update gripper touch and force sensors
         int t1 = hand1.touchingSides();
         worldState.setSensorInput("hand1.touch.left", sensorID++, (t1 & Hand.TOUCH_LEFT) != 0);
         worldState.setSensorInput("hand1.touch.right", sensorID++, (t1 & Hand.TOUCH_RIGHT) != 0);
         worldState.setSensorInput("hand1.touch.top", sensorID++, (t1 & Hand.TOUCH_TOP) != 0);
         worldState.setSensorInput("hand1.touch.bottom", sensorID++, (t1 & Hand.TOUCH_BOTTOM) != 0);
 
         int h1ObjectsGrasped = hand1.getWeldedObjects().size();
         worldState.setSensorInput("hand1.empty-grasp", sensorID++, h1ObjectsGrasped == 0);
         worldState.setSensorInput("hand1.grasp-one", sensorID++, h1ObjectsGrasped == 1);
         worldState.setSensorInput("hand1.grasp-two", sensorID++, h1ObjectsGrasped == 2);
         worldState.setSensorInput("hand1.grasp-three", sensorID++, h1ObjectsGrasped == 3);
         worldState.setSensorInput("hand1.grasp-many", sensorID++, h1ObjectsGrasped > 3);
 
         
         int t2 = hand2.touchingSides();
         worldState.setSensorInput("hand1.touch.left", sensorID++, (t2 & Hand.TOUCH_LEFT) != 0);
         worldState.setSensorInput("hand1.touch.right", sensorID++, (t2 & Hand.TOUCH_RIGHT) != 0);
         worldState.setSensorInput("hand1.touch.top", sensorID++, (t2 & Hand.TOUCH_TOP) != 0);
         worldState.setSensorInput("hand1.touch.bottom", sensorID++, (t2 & Hand.TOUCH_BOTTOM) != 0);
 
        int h2ObjectsGrasped = hand1.getWeldedObjects().size();
         worldState.setSensorInput("hand2.empty-grasp", sensorID++, h2ObjectsGrasped == 0);
         worldState.setSensorInput("hand2.grasp-one", sensorID++, h2ObjectsGrasped == 1);
         worldState.setSensorInput("hand2.grasp-two", sensorID++, h2ObjectsGrasped == 2);
         worldState.setSensorInput("hand2.grasp-three", sensorID++, h2ObjectsGrasped == 3);
         worldState.setSensorInput("hand2.grasp-many", sensorID++, h2ObjectsGrasped > 3);
 
         
     }
 
 
 
 
 }
 
 
