 package game;
 
 import java.util.ArrayList;
 
 import physics.BMWM3Properties;
 import physics.CarProperties;
 import physics.tools.Conversion;
 import physics.tools.MathTools;
 
 import com.jme3.app.Application;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.bullet.collision.PhysicsCollisionEvent;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.queue.RenderQueue.ShadowMode;
 
 import de.lessvoid.nifty.elements.render.TextRenderer;
 
 public class FreeForAllScreenState extends AbstractGameScreenState {
 
 	private ArrayList<Car> bots;
 	private boolean win = false;
 
 	public FreeForAllScreenState() {
 		super();
 	}
 
 	@Override
 	public void initialize(AppStateManager stateManager, Application a) {
 		/** init the screen */
 		super.initialize(stateManager, a);
 
 		bots = new ArrayList<Car>();
 
 		initGame();
 		initNiftyControls();
 	}
 
 	protected void initGame() {
 		super.initGame();
 		player.setPhysicsLocation(new Vector3f(0, 27, 700));
 		BMWM3Properties properties = new BMWM3Properties();
 		addBot(new Vector3f(new Vector3f(10, 27, 700)), properties);
 
 		addBot(new Vector3f(new Vector3f(20, 27, 800)), properties);
 		addBot(new Vector3f(new Vector3f(30, 27, 500)), properties);
 		addBot(new Vector3f(new Vector3f(40, 27, 600)), properties);
 		addBot(new Vector3f(new Vector3f(40, 27, 600)), properties);
 		addBot(new Vector3f(new Vector3f(300, 27, 800)), properties);
 		addBot(new Vector3f(new Vector3f(200, 27, 700)), properties);
 
 		/*
 		 * addBot(new Vector3f(new Vector3f(100, 27, 600)), new
 		 * BMWM3Properties()); addBot(new Vector3f(new Vector3f(400, 27, 600)),
 		 * new BMWM3Properties()); addBot(new Vector3f(new Vector3f(500, 27,
 		 * 500)), new BMWM3Properties()); addBot(new Vector3f(new Vector3f(70,
 		 * 27, 650)), new BMWM3Properties()); addBot(new Vector3f(new
 		 * Vector3f(90, 27, 600)), new BMWM3Properties()); addBot(new
 		 * Vector3f(new Vector3f(0, 27, 600)), new BMWM3Properties());
 		 */
 
 		resetCars();
 	}
 	
 	protected void initNiftyControls() {
 		super.initNiftyControls();
 	}
 
 	private void resetCars() {
 		for (Car bot : bots) {
 			bot.getNode().setLocalTranslation(
 					MathTools.randBetween(-2000, 2000), 27,
 					MathTools.randBetween(-2000, 2000));
 			bot.setLife(100.d);
 			bot.removeExplosion();
 		}
 		player.setLife(100.d);
 		player.removeExplosion();
 	}
 
 	protected void addBot(Vector3f location, CarProperties carProperties) {
 		Car bot = new Car(assetManager, carProperties,
 				carProperties.getRandomModel());
 		bot.setPhysicsLocation(location);
 		bot.getNode().setShadowMode(ShadowMode.CastAndReceive);
 
 		rootNode.attachChild(bot.getNode());
 		getPhysicsSpace().add(bot);
 
 		bots.add(bot);
 	}
 
 	@Override
 	public void update(float tpf) {
 		/** any main loop action happens here */
 		if (needReset) {
 			reset();
 			return;
 		}
 		super.update(tpf);
 
 		int nbBotsAlive = 0;
 
 		if (runIsOn) {
 			screen.findElementByName("life").getRenderer(TextRenderer.class)
 					.setText(((Integer) player.getLife()).toString());
 			for (Car bot : bots) {
 				if (bot.isAlive() && player.isAlive()) {
 					nbBotsAlive++;
 
 					bot.getEnginePhysics().setSpeed(
 							Math.abs(Conversion.kmToMiles(bot
 									.getCurrentVehicleSpeedKmHour())));
 					bot.getIA().act();
 					bot.getIA().target(player, 200, 0);
 					bot.accelerate(-(float) bot.getEnginePhysics().getForce() / 5);
 				}
 			}
 			if (nbBotsAlive == 0 && player.isAlive()) {
 				win = true;
 				runIsOn = false;
 				runFinish = true;
 
 			} else if (!player.isAlive()) {
 				win = false;
 				runIsOn = false;
 				runFinish = true;
 			}
 
 		} else {
 			if (runFinish) {
 				if (win) {
 					startLabel.setText("Gagne !");
 					//screen.findElementByName("startTimer")
 					//		.getRenderer(TextRenderer.class).setText("Gagne !");
 				}
 				else {
 					startLabel.setText("Perdu !");
 
 					//screen.findElementByName("startTimer")
 					//		.getRenderer(TextRenderer.class).setText("Perdu !");
 				}
 			}
 
 		}
 	}
 
 	protected void reset() {
 		super.reset();
 		resetCars();
 		runIsOn = false;
 
 		for (Car bot : bots) {
 			bot.stop(0);
 		}
 		player.stop(0);
 	}
 
 	@Override
 	public void collision(PhysicsCollisionEvent event) {
 		super.collision(event);
 	}
 
 }
