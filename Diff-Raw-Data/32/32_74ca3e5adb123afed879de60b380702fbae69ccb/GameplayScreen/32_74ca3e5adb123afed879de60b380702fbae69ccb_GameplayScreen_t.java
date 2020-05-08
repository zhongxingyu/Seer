 package com.siegedog.hava.derpygame;
 
 import com.badlogic.gdx.Application;
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.siegedog.hava.engine.FancyGame;
 import com.siegedog.hava.engine.GameCam2D;
 import com.siegedog.hava.engine.GameInput;
 import com.siegedog.hava.engine.Node;
 import com.siegedog.hava.engine.Resources;
 import com.siegedog.hava.engine.SaveGameHelper;
 import com.siegedog.hava.engine.TileMap;
 import com.siegedog.hava.engine.Unit;
 import com.siegedog.hava.engine.test.LolGun;
 
 public class GameplayScreen implements Screen {
 
 	public final int PIXELS_PER_METER = 32;
 
 	private static final int[] layersList = { 2, 3 };
 
 	FancyGame game;
 	SpriteBatch spriteBatch;
 	BitmapFont font;
 
 	Stage stage;
 	Sprite TS;
 
 	GameCam2D cam;
 	OrthographicCamera UICam;
 
 	GameInput gameInput;
 
 	Node root = new Node("root");
 	Unit dude1;
 	Unit dude2;
 
 	long startTime = System.nanoTime();
 	Vector3 tmp = new Vector3();
 
 	TileMap map;
 	float gravityStrength = 80f;
 
 	World world;
 	Box2DDebugRenderer boxDebugRenderer;
 
 	public GameplayScreen(FancyGame game) {
 		this.game = game;
 
 		GameplayScreen.instance = this;
 
 		font = new BitmapFont();
 		font.setColor(new Color(0.5f, 0.5f, 0.8f, 0.94f));
 		cam = new GameCam2D(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		UICam = new OrthographicCamera();
 
 		spriteBatch = new SpriteBatch();
 
 		// World setup
 		world = new World(new Vector2(0, -gravityStrength), true);
 
 		map = new TileMap("level", PIXELS_PER_METER);
 		map.loadCollisions("data/tiledmap/collisions.txt", world);
		boxDebugRenderer = new Box2DDebugRenderer(true, true, true);
 
 		// So far, just a thingy to let us drag the world around
 		gameInput = new GameInput(game, this);
 		Gdx.input.setInputProcessor(gameInput);
 
 		BasicSaveData save = new SaveGameHelper<BasicSaveData>().readSaveData("save.sav");
 		System.out.println("Loaded game...");
 		System.out.println(save.name);
 		
 		Resources.loadSfx("data/sound/sfx/");
 
 		// Dude setup
 		TS = new Sprite(new Texture("data/img/sprites/attractor.png"));
 		// dude = new Dude(sprite, this);
 
 		for(float cx = 7; cx < 30; cx+=2)
			root.addNode(new Crate(cx, 14, 30, 50));
 		
 		dude1 = new Unit("DERPY");
 
 		cam.follow(dude1.getPhysics(), true);
 
 		dude1.equip(new LolGun());
 		// dude1.attack(dude2);
 		// dude2.attack(dude1);
 		// dude1.attack(dude2);
 		// dude1.attack(dude2);
 		root.addNode(dude1);// .addNode(dude2);
 
 		// Stage setup
 		// stage = new Stage(map.getWidth(), map.getHeight(), false);
 		// stage.setCamera(cam);
 		// stage.addActor(dude);
 
 	}
 
 	@Override
 	public void render(float delta) { // called at 60fps
 		Gdx.gl.glClearColor(0.1f, 0.1f, 0.3f, 1f);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		// Async loading
 		Resources.update(delta);
 
 		// Do the physics
 		world.step(delta, 8, 3);
 
 		// And then the logic
 		root.update(delta);
 		// stage.act(delta);
 
 		// Cam should always be updated.
 		// It's not expensive and prevents a bunch of bugs.
 		cam.update(delta);
 
 		// And render everything (if we can)
 		renderGame(delta);
 	}
 
 	private void renderGame(float delta) {
 		// Start rendering shit. map first.
 		map.render(cam.getGameCam());
 
 		spriteBatch.setProjectionMatrix(cam.getGameCam().combined);
 		spriteBatch.begin();
 
 		// TS.draw(spriteBatch);
 		// stage.draw();
 		Node.renderAllNodes(spriteBatch);
 		spriteBatch.end();
 
 		if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
 			Matrix4 m = new Matrix4(cam.getGameCam().combined);
 			m.scale(PIXELS_PER_METER, PIXELS_PER_METER, PIXELS_PER_METER);
 			boxDebugRenderer.render(world, m);
 		}
 
 		float w = Gdx.graphics.getWidth();
 		float h = Gdx.graphics.getHeight();
 		spriteBatch.setProjectionMatrix(cam.getUICam().combined);
 		spriteBatch.begin();
 		font.draw(
 				spriteBatch,
 				"FPS: " + Gdx.graphics.getFramesPerSecond() + " World: "
 						+ world.getBodyCount() + " bodies." + "\n"
 						+ dude1.getBoxDebug(), cam.getUICam().position.x - w
 						/ 2 + 40, cam.getUICam().position.y - h / 2 + 40);
 
 		tmp.set(0, 0, 0);
 		cam.getGameCam().unproject(tmp);
 		font.draw(spriteBatch, "Location: " + tmp.x + "," + tmp.y, 20, 80);
 		spriteBatch.end();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		cam.resize(width, height);
 
 	}
 
 	@Override
 	public void show() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void dispose() {
 		instance = null;
 	}
 
 	public GameCam2D getCam() {
 		return cam;
 	}
 
 	public World getWorld() {
 		return world;
 	}
 
 	private static GameplayScreen instance;
 
 	public static GameplayScreen get() {
 		return instance;
 	}
 }
