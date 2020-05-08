 /*
     Copyright (c) 2012 Ray A. Conner
 
     Permission is hereby granted, free of charge, to any person obtaining a copy
     of this software and associated documentation files (the "Software"), to deal
     in the Software without restriction, including without limitation the rights
     to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
     copies of the Software, and to permit persons to whom the Software is
     furnished to do so, subject to the following conditions:
 
     The above copyright notice and this permission notice shall be included in all
     copies or substantial portions of the Software.
 
     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
     IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
     LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
     OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
     SOFTWARE.
  */
 
 package com.github.rconner.anansi;
 
 /**
  * Helper methods for Path objects.
  *
  * @author rconner
  */
 public class Paths {
 
     /**
      * Prevent instantiation.
      */
     private Paths() {
     }
 
     /**
     *
      * @param from
      * @param to
      * @param <V>
      * @param <E>
      * @return
      */
     public static <V, E> Path<V, E> create(V from, V to) {
         return new FullPath<V, E>(from, to, null);
     }
 
     /**
     *
      * @param from
      * @param to
      * @param over
      * @param <V>
      * @param <E>
      * @return
      */
     public static <V, E> Path<V, E> create(V from, V to, E over) {
         return new FullPath<V, E>(from, to, over);
     }
 
     private static final class FullPath<V, E> implements Path<V, E> {
         private final V from;
         private final V to;
         private final E over;
 
         private FullPath(V from, V to, E over) {
             this.from = from;
             this.to = to;
             this.over = over;
         }
 
         @Override
         public V from() {
             return from;
         }
 
         @Override
         public V to() {
             return to;
         }
 
         @Override
         public E over() {
             return over;
         }
     }
 
 }
