 package main;
 
 import jinngine.collision.SAP2;
 import jinngine.geometry.Box;
 import jinngine.math.Matrix3;
 import jinngine.math.Vector3;
 import jinngine.physics.Body;
 import jinngine.physics.ContactTrigger;
 import jinngine.physics.DefaultDeactivationPolicy;
 import jinngine.physics.DefaultScene;
 import jinngine.physics.constraint.contact.ContactConstraint;
 import jinngine.physics.force.Force;
 import jinngine.physics.force.GravityForce;
 import jinngine.physics.solver.NonsmoothNonlinearConjugateGradient;
 import models.House;
 import models.MainCamera;
 import models.Terrain;
 import models.UserObject;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.PixelFormat;
 import org.lwjgl.util.glu.GLU;
 
 import java.util.ArrayList;
 
 import static main.Settings.*;
 
 public class Main {
 
     private MainCamera camera;
     private Terrain terrain;
     private UserObject user;
     private UserObject bomb;
     private jinngine.physics.Scene scene;
     private jinngine.physics.Body box;
     private  Body currBomb;
     private ArrayList<House> houses = new ArrayList<House>();
     private Force gravity;
     private Matrix3 inertia;
     private Matrix3 inverse;
     private boolean bombReleased = false;
 
     public static void main(String[] args) {
         Main main = new Main();
         main.startLoop();
     }
 
     private void startLoop() {
 //        if (JOptionPane.showConfirmDialog(null, "Would You Like To Run In Fullscreen Mode?",
 //                "Start Fullscreen?", JOptionPane.YES_NO_OPTION) == 1) {
 //            Settings.fullScreen = false;
 //        }
         fullScreen = false;
 
 
         try {
             if (!CreateGLWindow(windowTitle, windowWidth, windowHeight, fullScreen)) {
                 // Quit If Window Was Not Created
                 throw new Exception();
             }
             initializeObjects();
             //hide the mouse
 //            Mouse.setGrabbed(true);
 
             long FPSSync = System.currentTimeMillis();
             while (!Keyboard.isKeyDown(exitKey) && !Display.isCloseRequested()) {
                 long currSync = System.currentTimeMillis();
 
                 if (currSync-FPSSync < 10) {
                     continue;
                 }
 
                 FPSSync = System.currentTimeMillis();
 
                 resetDisplay();
                 if (!Display.isActive()) {
                     // Quit if told to
                     break;
                 }
                 applyPhysics();
                 render();
                 processInput();
 
                 // Toggle Fullscreen / Windowed Mode
                 if (Keyboard.isKeyDown(changeWindowModeKey)) {
                     fullScreen = !fullScreen;
                     Display.setFullscreen(fullScreen);
                 }
                 Display.update();
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             Display.destroy();
         }
         System.exit(0);
     }
 
     private void resetDisplay() {
         GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
         GL11.glClearColor(1, 1, 1, 1);
     }
 
     private void render() {
         camera.render3D();
         terrain.render3D();
         user.render3D();
 //        if (bombReleased) {
         bomb.render3D();
 //        }
         for(House house: houses) {
             house.render3D();
         }
         //System.out.printf("%f, %f, %f\n",camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
     }
 
     private void applyPhysics() {
         scene.tick();
         user.setPosition((float) box.getPosition().x, (float) box.getPosition().y, (float) box.getPosition().z);
         bomb.setPosition((float) currBomb.getPosition().x, (float) currBomb.getPosition().y, (float) currBomb.getPosition().z);
     }
 
     private void initializeObjects() {
         scene = new DefaultScene(new SAP2(), new NonsmoothNonlinearConjugateGradient(44), new DefaultDeactivationPolicy());
 
         scene.setTimestep(0.01);
 
         camera = new MainCamera();
         camera.translate(-5.0f, -2.0f, -7.0f);
 
         terrain = new Terrain();
         terrain.scale(50.0f, 1000.0f, 1000.0f);
         terrain.translate(0.0f, -0.5f, 0.0f);
 
         float[] positionLeft = {0, 0, 0}, positionRight = {0, 0, 0};
         float[] width;
         House house;
         for (int i = 0; i < 30; i++) {
             // LEFT HOUSE
             house = new House(new float[]{(float) Math.random() * 1, (float) Math.random() * 1, (float) Math.random() * 1});
             width = new float[]{
                     minHouseWidth,
                     (float) Math.random() * houseHeightBounds + minimalHouseHeight,
                     (float) Math.floor(Math.random() * houseLengthBounds + minimalHouseLength)
             };
             // position house
             house.scale(width[0], width[1], width[2]);
             house.translate(-positionLeft[0], positionLeft[1], positionLeft[2]);
             houses.add(house);
             positionLeft[2] -= width[2] + spaceBetweenHouses;
 
             // RIGHT HOUSE
             house = new House(new float[]{(float) Math.random() * 1, (float) Math.random() * 1, (float) Math.random() * 1});
             width = new float[]{
                     minHouseWidth,
                     (float) Math.random() * houseHeightBounds + minimalHouseHeight,
                     (float) Math.random() * houseLengthBounds + minimalHouseLength
             };
             // position house
             house.scale(width[0], width[1], width[2]);
             house.translate(positionRight[0] + mainRoadWidth, positionRight[1], positionRight[2]);
             houses.add(house);
             positionRight[2] -= width[2] + spaceBetweenHouses;
         }
 
         Body leftHouses = new Body("leftHouses", new Box(20, minimalHouseHeight, 1000));
         leftHouses.setPosition(new Vector3(0 - 8, 0, 0));
         leftHouses.setFixed(true);
 
         Body rightHouses = new Body("rightHouses", new Box(20, minimalHouseHeight, 1000));
         rightHouses.setPosition(new Vector3(mainRoadWidth + 8, 0, 0));
         rightHouses.setFixed(true);
 
         Body floor = new Body("floor", new Box(1000, 5, 1000));
         floor.setPosition(new Vector3(0, -3, 0));
         floor.setFixed(true);
 
         Body back = new Body("back", new Box(50, 10, 100));
         back.setPosition(new Vector3(0, 0, -1000));
         back.setFixed(true);
 
         Body front = new Body("front", new Box(50, 100, 10));
         front.setPosition(new Vector3(0, 0, 1000));
         front.setFixed(true);
 
         Body left = new Body("left", new Box(10, 100, 1000));
         left.setPosition(new Vector3(-50, 0, 0));
         left.setFixed(true);
 
         Body right = new Body("right", new Box(10, 100, 1000));
         right.setPosition(new Vector3(50, 0, 0));
         right.setFixed(true);
 
         user = new UserObject();
         user.scale(0.3f, 0.3f, 0.3f);
         user.translate(5.0f, 1.0f, 0.0f);
 
         bomb = new UserObject();
         bomb.scale(0.1f, 0.1f, 0.1f);
         bomb.translate(5.0f, 1.0f, 0.0f);
 
         box = new Body("box", new Box(0.3f, 0.3f, 0.3f));
         box.setPosition(new Vector3(5.0f, 1.0f, 0.0f));
         box.setFixed(true);
 
         currBomb = box;
 
         // add all to scene
         scene.addBody(floor);
         scene.addBody(back);
         scene.addBody(front);
         scene.addBody(left);
         scene.addBody(right);
         scene.addBody(leftHouses);
         scene.addBody(rightHouses);
         scene.addBody(box);
 
     }
 
     private boolean CreateGLWindow(String windowTitle, int windowWidth, int windowHeight, boolean fullScreen) throws LWJGLException {
         DisplayMode bestMode = null;
         for (DisplayMode d: Display.getAvailableDisplayModes()) {
             if (d.getWidth() == windowWidth && d.getHeight() == windowHeight && d.getFrequency() <= 85) {
                 if (bestMode == null || (d.getBitsPerPixel() >= bestMode.getBitsPerPixel() && d.getFrequency() > bestMode.getFrequency())) bestMode = d;
             }
         }
 
         Display.setDisplayMode(bestMode);
         Display.create(new PixelFormat(8, 8, 8, 4));
         Display.setFullscreen(fullScreen);
         Display.setTitle(windowTitle);
         ReSizeGLScene(windowWidth, windowHeight);
 
         // Initialize Our Newly Created GL Window
         // Enable Smooth Shading
         GL11.glShadeModel(GL11.GL_SMOOTH);
         // Depth Buffer Setup
         GL11.glClearDepth(1.0f);
         // Enables Depth Testing
         GL11.glEnable(GL11.GL_DEPTH_TEST);
         // The Type Of Depth Testing To Do
         GL11.glDepthFunc(GL11.GL_LEQUAL);
         // Really Nice Perspective Calculations
         GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
         return true;
     }
 
     private void ReSizeGLScene(int windowWidth, int windowHeight) {
         if (windowHeight == 0) {
             windowHeight = 1;
         }
         // Reset The Current Viewport
         GL11.glViewport(0, 0, windowWidth, windowHeight);
 
         // Select The Projection Matrix
         GL11.glMatrixMode(GL11.GL_PROJECTION);
         // Reset The Projection Matrix
         GL11.glLoadIdentity();
 
         // Calculate The Aspect Ratio Of The Window
         GLU.gluPerspective(45.0f, (float) windowWidth / (float) windowHeight, 0.1f, 100.0f);
 
         // Select The Modelview Matrix
         GL11.glMatrixMode(GL11.GL_MODELVIEW);
         // Reset The Modelview Matrix
         GL11.glLoadIdentity();
     }
 
     protected void processInput() {
         if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
             scene.removeBody(box);
             user.translate(-0.1f, 0.0f, 0.0f);
             box.setPosition(new Vector3(user.getPosition().x, user.getPosition().y, user.getPosition().z));
             camera.translate(0.1f, 0.0f, 0.0f);
             scene.addBody(box);
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
             user.translate(0.1f, 0.0f, 0.0f);
             box.setPosition(new Vector3(user.getPosition().x, user.getPosition().y, user.getPosition().z));
             camera.translate(-0.1f, 0.0f, 0.0f);
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
             user.translate(0.0f, 0.0f, -0.1f);
             box.setPosition(new Vector3(user.getPosition().x, user.getPosition().y, user.getPosition().z));
             camera.translate(0.0f, 0.0f, 0.1f);
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
             user.translate(0.0f, 0.0f, 0.1f);
             box.setPosition(new Vector3(user.getPosition().x, user.getPosition().y, user.getPosition().z));
             camera.translate(0.0f, 0.0f, -0.1f);
         }
 
 
         if (Keyboard.isKeyDown(Keyboard.KEY_X)) {
             box.setPosition(new Vector3(user.getPosition().x, user.getPosition().y + 0.05, user.getPosition().z));
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
             box.setPosition(new Vector3(user.getPosition().x, user.getPosition().y - 0.05, user.getPosition().z));
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
             currBomb = null;
             Body new_bomb = new Body("bomb", new Box(0.1f, 0.1f, 0.1f));
             scene.addBody(new_bomb);
 
             currBomb = new_bomb;
 
             scene.addTrigger(new ContactTrigger(currBomb, 0.000001, new ContactTrigger.Callback() {
                 @Override
                 public void contactAboveThreshold(jinngine.physics.Body body, ContactConstraint contactConstraint) {
                     System.out.println("In contact with " + body);
                 }
 
                 @Override
                 public void contactBelowThreshold(jinngine.physics.Body body, ContactConstraint contactConstraint) {
                     System.out.println("Lost contact with " + body);
                 }
             }));
 
             scene.addForce(new GravityForce(new_bomb));
             new_bomb.setPosition(new Vector3(box.getPosition().x,box.getPosition().y-0.3, box.getPosition().z));
             bombReleased = true;
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_A)) {}
         if (Keyboard.isKeyDown(Keyboard.KEY_E)) {}
         if (Keyboard.isKeyDown(Keyboard.KEY_D)) {}
         if (Keyboard.isKeyDown(Keyboard.KEY_W)) {}
         if (Keyboard.isKeyDown(Keyboard.KEY_S)) {}
     }
 }
