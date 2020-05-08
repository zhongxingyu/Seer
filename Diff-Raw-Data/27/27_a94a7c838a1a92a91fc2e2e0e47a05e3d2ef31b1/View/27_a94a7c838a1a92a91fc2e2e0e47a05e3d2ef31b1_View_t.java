 package com.punchline.javalib.entities.components.generic;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.punchline.javalib.entities.Entity;
 import com.punchline.javalib.entities.components.physical.Sensor;
 
 /**
  * Generic Sensor component that represents an Entity's view.
  * @author Natman64
  *
  */
 public class View extends Sensor {
 	
	//region Fields
	
 	private float viewRange;
 	private float fov;
 	private int vertices;
 	private float offsetDegrees;
 
	private Vector2 position = Vector2.Zero.cpy();
	
	//endregion
	
 	//region Initialization
 	
 	/**
 	 * Constructs a View.
 	 * @param owner The owner of this View.
 	 * @param viewRange The range of this View.
 	 * @param fov The field of view.
 	 * @param vertices The number of vertices in the fixture. Must be above 3, but higher values may cause lag.
 	 */
 	public View(Entity owner, float viewRange, float fov, int vertices) {
 		super(owner);
 		
 		this.setViewRange(viewRange);
 		this.setFOV(fov);
 		this.setVertices(vertices);
 	}
 	
 	/**
 	 * Constructs a View with the default number of vertices, 5.
 	 * @param owner The owner of this View.
 	 * @param viewRange The range of this View.
 	 * @param fov The field of view.
 	 */
 	public View(Entity owner, float viewRange, float fov) {
 		this(owner, viewRange, fov, 8);
 	}
 	
 	//endregion
 
 	//region Accessors
 	
 	/**
 	 * @return The range of this Sensor's view, in meters.
 	 */
 	public float getViewRange() {
 		return viewRange;
 	}
 	
 	/**
 	 * @return This Sensor's field of view, from 0 to 1.
 	 */
 	public float getFOV() {
 		return fov;
 	}
 	
 	/**
 	 * @return The number of vertices in the fixture representing this sensor.
 	 */
 	public int getVertices() {
 		return vertices;
 	}
 	
 	/**
 	 * @return This sensor's offset from the angle of its body, in degrees.
 	 */
 	public float getOffset() {
 		return offsetDegrees;
 	}
 	
 	//endregion
 	
 	//region Mutators
 	
 	/**
 	 * Sets the range of this sensor's view, in meters. Takes effect only after {@link #refresh()} is called.
 	 * @param viewRange The new view range.
 	 */
 	public void setViewRange(float viewRange) {
 		if (viewRange < 0) viewRange = 0;
 		this.viewRange = viewRange;
 	}
 	
 	/**
 	 * Sets this sensor's field of view, from 0 to 1. Takes effect only after {@link #refresh()} is called.
 	 * @param fov The new field of view.
 	 */
 	public void setFOV(float fov) {
 		if (fov < 0) fov = 0;
 		if (fov > 1) fov = 1;
 		this.fov = fov;
 	}
 	
 	/**
 	 * Sets the number of vertices in the fixture representing this sensor. 
 	 * Takes effect only after {@link #refresh()} is called.
 	 * @param vertices The new amount of vertices.
 	 */
 	public void setVertices(int vertices) {
 		if (vertices < 3) vertices = 3;
 		if (vertices > 8) vertices = 8;
 		this.vertices = vertices;
 	}
 	
 	/**
 	 * Sets this sensor's offset from the angle of its body, in degrees. Takes effect only after {@link #refresh()} is called.
 	 * @param degrees The offset, in degrees.
 	 */
 	public void setOffset(float degrees) {
 		offsetDegrees = degrees;
 	}
	
	/**
	 * Sets the position of the View sensor relative to its parent body's position.
	 * @param position
	 */
	public void setPosition(Vector2 position) {
		this.position = position;
	}
 
 	//endregion
 	
 	//region Helpers
 	
 	@Override
 	public void refresh() {
 		super.refresh();
 		
 		Vector2[] verts = new Vector2[vertices];
		verts[0] = new Vector2(0, 0).add(position);
 		
 		float degrees = 360f * fov;
 		
 		float start = (float)Math.toRadians(offsetDegrees + degrees / 2);
 		
 		for (int i = 1; i < vertices; i++) {
			float angle = start - ((float) i / (vertices)) * (float)Math.toRadians(degrees);
 			
			verts[i] = new Vector2(
					viewRange * (float) Math.cos(angle), 
					viewRange * (float) Math.sin(angle)).add(position);
 		}
 		
 		PolygonShape shape = new PolygonShape();
 		shape.set(verts);
 		
 		FixtureDef fd = new FixtureDef();
 		fd.shape = shape;
 		fd.isSensor = true;
 		
 		fixture = body.createFixture(fd); //Create the new fixture
 		fixture.setUserData(owner); //Set the new fixture's user data
 		
 		shape.dispose();
 	}
 	
 	//endregion
 	
 }
