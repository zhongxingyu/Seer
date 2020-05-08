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
 package novelang.parser.documentation;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.nio.charset.Charset;
 import java.util.Comparator;
 import java.util.List;
 
 import org.apache.commons.lang.CharUtils;
 import com.google.common.collect.Ordering;
 import novelang.parser.GeneratedLexemes;
 import novelang.parser.SourceUnescape;
 import novelang.parser.shared.Lexeme;
 import novelang.rendering.RenderingEscape;
 import novelang.system.DefaultCharset;
 
 /**
  * Generates a source document containing all characters supported in input.
  *
  * @author Laurent Caillette
  */
 public class SourceCharactersTable {
 
   public static void main( String[] args ) throws FileNotFoundException {
     if( args.length != 1 ) {
       throw new IllegalArgumentException( "Expected argument: destination file name" ) ;
     }
     new SourceCharactersTable( args[ 0 ], DefaultCharset.SOURCE ).writeSourceDocument().close() ;
   }
 
   private final RenderingEscape.CharsetEncodingCapability renderingCapability ;
   private final PrintWriter writer ;
 
   private SourceCharactersTable( String fileName, Charset sourceCharset ) throws FileNotFoundException {
     final FileOutputStream outputStream = new FileOutputStream( fileName ) ;
     final OutputStreamWriter outputStreamWriter =
         new OutputStreamWriter( outputStream, sourceCharset ) ;
     writer = new PrintWriter( outputStreamWriter, true ) ;
     renderingCapability = RenderingEscape.createCapability( sourceCharset ) ;
   }
 
   private SourceCharactersTable writeSourceDocument() {
 
     writer.println( "== Characters supported in source documents" ) ;
     writer.println() ;
    writer.println( "| Escape name | Alias | Hex | Dec | Preview |" ) ;
 
     final List< Lexeme > lexemes = Ordering.from( COMPARATOR ).sortedCopy(
         GeneratedLexemes.getLexemes().values() ) ;
     for( Lexeme lexeme : lexemes ) {
      final String unicode = CharUtils.unicodeEscaped( lexeme.getCharacter() ).substring( 1 ) ;
      final String decimal = String.format( "%04d", ( int ) lexeme.getCharacter().charValue() ) ;
       final String longEscapeName =
           SourceUnescape.unicodeUpperNameToEscapeName( lexeme.getUnicodeName() );
       writer.append( "| " ) ;
       writer.append( doubleEscape( longEscapeName ) ) ;
       writer.append( " | " ) ;
       writer.append( doubleEscape( lexeme.getHtmlEntityName() ) ) ;
       writer.append( " | " ) ;
      writer.append( unicode ) ;
      writer.append( " | " ) ;
      writer.append( decimal ) ;
       writer.append( " | " ) ;
       writer.append( simpleEscape( longEscapeName ) ) ;
       writer.append( " |" ) ;
       writer.println() ;
     }
 
     return this ;
   }
 
   private void close() {
     writer.flush() ;
     writer.close() ;
   }
 
   private String simpleEscape( String name ) {
     if( null == name ) {
       return " " ;
     } else {
       return SourceUnescape.ESCAPE_START + name + SourceUnescape.ESCAPE_END ;
     }
   }
 
   private String doubleEscape( String name ) {
     if( null == name ) {
       return " " ;
     } else {
       return
           RenderingEscape.unconditionalEscapeToSource( SourceUnescape.ESCAPE_START ) +
           name +
           RenderingEscape.unconditionalEscapeToSource( SourceUnescape.ESCAPE_END )
       ;
     }
   }
 
   private static final Comparator< Lexeme > COMPARATOR = new Comparator< Lexeme >() {
     public int compare( Lexeme lexeme1, Lexeme lexeme2 ) {
       return lexeme1.getCharacter().compareTo( lexeme2.getCharacter() ) ;
     }
   };
 }
