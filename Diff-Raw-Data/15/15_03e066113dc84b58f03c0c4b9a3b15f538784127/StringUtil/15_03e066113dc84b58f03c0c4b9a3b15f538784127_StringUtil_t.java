 /*
  * Copyright 2010-2011 Heads Up Development Ltd.
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
 
 package org.headsupdev.support.java;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Map;
 
 /**
  * Various utility methods for working with strings
  *
  * @author Andrew Williams
  * @since 1.0
  */
 public class StringUtil
 {
     /**
      * Discover if the passed string is empty.
      * An empty string is one that is either zero characters in length or containing only whitespace.
      * A null string is also considered as empty and so will return true also.
      *
      * @param string The string to test
      * @return true if the string is null, of zero length or containing only whitespace.
      */
     public static boolean isEmpty( String string )
     {
         if ( string == null )
         {
             return true;
         }
 
         if ( string.length() == 0 )
         {
             return true;
         }
 
         return string.trim().length() == 0;
     }
 
     /**
      * Convert the string to title case - that is a string that starts with an upper case letter and all other
      * characters are in lower case.
      *
      * Note that this is identical to calling <code>StringUtil.toTitleCase( string, true )</code>.
      *
      * @param string The string to convert to title case
      * @return A new string with the case converted to title case
      */
     public static String toTitleCase( String string )
     {
         return toTitleCase( string, true );
     }
 
     /**
      * Convert the string to title case - that is a string that starts with an upper case letter and all other
      * characters are (optionally) in lower case.
      *
      * The second parameter specifies whether the remaining string should be converted to lower case.
      *
      * @param string The string to convert to title case
      * @param lower Determines if the remaining (all but first) characters should be forced to lower case.
      *   Passing false means that the source string's case will be respected.
      * @return A new string with the case converted to title case
      */
     public static String toTitleCase( String string, boolean lower )
     {
         if ( StringUtil.isEmpty( string ) )
         {
             return string;
         }
 
         StringBuffer out = new StringBuffer();
         out.append( string.substring( 0, 1 ).toUpperCase() );
         if ( lower )
         {
             out.append( string.substring( 1 ).toLowerCase() );
         }
         else
         {
             out.append( string.substring( 1 ) );
         }
 
         return out.toString();
     }
 
     /**
      * Formats a string from a template using the passed parameters
      *
      * @see #format(String, java.util.Map)
      * @param template      The source of the format string
      * @param parameters    The parameters for substitution
      * @return  Formatted string
      */
     public static String format( File template, Map<String, Object> parameters )
     {
         return format( FileUtil.toString( template ), parameters );
     }
 
     /**
      * Formats a string from a template using the passed parameters
      *
      * @see #format(String, String, String, java.util.Map)
      * @param template      The source of the format string
      * @param prefix        Beginning identifier of a placeholder
      * @param postfix       End identifier of a placeholder
      * @param parameters    The parameters for substitution
      * @return  Formatted string
      */
     public static String format( File template, String prefix, String postfix, Map<String, Object> parameters )
     {
         return format( FileUtil.toString( template ), prefix, postfix, parameters );
     }
 
     /**
      * Formats a string from a template using the passed parameters
      *
      * @see #format(String, java.util.Map)
      * @param template      The source of the format string
      * @param parameters    The parameters for substitution
      * @return  Formatted string
      */
     public static String format( InputStream template, Map<String, Object> parameters )
     {
         return format( IOUtil.toString( template ), parameters );
     }
 
     /**
      * Formats a string from a template using the passed parameters
      *
      * @see #format(String, String, String, java.util.Map)
      * @param template      The source of the format string
      * @param prefix        Beginning identifier of a placeholder
      * @param postfix       End identifier of a placeholder
      * @param parameters    The parameters for substitution
      * @return  Formatted string
      */
     public static String format( InputStream template, String prefix, String postfix, Map<String, Object> parameters )
     {
         return format( IOUtil.toString( template ), prefix, postfix, parameters );
     }
 
     /**
      * A string formatter using named placeholders.
      *
      * For example the string "Hello ${planet} could be transformed using a map of
      * "planet" => "world" to "Hello world".
      *
      * The method replaces all occurrences of named placeholders with the matching value from the map.
      * Matching uses java's built in String.format to insert replacement values.
      * Any placeholder which is not found in the map will remain in the resulting string.
      *
      * @param format        Format of the resulting string
      * @param parameters    Input parameters for substitution
      * @return Formatted string
      */
     public static String format( String format, Map<String, Object> parameters )
     {
         return format( format, "${", "}", parameters );
     }
 
     /**
      * A string formatter using named placeholders.
      *
      * For example, given prefix "&lt;" and postfix "&gt;" the string "Hello &lt;planet&gt; could be
      * transformed using a map of "planet" => "world" to "Hello world".
      *
      * The method replaces all occurrences of named placeholders (which start with the prefix and end
      * with the post fix) with the matching value from the map.
      * Matching uses java's built in String.format to insert replacement values.
      * Any placeholder which is not found in the map will remain in the resulting string.
      *
      * @param format        Format of the resulting string
      * @param prefix        Beginning identifier of a placeholder
      * @param postfix       End identifier of a placeholder
      * @param parameters    Input parameters for substitution
      * @return Formatted string
      */
     public static String format( String format, String prefix, String postfix, Map<String, Object> parameters )
     {
        StringBuilder idFormat = new StringBuilder( format.replace( "%", "%%" ) );
         ArrayList<Object> values = new ArrayList<Object>();
 
         int index = 1;
         for( String key : parameters.keySet() )
         {
             String match = prefix + key + postfix;
             String replace = "%" + index + "$s";
 
             int pos = idFormat.indexOf( match );
             while ( pos != -1 )
             {
                 idFormat.replace( pos, pos + match.length(), replace );
                 pos += replace.length();
                 pos = idFormat.indexOf( match, pos );
             }
 
             values.add( parameters.get( key ) );
             ++index;
         }
 
         return String.format( idFormat.toString(), values.toArray() );
     }
 }
