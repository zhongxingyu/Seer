 package org.isma.utils.collections;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Predicate;
 
 import java.util.ArrayList;
 
 public class CircleList<E> extends ArrayList<E> {
     @Override
     public boolean add(E e) {
         if (!contains(e)) {
             return super.add(e);
         }
         return false;
     }
 
     public E next(E element) {
         int indexOf = indexOf(element);
         if (indexOf == -1){
             return null;
         }
         if (indexOf + 1 == size()) {
             return get(0);
         }
         return get(indexOf + 1);
     }
 
     public E next(E element, Predicate predicate) {
         boolean respectPredicate = predicate.evaluate(element);
         int matches = CollectionUtils.countMatches(this, predicate);
         if (respectPredicate && matches == 1) {
             return null;
         }
         E nextElement = next(element);
         while (!predicate.evaluate(nextElement)) {
             nextElement = next(nextElement);
         }
         return nextElement;
     }
 }
