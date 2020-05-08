 package com.zephyrr.simplezones;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import sqlibrary.Database;
 
 /**
  *
  * @author Phoenix
  */
 public class ZonePlayer {
 
     private static HashMap<String, ZonePlayer> pMap;
 
     static {
         pMap = new HashMap<String, ZonePlayer>();
     }
 
     public static void fill(Database db, String prefix) {
         ResultSet rs = db.query("SELECT * FROM " + prefix + "players");
         try {
             while (rs.next()) {
                 int id = rs.getInt("P_Id");
                 String name = rs.getString("Name");
                 int tid = rs.getInt("TownID");
                 Town t = null;
                 if (tid != -1) {
                     ResultSet rs2 = db.query("SELECT TownName FROM " + prefix + "towns WHERE T_Id=" + tid);
                     rs2.next();
                     t = Town.getTown(rs2.getString("TownName"));
                 }
                 ZonePlayer zp = new ZonePlayer(name, id);
                 zp.setTown(t);
                 pMap.put(name, zp);
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
     }
 
     public static void save(Database db, String prefix) {
         db.wipeTable(prefix + "players");
         for (ZonePlayer zp : pMap.values()) {
             int id = zp.getID();
             String name = zp.getName();
             int town = -1;
             if (zp.getTown() != null) {
                 town = zp.getTown().getID();
             }
             String query = "INSERT INTO " + prefix + "players VALUES ("
                     + id + ","
                     + "'" + name + "',"
                     + town
                     + ")";
             db.query(query);
         }
     }
 
     public static HashMap<String, ZonePlayer> getPMap() {
         return pMap;
     }
 
     public static void registerUser(Player p) {
         if (!pMap.containsKey(p.getName())) {
             pMap.put(p.getName(), new ZonePlayer(p.getName(), pMap.size()));
         }
     }
 
     public static ZonePlayer findUser(String s) {
         return pMap.get(s);
     }
 
     public static ZonePlayer findUser(Player p) {
         return pMap.get(p.getName());
     }
     private Player player;
     private Town town;
     private ArrayList<Mail> mail;
     private int id;
     private Location corner1, corner2;
     private String name;
 
     public ZonePlayer(String name, int id) {
         player = SimpleZones.getPlayer(name);
         this.id = id;
         this.name = name;
         mail = new ArrayList<Mail>();
         corner1 = new Location(SimpleZones.getDefaultWorld(), 0, 0, 0);
         corner2 = corner1.clone();
     }
 
     public int getID() {
         return id;
     }
 
     public void setPlayer(Player p) {
         player = p;
     }
 
     public Player getPlayer() {
         return player;
     }
 
     public Town getTown() {
         return town;
     }
 
     public String getName() {
         return name;
     }
 
     public boolean equals(Object o) {
         if (!(o instanceof ZonePlayer)) {
             return false;
         }
         return ((ZonePlayer) o).getName().equals(getName());
     }
 
     public void setTown(Town t) {
         town = t;
     }
 
     public void leaveTown() {
         if (town == null) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You aren't a member of any towns.");
         } else if (town.getOwner().equals(getName())) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You can't leave a town while you own it.");
         } else {
             player.sendMessage(ChatColor.GOLD + "[SimpleZones] You have left " + town.getName());
             town = null;
         }
     }
 
     public ArrayList<Mail> getMailList() {
         return mail;
     }
 
     public void sendMail(Mail message) {
         if (player != null && player.isOnline()) {
             player.sendMessage(ChatColor.GOLD + "[SimpleZones] You have received a new mail message.");
             player.sendMessage(ChatColor.GOLD + "[SimpleZones] You can read it by typing " + ChatColor.GREEN + "/mail read " + (mail.size() + 1));
         }
         mail.add(message);
     }
     public void getMailInfo() {
         int read = 0, unread = 0;
         for(Mail m : mail) {
             if(m.isUnread())
                 unread++;
             else read++;
         }
         player.sendMessage(ChatColor.GOLD + "[SimpleZones] You have " + unread + " unread messages and " + read + " read messages.");
     }
 
     public void deleteMail(int index) {
         index--;
         if(index >= 0 && index < mail.size()) {
             mail.remove(index);
             player.sendMessage(ChatColor.GOLD + "[SimpleZones] The message has been deleted.");
         } else player.sendMessage(ChatColor.RED + "[SimpleZones] Invalid mail index.");
     }
 
     public void getMailInfo(int index) {
         index--;
         if (index >= 0 && index < mail.size()) {
             player.sendMessage(mail.get(index).getInfo());
         } else {
             player.sendMessage(ChatColor.RED + "That isn't a valid mail index.");
         }
     }
 
     public void readMail(int index) {
         index--;
         if (index >= 0 && index < mail.size()) {
             player.sendMessage(mail.get(index).read());
         } else {
             player.sendMessage(ChatColor.RED + "That isn't a valid mail index.");
         }
     }
 
     public boolean isDefining() {
         return corner2 == null;
     }
 
     public void setCorner(Location loc) {
         if (!isDefining()) {
             return;
         }
         if (corner1 == null) {
             corner1 = loc;
             player.sendMessage(ChatColor.GOLD + "[SimpleZones] First corner selected.  Please strike the second corner.");
         } else {
             if (loc.getWorld() != corner1.getWorld()) {
                 player.sendMessage(ChatColor.RED + "[SimpleZones] You must define two points in the same world.");
             } else {
                 corner2 = loc;
                 player.sendMessage(ChatColor.GOLD + "[SimpleZones] Second corner selected.");
             }
         }
     }
 
     /******************************************************************************
      * SIMPLEZONES COMMANDS
      ******************************************************************************/
     public boolean define() {
         if (town != null) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You can only be in one town at a time.");
         } else {
             corner1 = null;
             corner2 = null;
             player.sendMessage(ChatColor.GOLD + "Strike the first corner of your new town.");
         }
         return true;
     }
 
     public boolean create(String name, Database db, String prefix) {
         if (town != null) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You can only be in one town at a time.");
         } else {
             if (corner2 == null || (corner1.getBlockX() == 0 && corner1.getBlockY() == 0 && corner1.getBlockZ() == 0)) {
                 player.sendMessage(ChatColor.RED + "[SimpleZones] You need to define points first.");
             } else if (OwnedLand.hasOverlap(corner1, corner2, false)) {
                 player.sendMessage(ChatColor.RED + "[SimpleZones] There is another town contained in your selection.");
             } else if (Town.getTown(name) != null) {
                 player.sendMessage(ChatColor.RED + "[SimpleZones] There is already a town named " + name);
             } else {
                 try {
                     ResultSet rs = db.query("SELECT IFNULL(max(T_Id), 0) AS max FROM " + prefix + "towns");
                     rs.next();
                     int tid = rs.getInt("max") + 1;
                     Town t = new Town(tid, corner1, corner2, name);
                     town = t;
                     t.setOwner(getName());
                     town.setWarp(player.getLocation());
                     Town.addTown(town);
                     player.sendMessage(ChatColor.GOLD + "[SimpleZones] You are now the owner of " + name);
                    Town.save(db, prefix);
                 } catch (SQLException ex) {
                     ex.printStackTrace();
                 }
             }
         }
         return true;
     }
 
     public boolean plotDefine() {
         if (town == null || !town.getOwner().equals(name)) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You aren't the owner of a town." + town);
         } else {
             corner1 = null;
             corner2 = null;
             player.sendMessage(ChatColor.GOLD + "Strike the first corner of your new plot.");
         }
         return true;
     }
 
     public boolean plotCreate(Database db, String prefix) {
         if (town == null || !town.getOwner().equals(name)) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You aren't the owner of a town. ");
         } else if (corner2 == null || (corner1.getBlockX() == 0 && corner1.getBlockY() == 0 && corner1.getBlockZ() == 0)) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You need to define points first.");
         } else if (OwnedLand.getLandAtPoint(corner1) != town || OwnedLand.getLandAtPoint(corner2) != town) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] Your plot must be contained in your town.");
         } else if (OwnedLand.hasOverlap(corner1, corner2, true)) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] This plot overlaps with another.");
         } else {
             try {
                 ResultSet rs = db.query("SELECT IFNULL(max(P_Id), -1) AS max FROM " + prefix + "plots WHERE TownID=" + town.getID());
                 rs.next();
                 int pid = rs.getInt("max") + 1;
                 Plot p = new Plot(pid, corner1, corner2, town);
                 town.addPlot(p);
                 player.sendMessage(ChatColor.GOLD + "[SimpleZones] You have added a new plot to " + town.getName());
             } catch (SQLException ex) {
                 ex.printStackTrace();
             }
         }
         return true;
     }
 
     public boolean plotAddMember(String name) {
         if(!(OwnedLand.getLandAtPoint(player.getLocation()) instanceof Plot)) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You are not currently standing in a plot.");
         } else if(!((Plot)(OwnedLand.getLandAtPoint(player.getLocation()))).getTown().getOwner().equals(getName())) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You do not own this town.");
         } else if(!town.getMembers().contains(name)) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] " + name + " is not a member of your town.");
         } else if (!OwnedLand.getLandAtPoint(player.getLocation()).addMember(name)) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] " + name + " is already a member of this plot.");
         } else {
             player.sendMessage(ChatColor.GOLD + "[SimpleZones] " + name + " has been added to this plot.");
         }
         return true;
     }
 
     public boolean plotRemoveMember(String name) {
         if(!(OwnedLand.getLandAtPoint(player.getLocation()) instanceof Plot)) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You are not currently standing in a plot.");
         } else if(!((Plot)(OwnedLand.getLandAtPoint(player.getLocation()))).getTown().getOwner().equals(getName())) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You do not own this town.");
         } else if(!((Plot)(OwnedLand.getLandAtPoint(player.getLocation()))).getTown().getMembers().contains(name)) {
             player.sendMessage(ChatColor.GOLD + "[SimpleZones] " + name + " is not a member of your town.");
         } else if (!OwnedLand.getLandAtPoint(player.getLocation()).removeMember(name)) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] " + name + " is not a member of this plot.");
         } else {
             player.sendMessage(ChatColor.GOLD + "[SimpleZones] " + name + " has been removed from this plot.");
         }
         return true;
     }
 
     public boolean setOwner(String name) {
         if(town == null || !town.getOwner().equals(getName())) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You are not the owner of a town.");
         } else if(!town.getMembers().contains(name)) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] " + name + " is not a member of " + town.getName());
         } else {
             town.setOwner(name);
             player.sendMessage(ChatColor.GOLD + "[SimpleZones] You are no longer the owner of " + town.getName());
             ZonePlayer zp = ZonePlayer.findUser(name);
             if(!zp.getPlayer().isOnline()) {
                 Mail notif = new Mail("You have been given ownership of " + town.getName() + " by " + getName(), false, this);
                 zp.sendMail(notif);
             } else {
                 zp.getPlayer().sendMessage(ChatColor.GOLD + "[SimpleZones] You are now the owner of " + town.getName());
             }
         }
         return true;
     }
 
     public boolean invite(String name) {
         ZonePlayer zp = ZonePlayer.findUser(name);
         if(zp == null) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] There is no player named " + name);
         } else if(zp.getTown() != null) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] " + name + " is already in a town.");
         } else if(town == null || !town.getOwner().equals(getName())) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You do not own a town.");
         } else {
             zp.setTown(town);
             player.sendMessage(ChatColor.GOLD + "[SimpleZones] You have added " + name + " to your town.");
             if(zp.getPlayer() != null && zp.getPlayer().isOnline()) {
                 zp.getPlayer().sendMessage(ChatColor.GOLD + "[SimpleZones] You have been added to " + town.getName());
             } else {
                 Mail notif = new Mail("You have been added to " + town.getName(), false, this);
                 zp.sendMail(notif);
             }
             town.addMember(name);
         }
         return true;
     }
 
     public boolean ban(String name) {
         ZonePlayer zp = ZonePlayer.findUser(name);
         if(zp == null) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] There is no player named " + name);
         } else if(town == null || !town.getOwner().equals(getName())) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You are not the owner of a town.");
         } else if(!town.addBan(zp)) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] " + name + " is already banned from " + town.getName());
         } else {
             zp.setTown(null);
             if(zp.getPlayer() != null && zp.getPlayer().isOnline()) {
                 zp.getPlayer().sendMessage(ChatColor.GOLD + "[SimpleZones] You have been banned from " + town.getName());
             } else {
                 Mail notif = new Mail("You have been banned from " + town.getName(), false, this);
                 zp.sendMail(notif);
             }
             player.sendMessage(ChatColor.GOLD + "[SimpleZones] " + name + " has been banned from " + town.getName());
         }
         return true;
     }
 
     public boolean unban(String name) {
         ZonePlayer zp = ZonePlayer.findUser(name);
         if(zp == null) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] There is no player named " + name);
         } else if(town == null || !town.getOwner().equals(getName())) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You are not the owner of a town.");
         } else if(!town.unban(zp)) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] " + name + " isn't currently banned in " + town.getName());
         } else {
             if(zp.getPlayer() != null && zp.getPlayer().isOnline()) {
                 zp.getPlayer().sendMessage(ChatColor.GOLD + "[SimpleZones] You have been unbanned from " + town.getName());
             } else {
                 Mail notif = new Mail("You have been unbanned from " + town.getName(), false, this);
                 zp.sendMail(notif);
             }
             player.sendMessage(ChatColor.GOLD + "[SimpleZones] " + name + " has been unbanned from " + town.getName());
         }
         return true;
     }
 
     public boolean setWarp() {
         OwnedLand land = OwnedLand.getLandAtPoint(player.getLocation());
         if(land == null || town == null) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You are not in a town.");
             return true;
         }
         String owner;
         if(land instanceof Town)
             owner = ((Town)land).getOwner();
         else owner = ((Plot)land).getTown().getOwner();
         if(owner.equals(name)) {
             town.setWarp(player.getLocation());
             player.sendMessage(ChatColor.GOLD + "[SimpleZones] You have set the warp point for " + town.getName() + " to your location.");
         } else player.sendMessage(ChatColor.RED + "[SimpleZones] You don't own this land.");
         return true;
     }
 
     public boolean delete() {
         if(town == null || !town.getOwner().equals(name)) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You do not own a town.");
         } else {
             OwnedLand.stripLocations(town);
             for(String s : town.getMembers()) {
                 ZonePlayer zp = ZonePlayer.findUser(s);
                 if(zp == null)
                     continue;
                 zp.sendMail(new Mail("The town \"" + town.getName() + "\" has been deleted.", false, this));
                 zp.setTown(null);
             }
             Town.getTownList().remove(town.getName());
             player.sendMessage(ChatColor.GOLD + "[SimpleZones] You have deleted " + town.getName());
             town = null;
         }
         return true;
     }
 
     public boolean join(String s) {
         if(town != null) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You are already a member of a town.");
         } else if(Town.getTown(s) == null) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] There is no town named " + s);
         } else if(Town.getTown(s).getBans().contains(this)) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You are banned from " + s);
         } else {
             ZonePlayer owner = ZonePlayer.findUser(Town.getTown(s).getOwner());
             owner.sendMail(new Mail(name + " would like to join " + Town.getTown(s).getName(), false, this));
             player.sendMessage(ChatColor.GOLD + "[SimpleZones] The owner of " + s + " has been notified of your request.");
         }
         return true;
     }
 
     public boolean quit() {
         if(town == null) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You are not the member of a town.");
         } else if(town.getOwner().equals(name)) {
             player.sendMessage(ChatColor.RED + "[SimpleZones] You cannot quit if you own the town.");
         } else {
             town.removeMember(name);
             for(Plot p : town.getPlots())
                 p.removeMember(name);
             player.sendMessage(ChatColor.GOLD + "[SimpleZones] You have been removed from " + town.getName());
             town = null;
         }
         return true;
     }
 }
