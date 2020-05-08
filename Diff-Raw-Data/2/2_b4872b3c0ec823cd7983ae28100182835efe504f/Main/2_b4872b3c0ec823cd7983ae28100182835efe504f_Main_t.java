 package fi.hackoid;
 
 import java.io.IOException;
 
 import org.andengine.audio.music.Music;
 import org.andengine.audio.music.MusicFactory;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.Set;
 
 import org.andengine.engine.Engine;
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.camera.hud.HUD;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.AutoParallaxBackground;
 import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.sprite.UncoloredSprite;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.shader.PositionTextureCoordinatesShaderProgram;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.TextureRegionFactory;
 import org.andengine.opengl.texture.render.RenderTexture;
 import org.andengine.opengl.util.GLState;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 import org.andengine.util.debug.Debug;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactImpulse;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Manifold;
 
 import android.annotation.SuppressLint;
 import android.hardware.SensorManager;
 import android.opengl.GLES20;
 import android.view.KeyEvent;
 
 public class Main extends SimpleBaseGameActivity {
 
 	static final int CAMERA_WIDTH = 1280;
 	static final int CAMERA_HEIGHT = 720;
 
 	private BitmapTextureAtlas backgroundTextureAtlas;
 
 	private ITextureRegion backgroundTextureSky;
 	private ITextureRegion backgroundTextureCity;
 	private ITextureRegion backgroundTextureGround;
 
 	private BitmapTextureAtlas controlTextureAtlas;
 	private ITextureRegion horizontalControlTexture;
 	private ITextureRegion jumpControlTexture;
 	private ITextureRegion fireControlTexture;
 
 	private Camera camera;
 	private AutoParallaxBackground autoParallaxBackground;
 	private Main main;
 	Scene scene;
 	
 	BitmapTextureAtlas deathScreenAtlas;
 	ITextureRegion deathScreenTexture;
 
 	Player player = new Player();
 
 	PhysicsWorld world;
 
 	Stats stats;
 
 	private Music mMusic;
 
 	private Random random = new Random();
 
 	Set<BeerProjectile> beers = new HashSet<BeerProjectile>();
 	LinkedList<BeerProjectile> beersToBeRemoved = new LinkedList<BeerProjectile>();
 
 	Set<SpearProjectile> spears = new HashSet<SpearProjectile>();
 	LinkedList<SpearProjectile> spearsToBeRemoved = new LinkedList<SpearProjectile>();
 
 	Set<Enemy> enemies = new HashSet<Enemy>();
 
 	Tree tree;
 	Fuhrer fuhrer;
 	DeathScreen deathScreen;
 	
 	boolean playerDead = false;
 	
 	HUD yourHud;
 	Boss boss;
 
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		camera = new CustomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 
 		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,
 				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
 		engineOptions.getTouchOptions().setNeedsMultiTouch(true);
 
 		engineOptions.getAudioOptions().setNeedsMusic(true);
 
 		return engineOptions;
 	}
 
 	@Override
 	public Engine onCreateEngine(EngineOptions pEngineOptions) {
 		return new Engine(pEngineOptions) {
 			private boolean mRenderTextureInitialized;
 
 			private RenderTexture mRenderTexture;
 			private UncoloredSprite mRenderTextureSprite;
 
 			@SuppressLint("WrongCall")
 			@Override
 			public void onDrawFrame(final GLState pGLState) throws InterruptedException {
 				final boolean firstFrame = !this.mRenderTextureInitialized;
 
 				if (firstFrame) {
 					this.initRenderTextures(pGLState);
 					this.mRenderTextureInitialized = true;
 				}
 
 				final int surfaceWidth = this.mCamera.getSurfaceWidth();
 				final int surfaceHeight = this.mCamera.getSurfaceHeight();
 
 				this.mRenderTexture.begin(pGLState);
 				{
 					/* Draw current frame. */
 					super.onDrawFrame(pGLState);
 				}
 				this.mRenderTexture.end(pGLState);
 
 				/* Draw rendered texture with custom shader. */
 				{
 					pGLState.pushProjectionGLMatrix();
 
 					pGLState.orthoProjectionGLMatrixf(0, surfaceWidth, 0, surfaceHeight, -1, 1);
 					{
 						mRenderTextureSprite.onDraw(pGLState, this.mCamera);
 					}
 					pGLState.popProjectionGLMatrix();
 				}
 			}
 
 			private void initRenderTextures(final GLState pGLState) {
 				final int surfaceWidth = this.mCamera.getSurfaceWidth();
 				final int surfaceHeight = this.mCamera.getSurfaceHeight();
 
 				this.mRenderTexture = new RenderTexture(this.getTextureManager(), surfaceWidth, surfaceHeight);
 				this.mRenderTexture.init(pGLState);
 
 				final ITextureRegion renderTextureTextureRegion = TextureRegionFactory
 						.extractFromTexture(this.mRenderTexture);
 				this.mRenderTextureSprite = new UncoloredSprite(0, 0, renderTextureTextureRegion,
 						this.getVertexBufferObjectManager()) {
 					@Override
 					protected void preDraw(final GLState pGLState, final Camera pCamera) {
						if (android.os.Build.MODEL.contains("Nexus 7")) {
 							this.setShaderProgram(PositionTextureCoordinatesShaderProgram.getInstance());
 						} else {
 							this.setShaderProgram(Blur.RadialBlurShaderProgram.getInstance(stats.drunkness));
 						}
 						super.preDraw(pGLState, pCamera);
 
 						GLES20.glUniform2f(Blur.RadialBlurShaderProgram.sUniformRadialBlurCenterLocation,
 								Blur.mRadialBlurCenterX, 1 - Blur.mRadialBlurCenterY);
 					}
 				};
 			}
 		};
 
 	}
 
 	@Override
 	public void onCreateResources() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
 		player.createResources(this);
 
 		this.backgroundTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 2048, 2048);
 		this.backgroundTextureGround = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 				this.backgroundTextureAtlas, this, "background_texture_ground.png", 0, 0);
 		this.backgroundTextureSky = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.backgroundTextureAtlas,
 				this, "background_texture_sky.png", 0, 200);
 		this.backgroundTextureCity = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 				this.backgroundTextureAtlas, this, "background_texture_city.png", 0, 920);
 		this.backgroundTextureAtlas.load();
 
 		this.controlTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024);
 		this.horizontalControlTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 				this.controlTextureAtlas, this, "touchscreen_horizontal_control.png", 0, 0);
 		this.jumpControlTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.controlTextureAtlas,
 				this, "touchscreen_button_jump.png", 0, 190);
 		this.fireControlTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.controlTextureAtlas,
 				this, "touchscreen_button_fire.png", 210, 190);
 		this.controlTextureAtlas.load();
 
 		MusicFactory.setAssetBasePath("mfx/");
 		try {
 			this.mMusic = MusicFactory
 					.createMusicFromAsset(this.mEngine.getMusicManager(), this, "shortTsarpfSong.ogg");
 			this.mMusic.setLooping(true);
 		} catch (final IOException e) {
 			Debug.e(e);
 		}
 
 		this.getShaderProgramManager().loadShaderProgram(Blur.RadialBlurShaderProgram.getInstance(0));
 
 	}
 
 	boolean firstRun = true;
 
 	@Override
 	public Scene onCreateScene() {
 		this.main = this;
 
 		IUpdateHandler iUpdate = new IUpdateHandler() {
 			@Override
 			public void onUpdate(float pSecondsElapsed) {
 				if (firstRun) {
 					mMusic.play();
 					firstRun = false;
 				}
 
 				float rightmostEnemyX = 0;
 				Set<Enemy> enemiesToBeDeleted = new HashSet<Enemy>();
 				for (Enemy enemy : enemies) {
 					if (enemy.animatedSprite.getX() > rightmostEnemyX) {
 						rightmostEnemyX = enemy.animatedSprite.getX();
 					}
 					if (enemy.animatedSprite.getX() + 2000 < player.animatedSprite.getX()) {
 						enemiesToBeDeleted.add(enemy);
 						continue;
 					}
 
 					if (!enemy.dead) {
 						enemy.getPhysicsBody().setLinearVelocity(new Vector2(-1, 0));
 
 						if (random.nextInt(80) == 0) {
 							new BeerProjectile(main, world, enemy.getAnimatedSprite(), false, false);
 						}
 					}
 				}
 
 				for (Enemy enemy : enemiesToBeDeleted) {
 					enemies.remove(enemy);
 					scene.detachChild(enemy.animatedSprite);
 					world.destroyBody(enemy.body);
 				}
 
 				if (rightmostEnemyX + 500 < player.animatedSprite.getX()) {
 					enemies.add(new Enemy(main));
 				}
 
 				synchronized (beersToBeRemoved) {
 					while (!beersToBeRemoved.isEmpty()) {
 						BeerProjectile beer = beersToBeRemoved.pollFirst();
 						if (beer == null)
 							continue;
 						scene.detachChild(beer.animatedSprite);
 						world.destroyBody(beer.body);
 					}
 				}
 
 				synchronized (spearsToBeRemoved) {
 					while (!spearsToBeRemoved.isEmpty()) {
 						SpearProjectile spear = spearsToBeRemoved.pollFirst();
 						if (spear == null)
 							continue;
 						scene.detachChild(spear.sprite);
 						world.destroyBody(spear.body);
 					}
 				}
 
 				if (tree.x + 1000 < player.animatedSprite.getX()) {
 					tree.setX(tree.x + 2000);
 				} else if (tree.x - 1000 > player.animatedSprite.getX()) {
 					tree.setX(tree.x - 2000);
 				}
 
 				if (fuhrer.x + 5250 < player.animatedSprite.getX()) {
 					fuhrer.setX(fuhrer.x + 10500);
 				} else if (fuhrer.x - 5250 > player.animatedSprite.getX()) {
 					fuhrer.setX(fuhrer.x - 10500);
 				}
 			}
 
 			@Override
 			public void reset() {
 
 			}
 
 		};
 
 		this.mEngine.registerUpdateHandler(iUpdate);
 
 		scene = new Scene();
 		autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 0);
 		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
 		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(0.0f, new Sprite(0, CAMERA_HEIGHT
 				- this.backgroundTextureSky.getHeight(), this.backgroundTextureSky, vertexBufferObjectManager)));
 		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f, new Sprite(0, 300,
 				this.backgroundTextureCity, vertexBufferObjectManager)));
 		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f, new Sprite(0, CAMERA_HEIGHT
 				- this.backgroundTextureGround.getHeight(), this.backgroundTextureGround, vertexBufferObjectManager)));
 		scene.setBackground(autoParallaxBackground);
 
 		tree = new Tree(main, 400);
 		fuhrer = new Fuhrer(main, 3000);
 		
 		
 
 		createControllers();
 
 		this.world = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
 
 		player.createScene(vertexBufferObjectManager, CAMERA_WIDTH, CAMERA_HEIGHT, world);
 
 		scene.attachChild(player.animatedSprite);
 
 		enemies.add(new Enemy(main));
 
 		camera.setChaseEntity(player.animatedSprite);
 		camera.setCenter(camera.getCenterX(), camera.getCenterY() - 200);
 
 		final Rectangle ground = new Rectangle(-99999, 327, 99999999, 10, vertexBufferObjectManager);
 		ground.setColor(0, 0, 0, 0);
 
 		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
 
 		PhysicsFactory.createBoxBody(this.world, ground, BodyType.StaticBody, wallFixtureDef);
 
 		scene.attachChild(ground);
 
 		scene.registerUpdateHandler(this.world);
 
 		new Pie(main);
 
 		boss = new Boss(main);
 
 		world.setContactListener(new ContactListener() {
 
 			@Override
 			public void beginContact(Contact pContact) {
 				String userDataA = (String) pContact.getFixtureA().getBody().getUserData();
 				String userDataB = (String) pContact.getFixtureB().getBody().getUserData();
 
 				if (userDataA == null && userDataB == null) {
 					return;
 				}
 
 				if ("beer".equals(userDataA) || "beer".equals(userDataB)) {
 					BeerProjectile beer;
 					if ("beer".equals(userDataA)) {
 						beer = findBeerByBody(pContact.getFixtureA().getBody());
 					} else {
 						beer = findBeerByBody(pContact.getFixtureB().getBody());
 					}
 
 					if (beer == null) {
 						return;
 					}
 
 					if ("player".equals(userDataA) || "player".equals(userDataB)) {
 						stats.drinkBeer();
 					} else {
 						// riko
 					}
 					beer.destroy();
 				}
 
 				if ("spear".equals(userDataA) || "spear".equals(userDataB)) {
 					SpearProjectile Spear;
 					if ("spear".equals(userDataA)) {
 						Spear = findSpearByBody(pContact.getFixtureA().getBody());
 					} else {
 						Spear = findSpearByBody(pContact.getFixtureB().getBody());
 					}
 
 					if (Spear == null) {
 						return;
 					}
 
 					if ("enemy".equals(userDataA) || "enemy".equals(userDataB)) {
 						Enemy enemy;
 						if ("enemy".equals(userDataA)) {
 							enemy = findEnemyByBody(pContact.getFixtureA().getBody());
 						} else {
 							enemy = findEnemyByBody(pContact.getFixtureB().getBody());
 						}
 						if (enemy != null) {
 							enemy.die();
 						}
 					} else {
 						// riko
 					}
 					Spear.destroy();
 				}
 			}
 
 			@Override
 			public void endContact(Contact contact) {
 
 			}
 
 			@Override
 			public void preSolve(Contact contact, Manifold oldManifold) {
 
 			}
 
 			@Override
 			public void postSolve(Contact contact, ContactImpulse impulse) {
 
 			}
 
 		});
 
 		return scene;
 	}
 
 	private void createControllers() {
 		yourHud = new HUD();
 		stats = new Stats(yourHud, this.camera);
 		stats.createResources(this);
 		stats.createScene(this.getVertexBufferObjectManager());
 
 		player.stats = stats;
 
 		final int xSize = 500;
 		final int ySize = 300;
 
 		final Sprite horizontalControl = new Sprite(0, 400, xSize, ySize, horizontalControlTexture,
 				this.getVertexBufferObjectManager()) {
 			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 				float playerSpeed = 0;
 				if (!touchEvent.isActionUp()) {
 					if (X < (xSize / 2)) {
 						playerSpeed = (xSize / 2) - X;
 						playerSpeed = -playerSpeed;
 					} else {
 						playerSpeed = (X - xSize / 2) + 100;
 					}
 				}
 				if (xSize - X < 100 || Y < 80) {
 					playerSpeed = 0;
 				}
 				playerSpeed *= 0.75;
 				autoParallaxBackground.setParallaxChangePerSecond(playerSpeed / 5);
 				player.run(playerSpeed);
 				return true;
 			};
 		};
 		yourHud.registerTouchArea(horizontalControl);
 		yourHud.attachChild(horizontalControl);
 
 		final Sprite jumpControl = new Sprite(1070, 300, jumpControlTexture, this.getVertexBufferObjectManager()) {
 			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 				player.jump();
 				return true;
 			};
 		};
 		yourHud.registerTouchArea(jumpControl);
 		yourHud.attachChild(jumpControl);
 
 		final Sprite fireControl = new Sprite(1070, 510, fireControlTexture, this.getVertexBufferObjectManager()) {
 			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 				if (touchEvent.isActionDown()) {
 					new SpearProjectile(main, world, player.animatedSprite, player.facingRight);
 				}
 				return true;
 			};
 		};
 		yourHud.registerTouchArea(fireControl);
 		yourHud.attachChild(fireControl);
 		this.camera.setHUD(yourHud);
 	}
 
 	@Override
 	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
 		if (pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
 			if (mEngine.isRunning()) {
 				mEngine.stop();
 				mMusic.pause();
 			} else if(!playerDead){
 				mEngine.start();
 				mMusic.play();
 			}
 			return true;
 		} else {
 			return super.onKeyDown(pKeyCode, pEvent);
 		}
 	}
 
 	private BeerProjectile findBeerByBody(Body body) {
 		for (BeerProjectile beer : beers) {
 			if (body.equals(beer.body))
 				return beer;
 		}
 		return null;
 	}
 
 	private Enemy findEnemyByBody(Body body) {
 		for (Enemy enemy : enemies) {
 			if (body.equals(enemy.body))
 				return enemy;
 		}
 		return null;
 	}
 
 	private SpearProjectile findSpearByBody(Body body) {
 		for (SpearProjectile spear : spears) {
 			if (body.equals(spear.body))
 				return spear;
 		}
 		return null;
 	}
 	
 	public void playerDeath(){
 		playerDead = true;
 		stats.drunkness = 0;
 		deathScreen = new DeathScreen(main, yourHud);
 		
 		mEngine.stop();
 		
 	}
 	
 }
