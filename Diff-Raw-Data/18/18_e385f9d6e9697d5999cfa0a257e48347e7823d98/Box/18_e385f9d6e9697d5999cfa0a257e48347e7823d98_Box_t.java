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
 
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.util.List;
 
 import jig.engine.ImageResource;
 import jig.engine.RenderingContext;
 import jig.engine.physics.Body;
 import jig.engine.util.Vector2D;
 
 /**
  * A box. The primary physical object used by the Catto Physics Engine. Boxes
  * have velocity, position, angle of rotation, angular velocity, and moment of
  * inertia.
  * 
  * 
  */
 public class Box extends Body implements Comparable<Box> {
 
 	static int nextBodyId = 0;
 
 	protected Vector2D force;
 	
 	protected double halfHeight;
 	
 	protected double halfWidth;
 
 	protected double rotation;
 
 	protected double angularVelocity;
 
 	protected double torque;
 
 	protected double friction;
 
 	protected double mass, inverseMass;
 
 	protected double momentOfInertia;
 
 	protected double invMomentOfInertia;
 
 	protected int id;
 
 	/**
 	 * Creates a new Box with the specified image resources.
 	 * 
 	 * 
 	 * @param frameset
 	 *            a list of ImageResource objects which together make
 	 *            up all the frames this Box is capable of displaying.
 	 * 
 	 */
 	public Box(final List<ImageResource> frameset) {
 		super(frameset);
 		set(Double.MAX_VALUE, .2, 1.0, 0.0);
 		id = nextBodyId++;
 	}
 	
 	/**
 	 * Creates a new Box for use with the Catto Physics Engine.
 	 * 
 	 * @param rsc
 	 *            the name of the image resource to be used
 	 */
 	public Box(final String rsc) {
 		super(rsc);
 		set(Double.MAX_VALUE, .2, 1.0, 0.0);
 		id = nextBodyId++;
 	}
 
 	/**
 	 * Renders this box.
 	 * 
 	 * @param rc
 	 *            the game frame's rendering context.
 	 */
 	public void render(final RenderingContext rc) {
 		if (!active) {
 			return;
 		}
 
 		// Should be the same as above but actually one less operation
 		AffineTransform at = AffineTransform.getTranslateInstance(position
 				.getX() + halfWidth, position.getY() + halfHeight);
 		at.rotate(rotation);
 		at.translate(-halfWidth, -halfHeight);
 
 		render(rc, at);
 	}
 
 	/**
 	 * Initializes the Box with its 'static' attributes.
 	 * 
 	 * @param m
 	 *            the Box's mass
 	 * @param f
 	 *            the coefficient of friction
 	 * @param restitution
 	 *            the coefficient of restitution
 	 */
 	public void set(final double m, final double f, final double restitution, double rot) {
 		position = new Vector2D(0.0, 0.0);
 		rotation = rot;//0.0;
 		velocity = new Vector2D(0.0, 0.0);
 		angularVelocity = 0.0;
 		force = new Vector2D(0.0, 0.0);
 		torque = 0.0;
 		friction = f;
 
 		halfWidth = getWidth() / 2.0;
 		halfHeight = getHeight() / 2.0;
 
 //		width = new Vector2D(spriteDelegate.getWidth(), spriteDelegate
 //				.getHeight());
 		mass = m;
 
 		if (mass < Double.MAX_VALUE) {
 			inverseMass = 1.0 / mass;
 			momentOfInertia = mass
 					* (halfWidth * halfWidth * 4 
 							+ halfHeight * halfHeight * 4) / 12.0f;
 			invMomentOfInertia = 1.0 / momentOfInertia;
 		} else {
 			inverseMass = 0.0;
 			momentOfInertia = Double.MAX_VALUE;
 			invMomentOfInertia = 0.0;
 		}
 	}
 	
 	public void set(final double m, final double f, final double restitution) {
 		set( m, f, restitution, 0);
 	}
 
 	/**
 	 * Sets the position of the box's upper left corner 
 	 * if rotation were ignored.
 	 * 
 	 * @param p the desired position
 	 */
 	public void setPosition(final Vector2D p) {
 		position = p;
 	}
 	
 	/**
 	 * @return the position of the box's upper left corner, if 
 	 * rotation were ignored.
 	 */
 	public Vector2D getPosition() {
 		return position;
 	}
 	/**
 	 * Sets the position of this box's center.
 	 * 
 	 * @param p
 	 *            the box's new position
 	 */
 	public void setCenterPosition(final Vector2D p) {
 		position = new Vector2D(p.getX() - halfWidth, p.getY() - halfHeight);
 	}
 
 	/**
 	 * @return the position of the box's center.
 	 */
 	public Vector2D getCenterPosition() {
 		return new Vector2D(position.getX() + halfWidth, 
 				position.getY() + halfHeight);
 	}
 
 	/**
 	 * Sets the rotational orientation of this box.
 	 * 
 	 * @param r
 	 *            the angle of rotation in radians
 	 */
 	public void setRotation(final double r) {
 		rotation = r;
 	}
 
 	public double getRotation() {
 		return rotation;
 	}
 
 	/**
 	 * @return the hashcode for this box (just the box's unique id)
 	 */
 	@Override
 	public int hashCode() {
 		return id;
 	}
 
 	/**
 	 * Checks to see if this box is equal to the specified box.
 	 * 
 	 * @param o
 	 *            the other box to compare this one with
 	 * @return <code>true</code> iff this box is equal to the specified box
 	 */
 	@Override
 	public boolean equals(final Object o) {
 		if (o instanceof Box) {
 			return id == ((Box) o).id;
 		}
 		return false;
 	}
 
 	/**
 	 * Compares this box to another by checking their ids.
 	 * 
 	 * @param o
 	 *            a second box to which this box will be compared
 	 * 
 	 * @return -1,0,1 if the specified box is smaller, equal, or larger than
 	 *         this one
 	 * 
 	 */
 	public int compareTo(final Box o) {
 		if (o.id < id) {
 			return -1;
 		}
 		if (o.id > id) {
 			return 1;
 		}
 
 		return 0;
 	}
 
 	public void renderImg(Graphics2D g) {
		
		double xrot = width/2;
		double yrot = height/2;
		double rot = getRotation();
		
		double tx = position.getX()+2175;
		double ty = position.getY()+750;
		
		g.translate(tx, ty);
		g.rotate(rot, xrot, yrot);
		frames.get(0).draw(g, null);
		g.rotate(-rot, xrot, yrot);
		g.translate(-tx, -ty);
		
 	}
 	
 	/**
 	 * This method has no effect for Box instances, and should not be called.
 	 * Boxes must be updated with a CattoPhysicsEngine.
 	 * 
 	 * If called, this method throws an 
 	 * <code>UnsupportedOperationException</code>.
 	 * 
 	 * @param deltaMs ignored
 	 * 
 	 */
 	@Override
 	public void update(final long deltaMs) {
 		//throw new UnsupportedOperationException(
 		//		"Boxes should only be updated via a CattoPhysicsEngine");
 	}
 }
