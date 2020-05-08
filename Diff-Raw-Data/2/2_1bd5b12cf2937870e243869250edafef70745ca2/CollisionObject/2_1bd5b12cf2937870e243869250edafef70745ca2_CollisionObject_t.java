 /********************************************************************************
  *                                                                              *
  *  This file is part of the Tiny Collision Library.                            *
  *                                                                              *
  *  Copyright 2012 Chris Foster                                                 *
  *                                                                              *
  ********************************************************************************/
 
 package collision.core;
 
 import java.io.Serializable;
 
 import math.Matrix;
 import math.Ray;
 import math.Vector;
 
 public class CollisionObject implements Serializable
 {
 	private static final long serialVersionUID = 2785545827041090126L;
 
 	protected CollisionShape shape;
 
 	protected Matrix transform;
 	protected Matrix transformInverse;
 
 	protected Vector linearVelocity;
 	protected Vector angularVelocity;
 
 	public CollisionObject(CollisionShape shape)
 	{
 		this.shape = shape;
 
 		this.transform = new Matrix();
 		this.transformInverse = new Matrix();
 
 		this.linearVelocity = new Vector();
 		this.angularVelocity = new Vector();
 	}
 
 	public CollisionObject(CollisionShape shape, Matrix transform)
 	{
 		this(shape);
 		this.transform = transform;
 		this.transformInverse = transform.inverted();
 	}
 
 	public void rotate(float radiansX, float radiansY, float radiansZ)
 	{
 		Matrix matrix = Matrix.rotate(radiansX, radiansY, radiansZ);
 		this.transform = matrix.multiply(this.transform);
 		this.transformInverse = this.transform.inverted();
 	}
 
 	public void scale(float scaleX, float scaleY, float scaleZ)
 	{
 		Matrix matrix = Matrix.scale(scaleX, scaleY, scaleZ);
 		this.transform = matrix.multiply(this.transform);
 		this.transformInverse = this.transform.inverted();
 	}
 
 	public void translate(float translationX, float translationY, float translationZ)
 	{
 		Matrix matrix = Matrix.translate(translationX, translationY, translationZ);
 		this.transform = matrix.multiply(this.transform);
 		this.transformInverse = this.transform.inverted();
 	}
 
 	public float castRay(final Ray ray, CollisionPoint hitPoint)
 	{
		return this.shape.castRay(this.transform, ray, hitPoint);
 	}
 
 	public CollisionShape getShape()
 	{
 		return this.shape;
 	}
 }
