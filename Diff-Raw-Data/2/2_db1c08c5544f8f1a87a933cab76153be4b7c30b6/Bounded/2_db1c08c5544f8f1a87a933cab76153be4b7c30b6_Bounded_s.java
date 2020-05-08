 /*
  * Copyright (C) 2010 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.novelang.novelist;
 
 import java.util.Random;
 
 import com.google.common.base.Preconditions;
 
 /**
  * Various manipulation primitives for bounded values.
  *
  * @author Laurent Caillette
  */
 public class Bounded {
 
   private Bounded() { }
 
 
 // ==========
 // Percentage
 // ==========
 
  private static Percentage newPercentage( final float value ) {
     return new Percentage( value ) ;
   }
 
   public static Percentage newPercentage( final Random random ) {
     return newPercentage( percentage( random ) ) ;
   }
 
   private static float percentage( final Random random ) {
     return ( float ) ( 100.0 * Math.abs( random.nextDouble() ) ) ;
   }
 
 
   /**
    * A fractional value between 0 and 100.
    *
    * @author Laurent Caillette
    */
   public static class Percentage {
 
     private final float value ;
 
     private Percentage( final float value ) {
       Preconditions.checkArgument( isValid( value ) ) ;
       this.value = value ;
     }
 
     public static boolean isValid( final Float value ) {
       return ( value != null ) && ( value >= 0.0f ) && ( value <= 100.0f ) ;
     }
 
     public boolean hit( final Random random ) {
       return percentage( random ) < value;
     }
 
     public boolean isStrictlySmallerThan( final float other ) {
       return value < other ;
     }
 
     @Override
     public String toString() {
       return getClass().getSimpleName() + "[" + value + "]" ;
     }
   }
 
 
 // =====
 // Range
 // =====
 
   public static IntegerInclusiveExclusive newInclusiveRange(
       final int lowerBoundInclusive,
       final int upperBoundInclusive
   ) {
     return new IntegerInclusiveExclusive( lowerBoundInclusive, upperBoundInclusive + 1 ) ;
   }
   
 
   /**
    * Range defined by two positive integers.
    *
    * @author Laurent Caillette
    */
   public static class IntegerInclusiveExclusive {
 
     private final int lowerBoundInclusive ;
     private final int upperBoundExclusive;
 
     private IntegerInclusiveExclusive(
         final int lowerBoundInclusive,
         final int upperBoundExclusive
     ) {
       Preconditions.checkArgument( lowerBoundInclusive >= 0 ) ;
 
       // This is required by Random.nextInt( int ).
       Preconditions.checkArgument( upperBoundExclusive > lowerBoundInclusive ) ;
 
       this.lowerBoundInclusive = lowerBoundInclusive ;
       this.upperBoundExclusive = upperBoundExclusive;
     }
 
     public int boundInteger( final Random random ) {
       return
           lowerBoundInclusive +
           random.nextInt( upperBoundExclusive - lowerBoundInclusive )
       ;
     }
 
 
   }
 }
