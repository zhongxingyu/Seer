 package ecologylab.generic;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 
 /**
  * Utility routines for working with reflection.
  *
  * @author andruid
  */
 public class ReflectionTools extends Debug
 {
 	
 /**
  * Get the Field object with name fieldName, in thatClass.
  * 
  * @param thatClass
  * @param fieldName
  * 
  * @return	The Field object in thatClass, or null if there is none accessible.
  */
    public static Field getField(Class thatClass, String fieldName)
    {
 	   Field	result	= null;
 	   try
 	   {
 		   result		= thatClass.getField(fieldName);
 	   } catch (SecurityException e)
 	   {
 	   } catch (NoSuchFieldException e)
 	   {
 	   }	
 	   return result;
    }
 
    /**
     * Get the Field object with name fieldName, in thatClass.
     * 
     * @param thatClass
     * @param fieldName
     * 
     * @return	The Field object in thatClass, or null if there is none accessible.
     */
       public static Field getDeclaredField(Class thatClass, String fieldName)
       {
    	   Field	result	= null;
    	   try
    	   {
    		   result		= thatClass.getDeclaredField(fieldName);
    	   } catch (SecurityException e)
    	   {
    	   } catch (NoSuchFieldException e)
    	   {
    	   }	
    	   return result;
       }
    public static final Object	BAD_ACCESS	= new Object();
    
    /**
     * Return the value of the Field in the Object, or BAD_ACCESS if it can't be accessed.
     * 
     * @param that
     * @param field
     * @return
     */
    public static Object getFieldValue(Object that, Field field)
    {
 	   Object result	= null;
 	   try
 	   {
 		   result		= field.get(that);
 	   } catch (IllegalArgumentException e)
 	   {
 		   result		= BAD_ACCESS;
 		   e.printStackTrace();
 	   } catch (IllegalAccessException e)
 	   {
 		   result		= BAD_ACCESS;
 		   e.printStackTrace();
 	   }
 	   return result;
    }
    
    /**
     * Set a reference type Field to a value.
     * 
     * @param that		Object that the field is in.
     * @param field		Reference type field within that object.
     * @param value		Value to set the reference field to.
     * 
     * @return			true if the set succeeds.
     */
    public static boolean setFieldValue(Object that, Field field, Object value)
    {
 	   boolean result	= false;
 	   try
 	   {
 		   field.set(that, value);
 		   result		= true;
 	   } catch (IllegalArgumentException e)
 	   {
 		   e.printStackTrace();
 	   } catch (IllegalAccessException e)
 	   {
 		   e.printStackTrace();
 	   }
 	   return true;
    }
 /**
  * Wraps the no argument getInstance() method.
  * Checks to see if the class object passed in is null, or if
  * any exceptions are thrown by newInstance().
  * 
  * @param thatClass
  * @return	An instance of an object of the specified class, or null if the Class object was null or
  * an InstantiationException or IllegalAccessException was thrown in the attempt to instantiate.
  */
   	public static<T> T getInstance(Class<T> thatClass)
   	{
   		T result		= null;
   		if (thatClass != null)
   		{
   			try
   			{
   				result        	= thatClass.newInstance();
   			} catch (InstantiationException e)
   			{
   				e.printStackTrace();
   			} catch (IllegalAccessException e)
   			{
   				e.printStackTrace();
   			}
   		}
   		return result;
   	}
 
   	/**
   	 * Wraps the no argument getInstance() method.
   	 * Checks to see if the class object passed in is null, or if
   	 * any exceptions are thrown by newInstance().
   	 * 
   	 * @param thatClass
   	 * @return	An instance of an object of the specified class, or null if the Class object was null or
   	 * an InstantiationException or IllegalAccessException was thrown in the attempt to instantiate.
   	 */
   	public static<T> T getInstance(Class<T> thatClass, Class[] parameterTypes, Object[] args)
   	{
   		T result				= null;
   		if (thatClass != null)
   		{
   			try
   			{
   				Constructor<T> constructor	= thatClass.getDeclaredConstructor(parameterTypes);
   				if (constructor != null)
   					result		 		= constructor.newInstance(args);
 
   			} catch (SecurityException e1)
   			{
   				e1.printStackTrace();
   			} catch (NoSuchMethodException e1)
   			{
   				e1.printStackTrace();
   			} catch (IllegalArgumentException e)
   			{
   				e.printStackTrace();
   			} catch (InstantiationException e)
   			{
   				e.printStackTrace();
   			} catch (IllegalAccessException e)
   			{
   				e.printStackTrace();
   			} catch (InvocationTargetException e)
   			{
   				e.printStackTrace();
   			}
   		}
   		return result;
   	}
 
   	/**
   	 * Find a Method object if there is one in the context class, or return null if not.
   	 * 
   	 * @param context	Class to find the Method in.
   	 * @param name		Name of the method.
   	 * @param types		Array of Class objects indicating parameter types.
   	 * 
   	 * @return			The associated Method object, or null if non is accessible.	
   	 */
   	public static Method getMethod(Class context, String name, Class[] types)
   	{
   		Method result	= null;
   		try
 		{
 			result		= context.getMethod(name, types);
 		} catch (SecurityException e)
 		{
 		} catch (NoSuchMethodException e)
 		{
 		}
   		return result;
   	}
   	/**
   	 * See if the Field has the annotation in its declaration.
   	 * 
   	 * @param field
   	 * @param annotationClass
   	 * @return
   	 */  	
   	public static boolean isAnnotationPresent(Field field, Class annotationClass)
   	{
   		return field.isAnnotationPresent(annotationClass);
   	}
 
 	/**
 	 * Get the parameterized type tokens that the generic Field was declared with.
 	 * 
 	 * @param reflectType
 	 * @return
 	 */
 	public static Type[] getParameterizedTypeTokens(Field field)
 	{
 		Type[] result				= null;
 		Type reflectType			= field.getGenericType();
 		if (reflectType instanceof ParameterizedType)
 		{		
 			ParameterizedType pType	= (ParameterizedType) reflectType;
 			result	= pType.getActualTypeArguments();
 		}
 		return result;
 	}
 }
