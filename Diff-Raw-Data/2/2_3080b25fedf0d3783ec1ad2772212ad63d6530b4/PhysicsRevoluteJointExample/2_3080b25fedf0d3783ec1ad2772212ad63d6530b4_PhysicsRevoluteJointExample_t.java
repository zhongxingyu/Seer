 package org.anddev.andengine.examples;
 
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.sprite.AnimatedSprite;
 import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
 import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
 import org.anddev.andengine.util.MathUtils;
 
 import android.widget.Toast;
 
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
 import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
 
 public class PhysicsRevoluteJointExample extends BasePhysicsJointExample {
   private RevoluteJoint mRevoluteJoint;
 
   @Override
   public Scene onLoadScene() {
    final Scene scene = super.onLoadScene();
     initJoints(scene);
     return scene;
   }
 
   private void initJoints(final Scene scene) {
     final int centerX = CAMERA_WIDTH / 2;
     final int centerY = CAMERA_HEIGHT / 2;
 
     final float anchorFaceX = centerX - mBoxFaceTextureRegion.getWidth() * 0.5f;
     final float anchorFaceY = centerY - mBoxFaceTextureRegion.getHeight() * 0.5f;
 
     final AnimatedSprite anchorFace = new AnimatedSprite(anchorFaceX,
         anchorFaceY, mBoxFaceTextureRegion);
     final Body anchorBody = PhysicsFactory.createBoxBody(mPhysicsWorld,
         anchorFace, BodyType.DynamicBody);
 
     final AnimatedSprite movingFace = new AnimatedSprite(anchorFaceX,
         anchorFaceY + 100, mBoxFaceTextureRegion);
     final Body movingBody = PhysicsFactory.createBoxBody(mPhysicsWorld,
         movingFace, BodyType.DynamicBody);
 
     anchorFace.animate(200);
     movingFace.animate(200);
     anchorFace.setUpdatePhysics(false);
     movingFace.setUpdatePhysics(false);
 
     scene.getTopLayer().addEntity(anchorFace);
     scene.getTopLayer().addEntity(movingFace);
 
     mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(anchorFace,
         anchorBody, true, true, false, false));
     mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(movingFace,
         movingBody, true, true, false, false));
 
     final RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
     revoluteJointDef.initialize(anchorBody, movingBody,
         anchorBody.getWorldCenter());
     revoluteJointDef.enableMotor = true;
     revoluteJointDef.motorSpeed = MathUtils.degToRad(45);
     revoluteJointDef.maxMotorTorque = 100;
 
     mRevoluteJoint = (RevoluteJoint)mPhysicsWorld.createJoint(revoluteJointDef);
     Toast.makeText(this, "Motor speed: " + mRevoluteJoint.getMotorSpeed(),
         Toast.LENGTH_LONG).show();
   }
 }
