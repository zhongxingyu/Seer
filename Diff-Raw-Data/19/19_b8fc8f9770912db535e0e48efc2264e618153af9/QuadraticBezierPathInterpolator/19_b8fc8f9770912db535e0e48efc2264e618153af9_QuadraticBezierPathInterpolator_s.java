 package se2.e.utilities.path;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se2.e.utilities.PathInterpolator;
 import se2.e.utilities.Vector2D;
 import se2.e.utilities.Where;
 
 public class QuadraticBezierPathInterpolator extends BasePathInterpolator {
 	private List<Vector2D> points = new ArrayList<Vector2D>();
 
 	private static Vector2D intermediate(
 			Vector2D start, Vector2D center, Vector2D end,
 			double distance
 			) {
 		Vector2D q0 = new Vector2D(center).subtract(start).multiply(distance).add(start);
 		Vector2D q1 = new Vector2D(end).subtract(center).multiply(distance).add(center);
 		return new Vector2D(q1).subtract(q0).multiply(distance).add(q0);
 		}
 
 	private static void intermediate(
 			Vector2D p0, Vector2D c, Vector2D p1,
 			List<Vector2D> points
 			) {
 		intermediate(p0, c, p1, points, 0.0, 0.5, 1.0);
 		}
 
 	private static void intermediate(
 			Vector2D start, Vector2D center, Vector2D end,
 			List<Vector2D> points,
 			double t0, double m, double t1) {
 		Vector2D pt0 = intermediate(start, center, end, t0);
 		Vector2D pt1 = intermediate(start, center, end, t1);
 		Vector2D pm = intermediate(start, center, end, m);
 		Vector2D delta = new Vector2D(pt0).add(pt1).divide(2.0); 
 		if (delta.longerThan(1.0)) {
 			intermediate(start,center,end,points,t0,(t0+m)/2.0f,m);
 			points.add(pm);
 			intermediate(start,center,end,points,m, (m+t1)/2.0f, t1);
 			}
 		}
 
 	public QuadraticBezierPathInterpolator(Vector2D... positions) {
 		//List<Vector2D> points = new ArrayList<Vector2D>();
 		if (positions == null || positions.length == 0) return;
 		points.add(positions[0]);
 		for (int index = 0; index + 2 < positions.length; index += 2) {
 			intermediate(
 					positions[index],
 					positions[index + 1],
 					positions[index + 2],
 					points
 					);
 			points.add(positions[index + 2]);
 			}
 		if (positions.length % 2 == 0) {
 			points.add(positions[positions.length - 1]);
 			}
		for (int i = 1; i < points.size(); i++) {
			addStep(points.get(i - 1), points.get(i));
 			}
 		}
 	
 	public List<Vector2D> getIntermediatePoints() {
 		return points;
 		}
 
 	}
