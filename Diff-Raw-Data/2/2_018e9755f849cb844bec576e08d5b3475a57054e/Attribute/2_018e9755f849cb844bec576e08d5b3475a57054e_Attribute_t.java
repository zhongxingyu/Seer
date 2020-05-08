 package http;
 
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Represents a generic HTTP attribute with a name and a single or multiple values. The type of the value can be defined
  * on instantiation.
  *
  * @author Karl Bennett
  */
 public class Attribute<T> {
 
     private final String name;
     private final List<T> values;
 
 
     /**
      * Create an {@code Attribute} with a name and multiple values.
      *
      * @param name the name of the attribute.
      * @param values the values for the attribute.
      */
     public Attribute(String name, List<T> values) {
 
         this.name = name;
         this.values = values;
     }
 
     /**
      * Create an {@code Attribute} with a name and a single value.
      *
      * @param name the name of the attribute.
      * @param value the single value for the attribute.
      */
     public Attribute(String name, T value) {
 
         this.name = name;
         this.values = Collections.singletonList(value);
     }
 
 
     /**
      * @return the attributes name.
      */
     public String getName() {
         return name;
     }
 
     /**
      * @return the attributes values.
      */
     public List<T> getValues() {
         return values;
     }
 
     /**
     * @return the attributes single value or if the attribute has multiple values return the first one.
      */
     public T getValue() {
         return values.get(0);
     }
 }
