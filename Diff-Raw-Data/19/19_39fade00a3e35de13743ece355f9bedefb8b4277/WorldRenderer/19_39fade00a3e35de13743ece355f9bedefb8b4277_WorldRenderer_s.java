 package com.flipptor.orbgame;
 
 import box2dLight.RayHandler;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.physics.box2d.World;
 
 public class WorldRenderer {
 	private World world;
 	private OrthographicCamera camera;
 	private OrbGame game;
 	private float width, height;
 	private Box2DDebugRenderer renderer;
 	private RayHandler rayHandler;
 	private EntityHandler entityHandler;
 	
 	public WorldRenderer(OrbGame game) {
 		this.game = game;
 		
 		width = Gdx.graphics.getWidth()/10;
 		height = Gdx.graphics.getHeight()/10;
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		
 		world = new World(new Vector2(0,0), false);
 		
 		camera = new OrthographicCamera(width, height);
 		camera.position.set(width*0.5f, height*0.5f, 0);
 		camera.update();
 		
 		renderer = new Box2DDebugRenderer();
 		rayHandler = new RayHandler(world);
 		rayHandler.setCombinedMatrix(camera.combined);
 		
 		entityHandler = new EntityHandler(world, rayHandler);
 		
 	}
 	
 	public void render() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		renderer.render(world, camera.combined);
		renderer.setDrawBodies(true);
		rayHandler.render();
		rayHandler.update();
		entityHandler.update();
 		if(Gdx.input.isKeyPressed(Keys.ESCAPE)){
 			Gdx.app.exit();
 		}
 		world.step(1/60f, 6, 2);
 	}
 }
