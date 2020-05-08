 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.utils;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * A list that notifies its observers whenever an item is added/removed.
  * 
  * @param T The type of elements in the list.
  * 
  * @author Martin Pecka
  */
 public class ObservableList<T> extends ArrayList<T>
 {
     /** */
     private static final long   serialVersionUID = 3207567353639585374L;
 
     /**
      * Observers of the change notifications.
      */
     protected List<Observer<T>> observers        = new LinkedList<Observer<T>>();
 
     /**
      * Constructs an empty list with an initial capacity of ten.
      */
     public ObservableList()
     {
         super();
     }
 
     /**
      * Constructs a list containing the elements of the specified collection, in the order they are returned by the
      * collection's iterator.
      * 
      * @param c the collection whose elements are to be placed into this list.
      * @throws NullPointerException if the specified collection is null.
      */
     public ObservableList(Collection<? extends T> c)
     {
         super();
         if (c.size() == 0)
             return;
 
         Iterator<? extends T> it = c.iterator();
         for (T item = it.next(); it.hasNext(); item = it.next()) {
             add(item);
         }
     }
 
     /**
      * Constructs an empty list with the specified initial capacity.
      * 
      * @param initialCapacity the initial capacity of the list
      * 
      * @throws IllegalArgumentException if the specified initial capacity is negative
      */
     public ObservableList(int initialCapacity)
     {
         super(initialCapacity);
     }
 
     /**
      * Add a new observer.
      * 
      * @param observer The observer to add.
      */
     public void addObserver(Observer<T> observer)
     {
         observers.add(observer);
     }
 
     /**
      * Remove the specified observer.
      * 
      * @param observer The observer to remove.
      */
     public void removeObserver(Observer<T> observer)
     {
         observers.remove(observer);
     }
 
     /**
      * Notify all observers about the given change.
      * 
      * @param change The change that has happened.
      */
     protected void fireChangeNotification(ChangeNotification<T> change)
     {
         for (Observer<T> l : observers)
             l.changePerformed(change);
     }
 
     /**
      * Notify all observers about the given change.
      * 
      * @param item The item that has changed.
      * @param type The type of the change.
      */
     protected void fireChangeNotification(T item, ChangeTypes type)
     {
         fireChangeNotification(new ChangeNotification<T>(item, type));
     }
 
     /**
      * Notify all observers about the given change.
      * 
      * @param item The item that has changed.
      * @param oldItem The previous item (if type is CHANGE).
      * @param type The type of the change.
      */
     protected void fireChangeNotification(T item, T oldItem, ChangeTypes type)
     {
         fireChangeNotification(new ChangeNotification<T>(item, oldItem, type));
     }
 
     @Override
     public void add(int index, T element)
     {
         super.add(index, element);
         fireChangeNotification(element, ChangeTypes.ADD);
     }
 
     @Override
     public boolean add(T e)
     {
         super.add(e);
         fireChangeNotification(e, ChangeTypes.ADD);
         return true;
     }
 
     @Override
     public boolean addAll(Collection<? extends T> c)
     {
         if (c.size() == 0)
             return false;
 
         if (c.size() == 1) {
             add(c.iterator().next());
         } else {
             Iterator<? extends T> it = c.iterator();
            while (it.hasNext()) {
                T item = it.next();
                 add(item);
            }
         }
 
         return true;
     }
 
     @Override
     public boolean addAll(int index, Collection<? extends T> c)
     {
         if (c.size() == 0)
             return false;
 
         int i = index;
 
         if (c.size() == 1) {
             add(i, c.iterator().next());
         } else {
             Iterator<? extends T> it = c.iterator();
            while (it.hasNext()) {
                T item = it.next();
                 add(i++, item);
            }
         }
 
         return true;
     }
 
     @Override
     public void clear()
     {
         for (int i = size() - 1; i >= 0; i--) {
             remove(i);
         }
     }
 
     @Override
     public T remove(int index)
     {
         T i = null;
         if ((i = super.remove(index)) != null) {
             fireChangeNotification(i, ChangeTypes.REMOVE);
         }
         return null;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public boolean remove(Object o)
     {
         if (super.remove(o)) {
             fireChangeNotification((T) o, ChangeTypes.REMOVE);
             return true;
         }
         return false;
     }
 
     @Override
     protected void removeRange(int fromIndex, int toIndex)
     {
         List<T> removed = new LinkedList<T>();
         for (int i = fromIndex; i < toIndex; i++)
             removed.add(get(i));
 
         super.removeRange(fromIndex, toIndex);
 
         for (T item : removed)
             fireChangeNotification(item, ChangeTypes.REMOVE);
     }
 
     @Override
     public T set(int index, T element)
     {
         T item = super.set(index, element);
         fireChangeNotification(element, item, ChangeTypes.CHANGE);
         return item;
     }
 
     /**
      * Type of the change that can happen in the list
      * 
      * @author Martin Pecka
      */
     public enum ChangeTypes
     {
         /**
          * Item was removed
          */
         REMOVE,
         /**
          * Item was added
          */
         ADD,
         /**
          * Item was changed
          */
         CHANGE
     }
 
 }
