 package com.norcode.bukkit.buildinabox;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 
 import org.bukkit.material.Directional;
 import org.bukkit.material.EnderChest;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.PluginManager;
 
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 
 import com.norcode.bukkit.buildinabox.util.CuboidClipboard;
 
 import com.sk89q.worldedit.EditSession;
 import com.sk89q.worldedit.IncompleteRegionException;
 
 import com.sk89q.worldedit.LocalSession;
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldedit.bukkit.BukkitWorld;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.data.DataException;
 import com.sk89q.worldedit.regions.Region;
 import com.sk89q.worldedit.schematic.SchematicFormat;
 
 public class BuildingPlan {
     String name;
     String displayName;
     String filename;
     List<String> description;
     BuildInABox plugin;
     
     public static final EnumSet<Material> coverableBlocks = EnumSet.of(Material.LONG_GRASS, Material.SNOW, Material.AIR, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM, Material.DEAD_BUSH, Material.FIRE, Material.RED_ROSE, Material.YELLOW_FLOWER, Material.SAPLING);
     
     public BuildingPlan(BuildInABox plugin, String name, String filename, String displayName, List<String> description) {
         this.plugin = plugin;
         this.name = name;
         this.displayName = displayName;
         this.filename = filename;
         this.description = description;
         registerPermissions();
     }
 
     private void registerPermissions() {
         registerPermission("give", plugin.wildcardGivePerm);
         registerPermission("place", plugin.wildcardPlacePerm);
         registerPermission("pickup", plugin.wildcardPickupPerm);
         registerPermission("lock", plugin.wildcardLockPerm);
         registerPermission("unlock", plugin.wildcardUnlockPerm);
     }
 
     public void unregisterPermissions() {
         PluginManager pm = plugin.getServer().getPluginManager();
         pm.removePermission("biab.give." + name.toLowerCase());
         pm.removePermission("biab.place." + name.toLowerCase());
         pm.removePermission("biab.pickup." + name.toLowerCase());
         pm.removePermission("biab.lock." + name.toLowerCase());
         pm.removePermission("biab.unlock." + name.toLowerCase());
     }
 
     private void registerPermission(String action, Permission parent) {
        Permission p = new Permission("biab." + action.toLowerCase() + "." + name.toLowerCase(), "Permission to " + action + " " + name + " BIABs.", PermissionDefault.OP);
        p.addParent(parent, true);
        plugin.getServer().getPluginManager().addPermission(p);
        parent.recalculatePermissibles();
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setFilename(String filename) {
         this.filename = filename;
     }
 
     public void setDescription(List<String> description) {
         this.description = description;
     }
 
     public String getName() {
         return this.name;
     }
 
     public String getDisplayName() {
         return (displayName == null || displayName.equals("")) ? getName() : displayName;
     }
 
     public void setDisplayName(String displayName) {
         this.displayName = displayName;
     }
 
 
     public static Vector findEnderChest(CuboidClipboard cc) {
         for (int x = 0; x < cc.getSize().getBlockX(); x++) {
             for (int y = 0; y < cc.getSize().getBlockY(); y++) {
                 for (int z = 0; z < cc.getSize().getBlockZ(); z++) {
                     Vector v = new Vector(x,y,z);
                     if (cc.getPoint(v).getType() == BuildInABox.BLOCK_ID) {
                         return new Vector(-v.getBlockX(), -v.getBlockY(), -v.getBlockZ());
                     }
                 }
             }
         }
         return null;
     }
 
     public static BuildingPlan fromClipboard(BuildInABox plugin, Player player, String name) {
         WorldEditPlugin we = plugin.getWorldEdit();
         BuildingPlan plan = null;
         LocalSession session = we.getSession(player);
         EditSession es = new EditSession(new BukkitWorld(player.getWorld()), we.getWorldEdit().getConfiguration().maxChangeLimit);
         es.enableQueue();
         CuboidClipboard cc = null;
         try {
             com.sk89q.worldedit.LocalPlayer wp = we.wrapPlayer(player);
             Region region = session.getSelection(wp.getWorld());
             Vector min = region.getMinimumPoint();
             Vector max = region.getMaximumPoint();
             Vector pos = session.getPlacementPosition(wp);
 
             cc = new CuboidClipboard(
                     max.subtract(min).add(new Vector(1, 1, 1)),
                     min, min.subtract(pos));
             cc.copy(es);
             es.flushQueue();
         } catch (IncompleteRegionException e) {
             player.sendMessage(BuildInABox.getErrorMsg("world-edit-selection-needed"));
             return null;
         }
         Vector chestOffset = findEnderChest(cc);
         if (chestOffset == null) {
             player.sendMessage(BuildInABox.getErrorMsg("enderchest-not-found"));
             return null;
         }
         
         Directional md = (Directional) Material.getMaterial(BuildInABox.BLOCK_ID).getNewData((byte)cc.getPoint(new Vector(-chestOffset.getBlockX(), -chestOffset.getBlockY(), -chestOffset.getBlockZ())).getData());
         if (!md.getFacing().equals(BlockFace.NORTH)) {
             cc.rotate2D(CuboidClipboard.getRotationDegrees(md.getFacing(), BlockFace.NORTH));
             chestOffset = findEnderChest(cc);
         }
         cc.setOffset(chestOffset);
         try {
             SchematicFormat.MCEDIT.save(cc.toWorldEditClipboard(), new File(new File(plugin.getDataFolder(), "schematics"), name + ".schematic"));
             plan = new BuildingPlan(plugin, name, name+".schematic", name, null);
             plugin.getDataStore().saveBuildingPlan(plan);
         } catch (IOException e) {
             e.printStackTrace();
         } catch (DataException e) {
             e.printStackTrace();
         }
         return plan;
     }
 
     public File getSchematicFile() {
         return new File(new File(plugin.getDataFolder(), "schematics"), filename);
     }
 
     public CuboidClipboard getRotatedClipboard(BlockFace facing) {
         try {
             CuboidClipboard clipboard = new CuboidClipboard(SchematicFormat.MCEDIT.load(this.getSchematicFile()));
             clipboard.rotate2D(CuboidClipboard.getRotationDegrees(BlockFace.NORTH, facing));
             clipboard.setOffset(findEnderChest(clipboard));
             return clipboard;
         } catch (IOException e) {
             e.printStackTrace();
         } catch (DataException e) {
             e.printStackTrace();
         }
         return null;
     }
 
 
 
     public String getFilename() {
         return filename;
     }
 
     public List<String> getDescription() {
         if (description == null) {
             this.description = new ArrayList<String>();
         }
         return description;
     }
 
 }
