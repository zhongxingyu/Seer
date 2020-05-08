 /*
  * Tyson Nottingham
  * Matthew Swartzendruber
  * 5/23/2013
  * Homework 3: Rigid Body Physics
  */
 
 import javax.vecmath.*;
 
 public class CollisionHandler {
	private static final float COEFFICIENT_OF_RESTITUTION = .2f;
	private static final float COEFFICIENT_OF_FRICTION = .5f;
 	
 	public static void checkAndResolveCollision(PhysicsObject a, PhysicsObject b) {
 		CollisionInfo ci = getCollisionInfo(a, b);
 		if (ci == null)
 			return;
 		
 		// Vector from the center of mass of object a to the collision point
 		Vector2f r_ap = new Vector2f();
 		r_ap.scaleAdd(-1, a.getGlobalCenterOfMass(), ci.position);
 		// Vector from the center of mass of object b to the collision point
 		Vector2f r_bp = new Vector2f();
 		r_bp.scaleAdd(-1, b.getGlobalCenterOfMass(), ci.position);
 		// Velocity of object a at the point of collision
 		Vector2f v_ap1 = new Vector2f();
 		v_ap1.x = a.velocity.x - a.angularVelocity * r_ap.y;
 		v_ap1.y = a.velocity.y + a.angularVelocity * r_ap.x;
 		// Velocity of object b at the point of collision
 		Vector2f v_bp1 = new Vector2f();
 		v_bp1.x = b.velocity.x - b.angularVelocity * r_bp.y;
 		v_bp1.y = b.velocity.y + b.angularVelocity * r_bp.x;
 		// The collision impulse
 		Vector2f v_ab1 = new Vector2f();
 		v_ab1.scaleAdd(-1, v_bp1, v_ap1);
 		float tmpA = r_ap.x * ci.normal.y - r_ap.y * ci.normal.x;
 		float tmpB = r_bp.x * ci.normal.y - r_bp.y * ci.normal.x;
 		float j = -(1 + COEFFICIENT_OF_RESTITUTION) * v_ab1.dot(ci.normal) / (1 / a.mass + 1 / b.mass + tmpA * tmpA / a.momentOfInertia + tmpB * tmpB / b.momentOfInertia);
 		// Update object a's velocity
 		a.velocity.scaleAdd(j / a.mass, ci.normal, a.velocity);
 		// Update object b's velocity
 		b.velocity.scaleAdd(-j / b.mass, ci.normal, b.velocity);
 		// Update object a's angular velocity
 		a.angularVelocity += j * (r_ap.x * ci.normal.y - r_ap.y * ci.normal.x) / a.momentOfInertia;
 		// Update object b's angular velocity
 		b.angularVelocity -= j * (r_bp.x * ci.normal.y - r_bp.y * ci.normal.x) / b.momentOfInertia;
 		// Remove object overlap
 		a.position.scaleAdd(-ci.depth / (a.mass * (1 / a.mass + 1 / b.mass)), ci.normal, a.position);
 		b.position.scaleAdd(ci.depth / (b.mass * (1 / a.mass + 1 / b.mass)), ci.normal, b.position);
 		
 		// Start friction code
 		final Vector2f tangent = new Vector2f(v_ab1);
 		final Vector2f normalTemp = new Vector2f(ci.normal);
 		normalTemp.scale(v_ab1.dot(ci.normal));
 		tangent.scaleAdd(-1, normalTemp);
 		tangent.normalize();
 		if (!Float.isNaN(tangent.x))
 		{
 			tmpA = r_ap.x * tangent.y - r_ap.y * tangent.x;
 			tmpB = r_bp.x * tangent.y - r_bp.y * tangent.x;
 			float jt = (1 + COEFFICIENT_OF_RESTITUTION) * v_ab1.dot(tangent) / (1 / a.mass + 1 / b.mass + tmpA * tmpA / a.momentOfInertia + tmpB * tmpB / b.momentOfInertia);
 			Vector2f frictionJ = new Vector2f(tangent);
 			if (Math.abs(jt) < j * COEFFICIENT_OF_FRICTION)
 				frictionJ.scale(jt);
 			else
 				frictionJ.scale(j * COEFFICIENT_OF_FRICTION);
 			a.velocity.scaleAdd(-1 / a.mass, frictionJ, a.velocity);
 			b.velocity.scaleAdd(1 / b.mass, frictionJ, b.velocity);
			a.angularVelocity -= (jt * (r_ap.x * tangent.y - r_ap.y * tangent.x) / a.momentOfInertia) * COEFFICIENT_OF_FRICTION;
			b.angularVelocity += (jt * (r_bp.x * tangent.y - r_bp.y * tangent.x) / b.momentOfInertia) * COEFFICIENT_OF_FRICTION;
 		}
 		// End friction code
 		
 		a.clearCaches();
 		b.clearCaches();
 	}
 	
 	private static CollisionInfo getCollisionInfo(PhysicsObject a, PhysicsObject b) {
 		if (a == b)
 			return null;
 		
 		CollisionInfo ci = null;
 		
 		if (a instanceof HalfSpace) {
 			if (b instanceof Circle)
 				ci = getCollision((HalfSpace)a, (Circle)b);
 			else if (b instanceof ConvexPolygon)
 		        ci = getCollision((HalfSpace) a, (ConvexPolygon) b);
 		} else if (a instanceof Circle) {
 			if (b instanceof Circle)
 				ci = getCollision((Circle)a, (Circle)b);
 			else if (b instanceof ConvexPolygon)
 			    ci = getCollision((Circle) a, (ConvexPolygon) b);
 			else if (b instanceof HalfSpace)
 			    ci = getCollision((HalfSpace) b, (Circle) a);
 		} else if (a instanceof ConvexPolygon) {
 		    if (b instanceof ConvexPolygon)
 		        ci = getCollision((ConvexPolygon) a, (ConvexPolygon) b);
 		    else if (b instanceof Circle)
 		        ci = getCollision((Circle) b, (ConvexPolygon) a);
 			else if (b instanceof HalfSpace)
 			    ci = getCollision((HalfSpace) b, (ConvexPolygon) a);
 		}
 		return ci;
 	}
 
 	private static CollisionInfo getCollision(HalfSpace a, Circle b) {
 		float distance = a.normal.dot(b.position) - a.intercept - b.radius;
 		if (distance < 0) {
 			CollisionInfo ci = new CollisionInfo();
 			ci.normal = a.normal;
 			ci.depth = -distance;
 			ci.position = new Vector2f();
 			ci.position.scaleAdd(-(b.radius - ci.depth), ci.normal, b.position);
 			return ci;
 		}
 		return null;
 	}
 	
 	private static CollisionInfo getCollision(HalfSpace a, ConvexPolygon b) {
         Vector2f[] vertices = b.getVertices();
         
         Vector2f minVertex = vertices[0];
         float minDistance = a.normal.dot(minVertex) - a.intercept;
         for (int i = 0; i < vertices.length; i++) {
             final float distance = a.normal.dot(vertices[i]) - a.intercept;
             if (distance < minDistance) {
                 minDistance = distance;
                 minVertex = vertices[i];
             }
         }
         
         if (minDistance >= 0)
             return null;
         
         CollisionInfo ci = new CollisionInfo();
         ci.depth = -minDistance;
         ci.normal = a.normal;
         ci.position = new Vector2f(minVertex);
         ci.position.scaleAdd(ci.depth, ci.normal, ci.position);
         return ci;
     }
 
     private static CollisionInfo getCollision(Circle a, Circle b) {
 		Vector2f n = new Vector2f();
 		n.scaleAdd(-1, a.position, b.position);
 		float distance = n.length() - a.radius - b.radius;
 		if (distance < 0) {
 			CollisionInfo ci = new CollisionInfo();
 			n.normalize();
 			ci.normal = n;
 			ci.depth = -distance;
 			// Approximates collision point as center of overlapping region.
 			ci.position = new Vector2f();
 			ci.position.scaleAdd(a.radius - ci.depth / 2, ci.normal, a.position);
 			return ci;
 		}
 		return null;
 	}
     
     private static CollisionInfo getCollision(Circle c, ConvexPolygon t)
     {
         Vector2f[] verticesA = t.getVertices();
         Vector2f[] normalsA = t.getNormals();
         
         Vector2f[] normalsB = new Vector2f[normalsA.length];
         for (int i = 0; i < normalsB.length; i++)
         {
             Vector2f n = new Vector2f();
             n.scaleAdd(-1, verticesA[i], c.position);
             n.normalize();
             normalsB[i] = n;
         }
         float[] distanceFromA = new float[verticesA.length];
         float[][] distanceFromB = new float[verticesA.length][normalsB.length];
         int indexMinDistanceFromA = 0;
         int[] indexMinDistanceFromB = new int[verticesA.length];
         
         
         for (int i = 0; i < verticesA.length; i++) {
             Vector2f tmp = new Vector2f();
             tmp.scaleAdd(-1, verticesA[i], c.position);
             distanceFromA[i] = tmp.dot(normalsA[i]) - c.radius;
             if (distanceFromA[i] < distanceFromA[indexMinDistanceFromA])
                 indexMinDistanceFromA = i;
             if (distanceFromA[i] >= 0)
                 return null;
         }
 
         for (int i = 0; i < verticesA.length; i++) {
             for (int j = 0; j < verticesA.length; j++) {
                 Vector2f tmp = new Vector2f(verticesA[j]);
                 tmp.scaleAdd(-1, c.position, verticesA[j]);
                 distanceFromB[i][j] = tmp.dot(normalsB[i]) - c.radius;
                 if (distanceFromB[i][j] < distanceFromB[i][indexMinDistanceFromB[i]])
                     indexMinDistanceFromB[i] = j;
             }
             if (distanceFromB[i][indexMinDistanceFromB[i]] >= 0)
                 return null;
         }
         
         int indexMaxDistanceFromA = 0;
         for (int i = 1; i < verticesA.length; i++)
             if (distanceFromA[i] > distanceFromA[indexMaxDistanceFromA])
                 indexMaxDistanceFromA = i;
         int indexMaxDistanceFromB = 0;
         for (int i = 1; i < verticesA.length; i++)
             if (distanceFromB[i][indexMinDistanceFromB[i]] > distanceFromB[indexMaxDistanceFromB][indexMinDistanceFromB[indexMaxDistanceFromB]])
                 indexMaxDistanceFromB = i;
         
         CollisionInfo ci = new CollisionInfo();
         if (distanceFromA[indexMaxDistanceFromA] > distanceFromB[indexMaxDistanceFromB][indexMinDistanceFromB[indexMaxDistanceFromB]]) {
             ci.depth = -distanceFromA[indexMaxDistanceFromA];
             ci.normal = new Vector2f(normalsA[indexMaxDistanceFromA]);
             ci.position = new Vector2f(c.position);
             ci.position.scaleAdd(-ci.depth, ci.normal, ci.position);
         } else {
             ci.depth = -distanceFromB[indexMaxDistanceFromB][indexMinDistanceFromB[indexMaxDistanceFromB]];
             ci.normal = new Vector2f(normalsB[indexMaxDistanceFromB]);
             ci.normal.scale(-1);
             ci.position = new Vector2f(verticesA[indexMinDistanceFromB[indexMaxDistanceFromB]]);
             ci.position.scaleAdd(ci.depth, ci.normal, ci.position);
         }
         return ci;
     }
     
     private static CollisionInfo getCollision(ConvexPolygon a, ConvexPolygon b) {
         // Use Separating Axis Theorem to determine collision information.
         // See http://www.codezealot.org/archives/55
         
         // In short, if two convex polygons are not colliding, then there is
         // some axis onto which both polygons can be projected where their
         // projections do not overlap. Define the projection of the polygon by
         // projecting each vertex position onto the axis, then taking the line
         // segment between the minimum magnitude projected position and maximum
         // magnitude projected position. The axes to test are the normals of the
         // edges of the polygons.
         
         // Projection overlap on axis where overlap between polygons is minimal.
         // That axis will be the collision normal.
         float minOverlap = Float.POSITIVE_INFINITY;
         Vector2f minOverlapNormal = null;
         Vector2f minOverlapVertex = null;
         boolean usingBNormal = false;
         
         for (final Vector2f axis : a.getNormals()) {
             final Projection ap = new Projection(a, axis);
             final Projection bp = new Projection(b, axis);
         
             if (!ap.isOverlap(bp)) {
                 // The polygons are separated in this axis.
                 return null;
             } else {
                 // Find amount of overlap in this axis. Calculation
                 // overestimates for some overlaps, but I believe the overlaps
                 // which it overestimates will never actually be on the
                 // minimum overlapping axis. Conjecture is not proved though. 
                 final float overlap = ap.max - bp.min;
                 if (overlap < minOverlap) {
                     minOverlap = overlap;
                     minOverlapNormal = axis;
                     minOverlapVertex = bp.minVertex;
                 }
             }
         }
        
         // Do the same for the normals of edges of B.
         for (final Vector2f axis : b.getNormals()) {
             final Projection ap = new Projection(a, axis);
             final Projection bp = new Projection(b, axis);
         
             if (!ap.isOverlap(bp)) {
                 return null;
             } else {
                 final float overlap = bp.max - ap.min;
                 if (overlap < minOverlap) {
                     minOverlap = overlap;
                     minOverlapNormal = axis;
                     minOverlapVertex = ap.minVertex;
                     usingBNormal = true;
                 }
             }
         }
         
         final CollisionInfo ci = new CollisionInfo();
         ci.depth = minOverlap;
         ci.normal = new Vector2f(minOverlapNormal);
         
         // Approximate collision position as being halfway between vertex most
         // responsible for collision and the edge that the vertex is
         // penetrating. This can be very inaccurate in perfect edge-edge
         // collisions.
         ci.position = new Vector2f();
         ci.position.scaleAdd(ci.depth / 2, ci.normal, minOverlapVertex);
         
         if (usingBNormal) {
             ci.normal.negate();
         }
         
         return ci;
     }
 
     private static class Projection {
         public float min;
         public float max;
         public Vector2f minVertex;
         @SuppressWarnings("unused")
 		public Vector2f maxVertex;
         
         public Projection(final ConvexPolygon p, final Vector2f axis) {
             final Vector2f[] vertices = p.getVertices();
             minVertex = maxVertex = vertices[0];
             min = max = vertices[0].dot(axis);
             
             for (int i = 1; i < vertices.length; i++) {
                 final float vProj = vertices[i].dot(axis);
                 if (vProj < min) {
                     min = vProj;
                     minVertex = vertices[i];
                 } else if (vProj > max) {
                     max = vProj;
                     maxVertex = vertices[i];
                 }
             }
         }
         
         public boolean isOverlap(final Projection other) {
            return min < other.max && max > other.min;
         }
     }
 }
