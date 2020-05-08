 package uk.co.samatkins.dungeon.play;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import uk.co.samatkins.Entity;
 import uk.co.samatkins.FileRNG;
 import uk.co.samatkins.Tilemap;
 import uk.co.samatkins.dungeon.data.AssetManager;
 import uk.co.samatkins.dungeon.play.dungeon.Room;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.ParticleEffect;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 public class Dungeon extends Entity {
 	
 	public static final int TILE_WIDTH = 16;
 	public static final int TILE_HEIGHT = 16;
 	
 	private Tilemap tilemap;
 	
 	private ParticleEffect particles;
 	
 	private AssetManager assets;
 	private FileRNG random;
 	
 	private Room startRoom;
 	
 	public Player player;
 	private boolean isPlayersTurn;
 	
 	private List<DungeonEntity> entities;
 	private DungeonEntity currentEntity;
 	private int currentEntityIndex = 0;
 
 	public Dungeon(int width, int height) {
 		super();
 		
 		this.isPlayersTurn = true;
 		
 		this.assets = AssetManager.getInstance();
 		this.sprite = this.tilemap = new Tilemap(this.assets.getTilesTexture(), TILE_WIDTH, TILE_HEIGHT, width, height);
 		this.tilemap.setTileRect(0, width-1, 0, height-1, -1); // Set all to void
 		
 		this.particles = this.assets.getParticleAttacked();
 	}
 	
 	public void buildDungeon(String filename) {
 		this.tilemap.clear();
 		this.entities = new ArrayList<DungeonEntity>();
 		
 		this.random = new FileRNG(Gdx.files.internal(filename).read());
 		
 		// Create starting lists
 		int gridSize = 6,
 			gridCount = gridSize * gridSize;
 		int[] graph = new int[gridCount];
 			// graph[node] will contain its connections OR'd together 1=up, 2=right, 4=down, 8=left
 		ArrayList<Integer> connected = new ArrayList<Integer>(gridCount);
 		ArrayList<Integer> unconnected = new ArrayList<Integer>(gridCount);
 		for (int i=0; i<gridCount; i++) {
 			unconnected.add(i);
 		}
 		
 		int start = random.getIntBetween(0, unconnected.size()-1);
 		unconnected.remove((Object)start);
 		connected.add(start);
 		System.out.println("First node: " + start);
 		
 		// Repeatedly pick a node that's connected, and connect it to one of its neighbours
 		// until all are connected.
 		int a,b=0,direction;
 		while (unconnected.size() > 0) {
 			a = connected.get(random.getIntBetween(0, connected.size()-1));
 			
 			// If a fully connected, skip it
 			if (graph[a] == 15) {
 //				System.out.println("Skipping " + a);
 				continue;
 			}
 			
 			// b must neighbour a
 			direction = random.getIntBetween(0, 3);
 			switch (direction) {
 			case 0: // Up
 				if (a < gridSize) {
 					continue;
 				} else {
 					b = a - gridSize;
 					graph[a] = graph[a] | 1;
 					graph[b] = graph[b] | 4;
 				}
 				break;
 			case 1: // Right
 				if (a % gridSize == (gridSize-1)) {
 					continue;
 				} else {
 					b = a + 1;
 					graph[a] = graph[a] | 2;
 					graph[b] = graph[b] | 8;
 				}
 				break;
 			case 2: // Down
 				if (a >= (gridCount - gridSize)) {
 					continue;
 				} else {
 					b = a + gridSize;
 					graph[a] = graph[a] | 4;
 					graph[b] = graph[b] | 1;
 				}
 				break;
 			case 3: // Left
 				if (a % gridSize == 0) {
 					continue;
 				} else {
 					b = a - 1;
 					graph[a] = graph[a] | 8;
 					graph[b] = graph[b] | 2;
 				}
 				break;
 			}
 			
 			connected.add(b);
 			unconnected.remove((Object)b);
 			
 //			System.out.println("Connecting " + a + " to " + b);
 		}
 		
 		// Calculate size of 'chunks' that nodes fit into
 		int chunkWidth = (int)Math.floor(tilemap.getTilesAcross() / gridSize),
 			chunkHeight = (int)Math.floor(tilemap.getTilesDown() / gridSize);
 	
 		System.out.println("Chunks: " + chunkWidth + "x" + chunkHeight);
 		
 		int l,t,w,h;
 		Room[] rooms = new Room[gridCount];
 		for (int x=0; x<gridSize; x++) {
 			for (int y=0; y<gridSize; y++) {
 				l = (x * chunkWidth) + random.getIntBetween(0, chunkWidth/2);
 				t = (y * chunkHeight) + random.getIntBetween(0, chunkHeight/2);
 				w = random.getIntBetween(5, chunkWidth-(l % chunkWidth)-1);
 				h = random.getIntBetween(5, chunkHeight-(t % chunkHeight)-1);
 				
 				rooms[x + (y*gridSize)] = new Room(l, t, w, h);
 				this.buildRoom(l, t, w, h);
 			}
 		}
 		
 		this.startRoom = rooms[start];
 		
 		// For each room, connect it!
 		for (int i=0; i<gridCount; i++) {
 			// Only construct corridors right and down, so as to avoid duplicates
 			if ((graph[i] & 2) == 2) { // Connects right
 				// Create corridor to the right, to the right
 				this.connectRooms(rooms[i], rooms[i+1]);
 			}
 			if ((graph[i] & 4) == 4) { // Connects down
 				// Create corridor below
 				this.connectRooms(rooms[i], rooms[i+gridSize]);
 			}
 		}
 
 		// Get wall tiles to figure-out how they should look
 		for (int x=0; x<this.tilemap.getTilesAcross(); x++) {
 			for (int y=0; y<this.tilemap.getTilesDown(); y++) {
 				if (this.tilemap.getSolid(x, y)) {
 					int wallTile = 0
 							| (this.tilemap.getSolid(x, y+1) ? 1 : 0)
 							| (this.tilemap.getSolid(x+1, y) ? 2 : 0)
 							| (this.tilemap.getSolid(x, y-1) ? 4 : 0)
 							| (this.tilemap.getSolid(x-1, y) ? 8 : 0);
 					this.tilemap.setTile(x, y, wallTile);
 				}
 			}
 		}
 		
 		// Generate player
 		this.player = new Player(this, this.startRoom.getCentreX(), this.startRoom.getCentreY());
 		
 		// Place some enemies!
 		for (Room room : rooms) {
 			if (room == this.startRoom) { continue; }
 			
 			this.placeEnemy( room.getCentreX(), room.getCentreY() );
 		}
 //		this.placeEnemy( this.player.tileX+1, this.player.tileY );
 	}
 	
 	public boolean isTileSolid(int x, int y) {
 		return this.tilemap.getSolid(x, y);
 	}
 	
 	private void placeEnemy(int x, int y) {
 		Enemy e = Enemy.create(
 				this.assets.getEnemyData(this.random.getIntBetween(0, this.assets.getEnemyTypeCount()-1)),
 				this, x, y);
 		this.entities.add(e);
 		this.world.add(e);
 	}
 	
 	private void placeWall(int x, int y) {
 		if (tilemap.getTile(x, y) < 0) { // Only place if there's no existing tile
 			tilemap.setTile(x, y, 0);
 			tilemap.setSolid(x, y, true);
 		}
 	}
 	
 	private void placeFloor(int x, int y) {
 		tilemap.setTile(x, y, 16);
 		tilemap.setSolid(x, y, false);
 	}
 	
 	private void buildRoom(int left, int top, int width, int height) {
 		
 		int right = left + width,
 			bottom = top + height;
 		
 		// Top and bottom
 		for (int x=left; x <= right; x++) {
 			this.placeWall(x, top);
 			this.placeWall(x, bottom);
 		}
 		
 		// Left and right
 		for (int y=top; y <= bottom; y++) {
 			this.placeWall(left, y);
 			this.placeWall(right, y);
 		}
 		
 		// Floor!
 		for (int x=left + 1; x < right; x++) {
 			for (int y=top+1; y<bottom; y++) {
 				this.placeFloor(x, y);
 			}
 		}
 	}
 	
 	private void buildHorizontalCorridor(int x1, int x2, int y) {
 		if (x1 > x2) {
 			this.buildHorizontalCorridor(x2, x1, y);
 			return;
 		}
 		
 		for (int x=x1; x<=x2; x++) {
 			this.placeWall(x, y-1);
 			this.placeFloor(x, y);
 			this.placeWall(x, y+1);
 		}
 	}
 	
 	private void buildVerticalCorridor(int x, int y1, int y2) {
 		if (y1 > y2) {
 			this.buildVerticalCorridor(x, y2, y1);
 			return;
 		}
 		
 		for (int y=y1; y<=y2; y++) {
 			this.placeWall(x-1, y);
 			this.placeFloor(x, y);
 			this.placeWall(x+1, y);
 		}
 	}
 	
 	private void connectRooms(Room a, Room b) {
 		
 		// Decide whether to go h or v first.
 		if (random.getBoolean()) {
 			this.buildHorizontalCorridor(a.getRight()-1, b.getCentreX(), a.getCentreY());
 			this.buildVerticalCorridor(b.getCentreX(), a.getCentreY(), b.getBottom()+1);
 			
 			// Add missing corner pieces
 			this.placeWall(b.getCentreX() + 1, a.getCentreY() - 1);
 			this.placeWall(b.getCentreX() + 1, a.getCentreY() + 1);
 			this.placeWall(b.getCentreX() - 1, a.getCentreY() - 1);
 			this.placeWall(b.getCentreX() - 1, a.getCentreY() + 1);
 		} else {
 			this.buildVerticalCorridor(a.getCentreX(), a.getTop()-1, b.getCentreY());
 			this.buildHorizontalCorridor(a.getCentreX(), b.getLeft()+1, b.getCentreY());
 			
 			// Add missing corner pieces
 			this.placeWall(a.getCentreX()+1, b.getCentreY() - 1);
 			this.placeWall(a.getCentreX()+1, b.getCentreY() + 1);
 			this.placeWall(a.getCentreX()-1, b.getCentreY() - 1);
 			this.placeWall(a.getCentreX()-1, b.getCentreY() + 1);
 		}
 		
 	}
 	
 	/**
 	 * Get the room the player will start in
 	 * @return
 	 */
 	public Room getStartRoom() {
 		return this.startRoom;
 	}
 
 	public boolean isPlayersTurn() {
 		return this.isPlayersTurn;
 	}
 
 	public void endPlayerTurn() {
 		this.isPlayersTurn = false;
 		if (this.entities.size() > 0) {
 			this.currentEntity = this.entities.get(0);
 			this.currentEntityIndex = 0;
 		}
 	}
 	
 	@Override
 	public void act(float delta) {
 		super.act(delta);
 		
 		this.particles.update(delta);
 		
 		this.handleTurns();
 	}
 	
 	@Override
 	public void draw(SpriteBatch batch, float parentAlpha) {
 		super.draw(batch, parentAlpha);
 		this.particles.draw(batch);
 	}
 	
 	private void handleTurns() {
 		// Is it the player's turn?
 		if (this.isPlayersTurn) {
 			return;
 		}
 		
 		// Is current entity animating?
 		if ( this.player.isAnimating() || (this.currentEntity == null) || this.currentEntity.isAnimating()) {
 			return;
 		} //else {
 			//this.currentEntity = this.entities.get(this.currentEntityIndex);
 		//}
 
 		//this.currentEntity.takeTurn();
 		
 //		// Are there entities waiting to move?
 //		if (this.currentEntityIndex != this.entities.size()-1) {
 //			this.currentEntityIndex++;
 //		} else {
 //			System.out.println("All entities have moved");
 //			this.isPlayersTurn = true;
 //		}
 		
 		for (DungeonEntity e: this.entities) {
 			e.takeTurn();
 		}
 		this.isPlayersTurn = true;
 	}
 	
 	/**
 	 * Returns the DungeonEntity at the given tile coordinate, or null if it is empty.
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	public DungeonEntity getEntityAt(int x, int y) {
 		if ( (this.player.tileX == x) && (this.player.tileY == y) ) {
 			return this.player;
 		}
 		
 		// For now, should be few enough entities that iterating through all of them is acceptable
 		for (DungeonEntity e: this.entities) {
 			if ( (e.tileX == x) && (e.tileY == y) ) {
 				return e;
 			}
 		}
 		
 		return null;
 	}
 	
 	public void removeEntity(DungeonEntity e) {
 		this.entities.remove(e);
 		e.remove();
 	}
 	
 	public void playParticleEffect(String name, int tileX, int tileY) {
 		if (name.equals("attacked")) {
			this.particles.setPosition((tileX + 0.5f) * Dungeon.TILE_WIDTH, (tileY + 0.5f) * Dungeon.TILE_HEIGHT);
 			this.particles.start();
 		}
 	}
 }
