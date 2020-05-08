 package com.garbri.proigo.core;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.controllers.Controller;
 import com.badlogic.gdx.controllers.Controllers;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.physics.box2d.World;
 import com.garbri.proigo.core.controls.IControls;
 import com.garbri.proigo.core.controls.KeyboardControls;
 import com.garbri.proigo.core.controls.XboxListener;
 import com.garbri.proigo.core.objects.Ball;
 import com.garbri.proigo.core.objects.Car;
 import com.garbri.proigo.core.objects.Goal;
 import com.garbri.proigo.core.objects.Pitch;
 import com.garbri.proigo.core.utilities.SpriteHelper;
 
 public class SoccerScreen implements Screen{
 
 	
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
 	
 
 	private Car player1;
 	private Car player2;
 	private Car player3;
 	private Car player4;
 	private Ball ball;
 	private Goal leftGoal;
 	private Goal rightGoal;
 	
 	private Vector2 center;
 	
 	public Pitch pitch;
 
 	private SpriteHelper spriteHelper;
 	
 	private proigo game;
 	
 	ArrayList<IControls> controls =  new ArrayList<IControls>();
 	Controller[] controllers = new Controller[4];
 	
 	public SoccerScreen(proigo game)
 	{
 		this.game = game;
 		
 	}
 	
 	@Override
 	public void render(float delta) {
 	    Gdx.gl.glClearColor(0, 0.5f, 0.05f, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 	    // tell the camera to update its matrices.
 	    camera.update();
 
 	    //checkForReset
 	    if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE))
 	    {
 	    	resetGame();
 	    }
 	    
 	    //checkForReset
 	    if (Gdx.input.isKeyPressed(Input.Keys.F1))
 	    {
 	    	this.game.setScreen(this.game.maze1);
 	    }
 
 		spriteBatch.setProjectionMatrix(camera.combined);
 
 		player1.controlCar();
 		player2.controlCar();
 		player3.controlCar();
 		player4.controlCar();
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
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 	
 	@Override
 	public void show() {
 		screenWidth = 1400;
 		screenHeight = 900;
 
 		worldWidth = screenWidth / PIXELS_PER_METER;
 		worldHeight = screenHeight / PIXELS_PER_METER;
 
 		int i = 0;
 		
 		
 		for(Controller controller: Controllers.getControllers()) 
 		{
 		   Gdx.app.log("Main", controller.getName());
 		   XboxListener listener = new XboxListener();
 		   controller.addListener(listener);
 		   listener.getControls();
 		   controls.add(listener.getControls());
 		   this.controllers[i] = controller;
 		   i++;
 			   
 		}
 			
			if(controls.size() == 1){
 				controls.add( new KeyboardControls(Input.Keys.DPAD_UP, Input.Keys.DPAD_DOWN, Input.Keys.DPAD_LEFT, Input.Keys.DPAD_RIGHT));
			}else if(controls.size() == 0){
 				controls.add( new KeyboardControls(Input.Keys.DPAD_UP, Input.Keys.DPAD_DOWN, Input.Keys.DPAD_LEFT, Input.Keys.DPAD_RIGHT));
 				controls.add( new KeyboardControls(Input.Keys.W, Input.Keys.S, Input.Keys.A, Input.Keys.D));
			}
 		
 		//worldWidth = 800;
 		//worldHeight = 600;
 
 		//Box2d World init
 		this.center = new Vector2(worldWidth/2, worldHeight/2);
 
 		world = new World(new Vector2(0.0f, 0.0f), true);	
 
 		spriteHelper = new SpriteHelper();
 
 		createPlayer1();
 		createPlayer2();
 		createPlayer3();
 		createPlayer4();
 
 	    camera = new OrthographicCamera();
 	    camera.setToOrtho(false, screenWidth, screenHeight);
 	    spriteBatch = new SpriteBatch();		
 
 		debugRenderer = new Box2DDebugRenderer();
 
 		this.ball = new Ball(world, center.x, center.y, spriteHelper.getBallSprite());
 
 		this.pitch = new Pitch(world, worldWidth, worldHeight, center);
 		
 	}
 	
 	private void resetGame()
 	{
 		dispose();
 		spriteBatch = new SpriteBatch();
 		
 		this.player1.destroyCar();
 		this.player2.destroyCar();
 		this.player3.destroyCar();
 		this.player4.destroyCar();
 		
 		createPlayer1();
 		createPlayer2();
 		createPlayer3();
 		createPlayer4();
 	}
 	
 	private void createPlayer1()
 	{
 	    this.player1 = new Car("player1", world, 2, 4,
 	    		new Vector2(15f, center.y), (float) Math.PI/2, 60, 20, 180, controls.get(0), spriteHelper.getCarSprite(0), spriteHelper.getWheelSprite());
 	}
 	private void createPlayer3()
 	{
 	    this.player3 = new Car("player3", world, 2, 4,
 	    		new Vector2(15f, center.y), (float) Math.PI/2, 60, 20, 180, controls.get(2), spriteHelper.getCarSprite(0), spriteHelper.getWheelSprite());
 	}
 	private void createPlayer4()
 	{
 	    this.player4 = new Car("player4", world, 2, 4,
 	    		new Vector2(15f, center.y), (float) Math.PI/2, 60, 20, 180, controls.get(3), spriteHelper.getCarSprite(0), spriteHelper.getWheelSprite());
 	}
 	
 	private void createPlayer2()
 	{
 		this.player2 = new Car("player2", world, 2, 4,
 				new Vector2((worldWidth -15f), center.y), (float) (Math.PI + Math.PI/2), 60, 20, 180, controls.get(1), spriteHelper.getCarSprite(1), spriteHelper.getWheelSprite());
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
 		// TODO Auto-generated method stub
 		
 	}
 
 }
