 package com.example.homokaasuthegame;
 
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.ITiledTextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.QueryCallback;
 import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
 import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
 
 
 public class Enemy extends AnimatedSprite {
     protected static BodyDef bd;
 	protected static FixtureDef fd;
 	protected Body body;
 
     MouseJoint joint = null;
     private final Vector2 testPoint = new Vector2();
     private final Vector2 startPoint = new Vector2();
     private final Body groundBody;
     private Body hitBody = null;
 
 	static {
 	    bd = new BodyDef();
 	    fd = new FixtureDef();
 	}
 
 	public Enemy(float pX, float pY, float rot, float pWidth, float pHeight,
 			ITextureRegion pTextureRegion,
 			VertexBufferObjectManager vertexBufferObjectManager,
 			float bodyWScale, float bodyHScale) {
 		super(-900, -900, pWidth, pHeight, (ITiledTextureRegion)pTextureRegion,
 				vertexBufferObjectManager);
 
 		bd.type = BodyType.DynamicBody;
 		bd.active = true;
 
 		fd.density = 1.0f;
 		fd.friction = 0.1f;
 		fd.restitution = 0.01f;
 
 		body = PhysicsFactory.createBoxBody(MainActivity.physicsWorld,
 		        pWidth / 2f, pHeight / 2f,
 		        pWidth * bodyWScale, pHeight * bodyHScale, bd.type, fd);
 		body.setUserData(this);
 
 		MainActivity.physicsWorld.registerPhysicsConnector(
 		        new PhysicsConnector(this, body, true, true));
 		body.setTransform(pX, pY, rot);
 		MainActivity.mainScene.attachChild(this);
 
 		/* Drag */
 		BodyDef bodyDef = new BodyDef();
         groundBody = MainActivity.physicsWorld.createBody(bodyDef);
 	}
 
 	QueryCallback callback = new QueryCallback() {
         @Override
         public boolean reportFixture(Fixture fixture) {
             /* If the hit point is inside the fixture of the body
             /* we report it
              */
             Vector2 v = fixture.getBody().getPosition();
             if (fixture.testPoint(testPoint.x, testPoint.y) ||
                     v.dst(testPoint.x, testPoint.y) < 20f) { /* Touch dist */
                 hitBody = fixture.getBody();
                 if (hitBody.equals(body)) {
                     return false;
                 }
             }
             return true; /* Keep going until all bodies in the area are checked. */
         }
     };
 
 	@Override
     public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
             final float touchAreaX, final float touchAreaY) {
         switch (pSceneTouchEvent.getAction()) {
         case TouchEvent.ACTION_DOWN:
             if (joint != null)
                 break;
 
             testPoint.set(
                     pSceneTouchEvent.getX() / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
                     pSceneTouchEvent.getY() / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
 
             hitBody = null;
             MainActivity.physicsWorld.QueryAABB(callback,
                     testPoint.x - 0.0001f,
                     testPoint.y - 0.0001f,
                     testPoint.x + 0.0001f,
                     testPoint.y + 0.0001f);
 
             if (hitBody == null)
                 return false;
 
             /* Ignore kinematic bodies, they don't work with the mouse joint */
             if (hitBody.getType() == BodyType.KinematicBody)
                 return false;
 
             if (hitBody.equals(this.body)) {
                 MouseJointDef def = new MouseJointDef();
                 def.bodyA = groundBody;
                 def.bodyB = hitBody;
                 def.collideConnected = true;
                 def.target.set(testPoint.x, testPoint.y);
                 def.maxForce = 50f * hitBody.getMass() * MainActivity.physicsWorld.getGravity().len();
 
                 startPoint.set(testPoint.x, testPoint.y);
                 joint = (MouseJoint)((body.getWorld()).createJoint(def));
                 hitBody.setAwake(true);
             }
             break;
 
         case TouchEvent.ACTION_MOVE:
             if (joint != null) {
                 joint.setTarget(
                         new Vector2(pSceneTouchEvent.getX() / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
                                 pSceneTouchEvent.getY() / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT));
             }
             break;
 
         case TouchEvent.ACTION_UP:
             if (joint != null) {
                 MainActivity.physicsWorld.destroyJoint(joint);
                 joint = null;
             }
             return false;
         }
         return true;
     }
 
 	private boolean shouldDie() {
		return this.mX < -64 || this.mY > MainActivity.CAMERA_WIDTH + 64 || this.mY < -64;
 	}
 
 	@Override
 	protected void onManagedUpdate(final float pSecondsElapsed) {
 		super.onManagedUpdate(pSecondsElapsed);
 		if(shouldDie()) {
 			die();
 		}
 	}
 
 	private void die() {
 	    MainActivity.mainActivity.removeEnemy(this);
 	}
 }
