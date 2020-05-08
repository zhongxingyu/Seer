 package ex3.render.raytrace;
 
 import java.util.Map;
 
 import math.Ray;
 import math.Vec;
 
 
 /**
  * Simple Sphere object
  * 
  */
 public class Sphere extends Object3D {
 
 	protected Vec center;
 	protected double radius;
 
 	@Override
 	public double nearestIntersection(Ray ray) {
 
 		// calculate sphere intersection using the algebraic method
        double a = 1;
        double b = ray.direction.scale(2);
		return Double.POSITIVE_INFINITY;
 	}
 
 	@Override
 	public Vec normalAt(Vec intersection, Ray ray) {
 
		// TODO: Implement sphere normal computation
		return null;
 	}
 
 	@Override
 	public void init(Map<String, String> attributes) {
 		if (attributes.containsKey("center"))
 			center = new Vec(attributes.get("center"));
 		if (attributes.containsKey("radius"))
 			radius = Double.valueOf(attributes.get("radius"));
 		super.init(attributes);
 	}
 }
