 package screens;
 
 import ai.AI;
 import display.Camera;
 import display.HUD;
 import display.Ray;
 import entity.Enemy;
 import entity.Entity;
 import entity.LaserBeam;
 import entity.Player;
 import environment.*;
 import helpers.Delegate;
 import helpers.GLHelper;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.glu.GLU;
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector2f;
 import org.lwjgl.util.vector.Vector3f;
 import org.lwjgl.util.vector.Vector4f;
 import org.newdawn.slick.openal.AudioLoader;
 import org.newdawn.slick.util.ResourceLoader;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import static org.lwjgl.opengl.GL11.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: hp
  * Date: 23/04/13
  * Time: 3:10 PM
  * To change this template use File | Settings | File Templates.
  */
 
 public class GameplayScreen extends Screen {
     //TODO: get rid of these
     static boolean tempEnemyCollide = false;
     static boolean tempTerrainCollide = false;
     static boolean lastFired = false;
     Camera cam;
     Terrain terrain;
     Player player;
     Background background;
     Light l;
     Laser laser1;
     ArrayList<LaserBeam> lasers;
     ArrayList<Explosion> explosions;
     HashMap<Byte, Enemy> enemies = new HashMap<Byte, Enemy>();
 
     public GameplayScreen(Delegate d) {
         super(d);
         cam = new Camera(new Vector3f(0, 0, 0));
     }
 
     public void Initialize() {
         // Enable Texture 2D for texturing models
         glEnable(GL_TEXTURE_2D);
         // Enable depth testing for correct drawing order
         glEnable(GL_DEPTH_TEST);
 
         // Create a light
         float lightDirection[] = {-2f, 12f, 2f, 0f}; //direction/position
         float diffuse[] = {16f, 16f, 16f, 16f};
         float ambient[] = {56.6f, 56.6f, 56.9f, 57f};
         float specular[] = {6f, 6f, 6f, 6f};
         l = new Light(lightDirection, diffuse, ambient, specular);
         l.setLight();
 
         //Start Gamescreen music
         killAudio();
         try {
             audio = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("music/36-VersusMode.wav"));
         } catch (IOException e) {
             e.printStackTrace();
         }
         if (audioOn)
             audio.playAsMusic(1.0f, 1.0f, true);
 
         //Create Background
         background = new Background();
         //load the model
         player = new Player(new Model("data/Arwing/arwing.obj", 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -5.0f));
         enemies = new HashMap<Byte, Enemy>();
         enemies.put((byte) -1, new Enemy(new Model("data/DarkFighter/dark_fighter.obj", 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -7.5f), new AI()));
        terrain = new Terrain("data/terrain/terrain.obj", 20, 0.0f, 0.0f, 0.0f, 0.0f, -10.0f, 0.0f);
         laser1 = new Laser(cam);
 
         lasers = new ArrayList<LaserBeam>();
         explosions = new ArrayList<Explosion>();
 
         enemies.get((byte) -1).Initialize();
     }
 
     public void Render() {
         // Clear colour and depth buffers
         glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 
         glMatrixMode(GL_PROJECTION);
 
         //Player position accounting for offset and camera position
         Vector3f playerPos = new Vector3f();
         Vector3f.sub(player.offset, cam.getPosition(), playerPos);
         player.fatalCrashPos = new Vector3f(playerPos);
 
         // true if enemy is in crosshairs
         boolean enemyInTarget = false;
         // Push the 2D projection to stack
         glPushMatrix();
         {
             //region 3D stuff
             glLoadIdentity();
             // Specify the camera perspective
             GLU.gluPerspective(45.0f, (float) Display.getWidth() / (float) Display.getHeight(), .1f, 5000f);
             glMatrixMode(GL_MODELVIEW);
 
             // Rotate camera
             cam.setCameraView();
 
             //Render Background
             background.drawSkybox(50.0f);
 
             //Render the model the camera is focusing
             if (player.state != Entity.State.Dead) {
                 glPushMatrix();
                 {
                     glLoadIdentity();
                     player.Render();
                     Vector3f vec;
                     if (tempEnemyCollide) {
                         vec = new Vector3f(0.0f, 1.0f, 0.0f);
                     } else if (tempTerrainCollide) {
                         vec = new Vector3f(0.0f, 0.0f, 1.0f);
                     } else {
                         vec = new Vector3f(1.0f, 1.0f, 1.0f);
                     }
 
                     float scale = player.model.getScaleRatio();
                     glScalef(1.0f / scale, 1.0f / scale, 1.0f / scale);
                     GLHelper.renderSphere(player.center, player.radius, vec);
                 }
                 glPopMatrix();
             }
 
             //Translate camera
             cam.setCameraPosition();
 
             glPushMatrix();
             {
                 terrain.transform();
                 terrain.render();
             }
             glPopMatrix();

             //Render active lasers and perform cleanup
             glPushMatrix();
             {
                 for (Iterator<LaserBeam> i = lasers.iterator(); i.hasNext(); ) {
                     LaserBeam laser = i.next();
                     GLHelper.renderSphere(new Vector3f(laser.getPosition().x, laser.getPosition().y, laser.getPosition().z),
                             laser.radius, new Vector3f(1, 0, 0));
                     if (laser.isExpired) {
                         i.remove();
                     } else {
                         laser.Render();
                     }
                 }
             }
             glPopMatrix();
 
             //Render active Explosions
             glPushMatrix();
             {
                 for (Iterator<Explosion> i = explosions.iterator(); i.hasNext(); ) {
                     Explosion explosion = i.next();
                     if (explosion.getAlpha() <= 0) {
                         i.remove();
                     } else {
                         explosion.render();
                     }
                 }
             }
             glPopMatrix();
 
             Vector2f chPos = player.hud.crosshairPos;
 
             tempEnemyCollide = false;
 
             //Draw other 3d models not focused by the camera and check for intersection with crosshairs
             for (Enemy enemy : enemies.values()) {
                 glPushMatrix();
                 {
                     enemy.Render();
                     if (!enemyInTarget)
                         enemyInTarget = CheckPickingRay(chPos.x + Display.getWidth() / 2, chPos.y + Display.getHeight() / 2, enemy);
                     if(enemy.isAI())
                         enemy.setTarget(playerPos);
                     if (CheckCollisionWithPlayer(enemy)) {
                         //Create explosion on collision
                         //Explosion ex = new Explosion(1, playerPos);
                         //explosions.add(ex);
                         tempEnemyCollide = true;
                         crash();
                         if (player.health > 0) {
                             player.health -= .01f;
                         } else if (player.health < 0) {
                             player.health = 0;
                         }
                     }
 
                     //Check collisions with active lasers
                     for (LaserBeam laser : lasers) {
                         if (enemies.get(laser.ownerID) != enemy && CheckCollision2(laser.getPosition(), enemy.getPosition(),
                                 laser.radius, enemy.radius)) {
 
                             //Create explosion on collision and delete laser
                             Explosion ex = new Explosion(.5f, .01f, enemy.getPosition());
                             explosions.add(ex);
                             laser.isExpired = true;
                         }
                     }
 
                     float scale = enemy.model.getScaleRatio();
                     glScalef(1.0f / scale, 1.0f / scale, 1.0f / scale);
                     GLHelper.renderSphere(enemy.center, enemy.radius, new Vector3f(1.0f, 0.0f, 0.0f));
                 }
                 glPopMatrix();
             }
 
             // Player laser collision
             for (LaserBeam laser : lasers) {
                 if (laser.ownerID != -50 && CheckCollision2(laser.getPosition(), playerPos, laser.radius, player.radius)) {
 
                     //Create explosion on collision and delete laser
                     Explosion ex = new Explosion(.5f, .01f, playerPos);
                     explosions.add(ex);
                     laser.isExpired = true;
                 }
             }
 
             //Terrain Collision
             if (terrain.checkHeightMap(playerPos, player.radius)) {
                 tempTerrainCollide = true;
                 crash();
             } else {
                 tempTerrainCollide = false;
             }
 
             glMatrixMode(GL_PROJECTION);
             //endregion
         }
         glPopMatrix();
 
         player.hud.render(enemyInTarget, player.health);
     }
 
     public void Update() {
 //        moveXYZ(0.5f, 0);
 
         //Logic to handle camera movement in different states of animation / gameplay
         if (player.state != Entity.State.FatalCrash && player.state != Entity.State.Dead) {
             rotate(0.005f, player.hud);
         }
         //Camera follows ship slightly down when destroyed
         else if (player.state == Entity.State.FatalCrash) {
             cam.setPitch(.2f);
             if (tempTerrainCollide) {
                 player.state = Entity.State.Dead;
                 Explosion ex = new Explosion(.5f, .05f, player.fatalCrashPos);
                 explosions.add(ex);
             }
         }
 
         player.Update();
         player.setOffset(cam.getYaw(), cam.getPitch(), new Vector3f(player.model.getPosition().x, player.model.getPosition().y, -player.model.getPosition().z));
 
         //Update Lasers
         for (LaserBeam laser : lasers) {
             laser.Update();
         }
         //Update Enemies
         for (byte id : enemies.keySet()) {
             Enemy enemy = enemies.get(id);
             enemy.Update();
             if(enemy.isAI() && enemy.ai.isShooting){
                 enemy.ai.resetTimer();
                 LaserBeam temp = new LaserBeam(
                         new Vector3f(-enemy.model.transX, -enemy.model.transY, -enemy.model.transZ),
                         enemy.model.yaw,
                         enemy.model.pitch,
                         id);
                 System.out.println("Enemy pitch, yaw: " +enemy.model.pitch + ", "+ enemy.model.yaw);
                 System.out.println("Laser pitch, yaw: " +temp.pitch + ", " + temp.yaw);
                 lasers.add(temp);
             }
         }
 
         //Player input
         if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
             Exit();
             return;
         }
         if (player.state != Entity.State.FatalCrash && player.state != Entity.State.Dead) {
             if (Mouse.isButtonDown(0)) {
                 if (!lastFired)
                     shootLaser();
                 lastFired = true;
             } else {
                 lastFired = false;
             }
             if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
                 moveXZ(0.2f, 90);
             }
             if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
                 moveXZ(0.2f, 270);
             }
             if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
                 moveXYZ(0.2f, 0);
             }
             if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
                 moveXYZ(0.2f, 180);
             }
         }
     }
 
     protected void rotate(float mouseSpeed, HUD hud) {
         cam.rotate(mouseSpeed, hud);
     }
 
     protected void moveXZ(float units, int dir) {
         cam.move(units, dir);
     }
 
     protected void moveXYZ(float units, int dir) {
         cam.move(units, dir);
         cam.moveUp(units, dir);
     }
 
     protected void shootLaser() {
         LaserBeam temp = new LaserBeam(cam, player.offset, player.hud);
         temp.ownerID = -50;
         lasers.add(temp);
     }
 
     private void crash() {
         //moveXYZ(5.0f, 180);
         player.crash();
     }
 
     protected void Exit() {
         delegate.change(0);
     }
 
     /**
      * Calculate a picking ray and check if it intersects with an enemy
      *
      * @param x
      * @param y
      * @param enemy
      * @return
      */
     private boolean CheckPickingRay(float x, float y, Enemy enemy) {
         Ray ray = CalcPickingRay(x, y);
 
         return RaySphereIntTest(ray, enemy);
     }
 
     /**
      * Calculate the picking ray based on x and y screen coordinates
      *
      * @param x
      * @param y
      * @return
      */
     private Ray CalcPickingRay(float x, float y) {
         float px;
         float py;
 
         // Retrieve the projection matrix from OpenGL
         float[] proj = new float[16];
         GLHelper.glGetFloatv(GL_PROJECTION_MATRIX, proj);
 
         // Use the scaling in the projection matrix to adjust the x and y positions
         px = (((2.0f * x) / Display.getWidth()) - 1.0f) / proj[0];
         py = (((-2.0f * y) / Display.getHeight()) + 1.0f) / proj[5];
 
         // Create the Ray with an initial position of 0 and the direction of the mouse click
         Ray ray = new Ray();
         ray.origin = new Vector4f(0, 0, 0, 1);
         ray.direction = new Vector4f(px, py, 1, 0);
 
         // Get the current camera coords + enemy coords and invert them
         Matrix4f world = GLHelper.getInverseModelViewMatrix();
 
         // Transform the ray by the inverse of the absolute world coords of the enemy
         TransformRay(ray, world);
 
         return ray;
     }
 
     /**
      * Check if ray is intersecting with an enemy's hit sphere
      *
      * @param ray
      * @param enemy
      * @return
      */
     private boolean RaySphereIntTest(Ray ray, Enemy enemy) {
         // Create a vector from the center of the enemy to the origin of the ray
         Vector3f v = new Vector3f(ray.origin.x - enemy.center.x, ray.origin.y - enemy.center.y, ray.origin.z - enemy.center.z);
 
         // Form the quadratic equation, leaving out A because u is normalized, so A = u * u = 1
         float b = 2.0f * Vector3f.dot(new Vector3f(ray.direction.x, ray.direction.y, ray.direction.z), v); // B=2*(u.v)
         float c = Vector3f.dot(v, v) - (enemy.radius * enemy.radius); // C=v.v-r^2
 
         // calc the discriminant of the quadratic
         double discriminant = (b * b) - (4 * c);
 
         discriminant += HUD.TARGETY / 2;
 
         // If we are trying to sqrt a negative, i.e. number is imaginary, return false
         if (discriminant < 0.0)
             return false;
 
         discriminant = Math.sqrt(discriminant);
 
         // Find both solutions to the quadratic
         double s0 = (-b + discriminant) / 2.0;
         double s1 = (-b - discriminant) / 2.0;
 
         // If at least one is positive, intersection has occurred
         return (s0 >= 0.0 || s1 >= 0.0);
     }
 
     /**
      * Transform the ray origin and direction by a matrix and normalize the result
      *
      * @param ray
      * @param mat
      */
     private void TransformRay(Ray ray, Matrix4f mat) {
         Matrix4f.transform(mat, ray.origin, ray.origin);
         Matrix4f.transform(mat, ray.direction, ray.direction);
         ray.direction.normalise();
     }
 
     /**
      * Check if current player is colliding with another entity
      */
     private boolean CheckCollisionWithPlayer(Entity entity) {
         // Get the position of the player and entity and create a vector from the player to the entity
         Vector3f playerPos = new Vector3f();
         Vector3f.sub(player.offset, cam.getPosition(), playerPos);
 
         Vector3f v = new Vector3f();
         Vector3f.sub(playerPos, entity.getPosition(), v);
 
         // Calculate the magnitude of vector v as the distance and create a minimum acceptable distance for collision
         float dist = v.x * v.x + v.y * v.y + v.z * v.z;
         float minDist = player.radius + entity.radius;
 
         return dist <= minDist * minDist;
     }
 
     //TODO: Delete this later, testing
     private boolean CheckCollision2(Vector3f entityPos, Vector3f entity2Pos, float entityRadius, float entity2Radius) {
         // Get the position of the player and entity and create a vector from the player to the entity
 
         Vector3f v = new Vector3f();
         Vector3f.sub(entityPos, entity2Pos, v);
 
         // Calculate the magnitude of vector v as the distance and create a minimum acceptable distance for collision
         float dist = v.x * v.x + v.y * v.y + v.z * v.z;
         float minDist = entityRadius + entity2Radius;
 
         return dist <= minDist * minDist;
     }
 
 }
