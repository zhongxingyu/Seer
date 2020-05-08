 package propra2012.gruppe33.bomberman;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Point;
 import java.awt.Transparency;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.Random;
 import java.util.UUID;
 
 import propra2012.gruppe33.bomberman.graphics.rendering.scenegraph.grid.GridLoader;
 import propra2012.gruppe33.bomberman.graphics.rendering.scenegraph.grid.input.InputActivator;
 import propra2012.gruppe33.bomberman.graphics.rendering.scenegraph.grid.items.CollectableItem;
 import propra2012.gruppe33.bomberman.graphics.rendering.scenegraph.grid.items.spawners.SpawnDead;
 import propra2012.gruppe33.bomberman.graphics.rendering.scenegraph.grid.transform.DeltaPositionBroadcaster;
 
 import com.indyforge.foxnet.rmi.InvokerManager;
 import com.indyforge.foxnet.rmi.pattern.change.AdminSessionServer;
 import com.indyforge.foxnet.rmi.pattern.change.Session;
 import com.indyforge.foxnet.rmi.util.Future;
 import com.indyforge.foxnet.rmi.util.FutureCallback;
 import com.indyforge.twod.engine.graphics.ImageDesc;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.GraphicsEntity;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.RenderedImage;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.Scene;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.SceneProcessor;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.Text;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.Text.Alignment;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.math.Grid;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.math.Vector2f;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.network.scene.ResetNetworkTimeChange;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.network.scene.SceneChange;
 import com.indyforge.twod.engine.graphics.sprite.Sprite;
 import com.indyforge.twod.engine.resources.Resource;
 import com.indyforge.twod.engine.resources.TransientSystemFontResource;
 import com.indyforge.twod.engine.resources.assets.AssetManager;
 
 /**
  * 
  * @author Christopher Probst
  * 
  */
 public final class Game implements GameConstants, Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public enum Char {
 		Dwarf, Santa, Knight, Wizard
 	}
 
 	/*
 	 * 
 	 */
 	private String assetBundle = "res/default.zip",
 			mapPropAssetPath = "assets/maps/map_1.prop";
 	private float broadcastUpdateTime = 0.030f;
 	private int players = 1;
 
 	public int players() {
 		return players;
 	}
 
 	public Game players(int players) {
 		if (players < 1) {
 			throw new IllegalArgumentException("players must be >= 1");
 		}
 		this.players = players;
 		return this;
 	}
 
 	public String assetBundle() {
 		return assetBundle;
 	}
 
 	public Game assetBundle(String assetBundle) {
 		this.assetBundle = assetBundle;
 		return this;
 	}
 
 	public String mapPropAssetPath() {
 		return mapPropAssetPath;
 	}
 
 	public Game mapPropAssetPath(String mapPropAssetPath) {
 		this.mapPropAssetPath = mapPropAssetPath;
 		return this;
 	}
 
 	/**
 	 * @return the broadcastUpdateTime
 	 */
 	public float broadcastUpdateTime() {
 		return broadcastUpdateTime;
 	}
 
 	/**
 	 * @param broadcastUpdateTime
 	 *            the broadcastUpdateTime to set
 	 * @return this for chaining.
 	 */
 	public Game boadcastUpdateTime(float broadcastUpdateTime) {
 		this.broadcastUpdateTime = broadcastUpdateTime;
 		return this;
 	}
 
 	public SceneProcessor serverGame(SceneProcessor sceneProcessor)
 			throws Exception {
 		if (sceneProcessor == null) {
 			throw new NullPointerException("sceneProcessor");
 		}
 		return serverGame(sceneProcessor, assetBundle, mapPropAssetPath,
 				broadcastUpdateTime, players);
 	}
 
 	public SceneProcessor serverGame(SceneProcessor sceneProcessor,
 			String assetBundle, String mapPropAssetPath,
 			float broadcastUpdateTime, int players) throws Exception {
 		if (sceneProcessor == null) {
 			throw new NullPointerException("sceneProcessor");
 		}
 
 		// Check network mode
 		if (!sceneProcessor.hasAdminSessionServer()) {
 			throw new IllegalArgumentException("The scene processor "
 					+ "must a server");
 		}
 
 		// Get the server interface
 		final AdminSessionServer<SceneProcessor> server = sceneProcessor
 				.adminSessionServer();
 
 		// Here we put the choosen chars
 		Map<Long, Char> charMap = new HashMap<Long, Char>();
 
 		// Char random
 		Random r = new Random();
 
 		// Session
 		Map<Long, Session<SceneProcessor>> sessions = server.sessionMap();
 
 		// Copy ids with chars
 		for (long id : sessions.keySet()) {
 			charMap.put(id, Char.values()[r.nextInt(Char.values().length)]);
 		}
 
 		/*
 		 * SHUTDOWN!!! ???
 		 */
 		if (players != charMap.size()) {
 			return sceneProcessor.shutdownRequest(true);
 		}
 
 		// The player refs will be stored here
 		List<UUID> refs = new LinkedList<UUID>();
 
 		/*
 		 * Create a new server game scene!
 		 */
 		Scene scene = serverGameScene(refs, broadcastUpdateTime, assetBundle,
 				mapPropAssetPath, charMap);
 
 		// Apply the scene change
 		server.composite().applyChange(new SceneChange(scene));
 
 		// Enable input on all clients !!
 		int ptr = 0;
 		for (Long id : charMap.keySet()) {
 
 			// Create a new input activator
 			InputActivator inputAc = new InputActivator();
 
 			// Use linear reference
 			inputAc.entities().add(refs.get(ptr));
 
 			// Activate the client
 			InvokerManager im = InvokerManager.of(sessions.get(id).client());
 
 			// Get k!
 			final UUID k = refs.get(ptr++);
 
 			// Register!!!
 			im.closeFuture().add(new FutureCallback() {
 
 				/*
 				 * (non-Javadoc)
 				 * 
 				 * @see
 				 * com.indyforge.foxnet.rmi.util.FutureCallback#completed(com
 				 * .indyforge.foxnet.rmi.util.Future)
 				 */
 				@Override
 				public void completed(Future future) throws Exception {
 					SpawnDead sd = new SpawnDead();
 					sd.entities().add(k);
 					server.composite().applyChange(sd);
 				}
 			});
 
 			sessions.get(id).client().applyChange(inputAc);
 		}
 
 		// Reset the network time every where
 		server.composite().applyChange(new ResetNetworkTimeChange());
 
 		return sceneProcessor;
 	}
 
 	public Game copy() {
 		return new Game().assetBundle(assetBundle)
 				.mapPropAssetPath(mapPropAssetPath)
 				.boadcastUpdateTime(broadcastUpdateTime).players(players);
 	}
 
 	public Scene serverGameScene(List<UUID> refs, float broadcastUpdateTime,
 			String assetBundle, String mapPropAssetPath,
 			Map<Long, Char> playerIds) throws Exception {
 
 		if (playerIds == null || playerIds.isEmpty()) {
 			throw new IllegalArgumentException("No players ids provided");
 		}
 
 		// Delete old refs...
 		refs.clear();
 
 		// Create new asset manager using the given zip file
 		AssetManager assets = new AssetManager(new File(assetBundle));
 
 		// Use property file
 		Properties properties = new Properties();
 
 		// Try to load the given prop file
 		properties.load(assets.open(mapPropAssetPath));
 
 		// Parse the width and height
 		int width = Integer.parseInt(properties.getProperty(MAP_WIDTH_PROP));
 		int height = Integer.parseInt(properties.getProperty(MAP_HEIGHT_PROP));
 
 		// Create new scene with the given assets
		Scene scene = new Scene(assets, Math.round(width * 5 / 4f), height) {
 
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onUpdate(float tpf) {
 				super.onUpdate(tpf);
 
 				if (isSinglePressed(KeyEvent.VK_ESCAPE)) {
 					if (processor().hasSession()) {
 						// Simply terminate the connection
 						InvokerManager.of(processor().session()).close();
 					}
 				}
 			}
 		};
 
 		// Load the char array
 		char[][] map = assets.loadAsset(properties.getProperty(MAP_NAME_KEY),
 				GridLoader.LOADER, true).get();
 
 		// Lookup max spawn points
 		int maxSpawns = GridLoader.find(map, SPAWN).size();
 
 		/*
 		 * Compare max spawn points with the player count.
 		 */
 		if (playerIds.size() > maxSpawns) {
 			throw new IllegalArgumentException("Too many players");
 		}
 
 		// Generate the map randomally
 		GridLoader.generate(map, System.nanoTime());
 
 		// Put the new sounds
 		scene.soundManager().putSound(EXP_SOUND,
 				properties.getProperty(EXP_SOUND));
 		scene.soundManager().putSound(EAT_SOUND,
 				properties.getProperty(EAT_SOUND));
 		scene.soundManager().putSound(GLASS_SOUND,
 				properties.getProperty(GLASS_SOUND));
 		scene.soundManager().putSound(SHIELD_ON_SOUND,
 				properties.getProperty(SHIELD_ON_SOUND));
 		scene.soundManager().putSound(SHIELD_OFF_SOUND,
 				properties.getProperty(SHIELD_OFF_SOUND));
 		scene.soundManager().putSound(PICKUP_SOUND,
 				properties.getProperty(PICKUP_SOUND));
 		scene.soundManager().putSound(PLACE_SOUND,
 				properties.getProperty(PLACE_SOUND));
 
 		// Preload the next game map
 		scene.addProp(NEXT,
 				copy().mapPropAssetPath(properties.getProperty(NEXT)));
 
 		// Register the global resources
 		scene.addProp(ACTIVE_FLARE_IMAGE, assets.loadImage(
 				properties.getProperty(ACTIVE_FLARE_IMAGE), true));
 
 		scene.addProp(ITEM_INTERFACE,
 				assets.loadImage(properties.getProperty(ITEM_INTERFACE), true));
 
 		scene.addProp(DEFAULT_BOMB_IMAGE, assets.loadImage(
 				properties.getProperty(DEFAULT_BOMB_IMAGE), true));
 		scene.addProp(DEFAULT_BOMB_BAG_IMAGE, assets.loadImage(
 				properties.getProperty(DEFAULT_BOMB_BAG_IMAGE), true));
 
 		scene.addProp(NUKE_BOMB_IMAGE,
 				assets.loadImage(properties.getProperty(NUKE_BOMB_IMAGE), true));
 		scene.addProp(NUKE_BOMB_BAG_IMAGE, assets.loadImage(
 				properties.getProperty(NUKE_BOMB_BAG_IMAGE), true));
 
 		scene.addProp(FAST_BOMB_IMAGE,
 				assets.loadImage(properties.getProperty(FAST_BOMB_IMAGE), true));
 		scene.addProp(FAST_BOMB_BAG_IMAGE, assets.loadImage(
 				properties.getProperty(FAST_BOMB_BAG_IMAGE), true));
 
 		scene.addProp(PALISADE_HORI_IMAGE, assets.loadImage(
 				properties.getProperty(PALISADE_HORI_IMAGE), true));
 		scene.addProp(PALISADE_VERT_IMAGE, assets.loadImage(
 				properties.getProperty(PALISADE_VERT_IMAGE), true));
 		scene.addProp(PALISADE_BAG_IMAGE, assets.loadImage(
 				properties.getProperty(PALISADE_BAG_IMAGE), true));
 
 		scene.addProp(SHIELD_IMAGE,
 				assets.loadImage(properties.getProperty(SHIELD_IMAGE), true));
 		scene.addProp(SHIELD_POTION_IMAGE, assets.loadImage(
 				properties.getProperty(SHIELD_POTION_IMAGE), true));
 
 		scene.addProp(SPEED_IMAGE,
 				assets.loadImage(properties.getProperty(SPEED_IMAGE), true));
 		scene.addProp(SLOW_SHROOM_IMAGE, assets.loadImage(
 				properties.getProperty(SLOW_SHROOM_IMAGE), true));
 		scene.addProp(FAST_SHROOM_IMAGE, assets.loadImage(
 				properties.getProperty(FAST_SHROOM_IMAGE), true));
 
 		scene.addProp(BREAKABLE_IMAGE,
 				assets.loadImage(properties.getProperty(BREAKABLE_IMAGE), true));
 		scene.addProp(SOLID_IMAGE,
 				assets.loadImage(properties.getProperty(SOLID_IMAGE), true));
 		scene.addProp(GROUND_IMAGE,
 				assets.loadImage(properties.getProperty(GROUND_IMAGE), true));
 		scene.addProp(ESCAPE_IMAGE,
 				assets.loadImage(properties.getProperty(ESCAPE_IMAGE), true));
 
 		scene.addProp(CORNER_LD_IMAGE,
 				assets.loadImage(properties.getProperty(CORNER_LD_IMAGE), true));
 		scene.addProp(CORNER_LU_IMAGE,
 				assets.loadImage(properties.getProperty(CORNER_LU_IMAGE), true));
 		scene.addProp(CORNER_RD_IMAGE,
 				assets.loadImage(properties.getProperty(CORNER_RD_IMAGE), true));
 		scene.addProp(CORNER_RU_IMAGE,
 				assets.loadImage(properties.getProperty(CORNER_RU_IMAGE), true));
 
 		scene.addProp(WALL_D_IMAGE,
 				assets.loadImage(properties.getProperty(WALL_D_IMAGE), true));
 		scene.addProp(WALL_U_IMAGE,
 				assets.loadImage(properties.getProperty(WALL_U_IMAGE), true));
 		scene.addProp(WALL_R_IMAGE,
 				assets.loadImage(properties.getProperty(WALL_R_IMAGE), true));
 		scene.addProp(WALL_L_IMAGE,
 				assets.loadImage(properties.getProperty(WALL_L_IMAGE), true));
 
 		// Parse the width and height of the sprite
 		int spriteWidth = Integer.parseInt(properties
 				.getProperty(EXP_SPRITE_WIDTH));
 		int spriteHeight = Integer.parseInt(properties
 				.getProperty(EXP_SPRITE_HEIGHT));
 
 		// Register the global explosion content
 		scene.addProp(
 				EXP_SPRITE,
 				new Sprite(assets.loadImage(properties.getProperty(EXP_SPRITE),
 						true), spriteWidth, spriteHeight));
 
 		// Parse and setup map
 		GraphicsEntity grid = GridLoader.parse(map, scene, width, height,
 				broadcastUpdateTime, System.nanoTime(),
 				playerIds.size() == 1 ? 1 : 2, 0.2f, 0.2f, 0.2f, 0.1f, 0.1f,
 				0.1f, 0.1f);
 
 		/*
 		 * ********************************************************************
 		 * SETUP THE ITEM INTERFACE STUFF.
 		 * ********************************************************************
 		 */
 
 		// Load the item interface
 		RenderedImage itemInterface = new RenderedImage(
 				scene.imageProp(ITEM_INTERFACE));
 
 		// Set the correct scale
		itemInterface.scale().set(width * 0.25f, height);
 
 		// Move the interface to the right
 		itemInterface.position().x = width;
 
 		// Attach the item interface
 		scene.attach(itemInterface);
 
 		// The counter
 		int counter = 0;
 
 		// Create a new font
 		Resource<Font> font = new TransientSystemFontResource("Verdana",
 				Font.BOLD, 36);
 
 		// Used to create the text
 		ImageDesc countTextDesc = new ImageDesc().width(64).height(64)
 				.transparency(Transparency.TRANSLUCENT);
 
 		for (CollectableItem item : CollectableItem.values()) {
 
 			// Load the item holder
 			RenderedImage itemHolder = new RenderedImage(
 					scene.imageProp(GameRoutines.itemToAsset(item)));
 
 			// Set scale
			itemHolder.scale().scaleLocal(width * 0.10f);
 
 			// Set position
			itemHolder.position().set(width * 1.075f,
					width * 0.05f + width * 0.11f * counter++);
 
 			// Attach to scene
 			scene.attach(itemHolder);
 
 			// Create a new text object
 			Text text = new Text(countTextDesc);
 
 			// Shift down a bit
 			text.position().y += 0.3f;
 
 			// Center alignment
 			text.alignment(Alignment.Right);
 
 			// Set the font
 			text.fontResource(font);
 
 			// Use red color
 			text.textColor(Color.RED);
 
 			// Init with X
 			text.text("X");
 
 			// Simply attach
 			itemHolder.attach(text);
 
 			// Add the text as prop
 			scene.addProp(item, text);
 		}
 		/*
 		 * ********************************************************************
 		 * INTERFACE STUFF FINISHED
 		 * ********************************************************************
 		 */
 
 		// The spawn points
 		List<Point> spawnPoints = GridLoader.find(map, GameConstants.SPAWN);
 
 		/*
 		 * Shuffle all spawn points!
 		 */
 		Collections.shuffle(spawnPoints);
 
 		int i = 0;
 
 		/*
 		 * For all requested players.
 		 */
 		for (Entry<Long, Char> entry : playerIds.entrySet()) {
 
 			// Create new player as knight
 			GraphicsEntity player = null;
 
 			/*
 			 * Create new char.
 			 */
 			switch (entry.getValue()) {
 			case Dwarf:
 				player = GameRoutines.createRemoteDwarf(assets,
 						String.valueOf(entry.getKey()));
 				break;
 			case Knight:
 				player = GameRoutines.createRemoteKnight(assets,
 						String.valueOf(entry.getKey()));
 				break;
 			case Santa:
 				player = GameRoutines.createRemoteSanta(assets,
 						String.valueOf(entry.getKey()));
 				break;
 			case Wizard:
 				player = GameRoutines.createRemoteWizard(assets,
 						String.valueOf(entry.getKey()));
 				break;
 			default:
 				throw new IllegalArgumentException("Unknown char");
 			}
 
 			// Add the registration key to list
 			refs.add(player.registrationKey());
 
 			/*
 			 * Register to delta position broadcaster.
 			 */
 			scene.typeProp(DeltaPositionBroadcaster.class)
 					.entities()
 					.put(player.registrationKey(),
 							new Vector2f(spawnPoints.get(i)));
 
 			// Place to spawn
 			grid.childAt(grid.typeProp(Grid.class).index(spawnPoints.get(i++)))
 					.attach(player);
 		}
 
 		/**
 		 * AI TEST
 		 * 
 		 */
 		// GraphicsEntity aiPlayer = GameRoutines
 		// .createRemoteWizard(assets, "BOT");
 		//
 		// /*
 		// * Register to delta position broadcaster.
 		// */
 		// scene.typeProp(DeltaPositionBroadcaster.class)
 		// .entities()
 		// .put(aiPlayer.registrationKey(),
 		// new Vector2f(spawnPoints.get(spawnPoints.size() - 1)));
 		//
 		// // Place to spawn
 		// grid.childAt(
 		// grid.typeProp(Grid.class).index(
 		// spawnPoints.get(spawnPoints.size() - 1))).attach(
 		// aiPlayer);
 		//
 		// Bot bot = new Bot();
 		//
 		// DefaultAIProcessor proc = new DefaultAIProcessor(new
 		// DefaultAIControl(
 		// aiPlayer), bot);
 		// aiPlayer.attach(proc);
 
 		/**
 		 * AI END
 		 */
 
 		// Store the player refs
 		scene.addProp(PLAYERS_KEY, new LinkedList<UUID>(refs));
 
 		// Store the ids + chars
 		scene.addProp(SESSIONS_KEY, playerIds);
 
 		// Add the game creator
 		scene.addTypeProp(this);
 
 		return scene;
 	}
 }
