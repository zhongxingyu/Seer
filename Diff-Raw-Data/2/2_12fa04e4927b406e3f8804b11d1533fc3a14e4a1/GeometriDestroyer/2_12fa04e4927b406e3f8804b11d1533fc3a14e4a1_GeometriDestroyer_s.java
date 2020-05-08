 package se.danielj.geometridestroyer;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import se.danielj.geometridestroyer.misc.SpriteManager;
 import se.danielj.geometridestroyer.misc.SpriteManager.Sprites;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactImpulse;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.Manifold;
 import com.badlogic.gdx.physics.box2d.QueryCallback;
 import com.badlogic.gdx.physics.box2d.World;
 
 public class GeometriDestroyer implements Screen, InputProcessor {
 	
 	private OrthographicCamera camera;
 	private SpriteBatch batch;
 	private World world;
 	private IngameStage stage;
 	
 	private Core core;
 	
 	private boolean gameRunning = true;
 	
 	public int boxesLeft;
 	
 	private VictoryChecker victoryChecker;
 	
 	private int currentLevel;
 	
 	public static final int numberOfLevels = 14;
 	
 	private Renderer normalRenderer;
 	private Renderer pauseRenderer;
 	private Renderer renderer;
 	
 	public GeometriDestroyer(Core core, InputMultiplexer inputMultiplexer) {
 		this.core = core;
 		
 		camera = new OrthographicCamera(Constants.WIDTH, Constants.HEIGHT);
 		camera.position.set(Constants.WIDTH / 2, Constants.HEIGHT / 2, 0);
 		camera.update();
 		batch = new SpriteBatch();
 		
 		world = new World(new Vector2(0, -40), true); 
 		
 		stage = new IngameStage(core, world, this);
 		inputMultiplexer.addProcessor(this);
 		inputMultiplexer.addProcessor(stage);
 		
 		world.setContactListener(new ContactListener() {
 			@Override
 			public void beginContact(Contact contact) {
 				Object a = contact.getFixtureA().getBody().getUserData();
 				Object b = contact.getFixtureB().getBody().getUserData();
 				if (a instanceof Entity && b instanceof Entity) {
 					return;
 				}
 				if ((a instanceof Entity && ((Entity)a).isPlayer()) || (b instanceof Entity && ((Entity)b).isPlayer())) {
 					gameOver();
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
 		
 		victoryChecker = new NullVictoryChecker();
 		
 		pauseRenderer = new Renderer() {
 			@Override
 			public void render() {
 				Gdx.gl.glClearColor(1, 1, 1, 1);
 				Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 				
 				batch.setProjectionMatrix(camera.combined);
 				batch.begin();
 				batch.draw(SpriteManager.getSprite(Sprites.BACKGROUND), 0, 0, Constants.WIDTH, Constants.HEIGHT);
 				Iterator<Body> i = world.getBodies();
 				while (i.hasNext()) {
 					// Draw entities
 					Body body = i.next();
 					if (body.getUserData() instanceof Entity) {
 						Entity entity = (Entity) body.getUserData();
 						entity.draw(batch, body);
 					}
 				}
 				batch.end();
 				
 				// Check if victory
 				victoryChecker.check();
 				
 				stage.act();
 				stage.draw();
 			}
 		};
 		
 		normalRenderer = new Renderer() {
 			@Override
 			public void render() {
 				world.step(1/60f, 6, 2);
 				
 				Gdx.gl.glClearColor(1, 1, 1, 1);
 				Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 				
 				batch.setProjectionMatrix(camera.combined);
 				batch.begin();
 				batch.draw(SpriteManager.getSprite(Sprites.BACKGROUND), 0, 0, Constants.WIDTH, Constants.HEIGHT);
 				Iterator<Body> i = world.getBodies();
 				while (i.hasNext()) {
 					// Draw entities
 					Body body = i.next();
 					if (body.getUserData() instanceof Entity) {
 						Entity entity = (Entity) body.getUserData();
 						entity.draw(batch, body);
 					}
 				}
 				batch.end();
 				
 				// Check if victory
 				victoryChecker.check();
 				
 				stage.act();
 				stage.draw();
 			}
 		};
 		
 		renderer = normalRenderer;
 	}
 	
 	public void setLevel(int level) {
 		this.currentLevel = level;
 		{
 			Iterator<Body> i = world.getBodies();
 			List<Body> bodies = new LinkedList<Body>();
 			while (i.hasNext()) {
 				bodies.add(i.next());
 			}
 			for (Body body : bodies) {
 				world.destroyBody(body);
 			}
 		}
 		EntityCreator.createFloor(world);
 		switch (level) {
 		case 1:
 			EntityCreator.createSteelBox(world, 40, 5, 10, 10);
 			EntityCreator.createDestroyableBox(world, 40, 15, 10, 10);
 			EntityCreator.createSteelBox(world, 40, 25, 10, 10);
 			EntityCreator.createPlayerBox(world, 40, 32.5f, 5, 5);
 			boxesLeft = 1;
 			break;
 		case 2:
 			EntityCreator.createDestroyableBox(world, 25, 5, 10, 10);
 			EntityCreator.createDestroyableBox(world, 35, 5, 10, 10);
 			EntityCreator.createDestroyableBox(world, 45, 5, 10, 10);
 			EntityCreator.createDestroyableBox(world, 55, 5, 10, 10);
 			EntityCreator.createDestroyableBox(world, 30, 15, 10, 10);
 			EntityCreator.createDestroyableBox(world, 40, 15, 10, 10);
 			EntityCreator.createDestroyableBox(world, 50, 15, 10, 10);
 			EntityCreator.createDestroyableBox(world, 35, 25, 10, 10);
 			EntityCreator.createDestroyableBox(world, 45, 25, 10, 10);
 			EntityCreator.createPlayerBox(world, 40, 35, 10, 10);
 			boxesLeft = 8;
 			break;
 		case 3:
 			for (int i = 15; i <= 15 + 9 * 5; i += 9) {
 				EntityCreator.createDestroyableBox(world, i, 2.5f, 5, 5);
 			}
 			for (float i = 19.5f; i <= 19.5f + 9 * 4; i += 9) {
 				EntityCreator.createDestroyableBox(world, i, 7.5f, 5, 5);
 			}
 			for (int i = 24; i <= 24 + 9 * 3; i += 9) {
 				EntityCreator.createDestroyableBox(world, i, 12.5f, 5, 5);
 			}
 			for (float i = 28.5f; i <= 28.5f + 9 * 2; i += 9) {
 				EntityCreator.createDestroyableBox(world, i, 17.5f, 5, 5);
 			}
 			for (int i = 33; i <= 33 + 9 * 1; i += 9) {
 				EntityCreator.createDestroyableBox(world, i, 22.5f, 5, 5);
 			}
 			EntityCreator.createPlayerBox(world, 37.5f, 27.5f, 5, 5);
 			boxesLeft = 19;
 			break;
 		case 4:
 			EntityCreator.createDestroyableStar(world, 32, 5, 10, 10);
 			EntityCreator.createDestroyableStar(world, 48, 5, 10, 10);
 			EntityCreator.createDestroyableStar(world, 32, 15, 10, 10);
 			EntityCreator.createDestroyableStar(world, 48, 15, 10, 10);
 			EntityCreator.createDestroyableStar(world, 32, 25, 10, 10);
 			EntityCreator.createDestroyableStar(world, 48, 25, 10, 10);
 			EntityCreator.createPlayerBox(world, 40, 35, 10, 10);
 			boxesLeft = 5;
 			break;
 		case 5:
 			EntityCreator.createDestroyableStar(world, 10.2f, 5, 10, 10);
 			EntityCreator.createDestroyableStar(world, 30.1f, 5, 10, 10);
 			EntityCreator.createDestroyableStar(world, 49.90f, 5, 10, 10);
 			EntityCreator.createDestroyableStar(world, 69.80f, 5, 10, 10);
 			
 			EntityCreator.createDestroyableStar(world, 20.16f, 15, 10, 10);
 			EntityCreator.createDestroyableStar(world, 40, 15, 10, 10);
 			EntityCreator.createDestroyableStar(world, 59.85f, 15, 10, 10);
 			
 			EntityCreator.createDestroyableStar(world, 30.1f, 25, 10, 10);
 			EntityCreator.createDestroyableStar(world, 49.935f, 25, 10, 10);
 			
 			EntityCreator.createPlayerStar(world, 39.97f, 35, 10, 10);
 			boxesLeft = 7;
 			break;
 		case 6:
 			EntityCreator.createDestroyableBox(world, 40, 4f, 8, 8);
 			
 			EntityCreator.createDestroyableBox(world, 35, 10.5f, 5, 5);
 			EntityCreator.createSteelBox(world, 45, 10.5f, 5, 5);
 			
 			EntityCreator.createDestroyableBox(world, 40, 17f, 8, 8);
 			
 			EntityCreator.createSteelBox(world, 35, 23.5f, 5, 5);
 			EntityCreator.createDestroyableBox(world, 45, 23.5f, 5, 5);
 			
 			EntityCreator.createDestroyableBox(world, 40, 30f, 8, 8);
 			
 			EntityCreator.createPlayerStar(world, 40, 35.5f, 3, 3);
 			boxesLeft = 5;
 			break;
 		case 7:
 			Random r = new Random();
 			for (int i = 1; i < 50; ++i) {
 				int sign = (r.nextFloat() > 0.5f) ? 1 : -1;
 				EntityCreator.createDestroyableBox(world, sign * r.nextFloat() * r.nextFloat() * 40 + 40, 10 + 30 * r.nextFloat(), 3, 3);
 			}
 			
 			EntityCreator.createPlayerStar(world, 40, 50, 10, 10);
 			boxesLeft = 45; 
 			break;
 		case 8:
 			Random r2 = new Random();
 			for (int i = 1; i < 50; ++i) {
 				int sign = (r2.nextFloat() > 0.5f) ? 1 : -1;
 				EntityCreator.createDestroyableStar(world, sign * r2.nextFloat() * r2.nextFloat() * 40 + 40, 10 + 30 * r2.nextFloat(), 3, 3);
 			}
 			
 			EntityCreator.createPlayerStar(world, 40, 50, 10, 10);
 			boxesLeft = 47; 
 			break;
 		case 9:
 			EntityCreator.createDestroyableCross(world, 34, 5, 10, 10);
 			EntityCreator.createDestroyableCross(world, 46, 5, 10, 10);
 			
 			EntityCreator.createDestroyableCross(world, 40, 13, 10, 10);
 			
 			EntityCreator.createDestroyableCross(world, 34, 21, 10, 10);
 			EntityCreator.createDestroyableCross(world, 46, 21, 10, 10);
 			
 			EntityCreator.createDestroyableCross(world, 40, 29, 10, 10);
 			
 			EntityCreator.createPlayerBox(world, 38, 38, 8, 8);
 			boxesLeft = 5; 
 			break;
 		case 10:
 			EntityCreator.createDestroyableCross(world, 34, 5, 10, 10);
 			EntityCreator.createSteelCross(world, 46, 5, 10, 10);
 			
 			EntityCreator.createDestroyableCross(world, 40, 13, 10, 10);
 			
 			EntityCreator.createDestroyableCross(world, 34, 21, 10, 10);
 			EntityCreator.createDestroyableCross(world, 46, 21, 10, 10);
 			
 			EntityCreator.createSteelCross(world, 40, 29, 10, 10);
 			
 			EntityCreator.createPlayerBox(world, 38, 38, 8, 8);
 			boxesLeft = 4; 
 			break;
 		case 11:
 			EntityCreator.createDestroyableBox(world, 15, 5, 10, 10);
 			EntityCreator.createDestroyableBox(world, 40, 5, 10, 10);
 			EntityCreator.createDestroyableBox(world, 65, 5, 10, 10);
 			
 			EntityCreator.createDestroyableRectangle(world, 25, 15, 30, 10);
 			EntityCreator.createDestroyableRectangle(world, 55, 15, 30, 10);
 			
 			EntityCreator.createDestroyableBox(world, 15, 25, 10, 10);
 			EntityCreator.createSteelBox(world, 40, 25, 10, 10);
 			EntityCreator.createDestroyableBox(world, 65, 25, 10, 10);
 			
 			EntityCreator.createDestroyableRectangle(world, 25, 35, 30, 10);
 			EntityCreator.createDestroyableRectangle(world, 55, 35, 30, 10);
 			
 			EntityCreator.createPlayerBox(world, 35, 45, 10, 10);
 			boxesLeft = 9; 
 			break;
 		case 12:
 			EntityCreator.createDestroyableBox(world, 35, 5, 10, 10);
 			
 			EntityCreator.createSteelRectangle(world, 30, 25, 30, 10, (float)(Math.PI / 2));
 			EntityCreator.createDestroyableBox(world, 40, 15, 10, 10);
 			
 			EntityCreator.createDestroyableBox(world, 40, 25, 10, 10);
 			
 			EntityCreator.createDestroyableBox(world, 40, 35, 10, 10);
 			
 			EntityCreator.createPlayerBox(world, 35, 45, 10, 10);
 			boxesLeft = 3; 
 			break;
 		case 13:
 			EntityCreator.createDestroyableTriangle(world, 15, 5, 10, 10);
 			EntityCreator.createDestroyableTriangle(world, 25, 5, 10, 10);
 			EntityCreator.createDestroyableTriangle(world, 35, 5, 10, 10);
 			EntityCreator.createDestroyableTriangle(world, 45, 5, 10, 10);
 			EntityCreator.createDestroyableTriangle(world, 55, 5, 10, 10);
 			EntityCreator.createDestroyableTriangle(world, 65, 5, 10, 10);
 			
 			EntityCreator.createDestroyableTriangle(world, 20, 16, 12, 12);
 			EntityCreator.createDestroyableTriangle(world, 40, 16, 12, 12);
 			EntityCreator.createDestroyableTriangle(world, 60, 16, 12, 12);
 			
 			EntityCreator.createDestroyableRectangle(world, 30, 27, 20, 7.5f);
 			
 			EntityCreator.createPlayerTriangle(world, 45, 37, 10, 10);
 			boxesLeft = 8; 
 			break;
 		case 14:
 			EntityCreator.createSteelTriangle(world, 10, 3, 6, 6);
 			EntityCreator.createSteelTriangle(world, 16, 3, 6, 6);
 			EntityCreator.createSteelTriangle(world, 22, 3, 6, 6);
 			EntityCreator.createSteelTriangle(world, 28, 3, 6, 6);
 			EntityCreator.createSteelTriangle(world, 40, 3, 6, 6);
 			EntityCreator.createSteelTriangle(world, 46, 3, 6, 6);
 			EntityCreator.createSteelTriangle(world, 52, 3, 6, 6);
 			EntityCreator.createSteelTriangle(world, 58, 3, 6, 6);
 			EntityCreator.createSteelTriangle(world, 64, 3, 6, 6);
 			EntityCreator.createSteelTriangle(world, 70, 3, 6, 6);
 			
 			EntityCreator.createDestroyableTriangle(world, 13, 10, 8, 8);
 			EntityCreator.createDestroyableBox(world, 25, 10, 8, 8);
 			EntityCreator.createDestroyableBox(world, 43, 10, 8, 8);
 			EntityCreator.createDestroyableTriangle(world, 55, 10, 8, 8);
 			EntityCreator.createDestroyableTriangle(world, 67, 10, 8, 8);
 			
 			EntityCreator.createDestroyableBox(world, 17, 19, 10, 10);
 			EntityCreator.createDestroyableBox(world, 30, 18, 8, 8);
 			EntityCreator.createDestroyableBox(world, 38, 18, 8, 8);
 			EntityCreator.createDestroyableBox(world, 47.7f, 17.3f, 6.6f, 6.6f);
 			EntityCreator.createDestroyableRectangle(world, 61, 17.3f, 20, 6.6f);
 			
 			EntityCreator.createDestroyableBox(world, 26, 26, 8, 8);
 			EntityCreator.createDestroyableBox(world, 34, 26, 8, 8);
 			EntityCreator.createDestroyableBox(world, 52, 25.3f, 9.4f, 9.4f);
 			
 			EntityCreator.createPlayerBox(world, 30, 34, 8, 8);
 			boxesLeft = 13; 
 			break;
 		default:
 			break;
 		}
 	}
 	
 	private void victory() {
 		gameRunning = false;
 		stage.victory();
 	}
 	
 	private void gameOver() {
 		gameRunning = false;
 		stage.gameOver();
 		victoryChecker = new NullVictoryChecker();
 	}
 	
 	public void restart() {
 		show();
 		setLevel(currentLevel);
 	}
 
 	@Override
 	public void dispose() {
 		batch.dispose();
 		stage.dispose();
 		world.dispose();
 	}
 
 	@Override
 	public void render(float delta) {		
 		renderer.render();
 	}
 	
 	private interface Renderer {
 		public void render();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void pause() {
 		if (gameRunning) {
 			gameRunning = false;
 			renderer = pauseRenderer;
 			stage.pause();
 		}
 	}
 	
 	public void run() {
 		gameRunning = true;
 		renderer = normalRenderer;
 		stage.reset();
 	}
 
 	@Override
 	public void resume() {
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 		if (Input.Keys.ESCAPE == keycode || Input.Keys.BACK == keycode) {
 			core.setScreen(core.levelScreen);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		if (!gameRunning) {
 			return false;
 		}
 		float x = ((float)screenX) / Gdx.graphics.getWidth() * Constants.WIDTH;
 		float y = Constants.HEIGHT - ((float)screenY) / Gdx.graphics.getHeight() * Constants.HEIGHT;
 		world.QueryAABB(new QueryCallback() {
 			@Override
 			public boolean reportFixture(Fixture fixture) {
				if (((Entity)fixture.getBody().getUserData()).isDestroyable()) {
 					if (gameRunning) {
 						world.destroyBody(fixture.getBody());
 						if (boxesLeft > 0) {
 							--boxesLeft;
 						}
 						if (boxesLeft == 0) {
 							victoryChecker = new VictoryChecker() {
 								@Override
 								public void check() {
 									boolean sleeping = true;
 									Iterator<Body> i = world.getBodies();
 									while (i.hasNext()) {
 										Body body = i.next();
 										if (body.getUserData() instanceof Entity) {
 											if (body.isAwake()) {
 												sleeping = false;
 											}
 										}
 									}
 									if (sleeping) {
 										victory();
 									}
 								}
 							};
 						}
 					}
 				}
 				return false;
 			}
 		}, x, y, x, y);
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		return false;
 	}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		return false;
 	}
 
 	@Override
 	public void show() {
 		gameRunning = true;
 		renderer = normalRenderer;
 		stage.reset();
 		victoryChecker = new NullVictoryChecker();
 	}
 
 	@Override
 	public void hide() {
 	}
 	
 	private interface VictoryChecker {
 		public void check();
 	}
 	
 	private class NullVictoryChecker implements VictoryChecker {
 		@Override
 		public void check() {}
 	}
 }
