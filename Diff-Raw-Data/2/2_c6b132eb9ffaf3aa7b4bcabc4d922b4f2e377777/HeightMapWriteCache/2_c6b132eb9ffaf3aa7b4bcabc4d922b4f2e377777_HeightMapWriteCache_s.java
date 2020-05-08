 package net.nabaal.majiir.realtimerender.rendering;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import net.nabaal.majiir.realtimerender.Coordinate;
 import net.nabaal.majiir.realtimerender.image.WriteCache;
 
 public class HeightMapWriteCache extends HeightMapProvider implements WriteCache {
 
 	private final ConcurrentMap<Coordinate, HeightMapTile> tiles = new ConcurrentHashMap<Coordinate, HeightMapTile>();
 	private final HeightMap source;
 	
 	public HeightMapWriteCache(HeightMap source, int size) {
 		super(size);
 		this.source = source;
 	}
 	
 	@Override
 	public void commit() {
 		for (Map.Entry<Coordinate, HeightMapTile> entry : tiles.entrySet()) {
 			if (source instanceof HeightMapProvider) {
 				HeightMapProvider provider = (HeightMapProvider) source;
 				if (provider.getSize() == this.getSize()) {
 					provider.setHeightMapTile(entry.getKey(), entry.getValue());
					return;
 				}
 			}
 			
 			// TODO: Cleaner
 			for (int x = 0; x < (1 << getSize()); x++) {
 				for (int y = 0; y < (1 << getSize()); y++) {
 					Coordinate block = entry.getKey().zoomIn(getSize()).plus(new Coordinate(x, y, Coordinate.LEVEL_BLOCK));
 					source.setHeight(block, entry.getValue().getHeight(block));
 				}
 			}
 		}
 		tiles.clear();
 	}
 
 	@Override
 	protected HeightMapTile getHeightMapTile(Coordinate tileLocation) {
 		if (tiles.containsKey(tileLocation)) {
 			return tiles.get(tileLocation);
 		}
 		
 		if (source instanceof HeightMapProvider) {
 			HeightMapProvider provider = (HeightMapProvider) source;
 			if (provider.getSize() == this.getSize()) {
 				return provider.getHeightMapTile(tileLocation);
 			}
 		}
 		
 		return new HeightMapTile(tileLocation, source);
 	}
 
 	@Override
 	protected void setHeightMapTile(Coordinate tileLocation, HeightMapTile tile) {
 		if (tile == null) {
 			throw new IllegalArgumentException("HeightMapTile cannot be null.");
 		}
 		tiles.put(tileLocation, tile);
 	}
 
 }
