 package com.phyloa.dlib.dui;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 import javax.vecmath.Vector2f;
 
 import com.phyloa.dlib.renderer.Renderer2D;
 import com.phyloa.dlib.util.DMath;
 
 public class DLinePlot extends DUIElement
 {
 	ArrayList<String[]> labels = new ArrayList<String[]>();
 	ArrayList<Color> colors = new ArrayList<Color>();
 	ArrayList<float[]> values = new ArrayList<float[]>();
 	float maxValue = 0;
 	
 	String labelToShow;
 	float lx;
 	float ly;
 	Color labelColor;
 	
 	public float labelShowDist = 10;
 	
 	public DLinePlot( int width, int height )
 	{
 		super( 0, 0, width, height );
 	}
 	
 	public DLinePlot( int x, int y, int width, int height )
 	{
 		super( x, y, width, height );
 	}
 
 	public void render( Renderer2D g )
 	{
 		g.setLineWidth( 3 );
 		
 		g.pushMatrix();
 		g.translate( x, y );
 		g.color( Color.white );
 		g.line( 0, 0, 0, height );
 		g.line( 0, height, width, height );
 		float yScale = height / maxValue;
 		for( int i = 0; i < colors.size(); i++ )
 		{
 			g.color(  colors.get( i ) );
 			float[] vals = values.get( i );
 			if( vals.length <= 1 ) continue;
 			float xScale = width / (vals.length-1);
 			for( int j = 0; j < vals.length-1; j++ )
 			{
 				g.line( j*xScale, height - vals[j] * yScale, (j+1)*xScale, height - vals[j+1] * yScale );
 			}
 		}
 		
 		if( labelToShow != null )
 		{
 			g.color( labelColor );
 			g.drawOval( lx-5, ly-5, 10, 10 );
 			Vector2f strSize = g.getStringSize( labelToShow );
 			g.text( labelToShow, lx-strSize.x/2, ly - 10 - strSize.y );
 		}
 		
 		g.popMatrix();
 		
 		g.setLineWidth( 1 );
 	}
 
 	public void update( DUI ui )
 	{
 		
 	}
 	
 	public void addLine( float[] arr, Color c )
 	{
 		colors.add( c );
 		values.add( arr );
 		String[] label = new String[arr.length];
 		for( int i = 0; i < arr.length; i++ )
 		{
 			label[i] = Float.toString( arr[i] );
 		}
 		labels.add( label );
 		float max = DMath.maxf( arr );
 		if( max > maxValue )
 			maxValue = max;
 	}
 	
 	public void addLine( int[] arr, Color c )
 	{
 		float[] v = new float[arr.length];
 		String[] label = new String[arr.length];
 		for( int i = 0; i < v.length; i++ )
 		{
 			v[i] = arr[i];
 			label[i] = Integer.toString( arr[i] );
 		}
 		colors.add( c );
 		values.add( v );
 		labels.add( label );
 		float max = DMath.maxf( v );
 		if( max > maxValue )
 			maxValue = max;
 	}
 	
 	public void keyPressed( DKeyEvent dke )
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void keyReleased( DKeyEvent dke )
 	{
 		// TODO Auto-generated method stub
 		
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
 	public void mousePressed( DMouseEvent e )
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseReleased( DMouseEvent e )
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void mouseMoved( DMouseEvent e )
 	{
 		calcMouseHover( e.x-x, e.y-y );
 	}
 
 	public void mouseDragged( DMouseEvent e )
 	{
 	
 	}
 
 	public void mouseWheel( DMouseEvent dme )
 	{
 		
 	}
 	
 	public void calcMouseHover( int mx, int my )
 	{
 		float dist = Float.MAX_VALUE;
 		float yScale = height / maxValue;
 		for( int i = 0; i < colors.size(); i++ )
 		{
 			float[] vals = values.get( i );
 			float xScale = width / (vals.length-1);
 			for( int j = 0; j < vals.length; j++ )
 			{
 				float tx = j*xScale;
 				float ty = height - vals[j] * yScale;
 				
 				float dx = tx-mx;
 				float dy = ty-my;
 				float td = dx*dx + dy*dy;
 				if( td < dist )
 				{
 					dist = td;
 					lx = tx;
 					ly = ty;
 					labelToShow = labels.get( i )[j];
 					labelColor = colors.get( i );
 				}
 			}
 		}
 		
 		if( dist > labelShowDist*labelShowDist )
 		{
 			labelToShow = null;
 		}
 	}
 }
