 package de.findus.cydonia.main;
 
 import java.net.URL;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.swing.SwingUtilities;
 
 import com.jme3.app.AppTask;
 import com.jme3.app.Application;
 import com.jme3.app.StatsView;
 import com.jme3.asset.AssetNotFoundException;
 import com.jme3.audio.AudioNode;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.BulletAppState.ThreadingType;
 import com.jme3.bullet.collision.PhysicsCollisionEvent;
 import com.jme3.bullet.collision.PhysicsCollisionListener;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.font.BitmapFont;
 import com.jme3.font.BitmapText;
 import com.jme3.light.DirectionalLight;
 import com.jme3.light.Light;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.FastMath;
 import com.jme3.math.Matrix3f;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Transform;
 import com.jme3.math.Vector3f;
 import com.jme3.niftygui.NiftyJmeDisplay;
 import com.jme3.post.FilterPostProcessor;
 import com.jme3.post.filters.FogFilter;
 import com.jme3.post.ssao.SSAOFilter;
 import com.jme3.renderer.RenderManager;
 import com.jme3.renderer.queue.RenderQueue.Bucket;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.Spatial.CullHint;
 import com.jme3.shadow.CompareMode;
 import com.jme3.shadow.DirectionalLightShadowRenderer;
 import com.jme3.shadow.EdgeFilteringMode;
 import com.jme3.system.AppSettings;
 import com.jme3.system.JmeSystem;
 
 import de.findus.cydonia.appstates.GameInputAppState;
 import de.findus.cydonia.appstates.GeneralInputAppState;
 import de.findus.cydonia.appstates.MenuController;
 import de.findus.cydonia.bullet.Bullet;
 import de.findus.cydonia.events.AttackEvent;
 import de.findus.cydonia.events.ChooseTeamEvent;
 import de.findus.cydonia.events.ConnectionDeniedEvent;
 import de.findus.cydonia.events.ConnectionInitEvent;
 import de.findus.cydonia.events.Event;
 import de.findus.cydonia.events.EventListener;
 import de.findus.cydonia.events.EventMachine;
 import de.findus.cydonia.events.HitEvent;
 import de.findus.cydonia.events.InputEvent;
 import de.findus.cydonia.events.JumpEvent;
 import de.findus.cydonia.events.PickupEvent;
 import de.findus.cydonia.events.PlaceEvent;
 import de.findus.cydonia.events.PlayerJoinEvent;
 import de.findus.cydonia.events.PlayerQuitEvent;
 import de.findus.cydonia.events.RespawnEvent;
 import de.findus.cydonia.events.RestartRoundEvent;
 import de.findus.cydonia.events.RoundEndedEvent;
 import de.findus.cydonia.level.Flube;
 import de.findus.cydonia.level.WorldController;
 import de.findus.cydonia.main.ExtendedSettingsDialog.SelectionListener;
 import de.findus.cydonia.messages.BulletPhysic;
 import de.findus.cydonia.messages.InputMessage;
 import de.findus.cydonia.messages.JoinMessage;
 import de.findus.cydonia.messages.MoveableInfo;
 import de.findus.cydonia.messages.PlayerInfo;
 import de.findus.cydonia.messages.PlayerPhysic;
 import de.findus.cydonia.messages.ViewDirMessage;
 import de.findus.cydonia.messages.WorldStateUpdatedMessage;
 import de.findus.cydonia.player.InputCommand;
 import de.findus.cydonia.player.Player;
 import de.findus.cydonia.player.PlayerInputState;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.controls.DropDown;
 import de.lessvoid.nifty.controls.TextField;
 import de.lessvoid.nifty.screen.Screen;
 import de.lessvoid.nifty.screen.ScreenController;
 
 /**
  * GameController is the central controller of the game.
  * It loads all components, changes appStates, organizes the communication between the components.
  * 
  * @author Findus
  */
 public class GameController extends Application implements ScreenController, PhysicsCollisionListener, EventListener{
 	
 	public static final String TEXTURES_PATH = "de/findus/cydonia/textures/";
 	public static final String APPTITLE = "Cydonia Client";
 	
 	/**
 	 * The time in seconds it should take to compensate a deviation from the accurate (=server defined) physical location of an object. 
 	 */
 	private static final float SMOOTHING = 0.2f;
 	
     public static float PLAYER_SPEED = 5f;
     public static float PHYSICS_ACCURACY = (1f / 192);
     
     public static Transform ROTATE90LEFT = new Transform(new Quaternion().fromRotationMatrix(new Matrix3f(1, 0, FastMath.HALF_PI, 0, 1, 0, -FastMath.HALF_PI, 0, 1)));
     
     protected boolean showSettings = true;
     
     protected GameState gamestate;
     
     protected GameConfig gameConfig;
     
     protected WorldController worldController;
     
     protected MenuController menuController;
 
 	protected Node guiNode = new Node("Gui Node");
     
     private  boolean showFps = true;
     protected float secondCounter = 0.0f;
     protected int frameCounter = 0;
     protected BitmapText fpsText;
     protected BitmapFont guiFont;
     protected StatsView statsView;
     
     private BulletAppState bulletAppState;
     private GameInputAppState gameInputAppState;
     
     private Vector3f walkDirection = new Vector3f();
     private boolean left=false, right=false, up=false, down=false;
     
     private Nifty nifty;
     private TextField serverAddressInput;
     private TextField playerNameInput;
     private DropDown<String> teamInput;
     
     private Player player;
     private AudioNode throwSound;
     
     private ConcurrentHashMap<Integer, Player> players;
     private ConcurrentHashMap<Long, Bullet> bullets;
     private ServerConnector connector;
     
     private Thread inputSender;
     
     private EventMachine eventMachine;
     
     private ConcurrentLinkedQueue<Event> eventQueue;
     
     private WorldStateUpdatedMessage latestWorldState;
     
     private long roundStartTime;
     
     private int lastScorerId;
     
     @Override
     public void start() {
         // set some default settings in-case
         // settings dialog is not shown
         boolean loadSettings = false;
         if (settings == null) {
             setSettings(new AppSettings(true));
             loadSettings = true;
             settings.setTitle(APPTITLE);
         }
 
         // show settings dialog
         if (showSettings) {
         	
         	/* *********************************************** */
         	/* show own settings dialog instead of JMESystem's */
         	/* *********************************************** */
             if (!this.showSettingsDialog(settings, loadSettings)) {
                 return;
             }
         }
         
         // limit frame rate
         settings.setFrameRate(100);
         
         //re-setting settings they can have been merged from the registry.
         setSettings(settings);
         super.start();
     }
     
     /**
      * Shows settings dialog.
      * Copied from JmeDesktopSystem, because couldn't change the used dialog other way.
      * 
      * @param sourceSettings
      * @param loadFromRegistry
      * @return
      */
     private boolean showSettingsDialog(AppSettings sourceSettings, final boolean loadFromRegistry) {
         if (SwingUtilities.isEventDispatchThread()) {
             throw new IllegalStateException("Cannot run from EDT");
         }
 
         final AppSettings settings = new AppSettings(false);
         settings.copyFrom(sourceSettings);
         String iconPath = sourceSettings.getSettingsDialogImage();
         final URL iconUrl = JmeSystem.class.getResource(iconPath.startsWith("/") ? iconPath : "/" + iconPath);
         if (iconUrl == null) {
             throw new AssetNotFoundException(sourceSettings.getSettingsDialogImage());
         }
 
         final AtomicBoolean done = new AtomicBoolean();
         final AtomicInteger result = new AtomicInteger();
         final Object lock = new Object();
 
         final SelectionListener selectionListener = new SelectionListener() {
 
             public void onSelection(int selection) {
                 synchronized (lock) {
                     done.set(true);
                     result.set(selection);
                     lock.notifyAll();
                 }
             }
         };
         SwingUtilities.invokeLater(new Runnable() {
 
             public void run() {
                 synchronized (lock) {
                     ExtendedSettingsDialog dialog = new ExtendedSettingsDialog(settings, iconUrl, loadFromRegistry);
                     dialog.setSelectionListener(selectionListener);
                     dialog.showDialog();
                 }
             }
         });
 
         synchronized (lock) {
             while (!done.get()) {
                 try {
                     lock.wait();
                 } catch (InterruptedException ex) {
                 }
             }
         }
 
         sourceSettings.copyFrom(settings);
 
         return result.get() == ExtendedSettingsDialog.APPROVE_SELECTION;
     }
     
     @Override
     public void stop(boolean waitfor) {
     	super.stop(waitfor);
     	System.exit(0);
     }
 
     @Override
     public void initialize() {
         super.initialize();
         
         setPauseOnLostFocus(false);
         
         gameConfig = new GameConfig(true);
         
         eventMachine = new EventMachine();
         
         eventQueue = new ConcurrentLinkedQueue<Event>();
         
         this.gamestate = GameState.LOBBY;
         
         players = new ConcurrentHashMap<Integer, Player>();
         bullets = new ConcurrentHashMap<Long, Bullet>();
         
         Bullet.setAssetManager(assetManager);
         Bullet.preloadTextures();
         
         guiNode.setQueueBucket(Bucket.Gui);
         guiNode.setCullHint(CullHint.Never);
         loadFPSText();
         loadStatsView();
         
         guiViewPort.attachScene(guiNode);
         
         NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager,
     			inputManager,
     			audioRenderer,
     			guiViewPort);
     	nifty = niftyDisplay.getNifty();
     	guiViewPort.addProcessor(niftyDisplay);
 
     	menuController = new MenuController(this);
     	menuController.actualizeScreen();
     	
     	connector = new ServerConnector(this, eventMachine);
     	
     	gameInputAppState = new GameInputAppState(this);
     	
     	GeneralInputAppState generalInputAppState = new GeneralInputAppState(this);
     	stateManager.attach(generalInputAppState);
 
     	bulletAppState = new BulletAppState();
     	bulletAppState.setEnabled(false);
         bulletAppState.setThreadingType(ThreadingType.PARALLEL);
         stateManager.attach(bulletAppState);
         bulletAppState.getPhysicsSpace().setMaxSubSteps(16);
         bulletAppState.getPhysicsSpace().setAccuracy(PHYSICS_ACCURACY);
         bulletAppState.getPhysicsSpace().addCollisionListener(this);
         
 //        bulletAppState.getPhysicsSpace().enableDebug(assetManager);
         
         worldController = new WorldController(assetManager, bulletAppState.getPhysicsSpace());
         
         viewPort.attachScene(worldController.getRootNode());
 //        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
         viewPort.setBackgroundColor(new ColorRGBA(0f, 0f, 0f, 1f));
 
         if(settings.getInteger("shadowLevel") > 0) {
         	for(Light l : worldController.getLights()) {
         		if(l instanceof DirectionalLight) {
         			DirectionalLightShadowRenderer shadowRenderer = new DirectionalLightShadowRenderer(assetManager, 1024, settings.getInteger("shadowLevel"));
         			shadowRenderer.setLight((DirectionalLight) l);
         			shadowRenderer.setEdgeFilteringMode(EdgeFilteringMode.PCF8);
         			shadowRenderer.setShadowCompareMode(CompareMode.Hardware);
         			viewPort.addProcessor(shadowRenderer);
         		}
         	}
         }
         
         FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
         
         FogFilter fog=new FogFilter();
         fog.setFogColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 0.5f));
         fog.setFogDistance(100);
         fog.setFogDensity(1.5f);
 //        fpp.addFilter(fog);
         
 //        SSAOFilter ssaoFilter = new SSAOFilter();
 //        fpp.addFilter(ssaoFilter);
         
 //        FXAAFilter fxaaFilter = new FXAAFilter();
 //        fpp.addFilter(fxaaFilter);
         
         viewPort.addProcessor(fpp);
         
         cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.3f, 1000f);
         
         eventMachine.registerListener(this);
         
         throwSound = new AudioNode(assetManager, "de/findus/cydonia/sounds/throw_001.wav", false);
         throwSound.setLooping(false);
 		throwSound.setPositional(true);
 		throwSound.setLocalTranslation(Vector3f.ZERO);
 		throwSound.setVolume(1);
 		worldController.attachObject(throwSound);
     }
     
     public void connect() {
     	gamestate = GameState.LOADING;
     	menuController.actualizeScreen();
     	String serveraddress = this.serverAddressInput.getRealText();
     	connector.connectToServer(serveraddress, 6173);
     }
     
     
 
 	/**
      * Starts the actual game eg. the game loop.
      */
     public void startGame(String level) {
         worldController.loadWorld(level);
         
     	String playername = this.playerNameInput.getRealText();
     	int team = this.teamInput.getSelectedIndex() + 1;
     	player = new Player(connector.getConnectionId(), assetManager);
     	player.setName(playername);
     	player.setTeam(team);
     	players.put(player.getId(), player);
     	
         JoinMessage join = new JoinMessage(player.getId(), player.getName());
     	connector.sendMessage(join);
         
     	InputMessage chooseteam = null;
     	if(team == 1) {
     		chooseteam = new InputMessage(player.getId(), InputCommand.CHOOSETEAM1, true);
     	}else if(team == 2) {
     		chooseteam = new InputMessage(player.getId(), InputCommand.CHOOSETEAM2, true);
     	}
     	connector.sendMessage(chooseteam);
     	
     	bulletAppState.setEnabled(true);
     	gamestate = GameState.SPECTATE;
     	stateManager.attach(gameInputAppState);
     	menuController.actualizeScreen();
     }
     
     /**
      * Resumes the game after pausing.
      */
     public void resumeGame() {
     	gamestate = GameState.RUNNING;
     	stateManager.attach(gameInputAppState);
     	startInputSender();
     	menuController.actualizeScreen();
     }
     
     /**
      * pauses the game and opens the menu.
      */
     public void openMenu() {
     	stateManager.detach(gameInputAppState);
     	gamestate = GameState.MENU;
     	menuController.actualizeScreen();
     	stopInputSender();
     }
     
     public void stopGame() {
     	stopInputSender();
     	connector.disconnectFromServer();
     	stop();
     }
     
     public void gameOver() {
     	gamestate = GameState.SPECTATE;
     	menuController.actualizeScreen();
     	stopInputSender();
     }
 
 	@Override
     public void update() {
         super.update(); // makes sure to execute AppTasks
         if (speed == 0 || paused) {
             return;
         }
 
         float tpf = timer.getTimePerFrame() * speed;
 
         if (showFps) {
             secondCounter += timer.getTimePerFrame();
             frameCounter ++;
             if (secondCounter >= 1.0f) {
                 int fps = (int) (frameCounter / secondCounter);
                 fpsText.setText("Frames per second: " + fps);
                 secondCounter = 0.0f;
                 frameCounter = 0;
             }          
         }
 
         // update states
         stateManager.update(tpf);
 
         // update game specific things
         handleEvents();
         useLatestWorldstate();
         movePlayers(tpf);
         menuController.updateHUD();
         
         // update world and gui
         worldController.updateLogicalState(tpf);
         guiNode.updateLogicalState(tpf);
         worldController.updateGeometricState();
         guiNode.updateGeometricState();
 
         // render states
         stateManager.render(renderManager);
         renderManager.render(tpf, context.isRenderable());
         simpleRender(renderManager);
         stateManager.postRender();
     }
     
 	private void useLatestWorldstate() {
 		WorldStateUpdatedMessage worldState;
 		if(latestWorldState != null) {
 			synchronized (latestWorldState) {
 				worldState = latestWorldState;
 				latestWorldState = null;
 			}
 
 			for (PlayerPhysic physic : worldState.getPlayerPhysics()) {
 				Player p = players.get(physic.getId());
 				if(p != null) {
 					p.setExactLoc(physic.getTranslation());
 					p.setViewDir(physic.getOrientation());
 				}
 			}
 
 			for (BulletPhysic physic : worldState.getBulletPhysics()) {
 				Bullet b = bullets.get(physic.getId());
 				if(b == null) {
 					b = new Bullet(physic.getId(), physic.getSourceid());
 					bullets.put(b.getId(), b);
 					worldController.attachObject(b.getModel());
 				}
 				if(b != null) {	
 					b.setExactLoc(physic.getTranslation());
 					b.getControl().setPhysicsLocation(physic.getTranslation());
 					b.getControl().setLinearVelocity(physic.getVelocity());
 				}
 			}
 		}
 	}
 
 	private void handleEvents() {
 		Event e = null;
 		while ((e = eventQueue.poll()) != null) {
 			if (e instanceof ConnectionDeniedEvent) {
 				System.out.println("Server denied connection! Reason: '" + ((ConnectionDeniedEvent) e).getReason() + "'");
 				gamestate = GameState.LOBBY;
 				menuController.actualizeScreen();
 				clean();
 				connector.disconnectFromServer();
 			}else if (e instanceof ConnectionInitEvent) {
 				startGame(((ConnectionInitEvent) e).getLevel());
 			}else if (e instanceof AttackEvent) {
 				AttackEvent attack = (AttackEvent) e;
 				Player p = players.get(attack.getPlayerid());
 				attack(p, attack.getBulletid());
 			}else if (e instanceof HitEvent) {
 				HitEvent hit = (HitEvent) e;
 				hitPlayer(hit.getAttackerPlayerid(), hit.getVictimPlayerid(), hit.getHitpoints());
 			}else if (e instanceof PickupEvent) {
 				PickupEvent pickup = (PickupEvent) e;
 				Player p = players.get(pickup.getPlayerid());
 				Flube flube = worldController.getFlube(pickup.getMoveableid());
 				pickup(p, flube);
 			}else if (e instanceof PlaceEvent) {
 				PlaceEvent place = (PlaceEvent) e;
 				Player p = players.get(place.getPlayerid());
 				Vector3f loc = place.getLocation();
 				place(p, loc);
 			}else if (e instanceof PlayerJoinEvent) {
 				PlayerJoinEvent join = (PlayerJoinEvent) e;
 				int playerid = join.getPlayerId();
 				if(player.getId() != playerid) {
 					joinPlayer(playerid, join.getPlayername());
 				}
 			}else if (e instanceof ChooseTeamEvent) {
 				ChooseTeamEvent choose = (ChooseTeamEvent) e;
 				Player p = players.get(choose.getPlayerId());
 				chooseTeam(p, choose.getTeam());
 			}else if (e instanceof RespawnEvent) {
 				RespawnEvent respawn = (RespawnEvent) e;
 				Player p = players.get(respawn.getPlayerid());
 				respawn(p);
 			}else if (e instanceof PlayerQuitEvent) {
 				PlayerQuitEvent quit = (PlayerQuitEvent) e;
 				Player p = players.get(quit.getPlayerId());
 				quitPlayer(p);
 			}else if (e instanceof JumpEvent) {
 				JumpEvent jump = (JumpEvent) e;
 				Player p = players.get(jump.getPlayerid());
 				jump(p);
 			}else if (e instanceof InputEvent) {
     			InputEvent input = (InputEvent) e;
     			// only use inputs from other players, not our own inputs, that are sent back to us from the server
     			if(player.getId() != input.getPlayerid()) {
     				Player p = players.get(input.getPlayerid());
     				p.handleInput(input.getCommand(), input.isValue());
     			}
     		}else if (e instanceof RestartRoundEvent) {
 				for (Player p : players.values()) {
 					if(p.isAlive()) {
 						killPlayer(p);
 					}
 					p.setInventory(-1);
 				}
 				removeAllBullets();
 				worldController.resetWorld();
 				this.roundStartTime = System.currentTimeMillis();
 			}else if (e instanceof RoundEndedEvent) {
 				RoundEndedEvent roundEnded = (RoundEndedEvent) e;
 				for (Player p : players.values()) {
 					p.setInputState(new PlayerInputState());
 					if(p.getId() == roundEnded.getWinnerid()) {
 						p.setScores(p.getScores() + 1);
 					}
 				}
 				lastScorerId = roundEnded.getWinnerid();
 				gamestate = GameState.ROUNDOVER;
 				menuController.actualizeScreen();
 			}
 		}
 	}
 
 	@Override
 	public void newEvent(Event e) {
 		eventQueue.offer(e);
 	}
 	
 	private void removeAllBullets() {
 		for (Bullet b : bullets.values()) {
 			removeBullet(b);
 		}
 	}
 	
 	private void removeBullet(Bullet b) {
 		b.getModel().removeFromParent();
 		bullets.remove(b.getId());
 	}
 
 	private void clean() {
 		players.clear();
 	}
 	
 	public void setlatestWorldstate(WorldStateUpdatedMessage update) {
 		latestWorldState = update;
 	}
 	
 	public void setInitialState(GameConfig config, PlayerInfo[] pinfos, MoveableInfo[] minfos) {
 		gameConfig.copyFrom(config);
 		
 		for (PlayerInfo info : pinfos) {
 			if(player.getId() == info.getPlayerid()) continue;
			final Player p = new Player(info.getPlayerid(), assetManager);
 			p.setName(info.getName());
 			p.setTeam(info.getTeam());
 			p.setAlive(info.isAlive());
 			p.setHealthpoints(info.getHealthpoints());
 			p.setScores(info.getScores());
 			players.put(p.getId(), p);
 			if(p.isAlive()) {
 				p.getControl().setPhysicsLocation(worldController.getLevel().getSpawnPoint(p.getTeam()).getPosition());
				enqueue(new Callable<String>() {
					public String call() {
						worldController.attachPlayer(p);
						return null;
					}
				});
				
 			}
 		}
 		for (MoveableInfo info : minfos) {
 			Flube m = worldController.getFlube(info.getId());
 			if(m != null) {
 				m.getControl().setPhysicsLocation(info.getLocation());
 				if(!info.isInWorld()) {
 					worldController.detachFlube(m);
 				}
 			}
 		}
 	}
 	
 	public void handlePlayerInput(InputCommand command, boolean value) {
 		// send input to server if necessary
 		if(InputCommand.forwarded.contains(command)) {
 			InputMessage msg = new InputMessage(player.getId(), command, value);
 			connector.sendMessage(msg);
 		}
 		
 		switch (command) {
 		case SCOREBOARD:
 			if(value) {
 				menuController.showScoreboard();
 			} else {
 				menuController.hideScoreboard();
 			}
 			break;
 		case EXIT:
 			if(value) {
 				if(gamestate == GameState.RUNNING || gamestate == GameState.SPECTATE || gamestate == GameState.ROUNDOVER) {
 					openMenu();
 				}else if (gamestate == GameState.MENU) {
 					resumeGame();
 				}
 			}
 			break;
 
 		default:
 			player.handleInput(command, value);
 			break;
 		}
 	}
 
     /**
      * Moves the player according to user input state.
      * @param tpf time per frame
      */
 	private void movePlayers(float tpf) {
         
 		for (Player p : players.values()) {
 			if(p.getId() == player.getId()) {
 				p.setViewDir(cam.getDirection());
 				listener.setLocation(cam.getLocation());
 			    listener.setRotation(cam.getRotation());
 			}
 			
 			Vector3f viewDir = p.getControl().getViewDirection();
 			Vector3f viewLeft = new Vector3f();
 			ROTATE90LEFT.transformVector(viewDir, viewLeft);
 
 			walkDirection.set(0, 0, 0);
 			if(p.getInputState().isLeft()) walkDirection.addLocal(viewLeft);
 			if(p.getInputState().isRight()) walkDirection.addLocal(viewLeft.negate());
 			if(p.getInputState().isForward()) walkDirection.addLocal(viewDir);
 			if(p.getInputState().isBack()) walkDirection.addLocal(viewDir.negate());
 
 			
 			walkDirection.normalizeLocal().multLocal(PLAYER_SPEED);
 			
 			Vector3f correction = p.getExactLoc().subtract(p.getControl().getPhysicsLocation()).divide(SMOOTHING);
 			walkDirection.addLocal(correction);
 
 			walkDirection.multLocal(PHYSICS_ACCURACY);
 			p.getControl().setWalkDirection(walkDirection);
 			
 			if(p.getId() == connector.getConnectionId()) {
 				cam.setLocation(p.getEyePosition());
 			}
 		}
 		
     }
 
     public void simpleRender(RenderManager rm) {
         //TODO: add render code
     }
 
     @Override
 	public void collision(PhysicsCollisionEvent e) {
     	Spatial bullet = null;
     	Spatial other = null;
 
     	if(e.getNodeA() != null) {
     		Boolean sticky = e.getNodeA().getUserData("Sticky");
     		if (sticky != null && sticky.booleanValue() == true) {
     			bullet = e.getNodeA();
     			other = e.getNodeB();
     		}
     	}
     	if (e.getNodeB() != null) {
     		Boolean sticky = e.getNodeB().getUserData("Sticky");
     		if (sticky != null && sticky.booleanValue() == true) {
     			bullet = e.getNodeB();
     			other = e.getNodeA();
     		}
     	}
 
     	if(bullet != null && other != null) {
     		worldController.detachObject(bullet);
 			bullet.removeControl(RigidBodyControl.class);
     		if(other.getName().startsWith("player")) {
     			// Hit Player not here. only when message from server.
     		}else {
     			if (other instanceof Node) {
     				((Node) other).attachChild(bullet);
     			}else {
     				other.getParent().attachChild(bullet);
     			}
     		}
     	}
     }
 
 	private void hitPlayer(int sourceid, int victimid, double hitpoints) {
 		Player victim = players.get(victimid);
 		if(victim == null) {
 			return;
 		}
 		double hp = victim.getHealthpoints();
 		hp -= hitpoints;
 		System.out.println("hit - new hp: " + hp);
 		if(hp <= 0) {
 			hp = 0;
 			this.killPlayer(victim);
 			Player source = this.players.get(sourceid);
 			if(source != null) {
 				source.setScores(source.getScores() + 1);
 			}
 		}
 		victim.setHealthpoints(hp);
 	}
 	
 	private void killPlayer(Player p) {
 		if(p == null) return;
 		worldController.detachPlayer(p);
 		p.setAlive(false);
 		if(p.getId() == player.getId()) {
 			gameOver();
 		}
 	}
 	
 	private void attack(Player p, long bulletid) {
 		if(p == null) return;
 
 		p.setLastShot(System.currentTimeMillis());
 		Bullet b = new Bullet(bulletid, p.getId());
 		b.getControl().setPhysicsLocation(p.getControl().getPhysicsLocation());
 		b.getControl().setPhysicsLocation(p.getControl().getPhysicsLocation().add(p.getControl().getViewDirection().normalize()));
 		bullets.put(b.getId(), b);
 		worldController.attachObject(b.getModel());
 		
 		throwSound.setLocalTranslation(p.getControl().getPhysicsLocation());
 		throwSound.play();
 	}
 	
 	private void pickup(Player p, Flube flube) {
 		if(flube != null) {
 			worldController.detachFlube(flube);
 			if(p != null) {
 				p.setInventory((Long) flube.getId());
 			}
 		}
 	}
 	
 	private void place(Player p, Vector3f loc) {
 		if(p == null) return;
 
 		if(p.getInventory() >= 0) {
 			Flube m = worldController.getFlube(p.getInventory());
 			m.getControl().setPhysicsLocation(loc);
 			worldController.attachFlube(m);
 			p.setInventory(-1);
 		}
 	}
 	
 	private void jump(Player p) {
 		if(p == null) return;
 		p.jump();
 	}
 	
 	private void joinPlayer(int playerid, String playername) {
 		Player p = new Player(playerid, assetManager);
 		p.setName(playername);
 		players.put(playerid, p);
 	}
 	
 	private void quitPlayer(Player p) {
 		if(p == null) return;
 		worldController.detachPlayer(p);
 		players.remove(p.getId());
 	}
 	
 	private void respawn(final Player p) {
 		if(p == null) return;
 		p.setHealthpoints(100);
 		p.setAlive(true);
 
 		p.getControl().setPhysicsLocation(worldController.getLevel().getSpawnPoint(p.getTeam()).getPosition());
 		worldController.attachPlayer(p);
 		if(p.getId() == player.getId()) {
 			p.getModel().setCullHint(CullHint.Always);
 			resumeGame();
 		}
 	}
 	
 	private void chooseTeam(Player p, int team) {
 		if(p == null) return;
 		p.setTeam(team);
 	}
 	
 	public long getRemainingTime() {
 		long passedTime = System.currentTimeMillis() - roundStartTime;
 		return gameConfig.getLong("mp_roundtime") * 1000 - passedTime;
 	}
 	
 	public Player getLastScorer() {
 		return players.get(lastScorerId);
 	}
 
 	public String getScores() {
 		StringBuilder sb_team1 = new StringBuilder();
 		StringBuilder sb_team2 = new StringBuilder();
 		for (Player p : players.values()) {
 			if(p.getTeam() == 1) {
 				sb_team1.append("\n" + p.getName() + "\t\t" + p.getScores());
 			}else if(p.getTeam() == 2) {
 				sb_team2.append("\n" + p.getName() + "\t\t" + p.getScores());
 			}
 		}
 		StringBuilder sb = new StringBuilder();
 		sb.append("Team 1 \t\t Scores");
 		sb.append(sb_team1);
 		sb.append("\n\n\n\n");
 		sb.append("Team 2 \t\t Scores");
 		sb.append(sb_team2);
 		return sb.toString();
 	}
 	
 	public void scoreboard(boolean show) {
 		if(show) {
 			menuController.showScoreboard();
 		}else {
 			menuController.hideScoreboard();
 		}
 	}
 	
 	/**
 	 * Attaches FPS statistics to guiNode and displays it on the screen.
 	 */
 	public void loadFPSText() {
 	    guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
 	    fpsText = new BitmapText(guiFont, false);
 	    fpsText.setLocalTranslation(0, fpsText.getLineHeight(), 0);
 	    fpsText.setText("Frames per second");
 	    guiNode.attachChild(fpsText);
 	}
 
 	/**
 	 * Attaches Statistics View to guiNode and displays it on the screen above FPS statistics line.
 	 */
 	public void loadStatsView() {
 		statsView = new StatsView("Statistics View", assetManager, renderer.getStatistics());
 		//         move it up so it appears above fps text
 		statsView.setLocalTranslation(0, fpsText.getLineHeight(), 0);
 		guiNode.attachChild(statsView);
 	}
 
 	/**
 	 * Sets the displayFPS property.
 	 * @param show if true fps are painted
 	 */
 	public void setDisplayFps(boolean show) {
 		showFps = show;
 		fpsText.setCullHint(show ? CullHint.Never : CullHint.Always);
 	}
 
 	/**
 	 * Sets the displayStats property.
 	 * @param show if true stats are painted
 	 */
     public void setDisplayStatView() {
         boolean show = !statsView.isEnabled();
     	statsView.setEnabled(show);
         statsView.setCullHint(show ? CullHint.Never : CullHint.Always);
     }
 
 	@Override
 	public void bind(Nifty nifty, Screen screen) {
 		this.serverAddressInput = screen.findNiftyControl("serveraddress", TextField.class);
 		this.playerNameInput = screen.findNiftyControl("playername", TextField.class);
 		this.teamInput = screen.findNiftyControl("team", DropDown.class);
 		
 		this.teamInput.addItem("Team 1");
 		this.teamInput.addItem("Team 2");
 	}
 
 	@Override
 	public void onEndScreen() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onStartScreen() {
 		// TODO Auto-generated method stub
 	}
 	
 	public GameState getGamestate() {
 		return gamestate;
 	}
 
 	public Player getPlayer() {
 		return this.player;
 	}
 
 	public boolean isLeft() {
 		return left;
 	}
 
 	public void setLeft(boolean left) {
 		this.left = left;
 	}
 
 	public boolean isRight() {
 		return right;
 	}
 
 	public void setRight(boolean right) {
 		this.right = right;
 	}
 
 	public boolean isUp() {
 		return up;
 	}
 
 	public void setUp(boolean up) {
 		this.up = up;
 	}
 
 	public boolean isDown() {
 		return down;
 	}
 
 	public void setDown(boolean down) {
 		this.down = down;
 	}
 
 	/**
 	 * Retrieves nifty.
 	 * @return nifty object
 	 */
 	public Nifty getNifty() {
 		return nifty;
 	}
 
 	/**
      * Retrieves guiNode
      * @return guiNode Node object
      */
     public Node getGuiNode() {
         return guiNode;
     }
     
     /**
 	 * Starts the input sender loop.
 	 */
 	public void startInputSender() {
 		inputSender = new Thread(new InputSenderLoop());
 		inputSender.start();
 	}
     
     /**
 	 * Stops the input sender loop.
 	 */
 	public void stopInputSender() {
 		if(inputSender != null) {
 			inputSender.interrupt();
 		}
 	}
     
     /**
 	 * This class is used to send the user input state to the server in constant time intervals.
 	 * @author Findus
 	 *
 	 */
 	private class InputSenderLoop implements Runnable {
 
 		@Override
 		public void run() {
 			while(!Thread.interrupted()) {
 				ViewDirMessage msg = new ViewDirMessage();
 				msg.setPlayerid(player.getId());
 				msg.setViewDir(player.getViewDir());
 				
 				connector.sendMessage(msg);
 				
 				try {
 					Thread.sleep(50);
 				} catch (InterruptedException e) {
 					break;
 				}
 			}
 		}
 		
 	}
 }
