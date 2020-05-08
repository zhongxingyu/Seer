 package client;
 
 import java.util.PriorityQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.jme.app.FixedLogicrateGame;
 import com.jme.input.InputHandler;
 import com.jme.input.KeyBindingManager;
 import com.jme.input.KeyInput;
 import com.jme.light.PointLight;
 import com.jme.math.Quaternion;
 import com.jme.math.Vector3f;
 import com.jme.renderer.Camera;
 import com.jme.renderer.ColorRGBA;
 import com.jme.scene.Node;
 import com.jme.scene.state.CullState;
 import com.jme.scene.state.LightState;
 import com.jme.scene.state.MaterialState;
 import com.jme.scene.state.ZBufferState;
 import com.jme.system.DisplaySystem;
 import com.jme.system.GameSettings;
 import com.jme.system.JmeException;
 import com.jme.system.PropertiesGameSettings;
 
 import common.world.Ship;
 
 public class FlyingGame extends FixedLogicrateGame implements Game {
 
 	protected final PriorityQueue<Command> commandQueue;
 	protected final GameLogic logic;
 	protected final GameClient gc;
 	protected Camera cam;
 	protected final Player self;
 	protected final Node rootnode;
 	protected final int tps = 10;
 	protected final float ticklength = 1f/tps;
 	protected long lastRender = 0;
 	private InputHandler inputHandler;
 
 	public FlyingGame(int id, String name, GameClient gc) {
 		super();
 		this.gc = gc;
 		commandQueue = new PriorityQueue<Command>(51);
 		rootnode = new Node("root");
 		self = new Player(name, id);
 		logic = new GameLogic(self);
 		setConfigShowMode(ConfigShowMode.AlwaysShow);
 	}
 
 	protected void cleanup() {
 		gc.stop();
 	}
 
 	protected void initGame() {
 		setLogicTicksPerSecond(tps);
 		
 		
 		final ZBufferState buf = display.getRenderer().createZBufferState();
 		buf.setEnabled(true);
 		buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
 		rootnode.setRenderState(buf);
 
 		final CullState cs = display.getRenderer().createCullState();
 		cs.setCullFace(CullState.Face.Back);
 		rootnode.setRenderState(cs);
 		
 		// LIGHTS
 		final LightState ltst = display.getRenderer().createLightState();
 		ltst.setEnabled(true);
 
 		final PointLight lt = new PointLight();
 		lt.setLocation(new Vector3f(4, 2, 3));
 		lt.setDiffuse(ColorRGBA.white);
 		lt.setAmbient(ColorRGBA.white);
 		lt.setEnabled(true);
 
 		ltst.attach(lt);
 
 		rootnode.setRenderState(ltst);
 		
 		addPlayer(self);
 		inputHandler = new FlyingGameInputHandler(self.getShip());
 		
 		rootnode.updateRenderState();
 		
 		lastRender = System.currentTimeMillis();
 	}
 
 	protected void initSystem() {
 		
 		int width = settings.getWidth();
 		int height = settings.getHeight();
 		int depth = settings.getDepth();
 		int freq = settings.getFrequency();
 		boolean fullscreen = settings.isFullscreen();
 		
 		cam = null;
 
 		try {
 			display = DisplaySystem.getDisplaySystem(settings.getRenderer());
 			display.setTitle("AstroPew");
 			display.createWindow(width, height, depth, freq, fullscreen);
 
 			cam = display.getRenderer().createCamera(width, height);
 		} catch (final JmeException e) {
 			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not create displaySystem", e);
 			System.exit(1);
 		}
 
 		// set the background to black
 		display.getRenderer().setBackgroundColor(ColorRGBA.black.clone());
 
 		// initialize the camera
 		cam.setFrustumPerspective(45.0f, (float) width / (float) height, 1, 5000);
 		cam.setLocation(new Vector3f(0, 2, 4));
 		cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
 
 		// Signal that we've changed our camera's location/frustum. 
 		cam.update();
 
 		display.getRenderer().setCamera(cam);
 
 		KeyBindingManager.getKeyBindingManager().set("exit", KeyInput.KEY_ESCAPE);
 	}
 
 	protected void reinit() {
 		throw new RuntimeException("I hope this method isn't called... ");
 	}
 
 	protected void render(float percentWithinTick) {
 		long old = lastRender;
 		lastRender = System.currentTimeMillis();
 		float delta = 0.001f*(lastRender - old); // s since last render
 		rootnode.updateGeometricState(delta, true);
 		display.getRenderer().clearBuffers();
 		display.getRenderer().draw(rootnode);
 		//System.out.println("render " + delta);
 	}
 
 	protected void update(float unused) {
 		Command c;
 		synchronized (this) {
 			while (!commandQueue.isEmpty()) {
 				c = commandQueue.remove();
 				c.perform(this);
 			}
 		}
 		inputHandler.update(ticklength);
 		
 		long time = System.currentTimeMillis();
 		Ship ship = self.getShip();
 		ship.getPosition().addLocal(self.getShip().getMovement().mult(ticklength));
 		ship.setLastUpdate(time);
 		
 		gc.sender.send(PacketDataFactory.createPlayerUpdate(time, ship));
 		
 		if (KeyBindingManager.getKeyBindingManager().isValidCommand("exit")) {
 			finished = true;
 		}
 		//System.out.println("update ");
 	}
 
 	protected GameSettings getNewSettings() {
 		PropertiesGameSettings gs =  new PropertiesGameSettings("properties.cfg");
 		gs.load();
 		
 		return gs;
 	}
 
 	public synchronized void addCommand(Command cmd) {
 		commandQueue.add(cmd);
 	}
 
 	public void addPlayer(int id, String name) {
 		addPlayer( new Player(name, id) );
 	}
 	
 	private void addPlayer(Player p) {
 		Ship s = createShip(p);
 		logic.addShip(s);
 	}
 
 	private Ship createShip(Player p) {
 		final Ship s = new Ship(p);
 		
 		final MaterialState ms = display.getRenderer().createMaterialState();
 		ms.setDiffuse(s.getColor());
 		ms.setEmissive(s.getColor().multLocal(0.2f));
 		ms.setAmbient(s.getColor().multLocal(0.1f));
 		s.setRenderState(ms);
 		rootnode.attachChild(s);
 		
 		return s;
 	}
 
 	public void removePlayer(int id) {
 		Ship removed = logic.removeShip(logic.getPlayer(id));
 		removed.removeFromParent();
 	}
 
 	public void updatePosition(Vector3f pos, Quaternion ort, Vector3f dir, int id, long tick) {
 		final Ship s = logic.getShip(id);
 		if (s != null) {
 			if (s.shouldUpdate(tick)) {
 				s.getPosition().set(pos);
 				s.getOrientation().set(ort);
 				s.getMovement().set(dir);
 				s.resetGeometrics();
 				s.setLastUpdate(tick);
 			}
 		}
 	}
 
 	public void startInThread() {
 		final Thread t = new Thread() {
 			public void run() {
 				FlyingGame.this.start();
 			}
 		};
 		t.start();
 	}
 	
 }
