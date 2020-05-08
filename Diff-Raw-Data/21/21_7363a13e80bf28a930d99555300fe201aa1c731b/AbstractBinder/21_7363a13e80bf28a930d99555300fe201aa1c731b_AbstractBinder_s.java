 /**
  * Copyright (c) 2010 Ignasi Barrera
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package org.sjmvc.binding;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.apache.commons.lang.StringUtils;
 import org.sjmvc.error.Error;
 import org.sjmvc.error.ErrorType;
 import org.sjmvc.error.Errors;
 import org.sjmvc.util.ReflectionUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Base class for {@link Binder} implementations.
  * 
  * @author Ignasi Barrera
  * 
  * @see BindingResult
  * @see BindingError
  * @see RequestParameterBinder
  */
 public abstract class AbstractBinder<T, S> implements Binder<T, S>
 {
 	/** The logger. */
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(AbstractBinder.class);
 
 	/** The object target of the binding. */
 	protected T target;
 
 	/** The source if the binding. */
 	protected S source;
 
 	/** The binding errors. */
 	protected Errors errors;
 
 	/**
 	 * Creates the binder.
 	 * 
 	 * @param target The target of the binding.
 	 * @param source The source of the binding.
 	 */
 	public AbstractBinder(T target, S source)
 	{
 		super();
 		this.target = target;
 		this.source = source;
 		errors = new Errors();
 	}
 
 	@Override
 	public BindingResult<T> bind()
 	{
 		doBind();
 
 		BindingResult<T> result = new BindingResult<T>();
 		result.setTarget(target);
 		result.setErrors(errors);
 
 		return result;
 	}
 
 	/**
 	 * Executes the bind.
 	 * <p>
 	 * This method must be implemented by the {@link Binder} implementations to
 	 * perform the binding operation in the {@link #target} object, saving all
 	 * errors in the {@link #errors} object.
 	 */
 	protected abstract void doBind();
 
 	/**
 	 * Binds the given field to the given value in the target object.
 	 * 
 	 * @param currentObject The current object being processed.
 	 * @param name The name of the field to bind.
 	 * @param values The values to bind to the field.
 	 */
 	protected void bindField(Object currentObject, String name,
 			String... values)
 	{
 		String[] path = name.split("\\.");
 
 		try
 		{
 			if (path.length == 1)
 			{
 				LOGGER.trace("Binding simple property {} to {}", name,
 						currentObject.getClass().getName());
 
 				// Bind simple property
 				setValue(currentObject, path[0], values);
 			}
 			else
 			{
 				LOGGER.trace("Binding nested property {} to {}", name,
 						currentObject.getClass().getName());
 
 				// Recursively bind the nested values
 				String remainingPath = StringUtils.join(path, "", 1,
 						path.length);
 				Object nestedObject = ReflectionUtils.getProperty(
 						currentObject, path[0]);
 
 				// If nested object is null, create it
 				if (nestedObject == null)
 				{
 					LOGGER.trace("Nested property {} is null. Creating it.",
 							name);
 
 					Class<?> nestedType = ReflectionUtils.getFieldType(path[0],
 							currentObject.getClass());
 					nestedObject = nestedType.newInstance();
 
 					ReflectionUtils.setValue(currentObject, path[0],
 							nestedObject);
 				}
 
 				bindField(nestedObject, remainingPath, values);
 			}
 		}
 		catch (Exception ex)
 		{
 			LOGGER.debug("Could not bind property {} to {}", name,
 					currentObject.getClass().getName());
 
 			errors.add(new Error(ErrorType.BINDING, ex.getMessage()));
 		}
 	}
 
 	/**
 	 * Sets the given value to the given field.
 	 * 
 	 * @param currentObject The current object being processed.
 	 * @param name The name of the field.
 	 * @param values The values to set.
 	 * @throws BindingError If the value of the property cannot be set.
 	 */
 	protected void setValue(Object currentObject, String name, String values[])
 			throws BindingError
 	{
 		try
 		{
 			Field field = currentObject.getClass().getDeclaredField(name);
 			int modifiers = field.getModifiers();
 
 			if (!Modifier.isTransient(modifiers)
 					&& !Modifier.isStatic(modifiers))
 			{
 				// If property is a collection, iterate over the values
 				if (Collection.class.isAssignableFrom(field.getType()))
 				{
 					setCollectionValues(field, currentObject, name, values);
 				}
 				else if (field.getType().isArray())
 				{
 					setArrayValues(field, currentObject, name, values);
 				}
 				else
 				{
 					setSimpleValue(field, currentObject, name, values[0]);
 				}
 			}
 			else
 			{
 				LOGGER.debug("Property {} is static or transient "
 						+ "and binding will ignore it", name);
 			}
 		}
 		catch (Exception ex)
 		{
 			throw new BindingError("Could not bind property [" + name
 					+ "] of [" + currentObject.getClass().getName() + "]", ex);
 		}
 	}
 
 	/**
 	 * Set the value in a simple property.
 	 * 
 	 * @param field The field to set.
 	 * @param currentObject The object being processed.
 	 * @param name The name of the property.
	 * @param values The value to set.
 	 * @throws Exception If the value cannot be set in the property.
 	 */
 	protected void setSimpleValue(Field field, Object currentObject,
 			String name, String value) throws Exception
 	{
 		LOGGER.trace("Setting {} to {}", value, name);
 
 		// Value should be a single element array
 		ReflectionUtils.transformAndSet(currentObject, name, value);
 	}
 
 	/**
 	 * Set the values in a collection property.
 	 * 
 	 * @param field The field to set.
 	 * @param currentObject The object being processed.
 	 * @param name The name of the collection property.
 	 * @param values The values to set.
 	 * @throws Exception If the values cannot be set in the collection property.
 	 */
 	protected void setCollectionValues(Field field, Object currentObject,
 			String name, String values[]) throws Exception
 	{
 		LOGGER.trace("Setting [{}] to {} collection",
 				StringUtils.join(values, ", "), name);
 
 		// Get the type of the elements in the collection
 		Class<?> elementsType = ReflectionUtils.getFieldCollectionType(name,
 				currentObject.getClass());
 
 		// Get the collection and clear it
 		@SuppressWarnings("unchecked")
 		Collection<Object> col = (Collection<Object>) ReflectionUtils
 				.getProperty(currentObject, name);
 
 		if (col == null)
 		{
 			LOGGER.trace("Collection property {} is null. Creating it", name);
 
 			col = new ArrayList<Object>();
 		}
 
 		// Add the values to the collection
 		col.clear();
 
 		for (String currentValue : values)
 		{
 			col.add(ReflectionUtils.fromString(elementsType, currentValue));
 		}
 
 		// Save the collection in the object
 		ReflectionUtils.setValue(currentObject, name, col);
 	}
 
 	/**
 	 * Set the values in an array property.
 	 * 
 	 * @param field The field to set.
 	 * @param currentObject The object being processed.
 	 * @param name The name of the array property.
 	 * @param values The values to set.
 	 * @throws Exception If the values cannot be set in the array property.
 	 */
 	protected void setArrayValues(Field field, Object currentObject,
 			String name, String values[]) throws Exception
 	{
 		LOGGER.trace("Setting [{}] to {} array",
 				StringUtils.join(values, ", "), name);
 
 		Class<?> elementsType = field.getType().getComponentType();
 		Object array = Array.newInstance(elementsType, values.length);
 
 		for (int i = 0; i < values.length; i++)
 		{
 			Array.set(array, i,
 					ReflectionUtils.fromString(elementsType, values[i]));
 		}
 
 		// Save the array in the object
 		ReflectionUtils.setValue(currentObject, name, array);
 	}
 
 	@Override
 	public S getSource()
 	{
 		return source;
 	}
 
 	@Override
 	public T getTarget()
 	{
 		return target;
 	}
 
 }
