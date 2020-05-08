 /**
  * DISASTEROIDS
  * GameLoop.java
  */
 package disasteroids;
 
 import disasteroids.networking.Client;
 
 /**
  * The thread that runs the game.
  * @author Phillip Cohen
  */
 public class GameLoop extends Thread
 {
     /**
      * Instance of the thread. There should only be one!
      */
     private static GameLoop instance;
 
     /**
      * The time in milliseconds between each action loop.
      */
    private final static int PERIOD = 50;
 
     /**
      * Whether the thread is enabled. Always true unless the thread is being destroyed.
      */
     private boolean enabled = true;
 
     public GameLoop()
     {
         super( "Game loop thread" );
         setPriority( Thread.MAX_PRIORITY );
         start();
     }
 
     /**
      * Starts/resumes the game loop.
      */
     public static void startLoop()
     {
         if ( instance == null )
             instance = new GameLoop();
         else
             instance.enabled = true;
     }
 
     /**
      * Stops the game loop and thread nicely. Note: the current timestep will be completed first.
      */
     public static void stopLoop()
     {
         if ( instance == null ) // We're too late!
             return;
         else
         {
             instance.enabled = false;
             instance = null;
         }
         
     }
 
     /**
      * Starts an infinite loop which acts the game, sleeps, and repeats.
      * 
      * The amount of time to sleep is set by <code>period></code>.
      * If the game is running behind, it uses this sleep time as a cushion.
      */
     @Override
     public void run()
     {
         long timeOfLast = System.currentTimeMillis();
         while ( enabled )
         {
             try
             {
                 timeOfLast = System.currentTimeMillis();
 
                 if ( shouldRun() )
                     Game.getInstance().act();
 
                 while ( enabled && System.currentTimeMillis() - timeOfLast < PERIOD )
                     Thread.sleep( 2 );
 
             }
             catch ( InterruptedException ex )
             {
                 Running.fatalError( "Game loop interrupted while sleeping.", ex );
             }
         }
     }
 
     /**
      * Returns whether the game should run in the next step or not.
      */
     public boolean shouldRun()
     {
         // Don't run if the game is paused.
         if ( Game.getInstance() == null )
             return false;
         if ( Game.getInstance().isPaused() )
             return false;
 
         // If we're the cliient and the server isn't responding, hold up.
         if ( Client.is() && Client.getInstance().serverTimeout() )
             return false;
 
         return enabled;
     }
     
     public static boolean isRunning()
     {
         return (instance != null ) && (instance.isAlive());
     }
 }
