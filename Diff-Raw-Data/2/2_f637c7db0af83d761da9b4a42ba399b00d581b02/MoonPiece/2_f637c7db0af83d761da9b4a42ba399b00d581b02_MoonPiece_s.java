 package ddmp.projecttetra;
 
 import org.andengine.engine.Engine;
 import org.andengine.engine.Engine.EngineLock;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.opengl.texture.region.ITextureRegion;
 
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 
 /**
 * A piece of a planet.
  */
 public class MoonPiece implements IUpdateHandler {
 	
 	private static final FixtureDef PIECE_FIXTURE_DEF = 
 			PhysicsFactory.createFixtureDef(0.1f, 0.5f, 0.5f);
 	private static final float ALPHA_STEP = 0.01f;
 	
 	private Engine engine;
 	private PhysicsWorld physicsWorld;
 	private PhysicsConnector con;
 	
 	public MoonPiece(float x, float y, float size, ITextureRegion pieceTextureRegion,
 			Engine engine, PhysicsWorld physicsWorld) {
 		this.engine = engine;
 		this.physicsWorld = physicsWorld;
 		
 		Sprite sprite = new Sprite(x, y, size, size, pieceTextureRegion,
 				engine.getVertexBufferObjectManager());
 		Body body = PhysicsFactory.createBoxBody(physicsWorld, sprite, BodyType.DynamicBody,
 				PIECE_FIXTURE_DEF);
 		this.con = new PhysicsConnector(sprite, body);
 		engine.getScene().attachChild(sprite);
 		engine.getScene().registerUpdateHandler(this);
 		physicsWorld.registerPhysicsConnector(con);
 	}
 
 	@Override
 	public void onUpdate(float pSecondsElapsed) {
 		con.getShape().setAlpha(con.getShape().getAlpha() - ALPHA_STEP);
 		if(con.getShape().getAlpha() <= 0) {
 			die();
 		}
 	}
 	
 	private void die() {
 		EngineLock engineLock = engine.getEngineLock();
 		engineLock.lock();
 		
 		engine.getScene().detachChild(con.getShape());
 		engine.getScene().unregisterUpdateHandler(this);
 		con.getShape().dispose();
 		physicsWorld.unregisterPhysicsConnector(con);
 		physicsWorld.destroyBody(con.getBody());
 		
 		engineLock.unlock();
 	}
 
 	@Override
 	public void reset() {
 		
 	}
 	
 }
