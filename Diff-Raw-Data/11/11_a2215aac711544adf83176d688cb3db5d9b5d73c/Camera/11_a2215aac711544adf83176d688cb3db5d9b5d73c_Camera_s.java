 package ray;
 
 import ray.math.Point3;
 import ray.math.Vector3;
 
 /**
  * Represents a simple camera.
  * 
  * modified by DS 2/2012
  */
 public class Camera {
 	
 	/*
 	 * Fields that are read in from the input file to describe the camera.
 	 * You'll probably want to store some derived values to make ray generation easy.
 	 */
 	
 	protected final Point3 viewPoint = new Point3();
 	public void setViewPoint(Point3 viewPoint) { this.viewPoint.set(viewPoint); }
 	
 	protected final Vector3 viewDir = new Vector3(0, 0, -1);
 	public void setViewDir(Vector3 viewDir) { this.viewDir.set(viewDir); }
 	
 	protected final Vector3 viewUp = new Vector3(0, 1, 0);
 	public void setViewUp(Vector3 viewUp) { this.viewUp.set(viewUp); }
 	
 	protected final Vector3 projNormal = new Vector3(0, 0, 1);
 	public void setProjNormal(Vector3 projNormal) { this.projNormal.set(projNormal); }
 	
 	protected double viewWidth = 1.0;
 	public void setViewWidth(double viewWidth) { this.viewWidth = viewWidth; }
 	
 	protected double viewHeight = 1.0;
 	public void setViewHeight(double viewHeight) { this.viewHeight = viewHeight; }
 	
 	protected double projDistance = 1.0;
 	public void setprojDistance(double projDistance) { this.projDistance = projDistance; }
 	
 	/*
 	 * Derived values that are computed before ray generation.
 	 * basisU, basisV, and basisW form an orthonormal basis.
 	 * basisW points in same direction as projNormal.
 	 */
 	protected final Vector3 basisU = new Vector3();
 	protected final Vector3 basisV = new Vector3();
 	protected final Vector3 basisW = new Vector3();
 	protected final Vector3 centerDir = new Vector3();
 	
 	// Has the view been initialized?
 	protected boolean initialized = false;
 	
 	/**
 	 * Initialize the derived view variables to prepare for using the camera.
 	 */
 	public void initView() {
 
 		// set basis vectors according to projection normal and up direction
 		basisW.set(projNormal);
 		basisW.normalize();
 		Vector3 temp = new Vector3();
 		temp.set(basisW);
 		if(Math.abs(temp.x)<Math.abs(temp.y)) {
 			if(Math.abs(temp.x)<Math.abs(temp.z)){
 				temp.x=1;
 			} else {
 				temp.z=1;
 			}
 		} else {
 			if(Math.abs(temp.y)<Math.abs(temp.z)) {
 				temp.y=1;
 			} else {
 				temp.z=1;
 			}
 		}
 
		basisU.cross(temp,basisW);
		basisU.scale(1/basisU.getMagnitude());
 		basisV.cross(basisW, basisU); 
 		initialized = true;
 	}
 	
 	/**
 	 * Set outRay to be a ray from the camera through a point in the image.
 	 *
 	 * @param outRay The output ray (not normalized)
 	 * @param inU The u coord of the image point (range [0,1])
 	 * @param inV The v coord of the image point (range [0,1])
 	 */
 	public void getRay(Ray outRay, double inU, double inV) {
 		if (!initialized) initView();
 		Vector3 U = new Vector3();
 		U.set(basisU);
 		Vector3 V = new Vector3();
 		V.set(basisV);
 
 		Vector3 W = new Vector3();
 		W.set(basisW);
 		// Formula Comes from the Computer Graphics Textbook
 		double u = (-viewWidth/2) + (viewWidth)*inU;
 		double v = (-viewHeight/2) + (viewHeight)*inV;
 		
 		W.scale(projDistance*-1);
 		W.scaleAdd(u,U);
 		W.scaleAdd(v,V);
 		
 		outRay.set(viewPoint, W);
 		outRay.start = 0;
 		outRay.end = Double.POSITIVE_INFINITY;
 	}
 }
