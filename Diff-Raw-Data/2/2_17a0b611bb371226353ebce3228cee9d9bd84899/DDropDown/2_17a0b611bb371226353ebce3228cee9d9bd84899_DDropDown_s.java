 package com.phyloa.dlib.dui;
 
 import java.util.ArrayList;
 
 import com.phyloa.dlib.math.Point2i;
 import jp.objectclub.vecmath.Vector2f;
 
 import com.phyloa.dlib.renderer.Renderer2D;
 
 public class DDropDown extends DUIElement
 {
 	ArrayList<String> items = new ArrayList<String>();
 	
 	int selected = 0;
 	
 	boolean expanded = false;
 	
 	DDropDownTopPanel topPanel;
 	
 	public DDropDown( int x, int y, int width, int height )
 	{
 		super( x, y, width, height );
 		topPanel = new DDropDownTopPanel( x, y, width, height );
 		topPanel.ddd = this;
 	}
 	
 	public void addItems( String... items )
 	{
 		for( String s : items )
 		{
 			this.items.add( s );
 		}
 		topPanel.height = (int)getExpandedHeight();
 	}
 	
 	public void addItems( Object... items )
 	{
 		for( Object s : items )
 		{
 			this.items.add( s.toString() );
 		}
 		topPanel.height = (int)getExpandedHeight();
 	}
 	
 	public String getSelected()
 	{
 		return items.get( selected );
 	}
 	
 	float getExpandedHeight()
 	{
 		return Math.max( height, height*items.size() );
 	}
 	
 	public void render( Renderer2D r )
 	{
 		if( items.size() == 0 )
 		{
 			r.color( ui.theme.backgroundColor );
 			r.fillRect( x, y, width, height );
 			r.color(  ui.theme.borderColor );
 			r.drawRect( x, y, width, height );
 			return;
 		}
 		
 		r.pushMatrix();
 		r.translate( x, y );
 		
 		Vector2f stringSize = r.getStringSize( items.get( selected ) );
 		if( expanded )
 		{
 			
 		}
 		else
 		{
 			r.color( ui.theme.backgroundColor );
 			r.fillRect( 0, 0, width, height );
 			r.color( ui.theme.borderColor );
 			r.drawRect( 0, 0, width, height );
 			
 			r.text( items.get( selected ), 10, height/2 - stringSize.y/2 );
 			
 			
 			r.line( width-height, 0, width-height, height );
 			float w2 = width - height/2;
 			float h2 = height/2;
 			r.line( w2 - 5, h2 - 5, w2 + 5, h2 - 5 );
 			r.line( w2, h2 + 5, w2 + 5, h2 - 5 );
 			r.line( w2, h2 + 5, w2 - 5, h2 - 5 );
 			
 		}
 		
 		r.popMatrix();
 	}
 
 	public void update( DUI ui )
 	{
 		
 	}
 	
 	public void setSelected( int i )
 	{
 		selected = i;
 	}
 
 	public void keyPressed( DKeyEvent dke )
 	{
 		
 	}
 
 	public void keyReleased( DKeyEvent dke )
 	{
 		
 	}
 
 	public void mouseEntered( DMouseEvent e )
 	{
 		
 	}
 
 	public void mouseExited( DMouseEvent e )
 	{
 		
 	}
 
 	public void mousePressed( DMouseEvent e )
 	{
 		
 	}
 
 	public void mouseReleased( DMouseEvent e )
 	{
 		float mx = e.x - this.x;
 		float my = e.y - this.y;
 		
 		if( !expanded )
 		{
 			if( mx > width-height )
 			{
 				expanded = true;
 				Point2i loc = getScreenLocation();
 				topPanel.x = loc.x;
 				topPanel.y = loc.y;
 				topPanel.reset();
 				ui.setTopPanel( this, topPanel );
 			}
 		}
 	}
 
 	public void mouseMoved( DMouseEvent e )
 	{
 		
 	}
 
 	public void mouseDragged( DMouseEvent e )
 	{
 		
 	}
 
 	public void mouseWheel( DMouseEvent dme )
 	{
 		
 	}
 	
 	public void losingTopPanel( DUIElement e )
 	{
 		expanded = false;
 	}
 	
 	public static class DDropDownTopPanel extends DUIElement
 	{
 		DDropDown ddd;
 		int hover = 0;
 		
 		public DDropDownTopPanel( int x, int y, int width, int height )
 		{
 			super( x, y, width, height );
 		}
 		
 		public void reset()
 		{
 			hover = 0;
 			visible = true;
 		}
 		
 		public void update( DUI ui )
 		{
 			
 		}
 		
 		public void render( Renderer2D r )
 		{
 			r.pushMatrix();
 			r.translate( x, y );
 			r.color( ui.theme.backgroundColor );
 			r.fillRect( 0, 0, width, height );
 			
 			if( hover > -1 )
 			{
 				r.color( ui.theme.hoverColor );
 				r.fillRect( 0, hover*ddd.height, width, ddd.height );
 			}
 			
 			r.color( ui.theme.borderColor );
 			r.drawRect( 0, 0, width, height );
 			
 			for( int i = 0; i < ddd.items.size(); i++ )
 			{
 				//For some reason I added ddd.height to the text location when I was doing something with Dropdowns in Dialogs
 				//It breaks DDropDowns for tacticshooter though so I'm removing it for now.
 				//r.text( ddd.items.get( i ), 10, ddd.height + i*ddd.height + r.getStringSize( ddd.items.get( i ) ).y/2 );
 				
 				r.text( ddd.items.get( i ), 10, i*ddd.height + r.getStringSize( ddd.items.get( i ) ).y/2 );
 			}
 			r.popMatrix();
 		}
 
 		public void keyPressed( DKeyEvent dke )
 		{
 			
 		}
 
 		public void keyReleased( DKeyEvent dke )
 		{
 			
 		}
 
 		public void mouseEntered( DMouseEvent e )
 		{
 			
 		}
 
 		public void mouseExited( DMouseEvent e )
 		{
 			
 		}
 
 		public void mousePressed( DMouseEvent e )
 		{
 			
 		}
 
 		public void mouseReleased( DMouseEvent e )
 		{
 			float my = e.y - y;
 			int i = (int)(my / ddd.height);
 			ddd.setSelected( i );
 			this.visible = false;
 			ddd.expanded = false;
 			ui.event( new DUIEvent( ddd, ddd.selected ) );
 		}
 
 		public void mouseMoved( DMouseEvent e )
 		{
 			float my = e.y - y;
 			hover = (int)(my / ddd.height);
			if( my < 0 || my > height || e.x < x || e.x > x+width )
 			{
 				hover = -1;
 			}
 		}
 
 		public void mouseDragged( DMouseEvent e )
 		{
 			
 		}
 
 		public void mouseWheel( DMouseEvent dme )
 		{
 			
 		}		
 	}
 
 	public int getSelectedOrdinal()
 	{
 		return selected;
 	}
 
 	public void clearItems()
 	{
 		items.clear();
 		selected = 0;
 	}
 }
