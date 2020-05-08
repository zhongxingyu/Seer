 package com.game.rania.model.ammunition;
 
 import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
 import com.game.rania.model.element.RegionID;
 
 public class Laser extends Ammunition{
 
 	protected static final float laserTime = 0.25f;
 	
 	public Laser(float x, float y, float xTarget, float yTarget, Color laserColor){
 		super(laserTime, RegionID.LASER, x, y);
 		color.set(laserColor);
 		vAlign = Align.TOP;
 		angle = (float)Math.toDegrees(Math.atan2(-(xTarget - x), (yTarget - y)));
 		if (region != null) {
			scale.set(1.0f, new Vector2(xTarget - x, yTarget - y).len() / region.getRegionHeight());
 		}
 	}
 }
