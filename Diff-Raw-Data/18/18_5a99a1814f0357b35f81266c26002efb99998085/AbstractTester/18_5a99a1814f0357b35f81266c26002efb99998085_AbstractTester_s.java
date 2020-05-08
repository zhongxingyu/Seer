 package org.codereligion.test.bean;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.codereligion.test.bean.creation.ObjectFactory;
 import org.codereligion.test.bean.exception.BeanTestException;
 import org.codereligion.test.bean.reflect.ReflectUtil;
 
 
 /**
  * Abstract tester which provides the basic functionality for all bean testers.
  * 
  * @author sgroebler
  * @since 11.08.2012
  */
 abstract class AbstractTester <T> {
 
 	private static final String NO_SETTER_ERROR	= 	"The given class '%s' does not provide any public setters, only properties " +
 													"which are setable through public setters can be verified to be included in " +
 													"the to be tested method.";
 	
 	/**
 	 * The {@link Class} of the bean to be tested.
 	 */
 	protected final Class<T> beanClass;
 	
 	/**
 	 * Property names which should be excluded from the hashCode and equals check.
 	 */
 	protected Set<String> excludedPropertyNames = new HashSet<String>();
 	
 	/**
 	 * Constructs a new instance for the given {@code beanClass}.
 	 * 
 	 * @param beanClass the {@link Class} to test
 	 * @throws NullPointerException when the given parameter is {@code null}
 	 * @throws IllegalArgumentException when the given {@code beanClass} can not be tested
 	 */
 	protected AbstractTester(final Class<T> beanClass) {
 		this(beanClass, Collections.<String>emptySet());
 	}
 	
 	/**
 	 * Constructs a new instance for the given {@code beanClass}.
 	 * 
 	 * @param beanClass the {@link Class} to test
 	 * @param excludedPropertyNames the names of the properties to exclude from the test
 	 * @throws NullPointerException when any of the given parameters are {@code null}
 	 * @throws IllegalArgumentException when the given {@code beanClass} can not be tested
 	 */
 	protected AbstractTester(final Class<T> beanClass, final Set<String> excludedPropertyNames) {
 		
 		if (beanClass == null) {
 			throw new NullPointerException("beanClass must not be null.");
 		}
 		
 		if (!ObjectFactory.isCreateable(beanClass)) {
 			throw new IllegalArgumentException("The given class: " + beanClass.getCanonicalName() + " is not supported.");
 		}
 		
 		final boolean hasNoSetableProperties = ReflectUtil.getSetableProperties(beanClass).isEmpty();
 		
 		if (hasNoSetableProperties) {
 			throw new IllegalArgumentException(String.format(NO_SETTER_ERROR, beanClass.getCanonicalName()));
 		}
 
 		if (excludedPropertyNames == null) {
 			throw new NullPointerException("excludedPropertyNames must not be null.");
 		}
 		
 		this.beanClass = beanClass;
 		this.excludedPropertyNames = Collections.unmodifiableSet(excludedPropertyNames);
 	}
 
 	/**
 	 * Tests if all not excluded properties are considered in the method this class is testing.
 	 */
 	protected abstract void testIntegrity();
 	
 	/**
 	 * Tests if the method this class is testing can handle null values it is properties.
 	 */
 	protected abstract void testNullSafety();
 	
 	/**
 	 * Sets the given {@code value} with the given {@code setter} to the given {@code object}.
 	 * 
 	 * @param object the object to set the value on
 	 * @param setter the {@link Method} to set the given {@code value} with
 	 * @param value the value to be set
 	 * @throws BeanTestException when setting the given {@code value} with the given {@code setter}
 	 * was not possible
 	 */
 	protected void setValue(final T object, final Method setter, final Object value) {
 		try {
 			setter.invoke(object, value);
 		} catch (final IllegalAccessException e) {
 			throw new BeanTestException("The method: " + setter + " is inaccessable, thus can not be used to set test values.", e);
 		} catch (final InvocationTargetException e) {
 			throw new BeanTestException("The method: " + setter + " threw an exception on setting test values.", e);
 		} catch (final IllegalArgumentException e) {
 			throw new BeanTestException("Failed to set '" + value + "' on setter: " + setter + ".", e);
 		}
 	}
 
 	/**
 	 * Convenience method to throw a formatted {@link AssertionError} if the given condition is not {@code false}.
	 * 
 	 * @param condition the boolean condition to be checked
 	 * @param message the message to be formatted in cases an {@link AssertionError} is thrown
 	 * @param messageArgs the arguments to be used in message formatting
 	 */
 	protected void assertFalse(final boolean condition, final String message, final Object... messageArgs) {
 		if (condition) {
			final String formattedMessage = String.format(message, messageArgs);
			throw new AssertionError(formattedMessage);
 		}
 	}
 
	/**
 	 * Convenience method to throw a formatted {@link AssertionError} if the given condition is not {@code true}.
 	 * 
 	 * @param condition the boolean condition to be checked
 	 * @param message the message to be formatted in cases an {@link AssertionError} is thrown
 	 * @param messageArgs the arguments to be used in message formatting
 	 */
 	protected void assertTrue(final boolean condition, final String message, final Object... messageArgs) {
 		if (!condition) {
			assertFalse(condition, message, messageArgs);
 		}
 	}
 }
