 package fi.haju.haju3d.server.world.utils;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import fi.haju.haju3d.protocol.coordinate.LocalTilePosition;
import fi.haju.haju3d.protocol.coordinate.TilePosition;
 import fi.haju.haju3d.protocol.world.Chunk;
 import fi.haju.haju3d.protocol.world.Tile;
 
 public final class FloodFiller {
   private List<LocalTilePosition> front = new ArrayList<>();
   private Set<LocalTilePosition> visited = new HashSet<>();
   private Chunk ground;
   private Chunk orig;
 
   public FloodFiller(Chunk ground, Chunk orig) {
     this.ground = ground;
     this.orig = orig;
   }
 
   public void fill() {
    Set<TilePosition> edge = ground.getPosition().getEdgeTilePositions(ground.getSize());
    for(TilePosition pos : edge) {
      test(pos.getTileWithinChunk()); 
    }
     while (!front.isEmpty()) {
       LocalTilePosition v = front.remove(front.size() - 1);
       test(v.add(1, 0, 0));
       test(v.add(-1, 0, 0));
       test(v.add(0, 1, 0));
       test(v.add(0, -1, 0));
       test(v.add(0, 0, 1));
       test(v.add(0, 0, -1));
     }
   }
 
   private void test(LocalTilePosition n) {
     if (visited.contains(n)) {
       return;
     }
     if (!orig.isInside(n.x, n.y, n.z) || orig.get(n.x, n.y, n.z) == Tile.AIR) {
       return;
     }
     ground.set(n.x, n.y, n.z, orig.get(n.x, n.y, n.z));
     ground.setColor(n.x, n.y, n.z, orig.getColor(n.x, n.y, n.z));
     visited.add(n);
     front.add(n);
   }
 }
