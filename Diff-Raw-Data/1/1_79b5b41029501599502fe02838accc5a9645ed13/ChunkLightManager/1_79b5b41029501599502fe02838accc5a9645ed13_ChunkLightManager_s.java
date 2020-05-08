 package fi.haju.haju3d.client.chunk.light;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.Queues;
 import com.google.common.collect.Sets;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 import fi.haju.haju3d.client.chunk.ChunkProvider;
 import fi.haju.haju3d.protocol.coordinate.ChunkPosition;
 import fi.haju.haju3d.protocol.coordinate.GlobalTilePosition;
 import fi.haju.haju3d.protocol.coordinate.LocalTilePosition;
 import fi.haju.haju3d.protocol.coordinate.TilePosition;
 import fi.haju.haju3d.protocol.world.Chunk;
 import fi.haju.haju3d.protocol.world.ChunkCoordinateSystem;
 import fi.haju.haju3d.protocol.world.Tile;
 
 @Singleton
 public final class ChunkLightManager {
 
   private Map<ChunkPosition, ChunkLighting> chunkLights = new ConcurrentHashMap<>();
   
   public static final TileLight AMBIENT = new TileLight(0, 0, 0, false, false);
   public static final TileLight DAY_LIGHT = new TileLight(14, 14, 14, true, true);
 
   private ChunkCoordinateSystem chunkCoordinateSystem = ChunkCoordinateSystem.DEFAULT;
 
   
   @Inject
   private ChunkProvider chunkProvider;
     
   public TileLight getLight(ChunkPosition chunkPosition, LocalTilePosition position) {
     ChunkLighting light = chunkLights.get(chunkPosition);
     if(light == null) return new TileLight();
     return light.getLight(position);
   }
     
   public TileLight getLight(TilePosition pos) {
     return getLight(pos.getChunkPosition(), pos.getTileWithinChunk());
   }
   
   public TileLight getLightAtWorldPos(GlobalTilePosition worldPosition) {
     return getLight(chunkCoordinateSystem.getChunkPosition(worldPosition), chunkCoordinateSystem.getPositionWithinChunk(worldPosition));
   }
   
   public void calculateChunkLighting(Chunk chunk) {
     if (chunk.hasLight()) {
       Set<TilePosition> sunned = calculateDirectSunLight(chunk);
       calculateReflectedLight(sunned);
       calculateLightFromNeighbours(chunk);
     }
   }
   
   public void removeOpaqueBlock(TilePosition position) {
     if(isSunLight(position.add(LocalTilePosition.UP, chunkCoordinateSystem.getChunkSize()))) {
       addSunlightFrom(position);
     }
     updateLightFromNeighbours(position);
     calculateReflectedLight(Sets.newHashSet(position));
   }
   
   private void addSunlightFrom(TilePosition position) {
     setLight(position, DAY_LIGHT);
     setSunLight(position, true);
     calculateReflectedLight(Sets.newHashSet(position));
     TilePosition next = position.add(LocalTilePosition.DOWN, chunkCoordinateSystem.getChunkSize()); 
     if(!isOpaque(getTileAt(next))) {
       addSunlightFrom(next);
     }
   }
 
   public void addOpaqueBlock(TilePosition position) {
     Set<TilePosition> edge = null;
     if(isSunLight(position)) {
       edge = blockSunLight(position, TileLight.MAX_DISTANCE);
     } else {
       edge = updateDarkness(position, TileLight.MAX_DISTANCE);
     }
     calculateReflectedLight(edge);
   }
   
   private Set<TilePosition> blockSunLight(TilePosition position, int maxDist) {
     setSunLight(position, false);
     TilePosition down = position.add(LocalTilePosition.DOWN, chunkCoordinateSystem.getChunkSize());
     Set<TilePosition> edge = Sets.newHashSet();
     if(isSunLight(down)) {
       edge.addAll(blockSunLight(down, maxDist));
     }
     edge.addAll(updateDarkness(position, maxDist));
     return edge;
   }
 
   private boolean isSunLight(TilePosition pos) {
     ChunkLighting light = chunkLights.get(pos.getChunkPosition());
     if(light == null) return false;
     return light.getLight(pos.getTileWithinChunk()).inSun;
   }
   
   private void setSunLight(TilePosition pos, boolean sun) {
     ChunkLighting light = chunkLights.get(pos.getChunkPosition());
     if(light == null) return;
     TileLight val = light.getLight(pos.getTileWithinChunk());
     val.inSun = sun;
     val.source = sun;
     light.setLight(pos.getTileWithinChunk(), val);
   }
   
   private void setLight(TilePosition pos, TileLight val) {
     ChunkLighting light = chunkLights.get(pos.getChunkPosition());
     if(light == null) return;
     light.setLight(pos.getTileWithinChunk(), val);
   }
   
   private Optional<Tile> getTileAt(TilePosition p) {
     Optional<Chunk> c = chunkProvider.getChunkIfLoaded(p.getChunkPosition());
     if(!c.isPresent()) {
       return Optional.absent();
     } else {
       return Optional.of(c.get().get(p.getTileWithinChunk()));
     }
   }
 
   private void calculateLightFromNeighbours(Chunk chunk) {
     Set<TilePosition> edge = chunk.getPosition().getEdgeTilePositions(chunkCoordinateSystem.getChunkSize());
     Set<TilePosition> updated = Sets.newHashSet();
     for(TilePosition pos : edge) {
       if(updateLightFromNeighbours(pos)) {
         updated.add(pos);
       }
     }
     calculateReflectedLight(updated);
   }
 
   private Set<TilePosition> calculateDirectSunLight(Chunk chunk) {
     Set<TilePosition> res = Sets.newHashSet();
     ChunkPosition pos = chunk.getPosition();
     ChunkLighting lighting = chunkLights.get(pos);
     int cs = chunkCoordinateSystem.getChunkSize();
     if(lighting == null) {
       lighting = new ChunkLighting(cs);
       chunkLights.put(pos, lighting);
     }
     for (int x = 0; x < cs; x++) {
       for (int z = 0; z < cs; z++) {
         TileLight light = DAY_LIGHT;
         for (int y = cs - 1; y >= 0; y--) {
           if (isOpaque(chunk.get(x, y, z))) {
             break;
           }
           LocalTilePosition p = new LocalTilePosition(x, y, z); 
           lighting.setLight(p, light);
           res.add(new TilePosition(pos, p));
         }
       }
     }
     return res;
   }
   
   private static class DarkUpdater {
     public TilePosition pos;
     public int dist;
     
     public DarkUpdater(TilePosition pos, int d) {
       this.pos = pos;
       this.dist = d;
     }
     
   }
   
   private Set<TilePosition> updateDarkness(TilePosition position, int maxDist) {
     Set<TilePosition> edge = Sets.newHashSet();
     Set<TilePosition> processed = Sets.newHashSet();
     Queue<DarkUpdater> tbd = Queues.newArrayDeque();
     tbd.add(new DarkUpdater(position, 0));
     while(!tbd.isEmpty()) {
       DarkUpdater current = tbd.remove();
       TileLight l = getLight(current.pos);
       setLight(current.pos, AMBIENT);
       processed.add(current.pos);
       if(edge.contains(current.pos)) {
         edge.remove(current.pos);
       }
       List<TilePosition> neighs = current.pos.getDirectNeighbourTiles(chunkCoordinateSystem.getChunkSize());
       for(TilePosition n : neighs) {
         Optional<Tile> optTile = getTileAt(n);
         if(!optTile.isPresent() || isOpaque(optTile.get())) {
           continue;
         }
         if(current.dist+1 > maxDist) {
           if(!processed.contains(n)) edge.add(n);
           continue;
         }
         TileLight nl = getLight(n);
         if(nl.hasBrighter(l.getDimmer())) {
           if(!processed.contains(n)) edge.add(n);
         } else if(l.hasBrighter(nl) && nl.hasBrighter(AMBIENT)) {
           tbd.add(new DarkUpdater(n, current.dist+1));
         }
       }
     }
     return edge;
   }
 
   private boolean updateLightFromNeighbours(TilePosition position) {
     List<TilePosition> neighbours = position.getDirectNeighbourTiles(chunkCoordinateSystem.getChunkSize());
     TileLight light = getLight(position);
     boolean updated = false;
     for(TilePosition nPos : neighbours) {
       Optional<Tile> optTile = getTileAt(nPos);
       if(!optTile.isPresent() || isOpaque(optTile.get())) continue;
       TileLight nLight = getLight(nPos);
       TileLight rLight = nLight.getDimmer(); 
       if(rLight.hasBrighter(light)) {
         updated = true;
         light = rLight;
         setLight(position, rLight);
       }
     }
     return updated;
   }
   
   private void calculateReflectedLight(Set<TilePosition> updateStarters) {
     Queue<TilePosition> tbp = Queues.newArrayDeque(updateStarters);
     while(!tbp.isEmpty()) {
       TilePosition pos = tbp.remove();
       List<TilePosition> positions = pos.getDirectNeighbourTiles(chunkCoordinateSystem.getChunkSize());
       TileLight light = getLight(pos);
       TileLight nv = light.getDimmer();
       if(nv.hasLight()) {
         for(TilePosition nPos : positions) {
           if(isOpaque(getTileAt(nPos))) continue;
           TileLight val = getLight(nPos);
           if(nv.hasBrighter(val)) {
             setLight(nPos, val.combineBrightest(nv));
             tbp.add(nPos);
           }
         }
       }
     }
   }
   
   private boolean isOpaque(Tile t) {
     return t != Tile.AIR;
   }
   
   private boolean isOpaque(Optional<Tile> t) {
     return !t.isPresent() || isOpaque(t.get());
   }
   
 }
