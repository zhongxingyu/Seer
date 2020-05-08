 /*
  * Copyright (C) 2008 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package novelang.parser;
 
 import java.util.Map;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import com.google.common.collect.BiMap;
 import com.google.common.collect.Maps;
 import com.google.common.collect.ImmutableMap;
 
 /**
  * Table of escaped symbols, using HTML entity names whenever defined and Unicode names otherwise.
  *
  * @author Laurent Caillette
  */
 public class Escape {
 
   private static final Logger LOGGER = LoggerFactory.getLogger( Escape.class ) ;
 
   private static final BiMap< String, Character > ESCAPED_CHARACTERS;
   private static final BiMap< String, Character > ESCAPED_HTML_CHARACTERS ;
 
   /**
    * Left-pointing double angle quotation mark "&#xab;".
    */
   public static Character ESCAPE_START = '\u00ab' ; // 
 
   /**
    * Right-pointing double angle quotation mark "&#xbb;".
    */
   public static Character ESCAPE_END = '\u00bb' ; // 
 
 
   static {
 
     final BiMap< String, Character > escapedCharacters = Maps.newHashBiMap() ;
     final BiMap< String, Character > escapedHtmlCharacters = Maps.newHashBiMap() ;
 
     // Symbols to keep after character escape refactoring.
 
     escapedCharacters.put( "startescape", ESCAPE_START ) ;
     escapedCharacters.put( "endescape", ESCAPE_END ) ;
     escapedCharacters.put( "lowerthan", '<' ) ;
     escapedCharacters.put( "greaterthan", '>' ) ;
     escapedCharacters.put( "inlinelitteral", '`' ) ;
 
     escapedCharacters.put( "oelig", '\u0153' ) ;
     escapedCharacters.put( "OElig", '\u0152' ) ;
 
     escapedHtmlCharacters.put( "oelig", '\u0153' ) ;
     escapedHtmlCharacters.put( "OElig", '\u0152' ) ;
     escapedHtmlCharacters.put( "amp", '&' ) ;
     escapedHtmlCharacters.put( "lt", '<' ) ;
     escapedHtmlCharacters.put( "gt", '>' ) ;
 
     // Those escapedCharacters aren't needed anymore.
 
 //    escapedCharacters.put( "apos", "'" ) ;
 //    escapedCharacters.put( "hellip", "\u2026" ) ;
 //    escapedCharacters.put( "percent", "%" ) ;
 //    escapedCharacters.put( "lcub", "{" ) ;
 //    escapedCharacters.put( "rcub", "}" ) ;
 //    escapedCharacters.put( "plus", "+" ) ;
 //    escapedCharacters.put( "equals", "=" ) ;
 //    escapedCharacters.put( "dollar", "$" ) ;
 //    escapedCharacters.put( "numbersign", "#" ) ;
 //    escapedCharacters.put( "colon", ":" ) ;
 //    escapedCharacters.put( "lowline", "_" ) ;
 //    escapedCharacters.put( "euro", "\u20ac" ) ;
 //    escapedCharacters.put( "amp", "&" ) ;
 //    escapedCharacters.put( "solidus", "/" ) ;
 //    escapedCharacters.put( "lt", "<" ) ;
 //    escapedCharacters.put( "tilde", "~" ) ;
 //    escapedCharacters.put( "rp", ")" ) ;
 //    escapedCharacters.put( "quot", "\"" ) ;
 //    escapedCharacters.put( "fullstop", "." ) ;
 //    escapedCharacters.put( "deg", "\u00b0" ) ;
 //
     ESCAPED_CHARACTERS = Maps.unmodifiableBiMap( escapedCharacters ) ;
     ESCAPED_HTML_CHARACTERS = Maps.unmodifiableBiMap( escapedHtmlCharacters ) ;
   }
 
   public static Map< String, Character > getCharacterEscapes() {
     return new ImmutableMap.Builder().putAll( ESCAPED_CHARACTERS ).build() ;
   }
 
   public static String escapeHtml( Character unescaped ) {
     return ESCAPED_HTML_CHARACTERS.inverse().get( unescaped ) ;
   }
 
   public static Character unescapeCharacter( String escaped )
       throws NoUnescapedCharacterException
   {
     final Character unescaped = ESCAPED_CHARACTERS.get( escaped ) ;
     if( null == unescaped ) {
       final NoUnescapedCharacterException exception = new NoUnescapedCharacterException( escaped ) ;
       LOGGER.warn( "Unsupported symbol", exception ) ;
       throw exception ;
     } else {
       return unescaped ;
     }
   }
 
 
   /**
    * Returns escaped symbol.
    * @param unescaped must not be null.
    * @return null if not found.
    */
   private static String escapeCharacter( Character unescaped ) {
     return ESCAPED_CHARACTERS.inverse().get( unescaped ) ;
   }
 
   public static String escapeText( String text ) {
     final StringBuffer buffer = new StringBuffer() ;
     for( char c : text.toCharArray() ) {
       final String escaped = escapeCharacter( c ) ;
       if( null == escaped ) {
         buffer.append( c ) ;
       } else {
        buffer.append( ESCAPE_END ).append( escaped ).append( ESCAPE_START ) ;
       }
     }
     return buffer.toString();
   }
 
   private static final Pattern PLAIN_ESCAPE_PATTERN =
       Pattern.compile( "(" + ESCAPE_START + "(\\w+)" + ESCAPE_END + ")" ) ;
   private static final Pattern HTML_ESCAPE_PATTERN =
       Pattern.compile( "(\\&(\\w+);)" ) ;
 
   static {
     LOGGER.debug( "Crafted regex {}", PLAIN_ESCAPE_PATTERN.pattern() ) ;
     LOGGER.debug( "Crafted regex {}", HTML_ESCAPE_PATTERN.pattern() ) ;
   }
 
   public static String unescapeText( String text )
       throws NoUnescapedCharacterException
   {
     final Matcher matcher = PLAIN_ESCAPE_PATTERN.matcher( text ) ;
     final StringBuffer buffer = new StringBuffer() ;
     int keepFrom = 0 ;
     while( matcher.find() ) {
       if( matcher.start() > 0 && keepFrom < text.length() ) {
         final String previous = text.substring( keepFrom, matcher.start() ) ;
         buffer.append( previous ) ;
       }
       final String escapeCode = matcher.group( 2 ) ;
       final Character escapedSymbol = Escape.unescapeCharacter( escapeCode ) ;
       buffer.append( escapedSymbol ) ;
       keepFrom = matcher.end() ;
     }
     if( keepFrom < text.length() ) {
       final String tail = text.substring( keepFrom, text.length() ) ;
       buffer.append( tail ) ;
     }
     
     return buffer.toString() ;
   }
 
   public static String escapeHtmlText( String text ) {
     final StringBuffer buffer = new StringBuffer() ;
     for( char c : text.toCharArray() ) {
       final String escaped = escapeHtml( c ) ;
       if( null ==  escaped ) {
         buffer.append( c ) ;
       } else {
         buffer.append( '&' ).append( escaped ).append( ";" ) ;
       }
     }
     return buffer.toString() ;
   }
 }
