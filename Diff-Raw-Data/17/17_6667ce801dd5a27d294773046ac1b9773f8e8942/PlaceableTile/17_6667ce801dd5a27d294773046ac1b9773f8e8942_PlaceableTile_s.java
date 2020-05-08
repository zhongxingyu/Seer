 package se.chalmers.tda367.std.core;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 import se.chalmers.tda367.std.core.tiles.*;
 
 /**
  * Represents the choices of placeable tiles in the MapEditor.
  * @author Emil Edholm
  * @date   May 12, 2012
  */
 public enum PlaceableTile {
 	TERRAIN_TILE("Terrain tile") {
 		@Override
 		public IBoardTile getInstance() { return terrainTile; }
 	},
 	BUILDABLE_TILE("Buildable tile") {
 		@Override
 		public IBoardTile getInstance() { return buildableTile; }
 	},
 	PATH_TILE("Path tile") {
 		@Override
 		public IBoardTile getInstance() { return walkableTile; }
 	},
 	PLAYER_BASE_TILE("PlayerBase tile") {
 		@Override
		public IBoardTile getInstance() { return playerBaseTile; }
 	},
 	ENEMY_START_TILE("Enemy start position") { 
 		@Override
 		public IBoardTile getInstance() { return walkableTile; }
 	},
 	WAYPOINT("Waypoint") {
 		@Override
 		public IBoardTile getInstance() { return walkableTile; }
 	};
 	
 	private final transient static IBuildableTile buildableTile = getInstance(IBuildableTile.class);
	private final transient static IWalkableTile walkableTile   = getInstance(IWalkableTile.class);
	private final transient static IPlayerBase playerBaseTile   = getInstance(IPlayerBase.class);
	private final transient static IBoardTile terrainTile       = getInstance(IBoardTile.class);
 	
 	private final String fancyName;
 	private PlaceableTile(String fancyName) {
 		this.fancyName = fancyName;
 	}
 	
 	/** Returns an instance of the tile represented by the {@code PlaceableTile}. */
 	public abstract IBoardTile getInstance();
 	
 	@Override
 	public String toString() {
 		return fancyName;
 	}
 	
 	/** Used to get an instance from Guice */
 	private static <T> T getInstance(Class<T> type) {
 		Injector injector = Guice.createInjector();
 		return injector.getInstance(type);
 	}
 }
