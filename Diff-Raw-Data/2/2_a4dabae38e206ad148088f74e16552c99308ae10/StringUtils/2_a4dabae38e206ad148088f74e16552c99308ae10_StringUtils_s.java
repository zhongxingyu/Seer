 /*
 
 Copyright: 2010 Bindley Bioscience Center, Purdue University
 
 License: X11 license.
 
 	Permission is hereby granted, free of charge, to any person
 	obtaining a copy of this software and associated documentation
 	files (the "Software"), to deal in the Software without
 	restriction, including without limitation the rights to use,
 	copy, modify, merge, publish, distribute, sublicense, and/or sell
 	copies of the Software, and to permit persons to whom the
 	Software is furnished to do so, subject to the following
 	conditions:
 
 	The above copyright notice and this permission notice shall be
 	included in all copies or substantial portions of the Software.
 
 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 	EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 	OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 	NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 	HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 	WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 	FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 	OTHER DEALINGS IN THE SOFTWARE.
 
 */
 
 package edu.purdue.bbc.util;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.Collection;
 
 /**
  * A class for String operations.
  */
 public class StringUtils {
 	private static final Pattern NUMBER_PATTERN = 
		Pattern.compile("(\\d+\\.?\\d*|\\.\\d+)([Ee]\\d+)?");
 
 	/**
 	 * Converts a string to camel case, assuming '_' as the word separator.
 	 * 
 	 * @param s The string to be camel cased.
 	 * @return The camel cased string.
 	 */
 	public static String camelCase( String s ) {
 		return camelCase( s, '_' );
 	}
 
 	/**
 	 * Converts a string to camel case.
 	 * 
 	 * @param s The string to be camel cased.
 	 * @param space The current word separator character.
 	 * @return The camel cased string.
 	 */
 	public static String camelCase( String s, char space ) {
 		String[] words = s.split( ""+space );
 		StringBuilder returnValue = new StringBuilder( words[ 0 ]);
 		for ( int i=1; i < words.length; i++ ) {
 			if ( words[ i ].length( ) >= 1 )
 				returnValue.append( words[ i ].substring( 0, 1 ).toUpperCase( ));
 			if ( words[ i ].length( ) >= 2 )
 				returnValue.append( words[ i ].substring( 1 ));
 		}
 		return returnValue.toString( );
 	}
 
 	/**
 	 * Reverts a string from camel case, using '_' as the word separator.
 	 * 
 	 * @param s The string to be transformed.
 	 * @return The new string.
 	 */
 	public static String unCamelCase( String s ) {
 		return unCamelCase( s, '_' );
 	}
 
 	/**
 	 * Reverts a string from camel case.
 	 * 
 	 * @param s The string to be transformed.
 	 * @param space The new word separator character.
 	 * @return The new string.
 	 */
 	public static String unCamelCase( String s, char space ) {
 		final Pattern pattern = Pattern.compile( "([a-z0-9])([A-Z])" );
 		Matcher matcher = pattern.matcher( s );
 		String[] pieces = pattern.split( s );
 		while( matcher.find( )) {
 			s = matcher.replaceFirst( matcher.group( 1 ) + space + 
 			                    matcher.group( 2 ).toLowerCase( ));
 			matcher.reset( s );
 		}
 		return s;
 	}
 
 	/**
 	 * Joins a Collection of strings into a single string, inserting a space
 	 * between each one.
 	 * 
 	 * @param strings The Collection of strings to join.
 	 * @return The resulting string.
 	 */
 	public static String join( Collection<String> strings ) {
 		return join( strings, " " );
 	}
 
 	/**
 	 * Joins a Collection of strings into a single string, inserting a separator
 	 * string between each one.
 	 * 
 	 * @param strings The Collection of strings to join.
 	 * @param separator The separator to use between strings.
 	 * @return The resulting string.
 	 */
 	public static String join( Collection<String> strings, String separator ) {
 		StringBuilder returnValue = new StringBuilder( );
 		for ( String string : strings ) {
 			if ( returnValue.length( ) > 0 ) {
 				returnValue.append( separator );
 			}
 			returnValue.append( string );
 		}
 		return returnValue.toString( );
 	}
 
 	/**
 	 * Tests a string to determine whether it can be interpreted as a number.
 	 * 
 	 * @param s The string to test.
 	 * @return true if the string contains only a number.
 	 */
 	public static boolean isNumeric( String s ) {
 		return NUMBER_PATTERN.matcher( s ).matches( );
 	}
 }
 
