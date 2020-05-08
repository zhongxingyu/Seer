 package com.sun.darkstar.example.snowman.game.state.scene;
 
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.Callable;
 
 import com.environment.Environment;
 import com.environment.component.AstronomyComponent;
 import com.environment.component.EffectComponent;
 import com.environment.component.SkyComponent;
 import com.environment.component.WeatherComponent;
 import com.environment.component.enumn.ESkyPart;
 import com.environment.component.enumn.EWeather;
 import com.jme.bounding.BoundingBox;
 import com.jme.image.Texture;
 import com.jme.image.Texture.MagnificationFilter;
 import com.jme.image.Texture.MinificationFilter;
 import com.jme.input.MouseInput;
 import com.jme.math.FastMath;
 import com.jme.math.Vector3f;
 import com.jme.renderer.ColorRGBA;
 import com.jme.system.DisplaySystem;
 import com.jme.util.GameTaskQueueManager;
 import com.jme.util.TextureManager;
 import com.sun.darkstar.example.snowman.ClientApplication;
 import com.sun.darkstar.example.snowman.common.entity.enumn.ECursorState;
 import com.sun.darkstar.example.snowman.common.protocol.enumn.ETeamColor;
 import com.sun.darkstar.example.snowman.common.util.enumn.EWorld;
 import com.sun.darkstar.example.snowman.common.world.World;
 import com.sun.darkstar.example.snowman.data.util.DataManager;
 import com.sun.darkstar.example.snowman.game.Game;
 import com.sun.darkstar.example.snowman.game.entity.util.EntityManager;
 import com.sun.darkstar.example.snowman.game.entity.view.DynamicView;
 import com.sun.darkstar.example.snowman.game.entity.view.util.ViewManager;
 import com.sun.darkstar.example.snowman.game.input.SnowmanCameraHandler;
 import com.sun.darkstar.example.snowman.game.input.util.InputManager;
 import com.sun.darkstar.example.snowman.game.physics.util.PhysicsManager;
 import com.sun.darkstar.example.snowman.game.state.GameState;
 import com.sun.darkstar.example.snowman.game.state.enumn.EGameState;
 import com.sun.darkstar.example.snowman.game.task.util.TaskManager;
 
 /**
  * <code>BattleState</code> extends <code>GameState</code> to define the world
  * game state where the player interacts with other players in a snow ball
  * fight.
  * <p>
  * <code>BattleState</code> initializes by first loading the battle scene graph
  * then construct all the <code>IController</code> that is responsible for
  * handling user input events. It is also responsible for propagating the game
  * update invocation down to the <code>IController</code> and
  * <code>IEntity</code> it manages.
  * 
  * @author Yi Wang (Neakor)
  * @version Creation date: 07-11-2008 12:25 EST
  * @version Modified date: 08-11-2008 16:54 EST
  */
 public class BattleState extends GameState {
 	/**
 	 * The number of added entities.
 	 */
 	private int count;
 	/**
 	 * The id's of the flag goals
 	 */
 	private Map<ETeamColor, Integer> flagGoals = new HashMap<ETeamColor, Integer>();
 	/**
 	 * Our camera handler.
 	 */
 	private SnowmanCameraHandler cameraHandler;
 
 	private Environment environment;
 	private SkyComponent sky;
 	private EffectComponent effects;
 	private AstronomyComponent astronomy;
 	private WeatherComponent weather;
 
 	/**
 	 * Constructor of <code>BattleState</code>.
 	 * 
 	 * @param game
 	 *            The <code>Game</code> instance.
 	 */
 	public BattleState(Game game) {
 		super(EGameState.BattleState, game);
 	}
 
 	@Override
 	protected void initializeWorld() {
 		this.world = (World) DataManager.getInstance().getWorld(EWorld.Battle);
 
 		Callable<Void> exe = new Callable<Void>() {
 			@Override
 			public Void call() throws Exception {
 				// initialize our environment code
 		        buildEnvironment();
 		        // kill the ambient on the sun/moon
 				astronomy.getAstronomyLight().getLightList().get(0).setAmbient(new ColorRGBA(.1f, .1f, .1f, 1));
 				astronomy.getAstronomyLight().getLightList().get(1).setAmbient(new ColorRGBA(.1f, .1f, .1f, 1));
 		        // start snowing
 		        environment.setWeather(EWeather.Snow, world);
 				return null;
 			}
 		};
 		GameTaskQueueManager.getManager().render(exe);
 	}
 
 	@Override
 	protected void initializeState() {
 		MouseInput.get().setHardwareCursor(ClientApplication.class.getClassLoader().getResource(ECursorState.TryingToMove.getIconLocation()));
 	}
 
 	@Override
 	protected void updateState(float interpolation) {
 		this.cameraHandler.update(interpolation);
 		if (environment != null) {
 			this.environment.update(interpolation);
 		}
 	}
 
 	/**
 	 * Initialize a chase camera with the given dynamic view as target.
 	 * 
 	 * @param view
 	 *            The target <code>DynamicView</code> instance.
 	 */
 	public void initializeCameraHandler(DynamicView view) {
 		view.updateGeometricState(0, false);
 		this.cameraHandler = new SnowmanCameraHandler(DisplaySystem
 				.getDisplaySystem().getRenderer().getCamera(), view, this.game);
 		Vector3f targetOffset = new Vector3f(0, 0, 0);
 		targetOffset.setY(((BoundingBox) view.getWorldBound()).yExtent * 1.5f);
 		this.cameraHandler.setTargetOffset(targetOffset);
 		Vector3f dir = view.getLocalRotation().mult(new Vector3f(0, 0, -1));
 		Vector3f store = new Vector3f();
 		this.cameraHandler
 				.setAzimuth(FastMath.cartesianToSpherical(dir, store).y);
 	}
 
 	/**
 	 * Increment the number of entities have been added.
 	 */
 	public void incrementCount() {
 		this.count++;
 	}
 
 	/**
 	 * Retrieve the number of entities have been added.
 	 * 
 	 * @return The number of entities have been added.
 	 */
 	public int getCount() {
 		return this.count;
 	}
 
 	public void addFlagGoalId(ETeamColor color, Integer id) {
 		this.flagGoals.put(color, id);
 	}
 
 	public Integer getFlagGoalId(ETeamColor color) {
 		return this.flagGoals.get(color);
 	}
 
 	/**
 	 * Remove all objects from the world and reset the battle state for a new
 	 * game
 	 */
 	public void reset() {
 		if (this.world != null)
 			this.world.getDynamicRoot().detachAllChildren();
 		EntityManager.getInstance().cleanup();
 		ViewManager.getInstance().cleanup();
 		InputManager.getInstance().cleanup();
 		PhysicsManager.getInstance().cleanup();
 		TaskManager.getInstance().cleanup();
 		this.count = 0;
         this.game.getClient().getHandler().initialize();
 		this.flagGoals.clear();
 	}
 
 	private void buildEnvironment() {
 		this.environment = new Environment();
 		this.sky = new SkyComponent();
 		this.effects = new EffectComponent();
 		this.astronomy = new AstronomyComponent();
 		this.weather = new WeatherComponent();
 		this.buildConnection();
 		this.buildSkyComponent();
 		this.buildEffectComponent();
 		this.buildAstronomyComponent();
 		this.buildWeatherComponent();
 		this.environment.connect(this.sky);
 		this.environment.connect(this.effects);
 		this.environment.connect(this.astronomy);
 		this.environment.connect(this.weather);
 		this.environment.setCurrentTime("17.30");
 		this.environment.applyToScene(this.rootNode);
 	}
 
 	private void buildConnection() {
 		this.astronomy.connect(this.sky);
 		this.astronomy.connect(this.effects);
 	}
 
 	private void buildSkyComponent() {
 		this.sky.setUseSphere(false);
 		this.sky.setSkyRadius(75);
 		this.sky.setRotationCycle(120);
 		this.sky.setTexture(ESkyPart.DaySky, this.loadTexture("DaySky.jpg"));
 		this.sky
 				.setTexture(ESkyPart.NightSky, this.loadTexture("NightSky.jpg"));
 		this.sky.setTexture(ESkyPart.UpperDayCloud, this
 				.loadTexture("DayClouds.png"));
 		this.sky.setTexture(ESkyPart.UpperNightCloud, this
 				.loadTexture("NightClouds.png"));
 		this.sky.setTexture(ESkyPart.LowerDayCloud, this
 				.loadTexture("DayClouds.png"));
 		this.sky.setTexture(ESkyPart.LowerNightCloud, this
 				.loadTexture("NightClouds.png"));
 	}
 
 	private void buildEffectComponent() {
 		Texture[] flares = new Texture[4];
 		flares[0] = this.loadTexture("flare1.png");
 		flares[1] = this.loadTexture("flare2.png");
 		flares[2] = this.loadTexture("flare3.png");
 		flares[3] = this.loadTexture("flare4.png");
 		this.effects.setFlareTextures(flares);
 	}
 
 	private void buildAstronomyComponent() {
 		this.astronomy.setLengthOfDar(120);
         this.astronomy.setDawnColor(new ColorRGBA(0.9843f, 0.5098f, 0f, 1f));
         this.astronomy.setMoonColor(new ColorRGBA(0.2745f,0.4961f,0.8196f,1f));
 		this.astronomy.setSunTexture(this.loadTexture("Sun.png"));
 		this.astronomy.setMoonTexture(this.loadTexture("Moon.png"));
 		Texture[] textures = new Texture[6];
		textures[0] = this.loadTexture("Stars01.png");
		textures[1] = this.loadTexture("Stars02.png");
		textures[2] = this.loadTexture("Stars03.png");
		textures[3] = this.loadTexture("Stars04.png");
		textures[4] = this.loadTexture("Stars05.png");
		textures[5] = this.loadTexture("Stars06.png");
 		this.astronomy.setStarTextures(textures);
 	}
 
 	private void buildWeatherComponent() {
 		this.weather.setSnowTexture(this.loadTexture("Snowflake.png"));
 	}
 
 	private Texture loadTexture(String name) {
 		URL url = getClass().getClassLoader().getResource(
 				"com/sun/darkstar/example/snowman/data/texture/environment/"
 						+ name);
 		return TextureManager.loadTexture(url, MinificationFilter.Trilinear,
 				MagnificationFilter.Bilinear);
 	}
 }
