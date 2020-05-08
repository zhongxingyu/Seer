 package com.gemserk.games.facehunt.gamestates;
 
 import java.util.ArrayList;
 
 import com.artemis.Entity;
 import com.artemis.World;
 import com.artemis.utils.ImmutableBag;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.gemserk.analytics.Analytics;
 import com.gemserk.animation4j.transitions.Transition;
 import com.gemserk.animation4j.transitions.Transitions;
 import com.gemserk.animation4j.transitions.event.TransitionEventHandler;
 import com.gemserk.animation4j.transitions.sync.Synchronizers;
 import com.gemserk.commons.artemis.WorldWrapper;
 import com.gemserk.commons.artemis.components.TimerComponent;
 import com.gemserk.commons.artemis.systems.HitDetectionSystem;
 import com.gemserk.commons.artemis.systems.MovementSystem;
 import com.gemserk.commons.artemis.systems.PhysicsSystem;
 import com.gemserk.commons.artemis.systems.RenderLayer;
 import com.gemserk.commons.artemis.systems.ScriptSystem;
 import com.gemserk.commons.artemis.systems.SpriteRendererSystem;
 import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
 import com.gemserk.commons.artemis.systems.TimerSystem;
 import com.gemserk.commons.artemis.triggers.AbstractTrigger;
 import com.gemserk.commons.artemis.triggers.Trigger;
 import com.gemserk.commons.gdx.GameStateImpl;
 import com.gemserk.commons.gdx.box2d.BodyBuilder;
 import com.gemserk.commons.gdx.camera.Camera;
 import com.gemserk.commons.gdx.camera.CameraImpl;
 import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
 import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
 import com.gemserk.commons.gdx.games.Spatial;
 import com.gemserk.commons.gdx.games.SpatialImpl;
 import com.gemserk.commons.gdx.graphics.SpriteBatchUtils;
 import com.gemserk.commons.gdx.input.LibgdxPointer;
 import com.gemserk.commons.gdx.sounds.SoundPlayer;
 import com.gemserk.componentsengine.utils.Container;
 import com.gemserk.games.facehunt.EnemySpawnInfo;
 import com.gemserk.games.facehunt.FaceHuntGame;
 import com.gemserk.games.facehunt.Groups;
 import com.gemserk.games.facehunt.Spawner;
 import com.gemserk.games.facehunt.components.HealthComponent;
 import com.gemserk.games.facehunt.components.PointsComponent;
 import com.gemserk.games.facehunt.controllers.FaceHuntController;
 import com.gemserk.games.facehunt.controllers.FaceHuntControllerImpl;
 import com.gemserk.games.facehunt.entities.Templates;
 import com.gemserk.games.facehunt.systems.DamagePlayerSystem;
 import com.gemserk.games.facehunt.systems.FaceHuntControllerSystem;
 import com.gemserk.games.facehunt.systems.RandomMovementBehaviorSystem;
 import com.gemserk.games.facehunt.values.GameData;
 import com.gemserk.resources.ResourceManager;
 import com.gemserk.resources.ResourceManagerImpl;
 
 public class TutorialModeGameState extends GameStateImpl {
 
 	static class Wave {
 
 		public String[] texts;
 
 		EnemySpawnInfo[] types;
 
 	}
 
 	enum InternalGameState {
 		INTRO, PLAYING, PREPARE_INTRO
 	}
 
 	private final FaceHuntGame game;
 
 	private ResourceManager<String> resourceManager;
 
 	private SpriteBatch spriteBatch;
 
 	private Libgdx2dCameraTransformImpl worldCamera = new Libgdx2dCameraTransformImpl();
 
 	private Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();
 
 	private Camera cameraData;
 
 	private WorldWrapper worldWrapper;
 
 	private World world;
 
 	private com.badlogic.gdx.physics.box2d.World physicsWorld;
 
 	private BodyBuilder bodyBuilder;
 
 	private Box2DDebugRenderer box2dDebugRenderer;
 
 	private FaceHuntController controller;
 
 	private GameData gameData;
 
 	private Color waveIntroductionColor = new Color();
 
 	private Wave[] waves;
 
 	private int currentWaveIndex;
 
 	private int currentTextIndex;
 
 	private Wave currentWave;
 
 	private Spawner spawner;
 
 	private String currentText;
 
 	InternalGameState internalGameState;
 
 	private Templates templates;
 
 	private Entity player;
 
 	private Sprite whiteRectangle;
 
 	private SoundPlayer soundPlayer;
 
 	private int viewportWidth;
 
 	private int viewportHeight;
 
 	private BitmapFont tutorialFont;
 
 	public void setSoundPlayer(SoundPlayer soundPlayer) {
 		this.soundPlayer = soundPlayer;
 	}
 
 	public TutorialModeGameState(FaceHuntGame game) {
 		this.game = game;
 		this.gameData = new GameData();
 	}
 
 	public void restartGame() {
 		
 		Analytics.traker.trackPageView("/startTutorial", "/startTutorial", null);
 
 		gameData.gameOver = false;
 
 		viewportWidth = Gdx.graphics.getWidth();
 		viewportHeight = Gdx.graphics.getHeight();
 
 		worldCamera.center(0f, 0f);
 
 		cameraData = new CameraImpl(0f, 0f, 64f, 0f);
 
 		gameData.points = 0;
 
 		resourceManager = new ResourceManagerImpl<String>();
 
 		new GameResourceBuilder(resourceManager);
 
 		spriteBatch = new SpriteBatch();
 
 		tutorialFont = resourceManager.getResourceValue("TutorialFont");
 		tutorialFont.setScale(1f * viewportWidth / 800f);
 
 		ArrayList<RenderLayer> renderLayers = new ArrayList<RenderLayer>();
 		renderLayers.add(new RenderLayer(-1000, -100, backgroundLayerCamera));
 		renderLayers.add(new RenderLayer(-100, 100, worldCamera));
 
 		controller = new FaceHuntControllerImpl(new LibgdxPointer(0, worldCamera), new LibgdxPointer(1, worldCamera));
 
 		physicsWorld = new com.badlogic.gdx.physics.box2d.World(new Vector2(0f, 0f), false);
 		bodyBuilder = new BodyBuilder(physicsWorld);
 		box2dDebugRenderer = new Box2DDebugRenderer();
 
 		world = new World();
 
 		worldWrapper = new WorldWrapper(world);
 		worldWrapper.addRenderSystem(new SpriteUpdateSystem());
 		worldWrapper.addRenderSystem(new SpriteRendererSystem(renderLayers));
 		worldWrapper.addUpdateSystem(new PhysicsSystem(physicsWorld));
 		worldWrapper.addUpdateSystem(new HitDetectionSystem());
 		worldWrapper.addUpdateSystem(new TimerSystem());
 		worldWrapper.addUpdateSystem(new MovementSystem());
 		worldWrapper.addUpdateSystem(new FaceHuntControllerSystem());
 		worldWrapper.addUpdateSystem(new RandomMovementBehaviorSystem());
 		worldWrapper.addUpdateSystem(new DamagePlayerSystem());
 		worldWrapper.addUpdateSystem(new ScriptSystem());
 		worldWrapper.init();
 
 		templates = new Templates(world, bodyBuilder, resourceManager);
 
 		float worldWidth = viewportWidth * 1 / cameraData.getZoom();
 		float worldHeight = viewportHeight * 1 / cameraData.getZoom();
 
 		templates.createBorder((worldWidth * 0.5f), 0, worldWidth, 0.1f);
 		templates.createBorder((worldWidth * 0.5f), worldHeight, worldWidth, 1f);
 		templates.createBorder(0, (worldHeight * 0.5f), 0.1f, worldHeight);
 		templates.createBorder(worldWidth, (worldHeight * 0.5f), 0.1f, worldHeight);
 
 		Sprite backgroundSprite = resourceManager.getResourceValue("BackgroundSprite");
 		whiteRectangle = resourceManager.getResourceValue("OverlaySprite");
 
 		templates.createStaticSprite(backgroundSprite, 0f, 0f, viewportWidth, viewportHeight, 0f, (-101), 0f, 0f, Color.WHITE);
 
 		createFirstTypeFaceSpawner(new Rectangle(1, 1, worldWidth - 2, worldHeight - 2));
 
 		player = world.createEntity();
 		player.setTag("Player");
 		player.addComponent(new HealthComponent(new Container(100f, 100f), 0.5f));
 		player.refresh();
 
 		world.loopStart();
 
 		currentWaveIndex = 0;
 		currentTextIndex = 0;
 		currentText = "";
 
 		internalGameState = InternalGameState.PREPARE_INTRO;
 
 		waves = new Wave[] { new Wave() {
 			{
 				texts = new String[] { "Welcome to Face Hunt.\nHunt as many faces as you can\n by touching over them.", //
 						"Kill them before they kill you\n while they laugh.", "Let's begin practice, kill 15 yellow faces..." };
 				types = new EnemySpawnInfo[] { new EnemySpawnInfo(0, 15, 1f) };
 			}
 		}, new Wave() {
 			{
 				texts = new String[] { "Nicely done but don't celebrate yet,\nmore faces are coming!", "Some of them are just too fast...\nKill 10 green faces." };
 				types = new EnemySpawnInfo[] { new EnemySpawnInfo(1, 10, 1f), };
 			}
 		}, new Wave() {
 			{
 				texts = new String[] { "Some of them just don't want to die...", "Kill 10 red faces, wait for them to be yellow\n when they are vulnerable" };
 				types = new EnemySpawnInfo[] { new EnemySpawnInfo(2, 10, 1f), };
 			}
 		}, new Wave() {
 			{
 				texts = new String[] { "There are also friendly whity faces\nwhich will recover your life.", "However, do not kill them,\n else they will take revenge." };
 				types = new EnemySpawnInfo[] { new EnemySpawnInfo(3, 4, 1f) };
 			}
 		}, new Wave() {
 			{
 				texts = new String[] { "It seems like someone\n has improved their skills.", "Well done, now you will have\n to face a real challenge..." };
 				types = new EnemySpawnInfo[] { new EnemySpawnInfo(0, 0, 1f) };
 			}
 		}, };
 
 	}
 
 	BodyBuilder getBodyBuilder() {
 		return bodyBuilder;
 	}
 
 	void createFirstTypeFaceSpawner(final Rectangle spawnArea) {
 		Entity entity = world.createEntity();
 		final int minTime = 1000;
 		final int maxTime = 2000;
 		entity.addComponent(new TimerComponent(MathUtils.random(minTime, maxTime), new AbstractTrigger() {
 			@Override
 			public boolean handle(Entity e) {
 
 				if (spawner.isEmpty())
 					return false;
 
 				float probability = MathUtils.random(1f);
 				int type = spawner.getType(probability);
 
 				spawner.remove(type, 1);
 
 				TimerComponent timerComponent = e.getComponent(TimerComponent.class);
 				timerComponent.reset();
 				timerComponent.setCurrentTime(MathUtils.random(minTime, maxTime));
 
 				float x = MathUtils.random(spawnArea.x, spawnArea.width);
 				float y = MathUtils.random(spawnArea.y, spawnArea.height);
 
 				float angularVelocity = MathUtils.random(30f, 180f);
 
 				if (MathUtils.randomBoolean())
 					angularVelocity = -angularVelocity;
 
 				Vector2 linearVelocity = new Vector2(0f, 0f);
 				linearVelocity.x = MathUtils.random(1f, 4f);
 				linearVelocity.rotate(MathUtils.random(0f, 360f));
 
 				Sprite sprite = resourceManager.getResourceValue("HappyFaceSprite");
 
 				Spatial spatial = new SpatialImpl(x, y, 1f, 1f, 0f);
 
 				if (type == 0)
 					templates.createFaceFirstType(spatial, sprite, controller, linearVelocity, angularVelocity, new Color(1f, 1f, 0f, 1f), getFaceHitTrigger(), getFaceDeadHandler());
 				else if (type == 1)
 					templates.createFaceSecondType(spatial, sprite, controller, linearVelocity, angularVelocity, getFaceHitTrigger(), getFaceDeadHandler());
 				else if (type == 2)
 					templates.createFaceInvulnerableType(spatial, sprite, controller, linearVelocity, angularVelocity, getFaceHitTrigger(), getFaceDeadHandler());
 				else if (type == 3)
 					templates.createMedicFaceType(spatial, sprite, controller, linearVelocity, angularVelocity, getFaceHitTrigger(), getMedicFaceTouchTrigger());
 
 				Sound sound = resourceManager.getResourceValue("CritterSpawnedSound");
 				soundPlayer.play(sound);
				// sound.play();
 
 				return false;
 			}
 		}));
 		entity.refresh();
 	}
 
 	private Trigger getFaceDeadHandler() {
 		return new AbstractTrigger() {
 			@Override
 			protected boolean handle(Entity e) {
 				PointsComponent pointsComponent = e.getComponent(PointsComponent.class);
 				
 				if (pointsComponent != null) 
 					gameData.points += pointsComponent.getPoints();
 
 				HealthComponent healthComponent = player.getComponent(HealthComponent.class);
 				Container health = healthComponent.getHealth();
 				health.add(5f);
 				
 				Sound sound = resourceManager.getResourceValue("CritterKilledSound");
 				soundPlayer.play(sound);
 
 				return true;
 			}
 		};
 	}
 
 	private Trigger getMedicFaceTouchTrigger() {
 		return new AbstractTrigger() {
 			@Override
 			protected boolean handle(Entity e) {
 				HealthComponent healthComponent = player.getComponent(HealthComponent.class);
 				Container health = healthComponent.getHealth();
 				health.remove(20f);
 				Sound sound = resourceManager.getResourceValue("CritterKilledSound");
				sound.play();
 				return true;
 			}
 		};
 	}
 
 	private Trigger getFaceHitTrigger() {
 		return new AbstractTrigger() {
 			@Override
 			protected boolean handle(Entity e) {
 				Sound sound = resourceManager.getResourceValue("CritterBounceSound");
 				soundPlayer.play(sound);
				// sound.play();

 				return false;
 			}
 		};
 	}
 
 	@Override
 	public void render(int delta) {
 		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
 		worldCamera.zoom(cameraData.getZoom());
 		worldCamera.move(cameraData.getX(), cameraData.getY());
 		worldCamera.rotate(cameraData.getAngle());
 		worldWrapper.render();
 
 		if (Gdx.input.isKeyPressed(Keys.D))
 			box2dDebugRenderer.render(physicsWorld);
 
 		spriteBatch.begin();
 
 		HealthComponent healthComponent = player.getComponent(HealthComponent.class);
 		Container health = healthComponent.getHealth();
 		FaceHuntRenderUtils.renderBar(spriteBatch, whiteRectangle, health, (Gdx.graphics.getWidth() * 0.2f), (Gdx.graphics.getHeight() - 25), (Gdx.graphics.getWidth() * 0.6f), 10f);
 
 		if (currentWave != null) {
 			tutorialFont.setColor(waveIntroductionColor);
 
 			TextBounds bounds = tutorialFont.getMultiLineBounds(currentText);
 			float scale = SpriteBatchUtils.calculateScaleForText(viewportWidth, bounds.width, 0.8f);
 
 			tutorialFont.setScale(scale);
 			SpriteBatchUtils.drawMultilineTextCentered(spriteBatch, tutorialFont, //
 					currentText, (Gdx.graphics.getWidth() * 0.5f), (Gdx.graphics.getHeight() * 0.5f));
 			SpriteBatchUtils.drawMultilineTextCentered(spriteBatch, tutorialFont, //
 					"tap to continue", (Gdx.graphics.getWidth() * 0.8f), (Gdx.graphics.getHeight() * 0.1f));
 			tutorialFont.setScale(1f);
 		}
 
 		spriteBatch.end();
 	}
 
 	@Override
 	public void update(int delta) {
 		Synchronizers.synchronize(delta);
 
 		controller.update(delta);
 
 		if (internalGameState == InternalGameState.PLAYING) {
 			worldWrapper.update(delta);
 
 			HealthComponent healthComponent = player.getComponent(HealthComponent.class);
 			Container health = healthComponent.getHealth();
 
 			if (health.isEmpty()) {
 				gameData.gameOver = true;
 				game.pauseGameState.setPreviousScreen(game.tutorialScreen);
 				game.transition(game.pauseScreen);
 			}
 
 			ImmutableBag<Entity> faces = world.getGroupManager().getEntities(Groups.FaceGroup);
 
 			// for now, allow N to process next state...
 			if (spawner.isEmpty() && faces.isEmpty()) {
 				// if last wave, then game is over...
 
 				if (currentWaveIndex == waves.length - 1) {
 					gameData.gameOver = true;
 					game.transition(game.gameScreen, true);
 					
 					Analytics.traker.trackPageView("/finishTutorial", "/finishTutorial", null);
 					
 				} else {
 					internalGameState = InternalGameState.PREPARE_INTRO;
 					currentWaveIndex++;
 				}
 			}
 
 			if (Gdx.input.isKeyPressed(Keys.N)) {
 				internalGameState = InternalGameState.PREPARE_INTRO;
 				currentWaveIndex++;
 			}
 
 		}
 
 		if (internalGameState == InternalGameState.INTRO) {
 
 			if (Gdx.input.justTouched()) {
 				currentTextIndex++;
 				if (currentTextIndex < currentWave.texts.length)
 					currentText = currentWave.texts[currentTextIndex];
 			}
 
 			if (currentTextIndex >= currentWave.texts.length) {
 				internalGameState = InternalGameState.PLAYING;
 
 				Color endColor = new Color(Color.BLUE);
 				endColor.a = 0f;
 
 				Synchronizers.transition(waveIntroductionColor, Transitions.transitionBuilder().time(800).end(endColor));
 			}
 
 		}
 
 		if (internalGameState == InternalGameState.PREPARE_INTRO) {
 			Color endColor = new Color(Color.BLUE);
 			endColor.a = 1f;
 			waveIntroductionColor.a = 0f;
 
 			currentTextIndex = 0;
 
 			if (currentWaveIndex >= waves.length) {
 				game.transition(game.pauseScreen, true);
 				return;
 			}
 
 			currentWave = waves[currentWaveIndex];
 			currentText = currentWave.texts[currentTextIndex];
 
 			spawner = new Spawner(currentWave.types);
 
 			Synchronizers.transition(waveIntroductionColor, Transitions.transitionBuilder().time(800).end(endColor), //
 					new TransitionEventHandler() {
 						@Override
 						public void onTransitionFinished(Transition transition) {
 							internalGameState = InternalGameState.INTRO;
 						}
 					});
 
 		}
 
 		if (Gdx.input.isKeyPressed(Keys.BACK) || Gdx.input.isKeyPressed(Keys.ESCAPE)) {
 			game.pauseGameState.setPreviousScreen(game.tutorialScreen);
 			game.transition(game.pauseScreen);
 			Analytics.traker.trackPageView("/tutorialPaused", "/tutorialPaused", null);
 		}
 
 	}
 
 	@Override
 	public void init() {
 		if (gameData.gameOver)
 			restartGame();
 	}
 
 	@Override
 	public void resume() {
 		game.getAdWhirlViewHandler().hide();
 		Gdx.input.setCatchBackKey(true);
 	}
 
 	@Override
 	public void pause() {
 		game.getAdWhirlViewHandler().show();
 		Gdx.input.setCatchBackKey(false);
 	}
 
 	@Override
 	public void dispose() {
 		resourceManager.unloadAll();
 		spriteBatch.dispose();
 		spriteBatch = null;
 		physicsWorld.dispose();
 		gameData.gameOver = true;
 	}
 
 }
