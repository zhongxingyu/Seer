 /*
  * A re-implementation of the classic C=64 game 'Thrust'.
  *
  * @author "Colin Casey (colin.casey@org.com)"
  * @module "COMP 20050, COMP 30050"
  * @creation_date "April 2008"
  * @last_updated_date "April 2008"
  * @keywords "C=64", "Thrust", "game"
  */
 
 package thrust.entities.about;
 
 /**
  * @author Colin Casey (colin.casey@org.com)
 * @version 23 April 2008
  */
 public class GameState extends AbstractGameState {
 
   /** The bonus points associated with finishing a level. */
   private static int my_bonus;
   /** The current score of a player. */
   private static int my_score;
   /** The number of lives a player has left. */
   private static byte my_lives;
   /** The highest scores ever recorded in the game. */
   private static HighScoreInterface[] my_high_scores;
  /** */
  private static final KKFuelable MY_FUEL_MONITOR = new KKFuelable();
 
   public int bonus() {
     return my_bonus;
   }
 
   public void new_bonus(final int the_new_value) {
     my_bonus = the_new_value;
   }
 
   public int current_fuel() {
    return MY_FUEL_MONITOR.fuel();
   }
 
   public int maximum_fuel() {
    return MY_FUEL_MONITOR.maximum_fuel();
   }
 
   public int score() {
     return my_score;
   }
 
   public void change_score(final int some_new_points) {
     my_score = my_score + some_new_points;
   }
 
   public byte lives() {
     return my_lives;
   }
 
   public void change_lives(final byte some_new_lives) {
     my_lives = (byte)(my_lives + some_new_lives);
   }
 
   public HighScoreInterface[] high_scores() {
    final HighScoreInterface[] temp = new HighScoreInterface[HIGH_SCORE_COUNT];
    System.arraycopy(my_high_scores, 0, temp, 0, my_high_scores.length);
    return temp;
   }
 
   public HighScoreInterface high_score(final int the_index) {
     return my_high_scores[the_index];
   }
 
   public boolean new_high_score(final HighScoreInterface the_high_score) {
     return (the_high_score.score() >
       high_scores()[HIGH_SCORE_COUNT - 1].score());
   }
 
   public void add_high_score(final HighScoreInterface the_new_high_score) {
 
     HighScoreInterface temp;
 
     for (int i = HIGH_SCORE_COUNT - 1; i > -1; i--) {
       temp = high_scores()[i];
       if (the_new_high_score.score() > high_scores()[i].score())
       {
         high_scores()[i] = the_new_high_score;
       }
       if (i + 1 > HIGH_SCORE_COUNT - 1)
       {
         high_scores()[i + 1] = temp;
       }
     }
   }
 
   /**
    * A pair of a sequence of three initials and a score.
    *
    * @author Colin Casey (colin.casey@org.com)
    * @version 17 April 2008
    */
   public abstract class AbstractHighScore implements HighScoreInterface {
 
     /** The initials of the player. */
     private char[] my_initials;
 
     public int score() {
       return my_score;
     }
 
     public char[] initials() {
      final char[] temp = new char[my_initials.length];
      System.arraycopy(my_initials, 0, temp, 0, my_initials.length);
      return temp;
     }
 
     public void new_score(final int the_new_score) {
       my_score = the_new_score;
     }
 
     public void new_initials(final char[] the_new_initials) {
       my_initials = the_new_initials;
     }
   }
 }
