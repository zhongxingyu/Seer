 /*
  *  TextView.java
  *  SwingOSC
  *
  *  Copyright (c) 2005-2009 Hanns Holger Rutz. All rights reserved.
  *
  *	This software is free software; you can redistribute it and/or
  *	modify it under the terms of the GNU General Public License
  *	as published by the Free Software Foundation; either
  *	version 2, june 1991 of the License, or (at your option) any later version.
  *
  *	This software is distributed in the hope that it will be useful,
  *	but WITHOUT ANY WARRANTY; without even the implied warranty of
  *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  *	General Public License for more details.
  *
  *	You should have received a copy of the GNU General Public
  *	License (gpl.txt) along with this software; if not, write to the Free Software
  *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *
  *	For further information, please contact Hanns Holger Rutz at
  *	contact@sciss.de
  *
  *
  *  Changelog:
  *		08-Feb-07	created
  */
 package de.sciss.swingosc;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.JTextPane;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.MutableAttributeSet;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 
 /**
  *	Extends <code>JTextPane</code> by a buffering
  *	mechanism for data updates and utility methods for styling.
  *
  *	@author		Hanns Holger Rutz
 *	@version	0.57, 19-Dec-07
  */
 public class TextView
 extends JTextPane
 {
 	private final StringBuffer updateData = new StringBuffer();
 	private final List collDocListeners = new ArrayList();
 
 	public TextView()
 	{
 		super();
 //		addHyperlinkListener( new HyperlinkListener() {
 //			public void hyperlinkUpdate( HyperlinkEvent e )
 //			{
 //				System.out.println( "RECEIVED : " + e.getEventType() );
 //			}
 //		});
 	}
 
 	public void beginDataUpdate()
 	{
 		updateData.setLength( 0 );
 	}
 	
 	public void addData( String update )
 	{
 		updateData.append( update );
 	}
 	
 	public void endDataUpdate( int insertPos, int replaceLen )
 	throws BadLocationException
 	{
 		setString( insertPos, replaceLen, updateData.toString() );
 		updateData.setLength( 0 );
 	}
 	
 	public void setPage( String url )
 	throws IOException
 	{
 		removeAllDocListeners();
 		try {
 			super.setPage( url );
 		}
 		finally {
 			addAllDocListeners();
 		}
 //System.out.println( "now we've got " + this.getHyperlinkListeners().length + " listeners" );
 //System.out.println( "EditorKit is " + this.getEditorKit().getContentType() );
 	}
 	
 	public void setPage( URL url )
 	throws IOException
 	{
 		removeAllDocListeners();
 		try {
 			super.setPage( url );
 		}
 		finally {
 			addAllDocListeners();
 		}
 //System.out.println( "now we've got " + this.getHyperlinkListeners().length + " listeners" );		
 //System.out.println( "EditorKit is " + this.getEditorKit().getContentType() );
 	}
 	
 	private void addAllDocListeners()
 	{
 		final Document doc = getDocument();
 		for( int i = 0; i < collDocListeners.size(); i++ ) {
 			doc.addDocumentListener( (DocumentListener) collDocListeners.get( i ));
 		}
 	}
 	
 	private void removeAllDocListeners()
 	{
 		final Document doc = getDocument();
 		for( int i = 0; i < collDocListeners.size(); i++ ) {
 			doc.removeDocumentListener( (DocumentListener) collDocListeners.get( i ));
 		}
 	}
 
 	public void setString( int insertPos, int replaceLen, String str )
 	throws BadLocationException
 	{
 		final Document doc = getDocument();
 		if( insertPos == -1 ) {
 			insertPos = 0;
 			replaceLen = doc.getLength();
 		} else {
 			insertPos 	= Math.max( 0, Math.min( insertPos, doc.getLength () ));
 			replaceLen	= Math.max( 0, Math.min( replaceLen, doc.getLength() - insertPos ));
 		}
 		doc.remove( insertPos, replaceLen );
 		doc.insertString( insertPos, str, null );
 	}
 	
 	public void setFont( int rangeStart, int len, Font f )
 	{
 		if( rangeStart == -1 ) {
 			// fixes a bug with empty documents where
 			// the font is not properly applied through setCharacterAttributes...
 			setFont( f );
			return;
 		}
 		final MutableAttributeSet as = new SimpleAttributeSet();
 		StyleConstants.setFontFamily( as, f.getFamily() );
 		StyleConstants.setFontSize( as, f.getSize() );
 		StyleConstants.setBold( as, f.isBold() );
 		StyleConstants.setItalic( as, f.isItalic() );
 		applyAttr( rangeStart, len, as );
 	}
 	
 	public void setForeground( int rangeStart, int len, Color c )
 	{
 		if( rangeStart == -1 ) {
 			// fixes a bug with empty documents where
 			// the colour is not properly applied through setCharacterAttributes...
 			setForeground( c );
			return;
 		}
 		final MutableAttributeSet as = new SimpleAttributeSet();
 		StyleConstants.setForeground( as, c );
 		applyAttr( rangeStart, len, as );
 	}
 	
 	// this is here to make DocumentResponder less complex
 	// (because now it can connect both Caret and Document listeners to the same object)
 	// ; this just forwards the request to the Document.
 	// it takes care of removing and adding the listeners
 	// automatically if the document changes
 	public void addDocumentListener( DocumentListener l )
 	{
 		collDocListeners.add( l );
 		getDocument().addDocumentListener( l );
 	}
 	
 //	public void addHyperlinkListener( HyperlinkListener l )
 //	{
 //		System.out.println( "addHyperlinkListener" );
 //		super.addHyperlinkListener( l );
 //	}
 
 	public void removeDocumentListener( DocumentListener l )
 	{
 		collDocListeners.remove( l );
 		getDocument().removeDocumentListener( l );
 	}
 
 	private void applyAttr( int rangeStart, int len, AttributeSet as )
 	{
 		final StyledDocument doc = getStyledDocument();
 		if( rangeStart == -1 ) {
 			rangeStart	= 0;
 			len			= doc.getLength();
 		}
 		doc.setCharacterAttributes( rangeStart, len, as, false );
 	}
 
 	public void paintComponent( Graphics g )
 	{
 		final Color colrBg	= getBackground();
 
 		if( (colrBg != null) && (colrBg.getAlpha() > 0) ) {
 			g.setColor( colrBg );
 			g.fillRect( 0, 0, getWidth(), getHeight() );
 		}
 		super.paintComponent( g );
 	}
 
 	public void setBackground( Color c )
 	{
 		setOpaque( (c != null) && (c.getAlpha() == 0xFF) );
 		super.setBackground( c );
 	}
 }
