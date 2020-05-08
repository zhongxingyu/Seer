 package org.andengine.extension.rubeloader.factory;
 
 import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.extension.rubeloader.def.WorldDef;
 
 public class PhysicsWorldFactoryFixedStep extends PhysicsWorldFactory {
 	private final int mStepsPerSecond;
 
 	public PhysicsWorldFactoryFixedStep(int pStepsPerSecond) {
 		super();
 		this.mStepsPerSecond = pStepsPerSecond;
 	}
 
 	@Override
 	public PhysicsWorld populate(WorldDef pWorldDef) {
		PhysicsWorld ret = new FixedStepPhysicsWorld(mStepsPerSecond, pWorldDef.gravity, pWorldDef.allowSleep, pWorldDef.positionIterations, pWorldDef.velocityIterations);
 		tuneParams(pWorldDef, ret);
 		return ret;
 	}
 }
