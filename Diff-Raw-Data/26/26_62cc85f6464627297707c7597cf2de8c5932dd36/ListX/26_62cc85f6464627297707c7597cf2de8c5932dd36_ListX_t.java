 
 package com.gec.questoesGratis.tools;
 
 import java.util.List;
 import java.util.Random;
 
 public final class ListX {
 
    /**
     * Fisher-Yates shuffle list.
     * 
     * @param list the list to shuffle.
     * 
     * @see {http://en.wikipedia.org/wiki/Fisher-Yates_shuffle}
     */
    public static void shuffle( List< Long > list ) {
 
       final int n = list.size();
       final Random random = new Random();
 
       random.nextInt();
       for( int i = 0; i < n; i++ ) {
 
          final int target = i + random.nextInt( n - i );
          swap( list, i, target );
       }
    }
 
    /**
     * Simple swap list items.
     * 
     * @param list the list.
     * @param source the source item.
     * @param target the target item to swap to.
     */
    public static void swap( List< Long > list, int source, int target ) {
 
       final Long helper = list.get( source );
       list.set( source, list.get( target ) );
       list.set( target, helper );
    }
 
    /**
     * Return a random start index to get a sub list of the given list.
     * 
     * @param list
    * @param total the items count to get.
     * @return the random start index.
     */
   public static int randomStart( List< Long > list, int total ) {
 
       final int r;
 
       final int n = list.size();
      if( total > n ) {
          r = 0;
       } else {
          final Random random = new Random();
         r = random.nextInt( n - total );
       }
 
       return r;
    }
 }
