 package game;
 
 import gui.MenuGUI;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 /**
  *
 * @author Nikolaos, Michael
  *
  */
 public class Logic extends KeyAdapter implements ActionListener {
 
     final public static int MAX_LEVEL = 30;
     
     //various essential objects
     private static GameState gameState;
     private static ActionListener buttonPress = new Logic();
     private static KeyListener keyboard = new Logic();
     private static MenuGUI gui;
     private static GraphicsEngine graphicsEngine;
     private static Physics physicsEngine;
     private static timeToLive ttlLogic;
     private static Collision collisionCheck;
     
     //fire rate limiter variables
     private static long initialShootTime;
     private static long currentShootTime;
     private static boolean shootKeyReleased = true;
     
     
     //is game paused boolean
     private boolean paused = false;
     //the service used to execute all update functions
     private static ScheduledExecutorService timer;
     
     //logger, global logging level
     private final static Logger log = Logger.getLogger(Logic.class.getName());
     public final static Level LOG_LEVEL = Level.OFF;
 
     public static void main(String args[]) {
         try {
             //set log configuration to defaults
             BasicConfigurator.configure();
 
             //create, display gui
             gui = new MenuGUI(buttonPress, keyboard);
             gui.showMenu();
 
             log.setLevel(LOG_LEVEL);
             log.info("GUI has been started");
 
             //background music - different exception handles for jdk6 compatibility
             Sound backgroundMusic = new Sound("menu.wav");
             backgroundMusic.playLoop();
         } catch (UnsupportedAudioFileException ex) {
             log.trace("Unsupported audio format", ex);
         } catch (IOException ex) {
             log.trace("Audio I/O Exception", ex);
         } catch (LineUnavailableException ex) {
             log.trace("Audio Line Unavailable", ex);
         }
     }
 
     public static void startTimer() {
 
         timer = Executors.newScheduledThreadPool(4);
         timer.scheduleAtFixedRate(graphicsEngine, 2, 19, TimeUnit.MILLISECONDS);
         timer.scheduleAtFixedRate(physicsEngine, 2, 19, TimeUnit.MILLISECONDS);
         timer.scheduleAtFixedRate(collisionCheck, 2, 19, TimeUnit.MILLISECONDS);
         timer.scheduleAtFixedRate(gui, 0, 17, TimeUnit.MILLISECONDS);
         timer.scheduleAtFixedRate(ttlLogic, 2, 202, TimeUnit.MILLISECONDS);
         
         //single threaded game loop testing thread
         //testTimer tester = new testTimer(graphicsEngine,physicsEngine,gui,collisionCheck(),ttlLogic);
         //timer.scheduleAtFixedRate(tester, 0, 17, TimeUnit.MILLISECONDS);
 
 
     }
 
     public static void stopTimer() {
         timer.shutdown();
     }
     
     public static void executeTask(Runnable command, long delay, TimeUnit unit)
     {
         timer.schedule(command, delay, unit);
     }
 
     public static void startSinglePlayer() {
         setUpLevel();
     }
     
     public static void resetShip()
     {
         final PlayerShip oldShip = gameState.getPlayerShip();
         gameState.removePlayerShip();
         Thread resetShip = new Thread() {
         public void run() {
             gameState.addPlayerShip(oldShip);
             gameState.getPlayerShip().setCoord(new int[]{400, 300});
             gameState.getPlayerShip().setVelocity(new float[]{0, 0});
             gameState.getPlayerShip().setHeading(0);
             gameState.getPlayerShip().turnLeft(false);
             gameState.getPlayerShip().turnRight(false);
             gameState.getPlayerShip().accelerate(false);
         }
     };
         executeTask(resetShip,2500,TimeUnit.MILLISECONDS);
     }
 
 
     public static void startTwoPlayer() {
     }
 
     public static void showTutorial() {
     }
 
     public boolean isPaused() {
         return paused;
     }
 
     public static void displayWinner() {
     }
 
     public static void displayGameOver() {
     }
 
     public static void displayPlayerTwoTurn() {
     }
 
     public static void gameLogic() {
     }
 
     //set up some game essentials
     private static void setUpLevel() {
         gameState = new GameState(1, 0);
         gameState.addPlayerShip(new PlayerShip(new float[]{0, 0}, 90, new int[]{250, 150}, gameState, 3, 0, 0));
         gameState.addAsteroid(new Asteroid(new float[]{-1, -1}, -30, new int[]{650, 500}, gameState, Asteroid.LARGE_ASTEROID_SIZE));
 //        Random randu = new Random();
 //        for (int i = 0; i < 200; i++)
 //        {
 //            gameState.addProjectile(new Projectile(null, randu.nextFloat(), new int[] {randu.nextInt(800),randu.nextInt(600)},gameState));
 //        }
 
 
         graphicsEngine = new GraphicsEngine(gameState);
         physicsEngine = new Physics(gameState);
         ttlLogic = new timeToLive(gameState);
         collisionCheck = new Collision(gameState,physicsEngine);
 
     }
 
     //called whenever a key is pressed (thread seperate from timer)
     @Override
     public void keyPressed(KeyEvent e) {
         int keyCode = e.getKeyCode();
         //handles most basic key commands.  Should activate a boolean stating that the key has been pressed
         if (keyCode == KeyEvent.VK_UP) {
             //accelerate
             if (!paused && gameState.getPlayerShip() != null) {
                 gameState.getPlayerShip().accelerate(true);
             }
         } else if (keyCode == KeyEvent.VK_LEFT) {
             if (!paused && gameState.getPlayerShip() != null) {
                 gameState.getPlayerShip().turnLeft(true);
             }
         } else if (keyCode == KeyEvent.VK_RIGHT) {
             if (!paused && gameState.getPlayerShip() != null) {
                 gameState.getPlayerShip().turnRight(true);
             }
         } else if (keyCode == KeyEvent.VK_DOWN) {
             if (!paused && gameState.getPlayerShip() != null) {
                 gameState.getPlayerShip().useBomb();
             }
         } else if (keyCode == KeyEvent.VK_SPACE) {
             if (!paused && gameState.getPlayerShip() != null) {
                 if (shootKeyReleased) {
                     initialShootTime = System.currentTimeMillis();
                     shootKeyReleased = false;
                     gameState.getPlayerShip().shoot();
                 } else if (!shootKeyReleased) {
                     currentShootTime = System.currentTimeMillis();
                     if ((currentShootTime - initialShootTime) > PlayerShip.FIRE_RATE * 1000) {
                         gameState.getPlayerShip().shoot();
                         initialShootTime = currentShootTime;
                     }
                 }
             }
     }
         else if (keyCode == KeyEvent.VK_P) {
             if (!paused) {
                 stopTimer();
                 paused = true;
             } else {
                 startTimer();
                 paused = false;
             }
         }
     }
 
     //same as keyPressed, except when it is released
     @Override
     public void keyReleased(KeyEvent e) {
         int keyCode = e.getKeyCode();
         //stops doing whatever that keypress was doing
         if (keyCode == KeyEvent.VK_UP) {
             //accelerate
             if (gameState.getPlayerShip() != null) {
                 gameState.getPlayerShip().accelerate(false);
             }
         } else if (keyCode == KeyEvent.VK_LEFT) {
             if (gameState.getPlayerShip() != null) {
                 gameState.getPlayerShip().turnLeft(false);
             }
         } else if (keyCode == KeyEvent.VK_RIGHT) {
             if (gameState.getPlayerShip() != null) {
                 gameState.getPlayerShip().turnRight(false);
             }
         } else if (keyCode == KeyEvent.VK_SPACE) {
             shootKeyReleased = true;
         }
     }
 
     //This section needs a LOT of work....
     //called when a gui button is clicked
     @Override
     public void actionPerformed(ActionEvent e) {
         Object action = e.getSource();
         if (action == gui.singlePbutton) {
             startSinglePlayer();
             gui.displaySingleP(gameState);
             startTimer();
         } else if (action == gui.twoPbutton) {
             gui.displayTwoP(gameState);
         } else if (action == gui.leaderBoardButton) {
             gui.displayLeaderBoard();
         } else if (action == gui.tutorialButton) {
             gui.displayTutorial();
         } else if (action == gui.backButton) {
             gui.goBack();
         } else if (e.getSource() == gui.quitButton) {
             System.exit(0);
         }
     }
 }
