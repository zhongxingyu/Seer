 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package disasteroids;
 
 import java.text.DecimalFormat;
 import java.util.Random;
 
 /**
  *
  * @author AK90454
  */
 public class Util
 {
     private static DecimalFormat thousands = new DecimalFormat( "" );
 
     /**
      * Inserts comma seperators at each grouping.
      * 
      * @param number    The number to be formatted.
      * @return  the formatted string.
      * @since December 15, 2007
      */
     public static String insertThousandCommas( int number )
     {
         return thousands.format( number );
     }
 
     /**
      * Finds the distance between two game object using pythagorous, and compensating for the 
      * boundary of the level
      * @param one The first GameObject
      * @param two The second GameObject
      * @return The distance
      */
     public static double getDistance( GameObject one, GameObject two )
     {
//        double deltaX = ( one.getX() - two.getX() + Game.getInstance().GAME_WIDTH * 1 ) % Game.getInstance().GAME_WIDTH;
//        double deltaY = ( one.getY() - two.getY() + Game.getInstance().GAME_HEIGHT * 1 ) % Game.getInstance().GAME_HEIGHT;
//        return Math.sqrt( deltaX * deltaX + deltaY * deltaY );
        return Math.sqrt(Math.pow((one.getX()-two.getX())%Game.getInstance().GAME_WIDTH,2) 
                + Math.pow((one.getY()-two.getY())%Game.getInstance().GAME_HEIGHT,2));
     }
     
     private static Random[] instances = { new Random(), new Random(), new Random() };
 
     private static int nextRandomGenerator = 0;
 
     /**
      * Returns a global random generator.
      * Multiple instances may be used for speed
      * 
      * @return  a static instance of <code>Random</code>
      * @since January 18, 2008
      */
     public static Random getRandomGenerator()
     {
 
         nextRandomGenerator = ( nextRandomGenerator + 1 ) % instances.length;
         return instances[nextRandomGenerator];
     }
 
     public static double nextMidpointDouble()
     {
         return getRandomGenerator().nextDouble() * 2 - 1;
     }
 }
