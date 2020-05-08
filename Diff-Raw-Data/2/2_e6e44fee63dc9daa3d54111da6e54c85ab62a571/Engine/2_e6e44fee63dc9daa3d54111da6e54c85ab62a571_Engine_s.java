 package com.regawmod.engine2d;
 
 /**
  * Main 2D game engine
  * 
  * @author Dan Wager
  */
 public class Engine
 {
     public static final int EXIT_ERROR = 1;
 
     private static Game game;
     private static boolean isRunning;
 
     /**
      * Initializes the game engine to ready state
      * 
      * @param game The game
      */
     public static void init(Game game, int displayWidth, int displayHeight)
     {
         if (game == null)
             throw new IllegalArgumentException("Game is null! Idiot!");
 
         Engine.game = game;
 
        Window.init(displayWidth, displayHeight, false);
         Input.init();
         RenderUtil.initGraphics();
     }
 
     /**
      * Starts the game
      */
     public static void start()
     {
         if (game == null)
             throw new IllegalStateException("Game is null... Haven't called Engine.init(Game) yet! Dummy!");
 
         if (isRunning)
             return;
 
         game.init();
 
         run();
     }
 
     /**
      * Stops the game and shuts down the engine
      */
     public static void stop()
     {
         if (!isRunning)
             return;
 
         isRunning = false;
     }
 
     private static void run()
     {
         isRunning = true;
 
         Time.init();
 
         while (isRunning)
         {
             if (Window.isCloseRequested())
                 stop();
 
             FPS.update();
             float delta = Time.getDelta();
 
             getInput();
 
             update(delta);
 
             render();
         }
 
         cleanUp();
     }
 
     private static void getInput()
     {
         game.processInput();
         Input.update();
     }
 
     private static void update(float delta)
     {
         game.update(delta);
     }
 
     private static void render()
     {
         RenderUtil.clearScreen();
         game.render();
         Window.render();
     }
 
     private static void cleanUp()
     {
         Window.destroy();
         Input.destroy();
     }
 
     private Engine()
     {
     }
 }
