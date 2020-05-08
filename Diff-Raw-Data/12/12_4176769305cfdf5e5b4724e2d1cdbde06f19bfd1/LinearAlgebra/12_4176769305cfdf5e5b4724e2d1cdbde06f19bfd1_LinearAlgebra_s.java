 package RayTracing;
 
 public class LinearAlgebra {
 	
 	//we get a plane that is perpendicular to "perpen" and that contains "point"
 	//u and v are two orthogonal vectors on the plane
 	public static void getPerpendicularPlane(Vector v, Vector u, Vector point, Vector perpen){
 		//if perpen is (nx, ny, nz) and point is (x0, y0, z0)
 		//then the plane is nx(x-x0) + ny(y-y0) + nz(z-z0) = 0
 		Vector mid1=new Vector();
 		Vector mid2=new Vector();
 		getTwoIndependentVectorsOnPlane(mid1, mid2, point, perpen);
 		gramSchmidt(v, u, mid1, mid2);
 	}
 
 	private static void getTwoIndependentVectorsOnPlane(Vector result1, Vector result2, Vector point, Vector perpen){
 		double x1=1; double y1=2;
 		double x2=2; double y2=1;
 		//(x1, y1) and (x2, y2) are independent, so surely (x1,y1,z1) and (x2,y2,z2) are independent for each z1 and z2
 		double z1=getZOfPointInPlane(point, perpen, x1, y1);
 		double z2=getZOfPointInPlane(point, perpen, x2, y2);
 		result1.x=x1; result1.y=y1; result1.z=z1;
 		result2.x=x2; result2.y=y2; result2.z=z2;
 	}
 	
 	private static double getZOfPointInPlane(Vector point, Vector perpen, double x, double y){
 		//nx(x-x0) + ny(y-y0) + nz(z-z0) = 0
 		double nx=perpen.x; double ny=perpen.y; double nz=perpen.z;
 		double x0=point.x; double y0=point.y; double z0=point.z;
		return (nx*(x-x0)+ny*(y-y0))/nz + z0;
 	}
 	
 	private static void gramSchmidt(Vector result1, Vector result2, Vector b1, Vector b2) {
 		//based on http://en.wikipedia.org/wiki/GramSchmidt process
 		Vector v1=new Vector(b1);
 		Vector v2=new Vector(b2);
 		v1=v1.normalize();
 		v2=v2.sub(proj(v1,v2));
 		v2=v2.normalize();
 		result1.setVector(v1);
 		result2.setVector(v2);
 	}
 	
 	private static Vector proj(Vector u, Vector v){
 		return u.mul(u.dot(v)/u.dot(u));
 	}
 
 }
