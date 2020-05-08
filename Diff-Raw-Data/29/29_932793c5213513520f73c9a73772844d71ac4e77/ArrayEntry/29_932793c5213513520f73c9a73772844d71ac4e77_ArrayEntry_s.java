 package org.meeuw.json;
 
 /**
 * @author Michiel Meeuwissen
 * @since ...
 */
 public class ArrayEntry implements PathEntry {
    Integer index = 0;
 
     public ArrayEntry() {
     }
 
    protected void inc() {
        index++;
     }
 
     @Override
     public String toString() {
         return "[" + index + "]";
     }
 
     @Override
     public void append(StringBuilder builder) {
         builder.append('[').append(index).append(']');
     }
 }
