 package circlesvssquares;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.util.ArrayList;
 
 import org.jbox2d.common.Vec2;
 
 import pbox2d.PBox2D;
 import processing.core.PApplet;
 
 public class CirclesVsSquares extends PApplet {
 
     private static final long serialVersionUID = 7397694443868429500L;
 
     private static final float WORLD_GRAVITY = -50;
     private static final boolean DEBUG = true;
 
     private static CirclesVsSquares instance;
     public static CirclesVsSquares instance() {
         return instance; 
     }
 
     public static void main(String[] args) {
         PApplet.main(new String[] { CirclesVsSquares.class.getName() });
     }
 
     // A reference to our box2d world
     private PBox2D box2d;
 
     // Just a single box this time
     Player player;
 
     ArrayList<Box2DObjectNode> objectList;
     ArrayList<Box2DObjectNode> toRemoveList;
 
     boolean[] keys = new boolean[526];
     boolean mousePressed = false;
     boolean mouseClick = false;
     
     boolean enablePhysics;
     
     float zoom = 1;
     
     Box2DObjectNode target = null;
     Vec2 targetPoint = null;
     
     ObjectTypes currentType = ObjectTypes.NONE;
     
     enum ObjectTypes {
         NONE,
         GROUND,
         EASY_ENEMY
     };
     
 
     @Override
     public void setup() {
         instance = this;
         size(640, 400);
         smooth();
 
         // Initialize box2d physics and create the world
         box2d = new PBox2D(this);
         box2d.createWorld();
         box2d.setGravity(0, WORLD_GRAVITY);
 
         // Add a listener to listen for collisions!
         box2d.world.setContactListener(new CustomListener());
 
         enablePhysics = !DEBUG;
         if (DEBUG) {
             addMouseWheelListener(new MouseWheelListener() { 
                 public void mouseWheelMoved(MouseWheelEvent mwe) { 
                     mouseWheel(mwe.getWheelRotation());
                 }}); 
         }
         
         toRemoveList = new ArrayList<Box2DObjectNode>();
         objectList = new ArrayList<Box2DObjectNode>();
         
         Vec2 playerPos = new Vec2(200, 150);
         player = new Player(playerPos, box2d);
 
         // Create the UI
         if (DEBUG) createDebugUI();
        else LevelEditor.loadLevel("levels/level1.json", box2d);
         
     
     }
 
     public boolean checkKey(String k) {
         for(int i = 0; i < keys.length; i++) {
             if(KeyEvent.getKeyText(i).toLowerCase().equals(k.toLowerCase())) {
                 return keys[i];
             }
         }
         return false;
     }
 
     @Override
     public void keyPressed() { 
         keys[keyCode] = true;
     }
 
     @Override
     public void keyReleased() { 
         keys[keyCode] = false; 
     }
 
     void mouseWheel(int delta_) {
         float delta = delta_;
         zoom += delta / 10;
         if (zoom <= 0) {
             zoom = 0.1f;
         }
     }
 
     @Override
     public void mousePressed() {
         mousePressed = true;
         mouseClick = true;
     }
 
     @Override
     public void mouseReleased() {
         mousePressed = false;
     }
 
     @Override
     public void draw() {
         background(255);
         
         float swidth = width * (1 / zoom),
                 sheight = height * (1 / zoom);
 
         if (DEBUG) {
             // Check to see if a button was clicked on so multiple click commands do occur
             boolean wasClicked = Button.updateButtons();
             
             if (checkKey("R")) {
                 player.reset();
             }
             
             if (!wasClicked) {
                 Vec2 pos = box2d.coordPixelsToWorld(new Vec2(mouseX, mouseY)).mul(1/zoom)
                         .add(player.getPhysicsPosition());
                 
                 switch (currentType) {
                 case NONE:
                     break;
                 case GROUND:
                     // Create ground object
                     if (mouseClick && target == null) {
                         targetPoint = pos;
                         target = new Ground(targetPoint, 0, 0, box2d);
                     }
                     // Drag and drop ground object
                     else if (target != null) {
                         Vec2 diff = targetPoint.sub(pos);
                         if (target.getClass() == Ground.class) {
                             Ground g = (Ground) target;
                             g.setPhysicsPosition(pos.add(diff.mul(0.5f)));
                             g.w = Math.abs(box2d.scaleFactor * diff.x);
                             g.h = Math.abs(box2d.scaleFactor * diff.y);
                             g.updateBody();
                         }
                         if (!mousePressed) target = null;
                     }
                     break;
                 case EASY_ENEMY:
                     if (mouseClick) {
                         Enemy e = new Enemy(pos, 25, 25, box2d);
                     }
                     break;
                 }
             }
         }
 
         // Move the player if movement keys are held down
         Player.MovementDirection direction = Player.MovementDirection.NONE;
         if (checkKey("D")) {
             direction = Player.MovementDirection.RIGHT;
             
             if (!enablePhysics) player.setPhysicsPosition(player.getPhysicsPosition().add(new Vec2(1, 0)));
         } else if (checkKey("A")) {
             direction = Player.MovementDirection.LEFT;
             
             if (!enablePhysics) player.setPhysicsPosition(player.getPhysicsPosition().add(new Vec2(-1, 0)));
         }
         player.movePlayer(direction);
 
         // Attempt to jump if the jump key is held down
         if (checkKey("W")) {
             player.jumpIfPossible();
             if (!enablePhysics) player.setPhysicsPosition(player.getPhysicsPosition().add(new Vec2(0, 1)));
         }
         
         if (checkKey("S")) {
             if (!enablePhysics) player.setPhysicsPosition(player.getPhysicsPosition().add(new Vec2(0, -1)));
         }
 
         player.update();
 
         if (enablePhysics) {
         
             for (int i = objectList.size()-1; i >=0; i--) {
                 Box2DObjectNode n = objectList.get(i);
                 n.update();
             }
 
             // Step the physics simulation
             box2d.step();
         }
         
         
         // Remove objects after box2d has stepped
         for (int i = toRemoveList.size()-1; i >=0; i--) {
             Box2DObjectNode n = toRemoveList.get(i);
             n.destroy();
         }
         toRemoveList.clear();
 
         CirclesVsSquares cvs = CirclesVsSquares.instance();
         cvs.pushMatrix();
         cvs.scale(zoom);
 
         player.display(swidth, sheight);
 
         cvs.translate(swidth/2-player.getGraphicsPosition().x, sheight/2-player.getGraphicsPosition().y);
         for (int i = objectList.size()-1; i >=0; i--) {
             Box2DObjectNode n = objectList.get(i);
             n.display(swidth, sheight);
         }
         cvs.popMatrix();
         
         if (DEBUG) Button.displayButtons();
         mouseClick = false;
     }
 
     public void clearObjects() {
         while (objectList.size() > 0) {
             objectList.get(0).destroy();
         }
     }
     
     void createDebugUI() {
         Button saveButton = Button.createButton(new Vec2(), 60, 30, new ButtonCallback() {
             @Override
             public void call() {
                 LevelEditor.saveLevel(objectList);
             }
         });
         saveButton.text = "Save";
 
         Button loadButton = Button.createButton(new Vec2(60, 0), 60, 30, new ButtonCallback() {
             @Override
             public void call() {
                 clearObjects();
                 LevelEditor.loadLevel(null, box2d);
             }
         });
         loadButton.text = "Load";
 
         Button physicsButton = Button.createCheckBox(new Vec2(120, 0), 60, 30, new ButtonCallback() {
             @Override
             public void call() {
                 enablePhysics = this.isDown;
             }
         });
         physicsButton.text = "Physics";
         
         Button groundButton = Button.createCheckBox(new Vec2(180, 0), 60, 30, new ButtonCallback() {
             @Override
             public void call() {
                 if (this.isDown) {
                     currentType = ObjectTypes.GROUND;
                 } else {
                     currentType = ObjectTypes.NONE;
                 }
             }
         });
         groundButton.text = "Ground";
         
         Button enemyButton = Button.createCheckBox(new Vec2(240, 0), 60, 30, new ButtonCallback() {
             @Override
             public void call() {
                 if (this.isDown) {
                     currentType = ObjectTypes.EASY_ENEMY;
                 } else {
                     currentType = ObjectTypes.NONE;
                 }
             }
         });
         enemyButton.text = "Enemy";
     }
 }
