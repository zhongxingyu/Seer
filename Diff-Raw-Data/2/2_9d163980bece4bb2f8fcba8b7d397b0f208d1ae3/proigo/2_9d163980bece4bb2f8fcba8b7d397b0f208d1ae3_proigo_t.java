 package com.garbri.proigo.core;
 
 import com.badlogic.gdx.controllers.Controller;
 import com.badlogic.gdx.controllers.Controllers;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.garbri.proigo.core.objects.Ball;
 import com.garbri.proigo.core.objects.Car;
 import com.garbri.proigo.core.objects.Goal;
 import com.garbri.proigo.core.objects.Pitch;
 import com.garbri.proigo.core.utilities.BoxProp;
 import com.garbri.proigo.core.utilities.Controls;
 import com.garbri.proigo.core.utilities.SpriteHelper;
 
 public class proigo implements ApplicationListener {
 private long lastRender;
 	
 	private OrthographicCamera camera;
 	private SpriteBatch spriteBatch;
 	/**
 	 * This is the main box2d "container" object. All bodies will be loaded in
 	 * this object and will be simulated through calls to this object.
 	 */
 	private World world;
 	/**
 	 * This box2d debug renderer comes from libgdx test code. It draws lines
 	 * over all collision boundaries, so it is immensely useful for verifying
 	 * that the world collisions are as you expect them to be. It is, however,
 	 * slow, so only use it for testing.
 	 */
 	private Box2DDebugRenderer debugRenderer;	
 
 	private int screenWidth;
 	private int screenHeight;	
 	private float worldWidth;
 	private float worldHeight;
 	private static int PIXELS_PER_METER=10;      //how many pixels in a meter
 	
 	Controller[] controllers = new Controller[4];
 	
 	Car player1;
 	Car player2;
 	Ball ball;
 	Goal leftGoal;
 	Goal rightGoal;
 	
 	public Pitch pitch;
 	
 	private SpriteHelper spriteHelper;
 	
 	@Override
 	public void create() {		
 //		screenWidth = Gdx.graphics.getWidth();
 //		screenHeight = Gdx.graphics.getHeight();
 		
 		screenWidth = 1400;
 		screenHeight = 900;
 		
 		worldWidth = screenWidth / PIXELS_PER_METER;
 		worldHeight = screenHeight / PIXELS_PER_METER;
 		
 		//worldWidth = 800;
 		//worldHeight = 600;
 
 		//Box2d World init
 		Vector2 center = new Vector2(worldWidth/2, worldHeight/2);
 		
 		world = new World(new Vector2(0.0f, 0.0f), true);	
 		
 		spriteHelper = new SpriteHelper();
 	    
 		int i = 0;
 		
 		for(Controller controller: Controllers.getControllers()) 
 		{
 		   Gdx.app.log("Main", controller.getName());
 			   
 		   this.controllers[i] = controller;
 		   i++;
 			   
 		}
 		
 		
 		
		//if(this.controllers[0] != null)
 			
 			
 		
 	    this.player1 = new Car("player1", world, 2, 4,
 	    		new Vector2(15f, center.y), (float) Math.PI/2, 60, 20, 120, new Controls(Input.Keys.DPAD_UP, Input.Keys.DPAD_DOWN, Input.Keys.DPAD_LEFT, Input.Keys.DPAD_RIGHT), spriteHelper.getCarSprite(0), spriteHelper.getWheelSprite());
 	    
 	    this.player2 = new Car("player2", world, 2, 4,
 	    		new Vector2((worldWidth -15f), center.y), (float) (Math.PI + Math.PI/2), 60, 20, 120, new Controls(Input.Keys.W, Input.Keys.S, Input.Keys.A, Input.Keys.D), spriteHelper.getCarSprite(1), spriteHelper.getWheelSprite());
 		
 	    camera = new OrthographicCamera();
 	    camera.setToOrtho(false, screenWidth, screenHeight);
 	    spriteBatch = new SpriteBatch();		
 										
 		debugRenderer = new Box2DDebugRenderer();
 	    
 		this.ball = new Ball(world, center.x, center.y, spriteHelper.getBallSprite());
 		
 		this.pitch = new Pitch(world, worldWidth, worldHeight, center);
 	 
 	}
 	
 	private Controls getPlayerControls(int player, Controller controller)
 	{
 		
 		
 		switch(player)
 		{
 			case 1:
 				if (controller == null)
 				{
 					return new Controls(Input.Keys.DPAD_UP, Input.Keys.DPAD_DOWN, Input.Keys.DPAD_LEFT, Input.Keys.DPAD_RIGHT);
 				}
 				else
 				{
 					return null;
 				}
 		}
 		
 		return null;
 	}
 
 	@Override
 	public void dispose() {
 		spriteBatch.dispose();
 	}
 
 	@Override
 	public void render() {	
 	    Gdx.gl.glClearColor(0, 0.5f, 0.05f, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 	    // tell the camera to update its matrices.
 	    camera.update();
 	    
 		spriteBatch.setProjectionMatrix(camera.combined);
 
 		player1.controlCar();
 		player2.controlCar();
 		this.ball.update();
 		
 		Vector2 ballLocation = this.ball.getLocation();
 		this.pitch.leftGoal.checkForGoal(ballLocation, 0f);
 		this.pitch.rightGoal.checkForGoal(ballLocation, 0f);
 		
 		
 		/**
 		 * Have box2d update the positions and velocities (and etc) of all
 		 * tracked objects. The second and third argument specify the number of
 		 * iterations of velocity and position tests to perform -- higher is
 		 * more accurate but is also slower.
 		 */
 		world.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);
 		
 		world.clearForces();
 		
 		this.spriteBatch.begin();
 		//Update Player/Car 1
 		
 		player1.updateSprite(spriteBatch, PIXELS_PER_METER);
 		
 		//Update Player/Car 2
 		player2.updateSprite(spriteBatch, PIXELS_PER_METER);
 		
 		//Update Ball
 		SpriteHelper.updateSprite(ball.sprite, spriteBatch, PIXELS_PER_METER, ball.body);
 		
 		this.spriteBatch.end();
 		
 		/**
 		 * Draw this last, so we can see the collision boundaries on top of the
 		 * sprites and map.
 		 */
 		debugRenderer.render(world, camera.combined.scale(PIXELS_PER_METER,PIXELS_PER_METER,PIXELS_PER_METER));
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
 }
