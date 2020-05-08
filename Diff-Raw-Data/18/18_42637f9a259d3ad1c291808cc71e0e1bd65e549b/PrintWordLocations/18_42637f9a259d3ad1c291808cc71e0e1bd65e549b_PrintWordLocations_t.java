 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.pdfbox.examples.util;
 
 import org.apache.pdfbox.exceptions.InvalidPasswordException;
 
 
 import org.apache.pdfbox.pdmodel.PDDocument;
 import org.apache.pdfbox.pdmodel.PDPage;
 import org.apache.pdfbox.pdmodel.common.PDStream;
 import org.apache.pdfbox.util.PDFTextStripper;
 import org.apache.pdfbox.util.TextPosition;
 import org.apache.pdfbox.pdmodel.common.PDRectangle;
 
 import java.io.IOException;
 
 import java.util.List;
 import java.util.LinkedList;
 import java.lang.Math;
 import java.util.regex.Pattern;
 
 /**
  * This is an example on how to get some x/y coordinates of text.
  *
  * Usage: java org.apache.pdfbox.examples.util.PrintWordLocations &lt;input-pdf&gt;
  *
  * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
  * @version $Revision: 1.7 $
  */
 public class PrintWordLocations extends PDFTextStripper
 {
     public class MarginOffset
     {
         protected float _dx;
         protected float _dy;
 
         public MarginOffset(float dx, float dy)
         {
             _dx = dx;
             _dy = dy;
         }
 
         public float getX()
         {
             return _dx;
         }
 
         public float getY()
         {
             return _dy;
         }
     }
 
     public class WordBoxEmitter
     {
         protected LinkedList<WordBox> box_list = new LinkedList<WordBox>();
         protected Character last_character = new Character('\0');
         protected StringBuffer word = new StringBuffer("");
         protected MarginOffset _offset = new MarginOffset(0, 0);
 
         public void setOffset(MarginOffset offset)
         {
             _offset = offset;
         }
 
         protected boolean endsWord(char ch)
         {
             return !(isAlnumOrApostrophe(ch) || isHyphen(ch));
         }
     
         protected boolean isAlnumOrApostrophe(char ch)
         {
             return Character.isLetterOrDigit(ch) || (ch == '\'');
         }
     
         protected boolean isHyphen(char ch)
         {
             return ch == '-';
         }
 
         protected void emit()
         {
             float pointsToInch1200 = (float)16.6666; 
             float mysteryHeightScale = (float)1.5;
             float height;
             float width;
             float hpos;
             float vpos;

             if (word.toString().trim().length() > 0) {
                 for (WordBox wordbox : box_list) {
                     width = wordbox._width * pointsToInch1200;
                     height = wordbox._height * pointsToInch1200 * mysteryHeightScale;
                     hpos = (wordbox._xmin + _offset.getX()) * pointsToInch1200;
                     vpos = (wordbox._ymin + _offset.getY()) * pointsToInch1200 - height;
     
                     System.out.println( "<String HEIGHT=\""  + height +
                                              "\" WIDTH=\""   + width  +
                                              "\" HPOS=\""    + hpos   +
                                              "\" VPOS=\""    + vpos   +
                                              "\" CONTENT=\"" + word.toString().trim() +
                                              "\"/>" );
                 }
             }
             word = new StringBuffer("");
             last_character = new Character('\0');
             box_list.clear();
         }
 
         protected void processTextPosition(TextPosition text, MarginOffset offset)
         {
             Character current_character = text.getCharacter().toLowerCase().charAt(0);
             setOffset(offset);
     
             if (endsWord(current_character)) {
                 emit();
             }
             else {
                 if (box_list.size() == 0) {
                    box_list.addLast(new WordBox(text));
                 }
                 else if (box_list.getLast().accepts(text)) {
                     box_list.getLast().extendBy(text);
                 }
                 else {
                     if (!isHyphen(last_character)) {
                         emit();
		    }
                    box_list.addLast(new WordBox(text));
                    last_character = new Character('\0');
                 }
 
                if (isHyphen(last_character)) {
                    word = word.append(last_character);
                }
                 if (isAlnumOrApostrophe(current_character)) {
                     word = word.append(current_character);
                 }
             }
     
             last_character = current_character;
         }
 
         protected void endOfPage()
         {
             if (box_list.size() > 0) {
                 emit();
             }
         }
     }
 
     public class WordBox
     {
         public float _xmin;
         public float _ymin;
         public float _fontsize;
         public float _xscale;
         public float _yscale;
         public float _height;
         public float _width;
   
         public WordBox(TextPosition text)
         {
             _xmin     = text.getXDirAdj();
             _ymin     = text.getYDirAdj();
             _fontsize = text.getFontSize();
             _xscale   = text.getXScale();
             _yscale   = text.getYScale();
             _height   = text.getHeightDir();
             _width    = text.getWidthDirAdj();
         }
 
         public boolean rejects(TextPosition text)
         {
             return (text.getXDirAdj() < _xmin) ||
                    (text.getYDirAdj() + text.getWidthOfSpace() < _ymin);
         }
   
         public boolean accepts(TextPosition text) {
             return !rejects(text);
         }
   
         public void extendBy(TextPosition text)
         {
             float current_xmin = _xmin;
             float current_xmax = _xmin + _width;
             float current_ymin = _ymin;
             float current_ymax = _ymin + _height;
   
             float text_xmin    = text.getXDirAdj();
             float text_xmax    = text_xmin          + text.getWidthDirAdj();
             float text_ymin    = text.getYDirAdj();
             float text_ymax    = text_ymin          + text.getHeightDir();
   
             float new_xmin     = Math.min(current_xmin, text_xmin);
             float new_xmax     = Math.max(current_xmax, text_xmax);
             float new_ymin     = Math.min(current_ymin, text_ymin);
             float new_ymax     = Math.max(current_ymax, text_ymax);
   
             _xmin   = new_xmin;
             _width  = new_xmax - new_xmin;
             _ymin   = new_ymin;
             _height = new_ymax - new_ymin;
         }
     }
 
     protected WordBoxEmitter emitter = new WordBoxEmitter();
     protected MarginOffset _offset = new MarginOffset(0, 0);
 
     /**
      * Default constructor.
      *
      * @throws IOException If there is an error loading text stripper properties.
      */
     public PrintWordLocations() throws IOException
     {
         super.setSortByPosition( true );
     }
 
     public void setOffset(MarginOffset offset)
     {
         _offset = offset;
     }
 
     public void processDocuments( String[] args ) throws Exception
     {
         if( args.length != 1 )
         {
             usage();
         }
         else
         {
             PDDocument document = null;
             try
             {
                 document = PDDocument.load( args[0] );
                 if( document.isEncrypted() )
                 {
                     try
                     {
                         document.decrypt( "" );
                     }
                     catch( InvalidPasswordException e )
                     {
                         System.err.println( "Error: Document is encrypted with a password." );
                         System.exit( 1 );
                     }
                 }
                 PrintWordLocations printer = new PrintWordLocations();
                 List allPages = document.getDocumentCatalog().getAllPages();
                 System.out.println( "<?xml version=\"1.0\" encoding=\"UTF-8\"?><alto xmlns=\"http://www.loc.gov/standards/alto/alto-v2.0.xsd\"><Description><MeasurementUnit>inch1200</MeasurementUnit></Description><Layout>" );
                 for( int i=0; i<allPages.size(); i++ )
                 {
                     PDPage page = (PDPage)allPages.get( i );
 
                     if (page.getCropBox() != null) {
                         PDRectangle mediaBox = (PDRectangle)page.getMediaBox();
                         PDRectangle cropBox = (PDRectangle)page.getCropBox();
                         printer.setOffset(new MarginOffset(
                             cropBox.getLowerLeftX() - mediaBox.getLowerLeftX(),
                             cropBox.getLowerLeftY() - mediaBox.getLowerLeftY()
                         ));
                     }
 
                     System.out.println( "<Page>" );
                     System.out.println( "<PrintSpace>" );
                     System.out.println( "<TextBlock>" );
                     System.out.println( "<TextLine>" );
                     PDStream contents = page.getContents();
                     if( contents != null )
                     {
                         printer.processStream( page, page.findResources(), page.getContents().getStream() );
                     }
                     endOfPage();
                     System.out.println( "</TextLine>" );
                     System.out.println( "</TextBlock>" );
                     System.out.println( "</PrintSpace>" );
                     System.out.println( "</Page>");
                 }
                 System.out.println( "</Layout></alto>" );
             }
             finally
             {
                 if( document != null )
                 {
                     document.close();
                 }
             }
         }
     }
 
     /**
      * This will print the documents data.
      *
      * @param args The command line arguments.
      *
      * @throws Exception If there is an error parsing the document.
      */
     public static void main( String[] args ) throws Exception
     {
         PrintWordLocations handler = new PrintWordLocations();
         handler.processDocuments(args);
     }
 
     /**
      * A method provided as an event interface to allow a subclass to perform
      * some specific functionality when text needs to be processed.
      *
      * @param text The text to be processed
      */
     protected void processTextPosition( TextPosition text )
     {
         emitter.processTextPosition(text, _offset);
     }
 
     protected void endOfPage()
     {
         emitter.endOfPage();
     }
     
     /**
      * This will print the usage for this document.
      */
     private static void usage()
     {
         System.err.println( "Usage: java org.apache.pdfbox.examples.pdmodel.PrintWordLocations <input-pdf>" );
     }
 }
