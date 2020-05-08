 package http.header;
 
 import http.attribute.MultiValueAttribute;
 
 import java.util.Collection;
 import java.util.List;
 
 /**
  * Represents an HTTP header which can have a name and a single or multiple values. The type of the value can be defined
  * on instantiation.
  *
  * @author Karl Bennett
  */
 public class Header<T> extends MultiValueAttribute<T> {
 
     public static final String OPERATOR = ": ";
     public static final String DELIMITER = "\n";
 
     public static Collection<Header<String>> parse(String headers) {
 
        return parse(new MultiValueAttributeParser<Header<String>>(headers, OPERATOR, DELIMITER) {
 
             @Override
             protected Header<String> nextPair(String name, String value) {
 
                 return new Header<String>(name, value);
             }
         });
     }
 
 
     /**
      * Create an {@code Header} with a name and multiple values.
      *
      * @param name   the name of the attribute.
      * @param values the values for the attribute.
      */
     public Header(String name, List<T> values) {
         super(name, OPERATOR, DELIMITER, values);
     }
 
     /**
      * Create an {@code Header} with a name and a single value.
      *
      * @param name  the name of the attribute.
      * @param value the single value for the attribute.
      */
     public Header(String name, T value) {
         super(name, OPERATOR, DELIMITER, value);
     }
 }
