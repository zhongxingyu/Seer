 package aima.core.learning.framework;
 
 /**
  * Defines an attribute of an example; attributes are the key-value pairs that
  * make up an Example. See pages 699 and 700 of AIMAv3.
  * @author Ravi Mohan
  * @author Andrew Brown
  */
public class Attribute<E> {
 
     String name;
     E value;
 
     /**
      * Constructor
      * @param name
      * @param value 
      */
     public Attribute(String name, E value) {
         this.name = name;
         this.value = value;
     }
 
     /**
      * Constructor
      * @param value 
      */
     public Attribute(E value) {
         this.name = "Unnamed Attribute";
         this.value = value;
     }
 
     /**
      * Sets the attribute name
      * @param name 
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * Returns the attribute name
      * @return 
      */
     public String getName() {
         return this.name;
     }
 
     /**
      * Sets the attribute value
      * @param value 
      */
     public void setValue(E value) {
         this.value = value;
     }
 
     /**
      * Sets the attribute value auto-magically from a String to the generic
      * type.
      * @param value 
      */
     public void setValue(String value) {
         try {
             Class type = this.value.getClass();
             this.value = (E) type.getDeclaredConstructor(String.class).newInstance(value);
         } catch (Exception e) {
             this.value = null;
         }
     }
 
     /**
      * Returns the attribute value
      * @return 
      */
     public E getValue() {
         return this.value;
     }
 
     /**
      * Determines whether the attribute value is a valid one; override this
      * method in descendants
      * @param value
      * @return 
      */
     public boolean isValid(E value) {
         if (value != null) {
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Returns a string representation of the value of this attribute
      * @return 
      */
     public String toString() {
         return this.value.toString();
     }

     /**
      * Checks if this attribute is equivalent to another
      * @param a
      * @return 
      */
    public boolean equals(Attribute a) {
        if (!this.getName().equals(a.getName())) {
            return false;
        }
        if (!this.getValue().equals(a.getValue())) {
            return false;
        }
        return true;
     }
 }
