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
 package es.eucm.eadventure.editor.gui.otherpanels;
 
 import java.awt.BorderLayout;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 import javax.swing.JEditorPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.text.html.HTMLEditorKit;
 import javax.swing.text.rtf.RTFEditorKit;
 
 import es.eucm.eadventure.common.auxiliar.File;
 import es.eucm.eadventure.common.auxiliar.ReportDialog;
 import es.eucm.eadventure.common.data.chapter.book.BookPage;
 import es.eucm.eadventure.editor.control.controllers.AssetsController;
 import es.eucm.eadventure.editor.control.controllers.book.BookDataControl;
 import es.eucm.eadventure.editor.gui.auxiliar.ImageTransformer;
 import es.eucm.eadventure.editor.gui.displaydialogs.StyledBookDialog;
 import es.eucm.eadventure.engine.core.gui.GUI;
 
 public class BookPagePreviewPanel extends JPanel {
 
     /**
      * Required
      */
     private static final long serialVersionUID = 1L;
 
     private BookPage bookPage;
 
     private boolean isValid;
 
     private Image background, arrowLeftNormal, arrowRightNormal, arrowLeftOver, arrowRightOver;
     
     /**
      * Current state for arrows
      */
     private Image currentArrowLeft, currentArrowRight;
     
     /**
      * Coordinates for arrows
      */
     private int xLeft, xRight, yLeft, yRight;
 
     private JEditorPane editorPane;
 
     private Image image;
 
     private StyledBookDialog parent;
 
     public BookPagePreviewPanel( StyledBookDialog parent, BookPage bookPage, BookDataControl dControl ) {
 
         super( );
         this.parent = parent;
         isValid = true;
         this.bookPage = bookPage;
         
         loadImages( dControl );
         
         this.addMouseListener( new BookPageMouseListener( ) );
         URL url = null;
         if( bookPage.getType( ) == BookPage.TYPE_URL ) {
             editorPane = new JEditorPane( );
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
                     if( !( editorPane.getEditorKit( ) instanceof HTMLEditorKit ) && !( editorPane.getEditorKit( ) instanceof RTFEditorKit ) ) {
                         isValid = false;
                     }
                     else {
                     }
 
                 }
             }
             catch( IOException e ) {
             }
 
         }
         else if( bookPage.getType( ) == BookPage.TYPE_RESOURCE ) {
             editorPane = new JEditorPane( );
             url = AssetsController.getResourceAsURLFromZip( bookPage.getUri( ) );
             String ext = url.getFile( ).substring( url.getFile( ).lastIndexOf( '.' ) + 1, url.getFile( ).length( ) ).toLowerCase( );
             if( ext.equals( "html" ) || ext.equals( "htm" ) || ext.equals( "rtf" ) ) {
 
                 //Read the text
                 StringBuffer textBuffer = new StringBuffer( );
                 InputStream is = null;
                 try {
                     is = url.openStream( );
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
                     ProcessHTML processor = new ProcessHTML( textBuffer.toString( ) );
                     String htmlProcessed = processor.start( );
                     editorPane.setText( htmlProcessed );
                 }
                 else {
                     editorPane.setContentType( "text/rtf" );
                     editorPane.setText( textBuffer.toString( ) );
                 }
                 isValid = true;
 
             }
         }
         else if( bookPage.getType( ) == BookPage.TYPE_IMAGE ) {
             url = AssetsController.getResourceAsURLFromZip( bookPage.getUri( ) );
             if( bookPage.getUri( ) != null && bookPage.getUri( ).length( ) > 0 )
                 image = AssetsController.getImage( bookPage.getUri( ) );
         }
 
         if( url == null ) {
             isValid = false;
         }
 
         if( editorPane != null ) {
             addEditorPane( );
         }
     }
 
     /**
      * Load the required images for the book
      * @param dControl Controller with the required information
      */
     private void loadImages( BookDataControl dControl ) {
         background = AssetsController.getImage( dControl.getPreviewImage( ) );
         arrowLeftNormal = AssetsController.getImage( dControl.getArrowImagePath( BookDataControl.ARROW_LEFT, BookDataControl.ARROW_NORMAL ) );
         arrowRightNormal = AssetsController.getImage( dControl.getArrowImagePath( BookDataControl.ARROW_RIGHT, BookDataControl.ARROW_NORMAL ) );
         arrowLeftOver = AssetsController.getImage( dControl.getArrowImagePath( BookDataControl.ARROW_LEFT, BookDataControl.ARROW_OVER ) );
         arrowRightOver = AssetsController.getImage( dControl.getArrowImagePath( BookDataControl.ARROW_RIGHT, BookDataControl.ARROW_OVER ) );
         
         // If we have only left arrow, we use the mirrored image for the right arrow
         if ( arrowLeftNormal != null && arrowRightNormal == null ){          
             arrowRightNormal = ImageTransformer.getInstance().getScaledImage( arrowLeftNormal, -1.0f, 1.0f );
         }
         // If we have only right arrow, we use the mirrored image for the left arrow
         else if ( arrowLeftNormal == null && arrowRightNormal != null ){
               arrowLeftNormal =  ImageTransformer.getInstance( ).getScaledImage( arrowRightNormal, -1.0f, 1.0f );
         }
         
         // If we don't have an over image, we use the normal image for it
         if ( arrowRightOver == null && arrowLeftOver == null ){
             arrowLeftOver = arrowLeftNormal;
             arrowRightOver = arrowRightNormal;
         }
         // If we have only one image, we use the mirrored image for the one missing
         else if ( arrowRightOver != null && arrowLeftOver == null ){
             arrowLeftOver = ImageTransformer.getInstance( ).getScaledImage( arrowRightOver, -1.0f, 1.0f );
         }
         else if ( arrowRightOver == null && arrowLeftOver != null ){
             arrowRightOver = ImageTransformer.getInstance( ).getScaledImage( arrowLeftOver, -1.0f, 1.0f );
         }
         int margin = 20;
         xLeft = margin;
         yLeft = background.getHeight( null ) - arrowLeftNormal.getHeight( null ) - margin;
         xRight = background.getWidth( null ) - arrowRightNormal.getWidth( null ) - margin;
         yRight = background.getHeight( null ) - arrowRightNormal.getHeight( null ) - margin;
         
         currentArrowLeft = arrowLeftNormal;
         currentArrowRight = arrowRightNormal;
   
     }
 
     private void addEditorPane( ) {
 
         editorPane.setOpaque( false );
         //editorPane.setCaret( null );
         editorPane.setEditable( false );
         //editorPane.setHighlighter( null );
         BookPageMouseListener bookListener = new BookPageMouseListener( );
         editorPane.addMouseListener( bookListener );
         editorPane.addMouseMotionListener( bookListener );
 
         this.setOpaque( false );
 
         //this.setLayout( new BoxLayout(this, BoxLayout.LINE_AXIS) );
         this.setLayout( null );
         if( !bookPage.getScrollable( ) ) {
             editorPane.setBounds( bookPage.getMargin( ), bookPage.getMarginTop( ), GUI.WINDOW_WIDTH - bookPage.getMargin( ) - bookPage.getMarginEnd( ), GUI.WINDOW_HEIGHT - bookPage.getMarginTop( ) - bookPage.getMarginBottom( ) );
             this.add( editorPane );
         }
         else {
             JPanel viewPort = new JPanel( ) {
 
                 private static final long serialVersionUID = 1L;
 
                 @Override
                 public void paint( Graphics g ) {
 
                     if( background != null )
                         g.drawImage( background, 0, 0, background.getWidth( null ), background.getHeight( null ), null );
                     super.paint( g );
                 }
             };
             viewPort.setLayout( new BorderLayout( ) );
             viewPort.setOpaque( false );
 
             editorPane.setBounds( bookPage.getMargin( ), bookPage.getMarginTop( ), GUI.WINDOW_WIDTH - bookPage.getMargin( ) - bookPage.getMarginEnd( ), GUI.WINDOW_HEIGHT - bookPage.getMarginTop( ) - bookPage.getMarginBottom( ) );
 
             viewPort.add( editorPane, BorderLayout.CENTER );
             JScrollPane scroll = new JScrollPane( viewPort, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
             scroll.getViewport( ).setOpaque( false );
             scroll.getViewport( ).setBorder( null );
             scroll.setOpaque( false );
             this.add( scroll );
         }
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
 
     public class ProcessHTML {
 
         private String html;
 
         private int currentPos;
 
         private int state;
 
         private final int STATE_NONE = 0;
 
         private final int STATE_LT = 1;
 
         private final int STATE_SRC = 2;
 
         private final int STATE_EQ = 3;
 
         private final int STATE_RT = 4;
 
         private final int STATE_RTQ = 5;
 
         private String reference;
 
         public ProcessHTML( String html ) {
 
             this.html = html;
             currentPos = 0;
             state = STATE_NONE;
         }
 
         public String start( ) {
 
             state = STATE_NONE;
             String lastThree = "";
             reference = "";
             for( currentPos = 0; currentPos < html.length( ); currentPos++ ) {
                 char current = html.charAt( currentPos );
                 if( lastThree.length( ) < 3 )
                     lastThree += current;
                 else
                     lastThree = lastThree.substring( 1, 3 ) + current;
 
                 if( state == STATE_NONE ) {
                     if( current == '<' ) {
                         state = STATE_LT;
                     }
                 }
                 else if( state == STATE_LT ) {
                     if( lastThree.toLowerCase( ).equals( "src" ) ) {
                         state = STATE_SRC;
                     }
                     else if( current == '>' ) {
                         state = STATE_NONE;
                     }
                 }
 
                 else if( state == STATE_SRC ) {
                     if( current == '=' ) {
                         state = STATE_EQ;
                     }
                     else if( current != ' ' ) {
                         state = STATE_NONE;
                     }
                 }
                 else if( state == STATE_EQ ) {
                     if( current == '"' ) {
                         state = STATE_RTQ;
                     }
                     else if( current != ' ' ) {
                         reference += current;
                         state = STATE_RT;
                     }
                 }
                 else if( state == STATE_RTQ ) {
                     if( current != '>' && current != '"' ) {
                         reference += current;
                     }
                     else {
                         state = STATE_NONE;
                         replaceReference( currentPos - reference.length( ), reference.length( ) );
                     }
                 }
                 else if( state == STATE_RT ) {
                     if( current != '>' && current != ' ' ) {
                         reference += current;
                     }
                     else {
                         state = STATE_NONE;
                         replaceReference( currentPos - reference.length( ), reference.length( ) );
                     }
                 }
 
             }
 
             return html;
         }
 
         private void replaceReference( int index, int length ) {
 
             try {
                 int lastSlash = Math.max( bookPage.getUri( ).lastIndexOf( "/" ), bookPage.getUri( ).lastIndexOf( "\\" ) );
                 String assetPath = bookPage.getUri( ).substring( 0, lastSlash ) + "/" + reference;
                 String destinyPath = AssetsController.extractResource( assetPath );
                 if( destinyPath != null ) {
                     String leftSide = html.substring( 0, index );
                     String rightSide = html.substring( index + length, html.length( ) );
                     File file = new File( destinyPath );
                     html = leftSide + file.toURI( ).toURL( ).toString( ) + rightSide;
                 }
                 reference = "";
             }
             catch( Exception e ) {
                 ReportDialog.GenerateErrorReport( e, true, "UNKNOWERROR" );
             }
         }
     }
 
     @Override
     public void paint( Graphics g ) {
         
         if( background != null && !bookPage.getScrollable( ) )
             g.drawImage( background, 0, 0, background.getWidth( null ), background.getHeight( null ), null );
        if ( parent != null && currentArrowLeft != null && currentArrowRight != null ){
             if ( !parent.isInFirstPage( ) )
                 g.drawImage( currentArrowLeft, xLeft, yLeft, null );
             
             if ( !parent.isInLastPage( ) )
                 g.drawImage( currentArrowRight, xRight, yRight, null );
         }
         if( this.image != null )
             g.drawImage( this.image, bookPage.getMargin( ), bookPage.getMarginTop( ), GUI.WINDOW_WIDTH - bookPage.getMarginEnd( ), GUI.WINDOW_HEIGHT - bookPage.getMarginBottom( ), 0, 0, this.image.getWidth( null ), this.image.getHeight( null ), null );
         if( editorPane != null )
             super.paint( g );
     }
 
     /**
      * Paints the preview of the book to an image
      * 
      * @return The image with the contents of the preview
      */
     public Image paintToImage( ) {
 
         Image image = new BufferedImage( GUI.WINDOW_WIDTH, GUI.WINDOW_HEIGHT, BufferedImage.TYPE_3BYTE_BGR );
         Graphics g = image.getGraphics( );
         if( background != null && !bookPage.getScrollable( ) )
             g.drawImage( background, 0, 0, background.getWidth( null ), background.getHeight( null ), null );
         if ( arrowLeftNormal != null && arrowRightNormal != null ){
             g.drawImage( arrowLeftNormal, xLeft, yLeft, null );
             g.drawImage( arrowRightNormal, xRight, yRight, null );
         }
         if( editorPane != null )
             editorPane.paint( g.create( bookPage.getMargin( ), bookPage.getMarginTop( ), GUI.WINDOW_WIDTH - bookPage.getMargin( ) - bookPage.getMarginEnd( ), GUI.WINDOW_HEIGHT - bookPage.getMarginTop( ) - bookPage.getMarginBottom( ) ) );
         if( this.image != null )
             g.drawImage( this.image, bookPage.getMargin( ), bookPage.getMarginTop( ), GUI.WINDOW_WIDTH - bookPage.getMarginEnd( ), GUI.WINDOW_HEIGHT - bookPage.getMarginBottom( ), 0, 0, this.image.getWidth( null ), this.image.getHeight( null ), null );
         return image;
     }
     
     private boolean isInPreviousPage( int x, int y ){
         int xLeftEnd = xLeft + arrowLeftNormal.getWidth( null );
         int yLeftEnd = yLeft + arrowLeftNormal.getHeight( null );
         return ( xLeft < x ) && ( x < xLeftEnd ) && ( yLeft < y ) && ( y < yLeftEnd );
     }
     
     private boolean isInNextPage( int x, int y ){
         int xRightEnd = xRight + arrowRightNormal.getWidth( null );
         int yRightEnd = yRight + arrowRightNormal.getHeight( null );
         return ( xRight < x ) && ( x < xRightEnd ) && ( yRight < y ) && ( y < yRightEnd );
     }
 
     private class BookPageMouseListener extends MouseAdapter implements MouseMotionListener {
 
         @Override
         public void mouseClicked( MouseEvent evt ) {
 
             if( parent != null ) {
                 int x = evt.getX( );
                 int y = evt.getY( );
                 if( evt.getSource( ) == editorPane ) {
                     //Spread the call gauging the positions so the margin is taken into account
                     /*x += bookPage.getMargin( );
                     y += bookPage.getMarginTop( );*/
                     if ( isInNextPage( x, y ) ){
                         parent.nextPage( );
                     }
                     else if ( isInPreviousPage ( x, y ) ){
                         parent.previousPage( );
                     }
                 }
                 //parent.mouseClicked( x, y );
             }
         }
         
         @Override
         public void mouseMoved( MouseEvent evt ) {
             if ( evt.getSource( ) == editorPane ){
                 int x = evt.getX( );
                 int y = evt.getY( );
                         
                 if ( isInPreviousPage( x, y ) ){
                     currentArrowLeft = arrowLeftOver;                
                 }
                 else
                     currentArrowLeft = arrowLeftNormal;
                 
                 if ( isInNextPage ( x, y ) ){
                     currentArrowRight = arrowRightOver;
                 }
                 else
                     currentArrowRight = arrowRightNormal;
                 
                 editorPane.repaint( );
             }
         }
 
     }
 
 }
