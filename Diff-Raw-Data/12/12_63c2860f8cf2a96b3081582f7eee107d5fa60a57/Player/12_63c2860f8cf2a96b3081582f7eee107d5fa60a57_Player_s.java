// TODO (FIXED) use only one pacakge for model-related classes
// e.g. fr.iutvalence.java.mp.mygame
 package fr.iutvalence.java.mp.thelasttyper.client.data;
 
 /**
  * This class contain all the informations about a player. For now we are only
  * thinking about off-line mod.
  * 
  * @author culasb
  */
 public class Player
 {
     /**
      * initial lives
      */
     private static final int DEFAULT_LIVES = 3;
 
     /**
      * initial score
      */
     private static final int DEFAULT_SCORE = 0;
 
     /**
      * Player's name
      */
     private String playerName;
 
     /**
      * Player's score
      */
     private int score;
 
     /**
      * Player's lives
      */
     private int lives;
 
     /**
      * Player instantiation. Player's score = 0 and player's lives = 3. His name
      * is determined by s;
      * 
      * @param s
      *            Player's name
      */
     public Player(String s)
     {
         this.playerName = s;
         this.setScore(DEFAULT_SCORE);
         this.setLives(DEFAULT_LIVES);
     }
 
     /**
      * Get player's name
      * 
      * @return the player's name
      */
     public String getPlayerName()
     {
         return this.playerName;
     }
 
     // SCORE
 
     /**
      * Get player's score
      * 
      * @return the player's current score
      */
     public int getScore()
     {
         return this.score;
     }
 
     /**
      * Set player Name
      * 
      * @param score
      *            the new score
      */
     public void setScore(int score)
     {
         this.score = score;
     }
 
     /**
      * Increase the player's score
      * 
      * @param amount
      *            the amount of point that will be add to the score.
      * @return the new incremented score
      */
     public int increaseScore(int amount)
     {
         this.score = this.score + amount;
         return getScore() + amount;
     }
 
     // LIVES
 
     /**
      * Return player's lives
      * 
      * @return player's current lives
      */
     public int getLives()
     {
         return this.lives;
     }
 
     /**
      * Set player's lives
      * 
      * @param lives
      *            the new amount of lives
      */
     public void setLives(int lives)
     {
         this.lives = lives;
     }
 
     /**
      * Remove one life to the player's lives
      */
     public void decreaseLives()
     {
         this.lives = this.lives--;
     }
 
     /**
      * Set the lives to the initial value
      */
     public void resetLives()
     {
         this.setLives(DEFAULT_LIVES);
     }
 }
