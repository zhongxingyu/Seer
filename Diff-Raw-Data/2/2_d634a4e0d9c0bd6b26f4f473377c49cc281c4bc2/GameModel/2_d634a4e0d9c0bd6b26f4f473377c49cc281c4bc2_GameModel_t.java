 package io.github.ldears.ld26.models;
 
 import io.github.ldears.ld26.events.InputEventHandler;
 import io.github.ldears.ld26.map.*;
 import io.github.ldears.ld26.render.Renderer;
 
 import java.awt.Point;
 import java.util.*;
 
 /**
  * @author dector
  * @author wizzardich
  */
 public class GameModel implements InputEventHandler {
 	private Tile[][] data;
 	private Player player;
 	private final int TILE_SIZE = Renderer.TILE_SIZE;
 	private byte direction;
 	/**
 	 * Time passed since player position was last updated
 	 */
 	private float time;
 	private int ind = 0;
 
 	private class Player {
 		Point pos;
 		Container inventory;
 		float accel = 3*TILE_SIZE;
 		float stopAccel = 5*TILE_SIZE;
 		float velocity = 3*TILE_SIZE;
 	}
 
 	public GameModel() {
 		player = new Player();
 		player.inventory = new Container(0, 0, "inventory", 1, 0, 0, true);
 	}
 
 	public void setPlayerSpawn(int tiledX, int tiledY) {
 		player.pos = new Point(tiledX * TILE_SIZE + TILE_SIZE / 2
 				- TileType.PLAYER_WIDTH / 2, tiledY * TILE_SIZE);
 	}
 
 	public void init(Tile[][] tiles) {
 		data = tiles;
 	}
 
 	public void update(float dt) {
 		time += dt;
 		if ((direction & 3) != 0) player.velocity = Math.min(player.velocity + player.accel*dt, 5*TILE_SIZE);
 		else player.velocity = Math.max((player.velocity - player.stopAccel*dt), 3*TILE_SIZE);
 		// System.out.println(getAvailableAction());
 		int tiledX = player.pos.x / TILE_SIZE;
 		int tiledY = player.pos.y / TILE_SIZE;
 		int tiledRX = (player.pos.x + TileType.PLAYER_WIDTH) / TILE_SIZE;
 		int dist = (int) (time * player.velocity);
 		if (dist > 0) {
 			time = 0;
 			int playerX = player.pos.x;
 			int playerRX = playerX + TileType.PLAYER_WIDTH;
 			if ((direction & 2) != 0) {
 				if (((playerRX + dist) / TILE_SIZE) > tiledX)
 					if (data[tiledX + 1][tiledY].type == TileType.WALL_RIGHT)
 						return;
 				if (data[tiledRX][tiledY].type == TileType.WALL_MD)
 					if (playerRX + dist > tiledRX * TILE_SIZE
 							+ (TILE_SIZE - TileType.WALL_MIDDLE_WIDTH) / 2)
 						return;
 				player.pos.move(playerX + dist, player.pos.y);
 
 			}
 			if ((direction & 1) != 0) {
 				if (((playerX - dist) / TILE_SIZE) < tiledX)
 					if (data[tiledX - 1][tiledY].type == TileType.WALL_LEFT)
 						return;
 				if (data[tiledX][tiledY].type == TileType.WALL_MD)
 					if (playerX - dist < tiledX * TILE_SIZE
 							+ (TILE_SIZE + TileType.WALL_MIDDLE_WIDTH) / 2)
 						return;
 				player.pos.move(player.pos.x - dist, player.pos.y);
 			}
 		}
 	}
 
 	public Point getPlayerPosition() {
 		return player.pos;
 	}
 
 	public Tile[][] getTileMap() {
 		return data;
 	}
 
 	public int getTileMapWidth() {
 		return data.length;
 	}
 
 	public int getTileMapHeight() {
 		return data[0].length;
 	}
 
 	public Action getAvailableAction() {
 		GameObject c = getCurrentObject();
 		if (c != null) {
 			return c.getAction(player.inventory.isFull());
 		} else {
 			return Action.NONE;
 		}
 	}
 
 	public GameObject getCurrentObject() {
 		int tiledX = player.pos.x / TILE_SIZE;
 		int tiledY = player.pos.y / TILE_SIZE;
 		int tiledRX = (player.pos.x + TileType.PLAYER_WIDTH) / TILE_SIZE;
 		if (tiledRX * TILE_SIZE - player.pos.x < player.pos.x
 				+ TileType.PLAYER_WIDTH - tiledRX * TILE_SIZE)
 			tiledX = tiledRX;
 		Tile ofInterest = data[tiledX][tiledY];
 		Tile ofInterest2 = data[tiledX][tiledY + 1];
 		if ((!ofInterest.hasContainer())
 				&& (ofInterest2.hasContainer()))
 			return ofInterest2.getContent();
 		return ofInterest.getContent();
 	}
 
 	public PlayerDirection getPlayerDirection() {
 		if (((direction & 3) == 3) || ((direction & 3) == 0))
 			return PlayerDirection.STAND;
 		else if ((direction & 2) != 0)
 			return PlayerDirection.RIGHT;
 		else
 			return PlayerDirection.LEFT;
 	}
 
 	public Item removeInventoryItem() {
 		return player.inventory.poke();
 	}
 
 	public Item getInventoryItem() {
 		return player.inventory.get(0);
 	}
 
 	@Override
 	public void handleEvent(InputEvent event) {
 		switch (event) {
 		case LEFT_DOWN:
 			direction |= 1;
 			break;
 		case LEFT_UP:
 			direction ^= 1;
 			break;
 		case RIGHT_DOWN:
 			direction |= 2;
 			break;
 		case RIGHT_UP:
 			direction ^= 2;
 			break;
 		case X:
 			Action action = getAvailableAction();
 			exec(action);
 			ind = 0;
 			break;
 		case Z:
 //			System.out.println(ind);
 			GameObject go = getCurrentObject();
 			if (go.getClass() == Container.class) {
 				Container c = (Container) go;
 				if (!c.isEmpty()) {
 						ind += 1;
 						ind %= getContainerContents().size();
 					}
 				}
 //			System.out.println(ind);
 			break;
 		}
 
 //		System.out.println("GameModel: " + event + " received");
 	}
 
 	private void exec(Action action) {
 		switch (action) {
 		case USE_DOOR:
 			Door d = (Door) getCurrentObject();
 			int tiledX = d.getPairedDoor().coordinates().x;
 			int tiledY = d.getPairedDoor().coordinates().y;
 			setPlayerSpawn(tiledX, tiledY);
 			break;
 		case CLOSE_WINDOW:
 		case OPEN_WINDOW:
 			break;
 		case DROP_ITEM:
 			Container c = (Container) getCurrentObject();
 			c.add(player.inventory.poke());
 			break;
 		case GET_ITEM:
 			Container c1 = (Container) getCurrentObject();
 			player.inventory.add(c1.remove(c1.get(ind)));
 			break;
 		case CALL_PHONE:
 			// Check win condition
 			Map<ItemType, Point> itemsMap = new HashMap<ItemType, Point>();
 
 			for (int i = 0; i < data.length; i++) {
 				for (int j = 0; j < data[0].length; j++) {
 					GameObject go = data[i][j].getContent();
 
 					if (go != null && go.type == ObjectType.CONTAINER) {
 						for (Item item : ((Container) go).getContents()) {
 							itemsMap.put(item.itemType, new Point(i, j));
 						}
 					}
 				}
 			}
 
 			Iterator<ItemType> iter = winCond.keySet().iterator();
 			boolean failed = false;
 			winConditionsOk = true;
 
 			while (! failed && iter.hasNext()) {
 				ItemType type = iter.next();
 
 				Point pos = itemsMap.get(type);
 
 				boolean found = false;
 				for (Point sp : winCond.get(type)) {
					if (sp != null && pos != null) found |= (sp.x == pos.x && sp.y == pos.y);
 				}
 
 				if (! found) failed = true;
 
 				/*if (failed) {
 					System.out.println(type.name() + " failed");
 					System.out.println("At pos: " + pos);
 				}*/
 			}
 
 			if (failed) winConditionsOk = false;
 			break;
 		case NONE:
 			break;
 		}
 	}
 
 	private Map<ItemType, List<Point>> winCond;
 	private boolean winConditionsOk;
 
 	public void setWinConditions(Map<ItemType, List<Point>> map) {
 		winCond = map;
 	}
 
 	public boolean isWinConditionsOk() {
 		return winConditionsOk;
 	}
 
 	public List<Item> getContainerContents() {
 		GameObject current = getCurrentObject();
 		if ((current == null) || !(current.getClass() == Container.class)) return new LinkedList<Item>();
 		Container c = (Container)current;
 		return c.getContents();
 	}
 	
 	public int getSelectedIndex() {
 		return ind;
 	}
 }
