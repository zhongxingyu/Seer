 /*
  * DISASTEROIDS
  * Util.java
  */
 package disasteroids;
 
 import java.text.DecimalFormat;
 import java.util.Random;
 
 /**
  * A centrallized class for useful methods.
  * @author Andy Kooiman
  */
 public class Util
 {
     /**
      * Global array of random generators.
      * @see Util#getRandomGenerator()
      */
     private static ExtendedRandom[] instances =
     {
         new ExtendedRandom(), new ExtendedRandom(), new ExtendedRandom()
     };
 
     /**
      * The last used random generator in <code>instances</code>.
      * @see Util#instances
      */
     private static int lastGeneratorUsed = 0;
 
     /**
      * The formatter used in <code>insertThousandCommas</code>
      * @see Util#insertThousandCommas(int) 
      */
     private static DecimalFormat thousands = new DecimalFormat( "" );
 
     /**
      * Takes the given number and inserts comma seperators at each grouping.
      * "491911920518159419" becomes "491,911,920,518,159,419".
      */
     public static String insertThousandCommas( int number )
     {
         return thousands.format( number );
     }
 
     /**
      * Finds the distance between two game object using pythagorous, and compensating for the 
      * boundary of the level
      * 
      * Note: When the distance is large (about halve the width of the level) the distance may not be the
      * smallest possible.
      * 
      * @param one The first GameObject
      * @param two The second GameObject
      * @return The distance
      */
     public static double getDistance( GameObject one, GameObject two )
     {
         double deltaX = getDeltaX( one, two );
         double deltaY = getDeltaY( one, two );
         return Math.sqrt( deltaX * deltaX + deltaY * deltaY );
     }
 
     /**
      * Returns the x distance between two <code>GameObject</code>s.  Always a number between 
      * -GAME_WIDTH/2 and GAME_WIDTH/2.
      * Calculated as one-two.  A positive value indicates that one is to the right of two.
      * 
      * @param one The first <code>GameObject</code>
      * @param two The second <code>GameObject</code>
      * @return The distance between the two <code>GameObject</code>s
      */
     public static double getDeltaX( GameObject one, GameObject two )
     {
         double deltaX = ( one.getX() - two.getX() + Game.getInstance().GAME_WIDTH * 2 ) % Game.getInstance().GAME_WIDTH;
         if ( Math.abs( deltaX - Game.getInstance().GAME_WIDTH ) < Math.abs( deltaX ) )
             return deltaX - Game.getInstance().GAME_WIDTH;
         return deltaX;
     }
 
     /**
      * Returns the y distance between two <code>GameObject</code>s.  Always a number between 
      * -GAME_HEIGHT/2 and GAME_HEIGHT/2.
      * Calculated as one-two.  A positive value indicates that one is below two.
      * 
      * @param one The first <code>GameObject</code>
      * @param two The second <code>GameObject</code>
      * @return The distance between the two <code>GameObject</code>s
      */
     public static double getDeltaY( GameObject one, GameObject two )
     {
         double deltaY = ( one.getY() - two.getY() + Game.getInstance().GAME_HEIGHT * 2 ) % Game.getInstance().GAME_HEIGHT;
         if ( Math.abs( deltaY - Game.getInstance().GAME_HEIGHT ) < Math.abs( deltaY ) )
             return deltaY - Game.getInstance().GAME_HEIGHT;
         return deltaY;
     }
 
     /**
      * Calculates and returns an angle between -pi and pi that represents the direction
      * that one would go to coincide with two.
      * 
      * @param one The <code>GameObject</code> representing the start of this unit vector
      * @param two The <code>GameObject</code> representing the end of this unif vector
      * @return The angle from one to two
      */
     public static double getAngle( GameObject one, GameObject two )
     {
         return Math.atan2( getDeltaY( one, two ), +getDeltaX( one, two ) );
     }
 
     /**
      * Returns a global random generator, which gives better pseudorandomness than constantly making a new instance.
      * Multiple instances are cycled through to relieve bottlenecks.
      * 
      * @return  a static instance of <code>Random</code>
      */
     public static ExtendedRandom getRandomGenerator()
     {
         lastGeneratorUsed = ( lastGeneratorUsed + 1 ) % instances.length;
         return instances[lastGeneratorUsed];
     }
 
     /**
      * Java's random generator, with a few additional methods.
      * @author Phillip Cohen
      */
     public static class ExtendedRandom extends Random
     {
         /**
          * Randoms a random double between 0 and 2 * PI.
          */
         public double nextAngle()
         {
            return nextDouble() * 2 * Math.PI;            
         }
 
         /**
          * Returns a random double from -n/2 to n/2.
          */
         public static double nextMidpointDouble( int n )
         {
             return getRandomGenerator().nextDouble() * n - n / 2;
         }
     }
 }
