 package com.team14;
 
 /**
  *   Copyright 2011 David Kirchner dpk@dpk.net
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  * 
  * Source:
  * http://dpk.net/2011/05/08/libgdx-box2d-tiled-maps-full-working-example-part-2/
  */
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.World;
 
 public class Game implements ApplicationListener {
 	/**
 	 * The time the last frame was rendered, used for throttling framerate
 	 */
 	private long lastRender;
 
 	private TiledMapHelper tiledMapHelper;
 
 	/**
 	 * Holder of the texture for the various non-map sprites the game will have.
 	 */
 	private Texture overallTexture;
 
 	/**
 	 * As the name implies, this is the sprite for the jumper character. The
 	 * boolean is just to track which direction the jumper is facing. There are
 	 * better ways to handle this, but in a real game you would handle the
 	 * character sprite a lot differently (with animations and all that) so
 	 * let's call that outside the scope of this example.
 	 */
 	private Sprite jumperSprite;
 	private boolean jumperFacingRight;
 
 	/**
 	 * The libgdx SpriteBatch -- used to optimize sprite drawing.
 	 */
 	private SpriteBatch spriteBatch;
 
 	/**
 	 * This is the main box2d "container" object. All bodies will be loaded in
 	 * this object and will be simulated through calls to this object.
 	 */
 	private World world;
 
 	/**
 	 * This is the player character. It will be created as a dynamic object.
 	 */
 	private Body jumper;
 
 	/**
 	 * This box2d debug renderer comes from libgdx test code. It draws lines
 	 * over all collision boundaries, so it is immensely useful for verifying
 	 * that the world collisions are as you expect them to be. It is, however,
 	 * slow, so only use it for testing.
 	 */
 	private Box2DDebugRenderer debugRenderer;
 
 	/**
 	 * Box2d works best with small values. If you use pixels directly you will
 	 * get weird results -- speeds and accelerations not feeling quite right.
 	 * Common practice is to use a constant to convert pixels to and from
 	 * "meters".
 	 */
 	public static final float PIXELS_PER_METER = 60.0f;
 
 	/**
 	 * The screen's width and height. This may not match that computed by
 	 * libgdx's gdx.graphics.getWidth() / getHeight() on devices that make use
 	 * of on-screen menu buttons.
 	 */
 	private int screenWidth;
 	private int screenHeight;
 
 	public Game() {
 		super();
 
 		// Defer until create() when Gdx is initialized.
 		screenWidth = -1;
 		screenHeight = -1;
 	}
 
 	public Game(int width, int height) {
 		super();
 
 		screenWidth = width;
 		screenHeight = height;
 	}
 
 	@Override
 	public void create() {
 		/**
 		 * If the viewport's size is not yet known, determine it here.
 		 */
 		if (screenWidth == -1) {
 			screenWidth = Gdx.graphics.getWidth();
 			screenHeight = Gdx.graphics.getHeight();
 		}
 
 		tiledMapHelper = new TiledMapHelper();
 
 		tiledMapHelper = new TiledMapHelper();
 
 		tiledMapHelper.setPackerDirectory("assets/tiles");
 
 		tiledMapHelper.loadMap("assets/tiles/testmap.tmx");
 
 		tiledMapHelper.prepareCamera(screenWidth, screenHeight);
 
 		/**
 		 * Load up the overall texture and chop it in to pieces. In this case,
 		 * piece.
 		 */
 		overallTexture = new Texture(Gdx.files.internal("assets/sprite.png"));
 		overallTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 
 		jumperSprite = new Sprite(overallTexture, 0, 0, 21, 37);
 
 		spriteBatch = new SpriteBatch();
 
 		/**
 		 * You can set the world's gravity in its constructor. Here, the gravity
 		 * is negative in the y direction (as in, pulling things down).
 		 */
 		world = new World(new Vector2(0.0f, -10.0f), true);
 
 		BodyDef jumperBodyDef = new BodyDef();
 		jumperBodyDef.type = BodyDef.BodyType.DynamicBody;
		jumperBodyDef.position.set(1.0f, 3.0f);
 
 		jumper = world.createBody(jumperBodyDef);
 
 		/**
 		 * Boxes are defined by their "half width" and "half height", hence the
 		 * 2 multiplier.
 		 */
 		PolygonShape jumperShape = new PolygonShape();
 		jumperShape.setAsBox(jumperSprite.getWidth() / (2 * PIXELS_PER_METER),
 				jumperSprite.getHeight() / (2 * PIXELS_PER_METER));
 
 		/**
 		 * The character should not ever spin around on impact.
 		 */
 		jumper.setFixedRotation(true);
 
 		/**
 		 * The density and friction of the jumper were found experimentally.
 		 * Play with the numbers and watch how the character moves faster or
 		 * slower.
 		 */
 		FixtureDef jumperFixtureDef = new FixtureDef();
 		jumperFixtureDef.shape = jumperShape;
 		jumperFixtureDef.density = 1.0f;
 		jumperFixtureDef.friction = 5.0f;
 
 		jumper.createFixture(jumperFixtureDef);
 		jumperShape.dispose();
 
 		tiledMapHelper.loadCollisions("assets/collisions.txt", world,
 				PIXELS_PER_METER);
 
 		debugRenderer = new Box2DDebugRenderer();
 
 		lastRender = System.nanoTime();
 	}
 
 	@Override
 	public void resume() {
 	}
 
 	@Override
 	public void render() {
 		long now = System.nanoTime();
 
 		/**
 		 * Detect requested motion.
 		 */
 		boolean moveLeft = false;
 		boolean moveRight = false;
 		boolean doJump = false;
 
 		if (Gdx.input.isKeyPressed(Input.Keys.DPAD_RIGHT)) {
 			moveRight = true;
 		} else {
 			for (int i = 0; i < 2; i++) {
 				if (Gdx.input.isTouched(i)
 						&& Gdx.input.getX() > Gdx.graphics.getWidth() * 0.80f) {
 					moveRight = true;
 				}
 			}
 		}
 
 		if (Gdx.input.isKeyPressed(Input.Keys.DPAD_LEFT)) {
 			moveLeft = true;
 		} else {
 			for (int i = 0; i < 2; i++) {
 				if (Gdx.input.isTouched(i)
 						&& Gdx.input.getX() < Gdx.graphics.getWidth() * 0.20f) {
 					moveLeft = true;
 				}
 			}
 		}
 
 		if (Gdx.input.isKeyPressed(Input.Keys.DPAD_UP)) {
 			doJump = true;
 		} else {
 			for (int i = 0; i < 2; i++) {
 				if (Gdx.input.isTouched(i)
 						&& Gdx.input.getY() < Gdx.graphics.getHeight() * 0.20f) {
 					doJump = true;
 				}
 			}
 		}
 
 		/**
 		 * Act on that requested motion.
 		 * 
 		 * This code changes the jumper's direction. It's handled separately
 		 * from the jumping so that the player can jump and move simultaneously.
 		 * The horizontal figure was arrived at experimentally -- try other
 		 * values to experience different speeds.
 		 * 
 		 * The impulses are applied to the center of the jumper.
 		 */
 		if (moveRight) {
 			jumper.applyLinearImpulse(new Vector2(0.05f, 0.0f),
 					jumper.getWorldCenter());
 			if (jumperFacingRight == false) {
 				jumperSprite.flip(true, false);
 			}
 			jumperFacingRight = true;
 		} else if (moveLeft) {
 			jumper.applyLinearImpulse(new Vector2(-0.05f, 0.0f),
 					jumper.getWorldCenter());
 			if (jumperFacingRight == true) {
 				jumperSprite.flip(true, false);
 			}
 			jumperFacingRight = false;
 		}
 
 		/**
 		 * The jumper dude can only jump while on the ground. There are better
 		 * ways to detect ground contact, but for our purposes it is sufficient
 		 * to test that the vertical velocity is zero (or close to it). As in
 		 * the above code, the vertical figure here was found through
 		 * experimentation. It's enough to get the guy off the ground.
 		 * 
 		 * As before, impulse is applied to the center of the jumper.
 		 */
 		if (doJump && Math.abs(jumper.getLinearVelocity().y) < 1e-9) {
 			jumper.applyLinearImpulse(new Vector2(0.0f, 0.8f),
 					jumper.getWorldCenter());
 		}
 
 		/**
 		 * Have box2d update the positions and velocities (and etc) of all
 		 * tracked objects. The second and third argument specify the number of
 		 * iterations of velocity and position tests to perform -- higher is
 		 * more accurate but is also slower.
 		 */
 		world.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);
 
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		/**
 		 * A nice(?), blue backdrop.
 		 */
 		Gdx.gl.glClearColor(0, 0.5f, 0.9f, 0);
 
 		/**
 		 * The camera is now controlled primarily by the position of the main
 		 * character, and secondarily by the map boundaries.
 		 */
 
 		tiledMapHelper.getCamera().position.x = PIXELS_PER_METER
 				* jumper.getPosition().x;
 
 		/**
 		 * Ensure that the camera is only showing the map, nothing outside.
 		 */
 		if (tiledMapHelper.getCamera().position.x < Gdx.graphics.getWidth() / 2) {
 			tiledMapHelper.getCamera().position.x = Gdx.graphics.getWidth() / 2;
 		}
 		if (tiledMapHelper.getCamera().position.x >= tiledMapHelper.getWidth()
 				- Gdx.graphics.getWidth() / 2) {
 			tiledMapHelper.getCamera().position.x = tiledMapHelper.getWidth()
 					- Gdx.graphics.getWidth() / 2;
 		}
 
 		if (tiledMapHelper.getCamera().position.y < Gdx.graphics.getHeight() / 2) {
 			tiledMapHelper.getCamera().position.y = Gdx.graphics.getHeight() / 2;
 		}
 		if (tiledMapHelper.getCamera().position.y >= tiledMapHelper.getHeight()
 				- Gdx.graphics.getHeight() / 2) {
 			tiledMapHelper.getCamera().position.y = tiledMapHelper.getHeight()
 					- Gdx.graphics.getHeight() / 2;
 		}
 
 		tiledMapHelper.getCamera().update();
 		tiledMapHelper.render();
 
 		/**
 		 * Prepare the SpriteBatch for drawing.
 		 */
 		spriteBatch.setProjectionMatrix(tiledMapHelper.getCamera().combined);
 		spriteBatch.begin();
 
 		jumperSprite.setPosition(
 				PIXELS_PER_METER * jumper.getPosition().x
 						- jumperSprite.getWidth() / 2,
 				PIXELS_PER_METER * jumper.getPosition().y
 						- jumperSprite.getHeight() / 2);
 		jumperSprite.draw(spriteBatch);
 
 		/**
 		 * "Flush" the sprites to screen.
 		 */
 		spriteBatch.end();
 
 		/**
 		 * Draw this last, so we can see the collision boundaries on top of the
 		 * sprites and map.
 		 */
 		debugRenderer.render(world, tiledMapHelper.getCamera().combined.scale(
 				Game.PIXELS_PER_METER,
 				Game.PIXELS_PER_METER,
 				Game.PIXELS_PER_METER));
 
 		now = System.nanoTime();
 		if (now - lastRender < 30000000) { // 30 ms, ~33FPS
 			try {
 				Thread.sleep(30 - (now - lastRender) / 1000000);
 			} catch (InterruptedException e) {
 			}
 		}
 
 		lastRender = now;
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void dispose() {
 	}
 }
 
 //package com.team14;
 //
 //import com.badlogic.gdx.ApplicationListener;
 //import com.badlogic.gdx.Gdx;
 //import com.badlogic.gdx.graphics.GL10;
 //import com.badlogic.gdx.graphics.g2d.tiled.TileMapRenderer;
 //import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
 //import com.badlogic.gdx.graphics.g2d.tiled.TileAtlas;
 //import com.badlogic.gdx.graphics.g2d.tiled.TiledLoader;
 //import com.badlogic.gdx.graphics.OrthographicCamera;
 //import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 //import com.badlogic.gdx.math.Vector3;
 //
 //public class Game implements ApplicationListener {
 //	OrthographicCamera cam = new OrthographicCamera();
 //	Vector3 tmp = new Vector3();
 //    TileMapRenderer tileMapRenderer;
 //    SpriteBatch spriteBatch;
 //    
 //	private boolean gameOver = false;
 //	boolean automove = true;
 //	
 //	public void setGameOver(boolean status)
 //	{
 //		gameOver = status;
 //	}
 //	
 //	public boolean isGameOver()
 //	{
 //		return gameOver;
 //	}
 //	
 //	@Override
 //	public void create() {
 ////        TileMapRenderer tileMapRenderer;
 //        TiledMap map;
 //        TileAtlas atlas;
 //        // TiledLoader loader;
 //        spriteBatch = new SpriteBatch();
 //        // Load the tmx file into map
 //        map = TiledLoader.createMap(Gdx.files.internal("assets/tiles/testmap.tmx"));
 //
 //        // Load the tiles into atlas
 //        atlas = new TileAtlas(map, Gdx.files.internal("assets/tiles/"));
 //
 //        // Create the renderer
 //        tileMapRenderer = new TileMapRenderer(map, atlas, 32, 32, 5, 5);
 //	}
 //
 //	@Override
 //	public void dispose() {
 //		// TODO Auto-generated method stub
 //
 //	}
 //
 //	@Override
 //	public void pause() {
 //		// TODO Auto-generated method stub
 //
 //	}
 //
 //	@Override
 //	public void render() {
 //        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 //
 //        // if (automove) {
 //        //     updateCameraPosition();
 //        // }
 //
 //        cam.zoom = 0.9f;
 //        cam.update();
 //        
 //        tileMapRenderer.render(cam);
 //
 //        spriteBatch.begin();
 //        //font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, 20);
 //        //font.draw(spriteBatch, "InitialCol, LastCol: " + tileMapRenderer.getInitialCol() + "," + tileMapRenderer.getLastCol(), 20,
 //         //       40);
 //        //font.draw(spriteBatch, "InitialRow, LastRow: " + tileMapRenderer.getInitialRow() + "," + tileMapRenderer.getLastRow(), 20,
 //         //       60);
 //        
 //        tmp.set(0, 0, 0);
 //        cam.unproject(tmp);
 //        //font.draw(spriteBatch, "Location: " + tmp.x + "," + tmp.y, 20, 80);
 //        spriteBatch.end();
 //		// TODO Auto-generated method stub
 //
 //	}
 //
 //	@Override
 //	public void resize(int arg0, int arg1) {
 //	}
 //
 //	@Override
 //	public void resume() {
 //	}
 //
 //}
