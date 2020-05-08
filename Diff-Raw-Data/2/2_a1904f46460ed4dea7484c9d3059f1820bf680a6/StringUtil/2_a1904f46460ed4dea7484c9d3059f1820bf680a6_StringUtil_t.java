 package org.cujau.utils;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.text.Format;
 import java.util.Collection;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Utility functions for Strings.
  */
 public final class StringUtil {
 
     private static final Pattern PROPERTY_NAME_PATTERN = Pattern.compile( "(\\$\\{([\\w\\.]+)\\})" );
     private static final String DEF_SEP = ",";
 
     public static String toString( double[] ary ) {
         return toString( ary, DEF_SEP );
     }
 
     public static String toString( double[] ary, String separator ) {
         StringBuilder buf = new StringBuilder();
         for ( double e : ary ) {
             buf.append( e );
             buf.append( separator );
         }
         if ( buf.length() > 0 ) {
             buf.deleteCharAt( buf.length() - 1 );
         }
         return buf.toString();
     }
 
     /**
      * Convert the given array of objects into it's String representation. The array elements will
      * be separated by a comma (',').
      * 
      * @param <E>
      *            The type of the elements.
      * @param ary
      *            The array of the elements.
      * @return A String containg the String representation of each element in the array, separated
      *         by a comma.
      */
     public static <E> String toString( E[] ary ) {
         return toString( ary, DEF_SEP );
     }
 
     /**
      * Convert the given array of objects into it's String representation. The array elements will
      * be separated by the given <tt>separator</tt> string.
      * 
      * @param <E>
      *            The type of the elements.
      * @param ary
      *            The array of the elements.
      * @param separator
      *            The character(s) to place between each element of the array.
      * @return A String containg the String representation of each element in the array, separated
      *         by the given separator character(s).
      */
     public static <E> String toString( E[] ary, String separator ) {
         StringBuilder buf = new StringBuilder();
         for ( E e : ary ) {
             buf.append( e );
             buf.append( separator );
         }
         if ( buf.length() > 0 ) {
             buf.deleteCharAt( buf.length() - 1 );
         }
         return buf.toString();
     }
 
     /**
      * Convert the given collection of objects into it's String representation. The String
      * representation of an element of the collection will be separated from the next element with a
      * comma (',').
      * 
      * @param <E>
      *            The type of the elements.
      * @param col
      *            The collection of elements.
      * @return A String containing the String representation of each element in the Collection,
      *         separated by a comma.
      */
     public static <E> String toString( Collection<E> col ) {
         return toString( col, DEF_SEP );
     }
 
     /**
      * Convert the given collection of objects into it's String representation. The String
      * representation of an element of the collection will be separated from the next element with
      * the given separator.
      * 
      * @param <E>
      *            The type of the elements.
      * @param col
      *            The collection of elements.
      * @param separator
      *            The string/character that will separate elements in the returned string.
      * @return A String containing the String representation of each element in the Collection,
      *         separated by the given separator.
      */
     public static <E> String toString( Collection<E> col, String separator ) {
         return toString( col, null, separator );
     }
 
     public static <E> String toString( Collection<E> col, Format formatter ) {
         return toString( col, formatter, DEF_SEP );
     }
 
     public static <E> String toString( Collection<E> col, Format formatter, String separator ) {
         StringBuilder buf = new StringBuilder();
         if ( col == null ) {
             return buf.toString();
         }
         for ( E e : col ) {
             if ( e != null ) {
                 if ( formatter != null ) {
                     buf.append( formatter.format( e ) );
                 } else {
                     buf.append( e.toString() );
                 }
                 buf.append( separator );
             }
         }
         if ( buf.length() > 0 ) {
             buf.delete( buf.length() - separator.length(), buf.length() );
         }
         return buf.toString();
     }
 
     /**
      * Convert the given Throwable object into a stacktrace.
      * 
      * @param t
      *            The Throwable for which the stacktrace will be generated.
      * @return A String representation of the stacktrace.
      */
     public static String toString( Throwable t ) {
         if ( t == null ) {
             return "";
         }
         StringWriter stringWriter = new StringWriter();
         PrintWriter printWriter = new PrintWriter( stringWriter );
         t.printStackTrace( printWriter );
         printWriter.flush();
         printWriter.close();
         return stringWriter.toString();
     }
 
     /**
      * Replace the delimited property keys in the given String with their values taken from the
      * System properties.
      * <p>
      * A delimited property key has the form
      * <tt>${<em>{@link #PROPERTY_NAME_PATTERN property.name}</em>}</tt>. Property names may only
      * contain alphanumeric characters plus the '_' and '.' characters.
      * </p>
      * <p>
      * Any delimited propery keys in the given String that do not have replacements in the System
      * properties object will remain in the String.
      * </p>
      * 
      * @param orig
      *            The original String
      * @return The new String with the delimited properties replaced.
      */
     public static String replaceProperties( String orig ) {
         return replaceProperties( orig, System.getProperties() );
     }
 
     /**
      * Replace the delimited property keys in the given String with their values taken from the
      * given Properties object.
      * <p>
      * A delimited property key has the form
      * <tt>${<em>{@link #PROPERTY_NAME_PATTERN property.name}</em>}</tt>. Property names may only
      * contain alphanumeric characters plus the '_' and '.' characters.
      * </p>
      * <p>
      * Any delimited propery keys in the given String that do not have replacements in the given
      * Properties object will remain in the String.
      * </p>
      * 
      * @param orig
      *            The original String
      * @param replacements
      *            A Properties object from which the property values will be taken.
      * @return The new String with the delimited properties replaced.
      */
     public static String replaceProperties( String orig, Properties replacements ) {
         return replaceProperties( orig, replacements, false );
     }
 
     /**
      * Replace the delimited property keys in the given String with their values taken from the
      * given Properties object.
      * <p>
      * A delimited property key has the form
      * <tt>${<em>{@link #PROPERTY_NAME_PATTERN property.name}</em>}</tt>. Property names may only
      * contain alphanumeric characters plus the '_' and '.' characters.
      * </p>
      * <p>
      * Any delimited propery keys in the given String that do not have replacements in the given
      * Properties object can be removed (replaced with the empty String) from the resulting String
      * by setting the <tt>removeUnmatchedKeys</tt> parameter to <tt>true</tt>.
      * </p>
      * 
      * @param orig
      *            The original String
      * @param replacements
      *            A Properties object from which the property values will be taken.
      * @param removeUnmatchedKeys
      *            <tt>true</tt> if any delimited property keys in the given String should be removed
      *            from the resulting String if there is no matching property in the given Properties
      *            object.
      * @return The new String with the delimited properties replaced.
      */
     public static String replaceProperties( String orig, Properties replacements, boolean removeUnmatchedKeys ) {
         if ( orig == null ) {
             return null;
         }
 
         Matcher m = PROPERTY_NAME_PATTERN.matcher( orig );
         while ( m.find() ) {
             String propVal = null;
             if ( replacements != null ) {
                 propVal = replacements.getProperty( m.group( 2 ) );
             }
 
             if ( propVal != null ) {
                 orig = orig.replace( m.group( 1 ), propVal );
             } else if ( removeUnmatchedKeys ) {
                 orig = orig.replace( m.group( 1 ), "" );
             }
         }
         return orig;
     }
 
     public static String padLeft( String str, int totalLength ) {
         String ret = String.format( "%" + totalLength + "s", str );
         return ret;
     }
 
     public static String padRight( String str, int totalLength ) {
         int padding = totalLength - str.length();
         String padFmt = null;
        if ( padding <= 0 ) {
             padFmt = "";
         } else {
             padFmt = "%" + padding + "s";
         }
         String ret = String.format( "%s" + padFmt, str, "" );
         return ret;
     }
 }
