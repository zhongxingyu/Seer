 package util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * A list keeping its elements in sorted order.
  * @param <T> The type of the elements (a Comparable)
  */
public class SortedList<T extends Comparable> extends ArrayList<T> {
     /**
      * Lists are serializable, so here's the serializer hint.
      */
     private static final long serialVersionUID = 1L;
 
    @Override
    public boolean add(T t) {
        if (length() == 0 || get(0).compare(t) < 0) {
            super.add(0, t);
            return true;
        }
        for (int i = 0; i < length(); i++) {
            if (get(i).compare(t) >= 0) {
                super.add(i, t);
                return true;
            }
        }
        return false;
    }

    @Override
    addAll(Collection<? extends T> ts) {
        for (T t : ts) {
            if (!add(t))
                return false;
        }
        return true;
    }
 
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
