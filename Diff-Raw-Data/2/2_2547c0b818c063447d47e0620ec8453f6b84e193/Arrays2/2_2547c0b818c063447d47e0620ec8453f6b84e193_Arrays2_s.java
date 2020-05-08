 package org.metaborg.util.arrays;
 
 import java.util.Collection;
 
 public final class Arrays2 {
     public static <T> boolean addAll(Collection<T> addTo, T[] elementsToAdd) {
         boolean modified = false;
         for(T e : elementsToAdd) {
            modified = modified || addTo.add(e);
         }
         return modified;
     }
 }
