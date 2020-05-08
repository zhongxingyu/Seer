 package com.gramercysoftware.utils;
 
 import java.lang.reflect.ParameterizedType;
 import java.text.MessageFormat;
 
 /**
  * <p>This is a class designed to allow a user to interrogate a generic subclass to discover
  * the actual type of the generic.</p>
  * <p>If, at any point, this fails then a GenericsUtilsException will be thrown with the cause
  * nested and a meaningful message included.</p> 
  * 
  * @author David Harcombe <david.harcombe@gmail.com>
  */
 public class GenericsUtils {
	private GenericsUtils() {
	}
	
 	/**
 	 * <p>Returns the Class of the generic defined. 
 	 * For example, if you have classes thus:</p>
 	 * <pre>
 	 * public abstract class Foo&lt;T&gt; {
 	 *     public T doSomething();
 	 * }
 	 * 
 	 * public class Bar extends Foo&lt;String&gt; {
 	 *     public String doSomething() {
 	 *         return "Did something";
 	 *     }
 	 * }
 	 * </pre>
 	 * then:
 	 * <pre>
 	 * GenericsUtils.createT(Bar.class)
 	 * </pre>
 	 * <p>will return <code>new String()</code></p>
 	 * 
 	 * <p>On the other hand, if you have a class</p>
 	 * <pre>
 	 * public class Foo&lt;String&gt; {
 	 *     public String doSomething() {
 	 *         return "Did something";
 	 *     }
 	 * }
 	 * </pre>
 	 * <p>then this method with throw a <code>GenericsUtilsException</code>, as <code>Foo</code> does not
 	 * extend a generic parent class.</p>
 	
 	 * @param genericSubclass
 	 * @return
 	 * @throws GenericsUtilsException
 	 */
 	public static <T> T createT(Class<?> genericSubclass) throws GenericsUtilsException {
 		try {
 			return createT(genericSubclass.newInstance());
 		} catch (InstantiationException e) {
 			throw new GenericsUtilsException(MessageFormat.format("{0} cannot be instantiated; does it have a default constructor?", genericSubclass.getClass().getName()), e);
 		} catch (IllegalAccessException e) {
 			throw new GenericsUtilsException(MessageFormat.format("{0} cannot be instantiated; does it have a public default constructor?", genericSubclass.getClass().getName()), e);
 		}
 	}
 
 	/**
 	 * <p>Returns the Class of the generic defined. 
 	 * For example, if you have classes thus:</p>
 	 * <pre>
 	 * public abstract class Foo&lt;T&gt; {
 	 *     public T doSomething();
 	 * }
 	 * 
 	 * public class Bar extends Foo&lt;String&gt; {
 	 *     public String doSomething() {
 	 *         return "Did something";
 	 *     }
 	 * }
 	 * </pre>
 	 * then:
 	 * <pre>
 	 * GenericsUtils.createT(Bar.class)
 	 * </pre>
 	 * <p>will return <code>String.class</code></p>
 	 * 
 	 * <p>On the other hand, if you have a class</p>
 	 * <pre>
 	 * public class Foo&lt;String&gt; {
 	 *     public String doSomething() {
 	 *         return "Did something";
 	 *     }
 	 * }
 	 * </pre>
 	 * <p>then this method with throw a <code>GenericsUtilsException</code>, as <code>Foo</code> does not
 	 * extend a generic parent class.</p>
 	 * 
 	 * @param genericSubclass
 	 * @return
 	 * @throws GenericsUtilsException
 	 */
 	public static <T> Class<T> classOfT(Class<?> genericSubclass) throws GenericsUtilsException {
 		try {
 			return classOfT(genericSubclass.newInstance());
 		} catch (InstantiationException e) {
 			throw new GenericsUtilsException(MessageFormat.format("{0} cannot be instantiated; does it have a default constructor?", genericSubclass.getClass().getName()), e);
 		} catch (IllegalAccessException e) {
 			throw new GenericsUtilsException(MessageFormat.format("{0} cannot be instantiated; does it have a public default constructor?", genericSubclass.getClass().getName()), e);
 		}
 	}
 
 	/**
 	 * @see GenericsUtils.classOfT(Class<?> genericSubclass)
 	 * 
 	 * @param genericSubclass
 	 * @return
 	 * @throws GenericsUtilsException
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T, H> T createT(H genericSubclass) throws GenericsUtilsException {
 		try {
 			return (T) classOfT(genericSubclass).newInstance();
 		} catch (InstantiationException e) {
 			throw new GenericsUtilsException(MessageFormat.format("{0} cannot be instantiated; does it have a default constructor?", genericSubclass.getClass().getName()), e);
 		} catch (IllegalAccessException e) {
 			throw new GenericsUtilsException(MessageFormat.format("{0} cannot be instantiated; does it have a public default constructor?", genericSubclass.getClass().getName()), e);
 		}
 	}
 
 	/**
 	 * @see GenericsUtils.createT(Class<?> genericSubclass)
 	 * 
 	 * @param genericSubclass
 	 * @return
 	 * @throws GenericsUtilsException
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T, H> Class<T> classOfT(H genericSubclass) throws GenericsUtilsException {
 		try {
 			return ((Class<T>) ((ParameterizedType) genericSubclass.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
 		} catch (ClassCastException e) {
 			throw new GenericsUtilsException(MessageFormat.format("{0} is not a subclass of a generic superclass.", genericSubclass.getClass().getName()), e);
 		}
 	}
 }
