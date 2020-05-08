 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.toxbee.sleepfighter.utils.reflect;
 
import com.google.common.collect.ObjectArrays;
import com.google.common.reflect.TypeToken;

 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import se.toxbee.sleepfighter.utils.string.StringUtils;
 
 /**
  * ReflectionUtil provides very basic reflection utilities.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Nov 5, 2013
  */
 public class ReflectionUtil {
 	/**
 	 * {@link Method#invoke(Object, Object...)}
 	 */
 	public static Object invoke( Method m, Object receiver, Object... args ) {
 		try {
 			return m.invoke( receiver, args );
 		} catch ( IllegalArgumentException e ) {
 			ROJava6Exception.reThrow( e );
 		} catch ( IllegalAccessException e ) {
 			ROJava6Exception.reThrow( e );
 		} catch ( InvocationTargetException e ) {
 			ROJava6Exception.reThrow( e );
 		}
 
 		return null;
 	}
 
 	/**
 	 * {@link Class#getDeclaredMethod(String, Class...)}
 	 */
 	public static Method declaredMethod( Class<?> clazz, String name, Object args ) {
 		return declaredMethod( clazz, name, getClasses( args ) );
 	}
 
 	/**
 	 * {@link Class#getDeclaredMethod(String, Class...)}
 	 */
 	public static Method method( Class<?> clazz, String name, Object args ) {
 		return method( clazz, name, getClasses( args ) );
 	}
 
 	/**
 	 * {@link Class#getDeclaredMethod(String, Class...)}
 	 */
 	public static Method declaredMethod( Class<?> clazz, String name, Class<?>... parameterTypes ) {
 		try {
 			return clazz.getDeclaredMethod( name, parameterTypes );
 		} catch ( NoSuchMethodException e ) {
 			ROJava6Exception.reThrow( e );
 			return null;
 		}
 	}
 
 	/**
 	 * {@link Class#getMethod(String, Class...)}
 	 */
 	public static Method method( Class<?> clazz, String name, Class<?>... parameterTypes ) {
 		try {
 			return clazz.getMethod( name, parameterTypes );
 		} catch ( NoSuchMethodException e ) {
 			ROJava6Exception.reThrow( e );
 			return null;
 		}
 	}
 
 	/**
 	 * Creates a generic array of size with component type U given a generic object o.<br/>
 	 * Only works if o actually has a generic type U.
 	 *
 	 * @param o the generic object.
	 * @param size the size of array.
	 * @return the array.
 	 */
 	public static <U> U[] genericArray( Object o, int size ) {
 		Class<U> type = genericType( o );
 		return makeArray( type, size );
 	}
 
 	/**
 	 * Creates a generic array of size with with component type the same as in arr.<br/>
 	 * Only works if arr's components actually has a generic type U.
 	 *
 	 * @param arr the array.
	 * @param size the size of array.
	 * @return the array.
 	 */
 	public static <U> U[] genericArray( U[] arr, int size ) {
 		return makeArray( arrayClass( arr ), size );
 	}
 
 	/**
 	 * Returns the generic type of object o.<br/>
 	 * Only works if o actually has a generic type U.
 	 *
 	 * @param o the generic object.
 	 * @return the {@link Class} of U.
 	 */
 	@SuppressWarnings( "unchecked" )
 	public static <U> Class<U> genericType( Object o ) {
 		@SuppressWarnings( "serial" )
 		TypeToken<U> t = new TypeToken<U>( o.getClass() ) {};
 		return (Class<U>) t.getRawType();
 	}
 
 	/**
 	 * Returns a nested class/enum/interface from containing type as a subtype of target.
 	 *
 	 * @param containing the containing class/enum/interface.
 	 * @param target the target subtype.
 	 * @return the {@link Class} of nested type, or null if not found.
 	 */
 	public static <U> Class<? extends U> getNested( Class<?> containing, Class<U> target ) {
 		for ( Class<?> clazz : containing.getDeclaredClasses() ) {
 			Class<? extends U> result = asSubclass( clazz, target );
 			if ( result != null ) {
 				return result;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Constructs a new instance of type U given a {@link Class} & a set of parameters.
 	 *
	 * @param clazz the {@link Constructor}
 	 * @param params the parameters to pass to constructor.
 	 * @return the new instance.
 	 */
 	public static <U> U newInstance( Class<? extends U> clazz, Object... params ) {
 		return newInstance( getCtor( clazz, getClasses( params ) ), params );
 	}
 
 	/**
 	 * Constructs a new instance of type U given a constructor & a set of parameters.
 	 *
 	 * @param ctor the {@link Constructor}
 	 * @param params the parameters to pass to constructor.
 	 * @return the new instance.
 	 */
 	public static <U> U newInstance( Constructor<? extends U> ctor, Object... params ) {
 		try {
 			return ctor.newInstance( params );
 		} catch ( InstantiationException e ) {
 			ROJava6Exception.reThrow( e );
 		} catch( IllegalAccessException e ) {
 			ROJava6Exception.reThrow( e );
 		} catch ( IllegalArgumentException e ) {
 			ROJava6Exception.reThrow( e );
 		} catch ( InvocationTargetException e ) {
 			ROJava6Exception.reThrow( e );
 		}
 
 		// satisfy compiler.
 		return null;
 	}
 
 	/**
 	 * Returns a {@link Constructor} for clazz with paramTypes.
 	 *
 	 * @param clazz the {@link Class} to get constructor from.
 	 * @param paramTypes the types of the parameters.
 	 * @return the {@link Constructor}
 	 * @throws ROJava6Exception when {@link NoSuchMethodException} occurs.
 	 */
 	public static <U> Constructor<? extends U> getCtor( Class<? extends U> clazz, Class<?>... paramTypes ) {
 		try {
 			return clazz.getConstructor( paramTypes );
 		} catch ( NoSuchMethodException e ) {
 			ROJava6Exception.reThrow( e );
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the {@link Class} objects of objs.
 	 *
 	 * @param objs an array of objects.
 	 * @return the classes.
 	 */
 	public static Class<?>[] getClasses( Object... objs ) {
 		int len = objs.length;
 
 		Class<?>[] types = new Class<?>[len];
 		for ( int i = 0; i < len; ++i ) {
 			types[i] = objs[i].getClass();
 		}
 
 		return types;
 	}
 
 	/**
 	 * Creates an array of type T of size.
 	 *
 	 * @param clazz the Class object of type T.
 	 * @param size the size of array.
 	 * @return the array.
 	 */
 	public static <T> T[] makeArray( Class<T> clazz, int size ) {
 		return ObjectArrays.newArray( clazz, size );
 	}
 
 	/**
 	 * Returns the Class object for an element of a generic array.
 	 *
 	 * @param arr the array.
 	 * @return the Class object.
 	 */
 	@SuppressWarnings( "unchecked" )
 	public static <T> Class<T> arrayClass( T[] arr ) {
 		return (Class<T>) arr.getClass().getComponentType();
 	}
 
 	/**
 	 * Returns a {@link Class} for fully qualified class with name as a subclass of target.
 	 *
 	 * @param name the fully qualified name of class.
 	 * @param target the target class/interface.
 	 * @return the resulting {@link Class}.
	 * @throws ROJava6Exception when there's no class with name.
 	 */
 	public static <U> Class<? extends U> classForName( String name, Class<U> target ) {
 		Class<?> dirty = null;
 		try {
 			dirty = Class.forName( name );
 		} catch ( ClassNotFoundException e ) {
 			ROJava6Exception.reThrow( e );
 		}
 		Class<? extends U> clazz = dirty.asSubclass( target );
 		return clazz;
 	}
 
 	/**
 	 * {@code Class} to represent a subclass of the given class.
 	 * If successful, this {@code Class} is returned; otherwise null is returned.
 	 *
 	 * @return the {@code Class} or null.
 	 */
 	@SuppressWarnings( "unchecked" )
 	public static <U> Class<? extends U> asSubclass( Class<?> from, Class<U> target ) {
 		return target.isAssignableFrom( from ) ? (Class<U>) from : null;
 	}
 
 	/**
 	 * Returns the fully qualified package name of a package that is descendant of a package with clazz<br/>
 	 * If descendant is null, the package name of clazz is returned.
 	 *
 	 * @param clazz the clazz of the parent package.
 	 * @param descendant the descendant package.
 	 * @return the package name.
 	 */
 	public static String packageDecendantName( Class<?> clazz, String descendant ) {
 		return StringUtils.QUALIFIER_JOINER.join( clazz.getPackage().getName(), descendant );
 	}
 
 	/**
 	 * Returns the fully qualified package name of a package that is descendant of a package with clazz<br/>
 	 * If descendant is null, the package name of clazz is returned.
 	 *
 	 * @param o the object to get Class from (the Class of the parent package).
 	 * @param descendant the descendant package.
 	 * @return the package name.
 	 */
 	public static String packageDecendantName( Object o, String descendant ) {
 		return packageDecendantName( o.getClass(), descendant );
 	}
 }
