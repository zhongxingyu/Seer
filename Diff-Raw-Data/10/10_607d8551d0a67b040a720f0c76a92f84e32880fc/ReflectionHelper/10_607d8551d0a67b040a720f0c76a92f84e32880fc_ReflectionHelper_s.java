 package vooga.rts.util;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 
 /**
  * This is a class that makes it much easier to do reflection.
  * It has a few methods that return the information that is needed.
  * 
  * @author Jonathan Schmidt
  * @modified Francesco Agosti
  */
 public class ReflectionHelper {
 
     /**
      * Finds the Constructor of a specified class that take in the
      * provided parameters.
      * 
      * @param c The Class to search
      * @param params The parameters to find a constructor for
      * @return The constructor required to instantiate.
      * @throws ClassDefinitionException
      */
     public static Constructor<?> findConstructor (Class<?> c, Object ... params) {
         Class<?>[] types = toClassArray(params);
 
         Constructor<?>[] constructors = c.getConstructors();
         for (Constructor<?> con : constructors) {
             Class<?>[] conParams = con.getParameterTypes();
             if (paramsEqual(types, conParams)) {
                 return con;
             }
         }
         return null;
     }
     
     /**
      * Retuns a new instance of your class given your parameters. 
      * @param Class object
      * @param Parameters of constructor
      * @return
      */
     public static Object makeInstance(Class<?> c, Object ... params){
     	try {
    		System.out.println(findConstructor(c,params));
 			return findConstructor(c,params).newInstance(params);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
     }
     
     /**
      * Returns a new instance of the class defined by your path and your parameters
      */
     public static Object makeInstance(String path, Object ... params){
     	return makeInstance(makeClass(path), params);
     }
     
     /**
      * Returns the class object that is located at path.
      */
     private static Class<?> makeClass(String path){
     	Class<?> thisClass = null;
 		try {
 			thisClass = Class.forName(path);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
     	return thisClass;
     }
     /**
      * Finds and returns the requested method.
      * 
      * @param c The class to search in.
      * @param methodName The name of the Method.
      * @param params The parameters that the method takes in.
      * @return
      * @throws NoSuchMethodException
      */
     public static Method findMethod (Class<?> c, String methodName, Class<?>[] params)
                                                                                       throws NoSuchMethodException {
         Method res = c.getMethod(methodName, params);
         return res;
     }
 
     /**
      * Finds and returns the requested method
      * 
      * @param c The class to search in.
      * @param methodName The method name
      * @param params An array of objects that contain the parameters for the method.
      * @return The requested Method
      * @throws NoSuchMethodException
      */
     public static Method findMethod (Class<?> c, String methodName, Object[] params)
                                                                                     throws NoSuchMethodException {
         Class<?>[] types = toClassArray(params);
         return findMethod(c, methodName, types);
     }
 
     private static boolean paramsEqual (Class<?>[] c1, Class<?>[] c2) {
         if (c1.length != c2.length) {
             return false;
         }
         for (int i = 0; i < c1.length; i++) {
             boolean similar = c1[i].isAssignableFrom(c2[i]) || c2[i].isAssignableFrom(c1[i]);
             if (!similar) {
                 return false;
             }
         }
 
         return true;
     }
 
     private static Class<?>[] toClassArray (Object ... params) {
         Class<?>[] types = new Class<?>[params.length];
         for (int i = 0; i < params.length; i++) {
        	//System.out.println(params[i]);
             types[i] = params[i].getClass();
             // Override Primitive Types
             if (types[i] == Integer.class) {
                 types[i] = int.class;
             }
             if (types[i] == Boolean.class) {
                 types[i] = boolean.class;
             }
             if(types[i] == Double.class) {
             	types[i] = double.class;
             }
         }
         return types;
     }
 
     private static Field getField (String property, Object object) {
         Class<?> curClass = object.getClass();
         do {
             try {
                 Field toSet = curClass.getDeclaredField(property);
                 return toSet;
             }
             catch (NoSuchFieldException e) {
                 curClass = curClass.getSuperclass();
             }
             catch (SecurityException e) {
                 curClass = null;
             }
         } while (curClass != null);
         return null;
     }
 
     private static boolean setValue (Field toSet, Object object, Object value) {
         boolean prevAccess = toSet.isAccessible();
         toSet.setAccessible(true);
         try {
             toSet.set(object, value);
             return true;
         }
         catch (IllegalArgumentException e) {
         }
         catch (IllegalAccessException e) {
         }
         toSet.setAccessible(prevAccess);
         return false;
     }
 
     private static Object getValue (Field toSet, Object object) {
         Object value = null;
         boolean prevAccess = toSet.isAccessible();
         toSet.setAccessible(true);
         try {
             value = toSet.get(object);
         }
         catch (IllegalArgumentException e) {
         }
         catch (IllegalAccessException e) {
         }
         toSet.setAccessible(prevAccess);
         return value;
     }
 
     /**
      * Sets the value of a field in an object using reflection.
      * Iterates through all the super classes to find a field that
      * matches.
      * 
      * @param property The name of the property to set.
      * @param object The object that the property should be set on.
      * @param value The value to set the value to.
      * @return Whether it was able to set the value or not.
      */
     public static <T> boolean setValue (String property, Object object, T value) {
         Field toSet = getField(property, object);
         return setValue(toSet, object, value);
     }
 
     /**
      * Changes the value of a property by a certain amount. This makes it easier to increase
      * or decrease values by a certain amount. <br />
      * 
      * Right now it supports only Integers and Doubles <br />
      * It basically replaces setValue with a getValue and a modification.
      * 
      * @param property The property to change
      * @param object The object to change the value of
      * @param value How much to change the value by.
      * @return Whether it was able to change the value.
      */
     public static <T extends Number> boolean changeValue (String property, Object object, T value) {
         Field toSet = getField(property, object);
         Number val = (Number) getValue(toSet, object);
 
         if (value instanceof Integer) {
             Integer intVal = val.intValue();
             intVal += (Integer) value;
             val = intVal;
         }
 
         if (value instanceof Double) {
             Double dVal = val.doubleValue();
             dVal += (Double) dVal;
             val = dVal;
         }
 
         return setValue(toSet, object, val);
     }
 
     /**
      * Returns the value of a property of a specific Object.
      * 
      * @param property The property name to get.
      * @param object The object to get the value from.
      * @return The value of the property.
      */
     public static <T> T getValue (String property, Object object) {
         Field toSet = getField(property, object);
         try {
             @SuppressWarnings("unchecked")
             T result = (T) getValue(toSet, object);
             return result;
         }
         catch (Exception e) {
             return null;
         }
     }
 
 }
