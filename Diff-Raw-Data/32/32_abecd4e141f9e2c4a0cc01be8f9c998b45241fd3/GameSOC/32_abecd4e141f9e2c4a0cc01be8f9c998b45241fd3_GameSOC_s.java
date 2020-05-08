 package com.soc;
 
 import com.artemis.Entity;
 import com.artemis.World;
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.math.Vector2;
 import com.soc.components.Player;
 import com.soc.components.Position;
 import com.soc.components.Sprite;
 import com.soc.components.Velocity;
 import com.soc.systems.AnimationAttackSystem;
 import com.soc.systems.AnimationMainSystem;
 import com.soc.systems.CameraSystem;
 import com.soc.systems.MapCollisionSystem;
 import com.soc.systems.MapRenderSystem;
 import com.soc.systems.MovementSystem;
 import com.soc.systems.PlayerInputSystem;
 import com.soc.systems.SpriteRenderSystem;
 import com.soc.utils.MapLoader;
 
 public class GameSOC implements Screen {
 
 	private OrthographicCamera camera;
 	private Game game;
 	private World world;
 	private SpriteRenderSystem spriteRenderSystem;
 	private AnimationMainSystem animationMainSystem;
 	private AnimationAttackSystem animationAttackSystem;
 	private CameraSystem cameraSystem;
 	private MapRenderSystem map;
 	
 	public GameSOC(Game game) {
 		this.camera = new OrthographicCamera();
 		this.game=game;
 		
 		world = new World();
 		EntityFactory.initialize(world);
 		
 		//CreateMap
 		TiledMap map = MapLoader.loadMap("initial");
 
 		//Regular Systems
 	    world.setSystem(new PlayerInputSystem(camera));
 	    world.setSystem(new MapCollisionSystem(map));
 	    world.setSystem(new MovementSystem());
 	    
 	    //Specially treated systems
 		spriteRenderSystem = world.setSystem( new SpriteRenderSystem(camera), true );
 		cameraSystem = world.setSystem( new CameraSystem(camera), true);
 		animationMainSystem = world.setSystem( new AnimationMainSystem(camera), true );
 		animationAttackSystem=world.setSystem(new AnimationAttackSystem(camera),true);
 
 		
 		this.map = new MapRenderSystem(map, camera);
 		
 		world.initialize();
 		
 		camera.setToOrtho(false, 1280, 900);
 		
		EntityFactory.instance.createWarrior(2000, 300,10,10);
 	}
 
 	@Override
 	public void render(float delta) {
 
 		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		camera.update();
 		 world.setDelta(delta);
 	     world.process();
 	     
 	     map.render();
 	     animationMainSystem.process();
 	     spriteRenderSystem.process();
 	     cameraSystem.process();
 	     animationAttackSystem.process();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		System.out.println("Resize");
 
 	}
 
 	@Override
 	public void show() {
 		System.out.println("Show");
 	}
 
 	@Override
 	public void hide() {
 		System.out.println("Hide");
 	}
 
 	@Override
 	public void pause() {
 		System.out.println("Pause");
 	}
 
 	@Override
 	public void resume() {
 		System.out.println("Resume");
 	}
 
 	@Override
 	public void dispose() {
 		System.out.println("Dispose");
 	}
 
 }
