 class IntersectionResult {
	public static final IntersectionResult MISS = new IntersectionResult(null, Double.POSITIVE_INFINITY, null);

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
		return this != MISS;
 	}
 
 	public boolean closerThan(final IntersectionResult other) {
 		return t < other.t;
 	}
 
 	public boolean isMiss() {
		return this == MISS;
 	}
 }
