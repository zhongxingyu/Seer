 package de.findus.cydonia.main;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.concurrent.Callable;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.swing.SwingUtilities;
 
 import org.jdom2.JDOMException;
 import org.xml.sax.InputSource;
 
 import com.jme3.app.StatsView;
 import com.jme3.asset.AssetNotFoundException;
 import com.jme3.audio.AudioNode;
 import com.jme3.bullet.collision.PhysicsCollisionEvent;
 import com.jme3.collision.CollisionResult;
 import com.jme3.font.BitmapFont;
 import com.jme3.font.BitmapText;
 import com.jme3.light.DirectionalLight;
 import com.jme3.light.Light;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector3f;
 import com.jme3.niftygui.NiftyJmeDisplay;
 import com.jme3.post.FilterPostProcessor;
 import com.jme3.post.filters.BloomFilter;
 import com.jme3.post.filters.FogFilter;
 import com.jme3.renderer.queue.RenderQueue.Bucket;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial.CullHint;
 import com.jme3.shadow.CompareMode;
 import com.jme3.shadow.DirectionalLightShadowRenderer;
 import com.jme3.shadow.EdgeFilteringMode;
 import com.jme3.system.AppSettings;
 import com.jme3.system.JmeSystem;
 
 import de.findus.cydonia.appstates.GameInputAppState;
 import de.findus.cydonia.appstates.GeneralInputAppState;
 import de.findus.cydonia.appstates.MenuController;
 import de.findus.cydonia.events.ChooseTeamEvent;
 import de.findus.cydonia.events.ConnectionDeniedEvent;
 import de.findus.cydonia.events.ConnectionInitEvent;
 import de.findus.cydonia.events.Event;
 import de.findus.cydonia.events.FlagEvent;
 import de.findus.cydonia.events.InputEvent;
 import de.findus.cydonia.events.KillEvent;
 import de.findus.cydonia.events.PickupEvent;
 import de.findus.cydonia.events.PlaceEvent;
 import de.findus.cydonia.events.PlayerJoinEvent;
 import de.findus.cydonia.events.PlayerQuitEvent;
 import de.findus.cydonia.events.RespawnEvent;
 import de.findus.cydonia.events.RestartRoundEvent;
 import de.findus.cydonia.events.RoundEndedEvent;
 import de.findus.cydonia.level.Flag;
 import de.findus.cydonia.level.Flube;
 import de.findus.cydonia.level.Map;
 import de.findus.cydonia.level.MapXMLParser;
 import de.findus.cydonia.main.ExtendedSettingsDialog.SelectionListener;
 import de.findus.cydonia.messages.EquipmentInfo;
 import de.findus.cydonia.messages.InputMessage;
 import de.findus.cydonia.messages.JoinMessage;
 import de.findus.cydonia.messages.MoveableInfo;
 import de.findus.cydonia.messages.PlayerInfo;
 import de.findus.cydonia.messages.PlayerPhysic;
 import de.findus.cydonia.messages.ViewDirMessage;
 import de.findus.cydonia.messages.WorldStateUpdatedMessage;
 import de.findus.cydonia.player.Beamer;
 import de.findus.cydonia.player.Equipment;
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
 public class GameController extends MainController implements ScreenController{
 	
 	public static final String TEXTURES_PATH = "de/findus/cydonia/textures/";
 	public static final String APPTITLE = "Cydonia Client";
 	
 	/**
 	 * The time in seconds it should take to compensate a deviation from the accurate (=server defined) physical location of an object. 
 	 */
 	private static final float SMOOTHING = 0.2f;
 	
     public static void main(String[] args) {
     	String ip = "";
     	if(args.length > 0) {
     		ip = args[0];
     	}
     	System.out.println("ip: " + ip);
 		
 		GameController controller = new GameController();
 		controller.start(ip);
     }
     
     protected boolean showSettings = true;
 
 	protected Node guiNode = new Node("Gui Node");
     
     private  boolean showFps = true;
     protected float secondCounter = 0.0f;
     protected int frameCounter = 0;
     protected BitmapText fpsText;
     protected BitmapFont guiFont;
     protected StatsView statsView;
 
     private Node beamNode;
     
     private GameInputAppState gameInputAppState;
     
     private MenuController menuController;
     
     private Vector3f walkDirection = new Vector3f();
     private boolean left=false, right=false, up=false, down=false;
     
     private String serverAddress = "";
     
     private Nifty nifty;
     private TextField playerNameInput;
     private DropDown<String> teamInput;
     
     private Player player;
     private AudioNode throwSound;
 
     private ServerConnector connector;
     
     private Thread inputSender;
     
     private WorldStateUpdatedMessage latestWorldState;
     
     private long roundStartTime;
     
     private int lastScorerId;
 	public void start(String server) {
     	this.serverAddress = server;
     	this.start();
     }
     
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
     	stopInputSender();
     	connector.disconnectFromServer();
     	getEventMachine().stop();
     	
     	super.stop(waitfor);
 //    	System.exit(0);
     }
 
     @Override
     public void initialize() {
         super.initialize();
         
         setPauseOnLostFocus(false);
         
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
     	
     	connector = new ServerConnector(this, getEventMachine());
     	
     	gameInputAppState = new GameInputAppState(this);
     	
     	GeneralInputAppState generalInputAppState = new GeneralInputAppState(this);
     	stateManager.attach(generalInputAppState);
         
 //    	getBulletAppState().setDebugEnabled(true);
         
         viewPort.attachScene(getWorldController().getRootNode());
 //        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
         viewPort.setBackgroundColor(new ColorRGBA(0f, 0f, 0f, 1f));
 
         if(settings.getInteger("shadowLevel") > 0) {
         	for(Light l : getWorldController().getLights()) {
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
         
         BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Objects);
         fpp.addFilter(bloom);
         
         beamNode = new Node("Beams");
         getWorldController().attachObject(beamNode);
 //        refractionProcessor = new SimpleRefractionProcessor(assetManager);
 //        refractionProcessor.setRefractionScene(getWorldController().getRootNode());
 //        refractionProcessor.setDebug(true);
 //        refractionProcessor.setRenderSize(256, 256);
 //        refractionProcessor.getMaterial().getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
 //        viewPort.addProcessor(refractionProcessor);
         
         viewPort.addProcessor(fpp);
         
         cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.3f, 300f);
         
         throwSound = new AudioNode(assetManager, "de/findus/cydonia/sounds/throw_001.wav", false);
         throwSound.setLooping(false);
 		throwSound.setPositional(true);
 		throwSound.setLocalTranslation(Vector3f.ZERO);
 		throwSound.setVolume(1);
 		getWorldController().attachObject(throwSound);
 		
     	menuController.actualizeScreen();
     	connector.connectToServer(serverAddress, 6173);
     }
     
 
 	/**
      * Starts the actual game eg. the game loop.
      */
     public void startGame(String level) {
 //    	InputSource is = new InputSource(new StringReader(level));
     	InputSource is = new InputSource(ClassLoader.class.getResourceAsStream(level));
         MapXMLParser mapXMLParser = new MapXMLParser(assetManager);
         try {
 			Map map = mapXMLParser.loadMap(is);
 			getWorldController().loadWorld(map);
 		} catch ( IOException e) {
 			e.printStackTrace();
 			stopGame();
 		} catch (JDOMException e) {
 			e.printStackTrace();
 			stopGame();
 		}
     	
     	getBulletAppState().setEnabled(true);
     	setGamestate(GameState.LOBBY);
     	menuController.actualizeScreen();
     }
     
     public void joinGame() {
     	String playername = this.playerNameInput.getRealText();
     	int team = this.teamInput.getSelectedIndex() + 1;
     	player = getPlayerController().createNew(connector.getConnectionId());
     	player.setName(playername);
     	getPlayerController().setTeam(player, team);
     	
     	setGamestate(GameState.SPECTATE);
     	menuController.actualizeScreen();
     	
         JoinMessage join = new JoinMessage(player.getId(), player.getName());
     	connector.sendMessage(join);
         
     	InputMessage chooseteam = null;
     	if(team == 1) {
     		chooseteam = new InputMessage(player.getId(), InputCommand.CHOOSETEAM1, true);
     	}else if(team == 2) {
     		chooseteam = new InputMessage(player.getId(), InputCommand.CHOOSETEAM2, true);
     	}
     	connector.sendMessage(chooseteam);
     }
     
     /**
      * Resumes the game after pausing.
      */
     public void resumeGame() {
     	setGamestate(GameState.RUNNING);
     	stateManager.attach(gameInputAppState);
     	startInputSender();
     	menuController.actualizeScreen();
     }
     
     /**
      * pauses the game and opens the menu.
      */
     public void openMenu() {
     	stateManager.detach(gameInputAppState);
     	stopInputSender();
     	setGamestate(GameState.MENU);
     	menuController.actualizeScreen();
     }
     
     public void stopGame() {
     	stop();
     }
     
     public void gameOver() {
     	stopInputSender();
     	setGamestate(GameState.SPECTATE);
     	menuController.actualizeScreen();
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
         useLatestWorldstate();
         computeBeams(tpf);
         movePlayers(tpf);
         menuController.updateHUD();
         
         // update world and gui
         getWorldController().updateLogicalState(tpf);
         guiNode.updateLogicalState(tpf);
         getWorldController().updateGeometricState();
         guiNode.updateGeometricState();
 
         // render states
         stateManager.render(renderManager);
         renderManager.render(tpf, context.isRenderable());
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
 				Player p = getPlayerController().getPlayer(physic.getId());
 				if(p != null) {
 					p.setExactLoc(physic.getTranslation());
 					p.setViewDir(physic.getOrientation());
 				}
 			}
 		}
 	}
 
 	protected void handleEvent(Event e) {
 		if (e instanceof ConnectionDeniedEvent) {
 			System.out.println("Server denied connection! Reason: '" + ((ConnectionDeniedEvent) e).getReason() + "'");
 			setGamestate(GameState.LOBBY);
 			menuController.actualizeScreen();
 			clean();
 			connector.disconnectFromServer();
 		}else if (e instanceof ConnectionInitEvent) {
 			startGame(((ConnectionInitEvent) e).getLevel());
 		}else if (e instanceof KillEvent) {
 			KillEvent kill = (KillEvent) e;
 			Player p = getPlayerController().getPlayer(kill.getPlayerid());
 			killPlayer(p);
 		}else if (e instanceof PickupEvent) {
 			PickupEvent pickup = (PickupEvent) e;
 			Player p = getPlayerController().getPlayer(pickup.getPlayerid());
 			Flube flube = getWorldController().getFlube(pickup.getMoveableid());
 			pickup(p, flube);
 		}else if (e instanceof PlaceEvent) {
 			PlaceEvent place = (PlaceEvent) e;
 			Player p = getPlayerController().getPlayer(place.getPlayerid());
 			Vector3f loc = place.getLocation();
 			long moveableId = place.getMoveableid();
 			place(p, loc, moveableId);
 		}else if (e instanceof PlayerJoinEvent) {
 			PlayerJoinEvent join = (PlayerJoinEvent) e;
 			int playerid = join.getPlayerId();
 			if(player != null && player.getId() != playerid) {
 				joinPlayer(playerid, join.getPlayername());
 			}
 		}else if (e instanceof ChooseTeamEvent) {
 			ChooseTeamEvent choose = (ChooseTeamEvent) e;
 			Player p = getPlayerController().getPlayer(choose.getPlayerId());
 			chooseTeam(p, choose.getTeam());
 		}else if (e instanceof RespawnEvent) {
 			RespawnEvent respawn = (RespawnEvent) e;
 			Player p = getPlayerController().getPlayer(respawn.getPlayerid());
 			respawn(p);
 		}else if (e instanceof PlayerQuitEvent) {
 			PlayerQuitEvent quit = (PlayerQuitEvent) e;
 			Player p = getPlayerController().getPlayer(quit.getPlayerId());
 			quitPlayer(p);
 		}else if (e instanceof InputEvent) {
 			InputEvent input = (InputEvent) e;
 			// only use inputs from other players, not our own inputs, that are sent back to us from the server
 			if(player.getId() != input.getPlayerid()) {
 				Player p = getPlayerController().getPlayer(input.getPlayerid());
 				p.handleInput(input.getCommand(), input.isValue());
 			}
 		}else if (e instanceof RestartRoundEvent) {
 			for (Player p : getPlayerController().getAllPlayers()) {
 				if(p.isAlive()) {
 					killPlayer(p);
 				}
 				getPlayerController().reset(p);
 			}
 			getWorldController().resetWorld();
 			this.roundStartTime = System.currentTimeMillis();
 			menuController.updateScoreboard();
 		}else if (e instanceof RoundEndedEvent) {
 			RoundEndedEvent roundEnded = (RoundEndedEvent) e;
 			for (Player p : getPlayerController().getAllPlayers()) {
 				p.setInputState(new PlayerInputState());
 				if(p.getId() == roundEnded.getWinnerid()) {
 					p.setScores(p.getScores() + 1);
 				}
 			}
 			lastScorerId = roundEnded.getWinnerid();
 			setGamestate(GameState.ROUNDOVER);
 			menuController.actualizeScreen();
 		}else if(e instanceof FlagEvent) {
 			FlagEvent flagev = (FlagEvent) e;
 			if(flagev.getType() == FlagEvent.TAKE) {
 				Flag f = getWorldController().getFlag(flagev.getFlagid());
 				Player p = getPlayerController().getPlayer(flagev.getPlayerid());
 				takeFlag(p, f);
 			}else if(flagev.getType() == FlagEvent.SCORE) {
 				Flag f = getWorldController().getFlag(flagev.getFlagid());
 				Player p = getPlayerController().getPlayer(flagev.getPlayerid());
 				scoreFlag(p, f);
 				menuController.updateScoreboard();
 			}else if(flagev.getType() == FlagEvent.RETURN) {
 				Flag f = getWorldController().getFlag(flagev.getFlagid());
 				returnFlag(f);
 			}
 		}
 	}
 
 	private void clean() {
 		getPlayerController().removeAllPlayers();
 	}
 	
 	public void setlatestWorldstate(WorldStateUpdatedMessage update) {
 		latestWorldState = update;
 	}
 	
 	public void setInitialState(GameConfig config, PlayerInfo[] pinfos, MoveableInfo[] minfos) {
 		getGameConfig().copyFrom(config);
 		
 		for (PlayerInfo info : pinfos) {
 			final int playerid = info.getPlayerid();
 			Player p = getPlayerController().getPlayer(playerid);
 			if(p == null) {
 				p = getPlayerController().createNew(info.getPlayerid());
 			}
 			p.setName(info.getName());
 			getPlayerController().setTeam(p, info.getTeam());
 			p.setAlive(info.isAlive());
 			p.setHealthpoints(info.getHealthpoints());
 			p.setScores(info.getScores());
 			
 			p.getEquips().clear();
 			for(EquipmentInfo ei : info.getEquipInfos()) {
 				try {
 					Equipment equip = (Equipment) Class.forName(ei.getClassName()).newInstance();
 					equip.setMainController(this);
 					equip.setPlayer(p);
 					equip.loadInfo(ei);
 					p.getEquips().add(equip);
 					if(equip instanceof Beamer) {
 						((Beamer) equip).initGeometry();
 					}
 				} catch (InstantiationException e) {
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				} catch (ClassNotFoundException e) {
 					e.printStackTrace();
 				}
 			}
 			p.setCurrEquip(info.getCurrEquip());
 			
 			if(playerid == this.player.getId()) continue;
 			
 			if(p.isAlive()) {
 				p.getControl().setPhysicsLocation(getWorldController().getSpawnPoint(p.getTeam()).getPosition());
 				enqueue(new Callable<String>() {
 					public String call() {
 						getWorldController().attachPlayer(getPlayerController().getPlayer(playerid));
 						return null;
 					}
 				});
 				
 			}
 		}
 		for (MoveableInfo info : minfos) {
 			Flube m = getWorldController().getFlube(info.getId());
 			if(m != null) {
 				m.getControl().setPhysicsLocation(info.getLocation());
 				if(!info.isInWorld()) {
 					getWorldController().detachFlube(m);
 				}
 			}
 		}
     	
 //    	stateManager.attach(gameInputAppState);
     	startInputSender();
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
 				if(getGamestate() == GameState.RUNNING || getGamestate() == GameState.SPECTATE || getGamestate() == GameState.ROUNDOVER) {
 					openMenu();
 				}else if (getGamestate() == GameState.MENU) {
 					resumeGame();
 				}
 			}
 			break;
 
 		default:
 			if(getGamestate() == GameState.RUNNING) {
 				player.handleInput(command, value);
 			}
 			break;
 		}
 	}
 
     /**
      * Moves the player according to user input state.
      * @param tpf time per frame
      */
 	private void movePlayers(float tpf) {
         
 		for (Player p : getPlayerController().getAllPlayers()) {
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
 			
			Vector3f correction = p.getExactLoc().subtract(p.getControl().getPhysicsLocation()).mult(SMOOTHING);
 			walkDirection.addLocal(correction);
 
 			walkDirection.multLocal(PHYSICS_ACCURACY);
 			p.getControl().setWalkDirection(walkDirection);
 			
 			if(p.getId() == connector.getConnectionId()) {
 				cam.setLocation(p.getEyePosition());
 			}
 		}
 		
     }
 	
 	private void computeBeams(float tpf) {
 		for(Player p : getPlayerController().getAllPlayers()) {
 			if(p.getCurrentEquipment() instanceof Beamer) {
 				Beamer beamer = (Beamer) p.getCurrentEquipment();
 				if(beamer.isBeaming()) {
 					CollisionResult result = getWorldController().pickRoot(beamer.getPlayer().getEyePosition().add(beamer.getPlayer().getViewDir().normalize().mult(0.3f)), beamer.getPlayer().getViewDir());
 					if(result != null && result.getGeometry().getParent() != null && result.getGeometry().getParent().getName() != null && result.getGeometry().getParent().getName().startsWith("player")) {
 						Player victim = getPlayerController().getPlayer(Integer.valueOf(result.getGeometry().getParent().getName().substring(6)));
 						if(victim != null && victim.getTeam() != beamer.getPlayer().getTeam()) {
 							getPlayerController().setHealthpoints(victim, Math.max(0, victim.getHealthpoints() - 20*tpf));
 						}
 					}
 					beamer.update();
 				}
 			}
 		}
 	}
 
     @Override
 	public void collision(PhysicsCollisionEvent e) {
     	
     }
 	
 	public void killPlayer(Player p) {
 		super.killPlayer(p);
 		
 		if(p == null) return;
 		if(p.getId() == player.getId()) {
 			gameOver();
 		}
 	}
 
 	protected void respawn(final Player p) {
 		super.respawn(p);
 		
 		if(p == null) return;
 		if(p.getId() == player.getId()) {
 			p.getModel().setCullHint(CullHint.Always);
 			resumeGame();
 		}
 	}
 	
 	protected void joinPlayer(int playerid, String playername) {
 		super.joinPlayer(playerid, playername);
 		menuController.updateScoreboard();
 	}
 	
 	public long getRemainingTime() {
 		long passedTime = System.currentTimeMillis() - roundStartTime;
 		return getGameConfig().getLong("mp_roundtime") * 1000 - passedTime;
 	}
 	
 	public Player getLastScorer() {
 		return getPlayerController().getPlayer(lastScorerId);
 	}
 
 	public String getScores() {
 		StringBuilder sb_team1 = new StringBuilder();
 		StringBuilder sb_team2 = new StringBuilder();
 		for (Player p : getPlayerController().getAllPlayers()) {
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
 		// move it up so it appears above fps text
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
 		this.playerNameInput = screen.findNiftyControl("playername", TextField.class);
 		this.teamInput = screen.findNiftyControl("team", DropDown.class);
 		
 		this.teamInput.addItem("Team 1");
 		this.teamInput.addItem("Team 2");
 	}
 
 	@Override
 	public void onEndScreen() {
 	}
 
 	@Override
 	public void onStartScreen() {
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
 		if(inputSender == null || !inputSender.isAlive()) {
 			inputSender = new Thread(new InputSenderLoop());
 			inputSender.start();
 		}
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
