 package http.attribute;
 
 
 import http.util.Parser;
 
 import java.util.*;
 
 import static http.util.Asserts.assertNotNull;
 import static http.util.Checks.isEmpty;
 import static http.util.Checks.isNotEmpty;
 import static http.util.Checks.isNull;
 
 /**
  * Represents a generic attribute with a name and a single or multiple values. The type of the value can be defined on
  * instantiation.
  *
  * @author Karl Bennett
  */
 public class MultiValueAttribute<T> extends Attribute<T> {
 
    protected static abstract class MultiValueAttributeParser<A extends MultiValueAttribute> extends Parser {
 
         private final String operator;
         private final Map<String, A> attributes;
 
        public MultiValueAttributeParser(String string, String operator, String delimiter) {
             super(string, delimiter);
 
             this.operator = operator;
 
             this.attributes = new HashMap<>();
         }
 
         protected abstract A nextPair(String name, String value);
 
         @Override
         protected void next(String part) {
 
             String[] pair = part.split(operator);
 
             A attribute;
             if (2 == pair.length) {
 
                 attribute = attributes.get(pair[0]);
 
                 if (isNull(attribute)) {
 
                     attributes.put(pair[0], nextPair(pair[0], pair[1]));
 
                 } else {
 
                     attribute.addValue(pair[1]);
                 }
 
                 return;
             }
 
             throw new IllegalArgumentException("Part (" + part + ") is not in the correct format e.g. \"name" +
                     operator + "value\"");
         }
 
         @Override
         public void parse() {
 
             attributes.clear();
 
             super.parse();
         }
 
         public Collection<A> getAttributes() {
 
             return attributes.values();
         }
     }
 
     /**
      * Concatenate the {@link #toString()} values of a {@link Collection} of {@code MultiValueAttribute}. Each value
      * will be delimited by the supplied {@code delimiter} {@link String}.
      *
      * @param attributes the attributes that will have their string representations concatenated.
      * @return the concatenated parameter string.
      */
     public static <T, A extends MultiValueAttribute<T>> String toString(Collection<A> attributes) {
 
         if (isEmpty(attributes)) return "";
 
         Iterator<A> iterator = attributes.iterator();
 
         // Add the first attribute to the string.
         StringBuilder toStringHolder = new StringBuilder(iterator.next().toString());
 
         A attribute;
         String delimiter;
         while (iterator.hasNext()) {
 
             attribute = iterator.next();
             delimiter = attribute.getDelimiter();
 
             toStringHolder.append(delimiter).append(attribute);
         }
 
         return toStringHolder.toString();
     }
 
    protected static <A extends MultiValueAttribute<String>> Collection<A> parse(MultiValueAttributeParser<A> pasrer) {
 
         pasrer.parse();
 
         return new HashSet<>(pasrer.getAttributes());
     }
 
     /**
      * Filter any empty values out of the supplied {@link List}.
      *
      * @param list the {@code List} to filter.
      * @param <T>  the type of elements that the {@code List} holds.
      * @return the filtered list.
      */
     private static <T> List<T> filterEmptyElements(List<T> list) {
 
         List<T> filteredList = new ArrayList<>();
 
         for (T element : list) {
 
             if (isNotEmpty(element)) filteredList.add(element);
         }
 
         return filteredList;
     }
 
 
     private final String operator;
     private final String delimiter;
     private final List<T> values;
 
 
     /**
      * Create a {@code MultiValueAttribute} with a name and multiple values.
      *
      * @param name   the name of the attribute.
      * @param values the values for the attribute.
      * @throws IllegalArgumentException if the {@code Attribute}'s name is empty or the values are null.
      */
     public MultiValueAttribute(String name, String operator, String delimiter, List<T> values) {
         super(name, null);
 
         assertNotNull("operator", operator);
         assertNotNull("delimiter", delimiter);
         assertNotNull("values", values);
 
         this.operator = operator;
         this.delimiter = delimiter;
 
         // Cast the supplied list to an ArrayList so that it is definitely mutable.
         // We do this so that the addValue(T) and addAllValues(List<T>) methods work.
         this.values = new ArrayList<>(values);
     }
 
 
     /**
      * Create a {@code MultiValueAttribute} with a name and a single value. If the supplied value is {@code null} or
      * empty the {@code MultiValueAttribute} will be constructed without a value.
      *
      * @param name  the name of the attribute.
      * @param value the single value for the attribute.
      * @throws IllegalArgumentException if the {@code MultiValueAttribute}'s name is empty or the value is null.
      */
     public MultiValueAttribute(String name, String operator, String delimiter, T value) {
         super(name, null);
 
         assertNotNull("operator", operator);
         assertNotNull("delimiter", delimiter);
 
         this.operator = operator;
         this.delimiter = delimiter;
 
         this.values = new ArrayList<>();
         if (isNotEmpty(value)) this.values.add(value);
     }
 
 
     /**
      * Get the first value in the {@code MultiValueAttribute}s value list, unless the list is empty then {@code null} is
      * returned.
      *
      * @return the first value.
      */
     @Override
     public T getValue() {
 
         return isEmpty(values) ? null : values.get(0);
     }
 
     /**
      * @return the attributes values.
      */
     public List<T> getValues() {
 
         return Collections.unmodifiableList(values);
     }
 
     /**
      * Add an value to the {@code MultiValueAttribute}.
      *
      * @param value the to add.
      * @return the value that was added.
      */
     public T addValue(T value) {
 
         if (isNotEmpty(value)) values.add(value);
 
         return value;
     }
 
     /**
      * Add a list of values to the {@code MultiValueAttribute}.
      *
      * @param values the to add.
      * @return the values that were added.
      * @throws NullPointerException if the supplied {@code values} list is null.
      */
     public List<T> addAllValues(List<T> values) {
 
         this.values.addAll(filterEmptyElements(values));
 
         return values;
     }
 
 
     protected String getOperator() {
 
         return operator;
     }
 
     protected String getDelimiter() {
 
         return delimiter;
     }
 
 
     @Override
     public String toString() {
 
         StringBuilder toStringHolder = new StringBuilder();
 
         int i = 0;
         for (; i < (values.size() - 1); i++) {
 
             toStringHolder.append(getName()).append(operator).append(values.get(i)).append(delimiter);
         }
 
         toStringHolder.append(getName()).append(operator).append(values.get(i));
 
         return toStringHolder.toString();
     }
 }
