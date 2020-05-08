 package com.secondhand.model.entity;
 
import java.util.ArrayList;

 import com.secondhand.model.physics.Vector2;
 
 // I decided to refactor some code from player because the that class was so large
 class PlayerUtil {
 
 	private final Player player;
 	private boolean isMirroredMovement;
 
 	
 	public static final float PLAYER_SPEED = 40;
 
  	public PlayerUtil(final Player player) {
 		this.player = player;
 		this.isMirroredMovement = false;
 	}
 
 	public boolean isMirroredMovement() {
 		return this.isMirroredMovement;
 	}
 
 	public void setMirroredMovement(final boolean mirrored) {
 		this.isMirroredMovement = mirrored;
 	}
 	
 	public void reachToTouch(final Vector2 touch) {
 		Vector2 forcePosition;
 
 		if (this.isMirroredMovement()) {
 			final Vector2 v1 = new Vector2(touch.x - player.getCenterX(),
 					touch.y - player.getCenterY());
 			v1.mul(-1);
 			final Vector2 v2 = new Vector2(player.getCenterX(),
 					player.getCenterY());
 			v2.add(v1);
 			forcePosition = new Vector2(v2.x, v2.y);
 		} else {
 
 			forcePosition = new Vector2(touch.x, touch.y);
 		}
 
 		final Vector2 force = new Vector2((player.getCenterX() - touch.x),
 				player.getCenterY() - touch.y);
 
 		if (this.isMirroredMovement) {
 			force.mul(-1);
 		}
 
 		force.x = force.x / force.len();
 		force.y = force.y / force.len();		
 
 		force.mul(player.getSpeedMultiplier() * PLAYER_SPEED);
 		
 		player.physics.applyImpulse(force, forcePosition);
 	}
 
 
 }
