 /*
  *  Frame.java
  *  SwingOSC
  *
  *  Copyright (c) 2005-2008 Hanns Holger Rutz. All rights reserved.
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
  *		03-Feb-07	added acceptsMouseOver
  *		24-Nov-07	added keyboard shortcuts for close and minimize
  *		14-Jan-08	handles sucky cocoa bounds conversion
  */
 package de.sciss.swingosc;
 
 import java.awt.Component;
 import java.awt.Insets;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.AbstractAction;
 import javax.swing.ActionMap;
 import javax.swing.InputMap;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JRootPane;
 import javax.swing.KeyStroke;
 
 /**
  *	A simple <code>JFrame</code> subclass which adds
  *	support for an icon view (i.e. user draw func)
  *	and whose content pane uses a custom layout manager.
  *
  *	@author		Hanns Holger Rutz
 *	@version	0.57, 18-Jan-08
  */
 public class Frame
 extends JFrame
 {
 	public static final int	FLAG_UNDECORATED	= 0x01;
 	public static final int	FLAG_SCROLLPANE		= 0x02;
 	public static final int	FLAG_NORESIZE		= 0x04;
 	
 	private List		collMouseResp		= null;
 	
 	private boolean 	acceptsMouseOver	= false;
 	
 	private JComponent	topView;
 	private JScrollPane	scrollPane;
 	
 //	public Frame( boolean hasScroll )
 //	{
 //		super();
 //		init( hasScroll );
 //	}
 //	
 //	public Frame()
 //	{
 //		this( false );
 //	}
 //	
 //	public Frame( GraphicsConfiguration gc, boolean hasScroll )
 //	{
 //		super( gc );
 //		init( hasScroll );
 //	}
 //
 //	public Frame( GraphicsConfiguration gc )
 //	{
 //		this( gc, false );
 //	}
 	
 	public Frame( String title, Rectangle cocoaBounds, int flags )
 	{
 		super( title );
 		init( cocoaBounds, flags );
 	}
 
 //	public Frame( String title )
 //	{
 //		this( title, false );
 //	}
 //
 //	public Frame( String title, GraphicsConfiguration gc, boolean hasScroll )
 //	{
 //		super( title, gc );
 //		init( hasScroll );
 //	}
 //
 //	public Frame( String title, GraphicsConfiguration gc )
 //	{
 //		this( title, gc, false );
 //	}
 	
 	public JComponent getTopView()
 	{
 		return topView;
 	}
 	
 	private void init( Rectangle cocoaBounds, int flags )
 	{
 		if( (flags & FLAG_UNDECORATED) != 0 ) {
 			setUndecorated( true );
 		}
 		if( (flags & FLAG_NORESIZE) != 0 ) {
 			setResizable( false );
 		}
 		if( (flags & FLAG_SCROLLPANE) != 0 ) {
 			topView		= new ContentPane( false );
 			scrollPane	= new ScrollPane( topView ); // ...SCROLLBAR_AS_NEEDED
 //			scrollPane.setViewportBorder( null );
 //			scrollPane.setBorder( null );
 			setContentPane( scrollPane );
 		} else {
 			topView		= new ContentPane( true );
 			setContentPane( topView );
 		}
 		final JRootPane	rp		= getRootPane();
 		final ActionMap	amap	= rp.getActionMap();
 		final InputMap	imap	= rp.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW );
 		final int		menuMod	= Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
 		final Window	w		= this;
 		
 		// assign Cmd+W and Cmd+M to trigger window closing and minimization
 		// (as in SuperCollider.app)
 		imap.put( KeyStroke.getKeyStroke( KeyEvent.VK_W, menuMod ), "close" );
 		amap.put( "close", new AbstractAction() {
 			public void actionPerformed( ActionEvent e )
 			{
 				dispatchEvent( new WindowEvent( w, WindowEvent.WINDOW_CLOSING ));
 			}
 		});
 		imap.put( KeyStroke.getKeyStroke( KeyEvent.VK_M, menuMod ), "minimize" );
 		amap.put( "minimize", new AbstractAction() {
 			public void actionPerformed( ActionEvent e )
 			{
 				setExtendedState( Frame.ICONIFIED );
 			}
 		});
 		
 		topView.setPreferredSize( cocoaBounds.getSize() );
 		pack();	// frame is made displayable
 		final Rectangle screenBounds = getGraphicsConfiguration().getBounds();
 //		System.out.println( "screenBounds = " + screenBounds );
 //		final Dimension size = getSize();
 		final Insets insets = getInsets();
 //		System.out.println( "insets = " + insets );
 //		System.out.println( "size = " + size );
 		setLocation( screenBounds.x + cocoaBounds.x - insets.left,
 		             (screenBounds.y + screenBounds.height) - (cocoaBounds.y + cocoaBounds.height) - insets.top );
 	}
 	
 	public void setCocoaBounds( Rectangle r )
 	{
 		final Rectangle	screenBounds	= getGraphicsConfiguration().getBounds();
 		final Insets	insets			= getInsets();
 		
 		setBounds( screenBounds.x + r.x - insets.left,
 		           (screenBounds.y + screenBounds.height) - (r.y + r.height) - insets.top,
 		           r.width + (insets.left + insets.right), r.height + (insets.top + insets.bottom) );
 	}
 	
 	public void registerMouseResponder( AbstractMouseResponder r )
 	{
 		if( collMouseResp == null ) collMouseResp = new ArrayList();
 		collMouseResp.add( r );
 	}
 
 	public void unregisterMouseResponder( AbstractMouseResponder r )
 	{
 		collMouseResp.remove( r );
 	}
 	
 	public void setAcceptMouseOver( boolean onOff )
 	{
 		if( acceptsMouseOver != onOff ) {
 			acceptsMouseOver = onOff;
 			if( collMouseResp != null ) {
 				for( int i = 0; i < collMouseResp.size(); i++ ) {
 					((AbstractMouseResponder) collMouseResp.get( i )).setAcceptMouseOver( onOff );
 				}
 			}
 		}
 	}
 
 	public boolean getAcceptMouseOver()
 	{
 		return acceptsMouseOver;
 	}
 
      private static boolean isMacOs() {
    	 return System.getProperty( "os.name" ).contains( "Mac" );
      }
     
      private static boolean isWindows() {
    	 return System.getProperty( "os.name" ).contains( "Windows" );
      }
      
     /**
      *	A slightly modified version of what was published here
      *	http://www.beatniksoftware.com/jujitsu/svn/trunk/src/e/util/GuiUtilities.java
      */
     public void setAlpha( float alpha )
     {
     	try {
 	    	final Field peerField = Component.class.getDeclaredField( "peer" );
 	    	peerField.setAccessible( true );
 	    	final Object peer = peerField.get( this );
 	    	if( peer == null ) {
 //	    		System.err.println( "peer == null" );
 	    		return;
 	    	}
 	    	
 	    	if( isMacOs() ) {
 	    		final Class cWindowClass = Class.forName("apple.awt.CWindow");
 	    		if( cWindowClass.isInstance( peer )) {
 	    			// ((apple.awt.CWindow) peer).setAlpha( alpha );
 	    			final Method setAlphaMethod = cWindowClass.getMethod( "setAlpha", new Class[] { float.class });
 	                setAlphaMethod.invoke( peer, new Object[] { new Float( alpha )});
 	    		}
 	    	} else if( isWindows() ) {
 	    		// FIXME: can we do this on Windows?
 	        } else {
 	           	// long windowId = peer.getWindow();
 	        	final Class xWindowPeerClass = Class.forName( "sun.awt.X11.XWindowPeer" );
 	        	final Method getWindowMethod = xWindowPeerClass.getMethod( "getWindow", new Class[ 0 ]);
 	        	final long windowId = ((Long) getWindowMethod.invoke( peer, new Object[ 0 ])).longValue();
 	        	final long value = (int) (0xFF * alpha) << 24;
 	            // sun.awt.X11.XAtom.get("_NET_WM_WINDOW_OPACITY").setCard32Property(windowId, value);
 	        	final Class xAtomClass = Class.forName("sun.awt.X11.XAtom");
 	            final Method getMethod = xAtomClass.getMethod( "get", new Class[] { String.class });
 	            final Method setCard32PropertyMethod = xAtomClass.getMethod( "setCard32Property", new Class[] { long.class, long.class });
 	            setCard32PropertyMethod.invoke( getMethod.invoke( null, new Object[] { "_NET_WM_WINDOW_OPACITY" }), new Object[] { new Long( windowId ), new Long( value )});
 	        }
     	} catch( Exception ex ) {
     		ex.printStackTrace();
     		return;
         }
     }
     
 //    public void setCocoaBounds( Rectangle r )
 //    {
 //    	
 //		screenBounds 	= JSCWindow.screenBounds( server );
 //		cocoaHeight	= java.height - 22;
 //
 //		^Rect.new( java.left, screenBounds.height - java.top - 22 - cocoaHeight, java.width, cocoaHeight );
 //    }
 }
