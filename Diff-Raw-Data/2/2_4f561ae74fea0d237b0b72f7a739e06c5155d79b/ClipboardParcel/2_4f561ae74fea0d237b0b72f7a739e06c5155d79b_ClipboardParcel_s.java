 package ch.k42.metropolis.grid.urbanGrid.parcel;
 
 import ch.k42.metropolis.grid.urbanGrid.clipboard.Clipboard;
 import ch.k42.metropolis.grid.urbanGrid.config.SchematicConfig;
 import ch.k42.metropolis.generator.MetropolisGenerator;
 import ch.k42.metropolis.grid.urbanGrid.UrbanGrid;
 import ch.k42.metropolis.grid.urbanGrid.enums.ContextType;
 import ch.k42.metropolis.grid.urbanGrid.enums.Direction;
 import ch.k42.metropolis.grid.urbanGrid.enums.SchematicType;
 import ch.k42.metropolis.minions.Cartesian2D;
 import ch.k42.metropolis.minions.Cartesian3D;
 import ch.k42.metropolis.minions.Constants;
 import org.bukkit.Chunk;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.v1_7_R2.CraftChunk;
 
 /**
  * Represents a Parcel with a schematic/clipboard as building.
  *
  * @author Thomas Richner
  */
 
 public class ClipboardParcel extends Parcel {
 
     private Clipboard clipboard;
     private Direction direction;
 
     public ClipboardParcel(UrbanGrid grid, Cartesian2D base, Cartesian2D size, Clipboard clipboard, ContextType contextType, SchematicType schematicType, Direction direction) {
         super(base,size,contextType,schematicType,grid);
         this.clipboard = clipboard;
         this.direction = direction;
         grid.getStatistics().logSchematic(clipboard);
     }
 
     /*
      * WARNING: This is not a correct implementation of 'equals' but it fits our purpose
      */
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof ClipboardParcel)) return false;
 
         ClipboardParcel that = (ClipboardParcel) o;
 
         return clipboard.getGroupId().equals(that.clipboard.getGroupId());
     }
 
 
     @Override
     public int hashCode() {
         return clipboard.getGroupId().hashCode();
     }
 
     public void populate(MetropolisGenerator generator, Chunk chunk) {
         if (chunk.getX() == (chunkX) && chunk.getZ() == (chunkZ)) {
             if(clipboard==null) return;
             int streetLevel = Constants.BUILD_HEIGHT;
             clipboard.paste(generator, new Cartesian2D(chunkX,chunkZ), Constants.BUILD_HEIGHT);
             // TODO use config, don't always destroy
             generator.getDecayProvider().destroyChunks(chunkX, chunkZ, chunkSizeX, chunkSizeZ, clipboard.getBottom(streetLevel), clipboard.getSize().Y, clipboard.getConfig().getDecayOption());
 
         }
     }
 
     private final int cutoutDepth = 8;
    private final int cutoutHeight = 8;
 
     public Clipboard getClipboard() {
         return clipboard;
     }
 
     @Override
     public void postPopulate(MetropolisGenerator generator, Chunk chunk) {
         //To change body of implemented methods use File | Settings | File Templates.
         if(clipboard==null) return;
         //---- make sidewalk cutouts
         SchematicConfig.RoadCutout[] cuts = clipboard.getConfig().getCutouts();
 
         Cartesian3D base = new Cartesian3D(this.chunkX << 4, Constants.BUILD_HEIGHT - 1, this.chunkZ << 4);
 
         for (SchematicConfig.RoadCutout cut : cuts) {
             Cartesian3D offset, size;
             ContextType roadCheck;
             Parcel parcel;
 
             switch (direction) {
                 case NORTH:
                     offset = new Cartesian3D(clipboard.getSize().X - cut.startPoint, 0, -1);
                     size = new Cartesian3D(-cut.length, cutoutHeight, -cutoutDepth);
                     parcel = grid.getParcel(chunkX, chunkZ - 1);
                     roadCheck = parcel.getContextType();
                     if (roadCheck.equals(ContextType.STREET) || roadCheck.equals(ContextType.HIGHWAY)) {
                         cutoutBlocks(generator, base.add(offset), size, Material.STONE);
                     }
                     break;
                 case EAST:
                     offset = new Cartesian3D(clipboard.getSize().X, 0, clipboard.getSize().Z - cut.startPoint);
                     size = new Cartesian3D(cutoutDepth, cutoutHeight, -cut.length);
                     parcel = grid.getParcel(chunkX + (clipboard.getSize().X >> 4), chunkZ);
                     roadCheck = parcel.getContextType();
                     if (roadCheck.equals(ContextType.STREET) || roadCheck.equals(ContextType.HIGHWAY)) {
                         cutoutBlocks(generator, base.add(offset), size, Material.STONE);
                     }
                     break;
                 case SOUTH:
                     offset = new Cartesian3D(cut.startPoint - 1, 0, clipboard.getSize().Z);
                     size = new Cartesian3D(cut.length, cutoutHeight, cutoutDepth);
                     roadCheck = grid.getParcel(chunkX, chunkZ + (clipboard.getSize().Z >> 4)).getContextType();
                     if (roadCheck.equals(ContextType.STREET) || roadCheck.equals(ContextType.HIGHWAY)) {
                         cutoutBlocks(generator, base.add(offset), size, Material.STONE);
                     }
                     break;
                 case WEST:
                     offset = new Cartesian3D(-1, 0, cut.startPoint - 1);
                     size = new Cartesian3D(-cutoutDepth, cutoutHeight, cut.length);
                     parcel = grid.getParcel(chunkX - 1, chunkZ);
                     roadCheck = parcel.getContextType();
                     if (roadCheck.equals(ContextType.STREET) || roadCheck.equals(ContextType.HIGHWAY)) {
                         cutoutBlocks(generator, base.add(offset), size, Material.STONE);
                     }
                     break;
             }
         }
         ((CraftChunk)chunk).getHandle().initLighting(); //FIXME does it work?
     }
 
     /**
      * cuts out some blocks and puts stone on the bottom
      *
      * @param v vector to start at
      * @param s size of the box to cut out ( diagonal vector )
      */
     private void cutoutBlocks(MetropolisGenerator generator, Cartesian3D v, Cartesian3D s, Material floor) {
         int xdir, ydir, zdir;
         xdir = s.X < 0 ? -1 : 1;
         ydir = s.Y < 0 ? -1 : 1;
         zdir = s.Z < 0 ? -1 : 1;
 
         for (int x = 0; Math.abs(x) < Math.abs(s.X); x += xdir) {
             for (int z = 0; Math.abs(z) < Math.abs(s.Z); z += zdir) {
                 for (int y = 0; Math.abs(y) < Math.abs(s.Y); y += ydir) {
                     generator.getWorld().getBlockAt(v.X + x, v.Y + y, v.Z + z).setType(Material.AIR);
                 }
                 Block below = generator.getWorld().getBlockAt(v.X + x, v.Y - 1, v.Z + z);
                 if (!below.isEmpty()) {
                     generator.getWorld().getBlockAt(v.X + x, v.Y - 1, v.Z + z).setType(floor);
                 }
             }
         }
     }
 
     @Override
     public String toString() {
         if(clipboard==null)
             return "ClipboardParcel: No clip found";
         return "ClipboardParcel: " + clipboard.toString();
     }
 }
