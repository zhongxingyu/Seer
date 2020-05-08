 package dlib.util;
 
 import javax.vecmath.Point3f;
 import javax.vecmath.Vector3f;
 
 import dlib.math.Intersection;
 import dlib.math.Rayf;
 import dlib.math.Trianglef;
 
 public class DGeom
 {
 	public static Intersection RayTriCol(Rayf ray, Trianglef tri)
 	{
 		float epsilon = .00001f;
 
 		Point3f pt0 = tri.p1;
 		Point3f pt1 = tri.p2;
 		Point3f pt2 = tri.p3;
 		Vector3f e1 = new Vector3f();
 		Vector3f e2 = new Vector3f();
 		e1.sub(pt1, pt0);
 		e2.sub(pt2, pt0);
 		Vector3f p = new Vector3f();
 		p.cross(ray.dir, e2);
 		float a;
 		a = e1.dot(p);
 		if( a > -epsilon && a < epsilon )
 		{
 			return null;
 		}
 
 		float f = 1.f / a;
 		Vector3f s = new Vector3f();
 		s.sub(ray.loc, pt0);
 		float u = f * s.dot(p);
 
 		if( u < 0 || u > 1 )
 		{
 			return null;
 		}
 
 		Vector3f q = new Vector3f();
 		q.cross( s, e1 );
 		float v = f * ray.dir.dot(q);
 
 		if(v < 0 || u + v > 1)
 		{
 			return null;
 		}
 
 		float t = f * e2.dot(q);
 
 		if( t < 0 )
 		{
 			return null;
 		}
 		t *= .9999999999;
 		Point3f pos = new Point3f();
 		pos.set(ray.dir);
 		pos.scale(t);
 
 		Vector3f dist = new Vector3f();
 		dist.set(pos);
 		pos.add( ray.loc );
		return new Intersection( pos, ray, tri );
 	}
 }
