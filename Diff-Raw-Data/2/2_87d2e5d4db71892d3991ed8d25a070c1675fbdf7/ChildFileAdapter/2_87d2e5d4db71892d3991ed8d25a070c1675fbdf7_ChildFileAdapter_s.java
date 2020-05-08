 package com.bluespot.logic.adapters;
 
 import java.io.File;
 
 /**
  * An adapter that returns a child of the given file.
  * 
  * @author Aaron Faanes
  */
 public final class ChildFileAdapter implements Adapter<File, File> {
 
     /**
      * Constructs a filename adapter for the specified child name.
      * 
      * @param childName
      *            the name of the child file
      * @throws NullPointerException
      *             if {@code childName} is null
      */
     public ChildFileAdapter(final String childName) {
         if (childName == null) {
             throw new NullPointerException("childName is null");
         }
         this.childName = childName;
     }
 
     private final String childName;
 
     @Override
     public File adapt(final File source) {
         if (source == null) {
             return null;
         }
         return new File(source, this.getChildName());
     }
 
     /**
      * Returns the name of the child file.
      * 
      * @return the name of the child file
      */
     public String getChildName() {
         return this.childName;
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (obj == this) {
             return true;
         }
         if (!(obj instanceof ChildFileAdapter)) {
             return false;
         }
         final ChildFileAdapter adapter = (ChildFileAdapter) obj;
         return this.getChildName().equals(adapter.getChildName());
     }
 
     @Override
     public int hashCode() {
         int result = 17;
         result = 31 * result + this.getChildName().hashCode();
         return result;
     }
 
     @Override
     public String toString() {
        return String.format("has child with name \"%s\"", this.getChildName());
     }
 }
