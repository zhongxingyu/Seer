 package tesseract.objects;
 import java.awt.Color;
 
 import javax.media.j3d.*;
 import javax.vecmath.*;
 
 import common.CollidableObject;
 
 
 /**
  * Sphere.
  * @author ?
  *
  */
 public class Sphere extends PhysicalObject {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8407634448268590714L;
 
 	/**
 	 * Default Object color.
 	 */
 	private static final Color3f DEFAULT_COLOR = new Color3f(0.7f, 0.7f, 1);
 	
 	/**
 	 * Default divisions.
 	 */
 	private static final int DEFAULT_DIVISIONS = 22;
 	
 	/**
 	 * User definable color.
 	 */
 	private final Color3f myColor;
 	
 	/**
 	 * radius of sphere.
 	 */
 	public float radius;
 	
 	/**
 	 * Constructor.
 	 * @param theRadius of sphere.
 	 * @param position to start.
 	 */
 	public Sphere(final float theRadius, final Vector3f position) {
 		this(1, theRadius, position, DEFAULT_COLOR.get());
 	}
 	
 	/**
 	 * constructor.
 	 * @param mass of object.
 	 * @param theRadius of sphere.
 	 * @param position of sphere.
 	 * @param theColor of sphere.
 	 */
 	public Sphere(final float mass, final float theRadius,
 			final Vector3f position, final Color theColor) {
 		super(position, mass);
 		myColor = new Color3f(theColor);
		setShape(createShape(DEFAULT_DIVISIONS));
 		radius = theRadius;
 		
 		if (inverseMass != 0) {
 			inverseInertiaTensor.m00 = 2f / 5 / inverseMass 
 			* radius * radius;
 			inverseInertiaTensor.m11 = inverseInertiaTensor.m00;
 			inverseInertiaTensor.m22 = inverseInertiaTensor.m00;
 			inverseInertiaTensor.invert();
 		}
 		updateTransformGroup();
 	}
 
 	/**
 	 * createShape.
 	 * @param divisions
 	 * @return node
 	 */
 	protected Node createShape(final int divisions) {
 		Appearance appearance = new Appearance();
 		Material material = new Material();
 		material.setDiffuseColor(myColor);
 		appearance.setMaterial(material);
 		return new com.sun.j3d.utils.geometry.Sphere(radius,
 				com.sun.j3d.utils.geometry.Sphere.GENERATE_NORMALS,
 				divisions, appearance);
 	}
 }
