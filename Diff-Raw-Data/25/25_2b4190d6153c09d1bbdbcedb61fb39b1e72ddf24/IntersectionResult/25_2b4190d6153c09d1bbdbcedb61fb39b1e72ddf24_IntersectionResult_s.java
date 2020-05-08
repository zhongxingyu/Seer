 class IntersectionResult {
 	private final Ray ray;
 	private final double t;
 	final Sphere object;
 
 	IntersectionResult(final Ray ray, final double t, final Sphere object) {
 		this.ray = ray;
 		this.t = t;
 		this.object = object;
 	}
 
 	Vector getIntersectionPoint() {
 		return ray.getVector(t);
 	}
 
 	Vector getNormal() {
 		return object.getNormal(getIntersectionPoint());
 	}
 
 	public boolean isHit() {
		return t > 0;
 	}
 
 	public boolean closerThan(final IntersectionResult other) {
 		return t < other.t;
 	}
 
 	public boolean isMiss() {
		return t == 0;
 	}
 }
