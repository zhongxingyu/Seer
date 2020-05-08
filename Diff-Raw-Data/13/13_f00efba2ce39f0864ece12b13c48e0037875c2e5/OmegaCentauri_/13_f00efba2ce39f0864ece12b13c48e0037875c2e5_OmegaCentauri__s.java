 package MainPackage;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.*;
 import java.util.*;
 import javax.swing.*;
 
 // @author Michael Kieburtz
 public class OmegaCentauri_ extends Game implements Runnable {
 
     // game state varibles
     private boolean forward, rotateRight, rotateLeft = false;
     private boolean paused = false;
     private boolean loading = false;
     private double averageFPS = 0;
     private final Point screenSize = new Point(10000, 10000);
     private final Point2D.Double middleOfPlayer = new Point2D.Double();
     private long gameStartTime, loopTime;
     private int timeInGame;
     private long framesDrawn = 1;
     private final int FPSTimerDelay = 1000;
     private boolean canUpdate, canGetFPS = true;
     private final int UPS = 60;
     private final int UPSDelay = 1000 / UPS;
     // objects
     private final Renderer renderer;
     private final Panel panel = new Panel(1000, 600); // this will be changed when we do resolution things
     private Camera camera;
     private java.util.Timer FPSTimer = new java.util.Timer();
     private java.util.Timer UpdateTimer = new java.util.Timer();
     // varibles for loading
     private int[] yPositions = {-10000, -10000, 0, 0}; // starting y positions
     private int starChunksLoaded = 0;
     private ArrayList<StarChunk> stars = new ArrayList<StarChunk>();
 
     public OmegaCentauri_(int width, int height, long desiredFrameRate, Renderer renderer) {
         this.renderer = renderer;
         camera = new Camera(width, height);
         loading = true;
 
         player = new Player(0, 0, MainPackage.Type.Fighter);
 
         loopTime = (long) Math.ceil(1000 / desiredFrameRate); // 12 renders
 
         middleOfPlayer.x = camera.getLocation().x - player.getLocation().x + player.getImage().getWidth() / 2;
         middleOfPlayer.y = camera.getLocation().y - player.getLocation().y + player.getImage().getHeight() / 2;
         setUpWindow(width, height);
 
         loadGame();
     }
 
     private void setUpWindow(int width, int height) {
         setEnabled(true);
         setSize(width, height);
         setResizable(false);
         setVisible(true);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setFocusable(true);
         requestFocus();
         addKeyListener(this);
         setTitle("Omega Centauri");
         add(panel);
         setContentPane(panel);
     }
 
     private void loadGame() {
         while (loading) {
             // load 100 starChunks from each quadrant
             // load all the horizontal star chunks from each quadrant
             // then move down 100 to the next chunk down
 
             // quadrant 1
 
             /*  _______
              * |___|_x_|
              * |___|___|
              */
 
             if (yPositions[0] < 0) {
 
                 for (int x = 1; x < screenSize.x; x = x + 100) {
 
                     stars.add(new StarChunk(x, yPositions[0]));
                     starChunksLoaded++;
                 }
 
                 yPositions[0] += 100;
             }
 
 
             // quadrant 2
 
             /*  _______
              * |_x_|___|
              * |___|___|
              */
 
             if (yPositions[1] < 0) {
                 for (int x = -1; x > -screenSize.x; x = x - 100) {
 
                     stars.add(new StarChunk(x, yPositions[1]));
                     starChunksLoaded++;
 
                 }
 
                 yPositions[1] += 100;
             }
 
             // quadrant 3
 
             /*  _______
              * |___|___|
              * |_x_|___|
              */
 
             if (yPositions[2] < 10000) {
                 for (int x = -1; x > -screenSize.x; x = x - 100) {
 
                     stars.add(new StarChunk(x, yPositions[2]));
                     starChunksLoaded++;
                 }
 
                 yPositions[2] += 100;
             }
 
             // quadrant 4
 
             /*  _______
              * |___|___|
              * |___|_x_|
              */
 
             if (yPositions[3] < 10000) {
                 for (int x = 1; x < screenSize.x; x = x + 100) {
 
                     stars.add(new StarChunk(x, yPositions[3]));
                     starChunksLoaded++;
                 }
                 
                 yPositions[3] += 100;
             }
             
             
             // base case
             if (starChunksLoaded == (100 * 100) * 4) {
                 loading = false;
                 break;
             }
 
             // use active rendering to draw the screen
             renderer.drawLoadingScreen(panel.getGraphics(), starChunksLoaded / 400, panel.getWidth(), panel.getHeight());
 
             try {
                 Thread.sleep(10);
             } catch (InterruptedException ex) {
             }
 
         }
 
         startGame();
     }
 
     private void startGame() {
         // start the timers immeatitely then start the main game thread
         FPSTimer.schedule(new FPSTimer(), 1);
         UpdateTimer.schedule(new UpdateTimer(), 1);
         Thread game = new Thread(this);
         game.start();
     }
 
     @Override
     public void run() {
 
         long beforeTime, afterTime, timeDiff = 0L;
 
         gameStartTime = System.currentTimeMillis();
         averageFPS = getFrameRate();
         while (!paused) // game loop
         {
             beforeTime = System.currentTimeMillis();
 
 
            // make sure the window is active
            if (!hasFocus()) {
                setEnabled(false);
            }

            if (!isEnabled() && hasFocus()) {
                setEnabled(true);
            }


             // process input and preform logic
             if (canUpdate) {
                 gameUpdate();
                 syncGameStateVaribles();
                 canUpdate = false;
             }

             // draw to buffer and to screen
             if (canGetFPS) {
                 averageFPS = getFrameRate();
                 canGetFPS = false;
                 framesDrawn = 0;
             }
             renderer.drawScreen(panel.getGraphics(), player, middleOfPlayer.x, middleOfPlayer.y, averageFPS, stars, camera, player.getShots());
             framesDrawn++;
 
             afterTime = System.currentTimeMillis();
 
             timeDiff = afterTime - beforeTime;
 
 
             if (timeDiff > loopTime) {
                 continue; // don't sleep
             } else {
 
                 try {
                     Thread.sleep(loopTime - timeDiff);
                 } catch (InterruptedException ex) {
                 }
 
                 continue;
             }
 
         }
     }
 
     private void gameUpdate() {
         if (forward) {
             player.move(true);
         }
         if (rotateRight) {
 
             player.rotate(true); // positive
         }
 
         if (rotateLeft) {
             player.rotate(false); // negitive
         }
 
         if (!forward && player.isMoving()) {
             player.move(false);
         }
     }
     int keyCode;
 
     @Override
     public void CheckKeyPressed(KeyEvent e) {
         keyCode = e.getKeyCode();
         /*
          * 0 = stationary
          * 1 = both thrusters
          * 2 = right thruster
          * 3 = left thruster
          */
         switch (keyCode) {
             case KeyEvent.VK_W: {
                 forward = true;
                 player.changeImage(1);
             }
             break;
 
             case KeyEvent.VK_D: {
                 rotateRight = true;
                 if (!forward) {
                     player.changeImage(3);
                 }
                 if (rotateLeft) {
                     player.changeImage(0);
                 }
 
             }
             break;
 
             case KeyEvent.VK_A: {
                 rotateLeft = true;
                 if (!forward) {
                     player.changeImage(2);
                 }
                 if (rotateRight) {
                     player.changeImage(0);
                 }
             }
             break;
 
             case KeyEvent.VK_SHIFT: {
                 player.speedBoost();
             }
             break;
 
             case KeyEvent.VK_SPACE: {
                 player.shoot(new Point2D.Double(middleOfPlayer.x + camera.getLocation().x,
                         middleOfPlayer.y + camera.getLocation().y), player.getAngle() - 90);
 
             }
 
         } // end switch
 
     } // end method
 
     @Override
     public void CheckKeyReleased(KeyEvent e) {
         keyCode = e.getKeyCode();
 
         switch (keyCode) {
             case KeyEvent.VK_W: {
                 forward = false;
                 player.changeImage(0);
                 if (rotateRight) {
                     player.changeImage(3);
                 } else if (rotateLeft) {
                     player.changeImage(2);
                 }
             }
             break;
 
             case KeyEvent.VK_D: {
                 rotateRight = false;
                 if (!forward) {
                     player.changeImage(0);
                 } else {
                     player.changeImage(1);
                 }
             }
             break;
 
             case KeyEvent.VK_A: {
                 rotateLeft = false;
                 if (!forward) {
                     player.changeImage(0);
                 } else {
                     player.changeImage(1);
                 }
             }
 
             break;
 
             case KeyEvent.VK_SHIFT: {
                 player.stopSpeedBoosting();
             }
 
         } // end switch
     }
 
     private double getFrameRate() {
 
         return framesDrawn;
     }
 
     public class Panel extends JPanel {
 
         public Panel(int width, int height) {
             setSize(width, height);
             setVisible(true);
             setBackground(Color.BLACK);
             setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         }
 
         @Override
         protected void paintComponent(Graphics g) {
             super.paintComponent(g);
         }
     }
 
     private void syncGameStateVaribles() {
         camera.getLocation().x = player.getLocation().x - (getWidth() / 2);
         camera.getLocation().y = player.getLocation().y - (getHeight() / 2);
 
         middleOfPlayer.x = player.getLocation().x - camera.getLocation().x + player.getImage().getWidth() / 2;
         middleOfPlayer.y = player.getLocation().y - camera.getLocation().y + player.getImage().getHeight() / 2;
     }
 
     private class FPSTimer extends TimerTask {
 
         @Override
         public void run() {
             canGetFPS = true;
             FPSTimer.schedule(new FPSTimer(), FPSTimerDelay);
         }
     }
 
     private class UpdateTimer extends TimerTask {
 
         @Override
         public void run() {
             canUpdate = true;
             UpdateTimer.schedule(new UpdateTimer(), UPSDelay);
         }
     }
 }
