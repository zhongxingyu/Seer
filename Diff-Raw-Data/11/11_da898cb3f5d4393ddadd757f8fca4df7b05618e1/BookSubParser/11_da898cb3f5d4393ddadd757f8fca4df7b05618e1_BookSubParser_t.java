 /**
  * <e-Adventure> is an <e-UCM> research project. <e-UCM>, Department of Software
  * Engineering and Artificial Intelligence. Faculty of Informatics, Complutense
  * University of Madrid (Spain).
  * 
  * @author Del Blanco, A., Marchiori, E., Torrente, F.J. (alphabetical order) *
  * @author Lpez Maas, E., Prez Padilla, F., Sollet, E., Torijano, B. (former
  *         developers by alphabetical order)
  * @author Moreno-Ger, P. & Fernndez-Manjn, B. (directors)
  * @year 2009 Web-site: http://e-adventure.e-ucm.es
  */
 
 /*
  * Copyright (C) 2004-2009 <e-UCM> research group
  * 
  * This file is part of <e-Adventure> project, an educational game & game-like
  * simulation authoring tool, available at http://e-adventure.e-ucm.es.
  * 
  * <e-Adventure> is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option) any
  * later version.
  * 
  * <e-Adventure> is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * <e-Adventure>; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 package es.eucm.eadventure.common.loader.subparsers;
 
 import java.awt.Point;
 
 import org.xml.sax.Attributes;
 
 import es.eucm.eadventure.common.data.chapter.Chapter;
 import es.eucm.eadventure.common.data.chapter.book.Book;
 import es.eucm.eadventure.common.data.chapter.book.BookPage;
 import es.eucm.eadventure.common.data.chapter.book.BookParagraph;
 import es.eucm.eadventure.common.data.chapter.conditions.Conditions;
 import es.eucm.eadventure.common.data.chapter.resources.Resources;
 
 /**
  * Class to sub-parse books
  */
 public class BookSubParser extends SubParser {
 
     /* Attributes */
 
     /**
      * Constant for subparsing nothing
      */
     private static final int SUBPARSING_NONE = 0;
 
     /**
      * Constant for subparsing condition tag
      */
     private static final int SUBPARSING_CONDITION = 1;
 
     /**
      * Stores the current element being subparsed
      */
     private int subParsing = SUBPARSING_NONE;
 
     /**
      * The book being read
      */
     private Book book;
 
     /**
      * Current resources being read
      */
     private Resources currentResources;
 
     /**
      * Current conditions being read
      */
     private Conditions currentConditions;
 
     /**
      * Subparser for the conditions
      */
     private SubParser conditionSubParser;
 
     /* Methods */
 
     /**
      * Constructor
      * 
      * @param chapter
      *            Chapter data to store the read data
      */
     public BookSubParser( Chapter chapter ) {
 
         super( chapter );
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see es.eucm.eadventure.engine.loader.subparsers.SubParser#startElement(java.lang.String, java.lang.String,
      *      java.lang.String, org.xml.sax.Attributes)
      */
     @Override
     public void startElement( String namespaceURI, String sName, String qName, Attributes attrs ) {
 
         // If no element is being subparsed
         if( subParsing == SUBPARSING_NONE ) {
 
             // If it is a book tag, store the id of the book
             if( qName.equals( "book" ) ) {
                 String bookId = "";
                 String xPrevious = "", xNext = "", yPrevious = "", yNext = ""; 
 
                 for( int i = 0; i < attrs.getLength( ); i++ ){
                     if( attrs.getQName( i ).equals( "id" ) )
                         bookId = attrs.getValue( i );
                     else if ( attrs.getQName( i ).equals( "xPreviousPage" ) )
                         xPrevious = attrs.getValue( i );
                     else if ( attrs.getQName( i ).equals( "xNextPage" ) )
                         xNext = attrs.getValue( i );
                     else if ( attrs.getQName( i ).equals( "yPreviousPage" ) )
                         yPrevious = attrs.getValue( i );
                     else if ( attrs.getQName( i ).equals( "yNextPage" ) )
                         yNext = attrs.getValue( i );
                 }
 
                 book = new Book( bookId );
                 
                 if ( xPrevious != "" && yPrevious != "" ){
                     try {
                         int x = Integer.parseInt( xPrevious );
                         int y = Integer.parseInt( yPrevious );
                         book.setPreviousPagePoint( new Point( x, y) );
                     } catch ( NumberFormatException e ){
                         // Number in XML is wrong -> Do nothing
                     }
                 }
                 
                 if ( xNext != "" && yNext != "" ){
                     try {
                         int x = Integer.parseInt( xNext );
                         int y = Integer.parseInt( yNext );
                         book.setNextPagePoint( new Point( x, y) );
                     } catch ( NumberFormatException e ){
                         // Number in XML is wrong -> Do nothing
                     }
                 }
             }
 
             // If it is a resources tag, create the new resources
             else if( qName.equals( "resources" ) ) {
                 currentResources = new Resources( );
                 
                 for (int i = 0; i < attrs.getLength( ); i++) {
                     if (attrs.getQName( i ).equals( "name" ))
                         currentResources.setName( attrs.getValue( i ) );
                 }
 
             }
            
             // If it is a documentation tag, hold the documentation in the book
             else if( qName.equals( "documentation" ) ) {
                currentString= new StringBuffer();
             }
 
             // If it is a condition tag, create a new subparser
             else if( qName.equals( "condition" ) ) {
                 currentConditions = new Conditions( );
                 conditionSubParser = new ConditionSubParser( currentConditions, chapter );
                 subParsing = SUBPARSING_CONDITION;
             }
 
             // If it is an asset tag, read it and add it to the current resources
             else if( qName.equals( "asset" ) ) {
                 String type = "";
                 String path = "";
 
                 for( int i = 0; i < attrs.getLength( ); i++ ) {
                     if( attrs.getQName( i ).equals( "type" ) )
                         type = attrs.getValue( i );
                     if( attrs.getQName( i ).equals( "uri" ) )
                         path = attrs.getValue( i );
                 }
 
                 currentResources.addAsset( type, path );
             }
 
             else if( qName.equals( "text" ) ) {
                 book.setType( Book.TYPE_PARAGRAPHS );
             }
 
             else if( qName.equals( "pages" ) ) {
                 book.setType( Book.TYPE_PAGES );
             }
 
             else if( qName.equals( "page" ) ) {
                 String uri = "";
                 int type = BookPage.TYPE_URL;
                 int margin = 0;
                 int marginEnd = 0;
                 int marginTop = 0;
                 int marginBottom = 0;
                 boolean scrollable = false;
 
                 for( int i = 0; i < attrs.getLength( ); i++ ) {
                     if( attrs.getQName( i ).equals( "uri" ) )
                         uri = attrs.getValue( i );
 
                     if( attrs.getQName( i ).equals( "type" ) ) {
                         if( attrs.getValue( i ).equals( "resource" ) )
                             type = BookPage.TYPE_RESOURCE;
                         if( attrs.getValue( i ).equals( "image" ) )
                             type = BookPage.TYPE_IMAGE;
                     }
 
                     if( attrs.getQName( i ).equals( "scrollable" ) )
                         if( attrs.getValue( i ).equals( "yes" ) )
                             scrollable = true;
 
                     if( attrs.getQName( i ).equals( "margin" ) ) {
                         try {
                             margin = Integer.parseInt( attrs.getValue( i ) );
                         }
                         catch( Exception e ) {
                         }
                     }
 
                     if( attrs.getQName( i ).equals( "marginEnd" ) ) {
                         try {
                             marginEnd = Integer.parseInt( attrs.getValue( i ) );
                         }
                         catch( Exception e ) {
                         }
                     }
 
                     if( attrs.getQName( i ).equals( "marginTop" ) ) {
                         try {
                             marginTop = Integer.parseInt( attrs.getValue( i ) );
                         }
                         catch( Exception e ) {
                         }
                     }
 
                     if( attrs.getQName( i ).equals( "marginBottom" ) ) {
                         try {
                             marginBottom = Integer.parseInt( attrs.getValue( i ) );
                         }
                         catch( Exception e ) {
                         }
                     }
 
                 }
                 book.addPage( uri, type, margin, marginEnd, marginTop, marginBottom, scrollable );
 
             }
 
             // If it is a title or bullet tag, store the previous text in the book
             else if( qName.equals( "title" ) || qName.equals( "bullet" ) ) {
                 // Add the new text paragraph
                 if( currentString != null && currentString.toString( ).trim( ).replace( "\t", "" ).replace( "\n", "" ).length( ) > 0 )
                     book.addParagraph( new BookParagraph( BookParagraph.TEXT, currentString.toString( ).trim( ).replace( "\t", "" ) ) );
                 currentString = new StringBuffer( );
             }
 
             // If it is an image tag, store the image in the book
             else if( qName.equals( "img" ) ) {
 
                 // Add the new text paragraph
                 if( currentString.toString( ).trim( ).replace( "\t", "" ).replace( "\n", "" ).length( ) > 0 ) {
                     book.addParagraph( new BookParagraph( BookParagraph.TEXT, currentString.toString( ).trim( ).replace( "\t", "" ) ) );
                     currentString = new StringBuffer( );
                 }
 
                 String path = "";
 
                 for( int i = 0; i < attrs.getLength( ); i++ ) {
                     if( attrs.getQName( i ).equals( "src" ) )
                         path = attrs.getValue( i );
                 }
 
                 // Add the new image paragraph
                 book.addParagraph( new BookParagraph( BookParagraph.IMAGE, path ) );
             }
         }
 
         // If a condition is being subparsed, spread the call
         if( subParsing == SUBPARSING_CONDITION ) {
             conditionSubParser.startElement( namespaceURI, sName, qName, attrs );
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see es.eucm.eadventure.engine.loader.subparsers.SubParser#endElement(java.lang.String, java.lang.String,
      *      java.lang.String)
      */
     @Override
     public void endElement( String namespaceURI, String sName, String qName ) {
 
         // If no element is being subparsed
         if( subParsing == SUBPARSING_NONE ) {
 
             // If it is a book tag, add the book to the game data
             if( qName.equals( "book" ) ) {
                 chapter.addBook( book );
             }
 
             // If it is a resources tag, add the resources to the book
             else if( qName.equals( "resources" ) ) {
                 book.addResources( currentResources );
             }
            
            // If it is a documentation tag, hold the documentation in the book
            else if( qName.equals( "documentation" ) ) {
                book.setDocumentation( currentString.toString( ).trim( ) );
            }
 
             // If it is a text tag, add the text to the book
             else if( qName.equals( "text" ) ) {
                 // Add the new text paragraph
                 if( currentString != null && currentString.toString( ).trim( ).replace( "\t", "" ).replace( "\n", "" ).length( ) > 0 )
                     book.addParagraph( new BookParagraph( BookParagraph.TEXT, currentString.toString( ).trim( ).replace( "\t", "" ) ) );
             }
 
             // If it is a title tag, add the text to the book
             else if( qName.equals( "title" ) ) {
                 // Add the new title paragraph
                 if( currentString != null )
                     book.addParagraph( new BookParagraph( BookParagraph.TITLE, currentString.toString( ).trim( ).replace( "\t", "" ) ) );
             }
 
             else if( qName.equals( "bullet" ) ) {
                 // Add the new bullet paragraph
                 if( currentString != null )
                     book.addParagraph( new BookParagraph( BookParagraph.BULLET, currentString.toString( ).trim( ).replace( "\t", "" ) ) );
             }
 
             // Reset the current string
             currentString = new StringBuffer( );
         }
 
         // If a condition is being subparsed
         else if( subParsing == SUBPARSING_CONDITION ) {
 
             // Spread the end element call
             conditionSubParser.endElement( namespaceURI, sName, qName );
 
             // If the condition is being closed, add the conditions to the resources
             if( qName.equals( "condition" ) ) {
                 currentResources.setConditions( currentConditions );
                 subParsing = SUBPARSING_NONE;
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see es.eucm.eadventure.engine.loader.subparsers.SubParser#characters(char[], int, int)
      */
     @Override
     public void characters( char[] buf, int offset, int len ) {
 
         // If no element is being subparsed, read the characters
         if( subParsing == SUBPARSING_NONE )
             super.characters( buf, offset, len );
 
         // If a condition is being subparsed, spread the call
         else if( subParsing == SUBPARSING_CONDITION )
             conditionSubParser.characters( buf, offset, len );
     }
 }
