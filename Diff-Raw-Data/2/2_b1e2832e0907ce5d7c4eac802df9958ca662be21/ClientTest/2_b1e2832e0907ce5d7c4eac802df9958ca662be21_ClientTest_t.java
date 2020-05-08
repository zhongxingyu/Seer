 package client;
 
 import gameMap.Scene;
 import graphics.Color;
 import graphics.SpriteBatch;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.Random;
 import com.esotericsoftware.kryonet.Client;
 import common.InputMessage;
 import common.NetworkSystemBuilder;
 import common.PlayerCharacteristics;
 
 import misc.SoundManager;
 
 import utils.Camera2D;
 import utils.GameTime;
 import utils.Keyboard;
 import utils.Mouse;
 import utils.Network;
 import utils.Rectangle;
 import demos.Demo;
 import demos.WorldBuilder;
 import entityFramework.*;
 
 public class ClientTest extends Demo {
 	private static final Rectangle sr = new Rectangle(0,0, 800, 600);
 	private IEntityWorld gameWorld;
 	private Camera2D camera;
 	private SpriteBatch spriteBatch;
 	private Scene scene;
 	private Mouse mouse;
 	private Keyboard keyboard;
 	private SoundManager soundManager;
 	private Network network;
 	private EntityNetworkIDManager idManager;
 	 
 	public static void main(String[] args) {
 		new ClientTest().start();
 	}
 	public ClientTest() {
 		super(sr, 30);
 		this.idManager = new EntityNetworkIDManager();
 		this.network = new Network(45777,43567);
 	}
 	@Override
 	protected void end() {
 		this.network.reset();
 	}
 	@Override
 	public void update(GameTime time) {
 		this.mouse.poll(sr);
 		this.keyboard.poll();
 		sendInput();		
 		
 		
 		this.gameWorld.getEntityManager().destoryKilledEntities();
 		this.gameWorld.update(time);
 	}
 	private void sendInput() {
 		Client client = this.network.getClient();
 		InputMessage message = new InputMessage();
 		message.networkID = client.getID();
 		message.keyboard = this.keyboard;
 		message.mouse = this.mouse;
 		
 		client.sendTCP(message);
 	}
 	@Override
 	public void render(GameTime time) {
 		this.spriteBatch.clearScreen(Color.Black);
 		this.spriteBatch.begin(null, this.camera.getTransformation());
 		this.gameWorld.render();
 		this.spriteBatch.end();	
 	}
 	@Override
 	protected void initialize() {
 		this.connect();
 		this.createWorld();
 	}
 	
 	private void createWorld() {
 		this.scene = this.ContentManager.load("PerspectiveV5.xml", Scene.class);
 		this.mouse = new Mouse();
 		this.keyboard = new Keyboard();
 		this.soundManager = new SoundManager(this.ContentManager, 0.1f,1.0f,1.0f);
 		this.spriteBatch = new SpriteBatch(sr);
 		this.camera = new Camera2D(scene.getWorldBounds(), sr);
 		
 		Random rand = new Random();
 		
 		PlayerCharacteristics playerChars = new PlayerCharacteristics();
 		playerChars.bodyColor	= new Color(rand.nextFloat(), rand.nextFloat(),rand.nextFloat(),1.0f);
 		playerChars.eyeColor 	= new Color(rand.nextFloat(), rand.nextFloat(),rand.nextFloat(),1.0f);
 		playerChars.maneColor	= new Color(rand.nextFloat(), rand.nextFloat(),rand.nextFloat(),1.0f);
 		
 		
 		this.gameWorld = WorldBuilder.buildClientWorld(camera, scene, mouse, keyboard, ContentManager, this.soundManager, spriteBatch, false, "Player" + this.network.getClient().getID());
		NetworkSystemBuilder.createClientSystems(gameWorld, this.network, idManager, soundManager, ContentManager, playerChars);
 		
 		this.gameWorld.initialize();
 	}
 		
 	private void connect() {
 		this.network.getAvalibleLanHosts();
 		
 		Client client = this.network.getClient();
 		InetAddress adr = this.network.getAvalibleLanHosts().get(0);
 		this.network.connectToHost(adr);
 	}
 }
