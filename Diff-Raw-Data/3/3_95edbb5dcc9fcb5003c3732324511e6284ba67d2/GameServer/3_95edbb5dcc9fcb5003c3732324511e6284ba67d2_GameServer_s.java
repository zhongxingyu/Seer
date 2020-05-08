 /**
  * 
  */
 package de.findus.cydonia.server;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.LinkedList;
 
 import org.jdom2.JDOMException;
 import org.xml.sax.InputSource;
 
 import com.jme3.bullet.collision.PhysicsCollisionEvent;
 import com.jme3.collision.CollisionResult;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Spatial;
 import com.jme3.system.AppSettings;
 import com.jme3.system.JmeContext;
 
 import de.findus.cydonia.events.AddEvent;
 import de.findus.cydonia.events.BeamEvent;
 import de.findus.cydonia.events.ChooseTeamEvent;
 import de.findus.cydonia.events.ConnectionAddedEvent;
 import de.findus.cydonia.events.ConnectionRemovedEvent;
 import de.findus.cydonia.events.Event;
 import de.findus.cydonia.events.FlagEvent;
 import de.findus.cydonia.events.ConfigEvent;
 import de.findus.cydonia.events.InputEvent;
 import de.findus.cydonia.events.KillEvent;
 import de.findus.cydonia.events.PickupEvent;
 import de.findus.cydonia.events.PlaceEvent;
 import de.findus.cydonia.events.PlayerJoinEvent;
 import de.findus.cydonia.events.PlayerQuitEvent;
 import de.findus.cydonia.events.RemoveEvent;
 import de.findus.cydonia.events.RespawnEvent;
 import de.findus.cydonia.events.RestartRoundEvent;
 import de.findus.cydonia.events.RoundEndedEvent;
 import de.findus.cydonia.level.Flag;
 import de.findus.cydonia.level.Flube;
 import de.findus.cydonia.level.Map;
 import de.findus.cydonia.level.MapXMLParser;
 import de.findus.cydonia.level.SpawnPoint;
 import de.findus.cydonia.main.GameState;
 import de.findus.cydonia.main.MainController;
 import de.findus.cydonia.messages.ConnectionInitMessage;
 import de.findus.cydonia.messages.FlagInfo;
 import de.findus.cydonia.messages.InitialStateMessage;
 import de.findus.cydonia.messages.LocationUpdatedMessage;
 import de.findus.cydonia.messages.MoveableInfo;
 import de.findus.cydonia.messages.PlayerInfo;
 import de.findus.cydonia.messages.SpawnPointInfo;
 import de.findus.cydonia.player.Beamer;
 import de.findus.cydonia.player.EquipmentFactory;
 import de.findus.cydonia.player.EquipmentFactory.ServiceType;
 import de.findus.cydonia.player.InputCommand;
 import de.findus.cydonia.player.Player;
 import de.findus.cydonia.player.PlayerInputState;
 
 /**
  * @author Findus
  *
  */
 public class GameServer extends MainController{
 	
 	public static final String APPTITLE = "Cydonia Server";
 	
 	public static final int RELOAD_TIME = 500;
 
 	public static final float MAX_PICK_RANGE = 20;
 
 	public static final float MAX_PLACE_RANGE = 20;
 
 	public static final boolean FREE_PLACING = false;
 	
 	private static final String DEFAULTMAP = "level3";
 	
 	private static ConsoleWriter CWRITER = ConsoleWriter.getWriter();
 
 	public static void main(String[] args) {
 		MainController gameServer = new GameServer();
 		gameServer.start();
 	}
 	
 	private boolean running;
 	
 	private ServerConfigFrame configFrame;
 	
 	private Thread locationSenderLoop;
 	
     private GameplayController gameplayController;
     
     /**
      * Used for moving players.
      * Allocated only once and reused for performance reasons.
      */
     private Vector3f walkDirection = new Vector3f();
     
 	private NetworkController networkController;
 
 	private EquipmentFactory equipmentFactory;
 	
 	private Collection<ServerStateListener> stateListeners;
 	
 	public GameServer() {
 		super();
 		
 		this.stateListeners = new LinkedList<ServerStateListener>();
 		
 		configFrame = new ServerConfigFrame(this);
         configFrame.pack();
         
         CWRITER.addConsole(configFrame);
 	}
 	
 	public void setConfigFrameVisible(boolean visible) {
 		configFrame.setVisible(visible);
 	}
 	
 	@Override
 	public void start() {
 		start(DEFAULTMAP);
 	}
 	
 	public void start(String mapFileName) {
 		this.setRunning(true);
 		if (settings == null){
             settings = new AppSettings(true);
             settings.setTitle(APPTITLE);
         }
 		getGameConfig().putString("mapFileName", mapFileName);
 		super.start(JmeContext.Type.Headless);
 	}
 	
 	@Override
 	public void stop(boolean waitfor) {
 		CWRITER.writeLine("shutting down ...");
 		cleanup();
 		super.stop(waitfor);
 		this.setRunning(false);
 //		System.exit(0);
 	}
 	
 	protected void cleanup() {
 		super.cleanup();
 		networkController.stop();
 		locationSenderLoop.interrupt();
 		gameplayController.dispose();
 		configFrame.setVisible(true);
 //		configFrame.dispose();
 	}
 	
     @Override
     public void initialize() {
         super.initialize();
 
         this.equipmentFactory = new EquipmentFactory(ServiceType.SERVER, this);
         
         String mapfile = MAPFOLDER + getGameConfig().getString("mapFileName") + MAPEXTENSION;
         InputSource is = new InputSource(this.getClass().getResourceAsStream(mapfile));
         MapXMLParser mapXMLParser = new MapXMLParser(assetManager);
         try {
 			Map map = mapXMLParser.loadMap(is);
 			getWorldController().loadWorld(map);
 			informStateListeners();
 			CWRITER.writeLine("loaded map: " + getGameConfig().getString("mapFileName"));
 		} catch (IOException e) {
 			e.printStackTrace();
 			stop();
 		} catch (JDOMException e) {
 			e.printStackTrace();
 			stop();
 		}
         
         networkController = new NetworkController(this, getEventMachine());
 		
         getBulletAppState().setEnabled(true);
 		locationSenderLoop = new Thread(new LocationSenderLoop());
 		locationSenderLoop.start();
 		
 		gameplayController = new GameplayController(getEventMachine(), getGameConfig());
 		gameplayController.restartRound();
     }
     
     @Override
     public void update() {
         super.update(); // makes sure to execute AppTasks
         if (speed == 0 || paused) {
             return;
         }
 
         float tpf = timer.getTimePerFrame() * speed;
 
         // update states
         stateManager.update(tpf);
 
         computeBeams(tpf);
         
         // update game specific things
         movePlayers(tpf);
         
         // update world and gui
         getWorldController().updateLogicalState(tpf);
         getWorldController().updateGeometricState();
 
         stateManager.render(renderManager);
         renderManager.render(tpf, context.isRenderable());
         stateManager.postRender();
     }
 
 	@Override
 	protected void handleEvent(Event e) {
 		if(e instanceof ConnectionAddedEvent) {
 			connectionAdded(((ConnectionAddedEvent) e).getClientid());
 		}else if(e instanceof ConnectionRemovedEvent) {
 			connectionRemoved(((ConnectionRemovedEvent) e).getClientid());
 		}else if (e instanceof RestartRoundEvent) {
 			for (Player p : getPlayerController().getAllPlayers()) {
 				if(p.isAlive()) {
 					killPlayer(p);
 				}
 				getPlayerController().reset(p);
 			}
 			getWorldController().resetWorld();
 //			for (Player p : getPlayerController().getAllPlayers()) {
 //				respawn(p);
 //			}
 			getBulletAppState().setEnabled(true);
 		}else if (e instanceof RoundEndedEvent) {
 			getBulletAppState().setEnabled(false);
 			RoundEndedEvent roundEnded = (RoundEndedEvent) e;
 			for (Player p : getPlayerController().getAllPlayers()) {
 				p.setInputState(new PlayerInputState());
 				if(p.getId() == roundEnded.getWinteam()) {
 					p.setScores(p.getScores() + 1);
 				}
 			}
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
 			}else if(flagev.getType() == FlagEvent.RETURN) {
 				Flag f = getWorldController().getFlag(flagev.getFlagid());
 				returnFlag(f);
 			}
 		}else if(e instanceof KillEvent) {
 			KillEvent kill = (KillEvent) e;
 			Player p = getPlayerController().getPlayer(kill.getPlayerid());
 			killPlayer(p);
 		}else if(e instanceof PickupEvent) {
 			PickupEvent pickup = (PickupEvent) e;
 			Player p = getPlayerController().getPlayer(pickup.getPlayerid());
 			Flube f = getWorldController().getFlube(pickup.getMoveableid());
 			pickup(p, f);
 		}else if(e instanceof PlaceEvent) {
 			PlaceEvent place = (PlaceEvent) e;
 			Player p = getPlayerController().getPlayer(place.getPlayerid());
 			Flube f = getWorldController().getFlube(place.getMoveableid());
 			Vector3f loc = place.getLocation();
 			place(p, f, loc);
 		}else if(e instanceof RemoveEvent) {
 			RemoveEvent remove = (RemoveEvent) e;
 			if("flube".equalsIgnoreCase(remove.getObjectType())) {
 				Flube f = getWorldController().getFlube(remove.getObjectid());
 				getWorldController().removeFlube(f);
 			}else if("flag".equalsIgnoreCase(remove.getObjectType())) {
 				Flag f = getWorldController().getFlag((int)remove.getObjectid());
 				getWorldController().removeFlag(f);
 			}else if("spawnpoint".equalsIgnoreCase(remove.getObjectType())) {
 				SpawnPoint sp = getWorldController().getSpawnPoint((int)remove.getObjectid());
 				getWorldController().removeSpawnPoint(sp);
 			}
 		}else if(e instanceof AddEvent) {
 			AddEvent add = (AddEvent) e;
 			if("flube".equalsIgnoreCase(add.getObjectType())) {
 				Flube f = getWorldController().addNewFlube(add.getObjectid(), add.getLocation(), add.getObjectSpec());
 				getWorldController().attachFlube(f);
 			}else if("flag".equalsIgnoreCase(add.getObjectType())) {
 				Flag f = getWorldController().addNewFlag((int)add.getObjectid(), add.getLocation(), add.getObjectSpec());
 			}else if("spawnpoint".equalsIgnoreCase(add.getObjectType())) {
 				SpawnPoint sp = getWorldController().addNewSpawnPoint((int)add.getObjectid(), add.getLocation(), add.getObjectSpec());
 			}
 		}
 	}
 
 	private void movePlayers(float tpf) {
 		if(gameplayController.getGameState() != GameState.RUNNING) {
 			return;
 		}
 		for (Player p : this.getPlayerController().getAllPlayers()) {
 			if(p.isAlive()) {
 				Vector3f viewDir = p.getViewDir().clone();
 				if("ctf".equalsIgnoreCase(getGameConfig().getString("mp_gamemode"))) {
 					viewDir.setY(0).normalizeLocal();
 				}
 				Vector3f viewLeft = new Vector3f();
 				ROTATE90LEFT.transformVector(viewDir.clone().setY(0).normalizeLocal(), viewLeft);
 
 				walkDirection.set(0, 0, 0);
 				if(p.getInputState().isLeft()) walkDirection.addLocal(viewLeft);
 				if(p.getInputState().isRight()) walkDirection.addLocal(viewLeft.negate());
 				if(p.getInputState().isForward()) walkDirection.addLocal(viewDir);
 				if(p.getInputState().isBack()) walkDirection.addLocal(viewDir.negate());
 
 				walkDirection.normalizeLocal().multLocal(PHYSICS_ACCURACY * PLAYER_SPEED);
 				if("editor".equalsIgnoreCase(getGameConfig().getString("mp_gamemode"))) {
 					walkDirection.multLocal(1.5f);
 				}
 				
 				p.getControl().setWalkDirection(walkDirection);
 
 				if(getWorldController().isBelowBottomOfPlayground(p) && "ctf".equalsIgnoreCase(getGameConfig().getString("mp_gamemode"))) {
 					KillEvent ev = new KillEvent(p.getId(), true);
 					getEventMachine().fireEvent(ev);
 				}
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
 							getPlayerController().setHealthpoints(victim, victim.getHealthpoints() - 20*tpf);
 							if(victim.getHealthpoints() <= 0) {
 								beam(p, victim);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void collision(PhysicsCollisionEvent e) {
 		// collisionen müssen nur im spielmodus "ctf" berechnet werden
 		if(!"ctf".equalsIgnoreCase(getGameConfig().getString("mp_gamemode"))) return;
 		
 		Spatial other = null;
 		Spatial target = null;
 		
 		if(e.getNodeA() != null) {
 			
 			if(e.getNodeA().getUserData("FlagBase") != null && ((Boolean)e.getNodeA().getUserData("FlagBase")).booleanValue() == true) {
 				target = e.getNodeA();
 				other = e.getNodeB();
 			}
 		}
 		if (e.getNodeB() != null) {
 			
 			if(e.getNodeB().getUserData("FlagBase") != null && ((Boolean)e.getNodeB().getUserData("FlagBase")).booleanValue() == true) {
 				target = e.getNodeB();
 				other = e.getNodeA();
 			}
 		}
 		
 		if(target != null && other != null) {
 			if(other.getName().startsWith("player")) {
 				Player p = getPlayerController().getPlayer(Integer.parseInt(other.getName().substring(6)));
 				if(p.getTeam() == ((Integer)target.getUserData("team")).intValue()) { // own target
 					if(p.getFlag() != null) {
 						int stolenflagid = p.getFlag().getId();
 						p.setFlag(null);
 						Flag f = getWorldController().getFlag(((Integer)target.getUserData("id")).intValue());
 						if(f.isInBase()) {
 							System.out.println("Team " + p.getTeam() + " scored");
 							FlagEvent event = new FlagEvent(FlagEvent.SCORE, p.getId(), stolenflagid, true);
 							getEventMachine().fireEvent(event);
 						}
 					}
 				}else { // opponents target
 					if(p.getFlag() == null) {
 						Flag f = getWorldController().getFlag(((Integer)target.getUserData("id")).intValue());
 						if(f.isInBase()) {
 							f.setInBase(false);
 							System.out.println("Team " + p.getTeam() + " took flag");
 							FlagEvent event = new FlagEvent(FlagEvent.TAKE, p.getId(), f.getId(), true);
 							getEventMachine().fireEvent(event);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	protected void joinPlayer(int playerid, String playername) {
 		super.joinPlayer(playerid, playername);
 		
 		CWRITER.writeLine(playername + " joined");
 		
 		PlayerJoinEvent join = new PlayerJoinEvent(playerid, playername, true);
 		getEventMachine().fireEvent(join);
 	}
 	
 	protected void quitPlayer(Player p) {
 		super.quitPlayer(p);
 		if(p != null) {
 			CWRITER.writeLine(p.getName() + " quit");
 			
 			PlayerQuitEvent quit = new PlayerQuitEvent(p.getId(), true);
 			getEventMachine().fireEvent(quit);
 		}
 	}
 	
 	protected void respawn(Player p) {
 		super.respawn(p);
 		
 		if(p == null) return;
 		
 		CWRITER.writeLine(p.getName() + " respawned");
 		
 		RespawnEvent respawn = new RespawnEvent(p.getId(), true);
 		getEventMachine().fireEvent(respawn);
 	}
 	
 	
 	protected void beam(Player p, Player victim) {
 		super.beam(p, victim);
 		CWRITER.writeLine(p.getName() + " beamed " + victim.getName());
 		
 		BeamEvent ev = new BeamEvent(p.getId(), victim.getId(), true);
 		getEventMachine().fireEvent(ev);
 	}
 	
 	@Override
 	protected void scoreFlag(Player p, Flag flag) {
 		super.scoreFlag(p, flag);
 		if(p != null) {
 			gameplayController.playerScored(p);
 			CWRITER.writeLine(p.getName() + " scored the flag");
 		}
 	}
 	
 	@Override
 	protected void returnFlag(Flag flag) {
 		super.returnFlag(flag);
 		
 		String teamname = "";
 		if(flag.getTeam() == 1) {
 			teamname = "Blue";
 		}else if(flag.getTeam() == 2) {
 			teamname = "Red";
 		}
 		CWRITER.writeLine(teamname + " flag returned");
 	}
 
 	public void handlePlayerInput(int playerid, InputCommand command, boolean value) {
 		Player p = getPlayerController().getPlayer(playerid);
 		switch (command) {
 		case USEPRIMARY:
 			if(gameplayController.getGameState() == GameState.RUNNING) {
 				if(p.isAlive()) {
 					p.handleInput(command, value);
 					
 					InputEvent event = new InputEvent(p.getId(), command, value, true);
 					getEventMachine().fireEvent(event);
 				}else {
 					if(value && p.getTeam() > 0) {
 						respawn(p);
 					}
 				}
 			}
 			break;
 		case USESECONDARY:
 			if(gameplayController.getGameState() == GameState.RUNNING) {
 				if(p.isAlive()) {
 					p.handleInput(command, value);
 					
 					InputEvent event = new InputEvent(p.getId(), command, value, true);
 					getEventMachine().fireEvent(event);
 				}
 			}
 			break;
 		case CHOOSETEAM1:
 			chooseTeam(p, 1);
 			break;
 		case CHOOSETEAM2:
 			chooseTeam(p, 2);
 			break;
 		case QUITGAME:
 			quitPlayer(p);
 			break;
 
 		default:
 			if(gameplayController.getGameState() == GameState.RUNNING) {
 				p.handleInput(command, value);
 				InputEvent event = new InputEvent(p.getId(), command, value, true);
 				getEventMachine().fireEvent(event);
 			}
 			break;
 		}
 	}
 
 	protected void chooseTeam(Player p, int team) {
 		super.chooseTeam(p, team);
 		
 		if(p == null) return;
 		
 		ChooseTeamEvent event = new ChooseTeamEvent(p.getId(), team, true);
 		getEventMachine().fireEvent(event);
 		
 		if(getPlayerController().getPlayerCount() == 1) {
 			gameplayController.restartRound();
 		}
 	}
 	
 	public void sendInitialState(int playerid) {
 		PlayerInfo[] playerinfos = new PlayerInfo[getPlayerController().getPlayerCount()];
 		int i=0;
 		for (Player p : getPlayerController().getAllPlayers()) {
 			playerinfos[i] = new PlayerInfo(p);
 			i++;
 		}
 		
 		Collection<Flube> flubes = getWorldController().getAllFlubes();
 		MoveableInfo[] moveableinfos = new MoveableInfo[flubes.size()];
 		int j=0;
 		for (Flube m : flubes) {
 			moveableinfos[j] = new MoveableInfo(m);
 			j++;
 		}
 		
 		Collection<Flag> flags = getWorldController().getAllFlags();
 		FlagInfo[] flaginfos = new FlagInfo[flags.size()];
 		int k=0;
 		for (Flag f : flags) {
 			flaginfos[k] = new FlagInfo(f);
 			k++;
 		}
 		
 		Collection<SpawnPoint> spawnPoints = getWorldController().getAllSpawnPoints();
 		SpawnPointInfo[] spinfos = new SpawnPointInfo[spawnPoints.size()];
 		int l=0;
 		for (SpawnPoint sp : spawnPoints) {
 			spinfos[l] = new SpawnPointInfo(sp);
 			l++;
 		}
 		
 		long passedTime = System.currentTimeMillis() - gameplayController.getRoundStartTime();
 		
 		InitialStateMessage msg = new InitialStateMessage();
 		msg.setPassedRoundTime(passedTime);
 		msg.setPlayers(playerinfos);
 		msg.setMoveables(moveableinfos);
 		msg.setFlags(flaginfos);
 		msg.setSpawnPoints(spinfos);
 		msg.setconfig(getGameConfig());
 		networkController.sendMessage(msg, playerid);
 	}
 
 	public void setViewDir(int playerid, Vector3f dir) {
 		Player p = getPlayerController().getPlayer(playerid);
 		if(p == null || dir == null)  return;
 		p.setViewDir(dir);
 	}
 	
 	public void connectionAdded(int clientid) {
 		ConnectionInitMessage init = new ConnectionInitMessage();
 		init.setConnectionAccepted(true);
 		init.setText("Welcome");
 //		try {
 //			String xml = new MapXMLParser(assetManager).writeMap(worldController.getMap());
 //			init.setLevel(xml);
 //		} catch (IOException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 		init.setLevel(getGameConfig().getString("mapFileName"));
 		networkController.sendMessage(init, clientid);
 	}
 
 	public void connectionRemoved(int clientid) {
 		Player p = getPlayerController().getPlayer(clientid);
 		quitPlayer(p);
 	}
 
 	public void handleCommand(String command) {
 		String[] com = command.split("\\s+");
 		
 		if("mp_restartround".equalsIgnoreCase(com[0])) {
 			gameplayController.endRound(-1, true);
 		}else if("mp_gamemode".equalsIgnoreCase(com[0])) {
 			if(com.length < 2) {
 				CWRITER.writeLine("mp_gamemode is " + getGameConfig().getString("mp_gamemode"));
 			}else if(!com[1].equalsIgnoreCase(getGameConfig().getString("mp_gamemode"))) {
 				switchGameMode(com[1]);
 			}
 		}else if("mp_scorelimit".equalsIgnoreCase(com[0])) {
 			if(com.length < 2) {
 				CWRITER.writeLine("mp_scorelimit is " + getGameConfig().getInteger("mp_scorelimit"));
 			}else {
 				changeConfig("mp_scorelimit", com[1]);
 			}
 		}else if("mp_timelimit".equalsIgnoreCase(com[0])) {
 			if(com.length < 2) {
 				CWRITER.writeLine("mp_timelimit is " + getGameConfig().getLong("mp_timelimit"));
 			}else {
 				changeConfig("mp_timelimit", com[1]);
 			}
 		}
 	}
 	
 	private void switchGameMode(String mode) {
 		if("editor".equalsIgnoreCase(mode)) {
 			gameplayController.endRound(-1, true);
 			for(Player p : getPlayerController().getAllPlayers()) {
 				getPlayerController().setDefaultEquipment(p);
 				p.getControl().setGravity(0);
 			}
 			CWRITER.writeLine("switched gamemode to 'editor'");
 		}else if("ctf".equalsIgnoreCase(mode)) {
 			gameplayController.endRound(-1, true);
 			for(Player p : getPlayerController().getAllPlayers()) {
 				getPlayerController().setDefaultEquipment(p);
 				p.getControl().setGravity(25);
 			}
 			CWRITER.writeLine("switched gamemode to 'ctf'");
 		}
		changeConfig("mp_gamemode", mode);
 	}
 	
 	private void changeConfig(String key, Object value) {
 		getGameConfig().putObject(key, value);
 		
 		ConfigEvent event = new ConfigEvent(key, value, true);
 		getEventMachine().fireEvent(event);
 	}
 
 	public boolean isRunning() {
 		return this.running;
 	}
 	
 	private void setRunning(boolean running) {
 		this.running = running;
 		informStateListeners();
 	}
 	
 	@Override
 	public EquipmentFactory getEquipmentFactory() {
 		return this.equipmentFactory;
 	}
 	
 	public void registerStateListener(ServerStateListener listener) {
 		if(!stateListeners.contains(listener)) {
 			stateListeners.add(listener);
 		}
 	}
 	
 	public void unregisterStateListener(ServerStateListener listener) {
 		if(stateListeners.contains(listener)) {
 			stateListeners.remove(listener);
 		}
 	}
 	
 	public void informStateListeners() {
 		for(ServerStateListener sl : stateListeners) {
 			sl.stateChanged();
 		}
 	}
 
 	/**
 	 * This class is used to send the current state of the virtual world to all clients in constant intervals.
 	 * @author Findus
 	 *
 	 */
 	private class LocationSenderLoop implements Runnable {
 		@Override
 		public void run() {
 			while(!Thread.interrupted()) {
 				LocationUpdatedMessage worldstate = LocationUpdatedMessage.getUpdate(getPlayerController().getAllPlayers());
 				networkController.broadcast(worldstate);
 				
 				try {
 					Thread.sleep(50);
 				} catch (InterruptedException e) {
 					break;
 				}
 			}
 		}
 	}
 	
 	public interface ServerStateListener {
 		
 		public void stateChanged();
 	}
 }
