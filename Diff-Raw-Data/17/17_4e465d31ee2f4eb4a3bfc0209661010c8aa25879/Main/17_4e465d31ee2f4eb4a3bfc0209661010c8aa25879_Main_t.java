 package org.group20.sunstruck;
 
 import java.util.Iterator;
 
 import org.group20.sunstruck.gameobject.GameObject;
 import org.group20.sunstruck.world.map.segments.MapSegment;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 
 public class Main implements ApplicationListener {
 	// The width and height of the orthographical, camera
 	public static final float CAMERA_WIDTH = 10;
 	public static float bgScale = 1.0f;
 	public static float bgSpeed = 2.0f;
 	public static Body eastBorder;
 	public static Body northBorder;
 	public static Body westBorder;
 	public static Body southBorder;
 	private MapSegment last;
 	private MapSegment first;
 	private SpriteBatch guiBatch;
 	private SpriteBatch spriteBatch;
 	private SpriteBatch backgroundBatch;
 	private OrthographicCamera camera;
 	private Box2DDebugRenderer renderer;
	private float bgYOffset = 0;
 	private int bgIteration = 0;
 	private float time = 0;
 	private boolean run = true;
 
 	@Override
 	public void create() {
 		// Gdx.app.log("Simple Test", "Thread=" + Thread.currentThread().getId()
 		// + ", surface created");
 		Game.getInstance().initializePlayer();
 
 		// Scales the height.
 		float scale = (float) Gdx.graphics.getHeight()
 				/ Gdx.graphics.getWidth();
 		bgScale = CAMERA_WIDTH * scale / 2;
 
 		camera = new OrthographicCamera(CAMERA_WIDTH, CAMERA_WIDTH * scale);
 		camera.position.set(0, 0, 0);
 
 		// East border.
 		PolygonShape eastBorderPoly = new PolygonShape();
 		eastBorderPoly.setAsBox(0, CAMERA_WIDTH * scale);
 
 		BodyDef eastBorderDef = new BodyDef();
 		eastBorderDef.position.x = CAMERA_WIDTH;
 		eastBorderDef.position.y = 0;
 		eastBorderDef.type = BodyType.StaticBody;
 
 		eastBorder = Game.getInstance().getWorld().createBody(eastBorderDef);
 		eastBorder.createFixture(eastBorderPoly, 10);
 		eastBorderPoly.dispose();
 
 		// West border
 		PolygonShape westBorderPoly = new PolygonShape();
 		westBorderPoly.setAsBox(0, CAMERA_WIDTH * scale);
 
 		BodyDef westBorderDef = new BodyDef();
 		westBorderDef.position.x = -CAMERA_WIDTH;
 		westBorderDef.position.y = 0;
 		westBorderDef.type = BodyType.StaticBody;
 
 		westBorder = Game.getInstance().getWorld().createBody(westBorderDef);
 		westBorder.createFixture(westBorderPoly, 10);
 		westBorderPoly.dispose();
 
 		// North border
 		PolygonShape northBorderPoly = new PolygonShape();
 		northBorderPoly.setAsBox(CAMERA_WIDTH, 0);
 
 		BodyDef northBorderDef = new BodyDef();
 		northBorderDef.position.x = 0;
 		northBorderDef.position.y = CAMERA_WIDTH * scale;
 		northBorderDef.type = BodyType.StaticBody;
 
 		northBorder = Game.getInstance().getWorld().createBody(northBorderDef);
 		northBorder.createFixture(northBorderPoly, 10);
 		northBorderPoly.dispose();
 
 		// North border
 		PolygonShape southBorderPoly = new PolygonShape();
 		southBorderPoly.setAsBox(CAMERA_WIDTH, 0);
 
 		BodyDef southBorderDef = new BodyDef();
 		southBorderDef.position.x = 0;
 		southBorderDef.position.y = -CAMERA_WIDTH * scale;
 		southBorderDef.type = BodyType.StaticBody;
 
 		southBorder = Game.getInstance().getWorld().createBody(southBorderDef);
 		southBorder.createFixture(southBorderPoly, 10);
 		southBorderPoly.dispose();
 
 		spriteBatch = new SpriteBatch();
 		backgroundBatch = new SpriteBatch();
 		guiBatch = new SpriteBatch();
 		renderer = new Box2DDebugRenderer();
 
 		Game.getInstance().start();
 		first = Game.getInstance().getMap().getNext();
 		last = Game.getInstance().getMap().getNext();
 	}
 
 	@Override
 	public void render() {
 		if (!run)
 			return;
 
 		Gdx.gl.glClearColor(1, 1, 1, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		// Draw background
 		drawBackground();
 
 		if (Shop.isActive) {
 			drawGuiShop();
 			drawGameObjects();
 			return;
 		}
 
 		// Update game objects
 		Game.getInstance().update();
 
 		// Update physics
 		updatePhysics();
 
 		// Update camera
 		updateCamera();
 
 		// Draw background
 		drawBackground();
 
 		// Draw game objects.
 		drawGameObjects();
 
 		// Draw GUI controls objects.
 		drawGuiControls();
 
 		// renderer.render(Game.getInstance().getWorld());
 	}
 
 	private void drawGuiShop() {
 		guiBatch.begin();
 		for (Sprite sprite : Game.getInstance().getGui().getShopSpriteList())
 			sprite.draw(guiBatch);
 		guiBatch.end();
 	}
 
 	private void drawGameObjects() {
 		spriteBatch.setProjectionMatrix(camera.combined);
 		spriteBatch.begin();
 
 		GameObject go = null;
 		Iterator<Body> it = Game.getInstance().getWorld().getBodies();
 		while (it.hasNext()) {
 			go = (GameObject) it.next().getUserData();
 
 			if (go == null)
 				continue;
 			float x, y, originX, originY, halfWidth, halfHeight, scaleX, scaleY, rotation;
 
 			halfWidth = go.getWidth() / 2;
 			halfHeight = go.getHeight() / 2;
 
 			rotation = (float) (go.getBody().getAngle() * 180 / Math.PI);
 
 			x = go.getBody().getPosition().x - halfWidth / 2;
 			y = go.getBody().getPosition().y - halfHeight / 2;
 
 			originX = halfWidth / 2;
 			originY = halfHeight / 2;
 
 			scaleX = 2;
 			scaleY = 2;
 
 			spriteBatch.draw(go.getTexture(), x, y, originX, originY,
 					halfWidth, halfHeight, scaleX, scaleY, rotation);
 		}
 		spriteBatch.end();
 
 	}
 
 	private void updatePhysics() {
 		Game.getInstance().getWorld()
 				.step(Gdx.app.getGraphics().getDeltaTime(), 8, 3);
 	}
 
 	private void updateCamera() {
 		GL10 gl = Gdx.app.getGraphics().getGL10();
 		camera.update();
 		camera.apply(gl);
 	}
 
 	private void drawGuiControls() {
 		guiBatch.begin();
 		for (Sprite sprite : Game.getInstance().getGui().getControlSpriteList())
 			sprite.draw(guiBatch);
 		guiBatch.end();
 
 	}
 
 	/**
 	 * pushes the current matrix, draws the backgrounds then pops the matrix.
 	 */
 	private void drawBackground() {
 		TextureRegion rf = first.getTextureRegion();
 		TextureRegion rl = last.getTextureRegion();
 		float bgPosition = bgIteration * bgSpeed;
 		if (rf != null && rl != null) {
 			backgroundBatch.begin();
			backgroundBatch.draw(rf, -bgPosition, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			backgroundBatch.draw(rl, -bgPosition + Gdx.graphics.getWidth(), 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 			backgroundBatch.end();
 		} else {
 			System.out.println("drawBackground(): rf is:" + rf + ", rl is:"
 					+ rl);
 		}
 		time += Gdx.app.getGraphics().getDeltaTime();
 		if (time > 0.01f) {
 			time = 0;
 			bgIteration++;
 		}
		if (bgPosition > Gdx.graphics.getWidth() - 1) {
 			first = last;
 			last = Game.getInstance().getMap().getNext();
 			bgIteration = 0;
 		}
 
 	}
 
 	@Override
 	public void dispose() {
 		// Gdx.app.log("Simple Test", "Thread=" + Thread.currentThread().getId()
 		// + ", application destroyed");
 	}
 
 	@Override
 	public void pause() {
 		run = false;
 		// store data here
 	}
 
 	@Override
 	public void resume() {
 		run = true;
 	}
 
 	@Override
 	public void resize(int x, int y) {
 
 	}
 
 }
