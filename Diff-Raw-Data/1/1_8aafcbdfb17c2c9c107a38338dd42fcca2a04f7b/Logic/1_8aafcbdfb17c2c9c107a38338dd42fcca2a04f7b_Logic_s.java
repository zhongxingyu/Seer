 package game;
 
 import gui.MenuGUI;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.Random;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 /**
  * The purpose of the
  * <code>Logic</code> class is to manage all other classes and methods in such a
  * way as to produce a playable game. This includes (but is not limited to)
  * calling for the creation of the main menu, displaying the leaderboard or
  * starting the game in either single or two player mode as appropriate,
  * depending on player actions.
  *
  * @author Nikolaos, Michael, Anthony
  *
  */
 public class Logic extends KeyAdapter implements ActionListener {
 
     /**
      * Value of maximum level.
      */
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
     private static Progression gameProgress;
     private static AI gameAI;
     //fire rate limiter variables
     private static long initialShootTime;
     private static long currentShootTime;
     private static boolean shootKeyReleased = true;
     private static int shootCounter = 0;
     //is game paused boolean
     private boolean paused = false;
     //the service used to execute all update functions
     private static ScheduledExecutorService timer;
     //logger, global logging level
     private final static Logger log = Logger.getLogger(Logic.class.getName());
     /**
      * Logging level control - from no logging output to full logging.
      */
     public final static Level LOG_LEVEL = Level.OFF;
 
     /**
      * Main method; creates the main menu and acts as appropriate depending on
      * player input.
      *
      * @param args
      */
     public static void main(String args[]) {
         GameAssets.loadSounds();
         //set log configuration to defaults
         BasicConfigurator.configure();
 
         //create, display gui
         gui = new MenuGUI(buttonPress, keyboard);
         gui.showMenu();
 
         log.setLevel(LOG_LEVEL);
         log.info("GUI has been started");
 
         //background music - different exception handles for jdk6 compatibility
         GameAssets.theme.playLoop();
     }
 
     /**
      * Start the global timer responsible for keeping all game elements up to
      * date. The timer will use some form of multithreading to execute update
      * tasks concurrently.
      */
     public static void startTimer() {
 
         timer = Executors.newScheduledThreadPool(4);
         timer.scheduleAtFixedRate(graphicsEngine, 0, 17, TimeUnit.MILLISECONDS);
         timer.scheduleAtFixedRate(physicsEngine, 0, 17, TimeUnit.MILLISECONDS);
         timer.scheduleAtFixedRate(collisionCheck, 0, 17, TimeUnit.MILLISECONDS);
         timer.scheduleAtFixedRate(gui, 0, 17, TimeUnit.MILLISECONDS);
         timer.scheduleAtFixedRate(ttlLogic, 0, 200, TimeUnit.MILLISECONDS);
         timer.scheduleAtFixedRate(gameProgress, 0, 1, TimeUnit.SECONDS);
         timer.scheduleAtFixedRate(gameAI, 0, 500, TimeUnit.MILLISECONDS);
     }
 
     /**
      * Stops the timer.
      */
     public static void stopTimer() {
         timer.shutdown();
     }
 
     /**
      * ?
      *
      * @param command
      * @param delay
      * @param unit
      */
     public static void executeTask(Runnable command, long delay, TimeUnit unit) {
         timer.schedule(command, delay, unit);
     }
 
     /**
      * Start the single player game.
      */
     public static void startSinglePlayer() {
         GameAssets.theme.stop();
         setUpLevel(false);
     }
 
     /**
      * Starts the game in 2 player mode.
      */
     public static void startTwoPlayer() {
         GameAssets.theme.stop();
         setUpLevel(true);
     }
 
     /**
      * Shows tutorial information to the player.
      */
     public static void showTutorial() {
     }
 
     /**
      * Checks if the game is paused.
      *
      * @return true if game is paused, false otherwise
      */
     public boolean isPaused() {
         return paused;
     }
 
 
     /**
      * Displays "Game Over" message.
      */
     public static void displayGameOver(boolean singleP) {
         GameAssets.theme.stop();
         gui.displayGameOver(gameState, singleP);
     }
 
     /**
      * Display information relevant to player two's turn.
      */
     public static void displayPlayerTwoTurn() {
     }
 
     //set up some game essentials
     private static void setUpLevel(boolean twoPlayer) {
         gameState = new GameState();
         graphicsEngine = new GraphicsEngine(gameState);
         physicsEngine = new Physics(gameState);
         ttlLogic = new timeToLive(gameState);
         collisionCheck = new Collision(gameState, physicsEngine);
         gameProgress = new Progression(gameState,twoPlayer);
         gameAI = new AI(gameState);
         gameProgress.setupInitialLevel();
 
     }
 
     //called whenever a key is pressed (thread seperate from timer)
     /**
      * Handles event caused by user key presses.
      *
      * @param e
      */
     @Override
     public void keyPressed(KeyEvent e) {
         int keyCode = e.getKeyCode();
         PlayerShip player = gameState.getPlayerShip();
         //handles most basic key commands.  Should activate a boolean stating that the key has been pressed
         if (keyCode == KeyEvent.VK_UP) {
             //accelerate
             if (!paused && player != null) {
                 player.accelerate(true);
                 GameAssets.thrusters.playLoop();
             }
         } else if (keyCode == KeyEvent.VK_LEFT) {
             if (!paused && player != null) {
                 player.turnLeft(true);
             }
         } else if (keyCode == KeyEvent.VK_RIGHT) {
             if (!paused && player != null) {
                 player.turnRight(true);
             }
         } else if (keyCode == KeyEvent.VK_DOWN) {
             if (!paused && player != null) {
                 player.useBomb();
             }
         } else if (keyCode == KeyEvent.VK_SPACE) {
                 if(gameState.getPlayerShip() != null){
                     if (shootKeyReleased) {
                         initialShootTime = System.currentTimeMillis();
                         shootKeyReleased = false;
                         shootCounter = 0;
                         player.shoot();
                     } else if (!shootKeyReleased) {
                         currentShootTime = System.currentTimeMillis();
                         while ((currentShootTime - initialShootTime) > PlayerShip.FIRE_RATE * 1200 && shootCounter < 1) {
                                 player.shootDirection();
                                 shootCounter++;                      
                                 initialShootTime = currentShootTime;
                         }
                     }
                 }
         } else if (keyCode == KeyEvent.VK_P) {
             if (!paused) {
                 stopTimer();
                 paused = true;
             } else {
                 startTimer();
                 paused = false;
             }
         } else if (keyCode == KeyEvent.VK_B) {
             if (!paused && player != null) {
                 player.useBomb();
             }
         } else if (keyCode == KeyEvent.VK_Z) {
             Random randu = new Random();
             gameState.addAsteroid(new Asteroid(new float[]{1.5f, 1.5f}, randu.nextInt(360), new int[]{randu.nextInt(700), randu.nextInt(500)}, gameState, Asteroid.LARGE_ASTEROID_SIZE));
             //gameState.addAsteroid(new Asteroid(new float[]{Difficulty.randomAsteroidVelocity(10), Difficulty.randomHeading()}, randu.nextInt(360), new int[]{randu.nextInt(700), randu.nextInt(500)}, gameState, Asteroid.LARGE_ASTEROID_SIZE));
             //gameState.addProjectile(new Projectile(gameState.getAlienShip(), randu.nextInt(360), new int[] {gameState.getAlienShip().getX(), gameState.getAlienShip().getY()}, gameState));
         } else if (keyCode == KeyEvent.VK_ESCAPE) {
             //may have to add if statement for two player here
             stopTimer();
             GameAssets.theme.stop();
             displayGameOver(true);
         }
     }
 
     //same as keyPressed, except when it is released
     /**
      * Handle events caused by release of key.
      *
      * @param e
      */
     @Override
     public void keyReleased(KeyEvent e) {
         int keyCode = e.getKeyCode();
         PlayerShip player = gameState.getPlayerShip();
         //stops doing whatever that keypress was doing
         if (keyCode == KeyEvent.VK_UP) {
             //accelerate
             if (player != null) {
                 player.accelerate(false);
                 GameAssets.thrusters.stop();
             }
         } else if (keyCode == KeyEvent.VK_LEFT) {
             if (player != null) {
                 player.turnLeft(false);
             }
         } else if (keyCode == KeyEvent.VK_RIGHT) {
             if (player != null) {
                 player.turnRight(false);
             }
         } else if (keyCode == KeyEvent.VK_SPACE) {
             shootKeyReleased = true;
         }
     }
 
     //This section needs a LOT of work....
     //called when a gui button is clicked
     /**
      * Handles events relating to the user clicking menu buttons.
      *
      * @param e
      */
     @Override
     public void actionPerformed(ActionEvent e) {
         Object action = e.getSource();
         if (action == gui.singlePbutton) {
             startSinglePlayer();
             gui.displaySingleP(gameState);
             startTimer();
         } else if (action == gui.twoPbutton) {
             startTwoPlayer();
             gui.displayTwoP(gameState);
             startTimer();
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
