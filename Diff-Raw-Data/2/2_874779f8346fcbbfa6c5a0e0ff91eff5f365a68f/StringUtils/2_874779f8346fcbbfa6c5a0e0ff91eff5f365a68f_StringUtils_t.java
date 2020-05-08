 package iniconfigurationmanager.utils;
 
 import iniconfigurationmanager.parsing.Format;
 
 /**
  * StringUtils unites usefull methods for working with strings.
  */
 public class StringUtils {
 
     /**
      * Trims unescaped whitespaces from the start and from the end of the text.
      *
      * @param String text
      * @return String
      */
     public static String trim( String text ) {
         String trimmedText = text.trim();
 
         if ( trimmedText.endsWith( "" + Format.ESCAPE ) ) {
             return trimmedText + Format.WHITESPACE;
         } else {
             return trimmedText;
         }
     }
 
 
     /**
      * Trims inline comments from the line.
      * 
      * @param String line
      * @return String
      */
     public static String trimInlineComments( String line ) {
         int length = line.length();
         char last = ' ';
 
         for ( int index = 0; index < line.length(); index++ ) {
             if ( line.charAt( index ) == Format.COMMENT_START &&
                     last != Format.ESCAPE ) {
                 length = index;
                 break;
             }
 
             last = line.charAt( index );
         }
 
         return line.substring( 0, length );
     }
 
 
     /**
      *  Formats comment by adding Format.COMMENT_START sign after every newline
      *  character.
      *
      * @param String comment
      * @return String
      */
     public static String formatComment( String comment ) {
         StringBuilder sb = new StringBuilder();
 
         for ( char ch : comment.trim().toCharArray() ) {
             sb.append( ch );
 
             if ( ch == Format.NEWLINE.charAt( 0 ) ) {
                 sb.append( Format.COMMENT_START );
             }
         }
 
         return sb.toString();
     }
 
 
     /**
      * Adds slashes before every  ,:;$ character.
      * 
      * @param String unslashedString
      * @return String
      */
     public static String addSlashes( String unslashedString ) {
         String slashedString = unslashedString;
 
         slashedString.replaceAll( ",", "\\," );
         slashedString.replaceAll( ":", "\\:" );
         slashedString.replaceAll( ";", "\\;" );
         slashedString.replaceAll( "$", "\\$" );
 
         return slashedString;
     }
 
 
     /**
      * Removes slashes before every ,:;$ character.
      * 
      * @param String slashedString
      * @return String
      */
     public static String removeSlashes( String slashedString ) {
         String unslashedString = slashedString;
 
         unslashedString.replaceAll( "\\,", "," );
         unslashedString.replaceAll( "\\:", ":" );
         unslashedString.replaceAll( "\\;", ";" );
        unslashedString.replaceAll( "\\\\$", "\\$" );
 
         return unslashedString;
     }
 }
