 /*******************************************************************************
  * eAdventure (formerly <e-Adventure> and <e-Game>) is a research project of the e-UCM
  *          research group.
  *   
  *    Copyright 2005-2012 e-UCM research group.
  *  
  *     e-UCM is a research group of the Department of Software Engineering
  *          and Artificial Intelligence at the Complutense University of Madrid
  *          (School of Computer Science).
  *  
  *          C Profesor Jose Garcia Santesmases sn,
  *          28040 Madrid (Madrid), Spain.
  *  
  *          For more info please visit:  <http://e-adventure.e-ucm.es> or
  *          <http://www.e-ucm.es>
  *  
  *  ****************************************************************************
  * This file is part of eAdventure, version 1.5.
  * 
  *   You can access a list of all the contributors to eAdventure at:
  *          http://e-adventure.e-ucm.es/contributors
  *  
  *  ****************************************************************************
  *       eAdventure is free software: you can redistribute it and/or modify
  *      it under the terms of the GNU Lesser General Public License as published by
  *      the Free Software Foundation, either version 3 of the License, or
  *      (at your option) any later version.
  *  
  *      eAdventure is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU Lesser General Public License for more details.
  *  
  *      You should have received a copy of the GNU Lesser General Public License
  *      along with Adventure.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.common.gui;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.net.URL;
 
 import javax.swing.JEditorPane;
 import javax.swing.UIManager;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.EditorKit;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.text.html.HTMLEditorKit;
 
 import es.eucm.eadventure.engine.core.gui.GUI;
 
 /**
  * Editor pane adapted for the correct render of html images.
  * 
 * @author Angel S.
  * 
  */
 public class BookEditorPane extends JEditorPane {
 
     private static final long serialVersionUID = 1L;
 
     /**
      * Constructor
      *
      */
     public BookEditorPane( ) {
 
         setEditorKit( new BookHTMLEditorKit( ) );
         setBounds( 0, 0, GUI.WINDOW_WIDTH, GUI.WINDOW_HEIGHT );
         setOpaque( false );
         setEditable( true );
     }
 
     /**
      * Set document base. It will be used while html render
      * 
      * @param documentBase
      *            URL of the document base
      */
     public void setDocumentBase( URL documentBase ) {
 
         ( (HTMLDocument) getDocument( ) ).setBase( documentBase );
     }
 
     /**
      * 
      * @param g
      *            Graphics
      * @param x
      *            x-left-corner-position to draw in graphics
      * @param y
      *            x-left-corner-position to draw in graphics
      * @param width
      *            real width of the component
      * @param height
      *            real height of the component
      */
     public void paint( Graphics g, int x, int y, int width, int height ) {
 
         if( width != 0 && height != 0 ) {
             BufferedImage temp = new BufferedImage( GUI.WINDOW_WIDTH, GUI.WINDOW_HEIGHT, BufferedImage.TYPE_INT_ARGB );
             super.paint( temp.createGraphics( ) );
             Image i = temp.getScaledInstance( width, height, Image.SCALE_SMOOTH );
 
             g.drawImage( i, x, y, this );
         }
 
     }
 
     @Override
     public void setText( String t ) {
 
         try {
             Document doc = getDocument( );
             doc.remove( 0, doc.getLength( ) );
             if( t == null || t.equals( "" ) ) {
                 return;
             }
             Reader r = new StringReader( t );
             EditorKit kit = getEditorKit( );
             kit.read( r, doc, 0 );
         }
         catch( IOException ioe ) {
             UIManager.getLookAndFeel( ).provideErrorFeedback( this );
         }
         catch( BadLocationException ble ) {
             UIManager.getLookAndFeel( ).provideErrorFeedback( this );
         }
     }
 
     /**
      * HTMLEditor to solve problem with meta charset tag in html documents.
      * 
     * @author Angel S.
      */
     protected class BookHTMLEditorKit extends HTMLEditorKit {
 
         private static final long serialVersionUID = 1L;
 
         @Override
         public void read( Reader in, Document doc, int pos ) throws IOException, BadLocationException {
 
             if( doc instanceof HTMLDocument ) {
                 HTMLDocument hdoc = (HTMLDocument) doc;
                 Parser p = getParser( );
                 if( p == null ) {
                     throw new IOException( "Can't load parser" );
                 }
                 if( pos > doc.getLength( ) ) {
                     throw new BadLocationException( "Invalid location", pos );
                 }
 
                 ParserCallback receiver = hdoc.getReader( pos );
                 Boolean ignoreCharset = true;
                 p.parse( in, receiver, ( ignoreCharset == null ) ? false : ignoreCharset.booleanValue( ) );
                 receiver.flush( );
             }
             else {
                 super.read( in, doc, pos );
             }
         }
 
     }
 }
