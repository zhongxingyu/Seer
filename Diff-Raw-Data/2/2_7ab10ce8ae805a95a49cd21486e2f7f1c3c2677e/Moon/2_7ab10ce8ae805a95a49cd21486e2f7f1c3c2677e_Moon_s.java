 package ddmp.projecttetra;
 
 import java.util.List;
 
 import org.andengine.engine.Engine;
 import org.andengine.entity.shape.IShape;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.opengl.texture.ITexture;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.TextureRegionFactory;
 
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 
 /**
 * A planet in the game.
  */
 public class Moon {
 	
 	private static final FixtureDef MOON_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f, true);
 	private static final float MOON_MIN_SIZE = 0.15f; //In percent of camera height
 	private static final float MOON_MAX_SIZE = 0.25f;	//In percent of camera height
 	private static final float KILL_DISTANCE_SQUARED = 2250000;
 	
 	private Engine engine;
 	private PhysicsWorld physicsWorld;
 	private ITextureRegion moonTextureRegion;
 	private PhysicsConnector con;
 	private Body body;
 	private Sprite shape;
 	private Comet comet;
 	private boolean dead;
 
 	public Moon(float x, float y, ITextureRegion moonTextureRegion, Comet comet, Engine engine,
 					PhysicsWorld physicsWorld) {
 		this.comet = comet;
 		this.engine = engine;
 		this.physicsWorld = physicsWorld;
 		this.moonTextureRegion = moonTextureRegion;
 		this.dead = false;
 		
 		float scale = MOON_MIN_SIZE + (MOON_MAX_SIZE - MOON_MIN_SIZE) * (float) Math.random();
 		float size = scale * TetraActivity.CAMERA_HEIGHT;
 		shape = new Sprite(x, y, size, size, moonTextureRegion, 
 				engine.getVertexBufferObjectManager());
 		body = PhysicsFactory.createCircleBody(physicsWorld, shape, 
 				BodyType.StaticBody, MOON_FIXTURE_DEF);
 		body.getFixtureList().get(0).setUserData(this); /* A bit hacky. */
 		this.con = new PhysicsConnector(shape, body, true, true);
 	}
 	
 	public void update() {
 		float cometCenterX = comet.getShape().getX() + comet.getShape().getScaleCenterX();
 		float cometCenterY = comet.getShape().getY() + comet.getShape().getScaleCenterY();
 		float distanceX = shape.getX() + shape.getScaleCenterX() - cometCenterX;
 		float distanceY = shape.getY() + shape.getScaleCenterY() - cometCenterY;
 		float distanceSq = distanceX * distanceX + distanceY * distanceY;
 		/* Die if far away from comet */
 		if(distanceSq > KILL_DISTANCE_SQUARED) {
 			dead = true;
 		}
 		
 		checkForCollisionWithCommet();
 	}
 	
 	private void checkForCollisionWithCommet() {
 		if(dead) {
 			return;
 		}
 		
 		List<Contact> contacts = physicsWorld.getContactList();
 		Contact contact = null;
 		int size = contacts.size();
 		for(int i = 0; i < size; i++) {
 			contact = contacts.get(i);
 			if(contact.isTouching()) {
 				Object aData = contact.getFixtureA().getUserData();
 				Object bData = contact.getFixtureB().getUserData();
 				if((aData == this || bData == this) && 
 						(aData instanceof Comet || bData instanceof Comet)) {
 					/* This has collided with comet, break apart. */
 					breakApart();
 				}
 			}
 		}
 	}
 	
 	private void breakApart() {
 		dead = true;
 		ITexture texture = moonTextureRegion.getTexture();
 		int tX = (int) moonTextureRegion.getTextureX();
 		int tY = (int) moonTextureRegion.getTextureY();
 		int tW = (int) moonTextureRegion.getWidth();
 		int tH = (int) moonTextureRegion.getHeight();
 		ITextureRegion reg1 = TextureRegionFactory.extractFromTexture(texture, tX, tY, tW/2, tH/2);
 		ITextureRegion reg2 = TextureRegionFactory.extractFromTexture(texture, tX+tW/2, tY, tW/2, tH/2);
 		ITextureRegion reg3 = TextureRegionFactory.extractFromTexture(texture, tX, tY+tH/2, tW/2, tH/2);
 		ITextureRegion reg4 = TextureRegionFactory.extractFromTexture(texture, tX+tW/2, tY+tH/2, tW/2, tH/2);
 		
 		float sX = shape.getX();
 		float sY = shape.getY();
 		float size = shape.getWidth() / 2;
 		new MoonPiece(sX, sY, size, reg1, engine, physicsWorld);
 		new MoonPiece(sX+size, sY, size, reg2, engine, physicsWorld);
 		new MoonPiece(sX, sY+size, size, reg3, engine, physicsWorld);
 		new MoonPiece(sX+size, sY+size, size, reg4, engine, physicsWorld);
 	}
 	
 	public Body getBody() {
 		return con.getBody();
 	}
 	
 	public IShape getShape() {
 		return con.getShape();
 	}
 	
 	public PhysicsConnector getPhysicsConnector() {
 		return con;
 	}
 	
 	public boolean isDead() {
 		return dead;
 	}
 
 }
