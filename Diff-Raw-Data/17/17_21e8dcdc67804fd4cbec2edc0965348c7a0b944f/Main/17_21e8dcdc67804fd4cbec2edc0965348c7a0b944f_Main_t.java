 package main;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.ContextAttribs;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL30;
 import org.lwjgl.opengl.PixelFormat;
 import org.lwjgl.util.vector.Vector2f;
 import org.lwjgl.opengl.GL32;
 
 import shader.ShaderManager;
 import drawable.Movable;
 import drawable.Asteroid;
 import drawable.Player;
 import drawable.Drawable;
 import tool.Tools;
 
 public class Main {
 
     public static final int FPS = 60;
     public static final int DISPLAY_WIDTH = 800;
     public static final int DISPLAY_HEIGHT = 600;
     public static final int KEY_FORWARD = Keyboard.KEY_W;
     public static final int KEY_BACKWARD = Keyboard.KEY_S;
     public static final int KEY_LEFT = Keyboard.KEY_A;
     public static final int KEY_RIGHT = Keyboard.KEY_D;
     public static final int BTN_SHOOT = 0;
     private static Player player;
     private static int frameDelta;
     private static Bullets bullets;
     protected static List<Drawable> drawables;
     protected static List<Movable> movables;
 
     public static void main(String[] args) {
         initDisplay();
 
         int vao = GL30.glGenVertexArrays();
         GL30.glBindVertexArray(vao);
 
         GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
         // Allows to define size of point primitive in vertex shader
         GL11.glEnable(GL32.GL_PROGRAM_POINT_SIZE);
 
         ShaderManager.initShaders();
 
         Tools.checkGLErrors("Initialization");
 
         drawables = new LinkedList<Drawable>();
         movables = new LinkedList<Movable>();
         bullets = new Bullets();
 
         player = new Player();
         drawables.add(player);
         movables.add(player);
 
         Asteroid asteroid = new Asteroid();
         drawables.add(asteroid);
         movables.add(asteroid);
 
         // Init frame delta time so that first reading is sane.
         Tools.getDelta();
 
         while (!Display.isCloseRequested()) {
             frameDelta = Tools.getDelta();
 
             GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
 
             pollKeyboard();
             pollMouse();
 
             for (Movable movable : movables) {
                 movable.move(frameDelta);
                 if (movable.equals(player)) {
                     continue;
                 }
                 if (player.isColliding(movable)) {
                     System.out.println("Collision! " + movable);
                 }
             }
             for (Drawable drawable : drawables) {
                 drawable.draw();
             }
             bullets.removeOutOfViewBullets();
 
             Display.update();
 
             Tools.updateFPS();
 
             Tools.checkGLErrors("Main loop");
 
             Display.sync(FPS);
         }
 
         Display.destroy();
     }
 
     private static void initDisplay() {
         try {
             Display.setDisplayMode(new DisplayMode(DISPLAY_WIDTH, DISPLAY_HEIGHT));
             Display.setTitle("Asteroids");
             // Use OpenGL 3.2 Core profile so that deprecated functionality can't be used
             Display.create(new PixelFormat(), new ContextAttribs(3, 2).withProfileCore(true));
         } catch (LWJGLException ex) {
             System.out.println("Could not init display!");
             ex.printStackTrace();
             System.exit(1);
         }
     }
 
     private static void pollKeyboard() {
 
         while (Keyboard.next()) {
             // Key pushed event
             if (Keyboard.getEventKeyState()) {
                 switch (Keyboard.getEventKey()) {
                     case KEY_FORWARD:
                         player.moveVertically(true);
                         break;
                     case KEY_BACKWARD:
                         player.moveVertically(false);
                         break;
                     case KEY_LEFT:
                         player.moveSideways(true);
                         break;
                     case KEY_RIGHT:
                         player.moveSideways(false);
                         break;
                 }
             } // Key lifted event
             else {
                 switch (Keyboard.getEventKey()) {
                     case KEY_FORWARD:
                         player.moveVertically(false);
                         break;
                     case KEY_BACKWARD:
                         player.moveVertically(true);
                         break;
                     case KEY_LEFT:
                         player.moveSideways(false);
                         break;
                     case KEY_RIGHT:
                         player.moveSideways(true);
                         break;
                 }
             }
         }
     }
 
     private static void pollMouse() {
         float x = Mouse.getX();
         float y = Mouse.getY();
         // Project window coordinates to normal [-1.0, 1.0] coordinate space
         x -= DISPLAY_WIDTH / 2;     // [0, 800] => [-400, 400]
         y -= DISPLAY_HEIGHT / 2;
         x /= DISPLAY_WIDTH / 2;     // [-400, 400] => [-1, 1]
         y /= DISPLAY_HEIGHT / 2;
         Vector2f mouseDirection = new Vector2f(x, y);
 
         player.rotateTo(mouseDirection);
 
         while (Mouse.next()) {
             if (Mouse.getEventButtonState()) {
                 switch (Mouse.getEventButton()) {
                     case BTN_SHOOT: {
                         bullets.newBullet(player.getLocation(), mouseDirection);
                         break;
                     }
                 }
             }
         }
     }
 }
