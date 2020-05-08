 package se.chalmers.segway.scenes;
 
 import java.io.IOException;
 
 import org.andengine.entity.IEntity;
 import org.andengine.entity.modifier.LoopEntityModifier;
 import org.andengine.entity.modifier.ScaleModifier;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.util.SAXUtils;
 import org.andengine.util.level.EntityLoader;
 import org.andengine.util.level.simple.SimpleLevelEntityLoaderData;
 import org.xml.sax.Attributes;
 
 import se.chalmers.segway.entities.Player;
 import se.chalmers.segway.resources.ResourcesManager;
 import se.chalmers.segway.scenes.GameScene;
 import se.chalmers.segway.scenes.SceneManager;
 
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 
 public class LevelLoader extends EntityLoader<SimpleLevelEntityLoaderData> {
 
 	private static final String TAG_ENTITY = "entity";
 	private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
 	private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
 	private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";
 
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1 = "platform1";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2 = "platform2";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM3 = "platform3";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_COIN = "coin";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER = "player";
 	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_GOLDEN_COOKIE = "golden_cookie";
 
 	private Player player;
 	private ResourcesManager resourcesManager;
 	private SceneManager sceneManager;
 	private PhysicsWorld physicsWorld;
 	private VertexBufferObjectManager vbom;
 
 	public LevelLoader(PhysicsWorld pw, Player p) {
 		super(TAG_ENTITY);
 		this.init();
 		physicsWorld = pw;
 		player = p;
 	}
 
 	private void init() {
 		sceneManager = SceneManager.getInstance();
 		resourcesManager = ResourcesManager.getInstance();
 		vbom = resourcesManager.vbom;
 	}
 
 	final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(0, 0.01f,
 			0.5f);
 
 	public IEntity onLoadEntity(final String pEntityName,
 			final IEntity pParent, final Attributes pAttributes,
 			final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData)
 			throws IOException {
 		final int x = SAXUtils.getIntAttributeOrThrow(pAttributes,
 				TAG_ENTITY_ATTRIBUTE_X);
 		final int y = SAXUtils.getIntAttributeOrThrow(pAttributes,
 				TAG_ENTITY_ATTRIBUTE_Y);
 		final String type = SAXUtils.getAttributeOrThrow(pAttributes,
 				TAG_ENTITY_ATTRIBUTE_TYPE);
 
 		final Sprite levelObject;
 
 		// Cases for loading different objects
 		// Loads platform1
 		if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1)) {
 			levelObject = loadPlatform(x, y, "platform1",
 					resourcesManager.platform1_region);
 		} else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2)) {
 			levelObject = loadPlatform(x, y, "platform2",
 					resourcesManager.platform2_region);
 		} else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM3)) {
 			levelObject = loadPlatform(x, y, "platform3",
 					resourcesManager.platform3_region);
 		} else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_COIN)) {
 			ITextureRegion cookie = resourcesManager.cookies_region
					.getTextureRegion((int) (Math.random() * 8));
 			levelObject = new Sprite(x, y, cookie, vbom) {
 				@Override
 				protected void onManagedUpdate(float pSecondsElapsed) {
 					super.onManagedUpdate(pSecondsElapsed);
 
 					if (player.collidesWith(this)) {
 						// addToScore(10);
 						this.setVisible(false);
 						this.setIgnoreUpdate(true);
 					}
 				}
 			};
 			levelObject.registerEntityModifier(new LoopEntityModifier(
 					new ScaleModifier(1, 0.4f, 0.6f)));
 			// Loading player type objects
 		} else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER)) {
 			player.setX(x);
 			player.setY(y);
 
 			levelObject = player;
 		} else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_GOLDEN_COOKIE)) {
 			levelObject = new Sprite(x, y, resourcesManager.golden_cookie, vbom) {
 				@Override
 				protected void onManagedUpdate(float pSecondsElapsed) {
 					super.onManagedUpdate(pSecondsElapsed);
 
 					if (player.collidesWith(this)) {
 						GameScene gs = (GameScene) sceneManager
 								.getCurrentScene();
 						gs.showLevelComplete();
 					}
 				}
 			};
 
 		} else {
 			throw new IllegalArgumentException();
 		}
 
 		levelObject.setCullingEnabled(true);
 
 		return levelObject;
 	}
 
 	private Sprite loadPlatform(int x, int y, String platform,
 			ITextureRegion platformRegion) {
 		Sprite platformSprite = new Sprite(x, y, platformRegion, vbom);
 		final Body body = PhysicsFactory.createBoxBody(physicsWorld,
 				platformSprite, BodyType.StaticBody, FIXTURE_DEF);
 		body.setUserData(platform);
 		physicsWorld.registerPhysicsConnector(new PhysicsConnector(
 				platformSprite, body, true, false));
 
 		return platformSprite;
 	}
 
 }
