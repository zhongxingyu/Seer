 package computational_geometry.model.data_structures;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 /**
  * Elementary implementation of a circular list
  * It is a list but some methods are not implemented
  * @author eloi
  *
  * @param <T> : The type of the nodes
  */
 public class CircularList<T extends Comparable<T>> implements List<T>,
         Iterable<T> {
 
     private LinkNode<T> first;
     private int size;
 
     public CircularList() {
         this.first = null;
         this.size = 0;
     }
 
     public LinkNode<T> getNode(int i) {
         if (i < 0 || i >= size())
             return null;
 
         LinkNode<T> elem = first; // shouldn't be null cuz we tested w/ size
         for (int j = 0; j++ < i; elem = elem.getNext())
             ;
 
         return elem;
     }
 
     public T elementAt(int i) {
         LinkNode<T> elem = getNode(i);
         return elem != null ? elem.getValue() : null;
     }
 
     @Override
     public boolean add(T obj) {
         addLast(obj); // like a Vector
         return true;
     }
 
     public void addFirst(T obj) {
         add(obj, true);
     }
 
     public void addLast(T obj) {
         add(obj, false);
     }
 
     private void add(T obj, boolean atFirst) {
         LinkNode<T> newElem = new LinkNode<T>(obj);
         if (first == null) {
             first = newElem;
             first.setNext(first);
             first.setPrev(first);
         } else {
             newElem = first.insertBefore(newElem);
             if (atFirst) {
                 first = newElem;
             }
         }
         ++size;
     }
 
     //    public int findPos(T obj) {
     //        if (first == null) return -1;
     //
     //        int i = 0;
     //        LinkNode<T> elem = new LinkNode<T>(first);
     //        for ( ; ! elem.getNext().equals(first) &&
     //                ! elem.getValue().equals(obj); elem = elem.getNext())
     //            ++i;
     //
     //        return elem.getValue().equals(obj) ? i : -1;
     //    }
 
     @Override
     public int size() {
         return size;
     }
 
     public T removeElementAt(int i) {
         LinkNode<T> elem = getNode(i);
         if (elem == null)
             return null;
         --size;
         if (first.equals(elem)) {
             first = elem.getNext();
         }
        if (size == 0) {
            first = null;
        }
         return elem.remove().getValue();
     }
 
     public T firstElement() {
         return first != null ? first.getValue() : null;
     }
 
     public T lastElement() {
         return first != null ? first.getPrev().getValue() : null;
     }
 
     public String forward() {
         StringBuilder sb = new StringBuilder();
 
         for (LinkNode<T> elem = first;;) {
             sb.append(elem.getValue().toString());
             elem = elem.getNext();
             if (elem.equals(first))
                 break;
         }
         return sb.toString();
     }
 
     public String backward() {
         StringBuilder sb = new StringBuilder();
 
         for (LinkNode<T> elem = first;;) {
             sb.append(elem.getValue().toString());
             elem = elem.getPrev();
             if (elem.equals(first))
                 break;
         }
         return sb.toString();
     }
 
     @Override
     public boolean addAll(Collection<? extends T> collection) {
         for (T obj : collection) {
             addLast(obj);
         }
         return true;
     }
 
     @Override
     public void clear() {
         first = null;
         size = 0;
     }
 
     @Override
     public boolean contains(Object obj) {
         for (T t : this) {
             if (t.equals(obj))
                 return true;
         }
         return false;
     }
 
     @Override
     public boolean containsAll(Collection<?> arg0) {
         System.err.println("containsAll() not implemented");
         return false;
     }
 
     @Override
     public boolean isEmpty() {
         return !(size() > 0);
     }
 
     @Override
     public boolean remove(Object obj) {
         int i = 0;
         for (Object o : this) {
             if (obj.equals(o)) {
                 removeElementAt(i);
                 --size;
                 return true;
             }
             ++i;
         }
         return false;
     }
 
     @Override
     public boolean removeAll(Collection<?> collection) {
         System.err.println("removeAll() not implemented");
         return false;
     }
 
     @Override
     public boolean retainAll(Collection<?> collection) {
         System.err.println("retainAll() not implemented");
         return false;
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public Object[] toArray() {
         Object[] array = new Object[size()];
         int i = 0;
         for (Object o : this) {
             array[i++] = (T) o;
         }
         return array;
     }
 
     @Override
     @SuppressWarnings("hiding")
     public <T> T[] toArray(T[] obj) {
         System.err.println("toArray2() not implemented");
         return null;
     }
 
     @Override
     public Iterator<T> iterator() {
         return new CircularListIterator(this);
     }
 
     class CircularListIterator implements ListIterator<T> {
 
         LinkNode<T> next;
         LinkNode<T> previous;
         LinkNode<T> first;
         boolean hasNext;
         boolean hasPrevious;
 
         LinkNode<T> lastElementReturned;
 
         public CircularListIterator(CircularList<T> list) {
             this.next = list.first;
             this.previous = list.first;
             this.first = list.first;
             this.hasNext = list.first != null;
         }
 
         @Override
         public boolean hasNext() {
             return hasNext;
         }
 
         @Override
         public boolean hasPrevious() {
             return hasPrevious;
         }
 
         @Override
         public T next() {
             T value = next.getValue();
             lastElementReturned = next;
             next = next.getNext();
             if (next.equals(first))
                 hasNext = false;
 
             return value;
         }
 
         @Override
         public T previous() {
             T value = previous.getValue();
             lastElementReturned = previous;
             previous = previous.getPrev();
             if (previous.equals(first))
                 hasPrevious = false;
 
             return value;
         }
 
         @Override
         public void remove() { // unimplemented
             System.err.println("ListIterator.remove() not implemented");
         }
 
         @Override
         public void add(T arg0) {
             System.err.println("ListIterator.add() not implemented");
             // TODO Auto-generated method stub
         }
 
         @Override
         public int nextIndex() {
             System.err.println("nexListIterator.nextIndex() not implemented");
             // TODO Auto-generated method stub
             return 0;
         }
 
         @Override
         public int previousIndex() {
             System.err
                     .println("previouListIterator.previousIndex() not implemented");
             // TODO Auto-generated method stub
             return 0;
         }
 
         @Override
         public void set(T obj) {
             if (lastElementReturned != null) {
                 lastElementReturned.setValue(obj);
             }
         }
     }
 
     public CircularList<LinkNode<T>> getNodes() {
         CircularList<LinkNode<T>> list = new CircularList<LinkNode<T>>();
         for (int i = 0; i < size(); ++i) {
             list.add(getNode(i));
         }
         return list;
     }
 
     @Override
     public void add(int arg0, T arg1) {
         System.err.println("add() not implemented");
         // TODO Auto-generated method stub
     }
 
     @Override
     public boolean addAll(int arg0, Collection<? extends T> arg1) {
         System.err.println("addAll() not implemented");
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public T get(int arg0) {
         return elementAt(arg0);
     }
 
     @Override
     public int indexOf(Object arg0) {
         System.err.println("indexOf() not implemented");
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public int lastIndexOf(Object arg0) {
         System.err.println("lastIndexOf() not implemented");
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public ListIterator<T> listIterator() {
         return new CircularListIterator(this);
     }
 
     @Override
     public ListIterator<T> listIterator(int arg0) {
         return new CircularListIterator(this);
     }
 
     @Override
     public T remove(int arg0) {
         System.err.println("remove() not implemented");
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public T set(int arg0, T arg1) {
         LinkNode<T> node = getNode(arg0);
         T element = node.getValue();
         node.setValue(arg1);
         return element;
     }
 
     @Override
     public List<T> subList(int fromIndex, int toIndex) {
         if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
             throw new IndexOutOfBoundsException("fromIndex=" + fromIndex + ", toIndex=" + toIndex + ", size=" + size());
         }
         CircularList<T> newList = new CircularList<T>();
         LinkNode<T> node = getNode(toIndex-1);
         for (int i = 0; i < (toIndex - fromIndex); ++i) {
             newList.addFirst(node.getValue());
             node = node.getPrev();
         }
         return newList;
     }
 
 }
