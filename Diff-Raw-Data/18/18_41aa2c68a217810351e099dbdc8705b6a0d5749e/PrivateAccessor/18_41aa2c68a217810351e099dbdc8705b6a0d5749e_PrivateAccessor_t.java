 package com.hexcore.cas.test;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.TreeMap;
 
 import junit.framework.Assert;
 
 /**	Utility Class for easy access to private class data.
  * 	----NOTE: FOR USE FOR JUNIT TESTING ONLY!-----------
  * 
 * @author Karl Zöller
  *
  */
 public class PrivateAccessor
 {
 	private Object						referencedObject;
 	private Field[] 					fields;
 	private Method[]					methods;
 	private TreeMap<String, Field>		fieldMap;
 	private TreeMap<String, Method>		methodMap;
 	
 	public PrivateAccessor(Object o)
 	{
 		Assert.assertNotNull(o);
 		fields = o.getClass().getDeclaredFields();
 		methods = o.getClass().getDeclaredMethods();
 		fieldMap = new TreeMap<String, Field>();
 		methodMap=new TreeMap<String, Method>();
 		referencedObject = o;
 		
 		//Map fields and methods by name for easy access
 		for(int f = 0; f < fields.length; f++)
 		{
 			Field current = fields[f];
 			current.setAccessible(true);
 			fieldMap.put(current.getName(), current);
 		}
 		
 		for(int m = 0; m < methods.length; m++)
 		{
 			Method current = methods[m];
 			current.setAccessible(true);
 			methodMap.put(current.getName(), current);
 		}
 	}
 	
 	public Object getFieldValue(String fieldName)
 	{
 		try
 		{
 			return fieldMap.get(fieldName).get(referencedObject);
 		} catch (IllegalArgumentException e)
 		{
 			e.printStackTrace();
 		} catch (IllegalAccessException e)
 		{
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public void setFieldValue(String fieldName, Object val)
 	{
 		try
 		{
 			fieldMap.get(fieldName).set(referencedObject, val);
 		}
 		catch (IllegalArgumentException e)
 		{
 			e.printStackTrace();
 		}
 		catch (IllegalAccessException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public Object invokeMethod(String methodName, Object... params)
 	{
 		Assert.assertTrue(methodMap.containsKey(methodName));
 		
 		try
 		{
 			return methodMap.get(methodName).invoke(referencedObject, params);
 		} catch (IllegalArgumentException e)
 		{
 			e.printStackTrace();
 		} catch (IllegalAccessException e)
 		{
 			e.printStackTrace();
 		} catch (InvocationTargetException e)
 		{
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
