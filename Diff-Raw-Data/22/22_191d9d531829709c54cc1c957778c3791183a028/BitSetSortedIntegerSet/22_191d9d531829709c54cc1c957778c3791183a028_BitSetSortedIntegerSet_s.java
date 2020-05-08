 package com.mattdrees.dropboxcalories;
 
 import java.util.AbstractSet;
 import java.util.BitSet;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import java.util.SortedSet;
 
 import com.google.common.primitives.Ints;
 
 public class BitSetSortedIntegerSet extends AbstractSet<Integer> implements SortedSet<Integer>
 {
 
     public class BitSetIterator implements Iterator<Integer>
     {
         int index = 0;
 
         int found = 0;
         
         @Override
         public boolean hasNext()
         {
             return found < size;
         }
 
         @Override
         public Integer next()
         {
             if (!hasNext())
                 throw new NoSuchElementException();
             
             int checkIndex = index; 
             while (!bitSet.get(checkIndex))
             {
                 checkIndex++;
             }
             int next = checkIndex;
             index = next + 1;
             found++;
             return next;
         }
 
         @Override
         public void remove()
         {
             if (found == 0)
                 throw new IllegalStateException("next() has not yet been called");
             
             if (bitSet.get(index - 1))
             {
                 bitSet.clear(index - 1);
             }
             else
             {
                 throw new IllegalStateException("remove() was already called for this location");
             }
             
         }
 
     }
 
     final BitSet bitSet;
     
     private int size;
 
     private final int max;
     
     public BitSetSortedIntegerSet(int max)
     {
         this.max = max;
         this.bitSet = new BitSet(max);
     }
 
     @Override
     public Comparator<? super Integer> comparator()
     {
         return new Comparator<Integer>() {
 
             @Override
             public int compare(Integer o1, Integer o2)
             {
                 return o1.compareTo(o2);
             }
             
         };
         
     }
 
     @Override
     public SortedSet<Integer> subSet(Integer fromElement, Integer toElement)
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public SortedSet<Integer> headSet(Integer toElement)
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public SortedSet<Integer> tailSet(Integer fromElement)
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public Integer first()
     {
         return iterator().next();
     }
 
     @Override
     public Integer last()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public Iterator<Integer> iterator()
     {
         return new BitSetIterator();
     }
 
     @Override
     public int size()
     {
         return size;
     }
     
     @Override
     public boolean add(Integer newInt)
     {
         if (newInt > max)
             throw new IllegalArgumentException("can't add " + newInt + "; max is " + max);
         boolean changed = !bitSet.get(newInt);
        bitSet.set(newInt);
        size++;
         return changed;
     }
 
     @Override
     public boolean remove(Object o)
     {
         if (o instanceof Integer)
         {
             Integer intToRemove = (Integer) o;
             boolean result = bitSet.get(intToRemove);
             if (result)
             {
                 bitSet.clear(intToRemove);
                 size--;
             }
             return result;
         }
         else
         {
             return false;
         }
     }
 }
