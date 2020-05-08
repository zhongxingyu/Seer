 package com.phyloa.dlib.dui;
 
 import java.util.ArrayList;
 
 import javax.vecmath.Point2i;
 
 import com.phyloa.dlib.renderer.Renderer2D;
 
 public abstract class DUIElement implements DKeyListener, DMouseListener
 {
 	int x, y, width, height;
 	public String name;
 	
 	boolean visible = true;
 	DUI ui;
 	
 	ArrayList<DUIElement> children = new ArrayList<DUIElement>();
 	
 	DUIElement parent;
 	
 	boolean isInside = false;
 	
 	public DUIElement( int x, int y, int width, int height )
 	{
 		this.x = x;
 		this.y = y;
 		this.width = width;
 		this.height = height;
 	}
 	
 	public abstract void render( Renderer2D r );
 	public abstract void update( DUI ui );
 	
 	public String getName() 
 	{
 		return name;
 	}
 	
 	public void setName( String name ) 
 	{
 		this.name = name;
 	}
 	
 	public void setLocation( int x, int y )
 	{
 		this.x = x;
 		this.y = y;
 	}
 	
 	public void setSize( int width, int height )
 	{
 		this.width = width;
 		this.height = height;
 	}
 	
 	public boolean isVisible()
 	{
 		return visible;
 	}
 	
 	public void setVisible( boolean visible )
 	{
 		this.visible = visible;
 	}
 	
 	public boolean isInside( int mx, int my )
 	{
 		return mx >= x && my >= y && mx <= x + width && my <= y + height; 
 	}
 	
 	public Point2i getScreenLocation()
 	{
 		if( parent != null )
 		{
 			Point2i p = parent.getScreenLocation();
 			p.x += x;
 			p.y += y;
 			return p;
 		}
 		else
 		{
 			return new Point2i( x, y );
 		}
 	}
 	
 	public void setUI( DUI ui )
 	{
 		this.ui = ui;
 		for( int i = 0; i < children.size(); i++ )
 		{
 			children.get( i ).setUI( ui );
 		}
 	}
 	
 	public void handleChildrenMouseMoved( DMouseEvent e )
 	{
 		if( visible )
 		{
 			e.x = e.x - this.x;
 			e.y = e.y - this.y;
 			for( int i = 0; i < children.size(); i++ )
 			{
 				DUIElement el = children.get( i );
 				boolean inside = el.isInside( e.x, e.y );
 				if( inside || el.isInside && el.isVisible() )
 				{
 					el.isInside = inside;
 					el.mouseMoved( e );
 					el.handleChildrenMouseMoved( e );
 				}
 			}
 			e.x += this.x;
 			e.y += this.y;
 		}
 	}
 	
 	public void handleChildrenMousePressed( DMouseEvent e )
 	{
 		if( visible )
 		{
 			ui.setFocus( this );
 			e.x = e.x - this.x;
 			e.y = e.y - this.y;
 			for( int i = 0; i < children.size(); i++ )
 			{
 				DUIElement el = children.get( i );
 				if( el.isInside( e.x, e.y ) && el.isVisible() )
 				{
 					el.mousePressed( e );
 					el.handleChildrenMousePressed( e );
 				}
 			}
 			e.x += this.x;
 			e.y += this.y;
 		}
 	}
 	
 	public void handleChildrenMouseReleased( DMouseEvent e )
 	{
 		if( visible )
 		{
 			e.x = e.x - this.x;
 			e.y = e.y - this.y;
 			for( int i = 0; i < children.size(); i++ )
 			{
 				DUIElement el = children.get( i );
 				if( el.isInside( e.x, e.y ) && el.isVisible() )
 				{
 					el.mouseReleased( e );
 					el.handleChildrenMouseReleased( e );
 				}
 			}
 			e.x += this.x;
 			e.y += this.y;
 		}
 	}
 	
 	public void handleChildrenMouseDragged( DMouseEvent e )
 	{
 		if( visible )
 		{
 			e.x = e.x - this.x;
 			e.y = e.y - this.y;
 			for( int i = 0; i < children.size(); i++ )
 			{
 				DUIElement el = children.get( i );
 				boolean inside = el.isInside( e.x, e.y );
 				if( inside || el.isInside && el.isVisible() )
 				{
 					el.isInside = inside;
 					el.mouseDragged( e );
 					el.handleChildrenMouseDragged( e );
 				}
 			}
 			e.x += this.x;
 			e.y += this.y;
 		}
 	}
 
 	public void add( DUIElement e )
 	{
 		children.add( e );
 		e.parent = this;
 		e.setUI( ui );
 	}
 
 	public void updateChildren( DUI dui )
 	{
 		if( visible )
 		{
 			for( int i = 0; i < children.size(); i++ )
 			{
 				if( children.get( i ).visible )
 				{
 					children.get( i ).update( dui );
 					children.get( i ).updateChildren( dui );
 				}
 			}
 		}
 	}
 	
 	public void renderChildren( Renderer2D r )
 	{
 		if( visible )
 		{
 			r.pushMatrix();
 			r.translate( x, y );
 			for( int i = 0; i < children.size(); i++ )
 			{
 				if( children.get( i ).visible )
 				{
 					children.get( i ).render( r );
 					children.get( i ).renderChildren( r );
 				}
 			}
 			r.popMatrix();
 		}
 	}
 
 	public void handleChildrenMouseWheel( DMouseEvent e )
 	{
 		if( visible )
 		{
 			e.x = e.x - this.x;
 			e.y = e.y - this.y;
 			for( int i = 0; i < children.size(); i++ )
 			{
 				DUIElement el = children.get( i );
 				if( el.isInside( e.x, e.y ) )
 				{
 					el.mouseWheel( e );
 					el.handleChildrenMouseWheel( e );
 				}
 			}
 			e.x += this.x;
 			e.y += this.y;
 		}
 	}
 	
 	public void losingTopPanel( DUIElement e )
 	{
 		
 	}
 }
