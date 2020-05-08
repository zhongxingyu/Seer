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
 package novelang.rendering.xslt;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.joda.time.ReadableDateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 
 import java.io.ByteArrayOutputStream;
 import java.io.StringWriter;
 import java.io.StringReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.PrintStream;
 
 import novelang.common.LanguageTools;
 
 /**
  * @author Laurent Caillette
  */
 public class Numbering {
 
   private static final Logger LOGGER = LoggerFactory.getLogger( Numbering.class ) ;
   private static final DateTimeFormatter DATE_TIME_FORMATTER_NOSEPARATOR = DateTimeFormat.forPattern( "yyyyMMddkkmm" );
 
   /**
    * Just to get an easy case.
    * @return The constant String "{@code Hello!}".
    */
   public static String hello() {
     return "Hello!" ;
   }
 
   public static String asString( Object treeResultFragment ) {
     return asString( "", treeResultFragment ) ;
   }
 
 
   public static String numberAsText(
       Object numberObject,
       Object localeNameObject,
       Object caseObject
   ) {
     final String numberAsString = asString( "number", numberObject ) ;
     final String localeAsString = asString( "locale", localeNameObject ) ;
     final String caseAsString = asString( "case", caseObject ) ;
 
     final CaseType caseType ;
 
     if( caseObject instanceof String ) {
       CaseType possibleCaseType ;
       try {
         possibleCaseType = CaseType.valueOf( ( ( String ) caseObject ).toUpperCase() ) ;
       } catch( IllegalArgumentException e ) {
         LOGGER.warn(
             "Not a supported case type: {} (supported: {})",
             caseObject,
             ArrayUtils.toString( CaseType.values() )
         ) ;
         possibleCaseType = CaseType.LOWER ;
       }
       caseType = possibleCaseType ;
     } else {
       LOGGER.warn( "Case type is not a String: {}", caseAsString ) ;
       caseType = CaseType.LOWER ;
     }
 
 
     final String numberAsText ;
 
     if( numberObject instanceof Number ) {
       final Number number = ( Number ) numberObject ;
       if( "FR".equals( localeNameObject ) ) {
         numberAsText = numberAsLocalizedText( number.intValue(), TO_FRENCH_TEXT, caseType ) ;
       } else if( "EN".equals( localeNameObject ) ) {
         numberAsText = numberAsLocalizedText( number.intValue(), TO_ENGLISH_TEXT, caseType ) ;
       } else {
         LOGGER.warn( "Locale not supported: " + localeAsString ) ;
         numberAsText = number.toString() ;
       }
       return numberAsText ;
 
     } else {
       final String message = "!NAN! Cannot convert to number: " + numberAsString ;
       LOGGER.error( message ) ;
       return message ;
     }
   }
 
   private enum CaseType {
     LOWER, UPPER, CAPITAL ;
   }
 
   private static String numberAsLocalizedText(
       int number,
       Function< Number, String > textualizer,
       CaseType caseType
   ) {
     switch( caseType ) {
       case CAPITAL:
         final String s = textualizer.apply( number ) ;
         return s.substring( 0, 1 ).toUpperCase() + s.substring( 1, s.length() ) ;
       case LOWER:
         return textualizer.apply( number ) ;
       case UPPER:
         return textualizer.apply( number ).toUpperCase() ;
       default :
         LOGGER.warn( "Unsupported case: {}", caseType ) ;
         return textualizer.apply( number ) ;
     }
 
   }
 
 
   private static final Function< Number, String > TO_FRENCH_TEXT =
       new Function< Number, String >(){
         public String apply( Number number ) {
           switch( number.intValue() ) {
           case 0  : return "zro" ;
           case 1  : return "un" ;
           case 2  : return "deux" ;
           case 3  : return "trois" ;
           case 4  : return "quatre" ;
           case 5  : return "cinq" ;
           case 6  : return "six" ;
           case 7  : return "sept" ;
           case 8  : return "huit" ;
           case 9  : return "neuf" ;
           case 10 : return "dix" ;
           case 11 : return "onze" ;
           case 12 : return "douze" ;
           case 13  : return "treize" ;
           case 14  : return "quatorze" ;
           case 15  : return "quinze" ;
           case 16  : return "seize" ;
           case 17  : return "dix-sept" ;
           case 18  : return "dix-huit" ;
           case 19  : return "dix-neuf" ;
           case 20  : return "vingt" ;
          case 21  : return "vingt et un" ;
           case 22  : return "vingt-deux" ;
           case 23  : return "vingt-trois" ;
           case 24  : return "vingt-quatre" ;
           case 25  : return "vingt-cinq" ;
           case 26  : return "vingt-six" ;
           case 27  : return "vingt-sept" ;
           case 28  : return "vingt-huit" ;
           case 29  : return "vingt-neuf" ;
           case 30  : return "trente" ;
           case 31  : return "trente-et-un" ;
           case 32  : return "trente-deux" ;
           case 33  : return "trente-trois" ;
           case 34  : return "trente-quatre" ;
           case 35  : return "trente-cinq" ;
           case 36  : return "trente-six" ;
           case 37  : return "trente-sept" ;
           case 38  : return "trente-huit" ;
           case 39  : return "trente-neuf" ;
           case 40  : return "quarante" ;
           case 41  : return "quarante-et-un" ;
           case 42  : return "quarante-deux" ;
           case 43  : return "quarante-trois" ;
           case 44  : return "quarante-quatre" ;
           case 45  : return "quarante-cinq" ;
           case 46  : return "quarante-six" ;
           case 47  : return "quarante-sept" ;
           case 48  : return "quarante-huit" ;
           case 49  : return "quarante-neuf" ;
           case 50  : return "cinquante" ;
 
           default : throw new UnsupportedOperationException( "Not supported: " + number.intValue() ) ;
         }
       }
   } ;
 
   private static final Function< Number, String > TO_ENGLISH_TEXT =
       new Function< Number, String >(){
         public String apply( Number number ) {
           switch( number.intValue() ) {
           case 0  : return "zero" ;
           case 1  : return "one" ;
           case 2  : return "two" ;
           case 3  : return "three" ;
           case 4  : return "four" ;
           case 5  : return "five" ;
           case 6  : return "six" ;
           case 7  : return "seven" ;
           case 8  : return "eight" ;
           case 9  : return "nine" ;
           case 10  : return "ten" ;
           case 11 : return "eleven" ;
           case 12 : return "twelve" ;
           case 13 : return "thirteen" ;
           case 14  : return "fourteen" ;
           case 15  : return "fifteen" ;
           case 16  : return "sixteen" ;
           case 17  : return "seventeen" ;
           case 18  : return "eighteen" ;
           case 19  : return "nineteen" ;
           case 20  : return "twenty" ;
           case 21  : return "twenty-one" ;
           case 22  : return "twenty-two" ;
           case 23  : return "twenty-three" ;
           case 24  : return "twenty-four" ;
           case 25  : return "twenty-five" ;
           case 26  : return "twenty-six" ;
           case 27  : return "twenty-seven" ;
           case 28  : return "twenty-eight" ;
           case 29  : return "twenty-nine" ;
           case 30  : return "thirty" ;
           case 31  : return "thirty-one" ;
           case 32  : return "thirty-two" ;
           case 33  : return "thirty-three" ;
           case 34  : return "thirty-four" ;
           case 35  : return "thirty-five" ;
           case 36  : return "thirty-six" ;
           case 37  : return "thirty-seven" ;
           case 38  : return "thirty-eight" ;
           case 39  : return "thirty-nine" ;
           case 40  : return "fourty" ;
           case 41  : return "fourty-one" ;
           case 42  : return "fourty-two" ;
           case 43  : return "fourty-three" ;
           case 44  : return "fourty-four" ;
           case 45  : return "fourty-five" ;
           case 46  : return "fourty-six" ;
           case 47  : return "fourty-seven" ;
           case 48  : return "fourty-eight" ;
           case 49  : return "fourty-nine" ;
           case 50  : return "fifty" ;
 
           default : throw new UnsupportedOperationException( "Not supported: " + number.intValue() ) ;
         }
       }
   } ;
 
   public static String asString( String name, Object object ) {
     return
         ( null == name ? "" : name + ": " )
       + "'" + object + "' "
       + ( null == object ? "" : object.getClass().getName() )
     ;
 
   }
 
   public static String formatDateTime(
       Object readableDateTimeObject,
       Object formatDescriptionObject
   ) {
     final ReadableDateTime readableDateTime = Preconditions.checkNotNull(
         ( ReadableDateTime ) readableDateTimeObject ) ;
     final String formatDescription = Preconditions.checkNotNull(
         ( String ) formatDescriptionObject ) ;
     if( "BASE36".equals( formatDescription ) ) {
       final DateTimeFormatter format = DATE_TIME_FORMATTER_NOSEPARATOR ;
       final String formattedString = format.print( readableDateTime ) ;
       final long number = Long.parseLong( formattedString ) ;
       return Long.toString( number, 36 ) ;
     } else {
       final DateTimeFormatter format = DateTimeFormat.forPattern( formatDescription ) ;
       return format.print( readableDateTime ) ;
     }
   }
 
   protected static ReadableDateTime unformatDateTime(
       String formattedDateTime,
       String formatDescription
   ) {
     if( "BASE36".equals( formatDescription ) ) {
       final Long decimalNumber = Long.parseLong( formattedDateTime, 36 ) ;
       final String decimalNumberAsString = decimalNumber.toString() ;
       return DATE_TIME_FORMATTER_NOSEPARATOR.parseDateTime( decimalNumberAsString ) ;
     } else {
       final DateTimeFormatter formatter = DateTimeFormat.forPattern( formatDescription ) ;
       return formatter.parseDateTime( formattedDateTime ) ;
     }
 
   }
 
 
 }
