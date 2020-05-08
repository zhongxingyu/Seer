 package is.us.util;
 
 import java.io.*;
 import java.net.URL;
 import java.text.*;
 import java.util.*;
 import java.util.regex.Pattern;
 
 import org.slf4j.*;
 
 /**
  * Various utility methods for handling strings.
  *
  * @author Hugi Þórðarson
  */
 
 public class USStringUtilities extends Object {
 
 	private static final List<String> ALLOWED_PERMNO_CHARS = Arrays.asList( new String[] { "A", "Á", "B", "C", "D", "Ð", "E", "É", "F", "G", "H", "I", "Í", "J", "K", "L", "M", "N", "O", "Ó", "P", "Q", "R", "S", "T", "U", "Ú", "V", "W", "X", "Y", "Ý", "Þ", "Z", "Æ", "Ö" } ); // 30.07.2008 Bjarni Sævarsson
 
 	/**
 	 * Logger used for logging activites of this class.
 	 */
 	private static final Logger logger = LoggerFactory.getLogger( USStringUtilities.class );
 
 	/**
 	 * Name of our default encoding.
 	 */
 	private static final String DEFAULT_ENCODING = "UTF-8";
 
 	/**
 	 * This array contains sets of characters that can be considered similar looking.
 	 */
 	private static final List<String> SIMILAR_CHARACTERS = new ArrayList<String>();
 
 	private static final DecimalFormatSymbols ICELANDIC_DECIMAL_FORMAT_SYMBOLS = new DecimalFormatSymbols();
 
 	static {
 		ICELANDIC_DECIMAL_FORMAT_SYMBOLS.setDecimalSeparator( ',' );
 		ICELANDIC_DECIMAL_FORMAT_SYMBOLS.setGroupingSeparator( '.' );
 
 		SIMILAR_CHARACTERS.add( "A;Á" );
 		SIMILAR_CHARACTERS.add( "E;É" );
 		SIMILAR_CHARACTERS.add( "I;Í;1" );
 		SIMILAR_CHARACTERS.add( "O;Ó;0" );
 		SIMILAR_CHARACTERS.add( "U;Ú" );
 		SIMILAR_CHARACTERS.add( "Y;Ý" );
 	}
 
 	private static final String[] BINARY_SIZE_NAME = new String[] { "bytes", "kilobytes", "megabytes", "gigabytes", "terabytes", "petabytes", "exabytes", "zettabytes", "yottabytes" };
 	private static final String[] BINARY_SIZE_SYMBOL = new String[] { "B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
 	private static final String ICELANDIC_DECIMAL_FORMAT_PATTERN = "#,###.000";
 
 	// Most common HTML entity escape characters.. 
 	// Unicode int value = { Unicode hex value, Character, HTML Entity }
 	private static final HashMap<Integer, String[]> escapeChart = new HashMap<Integer, String[]>();
 
 	static {
 		escapeChart.put( 60, new String[] { "003C", "<", "&lt;" } );
 		escapeChart.put( 34, new String[] { "0022", "\"", "&quot;" } );
 		escapeChart.put( 162, new String[] { "00A2", "¢", "&cent;" } );
 		escapeChart.put( 165, new String[] { "00A5", "¥", "&#x00a5;" } );
 		escapeChart.put( 168, new String[] { "00A8", "¨", "&#x00a8;" } );
 		escapeChart.put( 171, new String[] { "00AB", "«", "&#x00ab;" } );
 		escapeChart.put( 174, new String[] { "00AE", "®", "&#x00ae;" } );
 		escapeChart.put( 177, new String[] { "00B1", "±", "&#x00b1;" } );
 		escapeChart.put( 180, new String[] { "00B4", "´", "&#x00b4;" } );
 		escapeChart.put( 183, new String[] { "00B7", "·", "&#x00b7;" } );
 		escapeChart.put( 186, new String[] { "00BA", "º", "&#x00ba;" } );
 		escapeChart.put( 189, new String[] { "00BD", "½", "&#x00bd;" } );
 		escapeChart.put( 192, new String[] { "00C0", "À", "&Agrave;" } );
 		escapeChart.put( 195, new String[] { "00C3", "Ã", "&Atilde;" } );
 		escapeChart.put( 198, new String[] { "00C6", "Æ", "&AElig;" } );
 		escapeChart.put( 201, new String[] { "00C9", "É", "&Eacute;" } );
 		escapeChart.put( 204, new String[] { "00CC", "Ì", "&Igrave;" } );
 		escapeChart.put( 207, new String[] { "00CF", "Ï", "&Iuml;" } );
 		escapeChart.put( 210, new String[] { "00D2", "Ò", "&Ograve;" } );
 		escapeChart.put( 213, new String[] { "00D5", "Õ", "&Otilde;" } );
 		escapeChart.put( 216, new String[] { "00D8", "Ø", "&Oslash;" } );
 		escapeChart.put( 219, new String[] { "00DB", "Û", "&Ucirc;" } );
 		escapeChart.put( 222, new String[] { "00DE", "Þ", "&THORN;" } );
 		escapeChart.put( 225, new String[] { "00E1", "á", "&aacute;" } );
 		escapeChart.put( 228, new String[] { "00E4", "ä", "&auml;" } );
 		escapeChart.put( 231, new String[] { "00E7", "ç", "&ccedil;" } );
 		escapeChart.put( 234, new String[] { "00EA", "ê", "&ecirc;" } );
 		escapeChart.put( 237, new String[] { "00ED", "í", "&iacute;" } );
 		escapeChart.put( 240, new String[] { "00F0", "ð", "&eth;" } );
 		escapeChart.put( 243, new String[] { "00F3", "ó", "&oacute;" } );
 		escapeChart.put( 246, new String[] { "00F6", "ö", "&ouml;" } );
 		escapeChart.put( 249, new String[] { "00F9", "ù", "&ugrave;" } );
 		escapeChart.put( 252, new String[] { "00FC", "ü", "&uuml;" } );
 		escapeChart.put( 255, new String[] { "00FF", "ÿ", "&yuml;" } );
 		escapeChart.put( 353, new String[] { "0161", "š", "&#x0161;" } );
 		escapeChart.put( 338, new String[] { "0152", "Œ", "&#x0152;" } );
 		escapeChart.put( 62, new String[] { "003E", ">", "&gt;" } );
 		escapeChart.put( 160, new String[] { "00A0", " ", "&#x00a0;" } );
 		escapeChart.put( 163, new String[] { "00A3", "£", "&#x00a3;" } );
 		escapeChart.put( 166, new String[] { "00A6", "¦", "&#x00a6;" } );
 		escapeChart.put( 169, new String[] { "00A9", "©", "&#x00a9;" } );
 		escapeChart.put( 172, new String[] { "00AC", "¬", "&#x00ac;" } );
 		escapeChart.put( 175, new String[] { "00AF", "¯", "&#x00af;" } );
 		escapeChart.put( 178, new String[] { "00B2", "²", "&#x00b2;" } );
 		escapeChart.put( 181, new String[] { "00B5", "µ", "&#x00b5;" } );
 		escapeChart.put( 184, new String[] { "00B8", "¸", "&#x00b8;" } );
 		escapeChart.put( 187, new String[] { "00BB", "»", "&#x00bb;" } );
 		escapeChart.put( 190, new String[] { "00BE", "¾", "&#x00be;" } );
 		escapeChart.put( 193, new String[] { "00C1", "Á", "&Aacute;" } );
 		escapeChart.put( 196, new String[] { "00C4", "Ä", "&Auml;" } );
 		escapeChart.put( 199, new String[] { "00C7", "Ç", "&Ccedil;" } );
 		escapeChart.put( 202, new String[] { "00CA", "Ê", "&Ecirc;" } );
 		escapeChart.put( 205, new String[] { "00CD", "Í", "&Iacute;" } );
 		escapeChart.put( 208, new String[] { "00D0", "Ð", "&ETH;" } );
 		escapeChart.put( 211, new String[] { "00D3", "Ó", "&Oacute;" } );
 		escapeChart.put( 214, new String[] { "00D6", "Ö", "&Ouml;" } );
 		escapeChart.put( 217, new String[] { "00D9", "Ù", "&Ugrave;" } );
 		escapeChart.put( 220, new String[] { "00DC", "Ü", "&Uuml;" } );
 		escapeChart.put( 223, new String[] { "00DF", "ß", "&szlig;" } );
 		escapeChart.put( 226, new String[] { "00E2", "â", "&acirc;" } );
 		escapeChart.put( 229, new String[] { "00E5", "å", "&aring;" } );
 		escapeChart.put( 232, new String[] { "00E8", "è", "&egrave;" } );
 		escapeChart.put( 235, new String[] { "00EB", "ë", "&euml;" } );
 		escapeChart.put( 238, new String[] { "00EE", "î", "&icirc;" } );
 		escapeChart.put( 241, new String[] { "00F1", "ñ", "&ntilde;" } );
 		escapeChart.put( 244, new String[] { "00F4", "ô", "&ocirc;" } );
 		escapeChart.put( 247, new String[] { "00F7", "÷", "&#x00f7;" } );
 		escapeChart.put( 250, new String[] { "00FA", "ú", "&uacute;" } );
 		escapeChart.put( 253, new String[] { "00FD", "ý", "&yacute;" } );
 		escapeChart.put( 8364, new String[] { "20AC", "€", "&#x20ac;" } );
 		escapeChart.put( 381, new String[] { "017D", "Ž", "&#x017d;" } );
 		escapeChart.put( 339, new String[] { "0153", "œ", "&#x0153;" } );
 		escapeChart.put( 38, new String[] { "0026", "&", "&amp;" } );
 		escapeChart.put( 161, new String[] { "00A1", "¡", "&#x00a1;" } );
 		escapeChart.put( 164, new String[] { "00A4", "¤", "&#x00a4;" } );
 		escapeChart.put( 167, new String[] { "00A7", "§", "&#x00a7;" } );
 		escapeChart.put( 170, new String[] { "00AA", "ª", "&#x00aa;" } );
 		escapeChart.put( 173, new String[] { "00AD", "¬", "&#x00ad;" } );
 		escapeChart.put( 176, new String[] { "00B0", "°", "&#x00b0;" } );
 		escapeChart.put( 179, new String[] { "00B3", "³", "&#x00b3;" } );
 		escapeChart.put( 182, new String[] { "00B6", "¶", "&#x00b6;" } );
 		escapeChart.put( 185, new String[] { "00B9", "¹", "&#x00b9;" } );
 		escapeChart.put( 188, new String[] { "00BC", "¼", "&#x00bc;" } );
 		escapeChart.put( 191, new String[] { "00BF", "¿", "&#x00bf;" } );
 		escapeChart.put( 194, new String[] { "00C2", "Â", "&Acirc;" } );
 		escapeChart.put( 197, new String[] { "00C5", "Å", "&Aring;" } );
 		escapeChart.put( 200, new String[] { "00C8", "È", "&Egrave;" } );
 		escapeChart.put( 203, new String[] { "00CB", "Ë", "&Euml;" } );
 		escapeChart.put( 206, new String[] { "00CE", "Î", "&Icirc;" } );
 		escapeChart.put( 209, new String[] { "00D1", "Ñ", "&Ntilde;" } );
 		escapeChart.put( 212, new String[] { "00D4", "Ô", "&Ocirc;" } );
 		escapeChart.put( 215, new String[] { "00D7", "×", "&#x00d7;" } );
 		escapeChart.put( 218, new String[] { "00DA", "Ú", "&Uacute;" } );
 		escapeChart.put( 221, new String[] { "00DD", "Ý", "&Yacute;" } );
 		escapeChart.put( 224, new String[] { "00E0", "à", "&agrave;" } );
 		escapeChart.put( 227, new String[] { "00E3", "ã", "&atilde;" } );
 		escapeChart.put( 230, new String[] { "00E6", "æ", "&aelig;" } );
 		escapeChart.put( 233, new String[] { "00E9", "é", "&eacute;" } );
 		escapeChart.put( 236, new String[] { "00EC", "ì", "&igrave;" } );
 		escapeChart.put( 239, new String[] { "00EF", "ï", "&iuml;" } );
 		escapeChart.put( 242, new String[] { "00F2", "ò", "&ograve;" } );
 		escapeChart.put( 245, new String[] { "00F5", "õ", "&otilde;" } );
 		escapeChart.put( 248, new String[] { "00F8", "ø", "&oslash;" } );
 		escapeChart.put( 251, new String[] { "00FB", "û", "&ucirc;" } );
 		escapeChart.put( 254, new String[] { "00FE", "þ", "&thorn;" } );
 		escapeChart.put( 352, new String[] { "0160", "Š", "&#x0160;" } );
 		escapeChart.put( 382, new String[] { "017E", "ž", "&#x017e;" } );
 		escapeChart.put( 376, new String[] { "0178", "Ÿ", "&#x0178;" } );
 	}
 
 	/**
 	 * No instances created, ever.
 	 */
 	private USStringUtilities() {}
 
 	/**
 	 * This method return true if a string is not null, and not equal to the empty String ""
 	 *
 	 * @param string The string to check
 	 */
 	public static final boolean stringHasValue( String string ) {
 		return string != null && string.length() > 0;
 	}
 
 	/**
 	 * Returns true if the string is not null( or empty ) and has some value other than "null"
 	 * 
 	 * @param string The string to check
 	 */
 	public static boolean stringHasValueAndNotNullString( String string ) {
 		return (USStringUtilities.stringHasValue( string ) && !"null".equals( string.toLowerCase() ));
 	}
 
 	/**
 	 * Returns true if string is not null, not empty, and contains something else that white spaces
 	 * 
 	 * @param string The string to check
 	 */
 	public static boolean stringHasValueTrimmed( String string ) {
 		if( !stringHasValue( string ) )
 			return false;
 
 		return stringHasValue( string.trim() );
 	}
 
 	/**
 	 * Validate an email address by checking that @ symbol and . are present.
 	 * 
 	 * @return false if the address is not valid.
 	 */
 	public static boolean validateEmailAddress( String email ) {
 
 		if( !USStringUtilities.stringHasValue( email ) )
 			return false;
 
 		if( email.indexOf( "@" ) == -1 || email.indexOf( "." ) == -1 )
 			return false;
 
 		return true;
 	}
 
 	/**
 	 * Checks if the string validates as a permno (only checks format of String).
 	 * 
 	 * @author Hugi Þórðarson
 	 * @author Bjarni Sævarsson 30.07.2008->changed check from regexp
 	 */
 	public static final boolean validatePermno( String permno ) {
 
 		if( !USStringUtilities.stringHasValue( permno ) )
 			return false;
 
 		permno = USStringUtilities.cleanupPermno( permno );
 
 		// A permno must be 5 characters long.
 		if( permno.length() != 5 )
 			return false;
 
 		// The first two characters of a permno must be upper case English/icelandic letters.
 		if( !ALLOWED_PERMNO_CHARS.contains( permno.substring( 0, 1 ) ) || !ALLOWED_PERMNO_CHARS.contains( permno.substring( 1, 2 ) ) ) {
 			return false;
 		}
 
 		// The first third character of a PERMNO must be a upper case English/Icelandic letter or a digit.
 		if( !ALLOWED_PERMNO_CHARS.contains( permno.substring( 2, 3 ) ) && !java.lang.Character.isDigit( permno.charAt( 3 ) ) ) {
 			return false;
 		}
 
 		// The last two characters of a PERMNO must be digits.
 		if( !java.lang.Character.isDigit( permno.charAt( 3 ) ) || !java.lang.Character.isDigit( permno.charAt( 4 ) ) ) {
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Converts bytes to a String using the specified encoding
 	 *
 	 * @param sourceData The data object to read from
 	 * @param encoding The encoding to use, use the standard Java encoding names specified in java.lang.String
 	 */
 	public static String stringFromDataUsingEncoding( byte[] sourceData, String encoding ) {
 
 		if( sourceData == null )
 			return null;
 
 		if( encoding == null ) {
 			encoding = DEFAULT_ENCODING;
 		}
 
 		try {
 			return new String( sourceData, encoding );
 		}
 		catch( Exception e ) {
 			logger.error( "Could not convert data to string", e );
 			return null;
 		}
 	}
 
 	/**
 	 * Indicates if two characters are "similar", in the sense that they could be difficult
 	 * for humans to differentiate between.
 	 * 
 	 * @author Hugi Þórðarson
 	 */
 	public static boolean areSimilar( char char1, char char2 ) {
 		for( String nextSet : SIMILAR_CHARACTERS ) {
 			String[] array = nextSet.split( ";" );
 			List<String> charsInSet = Arrays.asList( array );
 
 			if( char1 == char2 || (charsInSet.contains( String.valueOf( char1 ) ) && charsInSet.contains( String.valueOf( char2 ) )) )
 				return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Indicates if two strings are "similar", in the sense that they could be
 	 * difficult for humans to differentiate between.
 	 * 
 	 * @author Hugi Þórðarson
 	 */
 	public static boolean areSimilar( String string1, String string2 ) {
 
 		if( string1 == null || string2 == null ) {
 			return false;
 		}
 
 		string1 = replace( string1, " ", "" );
 		string2 = replace( string2, " ", "" );
 
 		int regno1Length = string1.length();
 		int regno2Length = string2.length();
 
 		if( regno1Length != regno2Length )
 			return false;
 
 		string1 = string1.toUpperCase();
 		string2 = string2.toUpperCase();
 
 		int shorterStringLength = (regno1Length < regno2Length) ? regno1Length : regno2Length;
 
 		for( int i = shorterStringLength - 1; i >= 0; i-- ) {
 			if( !areSimilar( string1.charAt( i ), string2.charAt( i ) ) ) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * This method will adjust a String to a certain length. If the string is
 	 * too long, it pads it with spaces on the left. If the string is too short,
 	 * it will cut of the end of it.
 	 * 
 	 * Created to make creation of fixed length string easier.
 	 * 
 	 * @author Hugi Þórðarson
 	 */
 	public static String padString( String string, int desiredLength ) {
 
 		String str = (string != null) ? string : "";
 		StringBuffer strBuff = new StringBuffer();
 		int strLength = str.length();
 
 		if( string != null )
 			strBuff.append( string );
 
 		if( desiredLength > 0 && desiredLength > strLength ) {
 			for( int i = 0; i <= desiredLength; i++ ) {
 				if( i > strLength )
 					strBuff.append( ' ' );
 			}
 		}
 		return strBuff.toString();
 	}
 
 	/**
 	 * This method will adjust a String to a certain length. If the string is
 	 * too short, it pads it on the left. If the string is too long,
 	 * it will cut of the left end of it.
 	 * 
 	 * Created to make creation of fixed length string easier.
 	 * 
 	 * @author Bjarni Sævarsson
 	 */
 	public static String padLeft( String string, String padString, int desiredLength ) {
 
 		String str = (string != null) ? string : "";
 		StringBuffer strBuff = new StringBuffer();
 
 		int strLength = str.length();
 
 		if( desiredLength > 0 && desiredLength > strLength ) {
 			for( int i = 0; i <= desiredLength; i++ ) {
 				if( i > strLength )
 					strBuff.append( padString );
 			}
 		}
 
 		strBuff.append( str );
 		return strBuff.toString();
 	}
 
 	/**
 	 * This method will adjust a String to a certain length. If the string is
 	 * too short, it pads it on the right. If the string is too long,
 	 * it will cut of the right end of it.
 	 * 
 	 * Created to make creation of fixed length string easier.
 	 * 
 	 * @author Bjarni Sævarsson
 	 */
 	public static String padRight( String string, String padString, int desiredLength ) {
 
 		String str = (string != null) ? string : "";
 		StringBuffer strBuff = new StringBuffer();
 
 		int strLength = str.length();
 
 		if( desiredLength > 0 && desiredLength > strLength ) {
 			for( int i = 0; i <= desiredLength; i++ ) {
 				if( i > strLength )
 					strBuff.append( padString );
 			}
 		}
 
 		strBuff.insert( 0, string );
 		return strBuff.toString();
 	}
 
 	/**
 	 * Adjusts a String to a certain length, perfect to use when creating fixed field length text files.
 	 *
 	 * @author Hugi Þórðarson
 	 */
 	public static String adjustStringToLength( String string, int desiredLength ) {
 
 		if( string == null )
 			return padString( null, desiredLength );
 
 		int originalLength = string.length();
 
 		if( originalLength == desiredLength ) {
 			return string;
 		}
 
 		if( originalLength > desiredLength ) {
 			return string.substring( 0, desiredLength );
 		}
 
 		if( originalLength < desiredLength ) {
 			return padString( string, desiredLength );
 		}
 
 		return null;
 	}
 
 	/**
 	 * Writes the given string to a file, using the given encoding.
 	 */
 	public static void writeStringToFileUsingEncoding( String sourceString, File destination, String encoding ) {
 
 		if( encoding == null ) {
 			encoding = DEFAULT_ENCODING;
 		}
 
 		try {
 			byte[] bytes = sourceString.getBytes( encoding );
 			USDataUtilities.writeBytesToFile( bytes, destination );
 		}
 		catch( UnsupportedEncodingException e ) {
 			logger.error( "Failed to write string to file: " + destination, e );
 		}
 	}
 
 	/**
 	  * Fetches the given string from an URL, using the given encoding.
 	  */
 	public static String readStringFromURLUsingEncoding( String sourceURL, String encoding ) {
 
 		if( encoding == null ) {
 			encoding = DEFAULT_ENCODING;
 		}
 
 		try {
 			return stringFromURL( new URL( sourceURL ), encoding );
 		}
 		catch( Exception e ) {
 			logger.error( "Could not read string from URL", e );
 			return null;
 		}
 	}
 
 	/**
 	 * Reads the given string from an Inputstream, using the given encoding.
 	 */
 	public static String readStringFromInputStreamUsingEncoding( InputStream in, String encoding ) {
 
 		if( encoding == null ) {
 			encoding = DEFAULT_ENCODING;
 		}
 
 		try {
 			StringBuffer out = new StringBuffer();
 			byte[] b = new byte[4096];
 
 			for( int n; (n = in.read( b )) != -1; ) {
 				out.append( new String( b, 0, n ) );
 			}
 
 			return out.toString();
 		}
 		catch( Exception e ) {
 			logger.error( "Could not read string from Inputstream", e );
 			return null;
 		}
 	}
 
 	/**
 	 * Replaces a substring with another string in the buffer.
 	 */
 	public static String replace( String buffer, String old, String newString ) {
 
 		if( buffer == null ) {
 			return null;
 		}
 
 		if( old == null ) {
 			return buffer;
 		}
 
 		if( newString == null ) {
 			newString = "";
 		}
 
 		return replaceStringByStringInString( old, newString, buffer );
 	}
 
 	/**
 	 * Reads a string from a file, using the given encoding. 
 	 */
 	public static String readStringFromFileUsingEncoding( File sourceFile, String encoding ) {
 
 		if( encoding == null ) {
 			encoding = DEFAULT_ENCODING;
 		}
 
 		byte[] bytes = USDataUtilities.readBytesFromFile( sourceFile );
 		return stringFromDataUsingEncoding( bytes, encoding );
 	}
 
 	/**
 	 * Attempts to format a user entered string to the standard used in the DB.
 	 *
 	 * @param permno The permno to format.
 	 * @author Hugi Þórðarson
 	 */
 	public static String cleanupPermno( String permno ) {
 
 		if( stringHasValue( permno ) ) {
 			permno = replace( permno, "-", "" );
 			permno = replace( permno, " ", "" );
 			permno = permno.toUpperCase();
 		}
 
 		return permno;
 	}
 
 	/**
 	 * Attempts to format a string to the standard used in the DB.
 	 *
 	 * @param regno The permno to format.
 	 * @author Hugi Þórðarson
 	 */
 	public static String cleanupRegno( String regno ) {
 
 		if( stringHasValue( regno ) ) {
 			regno = replace( regno, "-", "" );
 			regno = replace( regno, " ", "" );
 			regno = regno.toUpperCase();
 		}
 
 		return regno;
 	}
 
 	/**
 	 * Convert icelandic characters to English equivalents and remove spaces.
 	 */
 	public static String replaceIcelandicCharsAndRemoveSpaces( String s ) {
 		s = replaceStringByStringInString( " ", "", s );
 		s = replaceStringByStringInString( "Ö", "O", s );
 		s = replaceStringByStringInString( "É", "E", s );
 		s = replaceStringByStringInString( "Ý", "Y", s );
 		s = replaceStringByStringInString( "Ú", "U", s );
 		s = replaceStringByStringInString( "Í", "I", s );
 		s = replaceStringByStringInString( "Ó", "O", s );
 		s = replaceStringByStringInString( "Ð", "D", s );
 		s = replaceStringByStringInString( "Á", "A", s );
 		s = replaceStringByStringInString( "Æ", "AE", s );
 		s = replaceStringByStringInString( "Þ", "TH", s );
 		s = replaceStringByStringInString( "ö", "o", s );
 		s = replaceStringByStringInString( "é", "e", s );
 		s = replaceStringByStringInString( "ý", "y", s );
 		s = replaceStringByStringInString( "ú", "u", s );
 		s = replaceStringByStringInString( "í", "i", s );
 		s = replaceStringByStringInString( "ó", "o", s );
 		s = replaceStringByStringInString( "ð", "d", s );
 		s = replaceStringByStringInString( "á", "a", s );
 		s = replaceStringByStringInString( "æ", "ae", s );
 		s = replaceStringByStringInString( "þ", "th", s );
 		return s;
 	}
 
 	/**
 	 * Checks if all the objects in the given array are null.
 	 * 
 	 * @param objects The objects to check. Should never be null.
 	 */
 	public static int numberOfStringsWithValue( String... strings ) {
 		if( strings == null ) {
 			return 0;
 		}
 
 		int i = 0;
 
 		for( String s : strings ) {
 			if( USStringUtilities.stringHasValue( s ) )
 				i++;
 		}
 
 		return i;
 	}
 
 	/**
 	 * Checks if all the objects in the given array are null.
 	 * 
 	 * @param objects The objects to check. Should never be null.
 	 */
 	public static boolean anyStringHasValue( String... strings ) {
 		if( strings == null ) {
 			return false;
 		}
 		for( String s : strings ) {
 			if( USStringUtilities.stringHasValue( s ) )
 				return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Checks for the total number of wild card characters in the string.
 	 * 
 	 * Wildcard characters are "*" and "?".
 	 */
 	public static final int numberOfWildcardCharactersInString( String string ) {
 
 		if( !stringHasValue( string ) )
 			return 0;
 
 		int questionMarkCount = numberOfOccurrencesOfCharInString( '?', string );
 		int starCount = numberOfOccurrencesOfCharInString( '*', string );
 		return questionMarkCount + starCount;
 	}
 
 	/**
 	 * Checks for the total number of non wild card characters in the string.
 	 * 
 	 * Wildcard characters are "*" and "?".
 	 */
 	public static final int numberOfNonWildcardCharactersInString( String string ) {
 
 		if( !stringHasValue( string ) )
 			return 0;
 
 		return string.length() - numberOfWildcardCharactersInString( string );
 	}
 
 	/**
 	 * Replaces variable markers in originalString with the objects in the object 
 	 */
 	public static String stringWithFormat( String originalString, Object... objects ) {
 
 		if( originalString == null )
 			return null;
 
 		String VARIABLE = "\\{\\}";
 		String returnString = originalString;
 
 		for( int i = 0; i < objects.length; i++ ) {
 			Object replacement = objects[i];
 
 			if( replacement == null )
 				replacement = "null";
 
 			returnString = returnString.replaceFirst( VARIABLE, replacement.toString() );
 		}
 
 		return returnString;
 	}
 
 	/**
 	 * This method converts all instances of \n in a string to the HTML-equivalent <br />
 	 *
 	 * @param string The string to work on
 	 */
 	public static String convertBreakString( String string ) {
 		return replace( string, "\n", "<br />\n" );
 	}
 
 	/**
 	 * This method checks the ending of a string in a case insensitive manner.
 	 * If either of the parameters passed is null, "false" is returned.
 	 *
 	 * @param aString The string to check
 	 * @param suffix The suffix to check for
 	 */
 
 	public static boolean caseInsensitiveEndsWith( String aString, String suffix ) {
 
 		if( aString == null || suffix == null )
 			return false;
 
 		if( aString.toLowerCase().endsWith( suffix.toLowerCase() ) )
 			return true;
 
 		return false;
 	}
 
 	/**
 	 * Determines the last element of a path, i.e. a filename.
 	 *
 	 * @param sourceURL The URL to download from
 	 * @param encoding The encoding to use
 	 */
 	public static String fileNameFromPath( String aString ) {
 
 		if( aString == null )
 			return null;
 
 		int i = aString.lastIndexOf( "\\" );
 
 		if( i == -1 )
 			i = aString.lastIndexOf( "/" );
 
 		return aString.substring( i + 1, aString.length() );
 	}
 
 	/**
 	 * Goes through a string and performs the following changes:
 	 * 
 	 *  - Activates URLs.
 	 *  - Inserts br-tags
 	 */
 	public static String htmlify( String originalString ) {
 
 		String s = originalString;
 
 		if( s != null ) {
 			s = convertBreakString( s );
 			s = activateHyperlinksInString( s );
 			s = s.trim();
 		}
 
 		return s;
 	}
 
 	/**
 	 * Creates active, working hyperlinks.
 	 */
 	public static String activateHyperlinksInString( String string ) {
 
 		if( string == null )
 			return null;
 
		return string.replaceAll( "(((ht|f)(tp)(s?)://)([a-zA-Z0-9áÁðÐéÉíÍóÓuÚýÝþÞæÆöÖ]*(.))?[a-zA-Z0-9áÁðÐéÉíÍóÓuÚýÝþÞæÆöÖ]*((\\.)[a-zA-Z0-9áÁðÐéÉíÍóÓuÚýÝþÞæÆöÖ]{2,5})(/[\\w|\\d|\\.|%|&|;|=|\\?]*)*)(\\S)", "<a href=\"$0\">$0</a>" );
 	}
 
 	/**
 	 * Creates a link to each user in a tweet.
 	 */
 	public static String activateTwitterUsersInString( String string ) {
 
 		if( string == null )
 			return null;
 
 		return string.replaceAll( "@(\\w+)", "<a href=\"http://www.twitter.com/$1\">@$1</a>" );
 	}
 
 	/**
 	 * Attempts to fix an URL-string entered by a user. 
 	 */
 	public static String fixUrl( String url ) {
 
 		if( !stringHasValue( url ) )
 			return null;
 
 		url = url.trim().toLowerCase();
 
 		if( url.indexOf( "http://" ) == -1 )
 			url = "http://" + url;
 
 		return url;
 	}
 
 	/**
 	 * Takes a dictionary that contains strings, loops though the dictionary's keys and replaces with the values (both strings).
 	 * 
 	 * If the replacement dictionary is null, returns the original string unaltered.
 	 */
 	public static String replaceInStringWithValuesFromMap( String originalString, Map<String, String> replacements ) {
 
 		if( !stringHasValue( originalString ) )
 			return null;
 
 		if( replacements == null ) {
 			return originalString;
 		}
 
 		for( String key : replacements.keySet() ) {
 			if( stringHasValue( key ) ) {
 				String object = replacements.get( key );
 				originalString = replace( originalString, key, object );
 			}
 		}
 
 		return originalString;
 	}
 
 	/**
 	 * Abbreviates a string if it exceeds length and appends three dots. 
 	 */
 	public static String abbrString( String s, int length ) {
 		if( s == null ) {
 			return "";
 		}
 		String trailer = "...";
 		int trailerLen = trailer.length();
 		int newLen = (length > trailerLen) ? length - trailerLen : length;
 		if( s != null && s.length() > length && newLen >= 0 ) {
 			return s.substring( 0, newLen ) + "...";
 		}
 
 		return s;
 	}
 
 	/**
 	 * Formats bytes into human readable representation of it.
 	 * Using all default parameters.
 	 * 
 	 * @param 	bytes 			The bytes to format
 	 * @return 	A formated String representation of the bytes
 	 * @author 	Bjarni Sævarsson
 	 */
 	public static String formatBytes( double bytes ) {
 		return formatBytes( bytes, 0, 2, "", false );
 	}
 
 	/**
 	 * Formats bytes into human readable representation of it.
 	 * Using all default parameters.
 	 * 
 	 * @param 	bytes 			The bytes to format
 	 * @return 	A formated String representation of the bytes
 	 * @author 	Bjarni Sævarsson
 	 */
 	public static String formatBytes( double bytes, boolean showFullName ) {
 		String devider = (showFullName) ? " " : "";
 		return formatBytes( bytes, 0, 2, devider, showFullName );
 	}
 
 	/**
 	 * Formats bytes into human readable representation of it.
 	 * 
 	 * @param 	bytes 			The bytes to format
 	 * @param 	minFractions 	Sets the minimum number of digits allowed in the fraction portion of a number
 	 * @param 	maxFractions 	Sets the maximum number of digits allowed in the fraction portion of a number
 	 * @return 	A formated String representation of the bytes
 	 * @author 	Bjarni Sævarsson
 	 */
 	public static String formatBytes( double bytes, int minFractions, int maxFractions ) {
 		return formatBytes( bytes, minFractions, maxFractions, "", false );
 	}
 
 	/**
 	 * Formats bytes into human readable representation of it.
 	 * 
 	 * @param 	bytes 			The bytes to format
 	 * @param 	minFractions 	Sets the minimum number of digits allowed in the fraction portion of a number
 	 * @param 	maxFractions 	Sets the maximum number of digits allowed in the fraction portion of a number
 	 * @param 	nrNameDivider 	The string placed between the formated number and the binary symbol/name
 	 * @return 	A formated String representation of the bytes
 	 * @author 	Bjarni Sævarsson
 	 */
 	public static String formatBytes( double bytes, int minFractions, int maxFractions, String nrNameDivider ) {
 		return formatBytes( bytes, minFractions, maxFractions, nrNameDivider, false );
 	}
 
 	/**
 	 * Formats bytes into human readable representation of it.
 	 * 
 	 * @param 	bytes 			The bytes to format
 	 * @param 	minFractions 	Sets the minimum number of digits allowed in the fraction portion of a number
 	 * @param 	maxFractions 	Sets the maximum number of digits allowed in the fraction portion of a number
 	 * @param 	nrNameDivider 	The string placed between the formated number and the binary symbol/name
 	 * @param 	showFullName 	Should the full binary name be shown
 	 * @return 	A formated String representation of the bytes
 	 * @author 	Bjarni Sævarsson
 	 */
 	public static String formatBytes( double bytes, int minFractions, int maxFractions, String nrNameDivider, boolean showFullName ) {
 		int divCounter = 0;
 		double divBytes = bytes;
 
 		while( divBytes >= 1024 ) {
 			divBytes = divBytes / 1024; // 1024 is the binary thousand so no need to get any knickers in a twist for not using a constant for it
 			divCounter++;
 		}
 
 		String devider = (nrNameDivider == null) ? "" : nrNameDivider;
 
 		String ret = formatDouble( divBytes, minFractions, maxFractions, false );
 		ret += devider + ((showFullName) ? BINARY_SIZE_NAME[divCounter] : BINARY_SIZE_SYMBOL[divCounter]);
 
 		return ret;
 	}
 
 	/**
 	 * Formats double into human readable string representation of it.
 	 * 
 	 * @param 	number 				The number to be formated
 	 * @return 	A formated String representation of the double number
 	 * @author 	Bjarni Sævarsson
 	 */
 	public static String formatDouble( double number ) {
 		return formatDouble( number, 0, 2, false );
 	}
 
 	/**
 	 * Formats double into human readable string representation of it.
 	 * 
 	 * @param 	number 				The number to be formated
 	 * @param 	nrOfDecimalPlaces 	Number of decimal places to round to
 	 * @return 	A formated String representation of the double number
 	 * @author 	Bjarni Sævarsson
 	 */
 	public static String formatDouble( double number, int nrOfDecimalPlaces ) {
 		return formatDouble( number, nrOfDecimalPlaces, nrOfDecimalPlaces, false );
 	}
 
 	/**
 	 * Formats double into human readable string representation of it.
 	 * 
 	 * @param 	number 				The number to be formated
 	 * @param 	nrOfDecimalPlaces 	Number of decimal places to round to
 	 * @param 	forceDecimalPlaces 	Should the decimal places be forced, e.g. 1.000,00
 	 * @return 	A formated String representation of the double number
 	 * @author 	Bjarni Sævarsson
 	 */
 	public static String formatDouble( double number, int nrOfDecimalPlaces, boolean forceDecimalPlaces ) {
 		return formatDouble( number, 0, nrOfDecimalPlaces, forceDecimalPlaces );
 	}
 
 	/**
 	 * Formats double into human readable string representation of it.
 	 * 
 	 * @param 	number 				The number to be formated
 	 * @param 	nrOfDecimalPlaces 	Number of decimal places to round to
 	 * @param 	forceDecimalPlaces 	Should the decimal places be forced, e.g. 1.000,00
 	 * @return 	A formated String representation of the double number
 	 * @author 	Bjarni Sævarsson
 	 */
 	public static String formatDouble( double number, int nrOfDecimalPlaces, int maxNrOfDecimalPlaces, boolean forceDecimalPlaces ) {
 		DecimalFormat fmt = new DecimalFormat( ICELANDIC_DECIMAL_FORMAT_PATTERN, ICELANDIC_DECIMAL_FORMAT_SYMBOLS );
 
 		if( forceDecimalPlaces ) {
 			fmt.setMinimumFractionDigits( maxNrOfDecimalPlaces );
 		}
 		else {
 			fmt.setMinimumFractionDigits( nrOfDecimalPlaces );
 		}
 		fmt.setMaximumFractionDigits( maxNrOfDecimalPlaces );
 
 		String formatedNumber = fmt.format( number );
 
 		if( formatedNumber.startsWith( ICELANDIC_DECIMAL_FORMAT_SYMBOLS.getDecimalSeparator() + "" ) ) {
 			formatedNumber = "0" + formatedNumber;
 		}
 
 		return formatedNumber;
 	}
 
 	/**
 	 * Formats the user entered number to the database standard format.
 	 *  
 	 *  Numbers:
 	 *  - Contain 6 characters, 3 numeric, then three upper-case alphabetic characters (123ABC).
 	 *  - Contain only alphanumeric characters.
 	 *  
 	 *  Returns null if the number is not valid!
 	 */
 	public static String cleanupPreRegistrationNumber( String s ) {
 
 		if( s == null )
 			return null;
 
 		s = s.trim();
 		s = s.toUpperCase();
 		if( s.length() > 6 ) {
 			s = s.substring( 0, 6 );
 		}
 
 		if( !Pattern.matches( "\\p{Digit}{3}\\p{Upper}{3}", s ) ) {
 			return null;
 		}
 
 		return s;
 	}
 
 	/**
 	 * Un-escapes the most common escaped non-ASCII characters.
 	 * 
 	 * @param s String to parse
 	 * @return String with un-escaped non-ascii characters
 	 * @author Bjarni Sævarsson
 	 */
 	public static final String unescapeHTML( String s ) {
 		if( s == null ) {
 			return "";
 		}
 
 		char entitieStart = '&';
 		char entitieStop = ';';
 		StringBuffer out = new StringBuffer();
 		int entityIdx = 0;
 		int entitiesSize = escapeChart.size();
 		String[][] htmlEntities = new String[entitiesSize][2];
 		for( String[] entryVal : escapeChart.values() ) {
 			htmlEntities[entityIdx++] = new String[] { entryVal[1], entryVal[2] };
 		}
 		for( int e = 0; e < htmlEntities.length; e++ ) {
 			//			System.out.println( htmlEntities[e][1] );
 		}
 		int lastIdx = 0;
 		// go through the string and find an entitie start char
 		for( int i = 0; i < s.length(); i++ ) {
 			if( s.charAt( i ) == entitieStart ) {
 				// find an entitie stop char
 				for( int j = i + 1; j < s.length(); j++ ) {
 					if( s.charAt( j ) == entitieStop ) {
 						String entitie = s.substring( i, j + 1 );
 						// find an equivalent entry in the htmlEntities array
 						for( int e = 0; e < htmlEntities.length; e++ ) {
 							if( htmlEntities[e][1].equals( entitie ) ) {
 								// substitute the escaped char with the actual char
 								out.append( s.substring( lastIdx, i ) );
 								out.append( htmlEntities[e][0] );
 								lastIdx = j + 1;
 								break;
 							}
 						}
 						break;
 					}
 				}
 			}
 		}
 		// append the rest of the string
 		if( lastIdx < s.length() )
 			out.append( s.substring( lastIdx ) );
 
 		return out.toString();
 	}
 
 	/**
 	 * Escapes the most common non-ASCII characters.
 	 * 
 	 * @param s String to parse
 	 * @return String with escaped non-ascii characters
 	 * @author Bjarni Sævarsson
 	 */
 	public static final String escapeHTML( String s ) {
 		if( s == null ) {
 			return "";
 		}
 		StringBuffer buffer = new StringBuffer();
 		int n = s.length();
 		for( int i = 0; i < n; i++ ) {
 			char c = s.charAt( i );
 			if( escapeChart.containsKey( (int)c ) ) {
 				buffer.append( escapeChart.get( (int)c )[2] );
 			}
 			else {
 				buffer.append( c );
 			}
 		}
 		return buffer.toString();
 	}
 
 	/**
 	 * Creates a complete url, starting with the base url, appending each parameter in query string format.
 	 * 
 	 * @param baseURL The baseURL to use.
 	 * @param parameters A dictionary of URL parameters to append to the base url.
 	 * @return A complete URL
 	 */
 	public static String constructURLStringWithParameters( String baseURL, Map<String, String> parameters ) {
 
 		if( !stringHasValue( baseURL ) )
 			baseURL = "";
 
 		StringBuffer b = new StringBuffer();
 		b.append( baseURL );
 
 		if( parameters == null ) {
 			return b.toString();
 		}
 
 		if( parameters.size() > 0 ) {
 			b.append( "?" );
 
 			for( String nextKey : parameters.keySet() ) {
 				String nextValue = parameters.get( nextKey );
 
 				b.append( nextKey );
 				b.append( "=" );
 
 				if( nextValue != null )
 					b.append( nextValue );
 				else
 					b.append( "" );
 
 				b.append( "&" );
 			}
 		}
 
 		return b.toString();
 	}
 
 	/*------ The code below is ripped from the Project Wonder frameworks. ---------*/
 
 	/** removes any character which is in characters from the source string
 	 * 
 	 * @param source the string which will be modified
 	 * @param characters the characters that are not allowed to be in source
 	 * @return a new string without any characters from the characters argument
 	 */
 	private static String removeCharacters( String source, String characters ) {
 		StringBuffer buf = new StringBuffer();
 		int l = source.length();
 		for( int i = 0; i < l; i++ ) {
 			char c = source.charAt( i );
 			if( characters.indexOf( c ) == -1 ) {
 				buf.append( c );
 			}
 		}
 		return buf.toString();
 	}
 
 	/**
 	* Replaces a given string by another string in a string.
 	* @param old string to be replaced
 	* @param newString to be inserted
 	* @param buffer string to have the replacement done on it
 	* @return string after having all of the replacement done.
 	*/
 	private static String replaceStringByStringInString( String old, String newString, String buffer ) {
 		int begin, end;
 		int oldLength = old.length();
 		int length = buffer.length();
 		StringBuffer convertedString = new StringBuffer( length + 100 );
 
 		begin = 0;
 		while( begin < length ) {
 			end = buffer.indexOf( old, begin );
 			if( end == -1 ) {
 				convertedString.append( buffer.substring( begin ) );
 				break;
 			}
 			if( end == 0 )
 				convertedString.append( newString );
 			else {
 				convertedString.append( buffer.substring( begin, end ) );
 				convertedString.append( newString );
 			}
 			begin = end + oldLength;
 		}
 		return convertedString.toString();
 	}
 
 	/**
 	 * Counts the number of occurrences of a particular
 	 * <code>char</code> in a given string.
 	 * @param c char to count in string
 	 * @param s string to look for specified char in.
 	 * @return number of occurences of a char in the string
 	 */
 	private static int numberOfOccurrencesOfCharInString( char c, String s ) {
 		int result = 0;
 		if( s != null ) {
 			for( int i = 0; i < s.length(); )
 				if( s.charAt( i++ ) == c )
 					result++;
 		}
 		return result;
 	}
 
 	/**
 	 * Returns a string from the contents of the given URL.
 	 * 
 	 * @param url the URL to read from
 	 * @return the string that was read
 	 * @throws IOException if the connection fails
 	 */
 	private static String stringFromURL( URL url, String encoding ) {
 		InputStream is = null;
 		String result = null;
 
 		try {
 			is = url.openStream();
 			result = readStringFromInputStreamUsingEncoding( is, encoding );
 		}
 		catch( Exception e ) {
 			logger.error( "Error when downloading string from url: " + url, e );
 		}
 		finally {
 			try {
 				is.close();
 			}
 			catch( IOException e ) {
 				logger.error( "Error when downloading string from url: " + url, e );
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Missing comments! 
 	 */
 	public static boolean isNumeric( String nr ) {
 		try {
 			Double.parseDouble( nr );
 		}
 		catch( NumberFormatException e ) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Missing comments! 
 	 */
 	public static boolean isNatural( String nr ) {
 		int maxDigits = (Long.MAX_VALUE + "").length();
 		if( nr.matches( "[0-9]{1," + maxDigits + "}" ) ) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Checks if the specified String contains only digits. 
 	 * 
 	 * @param aString the string to check
 	 * @return true if the string contains only digits, false otherwise
 	 */
 	public static boolean isDigitsOnly( String aString ) {
 		for( int i = aString.length(); i-- > 0; ) {
 			char c = aString.charAt( i );
 			if( !Character.isDigit( c ) )
 				return false;
 		}
 		return true;
 	}
 }
