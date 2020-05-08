 package cz.emo4d.zen.screens;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Interpolation;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.actions.Actions;
 import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
 import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 
 import cz.emo4d.zen.Zen;
 import cz.emo4d.zen.gameplay.BulletManager;
 import cz.emo4d.zen.gameplay.Enemy;
 import cz.emo4d.zen.gameplay.PlayerManager;
 import cz.emo4d.zen.gameplay.RemotePlayer;
 import cz.emo4d.zen.remote.ClientMove;
 import cz.emo4d.zen.remote.DeviceEvent;
 import cz.emo4d.zen.remote.DeviceEventHandler;
 import cz.emo4d.zen.remote.RemoteControl;
 import cz.emo4d.zen.ui.GameGuiStage;
 
 
 public class GameScreen extends BaseScreen implements DeviceEventHandler {
 
 	//	private TiledMap map;
 	//	private OrthogonalTiledMapRenderer renderer;
 
 	private Map map;
 
 	private GameGuiStage gui;
 
 	private final PlayerManager playerManager;
 	private OrthographicCamera camera;
 	Vector2 moveVec = new Vector2();
 
 	private BulletManager bulletManager;
 	private Enemy enemy;
 
 	private GameInputAdapter gameInputAdapter = new GameInputAdapter(this);
 	private InputMultiplexer inputMpx = new InputMultiplexer();
 
 	private RemoteControl rc = new RemoteControl();
 	
 	// Master player remote connection
 	private int remoteMaster = -1;
 	
 	// Slave players array
 	private ArrayList<RemotePlayer> remoteSlaves;
 	private ArrayList<RemotePlayer> pendingSlaves;
 
 	private Vector2 kickvector = new Vector2();
 
 
 	public GameScreen(Zen game) {
 		super(game);
 
 		map = new Map("fit");
 
 		// create an orthographic camera, shows us 30x20 units of the world
 		camera = new OrthographicCamera();
 		camera.setToOrtho(false, 30, 20);
 		camera.update();
 
 
 		// invertovat Y souradnici pro indexovani s nulou v levem HORNIM rohu
 		playerManager = new PlayerManager(map, map.getCoord(7, 6));
 		playerManager.addPlayer(map.getCoord(7, 6));
 
 		bulletManager = new BulletManager(map, new Texture(Gdx.files.internal("data/bullet.png")));
 
 		enemy = new Enemy(map.getCoord(7, 8));
 		enemy.setMap(map);
 		
 		remoteSlaves = new ArrayList<RemotePlayer>();
 		pendingSlaves = new ArrayList<RemotePlayer>();
 		rc.RegisterEventHandler(this);
 
 		gui = new GameGuiStage(this);
 
 		inputMpx.addProcessor(gui);
 		inputMpx.addProcessor(gameInputAdapter);
 	}
 
 	public void onKeyPress(int keycode) {
 		if (keycode == Keys.CONTROL_LEFT) {
 			bulletManager.shoot(playerManager.getMainPlayer().position, playerManager.getMainPlayer().currentDir);
 		}
 		if (keycode == Keys.TAB) {
 			//			doTeleport(map.getCoord(14, 36));
 		}
 	}
 
 
 	@Override
 	public void render(float deltaTime) {
 		// clear the screen
 		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		// -- process input -- 
 		moveVec.set(0, 0);
 
 		// controller input
 		
 		ClientMove cm;
 		if (remoteMaster != -1) {
 			if ((cm = rc.getClientMove(remoteMaster)) != null) {
				//Gdx.app.log("MOVE","MASTER");
 				moveVec.set(cm.X,-cm.Y);
 				playerManager.controllerInput(0, moveVec);
 			}
 		}
 		for (int i = 0; i < pendingSlaves.size(); i++) {
 			RemotePlayer rp = pendingSlaves.get(i);
 			rp.localId = playerManager.addPlayer(playerManager.getMainPlayer().position);
			//Gdx.app.log("SLAVE CREATE", Integer.toString(rp.localId));
 			playerManager.controllerInput(rp.localId, new Vector2(1.0f,1.0f));
 			remoteSlaves.add(rp);
 			pendingSlaves.remove(i);
 			i--;
 		}
 		
 		for (int i = 0; i < remoteSlaves.size(); i++) {
 			if ((cm = rc.getClientMove(remoteSlaves.get(i).remoteId)) != null) {
 				//Gdx.app.log("MOVE","SLAVE " + Integer.toString(remoteSlaves.get(i).localId));
 				moveVec.set(cm.X,-cm.Y);
 				playerManager.controllerInput(remoteSlaves.get(i).localId ,moveVec);
 			}
 		}
 		
 		// keyboard input
 		playerManager.keyboardInput();
 
 
 		// -- update --
 		playerManager.update(deltaTime);
 
 		// teleport ven
 		if (playerManager.getMainPlayer().getCollidingOutPoint() != null) {
 			Map.Position outPt = playerManager.getMainPlayer().getCollidingOutPoint();
 
 			playerManager.setSwitchingRooms(true);
 			doTeleport(outPt);
 
 			Gdx.app.log("OUT", "--> " + outPt.identifier + " (" + outPt.mapName + ") @ " + outPt.coordinates.toString());
 		}
 
 		// teleport dovnitr
 		if (playerManager.getMainPlayer().getCollidingInPoint() != null) {
 			Gdx.app.log("IN", "<-- " + playerManager.getMainPlayer().getCollidingInPoint().identifier);
 		}
 
 
 
 
 		bulletManager.update(deltaTime);
 		bulletManager.collisionWithMap();
 		if (enemy.health > 0) {
 			int hits = bulletManager.collision(enemy);
 			enemy.health -= 20 * hits;
 			enemy.update(deltaTime);
 		}
 
 
 		// let the camera follow the player
 		camera.position.x = playerManager.getMainPlayer().position.x;
 		camera.position.y = playerManager.getMainPlayer().position.y;
 		camera.update();
 
 		// render 'underlay'
 		map.renderUnderlay(camera);
 
 		// render players & dynamic entities 
 		SpriteBatch batch = map.renderer.getSpriteBatch();
 		batch.begin();
 		playerManager.render(batch);
 		if (enemy.health > 0)
 			enemy.render(batch);
 		bulletManager.render(batch);
 		batch.end();
 
 		// render overlay
 		map.renderOverlay(camera);
 
 		// gui
 		gui.act(deltaTime);
 		gui.draw();
 	}
 
 
 
 
 	public void doTeleport(final Map.Position newPos) {
 		final Table background = gui.createBackground();
 
 		SequenceAction seq = new SequenceAction();
 
 		seq.addAction(Actions.fadeIn(0.2f, Interpolation.fade));
 
 		seq.addAction(new RunnableAction() {
 			@Override
 			public void run() {
 				map = new Map(newPos.mapName);
 				Map.Position targetPos = map.inPoints.get(newPos.identifier);
 				playerManager.teleportAllPlayers(targetPos.coordinates);
 
 				kickvector.set(targetPos.direction);
 				kickvector.mul(8);
 				playerManager.applyKick(kickvector);
 			}
 		});
 
 		seq.addAction(Actions.fadeOut(0.2f, Interpolation.fade));
 
 		seq.addAction(Actions.delay(0.3f));
 
 		seq.addAction(new RunnableAction() {
 			@Override
 			public void run() {
 				playerManager.setSwitchingRooms(false);
 			}
 		});
 
 		seq.addAction(Actions.removeActor());
 
 		background.addAction(seq);
 	}
 
 
 
 
 	@Override
 	public void acceptEvent(int type, int device, float X, float Y) {
 		if (type == DeviceEvent.MOVE) {
 			
 		} else if (type == DeviceEvent.CONNECT) {
 			Gdx.app.log("Player Connection", Float.toString(X));
 			if (X == 0.0) {
 				// Master connected
 				Gdx.app.log("Conn", "Master at slot " + Integer.toString(device));
 				remoteMaster = device;
 			} else {
 				Gdx.app.log("Conn", "Slave");
 				RemotePlayer rp = new RemotePlayer();
 				rp.remoteId = device;
 				
 				
 				pendingSlaves.add(rp);
 				
 			}
 			
 		} else if (type == DeviceEvent.DISCONNECT) {
 
 		}
 	}
 
 
 	@Override
 	public void show() {
 		super.show();
 
 		Gdx.input.setInputProcessor(inputMpx);
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		gui.setViewport(width, height, true);
 	}
 
 }
