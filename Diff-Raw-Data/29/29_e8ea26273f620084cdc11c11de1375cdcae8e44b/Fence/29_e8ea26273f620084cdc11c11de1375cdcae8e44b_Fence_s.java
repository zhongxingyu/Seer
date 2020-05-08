 package org.sanchome.shy.engine.entity;
 
 import org.sanchome.shy.engine.ApplicationClient;
 import org.sanchome.shy.engine.CollisionGroup;
 import org.sanchome.shy.engine.UserSettings;
 import org.sanchome.shy.engine.UserSettings.ShadowDetails;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.shapes.BoxCollisionShape;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.math.FastMath;
 import com.jme3.math.Matrix3f;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.queue.RenderQueue.ShadowMode;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 
 public class Fence extends AEntity {
 	
 	private Spatial model;
 	private RigidBodyControl rbc;
 	
 	@Override
 	public String getEntityName() {
 		return "Fence:"+order;
 	}
 	@Override
 	public void initForClient(AssetManager assetManager, Node myNode) {
 		myNode.setLocalScale(1.4f);
 		
 		// Model loading
 		model = assetManager.loadModel("models/blender/Fence.mesh.xml" );
 		
 		myNode.attachChild(model);
 		
 		// Set shadow options
 		model.setShadowMode(UserSettings.SHADOW_DETAIL == ShadowDetails.FULL ? ShadowMode.CastAndReceive : ShadowMode.Cast);
 		
 	}
 	@Override
 	public void initForServer(AssetManager assetManager, Node myNode, BulletAppState bulletAppState) {
 		// Adding physic collision shape
 		BoxCollisionShape bcs = new BoxCollisionShape(new Vector3f(0.4f, 3.5f, 3.3f));
 		rbc = new RigidBodyControl(bcs, 0.0f);
 		rbc.setCollisionGroup(CollisionGroup.TREES);
 		rbc.setCollideWithGroups(CollisionGroup.TREES_COLLISION_MASK);
 		model.setUserData("PhysicsRigidBody", rbc);
 		model.addControl(rbc);
 		
 		bulletAppState.getPhysicsSpace().add(rbc);
 	}
 	
 	@Override
 	public void setPosition(Vector3f position) {
 		float y = ApplicationClient.getCurrentWorld().getHeightAt(position.x, position.z, 0.0f);
 		rbc.setPhysicsLocation(position.setY(y));
 		
 		// Re-orient correctly against terrain
 		Matrix3f rot = Matrix3f.IDENTITY.clone();
 		rot.fromStartEndVectors(Vector3f.UNIT_Y, ApplicationClient.getCurrentWorld().getNormalAt(position.x, position.z) );
 		rbc.setPhysicsRotation(rot);
 	}
 	
 	@Override
 	public void setYRotation(float yRadian) {
 		Matrix3f rot = Matrix3f.IDENTITY.clone();
		rot.fromAngleAxis(yRadian + FastMath.HALF_PI, Vector3f.UNIT_Y);
 		Matrix3f orig = rbc.getPhysicsRotationMatrix();
 		rot = orig.mult(rot);
 		rbc.setPhysicsRotation(rot);
 	}
 }
