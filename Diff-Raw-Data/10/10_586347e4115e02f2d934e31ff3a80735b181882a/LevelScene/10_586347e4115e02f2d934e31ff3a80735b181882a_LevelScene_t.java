 package gamedev.scenes;
 
 import gamedev.game.Direction;
 import gamedev.game.GameActivity;
 import gamedev.game.SceneManager.SceneType;
 import gamedev.objects.Player;
 import gamedev.objects.Player.PlayerState;
 
 import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
 import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
 import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
 import org.andengine.extension.tmx.TMXLayer;
 import org.andengine.extension.tmx.TMXLoader;
 import org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.tmx.TMXLayer;
 import org.andengine.extension.tmx.TMXLoader;
 import org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener;
 import org.andengine.extension.tmx.TMXObject;
 import org.andengine.extension.tmx.TMXObjectGroup;
 import org.andengine.extension.tmx.TMXProperties;
 import org.andengine.extension.tmx.TMXTile;
 import org.andengine.extension.tmx.TMXTileProperty;
 import org.andengine.extension.tmx.TMXTiledMap;
 import org.andengine.extension.tmx.util.exception.TMXLoadException;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.TextureRegion;
 import org.andengine.util.debug.Debug;
 import org.andengine.util.math.MathUtils;
 
 import android.opengl.GLES20;
 
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 
 /**
  * Base class for all levels
  * 
  */
 public class LevelScene extends BaseScene {
 
 
 	// TMX Map containing the Level
 	protected TMXTiledMap mTMXTiledMap;
 	protected String tmxFileName;
 	
 	// Controls
 	protected AnalogOnScreenControl pad;
 	protected BitmapTextureAtlas controlTexture;
 	protected TextureRegion controlBaseTextureRegion;
 	protected TextureRegion controlKnobTextureRegion;
 
 	// Player. Each level has to create the Player and its position in the world
 	protected Player player;
 	
 	
 	public LevelScene(String tmxFileName) {
 		super();
 		this.player = new Player();
 		this.resourcesManager.player = player;
 		this.tmxFileName = tmxFileName;
 		this.createMap();
 		this.connectPhysics();
 		this.createControls();
 		this.attachChild(this.player);
 	}
 
 	@Override
	public void createScene() {
	}

	@Override
 	public void onBackKeyPressed() {
 		// TODO GOTO Pause menu
 	}
 
 	@Override
 	public SceneType getSceneType() {
 		return SceneType.SCENE_GAME;
 	}
 
 	@Override
 	public void disposeScene() {
 		// TODO Unload resources
 
 	}
 
 	protected void connectPhysics() {
 		this.registerUpdateHandler(this.resourcesManager.physicsWorld);
 	}
 	
 	protected void createMap() {
 		try {
 			final TMXLoader tmxLoader = new TMXLoader(this.activity.getAssets(),
 					this.engine.getTextureManager(),
 					TextureOptions.BILINEAR_PREMULTIPLYALPHA,
 					this.vbom,
 					new ITMXTilePropertiesListener() {
 						@Override
 						public void onTMXTileWithPropertiesCreated(
 								final TMXTiledMap pTMXTiledMap,
 								final TMXLayer pTMXLayer,
 								final TMXTile pTMXTile,
 								final TMXProperties<TMXTileProperty> pTMXTileProperties) {
 						}
 					});
 
 			// Load the TMXTiledMap from tmx asset.
 			this.mTMXTiledMap = tmxLoader.loadFromAsset("tmx/" + this.tmxFileName);
 
 		} catch (final TMXLoadException e) {
 			Debug.e(e);
 		}
 		
 		
 		TMXLayer tmxLayerZero = null;
 		
 		// Attach other layers from the TMXTiledMap, if it has more than one.
 		for (int i=0; i<this.mTMXTiledMap.getTMXLayers().size(); i++) {
 			TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(i);
 			if (i == 0) tmxLayerZero = tmxLayer;
 			// Only add non-object layers
 			if (!tmxLayer.getTMXLayerProperties().containsTMXProperty("boundaries", "true")) {
 				this.attachChild(tmxLayer);
 			}
 		}
 		
 		this.camera.setBounds(0, 0, tmxLayerZero.getWidth(), tmxLayerZero.getHeight());
 		this.camera.setBoundsEnabled(true);
 		this.createUnwalkableObjects(this.mTMXTiledMap);
 	}
 	
 	protected void createUnwalkableObjects(TMXTiledMap map) {
 		for (final TMXObjectGroup group : this.mTMXTiledMap.getTMXObjectGroups()) {
 			if (group.getTMXObjectGroupProperties().containsTMXProperty(
 					"boundaries", "true")) {
 				// This is our "wall" layer. Create the boxes from it
 				for (final TMXObject object : group.getTMXObjects()) {
 					final Rectangle rect = new Rectangle(object.getX(),
 							object.getY(), object.getWidth(),
 							object.getHeight(),
 							this.resourcesManager.vbom);
 					final FixtureDef boxFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0);
 					PhysicsFactory.createBoxBody(this.resourcesManager.physicsWorld, rect,
 							BodyType.StaticBody, boxFixtureDef);
 					rect.setVisible(false);
 					this.attachChild(rect);
 				}
 			}
 		}
 	}
 	
 	protected void createControls() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 		this.controlTexture = new BitmapTextureAtlas(
 				this.textureManager, 256, 128, TextureOptions.BILINEAR);
 		this.controlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.controlTexture, this.activity, "onscreen_control_base.png", 0, 0);
 		this.controlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.controlTexture, this.activity, "onscreen_control_knob.png", 128, 0);
 		this.controlTexture.load();
 		
 		this.pad = new AnalogOnScreenControl(0, GameActivity.HEIGHT
 				- this.controlBaseTextureRegion.getHeight(), this.camera,
 				this.controlBaseTextureRegion, this.controlKnobTextureRegion,
 				0.1f, 200, this.vbom, createControlListener(this.player));
 
 		this.pad.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
 		this.pad.getControlBase().setAlpha(0.5f);
 		this.pad.getControlBase().setScaleCenter(0, 128);
 		this.pad.getControlBase().setScale(1.25f);
 		this.pad.getControlKnob().setScale(1.25f);
 		this.pad.refreshControlKnobPosition();
 		this.setChildScene(this.pad);
 	}
 
 	private IAnalogOnScreenControlListener createControlListener(final Player player) {
 		
 		return new IAnalogOnScreenControlListener() {
 			@Override
 			public void onControlChange(
 					final BaseOnScreenControl pBaseOnScreenControl,
 					final float pValueX, final float pValueY) {
 				
 				// Compute direction in degree (from -180 to +180).
 				float degree = MathUtils.radToDeg((float) Math.atan2(
 						pValueX, pValueY));
 				
 				// Set the direction and State
 				// TODO Handle the velocity on a method in the Player class
 				player.setDirection(Direction.getDirectionFromDegree(degree));
 				if (degree == 0) {
 					player.setState(PlayerState.IDLE);
 					player.body.setLinearVelocity(0, 0);
 				} else if (Math.abs(pValueX) > 0.75 || Math.abs(pValueY) > 0.75) {
 					player.setState(PlayerState.RUNNING);
 					player.body.setLinearVelocity(pValueX * 7, pValueY * 7);
 				} else {
 					player.setState(PlayerState.WALKING);
 					player.body.setLinearVelocity(pValueX * 5, pValueY * 5);					
 				}
 				
 			}
 
 			@Override
 			public void onControlClick(
 					AnalogOnScreenControl pAnalogOnScreenControl) {
 				// TODO Auto-generated method stub
 				
 			}
 
 		};
 	}
 
 	
 }
