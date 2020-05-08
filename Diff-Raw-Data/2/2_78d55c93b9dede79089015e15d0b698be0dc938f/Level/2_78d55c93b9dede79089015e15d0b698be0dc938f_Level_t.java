 package level;
 
 import inventory.ConcreteItem;
 import inventory.Item;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.StringTokenizer;
 
 import npc.NPC;
 import player.Player;
 import utils.JsonUtil;
 import utils.JsonUtil.JSONInventory;
 import utils.JsonUtil.JSONItem;
 import utils.JsonUtil.JSONNpc;
 import utils.JsonUtil.JSONPlayer;
 import utils.Location;
 import app.RPGame;
 import collisions.BoundaryCollision;
 import collisions.ItemCollision;
 import collisions.AutomatedCharCollision;
 import collisions.SceneryCollision;
 
 import com.golden.gamedev.engine.BaseIO;
 import com.golden.gamedev.engine.BaseLoader;
 import com.golden.gamedev.engine.timer.SystemTimer;
 import com.golden.gamedev.object.PlayField;
 import com.golden.gamedev.object.Sprite;
 import com.golden.gamedev.object.SpriteGroup;
 import com.golden.gamedev.object.background.abstraction.AbstractTileBackground;
 import com.golden.gamedev.util.FileUtil;
 import com.golden.gamedev.util.ImageUtil;
 import com.google.gson.Gson;
 
 import enemy.Enemy;
 import evented.Evented;
 import gameCharacter.GameCharacter;
 
 public class Level extends AbstractTileBackground implements Evented {
 
 	private static final long serialVersionUID = 1L;
 
 	private final String levelname;
 	private BaseIO baseio;
 	private BaseLoader bsloader;
 
 	Chipset chipsetE;
 	Chipset chipsetF;
 	Chipset chipsetG;
 	Chipset[] chipset;
 
 	private static final int TILE_WIDTH = 32, TILE_HEIGHT = 32;
 	int[][] layer1 = new int[40][25]; // the lower tiles
 	int[][] layer2 = new int[40][25]; // the upper tiles
 
 	private SystemTimer levelTimer = new SystemTimer();
 	protected long levelStartTime;
 	protected String nextLevelName;
 	protected String startText;
 
 	private RPGame game;
 	private LevelInventory<Item> inventory;
 
 	public Level(BaseLoader bsLoader, BaseIO bsIO, RPGame game, String levelname) {
 		super(0, 0, TILE_WIDTH, TILE_HEIGHT);
 
 		this.game = game;
 		this.inventory = new LevelInventory<Item>(game);
 		this.levelname = levelname;
 		this.baseio = bsIO;
 		this.bsloader = bsLoader;
 
 		initResources();
 	}
 
 	public void initResources() {
 		Gson gson = new Gson();
 		String json = JsonUtil.getJSON(levelname);
 
 		JsonUtil.JSONLevel level = gson
 				.fromJson(json, JsonUtil.JSONLevel.class);
 
 		setChipsets();
 		setTiles(level);
 		setSize(layer1.length, layer1[0].length);
 		setLevelTimer();
 
 		setPlayer(level);
 		setNpcs(level);
 		setItems(level);
 		setEnemies();
 
 		setCollisions();
 	}
 
 	private void setCollisions() {
 		SceneryCollision sceneCol = new SceneryCollision();
 		ItemCollision itCol = new ItemCollision();
 		AutomatedCharCollision collision = new AutomatedCharCollision();
 		BoundaryCollision boundCol = new BoundaryCollision(this);
 
 		PlayField field = game.getField();
 		SpriteGroup player = field.getGroup("player");
 
 		game.getField().addCollisionGroup(player, field.getGroup("npcs"),
 				collision);
 		game.getField().addCollisionGroup(player, field.getGroup("items"),
 				itCol);
 		game.getField().addCollisionGroup(player, field.getGroup("scenery"),
 				sceneCol);
 		game.getField().addCollisionGroup(player, null, boundCol);
 	}
 
 	private void setChipsets() {
 		chipsetE = new Chipset(bsloader.getImages("rsc/level/ChipSet2.png", 6,
 				24, false));
 		chipsetF = new Chipset(bsloader.getImages("rsc/level/ChipSet3.png", 6,
 				24));
 		chipsetG = new Chipset(bsloader.getImages("rsc/player/player.png", 6, 
 				24));
 
 		chipset = new Chipset[16];
 		BufferedImage[] image = bsloader.getImages("rsc/level/ChipSet1.png", 4,
 				4, false);
 		int[] chipnum = new int[] { 0, 1, 4, 5, 8, 9, 11, 12, 2, 3, 6, 7, 10,
 				11, 14, 15 };
 		for (int i = 0; i < chipset.length; i++) {
 			int num = chipnum[i];
 			BufferedImage[] chips = ImageUtil.splitImages(image[num], 3, 4);
 			chipset[i] = new Chipset(chips);
 		}
 	}
 
 	public LevelInventory<Item> getInventory() {
 		return inventory;
 	}
 
 	private void setLevelTimer() {
 		levelTimer.setFPS(100);
 		levelTimer.startTimer();
 		levelStartTime = levelTimer.getTime();
 	}
 
 	private void setPlayer(JsonUtil.JSONLevel level) {
 		JSONPlayer jPlayer = level.player;
 		SpriteGroup group = new SpriteGroup("player");
 
 		Location playerLoc = new Location(jPlayer.location);
 		Player player = new Player(new GameCharacter(game, playerLoc,
 				jPlayer.directionsURL), jPlayer.actionsURL);
 
 		game.setPlayer(player);
 		group.add(player.getCharacter());
 		game.getField().addGroup(group);
 	}
 
 	private void setItems(JsonUtil.JSONLevel level) {
 		JSONInventory inventory = level.inventory;
 		SpriteGroup group = new SpriteGroup("items");
 
 		for (JSONItem it : inventory.items) {
 			Item item = new ConcreteItem(game, it);
 			group.add(item);
 			System.out.println("Added concrete item to sprite group");
 		}
 
 		game.getField().addGroup(group);
 	}
 
 	private void setNpcs(JsonUtil.JSONLevel level) {
 		JSONNpc[] npcs = level.npcs;
 		SpriteGroup group = new SpriteGroup("npcs");
 
 		for (JSONNpc jsonNpc : npcs) {
 			Location loc = new Location(jsonNpc.location);
			NPC npc = new NPC(new GameCharacter(game, loc, jsonNpc.directions));
 			group.add(npc.getCharacter());
 		}
 		game.getField().addGroup(group);
 	}
 	
 	private void setEnemies(){
 		SpriteGroup group = new SpriteGroup("enemies");
 		Enemy enemy = new Enemy(game,new GameCharacter(game, new Location(250,250), "rsc/config/player_directions.json"),"doesntmatter");
 		group.add(enemy.getCharacter());
 		game.getField().addGroup(group);
 	}
 
 	private void setTiles(JsonUtil.JSONLevel level) {
 		String[] lowerTile = FileUtil.fileRead(baseio
 				.getStream(level.lowerFilename));
 		String[] upperTile = FileUtil.fileRead(baseio
 				.getStream(level.upperFilename));
 
 		SpriteGroup scenery = new SpriteGroup("scenery");
 
 		for (int j = 0; j < layer1[0].length; j++) {
 			StringTokenizer lowerToken = new StringTokenizer(lowerTile[j]);
 			StringTokenizer upperToken = new StringTokenizer(upperTile[j]);
 			for (int i = 0; i < layer1.length; i++) {
 				layer1[i][j] = Integer.parseInt(lowerToken.nextToken());
 
 				Location loc = new Location(TILE_WIDTH * i, TILE_HEIGHT * j);
 				int type = Integer.parseInt(upperToken.nextToken());
 				setSceneryLayer(type, loc, scenery);
 				}
 		game.getField().addGroup(scenery);
 		}
 	}
 
 	private void setSceneryLayer(int type, Location loc, SpriteGroup scenery) {
 		if (type == -1)
 			return;
 		scenery.add(new Sprite(chipsetF.image[type], loc.getX(), loc.getY()));
 	}
 
 	public void nextLevel(String next) {
 
 	}
 
 	public void setStartText(String text) {
 		startText = text;
 	}
 
 	public void setNextLevel(String nextLevel) {
 		nextLevelName = nextLevel;
 	}
 
 	public void render(Graphics2D g) {
 		for (int i = 0; i < layer1.length; i++) {
 			for (int j = 0; j < layer1[0].length; j++) {
 				renderTile(g, i, j, TILE_WIDTH * i, TILE_HEIGHT * j);
 			}
 		}
 
 		GameCharacter player = game.getPlayer().getCharacter();
 		setToCenter((int) player.getX(), (int) player.getY(),
 				player.getWidth(), player.getHeight());
 
 		super.render(g);
 	}
 
 	public void renderTile(Graphics2D g, int tileX, int tileY, int x, int y) {
 		// render layer 1
 		int tilenum = layer1[tileX][tileY];
 
 		if (tilenum < chipsetE.image.length)
 			g.drawImage(chipsetE.image[tilenum], x, y, null);
 		else if (tilenum >= chipsetE.image.length) {
 			BufferedImage image = chipset[tilenum - chipsetE.image.length].image[2];
 			g.drawImage(image, x, y, null);
 		}
 	}
 	
 	// chipset is only a pack of images
 	class Chipset {
 		BufferedImage[] image;
 
 		public Chipset(BufferedImage[] image) {
 			this.image = image;
 		}
 
 	}
 }
