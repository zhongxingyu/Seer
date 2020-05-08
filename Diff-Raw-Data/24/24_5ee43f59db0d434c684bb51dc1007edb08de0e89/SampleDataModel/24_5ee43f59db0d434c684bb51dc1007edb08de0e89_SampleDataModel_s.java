 /**
  * 
  */
 package nl.lxtreme.test.model;
 
 
 import java.util.*;
 
 
 /**
  * @author jajans
  */
 public class SampleDataModel
 {
   // VARIABLES
 
   private final int[] values;
   private final long[] timestamps;
   private final int[] cursors;
   private int sampleRate;
 
   // CONSTRUCTORS
 
   /**
    * @param aSize
    */
   public SampleDataModel( final int aSize )
   {
     if ( aSize <= 0 )
     {
       throw new IllegalArgumentException();
     }
     this.values = new int[aSize];
     this.timestamps = new long[aSize];
 
    final boolean run1 = true;
     if ( run1 )
     {
       int value = 0xAAA;
       for ( int i = 0; i < aSize; i++ )
       {
         // A transition each 5ns...
         if ( ( i % 5 ) == 0 )
         {
           value = ( value == 0xAAA ) ? 0x555 : 0xAAA;
         }
 
         this.values[i] = value;
         this.timestamps[i] = i;
       }
 
       this.sampleRate = 1000000000; // 1000MHz
     }
     else
     {
       for ( int i = 0; i < aSize; i++ )
       {
         this.values[i] = ( ( 1 * i ) % 1024 );
         this.timestamps[i] = i;
       }
 
       this.sampleRate = 1000000000;
     }
     this.cursors = new int[] { 100, 200 };
   }
 
   // METHODS
 
   /**
    * Provides a binary search for arrays of long-values.
    * <p>
    * This implementation is directly copied from the JDK
    * {@link Arrays#binarySearch(long[], long)} implementation, slightly modified
    * to only perform a single comparison-action.
    * </p>
    * 
    * @param aArray
    *          the array of long values to search in;
    * @param aFromIndex
    *          the from index to search from;
    * @param aToIndex
    *          the to index to search up and until;
    * @param aKey
    *          the value to search for.
    * @return the index of the given key, which is either the greatest index of
    *         the value less or equal to the given key.
    * @see Arrays#binarySearch(long[], long)
    */
   static final int binarySearch( final long[] aArray, final int aFromIndex, final int aToIndex, final long aKey )
   {
     int mid = -1;
     int low = aFromIndex;
     int high = aToIndex - 1;
 
     while ( low <= high )
     {
       mid = ( low + high ) >>> 1;
       final long midVal = aArray[mid];
 
       final int c = ( aKey < midVal ? -1 : ( aKey == midVal ? 0 : 1 ) );
       if ( c > 0 )
       {
         low = mid + 1;
       }
       else if ( c < 0 )
       {
         high = mid - 1;
       }
       else
       {
         return mid; // key found
       }
     }
 
     if ( mid < 0 )
     {
       return low;
     }
 
     // Determine the insertion point, avoid crossing the array boundaries...
     if ( mid < aToIndex - 1 )
     {
       // If the searched value is greater than the value of the found index,
       // insert it after this value, otherwise before it (= the last return)...
       if ( aKey > aArray[mid] )
       {
         return mid + 1;
       }
     }
 
     return mid;
   }
 
   public long getAbsoluteLength()
   {
     int idx = this.timestamps.length - 1;
     if ( idx >= 0 )
     {
       return this.timestamps[idx] + 1;
     }
     return -1;
   }
 
   public int[] getCursors()
   {
     return this.cursors;
   }
 
   public int getSampleRate()
   {
     return this.sampleRate;
   }
 
   public int getSize()
   {
     return this.values.length;
   }
 
   public int getTimestampIndex( final long aValue )
   {
     final int length = this.timestamps.length;
     return binarySearch( this.timestamps, 0, length, aValue );
   }
 
   public long[] getTimestamps()
   {
     return this.timestamps;
   }
 
   public int[] getValues()
   {
     return this.values;
   }
 
   public int getWidth()
   {
     return 9;
   }
 }
