 package com.undeadscythes.udsplugin;
 
 import com.undeadscythes.udsplugin.SaveablePlayer.PlayerRank;
 import java.io.*;
 import java.util.*;
 import org.apache.commons.lang.*;
 import org.bukkit.*;
 import org.bukkit.util.Vector;
 
 /**
  * An area of blocks with in a world.
  * @author UndeadScythes
  */
 public class Region implements Saveable {
     /**
      * Region flags.
      */
     public enum RegionFlag {
         PROTECTION(true),
         MOBS(false),
         PVE(true),
         LOCK(true),
         VINES(true),
         FOOD(true),
         FIRE(false),
         SNOW(false),
         PVP(false);
 
         private boolean defaultValue;
 
         RegionFlag(final boolean value) {
             this.defaultValue = value;
         }
 
         /**
          * Get this flag's default value.
          * @return
          */
         public boolean isDefault() {
             return defaultValue;
         }
 
         @Override
         public String toString() {
             return name().toLowerCase();
         }
 
         /**
          * Get a flag by name.
          * @param string The name of the flag.
          * @return The flag or <code>null</code> if there was no match.
          */
         public static RegionFlag getByName(final String string) {
             for(RegionFlag flag : values()) {
                 if(flag.name().equals(string.toUpperCase())) {
                     return flag;
                 }
             }
             return null;
         }
     }
 
     /**
      * Region type.
      */
     public enum RegionType {
         NORMAL,
         SHOP,
         BASE,
         QUARRY,
         HOME,
         ARENA,
         CITY;
 
         /**
          * Get a region type by name.
          * @param string Name of region type.
          * @return The region type or <code>null</code> if there was no match.
          */
         public static RegionType getByName(final String string) {
             for(RegionType type : values()) {
                 if(type.name().equals(string.toUpperCase())) {
                     return type;
                 }
             }
             return null;
         }
     }
 
     /**
      * File name of region storage file.
      */
     public static final String PATH = "regions.csv";
 
     private transient String name;
     private transient Vector v1;
     private transient Vector v2;
     private Location warp;
     private transient SaveablePlayer owner;
     private transient Set<SaveablePlayer> members = new HashSet<SaveablePlayer>();
     private transient String data;
     private transient Set<RegionFlag> flags;
     private transient RegionType type;
     private PlayerRank rank = PlayerRank.NONE;
 
     /**
      * Initialise a brand new region.
      * @param name Name of the region.
      * @param v1 Minimum block position.
      * @param v2 Maximum block position.
      * @param warp Warp location of the region.
      * @param owner Owner of the region.
      * @param data Data of region, if any.
      * @param type Region type.
      */
     public Region(final String name, final Vector v1, final Vector v2, final Location warp, final SaveablePlayer owner, final String data, final RegionType type) {
         this.name = name;
         this.v1 = Vector.getMinimum(v1, v2);
         this.v2 = Vector.getMaximum(v1, v2);
         this.warp = warp;
         this.owner = owner;
         this.data = data;
         flags = new HashSet<RegionFlag>();
         for(RegionFlag flag : RegionFlag.values()) {
             if(flag.isDefault()) {
                 flags.add(flag);
             }
         }
         this.type = type;
     }
 
     /**
      * Initialise a region from a string record.
      * @param record A line from a save file.
      * @throws IOException Thrown when vectors can't be loaded.
      */
     public Region(final String record) throws IOException {
         final String[] recordSplit = record.split("\t");
         name = recordSplit[0];
         v1 = getBlockPos(recordSplit[1]);
         v2 = getBlockPos(recordSplit[2]);
         warp = (Location)(new SaveableLocation(recordSplit[3]));
         owner = UDSPlugin.getPlayers().get(recordSplit[4]);
         members = new HashSet<SaveablePlayer>();
         if(!recordSplit[5].equals("")) {
             for(String member : recordSplit[5].split(",")) {
                 members.add(UDSPlugin.getPlayers().get(member));
             }
         }
         data = recordSplit[6];
         flags = new HashSet<RegionFlag>();
         for(String flag : recordSplit[7].split(",")) {
             flags.add(RegionFlag.getByName(flag));
         }
         type = RegionType.getByName(recordSplit[8]);
         rank = PlayerRank.getByName(recordSplit[9]);
     }
 
     /**
      * Helper function to build a new block position from a string.
      * @param string String containing coded block position.
      * @return The corresponding new block position.
      */
     private Vector getBlockPos(final String string) {
         final String[] split = string.replace("(", "").replace(")", "").split(",");
         final double x = Double.parseDouble(split[0]);
         final double y = Double.parseDouble(split[1]);
         final double z = Double.parseDouble(split[2]);
         return new Vector(x, y, z);
     }
 
     @Override
     public String getRecord() {
         final ArrayList<String> record = new ArrayList<String>();
         record.add(name);
         record.add(v1.toString());
         record.add(v2.toString());
         record.add(new SaveableLocation(warp).toString());
         record.add(owner == null ? "" : owner.getName());
         final ArrayList<String> memberList = new ArrayList<String>();
         for(SaveablePlayer member : members) {
             memberList.add(member.getName());
         }
         record.add(StringUtils.join(memberList, ","));
         record.add(data);
         record.add(StringUtils.join(flags.toArray(), ","));
         record.add(type.toString());
         record.add(rank.toString());
         return StringUtils.join(record.toArray(), "\t");
     }
 
     /**
      * Clear the members of the region.
      */
     public void clearMembers() {
         members = new HashSet<SaveablePlayer>();
     }
 
     @Override
     public String toString() {
         Bukkit.getLogger().info("Implicit Region.toString()."); // Implicit .toString()
         return name;
     }
 
     /**
      * Change the owner of the region.
      * @param owner New owner name.
      */
     public void changeOwner(final SaveablePlayer owner) {
         this.owner = owner;
     }
 
     /**
      * Get the region members.
      * @return Region members.
      */
     public Set<SaveablePlayer> getMembers() {
         return members;
     }
 
     /**
      * Change the name of the region.
      * @param newName New region name.
      */
     public void changeName(final String newName) {
         name = newName;
     }
 
     /**
      * Get the name of the owner of this region.
      * @return Owner name.
      */
     public String getOwnerName() {
         return owner == null ? "" : owner.getNick();
     }
 
     /**
      * Check if the player owns this region.
      * @param player Player to check.
      * @return <code>true</code> if this is the owner of the region, <code>false</code> otherwise.
      */
     public boolean isOwner(final SaveablePlayer player) {
         return player != null && player.equals(owner);
     }
 
     /**
      *
      * @param player
      */
     public void sendInfo(final SaveablePlayer player) {
         player.sendMessage(Color.MESSAGE + "Region " + name + " info:");
         player.sendMessage(Color.TEXT + "Owner: " + (owner == null ? "" : owner.getName()) + (rank.equals(PlayerRank.NONE) ? "" : " (" + rank.toString() + ")"));
        player.sendMessage(Color.TEXT + "Members: " + StringUtils.join(members, ", "));
         player.sendMessage(Color.TEXT + "Type: " + type.toString());
         if(flags.isEmpty()) {
             player.sendMessage(Color.TEXT + "No flags.");
         } else {
             String flagString = "";
             for(RegionFlag test : flags) {
                 flagString = flagString.concat(test.toString() + ", ");
             }
             player.sendMessage(Color.TEXT + "Flags: " + flagString.substring(0, flagString.length() - 2));
         }
         player.sendMessage(Color.TEXT + "Volume: " + getVolume());
     }
 
     /**
      *
      * @return
      */
     public int getVolume() {
         return (v2.getBlockX() - v1.getBlockX() + 1) * (v2.getBlockY() - v1.getBlockY() + 1) * (v2.getBlockZ() - v1.getBlockZ() + 1);
     }
 
     /**
      * Expand this region in some direction.
      * @param direction Direction to expand.
      * @param distance Distance to expand.
      */
     public void expand(final Direction direction, final int distance) {
         if(direction.equals(Direction.NORTH)) {
             v1.add(new Vector(0, 0, -distance));
         } else if(direction.equals(Direction.SOUTH)) {
             v2.add(new Vector(0, 0, distance));
         } else if(direction.equals(Direction.EAST)) {
             v2.add(new Vector(distance, 0, 0));
         } else if(direction.equals(Direction.WEST)) {
             v1.add(new Vector(-distance, 0, 0));
         } else if(direction.equals(Direction.UP)) {
             v2.add(new Vector(0, distance, 0));
         } else if(direction.equals(Direction.DOWN)) {
             v1.add(new Vector(0, -distance, 0));
         }
     }
 
     /**
      * Get the region's rank.
      * @return Player rank of the region.
      */
     public PlayerRank getRank() {
         return rank;
     }
 
     /**
      * Set this regions player rank.
      * @param rank Rank to set.
      */
     public void setRank(final PlayerRank rank) {
         this.rank = rank;
     }
 
     /**
      * Get this regions flags.
      * @return The region flags.
      */
     public Set<RegionFlag> getFlags() {
         return flags;
     }
 
     /**
      * Change this regions defining points.
      * @param v1 New v1.
      * @param v2 New v2.
      */
     public void changeV(final Vector v1, final Vector v2) {
         this.v1 = v1;
         this.v2 = v2;
     }
 
     /**
      * Place single corner markers around the region.
      */
     public void placeCornerMarkers() {
         final EditableWorld world = new EditableWorld(getWorld());
         world.buildTower(v1.getBlockX(), v1.getBlockZ(), 1, Material.FENCE, Material.TORCH);
         world.buildTower(v2.getBlockX(), v1.getBlockZ(), 1, Material.FENCE, Material.TORCH);
         world.buildTower(v1.getBlockX(), v2.getBlockZ(), 1, Material.FENCE, Material.TORCH);
         world.buildTower(v2.getBlockX(), v2.getBlockZ(), 1, Material.FENCE, Material.TORCH);
     }
 
     /**
      * Place 3 wide side markers around the region.
      */
     public void placeMoreMarkers() {
         final EditableWorld world = new EditableWorld(getWorld());
         world.buildLine(v1.getBlockX(), (v1.getBlockZ() + v2.getBlockZ()) / 2 - 3, 0, 6, Material.FENCE, Material.TORCH);
         world.buildLine(v2.getBlockX(), (v1.getBlockZ() + v2.getBlockZ()) / 2 - 3, 0, 6, Material.FENCE, Material.TORCH);
         world.buildLine((v1.getBlockX() + v2.getBlockX()) / 2 - 3, v1.getBlockZ(), 6, 0, Material.FENCE, Material.TORCH);
         world.buildLine((v1.getBlockX() + v2.getBlockX()) / 2 - 3, v2.getBlockZ(), 6, 0, Material.FENCE, Material.TORCH);
     }
 
     /**
      * Place 10 block high towers in each corner of the region.
      */
     public void placeTowers() {
         final EditableWorld world = new EditableWorld(getWorld());
         world.buildTower(v1.getBlockX(), v1.getBlockZ(), 10, Material.FENCE, Material.GLOWSTONE);
         world.buildTower(v1.getBlockX(), v2.getBlockZ(), 10, Material.FENCE, Material.GLOWSTONE);
         world.buildTower(v2.getBlockX(), v1.getBlockZ(), 10, Material.FENCE, Material.GLOWSTONE);
         world.buildTower(v2.getBlockX(), v2.getBlockZ(), 10, Material.FENCE, Material.GLOWSTONE);
     }
 
     /**
      * Get the number of members this region has.
      * @return The number of members of this region.
      */
     public int getMemberNo() {
         return members.size();
     }
 
     /**
      * Checks if this region overlaps another region.
      * @param region Region to check.
      * @return <code>true</code> if this region overlaps with the other, <code>false</code> otherwise.
      */
     public boolean hasOverlap(final Region region) {
         return !(v1.getX() > region.getV2().getX() || v2.getX() < region.getV1().getX() || v1.getZ() > region.getV2().getZ() || v2.getZ() < region.getV1().getZ() || v1.getY() > region.getV2().getY() || v2.getY() < region.getV1().getY());
     }
 
     /**
      * Set the warp point of a region.
      * @param location Location to set as warp.
      */
     public void setWarp(final Location location) {
         warp = location;
     }
 
     /**
      * Get the name of the owner of this region.
      * @return Regions owner's name.
      */
     public SaveablePlayer getOwner() {
         return owner;
     }
 
     /**
      * Get the name of the region.
      * @return Name of the region.
      */
     public String getName() {
         return name;
     }
 
     /**
      * Change the name of the region.
      * @param name New name of the region.
      */
     public void rename(final String name) {
         this.name = name;
     }
 
     /**
      * Check if a location is contained within the region.
      * @param location Location to check.
      * @return <code>true</code> if the location is contained within the region, <code>false</code> otherwise.
      */
     public boolean contains(final Location location) {
         return contains(location.getWorld(), location.getX(), location.getY(), location.getZ());
     }
 
     /**
      * Check if coordinates are contained within the region.
      * @param world World of coordinates.
      * @param x X-Coordinate.
      * @param y Y-Coordinate.
      * @param z Z-Coordinate.
      * @return <code>true</code> if the location is contained within the region, <code>false</code> otherwise.
      */
     public boolean contains(final World world, final double x, final double y, final double z) {
         return warp.getWorld().equals(world) && x > v1.getX() && x < v2.getX() + 1 && z > v1.getZ() && z < v2.getZ() + 1 && y > v1.getY() && y < v2.getY() + 1;
     }
 
     /**
      * Get the type of this region.
      * @return Type of the region.
      */
     public RegionType getType() {
         return type;
     }
 
     /**
      * Get the warp location of this region.
      * @return Location of region warp point.
      */
     public Location getWarp() {
         return warp;
     }
 
     /**
      * Check if a player is a member of a region.
      * @param player Player name.
      * @return <code>true</code> if player is a member, <code>false</code> otherwise.
      */
     public boolean hasMember(final SaveablePlayer player) {
         return members.contains(player);
     }
 
     /**
      * Add a player as a member of the region.
      * @param player Player name.
      * @return <code>true</code> if the player was not already a member of the region, <code>false</code> otherwise.
      */
     public boolean addMember(final SaveablePlayer player) {
         return members.add(player);
     }
 
     /**
      * Remove a player as a member of a region.
      * @param player Player name.
      * @return <code>true</code> if the player was a member of the region, <code>false</code> otherwise.
      */
     public boolean delMember(final SaveablePlayer player) {
         return members.remove(player);
     }
 
     /**
      * Get the regions minimum vector.
      * @return Vector 1.
      */
     public Vector getV1() {
         return v1;
     }
 
     /**
      * Get the regions maximum vector.
      * @return Vector 2.
      */
     public Vector getV2() {
         return v2;
     }
 
     /**
      * Get the world this region is in.
      * @return The regions world.
      */
     public World getWorld() {
         return warp.getWorld();
     }
 
     /**
      * Get the data stored with this region.
      * @return The region data.
      */
     public String getData() {
         return data;
     }
 
     /**
      * Check if a region has a certain flag set.
      * @param flag Flag to check.
      * @return <code>true</code> if this flag is set, <code>false</code> otherwise.
      */
     public boolean hasFlag(final RegionFlag flag) {
         return flags.contains(flag);
     }
 
     /**
      * Set a flag in this region.
      * @param flag Flag to set.
      * @return <code>true</code> if this flag was not already set, <code>false</code> otherwise.
      */
     public boolean setFlag(final RegionFlag flag) {
         return flags.add(flag);
     }
 
     /**
      * Toggle a flag in this region.
      * @param flag Flag to toggle.
      * @return <code>true</code> if this flag is now set, <code>false</code> otherwise.
      */
     public boolean toggleFlag(final RegionFlag flag) {
         if(!flags.add(flag)) {
             return !flags.remove(flag);
         }
         return true;
     }
 }
