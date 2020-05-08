 package main;
 
 import jinngine.collision.SAP2;
 import jinngine.math.Vector3;
 import jinngine.physics.Body;
 import jinngine.physics.DefaultDeactivationPolicy;
 import jinngine.physics.DefaultScene;
 import jinngine.physics.Scene;
 import jinngine.physics.force.GravityForce;
 import jinngine.physics.solver.NonsmoothNonlinearConjugateGradient;
 import models.House;
 import models.MainCamera;
 import models.Terrain;
 import models.UserObject;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.Sys;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.PixelFormat;
 import org.lwjgl.util.glu.GLU;
 
 import java.util.ArrayList;
 
 public class Main {
 
     private MainCamera camera;
     private Terrain terrain;
     private UserObject user;
     private Scene scene;
     private Body box;
     private ArrayList<House> houses = new ArrayList<House>();
 
     public static void main(String[] args) {
         Main main = new Main();
         main.startLoop();
     }
 
     private void startLoop() {
 //        if (JOptionPane.showConfirmDialog(null, "Would You Like To Run In Fullscreen Mode?",
 //                "Start Fullscreen?", JOptionPane.YES_NO_OPTION) == 1) {
 //            Settings.fullScreen = false;
 //        }
         Settings.fullScreen = false;
 
 
         try {
             if (!CreateGLWindow(Settings.windowTitle, Settings.windowWidth, Settings.windowHeight, Settings.fullScreen)) {
                 // Quit If Window Was Not Created
                 throw new Exception();
             }
             initializeObjects();
             //hide the mouse
 //            Mouse.setGrabbed(true);
 
             while (!Keyboard.isKeyDown(Settings.exitKey) && !Display.isCloseRequested()) {
                 resetDisplay();
                 if (!Display.isActive()) {
                     // Quit if told to
                     break;
                 }
                 render();
                 processInput();
                 applyPhysics();
 
                 // Toggle Fullscreen / Windowed Mode
                 if (Keyboard.isKeyDown(Settings.changeWindowModeKey)) {
                     Settings.fullScreen = !Settings.fullScreen;
                     Display.setFullscreen(Settings.fullScreen);
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
         for(House house: houses) {
             house.render3D();
         }
         System.out.printf("%f, %f, %f\n",camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
     }
 
     private void applyPhysics() {
         scene.tick();
         user.setPosition((float) box.getPosition().x, (float) box.getPosition().y, (float) box.getPosition().z);
     }
 
     private void initializeObjects() {
         scene = new DefaultScene(new SAP2(), new NonsmoothNonlinearConjugateGradient(44), new DefaultDeactivationPolicy());
         scene.setTimestep(0.01);
 
         camera = new MainCamera();
        camera.translate(0.0f, -2.0f, -5.0f);
 
         terrain = new Terrain();
         terrain.scale(50.0f, 1000.0f, 1000.0f);
         terrain.translate(0.0f, -0.5f, 0.0f);
 
         float[] position = {0, 0, 0};
         float[] width;
         for(int i=0; i<10; i++) {
             // left house
             House h = new House(new float[]{1.0f,0,0});
             width = new float[]{1.0f, (float) Math.random()*1+Settings.minimalHouseHeight, (float) Math.random()*1};
 
             // position house
             h.scale(width[0],width[1],width[2]);
             h.translate(position[0],position[1],position[2]);
 
             // add house for collision detection
             Body house = new Body("house", new jinngine.geometry.Box(width[0],width[1],width[2]));
             house.setPosition(new Vector3(position[0],position[1],position[2]));
             house.setFixed(true);
             scene.addBody(house);
             position[0] = width[0];
             position[1] = width[1];
             position[2] = width[2];
             houses.add(h);
         }
 
        Body floor = new Body("floor", new jinngine.geometry.Box(100,1, 100));
        floor.setPosition(new Vector3(0,0,0));
         floor.setFixed(true);
 
         Body back = new Body( "back", new jinngine.geometry.Box(50,10,100));
         back.setPosition(new Vector3(0,0,-1000));
         back.setFixed(true);
 
         Body front = new Body( "front", new jinngine.geometry.Box(50,100,10));
         front.setPosition(new Vector3(0,0,1000));
         front.setFixed(true);
 
         Body left = new Body( "left", new jinngine.geometry.Box(10,100,1000));
         left.setPosition(new Vector3(-50,0,0));
         left.setFixed(true);
 
         Body right = new Body( "right", new jinngine.geometry.Box(10,100,1000));
         right.setPosition(new Vector3(50,0,0));
         right.setFixed(true);
 
         user = new UserObject();
         user.scale(0.3f, 0.3f, 0.3f);
         user.translate(0.0f, 1.0f, 0.0f);
 
         box = new Body( "box", new jinngine.geometry.Box(0.3f,0.3f,0.3f) );
         box.setPosition(new Vector3(0.0f, 1.0f, 0.0f));
 
         // add all to scene
         scene.addBody(floor);
         scene.addBody(back);
         scene.addBody(front);
         scene.addBody(left);
         scene.addBody(right);
         scene.addBody(box);
 
         scene.addForce(new GravityForce(box));
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
             user.translate(-0.1f, 0.0f, 0.0f);
             box.setPosition(new Vector3(user.getPosition().x, user.getPosition().y, user.getPosition().z));
             camera.translate(0.1f, 0.0f, 0.0f);
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
 
 
         if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
             box.setPosition(new Vector3(user.getPosition().x, user.getPosition().y + 0.05, user.getPosition().z));
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {}
         if (Keyboard.isKeyDown(Keyboard.KEY_A)) {}
         if (Keyboard.isKeyDown(Keyboard.KEY_E)) {}
         if (Keyboard.isKeyDown(Keyboard.KEY_D)) {}
         if (Keyboard.isKeyDown(Keyboard.KEY_W)) {}
         if (Keyboard.isKeyDown(Keyboard.KEY_S)) {}
     }
 }
