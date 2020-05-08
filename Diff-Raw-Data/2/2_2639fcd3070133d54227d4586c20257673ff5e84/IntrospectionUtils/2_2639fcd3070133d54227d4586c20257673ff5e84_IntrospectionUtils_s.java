 package org.nohope.reflection;
 
 import org.apache.commons.lang3.ArrayUtils;
 import org.nohope.IMatcher;
 import org.nohope.typetools.StringUtils;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.lang.reflect.Array;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.GenericArrayType;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.lang.reflect.WildcardType;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import static org.nohope.reflection.ModifierMatcher.*;
 
 //import static java.lang.reflect.Modifier.PUBLIC;
 
 /**
  * Set of introspection utils aimed to reduce problems caused by reflecting
  * native/inherited types.
  * <p/>
  * This class extensively uses "types compatibility" term which means:
  * <p/>
  * Types are compatible if:
  * 1. source type can be auto(un)boxed to target type
  * 2. source type is child of target type
  * 3. source and target are array types then one of these rules should be
  * applied to their component types.
  *
  * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
  * @since 8/12/11 5:42 PM
  */
 public final class IntrospectionUtils {
     /**
      * Default stake trace depth for method invocation.
      */
     private static final int DEFAULT_INVOKE_DEPTH = 3;
     /**
      * Method name for constructor (value: "new"). *
      */
     private static final String CONSTRUCTOR = "new";
 
     /**
      * list of java primitive types.
      */
     private static final List<Class<?>> PRIMITIVES = new ArrayList<>();
     /**
      * lookup map for matching primitive types and their object wrappers.
      */
     private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS =
             new HashMap<>();
 
     static {
         PRIMITIVES.add(Byte.TYPE);
         PRIMITIVES.add(Short.TYPE);
         PRIMITIVES.add(Integer.TYPE);
         PRIMITIVES.add(Long.TYPE);
         PRIMITIVES.add(Float.TYPE);
         PRIMITIVES.add(Double.TYPE);
         PRIMITIVES.add(Boolean.TYPE);
         PRIMITIVES.add(Character.TYPE);
 
         PRIMITIVES_TO_WRAPPERS.put(Byte.TYPE, Byte.class);
         PRIMITIVES_TO_WRAPPERS.put(Short.TYPE, Short.class);
         PRIMITIVES_TO_WRAPPERS.put(Integer.TYPE, Integer.class);
         PRIMITIVES_TO_WRAPPERS.put(Long.TYPE, Long.class);
         PRIMITIVES_TO_WRAPPERS.put(Float.TYPE, Float.class);
         PRIMITIVES_TO_WRAPPERS.put(Double.TYPE, Double.class);
         PRIMITIVES_TO_WRAPPERS.put(Boolean.TYPE, Boolean.class);
         PRIMITIVES_TO_WRAPPERS.put(Character.TYPE, Character.class);
     }
 
     /**
      * Utility class constructor.
      */
     private IntrospectionUtils() {
     }
 
     /**
      * @return list of primitive types
      */
     public static List<Class<?>> getPrimitives() {
         return new ArrayList<>(PRIMITIVES);
     }
 
     /**
      * Returns referenced wrapper for primitive type.
      *
      * @param p class suppose to be a primitive
      * @return wrapper for primitive, {@code null} if passed type is not a
      *         primitive
      */
     public static Class<?> primitiveToWrapper(final Class p) {
         return PRIMITIVES_TO_WRAPPERS.get(p);
     }
 
     /**
      * Returns boxed version of given primitive type (if actually it is a
      * primitive type).
      *
      * @param type type to translate
      * @return boxed primitive class or class itself if not primitive.
      */
     public static Class<?> tryFromPrimitive(final Class type) {
         if (type == null || !type.isPrimitive()) {
             return type;
         }
         return primitiveToWrapper(type);
     }
 
     /**
      * Invokes compatible constructor of given type with given constructor
      * arguments.
      *
      * @param type type to construct
      * @param args constructor arguments
      * @param <T>  type
      * @return new instance
      * @throws NoSuchMethodException     if no or more than one compatible
      *                                   constructor found
      * @throws InvocationTargetException on constructor invocation exception
      * @throws IllegalAccessException    on on attempt to invoke
      *                                   protected/private constructor
      * @throws InstantiationException    on constructing exception
      */
     public static <T> T newInstance(final Class<T> type, final Object... args)
             throws NoSuchMethodException, InvocationTargetException,
             IllegalAccessException, InstantiationException {
 
         final Constructor<T> constructor =
                 searchConstructor(type, getClasses(args));
         final Class[] signature = constructor.getParameterTypes();
 
         try {
             final Object[] params = adaptTo(args, signature);
 
             // request privileges
             AccessController.doPrivileged(new PrivilegedAction<Void>() {
                 @Override
                 public Void run() {
                     constructor.setAccessible(true);
                     return null;
                 }
             });
 
             return constructor.newInstance(params);
         } catch (final ClassCastException e) {
             throw cantInvoke(type, CONSTRUCTOR, signature, args, e);
         }
     }
 
     /**
      * Invokes compatible <b>public</b> method of given instance with given name and
      * parameters.
      *
      * @param instance   target object object
      * @param methodName name of method
      * @param args       method arguments
      * @return method invocation result
      * @throws NoSuchMethodException     if no or more than one compatible
      *                                   method found
      * @throws InvocationTargetException on method invocation exception
      * @throws IllegalAccessException    on on attempt to invoke
      *                                   protected/private method
      */
     public static Object invoke(final Object instance,
                                 final String methodName,
                                 final Object... args)
             throws NoSuchMethodException, InvocationTargetException,
             IllegalAccessException {
         return invoke(instance, and(PUBLIC, not(ABSTRACT)), methodName, args);
     }
 
     /**
      * Invokes compatible method of given instance with given
      * modifiers, name and parameters.
      * <p />
      * Example
      * <pre>
      * invoke(this, {@link ModifierMatcher#PACKAGE_DEFAULT PACKAGE_DEFAULT}, "method");
      * </pre>
      *
      * @param instance   target object object
      * @param methodName name of method
      * @param args       method arguments
      * @return method invocation result
      * @throws NoSuchMethodException     if no or more than one compatible
      *                                   method found
      * @throws InvocationTargetException on method invocation exception
      * @throws IllegalAccessException    on on attempt to invoke
      *                                   protected/private method
      *
      * @see #searchMethod(Object, IModifierMatcher, String, Class[])
      */
     public static Object invoke(@Nonnull final Object instance,
                                 final IModifierMatcher matcher,
                                 final String methodName,
                                 final Object... args)
             throws NoSuchMethodException, InvocationTargetException,
                    IllegalAccessException {
         final Method method =
                 searchMethod(instance, matcher, methodName, getClasses(args));
         return invoke(method, instance, args);
     }
 
     public static Object invoke(final Method method,
                                 @Nonnull final Object instance,
                                 final Object... args)
             throws NoSuchMethodException, InvocationTargetException,
                    IllegalAccessException {
         final Class[] sig = method.getParameterTypes();
 
         try {
             final Object[] params = adaptTo(args, sig);
             final int flags = method.getModifiers();
             final Class<?> clazz = instance.getClass();
             final int classFlags = clazz.getModifiers();
 
             // request privileges for non-public method/instance class/parent class
             if (!PUBLIC.matches(flags)
                 || !PUBLIC.matches(classFlags)
                 || clazz != method.getDeclaringClass()) {
                 AccessController.doPrivileged(new PrivilegedAction<Void>() {
                     @Override
                     public Void run() {
                         method.setAccessible(true);
                         return null;
                     }
                 });
             }
 
             return method.invoke(instance, params);
         } catch (final ClassCastException e) {
             throw cantInvoke(instance.getClass(), method.getName(), sig, args, e);
         }
     }
 
     /**
      * Checks if target class is assignable from source class in terms of
      * auto(un)boxing. if given classes are array types then recursively checks
      * if their component types are assignable from each other.
      * <p/>
      * Note: assignable means inheritance:
      * <pre>
      *   target
      *     ^
      *     |
      *   source
      * </pre>
      *
      * @param target target class
      * @param source source class
      * @return {@code true} if target is assignable from source
      */
     public static boolean isAssignable(final Class target, final Class source) {
         if (target == null || source == null) {
             throw new IllegalArgumentException("classes");
         }
 
         if (target.isArray() && source.isArray()) {
             return isAssignable(target.getComponentType(),
                     source.getComponentType());
         }
         return tryFromPrimitive(target).isAssignableFrom(tryFromPrimitive(source));
     }
 
     /**
      * Checks if given arrays of types are compatible.
      *
      * @param targets array of types
      * @param sources array of types
      * @return {@code true} if types are compatible
      */
     public static boolean areTypesCompatible(final Class[] targets,
                                              final Class[] sources) {
         // check if types are "varargs-compatible"
         if (sources.length != targets.length) {
             return false;
         }
 
         for (int i = 0; i < targets.length; i++) {
             // if we got null here then types are definitely compatible
             // (if target is not a primitive)
             if (sources[i] == null) {
                 if (targets[i].isPrimitive()) {
                     return false;
                 }
                 continue;
             }
 
             if (!isAssignable(targets[i], sources[i])) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Checks if types compatible in term of varargs.
      *
      * @param targets target types
      * @param sources source types
      * @return {@code true} if types are vararg-compatible
      */
     public static boolean areTypesVarargCompatible(final Class[] targets,
                                                    final Class[] sources) {
         if (!isVarargs(targets)) {
             return areTypesCompatible(targets, sources);
         }
 
         final Class[] flat = flattenVarargs(targets);
         final int flatSize = flat.length;
         final int srcSize = sources.length;
 
         // last vararg can be omitted
         if (srcSize == flatSize - 1) {
             return areTypesCompatible(ArrayUtils.subarray(flat, 0, flatSize - 1), sources);
         }
         // not a vararg
         if (srcSize == flatSize) {
             return areTypesCompatible(flat, sources);
         }
         // vararg should be assembled
         if (srcSize > flatSize) {
             final Class vararg = flat[flatSize - 1];
             for (int i = flatSize; i < srcSize; i++) {
                 if (!isAssignable(vararg, sources[i])) {
                     return false;
                 }
             }
 
             return areTypesCompatible(flat, ArrayUtils.subarray(sources, 0, flatSize));
         }
 
         return false;
     }
 
     /**
      * Searches for constructor of given type compatible with given signature.
      *
      * @param type      type
      * @param signature signature
      * @param <T>       type of object for search
      * @return constructor compatible with given signature
      * @throws NoSuchMethodException if no or more than one constructor found
      */
     @SuppressWarnings("unchecked")
     private static <T> Constructor<T> searchConstructor(
             final Class<T> type, final Class[] signature)
             throws NoSuchMethodException {
 
         final Constructor[] constructors = type.getDeclaredConstructors();
         Constructor found = null;
         Constructor vararg = null;
         int varargsFound = 0;
 
         for (final Constructor constructor : constructors) {
             final Class[] types = constructor.getParameterTypes();
 
             // Check for signature types compatibility
             if (areTypesCompatible(types, signature)) {
                 if (found != null) {
                     // we got one compatible constructor already...
                     throw tooMuch(type, CONSTRUCTOR, signature, ALL);
                 }
                 found = constructor;
             } else if (areTypesVarargCompatible(types, signature)) {
                 vararg = constructor;
                 varargsFound++;
             }
         }
 
         // there is no such constructor at all...
         if (found == null) {
             if (varargsFound > 1) {
                 throw tooMuch(type, CONSTRUCTOR, signature, ALL);
             }
             if (varargsFound == 1) {
                 return vararg;
             }
             throw notFound(type, CONSTRUCTOR, signature, ALL);
         }
 
         // this _should_ be an Constructor<T> huh?
         return (Constructor<T>) found;
     }
 
     /**
      * Checks if lower method overrides higher method.
      *
      * @param higher method used to be higher in class hierarchy
      * @param lower method used to be lower in class hierarchy
      * @return true if lower overrides higher
      */
     public static boolean isOverridden(final Method higher, final Method lower) {
         final Class<?> higherClass = higher.getDeclaringClass();
         final Class<?> lowerClass = lower.getDeclaringClass();
         return higherClass != lowerClass // child
                && higherClass.isAssignableFrom(lowerClass)
                && higher.getName().equals(lower.getName())
                && Arrays.deepEquals(higher.getParameterTypes(), lower.getParameterTypes()) //TODO: Too strict?
                && higher.getReturnType().isAssignableFrom(lower.getReturnType());
     }
 
     public static Set<Method> searchMethods(final Class<?> clazz,
                                             final IMatcher<Method> matcher) {
         final Set<Method> mth = new HashSet<>();
 
         Class<?> parent = clazz;
         while (parent != null) {
             for (final Method m : parent.getDeclaredMethods()) {
                 if (!matcher.matches(m)) {
                     continue;
                 }
 
                 /* Here we need to ensure no overridden methods from parent
                    will be added in search result */
                 boolean toBeAdded = true;
                 for (final Method added : mth) {
                     if (isOverridden(m, added)) {
                         // skipping overridden methods from parent
                         toBeAdded = false;
                         break;
                     }
                 }
 
                 if (toBeAdded) {
                     mth.add(m);
                 }
             }
 
             parent = parent.getSuperclass();
         }
 
         return mth;
     }
 
     /**
      * Searches for <b>public</b> method of given instance with given name
      * and compatible signature.
      *
      * @param instance   instance
      * @param methodName method name
      * @param signature  method signature
      * @return constructor compatible with given signature
      * @throws NoSuchMethodException if no or more than one method found
      *
      * @see #searchMethod(Object, IModifierMatcher, String, Class[])
      */
     public static Method searchMethod(final Object instance,
                                       final String methodName,
                                       final Class... signature)
             throws NoSuchMethodException {
         return searchMethod(instance, and(PUBLIC, not(ABSTRACT)), methodName, signature);
     }
 
     /**
      * Searches for method with given modifiers (public methods will be always
      * included in search) of given instance with given name and compatible signature.
      *
      * <p />
      * Example usage:
      * <pre>
      * searchMethod(this,
      *              {@link Modifier#PRIVATE PRIVATE} | {@link Modifier#PROTECTED PROTECTED},
      *              "invokeMe");
      * </pre>
      *
      * @param instance   instance
      * @param methodName method name
      * @param signature  method signature
      * @return constructor compatible with given signature
      * @throws NoSuchMethodException if no or more than one method found
      */
     public static Method searchMethod(final Object instance,
                                       final IModifierMatcher matcher,
                                       final String methodName,
                                       final Class... signature)
             throws NoSuchMethodException {
 
         final Class<?> type;
         if (instance instanceof Class) {
             type = (Class) instance;
         } else {
             type = instance.getClass();
         }
 
         final Set<Method> methods = searchMethods(type, new IMatcher<Method>() {
             @Override
             public boolean matches(final Method target) {
                 return methodName.equals(target.getName())
                        && matcher.matches(target.getModifiers());
             }
         });
 
         Method found = null;
         Method vararg = null;
         int varargsFound = 0;
 
         for (final Method method : methods) {
             final Class[] types = method.getParameterTypes();
 
             // Check for signature types compatibility
             if (areTypesCompatible(types, signature)) {
                 if (found != null) {
                     // we got one compatible method already...
                     throw tooMuch(type, methodName, signature, matcher);
                 }
                 found = method;
             } else if (areTypesVarargCompatible(types, signature)) {
                 vararg = method;
                 varargsFound++;
             }
         }
 
         // there is no such method at all...
         if (found == null) {
             switch (varargsFound) {
                 case 0:
                     throw notFound(type, methodName, signature, matcher);
                 case 1:
                     return vararg;
                 default:
                     throw tooMuch(type, methodName, signature, matcher);
             }
         }
 
         return found;
     }
 
     /**
      * Shrinks array component type to common parent type of all array elements.
      *
      * @param array given array
      * @return casted array
      */
     public static Object shrinkType(final Object[] array) {
         if (array.length == 0) {
             return array;
         }
 
         int firstNotNull = -1;
         for (int i = 0; i < array.length; i++) {
             if (null != array[i]) {
                 firstNotNull = i;
                 break;
             }
         }
 
         // array of nulls
         if (firstNotNull == -1) {
             return array;
         }
 
         Class<?> common = array[firstNotNull].getClass();
         for (int i = firstNotNull; i < array.length; i++) {
             final Object element = array[i];
 
             if (element != null) {
                 // can't be null
                 common = findCommonParent(element.getClass(), common);
             }
         }
 
         return shrinkTypeTo(array, common);
     }
 
     /**
      * Finds superclass common for passed classes.
      * <p/>
      * Note: this method passes through interface classes.
      *
      * @param c1 first class
      * @param c2 second class
      * @return common parent class if exists {@code null} otherwise
      */
     @Nullable
     public static Class<?> findCommonParent(final Class<?> c1,
                                             final Class<?> c2) {
         if (c1 == null) {
             return c2;
         }
         if (c2 == null) {
             return c1;
         }
 
         if (isAssignable(c1, c2)) {
             return c1;
         }
         if (isAssignable(c2, c1)) {
             return c2;
         }
 
         Class c1Parent = tryFromPrimitive(c1).getSuperclass();
         while (c1Parent != null && !c2.isInterface()) {
             if (isAssignable(c1Parent, c2)) {
                 return c1Parent;
             }
             c1Parent = c1Parent.getSuperclass();
         }
 
         // c1 or c2 is an interface (which not yet supported)
         return null;
     }
 
     /**
      * Casts array object to a given type.
      * <p/>
      * i.e:
      * <pre>
      *      shrinkTypeTo(Object[] {1, 2, 3}, int.class) --> int[] {1, 2, 3}
      * </pre>
      *
      * @param source array to be casted
      * @param clazz  desired type
      * @param <T>    desired type
      * @return casted array
      * @throws IllegalArgumentException if source object is not an array
      * @throws ClassCastException       on array storing exception
      */
     public static <T> Object shrinkTypeTo(final Object source,
                                           final Class<T> clazz) {
         if (source == null || !source.getClass().isArray()) {
             throw new IllegalArgumentException("array expected");
         }
 
         if (source.getClass().getComponentType() == clazz) {
             return source;
         }
 
         final int arrayLength = Array.getLength(source);
         final Object result = Array.newInstance(clazz, arrayLength);
         // zero length array
         if (arrayLength == 0) {
             if (isAssignable(source.getClass().getComponentType(), clazz)) {
                 return result;
             }
             throw arrayCastError(source, clazz);
         }
 
 
         for (int i = 0; i < arrayLength; i++) {
             Object origin = Array.get(source, i);
 
             try {
                 // in case if we got multidimensional array
                 if (origin != null
                         && origin.getClass().isArray()
                         && clazz.isArray()) {
                     origin = shrinkTypeTo(origin, clazz.getComponentType());
                 }
                 Array.set(result, i, origin);
             } catch (IllegalArgumentException e) {
                 throw arrayCastError(origin, clazz, e);
             }
         }
         return result;
     }
 
     /**
      * Shrinks component type of given array to given type.
      *
      * @param source array to be casted
      * @param clazz  desired type
      * @param <T>    desired type
      * @return casted array
      */
     public static <T> Object shrinkTypeTo(final Object[] source,
                                           final Class<T> clazz) {
         return shrinkTypeTo((Object) source, clazz);
     }
 
     /**
      * Creates object array from given object. If object is of array type
      * then shrinking it to {@link Object} type, else wraps it with new
      * {@link Object[]} instance.
      *
      * @param source object
      * @return array of objects
      * @see #shrinkTypeTo(Object, Class)
      */
     @Nullable
     public static Object[] toObjArray(final Object source) {
         if (source == null) {
             return null;
         }
         if (!source.getClass().isArray()) {
             return new Object[]{source};
         }
         return (Object[]) shrinkTypeTo(source, Object.class);
     }
 
     /**
      * @return name of method which called this method
      */
     public static String reflectSelfName() {
         return getMethodName(0);
     }
 
     /**
      * @return name of method which called method invoked this method
      */
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
 
     @Nullable
     public static String getCanonicalClassName(final Object obj) {
         return obj == null ? null : obj.getClass().getCanonicalName();
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
             for (final Class clazz : arguments) {
                 names[i++] = (clazz == null) ? null : clazz.getCanonicalName();
             }
         }
 
         return names;
     }
 
     /**
      * Returns list of types of given list of objects.
      *
      * @param arguments array of object
      * @return array of classes of given objects
      */
     public static Class[] getClasses(final Object... arguments) {
         final Class[] signature = new Class[arguments.length];
         {
             int i = 0;
             for (final Object argument : arguments) {
                 signature[i++] = (argument == null)
                         ? null
                         : argument.getClass();
             }
         }
 
         return signature;
     }
 
     /**
      * Casts object to a given class.
      *
      * @param obj object to cast
      * @param clazz type to cast to
      * @param <T> type
      * @throws ClassCastException if cast failed
      * @return casted object, {@code null} if {@code null} object passed
      */
     @Nullable
     @SuppressWarnings("unchecked")
     public static<T> T cast(@Nullable final Object obj,
                             @Nonnull final Class<T> clazz) {
         if (obj == null) {
             return null;
         }
 
         if (instanceOf(obj, clazz)) {
             return (T) obj;
         }
 
        throw new ClassCastException("Unable to cast " + obj + " to " + clazz);
     }
 
     @Nullable
     public static<T> T cast(@Nullable final Object obj,
                             @Nonnull final TypeReference<T> ref) {
         return cast(obj, ref.getTypeClass());
     }
 
     /**
      * Safely casts given object to given class.
      *
      * @param obj object to cast
      * @param clazz type to cast to
      * @param defaultValue default value in case object cannot be casted
      * @param <T> type
      * @return casted object or {@code null} if object not an instance
      *         of given class
      */
     @Nullable
     @SuppressWarnings("unchecked")
     public static<T> T safeCast(@Nullable final Object obj,
                                 @Nonnull final Class<T> clazz,
                                 @Nullable final T defaultValue) {
         if (instanceOf(obj, clazz)) {
             return (T) obj;
         }
 
         return defaultValue;
     }
 
     @Nullable
     public static<T> T safeCast(@Nullable final Object obj,
                                 @Nonnull final Class<T> clazz) {
         return safeCast(obj, clazz, null);
     }
 
     @Nullable
     public static<T> T safeCast(@Nullable final Object obj,
                                 @Nonnull final TypeReference<T> ref) {
         return safeCast(obj, ref, null);
     }
 
     @Nullable
     public static<T> T safeCast(@Nullable final Object obj,
                                 @Nonnull final TypeReference<T> ref,
                                 @Nullable final T defaultValue) {
         return safeCast(obj, ref.getTypeClass(), defaultValue);
     }
 
     /**
      * Check if lower object is instance of higher class.
      * <p />
      * This test works the same way as {@code instanceof} keyword.
      * <pre>
      *     (a instanceof SomeObject) == instanceOf(a, SomeObject.class)
      * </pre>
      *
      * <b>NOTE</b>: You can pass pass primitive type as class, but eventually
      * you'll get {@code false} because of autoboxing. For example
      * <pre>
      *     instanceOf(1, int.class) == false
      * </pre>
      *
      * @param object object
      * @param clazz class
      * @return {@code true} if object is instance of given class.
      *         {@code false} if lower object is {@code null}.
      */
     public static boolean instanceOf(@Nullable final Object object,
                                      @Nonnull final Class<?> clazz) {
         return object != null && clazz.isAssignableFrom(object.getClass());
     }
 
     /**
      * @param lower class suppose to be lower in inheritance hierarchy
      * @param higher class suppose to be higher in inheritance hierarchy
      * @see #instanceOf(Object, Class)
      */
     public static boolean instanceOf(@Nullable final Class<?> lower,
                                      @Nonnull final Class<?> higher) {
         return lower != null && higher.isAssignableFrom(lower);
     }
 
     /**
      * Returns give object class if it's possible.
      *
      * @param obj some object
      * @return {@link Class class} of given object or itself if
      *         it is already a class instance, {@code null} if {@code null} passed.
      */
     @Nullable
     public static Class<?> getClass(@Nullable final Object obj) {
         if (obj == null) {
             return null;
         }
 
         if (obj instanceof Class) {
             return (Class) obj;
         }
 
         return obj.getClass();
     }
 
     /**
      * Returns class for given type.
      *
      * @param type type
      * @return class object
      */
     @Nullable
     public static Class<?> getClass(final Type type) {
         if (type instanceof Class) {
             return (Class) type;
         }
         if (type instanceof ParameterizedType) {
             return getClass(((ParameterizedType) type).getRawType());
         }
         if (type instanceof GenericArrayType) {
             final Type componentType = ((GenericArrayType) type)
                     .getGenericComponentType();
             final Class<?> componentClass = getClass(componentType);
             if (componentClass != null) {
                 return Array.newInstance(componentClass, 0).getClass();
             }
         }
 
         //TODO: java.lang.reflect.TypeVariable, java.lang.reflect.WildcardType
         return null;
     }
 
     public static Class<?>[] getAllBounds(final Type type) {
         final List<Class<?>> result = new ArrayList<>();
         if (type instanceof TypeVariable) {
             final TypeVariable variable = (TypeVariable) type;
             final Type[] bounds = variable.getBounds();
             for (final Type bound : bounds) {
                 result.add(getClass(bound));
             }
         }
         if (type instanceof WildcardType) {
             final WildcardType wildcard = (WildcardType) type;
             // TODO: ...
             for (final Type bound : wildcard.getLowerBounds()) {
                 result.add(getClass(bound));
             }
             for (final Type bound : wildcard.getUpperBounds()) {
                 result.add(getClass(bound));
             }
         }
 
         return result.toArray(new Class<?>[result.size()]);
     }
 
     /**
      * Returns component type of last class in given signature of classes.
      *
      * @param signature signature of types
      * @return vararg component type
      */
     private static Class getVarargComponentType(final Class[] signature) {
         return signature[signature.length - 1].getComponentType();
     }
 
     /**
      * Casts list of objects to a given list of types.
      * Node: types of objects should be already compatible with given list of
      * types.
      *
      * @param objects list of objects to cast to
      * @param types   corresponding types
      * @return list of objects casted to given list of types
      */
     static Object[] adaptTo(final Object[] objects, final Class[] types) {
         final int argsLength = objects.length;
         final int typesLength = types.length;
 
         final List<Object> result = new ArrayList<>();
         if (argsLength == typesLength) {
             for (int i = 0; i < argsLength; i++) {
                 final Class type = types[i];
                 final Object object = objects[i];
 
                 if (type.isArray() && object != null) {
                     final Class<?> component = type.getComponentType();
                     result.add(shrinkTypeTo(object, component));
                     continue;
                 }
                 result.add(objects[i]);
             }
         } else if (isVarargs(types)) {
             final Class<?> clazz = getVarargComponentType(types);
 
             // vararg should be defaulted
             if (argsLength == typesLength - 1) {
                 return adaptTo(ArrayUtils.add(objects,
                         Array.newInstance(clazz, 0)), types);
             }
             // aggregate last arguments
             if (argsLength > typesLength) {
                 final int varargIndex = typesLength - 1;
 
                 final Object[] newParams =
                         ArrayUtils.subarray(objects, 0, varargIndex);
                 final Object[] varargRest =
                         ArrayUtils.subarray(objects, varargIndex,
                                 argsLength);
                 return adaptTo(ArrayUtils.add(newParams, varargRest), types);
             }
         }
         return result.toArray();
     }
 
     /**
      * Tricky method to get object wrapper for primitive type.
      *
      * @param clazz class
      * @param <T>   type
      * @return referenced wrapper class for all primitive types passe
      */
     static <T> Class autoBox(final Class<T> clazz) {
         return Array.get(Array.newInstance(clazz, 1), 0).getClass();
     }
 
     /**
      * Constructs array type of given type.
      *
      * @param clazz type
      * @param depth resulted array dimension
      * @param <T>   type
      * @return array type of given type with passed dimension
      */
     static <T> Class<?> toArrayType(final Class<T> clazz, final int depth) {
         Class result = clazz;
         for (int i = 0; i < depth; i++) {
             result = Array.newInstance(result, 0).getClass();
         }
         return result;
     }
 
     /**
      * Checks if given signature is possible vararg-like signature.
      *
      * @param signature array of classes
      * @return {@code true} if last element is type of array
      */
     private static boolean isVarargs(final Class[] signature) {
         final int length = signature.length;
         return length > 0 && signature[length - 1].isArray();
     }
 
     /**
      * Make last argument argument of vararg signature "flat".
      *
      * @param signature array of classes
      * @return new signature
      */
     static Class[] flattenVarargs(final Class[] signature) {
         if (isVarargs(signature)) {
             final Class[] result = signature.clone();
             final int length = signature.length;
             result[length - 1] = signature[length - 1].getComponentType();
             return result;
         }
         return signature;
     }
 
     /**
      * Returns method name in current thread stack.
      *
      * @param stackDepthShift position in stack to get deeper called method
      * @return method name
      */
     private static String getMethodName(final int stackDepthShift) {
         final StackTraceElement[] currStack =
                 Thread.currentThread().getStackTrace();
         // Find caller function name
         return currStack[DEFAULT_INVOKE_DEPTH + stackDepthShift]
                 .getMethodName();
     }
 
     /**
      * Helper function for constructing exception.
      *
      * @param message    exceptional message
      * @param type       type affected
      * @param methodName method
      * @param signature  method signature
      * @return constructed exception
      */
     private static NoSuchMethodException abort(final String message,
                                                final Class type,
                                                final String methodName,
                                                final Class[] signature,
                                                final IModifierMatcher matcher) {
         return new NoSuchMethodException(String.format(message,
                 type.getCanonicalName(), methodName,
                 StringUtils.join(getClassNames(signature)), matcher));
     }
 
     /**
      * Constructs exception for too much found method.
      *
      * @param type       type affected
      * @param methodName method
      * @param signature  method signature
      * @return constructed exception
      */
     private static NoSuchMethodException tooMuch(
             final Class type, final String methodName,
             final Class[] signature,
             final IModifierMatcher matcher) {
         return abort("More than one method %s#%s found conforms signature [%s] and matcher %s",
                 type, methodName, signature, matcher);
     }
 
     /**
      * Constructs exception for not found exception.
      *
      * @param type       type affected
      * @param methodName method name
      * @param signature  method signature
      * @return constructed exception
      */
     private static NoSuchMethodException notFound(
             final Class type, final String methodName,
             final Class[] signature, final IModifierMatcher matcher) {
         return abort("No methods %s#%s found to conform signature [%s] and matcher %s",
                 type, methodName, signature, matcher);
     }
 
     /**
      * Constructs {@link ClassCastException} for array casting error case.
      *
      * @param elem  element of array caused cast exception
      * @param clazz type of array
      * @param cause original exception
      * @return {@link ClassCastException} instance
      */
     private static ClassCastException arrayCastError(final Object elem,
                                                      final Class clazz,
                                                      final Throwable cause) {
         final ClassCastException ex = new ClassCastException(String.format(
                 "Unexpected value %s (%s) for array of type %s"
                 , elem
                 , elem == null ? "unknown" : elem.getClass().getCanonicalName()
                 , clazz.getCanonicalName()));
         ex.initCause(cause);
         throw ex;
     }
 
     /**
      * Constructs {@link ClassCastException} for array casting error case.
      *
      * @param src   source array
      * @param clazz type of destination array
      * @return {@link ClassCastException} instance
      */
     private static ClassCastException arrayCastError(final Object src,
                                                      final Class clazz) {
         throw new ClassCastException(String.format(
                 "Incompatible types found - source %s destination %s[]"
                 , src.getClass().getCanonicalName()
                 , clazz.getCanonicalName()));
     }
 
     /**
      * Constructs {@link NoSuchMethodException} in case when found method
      * can't be invoked.
      *
      * @param type       type of object
      * @param methodName method name failed invocation
      * @param signature  method signature
      * @param args       arguments was passed to method
      * @param cause      original exception
      * @return {@link NoSuchMethodException} instance
      */
     private static NoSuchMethodException cantInvoke(final Class type,
                                                     final String methodName,
                                                     final Class[] signature,
                                                     final Object[] args,
                                                     final Throwable cause) {
         final NoSuchMethodException e = new NoSuchMethodException(String.format(
                 "Unable to invoke method %s#%s(%s) with parameters [%s]"
                 , type.getCanonicalName()
                 , methodName
                 , StringUtils.join(getClassNames(signature))
                 , StringUtils.join(args)));
 
         return (NoSuchMethodException) e.initCause(cause);
     }
 }
