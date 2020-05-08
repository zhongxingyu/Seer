 /*
  * Copyright 2012 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package bingo.lang;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import bingo.lang.exceptions.CloneException;
 
 /**
  * null safe {@link Object} utility.
  */
 //From apache commons-lang3, under Apache License 2.0
 public class Objects {
 
 	protected Objects() {
 
 	}
 
 	// Null-safe equals/hashCode
 	//-----------------------------------------------------------------------
 	/**
 	 * <p>
 	 * Compares two objects for equality, where either one or both objects may be {@code null}.
 	 * </p>
 	 * 
 	 * <pre>
 	 * Objects.equals(null, null)                  = true
 	 * Objects.equals(null, "")                    = false
 	 * Objects.equals("", null)                    = false
 	 * Objects.equals("", "")                      = true
 	 * Objects.equals(Boolean.TRUE, null)          = false
 	 * Objects.equals(Boolean.TRUE, "true")        = false
 	 * Objects.equals(Boolean.TRUE, Boolean.TRUE)  = true
 	 * Objects.equals(Boolean.TRUE, Boolean.FALSE) = false
 	 * </pre>
 	 * 
 	 * @param object1 the first object, may be {@code null}
 	 * @param object2 the second object, may be {@code null}
 	 * @return {@code true} if the values of both objects are the same
 	 */
 	public static boolean equals(Object object1, Object object2) {
 		if (object1 == object2) {
 			return true;
 		}
 		if (object1 == null || object2 == null) {
 			return false;
 		}
 		return object1.equals(object2);
 	}
 
 	/**
 	 * <p>
 	 * Compares two objects for inequality, where either one or both objects may be {@code null}.
 	 * </p>
 	 * 
 	 * <pre>
 	 * Objects.notEqual(null, null)                  = false
 	 * Objects.notEqual(null, "")                    = true
 	 * Objects.notEqual("", null)                    = true
 	 * Objects.notEqual("", "")                      = false
 	 * Objects.notEqual(Boolean.TRUE, null)          = true
 	 * Objects.notEqual(Boolean.TRUE, "true")        = true
 	 * Objects.notEqual(Boolean.TRUE, Boolean.TRUE)  = false
 	 * Objects.notEqual(Boolean.TRUE, Boolean.FALSE) = true
 	 * </pre>
 	 * 
 	 * @param object1 the first object, may be {@code null}
 	 * @param object2 the second object, may be {@code null}
 	 * @return {@code false} if the values of both objects are the same
 	 */
 	public static boolean notEqual(Object object1, Object object2) {
 		return equals(object1, object2) == false;
 	}
 
 	/**
 	 * <p>
 	 * Gets the hash code of an object returning zero when the object is {@code null}.
 	 * </p>
 	 * 
 	 * <pre>
 	 * Objects.hashCode(null)   = 0
 	 * Objects.hashCode(obj)    = obj.hashCode()
 	 * </pre>
 	 * 
 	 * @param obj the object to obtain the hash code of, may be {@code null}
 	 * @return the hash code of the object, or zero if null
 	 */
 	public static int hashCode(Object obj) {
 		// hashCode(Object) retained for performance, as hash code is often critical
 		return obj == null ? 0 : obj.hashCode();
 	}
 
 	/**
 	 * <p>
 	 * Gets the hash code for multiple objects.
 	 * </p>
 	 * 
 	 * <p>
	 * This allows a hash code to be rapidly calculated for a number of objects. The hash code for a single object is
	 * the <em>not</em> same as {@link #hashCode(Object)}. The hash code for multiple objects is the same as that
 	 * calculated by an {@code ArrayList} containing the specified objects.
 	 * </p>
 	 * 
 	 * <pre>
 	 * Objects.hashCodeMulti()                 = 1
 	 * Objects.hashCodeMulti((Object[]) null)  = 1
 	 * Objects.hashCodeMulti(a)                = 31 + a.hashCode()
 	 * Objects.hashCodeMulti(a,b)              = (31 + a.hashCode()) * 31 + b.hashCode()
 	 * Objects.hashCodeMulti(a,b,c)            = ((31 + a.hashCode()) * 31 + b.hashCode()) * 31 + c.hashCode()
 	 * </pre>
 	 * 
 	 * @param objects the objects to obtain the hash code of, may be {@code null}
 	 * @return the hash code of the objects, or zero if null
 	 */
 	public static int hashCodeMulti(Object... objects) {
 		int hash = 1;
 		if (objects != null) {
 			for (Object object : objects) {
 				hash = hash * 31 + hashCode(object);
 			}
 		}
 		return hash;
 	}
 
 	// ToString
 	//-----------------------------------------------------------------------
 	/**
 	 * <p>
 	 * Gets the {@code toString} of an {@code Object} returning an empty string ("") if {@code null} input.
 	 * </p>
 	 * 
 	 * <pre>
 	 * Objects.toString(null)         = ""
 	 * Objects.toString("")           = ""
 	 * Objects.toString("bat")        = "bat"
 	 * Objects.toString(Boolean.TRUE) = "true"
 	 * </pre>
 	 * 
 	 * @see String#valueOf(Object)
 	 * 
 	 * @param obj the Object to {@code toString}, may be null
 	 * 
 	 * @return the passed in Object's toString, or empty("") if {@code null} input
 	 */
 	public static String toString(Object obj) {
 		return obj == null ? Strings.EMPTY : obj.toString();
 	}
 
     // Identity ToString
     //-----------------------------------------------------------------------	
 	/**
 	 * <p>
 	 * Gets the toString that would be produced by {@code Object} if a class did not override toString itself. {@code
	 * null} will return {@code null}.
 	 * </p>
 	 * 
 	 * <pre>
	 * Objects.identityToString(null)         = null
 	 * Objects.identityToString("")           = "java.lang.String@1e23"
 	 * Objects.identityToString(Boolean.TRUE) = "java.lang.Boolean@7fa"
 	 * </pre>
 	 * 
 	 * @param object the object to create a toString for, may be {@code null}
	 * @return the default toString text, or {@code null} if {@code null} passed in
 	 */
 	public static String identityToString(Object object) {
 		if (object == null) {
 			return Strings.EMPTY;
 		}
 		StringBuffer out = new StringBuffer();
 		identityToString(object, out);
 		return out.toString();
 	}
 
 	/**
 	 * <p>
 	 * Appends the toString that would be produced by {@code Object} if a class did not override toString itself.
 	 * {@code null} will throw a NullPointerException for either of the two parameters.
 	 * </p>
 	 * 
 	 * <pre>
 	 * Objects.identityToString(buf, "")            = buf.append("java.lang.String@1e23")
 	 * Objects.identityToString(buf, Boolean.TRUE)  = buf.append("java.lang.Boolean@7fa")
 	 * Objects.identityToString(buf, Boolean.TRUE)  = buf.append("java.lang.Boolean@7fa")
 	 * </pre>
 	 * 
 	 * @param object the object to create a toString for
 	 * @param out the buffer to append to
 	 */
 	public static void identityToString(Object object, StringBuffer out) {
 		if (object == null) {
 			throw new NullPointerException("Cannot get the toString of a null identity");
 		}
 		out.append(object.getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(object)));
 	}
 
 	// cloning
 	//-----------------------------------------------------------------------
 	/**
 	 * <p>
 	 * Clone an object.
 	 * </p>
 	 * 
 	 * @param <T> the type of the object
 	 * @param obj the object to clone, null returns null
 	 * @return the clone if the object implements {@link Cloneable} otherwise {@code null}
 	 * @throws CloneException if the object is cloneable and the clone operation fails
 	 */
 	public static <T> T clone(final T obj) {
 		if (obj instanceof Cloneable) {
 			final Object result;
 			if (obj.getClass().isArray()) {
 				final Class<?> componentType = obj.getClass().getComponentType();
 				if (!componentType.isPrimitive()) {
 					result = ((Object[]) obj).clone();
 				} else {
 					int length = Array.getLength(obj);
 					result = Array.newInstance(componentType, length);
 					while (length-- > 0) {
 						Array.set(result, length, Array.get(obj, length));
 					}
 				}
 			} else {
 				try {
 					final Method clone = obj.getClass().getMethod("clone");
 					result = clone.invoke(obj);
 				} catch (final NoSuchMethodException e) {
 					throw new CloneException("Cloneable type " + obj.getClass().getName() + " has no clone method", e);
 				} catch (final IllegalAccessException e) {
 					throw new CloneException("Cannot clone Cloneable type " + obj.getClass().getName(), e);
 				} catch (final InvocationTargetException e) {
 					throw new CloneException("Exception cloning Cloneable type " + obj.getClass().getName(), e.getCause());
 				}
 			}
 			@SuppressWarnings("unchecked")
 			final T checked = (T) result;
 			return checked;
 		}
 
 		return null;
 	}
 }
