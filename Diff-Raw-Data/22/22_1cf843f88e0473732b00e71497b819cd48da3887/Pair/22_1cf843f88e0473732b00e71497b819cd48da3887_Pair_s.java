 /*
   This file is part of opensearch.
   Copyright © 2009, Dansk Bibliotekscenter a/s,
   Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
 
   opensearch is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
 
   opensearch is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package dk.dbc.opensearch.common.types;
 
 
 /**
  * Pair defines a type that takes two elements and stores them for later
  * retrieval.
  */
public interface Pair<E, V>// implements Comparator
 {
 
     E getFirst();
 

     V getSecond();
 
 
     /**
      * Asserts equality of the {@link Pair} object and another {@link Pair}
      * object
      * @param obj the {@link Pair} to compare this object with
      * @return true if the {@link Pair} is equal to this object
      */
     boolean equals( Object obj );
 
 
     /**
      * @return a string representation of the object
      */
     @Override
     String toString();
 
 
     /**
      * @return the hash code of this object
      */
     @Override
     int hashCode();
 
 
 }
