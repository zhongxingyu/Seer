 package de.fhro.inf.p3.uebung01;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: felix
  * Date: 10/4/13
  * Time: 10:53 AM
  * Übung 01
  */
 public class RoList {
 
     private static final int ARRAY_LENGTH = 100;
 
     private Object[] elements;
     private int size;
 
 
     public RoList() {
         elements = new Object[ARRAY_LENGTH];
         size = 0;
     }
 
     public RoList(RoList xs) {
         elements = new Object[xs.elements.length];
         System.arraycopy(xs.elements, 0, elements, 0, xs.size);
         size = xs.size;
     }
 
 
     /**
      * removes all elements from this
      */
     public void clear() {
         elements = new Object[ARRAY_LENGTH];
         size = 0;
     }
 
     /**
      * adds x to this
      *
      * @param x element
      */
     public void add(Object x) {
         extend();
         elements[size++] = x;
     }
 
     /**
      * adds all elements in xs to this
      *
      * @param xs collection of elements
      */
     public void addAll(Collection xs) {
         for (Object x : xs) {
             add(x);
         }
     }
 
     /**
      * removes element at position i
      *
      * @param i position
      */
     public void remove(int i) {
         if (i < 0 || i >= size)
             throw new IndexOutOfBoundsException();
 
         System.arraycopy(elements, i + 1, elements, i, size - i - 1);
         elements[--size] = null;
         shrink();
     }
 
     /**
      * removes element x from this
      *
      * @param x element
      * @return true when element x exists
      */
     public boolean remove(Object x) {
         int i = indexOf(x);
         if (i < 0)
             return false;
         else {
             remove(i);
             return true;
         }
     }
 
     /**
      * removes all elements in xs from this
      *
      * @param xs collection of elements
      */
     public void removeAll(Collection xs) {
         for (Object x : xs) {
             remove(x);
         }
     }
 
     /**
      * indexOf method
      *
      * @param x element
      * @return index or -1 when not found
      */
     public int indexOf(Object x) {
         for (int i = 0; i < size; i++) {
             if (x.equals(elements[i])) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * contains method
      *
      * @param x element
      * @return true or false
      */
     public boolean contains(Object x) {
         return indexOf(x) != -1;
     }
 
     /**
      * returns element at position i
      *
      * @param i index
      * @return element @ [i]
      */
     public Object get(int i) {
         if (i >= size)
             throw new IndexOutOfBoundsException();
 
         return elements[i];
     }
 
     /**
      * replaces element y at position i with x
      *
      * @param i position
      * @param x element
      * @return element y
      */
     public Object set(int i, Object x) {
         if (i >= size)
             throw new IndexOutOfBoundsException();
 
         Object e = elements[i];
         elements[i] = x;
         return e;
     }
 
     /**
      * size()
      *
      * @return size
      */
     public int size() {
         return size;
     }
 
 
     /**
      * extends array
      */
     private void extend() {
         if (size >= elements.length) {
             Object[] tmp = new Object[elements.length + ARRAY_LENGTH];
             System.arraycopy(elements, 0, tmp, 0, size);
             elements = tmp;
         }
     }
 
     /**
      * shrink array
      */
     private void shrink() {
         if (size + ARRAY_LENGTH < elements.length) {
             Object[] tmp = new Object[elements.length - ARRAY_LENGTH];
             System.arraycopy(elements, 0, tmp, 0, tmp.length);
             elements = tmp;
         }
     }
 
 
     /**
      * iterator for RoList
      *
      * @return iterator
      */
     public Iterator iterator() {
         return new RoIterator(this);
     }
 
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         RoList roList = (RoList) o;
 
         return size == roList.size && Arrays.equals(elements, roList.elements);
     }
 
     @Override
     public int hashCode() {
         int result = Arrays.hashCode(elements);
         result = 31 * result + size;
         return result;
     }
 
     @Override
     public RoList clone() {
         return new RoList(this);
     }
 
 
     private class RoIterator implements Iterator {
         private RoList list;
         private int pos = 0;
 
         public RoIterator(RoList list) {
             this.list = list;
         }
 
         @Override
         public boolean hasNext() {
             return pos < list.size();
         }
 
         @Override
         public Object next() throws NoSuchElementException {
             if (hasNext())
                 return list.get(pos++);
             else
                 throw new NoSuchElementException();
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException();
         }
     }
 }
