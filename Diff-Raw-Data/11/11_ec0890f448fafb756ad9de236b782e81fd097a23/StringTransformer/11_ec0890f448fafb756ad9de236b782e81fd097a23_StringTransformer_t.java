 package com.github.sfleiter.cdi_interceptors.impl;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 public class StringTransformer {
     
     @SuppressWarnings("rawtypes")
     void transform(StringBuilder sb, Object o, int maximumCount) {
         Iterator iterator;
         int size = 0;
         if (o == null) {
             sb.append("null");
             return;
         } else if (o instanceof Collection) {
             Collection c = (Collection) o;
             size = c.size();
             iterator = c.iterator();
             // fall through
         } else if (o.getClass().isArray()) {
             Object[] array = (Object[]) o;
             size = array.length;
             iterator = new ArrayIterator(array);
             // fall through
         } else {
             // no array or collection
             sb.append(o.toString());
             return;
         }
         
         // array or collection
         sb.append("Collection[size=").append(size).append(", ");
         int i = 1;
         while (i < maximumCount && iterator.hasNext()) {
             Object elem = iterator.next();
             sb.append(toString(elem));
             if (iterator.hasNext()) {
                 sb.append(", ");
             }
         }
         sb.append("]");
     }
 
     private String toString(Object object) {
         return object != null ? object.toString() : "null";
     }
 
 }
