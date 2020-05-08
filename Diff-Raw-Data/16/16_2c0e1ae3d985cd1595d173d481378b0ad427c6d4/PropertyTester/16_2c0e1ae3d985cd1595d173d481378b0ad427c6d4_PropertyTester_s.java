 /*
  * Copyright 2011 Ritz, Bruno <bruno.ritz@gmail.com>
  *
  * This file is part of S-Plan.
  *
  * S-Plan is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation, either version 3 of the License, or any later version.
  *
  * S-Plan is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
  * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with S-Plan. If not, see
  * http://www.gnu.org/licenses/.
  */
 package org.splan.testing;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import org.splan.utils.validation.NotNull;
 
 /**
  * A helper class to simplify testing getters and setters of properties.
  * <p>
  *
  * Objects of this class are not thread safe.
  *
  * @author Ritz, Bruno &lt;bruno.ritz@gmail.com&gt;
  */
 public class PropertyTester
 {
 	private static final String PFX_GETTER = "get";
 	private static final String PFX_BOOLGETTER = "is";
 	private static final String PFX_SETTER = "set";
 
 	private final Object target;
 
 	/**
 	 * Creates a new property tester.
 	 *
 	 * @param target
 	 *     The object to test
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>target</code> is <code>null</code>
 	 */
 	public PropertyTester(@NotNull Object target)
 		throws IllegalArgumentException
 	{
 		this.target = target;
 	}
 
 	/**
 	 * Indicates if <code>right</code> can be converted to <code>left</code>.
 	 *
 	 * @param left
 	 *     The conversion target
 	 * @param right
 	 *     The class to test
 	 *
 	 * @return
 	 *     If <code>right</code> is an instance of <code>left</code>
 	 */
 	private boolean isAssignableFrom(Class<?> left, Class<?> right)
 	{
 		boolean retval;
 
 		if (left.isPrimitive())
 		{
 			retval = Integer.class.isAssignableFrom(right)
 				|| Float.class.isAssignableFrom(right)
 				|| Double.class.isAssignableFrom(right)
 				|| Byte.class.isAssignableFrom(right)
 				|| Long.class.isAssignableFrom(right)
 				|| Short.class.isAssignableFrom(right)
 				|| Boolean.class.isAssignableFrom(right)
				|| Character.class.isAssignableFrom(right);
 		}
 		else
 		{
 			retval = left.isAssignableFrom(right);
 		}
 
 		return retval;
 	}
 
 	/**
 	 * Returns the getter method for given property. If no suitable getter can be found, this method will return
 	 * <code>null</code>.
 	 *
 	 * @param type
 	 *     The data type of the property
 	 * @param property
 	 *     The name of the property
 	 *
 	 * @return
 	 *     The getter method of the property or <code>null</code>
 	 */
 	private Method getGetter(Class<?> type, String property)
 	{
 		Method retval = null;
 
 		try
 		{
 			String methodName;
 			Method method;
 
 			if (type == Boolean.class)
 			{
 				methodName = PFX_BOOLGETTER;
 			}
 			else
 			{
 				methodName = PFX_GETTER;
 			}
 
 			methodName += property.substring(0, 1).toUpperCase() + property.substring(1);
 			method = this.target.getClass().getMethod(methodName, new Class[0]);
 
 			if (this.isAssignableFrom(method.getReturnType(), type))
 			{
 				retval = method;
 			}
 		}
 		catch (NoSuchMethodException e)
 		{
 		}
 
 		return retval;
 	}
 
 	/**
 	 * Returns the setter method for given property. If no suitable setter can be found, this method will return
 	 * <code>null</code>.
 	 *
 	 * @param type
 	 *     The data type of the property
 	 * @param property
 	 *     The name of the property
 	 *
 	 * @return
 	 *     The setter method of the property or <code>null</code>
 	 */
 	private Method getSetter(Class<?> type, String property)
 	{
 		String methodName = PFX_SETTER + property.substring(0, 1).toUpperCase() + property.substring(1);
 		Method[] methods = this.target.getClass().getMethods();
 		Method retval = null;
 
 		for (int i = 0; i < methods.length; i++)
 		{
 			if (methods[i].getName().equals(methodName))
 			{
 				Class[] params = methods[i].getParameterTypes();
 
 				if ((params.length == 1) && ((type == null) || this.isAssignableFrom(params[0], type)))
 				{
 					retval = methods[i];
 					break;
 				}
 			}
 		}
 
 		return retval;
 	}
 
 	/**
 	 * Tests a read/write property. The following aspects are tested:
 	 * <ul>
 	 *     <li>
 	 *         Verify that the getter return the same value as previously assigned to the property using the setter
 	 *     </li>
 	 *     <li>Verify that a no-args getter method exists for the property and that it returns the correct type</li>
 	 *     <li>
 	 *         Verify that a setter method exists for the property and that it takes one argument of the correct type
 	 *     </li>
 	 * </ul>
 	 *
 	 * @param <T>
 	 *     The data type of the property
 	 * @param property
 	 *     The name of the property to test
 	 * @param type
 	 *     The expected data type of the property
 	 * @param arg
 	 *     The argument to pass to the setter
 	 *
 	 * @throws ValidationException
 	 *     If the property could not be verified
 	 */
 	public <T> void testProperty(String property, Class<T> type, T arg)
 		throws ValidationException
 	{
 		Method getter = this.getGetter(type, property);
 		Method setter = this.getSetter(type, property);
 
 		if (getter == null)
 		{
 			throw new ValidationException("No getter defined for property '" + property + "'");
 		}
 
 		if (setter == null)
 		{
 			throw new ValidationException("No setter defined for property '" + property + "'");
 		}
 
 		try
 		{
 			Object value;
 
 			setter.invoke(this.target, arg);
 			value = getter.invoke(this.target);
 
 			if (((arg == null) && (value != null))
 				|| (arg != value) && !arg.equals(value))
 			{
 				throw new ValidationException("Getter for property '" + property + "' returned a different value");
 			}
 		}
 		catch (IllegalAccessException e)
 		{
 			throw new ValidationException("Failed to validate property '" + property + "': " + e.getMessage());
 		}
 		catch (InvocationTargetException e)
 		{
 			throw new ValidationException("Failed to validate property '" + property + "': " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Tests a read/write property. The following aspects are tested:
 	 * <ul>
 	 *     <li>
 	 *         Verify that the getter return the same value as previously assigned to the property using the setter
 	 *     </li>
 	 *     <li>Verify that a no-args getter method exists for the property and that it returns the correct type</li>
 	 *     <li>
 	 *         Verify that a setter method exists for the property and that it takes one argument of the correct type
 	 *     </li>
 	 *     <li>
 	 *         Verify that arguments marked as invalid are rejected using the proper exception
 	 *         (<code>IllegalArgumentException</code>)
 	 *     </li>
 	 * </ul>
 	 *
 	 * @param <T>
 	 *     The data type of the property
 	 * @param property
 	 *     The name of the property to test
 	 * @param type
 	 *     The expected data type of the property
 	 * @param arg
 	 *     The argument to pass to the setter
 	 *
 	 * @throws ValidationException
 	 *     If the property could not be verified
 	 */
 	public <T> void testInvalidProperty(String property, Class<T> type, T arg)
 		throws ValidationException
 	{
 		Method getter = this.getGetter(type, property);
 		Method setter = this.getSetter(type, property);
 
 		if (getter == null)
 		{
 			throw new ValidationException("No getter defined for property '" + property + "'");
 		}
 
 		if (setter == null)
 		{
 			throw new ValidationException("No setter defined for property '" + property + "'");
 		}
 
 		try
 		{
 			setter.invoke(this.target, arg);
 
 			throw new ValidationException("Invalid value not rejected for property '" + property + "'");
 		}
 		catch (IllegalAccessException e)
 		{
 			throw new ValidationException("Failed to validate property '" + property + "': " + e.getMessage());
 		}
 		catch (InvocationTargetException e)
 		{
 			if (!(e.getCause() instanceof IllegalArgumentException))
 			{
 				throw new ValidationException("Invalid value for property '" + property + "' not properly rejected");
 			}
 		}
 	}
 
 	/**
 	 * Tests a read only property. The following aspects are tested:
 	 * <ul>
 	 *     <li>
 	 *         Verify that the getter return the same value as previously assigned to the property using the setter
 	 *     </li>
 	 *     <li>Verify that a getter method exists for the property</li>
 	 *     <li>Verify that no setter method exists for the property</li>
 	 * </ul>
 	 *
 	 * @param <T>
 	 *     The data type of the property
 	 * @param property
 	 *     The name of the property to test
 	 * @param type
 	 *     The expected data type of the property
 	 * @param arg
 	 *     The argument to pass to the setter
 	 *
 	 * @throws ValidationException
 	 *     If the property could not be verified
 	 */
 	public <T> void testReadonlyProperty(String property, Class<T> type, T arg)
 		throws ValidationException
 	{
 		Method getter = this.getGetter(type, property);
 		Method setter = this.getSetter(type, property);
 
 		if (getter == null)
 		{
 			throw new ValidationException("No getter defined for property '" + property + "'");
 		}
 
 		if (setter != null)
 		{
 			throw new ValidationException("Setter method defined for property '" + property + "'");
 		}
 	}
 }
