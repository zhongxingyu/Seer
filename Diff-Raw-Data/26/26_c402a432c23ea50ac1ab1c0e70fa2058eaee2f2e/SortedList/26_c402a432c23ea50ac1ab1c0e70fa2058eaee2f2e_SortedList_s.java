 package util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * A list keeping its elements in sorted order.
  * @param <T> The type of the elements (a Comparable)
  */
public class SortedList<T> {
     /**
      * Lists are serializable, so here's the serializer hint.
      */
     private static final long serialVersionUID = 1L;
 
 
     /**
      * This operation is not supported and will always throw an exception.
      * @param i ignored
      * @param ts ignored
      * @return nothing
      */
     @Override
     public boolean addAll(final int i,
                           final Collection<? extends T> ts) {
         throw new UnsupportedOperationException();
     }
 
     /**
      * This operation is not supported and will always throw an exception.
      * @param i ignored
      * @param t ignored
      * @return nothing
      */
     @Override
     public T set(final int i, final T t) {
         throw new UnsupportedOperationException();
     }
 
     /**
      * This operation is not supported and will always throw an exception.
      * @param i ignored
      * @param t ignored
      */
     @Override
     public void add(final int i, final T t) {
         throw new UnsupportedOperationException();
     }
 }
