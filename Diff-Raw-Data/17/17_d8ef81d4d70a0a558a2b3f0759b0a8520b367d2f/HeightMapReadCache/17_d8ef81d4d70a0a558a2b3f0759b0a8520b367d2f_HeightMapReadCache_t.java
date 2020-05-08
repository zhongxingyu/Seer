 package net.nabaal.majiir.realtimerender.rendering;
 
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import net.nabaal.majiir.realtimerender.Coordinate;
 import net.nabaal.majiir.realtimerender.image.ReadCache;
 
 public class HeightMapReadCache extends HeightMapProvider implements ReadCache {
 
 	private final ConcurrentMap<Coordinate, HeightMapTile> chunks = new ConcurrentHashMap<Coordinate, HeightMapTile>();
 	private final HeightMap source;
 	
 	public HeightMapReadCache(HeightMap source, int size) {
 		super(size);
 		this.source = source;
 	}
 	
 	@Override
 	public void clear() {
 		chunks.clear();
 	}
 
 	@Override
 	protected HeightMapTile getHeightMapTile(Coordinate tileLocation) {
 		if (chunks.containsKey(tileLocation)) {
 			return chunks.get(tileLocation);
 		}
 		
 		HeightMapTile tile = null;
 		if (source instanceof HeightMapProvider) {
 			HeightMapProvider provider = (HeightMapProvider) source;
 			if (provider.getSize() == this.getSize()) {
 				tile = provider.getHeightMapTile(tileLocation);
 			}
 		}
 		
 		if (tile == null) {
 			tile = new HeightMapTile(tileLocation, source);
 		}
 		
 		chunks.put(tileLocation, tile);
 		return tile;
 	}
 
 	@Override
 	protected void setHeightMapTile(Coordinate tileLocation, HeightMapTile tile) {
 		if (tile != null) {
 			chunks.put(tileLocation, tile);
 		} else {
 			chunks.remove(tileLocation);
 			tile = new HeightMapTile(tileLocation);
 		}
 		
 		if (source instanceof HeightMapProvider) {
 			HeightMapProvider provider = (HeightMapProvider) source;
 			if (provider.getSize() == this.getSize()) {
 				provider.setHeightMapTile(tileLocation, tile);
 				return;
 			}
 		}
 		
		for (int x = 0; x < (1 << getSize()); x++) {
			for (int y = 0; y < (1 << getSize()); y++) {
				Coordinate block = tileLocation.zoomIn(getSize()).plus(new Coordinate(x, y, Coordinate.LEVEL_BLOCK));
 				source.setHeight(block, tile.getHeight(block));
 			}
 		}
 	}
 
 }
