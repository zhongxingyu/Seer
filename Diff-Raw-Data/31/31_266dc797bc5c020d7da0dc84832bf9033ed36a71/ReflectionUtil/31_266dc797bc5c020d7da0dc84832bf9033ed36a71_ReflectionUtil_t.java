 package eu.execom.testutil.util;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * Util class for reflection logic needed by testutil.
  * 
  * @author Dusko Vesin
  * @author Nikola Olah
  * @author Bojan Babic
  * @author Nikola Trkulja
  */
 public final class ReflectionUtil {
 
     /** The Constant GET_METHOD_PREFIX. */
     private static final String GET_METHOD_PREFIX = "get";
 
     /** The Constant IS_METHOD_PREFIX. */
     private static final String IS_METHOD_PREFIX = "is";
 
     /**
      * Instantiates a new reflection util.
      */
     private ReflectionUtil() {
         super();
     }
 
     /**
      * Check if specified method of class X is get method. Primitive boolean type fields have "is" for prefix of their
      * get method and all other types have "get" prefix for their get method so this method checks if field name gotten
      * from method name has a matched field name in the class X. Methods with prefix "is" have to have underlying field
      * of primitive boolean class.
      * 
      * @param classs
      *            class that method belongs to
      * @param method
      *            thats checking
      * @return <code>true</code> if method is "real" get method, <code>false</code> otherwise
      */
     public static boolean isGetMethod(final Class<?> classs, final Method method) {
         try {
             if (method.getName().startsWith(IS_METHOD_PREFIX)) {
                 // if field type is primitive boolean
                 return classs.getDeclaredField(getFieldName(method)).getType() == boolean.class;
             }
             return method.getName().startsWith(GET_METHOD_PREFIX)
                     && findFieldInInheritance(classs, getFieldName(method)) != null;
         } catch (final Exception e) {
             return false;
         }
     }
 
     /**
      * Get field name from specified get method. It differs get methods for regular objects and primitive boolean fields
      * by their get method prefix. ("is" is the prefix for primitive boolean get method)
      * 
      * @param method
      *            that is checked
      * @return field name represented by specified get method
      */
     public static String getFieldName(final Method method) {
         String fieldName;
         if (method.getName().startsWith(IS_METHOD_PREFIX)) {
             fieldName = StringUtils.removeStart(method.getName(), IS_METHOD_PREFIX);
         } else {
             fieldName = StringUtils.removeStart(method.getName(), GET_METHOD_PREFIX);
         }
         return StringUtils.uncapitalize(fieldName);
     }
 
     /**
      * Searches trough property class inheritance tree for field with specified name. Starting from property class
      * method recursively climbs higher in the inheritance tree until it finds field with specified name or reached
      * object in which case returns null.
      * 
      * @param fieldClass
      *            class of the field.
      * @param fieldName
      *            name of the field
      * @return {@link Field} with specified name, otherwise <code>null</code>>
      */
     public static Field findFieldInInheritance(final Class<?> fieldClass, final String fieldName) {
         if (fieldClass == null) {
             return null;
         }
         try {
             return fieldClass.getDeclaredField(fieldName);
         } catch (final Exception e) {
             return findFieldInInheritance(fieldClass.getSuperclass(), fieldName);
         }
     }
 
     /**
      * Determines if specified object is instance of {@link List}.
      * 
      * @param <X>
      *            the generic type
      * @param object
      *            unidentified object
      * @return <code>true</code> if specified object is instance of {@link List} , <code>false</code> otherwise
      */
     public static <X> boolean isListType(final X object) {
         return object instanceof List;
     }
 
     /**
      * Check if specified class is contained in entity types.
      * 
      * @param object
      *            thats checked
      * @param entityTypes
      *            list of entity types
      * @return <code>true</code> if specified class is contained in entity types, <code>false</code> otherwise.
      */
     public static boolean isEntityType(final Class<?> object, final List<Class<?>> entityTypes) {
         return entityTypes.contains(object);
     }
 
     /**
      * Check if specified class is contained in complex types.
      * 
      * @param classs
      *            thats checking
      * @param complexTypes
      *            the complex types
      * @return <code>true</code> if specified class is contained in complex types, <code>false</code> otherwise.
      */
     public static boolean isComplexType(final Class<?> classs, final List<Class<?>> complexTypes) {
         return complexTypes.contains(classs);
     }
 
     /**
      * Check if specified class is contained in ignored types.
      * 
      * @param classs
      *            thats checked
      * @param ignoredTypes
      *            list of ignored types
      * @return <code>true</code> if specified class is contained in ignored types, <code>false</code> otherwise.
      */
     public static boolean isIgnoredType(final Class<?> classs, final List<Class<?>> ignoredTypes) {
         return ignoredTypes.contains(classs);
     }
 
     /**
      * Checks if object is ignored type.
      * 
      * 
     * @param firstObject
      *            that is checked
     * @param secondObject
      *            that is checked
      * @param ignoredTypes
      *            list of ignored type
      * @return <code>true</code> if type of expected or actual is ignored type, <code>false</code> otherwise.
      */
    public static boolean isIgnoredType(final Object firstObject, final Object secondObject,
             final List<Class<?>> ignoredTypes) {
 
        if (secondObject != null) {
            return isIgnoredType(secondObject.getClass(), ignoredTypes);
         }
 
        if (firstObject != null) {
            return isIgnoredType(firstObject.getClass(), ignoredTypes);
         }
 
         return false;
     }
 
 }
