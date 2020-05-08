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
  */
 package es.eucm.eadventure.engine.core.control.functionaldata;
 
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.swing.JEditorPane;
 import javax.swing.JPanel;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.text.html.HTMLEditorKit;
 import javax.swing.text.html.HTMLFrameHyperlinkEvent;
 import javax.swing.text.rtf.RTFEditorKit;
 
 import es.eucm.eadventure.common.auxiliar.ReportDialog;
 import es.eucm.eadventure.common.data.chapter.book.BookPage;
 import es.eucm.eadventure.common.gui.BookEditorPane;
 import es.eucm.eadventure.engine.core.control.Game;
 import es.eucm.eadventure.engine.core.gui.editorpane.BookEditorPaneEngine;
 import es.eucm.eadventure.engine.multimedia.MultimediaManager;
 import es.eucm.eadventure.engine.resourcehandler.ResourceHandler;
 
 public class FunctionalBookPage extends JPanel {
 
     private static final long serialVersionUID = 1L;
 
     private BookPage bookPage;
 
     private boolean isValid;
 
     private Image background, currentArrowLeft,
             currentArrowRight;
 
     private Point previousPage, nextPage;
 
     private Image image;
 
     private FunctionalStyledBook fBook;
 
     private BookEditorPane editorPane;
 
     private boolean htmlToImage = false;
 
     public FunctionalBookPage( Image background ) {
 
         this.background = background;
     }
 
     public FunctionalBookPage( BookPage bookPage, FunctionalStyledBook fBook, Image background, Image currentArrowLeft, Image currentArrowRight, Point previousPage, Point nextPage, boolean listenHyperLinks ) {
 
         super( );
         editorPane = new BookEditorPaneEngine( bookPage.getUri( ) );
         isValid = true;
         this.bookPage = bookPage;
         this.fBook = fBook;
         this.background = background;
         this.currentArrowLeft = currentArrowLeft;
         this.currentArrowRight = currentArrowRight;
         this.previousPage = previousPage;
         this.nextPage = nextPage;
 
         FunctionalBookMouseListener bookListener = new FunctionalBookMouseListener( );
         this.addMouseListener( bookListener );
         this.addMouseMotionListener( bookListener );
 
         switch( bookPage.getType( ) ) {
             case BookPage.TYPE_URL:
                 isValid = createURLPage( listenHyperLinks );
                 break;
             case BookPage.TYPE_RESOURCE:
                 isValid = createResourcePage( );
                 break;
             case BookPage.TYPE_IMAGE:
                 isValid = createImagePage( );
             default:
                 isValid = false;
         }
 
         if( editorPane != null ) {
             FunctionalBookMouseListener bookListener2 = new FunctionalBookMouseListener( );
             editorPane.addMouseListener( bookListener2 );
             editorPane.addMouseMotionListener( bookListener2 );
             editorPane.setOpaque( false );
             editorPane.setEditable( false );
 
             this.setOpaque( false );
 
             this.setLayout( null );
 
             /*editorPane.setBounds( bookPage.getMargin( ), bookPage.getMarginTop( ), GUI.WINDOW_WIDTH - bookPage.getMargin( ) - bookPage.getMarginEnd( ), GUI.WINDOW_HEIGHT - bookPage.getMarginTop( ) - bookPage.getMarginBottom( ) );
             if( bookPage.getScrollable( ) )
                 this.add( new JScrollPane( editorPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED ) );
             else
                 this.add( editorPane );*/
         }
     }
 
     private boolean createImagePage( ) {
 
         image = MultimediaManager.getInstance( ).loadImageFromZip( bookPage.getUri( ), MultimediaManager.IMAGE_SCENE );
         return image != null;
     }
 
     private boolean createResourcePage( ) {
 
         // Check if there is an image created for the representatio of this page
         image = MultimediaManager.getInstance( ).loadImageFromZip( "assets/image/" + bookPage.getImageName( ) + ".png", MultimediaManager.IMAGE_SCENE );
 
         if( image == null ) {
             String uri = bookPage.getUri( );
             String ext = uri.substring( uri.lastIndexOf( '.' ) + 1, uri.length( ) ).toLowerCase( );
             if( ext.equals( "html" ) || ext.equals( "htm" ) || ext.equals( "rtf" ) ) {
 
                 //Read the text
                 StringBuffer textBuffer = new StringBuffer( );
                 InputStream is = ResourceHandler.getInstance( ).getResourceAsStreamFromZip( uri );//null;
                 try {
                     int c;
                     while( ( c = is.read( ) ) != -1 ) {
                         textBuffer.append( (char) c );
                     }
                 }
                 catch( IOException e ) {
                     isValid = false;
                 }
                 finally {
                     if( is != null ) {
                         try {
                             is.close( );
                         }
                         catch( IOException e ) {
                             isValid = false;
                         }
                     }
                 }
 
                 //Set the proper content type
                 if( ext.equals( "html" ) || ext.equals( "htm" ) ) {
                     editorPane.setContentType( "text/html" );
                     editorPane.setText( textBuffer.toString( ) );
                     URL url = ResourceHandler.getInstance( ).getResourceAsURL( uri );
                     try {
                         editorPane.setDocumentBase( new URL( url.getProtocol( ), url.getHost( ), url.getPort( ), url.getFile( ) ) );
                     }
                     catch( MalformedURLException e ) {
                         e.printStackTrace( );
                     }
                 }
                 else {
                     editorPane.setContentType( "text/rtf" );
                     editorPane.setText( textBuffer.toString( ) );
                 }
                 isValid = true;
 
             }
         }
         else {
             htmlToImage = true;
         }
 
         return isValid;
     }
 
     private boolean createURLPage( boolean listenHyperLinks ) {
 
         URL url = null;
         try {
             url = new URL( bookPage.getUri( ) );
             url.openStream( ).close( );
         }
         catch( Exception e ) {
             isValid = false;
         }
 
         try {
             if( isValid ) {
                 editorPane.setPage( url );
                 editorPane.setEditable( false );
                 if( listenHyperLinks )
                     editorPane.addHyperlinkListener( new BookHyperlinkListener( ) );
                 if( !( editorPane.getEditorKit( ) instanceof HTMLEditorKit ) && !( editorPane.getEditorKit( ) instanceof RTFEditorKit ) ) {
                     isValid = false;
                 }
 
             }
         }
         catch( IOException e ) {
         }
         return isValid;
     }
 
     private class FunctionalBookMouseListener extends MouseAdapter implements MouseMotionListener {
 
         @Override
         public void mouseClicked( MouseEvent evt ) {
 
             MouseEvent nEvt = createMouseEvent( evt );
             Game.getInstance( ).mouseClicked( nEvt );
         }
 
         @Override
         public void mouseMoved( MouseEvent evt ) {
 
             MouseEvent nEvt = createMouseEvent( evt );
             Game.getInstance( ).mouseMoved( nEvt );
         }
 
         private MouseEvent createMouseEvent( MouseEvent evt ) {
 
             int x = evt.getX( );
             int y = evt.getY( );
             if( evt.getSource( ) == editorPane ) {
                 //Spread the call gauging the positions so the margin is taken into account
                 x += bookPage.getMargin( );
                 y += bookPage.getMarginTop( );
             }
 
             MouseEvent nEvt = new MouseEvent( (Component) evt.getSource( ), evt.getID( ), evt.getWhen( ), evt.getModifiers( ), x, y, evt.getClickCount( ), evt.isPopupTrigger( ), evt.getButton( ) );
             return nEvt;
         }
 
     }
 
     /**
      * Listener for the Hyperlinks in the HTML books
      * 
      * @author Javier Torrente
      * 
      */
     private class BookHyperlinkListener implements HyperlinkListener {
 
         public void hyperlinkUpdate( HyperlinkEvent e ) {
 
             if( e.getEventType( ) == HyperlinkEvent.EventType.ACTIVATED ) {
                 JEditorPane pane = (JEditorPane) e.getSource( );
                 if( e instanceof HTMLFrameHyperlinkEvent ) {
                     HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
                     HTMLDocument doc = (HTMLDocument) pane.getDocument( );
                     doc.processHTMLFrameHyperlinkEvent( evt );
                 }
                 else {
                     try {
                         pane.setPage( e.getURL( ) );
                     }
                     catch( Exception t ) {
                         ReportDialog.GenerateErrorReport( t, Game.getInstance( ).isFromEditor( ), "UNKNOWERROR" );
                     }
                 }
             }
         }
     }
 
     @Override
     public void paint( Graphics g ) {
 
         g.drawImage( background, 0, 0, background.getWidth( null ), background.getHeight( null ), null );
         if( image != null ) {
             if( htmlToImage )
                 g.drawImage( image, 0, 0, null );
             else
                g.drawImage( image, bookPage.getMargin( ), bookPage.getMarginTop( ), this.getWidth( ) - bookPage.getMarginEnd( ), this.getHeight( ) - bookPage.getMarginBottom( ), 0, 0, image.getWidth( null ), image.getHeight( null ), null );
         }
         else if( editorPane != null )
             editorPane.paint( g, bookPage.getMargin( ), bookPage.getMarginTop( ), getWidth( ) - bookPage.getMarginEnd( ), getHeight( ) - bookPage.getMarginBottom( ) );
         if( currentArrowLeft != null && currentArrowRight != null ) {
             if( !fBook.isInFirstPage( ) )
                 g.drawImage( currentArrowLeft, previousPage.x, previousPage.y, currentArrowLeft.getWidth( null ), currentArrowLeft.getHeight( null ), null );
 
             if( !fBook.isInLastPage( ) )
                 g.drawImage( currentArrowRight, nextPage.x, nextPage.y, currentArrowRight.getWidth( null ), currentArrowRight.getHeight( null ), null );
         }
         super.paint( g );
     }
 
     /**
      * @return the bookPage
      */
     public BookPage getBookPage( ) {
 
         return bookPage;
     }
 
     /**
      * @param bookPage
      *            the bookPage to set
      */
     public void setBookPage( BookPage bookPage ) {
 
         this.bookPage = bookPage;
     }
 
     /**
      * @return the isValid
      */
     @Override
     public boolean isValid( ) {
 
         return isValid;
     }
 
     /**
      * @param isValid
      *            the isValid to set
      */
     public void setValid( boolean isValid ) {
 
         this.isValid = isValid;
     }
 
     public void setCurrentArrowLeft( Image currentArrowLeft ) {
 
         this.currentArrowLeft = currentArrowLeft;
         this.repaint( );
     }
 
     public void setCurrentArrowRight( Image currentArrowRight ) {
 
         this.currentArrowRight = currentArrowRight;
         this.repaint( );
     }
 
 }
