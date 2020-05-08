 package http.attribute;
 
 import static http.util.Asserts.assertNotEmpty;
 
 /**
  * Represents a generic attribute with a name and a value. The type of the value can be defined on instantiation.
  *
  * @author Karl Bennett
  */
 public class Attribute<T> {
 
     private final String name;
     private final T value;
 
 
     /**
      * Create an {@code Attribute} with a name and value.
      *
      * @param name the name of the attribute.
      * @param value the value for the attribute.
      * @throws IllegalArgumentException if the {@code Attribute}'s name is empty or the values are null.
      */
     public Attribute(String name, T value) {
 
         assertNotEmpty("name", name);
 
         this.name = name;
         this.value = value;
     }
 
 
     /**
      * @return the attributes name.
      */
     public String getName() {
 
         return name;
     }
 
     /**
      * @return the attributes value.
      */
     public T getValue() {
 
         return value;
     }
 
 
     @Override
     public boolean equals(Object o) {
 
         if (this == o) return true;
 
         if (o == null || getClass() != o.getClass()) return false;
 
         Attribute attribute = (Attribute) o;
 
        return getName().equals(attribute.getName()) &&
                (getValue() == null ? attribute.getValue() == null : getValue().equals(attribute.getValue()));
     }
 
     @Override
     public int hashCode() {
 
        int result = getName().hashCode();
 
        result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
 
         return result;
     }
 
     @Override
     public String toString() {
 
        return getName() + ": " + getValue();
     }
 }
