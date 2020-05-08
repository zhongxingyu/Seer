 /*
  * Copyright 2012 Robert Philipp
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.microtitan.diffusive.utils;
 
 import java.lang.reflect.Array;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 public class ReflectionUtils {
 	
 	private static final Logger LOGGER = Logger.getLogger( ReflectionUtils.class );
 	
 	private static Map< String, Class< ? > > TYPE_MAP = new HashMap<>();
 	static {
 		TYPE_MAP.put( void.class.getName(), void.class );
 		TYPE_MAP.put( int.class.getName(), int.class );
 		TYPE_MAP.put( short.class.getName(), short.class );
 		TYPE_MAP.put( long.class.getName(), long.class );
 		TYPE_MAP.put( double.class.getName(), double.class );
 		TYPE_MAP.put( float.class.getName(), float.class );
 		TYPE_MAP.put( byte.class.getName(), byte.class );
 		TYPE_MAP.put( char.class.getName(), char.class );
 		TYPE_MAP.put( boolean.class.getName(), boolean.class );
 	}
 	
 	private static Map< Class< ? >, Class< ? > > WRAP_MAP = new HashMap<>();
 	static {
 		WRAP_MAP.put( void.class, Void.class );
 		WRAP_MAP.put( int.class, Integer.class );
 		WRAP_MAP.put( short.class, Short.class );
 		WRAP_MAP.put( long.class, Long.class );
 		WRAP_MAP.put( double.class, Double.class );
 		WRAP_MAP.put( float.class, Float.class );
 		WRAP_MAP.put( byte.class, Byte.class );
 		WRAP_MAP.put( char.class, Character.class );
 		WRAP_MAP.put( boolean.class, Boolean.class );
 	}
 	
 	// invert the wrapped map
 	private static Map< Class< ? >, Class< ? > > UNWRAP_MAP = new HashMap<>();
 	static {
 		for( Map.Entry< Class< ? >, Class< ? > > entry : WRAP_MAP.entrySet() )
 		{
 			UNWRAP_MAP.put( entry.getValue(), entry.getKey() );
 		}
 	}
 	
 	private static Map< String, Class< ? > > ENCODED_TYPE_MAP = new HashMap<>();
 	static {
 		ENCODED_TYPE_MAP.put( "I", int.class );
 		ENCODED_TYPE_MAP.put( "S", short.class );
 		ENCODED_TYPE_MAP.put( "J", long.class );
 		ENCODED_TYPE_MAP.put( "D", double.class );
 		ENCODED_TYPE_MAP.put( "F", float.class );
 		ENCODED_TYPE_MAP.put( "B", byte.class );
 		ENCODED_TYPE_MAP.put( "C", char.class );
 		ENCODED_TYPE_MAP.put( "Z", boolean.class );
 	}
 
 	/**
 	 * Utility method that returns the {@link Class} object for the specified class name.
 	 * @param className The name of the {@link Class} for which to return the {@link Class} object.
 	 * @return the {@link Class} object for the specified class name.
 	 * @throws IllegalArgumentException
 	 */
	public static Class< ? > getClazz( final String className, final boolean...isLoggingEnabled )
 	{
 		// check first if the class is a primitive
 		Class< ? > clazz = TYPE_MAP.get( className );
 		
 		try
 		{
 			if( clazz == null )
 			{
 				final int arrayDimension = className.lastIndexOf( "[" )+1;
 				
 				// if we have an array get the array class
 				if( arrayDimension > 0 )
 				{
 					// grab the type of the array either a primitive or an object at this point
 					String arrayType = null;
 					if( className.substring( arrayDimension, arrayDimension+1 ).equals( "L" ) )
 					{
 						arrayType = className.substring( arrayDimension+1 );
 					}
 					else
 					{
 						arrayType = className.substring( arrayDimension );
 					}
 					Class< ? > arrayClazz = ENCODED_TYPE_MAP.get( arrayType );
 					if( arrayClazz == null )
 					{
 						arrayClazz = Class.forName( arrayType );
 					}
 	
 					final int[] dims = new int[ arrayDimension ];
 					for( int i = 0; i < arrayDimension; i++ )
 					{
 						dims[ i ] = 1;
 					}
 					clazz = Array.newInstance( arrayClazz, dims ).getClass();
 				}
 			}
 			
 			// not a primitive, so do the normal reflective thing
 			if( clazz == null )
 			{
 				clazz = Class.forName( className );
 			}
 		}
 		catch( ClassNotFoundException e )
 		{
 			final String message = "Could not instantiate class from specified class name: " + className;
			if( isLoggingEnabled != null && isLoggingEnabled[ 0 ] ) 
 			{
 				LOGGER.error( message, e );
 			}
 			throw new IllegalArgumentException( message, e );
 		}
 		return clazz;
 	}
 	
 	/**
 	 * If the specified {@link Class} is a primitive, then this function will return the wrapped
 	 * version. For example, if the specified {@link Class} is {@code int.class} or {@code Integer.TYPE}
 	 * this method will return {@code Integer.class}
 	 * @param clazz The {@link Class} to wrap
 	 * @return The specified {@link Class} or the wrapped version of the {@link Class} if it is a primitive
 	 */
 	public static Class< ? > wrapPrimitive( final Class< ? > clazz )
 	{
 		if( WRAP_MAP.containsKey( clazz ) )
 		{
 			return WRAP_MAP.get( clazz ); 
 		}
 		else
 		{
 			return clazz;
 		}
 	}
 
 	/**
 	 * If the specified {@link Class} is a wrapped primitive, then this function will return the primitive
 	 * version. For example, if the specified {@link Class} is {@code Integer.class} this method will return 
 	 * {@code int.class} or equivalently, or {@code Integer.TYPE}
 	 * @param clazz The {@link Class} to unwrap
 	 * @return The specified {@link Class} or the primitive version of the {@link Class} if it is a wrapped primitive
 	 */
 	public static Class< ? > unwrapPrimitive( final Class< ? > clazz )
 	{
 		if( UNWRAP_MAP.containsKey( clazz ) )
 		{
 			return UNWRAP_MAP.get( clazz ); 
 		}
 		else
 		{
 			return clazz;
 		}
 	}
 
 	/**
 	 * Casts the specified object to the specified class, taking care of the special case where the
 	 * specified clazz is a primitive. 
 	 * @param clazz The {@link Class} to which to cast the object
 	 * @param object The object which to cast to the {@link Class}
 	 * @return The object cast to the specified {@link Class} type
 	 */
 	@SuppressWarnings( "unchecked" )
 	public static < T > T cast( final Class< ? extends T > clazz, final Object object )
 	{
 		final Class< ? > primitiveClazz = WRAP_MAP.get( clazz );
 		if( primitiveClazz != null && primitiveClazz.equals( object.getClass() ) ) //&& !clazz.equals( void.class ) )
 		{
 			return (T)object;
 		}
 		return clazz.cast( object );
 	}
 
 	/**
 	 * Returns the primitive {@link Class} object if the specified class name represents
 	 * a primitive type; otherwise, returns <code>null</code>
 	 * @param classname The name of the class (or primitive) for which to retrieve the {@link Class}
 	 * @return the primitive {@link Class} object if the specified class name represents
 	 * a primitive type; otherwise, returns <code>null</code>
 	 */
 	public static Class< ? > getPrimitive( final String classname )
 	{
 		return TYPE_MAP.get( classname );
 //		if( Integer.TYPE.toString().equals( classname ) )
 //		{
 //			return Integer.TYPE;
 //		}
 //		if( Double.TYPE.toString().equals( classname ) )
 //		{
 //			return Double.TYPE;
 //		}
 //		if( Float.TYPE.toString().equals( classname ) )
 //		{
 //			return Float.TYPE;
 //		}
 //		if( Long.TYPE.toString().equals( classname ) )
 //		{
 //			return Long.TYPE;
 //		}
 //		if( Short.TYPE.toString().equals( classname ) )
 //		{
 //			return Short.TYPE;
 //		}
 //		if( Boolean.TYPE.toString().equals( classname ) )
 //		{
 //			return Boolean.TYPE;
 //		}
 //		if( Character.TYPE.toString().equals( classname ) )
 //		{
 //			return Character.TYPE;
 //		}
 //		if( Byte.TYPE.toString().equals( classname ) )
 //		{
 //			return Byte.TYPE;
 //		}
 //		return null;
 	}
 	
 }
