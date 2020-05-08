 package RayTracing;
 
 public class Ray {
 	public Vector origin;
 	public Vector direction;
 	//let P0=origin and V=direction, then ray is P=P0+tV
 	
 	public Ray(){
 		
 	}
 	
 	public Ray(Vector origin, Vector direction){
 		this.origin=origin;
 		this.direction=direction;
 	}
 	
 	public static Ray getRay(Vector startPoint, Vector toPoint){
 		Ray ray=new Ray();
 		Vector direction=toPoint.sub(startPoint).normalize();
 		ray.setDirection(direction);
 		ray.setOrigin(startPoint);
 		return ray;
 	}
 	
 	public Vector get_t_on_ray(double t){
 		return origin.add(direction.mul(t));
 	}
 	
 	public static Ray getReflectedRay(Vector normal, Vector hittingRay, Vector hittingPoint){
 		Vector L=hittingRay;
 		Vector N=normal;
		Vector direction=N.mul(2*L.dot(N)).sub(L);
 		Ray r=new Ray();
 		r.setDirection(direction);
 		r.setOrigin(hittingPoint);
 		return r;
 	}
 	
 	public void setOrigin(Vector origin){
 		this.origin=origin;
 	}
 	
 	public void setDirection(Vector direction){
 		this.direction=direction;
 	}
 	
 	public Vector P0(){
 		return origin;
 	}
 	
 	public Vector V(){
 		return direction;
 	}
 }
