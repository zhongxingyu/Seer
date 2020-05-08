 package MainPackage;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.*;
 import java.util.*;
 import javax.swing.*;
 
 /**
  * @author Michael Kieburtz
  * @author Davis Freeman
  */
 public class OmegaCentauri_ extends Game implements Runnable {
 
     private final String Version = "Dev 1.0.2";
     /*
      * GAME STATE VARIBLES:
      */
     private boolean forward, rotateRight, rotateLeft, shooting = false; // movement booleans 
     private boolean paused = false;
     private boolean loading = false;
     private final Point screenSize = new Point(10000, 10000);
     private final Point2D.Double middleOfPlayer = new Point2D.Double(); // SCREEN LOCATION of the middle of the player
     // TIMING STUFF
     private int averageFPS = 0;
     private final int UPS = 80;
     private final int UPSDelay = 1000 / UPS;
     private long loopTime;
     private int framesDrawn = 1;
     private final int FPSTimerDelay = 1000;
     private boolean canUpdate, canGetFPS = true;
     /*
      * OBJECTS:
      */
     private final Renderer renderer;
     private final Panel panel = new Panel(1000, 600); // this will be changed when we do resolution things
     private Camera camera;
     private ArrayList<ICollisionListener> collisionListeners = new ArrayList<ICollisionListener>();
     
     // TIMERS
     private java.util.Timer FPSTimer = new java.util.Timer();
     private java.util.Timer UpdateTimer = new java.util.Timer();
     /*
      * LOADING VARIBLES:
      */
     private int[] yPositions = {-10000, -10000, 0, 0}; // starting y positions
     private int starChunksLoaded = 0;
     private ArrayList<StarChunk> stars = new ArrayList<StarChunk>();
 
     public OmegaCentauri_(int width, int height, long desiredFrameRate, Renderer renderer) {
 
         this.renderer = renderer;
         camera = new Camera(width, height);
         loading = true;
 
         player = new Player(0, 0, MainPackage.Type.Fighter, 5, 5, 4, .15, camera.getLocation(), 155, 100);
         enemyShips.add(new EnemyFighter(200, 0, MainPackage.Type.Fighter, 5, 3, 5, .15, camera.getLocation(), 500, 100));
         syncGameStateVaribles();
 
         player.setUpHitbox(camera.getLocation());
         
         collisionListeners.add(player);
         
         for (EnemyShip enemy : enemyShips)
             collisionListeners.add(enemy);
         
         loopTime = (long) Math.ceil(1000 / desiredFrameRate); // 12 renders for now
 
         setUpWindow(width, height);
 
         loadGame();
     }
 
     private void setUpWindow(int width, int height) {
         setEnabled(true);
         setUndecorated(true);
         setPreferredSize(new Dimension(width, height));
         pack();
         setLocationRelativeTo(null);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setFocusable(true);
         requestFocus();
         addKeyListener(this);
         setTitle("Omega Centauri");
         add(panel);
         setContentPane(panel);
         setVisible(true);
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
             }
 
             // use active rendering to draw the screen
             renderer.drawLoadingScreen(panel.getGraphics(), starChunksLoaded / 400, panel.getWidth(), panel.getHeight());
 
             if (!loading) {
                 break;
             }
 
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
 
         averageFPS = getFrameRate();
         boolean running = true;
         while (running) // game loop
         {
             beforeTime = System.currentTimeMillis();
 
             // draw to buffer and to screen
             if (canGetFPS) {
                 averageFPS = getFrameRate();
                 canGetFPS = false;
                 if (averageFPS == 1) {
                     averageFPS = 80;
                 }
                 framesDrawn = 0;
             }
 
             // process input and preform logic
             shipsToDraw.add(player);
             shipsToDraw.addAll(enemyShips);
             shipsToDraw.addAll(allyShips);
 
             for (Ship ship : shipsToDraw) {
                 shotsToDraw.addAll(ship.getShots());
             }
 
             if (canUpdate) {
                 gameUpdate();
                 syncGameStateVaribles();
                 canUpdate = false;
             }
 
             // draw screen with active rendering
             renderer.drawScreen(panel.getGraphics(), shipsToDraw, middleOfPlayer.x, middleOfPlayer.y,
                     averageFPS, stars, camera, shotsToDraw, Version);
             framesDrawn++;
 
             shipsToDraw.clear();
             shotsToDraw.clear();
 
             afterTime = System.currentTimeMillis();
 
             timeDiff = afterTime - beforeTime;
 
 
             if (timeDiff > loopTime) {
                 continue; // don't sleep
 
             } else {
 
                 try {
                     Thread.sleep(loopTime - timeDiff);
                 } catch (InterruptedException ex) {
                 }
 
             }
 
         }
     }
 
     private void gameUpdate() {
 
         if (camera.getSize().x != getWidth() || camera.getSize().y != getHeight()) {
             camera.setSize(getWidth(), getHeight());
         }
 
         if (forward) {
             player.move(ShipState.Thrusting);
         }
         if (rotateRight) {
 
             player.rotate(ShipState.TurningRight);
         }
         if (rotateLeft) {
             player.rotate(ShipState.TurningLeft);
         }
         if (!forward && player.isMoving()) {
             player.move(ShipState.Drifting);
         }
         if (shooting && player.canShoot()) {
             player.shoot(camera.getLocation());
         }
         for (Shot shot : shotsToDraw) {
             shot.updateLocation();
         }
         for (EnemyShip enemyShip : enemyShips) {
             enemyShip.update(player.getLocation(), camera.getLocation());
         }
         if (player.getShots().size() > 0)
             player.purgeShots();
         
         //System.out.println(Calculator.collisionCheck(enemyShips.get(0).returnHitbox(), player.returnHitbox()));
         
         
         for (Shot shot : shotsToDraw)
         {
             // check for collisions with enemy shots and the player
             if (Calculator.collisionCheck(player.returnHitbox(), shot.returnHitbox()))
             {
                 //System.out.println("UR BAD");
                 for (ICollisionListener collisionListener : collisionListeners)
                     collisionListener.CollisionEvent(player, shot);
             }
             
             // check for collisions with player shots and enemys
             
             for (EnemyShip enemyShip : enemyShips)
             {
                 if (Calculator.collisionCheck(enemyShip.returnHitbox(), shot.returnHitbox()))
                 {
                     for (ICollisionListener collisionListener : collisionListeners)
                         collisionListener.CollisionEvent(enemyShip, shot);
                 }
             }
         }
         
     }
     int keyCode;
 
     @Override
     public void CheckKeyPressed(KeyEvent e) {
         keyCode = e.getKeyCode();
         /*
          * 0 = stationary
          * 1 = thrusting
          * 2 = turning right
          * 3 = turning left
          */
         switch (keyCode) {
             case KeyEvent.VK_W: {
                 forward = true;
                 if (!rotateRight && !rotateLeft) {
                     player.changeImage(ShipState.Thrusting);
                 } else if (rotateLeft && !rotateRight) {
                     player.changeImage(ShipState.TurningLeftThrusting);
                 } else if (!rotateLeft && rotateRight) {
                     player.changeImage(ShipState.TurningRightThrusting);
                 }
             }
             break;
 
             case KeyEvent.VK_D: {
                 rotateRight = true;
                 if (!forward) {
                     player.changeImage(ShipState.TurningRight);
                 } else if (forward) {
                     player.changeImage(ShipState.TurningRightThrusting);
                 } else if (rotateLeft) {
                     player.changeImage(ShipState.Idle);
                 }
 
             }
             break;
 
             case KeyEvent.VK_A: {
                 rotateLeft = true;
                 if (!forward) {
                     player.changeImage(ShipState.TurningLeft);
                 } else if (forward) {
                     player.changeImage(ShipState.TurningLeftThrusting);
                 } else if (rotateRight) {
                     player.changeImage(ShipState.Idle);
                 }
 
             }
             break;
 
             case KeyEvent.VK_SHIFT: {
                 player.speedBoost();
             }
             break;
 
             case KeyEvent.VK_SPACE: {
                 shooting = true;
                break;
             }
             
             case KeyEvent.VK_Q: {
                 System.exit(0);
             }
 
         } // end switch
 
     } // end method
 
     @Override
     public void CheckKeyReleased(KeyEvent e) {
         keyCode = e.getKeyCode();
 
         switch (keyCode) {
             case KeyEvent.VK_W: {
                 forward = false;
                 player.changeImage(ShipState.Idle);
                 if (rotateRight) {
                     player.changeImage(ShipState.TurningLeft);
                 } else if (rotateLeft) {
                     player.changeImage(ShipState.TurningRight);
                 }
             }
             break;
 
             case KeyEvent.VK_D: {
                 rotateRight = false;
                 if (!forward) {
                     player.changeImage(ShipState.Idle);
                 } else {
                     player.changeImage(ShipState.Thrusting);
                 }
             }
             break;
 
             case KeyEvent.VK_A: {
                 rotateLeft = false;
                 if (!forward) {
                     player.changeImage(ShipState.Idle);
                 } else {
                     player.changeImage(ShipState.Thrusting);
                 }
             }
 
             break;
 
             case KeyEvent.VK_SHIFT: {
                 player.stopSpeedBoosting();
             }
 
             case KeyEvent.VK_SPACE: {
                 shooting = false;
             }
 
         } // end switch
     }
 
     private int getFrameRate() {
 
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
         camera.move(player.getLocation().x - (getWidth() / 2), player.getLocation().y - (getHeight() / 2));
 
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
