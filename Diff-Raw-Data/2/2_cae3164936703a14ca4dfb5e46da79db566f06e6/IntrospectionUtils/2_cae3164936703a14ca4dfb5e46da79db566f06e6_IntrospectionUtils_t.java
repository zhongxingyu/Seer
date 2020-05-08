 package org.ritsuka.youji.util.reflection;
 
 import org.apache.commons.lang.StringUtils;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.GenericArrayType;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 
 /**
  * Set of introspection utils aimed to reduce problems caused by reflecting
  * native/inherited types.
  *
  * @author Ketoth Xupack <ketoth.xupack@gmail.com>
  * @since 8/12/11 5:42 PM
  */
 public final class IntrospectionUtils {
     private static final int DEFAULT_INVOKE_DEPTH = 3;
 
     /** Utility class constructor. */
     private IntrospectionUtils() {
     }
 
     /**
      * Checks if given array if types are compatible. i.e targets is
      * subclasses of sources or same types (in terms of auto(un)boxing also).
      *
      * @param targets array of types
      * @param sources array of types
      * @return {@code true} if types are compatible
      */
     @SuppressWarnings("unchecked")
     public static boolean areTypesCompatible(final Class[] targets,
                                              final Class[] sources) {
         if (targets.length != sources.length) {
             return false;
         }
 
         for (int i = 0; i < targets.length; i++) {
             // if we got null here then types are definitely compatible
             if (sources[i] == null) {
                 continue;
             }
 
             if (!translateFromPrimitive(targets[i]).isAssignableFrom(sources[i])) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Returns boxed version of given primitive type (if actually it is a
      * primitive type).
      *
      * @param type type to translate
      * @return boxed primitive class or class itself if not primitive.
      */
     public static Class translateFromPrimitive(final Class type) {
         if (type == Byte.TYPE) return Byte.class;
         if (type == Short.TYPE) return Short.class;
         if (type == Integer.TYPE) return Integer.class;
         if (type == Long.TYPE) return Long.class;
         if (type == Float.TYPE) return Float.class;
         if (type == Double.TYPE) return Double.class;
         if (type == Boolean.TYPE) return Boolean.class;
         if (type == Character.TYPE) return Character.class;
         return type;
     }
 
     /**
      * Searches for constructor of given type compatible with given signature.
      *
      * @param type type
      * @param signature signature
      * @return constructor compatible with given signature
      * @throws NoSuchMethodException if no or more than one constructor found
      */
     @SuppressWarnings("unchecked")
     public static<T> Constructor<T> searchCompatibleConstructor(
             final Class<T> type, final Class[] signature)
             throws NoSuchMethodException {
 
         Constructor[] constructors = type.getConstructors();
         Constructor found = null;
         for (Constructor constructor : constructors) {
             Class[] types = constructor.getParameterTypes();
 
             // Does it have the same number of arguments that we're looking for.
             if (types.length != signature.length) {
                 continue;
             }
 
             // Check for signature types compatibility
             if (areTypesCompatible(types, signature)) {
                 if (found != null) {
                     // we got one compatible constructor already...
                     throw tooMuch(type, signature);
                 }
                 found = constructor;
             }
         }
 
         // there is no such constructor at all...
         if (found == null) {
             throw notFound(type, signature);
         }
 
         // this _should_ be an Constructor<T> huh?
         return (Constructor<T>) found;
     }
 
     /**
      * Searches for method of given instance with given name with compatible
      * signature.
      *
      * @param instance instance
      * @param methodName method name
      * @param signature method signature
      * @return constructor compatible with given signature
      * @throws NoSuchMethodException if no or more than one method found
      */
     @SuppressWarnings("unchecked")
     public static Method searchCompatibleMethod(final Object instance,
                                                 final String methodName,
                                                 final Class[] signature)
             throws NoSuchMethodException {
         final Class type;
         if (instance instanceof Class) {
             type = (Class) instance;
         } else {
             type = instance.getClass();
         }
 
         final Method[] constructors = type.getMethods();
         Method found = null;
 
         for (Method constructor : constructors) {
             Class[] types = constructor.getParameterTypes();
 
             // Does it have the same number of arguments that we're looking for.
             if (types.length != signature.length
                 || !methodName.equals(constructor.getName())) {
                 continue;
             }
 
             // Check for signature types compatibility
             if (areTypesCompatible(types, signature)) {
                 if (found != null) {
                     // we got one compatible constructor already...
                     throw tooMuch(type, signature);
                 }
                 found = constructor;
             }
         }
 
         // there is no such method at all...
         if (found == null) {
             throw notFound(type, signature);
         }
 
         return found;
     }
 
     /**
      * Invokes compatible method of given instance with given name and
      * parameters.
      *
      * @param instance target object object
      * @param methodName name of method
      * @param arguments method arguments
      * @return method invocation result
      * @throws NoSuchMethodException if no or more than one compatible
      * method found
      * @throws InvocationTargetException on method invocation exception
      * @throws IllegalAccessException on on attempt to invoke
      * protected/private method
      */
     public static Object invokeCompatibleMethod(final Object instance,
                                                 final String methodName,
                                                 final Object[] arguments)
             throws NoSuchMethodException, InvocationTargetException,
                    IllegalAccessException {
 
         Method method = searchCompatibleMethod(instance, methodName,
                 getClasses(arguments));
 
         return method.invoke(instance, arguments);
     }
 
     /**
      * Invokes compatible constructor of given type with given constructor
      * arguments.
      *
      * @param type type to construct
      * @param arguments constructor arguments
      * @param <T> type
      * @return new instance
      *
      * @throws NoSuchMethodException if no or more than one compatible
      * constructor found
      * @throws InvocationTargetException on constructor invocation exception
      * @throws IllegalAccessException on on attempt to invoke
      * protected/private constructor
      * @throws InstantiationException on constructing exception
      */
     public static<T> T newCompatibleInstance(final Class<T> type,
                                              final Object[] arguments)
             throws NoSuchMethodException, InvocationTargetException,
                    IllegalAccessException, InstantiationException {
         Constructor<T> constructor =
                 searchCompatibleConstructor(type, getClasses(arguments));
 
         return constructor.newInstance(arguments);
     }
 
     /**
      * Returns method name in current thread stack.
      *
      * @param stackShift position in stack to get deeper called method
      * @return method name
      */
     private static String getMethodName(final int stackShift) {
         StackTraceElement[] currStack = Thread.currentThread().getStackTrace();
         // Find caller function name
         return currStack[DEFAULT_INVOKE_DEPTH + stackShift].getMethodName();
     }
 
     /** @return name of method which called this method */
     public static String reflectSelfName() {
         return getMethodName(0);
     }
 
     /** @return name of method which called method invoked this method */
     public static String reflectCallerName() {
         return getMethodName(1);
     }
 
     /**
      * Transforms list of objects to list of their canonical names.
      *
      * @param arguments list of objects
      * @return canonical names for given object types
      */
     public static String[] getClassNames(final Object... arguments) {
         return getClassNames(getClasses(arguments));
     }
 
     /**
      * Transforms list of classes to list of their canonical names.
      *
      * @param arguments list of classes
      * @return canonical names for given classes
      */
     public static String[] getClassNames(final Class... arguments) {
         final String[] names = new String[arguments.length];
         {
             int i = 0;
             for (Class clazz : arguments) {
                 names[i++] = (clazz == null) ? null : clazz.getCanonicalName();
             }
         }
 
         return names;
     }
 
     /**
      * Get types of given objects.
      *
      * @param arguments array of object
      * @return array of classes of given objects
      */
     public static Class[] getClasses(final Object[] arguments) {
         Class[] signature = new Class[arguments.length];
         {
             int i = 0;
             for (Object argument : arguments) {
                 signature[i++] = (argument == null) ? null : argument.getClass();
             }
         }
 
         return signature;
     }
 
     /**
      * Returns class for given type.
      *
      * @param type type
      * @return class object
      */
     public static Class<?> getClass(final Type type) {
         if (type instanceof Class) {
             return (Class) type;
         }
         if (type instanceof ParameterizedType) {
             return getClass(((ParameterizedType) type).getRawType());
         }
         if (type instanceof GenericArrayType) {
             Type componentType = ((GenericArrayType) type)
                     .getGenericComponentType();
             Class<?> componentClass = getClass(componentType);
             if (componentClass != null) {
                 return Array.newInstance(componentClass, 0).getClass();
             }
         }
         return null;
     }
 
     /**
      * Helper function for constructing exception.
      *
      * @param message exceptional message
      * @param type type affected
      * @param signature method signature
      * @return constructed exception
      */
     private static NoSuchMethodException abort(final String message,
             final Class type, final Class[] signature) {
         return new NoSuchMethodException(message
                 + " "
                 + type.getCanonicalName()
                + '[' + StringUtils.join(signature, ',') + ']');
     }
 
     /**
      * Constructs exception for too much found method.
      *
      * @param type type affected
      * @param signature method signature
      * @return constructed exception
      */
     private static NoSuchMethodException tooMuch(
             final Class type, final Class[] signature) {
         return abort("More than one method found for", type, signature);
     }
 
     /**
      * Constructs exception for not found exception.
      *
      * @param type type affected
      * @param signature method signature
      * @return constructed exception
      */
     private static NoSuchMethodException notFound(
             final Class type, final Class[] signature) {
         return abort("No such method found for", type, signature);
     }
 }
