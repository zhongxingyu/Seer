 package game;
 
 import java.util.ArrayList;
 import java.util.concurrent.TimeUnit;
 
 import physics.BMWM3Properties;
 import physics.CarProperties;
 import physics.DodgeViperProperties;
 import physics.tools.Conversion;
 import physics.tools.MathTools;
 import save.Comptes;
 import save.ProfilCurrent;
 
 import com.jme3.app.Application;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.bullet.collision.PhysicsCollisionEvent;
 import com.jme3.bullet.collision.shapes.BoxCollisionShape;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.queue.RenderQueue.ShadowMode;
 import com.jme3.scene.Spatial;
 
 import de.lessvoid.nifty.elements.render.TextRenderer;
 
 public class FreeForAllScreenState extends AbstractGameScreenState {
 
 	private ArrayList<Car> bots;
 	private boolean win = false;
 
	private boolean flagNeedClear = false;
 	private boolean givePt = false;
 	private boolean bonus = false;
 	private int nbBotDead = 0;
 	private int argent = 0;
 
 	private DigitalDisplay digitalLife;
 	private DigitalDisplay digitalRemainingBots;
 
 	private int nbBots;
 	private int nbBotsAlive = 0;
 
 	public FreeForAllScreenState() {
 		super();
 	}
 
 	@Override
 	public void initialize(AppStateManager stateManager, Application a) {
 		/** init the screen */
 		super.initialize(stateManager, a);
 
 		bots = new ArrayList<Car>();
 
 		initNiftyControls();
 		try {
 			initGame();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		this.reset();
 	}
 
 	@Override
 	public void onEndScreen() {
 		super.onEndScreen();
 	}
 	
 	protected void initGame() throws Exception {
 		super.initGame();
 
 		player.setPhysicsLocation(new Vector3f(0, 27, 700));
 
 		nbBots = 8;
 		digitalRemainingBots.setText(nbBots + "/" + nbBots);
 		nbBotsAlive = nbBots;
 		
 		for(int i=0;i<nbBots; i++) {
 			//XXX
 			/*int Min = 1;
 			int Max = Comptes.getListCar().size()-1;
 			int random = Min + (int)(Math.random() * ((Max - Min) + 1));
 			CarProperties properties = Comptes.getListCar().get(random);*/
 			BMWM3Properties properties = new BMWM3Properties();
 			addBot(new Vector3f(new Vector3f(i*50, 27, i*50)), properties);
 
 		/*for (int i = 0; i < nbBots; i++) {
 			// XXX
 			CarProperties properties = (ProfilCurrent.getInstance() == null) ? new BMWM3Properties()
 					: ProfilCurrent.getInstance().getCar()
 							.get(ProfilCurrent.getInstance().getChoixCar());
 			addBot(new Vector3f(new Vector3f(i * 50, 27, i * 50)), properties);*/
 		}
 
 		resetCars();
 
 		addObjects();
 	}
 
 	protected void initNiftyControls() {
 		digitalLife = new DigitalDisplay(nifty, screen, "life", 100);
 		digitalRemainingBots = new DigitalDisplay(nifty, screen,
 				"digital_remaining_bots", 500);
 	}
 
 	private void resetCars() {
 		for (Car bot : bots) {
 			bot.getNode().setLocalTranslation(
 					MathTools.randBetween(-2000, 2000), 27,
 					MathTools.randBetween(-2000, 2000));
 			bot.setLife(100.d);
 			bot.removeExplosion();
 		}
 		player.setLife(100);
 		player.removeExplosion();
 	}
 
 	protected void addBot(Vector3f location, CarProperties carProperties) {
 		Car bot = new Car(assetManager, carProperties,
 				carProperties.getRandomModel());
 		bot.setPhysicsLocation(location);
 		bot.getNode().setShadowMode(ShadowMode.CastAndReceive);
 
 		rootNode.attachChild(bot.getNode());
 		getPhysicsSpace().add(bot);
 
 		bot.setLife(100);
 		bots.add(bot);
 	}
 
 	protected void addObjects() {
 		BoxCollisionShape treeShape = new BoxCollisionShape(new Vector3f(0.5f,
 				10.f, 1.f));
 		BoxCollisionShape plotShape = new BoxCollisionShape(new Vector3f(0.5f,
 				0.6f, 1.f));
 
 		Spatial node2 = assetManager.loadModel("Models/Tree/Tree.mesh.j3o");
 		node2.setShadowMode(ShadowMode.Cast);
 		node2.setName("Tree");
 		for (int i = 0; i < 60; i++) {
 			Spatial node = node2.clone();
 			node.scale(5);
 			node.setLocalTranslation(MathTools.randBetween(-1000, 1000), 25,
 					MathTools.randBetween(-1000, 1000));
 
 			RigidBodyControl controlTree = new RigidBodyControl(treeShape, 0.f);
 			controlTree.setUserObject("Tree");
 
 			node.addControl(controlTree);
 			getPhysicsSpace().add(node);
 
 			rootNode.attachChild(node);
 
 		}
 
 		Spatial node = assetManager
 				.loadModel("Models/cone_altglass/cone_altglass.j3o");
 		node.scale(0.2f);
 		node.setLocalTranslation(0, 27, 200);
 		for (int i = 0; i < 200; i++) {
 			Spatial plot = node.clone();
 			plot.setLocalTranslation(MathTools.randBetween(-1000, 1000), 27,
 					MathTools.randBetween(-1000, 1000));
 
 			RigidBodyControl controlPlot = new RigidBodyControl(plotShape);
 			controlPlot.setMass(0.75f);
 			controlPlot.setFriction(10);
 
 			plot.addControl(controlPlot);
 			getPhysicsSpace().add(plot);
 			rootNode.attachChild(plot);
 		}
 	}
 
 	@Override
 	public void update(float tpf) {
 		/** any main loop action happens here */
 		if (needReset) {
 			reset();
 			return;
 		}
 		super.update(tpf);
		
		if (flagNeedClear)	{
			digitalStart.setText("Ready ?");
			flagNeedClear = false;
		}
 
 		nbBotsAlive = 0;
 		String sTimer;
 		String conclusion;
 
 		if (runIsOn) {
 			nbBotsAlive = 0;
 			digitalLife.setText(((Integer) player.getLife()).toString());
 			for (Car bot : bots) {
 				if (bot.isAlive() && player.isAlive()) {
 					++nbBotsAlive;
 
 					bot.getEnginePhysics().setSpeed(
 							Math.abs(Conversion.kmToMiles(bot
 									.getCurrentVehicleSpeedKmHour())));
 					bot.getIA().act();
 					bot.getIA().target(player, 200, 0);
 					bot.accelerate(-(float) bot.getEnginePhysics().getForce() / 5);
 				} else {
 					bot.stop(3000);
 				}
 			}
 			digitalRemainingBots.setText(nbBotsAlive + "/" + nbBots);
 
 			if (nbBotsAlive == 0 && player.isAlive()) {
 				win = true;
 				runIsOn = false;
 				runFinish = true;
 			} else if (!player.isAlive()) {
 				digitalLife.setText("0");
 				win = false;
 				runIsOn = false;
 				runFinish = true;
 			}
 			timePlayer = (System.currentTimeMillis() - startTime);
 
 			sTimer = String.format("%d : %d",
 					TimeUnit.MILLISECONDS.toSeconds(timePlayer),
 					(timePlayer % 1000) / 10);
 
 			screen.findElementByName("timer").getRenderer(TextRenderer.class)
 					.setText(sTimer);
 		} else {
 			long secondes = TimeUnit.MILLISECONDS.toSeconds(timePlayer);
 			long millisec = (timePlayer % 1000) / 10;
 			if (runFinish) {
 				if (win) {
 					if (givePt == false) {
 						argent = 500;
 						givePt = true;
 						if (ProfilCurrent.getInstance() != null)
 							ProfilCurrent.getInstance().setMonnaie(
 									ProfilCurrent.getInstance().getMonnaie()
 											+ argent);
 					}
 					conclusion = "Gagne! 500 Eur";
 				} else {
 					int nbbot = 0;
 					int nbbotlive = 0;
 
 					for (Car bot : bots) {
 						++nbbot;
 						if (bot.isAlive())
 							++nbbotlive;
 					}
 					if (givePt == false) {
 						nbBotDead = nbbot - nbbotlive;
 						if (secondes > 0) {
 							argent = (int) ((nbBotDead * 45000) / secondes);
 						} else {
 							argent = 0;
 						}
 						givePt = true;
 						if (ProfilCurrent.getInstance() != null)
 							ProfilCurrent.getInstance().setMonnaie(
 									ProfilCurrent.getInstance().getMonnaie()
 											+ argent);
 					}
 					conclusion = "Perdu! " + argent + " Eur";
					digitalLife.setText("0");
 
 				}
 				String text = String.format("%d : %d", secondes, millisec);
 
 				if (ProfilCurrent.getInstance() != null) {
 					if (!ProfilCurrent.getInstance().getTimefree().equals("")) {
 						String tps[] = ProfilCurrent.getInstance()
 								.getTimefree().split(" : ");
 						// bat le nombre de bot tué
 						if (ProfilCurrent.getInstance().getCardead() < nbBotDead) {
 							ProfilCurrent.getInstance().setTimefree(text);
 							ProfilCurrent.getInstance().setCardead(nbBotDead);
 							ProfilCurrent.getInstance()
 									.setMonnaie(
 											ProfilCurrent.getInstance()
 													.getMonnaie() + 1000);
 							bonus = true;
 						}
 						// bat le temps de bot tué
 						else if (ProfilCurrent.getInstance().getCardead() == nbBotDead) {
 							if (Long.parseLong(tps[0]) > secondes
 									|| (Long.parseLong(tps[0]) == secondes && Long
 											.parseLong(tps[1]) > millisec)) {
 								ProfilCurrent.getInstance().setTimefree(text);
 								ProfilCurrent.getInstance().setMonnaie(
 										ProfilCurrent.getInstance()
 												.getMonnaie() + 1000);
 								bonus = true;
 							}
 						}
 					} else {
 						// Premier score
 						ProfilCurrent.getInstance().setTimefree(text);
 						ProfilCurrent.getInstance().setCardead(nbBotDead);
 					}
 					Comptes.modifier(ProfilCurrent.getInstance());
 					Comptes.Enregistrer();
 				} // Fin Enregistrement Profil
 
 				if (bonus)
 					conclusion = conclusion + "\nBonus 1000 Eur!";
 				digitalStart.setText(conclusion);
			} else {
				digitalLife.setText("100");
 			}
 
 		}
 	}
 
 	protected void reset() {
 		super.reset();
 		resetCars();
 		runIsOn = false;
 		givePt = false;
 		bonus = false;
		flagNeedClear = true;
 		nbBotDead = 0;
 		for (Car bot : bots) {
 			bot.stop(0);
 		}
 		player.stop(0);
 		screen.findElementByName("timer").getRenderer(TextRenderer.class)
 				.setText("0 : 0");
 		player.setLife(100);
 		
 		digitalRemainingBots.setText(nbBots + "/" + nbBots);
 		digitalLife.setText("100");
 		digitalStart.setText("ready ?");
 	}
 
 	@Override
 	public void collision(PhysicsCollisionEvent event) {
 		super.collision(event);
 	}
 
 }
