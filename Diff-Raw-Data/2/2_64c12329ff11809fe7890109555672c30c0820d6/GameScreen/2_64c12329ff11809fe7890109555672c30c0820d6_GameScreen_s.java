 package com.gemserk.games.facehunt;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Disposable;
 import com.gemserk.animation4j.gdx.converters.LibgdxConverters;
 import com.gemserk.animation4j.transitions.Transition;
 import com.gemserk.animation4j.transitions.Transitions;
 import com.gemserk.animation4j.transitions.sync.Synchronizers;
 import com.gemserk.commons.gdx.ScreenAdapter;
 import com.gemserk.commons.values.FloatValue;
 import com.gemserk.componentsengine.components.FieldsReflectionComponent;
 import com.gemserk.componentsengine.components.annotations.EntityProperty;
 import com.gemserk.componentsengine.entities.Entity;
 import com.gemserk.componentsengine.properties.Properties;
 import com.gemserk.componentsengine.templates.JavaEntityTemplate;
 import com.gemserk.componentsengine.templates.RegistrableTemplateProvider;
 import com.gemserk.componentsengine.templates.TemplateProvider;
 import com.gemserk.games.facehunt.components.DefaultParametersBuilder;
 import com.gemserk.games.facehunt.components.MovementComponent;
 import com.gemserk.games.facehunt.components.RenderComponent;
 import com.gemserk.games.facehunt.components.SpawnerComponent;
 import com.gemserk.games.facehunt.entities.FaceEntityTemplate;
 import com.gemserk.games.facehunt.entities.FadeAnimationTemplate;
 import com.gemserk.games.facehunt.entities.MoveableEntityTemplate;
 import com.gemserk.games.facehunt.entities.RenderableEntityTemplate;
 import com.gemserk.games.facehunt.entities.SpatialEntityTemplate;
 import com.gemserk.games.facehunt.entities.SpawnerEntityTemplate;
 import com.gemserk.games.facehunt.entities.Tags;
 import com.gemserk.games.facehunt.entities.TouchableEntityTemplate;
 import com.gemserk.games.facehunt.values.GameData;
 import com.gemserk.games.facehunt.values.Movement;
 import com.gemserk.games.facehunt.values.Spatial;
 import com.gemserk.games.facehunt.values.Spawner;
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Provider;
 
 public class GameScreen extends ScreenAdapter {
 
 	private final Game game;
 
 	private Texture background;
 
 	private SpriteBatch spriteBatch;
 
 	private Texture happyFace;
 
 	private Texture sadFace;
 
 	EntityManager entityManager;
 
 	private RegistrableTemplateProvider templateProvider;
 
 	private ArrayList<Disposable> disposables = new ArrayList<Disposable>();
 
 	public GameScreen(Game game) {
 		this.game = game;
 
 		background = new Texture(Gdx.files.internal("data/background01-1024x512.jpg"));
 		happyFace = new Texture(Gdx.files.internal("data/face-sad-64x64.png"));
 		sadFace = new Texture(Gdx.files.internal("data/face-happy-64x64.png"));
 
 		happyFace.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		sadFace.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 
 		heart = new Texture(Gdx.files.internal("data/heart-32x32.png"));
 
 		spriteBatch = new SpriteBatch();
 		bounceSound = Gdx.audio.newSound(Gdx.files.internal("data/bounce.wav"));
 
 		critterKilledSound = Gdx.audio.newSound(Gdx.files.internal("data/critter-killed.wav"));
 		critterSpawnedSound = Gdx.audio.newSound(Gdx.files.internal("data/critter-spawned.wav"));
 
 		disposables.add(spriteBatch);
 		disposables.add(background);
 		disposables.add(happyFace);
 		disposables.add(sadFace);
 		disposables.add(heart);
 		disposables.add(spriteBatch);
 		disposables.add(bounceSound);
 		disposables.add(critterKilledSound);
 		disposables.add(critterSpawnedSound);
 
 		templateProvider = new RegistrableTemplateProvider();
 
 		Injector injector = Guice.createInjector(new AbstractModule() {
 
 			@Override
 			protected void configure() {
 				bind(TemplateProvider.class).toInstance(templateProvider);
 			}
 		});
 
 		entityManager = injector.getInstance(EntityManager.class);
 
 		Provider<JavaEntityTemplate> javaEntityTemplateProvider = injector.getProvider(JavaEntityTemplate.class);
 
 		templateProvider.add("entities.Face", javaEntityTemplateProvider.get().with(new FaceEntityTemplate()));
 		templateProvider.add("FadeAnimation", javaEntityTemplateProvider.get().with(new FadeAnimationTemplate()));
 		templateProvider.add("Spawner", javaEntityTemplateProvider.get().with(new SpawnerEntityTemplate()));
 
 		templateProvider.add("entities.Spatial", javaEntityTemplateProvider.get().with(new SpatialEntityTemplate()));
 		templateProvider.add("entities.Moveable", javaEntityTemplateProvider.get().with(new MoveableEntityTemplate()));
 		templateProvider.add("entities.Renderable", javaEntityTemplateProvider.get().with(new RenderableEntityTemplate()));
 		templateProvider.add("entities.Touchable", javaEntityTemplateProvider.get().with(new TouchableEntityTemplate()));
 
 		// templateProvider.add("entities.Face", javaEntityTemplateProvider.get().with(new FaceEntityTemplate()));
 
 		JavaEntityTemplate javaEntityTemplate = new JavaEntityTemplate();
 		javaEntityTemplate.setInjector(injector);
 
 		startColor = new Color(1f, 1f, 1f, 0f);
 		endColor = new Color(1f, 1f, 1f, 1f);
 		world = new World(new Vector2(0, 0), new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
 
 		Sprite fontSprite = new Sprite(new Texture(Gdx.files.internal("data/font.png")));
 		font = new BitmapFont(Gdx.files.internal("data/font.fnt"), fontSprite, false);
 
 		restartGame();
 	}
 
 	protected void restartGame() {
 
 		entityManager.removeAll();
 
 		entityManager.addEntity(templateProvider.getTemplate("Spawner").instantiate("global.spawner", new HashMap<String, Object>() {
 			{
 				put("respawnTime", new FloatValue(3000f));
 				put("spawner", new Spawner(templateProvider.getTemplate("FadeAnimation"), new HashMap<String, Object>() {
 					{
 						put("image", new Sprite(happyFace));
 						put("startColor", startColor);
 						put("endColor", endColor);
 						put("shouldSpawn", true);
 					}
 				}, new FaceDefaultParametersBuilder(), 10, 1.5f, 1.02f, 6.5f));
 			}
 		}));
 
 		gameData.lives = 2;
 		gameData.killedCritters = 0;
 
 		movementComponent = new MovementComponent("movement", world, bounceSound);
 		renderComponent = new RenderComponent("render");
 		touchableComponent = new TouchableComponent("touchable", entityManager, templateProvider, critterKilledSound, sadFace, gameData);
 		spawnerComponent = new SpawnerComponent("spawner", entityManager, world, critterSpawnedSound);
 
 		identity = new Matrix4().idt();
 
 		font.setColor(0f, 0f, 0f, 0.8f);
 		// font.setScale(1f, 1.5f);
 
 		fadeInColor = Transitions.transition(startColor, LibgdxConverters.color());
 		fadeInColor.set(endColor, 2000);
 
 		gameState = GameState.Starting;
 	}
 
 	Matrix4 identity = new Matrix4();
 
 	enum GameState {
 		Starting, Playing, GameOver
 	}
 
 	GameState gameState = GameState.Starting;
 
 	public static class LibgdxPointer {
 
 		boolean touched = false;
 
 		Vector2 pressedPosition = new Vector2();
 
 		Vector2 releasedPosition = new Vector2();
 
 		public boolean wasPressed;
 
 		public boolean wasReleased;
 
 		public int index;
 		
 		public LibgdxPointer(int index) {
 			this.index = index;
 		}
 
 		public void update() {
 			
 			if (Gdx.input.isTouched(index)) {
 				
 				if (!touched) {
 					touched = true;
 					wasPressed = true;
 					pressedPosition.set(Gdx.input.getX(index), Gdx.graphics.getHeight() - Gdx.input.getY(index));
 				} else 
 					wasPressed = false;
 				
 			} 
 			
			if (Gdx.input.isTouched(index)) {
 				
 				if (touched) {
 					touched = false;
 					wasReleased = true;
 					releasedPosition.set(Gdx.input.getX(index), Gdx.graphics.getHeight() - Gdx.input.getY(index));
 				} else {
 					wasReleased = false;
 				}
 				
 			}
 		}
 
 	}
 
 	LibgdxPointer[] libgdxPointers = { new LibgdxPointer(0), new LibgdxPointer(1), new LibgdxPointer(2), new LibgdxPointer(3), new LibgdxPointer(4) };
 
 	@Override
 	public void render(float delta) {
 		int width = Gdx.graphics.getWidth();
 		int height = Gdx.graphics.getHeight();
 
 		int centerX = width / 2;
 		int centerY = height / 2;
 
 		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		updatePointers();
 
 		if (gameState == GameState.Starting) {
 
 			spriteBatch.setTransformMatrix(identity);
 			spriteBatch.begin();
 			spriteBatch.setColor(fadeInColor.get());
 			spriteBatch.draw(background, 0, 0);
 			spriteBatch.end();
 
 			if (!fadeInColor.isTransitioning())
 				gameState = GameState.Playing;
 
 		} else if (gameState == GameState.Playing) {
 
 			spriteBatch.setTransformMatrix(identity);
 			spriteBatch.begin();
 			spriteBatch.setColor(Color.WHITE);
 			spriteBatch.draw(background, 0, 0);
 			spriteBatch.end();
 
 			ArrayList<Entity> entities = entityManager.getEntities();
 
 			spriteBatch.setTransformMatrix(identity);
 			spriteBatch.begin();
 
 			for (int i = 0; i < entities.size(); i++) {
 				final Entity entity = entities.get(i);
 
 				// make some logic for the entity
 
 				touchableComponent.detectTouchAndKill(entity, delta);
 				movementComponent.update(entity, delta);
 
 				spawnerComponent.update(entity, delta);
 
 				if (entity.hasTag(Tags.ANIMATION)) {
 
 					Color color = Properties.getValue(entity, "color");
 					Color endColor = Properties.getValue(entity, "endColor");
 
 					if (color.equals(endColor)) {
 						Boolean shouldSpawn = Properties.getValue(entity, "shouldSpawn");
 						if (shouldSpawn) {
 
 							final Spatial spatial = Properties.getValue(entity, "spatial");
 							final Movement movement = Properties.getValue(entity, "movement");
 							final FloatValue aliveTime = Properties.getValue(entity, "aliveTime");
 							final Sprite sprite = Properties.getValue(entity, "image");
 
 							entityManager.addEntity(templateProvider.getTemplate("entities.Face").instantiate("touchable." + entity.getId(), new HashMap<String, Object>() {
 								{
 									put("spatial", new Spatial().set(spatial));
 									put("movement", new Movement().set(movement));
 									put("image", sprite);
 									put("aliveTime", aliveTime);
 								}
 							}));
 						}
 						entityManager.remove(entity);
 					}
 
 				}
 
 				if (entity.hasTag(Tags.ALIVE)) {
 
 					FloatValue aliveTime = Properties.getValue(entity, "aliveTime");
 					// Boolean touchable = Properties.getValue(entity, "touchable");
 
 					// aliveTime.value -= 1f * delta;
 
 					if (aliveTime.value <= 0f) {
 						gameData.lives--;
 						entityManager.remove(entity);
 					}
 
 				}
 
 				if (gameData.lives <= 0) {
 					fadeInColor.set(new Color(1f, 1f, 1f, 0f), 2000);
 					gameState = GameState.GameOver;
 				}
 
 				renderComponent.render(entity, spriteBatch);
 			}
 
 			spriteBatch.end();
 
 			spriteBatch.setTransformMatrix(identity);
 			spriteBatch.begin();
 
 			font.setColor(0.2f, 0.2f, 1f, 1f);
 
 			String str = "Smiles: " + gameData.killedCritters;
 			// TextBounds textBounds = font.getBounds(str);
 			font.draw(spriteBatch, str, 10, height - 10);
 
 			// str = "Lives: " + gameData.lives;
 			// textBounds = font.getBounds(str);
 			// font.draw(spriteBatch, str, width - textBounds.width - 10, height - 20);
 
 			int maxLives = 2;
 			int spaceBetweenLives = 5;
 
 			int xStart = width - 10 - (heart.getWidth() + spaceBetweenLives) * maxLives;
 			int y = height - heart.getHeight() - 10;
 
 			for (int i = 0; i < gameData.lives; i++) {
 				int x = xStart + i * (heart.getWidth() + spaceBetweenLives);
 				spriteBatch.draw(heart, x - heart.getWidth() / 2, y - heart.getHeight() / 2);
 			}
 
 			spriteBatch.end();
 
 		} else if (gameState == GameState.GameOver) {
 
 			spriteBatch.setTransformMatrix(identity);
 			spriteBatch.begin();
 			spriteBatch.setColor(fadeInColor.get());
 			spriteBatch.draw(background, 0, 0);
 
 			String str = "Game Over";
 			TextBounds textBounds = font.getBounds(str);
 			font.setColor(1f, 0.3f, 0.3f, 1f);
 			font.draw(spriteBatch, str, centerX - textBounds.width / 2, centerY + textBounds.height / 2 + textBounds.height);
 
 			str = "Smiles: " + gameData.killedCritters + " points";
 			textBounds = font.getBounds(str);
 			font.draw(spriteBatch, str, centerX - textBounds.width / 2, centerY + textBounds.height / 2);
 
 			str = "Tap screen to restart";
 			textBounds = font.getBounds(str);
 			font.draw(spriteBatch, str, centerX - textBounds.width / 2, centerY + textBounds.height / 2 - textBounds.height);
 
 			spriteBatch.end();
 
 			if (!fadeInColor.isTransitioning()) {
 				if (Gdx.input.justTouched())
 					restartGame();
 			}
 
 		}
 
 		Synchronizers.synchronize();
 
 	}
 
 	private void updatePointers() {
 		for (int i = 0; i < libgdxPointers.length; i++) 
 			libgdxPointers[i].update();
 	}
 
 	class FaceDefaultParametersBuilder implements DefaultParametersBuilder {
 
 		private Random random = new Random();
 
 		@Override
 		public Map<String, Object> buildParameters(Map<String, Object> parameters) {
 			// should be outside...
 			Vector2 position = Vector2Random.vector2(world.min.x + 10, world.min.y + 10, world.max.x - 10, world.max.y - 10);
 			Vector2 velocity = Vector2Random.vector2(-1f, -1f, 1f, 1f).mul(100f);
 			float angle = random.nextFloat() * 360;
 
 			// I want this to be dynamic based on the player's performance.
 			float minAliveTime = 3000f;
 			float maxAliveTime = 6000f;
 
 			float aliveTime = minAliveTime + random.nextFloat() * (maxAliveTime - minAliveTime);
 			float angularVelocity = random.nextFloat() * 180f - 90f;
 
 			parameters.put("spatial", new Spatial(position, angle));
 			parameters.put("movement", new Movement(velocity, angularVelocity));
 			parameters.put("aliveTime", new FloatValue(aliveTime));
 			parameters.put("image", new Sprite(happyFace));
 
 			return parameters;
 		}
 	}
 
 	public class TouchableComponent extends FieldsReflectionComponent {
 
 		TemplateProvider templateProvider;
 
 		EntityManager entityManager;
 
 		private final Sound sound;
 
 		private final Texture image;
 
 		Color endColor = new Color(1f, 1f, 1f, 0f);
 
 		private final GameData gameData;
 
 		@EntityProperty(readOnly = true)
 		Spatial spatial;
 
 		@EntityProperty(readOnly = true)
 		FloatValue radius;
 
 		public TouchableComponent(String id, EntityManager entityManager, TemplateProvider templateProvider, Sound sound, Texture image, GameData gameData) {
 			super(id);
 			this.entityManager = entityManager;
 			this.templateProvider = templateProvider;
 			this.sound = sound;
 			this.image = image;
 			this.gameData = gameData;
 		}
 
 		protected void detectTouchAndKill(final Entity entity, float delta) {
 
 			if (!entity.hasTag(Tags.TOUCHABLE))
 				return;
 
 			if (!Gdx.input.justTouched())
 				return;
 
 			super.setEntity(entity);
 			super.preHandleMessage(null);
 			
 			final Spatial spatial = Properties.getValue(entity, "spatial");
 			Vector2 position = spatial.position;
 			
 			for (int i = 0; i < libgdxPointers.length; i++) {
 				
 				LibgdxPointer libgdxPointer = libgdxPointers[i];
 				
 				if (!libgdxPointer.wasPressed) 
 					continue;
 				
 				if (position.dst(libgdxPointer.pressedPosition) > (32f + 5f))
 					continue;
 				
 				sound.play(1f);
 				entityManager.remove(entity);
 				gameData.killedCritters++;
 
 				final Movement movement = Properties.getValue(entity, "movement");
 				final Color color = Properties.getValue(entity, "color");
 
 				movement.angularVelocity = 400f;
 				movement.velocity.set(0, 0);
 
 				Synchronizers.transition(spatial.size, Transitions.transitionBuilder(spatial.size) //
 						.end(new Vector2(0f, 0f)) //
 						.time(700) //
 						.build()); //
 
 				entityManager.addEntity(templateProvider.getTemplate("FadeAnimation").instantiate("animation." + entity.getId(), new HashMap<String, Object>() {
 					{
 						put("spatial", spatial);
 						put("movement", movement);
 						put("image", new Sprite(image));
 						put("startColor", color);
 						put("endColor", endColor);
 					}
 				}));
 				
 				break;
 				
 			}
 			
 //			int x = Gdx.input.getX();
 //			int y = Gdx.graphics.getHeight() - Gdx.input.getY();
 //
 //			if (position.dst(x, y) > 35f)
 //				return;
 
 			super.postHandleMessage(null);
 
 		}
 
 	}
 
 	RenderComponent renderComponent;
 
 	MovementComponent movementComponent;
 
 	private TouchableComponent touchableComponent;
 
 	private SpawnerComponent spawnerComponent;
 
 	private Sound bounceSound;
 
 	private GameData gameData = new GameData();
 
 	private BitmapFont font;
 
 	private Transition<Color> fadeInColor;
 
 	private Sound critterKilledSound;
 
 	private Sound critterSpawnedSound;
 
 	private final Color startColor;
 
 	private final Color endColor;
 
 	private final World world;
 
 	private Texture heart;
 
 	@Override
 	public void show() {
 
 	}
 
 	@Override
 	public void dispose() {
 		for (Disposable disposable : disposables)
 			disposable.dispose();
 	}
 
 }
