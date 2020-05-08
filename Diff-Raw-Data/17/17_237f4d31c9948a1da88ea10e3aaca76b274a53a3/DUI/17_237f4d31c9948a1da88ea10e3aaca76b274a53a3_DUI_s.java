 package com.phyloa.dlib.dui;
 
 import java.awt.Component;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.util.ArrayList;
 
 import com.phyloa.dlib.renderer.Renderer2D;
 import com.phyloa.dlib.util.DKeyHandler;
 import com.phyloa.dlib.util.DMouseHandler;
 
 public class DUI implements DMouseListener, DKeyListener
 {
 	DEventMapper dem;
 	ArrayList<DUIListener> listeners = new ArrayList<DUIListener>();
 	
 	DUIElement focus = null;
 	DUIElement hover = null;
 	DUIElement rootPane = new DPanel( 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE );
 	
 	boolean enabled = true;
 	
 	public DUI( DEventMapper dem )
 	{
 		this.dem = dem;
 		dem.addDKeyListener( this );
 		dem.addDMouseListener( this );
 	}
 	
 	public DUI( DEventMapper dem, int x, int y, int width, int height )
 	{
 		this( dem );
 		rootPane = new DPanel( x, y, width, height );
 	}
 	
 	public void update()
 	{
 		if( enabled )
 		{
 			rootPane.update( this );
 			rootPane.updateChildren( this );
 		}
 	}
 	
 	public void render( Renderer2D r )
 	{
 		if( enabled )
 		{
 			rootPane.render( r );
 			rootPane.renderChildren( r );
 		}
 	}
 	
 	public void addDUIListener( DUIListener l )
 	{
 		listeners.add( l );
 	}
 	
 	public void add( DUIElement e )
 	{
 		rootPane.add( e );
 		e.setUI( this );
 	}
 	
 	public void event( DUIEvent e )
 	{
 		for( DUIListener l : listeners )
 		{
 			l.event( e );
 		}
 	}
 
 	public DUIElement getFocus()
 	{
 		return focus;
 	}
 
 	public void setFocus( DUIElement focus )
 	{
 		this.focus = focus;
 	}
 
 	public void mouseMoved( DMouseEvent e )
 	{
 		if( enabled )
 		{
 			rootPane.mouseMoved( e );
 			rootPane.handleChildrenMouseMoved( e );
 		}
 	}
 
 	public void mousePressed( DMouseEvent e )
 	{
 		if( enabled )
 		{
 			rootPane.mousePressed( e );
 			rootPane.handleChildrenMousePressed( e );
 		}
 	}
 
 	public void mouseReleased( DMouseEvent e )
 	{
 		if( enabled )
 		{
 			rootPane.mouseReleased( e );
 			rootPane.handleChildrenMouseReleased( e );
 		}
 	}
 
 	@Override
 	public void keyPressed( DKeyEvent dke )
 	{
 		if( enabled && focus != null )
 		{
 			focus.keyPressed( dke );
 		}
 	}
 
 	@Override
 	public void keyReleased( DKeyEvent dke )
 	{
 		if( enabled && focus != null )
 		{
 			focus.keyReleased( dke );
 		}
 	}
 
 	@Override
 	public void mouseEntered( DMouseEvent e )
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseExited( DMouseEvent e )
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseDragged( DMouseEvent e )
 	{
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void setEnabled( boolean enabled )
 	{
 		this.enabled = enabled;
 		if( enabled )
 		{
 			dem.addDKeyListener( this );
 			dem.addDMouseListener( this );
 		} else
 		{
 			dem.removeDKeyListener( this );
 			dem.removeDMouseListener( this );
 		}
 	}
 }
