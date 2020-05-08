 package com.jclark.microxml.tree;
 
 import org.jetbrains.annotations.NotNull;
 
 /**
  * An attribute, representing a name-value pair.
  *
  * @author <a href="mailto:jjc@jclark.com">James Clark</a>
  */
 public class Attribute implements Cloneable {
     @NotNull
     private final String name;
     @NotNull
     private String value;
 
     public Attribute(@NotNull String name, @NotNull String value) {
         Element.checkNotNull(name);
         Element.checkNotNull(value);
         this.name = name;
         this.value = value;
     }
 
     @NotNull
     public String getName() {
         return name;
     }
 
     @NotNull
     public String getValue() {
         return value;
     }
 
     public void setValue(@NotNull String value) {
         Element.checkNotNull(value);
         this.value = value;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null || getClass() != obj.getClass())
             return false;
         Attribute att = (Attribute)obj;
         return name.equals(att.name) && value.equals(att.value);
     }
 
     @Override
     public int hashCode() {
         return 31 * name.hashCode() + value.hashCode();
     }
 
     @Override
     public Attribute clone() {
         try {
        return (Attribute) super.clone();
         }
         catch (CloneNotSupportedException e) {
             throw new InternalError();
         }
     }
 
     /**
      * Returns the Location of this Attribute.
      * The Location's range should cover the entire attribute starting from the first character of the
      * name up to and including the closing quote of the value.
      * @return the Location of this Attribute; null if not available
      */
     Location getLocation() {
         return null;
     }
 
     /**
      * Return the Location of a range of characters in the value of this Attribute.
      * @param beginIndex the index of the first character of the range
      * @param endIndex the index after the last character of the range
      * @return the Location for the specified range; null if no Location is available
      */
     Location getValueLocation(int beginIndex, int endIndex) {
         if (beginIndex < 0 || beginIndex > endIndex || endIndex > value.length())
             throw new IndexOutOfBoundsException();
         return null;
     }
 }
