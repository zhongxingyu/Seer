 package ch.k42.metropolis.model.parcel;
 
 import ch.k42.metropolis.generator.MetropolisGenerator;
 import ch.k42.metropolis.minions.Cartesian;
 import ch.k42.metropolis.minions.Constants;
 import ch.k42.metropolis.minions.VoronoiGenerator;
 import ch.k42.metropolis.model.enums.ContextType;
 import ch.k42.metropolis.model.enums.Direction;
 import ch.k42.metropolis.model.grid.Grid;
 import org.bukkit.Chunk;
 
 import ch.k42.metropolis.WorldEdit.*;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.util.noise.SimplexOctaveGenerator;
 
 /**
  * Represents a Parcel with a schematic/clipboard as building.
  *
  * @author Thomas Richner
  */
 
 public class ClipboardParcel extends Parcel {
 
     private Clipboard clipboard;
     private Direction direction;
 
     public ClipboardParcel(Grid grid, int chunkX, int chunkZ, int chunkSizeX, int chunkSizeZ, Clipboard clipboard, ContextType contextType, Direction direction) {
         super(grid, chunkX, chunkZ, chunkSizeX, chunkSizeZ, contextType);
         this.clipboard = clipboard;
         this.direction = direction;
         grid.fillParcels(chunkX, chunkZ, this);
     }
 
     public void populate(MetropolisGenerator generator, Chunk chunk) {
         if (chunk.getX() == (chunkX) && chunk.getZ() == (chunkZ)) {
             int streetLevel = Constants.BUILD_HEIGHT;
             clipboard.paste(generator, (chunkX << 4), (chunkZ << 4), Constants.BUILD_HEIGHT, direction);
             // TODO use config, don't always destroy
             generator.getDecayProvider().destroyChunks(chunkX, chunkZ, chunkSizeX, chunkSizeZ, clipboard.getBottom(streetLevel), clipboard.getSizeY(), clipboard.getDecayOptions());
         }
     }
 
     private final int cutoutDepth = 8;
     private final int cutoutHeight = 8;
 
     @Override
     public void postPopulate(MetropolisGenerator generator, Chunk chunk) {
         //To change body of implemented methods use File | Settings | File Templates.
 
         //---- make sidewalk cutouts
         SchematicConfig.RoadCutout[] cuts = clipboard.getSettings().getCutouts();
 
         Cartesian base = new Cartesian(this.chunkX << 4, Constants.BUILD_HEIGHT - 1, this.chunkZ << 4);
 
         for (SchematicConfig.RoadCutout cut : cuts) {
             Cartesian offset, size;
             ContextType roadCheck;
             Parcel parcel;
 
             switch (direction) {
                 case NORTH:
                     offset = new Cartesian(clipboard.getSizeX(direction) - cut.startPoint, 0, -1);
                     size = new Cartesian(-cut.length, cutoutHeight, -cutoutDepth);
                     parcel = grid.getParcel(chunkX, chunkZ - 1);
                     roadCheck = parcel.getContextType();
                     if (roadCheck.equals(ContextType.STREET) || roadCheck.equals(ContextType.HIGHWAY)) {
                         cutoutBlocks(generator, base.add(offset), size, Material.STONE);
                     }
                     break;
                 case EAST:
                     offset = new Cartesian(clipboard.getSizeX(direction), 0, clipboard.getSizeZ(direction) - cut.startPoint);
                     size = new Cartesian(cutoutDepth, cutoutHeight, -cut.length);
                     parcel = grid.getParcel(chunkX + (clipboard.getSizeX(direction) >> 4), chunkZ);
                     roadCheck = parcel.getContextType();
                     if (roadCheck.equals(ContextType.STREET) || roadCheck.equals(ContextType.HIGHWAY)) {
                         cutoutBlocks(generator, base.add(offset), size, Material.STONE);
                     }
                     break;
                 case SOUTH:
                    offset = new Cartesian(cut.startPoint + 1, 0, clipboard.getSizeZ(direction));
                     size = new Cartesian(cut.length, cutoutHeight, cutoutDepth);
                     roadCheck = grid.getParcel(chunkX, chunkZ + (clipboard.getSizeZ(direction) >> 4)).getContextType();
                     if (roadCheck.equals(ContextType.STREET) || roadCheck.equals(ContextType.HIGHWAY)) {
                         cutoutBlocks(generator, base.add(offset), size, Material.STONE);
                     }
                     break;
                 case WEST:
                    offset = new Cartesian(-1, 0, cut.startPoint + 1);
                     size = new Cartesian(-cutoutDepth, cutoutHeight, cut.length);
                     parcel = grid.getParcel(chunkX - 1, chunkZ);
                     roadCheck = parcel.getContextType();
                     if (roadCheck.equals(ContextType.STREET) || roadCheck.equals(ContextType.HIGHWAY)) {
                         cutoutBlocks(generator, base.add(offset), size, Material.STONE);
                     }
                     break;
             }
         }
     }
 
     /**
      * cuts out some blocks and puts stone on the bottom
      *
      * @param v vector to start at
      * @param s size of the box to cut out ( diagonal vector )
      */
     private void cutoutBlocks(MetropolisGenerator generator, Cartesian v, Cartesian s, Material floor) {
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
         return "ClipboardParcel: " + clipboard.toString();
     }
 }
