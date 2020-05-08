 package ex3.render.raytrace;
 
 import math.Point3D;
 import math.Ray;
 import math.Vec;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class TriMesh extends Object3D {
 	List<Point3D[]> mesh;
 
 	@Override
 	public double nearestIntersection(Ray ray) {
		double shortestLength = Double.POSITIVE_INFINITY;
 
 		for (Point3D[] tri : mesh) {
 
			Point3D intersectionPoint = planeIntersection(ray, tri);
 
 			if (intersectionPoint != null) {
 				double length = new Vec(intersectionPoint, ray.origin).length();
 				length = length / ray.direction.length();
 
 				if (length < shortestLength && intersectTri(tri, intersectionPoint, ray)) {			 
 					shortestLength = length;
 				}
 
 			}
 		}
 		return shortestLength;
 	}
 	
 
 	@Override
 	public Vec normalAt(Point3D intersection, Ray ray) {
 		return null; 
 	}
 
 	public void init(Map<String, String> attributes) {
 		mesh = new ArrayList<Point3D[]>();
 
 		for (String attribute : attributes.keySet()) {
 			if (attribute.startsWith("tri")) {
 				String[] triangleValues = attributes.get(attribute).split("\\s+");
 				if (triangleValues.length != 9) {
 					throw new IllegalArgumentException("Triangle must have 9 values representing 3 3d points!");
 				}
 				Point3D[] tri = new Point3D[3];
 				tri[0] = new Point3D(
 						Double.parseDouble(triangleValues[0]),
 						Double.parseDouble(triangleValues[1]),
 						Double.parseDouble(triangleValues[2]));
 				tri[1] = new Point3D(
 						Double.parseDouble(triangleValues[3]),
 						Double.parseDouble(triangleValues[4]),
 						Double.parseDouble(triangleValues[5]));
 				tri[2] = new Point3D(
 						Double.parseDouble(triangleValues[6]),
 						Double.parseDouble(triangleValues[7]),
 						Double.parseDouble(triangleValues[8]));
 
 				mesh.add(tri);
 			}
 		}
 	}
 
	public Point3D planeIntersection(Ray ray, Point3D[] tri) {
 		Vec v1 = new Vec(tri[1], tri[0]);
 		Vec v2 = new Vec(tri[2], tri[0]);
 		Point3D intersectionPoint;
 
 		Vec planeNormal =  Vec.crossProd(v1, v2);
 		Point3D planePoint = tri[0];
 
 		// check if ray direction is parallel to plane 
 		double RayNormalDot = Vec.dotProd(ray.direction, planeNormal);
		if (RayNormalDot <= 0) {
 			return null;
 		}
 
 		Vec rayOriginToPlanePoint = new Vec(planePoint, ray.origin);
 		double normalDotRoToPp = Vec.dotProd(rayOriginToPlanePoint, planeNormal);
 		double distanceScalar = normalDotRoToPp / RayNormalDot;
 
 		intersectionPoint = new Point3D(ray.origin);
 		intersectionPoint.mac(distanceScalar, ray.direction);
 
 		return intersectionPoint;
 
 
 
 	}
 
 	public boolean intersectTri(Point3D[] tri, Point3D intersectionPoint, Ray ray){
 		Vec v1 = new Vec(tri[0], ray.origin);
 		Vec v2 = new Vec(tri[1], ray.origin);
 		Vec v3 = new Vec(tri[2], ray.origin);
 
 		Vec normal1 = Vec.crossProd(v1, v2);
 		Vec normal2 = Vec.crossProd(v2, v3);
 		Vec normal3 = Vec.crossProd(v3, v1);
 
		if (normal1.length() > 0 && normal2.length() > 0 && normal3.length() > 0) {
 			normal1.normalize();
 			normal2.normalize();
 			normal3.normalize();
 		} else {
 			return false;
 		}
 
 		Vec rayOriginToIntersection = new Vec(intersectionPoint, ray.origin);
 
		if (Vec.dotProd(rayOriginToIntersection, normal1) < 0) {
 			return false;
 		}
 
		if (Vec.dotProd(rayOriginToIntersection, normal2) < 0) {
 			return false;
 		}
 
		if (Vec.dotProd(rayOriginToIntersection, normal3) < 0) {
 			return false;
 		}
 
 		return true;
 	}
 
 
 }
