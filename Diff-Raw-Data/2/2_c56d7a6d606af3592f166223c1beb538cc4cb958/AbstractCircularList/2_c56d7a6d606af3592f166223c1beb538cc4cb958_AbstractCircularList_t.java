 package circularlist;
 
 import list.List;
 
 import java.util.Iterator;
 
 /**
  * Created by Travis on 10/7/13.
  */
 public abstract class AbstractCircularList<E> implements CircularList<E> {
 
     protected List<E> list;
 
     @Override
     public boolean isEmpty() {
         return list.isEmpty();
     }
 
     @Override
     public int size() {
         return list.size();
     }
 
     @Override
     public void clear() {
         list.clear();
 
     }
 
     @Override
     public boolean add(E item) {
         return list.add(item);
     }
 
     @Override
     public void add(int index, E item) throws IndexOutOfBoundsException {
         if (index < 0) {
             throw new IndexOutOfBoundsException();
         }
         if (index == size()) {
             add(item);
             return;
         }
 
         list.add(newIndex(index), item);
 
     }
 
     @Override
     public E remove(int index) throws IndexOutOfBoundsException {
         if (index < 0 || size() <= 0) {
             throw new IndexOutOfBoundsException();
         }
 
         return list.remove(newIndex(index));
     }
 
     @Override
     public E get(int index) throws IndexOutOfBoundsException {
        if (index < 0 || size() <= 0) {
             throw new IndexOutOfBoundsException();
         }
         return list.get(newIndex(index));
 
     }
 
     @Override
     public Iterator<E> iterator() {
         return null;
     }
 
     private int newIndex(int index) {
         if (size() <= 0) {
             return 0;
         }
         return index % size();
     }
 }
