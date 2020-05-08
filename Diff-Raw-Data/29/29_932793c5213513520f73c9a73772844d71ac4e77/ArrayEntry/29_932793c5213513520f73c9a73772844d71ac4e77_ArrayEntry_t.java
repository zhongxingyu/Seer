 package org.meeuw.json;
 
 /**
 * @author Michiel Meeuwissen
 * @since ...
 */
 public class ArrayEntry implements PathEntry {
    final int index;
 
     public ArrayEntry() {
        index = 0;
    }
    private ArrayEntry(int index) {
        this.index = index;
     }
 
    protected ArrayEntry inc() {
        return new ArrayEntry(index + 1);
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
