 package fi.haju.haju3d.client.ui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import com.google.common.collect.Sets;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.jme3.collision.CollisionResults;
 import com.jme3.math.Ray;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Spatial;
 
 import fi.haju.haju3d.client.ClientSettings;
 import fi.haju.haju3d.client.chunk.ChunkProvider;
 import fi.haju.haju3d.client.ui.mesh.ChunkSpatialBuilder;
 import fi.haju.haju3d.protocol.Vector3i;
 import fi.haju.haju3d.protocol.world.Chunk;
 import fi.haju.haju3d.protocol.world.TilePosition;
 import fi.haju.haju3d.protocol.world.World;
 
 @Singleton
 public class WorldManager {
 
   private static final float PICK_ACCURACY = 0.001f;
 
   static final float SCALE = 1;
   
   @Inject
   private ChunkProvider chunkProvider;
   @Inject
   private ChunkSpatialBuilder builder;
   @Inject
   private ClientSettings settings;
   
   private World world = new World();
   private Map<Vector3i, ChunkSpatial> chunkSpatials = new ConcurrentHashMap<>();
   private AtomicBoolean running = new AtomicBoolean(false);
   private Object lock = new Object();
   private transient Vector3i position;
     
   private Runnable runnable = new Runnable() {
     @Override
     public void run() {
       while (running.get()) {
         try {
           Thread.sleep(10L);
         } catch (InterruptedException e) {
         }
         if (position != null) {
           makeChunkNearPosition(position);
         }
         removeFarChunks(position);
       }
     }
   };
   
   private Vector3i getWorldPosition(Vector3f location) {
     return new Vector3i((int) Math.floor(location.x / SCALE), (int) Math.floor(location.y / SCALE), (int) Math.floor(location.z / SCALE));
   }
   
   public Vector3i getChunkIndexForLocation(Vector3f location) {
     return world.getChunkIndex(getWorldPosition(location));
   }
   
   public Vector3f getGlobalPosition(Vector3i worldPosition) {
     return new Vector3f(worldPosition.x * SCALE, worldPosition.y * SCALE, worldPosition.z * SCALE);
   }
   
   public Vector3f getTerrainCollisionPoint(Vector3f from, Vector3f to, float distanceFix) {
     return getCollisionPoint(from, to, distanceFix, false);
   }
   
   public TilePosition getVoxelCollisionPoint(Vector3f from, Vector3f to) {
     Vector3f collision = getCollisionPoint(from, to, 0.0f, true);
    int chunkSize = world.getChunkSize();
     if(collision == null) return null;
     // Move collision slightly to the other side of the polygon
     Vector3f collisionTile = collision.add(to.subtract(from).normalize().mult(PICK_ACCURACY));
     Vector3i chunkPos = new Vector3i(
        (int) (collisionTile.x / chunkSize / SCALE), 
        (int) (collisionTile.y / chunkSize / SCALE), 
        (int) (collisionTile.z / chunkSize / SCALE));
     Vector3i tilePos = new Vector3i(
        (int) (collisionTile.x / SCALE - chunkPos.x * chunkSize),
        (int) (collisionTile.y / SCALE - chunkPos.y * chunkSize),
        (int) (collisionTile.z / SCALE - chunkPos.z * chunkSize)
         );
    if(tilePos.x < 0) {
      chunkPos.x -= 1;
      tilePos.x += chunkSize - 1;
    }
    if(tilePos.y < 0) {
      chunkPos.y -= 1;
      tilePos.y += chunkSize - 1;
    }
    if(tilePos.z < 0) {
      chunkPos.z -= 1;
      tilePos.z += chunkSize - 1;
    }
     return new TilePosition(chunkPos, tilePos);
   }
   
   private Vector3f getCollisionPoint(Vector3f from, Vector3f to, float distanceFix, boolean useBoxes) {
     Set<Vector3i> chunkPositions = Sets.newHashSet(getChunkIndexForLocation(from), getChunkIndexForLocation(to));
     Ray ray = new Ray(from, to.subtract(from).normalize());
     float distance = from.distance(to);
     for (Vector3i pos : chunkPositions) {
       ChunkSpatial cs = getChunkSpatial(pos);
       CollisionResults collision = new CollisionResults();
       Spatial spatial = cs == null ? null : (useBoxes ? cs.cubes : cs.lowDetail); 
       if (spatial != null && spatial.collideWith(ray, collision) != 0) {
         Vector3f closest = collision.getClosestCollision().getContactPoint();
         boolean collided = closest.distance(from) <= distance + distanceFix;
         if (collided) {
           return closest;
         }
       }
     }
     return null;
   }
   
   private void removeFarChunks(Vector3i centerChunkIndex) {
     Set<Vector3i> remove = new HashSet<>();
     for (Map.Entry<Vector3i, ChunkSpatial> c : chunkSpatials.entrySet()) {
       Vector3i v = c.getKey();
       if (centerChunkIndex.distanceTo(v) > settings.getChunkRenderDistance() + 1) {
         remove.add(v);
       }
     }
     for (Vector3i r : remove) {
       chunkSpatials.remove(r);
     }
   }
 
   private void makeChunkNearPosition(Vector3i centerChunkIndex) {
     List<Vector3i> indexes = new ArrayList<>();
     indexes.add(centerChunkIndex);
     indexes.addAll(centerChunkIndex.getPositionsAtMaxDistance(settings.getChunkRenderDistance()));
     Collections.sort(indexes, new Comparator<Vector3i>() {
       @Override
       public int compare(Vector3i o1, Vector3i o2) {
         if(o1.distanceTo(position) < o2.distanceTo(position)) return -1;
         if(o1.distanceTo(position) > o2.distanceTo(position)) return 1;
         return 0;
       }
     });
     for (Vector3i i : indexes) {
       if (chunkSpatials.containsKey(i)) {
         continue;
       }
       makeChunkAt(i);
       break;
     }
   }
 
   public void rebuildChunkSpatial(ChunkSpatial spatial) {
     builder.rebuildChunkSpatial(world, spatial);
   }
   
   private void makeChunkAt(Vector3i chunkIndex) {
     // need 3x3 chunks around meshing area so that mesh borders can be handled correctly
     List<Chunk> chunks = chunkProvider.getChunks(chunkIndex.getSurroundingPositions());
     for (Chunk c : chunks) {
       world.setChunk(c.getPosition(), c);
     }
     ChunkSpatial spatial = builder.makeChunkSpatial(world, chunkIndex);
     chunkSpatials.put(chunkIndex, spatial);
   }
 
   public void setPosition(Vector3i position) {
     this.position = position;
     synchronized (lock) {
       lock.notify();
     }
   }
   
   public void start() {
     if (!running.get()) {
       running.set(true);
       Thread thread = new Thread(runnable, "WorldManager");
       thread.setPriority(3);
       thread.start();
     }
   }
   
   public void stop() {
     running.set(false);
     synchronized (lock) {
       lock.notify();
     }
   }
 
   public ChunkSpatial getChunkSpatial(Vector3i pos) {
     return chunkSpatials.get(pos);
   }
 
   public int getChunkSize() {
     return world.getChunkSize();
   }
 
 
 }
