 package com.secondhand.model;
 
 import java.util.List;
 
 import org.anddev.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
 
 import com.badlogic.gdx.physics.box2d.Body;
 import com.secondhand.view.opengl.Polygon;
 
 public abstract class PolygonEntity extends Entity {
 	
 	protected final Polygon polygon;
 	private final Body mBody;
 	
 	// polygon won't be allowed to grow.
 	private final float radius; 
 	
 	
 	public PolygonEntity(final Polygon polygon, final boolean isEdible, final GameWorld level) {
 		super(polygon, isEdible, level);
 		
 		mBody=physics.createType(polygon, this);
 		this.setBody(mBody);
 		
 		this.polygon = polygon;
 		polygon.setBody(this.getBody());
 		this.radius = computeRadius(polygon.getPolygon());
 	}
 	
 	@Override
 	public boolean isCircle(){
 		return false;
 	}
 		
 	public Vector2 getCenterOfMass(){
		final Vector2 v = new Vector2(this.getBody().getMassData().center.x, this.getBody().getMassData().center.x) ;
 		
 		return new Vector2(v.x * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
 				v.y * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
 	}
 	
 	private final float computeRadius(final List<Vector2> polygon) {
 		
 		// we define the radius to be the maximum length between the center of mass
 		// and a vertex in the polygon. 
 		
 		float maxLength = 0;
 		
 		final Vector2 center = getCenterOfMass();
 
 		for(int i = 0; i < polygon.size(); ++i) {
 			final Vector2 p1 = polygon.get(i);
 
 			final float length = (float)Math.sqrt(Math.pow(p1.x - center.x, 2) + Math.pow(p1.y - center.y, 2));
 			if(length > maxLength) {
 				maxLength = length;
 			}
 		}
 		
 		return maxLength;
 	}
 	
 	@Override 
 	public float getRadius() {
 		return radius;
 	}
 	
 	public Polygon getPolygon() {
 		return this.polygon;
 	}
 }
