 package Main;
 
 import Entity.Player;
 import Level.Map;
 import box2dLight.PointLight;
 import box2dLight.RayHandler;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
 import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.FPSLogger;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.utils.GdxNativesLoader;
 
 public class Game implements ApplicationListener {
 	GdxNativesLoader gdxLoad = new GdxNativesLoader();
 	
 	
 	public static OrthographicCamera camera;
 	public static World world;
 	private Box2DDebugRenderer renderer;
 	
 	public static Map map;
 	
 	private float width, height;
 	
 	private FPSLogger logger;
 	
 	public static RayHandler handler;	
 		
 	private Player player;
 	
	private PointLight light1;
	
 	@Override
 	public void create() {		
 		width = Gdx.graphics.getWidth();
 		height = Gdx.graphics.getHeight();
 				
 		camera = new OrthographicCamera(width, height);
 		camera.position.set(width * 0.5f, height * 0.5f, 0);
 		camera.update();
 		
 		world = new World(new Vector2(0, 0), false);
 		
 		renderer = new Box2DDebugRenderer();
 		
 		logger = new FPSLogger();
 		
 		map = new Map();
 		map.create();
 		
 		player = new Player(new Sprite(new Texture("res/a.png")), (TiledMapTileLayer)map.getMap().getLayers().get(0));
 		
 		handler = new RayHandler(world);
 		handler.setCombinedMatrix(camera.combined);
		
		light1 = new PointLight(handler, 5000, Color.CYAN, 500, 0, 0);
 	}
 
 	@Override
 	public void dispose() {
 		world.dispose();
 	}
 
 	@Override
 	public void pause() {
 		
 	}
 
 	@Override
 	public void render() {
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		camera.position.set(player.getX() + player.getWidth() / 2, player.getY() + player.getHeight() / 2, 0);
 		camera.update();
 		map.getCamera().position.set(player.getX() + player.getWidth() / 2, player.getY() + player.getHeight() / 2, 0);
 		map.getCamera().update();
 		
 		renderer.render(world, camera.combined);
 		
 		map.render();
 		
 		map.getRenderer().getSpriteBatch().begin();
 		player.draw(map.getRenderer().getSpriteBatch());
 		map.getRenderer().getSpriteBatch().end();
 		
 		handler.setAmbientLight(0f, 0f, 0f, 0.29f);
 		handler.updateAndRender();
 		
 		if(Gdx.input.isKeyPressed(Keys.ESCAPE)) {
 			Gdx.app.exit();
 		}
 		
		light1.setPosition(100 - map.getLightOffsetX() + Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() - 100 - map.getLightOffsetY());
		
 		logger.log();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		map.resize(width, height);
 	}
 
 	@Override
 	public void resume() {
 		
 	}
 	
 	public static void main(String[] args) {
 		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
 		cfg.title = "Dungeon Game";
 		cfg.useGL20 = true;
 		cfg.setFromDisplayMode(cfg.getDesktopDisplayMode());
 		cfg.fullscreen = false;
 		cfg.vSyncEnabled = false;
 			
 		new LwjglApplication(new Game(), cfg);
 	}
 
 }
