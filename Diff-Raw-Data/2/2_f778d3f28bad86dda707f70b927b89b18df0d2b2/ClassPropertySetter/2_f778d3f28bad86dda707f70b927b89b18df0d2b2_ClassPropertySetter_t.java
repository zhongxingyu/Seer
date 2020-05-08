 package com.stresstest.random.constructor;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Member;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.TreeSet;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.stresstest.random.ObjectGenerator;
 import com.stresstest.random.ValueGenerator;
 import com.stresstest.random.ValueGeneratorFactory;
 
 /**
  * Abstraction of object property.
  * 
  * @author Anton Oparin
  * 
  * @param <T>
  *            parameterized {@link Class}.
  */
 abstract public class ClassPropertySetter<T> {
 
     /**
      * Sets value for configured field.
      * 
      * @param target
      *            T value to update
      */
     abstract public void setProperties(Object target);
 
     abstract public List<ValueGenerator<?>> getValueGenerators();
 
     abstract public ClassPropertySetter<T> clone(List<ValueGenerator<?>> generatorsToUse);
 
     /**
      * Returns affected Class. Supposed to be used primerely inside invocation.
      * 
      * @return affected {@link Class}.
      */
     abstract protected Class<?> getAffectedClass();
 
     /**
      * Filter for applicable methods, uses only set and add methods
      */
     final private static Predicate<Member> FILTER_APPLICABLE_METHODS = new Predicate<Member>() {
         @Override
         public boolean apply(Member input) {
             if ((input.getModifiers() & Modifier.STATIC) != 0)
                 return false;
             String name = input.getName().toLowerCase();
             return name.startsWith("set") || name.startsWith("add");
         }
     };
 
     /**
      * Comparator based on String presentation, needed to distinguish the same field on the different levels of inheritance.
      */
     final private static Comparator<ClassPropertySetter<?>> COMPARE_STRING_PRESENTATION = new Comparator<ClassPropertySetter<?>>() {
         @Override
         public int compare(final ClassPropertySetter<?> firstPropertySetter, final ClassPropertySetter<?> secondPropertySetter) {
             return firstPropertySetter.toString().compareTo(secondPropertySetter.toString());
         }
     };
 
     /**
      * Comparator based on Presentation type.
      */
     final static Comparator<ClassPropertySetter<?>> COMPARE_PRESENTATION_TYPE = new Comparator<ClassPropertySetter<?>>() {
         @Override
         public int compare(final ClassPropertySetter<?> firstPropertySetter, final ClassPropertySetter<?> secondPropertySetter) {
             boolean firstSimpleProperty = firstPropertySetter instanceof ClassPropertySimpleSetter;
             boolean secondSimpleProperty = secondPropertySetter instanceof ClassPropertySimpleSetter;
             if (firstSimpleProperty && secondSimpleProperty) {
                 // Step 1. Check field names
                 Field firstField = ((ClassPropertySimpleSetter<?>) firstPropertySetter).field;
                 Field secondField = ((ClassPropertySimpleSetter<?>) secondPropertySetter).field;
                 if (firstField != null && secondField != null) {
                     int comparison = secondField.getName().compareTo(firstField.getName());
                     if (comparison != 0)
                         return comparison;
                 }
                 // Step 2. Check method names
                 Method firstMethod = ((ClassPropertySimpleSetter<?>) firstPropertySetter).method;
                 Method secondMethod = ((ClassPropertySimpleSetter<?>) secondPropertySetter).method;
                 if (firstMethod != null && secondMethod != null) {
                     int comparison = secondMethod.getName().compareTo(firstMethod.getName());
                     if (comparison != 0)
                         return comparison;
                 }
                 // Step 2. Check classes
                 Class<?> firstClass = ((ClassPropertySimpleSetter<?>) firstPropertySetter).getAffectedClass();
                 Class<?> secondClass = ((ClassPropertySimpleSetter<?>) secondPropertySetter).getAffectedClass();
                 if (firstClass != secondClass) {
                     return firstClass.isAssignableFrom(secondClass) ? 1 : -1;
                 }
             } else if (!firstSimpleProperty && !secondSimpleProperty) {
                 // Comparison of Collections is equivalent to comparison of the types
                 return compare(((ClassPropertyCollectionSetter<?>) firstPropertySetter).initialPropertySetter,
                         ((ClassPropertyCollectionSetter<?>) secondPropertySetter).initialPropertySetter);
             } else if (firstSimpleProperty) {
                 return 1;
             } else if (secondSimpleProperty) {
                 return -1;
             }
             return 0;
         }
     };
 
     /**
      * Extracts and normalizes Member name.
      */
     public static String extractMemberName(Member method) {
         String fieldName = extractFieldName(method);
         return (fieldName.startsWith("set") || fieldName.startsWith("add") || fieldName.startsWith("get")) ? fieldName.substring(3) : fieldName;
     }
 
     /**
      * Extracts and normalizes field name.
      */
     public static String extractFieldName(Member member) {
         return member != null ? member.getName().toLowerCase() : "";
     }
 
     /**
      * Finds field for specified field name.
      * 
      * @param searchClass
      *            class to search in.
      * @param fieldName
      *            name of the field.
      * @return Field or null if not found.
      */
     public static Field findField(final ClassAccessWrapper<?> searchClass, final String fieldName) {
         // Step 1. Filter all field's with specified name
         Collection<Field> fieldCandidates = Collections2.filter(searchClass.getFields(), new Predicate<Field>() {
             @Override
             public boolean apply(Field field) {
                 return fieldName.equals(extractFieldName(field));
             }
         });
         // Step 2. Return first field in sorted Collection.
         return fieldCandidates.isEmpty() ? null : fieldCandidates.iterator().next();
     }
 
     /**
      * Finds possible method for specified field name.
      * 
      * @param searchClass
      *            class to search in.
      * @param methodName
      *            name of the Method
      * @return possible set method for specified field name.
      */
     public static Method findSetMethod(final ClassAccessWrapper<?> searchClass, final String methodName) {
         // Step 1. Filter method candidates
         Collection<Method> methodCandidates = Collections2.filter(searchClass.getMethods(), new Predicate<Method>() {
             @Override
             public boolean apply(Method method) {
                 return method.getParameterTypes().length == 1 && method.getName().toLowerCase().startsWith("set")
                         && extractMemberName(method).equals(methodName);
             }
         });
         // Step 2. Return first method in the Collection
         return methodCandidates.isEmpty() ? null : methodCandidates.iterator().next();
     }
 
     /**
      * Finds possible add method for specified field name.
      * 
      * @param searchClass
      *            Class to search for.
      * @param methodName
      *            name of the method.
      * @return possible add method for specified field name.
      */
     public static Method findAddMethod(final ClassAccessWrapper<?> searchClass, final String methodName) {
         // Step 1. Filter method candidates
         Collection<Method> methodCandidates = Collections2.filter(searchClass.getMethods(), new Predicate<Method>() {
             @Override
             public boolean apply(Method method) {
                 String possibleFieldName = extractMemberName(method);
                 return method.getParameterTypes().length == 1 && method.getName().toLowerCase().startsWith("add")
                         && (methodName.startsWith(possibleFieldName) || possibleFieldName.startsWith(methodName));
             }
         });
         // Step 2. Return first field
         return methodCandidates.isEmpty() ? null : methodCandidates.iterator().next();
     }
 
     /**
      * Builds property setter for the specified field.
      * 
      * @param field
      *            field to set
      * @param valueGenerator
      *            {@link ValueGenerator} to use.
      * @return PropertySetter for the provided field.
      */
     public static <T> ClassPropertySetter<T> createFieldSetter(final ClassAccessWrapper<?> sourceClass, final Field field) {
         // Step 1. Sanity check
         if (field == null)
             throw new IllegalArgumentException();
         // Step 2. Retrieve possible set name for the field
         Method possibleMethods = findSetMethod(sourceClass, extractFieldName(field));
         // Step 3. Create possible field setter.
         return create(sourceClass, field, possibleMethods);
     }
 
     /**
      * * Constructs property setter based on provided Method.
      * 
      * @param method
      *            target method.
      * @param valueGenerator
      *            {@link ValueGenerator} to use.
      * @return constructed PropertySetter for the method, or <code>null</code> if such PropertySetter can't be created.
      */
     public static <T> ClassPropertySetter<T> createMethodSetter(final ClassAccessWrapper<?> sourceClass, final Method method) {
         if (method == null)
             throw new IllegalArgumentException();
         if (method.getParameterTypes().length != 1)
             return null;
         Field possibleField = findField(sourceClass, extractMemberName(method));
         return create(sourceClass, possibleField, method);
     }
 
     /**
      * Generates PropertySetter for provided Field, Method and {@link ValueGenerator}.
      * 
      * @param field
      *            target field.
      * @param method
      *            target set method.
      * @param valueGenerator
      *            {@link ValueGenerator} to use.
      * @return constructed PropertySetter.
      */
     public static <T> ClassPropertySetter<T> create(final ClassAccessWrapper<?> sourceClass, final Field field, final Method method) {
         return create(sourceClass, field, method, null);
     }
 
     @SuppressWarnings("unchecked")
     public static <T> ClassPropertySetter<T> create(final ClassAccessWrapper<?> sourceClass, final Field field, final Method method,
             ValueGenerator<T> valueGenerator) {
         Class<T> targetClass = (Class<T>) (field != null ? field.getType() : method.getParameterTypes()[0]);
         if (valueGenerator == null) {
             if (Collection.class.isAssignableFrom(targetClass)) {
                 if (field != null)
                     return new ClassPropertyCollectionSetter<T>(sourceClass, field);
                 else if (method != null)
                     return new ClassPropertyCollectionSetter<T>(sourceClass, method);
             } else {
                 valueGenerator = ObjectGenerator.getValueGenerator(targetClass);
             }
         }
 
         return new ClassPropertySimpleSetter<T>(field, method, valueGenerator);
     }
 
     /**
      * Extracts all possible PropertySetters with specified access level.
      * 
      * @param searchClass
      *            {@link ClassAccessWrapper} access wrapper to generate properties for.
      * @return list of all PropertySetter it can set, ussing specified field.
      */
     public static <T> Collection<ClassPropertySetter<?>> extractAvailableProperties(final ClassAccessWrapper<T> searchClass,
             final ValueGeneratorFactory valueGeneratorFactory) {
         // Step 1. Create Collection field setters
         final Collection<ClassPropertySetter<?>> propertySetters = new TreeSet<ClassPropertySetter<?>>(COMPARE_STRING_PRESENTATION);
         propertySetters.addAll(valueGeneratorFactory.getPropertySetterManager().getApplicableProperties(searchClass));
         for (Field field : searchClass.getFields()) {
             if ((field.getModifiers() & Modifier.STATIC) == 0)
                 propertySetters.add(createFieldSetter(searchClass, field));
         }
         // Step 2. Create Collection of method setters
         for (Method method : Collections2.filter(searchClass.getMethods(), FILTER_APPLICABLE_METHODS)) {
            if (method.getParameterTypes().length != 1 || method.getParameterTypes()[0] == Object.class || searchClass.getSourceClass().equals(method.getParameterTypes()[0]))
                 continue;
             ClassPropertySetter<?> propertySetter = createMethodSetter(searchClass, method);
             if (propertySetter != null) {
                 propertySetters.add(propertySetter);
             }
         }
 
         final List<ClassPropertySetter<?>> resultSetters = new ArrayList<ClassPropertySetter<?>>(propertySetters);
         Collections.sort(resultSetters, COMPARE_PRESENTATION_TYPE);
         // Step 3. Returning accumulated result
         return resultSetters;
     }
 
     public static <T> ClassPropertySetter<T> constructPropertySetter(final ClassAccessWrapper<T> searchClass, final ValueGeneratorFactory valueGeneratorFactory) {
         return new ClassPropertyCombinedSetter<T>(extractAvailableProperties(searchClass, valueGeneratorFactory));
     }
 
 }
