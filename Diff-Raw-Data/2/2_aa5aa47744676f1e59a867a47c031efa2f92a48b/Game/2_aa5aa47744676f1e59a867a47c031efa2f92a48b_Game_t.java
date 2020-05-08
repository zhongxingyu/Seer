 package fr.iutvalence.java.mp.thelasttyper.client.data;
 
 /**
  * Class representing a game. For have rules read : readme.txt .
  * 
  * @author culasb
  */
 
 public class Game
 {
 
     /**
      * the default time for the game to check everything in millisecond.
      */
     public final static int DEFAULT_CHECKTIME = 500;
 
     /**
      * the default level of a game
      */
     public final static int DEFAULT_LEVEL = 1;
 
     /**
      * the default max level of a game
      */
     public final static int DEFAULT_MAX_LEVEL = 5;
 
     /**
      * the default amount of words per level
      */
     public final static int DEFAULT_WORDS_AMOUNT = 20;
 
     /**
      * Game's player
      */
     private Player player;
 
     /**
      * The level
      */
     private Level level;
     
 
     /**
      * Game instantiation : The level is initialized at the Default level(1) and
      * create to recover a list of words with the default level. More over a
      * player is initialize
      * 
      * @param wm
      *            the wordmanager used for the game
      */
     // TODO (FIXED) the WM should be taken as parameter
     public Game(WordsManager wm)
     {
         this.level = new Level(DEFAULT_LEVEL, wm, DEFAULT_WORDS_AMOUNT);
         this.player = new Player("test"); // TODO changer ça avec un fichier
                                           // config plutot
     }
 
     /**
      * The running of the game
      */
     public void play()
     {
         boolean isGameOn = true;
         // TODO (FIXED) inner comments should not use javadoc syntax
 
         // exception to do : OutofLivesException : when the player have 0 lives.
         // Will allow to end the game
 
         // TODO (think about it) isGameOn looks much more like a local variable
         // than a field
         while (isGameOn)
         {
            while (this.level.play())
             {
                 try
                 {
                     Thread.sleep(DEFAULT_CHECKTIME);
 
                 }
                 catch (InterruptedException e)
                 {
                     e.printStackTrace();
                 }
             }
             if (!this.levelUp())
                 isGameOn = false;
         }
         this.endGame();
     }
 
     // TODO (FIXED) this method is useless
 
     /**
      * return the current level of the current game
      * 
      * // TODO (fix) fix return tag comment
      * 
      * // TODO (FIXED) fix return tag comment
      * 
      * @return the Level of the current game
      */
     private Level getLevel()
     {
         return this.level;
     }
 
     /**
      * change the level of the current game
      * 
      * @param level
      *            the new level (int)
      */
     private void setlevel(Level level)
     {
         this.level = level;
     }
 
     // LEVEL UP
     /**
      * Used when a new level is started
      * 
      * @return return true if a new level can be played. if there is no more
      *         level return false
      */
     // TODO (FIXED) this method should return a boolean indicating if the next
     // level is
     // ready to be played or not (there is no more level). this chould avoid to
     // call endGame from this
     // method
     private boolean levelUp()
     {
         WordsManager wm = null;
         // TODO create a function to initialize this wordmanager
 
         // TODO complete with the future functions and classes
         if (this.level.getLevelNumber() == DEFAULT_MAX_LEVEL)
             return false;
         else
         {
             this.setlevel(new Level(this.level.getLevelNumber() + 1, wm, DEFAULT_WORDS_AMOUNT));
         }
         return true;
     }
 
     /**
      * Give all conditions for the ending of this game.
      */
     private void endGame()
     {
         // TODO determiner les conditions d'arret du jeu.
     }
 
 }
