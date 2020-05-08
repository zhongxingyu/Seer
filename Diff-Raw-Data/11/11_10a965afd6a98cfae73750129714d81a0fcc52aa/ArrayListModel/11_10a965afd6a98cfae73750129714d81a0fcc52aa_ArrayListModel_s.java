 package nl.nikhef.jgridstart.gui.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import javax.swing.ListModel;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 
 /** An ArrayList that can be used as a ListModel.
  * 
  * inspired by http://www.java2s.com/Code/Java/Swing-JFC/ArrayListwithaListModelforeaseofuse.htm
  * 
  * TODO implement addAll()
  * TODO implement all constructor combinations
  * 
  * @author wvengen
  * @param <T> class of items in this list
  */
 public class ArrayListModel<T> extends ArrayList<T> implements ListModel {
     protected Object source;
     
     /** Constructs an empty list with an initial capacity */
     public ArrayListModel() {
 	super();
 	source = this;
     }
     /** Constructs an empty list with an initial capacity
      * 
      * @param source the source Object for events
      */
     public ArrayListModel(Object source) {
 	super();
 	this.source = source;
     }
     /** Constructs a list containing the elements of the specified collection, in the order
      * they are returned by the collection's iterator.
      * 
      * @param c the collection whose elements are to be placed into this list
      * @param source the source Object for events
      */
     public ArrayListModel(Collection<? extends T> c, Object source) {
 	super(c);
 	this.source = source;
     }
     /** Constructs an empty list with the specified initial capacity.
      *
      * @param initialCapacity the initial capaity of the list
      * @param source the source Object for events
      */
     public ArrayListModel(int initialCapacity, Object source) {
 	super(initialCapacity);
 	this.source = source;
     }
     
     /*
      * ListModel implementation 
      */
     protected ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();
     
     public T getElementAt(int index) {
 	return get(index);
     }
     public int getSize() {
 	return size();
     }
     public void addListDataListener(ListDataListener l) {
 	listeners.add(l);
     }
     public void removeListDataListener(ListDataListener l) {
 	listeners.remove(l);
     }
     
     protected void notifyAdded(int start, int end) {
 	ListDataEvent e = new ListDataEvent(source, ListDataEvent.INTERVAL_ADDED, start, end);
 	for (Iterator<ListDataListener> i = listeners.iterator(); i.hasNext(); ) {
 	    i.next().intervalAdded(e);
 	}
     }
     protected void notifyRemoved(int start, int end) {
 	ListDataEvent e = new ListDataEvent(source, ListDataEvent.INTERVAL_REMOVED, start, end);
 	for (Iterator<ListDataListener> i = listeners.iterator(); i.hasNext(); ) {
 	    i.next().intervalRemoved(e);
 	}
     }
     protected void notifyChanged(int index) {
 	ListDataEvent e = new ListDataEvent(source, ListDataEvent.CONTENTS_CHANGED, index, index);
 	for (Iterator<ListDataListener> i = listeners.iterator(); i.hasNext(); ) {
 	    i.next().contentsChanged(e);
 	}
     }
 
     /*
      * Overrides just to call listeners
      */
     @Override
     public boolean add(T o) {
 	boolean r = super.add(o);
 	if (r) notifyAdded(getSize()-1, getSize()-1);
 	return r;
     }
     @Override
     public void add(int index, T o) {
 	super.add(index, o);
 	notifyAdded(index, index);
     }
     @Override
     public T remove(int index) {
	T o = super.remove(index);
 	notifyRemoved(index, index);
 	return o;
     }
     @Override
     public boolean remove(Object o) {
 	int index = indexOf(o);
 	if (index<0) return false;
 	remove(index);
 	return true;
     }
     @Override
     public void removeRange(int from, int to) {
	super.removeRange(from, to);
 	notifyRemoved(from, to);
     }
     @Override
     public void clear() {
 	int size = getSize();
	super.clear();
 	notifyRemoved(0, size-1);
     }
     @Override
     public T set(int index, T el) {
 	T o = super.set(index, el);
 	notifyChanged(index);
 	return o;
     }
 }
