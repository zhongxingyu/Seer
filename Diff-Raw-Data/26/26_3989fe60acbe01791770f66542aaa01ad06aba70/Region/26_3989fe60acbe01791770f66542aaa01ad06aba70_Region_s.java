 package com.undeadscythes.udsplugin;
 
 import com.undeadscythes.udsplugin.utilities.*;
 import java.io.*;
 import java.util.*;
 import org.apache.commons.lang.*;
 import org.bukkit.*;
 import org.bukkit.block.*;
 import org.bukkit.util.Vector;
 
 /**
  * An area of blocks with in a world.
  * 
  * @author UndeadScythes
  */
 public class Region extends Cuboid implements Saveable {
     private String name;
     private Location warp;
     private SaveablePlayer owner;
     private HashSet<SaveablePlayer> members = new HashSet<SaveablePlayer>(0);
     private String data;
     private EnumSet<RegionFlag> flags = EnumSet.noneOf(RegionFlag.class);
     private RegionType type;
    private PlayerRank rank = PlayerRank.NONE;
 
     public Region(final String name, final Vector v1, final Vector v2, final Location warp, final SaveablePlayer owner, final String data, final RegionType type) {
         this.name = name;
         setV1(VectorUtils.getFlooredVector(Vector.getMinimum(v1, v2)));
         setV2(VectorUtils.getFlooredVector(Vector.getMaximum(v1, v2)));
         this.warp = warp;
         setWorld(warp.getWorld());
         this.owner = owner;
         this.data = data;
         for(RegionFlag flag : RegionFlag.values()) {
             if(flag.isDefaulted()) {
                 flags.add(flag);
             }
         }
         this.type = type;
     }
 
     public Region(final String record) throws IOException {
         final String[] recordSplit = record.split("\t");
         name = recordSplit[0];
         setV1(VectorUtils.getVector(recordSplit[1]));
         setV2(VectorUtils.getVector(recordSplit[2]));
         warp = LocationUtils.parseLocation(recordSplit[3]);
         setWorld(warp.getWorld());
         owner = PlayerUtils.getPlayer(recordSplit[4]);
         members = new HashSet<SaveablePlayer>(0);
         if(!recordSplit[5].isEmpty()) {
             for(String member : recordSplit[5].split(",")) {
                 members.add(PlayerUtils.getPlayer(member));
             }
         }
         data = recordSplit[6];
         for(String flag : recordSplit[7].split(",")) {
             if(RegionFlag.getByName(flag) != null) {
                 flags.add(RegionFlag.getByName(flag));
             }
         }
         type = RegionType.getByName(recordSplit[8]);
         rank = PlayerRank.getByName(recordSplit[9]);
     }
 
     @Override
     public final String getRecord() {
         final ArrayList<String> record = new ArrayList<String>(10);
         record.add(name);
         record.add(getV1String());
         record.add(getV2String());
         record.add(LocationUtils.getString(warp));
        record.add(owner == null ? "" : owner.getName());
         final ArrayList<String> memberList = new ArrayList<String>(0);
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
 
     public final void clearMembers() {
         members = new HashSet<SaveablePlayer>(0);
     }
 
     public final void changeOwner(final SaveablePlayer owner) {
         this.owner = owner;
         rank = null;
     }
 
     public final Set<SaveablePlayer> getMembers() {
         return members; //TODO: Check calls to this method and add methods for each one.
     }
 
     public final void setType(final RegionType type) {
         this.type = type;
     }
 
     public final String getOwnerName() {
         return owner == null ? "" : owner.getNick();
     }
 
     public final boolean isOwnedBy(final SaveablePlayer player) {
         return player != null && player.equals(owner);
     }
 
     public final String getMemberList() {
         String membersString = "";
         for(SaveablePlayer member : members) {
             membersString = membersString.concat(", " + member.getName());
         }
         return membersString.replaceFirst(", ", "");
     }
 
     public final void sendInfo(final SaveablePlayer player) {
         player.sendNormal("Region " + name + " info:");
        player.sendText("Owner:" + (owner == null ? "" : " " + owner.getName()) + (rank.equals(PlayerRank.NONE) ? "" : " " + rank.toString() + "+"));
         player.sendText("Members: " + getMemberList());
         player.sendText("Type: " + type.toString());
         if(flags.isEmpty()) {
             player.sendText("No flags.");
         } else {
             String flagString = "";
             for(RegionFlag test : flags) {
                 flagString = flagString.concat(test.toString() + ", ");
             }
             player.sendText("Flags: " + flagString.substring(0, flagString.length() - 2));
         }
         player.sendText("Volume: " + getVolume());
     }
 
     public final PlayerRank getRank() {
         return rank;
     }
 
     public final void setRank(final PlayerRank rank) {
         this.rank = rank;
         owner = null;
     }
 
     public final Set<RegionFlag> getFlags() {
         return flags; //TODO: find calls to this method and create new methods for each case.
     }
 
     public final void placeCornerMarkers() {
         final EditableWorld world = new EditableWorld(getWorld());
         world.buildTower(getV1().getBlockX(), getV1().getBlockZ(), 1, Material.FENCE, Material.TORCH);
         world.buildTower(getV2().getBlockX(), getV1().getBlockZ(), 1, Material.FENCE, Material.TORCH);
         world.buildTower(getV1().getBlockX(), getV2().getBlockZ(), 1, Material.FENCE, Material.TORCH);
         world.buildTower(getV2().getBlockX(), getV2().getBlockZ(), 1, Material.FENCE, Material.TORCH);
     }
 
     public final void placeMoreMarkers() {
         final EditableWorld world = new EditableWorld(getWorld());
         world.buildLine(getV1().getBlockX(), (getV1().getBlockZ() + getV2().getBlockZ()) / 2 - 3, 0, 6, Material.FENCE, Material.TORCH);
         world.buildLine(getV2().getBlockX(), (getV1().getBlockZ() + getV2().getBlockZ()) / 2 - 3, 0, 6, Material.FENCE, Material.TORCH);
         world.buildLine((getV1().getBlockX() + getV2().getBlockX()) / 2 - 3, getV1().getBlockZ(), 6, 0, Material.FENCE, Material.TORCH);
         world.buildLine((getV1().getBlockX() + getV2().getBlockX()) / 2 - 3, getV2().getBlockZ(), 6, 0, Material.FENCE, Material.TORCH);
     }
 
     public final void placeTowers() {
         final EditableWorld world = new EditableWorld(getWorld());
         world.buildTower(getV1().getBlockX(), getV1().getBlockZ(), 10, Material.FENCE, Material.GLOWSTONE);
         world.buildTower(getV1().getBlockX(), getV2().getBlockZ(), 10, Material.FENCE, Material.GLOWSTONE);
         world.buildTower(getV2().getBlockX(), getV1().getBlockZ(), 10, Material.FENCE, Material.GLOWSTONE);
         world.buildTower(getV2().getBlockX(), getV2().getBlockZ(), 10, Material.FENCE, Material.GLOWSTONE);
     }
 
     public final int getMemberNo() {
         return members.size();
     }
 
     public final void setWarp(final Location location) {
         warp = location;
     }
 
     public final SaveablePlayer getOwner() {
         return owner;
     }
 
     public final String getName() {
         return name;
     }
 
     public final void rename(final String name) {
         this.name = name;
     }
 
     public final RegionType getType() {
         return type;
     }
 
     public final Location getWarp() {
         return LocationUtils.findSafePlace(warp);
     }
 
     public final boolean hasMember(final SaveablePlayer player) {
         return members.contains(player);
     }
 
     public final boolean addMember(final SaveablePlayer player) {
         return members.add(player);
     }
 
     public final boolean delMember(final SaveablePlayer player) {
         return members.remove(player);
     }
 
     public final String getData() {
         return data;
     }
     
     public final void setData(final String data) {
         this.data = data;
     }
 
     public final boolean hasFlag(final RegionFlag flag) {
         return flags.contains(flag);
     }
 
     public final boolean setFlag(final RegionFlag flag) {
         return flags.add(flag);
     }
 
     public final boolean toggleFlag(final RegionFlag flag) {
         if(!flags.add(flag)) {
             return !flags.remove(flag);
         }
         return true;
     }
 
     public final boolean contains(final Location location) {
         return location.toVector().isInAABB(getV1(), getV2());
     }
     
     public final void replace(final Material from, final Material to) {
         final World world = warp.getWorld();
         for(int x = getV1().getBlockX(); x < getV2().getBlockX(); x++) {
             for(int y = getV1().getBlockY(); y < getV2().getBlockY(); y++) {
                 for(int z = getV1().getBlockZ(); z < getV2().getBlockZ(); z++) {
                     final Block block = world.getBlockAt(x, y, z);
                     if(!block.getType().equals(from)) {
                         continue;
                     }
                     block.setType(to);
                 }
             }
         }
     }
 }
