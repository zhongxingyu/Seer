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
      * Game's player
      */
     private Player player;
 
     /**
      * The level
      */
     private Level level;
 
     /**
      * If the current game still in progress
      */
     private boolean isGameOn;
 
     /**
      * Game instantiation : The level is initialized at the Default level(1) and
      * create to recover a list of words with the default level. More over a
      * player is initialize
      */
     // TODO (fix) the WM should be taken as parameter
     public Game()
     {
         WordsManager wm = null;
 
         this.level = new Level(DEFAULT_LEVEL, wm);
         this.player = new Player("test");
     }
 
     /**
      * The running of the game
      */
     public void play()
     {
         // TODO (fix) inner comments should not use javadoc syntax
         /**
          * exception to do : OutofLivesException : when the player have 0 lives.
          * Will allow to end the end LevelUpException : notify when the level is
          * complete
          * 
          */
         // TODO (think about it) isGameOn looks much more like a local variable
         // than a field
         while (this.isGameOn)
         {
 
             boolean levelStatus = level.play();
             try
             {
                 Thread.sleep(DEFAULT_CHECKTIME);
 
             }
             catch (InterruptedException e)
             {
                 e.printStackTrace();
             }
         }
     }
 
     // TODO (fix) this method is useless
     /**
      * return the current player of this game.
      * 
      * @return Game's Player
      */
     private Player getPlayer()
     {
         return this.player;
     }
 
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
      */
     // TODO (fix) this method should return a boolean indicating if the next
     // level is
     // ready to be played or not (there is no more level). this chould avoid to
     // call endGame from this
     // method
     private void levelUp()
     {
         WordsManager wm = null;
         // TODO create a function to initialize this wordmanager
 
         // TODO complete with the future functions and classes
         if (this.level.getLevel() == DEFAULT_MAX_LEVEL)
             endGame();
         else
         {
             this.setlevel(new Level(this.level.getLevel() + 1, wm));
         }
     }
 
     /**
      * Give all conditions for the ending of this game.
      */
     private void endGame()
     {
         // TODO determiner les conditions d'arret du jeu.
     }
 
     /**
      * This function allow the user to know if the level is completed or not.
      * 
      * @return return true if the level is complete
      */
     // TODO a gérer dans level
     private boolean isLevelComplete()
     {
         // TODO complete with the future functions and classes
         return (this.getLevel().getAliveWords().isEmpty() && this.getLevel().getWordsManager()
                 .getWordsList(50, this.getLevel().getLevel()).isEmpty());
     }
 
 }
