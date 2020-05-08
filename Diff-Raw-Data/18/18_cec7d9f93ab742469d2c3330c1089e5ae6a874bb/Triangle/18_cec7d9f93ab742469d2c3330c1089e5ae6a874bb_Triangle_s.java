 package ex3.render.raytrace;
 
 import math.Point3D;
 import math.Ray;
 import math.Vec;
 
 public class Triangle extends Object3D {
 
 	private Point3D p0 = null;
 	private Point3D p1 = null;
 	private Point3D p2 = null;
 


 	public Triangle (Point3D[] tri, Material material) {
 		p0 = tri[0];
 		p1 = tri[1];
 		p2 = tri[2];
 		this.material = material;
 	}
 
 
 	@Override
 	public Vec normalAt(Point3D intersection, Ray ray) {
 
		Vec normal = new Vec(Vec.crossProd(new Vec(p1, p0), new Vec(p2, p0)));
 		normal.normalize();
 
 		if (Vec.dotProd(normal, ray.direction) > 0) {
 			normal.negate();
 		}
 		return normal;
 	}
 
 	public Point3D planeIntersection(Ray ray) {
 		Vec v1 = new Vec(p1, p0);
 		Vec v2 = new Vec(p2, p0);
 		Point3D intersectionPoint;
 
		Vec planeNormal = Vec.crossProd(v1, v2);
 		Point3D planePoint = p0;
 
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
 
 	public boolean intersectTri(Point3D intersectionPoint, Ray ray){
 		Vec v1 = new Vec(p0, ray.origin);
 		Vec v2 = new Vec(p1, ray.origin);
 		Vec v3 = new Vec(p2, ray.origin);
 
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
 
 	@Override
 	public Hit nearestIntersection(Ray ray) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	public Point3D getP0() {
 		return p0;
 	}
 	
 	public Point3D getP1() {
 		return p1;
 	} 
 	
 	public Point3D getP2() {
 		return p2;
 	}
 
 }
