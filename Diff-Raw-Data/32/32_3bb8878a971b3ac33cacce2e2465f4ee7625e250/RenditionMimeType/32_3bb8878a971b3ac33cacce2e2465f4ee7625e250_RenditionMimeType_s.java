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
 
 package novelang.rendering;
 
 import java.util.Arrays;
 import java.util.List;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 
 /**
  * @author Laurent Caillette
 */
 public enum RenditionMimeType {
   
   TXT( "text/plain" ),
   NLP( "text/plain" ),
   XML( "text/xml" ),
   PDF( "application/pdf" ),
   HTML( "text/html" ) ;
 
   private final String identifier ;
 
   private static final Function< RenditionMimeType,String > FILE_EXTENSION_EXTRACTOR =
       new Function< RenditionMimeType, String >() {
         public String apply( RenditionMimeType renditionMimeType ) {
           return renditionMimeType.getFileExtension() ;
         }
       }
   ;
 
   private RenditionMimeType( String identifier ) {
     this.identifier = identifier ;
   }
 
   public String getMimeName() {
     return identifier;
   }
 
   public String getFileExtension() {
     return name().toLowerCase() ;
   }
 
   public static boolean contains( String name ) {
     for( RenditionMimeType renditionMimeType : RenditionMimeType.values() ) {
       if( renditionMimeType.name().toLowerCase().equals( name ) ) {
         return true ;
       }
     }
     return false ;
   }
 
   public static Iterable< String > getFileExtensions() {
     final List< RenditionMimeType > elements = Arrays.asList( values() ) ;
     return Lists.transform( elements, FILE_EXTENSION_EXTRACTOR ) ;
   }
 }
