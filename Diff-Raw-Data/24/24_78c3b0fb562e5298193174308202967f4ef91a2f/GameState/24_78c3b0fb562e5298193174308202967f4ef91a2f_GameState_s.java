 package com.inda.hacksmack.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.util.Rectangle;
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.tiled.TiledMap;
 
 import com.inda.hacksmack.HackSmackConstants;
 import com.inda.hacksmack.ResourceManager;
 import com.inda.hacksmack.factory.EnemyFactory;
 import com.inda.hacksmack.factory.ItemFactory;
 import com.inda.hacksmack.model.Item.ItemType;
 import com.inda.hacksmack.model.cutscene.CutScene;
 import com.inda.hacksmack.model.cutscene.IntroCutScene;
 
 /**
  * Contains the GameState - this can be momentarily dumped and loaded to save/retrieve gamestate
  * 
  * @author Jensa
  */
 public class GameState {
 
 	private GameMode gameMode = GameMode.GAMEPLAY;
 	private CutScene cutScene = new IntroCutScene();
 	private Map map;
 	private List<Enemy> enemies = new ArrayList<Enemy>();
 	private List<Item> items = new ArrayList<Item>();
 	private List<Projectile> projectiles = new ArrayList<Projectile>();
 	private Player player;
 	private HealthBar healthBar = new HealthBar();
 	private HeatAndBatteryBar heatAndBatteryBar = new HeatAndBatteryBar();
 
 	/**
 	 * Loads a default map
 	 */
 	public GameState() {
 		this("level_1");
 	}
 
 	public GameState(String mapName) {
 		setUpMap(mapName);	
 	}
 
 	public void setUpMap(String mapName) {
 		this.map = new Map(ResourceManager.getInstance().getTiledMap(mapName));
 		
 		//Load all enemies from tilefile:
 		TiledMap tileMap = map.getTileMap();
 		int objcnt = tileMap.getObjectCount(0);
 		for (int i = 0; i < objcnt; i++) {
 			String type = tileMap.getObjectProperty(0, i, "type", "");
 			String id = tileMap.getObjectProperty(0, i, "id", "");
 			if(type.equals("enemy")){
 				Enemy newEnemy = EnemyFactory.newEnemy(id, new Vector2f(tileMap.getObjectX(0, i), tileMap.getObjectY(0, i)));
 				enemies.add(newEnemy);
 			} else if(type.equals("gem")){
 				Item item = ItemFactory.newItemEntity(id, new Vector2f(tileMap.getObjectX(0, i), tileMap.getObjectY(0, i)), ItemType.GEM, true);
 				items.add(item);
 			}
 		}
		
		player = new Player();
		player.setGameState(this);
		player.setSpeed(200);
		player.setWeaponDamage(50);
		Image []frame = new Image[1];
		frame[0] = ResourceManager.getInstance().getImage("player");
		player.setAnimation(new Animation(frame, 1));
		player.setPosition(new Vector2f(200, 200));
 	}
 
 	public Map getMap() {
 		return map;
 	}
 
 	public void setMap(Map map) {
 		this.map = map;
 	}
 
 	public List<Enemy> getEnemies() {
 		return enemies;
 	}
 
 	public void setEnemies(List<Enemy> enemies) {
 		this.enemies = enemies;
 	}
 
 	public List<Item> getItems() {
 		return items;
 	}
 
 	public void setItems(List<Item> items) {
 		this.items = items;
 	}
 
 	public List<Projectile> getProjectiles() {
 		return projectiles;
 	}
 
 	public void setProjectiles(List<Projectile> projectiles) {
 		this.projectiles = projectiles;
 	}
 
 	public Player getPlayer() {
 		return player;
 	}
 
 	public void setPlayer(Player player) {
 		this.player = player;
 	}
 
 	public GameMode getGameMode() {
 		return gameMode;
 	}
 
 	public void setGameMode(GameMode gameMode) {
 		this.gameMode = gameMode;
 	}
 
 	public CutScene getCutScene() {
 		return cutScene;
 	}
 
 	public void setCutScene(CutScene cutScene) {
 		this.cutScene = cutScene;
 	}
 
 	public HealthBar getHealthBar() {
 		return healthBar;
 	}
 
 	public void setHealthBar(HealthBar healthBar) {
 		this.healthBar = healthBar;
 	}
 
 	public HeatAndBatteryBar getHeatAndBatteryBar() {
 		return heatAndBatteryBar;
 	}
 
 	public void setHeatAndBatteryBar(HeatAndBatteryBar heatAndBatteryBar) {
 		this.heatAndBatteryBar = heatAndBatteryBar;
 	}
 
 	public void drawFogOfWar(Graphics graphics) {
 		graphics.setColor(Color.black);
 		int tileSize = HackSmackConstants.TILE_SIZE;
 		for (int i = 0; i < HackSmackConstants.SCREEN_WIDTH; i += tileSize) {
 			for (int j = 0; j < HackSmackConstants.SCREEN_HEIGHT; j += tileSize) {
 				
 				float xdiff = player.getPosition().x - i;
 				float ydiff = player.getPosition().y - j;
 				if(xdiff*xdiff + ydiff*ydiff > HackSmackConstants.FOG_OF_WAR_DISTANCE * HackSmackConstants.FOG_OF_WAR_DISTANCE ){
 					graphics.fillRect(i, j, tileSize, tileSize);
 				}
 			}
 		}
 	}
 
 	public boolean getHasGemsLeft() {
 		int cnt = 0;
 		
 		for (int i = 0; i < items.size(); i++) {
 			Item item = items.get(i);
 			if(item.getType().equals(ItemType.GEM)){
 				cnt ++;
 			}
 		}
		return cnt > 0;
 	}
 }
