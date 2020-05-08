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
 import org.lwjgl.util.vector.Vector3f;
 import org.newdawn.slick.opengl.Texture;
 
 import java.util.ArrayList;
 
 import static main.Settings.*;
 import static main.Utilities.loadTextures;
 import static org.lwjgl.opengl.GL11.*;
 
 public class Main {
 
     private MainCamera camera;
     private Terrain terrain;
     private UserObject user;
     private jinngine.physics.Scene scene;
     private jinngine.physics.Body box;
     private ArrayList<House> houses = new ArrayList<House>();
     private ArrayList<Zombie> liveZombies = new ArrayList<Zombie>();
     private ArrayList<DeadZombie> deadZombies = new ArrayList<DeadZombie>();
 
     private ArrayList<Char> zombiesKilled = new ArrayList<Char>();
 
     private ArrayList<Bomb> bombs = new ArrayList<Bomb>();
 
     private long bombTimer = System.currentTimeMillis();
     private long zombieIncreaseTimer = System.currentTimeMillis();
     private int zombieID = 0;
 
     private int numberOfZombiesEscaped = 0;
     private int numberOfZombiesKilled = 0;
     private int numberOfZombiesAtOnce = 1;
 
     private float lengthOfCity;
     private int specialWeaponNumber = 0;
 
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
         Display.setTitle(windowTitle +  " " + version);
         ReSizeGLScene(windowWidth, windowHeight);
         // Enable Smooth Shading
         glShadeModel(GL_SMOOTH);
         // Don't render hidden faces
         glEnable(GL_CULL_FACE);
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
 
     private void addZombie() {
         if (System.currentTimeMillis() - zombieIncreaseTimer >= zombieIncreaseInterval) {
             numberOfZombiesAtOnce += 5;
             zombieIncreaseTimer = System.currentTimeMillis();
         }
 
         if (liveZombies.size() < numberOfZombiesAtOnce) {
            zombieID = (zombieID == Integer.MAX_VALUE) ? 0 : zombieID+1;
             // TODO: Zombie texture?
             Zombie zombie = new Zombie(
                     new Body("Zombie" + zombieID, new Box(zombieSize * 2, zombieSize * 2, zombieSize * 2)),
                     loadTextures("RGTI_zombies/textures/zombie.png")
             );
             zombie.scale(zombieSize, zombieSize, zombieSize);
 
             zombie.getBody().setPosition(new Vector3(
                     (float) Math.random() * mainRoadWidth,
                     0,
                     user.getPosition().z - 30 - Math.random() * 40
             ));
 
             scene.addForce(new GravityForce(zombie.getBody()));
             scene.addBody(zombie.getBody());
 
             liveZombies.add(zombie);
         }
 
         for(Zombie z: liveZombies) {
             z.getBody().setPosition(new Vector3(
                     z.getBody().getPosition().x,
                     z.getBody().getPosition().y,
                     z.getBody().getPosition().z + zombieObjectSpeed
             ));
         }
         removeUnseenZombies();
     }
 
     private void removeUnseenZombies(){
         for (int i = 0; i < liveZombies.size(); i++) {
             if (liveZombies.get(i).getBody().getPosition().z > user.getPosition().z+15) {
                 scene.removeBody(liveZombies.get(i).getBody());
                 liveZombies.remove(i--);
                 numberOfZombiesEscaped++;
             }
         }
     }
 
     private void resetDisplay() {
         glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
         glClearColor(0, 1, 1, 1);
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
         for(Zombie zombie: liveZombies) {
             zombie.render3D();
         }
         for(DeadZombie zombie: deadZombies) {
             zombie.render3D();
         }
         for(Char c: zombiesKilled) {
             c.render3D();
         }
     }
 
     private void applyPhysics() {
         try {
             scene.tick();
             user.setPosition(
                     (float) box.getPosition().x,
                     (float) box.getPosition().y,
                     (float) box.getPosition().z
             );
 
             for (Bomb bomb : bombs) {
                 bomb.setPosition(
                         (float) bomb.getBody().getPosition().x,
                         (float) bomb.getBody().getPosition().y,
                         (float) bomb.getBody().getPosition().z
                 );
             }
 
             for (Zombie zombie : liveZombies) {
                 zombie.setPosition(
                         (float) zombie.getBody().getPosition().x,
                         (float) zombie.getBody().getPosition().y,
                         (float) zombie.getBody().getPosition().z);
             }
         } catch (Exception e) {
             System.err.print("ConcurrentModificationException");
         }
     }
 
     private void initializeObjects() {
         scene = new DefaultScene(new SAP2(), new NonsmoothNonlinearConjugateGradient(44), new DefaultDeactivationPolicy());
 
         scene.setTimestep(0.01);
 
         lengthOfCity = initializeHouses();
 
         terrain = new Terrain(loadTextures("RGTI_zombies/textures/background.png"));
         terrain.scale(mainRoadWidth / 2, 0, lengthOfCity / 2);
         terrain.translate(mainRoadWidth / 2, -0.5f, lengthOfCity / 2);
 
         // TODO: Move user higher and camera lower, user object texture?
         user = new UserObject(loadTextures("RGTI_zombies/textures/user.png"));
         user.scale(userSize[0], userSize[1], userSize[2]);
         user.translate(mainRoadWidth / 2.0f, 1.0f, -10.0f);
 
         camera = new MainCamera();
         camera.translate(-user.getPosition().x, -user.getPosition().y - 4.0f, -user.getPosition().z - 20.0f);
 
         box = new Body("box", new Box(0.5f, 0.5f, 0.5f));
        box.setPosition(new Vector3(mainRoadWidth / 2.0f, 5.0f, -10.0f));
         box.setFixed(true);
 
         // Zombies killed
         numberOfZombiesKilled = 1234567890;
         float position = 0.5f;
         for(char c: Integer.toString(numberOfZombiesKilled).toCharArray()) {
             Char newChar = new Char(loadTextures("RGTI_zombies/textures/numbers/"+c+".png"));
             zombiesKilled.add(newChar);
             newChar.scale(0.5f, 0.5f, 0.5f);
             newChar.translate(position++, 1.0f, -15.0f);
         }
 
         addToScene();
     }
 
     private float initializeHouses() {
         float[] positionLeft = {-minHouseWidth, 0, 0}, positionRight = {mainRoadWidth + minHouseWidth, 0, 0};
         float[] width;
         House house;
 
         Texture houseTexture = loadTextures("RGTI_zombies/textures/houseWall.png");
 
         for (int i = 0; i < 200; i++) {
             // LEFT HOUSE
             house = new House(new float[]{(float) Math.random(), (float) Math.random(), (float) Math.random()}, houseTexture);
             width = new float[]{
                     minHouseWidth,
                     (float) Math.random() * houseHeightBounds + minimalHouseHeight,
                     (float) Math.random() * houseLengthBounds + minimalHouseLength
             };
             // position house
             positionLeft[2] -= width[2];
             house.scale(width[0], width[1], width[2]);
             house.translate(positionLeft[0], width[1] - 2, positionLeft[2]);
             houses.add(house);
             positionLeft[2] -= width[2] + spaceBetweenHouses;
 
             // RIGHT HOUSE
             house = new House(new float[]{(float) Math.random(), (float) Math.random(), (float) Math.random()}, houseTexture);
             width = new float[]{
                     minHouseWidth,
                     (float) Math.random() * houseHeightBounds + minimalHouseHeight,
                     (float) Math.random() * houseLengthBounds + minimalHouseLength
             };
             // position house
             positionRight[2] -= width[2];
             house.scale(width[0], width[1], width[2]);
             house.translate(positionRight[0], width[1] - 2, positionRight[2]);
             houses.add(house);
             positionRight[2] -= width[2] + spaceBetweenHouses;
         }
         return Math.min(positionLeft[2], positionRight[2]);
     }
 
     private void addToScene() {
         Body leftHouses = new Body("leftHouses", new Box(minHouseWidth, minimalHouseHeight, -lengthOfCity));
         leftHouses.setPosition(new Vector3(-minHouseWidth+0.6, 0, 0));
         leftHouses.setFixed(true);
 
         Body rightHouses = new Body("rightHouses", new Box(minHouseWidth, minimalHouseHeight, -lengthOfCity));
         rightHouses.setPosition(new Vector3(mainRoadWidth+minHouseWidth-0.6, 0, 0));
         rightHouses.setFixed(true);
 
         Body floor = new Body("floor", new Box(1000, 5, 10000));
         floor.setPosition(new Vector3(0, -3, 0));
         floor.setFixed(true);
         scene.addBody(floor);
         scene.addBody(leftHouses);
         scene.addBody(rightHouses);
     }
 
     protected void processInput() {
         user.rotate(0,5f,0);
         if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) && (user.getPosition().x > user.getScale().y || godMode)) {
             user.translate(-0.1f, 0.0f, 0.0f);
             box.setPosition(new Vector3(user.getPosition().x, user.getPosition().y, user.getPosition().z));
             camera.translate(0.1f, 0.0f, 0.0f);
             // Is the user far enough from both buildings not to collide
             if(user.getPosition().x > user.getScale().x && user.getPosition().x < mainRoadWidth - user.getScale().x) {
                 user.setRotation(new Vector3f(user.getRotation().x, user.getRotation().y, 0));
             } else {
                 user.setRotation(new Vector3f(0, 0, 90));
             }
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) && (user.getPosition().x < mainRoadWidth - user.getScale().y || godMode)) {
             user.translate(0.1f, 0.0f, 0.0f);
             box.setPosition(new Vector3(user.getPosition().x, user.getPosition().y, user.getPosition().z));
             camera.translate(-0.1f, 0.0f, 0.0f);
             // Is the user far enough from both buildings not to collide
             if(user.getPosition().x < mainRoadWidth - user.getScale().x && user.getPosition().x > user.getScale().x) {
                 user.setRotation(new Vector3f(user.getRotation().x, user.getRotation().y, 0));
             } else {
                 user.setRotation(new Vector3f(0, 0, 90));
             }
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
             user.translate(0.0f, 0.0f, -0.1f);
             box.setPosition(new Vector3(user.getPosition().x, user.getPosition().y, user.getPosition().z));
             camera.translate(0.0f, 0.0f, 0.1f);
 
             if (user.getPosition().z <= lengthOfCity + 30) {
                 float distanceBetweenCameraAndUser = user.getPosition().z + camera.getPosition().z;
                 user.setPosition(user.getPosition().x,user.getPosition().y,0f);
                 camera.setPosition(camera.getPosition().x,camera.getPosition().y,0f);
                 user.translate(0f, 0f, -10.0f);
                 camera.translate(0f, 0f, -user.getPosition().z + distanceBetweenCameraAndUser);
                 box.setPosition(user.getPosition().x,user.getPosition().y,user.getPosition().z);
                 liveZombies.clear();
             }
 
         }

         if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) && (user.getPosition().z < -10)) {
             user.translate(0.0f, 0.0f, 0.1f);
             box.setPosition(new Vector3(user.getPosition().x, user.getPosition().y, user.getPosition().z));
             camera.translate(0.0f, 0.0f, -0.1f);
         }
 
 
         if (Keyboard.isKeyDown(Keyboard.KEY_X) && !Keyboard.isKeyDown(Keyboard.KEY_Z)) {
             box.setPosition(new Vector3(user.getPosition().x, user.getPosition().y + 0.05, user.getPosition().z));
             camera.translate(0.0f, -0.05f, -0.1f);
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_Z) && user.getPosition().y >= 0.5f && !Keyboard.isKeyDown(Keyboard.KEY_X)) {
             box.setPosition(new Vector3(user.getPosition().x, user.getPosition().y - 0.05, user.getPosition().z));
             camera.translate(0.0f, 0.05f, 0.1f);
         }
         // TODO: something not working with nukes
         // Special weapon 1, NUKE
         if(Keyboard.isKeyDown(Keyboard.KEY_1) && maxNukes > 0) {
             specialWeaponNumber = 1;
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
 
             if (bombs.size() > 20) {
                 scene.removeBody(bombs.get(0).getBody());
                 bombs.remove(0);
             }
 
             if ((System.currentTimeMillis() - bombTimer) <= 1000 / maxBombThrowingSpeed) {
                 return;
             }
             bombTimer = System.currentTimeMillis();
 
             float bombSizeTemp = bombSize;
             if(specialWeaponNumber == 1) {
                 bombSizeTemp = nukeSize;
                maxNukes --;
             }
             specialWeaponNumber = 0;
             // TODO: bomSizeTemp scale and body doesn't work properly and implement Sphere instead of Box
            Bomb bomb = new Bomb(new Body("Bomb" + bombs.size(), new Box(bombSizeTemp, bombSizeTemp, bombSizeTemp)));
            bomb.scale(bombSizeTemp, bombSizeTemp, bombSizeTemp);
             bomb.translate(5.0f, 1.0f, 0.0f);
 
            scene.addTrigger(new ContactTrigger(bomb.getBody(), 0.000001, new ContactTrigger.Callback() {
                 @Override
                 public void contactAboveThreshold(jinngine.physics.Body body, ContactConstraint contactConstraint) {
 
                     for (Zombie z : liveZombies) {
                         if (z.getBody().identifier.equals(body.identifier)) {
                             int currZombieHealth = z.getZombieHealth() - bombMaxPower;
                             if (currZombieHealth == 0) {
                                 scene.removeBody(z.getBody());
 
                                 DeadZombie deadZombie = new DeadZombie();
                                 deadZombie.scale(z.getScale().x * 5, z.getScale().y, z.getScale().z * 5);
                                 deadZombie.setPosition(z.getPosition().x - deadZombie.getScale().x / 2, terrain.getPosition().y + 0.01f, z.getPosition().z);
                                 deadZombies.add(deadZombie);
 
                                 numberOfZombiesKilled++;
                                 liveZombies.remove(z);
                             } else {
                                 z.setZombieHealth(currZombieHealth);
                             }
                             break;
                         }
                     }
                 }
 
                 @Override
                 public void contactBelowThreshold(jinngine.physics.Body body, ContactConstraint contactConstraint) {}
             }));
 
             scene.addForce(new GravityForce(bomb.getBody()));
             scene.addBody(bomb.getBody());
             bomb.getBody().setPosition(new Vector3(
                     box.getPosition().x,
                     box.getPosition().y - 0.3,
                     box.getPosition().z
             ));
             bombs.add(bomb);
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_A)) {}
         if (Keyboard.isKeyDown(Keyboard.KEY_E)) {}
         if (Keyboard.isKeyDown(Keyboard.KEY_D)) {}
         if (Keyboard.isKeyDown(Keyboard.KEY_W)) {}
         if (Keyboard.isKeyDown(Keyboard.KEY_S)) {}
     }
 }
