 package de.findus.cydonia.main;
 
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import com.jme3.app.Application;
 import com.jme3.app.StatsView;
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
 import com.jme3.post.filters.FXAAFilter;
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
 import de.findus.cydonia.level.Moveable;
 import de.findus.cydonia.level.WorldController;
 import de.findus.cydonia.messages.BulletPhysic;
 import de.findus.cydonia.messages.MoveableInfo;
 import de.findus.cydonia.messages.PlayerInfo;
 import de.findus.cydonia.messages.PlayerPhysic;
 import de.findus.cydonia.messages.ViewDirMessage;
 import de.findus.cydonia.messages.WorldStateUpdatedMessage;
 import de.findus.cydonia.player.InputCommand;
 import de.findus.cydonia.player.Player;
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
 	
 	/**
 	 * The time in seconds it should take to compensate a deviation from the accurate (=server defined) physical location of an object. 
 	 */
 	private static final float SMOOTHING = 0.2f;
 	
     public static float PLAYER_SPEED = 5f;
     public static float PHYSICS_ACCURACY = (1f / 192);
     
     public static Transform ROTATE90LEFT = new Transform(new Quaternion().fromRotationMatrix(new Matrix3f(1, 0, FastMath.HALF_PI, 0, 1, 0, -FastMath.HALF_PI, 0, 1)));
     
     protected boolean showSettings = true;
     
     protected GameState gamestate;
     
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
     
     @Override
     public void start() {
         // set some default settings in-case
         // settings dialog is not shown
         boolean loadSettings = false;
         if (settings == null) {
             setSettings(new AppSettings(true));
             loadSettings = true;
         }
 
         // show settings dialog
         if (showSettings) {
             if (!JmeSystem.showSettingsDialog(settings, loadSettings)) {
                 return;
             }
         }
         
         // limit frame rate
         settings.setFrameRate(100);
         
         //re-setting settings they can have been merged from the registry.
         setSettings(settings);
         super.start();
     }
     
     @Override
     public void stop(boolean waitfor) {
     	super.stop(waitfor);
     	System.exit(0);
     }
 
     @Override
     public void initialize() {
         super.initialize();
         
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
     	
     	gameInputAppState = new GameInputAppState(this, eventMachine);
     	
 
     	bulletAppState = new BulletAppState();
     	bulletAppState.setEnabled(false);
         bulletAppState.setThreadingType(ThreadingType.PARALLEL);
         stateManager.attach(bulletAppState);
         bulletAppState.getPhysicsSpace().setMaxSubSteps(16);
         bulletAppState.getPhysicsSpace().setAccuracy(PHYSICS_ACCURACY);
         bulletAppState.getPhysicsSpace().addCollisionListener(this);
         
         bulletAppState.getPhysicsSpace().enableDebug(assetManager);
         
         worldController = new WorldController(assetManager, bulletAppState.getPhysicsSpace());
         
         viewPort.attachScene(worldController.getRootNode());
         viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
 //        viewPort.setBackgroundColor(new ColorRGBA(0f, 0f, 0f, 1f));
         
         for(Light l : worldController.getLights()) {
         	if(l instanceof DirectionalLight) {
         		DirectionalLightShadowRenderer shadowRenderer = new DirectionalLightShadowRenderer(assetManager, 1024, 3);
         		shadowRenderer.setLight((DirectionalLight) l);
         		shadowRenderer.setEdgeFilteringMode(EdgeFilteringMode.PCF8);
         		shadowRenderer.setShadowCompareMode(CompareMode.Hardware);
         		viewPort.addProcessor(shadowRenderer);
         	}
         }
         
         FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
         
         FogFilter fog=new FogFilter();
         fog.setFogColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 1.0f));
         fog.setFogDistance(100);
         fog.setFogDensity(1.5f);
 //        fpp.addFilter(fog);
         
         SSAOFilter ssaoFilter = new SSAOFilter();
         fpp.addFilter(ssaoFilter);
         
         FXAAFilter fxaaFilter = new FXAAFilter();
         fpp.addFilter(fxaaFilter);
         
         viewPort.addProcessor(fpp);
         
         cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.5f, 1000f);
         
         eventMachine.registerListener(this);
         
         connector = new ServerConnector(this, eventMachine);
         
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
     	
         InputEvent join = new InputEvent(player.getId(), InputCommand.JOINGAME, true, true);
     	eventMachine.fireEvent(join);
         
     	InputEvent chooseteam = null;
     	if(team == 1) {
     		chooseteam = new InputEvent(player.getId(), InputCommand.CHOOSETEAM1, true, true);
     	}else if(team == 2) {
     		chooseteam = new InputEvent(player.getId(), InputCommand.CHOOSETEAM2, true, true);
     	}
     	eventMachine.fireEvent(chooseteam);
     	
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
    	menuController.actualizeScreen();
     	stateManager.attach(gameInputAppState);
     	bulletAppState.setEnabled(true);
     	startInputSender();
     }
     
     /**
      * pauses the game and opens the menu.
      */
     public void openMenu() {
     	bulletAppState.setEnabled(false);
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
 //    	stateManager.detach(gameInputAppState);
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
 				Moveable moveable = worldController.getMoveable(pickup.getMoveableid());
 				pickup(p, moveable);
 			}else if (e instanceof PlaceEvent) {
 				PlaceEvent place = (PlaceEvent) e;
 				Player p = players.get(place.getPlayerid());
 				Vector3f loc = place.getLocation();
 				place(p, loc);
 			}else if (e instanceof PlayerJoinEvent) {
 				PlayerJoinEvent join = (PlayerJoinEvent) e;
 				int playerid = join.getPlayerId();
 				if(player.getId() != playerid) {
 					joinPlayer(playerid);
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
     			Player p = players.get(input.getPlayerid());
     			handlePlayerInput(p, input.getCommand(), input.isValue());
     		}else if (e instanceof RestartRoundEvent) {
 				for (Player p : players.values()) {
 					if(p.isAlive()) {
 						killPlayer(p);
 					}
 					p.setKills(0);
 					p.setDeaths(0);
 					p.setInventory(-1);
 				}
 				removeAllBullets();
 				worldController.resetWorld();
 			}else if (e instanceof RoundEndedEvent) {
 				RoundEndedEvent roundEnded = (RoundEndedEvent) e;
 				if(roundEnded.getWinnerid() >= 0) {
 					Player p = players.get(roundEnded.getWinnerid());
 					if(p != null) {
 						p.setKills(p.getKills() + 1);
 					}
 				}
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
 	
 	public void setInitialState(PlayerInfo[] pinfos, MoveableInfo[] minfos) {
 		for (PlayerInfo info : pinfos) {
 			if(player.getId() == info.getPlayerid()) continue;
 			Player p = new Player(info.getPlayerid(), assetManager);
 			p.setName(info.getName());
 			p.setTeam(info.getTeam());
 			p.setAlive(info.isAlive());
 			p.setHealthpoints(info.getHealthpoints());
 			p.setKills(info.getKills());
 			p.setDeaths(info.getDeaths());
 			players.put(p.getId(), p);
 			if(p.isAlive()) {
 				p.getControl().setPhysicsLocation(worldController.getLevel().getSpawnPoint(p.getTeam()).getPosition());
 				worldController.attachPlayer(p);
 			}
 		}
 		for (MoveableInfo info : minfos) {
 			Moveable m = worldController.getMoveable(info.getId());
 			if(m != null) {
 				m.getControl().setPhysicsLocation(info.getLocation());
 				if(!info.isInWorld()) {
 					worldController.detachMoveable(m);
 				}
 			}
 		}
 	}
 	
 	private void handlePlayerInput(Player p, InputCommand command, boolean value) {
 		if(p == null) return;
 		switch (command) {
 		case SCOREBOARD:
 			scoreboard(value);
 			break;
 		case QUITGAME:
 			quitPlayer(p);
 			break;
 		case EXIT:
 			if(gamestate == GameState.RUNNING) {
 				openMenu();
 			}else if (gamestate == GameState.MENU) {
 				resumeGame();
 			}
 			break;
 
 		default:
 			p.handleInput(command, value);
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
 				source.setKills(source.getKills() + 1);
 			}
 		}
 		victim.setHealthpoints(hp);
 	}
 	
 	private void killPlayer(Player p) {
 		if(p == null) return;
 		worldController.detachPlayer(p);
 		p.setAlive(false);
 		p.setDeaths(p.getDeaths() + 1);
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
 	
 	private void pickup(Player p, Moveable moveable) {
 		if(moveable != null) {
 			worldController.detachMoveable(moveable);
 			if(p != null) {
 				p.setInventory((Long) moveable.getId());
 			}
 		}
 	}
 	
 	private void place(Player p, Vector3f loc) {
 		if(p == null) return;
 
 		if(p.getInventory() >= 0) {
 			Moveable m = worldController.getMoveable(p.getInventory());
 			m.getControl().setPhysicsLocation(loc);
 			worldController.attachMoveable(m);
 			p.setInventory(-1);
 		}
 	}
 	
 	private void jump(Player p) {
 		if(p == null) return;
 		p.getControl().jump();
 	}
 	
 	private void joinPlayer(int playerid) {
 		Player p = new Player(playerid, assetManager);
 		players.put(playerid, p);
 	}
 	
 	private void quitPlayer(Player p) {
 		if(p == null) return;
 		worldController.detachPlayer(p);
 		players.remove(p.getId());
 	}
 	
 	private void respawn(Player p) {
 		if(p == null) return;
 		p.setHealthpoints(100);
 		p.setAlive(true);
 		p.getControl().setPhysicsLocation(worldController.getLevel().getSpawnPoint(p.getTeam()).getPosition());
 		worldController.attachPlayer(p);
 		if(p.getId() == player.getId()) {
 			resumeGame();
 		}
 	}
 	
 	private void chooseTeam(Player p, int team) {
 		if(p == null) return;
 		p.setTeam(team);
 	}
 	
 	public String getScores() {
 		StringBuilder sb_team1 = new StringBuilder();
 		StringBuilder sb_team2 = new StringBuilder();
 		for (Player p : players.values()) {
 			if(p.getTeam() == 1) {
 				sb_team1.append("\n" + p.getName() + "\t\t" + p.getKills() + "\t\t" + p.getDeaths());
 			}else if(p.getTeam() == 2) {
 				sb_team2.append("\n" + p.getName() + "\t\t" + p.getKills() + "\t\t" + p.getDeaths());
 			}
 		}
 		StringBuilder sb = new StringBuilder();
 		sb.append("Team 1 \t\t Kills \t\t Deaths");
 		sb.append(sb_team1);
 		sb.append("\n\n\n\n");
 		sb.append("Team 2 \t\t Kills \t\t Deaths");
 		sb.append(sb_team2);
 		return sb.toString();
 	}
 	
 	public void scoreboard(boolean show) {
 		if(gamestate == GameState.RUNNING || gamestate == GameState.SPECTATE) {
 			if(show) {
 				menuController.showScorebord();
 			}else {
 				menuController.actualizeScreen();
 			}
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
 		inputSender.interrupt();
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
