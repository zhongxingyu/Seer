 /*
  * A re-implementation of the classic C=64 game 'Thrust'.
  * @author "Joe Kiniry (kiniry@acm.org)"
  *
  * @module "COMP 20050, COMP 30050"
  * @creation_date "March 2007"
  * @last_updated_date "April 2008"
  * @keywords "C=64", "Thrust", "game"
  */
 
 package thrust.entities.about;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 
 /**
  * The state of the Thrust game, including current score, bonus, fuel, lives,
  * and high scores.
  *
  * @author Joe Kiniry (kiniry@acm.org)
  * @version 11 April 2008
  */
public abstract class GameState extends AbstractGameState{
   /**
    * There are eight high scores.
    */
   public static final int HIGH_SCORE_COUNT = 8;
   //@ invariant HIGH_SCORE_COUNT == 8;
   //@ invariant (* There are eight high scores. *);
 
   /**
    * Stores a bonus value.
    */
   private int my_bonus;
   //@ ensures 0 <= my_bonus;
 
   /**
    * Stores a fuel value.
    */
   private int my_fuel;
   //@ ensures 0 <= my_fuel;
 
   /**
    * Stores the current score.
    */
   private int my_score;
   //@ ensures 0 <= my_score;
 
   /**
    * Stores the highScore in an array.
    */
   private HighScoreInterface[] my_highScore;
   //@ ensures 0 <= some_highScore[];
 
   /**
    * Stores the current number of lives.
    */
   private byte my_lives;
   //@ ensures 0 <= lives;
 
   /**
    * @return What is the current bonus?
    * @bon BONUS What is your value?
    */
 
   public /*@ pure @*/ int bonus()
   {
     return my_bonus;
   }
   //@ ensures 0 <= \result;
 
   /**
    * @param the_new_value This is your new value.
    */
   //@ requires 0 <= the_new_value;
   //@ ensures bonus() == the_new_value;
   public void new_bonus(final int the_new_value)
   {
     my_bonus = the_new_value;
   }
   //@ invariant (* Bonus values are always non-negative. *);
   //@ invariant 0 <= bonus();
 
   /**
    * @return How much fuel do you contain?
    * @idea The current fuel of the spaceship.
    * @note Note that the {@link thrust.entities.in_game.Spaceship}
    * class should be the actual owner of this data; this is just
    * a convenience method.
    */
   //@ ensures 0 <= \result;
   public /*@ pure @*/ int current_fuel()
   {
     return my_fuel;
   }
 
   /**
    * @return How much fuel can you contain?
    * @idea The maximum fuel of the spaceship.
    */
   //@ ensures 0 <= \result;
   public  /*@ pure @*/  int maximum_fuel()
   {
     final int the_max_feul = 2000;
     return the_max_feul;
   }
 
   /**
    * @return What is the current score?
    */
   //@ ensures 0 <= \result;
   public  /*@ pure @*/  int score()
   {
     return my_score;
   }
 
   //@ invariant (* Score is always non-negative and finite. *);
   //@ invariant 0 <= score();
 
   /**
    * Change the current score by this many points.
    * @param some_new_points the new points to add to the current score.
    */
   //@ ensures score() == \old(score() + some_new_points);
   public void change_score(final int some_new_points)
   {
     my_score = some_new_points;
   }
 
   /**
    * @return How many lives do you have?
    */
   //@ ensures 0 <= \result;
   public  /*@ pure @*/  byte lives()
   {
     return my_lives;
   }
 
   //@ invariant (* Number of lives is always non-negative and finite. *);
   //@ invariant 0 <= lives();
 
   /**
    * @param some_new_lives Change the current lives by this many lives.
    */
   //@ ensures lives() == \old(lives() + some_new_lives);
   public /*@ pure @*/ void change_lives(final byte some_new_lives)
   {
     my_lives = some_new_lives;
   }
 
   /**
    * @return What are the current high scores?
    */
   //@ ensures \result.length == HIGH_SCORE_COUNT;
   //@ ensures \nonnullelements(\result);
   public /*@ pure @*/ HighScoreInterface[] high_scores()
   {
     return my_highScore;
   }
 
   /*@ invariant (\forall int i, j; 0 <= i & i < j & j < HIGH_SCORE_COUNT &
     @            high_scores()[i].score() >= high_scores()[j].score());
     @ invariant (* High scores are ordered from high to low. *);
     @*/
 
   /*@ initially (* There is a fixed initial set of high scores. *);
     @ initially \nonnullelements(high_scores());
     @*/
 
   /**
    * @param the_index the index to lookup.
    * @return What is the high score at this index?
    */
   //@ requires 0 <= the_index & the_index < HIGH_SCORE_COUNT;
   //@ ensures \result.equals(high_scores()[the_index]);
   public /*@ pure non_null @*/
   HighScoreInterface high_score(final int the_index)
   {
     return my_highScore[the_index];
   }
 
   /**
    *
    * @param the_high_score the potential high score to check.
    * @return Is this score a new high score?
    */
   /*@ ensures \result <==> high_scores()[0].score() >= the_high_score.score() &
     @                      the_high_score.score() >= high_scores()[HIGH_SCORE_COUNT-1].score();
     @*/
   public /*@ pure @*/ boolean
   new_high_score(final HighScoreInterface the_highscore)
   {
     for (int j = 0; j < my_highScore.length; j++)
     {
       if (my_highScore[j].score() < the_highscore.score())
       {
         return true;
       }
     }
     return false;
   }
 
   /**
    * @param the_new_high_score Insert this score into the high score table.
   * @throws FileNotFoundException 
   * @throws FileNotFoundException 
    */
   /*@ ensures new_high_score(the_new_high_score) ==>
     @         (\exists int i; 0 <= i & i < HIGH_SCORE_COUNT;
     @          high_score(i).equals(the_new_high_score));
     @*/
   public void
   add_high_score(/*@ non_null @*/ final HighScoreInterface the_new_high_score)
   {
 
     try {
      FileReader fis   = new FileReader("HighScore.txt");
      BufferedReader input   = new BufferedReader(fis);
      char[] storeInput = new char[49];
       /*
        * stores initials and
        * scores as chars, then
        * converts them.
        */
 
      while(input.readLine()!=null)
       {
        String s = input.readLine();
        for(int i =0;i<s.length();i++)
         {
           storeInput[i] = s.charAt(i);
         }
       }
     }
 catch (IOException e) {
}
 
 
 
 
     /**
      * the found variable is true if the value in
      * score variable in the HighScoreInterface array
      * is less than the current score.
      */
     boolean found = false;
     if (new_high_score(the_new_high_score))
     {
       for (int j = 0; j < my_highScore.length; j++)
       {
         if (my_highScore[j].score() < the_new_high_score.score() && !found)
         {
           my_highScore[j] = the_new_high_score;
           found = true;
         }
         if (found)
         {
           my_highScore[j - 1] = my_highScore[j];
         }
       }
     }
   }
 
   /**
    * A pair of a sequence of three initials and a score.
    *
    * @author Joe Kiniry (kiniry@acm.org)
    * @version 11 April 2008
    */
 }
