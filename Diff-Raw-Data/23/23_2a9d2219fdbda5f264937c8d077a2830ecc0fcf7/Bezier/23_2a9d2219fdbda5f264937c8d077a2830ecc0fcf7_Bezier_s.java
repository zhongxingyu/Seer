 package osu.math;
 
 import android.graphics.PointF;
 import android.util.FloatMath;
 /**
  * A simple Bezier curve evaluator
  * @author dkilian
  */
 public class Bezier 
 {
 	/** A preallocated temporary storage buffer, for GC performance */
 	private static float[] buf;
 	/** GC */
 	private static PointF p = new PointF();
 	
 	/**
 	 * Evaluates an array of 2D points, arranged [x1 y1 x2 y2 ... xn yn]
 	 * @param points The points to interpolate
 	 * @param t The point along the curve to evaluate, between 0 and 1
 	 * @param target Receives the X and Y coordinates of the evaluated path
 	 */
 	public static void evaluate2d(float[] points, float t, PointF target)
 	{		
 		if (buf == null || buf.length < points.length)
 			buf = new float[points.length];
 		
 		for (int i = 0; i < points.length; ++i)
 			buf[i] = points[i];
 		
 		float oneMinusT = 1.f - t;
 		int degree = points.length / 2 - 1;
 		
 		for (int i = 0; i < degree; ++i)
 		{
 			for (int j = 0; j < degree - i; ++j)
 			{
 				buf[2*j+0] = oneMinusT * buf[2*j+0] + t * buf[2*(j+1)+0];
 				buf[2*j+1] = oneMinusT * buf[2*j+1] + t * buf[2*(j+1)+1];
 			}
 		}
 		
 		target.x = buf[0];
 		target.y = buf[1];
 	}
 	
 	/**
 	 * Computes an estimation of the length of the given bezier path
 	 * @param points The points that compose the path
 	 * @return The length of the path, in whatever units the control points are defined in
 	 */
 	public static float length(float[] points)
 	{
		return length(points, .1f);
 	}
 
 	/**
 	 * Computes an estimation of the length of the given bezier path
 	 * @param points The points that compose the path
 	 * @param dt The step amount. Larger values yield more accurate results but more computation time.
 	 * @return The length of the path, in whatever units the control points are defined in
 	 */
 	public static float length(float[] points, float dt)
 	{
 		float x0 = points[0], y0 = points[1];
 		float len = 0;
 		
 		for (float t = 0.f; t <= 1.f; t += dt)
 		{
 			evaluate2d(points, t, p);
 			float x = p.x;
 			float y = p.y;
 			len += FloatMath.sqrt((x - x0) * (x - x0) + (y - y0) * (y - y0));
 			x0 = x;
 			y0 = y;
 		}
 		
 		return len;
 	}
 }
