 package com.bluespot.collections.table;
 
 import java.awt.Point;
 
 /**
  * Skeletal implementation of the {@link TableIterator} interface.
  * 
  * @author Aaron Faanes
  * @param <T>
  *            Type of element contained in this iterator's parent table
 * @see StrategyTableIterator
  */
 public abstract class AbstractTableIterator<T> implements TableIterator<T> {
 
     /**
      * The current position of this iterator
      */
     protected Point currentPoint;
 
     /**
      * The table used in iteration
      */
     protected final Table<T> table;
 
     /**
      * Constructs an iterator over the specified table.
      * 
      * @param table
      *            the table used for iteration
      */
     public AbstractTableIterator(final Table<T> table) {
         this.table = table;
     }
 
     public T get() {
         if (this.currentPoint == null) {
             this.next();
         }
         return this.table.get(this.currentPoint);
     }
 
     public Point getLocation() {
         final Point targetPoint = new Point();
         this.getLocation(targetPoint);
         return targetPoint;
     }
 
     public void getLocation(final Point targetPoint) {
         if (this.currentPoint == null) {
             this.next();
         }
         targetPoint.setLocation(this.currentPoint);
     }
 
     public T put(final T value) {
         if (this.currentPoint == null) {
             this.next();
         }
         return this.table.put(this.currentPoint, value);
     }
 
     @Override
     public void remove() {
         if (this.currentPoint == null) {
             this.next();
         }
         this.table.remove(this.currentPoint);
     }
 }
