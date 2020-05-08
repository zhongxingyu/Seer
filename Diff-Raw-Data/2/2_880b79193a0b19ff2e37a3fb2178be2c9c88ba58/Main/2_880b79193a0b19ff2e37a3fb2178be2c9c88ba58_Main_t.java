 package main;
 
 import jinngine.collision.SAP2;
 import jinngine.geometry.Box;
 import jinngine.math.Vector3;
 import jinngine.physics.Body;
 import jinngine.physics.ContactTrigger;
 import jinngine.physics.DefaultDeactivationPolicy;
 import jinngine.physics.DefaultScene;
 import jinngine.physics.constraint.contact.ContactConstraint;
 import jinngine.physics.force.GravityForce;
 import jinngine.physics.solver.NonsmoothNonlinearConjugateGradient;
 import models.*;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.PixelFormat;
 import org.lwjgl.util.glu.GLU;
 
 import java.util.ArrayList;
 
 import static main.Settings.*;
 import static org.lwjgl.opengl.GL11.*;
 
 public class Main {
 
     private MainCamera camera;
     private Terrain terrain;
     private UserObject user;
     private jinngine.physics.Scene scene;
     private jinngine.physics.Body box;
     private ArrayList<House> houses = new ArrayList<House>();
     private ArrayList<Zombie> zombies = new ArrayList<Zombie>();
     private ArrayList<Bomb> bombs = new ArrayList<Bomb>();
 
     private long bombTimer;
     private long zombieIncreaseTimer;
     private  int bombThrowingSpeed = maxBombThrowingSpeed;
     private int zombieIDCounter = 0;
 
     private int numberOfZombiesEscaped = 0;
     private int numberOfZombiesKilled = 0;
     private int numberOfZombiesAtOnce = 1;
 
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
             bombTimer = System.currentTimeMillis();
             zombieIncreaseTimer = System.currentTimeMillis();
 
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
                 addZombie();
                 Display.update();
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             Display.destroy();
         }
         System.exit(0);
     }
 
     private void addZombie() {
 
         if (System.currentTimeMillis() - zombieIncreaseTimer >= 1000*zombieIncreaseInterval) {
             numberOfZombiesAtOnce++;
             zombieIncreaseTimer = System.currentTimeMillis();
         }
 
         if (zombies.size() < numberOfZombiesAtOnce) {
             zombieIDCounter = (zombieIDCounter == Integer.MAX_VALUE) ? 0 : zombieIDCounter+1;
             Zombie zombie = new Zombie(new Body(Integer.toString(zombieIDCounter), new Box(0.5, 0.5, 0.5)));
             zombie.scale(0.3f, 0.3f, 0.3f);
 
             float zombieX = (float) Math.random() * mainRoadWidth;
             float zombieZ = (float) Math.random() * 40;
 
             zombieX += (minHouseWidth*1.5f);
 
             zombieX = Math.min(zombieX, mainRoadWidth - (minHouseWidth * 1.5f));
 
 
             zombie.getZombieBody().setPosition(new Vector3(zombieX, 0.1, user.getPosition().z - 30 - zombieZ));
 
             scene.addForce(new GravityForce(zombie.getZombieBody()));
             scene.addBody(zombie.getZombieBody());
 
             zombies.add(zombie);
         }
 
         for(Zombie z: zombies) {
             z.getZombieBody().setPosition(new Vector3(z.getZombieBody().getPosition().x, z.getZombieBody().getPosition().y, z.getZombieBody().getPosition().z+(0.01*zombieObjectSpeed)));
         }
         removeUnseenZombies();
     }
 
     private void removeUnseenZombies(){
         for (int i = 0; i < zombies.size(); i++) {
             if (zombies.get(i).getZombieBody().getPosition().z > user.getPosition().z+15) {
                 scene.removeBody(zombies.get(i).getZombieBody());
                 zombies.remove(i);
                 numberOfZombiesEscaped++;
                 i--;
             }
         }
     }
 
     private void resetDisplay() {
         glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
         glClearColor(1, 1, 1, 1);
     }
 
     private void render() {
         camera.render3D();
         terrain.render3D();
         user.render3D();
 
         for (Bomb bomb : bombs) {
             bomb.render3D();
         }
         for(House house: houses) {
             house.render3D();
         }
         for(Zombie zombie: zombies) {
             zombie.render3D();
         }
     }
 
     private void applyPhysics() {
         scene.tick();
         user.setPosition((float) box.getPosition().x, (float) box.getPosition().y, (float) box.getPosition().z);
 
         for (Bomb bomb : bombs) {
             bomb.setPosition((float) bomb.getBombBody().getPosition().x, (float) bomb.getBombBody().getPosition().y, (float) bomb.getBombBody().getPosition().z);
         }
 
         for (Zombie zombie : zombies) {
             zombie.setPosition((float) zombie.getZombieBody().getPosition().x, (float) zombie.getZombieBody().getPosition().y, (float) zombie.getZombieBody().getPosition().z);
         }
     }
 
     private void initializeObjects() {
         scene = new DefaultScene(new SAP2(), new NonsmoothNonlinearConjugateGradient(44), new DefaultDeactivationPolicy());
 
         scene.setTimestep(0.01);
 
         terrain = new Terrain();
         terrain.scale(50.0f, 1000.0f, 1000.0f);
         terrain.translate(0.0f, -0.5f, 0.0f);
 
         float[] positionLeft = {0, 0, 0}, positionRight = {0, 0, 0};
         float[] width;
         House house;
         for (int i = 0; i < 200; i++) {
             // LEFT HOUSE
             house = new House(new float[]{(float) Math.random() * 1, (float) Math.random() * 1, (float) Math.random() * 1});
             width = new float[]{
                     minHouseWidth,
                     (float) Math.random() * houseHeightBounds + minimalHouseHeight,
                     (float) Math.floor(Math.random() * houseLengthBounds + minimalHouseLength)
             };
             // position house
             positionLeft[2] -= width[2];
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
             positionRight[2] -= width[2];
             house.scale(width[0], width[1], width[2]);
             house.translate(positionRight[0] + mainRoadWidth, positionRight[1], positionRight[2]);
             houses.add(house);
             positionRight[2] -= width[2] + spaceBetweenHouses;
         }
 
         Body leftHouses = new Body("leftHouses", new Box(20, minimalHouseHeight, 10000));
         leftHouses.setPosition(new Vector3(0 - 9, 0, 0));
         leftHouses.setFixed(true);
 
         Body rightHouses = new Body("rightHouses", new Box(20, minimalHouseHeight, 10000));
         rightHouses.setPosition(new Vector3(mainRoadWidth + 9, 0, 0));
         rightHouses.setFixed(true);
 
         Body floor = new Body("floor", new Box(1000, 5, 10000));
         floor.setPosition(new Vector3(0, -3, 0));
         floor.setFixed(true);
 
         Body back = new Body("back", new Box(50, 10, 100));
         back.setPosition(new Vector3(0, 0, -1000));
         back.setFixed(true);
 
         Body front = new Body("front", new Box(50, 100, 10));
         front.setPosition(new Vector3(0, 0, 10000));
         front.setFixed(true);
 
         Body left = new Body("left", new Box(10, 100, 1000));
         left.setPosition(new Vector3(-50, 0, 0));
         left.setFixed(true);
 
         Body right = new Body("right", new Box(10, 100, 1000));
         right.setPosition(new Vector3(50, 0, 0));
         right.setFixed(true);
 
         user = new UserObject();
         user.scale(0.3f, 0.3f, 0.4f);
         user.translate((mainRoadWidth/2.0f), 1.0f, -10.0f);
 
         camera = new MainCamera();
         camera.translate(-user.getPosition().x, -user.getPosition().y - 4.0f, -user.getPosition().z - 16.0f);
 
         box = new Body("box", new Box(0.5f, 0.5f, 0.5f));
         box.setPosition(new Vector3((mainRoadWidth/2.0f), 1.0f, -10.0f));
         box.setFixed(true);
 
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
         // Enable Texture Loading
         glEnable(GL_TEXTURE_2D);
         // Enable Smooth Shading
         glShadeModel(GL_SMOOTH);
         // Depth Buffer Setup
         glClearDepth(1.0f);
         // Enables Depth Testing
         glEnable(GL_DEPTH_TEST);
         // The Type Of Depth Testing To Do
         glDepthFunc(GL_LEQUAL);
         // Really Nice Perspective Calculations
         glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
         return true;
     }
 
     private void ReSizeGLScene(int windowWidth, int windowHeight) {
         if (windowHeight == 0) {
             windowHeight = 1;
         }
         // Reset The Current Viewport
         glViewport(0, 0, windowWidth, windowHeight);
 
         // Select The Projection Matrix
         glMatrixMode(GL_PROJECTION);
         // Reset The Projection Matrix
         glLoadIdentity();
 
         // Calculate The Aspect Ratio Of The Window
         GLU.gluPerspective(45.0f, (float) windowWidth / (float) windowHeight, 0.1f, 60.0f);
 
         // Select The Modelview Matrix
         glMatrixMode(GL_MODELVIEW);
         // Reset The Modelview Matrix
         glLoadIdentity();
     }
 
     protected void processInput() {
 
         if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) && (user.getPosition().x > (minHouseWidth*1.5f))) {
             user.translate(-0.1f, 0.0f, 0.0f);
             box.setPosition(new Vector3(user.getPosition().x, user.getPosition().y, user.getPosition().z));
             camera.translate(0.1f, 0.0f, 0.0f);
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) && user.getPosition().x < (mainRoadWidth - (minHouseWidth * 1.5f))) {
             user.translate(0.1f, 0.0f, 0.0f);
             box.setPosition(new Vector3(user.getPosition().x, user.getPosition().y, user.getPosition().z));
             camera.translate(-0.1f, 0.0f, 0.0f);
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
             user.translate(0.0f, 0.0f, -0.1f);
             box.setPosition(new Vector3(user.getPosition().x, user.getPosition().y, user.getPosition().z));
             camera.translate(0.0f, 0.0f, 0.1f);
 
             if (user.getPosition().z <= -1000) {
                 float distanceBetweenCameraAndUser = user.getPosition().z + camera.getPosition().z;
                 user.setPosition(user.getPosition().x,user.getPosition().y,0f);
                 camera.setPosition(camera.getPosition().x,camera.getPosition().y,0f);
                 user.translate(0f, 0f, -10.0f);
                 camera.translate(0f, 0f, -user.getPosition().z + distanceBetweenCameraAndUser);
                 box.setPosition(user.getPosition().x,user.getPosition().y,user.getPosition().z);
                zombies.clear();
             }
 
         }
         if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) && (user.getPosition().z < -10)) {
             user.translate(0.0f, 0.0f, 0.1f);
             box.setPosition(new Vector3(user.getPosition().x, user.getPosition().y, user.getPosition().z));
             camera.translate(0.0f, 0.0f, -0.1f);
         }
 
 
         if (Keyboard.isKeyDown(Keyboard.KEY_X)) {
             box.setPosition(new Vector3(user.getPosition().x, user.getPosition().y + 0.05, user.getPosition().z));
             camera.translate(0.0f, -0.05f, -0.1f);
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_Z) && user.getPosition().y >= 0.5f) {
             box.setPosition(new Vector3(user.getPosition().x, user.getPosition().y - 0.05, user.getPosition().z));
             camera.translate(0.0f, 0.05f, 0.1f);
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
 
             if (bombs.size() > 20) {
                 scene.removeBody(bombs.get(0).getBombBody());
                 bombs.remove(0);
             }
 
             if ((System.currentTimeMillis() - bombTimer) <= (1000/bombThrowingSpeed)) {
                 return;
             }
             bombTimer = System.currentTimeMillis();
 
             Bomb bomb = new Bomb(new Body(Integer.toString(bombs.size()), new Box(0.1, 0.1, 0.1)));
             bomb.scale(0.1f, 0.1f, 0.1f);
             bomb.translate(5.0f, 1.0f, 0.0f);
 
             scene.addTrigger(new ContactTrigger(bomb.getBombBody(), 0.0001, new ContactTrigger.Callback() {
                 @Override
                 public void contactAboveThreshold(jinngine.physics.Body body, ContactConstraint contactConstraint) {
 
                     for (Zombie z : zombies) {
                         if (z.getZombieBody().identifier.equals(body.identifier)) {
                             int currZombieHealth = z.getZombieCurrentHealth() - bombMaxPower;
                             if (currZombieHealth == 0) {
                                 scene.removeBody(z.getZombieBody());
                                 zombies.remove(z);
                                 numberOfZombiesKilled++;
                             } else {
                                 z.setZombieCurrentHealth(currZombieHealth);
                             }
                             break;
                         }
                     }
                 }
 
                 @Override
                 public void contactBelowThreshold(jinngine.physics.Body body, ContactConstraint contactConstraint) {
                 }
             }));
 
             scene.addForce(new GravityForce(bomb.getBombBody()));
             scene.addBody(bomb.getBombBody());
             bomb.getBombBody().setPosition(new Vector3(box.getPosition().x,box.getPosition().y-0.3, box.getPosition().z));
             bombs.add(bomb);
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_A)) {}
         if (Keyboard.isKeyDown(Keyboard.KEY_E)) {}
         if (Keyboard.isKeyDown(Keyboard.KEY_D)) {}
         if (Keyboard.isKeyDown(Keyboard.KEY_W)) {}
         if (Keyboard.isKeyDown(Keyboard.KEY_S)) {}
     }
 }
