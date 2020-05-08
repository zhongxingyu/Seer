 package com.punchline.NinjaSpacePirate.gameplay.entities.systems.spawn;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import com.badlogic.gdx.math.Vector2;
 import com.punchline.NinjaSpacePirate.gameplay.StealthWorld;
 import com.punchline.javalib.entities.Entity;
 import com.punchline.javalib.entities.components.physical.Transform;
 import com.punchline.javalib.entities.systems.EntitySystem;
 
 /**
  * System that spawns the tiles making up the game world.
  * @author Natman64
  *
  */
 public class TileSpawnSystem extends EntitySystem {
 	
 	//region Inner Classes
 	
 	/** Contains the information needed to construct a tile, minus its position. */
 	private class TileArgs {
 		/** The key of the sprite's TextureRegion in the game's SpriteSheet. */
 		public String spriteKey;
 		
 		/** Whether this tile is a physical obstacle. */
 		public boolean blocked;
 		
 		/** Constructs TileArgs.
 		 * @param spriteKey The key of the tile's sprite.
 		 * @param blocked Whether this tile is a physical obstacle. */
 		public TileArgs(String spriteKey, boolean blocked) {
 			this.spriteKey = spriteKey;
 			this.blocked = blocked;
 		}
 	}
 	
 	/** Contains {@link TileArgs} for an entire row of tiles of size {@link #ROW_SIZE}. */
 	private class TileRow {
 		/** The size of a row of tiles. */
 		public static final int ROW_SIZE = 7;
 		
 		/** The TileArgs for creating this row of tiles. */
 		public TileArgs[] tiles = new TileArgs[ROW_SIZE];
 		
 		/** Constructs a TileRow.
 		 * @param tiles The TileArgs array for creating this row of tiles. Must have size {@link #ROW_SIZE}. */
 		public TileRow(TileArgs... tiles) {
 			if (tiles.length != ROW_SIZE) {
 				throw new IllegalArgumentException("Wrong number of tiles passed to TileRow constructor.");
 			}
 			
			this.tiles = tiles;
 		}
 	}
 	
 	//endregion
 
 	//region Constants
 	
 	private static final float SPAWN_DISTANCE = 12f;
 	
 	//endregion
 	
 	//region Fields
 	
 	private int y = StealthWorld.TILE_SPAWN_Y;
 	private LinkedList<String> rowsToSpawn = new LinkedList<String>();
 	private HashMap<String, TileRow> rowTemplates = new HashMap<String, TileRow>();
 	private HashMap<String, String[]> locationTemplates = new HashMap<String, String[]>();
 	
 	private Entity player;
 	
 	//endregion
 	
 	//region Initialization
 	
 	/**
 	 * Constructs and initializes. a TileSpawnSystem.
 	 */
 	public TileSpawnSystem() {
 		buildRowTemplates();
 		buildLocationTemplates();
 	}
 	
 	private void buildRowTemplates() {
 		TileArgs floor = new TileArgs("Floor", false);
 		TileArgs floorVent = new TileArgs("FloorVent", false);
 		TileArgs floorGrate = new TileArgs("FloorGrate", false);
 		TileArgs floorLight = new TileArgs("FloorLight", false);
 		
 		TileArgs whiteWallVertical = new TileArgs("WhiteWallVertical", true);
 		TileArgs whiteWallVentEast = new TileArgs("WhiteWallVentEast", true);
 		TileArgs whiteWallVentWest = new TileArgs("WhiteWallVentWest", true);
 		TileArgs whiteWallRedLightEast = new TileArgs("WhiteWallRedLightEast", true);
 		TileArgs whiteWallRedLightWest = new TileArgs("WhiteWallRedLightWest", true);
 		
 		TileArgs[] args = new TileArgs[TileRow.ROW_SIZE];
		TileRow row = null;
 		
 		args[0] = whiteWallVertical;
 		args[1] = floorLight;
 		args[2] = floor;
 		args[3] = floor;
 		args[4] = floor;
 		args[5] = floorLight;
 		args[6] = whiteWallVertical;
		row = new TileRow(args);
 		
		rowTemplates.put("HallSegment", row);
 		
 		args[0] = whiteWallVentEast;
 		args[6] = whiteWallVentWest;
		row = new TileRow(args);
 		
		rowTemplates.put("HallSegmentWallVents", row);
 		
 		args[0] = whiteWallVertical;
 		args[3] = floorVent;
 		args[6] = whiteWallVertical;
		row = new TileRow(args);
 		
		rowTemplates.put("HallSegmentFloorVent", row);
 	}
 	
 	private void buildLocationTemplates() {
 		String[] loc = new String[16];
 		loc[0] = "HallSegment";
 		loc[1] = "HallSegment";
 		loc[2] = "HallSegment";
 		loc[3] = "HallSegmentWallVents";
 		loc[4] = "HallSegment";
 		loc[5] = "HallSegment";
 		loc[6] = "HallSegment";
 		loc[7] = "HallSegmentFloorVent";
 		loc[8] = "HallSegment";
 		loc[9] = "HallSegment";
 		loc[10] = "HallSegment";
 		loc[11] = "HallSegmentWallVents";
 		loc[12] = "HallSegment";
 		loc[13] = "HallSegment";
 		loc[14] = "HallSegment";
 		
 		locationTemplates.put("HallSegment", loc);
 		
 	}
 	
 	//endregion
 	
 	@Override
 	public void dispose() { }
 
 	//region Processing
 	
 	@Override
 	public void processEntities() {
 		super.processEntities();
 		
 		queueSpawnLocation("HallSegment"); //demo code
 		
 		Transform t = player.getComponent(Transform.class);
 		
 		while (t.getPosition().y + SPAWN_DISTANCE >= y) {
 			spawnRow(rowsToSpawn.removeFirst());
 		}
 	}
 	
 	//endregion
 	
 	//region Entity Processing
 	
 	@Override
 	protected void process(Entity e) { 
 		this.player = e;
 	}
 
 	@Override
 	public boolean canProcess(Entity e) { 
 		return e.getTag().equals("Player");
 	}
 	
 	//endregion
 
 	//region Helpers
 	
 	private void spawnRow(String rowKey) {
 		TileRow row = rowTemplates.get(rowKey);
 		
 		int i = 0;
 		for (int x = -TileRow.ROW_SIZE / 2; x <= TileRow.ROW_SIZE / 2; x++, i++) {
 			TileArgs args = row.tiles[i];
 			
 			world.createEntity("Tile", args.spriteKey, new Vector2(x, y), args.blocked);
 		}
 		
 		y++;
 	}
 	
 	private void queueSpawnRow(String rowKey) {
 		rowsToSpawn.add(rowKey);
 	}
 	
 	private void queueSpawnLocation(String locKey) {
 		String[] loc = locationTemplates.get(locKey);
 		
 		for (String row : loc) {
 			queueSpawnRow(row);
 		}
 	}
 	
 	//endregion
 	
 }
