 package level;
 
 import gameCharacter.GameCharacter;
 import inventory.ConcreteItem;
 import inventory.Item;
 import inventory.SuperAccessory;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.StringTokenizer;
 import npc.NPC;
 import player.Player;
 import player.Projectile;
 import store.ItemStore;
 import store.StoreManagerNPC;
 import utils.JsonUtil;
 import utils.Location;
 import app.RPGame;
 import collisions.BoundaryCollision;
 import collisions.EnemyCollision;
 import collisions.ItemCollision;
 import collisions.NPCCollision;
 import collisions.PlayerProjectileCollision;
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
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import enemy.AbstractEnemy;
 import evented.Evented;
 
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
 
     protected RPGame game;
     private LevelInventory<Item> inventory;
     private ItemStore store;
     private StoreManagerNPC manager;
 
 
     public Level (BaseLoader bsLoader,
                   BaseIO bsIO,
                   RPGame game,
                   String levelname) {
         super(0, 0, TILE_WIDTH, TILE_HEIGHT);
 
         this.game = game;
         this.inventory = new LevelInventory<Item>(game);
         this.levelname = levelname;
         this.baseio = bsIO;
         this.bsloader = bsLoader;
 
         initResources();
     }
 
 
     public void initResources () {
         JsonObject level = JsonUtil.getJSON(levelname);
 
         setChipsets();
         setTiles(level);
         setSize(layer1.length, layer1[0].length);
         setLevelTimer();
 
         setPlayer(level);
         setNpcs(level);
         setItems(level);
         setEnemies(level);
         setStore(level);
 
         setCollisions();
     }
 
 
     private void setCollisions () {
         SceneryCollision sceneCol = new SceneryCollision();
         ItemCollision itCol = new ItemCollision();
         NPCCollision collision = new NPCCollision();
         EnemyCollision enCol = new EnemyCollision();
         BoundaryCollision boundCol = new BoundaryCollision(this);
         PlayerProjectileCollision pCol = new PlayerProjectileCollision();
 
         PlayField field = game.getField();
         SpriteGroup player = field.getGroup("player");
 
         game.getField().addCollisionGroup(player,
                                           field.getGroup("npcs"),
                                           collision);
         game.getField().addCollisionGroup(player,
                                           field.getGroup("items"),
                                           itCol);
         game.getField().addCollisionGroup(player,
                                           field.getGroup("scenery"),
                                           sceneCol);
         game.getField().addCollisionGroup(player,
                                           field.getGroup("enemies"),
                                           enCol);
         game.getField().addCollisionGroup(player, null, boundCol);
         game.getField().addCollisionGroup(field.getGroup("enemies"),
         		field.getGroup("projectiles"),
                pCol);
     }
 
 
     private void setChipsets () {
         chipsetE =
             new Chipset(bsloader.getImages("rsc/level/ChipSet2.png",
                                            6,
                                            24,
                                            false));
         chipsetF =
             new Chipset(bsloader.getImages("rsc/level/ChipSet3.png", 6, 24));
         chipsetG =
             new Chipset(bsloader.getImages("rsc/player/playerstart.png", 6, 24));
 
         chipset = new Chipset[16];
         BufferedImage[] image =
             bsloader.getImages("rsc/level/ChipSet1.png", 4, 4, false);
         int[] chipnum =
             new int[] { 0, 1, 4, 5, 8, 9, 11, 12, 2, 3, 6, 7, 10, 11, 14, 15 };
         for (int i = 0; i < chipset.length; i++) {
             int num = chipnum[i];
             BufferedImage[] chips = ImageUtil.splitImages(image[num], 3, 4);
             chipset[i] = new Chipset(chips);
         }
     }
 
 
     public LevelInventory<Item> getInventory () {
         return inventory;
     }
     
     public ItemStore getStore(){
     	return store;
     }
 
 
 	private void setLevelTimer() {
 		levelTimer.setFPS(100);
 		levelTimer.startTimer();
 		levelStartTime = levelTimer.getTime();
 	}
 
 	private void setPlayer(JsonObject level) {
 		JsonObject jPlayer = level.getAsJsonObject("player");
 		JsonArray jLocation = jPlayer.getAsJsonArray("location");
 		SpriteGroup group = new SpriteGroup("player");
 		int[] location = new int[] { jLocation.get(0).getAsInt(),
 				jLocation.get(1).getAsInt() };
 
 		Location playerLoc = new Location(location);
 		Player player = new Player(new GameCharacter(game, playerLoc, jPlayer
 				.get("directionsURL").getAsString()), jPlayer.get("actionsURL")
 				.getAsString());
 
 		JsonObject inventory = jPlayer.getAsJsonObject("playerInventory");
 		JsonArray items = inventory.getAsJsonArray("items");
 		for (int i = 0; i < items.size(); i++) {
 			JsonObject it = items.get(i).getAsJsonObject();
 			Item item = new ConcreteItem(game, it);
 			if (it.get("name").getAsString().contains("money")) {
 				item.setEquippable(false);
 				item.setDroppable(false);
 			}
 			item.setWrapper(player.getCharacter().getInventory());
 			player.getCharacter().getInventory().add(item, item.getQuantity());
 		}
 
 		game.setPlayer(player);
 		group.add(player.getCharacter());
 		
 		SpriteGroup projectiles = new SpriteGroup("projectiles");
 		game.getField().addGroup(projectiles);
 		
 		game.getField().addGroup(group);
 	}
     
     private void setStore(JsonObject level) {
 		JsonObject store = level.getAsJsonObject("store");
 		JsonArray storeItems = store.getAsJsonArray("storeItems");
 		SpriteGroup group = new SpriteGroup("storeItems");
 
 		for (int i = 0; i < storeItems.size(); i++) {
 			JsonObject it = storeItems.get(i).getAsJsonObject();
 			Item item = null;
 			item = new ConcreteItem(game, it);
 //			if (it.get("name").getAsString().contains("money")){
 //			    item.setEquippable(false);
 //			}
 			group.add(item);
 		}
 		game.getField().addGroup(group);
 	}
 
 	private void setItems(JsonObject level) {
 		JsonObject inventory = level.getAsJsonObject("inventory");
 		JsonArray items = inventory.getAsJsonArray("items");
 		SpriteGroup group = new SpriteGroup("items");
 
 		for (int i = 0; i < items.size(); i++) {
 			JsonObject it = items.get(i).getAsJsonObject();
 
 			if (it.get("name").getAsString().contains("super")) {
 				Item item = new SuperAccessory(game, it);
 				group.add(item);
 			} else {
 				Item item = new ConcreteItem(game, it);
 
 				if (it.get("name").getAsString().contains("money")) {
 					item.setEquippable(false);
 
 				}
 				group.add(item);
 			}
 		}
 		game.getField().addGroup(group);
 	}
 
 	private void setNpcs(JsonObject level) {
 		JsonArray npcs = level.getAsJsonArray("npcs");
 		SpriteGroup group = new SpriteGroup("npcs");
 
 		for (int i = 0; i < npcs.size(); i++) {
 			JsonObject jNPC = npcs.get(i).getAsJsonObject();
 			JsonArray jLocation = jNPC.get("location").getAsJsonArray();
 
 			Location loc = new Location(new int[] {
 					jLocation.get(0).getAsInt(), jLocation.get(1).getAsInt() });
 			String npcName = jNPC.get("name").getAsString();
 			JsonObject move = jNPC.getAsJsonObject("movement");
 			NPC npc = NPC.createNPC(npcName, new GameCharacter(game, loc, jNPC
 					.get("directions").getAsString()), jNPC
 					.getAsJsonObject("movement"));
 
 			group.add(npc.getCharacter());
 
 		}
 		game.getField().addGroup(group);
 	}
 
 	private void setEnemies(JsonObject level) {
 		JsonArray enemies = level.getAsJsonArray("enemies");
 		SpriteGroup group = new SpriteGroup("enemies");
 
 		for (int i = 0; i < enemies.size(); i++) {
 			JsonObject jEnemy = enemies.get(i).getAsJsonObject();
 			JsonArray jLocation = jEnemy.get("location").getAsJsonArray();
 
 			Location loc = new Location(new int[] {
 					jLocation.get(0).getAsInt(), jLocation.get(1).getAsInt() });
 			String enemyName = jEnemy.get("name").getAsString();
 
 			AbstractEnemy enemy = AbstractEnemy.createEnemy(enemyName, game,
 					new GameCharacter(game, loc, jEnemy.get("directions")
 							.getAsString()), jEnemy);
 			group.add(enemy.getCharacter());
 		}
 
 		game.getField().addGroup(group);
 	}
 
 	private void setTiles(JsonObject level) {
 		String[] lowerTile = FileUtil.fileRead(baseio.getStream(level.get(
 				"lowerFilename").getAsString()));
 		String[] upperTile = FileUtil.fileRead(baseio.getStream(level.get(
 				"upperFilename").getAsString()));
 
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
