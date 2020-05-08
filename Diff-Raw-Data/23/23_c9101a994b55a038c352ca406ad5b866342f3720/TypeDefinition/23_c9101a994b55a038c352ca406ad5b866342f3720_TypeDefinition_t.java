 package com.psddev.dari.util;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 
 public class TypeDefinition<T> {
 
     private final Type type;
 
     /** Returns an instance based on the given {@code type}. */
     public static TypeDefinition<?> getInstance(Type type) {
         return INSTANCES.get(type);
     }
 
     private static final PullThroughCache<Type, TypeDefinition<?>>
             INSTANCES = new PullThroughCache<Type, TypeDefinition<?>>() {
 
         @Override
         protected TypeDefinition<?> produce(Type type) {
             return new TypeDefinition<Object>(type);
         }
     };
 
     /** Returns an instance based on the given {@code objectClass}. */
     @SuppressWarnings("unchecked")
     public static <T> TypeDefinition<T> getInstance(Class<T> objectClass) {
         return (TypeDefinition<T>) getInstance((Type) objectClass);
     }
 
     /** Returns an instance based on the given {@code typeReference}. */
     @SuppressWarnings("unchecked")
     public static <T> TypeDefinition<T> getInstance(TypeReference<T> typeReference) {
         return (TypeDefinition<T>) getInstance(typeReference.getType());
     }
 
     /** Creates an instance based on the given {@code type}. */
     protected TypeDefinition(Type type) {
         this.type = type;
     }
 
     /**
      * Returns the defintion's Type, including any Generic Parameters it knows about
      * ex. TypeDefinition.getInstance(new TypeReference<List<String>>() { }).getType(); // List<String>
      * A TypeReference needs to be provided to getInstance in order for the Parameterized
      * type information to be retained (type erasure)
      */
     public Type getType() {
         return type;
     }
 
     /**
      * Returns the defintion's Class, not including any Generic Parameters it knows about
      * ex. TypeDefinition.getInstance(new TypeReference<List<String>>() { }).getObjectClass(); // List
      */
     @SuppressWarnings("unchecked")
     public Class<T> getObjectClass() {
 
         Type type = getType();
         if (type instanceof Class) {
             return (Class<T>) type;
 
         } else if (type instanceof ParameterizedType) {
             return (Class<T>) ((ParameterizedType) type).getRawType();
 
         } else {
             throw new IllegalStateException(String.format(
                     "Cannot find an object class based on [%s] type!",
                     type));
         }
     }
 
     /**
      * Returns an unmodifiable list of all the classes that can be assigned
      * from this one with the most specific match (itself) first.
      */
     public List<Class<? super T>> getAssignableClasses() {
         return assignableClasses.get();
     }
 
     private final PullThroughValue<List<Class<? super T>>>
             assignableClasses = new PullThroughValue<List<Class<? super T>>>() {
 
         @Override
         protected List<Class<? super T>> produce() {
 
             Class<T> objectClass = getObjectClass();
             List<Class<? super T>> classes = new ArrayList<Class<? super T>>();
             classes.add(objectClass);
 
             Class<? super T> superClass = objectClass.getSuperclass();
             if (superClass != null) {
                 classes.addAll(getInstance(superClass).getAssignableClasses());
             }
 
             return Collections.unmodifiableList(classes);
         }
     };
 
     /**
      * Returns an unmodifiable set of all classes and interfaces that are
      * either the same as, or is a super class or super interface of,
      * this type.
      */
     public Set<Class<?>> getAssignableClassesAndInterfaces() {
         return assignableClassesAndInterfaces.get();
     }
 
     private final transient PullThroughValue<Set<Class<?>>>
             assignableClassesAndInterfaces = new PullThroughValue<Set<Class<?>>>() {
 
         @Override
         protected Set<Class<?>> produce() {
             Class<T> objectClass = getObjectClass();
            Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
 
             classes.add(objectClass);
 
            for (Class<?> interfaceClass : objectClass.getInterfaces()) {
                classes.addAll(getInstance(interfaceClass).getAssignableClassesAndInterfaces());
            }

             Class<?> superClass = objectClass.getSuperclass();
             if (superClass != null) {
                 classes.addAll(getInstance(superClass).getAssignableClassesAndInterfaces());
             }
 
             return Collections.unmodifiableSet(classes);
         }
     };
 
     /** Returns an unmodifiable list of all the fields. */
     public List<Field> getAllFields() {
         return allFields.get();
     }
 
     private final PullThroughValue<List<Field>>
             allFields = new PullThroughValue<List<Field>>() {
 
         @Override
         protected List<Field> produce() {
 
             Class<T> objectClass = getObjectClass();
             List<Field> fields = new ArrayList<Field>();
             for (Field field : objectClass.getDeclaredFields()) {
                 field.setAccessible(true);
                 fields.add(field);
             }
 
             Class<? super T> superClass = objectClass.getSuperclass();
             if (superClass != null) {
                 fields.addAll(getInstance(superClass).getAllFields());
             }
 
             return Collections.unmodifiableList(fields);
         }
     };
 
     /**
      * Returns an unmodifiable map of all the serializable (non-static
      * and non-transient) fields with the normalized names as keys.
      */
     public Map<String, List<Field>> getAllSerializableFields() {
         return allSerializableFields.get();
     }
 
     private final PullThroughValue<Map<String, List<Field>>>
             allSerializableFields = new PullThroughValue<Map<String, List<Field>>>() {
 
         @Override
         protected Map<String, List<Field>> produce() {
 
             Class<T> objectClass = getObjectClass();
             Map<String, List<Field>> fieldsMap = new LinkedHashMap<String, List<Field>>();
 
             Class<? super T> superClass = objectClass.getSuperclass();
             if (superClass != null) {
                 for (Map.Entry<String, List<Field>> entry : getInstance(superClass).getAllSerializableFields().entrySet()) {
                     fieldsMap.put(entry.getKey(), new ArrayList<Field>(entry.getValue()));
                 }
             }
 
             for (Field field : objectClass.getDeclaredFields()) {
                 int mod = field.getModifiers();
                 if (!Modifier.isStatic(mod)
                         && !Modifier.isTransient(mod)) {
 
                     // Normalize common field name formats:
                     // _name, name_, fName or mName.
                     String name = field.getName();
                     if (name.startsWith("_")) {
                         name = name.substring(1);
                     } else if (name.endsWith("_")) {
                         name = name.substring(0, name.length() - 1);
                     } else if (name.length() > 2
                             && (name.charAt(0) == 'f' || name.charAt(0) == 'm')
                             && Character.isUpperCase(name.charAt(1))) {
                         name = Character.toLowerCase(name.charAt(1)) + name.substring(2);
                     }
 
                     List<Field> fields = fieldsMap.get(name);
                     if (fields == null) {
                         fields = new ArrayList<Field>();
                         fieldsMap.put(name, fields);
                     }
 
                     field.setAccessible(true);
                     fields.add(field);
                 }
             }
 
             return fieldsMap;
         }
     };
 
     /** Returns the first field with the given {@code name}. */
     public Field getField(String name) {
         for (Field field : getAllFields()) {
             if (field.getName().equals(name)) {
                 return field;
             }
         }
         List<Field> fields = getAllSerializableFields().get(name);
         return ObjectUtils.isBlank(fields) ? null : fields.get(fields.size() - 1);
     }
 
     /** Returns an unmodifiable list of all the constructors. */
     public List<Constructor<T>> getConstructors() {
         return constructors.get();
     }
 
     private final PullThroughValue<List<Constructor<T>>>
             constructors = new PullThroughValue<List<Constructor<T>>>() {
 
         @Override
         @SuppressWarnings("unchecked")
         protected List<Constructor<T>> produce() {
             List<Constructor<T>> constructors = new ArrayList<Constructor<T>>();
             for (Constructor<?> constructor : getObjectClass().getDeclaredConstructors()) {
                 constructor.setAccessible(true);
                 constructors.add((Constructor<T>) constructor);
             }
             return Collections.unmodifiableList(constructors);
         }
     };
 
     /**
      * Returns the constructor that can be called with the given array of
      * {@code parameterClasses}.
      */
     public Constructor<T> getConstructor(Class<?>... parameterClasses) {
         int length = parameterClasses.length;
         NEXT_CONSTRUCTOR:
             for (Constructor<T> constructor : getConstructors()) {
                 Class<?>[] declared = constructor.getParameterTypes();
                 if (length == declared.length) {
 
                     for (int i = 0; i < length; ++ i) {
                         if (!declared[i].isAssignableFrom(parameterClasses[i])) {
                             continue NEXT_CONSTRUCTOR;
                         }
                     }
 
                     return (Constructor<T>) constructor;
                 }
             }
 
         return null;
     }
 
     /** Creates a new instance. */
     public T newInstance() {
         Constructor<T> constructor = getConstructor();
         if (constructor == null) {
             throw new IllegalStateException(String.format(
                     "Can't create an instance of [%s] without a nullary constructor!",
                     getObjectClass()));
         }
 
         try {
             return constructor.newInstance();
 
         } catch (IllegalAccessException ex) {
             throw new IllegalStateException(ex);
 
         } catch (InstantiationException ex) {
             throw new IllegalStateException(String.format(
                     "Can't instantiate an instance of [%s]!",
                     getObjectClass()), ex);
 
         } catch (InvocationTargetException ex) {
             Throwable cause = ex.getCause();
             if (cause == null) {
                 cause = ex;
             }
             throw cause instanceof RuntimeException ?
                     (RuntimeException) cause :
                     new RuntimeException(cause.getMessage(), cause);
         }
     }
 
     /** Returns an unmodifiable list of all the methods. */
     public List<Method> getAllMethods() {
         return allMethods.get();
     }
 
     private final PullThroughValue<List<Method>>
             allMethods = new PullThroughValue<List<Method>>() {
 
         @Override
         protected List<Method> produce() {
 
             Class<T> objectClass = getObjectClass();
             List<Method> methods = new ArrayList<Method>();
             for (Method method : objectClass.getDeclaredMethods()) {
                 method.setAccessible(true);
                 methods.add(method);
             }
 
             Class<? super T> superClass = objectClass.getSuperclass();
             if (superClass != null) {
                 methods.addAll(getInstance(superClass).getAllMethods());
             }
 
             return Collections.unmodifiableList(methods);
         }
     };
 
     /**
      * Returns an unmodifiable map of all the getters (public non-static
      * methods that have no parameters and return a value) with the
      * normalized names as keys.
      */
     public Map<String, Method> getAllGetters() {
         return allGetters.get();
     }
 
     private final PullThroughValue<Map<String, Method>>
             allGetters = new PullThroughValue<Map<String, Method>>() {
 
         @Override
         protected Map<String, Method> produce() {
 
             Map<String, Method> getters = new LinkedHashMap<String, Method>();
             for (Method method : getAllMethods()) {
                 if (method.getDeclaringClass() != Object.class) {
 
                     int mod = method.getModifiers();
                     if (Modifier.isPublic(mod)
                             && !Modifier.isStatic(mod)
                             && method.getReturnType() != void.class
                             && method.getReturnType() != Void.class
                             && method.getParameterTypes().length == 0) {
 
                         String methodName = method.getName();
                         Matcher nameMatcher = StringUtils.getMatcher(methodName, "^(get|(is|has))([^a-z])(.*)$");
                         if (nameMatcher.matches()) {
 
                             String name = ObjectUtils.isBlank(nameMatcher.group(2))
                                     ? nameMatcher.group(3).toLowerCase() + nameMatcher.group(4)
                                     : methodName;
                             getters.put(name, method);
                         }
                     }
                 }
             }
 
             return Collections.unmodifiableMap(getters);
         }
     };
 
     /**
      * Returns an unmodifiable map of all the setters (public non-static
      * methods that take one parameter and doesn't return anything) with the
      * normalized names as keys.
      */
     public Map<String, Method> getAllSetters() {
         return allSetters.get();
     }
 
     private final PullThroughValue<Map<String, Method>>
             allSetters = new PullThroughValue<Map<String, Method>>() {
 
         @Override
         protected Map<String, Method> produce() {
 
             Map<String, Method> setters = new LinkedHashMap<String, Method>();
             for (Method method : getAllMethods()) {
                 if (method.getDeclaringClass() != Object.class) {
 
                     int mod = method.getModifiers();
                     if (Modifier.isPublic(mod)
                             && !Modifier.isStatic(mod)
                             && (method.getReturnType() == void.class || method.getReturnType() == Void.class)
                             && method.getParameterTypes().length == 1) {
 
                         String methodName = method.getName();
                         Matcher nameMatcher = StringUtils.getMatcher(methodName, "^set([^a-z])(.*)$");
                         if (nameMatcher.matches()) {
                             setters.put(nameMatcher.group(1).toLowerCase() + nameMatcher.group(2), method);
                         }
                     }
                 }
             }
 
             return Collections.unmodifiableMap(setters);
         }
     };
 
     /** {@link TypeDefinition} utility methods. */
     public static final class Static {
 
         private Static() {
         }
 
         /** Invalidates all caches. */
         public static void invalidateAll() {
             INSTANCES.invalidate();
         }
     }
 }
