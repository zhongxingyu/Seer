 package js.math;
 
 import android.util.FloatMath;
 
 public class Vector2D
 {
 	public float x;
 	public float y;
 
 	public Vector2D()
 	{
 	}
 
 	public Vector2D(float x, float y)
 	{
 		this.x = x;
 		this.y = y;
 	}
 
 	public Vector2D(Vector2D v)
 	{
 		this.x = v.x;
 		this.y = v.y;
 	}
 
 	public final Vector2D clone()
 	{
 		return new Vector2D(x, y);
 	}
 
 	public final void plus(Vector2D v)
 	{
 		x += v.x;
 		y += v.y;
 	}
 
 	public final void plus(float x, float y)
 	{
 		this.x += x;
 		this.y += y;
 	}
 
 	public final void plus(float x, float y, float s)
 	{
 		this.x += x * s;
 		this.y += y * s;
 	}
 
 	public final void plus(Vector2D v, float s)
 	{
 		x += v.x * s;
 		y += v.y * s;
 	}
 
 	public final void plus(Vector2D a, Vector2D b)
 	{
 		x = a.x + b.x;
 		y = a.y + b.y;
 	}
 
 	public final void minus(Vector2D v)
 	{
 		x -= v.x;
 		y -= v.y;
 	}
 
 	public final void minus(Vector2D v, float s)
 	{
 		x -= v.x * s;
 		y -= v.y * s;
 	}
 
 	public final void minus(Vector2D a, Vector2D b)
 	{
 		x = a.x - b.x;
 		y = a.y - b.y;
 	}
 
 	public final void negative()
 	{
 		x = -x;
 		y = -y;
 	}
 
 	public final float length()
 	{
 		return FloatMath.sqrt(x * x + y * y);
 	}
 
 	public final float quad()
 	{
 		return x * x + y * y;
 	}
 
	public final float dist2(Vector2D b)
	{
		return (b.x - x) * (b.x - x) + (b.y - y) * (b.y - y);
	}

 	public final void normalize()
 	{
 		float length = length();
 
 		if (MathUtils.isZero(length) == false)
 		{
 			x /= length;
 			y /= length;
 		}
 		else
 		{
 			x = 0;
 			y = 0;
 		}
 	}
 
 	public final void scale(float scalar)
 	{
 		x *= scalar;
 		y *= scalar;
 	}
 
 	public final void interpolate(Vector2D a, Vector2D b, float position)
 	{
 		x = (b.x - a.x) * position + a.x;
 		y = (b.y - a.y) * position + a.y;
 	}
 
 	public final void set(float x, float y)
 	{
 		this.x = x;
 		this.y = y;
 	}
 
 	public final void set(Vector2D a)
 	{
 		this.x = a.x;
 		this.y = a.y;
 	}
 
 	public final void set(Vector2D a, float scalar)
 	{
 		this.x = a.x * scalar;
 		this.y = a.y * scalar;
 	}
 
 	public final float scalar(Vector2D b)
 	{
 		return x * b.x + y * b.y;
 	}
 
 	public final static float scalar(Vector2D a, Vector2D b)
 	{
 		return a.x * b.x + a.y * b.y;
 	}
 
 	public final float dot(Vector2D b)
 	{
 		return x * b.y - y * b.x;
 	}
 
 	public final static float dot(Vector2D a, Vector2D b)
 	{
 		return a.x * b.y - a.y * b.x;
 	}
 
 	public final float cosFi(Vector2D b)
 	{
 		float l1 = quad();
 
 		if (MathUtils.isZero(l1))
 		{
 			return 0;
 		}
 
 		float l2 = b.quad();
 
 		if (MathUtils.isZero(l2))
 		{
 			return 0;
 		}
 
 		return scalar(b) / ((FloatMath.sqrt(l1) * FloatMath.sqrt(l2)));
 	}
 
 	@Override
 	public String toString()
 	{
 		return String.format("%3.3f %3.3f", x, y);
 	}
 }
