 package cz.emo4d.zen.gameplay;
 
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.Pool;
 
 import cz.emo4d.zen.screens.Map;
 
 public class Entity {
 
 	public enum Direction {
 		S, SW, W, NW, N, NE, E, SE
 	}
 
 	protected static Pool<Rectangle> rectPool = new Pool<Rectangle>() {
 		@Override
 		protected Rectangle newObject() {
 			return new Rectangle();
 		}
 	};
 
 	protected static Array<Rectangle> tiles = new Array<Rectangle>();
 
 	protected Map currentMap;
 
 	public Vector2 position;
 	public Vector2 velocity;
 	public float WIDTH;
 	public float HEIGHT;
 
 
 	public Entity() {
 		position = new Vector2();
 		velocity = new Vector2();
 	}
 
 	public void setMap(Map map) {
 		this.currentMap = map;
 	}
 
 	public Map getMap() {
 		return currentMap;
 	}
 
 
 	public static int getDirectionNumber(Direction dir) {
 		switch (dir) {
 			case S:
 				return 0;
 			case SW:
 				return 1;
 			case W:
 				return 2;
 			case NW:
 				return 3;
 			case N:
 				return 4;
 			case NE:
 				return 5;
 			case E:
 				return 6;
 			case SE:
 				return 7;
 		}
 		return -1;
 	}
 
 	public Entity collision() {
 		// perform basic collision detection
 
 		Rectangle playerRect = rectPool.obtain();
 		playerRect.set(this.position.x, this.position.y, this.WIDTH, this.HEIGHT);
 
 		getTiles((int) this.position.x, (int) this.position.y, (int) (this.position.x + this.WIDTH), (int) (this.position.y + this.HEIGHT), tiles);
 
 		for (Rectangle tile : tiles) {
 			if (playerRect.overlaps(tile)) {
 				//this.velocity.x = 0;
 				rectPool.free(playerRect);
 				return new Entity();
 			}
 		}
 
 		rectPool.free(playerRect);
 		return null;
 	}
 
 	protected void getTiles(int startX, int startY, int endX, int endY, Array<Rectangle> tiles) {
		TiledMapTileLayer layer = (TiledMapTileLayer) currentMap.map.getLayers().getLayer(1);
 		rectPool.freeAll(tiles);
 		tiles.clear();
 		for (int y = startY; y <= endY; y++) {
 			for (int x = startX; x <= endX; x++) {
 				Cell cell = layer.getCell(x, y);
 				if (cell != null) {
 					Rectangle rect = rectPool.obtain();
 					rect.set(x, y, 1, 1);
 					tiles.add(rect);
 				}
 			}
 		}
 	}
 
 }
