 package com.calumgilchrist.ld23.tinyworld.core;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.World;
 
 public class Player extends Planetoid {
 
 	
 	public Player(Vec2 startPos, Sprite s, BodyDef bodyDef, World world) {
 		super(startPos, s, bodyDef, world);
 	
 	}
 	
 	/**
 	 * Add mass to the object
 	 * @param mass
 	 */
 	public void addMass(float mass) {
 		float oldMass = this.getBody().m_mass;
 		
 		float newMass = oldMass + mass;
 		
 		this.getBody().m_mass = newMass;
 	}
 	
 	public void applyThrust(Vec2 force) {

		force = force.mul(1/Constants.PHYS_RATIO);
 		this.getBody().applyForce(force, this.getBody().getWorldCenter());
 	}
 
 }
