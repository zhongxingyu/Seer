 package com.musicgame.PumpAndJump;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Intersector;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Polygon;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.musicgame.PumpAndJump.Util.IntersectionUtil;
 import com.musicgame.PumpAndJump.Util.TextureMapping;
 import com.musicgame.PumpAndJump.Util.AnimationUtil.Point;
 
 public abstract class Model
 {
 	public Point p;
 	public Point angle;
 	public Point scalar;
 	public Point gp;
 	public Point ga;
 	public Polygon hull;
 	public Polygon poly;
 	Matrix4 before;
	Sprite image;
 	ArrayList< Model > children;
 
 	public Model( Point _p, Point _angle, Point _scale )
 	{
 		p = _p; angle =  _angle; scalar = _scale;
 		children = new ArrayList< Model >();
 	}
 
 	public void pushTransforms( SpriteBatch sb )
 	{
 		Matrix4 mv = sb.getTransformMatrix();
 		before = new Matrix4( mv );
 
 		sb.setTransformMatrix( getModelView( mv ) );
 	}
 
 	public void translate( float x, float y, float z )
 	{
 		p.x += x;
 		p.y += y;
 		p.z += z;
 	}
 
 	public void rotate( float x_degrees, float y_degrees, float z_degrees )
 	{
 		angle.x += x_degrees;
 		angle.y += y_degrees;
 		angle.z += z_degrees;
 	}
 
 	public void scale( float x_scalar, float y_scalar, float z_scalar )
 	{
 		scalar.x *= x_scalar;
 		scalar.y *= y_scalar;
 		scalar.z *= z_scalar;
 	}
 
 	public void popTransforms( SpriteBatch sb )
 	{
 		sb.setTransformMatrix( before );
 	}
 
 	public void drawSprite( SpriteBatch sb )
 	{
 		if( image != null )
 			image.draw( sb );
 	}
 
 	public abstract void display( SpriteBatch sb );
 
 	protected Matrix4 getModelView( Matrix4 mv )
 	{
 		mv.translate( p.x, p.y, p.z );
 
 		mv.rotate( 1.0f, 0.0f, 0.0f, angle.x );
 		mv.rotate( 0.0f, 1.0f, 0.0f, angle.y );
 		mv.rotate( 0.0f, 0.0f, 1.0f, angle.z );
 
 		mv.scale( scalar.x, scalar.y, scalar.z );
 
 		return mv;
 	}
 
 	public void update( Matrix4 before )
 	{
 		Matrix4 mv = getModelView( before.cpy() );
 
 		if( poly != null )
 		{
 			Vector2[] points = IntersectionUtil.FloatToVector2( poly.getVertices() );
 
 			for( Vector2 p : points )
 			{
 				Vector3 point= new Vector3( p.x, p.y, 0 );
 
 				point = point.mul( mv );
 
 				p.x = point.x;
 				p.y = point.y;
 			}
 
 			float[] fpoints = IntersectionUtil.Vector2ToFloat( points );
 			hull = new Polygon( fpoints );
 
 
 		}
 		for( Model m : children )
 		{
 			m.update( new Matrix4( mv ) );
 		}
 	}
 
 	public boolean intersects( Polygon otherHull )
 	{
 		if( hull != null )
 		{
 			if( Intersector.overlapConvexPolygons( hull, otherHull ) )
 				return true;
 		}
 
 		for( Model m: children )
 		{
 			if( m.intersects(otherHull) )
 				return true;
 		}
 
 		return false;
 	}
 
 	public void print()
 	{
 		if( hull != null )
 		{
 			float[] points = hull.getVertices();
 			for( int i = 0; i < points.length; i+=2 )
 			{
 				System.out.println( "("+points[i]+","+points[i+1]+")" );
 			}
 		}
 		else
 			System.out.println( hull );
 	}
 }
