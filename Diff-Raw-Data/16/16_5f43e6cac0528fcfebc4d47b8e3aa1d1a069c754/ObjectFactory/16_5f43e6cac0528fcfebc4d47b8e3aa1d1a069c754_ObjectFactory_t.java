 package nl.pwiddershoven.jfactory;
 
 import com.google.common.collect.Maps;
 import nl.pwiddershoven.jfactory.annotations.AfterFactoryBuild;
 import nl.pwiddershoven.jfactory.types.LazyValue;
 import nl.pwiddershoven.jfactory.types.Sequence;
 import nl.pwiddershoven.jfactory.types.Trait;
 import nl.pwiddershoven.jfactory.utils.ReflectionUtils;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.*;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Maps.newHashMap;
 
 public abstract class ObjectFactory<T> {
 
     private Class<T> factoryClass;
     private Map<String, Trait> traits = newHashMap();
     private Map<String, Object> propertyValues = newHashMap();
     private Map<String, Object> fieldValues = newHashMap();
 
     // map of sequences by name for factory classes
     private static Map<Class, Map<String, Integer>> sequences = newHashMap();
 
     /** Public **/
 
     public ObjectFactory(Class<T> factoryClass) {
         this.factoryClass = factoryClass;
 
         define(); //define the factory, calls subclasses.
     }
 
     /**
      * Build object.
      * @return
      */
     public T build(Object... attributes) {
         T object = ReflectionUtils.createObject(factoryClass);
 
         String trait = null;
 
        // check if the first attribute matches the name of a defined trait.
        // Also check if the number of attributes is odd. If it's even, we'll assume it's a number of key/value pairs
        // for properties and not meant to apply a trait.
        if(attributes.length > 0 && attributes.length %2 != 0 && traits.containsKey((String)attributes[0])) {
             // the first attribute matched the name of a defined trait, assume the trait was meant to be applied
             trait = (String)attributes[0];
 
             // now remove the the trait name from the list of attributes
             attributes = Arrays.copyOfRange(attributes, 1, attributes.length);
         }
 
         if(trait != null) {
             // a trait was defined, apply it
             Trait t = traits.get(trait);
             t.apply();
         }
 
         // merge default properties with supplied attributes
         Map<String, Object> propertyValues = createObjectPropertyValues(this.propertyValues, attributes);
 
         // now set properties and fields to the created object
         setProperties(object, propertyValues);
         setFields(object, fieldValues);
 
         executeCallbacks(AfterFactoryBuild.class, object);
         return object;
     }
 
     /** DSL methods **/
 
     /**
      * Method that defines the factory. Should be implemented by subclasses.
      * The implementation of this method should call dsl methods below, to populate the factory.
      */
     protected abstract void define();
 
     /**
      * Register a trait for this factory
      * @param trait
      */
     protected void trait(Trait trait) {
         traits.put(trait.getName(), trait);
     }
 
     /**
      * Register a static value for the given field, can be private.
      * @param name name of the field
      * @param value value that should be assigned to each built object for the given field
      */
     protected void field(String name, Object value) {
         fieldValues.put(name, value);
     }
 
     /**
      * Register a static value for the given property
      * @param name name of the property
      * @param value value that should be assigned to each built object for the given property
      */
     protected void property(String name, Object value) {
         propertyValues.put(name, value);
     }
 
     /**
      * Register a sequence value for a given property
      * @param name name of the property
      * @param seq Sequence object, to be evaluated later
      */
     protected void sequence(String name, Sequence seq) {
         propertyValues.put(name, seq);
     }
 
     protected int rand(int max) {
         return new Random().nextInt(max);
     }
 
     /** Protected methods **/
 
     protected Class<T> getFactoryClass() {
         return factoryClass;
     }
 
     protected void executeCallbacks(Class<? extends Annotation> annotationType, T object) {
         //first gather a list of all methods down the inheritance chain which applied the given annotation
         List<Method> annotatedMethods = newArrayList();
 
         Class clz = getClass();
         while(ObjectFactory.class.isAssignableFrom(clz)) {
             // add the methods to the beginning of the method list, so we get a list ordered by position in the inheritance chain
             annotatedMethods.addAll(0, ReflectionUtils.getAnnotatedMethods(clz, annotationType));
             clz = clz.getSuperclass();
         }
 
         // now call all methods
         for(Method method : annotatedMethods) {
             ReflectionUtils.invokeMethod(this, method, object);
         }
     }
 
     /** Private methods **/
 
     private void setProperty(Object target, String name, Object value) {
         if(! ReflectionUtils.setProperty(target, name, getValue(value))) {
             // no property was found, try to set the field directly
             setField(target, name, value);
         }
     }
 
     private void setField(Object target, String name, Object value) {
         ReflectionUtils.setField(target, name, getValue(value));
     }
 
     private void setProperties(T object, Map<String, Object> propertyValues) {
         for(String property: propertyValues.keySet()) {
             Object value = propertyValues.get(property);
             if(value instanceof Sequence) {
                 value = ((Sequence) value).apply(currentSequence(property)); //lazy evaluate sequence. TODO: see how to make this nicer/more generic
             }
 
             setProperty(object, property, value);
         }
     }
 
     private void setFields(T object, Map<String, Object> fieldValues) {
         for(String field : fieldValues.keySet()) {
             Object value = fieldValues.get(field);
             setField(object, field, value);
         }
     }
 
     private Object getValue(Object value) {
         return value instanceof LazyValue ? ((LazyValue) value).evaluate() : value;
     }
 
     /**
      * Merge passed attributes with the supplied property values.
      * @param defaultPropertyValues
      * @param attributes
      * @return
      */
     private Map<String, Object> createObjectPropertyValues(Map<String, Object> defaultPropertyValues, Object... attributes) {
         Map<String, Object> propertyValues = newHashMap(defaultPropertyValues);
 
         if(attributes != null) {
             Iterator<Object> iterator = newArrayList(attributes).iterator();
             Map<String, Object> propertyOverrideMap = new HashMap<String, Object>();
 
             while(iterator.hasNext()) {
                 String name = (String)iterator.next();
 
                 // we can only create a map entry if we have both a key and value, so make sure there's a value left
                 if(iterator.hasNext()) {
                     Object object = iterator.next();
                     propertyOverrideMap.put(name, object);
                 }
             }
 
             propertyValues.putAll(propertyOverrideMap);
         }
 
         return propertyValues;
     }
 
     /**
      * Retrieve the current sequence value for the given property.
      * @param name name of the property that should be assigned the sequence value.
      * @return The current value of the referenced sequence.
      */
     private int currentSequence(String name) {
         Map<String, Integer> sequencesForClass = sequences.get(getClass());
         if(sequencesForClass == null) {
             sequencesForClass = new HashMap<String, Integer>();
             sequences.put(getClass(), sequencesForClass);
         }
 
         Integer seq = sequencesForClass.get(name);
         seq = seq == null ? 1 : seq + 1;
 
         sequencesForClass.put(name, seq);
         return seq;
     }
 }
