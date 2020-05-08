 /*
  *  Copyright (C) 2010 Markus Echterhoff <tam@edu.uni-klu.ac.at>
  *
  *  This file is part of EvoPaint.
  *
  *  EvoPaint is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with EvoPaint.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package evopaint.util.mapping;
 
 import evopaint.interfaces.IRandomNumberGenerator;
 import evopaint.util.logging.Logger;
 import java.util.AbstractCollection;
 import java.util.Iterator;
 
 /**
  *
  * @author Markus Echterhoff <tam@edu.uni-klu.ac.at>
  */
 public class ParallaxMap<T> extends AbstractCollection<T> {
     private class ParallaxMapIterator<T> implements Iterator {
 
         T [] data;
         int i;
 
         public boolean hasNext() {
             if (i == data.length) {
                     return false;
             }
             while (data[i] == null) {
                 i++;
                 if (i == data.length) {
                     return false;
                 }
             }
             return true;
         }
 
         public T next() {
             return data[i++];
         }
 
         public void remove() {
             data[i] = null;
         }
 
         public ParallaxMapIterator(T [] array) {
             this.i = 0;
             this.data = array;
         }
     }
 
     private T [] data;
     protected int width;
     protected int height;
     private int numElements;
 
     @Override
     public int size() {
         return data == null ? 0 : data.length;
     }
 
     @Override
     public Iterator<T> iterator() {
         return new ParallaxMapIterator(data);
     }
 
     public int getWidth() {
         return width;
     }
 
     public int getHeight() {
         return height;
     }
 
     public void setData(Object [] data) {
         this.data = (T[])data;
     }
 
     public T [] getData() {
         return data;
     }
 
     public T getNotSynchronizedUnclamped(int i) {
         return data[i];
     }
 
     public synchronized T getUnclamped(int i) {
         return data[i];
     }
 
     public synchronized T get(int i) {
         return data[wrap(i, data.length)];
     }
 
     public synchronized T get(int x, int y) {
         return data[wrap(y, height) * width + wrap(x, width)];
     }
 
     public synchronized T get(AbsoluteCoordinate ac) {
         return data[wrap(ac.y, height) * width + wrap(ac.x, width)];
     }
 
     public synchronized T get(AbsoluteCoordinate ac, RelativeCoordinate rc) {
         return data[wrap(ac.y + rc.y, height) * width + wrap(ac.x + rc.x, width)];
     }
 
     /* *
      * gets absolute index of a random free spot in the neighborhood of ac
      * the returned index will be wrapped to lie within the bounds of this
      * parallax map.
      *
      * @return a new AbsoluteCoordinate, pointing to the free spot
      */
     public synchronized AbsoluteCoordinate getRandomFreeNeighborCoordinateOf(AbsoluteCoordinate ac, IRandomNumberGenerator rng) {
 
         // get number of free spots in neighborhood
         int numFree = 0;
         for (int y = ac.y - 1; y <= ac.y + 1; y++) {
             for (int x = ac.x - 1; x <= ac.x + 1; x++) {
                 if (get(x, y) == null) {
                     numFree++;
                 }
             }
         }
 
         // lazy way out if we have no free spots
         if (numFree == 0) {
             return null;
         }
 
         // gather indices of free spots
         int [] indices = new int[numFree];
         int i = 0;
         for (int y = ac.y - 1; y <= ac.y + 1; y++) {
             for (int x = ac.x - 1; x <= ac.x + 1; x++) {
                 if (get(x, y) == null) {
                     indices[i] = wrap(y, height) * width + wrap(x, width);
                     i++;
                     if (i == numFree) {
                         int chosenIndex = indices[rng.nextPositiveInt(indices.length)];
                         return new AbsoluteCoordinate(chosenIndex % width, chosenIndex / width, this);
                     }
                 }
             }
         }
         
         assert (false);
         return null;
     }
 
     public synchronized int [] getShuffledIndices(IRandomNumberGenerator rng) {
         int [] indices = new int[numElements];
 
         for (int i = 0, ii = 0; ii < numElements && i < data.length; i++) {
             if (data[i] != null) {
                 indices[ii++] = i;
             }
         }
         
         // Durstenfeld's algorithm for permutating
         // http://en.wikipedia.org/wiki/Fisher-Yates_shuffle
         // n is the number of items remaining to be shuffled.
         for (int n = indices.length; n > 1; n--) {
             // Pick a random element to swap with the nth element.
             int k = rng.nextPositiveInt(n);  // 0 <= k <= n-1 (0-based array)
             // Swap array elements.
             int tmp = indices[k];
             indices[k] = indices[n-1];
             indices[n-1] = tmp;
         }
 
         return indices;
     }
  
     public T getRandom(IRandomNumberGenerator rng) {
         int rnd = rng.nextPositiveInt(data.length);
         int i = rnd;
         while (data[i] == null) {
             i++;
             if (i == data.length) {
                 i = 0;
             }
             if (i == rnd) {
                 Logger.log.warning("calling getRandom() on empty ParallaxMap", (Object[])null);
             }
         }
         return data[i];
     }
 
     public synchronized void set(AbsoluteCoordinate ac, T object) {
         int i = wrap(ac.y, height) * width + wrap(ac.x, width);
         set(i, object);
     }
 
     public synchronized void set(int i, T object) {
         if (data[i] == null) {
             if (object != null) {
                 numElements++;
             }
         } else if (object == null) {
                 numElements--;
         }
         data[i] = object;
     }
 
     protected synchronized void set(int x, int y, T object) {
         int i = wrap(y, height) * width + wrap(x, width);
         set(i, object);
     }
 
     public synchronized void remove(AbsoluteCoordinate ac) {
         int i = wrap(ac.y, height) * width + wrap(ac.x, width);
         set(i, null);
     }
 
     public synchronized void remove(int x, int y) {
         int i = wrap(y, height) * width + wrap(x, width);
         set(i, null);
     }
 
     @Override
     public boolean remove(Object o) {
         return false;
     }
 
     public static int wrap(int index, int length) {
         while (index < 0) {
             index += length;
         }
         while (index >= length) {
             index -= length;
         }
         return index;
     }
 
     public synchronized void reset() {
         for (int i = 0; i < data.length; i++) {
             data[i] = null;
         }
         numElements = 0;
     }
 
    public synchronized void recount() {
        numElements = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] != null) {
                numElements++;
            }
        }
    }

     public ParallaxMap(T[] array, int width, int height) {
         assert (array != null);
         assert (width > 0);
         assert (height > 0);
         this.data = array;
         this.width = width;
         this.height = height;
     }
 }
 
