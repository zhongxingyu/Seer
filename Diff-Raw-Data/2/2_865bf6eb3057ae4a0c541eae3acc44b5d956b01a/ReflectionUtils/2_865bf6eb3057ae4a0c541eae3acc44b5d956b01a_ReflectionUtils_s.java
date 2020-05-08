 package com.emergentideas.utils;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.persistence.Id;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.emergentideas.webhandle.CallSpec;
 
 public class ReflectionUtils {
 	
 	protected static Class[] primitives = new Class[] { Boolean.TYPE, Byte.TYPE, Character.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE, Void.TYPE };
 
 	
 	/**
 	 * Returns true if the method follows the rules to be a setter on a bean.
 	 * @param method
 	 * @return
 	 */
 	public static boolean isSetterMethod(Method method) {
 		if(isTypedMethod("set", method) == false) {
 			return false;
 		}
 		
 		return method.getParameterTypes().length == 1;
 	}
 	
 	/**
 	 * Returns true if the method follows the rules to be a getter on a bean.
 	 * @param method
 	 * @return
 	 */
 	public static boolean isGetterMethod(Method method) {
 		if(isTypedMethod("get", method) == false) {
 			return false;
 		}
 		
 		return method.getParameterTypes().length == 0;
 	}
 	
 
 	
 	/**
 	 * Returns true if the method follows the naming rules for the type (getter/setter)
 	 * @param type
 	 * @param method
 	 * @return
 	 */
 	protected static boolean isTypedMethod(String type, Method method) {
 		String methodName = method.getName();
 		
 		if(methodName.startsWith(type) && methodName.length() > 3 && Character.isUpperCase(methodName.charAt(3))) {
 			return true;
 		}
 
 		return false;
 	}
 	
 	/**
 	 * Assuming this is a getter or setter method, return the property name.  If it is not a getter or setter, return null.
 	 * @param m
 	 * @return
 	 */
 	public static String getPropertyName(Method m) {
 		if(m == null) {
 			return null;
 		}
 		
 		String methodName = m.getName();
 		if(methodName.startsWith("get")) {
 			methodName = methodName.substring(3);
 		}
 		else if(methodName.startsWith("set")) {
 			methodName = methodName.substring(3);
 		}
 		else if(methodName.startsWith("is")) {
 			methodName = methodName.substring(2);
 		}
 		else {
 			return null;
 		}
 		
 		if(StringUtils.isBlank(methodName)) {
 			return null;
 		}
 		
 		if(Character.isUpperCase(methodName.charAt(0)) == false) {
 			return null;
 		}
 		
 		return Character.toLowerCase(methodName.charAt(0)) + methodName.substring(1);
 	}
 
 	/**
 	 * returns the setter method if it exists for a given object and property name.  If two or more 
 	 * methods with the correct name but differing types are found, the first found will be returned
 	 * @param focus
 	 * @param propertyName like address
 	 * @return
 	 */
 	public static Method getSetterMethod(Object focus, String propertyName) {
 		return getSetterMethodFromClass(focus.getClass(), propertyName);
 	}
 	
 	/**
 	 * returns the setter method if it exists for a given object and property name.  If two or more 
 	 * methods with the correct name but differing types are found, the first found will be returned
 	 * @param focus
 	 * @param propertyName like address
 	 * @return
 	 */
 	public static Method getSetterMethodFromClass(Class focus, String propertyName) {
 		String methodName = "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
 		for(Method m : focus.getMethods()) {
 			if(m.getName().equals(methodName)) {
 				if(m.getParameterTypes().length == 1) {
 					return m;
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * returns the setter method if it exists for a given object and property name.  
 	 * @param focus
 	 * @param propertyName like address
 	 * @return
 	 */
 	public static Method getGetterMethod(Object focus, String propertyName) {
 		return getGetterMethodFromClass(focus.getClass(), propertyName);
 	}
 	
 	public static Method getGetterMethodFromClass(Class focus, String propertyName) {
 		String methodName = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
 		for(Method m : focus.getMethods()) {
 			if(m.getName().equals(methodName)) {
 				if(m.getParameterTypes().length == 0) {
 					return m;
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Returns the array version of a class if the type passed is not already an array class.  For instance,
 	 * if Integer.class is passed, the return value is Integer[].class.  If Integer[].class is passed, the
 	 * return value is Integer[].class.
 	 * @param type The type to convert.
 	 * @return
 	 */
 	public static Class getArrayStyle(Class type) {
 		if(isArrayStyle(type)) {
 			// Okay, it is an array, so just return it
 			return type;
 		}
 		return Array.newInstance(type, 0).getClass();
 	}
 	
 	/**
 	 * Returns the component type if the class is an array class or <code>type</code> otherwise.
 	 * @param type
 	 * @return
 	 */
 	public static Class getNonArrayStyle(Class type) {
 		if(isArrayStyle(type)) {
 			return type.getComponentType();
 		}
 		return type;
 	}
 	
 	/**
 	 * Makes a single object into an array of those objects (length 1) if it is not already an array. If <code>o</code>
 	 * is null, then return null.
 	 * @param o
 	 * @return
 	 */
 	public static <O> O[] makeArrayFromObject(O o) {
 		if(o == null) {
 			return null;
 		}
 		if(o.getClass().isArray()) {
 			return (O[])o;
 		}
 		
 		O[] result = (O[])Array.newInstance(o.getClass(), 1);
 		result[0] = o;
 		return result;
 	}
 	
 	/**
 	 * Returns true if this class is an array type of some sort.
 	 * @param type
 	 * @return
 	 */
 	public static boolean isArrayStyle(Class type) {
 		if(type.getName().startsWith("[")) {
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * returns true if the object is an array of something
 	 * @param o
 	 * @return
 	 */
 	public static boolean isArrayType(Object o) {
 		return o.getClass().isArray();
 	}
 	
 	/**
 	 * Returns the annotation of the specified type if it exists on the method.
 	 * @param m
 	 * @param type
 	 * @return
 	 */
 	public static <T> T getAnnotation(Method m, Class<T> type) {
 		for(Annotation anno : m.getAnnotations()) {
 			if(type.isAssignableFrom(anno.getClass())) {
 				return (T)anno;
 			}
 		}
 		
 		return null;
 	}
 	
 	/** Returns the annotation of the specified type if it exists on the field.
 	 * @param f
 	 * @param type
 	 * @return
 	 */
 	public static <T> T getAnnotation(Field f, Class<T> type) {
 		for(Annotation anno : f.getAnnotations()) {
 			if(type.isAssignableFrom(anno.getClass())) {
 				return (T)anno;
 			}
 		}
 		
 		return null;
 	}
 
 	
 	/**
 	 * Returns the annotation of the specified type if it exists on the class itself.
 	 * @param m
 	 * @param type
 	 * @return
 	 */
 	public static <T> T getAnnotationOnClass(Class c, Class<T> type) {
 		for(Annotation anno : c.getAnnotations()) {
 			if(type.isAssignableFrom(anno.getClass())) {
 				return (T)anno;
 			}
 		}
 		
 		return null;
 	}
 
 	
 	/**
 	 * Returns true if an annotation of the specified class exists on the method.
 	 * @param m
 	 * @param type
 	 * @return
 	 */
 	public static <T> boolean hasAnnotation(Method m, Class<T> type) {
 		for(Annotation anno : m.getAnnotations()) {
 			if(type.isAssignableFrom(anno.getClass())) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Returns true if <code>type</code> is the class for a primitive type like int or float.
 	 * @param type
 	 * @return
 	 */
     public static boolean isPrimitive(Class type) {
     	for(Class c : primitives) {
     		if(c == type) {
     			return true;
     		}
     	}
     	
     	return false;
     }
 
     /**
      * Returns the primitive type for a class like Integer which has a corresponding primitive. 
      * @param type
      * @return
      */
     public static Class getPrimitiveType(Class type) {
         if (type == Boolean.class)
         	return Boolean.TYPE;
         if (type == Byte.class)
         	return Byte.TYPE;
         if (type == Character.class)
         	return Character.TYPE;
         if (type == Short.class)
         	return Short.TYPE;
         if (type == Integer.class)
         	return Integer.TYPE;
         if (type == Long.class)
         	return Long.TYPE;
         if (type == Float.class)
         	return Float.TYPE;
         if (type == Double.class)
         	return Double.TYPE;
         if (type == Void.class)
         	return Void.TYPE;
         return null;
     }
     
     /**
      * returns the default value for a primitive type as a not primitive object.  So,
      * if Integer.TYPE is passed then the return value will be like new Integer(0).
      * @param type
      * @return
      */
     public static <T> Object getDefault(Class<T> type) {
         if (type == Boolean.TYPE)
         	return Boolean.valueOf(false);
         if (type == Byte.TYPE)
         	return Byte.valueOf((byte)0);
         if (type == Character.TYPE)
         	return Character.valueOf((char)0);
         if (type == Short.TYPE)
         	return Short.valueOf((short)0);
         if (type == Integer.TYPE)
         	return Integer.valueOf(0);
         if (type == Long.TYPE)
         	return Long.valueOf(0);
         if (type == Float.TYPE)
         	return Float.valueOf(0);
         if (type == Double.TYPE)
         	return Double.valueOf(0);
         return null;
     }
 
 
     /**
      * Gets the first method from <code>focus</code> of the name <code>methodName</code>. Returns null
      * if no method of that name is found. It actually looks for the first method with a given name
      * in the declared class, if not found there, then in the superclass and so on. This makes sure that
      * if there's an overloaded method that the one returned has the parameterized signature.
      * @param focus
      * @param methodName
      * @return
      */
 	public static <T> Method getFirstMethod(Class<T> focus, String methodName) {
 		Method foundMethod = null;
 		Class<?> c = focus;
 		found: while(c != null) {
 			for(Method m : c.getDeclaredMethods()) {
 				if(m.getName().equals(methodName)) {
 					foundMethod = m;
 					break found;
 				}
 			}
 //			ParameterizedType parameterizedType = (ParameterizedType) c.getGenericSuperclass();
 //			c = (Class) parameterizedType.getActualTypeArguments()[0];			
 			c = c.getSuperclass();
 		}
 		
 		Method secondFound = null;
 		try {
 			secondFound = focus.getMethod(methodName, foundMethod.getParameterTypes());
 		} catch(Exception e) {}
		return null;
 	}
 	
 	public static <T> CallSpec[] getMethodsWithAnnotaion(Object focus, Class<T> annotation) {
 		if(focus == null || annotation == null) {
 			return new CallSpec[0];
 		}
 		
 		List<CallSpec> result = new ArrayList<CallSpec>();
 		for(Method m : focus.getClass().getMethods()) {
 			T t = getAnnotation(m, annotation);
 			if(t != null) {
 				result.add(new CallSpec(focus, m, false));
 			}
 		}
 		
 		return result.toArray(new CallSpec[result.size()]);
 	}
 	
     /**
      * Gets the first method from <code>focus</code> of the name <code>methodName</code> and creates a 
      * call spec so it can be called. Returns null if no method of that name is found.
      * @param focus
      * @param methodName
      * @return
      */
 	public static <T> CallSpec getFirstMethodCallSpec(Object focus, String methodName) {
 		Method m = getFirstMethod(focus.getClass(), methodName);
 		if(m != null) {
 			return new CallSpec(focus, m, false);
 		}
 		
 		return null;
 	}
 	
 	public static boolean isPublic(Method m) {
 		return Modifier.isPublic(m.getDeclaringClass().getModifiers());
 	}
 	
 	public static boolean isReturnTypeVoid(Method m) {
 		Class c = m.getReturnType();
 		return c.equals(Void.class) || c.equals(Void.TYPE);
 	}
 	
 	public static Class getClassForName(String name) throws ClassNotFoundException {
 		return Thread.currentThread().getContextClassLoader().loadClass(name);
 	}
 	
 	public static <T> boolean contains(T[] list, T searchTerm) {
 		for(T item : list) {
 			if(searchTerm == null && item == null) {
 				return true;
 			}
 			if(searchTerm == null || item == null) {
 				continue;
 			}
 			
 			if(searchTerm.equals(item)) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * If one class can be assigned from the other, it gets the distance between the 
 	 * two in terms of how many class definitions are between the two classes.
 	 * @param one
 	 * @param two
 	 * @return
 	 */
 	public static Integer findClassDistance(Class one, Class two) {
 		Class base;
 		Class extension;
 		
 		if(one.equals(two)) {
 			return 0;
 		}
 		else if(one.isAssignableFrom(two)) {
 			base = one;
 			extension = two;
 		}
 		else if(two.isAssignableFrom(one)) {
 			base = two;
 			extension = one;
 		}
 		else {
 			return null;
 		}
 		
 		int distance = Integer.MAX_VALUE;
 		Integer possible = findClassDistance(base, extension.getSuperclass());
 		if(possible != null && possible < distance) {
 			distance = possible;
 		}
 		
 		for(Class inter : extension.getInterfaces()) {
 			possible = findClassDistance(base, inter);
 			if(possible != null && possible < distance) {
 				distance = possible;
 			}
 		}
 		
 		return distance + 1;
 	}
 	
 	public static Class<?> determineIdClass(Class entity) {
 		for(Field f : entity.getDeclaredFields()) {
 			if(getAnnotation(f, Id.class) != null) {
 				Class result = f.getType();
 				if(isPrimitive(result)) {
 					return getDefault(result).getClass();
 				}
 				return result;
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Determines the id setter method if it exists.
 	 * @param entity
 	 * @return
 	 */
 	public static Method getIdSetterMethod(Class entity) {
 		for(Field f : entity.getDeclaredFields()) {
 			if(getAnnotation(f, Id.class) != null) {
 				return getSetterMethodFromClass(entity, f.getName());
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Returns the name of the setter method for the ID member or null if there
 	 * either is no ID member or no bean setter method for it. 
 	 * @param entity
 	 * @return
 	 */
 	public static String getIdSetterMethodName(Class entity) {
 		Method m = getIdSetterMethod(entity);
 		if(m != null) {
 			return m.getName();
 		}
 		
 		return null;
 	}
 	
 	public static Class<?> determineParameterizedArgumentType(Type parameterType, Object objBeingCalled) {
 		if(parameterType != null && parameterType instanceof TypeVariable) {
 			TypeVariable type = (TypeVariable)parameterType;
 			TypeVariable[] classTypes = type.getGenericDeclaration().getTypeParameters();
 			
 			int index = -1;
 			
 			for(int i = 0; i < classTypes.length; i++) {
 				if(classTypes[i].getName().equals(type.getName())) {
 					index = i;
 					break;
 				}
 			}
 			
 			Type[] types = ((ParameterizedType)objBeingCalled.getClass().getGenericSuperclass()).getActualTypeArguments();
 			
 			if(index >= 0) {
 				return (Class)types[index];
 			}
 		}
 		return null;
 	}
 	
 
 }
