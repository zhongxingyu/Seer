 package org.cyclopsgroup.eulerer.p220;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.lang.Validate;
 
 /**
  * Class that traverse and remember location. Implemented in a brutal force solution that takes time linear to number of
  * steps
  *
  * @author <a href="mailto:jiaqi@cyclopsgroup.org">Jiaqi Guo</a>
  */
 public class HeighwayDragon
 {
     @SuppressWarnings( "serial" )
     private static class TerminationSignal
         extends Error
     {
     }
 
     private static final char[] INITIAL_PLAN = "Fa".toCharArray();
 
     private static final char[] PLAN_A = "aRbFR".toCharArray();
 
     private static final char[] PLAN_B = "LFaLb".toCharArray();
 
     private Map<String, Path> cachedPaths = new HashMap<String, Path>();
 
     private final int maxLevels;
 
     private Traveler traveler = new Traveler();
 
     /**
      * @param maxLevels Number of order, or levels 'a' and 'b' are expanded
      */
     public HeighwayDragon( int maxLevels )
     {
        Validate.isTrue( maxLevels >= 0, "Invalid number of max levels " + maxLevels );
         this.maxLevels = maxLevels;
     }
 
     private void expandSubstitution( long maxSteps, int level, char substitution )
     {
         if ( level >= maxLevels )
         {
             return;
         }
         String pathKey = level + "-" + substitution;
 
         // If it's a known path and path doesn't exceed max steps, return result directly
         Path path = cachedPaths.get( pathKey );
         if ( path != null && path.steps + traveler.getSteps() <= maxSteps )
         {
             traveler.walk( path );
             return;
         }
 
         // If path is unknown, cache it
         Traveler from = traveler.clone();
         switch ( substitution )
         {
             case 'a':
                 traverseFor( maxSteps, level + 1, PLAN_A );
                 break;
             case 'b':
                 traverseFor( maxSteps, level + 1, PLAN_B );
                 break;
             default:
                 throw new AssertionError( "Unknown substitution" );
         }
         if ( path != null )
         {
             return;
         }
         path = traveler.pathFrom( from );
         cachedPaths.put( pathKey, path );
     }
 
     /**
      * @return Current location
      */
     public final Traveler getTraveler()
     {
         return traveler;
     }
 
     /**
      * @param maxSteps Traverse for given number of steps
      */
     public void traverseFor( long maxSteps )
     {
         Validate.isTrue( maxSteps > 0, "Invalid number of max steps " + maxSteps );
 
         if ( maxSteps <= 0 )
         {
             throw new IllegalArgumentException( "Invalid steps " + maxSteps );
         }
         try
         {
             traverseFor( maxSteps, 0, INITIAL_PLAN );
         }
         catch ( TerminationSignal t )
         {
             // The last step is taken, return expectedly
             return;
         }
         // The last step has not been taken
         throw new IllegalArgumentException( maxLevels + " levels plan doesn't have " + maxSteps + " steps" );
     }
 
     private void traverseFor( final long maxSteps, int level, char[] plan )
     {
         for ( char ch : plan )
         {
             switch ( ch )
             {
                 case 'F':
                     traveler.stepForward();
                     break;
                 case 'L':
                     traveler.turnLeft();
                     break;
                 case 'R':
                     traveler.turnRight();
                     break;
                 case 'a':
                 case 'b':
                     expandSubstitution( maxSteps, level, ch );
                     break;
                 default:
                     throw new AssertionError( "Unknown plan " + ch );
             }
             if ( maxSteps == traveler.getSteps() )
             {
                 // An error that tells last step is take can interrupt all recursions nicely
                 // This is slightly easier than returning a value
                 throw new TerminationSignal();
             }
         }
     }
 }
