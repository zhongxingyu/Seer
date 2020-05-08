 package dlib.graphics.irenderer;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.event.KeyListener;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.util.EmptyStackException;
 import java.util.Stack;
 
 import dlib.graphics.Renderer;
 import dlib.graphics.Transformable;
 import dlib.graphics.Renderer.ShapeType;
 import dlib.util.DGraphics;
 
 public class Graphics2DImageRenderer implements Renderer
 {
 	BufferedImage im;
 	Graphics2D g;
 	
 	Color fill = new Color( 255, 255, 255, 0 );
 	Color stroke = new Color( 0, 0, 0 );
 	Stack<AffineTransform> mat = new Stack<AffineTransform>();
 	
 	public Graphics2DImageRenderer( int x, int y )
 	{
 		im = DGraphics.createBufferedImage( x, y );
 		g = im.createGraphics();
 	}
 	
 	public void beginShape( ShapeType type )
 	{
 		
 	}
 
 	public void box( float width, float height, float length )
 	{
 		
 	}
 
 	public void ellipse( float x, float y, float width, float height )
 	{
 		pushMatrix();
 		translate( -width/2, -height/2 );
 		g.setColor( fill );
 		g.fillOval( (int)x, (int)y, (int)width, (int)height );
 		g.setColor( stroke );
 		g.drawOval( (int)x, (int)y, (int)width, (int)height );
 		popMatrix();
 	}
 
 	public void endShape()
 	{	
 		
 	}
 
 	public void fill( int c )
 	{
 		fill = new Color( c, true ); 
 	}
 
 	public void fill( float r, float g, float b )
 	{
 		fill( DGraphics.rgba( (int)r, (int)g, (int)b, 255 ) );
 	}
 
 	public void fill( float r, float g, float b, float a )
 	{
 		fill( DGraphics.rgba( (int)r, (int)g, (int)b, (int)a ) );
 	}
 
 	public void line( float x1, float y1, float x2, float y2 )
 	{
 		g.drawLine( (int)x1, (int)y1, (int)x2, (int)y2 );
 	}
 
 	public void line( float x1, float y1, float z1, float x2, float y2, float z2 )
 	{
 		g.drawLine( (int)x1, (int)y1, (int)x2, (int)y2 );
 	}
 
 	public void popMatrix()
 	{
 		try
 		{
 			g.setTransform( mat.pop() );
 		}
 		catch( EmptyStackException e )
 		{
 			System.err.println( "Stack is empty" );
 		}
 	}
 
 	public void pushMatrix()
 	{
 		mat.push( g.getTransform() );
 	}
 
 	public void rect( float x, float y, float width, float height )
 	{
 		g.setColor( fill );
 		g.fillRect( (int)x, (int)y, (int)width, (int)height );
 		g.setColor( stroke );
 		g.drawRect( (int)x, (int)y, (int)width, (int)height );
 	}
 
 	public void rotate( float angle, float vx, float vy, float vz )
 	{
 		g.rotate( angle );
 	}
 
 	public void scale( float x, float y )
 	{
 		g.scale( x, y );
 	}
 
 	public void scale( float x, float y, float z )
 	{
 		g.scale( x, y );
 	}
 
 	public void stroke( int c )
 	{
 		stroke = new Color( c, true ); 
 	}
 
 	public void stroke( float r, float g, float b )
 	{
 		stroke( DGraphics.rgba( (int)r, (int)g, (int)b, 255 ) );
 	}
 
 	public void stroke( float r, float g, float b, float a )
 	{
 		stroke( DGraphics.rgba( (int)r, (int)g, (int)b, (int)a ) );
 	}
 	
 	public void text( String text, float x, float y )
 	{	
 		g.setColor( stroke );
 		g.drawString( text, x, y );
 	}
 	
 	public void drawImage( Image im, float x, float y )
 	{
 		g.drawImage( im, (int)x, (int)y, null );
 	}
 
 	public void texture( Image img )
 	{	
 		
 	}
 
 	public void translate( float x, float y )
 	{	
 		g.translate( x, y );
 	}
 
 	public void translate( float x, float y, float z )
 	{	
 		g.translate( x, y );
 	}
 
 	public void vertex( float x, float y )
 	{	
 		
 	}
 
 	public void vertex( float x, float y, float z )
 	{
 		
 		
 	}
 	
 	public void rotateX( float angle ){}
 	public void rotateY( float angle ){}
 	public void rotateZ( float angle ){}
 	
 	public void rotate( float angle ) 
 	{ 
 		g.rotate( angle ); 
 	}
 
 	public void begin() {
 		
 	}
 
 	public void frameRate(float r) {
 		
 	}
 
 	public void initialize() {
 		
 	}
 
 	public void size(int x, int y) {
 		
 	}
 
 	public void update() {
 		
 	}
 	
 	public BufferedImage getImage()
 	{
 		return im;
 	}
 
 	public void addKeyListener(KeyListener listener) 
 	{
 		
 	}
 }
