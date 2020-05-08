 package tesseract.forces;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.media.j3d.Transform3D;
 import javax.vecmath.Quat4f;
 import javax.vecmath.Vector2f;
 import javax.vecmath.Vector3f;
 
 import tesseract.objects.PhysicalObject;
 
 public class AirDrag extends Force {
 
 	private static final float COEFFICIENT = 20f;
 
 
 	@Override
 	protected Vector3f calculateForce(PhysicalObject obj) {
 		if (obj.isNodeNull() || obj.getVelocity().length() == 0) {
 			return new Vector3f();
 		}
 		
 		Vector3f v = new Vector3f(obj.getVelocity());
 		
 		Vector3f p = new Vector3f(obj.getPosition());
 		p.negate();
 		
 		Vector3f c = new Vector3f();
 		c.sub(new Vector3f(0, 1, 0), v);
 		
 		Quat4f r = new Quat4f(c.x, c.y, c.z, 0);
 		r.normalize();
 		
 		
 		Vector3f com = new Vector3f(-obj.getCenterOfMass().x, -obj.getCenterOfMass().y, -obj.getCenterOfMass().z);
 		com.negate();
 		
 		Transform3D tmp = new Transform3D();
 		tmp.setTranslation(com);
 		Transform3D tmp2 = new Transform3D();
 		tmp2.setRotation(r);
 		com.negate();
 		com.add(p);
 		tmp2.setTranslation(com);
 		tmp2.mul(tmp);
 		
 		ArrayList<Vector3f> vertices = obj.getVertices();
 		ArrayList<Vector2f> points = new ArrayList<Vector2f>(); 
 		
 		for (Vector3f point : vertices) {
 			tmp2.transform(point);
 			Vector2f newPoint = new Vector2f(point.x, point.z);
 			
 			// Place min y at front of arraylist if it's the minimum
 			if (points.size() == 0) {
 				points.add(newPoint);
 				
 			} else if (newPoint.y < points.get(0).y
 					|| (newPoint.y == points.get(0).y 
 							&& newPoint.x < points.get(0).x)) {
 				Vector2f oldPoint = points.get(0);
 				points.set(0, newPoint);
 				points.add(oldPoint);
 				
 			} else {
 				points.add(newPoint);
 			}
 		}
 		
 		List<Vector2f> hull = convexHull(points);
 		
 		float surfaceArea = areaOfHull(hull);
 		
 		float force = 0.5f * v.lengthSquared() * COEFFICIENT * surfaceArea; 
 		
 		v.normalize();
 		v.scale(-force);
 		
		return new Vector3f();
 		
 	}
 	
 	/**
 	 * 
 	 * @param hull vector list.
 	 * @return area
 	 */
 	private float areaOfHull(final List<Vector2f> hull) {
 		float area = 0;
 		Vector2f p = hull.get(0);
 		
 		for (int i = 2; i < hull.size(); i++) {
 			// Area of triangle p0 - p(i-1) - p(i)
 			Vector2f ab = new Vector2f();
 			Vector2f ac = new Vector2f();
 			
 			ab.sub(hull.get(i - 1), p);
 			ac.sub(hull.get(i), p);
 			
 			area += 0.5f * (ab.x * ac.y - ac.x * ab.y);
 		}
 		
 		return area;
 	}
 	
 	/**
 	 * Graham's convex hull algorithm from pseudocode on wikipedia.
 	 * @param points point list.
 	 * @return point list.
 	 */
 	private List<Vector2f> convexHull(final ArrayList<Vector2f> points) {
 		Collections.sort(points, new Vector2fAngleCompare(points.get(0)));
 		
 		points.set(0, points.get(points.size() - 1));
 		
 		int m = 2;
 		for (int i = m + 1; i < points.size(); i++) {
 			try {
 			while (i < points.size() - 1 && ccw(points.get(m - 1),
 					points.get(m), points.get(i)) <= 0) {
 				if (m == 2) {
 					final Vector2f vec = points.get(m);
 					points.set(m, points.get(i));
 					points.set(i, vec);
 					i++;
 					
 				} else {
 					m--;
 				}
 			}
 			} catch (Exception e) {
 				System.out.println(e);
 			}
 			
 			m++;
 			
 			final Vector2f vec = points.get(m);
 			points.set(m, points.get(i));
 			points.set(i, vec);
 		}
 		
 		return points.subList(0, m + 1);
 	}
 	
 	/**
 	 * 
 	 * @param v1 vector.
 	 * @param v2 vector.
 	 * @param v3 vector.
 	 * @return result
 	 */
 	private float ccw(final Vector2f v1, final Vector2f v2, final Vector2f v3) {
 		return (v2.x - v1.x) * (v3.y - v1.y) - (v2.y - v1.y) * (v3.x - v1.x);  
 	}
 	
 	
 	/**
 	 * 
 	 * 
 	 *
 	 */
 	private class Vector2fAngleCompare implements Comparator<Vector2f> {
 		/**
 		 * Base vector.
 		 */
 		Vector2f base;
 		
 		/**
 		 * constructor.
 		 * @param theBase the base.
 		 */
 		public Vector2fAngleCompare(final Vector2f theBase) {
 			base = theBase;
 		}
 		
 		/**
 		 * @param o1 vector to compare
 		 * @param o2 vector2 to compare
 		 * @return comparison
 		 */
 		public int compare(final Vector2f o1, final Vector2f o2) {
 			return (int) Math.signum(vecAngle(o1) - vecAngle(o2));
 		}	
 		
 		/**
 		 * 
 		 * @param vector to look at.
 		 * @return result
 		 */
 		private float vecAngle(final Vector2f vector) {
 			final Vector2f v = new Vector2f();
 			v.sub(vector, base);
 			
 			return v.y / (v.x * v.x + v.y * v.y);
 		}
 	}
 }
