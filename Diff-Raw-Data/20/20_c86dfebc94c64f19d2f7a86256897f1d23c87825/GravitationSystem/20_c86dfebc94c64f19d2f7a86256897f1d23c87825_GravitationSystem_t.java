 package se.exuvo.planets.systems;
 
 import se.exuvo.planets.components.Acceleration;
 import se.exuvo.planets.components.Mass;
 import se.exuvo.planets.components.Position;
 import se.exuvo.settings.Settings;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.annotations.Mapper;
 import com.artemis.systems.IntervalEntitySystem;
 import com.artemis.utils.FastMath;
 import com.artemis.utils.ImmutableBag;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 
 public class GravitationSystem extends IntervalEntitySystem {
 	@Mapper ComponentMapper<Mass> mm;
 	@Mapper ComponentMapper<Acceleration> am;
 	@Mapper ComponentMapper<Position> pm;
 	
 	// TODO make somehow settable by user?
 	// TODO calculate this realtive to fps.
 	private float G = 1E16f;//(float) (6.6726 * Math.pow(10.0,-11.0));
 
 	public GravitationSystem() {
 		super(Aspect.getAspectForAll(Mass.class, Acceleration.class, Position.class), Settings.getFloat("PhysicsStep"));
 	}
 
 	@Override
 	protected void processEntities(ImmutableBag<Entity> entities) {
 		
 		// TODO do we need an acceleration-system if we do clear here? we could update the velocity directly. unless the user wants to see the acceleration...
 		// clear old accelerations
 		for (int i = 0; i < entities.size(); i++) {
 			Entity e = entities.get(i);
 			Acceleration a = am.get(e);
 			a.vec.set(Vector2.Zero); // reset
 		}
 		
		// calculate gravity effects on gravity.
 		for (int i = 0; i < entities.size(); i++) {
 			Entity e = entities.get(i);
 			Acceleration a = am.get(e);
 			
 			Mass m = mm.get(e);
 			Position p = pm.get(e);
 			
 			for (int j = i+1; j < entities.size(); j++) {
 				Entity e2 = entities.get(j);
 				Acceleration a2 = am.get(e);
 				
 				Mass m2 = mm.get(e2);
 				Position p2 = pm.get(e2);
 				
 				// F = G*m*M/d^2
 				// F = ma
 				// a = G*M/d^2 = M * G/d^2
 				// where:
 				// m = e.mass
 				// M = e2.mass
 				// G = grav. constant
 				// d = distance between e and e2
 				
 				
 				
 				// vector from p to p2
 				Vector2 posDiff = p2.vec.cpy().sub(p.vec);
 				
 				// distance from p to p2
 				float distanceSquared = posDiff.len2(); // distance squared
 				
 				// angle from p to p2
 				float angle = MathUtils.atan2(posDiff.y, posDiff.x); // should be fast.
 				
 				// G/d^2
 				float k = G / distanceSquared;
				
 				// the accelerations are created as:
 				//
 				// <-----.-----> (0 angle)
 				//
 				// and then rotated counter-clockwise.
 				
 				// acceleration for p towards p2
				Vector2 v = new Vector2(m2.mass * k, 0);
 				rotate(v, angle);
 				a.vec.add(v);
 				
 				// acceleration for p2 towards p
				Vector2 v2 = new Vector2(m.mass * k, 0);
 				rotate(v2, angle);
 				a2.vec.add(v2);
 				
 				//System.out.println("d^2: " + distanceSquared); 
 				//System.out.println("G: " + G); 
 				//System.out.println("k: " + k); 
 				//System.out.println("degrees: " + angle); 
 				//System.out.println("v: " + v.x +" "+ v.y);
 				//System.out.println("v2: " + v2.x +" "+ v2.y);
 			}
 		}
 
 		// TODO use FastMath TrigLUT Utils from artemis or MathUtils from libgdx?
 	}
 	
	// TODO it's possible there's an lib-implementation of this (NOT Vector2.rotate() -- it uses java.lang.Math)
 	/**
	 * Rotates the given {@link Vector2} {@code angle} number of radians counter-clockwise, using  {@link FastMath}.
 	 */
 	private static void rotate(Vector2 v, float angle) {
 		float cos = (float)FastMath.cos(angle);
 		float sin = (float)FastMath.sin(angle);
 
 		v.x = v.x * cos - v.y * sin;
 		v.y = v.x * sin + v.y * cos;
 	}
 }
