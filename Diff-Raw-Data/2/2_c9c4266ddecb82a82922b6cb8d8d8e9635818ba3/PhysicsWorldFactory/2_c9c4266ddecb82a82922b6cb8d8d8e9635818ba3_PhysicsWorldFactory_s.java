 package org.andengine.extension.rubeloader.factory;
 
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.extension.rubeloader.def.WorldDef;
 
 public class PhysicsWorldFactory implements IPhysicsWorldFactory {
 	@Override
 	public PhysicsWorld populate(WorldDef pWorldDef) {
		PhysicsWorld ret = new PhysicsWorld(pWorldDef.gravity, pWorldDef.allowSleep, pWorldDef.positionIterations, pWorldDef.velocityIterations);
 		tuneParams(pWorldDef, ret);
 		return ret;
 	}
 
 	protected void tuneParams(WorldDef pWorldDef, PhysicsWorld pPhysicsWorld) {
 		pPhysicsWorld.setAutoClearForces(pWorldDef.autoClearForces);
 		pPhysicsWorld.setWarmStarting(pWorldDef.warmStarting);
 		pPhysicsWorld.setContinuousPhysics(pWorldDef.continuousPhysics);
 	}
 }
