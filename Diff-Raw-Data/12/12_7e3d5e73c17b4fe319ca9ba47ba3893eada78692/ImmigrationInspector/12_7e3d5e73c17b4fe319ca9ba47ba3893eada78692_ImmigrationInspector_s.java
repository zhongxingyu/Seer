 package edgruberman.bukkit.borderpatrol;
 
 import java.util.Random;
 import java.util.logging.Level;
 
 import net.minecraft.server.MathHelper;
 import net.minecraft.server.WorldServer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.TravelAgent;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.event.world.PortalCreateEvent;
 import org.bukkit.plugin.Plugin;
 
 /**
  * Replacement for PortalTravelAgent that ensures portals are only
  * found and created within borders
  */
 final class ImmigrationInspector implements TravelAgent, Listener {
 
     private final Plugin plugin;
     private final CivilEngineer engineer;
 
     ImmigrationInspector(final Plugin plugin, final CivilEngineer engineer) {
         this.plugin = plugin;
         this.engineer = engineer;
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerPortal(final PlayerPortalEvent event) {
         // Let normality happen if no border defined for target world
         final Border border = this.engineer.getBorder(event.getTo().getWorld());
         if (border == null) return;
 
         this.plugin.getLogger().log(Level.FINEST, event.getPlayer().getName() + " entered a portal at " + ImmigrationInspector.describeLocation(event.getFrom()));
         event.setPortalTravelAgent(this);
     }
 
     /**
      * Generate a human readable location reference that shows translated
      * coordinates for portal calculations also.
      *
      * @param l location to describe
      * @return textual representation of location
      */
     private static String describeLocation(final Location l) {
         if (l == null) return null;
 
         final boolean normal = l.getWorld().getEnvironment().equals(Environment.NORMAL);
         final double toNether = 1D / 8D;
         final double toOverworld = 8D;
         final double translation = ( normal ? toNether : toOverworld);
 
         return "[" + l.getWorld().getName() + "]"
             + " x:" + l.getBlockX()
             + " y:" + l.getBlockY()
             + " z:" + l.getBlockZ()
             + " | " + ( normal ? "Nether" : "Overworld") + " match is"
             + " x:" + Math.round(l.getX() * translation)
             + " y:" + l.getBlockY()
             + " z:" + Math.round(l.getZ() * translation)
             + " ( "
             + l.getX() + "," + l.getY() + "," + l.getZ()
             + " | "
             + l.getX() * translation
             + "," + l.getY()
             + "," + l.getZ() * translation
             + " )";
     }
 
     private int getWorldHeight(final World world) {
         if (world.getEnvironment() == Environment.NETHER) return 128;
 
         return 256;
     }
 
     private int searchRadius = 128;
     private int creationRadius = 14; // 16 -> 14
     private boolean canCreatePortal = true;
 
     private final Random random = new Random();
 
     @Override
     public Location findOrCreate(final Location destination) {
         // Ensure destination chunks will load while searching
         final WorldServer worldServer = ((CraftWorld) destination.getWorld()).getHandle();
        worldServer.chunkProviderServer.forceChunkLoad = true;
 
         // Search for existing portal within border
         this.plugin.getLogger().log(Level.FINEST, "Attempting to locate an existing portal near " + ImmigrationInspector.describeLocation(destination));
         Location result = this.findPortal(destination);
         this.plugin.getLogger().log(Level.FINEST, "Existing portal found at " + ImmigrationInspector.describeLocation(destination));
 
         // If no existing portal found, create new portal
         if (result == null && this.canCreatePortal) {
             // Attempt to create portal within border
             this.plugin.getLogger().log(Level.FINEST, "Requesting portal creation at " + ImmigrationInspector.describeLocation(destination));
             if (this.createPortal(destination)) {
                 // Find the newly created portal
                 result = this.findPortal(destination);
                 this.plugin.getLogger().log(Level.FINEST, "Identified newly created portal at " + ImmigrationInspector.describeLocation(destination));
             }
         }
 
         // Fallback to original location
         if (result == null) {
             this.plugin.getLogger().log(Level.FINEST, "Unable to find or create portal; Falling back to original destination at " + ImmigrationInspector.describeLocation(destination));
             result = destination;
         }
 
         // Return chunks to normal loading procedure
        worldServer.chunkProviderServer.forceChunkLoad = false;
 
         return result;
     }
 
     @Override
     public Location findPortal(final Location location) {
         final World world = location.getWorld();
         final Border border = this.engineer.getBorder(world);
 
         if (world.getEnvironment() == Environment.THE_END) {
             final int i = MathHelper.floor(location.getBlockX());
             final int j = MathHelper.floor(location.getBlockY()) - 1;
             final int k = MathHelper.floor(location.getBlockZ());
             final byte b0 = 1;
             final byte b1 = 0;
 
             for (int l = -2; l <= 2; ++l) {
                 for (int i1 = -2; i1 <= 2; ++i1) {
                     for (int j1 = -1; j1 < 3; ++j1) {
                         final int k1 = i + i1 * b0 + l * b1;
                         final int l1 = j + j1;
                         final int i2 = k + i1 * b1 - l * b0;
                         final boolean flag = j1 < 0;
 
                         if (world.getBlockTypeIdAt(k1, l1, i2) != (flag ? Material.OBSIDIAN.getId() : 0)) {
                             return null;
                         }
                     }
                 }
             }
 
             return location;
         }
 
         double distanceSquaredClosest = -1.0D;
         int foundX = 0;
         int foundY = 0;
         int foundZ = 0;
         final int originalX = location.getBlockX();
         final int originalZ = location.getBlockZ();
 
         for (int x = originalX - this.searchRadius; x <= originalX + this.searchRadius; ++x) {
             final double dX = x + 0.5D - location.getX();
 
             for (int z = originalZ - this.searchRadius; z <= originalZ + this.searchRadius; ++z) {
 
                 if (!border.contains(x, z)) continue;
 
                 final double dZ = z + 0.5D - location.getZ();
 
                 for (int y = this.getWorldHeight(world) - 1; y >= 0; --y) {
                    // Using (world.getBlock(x, y, z).getType() == Material.PORTAL) leaked memory
                     if (world.getBlockTypeIdAt(x, y, z) != Material.PORTAL.getId()) continue;
 
                     while (world.getBlockTypeIdAt(x, y - 1, z) == Material.PORTAL.getId()) --y;
 
                     final double dY = y + 0.5D - location.getY();
                     final double distanceSquared = dX * dX + dY * dY + dZ * dZ;
 
                     if (distanceSquaredClosest >= 0.0D && distanceSquared >= distanceSquaredClosest) continue;
 
                     distanceSquaredClosest = distanceSquared;
                     foundX = x;
                     foundY = y;
                     foundZ = z;
                 }
             }
         }
 
         if (distanceSquaredClosest >= 0.0D) {
             // Return the middle of the lower two portal blocks
             double portalX = foundX + 0.5D;
             final double portalY = foundY + 0.5D;
             double portalZ = foundZ + 0.5D;
 
             if (world.getBlockAt(foundX - 1, foundY, foundZ).getType() == Material.PORTAL) {
                 portalX -= 0.5D;
             }
 
             if (world.getBlockAt(foundX + 1, foundY, foundZ).getType() == Material.PORTAL) {
                 portalX += 0.5D;
             }
 
             if (world.getBlockAt(foundX, foundY, foundZ - 1).getType() == Material.PORTAL) {
                 portalZ -= 0.5D;
             }
 
             if (world.getBlockAt(foundX, foundY, foundZ + 1).getType() == Material.PORTAL) {
                 portalZ += 0.5D;
             }
 
             return new Location(location.getWorld(), portalX, portalY, portalZ, location.getYaw(), location.getPitch());
         } else {
             return null;
         }
     }
 
     @Override
     public boolean createPortal(final Location location) {
         final World world = location.getWorld();
         final Border border = this.engineer.getBorder(world);
         final net.minecraft.server.World nmsWorld = ((org.bukkit.craftbukkit.CraftWorld) world).getHandle();
 
         if (location.getWorld().getEnvironment() == Environment.THE_END) {
             final int i = MathHelper.floor(location.getBlockX());
             final int j = MathHelper.floor(location.getBlockY()) - 1;
             final int k = MathHelper.floor(location.getBlockZ());
             final byte b0 = 1;
             final byte b1 = 0;
 
             for (int l = -2; l <= 2; ++l) {
                 for (int i1 = -2; i1 <= 2; ++i1) {
                     for (int j1 = -1; j1 < 3; ++j1) {
                         final int k1 = i + i1 * b0 + l * b1;
                         final int l1 = j + j1;
                         final int i2 = k + i1 * b1 - l * b0;
                         final boolean flag = j1 < 0;
 
                         world.getBlockAt(k1, l1, i2).setTypeId(flag ? Material.OBSIDIAN.getId() : 0);
                     }
                 }
             }
 
             return true;
         }
 
         double d0 = -1.0D;
         final int i = location.getBlockX();
         final int j = location.getBlockY();
         final int k = location.getBlockZ();
         int l = i;
         int i1 = j;
         int j1 = k;
         int k1 = 0;
         final int l1 = this.random.nextInt(4);
 
         int i2;
         double d1;
         int j2;
         double d2;
         int k2;
         int l2;
         int i3;
         int j3;
         int k3;
         int l3;
         int i4;
         int j4;
         int k4;
         double d3;
         double d4;
 
         for (i2 = i - this.creationRadius; i2 <= i + this.creationRadius; ++i2) {
             d1 = i2 + 0.5D - location.getX();
 
             for (j2 = k - this.creationRadius; j2 <= k + this.creationRadius; ++j2) {
                 if (!border.contains(i2, j2)) continue;
 
                 d2 = j2 + 0.5D - location.getZ();
 
                 label271:
                 for (l2 = this.getWorldHeight(world) - 1; l2 >= 0; --l2) {
                     if (!world.getBlockAt(i2, l2, j2).isEmpty()) continue;
 
                     while (l2 > 0 && world.getBlockAt(i2, l2 - 1, j2).isEmpty()) {
                         --l2;
                     }
 
                     for (k2 = l1; k2 < l1 + 4; ++k2) {
                         j3 = k2 % 2;
                         i3 = 1 - j3;
                         if (k2 % 4 >= 2) {
                             j3 = -j3;
                             i3 = -i3;
                         }
 
                         for (l3 = 0; l3 < 3; ++l3) {
                             for (k3 = 0; k3 < 4; ++k3) {
                                 for (j4 = -1; j4 < 5; ++j4) {
                                     i4 = i2 + (k3 - 1) * j3 + l3 * i3;
                                     k4 = l2 + j4;
                                     final int l4 = j2 + (k3 - 1) * i3 - l3 * j3;
 
                                     if (j4 < 0 && !nmsWorld.getMaterial(i4, k4, l4).isBuildable() || j4 >= 0 && !world.getBlockAt(i4, k4, l4).isEmpty()) {
                                         continue label271;
                                     }
                                 }
                             }
                         }
 
                         d3 = l2 + 0.5D - location.getY();
                         d4 = d1 * d1 + d3 * d3 + d2 * d2;
                         if (d0 < 0.0D || d4 < d0) {
                             d0 = d4;
                             l = i2;
                             i1 = l2 + 1;
                             j1 = j2;
                             k1 = k2 % 4;
                         }
                     }
                 }
             }
         }
 
         if (d0 < 0.0D) {
             for (i2 = i - this.creationRadius; i2 <= i + this.creationRadius; ++i2) {
                 d1 = i2 + 0.5D - location.getX();
 
                 for (j2 = k - this.creationRadius; j2 <= k + this.creationRadius; ++j2) {
                     if (!border.contains(i2, j2)) continue;
 
                     d2 = j2 + 0.5D - location.getZ();
 
                     label219:
                     for (l2 = this.getWorldHeight(world) - 1; l2 >= 0; --l2) {
                         if (!world.getBlockAt(i2, l2, j2).isEmpty()) continue;
 
                         while (l2 > 0 && world.getBlockAt(i2, l2 - 1, j2).isEmpty()) {
                             --l2;
                         }
 
                         for (k2 = l1; k2 < l1 + 2; ++k2) {
                             j3 = k2 % 2;
                             i3 = 1 - j3;
 
                             for (l3 = 0; l3 < 4; ++l3) {
                                 for (k3 = -1; k3 < 5; ++k3) {
                                     j4 = i2 + (l3 - 1) * j3;
                                     i4 = l2 + k3;
                                     k4 = j2 + (l3 - 1) * i3;
                                     if (k3 < 0 && !nmsWorld.getMaterial(j4, i4, k4).isBuildable() || k3 >= 0 && !world.getBlockAt(j4, i4, k4).isEmpty()) {
                                         continue label219;
                                     }
                                 }
                             }
 
                             d3 = l2 + 0.5D - location.getY();
                             d4 = d1 * d1 + d3 * d3 + d2 * d2;
                             if (d0 < 0.0D || d4 < d0) {
                                 d0 = d4;
                                 l = i2;
                                 i1 = l2 + 1;
                                 j1 = j2;
                                 k1 = k2 % 2;
                             }
                         }
                     }
                 }
             }
         }
 
         // Final check to ensure coordinates to be used are inside border.
         if (!border.contains(l, j1)) return false;
 
         final int i5 = l;
         int j5 = i1;
 
         j2 = j1;
         int k5 = k1 % 2;
         int l5 = 1 - k5;
 
         if (k1 % 4 >= 2) {
             k5 = -k5;
             l5 = -l5;
         }
 
         boolean flag;
 
         // CraftBukkit start - portal create event
         final java.util.ArrayList<org.bukkit.block.Block> blocks = new java.util.ArrayList<org.bukkit.block.Block>();
         // Find out what blocks the portal is going to modify, duplicated from below
 
         if (d0 < 0.0D) {
             if (i1 < 70) {
                 i1 = 70;
             }
 
             if (i1 > this.getWorldHeight(world) - 10) {
                 i1 = this.getWorldHeight(world) - 10;
             }
 
             j5 = i1;
 
             for (l2 = -1; l2 <= 1; ++l2) {
                 for (k2 = 1; k2 < 3; ++k2) {
                     for (j3 = -1; j3 < 3; ++j3) {
                         i3 = i5 + (k2 - 1) * k5 + l2 * l5;
                         l3 = j5 + j3;
                         k3 = j2 + (k2 - 1) * l5 - l2 * k5;
                         final Block b = world.getBlockAt(i3, l3, k3);
                         if (!blocks.contains(b)) {
                             blocks.add(b);
                         }
                     }
                 }
             }
         }
 
         for (l2 = 0; l2 < 4; ++l2) {
             for (k2 = 0; k2 < 4; ++k2) {
                 for (j3 = -1; j3 < 4; ++j3) {
                     i3 = i5 + (k2 - 1) * k5;
                     l3 = j5 + j3;
                     k3 = j2 + (k2 - 1) * l5;
                     final Block b = world.getBlockAt(i3, l3, k3);
                     if (!blocks.contains(b)) {
                         blocks.add(b);
                     }
                 }
             }
         }
 
         final PortalCreateEvent event = new PortalCreateEvent(blocks, world, PortalCreateEvent.CreateReason.OBC_DESTINATION);
         Bukkit.getServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) {
             return false;
         }
         // CraftBukkit end
 
         // Build portal
         if (d0 < 0.0D) {
             if (i1 < 70) {
                 i1 = 70;
             }
 
             if (i1 > this.getWorldHeight(world) - 10) {
                 i1 = this.getWorldHeight(world) - 10;
             }
 
             j5 = i1;
 
             for (l2 = -1; l2 <= 1; ++l2) {
                 for (k2 = 1; k2 < 3; ++k2) {
                     for (j3 = -1; j3 < 3; ++j3) {
                         i3 = i5 + (k2 - 1) * k5 + l2 * l5;
                         l3 = j5 + j3;
                         k3 = j2 + (k2 - 1) * l5 - l2 * k5;
                         flag = j3 < 0;
                         world.getBlockAt(i3, l3, k3).setTypeId((flag ? Material.OBSIDIAN.getId() : 0));
                     }
                 }
             }
         }
 
         for (l2 = 0; l2 < 4; ++l2) {
 
             // Do not apply physics.
             for (k2 = 0; k2 < 4; ++k2) {
                 for (j3 = -1; j3 < 4; ++j3) {
                     i3 = i5 + (k2 - 1) * k5;
                     l3 = j5 + j3;
                     k3 = j2 + (k2 - 1) * l5;
                     flag = k2 == 0 || k2 == 3 || j3 == -1 || j3 == 3;
                     world.getBlockAt(i3, l3, k3).setTypeId((flag ? Material.OBSIDIAN.getId() : Material.PORTAL.getId()), false);
                 }
             }
 
             // Now apply physics.
             for (k2 = 0; k2 < 4; ++k2) {
                 for (j3 = -1; j3 < 4; ++j3) {
                     i3 = i5 + (k2 - 1) * k5;
                     l3 = j5 + j3;
                     k3 = j2 + (k2 - 1) * l5;
                     world.getBlockAt(i3, l3, k3).setTypeId(world.getBlockTypeIdAt(i3, l3, k3));
                 }
             }
         }
 
         return true;
     }
 
     @Override
     public ImmigrationInspector setSearchRadius(final int radius) {
         this.searchRadius = radius;
         return this;
     }
 
     @Override
     public int getSearchRadius() {
         return this.searchRadius;
     }
 
     @Override
     public ImmigrationInspector setCreationRadius(final int radius) {
         this.creationRadius = radius < 2 ? 0 : radius - 2;
         return this;
     }
 
     @Override
     public int getCreationRadius() {
         return this.creationRadius;
     }
 
     @Override
     public boolean getCanCreatePortal() {
         return this.canCreatePortal;
     }
 
     @Override
     public void setCanCreatePortal(final boolean create) {
         this.canCreatePortal = create;
     }
 
 }
