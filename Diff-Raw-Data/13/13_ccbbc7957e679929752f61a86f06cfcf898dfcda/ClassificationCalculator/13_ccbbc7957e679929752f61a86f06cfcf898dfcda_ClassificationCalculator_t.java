 /*----------------    FILE HEADER  ------------------------------------------
  This file is part of deegree.
  Copyright (C) 2001-2007 by:
  Department of Geography, University of Bonn
  http://www.giub.uni-bonn.de/deegree/
  lat/lon GmbH
  http://www.lat-lon.de
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  Contact:
 
  Andreas Poth
  lat/lon GmbH
  Aennchenstr. 19
  53177 Bonn
  Germany
  E-Mail: poth@lat-lon.de
 
  Prof. Dr. Klaus Greve
  Department of Geography
  University of Bonn
  Meckenheimer Allee 166
  53115 Bonn
  Germany
  E-Mail: greve@giub.uni-bonn.de
  ---------------------------------------------------------------------------*/
 
 package org.deegree.igeo.style.classification;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.deegree.igeo.style.model.classification.Intervallable;
 import org.deegree.igeo.style.model.classification.ValueRange;
 
 /**
  * <code>ClassificationCalculator</code>
  * 
  * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
  * @author last edited by: $Author$
  * 
  * @version $Revision$, $Date$
  * 
  */
 public class ClassificationCalculator<U extends Comparable<U>> {
 
     public List<ValueRange<U>> calculateUniqueValues( List<Intervallable<U>> values ) {
         List<ValueRange<U>> classified = new ArrayList<ValueRange<U>>( values.size() );
         for ( Intervallable<U> value : values ) {
             classified.add( new ValueRange<U>( value, value, 1 ) );
         }
         return classified;
     }
 
     public List<ValueRange<U>> calculateQualityClassification( List<Intervallable<U>> values ) {
         List<ValueRange<U>> classified = new ArrayList<ValueRange<U>>();
         for ( Intervallable<U> value : values ) {
             boolean isInserted = false;
             for ( ValueRange<U> vr : classified ) {
                 if ( value.equals( vr.getMin() ) ) {
                     vr.increaseCount();
                     isInserted = true;
                 }
             }
             if ( !isInserted ) {
                 classified.add( new ValueRange<U>( value, value, 1 ) );
             }
         }
         return classified;
     }
 
     /**
      * Calculates a classification with equal number of values in each class. The number ob returning maximum values may
     * differ from the given number of classes, if there are a lot of equal values. If numberOfClasses is greater than
     * the number of values the numberOfClasses is set to the number of values.
      * 
      * @param values
      *            the values to classify
      * @param numberOfClasses
      *            the number of classes
      * @return a list with the maximum values of the classes
      */
     public List<ValueRange<U>> calculateQuantileClassification( List<Intervallable<U>> values, int numberOfClasses ) {
        if ( numberOfClasses > values.size() ) {
            numberOfClasses = values.size();
        }
         List<ValueRange<U>> result = new ArrayList<ValueRange<U>>();
         Collections.sort( values, new Comparator<Intervallable<U>>() {
             public int compare( Intervallable<U> o1, Intervallable<U> o2 ) {
                 return o1.getValue().compareTo( o2.getValue() );
             }
 
         } );
 
         if ( values != null && values.size() > 0 ) {
             // if all values in the value list are the same, create two classes, 'null - value' and
             // 'value - null'
             if ( values.get( 0 ).equals( values.get( values.size() - 1 ) ) ) {
                 result.add( new ValueRange<U>( null, values.get( 0 ), 0 ) );
                 result.add( new ValueRange<U>( values.get( 0 ), null, values.size() ) );
             } else {
                 int valuesPerClass = (int) Math.floor( values.size() / numberOfClasses );
 
                 // position of the max value inserted in the last class
                 int pos = valuesPerClass - 1;
                 // number of values put in classes
                 int counter = 0;
                 // the min value of the next class
                 Intervallable<U> minValue = null;
 
                 for ( int i = 0; i < numberOfClasses; i++ ) {
                     // the max value of this class
                     Intervallable<U> maxValue = null;
                     // number of values in this class
                     int count = valuesPerClass;
 
                     // increase position and the number of values in this class if values at the
                     // current and the next position equals
                     if ( pos < values.size() - 1 && values.get( pos ).equals( values.get( pos + 1 ) ) ) {
                         while ( pos < values.size() - 1 && values.get( pos ).equals( values.get( pos + 1 ) ) ) {
                             pos++;
                             count++;
                         }
                     }
                     // calculate mean value, if the last class is not reached
                     if ( pos < values.size() - 1 ) {
                         maxValue = values.get( pos ).calculateMean( values.get( pos + 1 ).getValue() );
                     }
 
                     // update position
                     pos = pos + valuesPerClass;
 
                     // last class is reached, add value range with null as max and break the loop
                     if ( pos > values.size() || i == numberOfClasses ) {
                         count = values.size() - counter;
                         result.add( new ValueRange<U>( minValue, null, count ) );
                         break;
                     }
                     result.add( new ValueRange<U>( minValue, maxValue, count ) );
                     minValue = maxValue;
                     counter = counter + count;
                 }
             }
         }
         return result;
     }
 
     /**
      * Calculates a classification with same range of the classes. If the values are all equal, an map with only one
      * entry (the containing value) will be returned.
      * 
      * @param values
      *            the values to classify
      * @param numberOfClasses
      *            the number of classes
      * @return a list with the maximum values of the classes
      */
     public List<ValueRange<U>> calculateEqualInterval( List<Intervallable<U>> values, int numberOfClasses ) {
         List<ValueRange<U>> result = new ArrayList<ValueRange<U>>();
         Collections.sort( values, new Comparator<Intervallable<U>>() {
             public int compare( Intervallable<U> o1, Intervallable<U> o2 ) {
                 return o1.getValue().compareTo( o2.getValue() );
             }
 
         } );
         if ( values != null && values.size() > 0 ) {
 
             Intervallable<U> min = values.get( 0 );
             Intervallable<U> max = values.get( values.size() - 1 );
 
             Intervallable<U> interval = min.calculateInterval( max.getValue(), numberOfClasses );
 
             // return classification with the single value, if range of the classes would be 0
             if ( min.equals( max ) ) {
                 result.add( new ValueRange<U>( max, max, values.size() ) );
                 return result;
             }
             Intervallable<U> lastMax = null;
             Intervallable<U> current = min;
             int countIndex = 0;
             for ( int i = 0; i < numberOfClasses; i++ ) {
                 current = current.getNextValue( interval.getValue() );
                 // ensure, that the last value range does not have a upper limit
                 Intervallable<U> currentMaxValue = current;
                 if ( i == numberOfClasses - 1 ) {
                     currentMaxValue = null;
                 }
                 Intervallable<U> lastMaxValue = null;
                 if ( lastMax != null ) {
                     lastMaxValue = lastMax;
                 }
                 int count = 0;
                 if ( currentMaxValue != null ) {
                     while ( values.get( countIndex ).getValue().compareTo( currentMaxValue.getValue() ) < 0 ) {
                         countIndex++;
                         count++;
                     }
                 } else {
                     count = values.size() - countIndex;
                 }
                 result.add( new ValueRange<U>( lastMaxValue, currentMaxValue, count ) );
                 lastMax = current;
             }
 
         }
         return result;
     }
 }
