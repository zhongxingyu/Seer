 package com.redshape.utils;
 
 import sun.net.util.IPAddressUtil;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.lang.reflect.InvocationTargetException;
 import java.math.BigInteger;
 import java.nio.charset.Charset;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.regex.Pattern;
 
 
 @SuppressWarnings("restriction")
 public class StringUtils {
     public static List<String> camelCaseDelimiters = Arrays.asList( "_", "-" );
 	private static final String RANDOM_STRING_SOURCE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
 	private static final String ESCAPE_SYMBOL = "\\";
 
 	public static String[] chunks( String source, int chunkSize ) {
 		int leastLength = source.length();
 		float chunksCount = leastLength / chunkSize;
 
 		String[] chunks = new String[Math.round(chunksCount) + ( leastLength - chunkSize * chunksCount > 0 ? 1 : 0 )];
 		int i = 0 ;
 		int offset = 0;
 		while ( leastLength >= chunkSize ) {
 			offset = i++ * chunkSize;
 			chunks[i-1] = source.substring( offset, offset + chunkSize );
 			leastLength -= chunkSize;
 		}
 
 		if ( leastLength > 0 ) {
 			chunks[i+1] = source.substring( offset, offset + leastLength );
 		}
 
 		return chunks;
 	}
 
     public static String repeat(String source, int times) {
         String result = "";
 
         while (times > 0) {
             result = result.concat(source);
             times -= 1;
         }
 
         return result;
     }
 
     public static String reverseSentence( String sentence ) {
     	return reverseSentence( sentence, " ");
     }
     
     public static String reverseSentence( String sentence, String delimiter ) {
     	StringBuilder builder = new StringBuilder();
         String[] parts = sentence.split( Pattern.quote( delimiter ) );
         for ( int i = parts.length - 1; i >= 0; i--) {
             builder.append( parts[i] );
             
             if ( i != 0 ) {
             	builder.append( delimiter );
             }
         }
         
         return builder.toString();
     }
 
 	public static String randomString(int length) {
 		if (length < 1) {
 			return "";
 		}
 
 		StringBuilder sb = new StringBuilder(length);
 		for (int i = 0; i < length; i++) {
 			sb.append(
 				RANDOM_STRING_SOURCE.charAt(
 					(int)(Math.random() * RANDOM_STRING_SOURCE.length())
 				)
 			);
 		}
 
 		return sb.toString();
 	}
 
     public static String reverse( String input ) {
           return String.valueOf( reverse( input.toCharArray() ) );
     }
 
     public static char[] reverse( char[] input ) {
         char[] result = new char[input.length];
 
         int lastOffset = input.length - 1;
         for( int i = lastOffset; i >= 0; i-- ) {
             result[lastOffset - i] = input[i];
         }
 
         return result;
     }
 
 	public static String trim( String source ) {
 		return trim( source, " ");
 	}
 
 	public static String trim( String source, String needle ) {
 		if ( source == null ) {
 			throw new IllegalArgumentException("<null>");
 		}
 
 		while ( source.startsWith(needle) ) {
 			source = source.substring(1);
 		}
 
 		while ( source.endsWith(needle) ) {
 			source = source.substring(0, source.length() - 1 );
 		}
 
 		return source;
 	}
 
     public static String getFileExtension( File file ) {
         return getFileExtension( file.getPath() );
     }
 
     public static String getFileExtension( String name ) {
     	int index = name.lastIndexOf(".");
     	if ( index != -1 ) {
     		return name.substring( index + 1, name.length() );
     	} else {
     		return "";
     	}
     }
 
     public static String toCamelCase( String name ) {
         return toCamelCase( name, true );
     }
 
     /**
 	* Camelize input string
 	* @param name Input string
 	* @param ucfirst Make first character uppercased
 	* @return String
 	*/
     public static String toCamelCase( String name, boolean ucfirst ) {
         StringBuilder result = new StringBuilder();
 
         for (int i = 0; i < name.length(); i++) {
             String prevChar = name.substring(i > 0 ? i - 1 : 0, i > 0 ? i : 1);
             String currChar = name.substring(i, i + 1);
 
             if ( camelCaseDelimiters.contains( prevChar ) || ( ucfirst && i == 0 && !camelCaseDelimiters.contains( currChar ) ) ) {
                 result.append( currChar.toUpperCase() );
             } else if ( !camelCaseDelimiters.contains( currChar ) ) {
                 result.append(currChar);
             }
         }
 
         return result.toString();
     }
 
     public static String fromCamelCase( String name, String delimiter ) {
         StringBuilder result = new StringBuilder();
 
         int last_delimiter_pos = 0;
         for ( int i = 0; i < name.length(); i++ ) {
             String currChar = name.substring(i, i + 1);
 
             if ( currChar.toUpperCase().equals( currChar ) && i != last_delimiter_pos - 1 ) {
                 if ( i > 0 ) {
                     result.append( delimiter );
                     last_delimiter_pos = i;
                 }
 
                 result.append( currChar.toLowerCase() );
             } else {
                 result.append( currChar.toLowerCase() );
             }
         }
 
         return result.toString();
     }
 
     public static String join( Collection<?> join, String separator ) {
 		return join( join.toArray(), separator );
     }
 
 	public static String join( Collection<?> join, String separator, IFunction<?, String> filter ) {
 		return join( join.toArray( new Object[join.size()] ), separator, filter );
 	}
 
 	public static String join( Object[] join, String separator ) {
 		return join( join, separator, null );
 	}
 
     public static String join( Object[] join, String separator, IFunction<?, String> filter ) {
         StringBuilder builder = new StringBuilder();
         int i = 0;
         for ( Object joinItem : join ) {
 			String value = joinItem == null ? null : joinItem.toString();
 			if ( filter != null ) {
 				try {
 					value = filter.invoke( value );
 				} catch ( InvocationTargetException e ) {
 					throw new IllegalArgumentException("Filtering exception", e );
 				}
 			}
 
 			builder.append( value );
 
 			if ( i++ != join.length - 1 ) {
 				builder.append( separator );
 			}
         }
 
         return builder.toString();
     }
 
     public static String formatDuration( int value ) {
         return formatDuration( value, false );
     }
 
     public static String formatDuration( int value, boolean displayHours ) {
         String result = "";
         if ( displayHours ) {
             if ( value > Constants.TIME_HOUR ) {
                 float hours = value / Constants.TIME_HOUR;
                 value -= hours * Constants.TIME_HOUR;
                 result += ( ( hours > 9 ? Math.round(hours) : "0" + Math.round(hours) ) + ":").toString();
             } else {
                 result += "00:";
             }
         }
 
         if ( value > Constants.TIME_MINUTE  ) {
             float minutes = value / Constants.TIME_MINUTE;
             value -= minutes * Constants.TIME_MINUTE;
             result += ( ( minutes > 9 ? Math.round( minutes ) : "0" + Math.round( minutes ) ) + ":" ).toString();
         } else {
             result += "00:";
         }
 
         if ( value >= Constants.TIME_SECOND ) {
             float seconds = value / Constants.TIME_SECOND;
             result += ( ( seconds > 9 ? Math.round(seconds) : "0" + Math.round(seconds) ) ).toString();
         } else {
             result += "00";
         }
 
         return result;
     }
 
     public static String ucfirst( String value ) {
 		if ( value.isEmpty() ) {
 			return value;
 		}
 
     	return value.substring(0, 1).toUpperCase().concat( value.substring(1) );
     }
 
     public static String lcfirst( String value ) {
 		if ( value.isEmpty() ) {
 			return value;
 		}
 
     	return value.substring(0, 1).toLowerCase().concat( value.substring(1) );
     }
 
     public static byte[] stringToIP( String addrString ) {
 		if ( addrString.isEmpty() ) {
 			throw new IllegalArgumentException("Empty address given");
 		}
 
         return IPAddressUtil.textToNumericFormatV4(addrString);
     }
 
 	public static Integer hex( String data ) {
 		return Integer.valueOf(
 			String.format("%x", new BigInteger(1, data.getBytes( Charset.forName("UTF-8") ) ) )
 		);
 	}
 
     public static String IPToString( byte[] address ) {
         String[] result = new String[address.length];
         for ( int i = 0; i < address.length; i++ ) {
             result[i] = String.valueOf( address[i] & 0xff );
         }
 
         return StringUtils.join( result, "." );
     }
 
     public static String formatDate( String format, Date date ) {
     	return new SimpleDateFormat(format).format(date);
     }
 
     public static String stackTraceAsString( Throwable e ) {
     	final Writer writer = new StringWriter();
     	final PrintWriter printer = new PrintWriter(writer);
 
     	e.printStackTrace(printer);
 
     	return writer.toString();
     }
 
 	public static String escape( String orig, String[] escapeSequences ) {
 		return escape(orig, escapeSequences, ESCAPE_SYMBOL );
 	}
 
 	public static String escape( String orig, String[] escapeSequences, String escapeSymbol ) {
 		if ( orig == null ) {
 			throw new IllegalArgumentException("<null>");
 		}
 
 		String replacement = orig;
 		for ( String sequence : escapeSequences ) {
 			int lastIndex = replacement.indexOf(sequence);
 			while ( lastIndex > 0 && lastIndex !=
 											replacement.indexOf( escapeSymbol, lastIndex - 2) + 1 ) {
 				replacement = replacement.replace( sequence, escapeSymbol + sequence );
 				lastIndex = replacement.indexOf(sequence, lastIndex);
 			}
 		}
 
 		return replacement;
 	}
 
 	public static String wordWrap( String sentence, int interval, String separator ) {
 		int count = (int) ( sentence.length() / interval );
		int i = 1;
 		StringBuilder builder = new StringBuilder();
		while ( (i + 1) != count - 1  && (i - 1) < sentence.length() ) {
			builder.append( sentence.substring( (i - 1) * interval, (i + 1) * interval ) )
 				   .append( separator );

            i++;
 		}
 
 		return builder.toString();
 	}
 
     public static String toHEX(String str) {
         StringBuilder builder = new StringBuilder();
         for (int i = 0; i < str.length(); i++) {
             byte c = (byte)str.charAt(i);
             String tmp = Integer.toHexString(c).toUpperCase();
             for (int j = 0; j < 2 - tmp.length(); j++) {
                 tmp = "0" + tmp;
             }
             
             builder.append( tmp );
         }
 
         return builder.toString();
     }
     
 }
