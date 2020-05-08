 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.pitt.isp.sverchkov.math;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * A class to wrap an iterable the elements of which implement doubleValue
  * @author YUS24
  */
 public class DoubleIterable implements Iterable<Double> {
     
     private final Iterable iterable;
     
     public DoubleIterable( Iterable iterable ){
         this.iterable = iterable;
     }
 
     @Override
     public Iterator<Double> iterator() {
         return iterable.iterator();
     }
     
     private static class DoubleIterator<T> implements Iterator<Double> {
         
         private final Iterator<T> iterator;
         
         private DoubleIterator( Iterator<T> iterator ){
             this.iterator = iterator;
         }
 
         @Override
         public boolean hasNext() {
             return iterator.hasNext();
         }
 
         @Override
         public Double next() {
             T next = iterator.next();
             try {
                return (Double) next.getClass().getMethod( "doubleValue" ).invoke(next );
             } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                 throw new IllegalArgumentException(ex);
             }
         }
 
         @Override
         public void remove() {
             iterator.remove();
         }
         
     }
 }
