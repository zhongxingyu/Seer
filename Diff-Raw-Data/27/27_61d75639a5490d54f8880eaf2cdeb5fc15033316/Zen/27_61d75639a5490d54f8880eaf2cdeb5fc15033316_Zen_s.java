 package cz.emo4d.zen;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
 import com.badlogic.gdx.maps.tiled.TmxMapLoader;
 import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.Pool;
 
 import cz.emo4d.zen.remote.DeviceEvent;
 import cz.emo4d.zen.remote.DeviceEventHandler;
 import cz.emo4d.zen.remote.RemoteControl;
 
 public class Zen implements ApplicationListener, DeviceEventHandler {
 		
 	private TiledMap map;
 	private OrthogonalTiledMapRenderer renderer;	
 
 	private Player player;
 	private OrthographicCamera camera;
 	private SpriteBatch batch;
 	Vector2 moveVec = new Vector2();
 	
 
 	@Override
 	public void create() {
 		// load the map, set the unit scale to 1/32 (1 unit == 32 pixels)
 		map = new TmxMapLoader().load("data/maps/test.tmx");
 		renderer = new OrthogonalTiledMapRenderer(map, 1 / 32f);
 		map.getTileSets().getTile(1).getTextureRegion().getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
 
 		// create an orthographic camera, shows us 30x20 units of the world
 		camera = new OrthographicCamera();
 		camera.setToOrtho(false, 30, 20);
 		camera.update();
 
 		// create player we want to move around the world
 		int height = (Integer) map.getProperties().get("height");
 		player = new Player(new Vector2(13, height - 3), 0, 0);
 		
		RemoteControl rc = new RemoteControl();
 		rc.RegisterEventHandler(this);
 	}
 
 	@Override
 	public void render() {
 		// clear the screen
 		Gdx.gl.glClearColor(0.7f, 0.7f, 1.0f, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		// get the delta time
 		float deltaTime = Gdx.graphics.getDeltaTime();
 
 		// process input 
 		moveVec.set(0, 0);
 		
 		if (Gdx.input.isKeyPressed(Keys.UP)) {
 			moveVec.y = player.MAX_VELOCITY;
 		}
 		else if (Gdx.input.isKeyPressed(Keys.DOWN)) {
 			moveVec.y = -player.MAX_VELOCITY;
 		}
 		if (Gdx.input.isKeyPressed(Keys.LEFT)) {
 			moveVec.x = -player.MAX_VELOCITY;
 		}
 		else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
 			moveVec.x = player.MAX_VELOCITY;
 		}
 		
 		if (moveVec.x != 0 || moveVec.y != 0) {
 			player.move(moveVec);
 		}
 		
 		// update
 		player.update(deltaTime, map);
 		
 		// let the camera follow the player
 		camera.position.x = player.position.x;
 		camera.position.y = player.position.y;
 		camera.update();
 
 		// set the tile map renderer view based on what the
 		// camera sees and render the map
 		renderer.setView(camera);
 		renderer.render();
 
 		// render
 		SpriteBatch batch = renderer.getSpriteBatch();
 		batch.begin();
 		player.render(batch);
 		batch.end();		
 	}	
 	
 
 	@Override
 	public void dispose() {
 		if (batch != null)
 			batch.dispose();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 	}
 
 	@Override
 	public void acceptEvent(int type, int device, float X, float Y) {
 		if (type == DeviceEvent.MOVE) {
			player.move(new Vector2(X * player.MAX_VELOCITY,  -Y * player.MAX_VELOCITY));
 		}
 		
 	}
 }
