 package suite.rt;
 
 import java.util.Arrays;
 
 import suite.math.Vector;
 import suite.rt.RayTracer.Material;
 import suite.rt.RayTracer.Ray;
 import suite.rt.RayTracer.RayHit;
 import suite.rt.RayTracer.RayTraceObject;
 
 public class Planar {
 
 	public static class Parallelogram extends PlanarObject implements RayTraceObject {
 		public Parallelogram(Vector origin, Vector axis0, Vector axis1, Material material) {
 			super(origin, axis0, axis1, material);
 		}
 
 		@Override
 		public boolean isHit(float x, float y) {
 			return 0f <= x && x < 1f && 0f <= y && y < 1f;
 		}
 	}
 
 	public static class Triangle extends PlanarObject implements RayTraceObject {
 		public Triangle(Vector origin, Vector axis0, Vector axis1, Material material) {
 			super(origin, axis0, axis1, material);
 		}
 
 		public static RayTraceObject c(Vector origin, Vector axis0, Vector axis1, Material material) {
 			Vector v0 = Vector.add(origin, axis0);
 			Vector v1 = Vector.add(origin, axis1);
 			Triangle triangle = new Triangle(origin, axis0, axis1, material);
			return BoundingBox.bound(Arrays.asList(v0, v1), triangle);
 		}
 
 		@Override
 		public boolean isHit(float x, float y) {
 			return 0f <= x && 0f <= y && x + y < 1f;
 		}
 	}
 
 	public static abstract class PlanarObject implements RayTraceObject {
 		private Vector origin;
 		private Vector axis0, axis1;
 		private Plane plane;
 
 		public PlanarObject(Vector origin, Vector axis0, Vector axis1, Material material) {
 			this.origin = origin;
 			this.axis0 = axis0;
 			this.axis1 = axis1;
 
 			Vector normal = Vector.cross(axis0, axis1);
 			plane = new Plane(normal, Vector.dot(origin, normal), material);
 		}
 
 		@Override
 		public RayHit hit(final Ray ray) {
 			RayHit rayHit;
 			RayHit rayHit0 = plane.hit(ray);
 
 			if (rayHit0 != null) {
 				Vector planarDir = Vector.sub(rayHit0.detail().hitPoint(), origin);
 				float x = Vector.dot(planarDir, axis0) / Vector.normsq(axis0);
 				float y = Vector.dot(planarDir, axis1) / Vector.normsq(axis1);
 				rayHit = isHit(x, y) ? rayHit0 : null;
 			} else
 				rayHit = null;
 
 			return rayHit;
 		}
 
 		public abstract boolean isHit(float x, float y);
 	}
 
 }
