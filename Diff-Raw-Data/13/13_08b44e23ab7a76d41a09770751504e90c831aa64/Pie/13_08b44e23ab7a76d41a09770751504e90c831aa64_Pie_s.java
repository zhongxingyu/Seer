 package com.example.homokaasuthegame;
 
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 
 
 /**
  * A delicious pastry.
  * @author kviiri
  *
  */
 public class Pie extends Sprite {
     private static final float WIDTH = 453;
     private static final float HEIGHT = 145;
     private static ITextureRegion pieTextureRegion;
 
     static void init(MainActivity activity) {
        pieTextureRegion = activity.loadTexture("pie.png", (int)WIDTH, (int)HEIGHT, 0, 0);
     }
 
 	public Pie(float pX, float pY,
 	        VertexBufferObjectManager pVertexBufferObjectManager) {
 		super(pX, pY, 453f, 145f, pieTextureRegion, pVertexBufferObjectManager);
 
 		BodyDef bd = new BodyDef();
 		bd.type = BodyDef.BodyType.StaticBody;
 		bd.position.set(pX, pY);
 
 		FixtureDef fd = new FixtureDef();
 		//fd.isSensor = true;
 		Body body = PhysicsFactory.createBoxBody(MainActivity.physicsWorld,
		        WIDTH / 2f, HEIGHT / 2f, WIDTH - 5f, HEIGHT -5f, bd.type, fd);
 
 		MainActivity.physicsWorld.registerPhysicsConnector(
 		        new PhysicsConnector(this, body, true, false));
 		body.setTransform(pX, pY, 0);
 		MainActivity.mainScene.attachChild(this);
 	}
 
 }
