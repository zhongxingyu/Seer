 package fr.iutvalence.java.mp.thelasttyper.client.data;
 
 
 /**
  * Class representing a game. For have rules read : readme.txt .
  * 
  * @author culasb
  */
 public class Game
 {
     /**
      * Game's player
      */
     public Player player;
     
     /**
      * the default level of a game
      */
     private final static int DEFAULT_LEVEL = 1;
 
     /**
      * the default max level of a game
      */
     private final static int DEFAULT_MAX_LEVEL = 5;
 
     /**
      * Create a new level
      */
     Level level;
 
     /**
      * Game instantiation : The level is initialize at the Default level(1) and
      * create to recover a list of words with the default level. More over a
      * player is initialize
      */
     public Game()
     {
         WordsManager wm = null;
         //TODO create a function to initialize this wordmanager
         this.level = new Level(DEFAULT_LEVEL,wm);
         // TODO (fixed) simply field initialization (useless temp variable)
         this.player = new Player("test");
 
         
     }
 
     /**
      * return the current player of this game.
      *@return Game's Player
      */
     public Player getPlayer()
     {
         return this.player;
     }
 
     /**
      * get the default level of a game
      * 
      * @return the default level, an int
      */
     public static int getDefaultLevel()
     {
         return DEFAULT_LEVEL;
     }
 
     /**
      * get the default maximum level of a game
      * 
      * @return the default maxlevel
      */
 
     public static int getDefaultMaxLevel()
     {
         return DEFAULT_MAX_LEVEL;
     }
 
     /**
      * return the current level of the current game
      * 
      * @return an int
      */
     public Level getLevel()
     {
         return this.level;
     }
 
     /**
      * change the level of the current game
      * 
      * @param level
      *            the new level (int)
      */
     public void setlevel(Level level)
     {
         this.level = level;
     }
 
   
 
     // LEVEL UP
     /**
      * Used when a new level is started
      */
     public void levelUp()
     {
         WordsManager wm = null;
       //TODO create a function to initialize this wordmanager
         
         // TODO complete with the future functions and classes
         if (this.getLevel().getLevel() == DEFAULT_MAX_LEVEL)
             endGame();
         else
         {
             this.setlevel(new Level(this.getLevel().getLevel()+1, wm));
         }
     }
 
     /**
      * Give all conditions for the ending of this game.
      */
     public void endGame()
     {
         // TODO determiner les conditions d'arret du jeu.
     }
 
     /**
      * This function allow the user to know if the level is completed or not.
      * 
      * @return return true if the level is complete
      */
     public boolean isLevelComplete()
     {
         // TODO complete with the future functions and classes
        return (this.getLevel().getAliveWords().isEmpty() && this.getLevel().getWordsManager().getWordsList(50, this.getLevel().getLevel()).isEmpty());
     }
 
 }
