 package dlib.math;
 
import javax.vecmath.Point3f;

 public class Intersection
 {
 	Rayf ray;
	Geom g;
	Point3f loc;
 	
	public Intersection( Point3f loc, Rayf ray, Geom g )
	{
		this.ray = ray;
		this.g = g;
		this.loc = loc;
	}
 }
