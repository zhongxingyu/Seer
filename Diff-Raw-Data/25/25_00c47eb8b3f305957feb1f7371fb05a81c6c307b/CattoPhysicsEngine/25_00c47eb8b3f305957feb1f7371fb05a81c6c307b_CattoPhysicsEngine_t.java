 /**
  * From Erin Catto's 2006 GDC Game Engine "Box2D"
  *
  * Copyright (c) 2006 Erin Catto http://www.gphysics.com
  *
  * Permission to use, copy, modify, distribute and sell this software
  * and its documentation for any purpose is hereby granted without fee,
  * provided that the above copyright notice appear in all copies.
  * Erin Catto makes no representations about the suitability 
  * of this software for any purpose.  
  * It is provided "as is" without express or implied warranty.
  *
  * Modified for jig.engine... 
  */
 package physics;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import jig.engine.PhysicsEngine;
 import jig.engine.RenderingContext;
 import jig.engine.physics.BodyLayer;
 import jig.engine.util.Vector2D;
 
 /**
  * A port of Erin Catto's Box2D physics engine presented at the 2006 GDC. The
  * physics engine provides realistic physics for rectangular objects and works
  * by finding collision points between two objects and calculating the impulse
  * required at each point to ensure that the objects do not overlap. The model
  * includes:
  * 
  * <ul>
  * <li>Mass</li>
  * <li>Moment of Inertia</li>
  * <li>Velocity and angular velocity</li>
  * <li>Permanent constraints between objects (joints)</li>
  * <li>Gravity</li>
  * </ul>
  * 
  * Erin Catto's Box2D implementation can be found at:
  * {@link http://www.gphysics.com/box2d/}
  * 
  * @author Erin Catto
  * @author (Ported by) Scott Wallace
  * 
  */
 public class CattoPhysicsEngine implements PhysicsEngine<Box> {
 
 	int iterations = 5;
 
 	/*
 	 * DESIGN: Do we really want to smooth? It seems to make he physics more
 	 * stable If so, we may need an interface for this
 	 */
 	int smoothLength = 5;
 
 	long smoothing;
 
 	long[] s;
 
 	int iSmooth;
 
 	protected ArrayList<Joint> joints;
 
 	/** DESIGN: consider making this private. */
 	protected ArrayList<Arbiter> arbiters;
 
 	private Vector2D gravity;
 
 	protected LinkedList<BodyLayer<? extends Box>> managedLayers;
 
 	boolean drawArbiters = false;
 
 	/**
 	 * Creates a physics engine with the gravitational Vector (0,100).
 	 */
 	public CattoPhysicsEngine() {
 		this(new Vector2D(0, 100));
 	}
 
 	/**
 	 * Creates a physics engine with the specified gravitational vector.
 	 * 
 	 * @param g
 	 *            the magnitude and direction of gravity
 	 */
 	public CattoPhysicsEngine(final Vector2D g) {
 		joints = new ArrayList<Joint>(20);
 		arbiters = new ArrayList<Arbiter>(20);
 		gravity = g;
 
 		smoothing = 1000 / 60 * smoothLength;
 		s = new long[smoothLength];
 		for (int i = 0; i < smoothLength; i++) {
 			s[i] = 1000 / 60;
 		}
 		iSmooth = 0;
 		managedLayers = new LinkedList<BodyLayer<? extends Box>>();
 	}
 
 	/**
 	 * Sets the force of gravity to the direction and magnitude of the specified
 	 * vector.
 	 * 
 	 * @param g
 	 *            a vector representation of the gravitational force
 	 */
 	public void setGravity(final Vector2D g) {
 		gravity = g;
 	}
 
 	/**
 	 * Registers a viewable layer with the physics engine so that all objects in
 	 * that lahyer will be governed by the laws of physics.
 	 * 
 	 * @param v
 	 *            the ViewableLayer to be managed
 	 */
 	public void manageViewableSet(final BodyLayer<? extends Box> v) {
 		managedLayers.add(v);
 
 		int maxCollisions = 0;
 		for (BodyLayer<?> l : managedLayers) {
 			maxCollisions += 2 * l.size();
 		}
 		arbiters.ensureCapacity(Math.min(200, maxCollisions));
 	}
 
 	/**
 	 * Render (or don't render) the arbiters...
 	 * 
 	 * 
 	 * @param p
 	 *            <code>true</code> iff the engine should draw markup for each
 	 *            arbiter
 	 * 
 	 */
 	public void setDrawArbiters(final boolean p) {
 		drawArbiters = p;
 
 		if (drawArbiters) {
 			drawArbiters = Arbiter.loadImages();
 			drawArbiters = Joint.loadImages();
 		}
 	}
 
 	/**
 	 * Joints are not Viewable objects, and so are not members of the managed
 	 * ViewableLayers. Instead, joints must be added using this method.
 	 * 
 	 * @param j
 	 *            the joint constraint
 	 */
 	public void add(final Joint j) {
 		joints.add(j);
 	}
 
 	/**
 	 * Clears the physics engine of all its members.
 	 */
 	public void clear() {
 		managedLayers.clear();
 		joints.clear();
 		arbiters.clear();
 
 	}
 
 	/**
 	 * Steps the physics simulation.
 	 * 
 	 * @param dt
 	 *            the time in seconds since the last iteration of the game loop.
 	 *            Note that the time should be relatively smooth form iteration
 	 *            to iteration. <code>applyLawsOfPhysics</code> will provide
 	 *            some smoothing by default.
 	 * 
 	 * @see #smoothing
 	 * @see #smoothLength
 	 * @see #applyLawsOfPhysics(long)
 	 * 
 	 * DESIGN: do we really want smoothing? If so, we need an iterface
 	 */
 	protected void step(final double dt) {
 		double inverseDeltaTime;
 
 		if (dt > 0.0) {
 			inverseDeltaTime = 1.0 / dt;
 		} else {
 			inverseDeltaTime = 0.0;
 		}
 
 		broadPhase();
 		Vector2D n;
 
 		for (BodyLayer<? extends Box> layer : managedLayers) {
 			for (Box b : layer) {
 				// b = bi.next();
 
 				if (b.inverseMass == 0.0) {
 					continue;
 				}
 				n = b.force.scale(b.inverseMass);
 				n = n.translate(gravity);
 				n = n.scale(dt);
 
 				b.setVelocity(b.getVelocity().translate(n));
 				b.angularVelocity += dt * b.invMomentOfInertia * b.torque;
 
 			}
 		}
 		for (Arbiter a : arbiters) {
 			a.preStep(inverseDeltaTime);
 		}
 
 		for (Joint j : joints) {
 			j.preStep(inverseDeltaTime);
 		}
 
 		for (int i = 0; i < iterations; i++) {
 
 			for (Arbiter a : arbiters) {
 				a.applyImpulse();
 			}
 			for (Joint j : joints) {
 				j.applyImpulse();
 			}
 		}
 		for (BodyLayer<? extends Box> layer : managedLayers) {
 			for (Box b : layer) {
				
 				b.setPosition(b.getPosition().translate(
 						b.getVelocity().scale(dt)));
 
 				b.rotation += dt * b.angularVelocity;
 				b.force = new Vector2D(0.0, 0.0);
 				b.torque = 0.0;
 
 			}
 		}
 	}
 
 	/**
 	 * Performs high-level collision detection to determine which objects are in
 	 * contact. Resolution of the colliding objects is handled by iteratively
 	 * calculating the impulses required to ensure no objects are overlapped.
 	 * This calculation is performed by the <code>Arbiter</code> instances.
 	 * 
 	 * Games which want to add minor tweaks to this physics engine may be able
 	 * to do so simply by overriding this method and providing their own Arbiter
 	 * subclasses using the <code>generateArbiter(Box, Box)</code> method.
 	 * 
 	 * @see #step(double)
 	 * @see #generateArbiter(Box, Box)
 	 */
 	protected void broadPhase() {
 		// NOTE (efficiency): broad phase is O(n^2); could be improved
 		Box bi, bj;
 		int i, j;
 		int nbodies;
 
 		for (BodyLayer<? extends Box> layer : managedLayers) {
 
 			nbodies = layer.size();
 
 			for (i = 0; i < nbodies; i++) {
 				bi = layer.get(i);
 
 				for (j = i + 1; j < nbodies; j++) {
 					bj = layer.get(j);
 
 					if (bi.inverseMass == 0.0 && bj.inverseMass == 0.0) {
 						continue;
 					}
 					generateArbiter(bi, bj);
 				}
 
 			}
 		}
 	}
 
 	/**
 	 * Creates an Arbiter to manage the (potential) collision between two boxes.
 	 * This method should create a temporary arbiter, and use it to check if the
 	 * collision actually occurred.
 	 * 
 	 * Overriding this method may be useful to inject a new <code>Arbiter</code>
 	 * subclass.
 	 * 
 	 * @param bi
 	 *            one of the boxes in the (potential) collision
 	 * @param bj
 	 *            the other box involved in the (potential) collision
 	 */
 	protected void generateArbiter(final Box bi, final Box bj) {
 		Arbiter newArb = new Arbiter(bi, bj);
 
 		if (newArb.numContacts > 0) {
 			int index = arbiters.indexOf(newArb);
 			if (index > -1) {
 				arbiters.get(index).update(newArb);
 			} else {
 				arbiters.add(newArb);
 			}
 		} else {
 			arbiters.remove(newArb);
 		}
 	}
 
 	/**
 	 * Renders the physics markup onto the game's drawing surface.
 	 * 
 	 * @param rc
 	 *            the game frame's rendering context.
 	 */
 	public void renderPhysicsMarkup(final RenderingContext rc) {
 		/*
 		 * Draw Arbiters & Joint hinges if requested
 		 */
 		if (drawArbiters) {
 			for (Arbiter a : arbiters) {
 				a.render(rc);
 			}
 			for (Joint j : joints) {
 				j.render(rc);
 			}
 		}
 	}
 
 	/**
 	 * Applies the laws of physics encapsulated by this engine to the Viewable
 	 * objects in the registered layers.
 	 * 
 	 * @param deltaMs
 	 *            the time (in milliseconds) that have elapsed since the last
 	 *            iteration of the game loop
 	 */
 	public void applyLawsOfPhysics(final long deltaMs) {
 
 		smoothing -= s[iSmooth];
 		s[iSmooth] = deltaMs;
 		smoothing += deltaMs;
 		iSmooth += 1;
 		iSmooth %= smoothLength;
 
 		double t = (double) smoothing / smoothLength / 1000.0;
 		step(t);
 
 	}
 
 }
