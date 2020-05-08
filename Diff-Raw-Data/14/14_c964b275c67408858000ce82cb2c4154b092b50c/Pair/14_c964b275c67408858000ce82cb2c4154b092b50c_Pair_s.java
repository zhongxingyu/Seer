 // Copyright 2010 Nathaniel Harward
 //
 // This file is part of ndh-commons.
 //
 // ndh-commons is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // ndh-commons is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with ndh-commons. If not, see <http://www.gnu.org/licenses/>.
 
 package nerds.antelax.commons.util;
 
 import com.google.common.base.Preconditions;
 
 /**
  * Modeled after C++'s STL std::pair. Safe to use in collections insofar as the constituent types are.
 * 
  * @param <T1>
  *            the first object type in the pair
  * @param <T2>
  *            the second object type in the pair
 * @see http://www.cplusplus.com/reference/std/utility/pair/
  */
 public class Pair<T1, T2> {
 
     private final T1 first;
     private final T2 second;
 
     public Pair(final T1 first, final T2 second) {
     	Preconditions.checkArgument(first != null);
     	Preconditions.checkArgument(second != null);
         this.first = first;
         this.second = second;
     }
 
     public Pair(final Pair<T1, T2> pair) {
         Preconditions.checkArgument(pair != null);
         first = pair.first();
         second = pair.second();
     }
 
     public static <U, V> Pair<U, V> make_pair(final U first, final V second) {
         return new Pair<U, V>(first, second);
     }
 
     public T1 first() {
         return first;
     }
 
     public T2 second() {
         return second;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((first == null) ? 0 : first.hashCode());
         result = prime * result + ((second == null) ? 0 : second.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (obj instanceof Pair<?, ?>) {
             final Pair<?, ?> other = (Pair<?, ?>) obj;
             return first.equals(other.first()) && second.equals(other.second());
         }
         return false;
     }
 
 }
