 package ex3.render.raytrace;
 
 import java.util.Map;
 
import com.sun.org.apache.xpath.internal.operations.Variable;
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
        double b;
        double c;

        Vec directionTwice = new Vec(ray.direction);
        Vec diffVector = new Vec(ray.p_origin);
        directionTwice.scale(2);
        diffVector.sub(center);

        // solve the equation:
        // a = 1
        // b = 2V * (Po - O)
        // c = |Po - 0|^2 - r^2
        b = directionTwice.dotProd(diffVector);
        c = diffVector.lengthSquared() - radius * radius;

        return (b * b - 4 * c) / 2;
 	}
 
 	@Override
 	public Vec normalAt(Vec intersection, Ray ray) {
 
        Vec normal = new Vec(intersection);
        normal.sub(center);

        normal.normalize();

		return normal;
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
