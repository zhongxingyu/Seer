 /* This file is part of VoltDB.
  * Copyright (C) 2008-2010 VoltDB L.L.C.
  *
  * VoltDB is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * VoltDB is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.voltdb.utils;
 
 /**
  * Class representing a pair of generic-ized types. Supports equality, hashing
  * and all that other nice Java stuff. Based on STL's pair class in C++.
  *
  */
 public class Pair<T, U> implements Comparable<Pair<T, U>> {
 
     private final T m_first;
     private final U m_second;
     private transient final int m_hash;
 
     public Pair(T first, U second, final boolean hash) {
         m_first = first;
         m_second = second;
         if (hash) {
             m_hash = (first == null ? 0 : first.hashCode() * 31) +
                      (second == null ? 0 : second.hashCode());
         } else {
             m_hash = 0;
         }
     }
 
     public Pair(T first, U second) {
         this(first, second, true);
     }
 
     public String toString() {
         return "<" + m_first.toString() + ", " + m_second.toString() + ">";
     }
 
     public int hashCode() {
         return m_hash;
     }
     
     @Override
     public int compareTo(Pair<T, U> other) {
         return (other.m_hash - this.m_hash);
     }
     
     public Object get(int idx) {
         if (idx == 0) return m_first;
         else if (idx == 1) return m_second;
         return null;
     }
 
     /**
      * @param o Object to compare to.
      * @return Is the object equal to a value in the pair.
      */
     public boolean contains(Object o) {
         if ((m_first != null) && (m_first.equals(o))) return true;
         if ((m_second != null) && (m_second.equals(o))) return true;
         if (o != null) return false;
         return ((m_first == null) || (m_second == null));
     }
 
     public boolean equals(Object o) {
         if (this == o) {
             return true;
         }
         if (o == null || !(getClass().isInstance(o))) {
             return false;
         }
 
         @SuppressWarnings("unchecked")
         Pair<T, U> other = (Pair<T, U>) o;
 
         return (m_first == null ? other.m_first == null : m_first.equals(other.m_first))
                 && (m_second == null ? other.m_second == null : m_second.equals(other.m_second));
     }
 
     /**
      * @return the first
      */
    @SuppressWarnings("unchecked")
    public <X extends T> X getFirst() {
        return (X)m_first;
     }
 
     /**
      * @return the second
      */
    @SuppressWarnings("unchecked")
    public <X extends U> X getSecond() {
        return (X)m_second;
     }
 
     /**
      * Convenience class method for constructing pairs using Java's generic type
      * inference.
      */
     public static <T, U> Pair<T, U> of(T x, U y) {
         return new Pair<T, U>(x, y);
     }
 }
