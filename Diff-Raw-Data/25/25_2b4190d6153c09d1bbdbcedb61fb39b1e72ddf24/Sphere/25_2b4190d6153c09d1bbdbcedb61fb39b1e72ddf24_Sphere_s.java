 class Sphere {
 	final double radius;
 	final Vector center;
 	final Vector emission;
 	final Vector color;
 	final Material material;
 
 	Sphere(final double rad_, final Vector p_, final Vector e_, final Vector c_, final Material refl_) {
 		radius = rad_;
 		center = p_;
 		emission = e_;
 		color = c_;
 		material = refl_;
 	}
 
 	IntersectionResult intersect(final Ray ray) {
 		final Vector v = center.minus(ray.origin);
 		final double b = v.dot(ray.direction);
 		final double discriminant = b * b - v.dot(v) + radius * radius;
 		if (discriminant < 0) {
			return new IntersectionResult(ray, 0, this);
 		}
 		final double d = Math.sqrt(discriminant);
 		final double tfar = b + d;
 		final double eps = 1e-4;
 		if (tfar <= eps) {
			return new IntersectionResult(ray, 0, this);
 		}
 		final double tnear = b - d;
 		if (tnear <= eps) {
 			return new IntersectionResult(ray, tfar, this);
 		}
 		return new IntersectionResult(ray, tnear, this);
 	}
 
 	Vector getNormal(final Vector intersectionPoint) {
 		return intersectionPoint.minus(center).norm();
 	}
 };
