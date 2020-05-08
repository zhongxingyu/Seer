 /*
  * Copyright (c) 2008-2010 David Kellum
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package iudex.util;
 
 import java.nio.CharBuffer;
 
 import com.gravitext.util.CharSequences;
 import com.gravitext.util.ResizableCharBuffer;
 
 /**
  * Utility character classifications and transforms.
  */
 public final class Characters
 {
     /**
      * Replace sequences of isCtrlWS() characters with a single SPACE,
      * plus trims leading/trailing.
      */
     public static CharBuffer cleanCtrlWS( final CharSequence in )
     {
         return replaceCtrlWS( in, " " );
     }
 
     /**
      * Replace sequences of isCtrlWS() characters with a specified
      * replacement sequence, plus trims leading/trailing.
      */
     public static CharBuffer replaceCtrlWS( final CharSequence in,
                                             final CharSequence rep )
     {
         return replaceCtrlWS( in, rep, true );
     }
 
     /**
      * Replace sequences of isCtrlWS() characters with a specified
      * replacement sequence, and trim leading/trailing if specified.
      */
     public static CharBuffer replaceCtrlWS( final CharSequence in,
                                             final CharSequence rep,
                                             final boolean doTrim )
     {
         int size_est = in.length();
         if( rep.length() > 1 ) size_est += ( 8 * ( rep.length() - 1 ) );
         final ResizableCharBuffer out = new ResizableCharBuffer( size_est );
 
         int i = 0, last = 0;
         final int end = in.length();
         boolean ws = false;
 
         while( i < end ) {
             if( isCtrlWS( in.charAt( i ) ) ) {
                 if( !ws ) {
                     out.append( in, last, i );
                     ws = true;
                 }
             }
             else if( ws ) {
                 if( out.position() > 0 || !doTrim ) out.append( rep );
                 last = i;
                 ws = false;
             }
             ++i;
         }
         if( ws ) {
             if( !doTrim ) out.append( rep );
         }
         else {
             out.append( in, last, end );
         }
         return out.flipAsCharBuffer();
     }
 
     /**
      * Replace control characters (isCtrlWS() minus the set of isHTMLWS())
      * with a specified replacement character.
      */
     public static CharBuffer replaceCtrl( final CharSequence in,
                                           final char rep )
     {
         CharBuffer b = CharSequences.writableCharBuffer( in );
 
         final int end = b.limit();
         for( int i = b.position(); i < end; ++i ) {
             char c = b.get( i );
             if( !isHTMLWS( c ) && isCtrlWS( c ) ) {
                 b.put( i, rep );
             }
         }
 
         return b;
     }
 
     /**
      * Return the number or words in sequence when tokenized by isCtrlWS()
      * and starting with a character of class LetterOrDigit.
      */
     public static int wordCount( final CharSequence in )
     {
         int i = 0;
         boolean nonWord = true;
         int count = 0;
         final int end = in.length();
         while( i < end ) {
             final char c = in.charAt( i );
             if( isCtrlWS( c ) ) {
                 nonWord = true;
             }
             else if( nonWord && Character.isLetterOrDigit( c ) ) {
                 nonWord = false;
                 ++count;
             }
 
             ++i;
         }
         return count;
     }
 
     /**
     * Return true if all characters in input are isCtrlWS().
      */
     public static boolean isEmptyCtrlWS( final CharSequence in )
     {
         int i = 0;
         final int end = in.length();
         while( i < end ) {
             if( ! isCtrlWS( in.charAt( i ) ) ) return false;
             ++i;
         }
         return true;
     }
 
     /**
      * Return true is c is a C0 or C1 control character, Unicode
      * defined whitespace, or BOM.
      */
     public static boolean isCtrlWS( final char c )
     {
         switch( c ) {
 
             /* C0 control characters (not allowed in XML) */
         case 0x0000: // NUL
         case 0x0001:
         case 0x0002:
         case 0x0003:
         case 0x0004:
         case 0x0005:
         case 0x0006:
         case 0x0007: // BEL
         case 0x0008: // BS
 
         case 0x0009: // HT
         case 0x000A: // LF
         case 0x000B: // VT
         case 0x000C: // FF
         case 0x000D: // CR
 
         case 0x000E: // SO
         case 0x000F: // SI
         case 0x0010: // DLE
         case 0x001B: // ES
 
         case 0x001C:
         case 0x001D:
         case 0x001E:
         case 0x001F:
         case 0x0020:
 
         case 0x007F: // DEL
 
             /* C1 control characters (not allowed in XML 1.0) */
         case 0x0080:
         case 0x0081:
         case 0x0082:
         case 0x0083:
         case 0x0084:
         case 0x0085: // NEL ~CR+LF
         case 0x0086:
         case 0x0087:
         case 0x0088:
         case 0x0089:
         case 0x008A:
         case 0x008B:
         case 0x008C:
         case 0x008D:
         case 0x008E:
         case 0x008F:
         case 0x0090:
         case 0x0091:
         case 0x0092:
         case 0x0093:
         case 0x0094:
         case 0x0095:
         case 0x0096:
         case 0x0097:
         case 0x0098:
         case 0x0099:
         case 0x009A:
         case 0x009B:
         case 0x009C:
         case 0x009D:
         case 0x009E:
         case 0x009F:
 
         case 0x00A0: // NBSP
         case 0x1680: // OGHAM SPACE MARK
         case 0x180E: // MONGOLIAN VOWEL SEPARATOR
 
         case 0x2000: // EN QUAD
         case 0x2001:
         case 0x2002:
         case 0x2003:
         case 0x2004:
         case 0x2005:
         case 0x2006:
         case 0x2007:
         case 0x2008:
         case 0x2009:
         case 0x200A:
         case 0x200B: // ZERO WIDTH SPACE
 
         case 0x2028: // LINE SEPARATOR
         case 0x2029: // PARAGRAPH SEPARATOR
 
         case 0x202F: // NARROW NO-BREAK SPACE
 
         case 0x205F: // MEDIUM MATHEMATICAL SPACE
 
         case 0x3000: // IDEOGRAPHIC SPACE
 
         case 0xFEFF: // BOM
         case 0xFFFE: // Bad BOM: (not assigned)
         case 0xFFFF: // Not assigned (invalid Unicode)
             return true;
         }
         return false;
     }
 
     /**
      * Return true if character is a recognized whitespace in HTML 4, and still
      * XML safe.
      */
     public static boolean isHTMLWS( final char c )
     {
         switch( c ) {
         case 0x0009: // HT
         case 0x000A: // LF
         case 0x000D: // CR
         case 0x0020: // SPACE
         case 0x200B: // ZERO WIDTH SPACE
             return true;
         }
         return false;
     }
 
 }
