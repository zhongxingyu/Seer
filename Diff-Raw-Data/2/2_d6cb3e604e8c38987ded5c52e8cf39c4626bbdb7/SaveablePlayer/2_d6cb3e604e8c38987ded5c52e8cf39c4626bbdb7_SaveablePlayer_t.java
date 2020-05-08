 package com.undeadscythes.udsplugin;
 
 import com.undeadscythes.udsplugin.eventhandlers.AsyncPlayerChat.Channel;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.util.logging.*;
 import org.apache.commons.lang.*;
 import org.bukkit.*;
 import org.bukkit.block.*;
 import org.bukkit.conversations.*;
 import org.bukkit.entity.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.inventory.InventoryView.Property;
 import org.bukkit.inventory.*;
 import org.bukkit.map.*;
 import org.bukkit.metadata.*;
 import org.bukkit.permissions.*;
 import org.bukkit.plugin.*;
 import org.bukkit.potion.*;
 import org.bukkit.util.Vector;
 
 /**
  * An extension of Minecraft players adding various fields and methods.
  * @author UndeadScythes
  */
 public class SaveablePlayer implements Saveable, Player {
     /**
      * A player rank granting permission.
      * @author UndeadScythes
      */
     public enum PlayerRank {
         /**
          * Basic player rank.
          */
         DEFAULT(ChatColor.WHITE, 0),
         /**
          * Player with build rights.
          */
         MEMBER(ChatColor.GREEN, 1),
         /**
          * Donating or long term player.
          */
         VIP(ChatColor.DARK_PURPLE, 1),
         /**
          * Trustee.
          */
         WARDEN(ChatColor.AQUA, 2),
         /**
          * Player moderator.
          */
         MOD(ChatColor.DARK_AQUA, 3),
         /**
          * Server administrator.
          */
         ADMIN(ChatColor.YELLOW, 4),
         /**
          * Server owner.
          */
         OWNER(ChatColor.GOLD, 5),
         /**
          * Special rank for normally owned regions.
          */
         NONE(null, 5);
 
         private ChatColor color;
         private int ranking;
 
         PlayerRank(ChatColor color, int rank) {
             this.color = color;
             this.ranking = rank;
         }
 
         public ChatColor color() {
             return color;
         }
 
         public static PlayerRank getByRanking(int ranking) {
             for(PlayerRank rank : values()) {
                 if(rank.ranking == ranking) {
                     return rank;
                 }
             }
             return null;
         }
 
         public static PlayerRank getAbove(PlayerRank rank) {
             return getByRanking(rank.ranking + 1);
         }
 
         public static PlayerRank getBelow(PlayerRank rank) {
             return getByRanking(rank.ranking - 1);
         }
 
         public static PlayerRank getByName(String string) {
             for(PlayerRank rank : values()) {
                 if(rank.name().equals(string.toUpperCase())) {
                     return rank;
                 }
             }
             return null;
         }
 
         @Override
         public String toString() {
             return name().toLowerCase().replaceFirst("[a-z]", name().substring(0, 1).toUpperCase());
         }
     }
 
     /**
      * File name of player file.
      */
     public static String PATH = "players.csv";
     /**
      * Current record version.
      */
     public static int VERSION = 1;
 
     private String name;
     private Player base;
     private int bounty = 0;
     private int money = 0;
     private PlayerRank rank = PlayerRank.DEFAULT;
     private long vipTime = 0;
     private int vipSpawns = 0;
     private long jailTime = 0;
     private long jailSentence = 0;
     private int bail = 0;
     private String nick;
     private long timeLogged = 0;
     private Location back = null;
     private boolean godMode = false;
     private boolean lockdownPass = false;
     private long lastDamageCaused = 0;
     private SaveablePlayer challenger = null;
     private int wager = 0;
     private long prizeClaim = 0;
     private Location checkPoint = null;
     private ChatRoom chatRoom = null;
     private HashSet<String> ignoredPlayers = new HashSet<String>();
     private Channel channel = Channel.PUBLIC;
     private LinkedList<Long> lastChats = new LinkedList<Long>();
     private ItemStack[] inventoryCopy = null;
     private ItemStack[] armorCopy = null;
     private UUID selectedPet = null;
     private SaveablePlayer whisperer = null;
     private int powertoolID = 0;
     private String powertoolCmd = "";
     private Clan clan = null;
 
     /**
      * Initialise a brand new player extension.
      * @param player Player to connect to this extension.
      */
     public SaveablePlayer(Player player) {
         this.base = player;
         nick = player.getName();
         name = player.getName();
     }
 
     /**
      * Initialise an extended player from a string record.
      * @param record A line from a save file.
      */
     public SaveablePlayer(String record) {
         String[] recordSplit = record.split("\t");
         name = recordSplit[0];
         bounty = Integer.parseInt(recordSplit[1]);
         money = Integer.parseInt(recordSplit[2]);
         rank = PlayerRank.getByName(recordSplit[3]);
         vipTime = Long.parseLong(recordSplit[4]);
         vipSpawns = Integer.parseInt(recordSplit[5]);
         jailTime = Long.parseLong(recordSplit[6]);
         jailSentence = Long.parseLong(recordSplit[7]);
         bail = Integer.parseInt(recordSplit[8]);
         nick = recordSplit[9];
         timeLogged = Long.parseLong(recordSplit[10]);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public String getRecord() {
         ArrayList<String> record = new ArrayList<String>();
         record.add(name);
         record.add(bounty + "");
         record.add(money + "");
         record.add(rank.toString());
         record.add(vipTime + "");
         record.add(vipSpawns + "");
         record.add(jailTime + "");
         record.add(jailSentence + "");
         record.add(bail + "");
         record.add(nick);
         record.add(timeLogged + "");
         return StringUtils.join(record.toArray(), "\t");
     }
 
     @Override
     public String toString() {
         return name;
     }
 
     /**
      * Warp an existing player with these extensions.
      * @param player Player to wrap.
      */
     public void wrapPlayer(Player player) {
         this.base = player;
         player.setDisplayName(nick);
     }
 
     public void addTime(long time) {
         timeLogged += time;
     }
 
     public void toggleLockdownPass() {
         lockdownPass ^= true;
     }
 
     public UUID getSelectedPet() {
         return selectedPet;
     }
 
     public void selectPet(UUID id) {
         selectedPet = id;
     }
 
     public void setWhisperer(SaveablePlayer player) {
         whisperer = player;
     }
 
     public int getWager() {
         return wager;
     }
 
     public boolean hasScuba() {
         return getInventory().getHelmet().getType().equals(Material.GLASS);
     }
 
     public SaveablePlayer getWhisperer() {
         return whisperer;
     }
 
     public int countItems(ItemStack search) {
         final ItemStack[] inventory = getInventory().getContents();
         int count = 0;
         for(int i = 0; i < inventory.length; i++) {
             ItemStack item = inventory[i];
             if(item != null && item.getType() == search.getType() && item.getData().getData() == search.getData().getData()) {
                 count += item.getAmount();
             }
         }
         return count;
     }
 
     public int getPowertoolID() {
         return powertoolID;
     }
 
     public String getPowertool() {
         return powertoolCmd;
     }
 
     public void claimPrize() {
         prizeClaim = System.currentTimeMillis();
     }
 
     public boolean hasClaimedPrize() {
         return (prizeClaim + Timer.DAY > System.currentTimeMillis());
     }
 
     public Session forceSession() {
         Session session;
         if((session = UDSPlugin.getSessions().get(getName())) == null) {
             session = new Session(this);
             UDSPlugin.getSessions().put(getName(), session);
         }
         return session;
     }
 
     public void setPowertoolID(int ID) {
         powertoolID = ID;
     }
 
     public void setPowertool(String cmd) {
         powertoolCmd = cmd;
     }
 
     /**
      * Get the players saved inventory.
      * @return The players saved inventory, <code>null</code> if none exists.
      */
     public ItemStack[] getInventoryCopy() {
         return inventoryCopy;
     }
 
     public boolean isInShop(Location location) {
         for(Region region : UDSPlugin.getShops().values()) {
             if(location.toVector().isInAABB(region.getV1(), region.getV2())) {
                 return true;
             }
         }
         return false;
     }
 
     public void setClan(Clan clan) {
         this.clan = clan;
     }
 
     /**
      * Save this players inventory for later retrieval.
      */
     public void saveInventory() {
         inventoryCopy = getInventory().getContents();
     }
 
     public void saveItems() {
         saveInventory();
         saveArmor();
     }
 
     /**
      * Load this players armor.
      */
     public void loadArmor() {
         getInventory().setArmorContents(armorCopy);
         armorCopy = null;
     }
 
     public void endChallenge() {
         wager = 0;
         challenger = null;
     }
 
     /**
      * Load this players inventory.
      */
     public void loadInventory() {
         getInventory().setContents(inventoryCopy);
         inventoryCopy = null;
     }
 
     /**
      * Save this players armor for later retrieval.
      */
     public void saveArmor() {
         armorCopy = getInventory().getArmorContents();
     }
 
     /**
      * Get this players bail.
      * @return This players bail.
      */
     public int getBail() {
         return bail;
     }
 
     public void loadItems() {
         loadInventory();
         loadArmor();
     }
 
     /**
      * Update the chat times with a new value.
      * @return <code>true</code> if chat events are not occurring too frequently, <code>false</code> otherwise.
      */
     public boolean newChat() {
         if(lastChats.size() > 5) {
             lastChats.removeFirst();
         }
         lastChats.offerLast(System.currentTimeMillis());
         if(lastChats.size() == 5 && lastChats.getLast() - lastChats.getFirst() < 3000) {
             return false;
         }
         return true;
     }
 
     public String getTimeLogged() {
         return timeToString(timeLogged);
     }
 
     public String getLastSeen() {
         return timeToString(System.currentTimeMillis() - getLastPlayed());
     }
 
     /**
      * Construct an english reading string of the remaining VIP time.
      * @return English reading string.
      */
     public String getVIPTimeString() {
         return timeToString(Config.VIP_TIME - System.currentTimeMillis() - getVIPTime());
     }
 
     public String timeToString(long time) {
         String timeString = "";
         while(time >= Timer.SECOND) {
             if(time >= Timer.DAY) {
                 int days = (int)(time / Timer.DAY);
                 timeString = timeString.concat(days + (days == 1 ? " day" : " days"));
                 time -= days * Timer.DAY;
             } else if(time >= Timer.HOUR) {
                 int hours = (int)(time / Timer.HOUR);
                 timeString = timeString.concat(hours + (hours == 1 ? " hour" : " hours"));
                 time -= hours * Timer.HOUR;
             } else if(time >= Timer.MINUTE) {
                 int minutes = (int)(time / Timer.MINUTE);
                 timeString = timeString.concat(minutes + (minutes == 1 ? " minute" : " minutes"));
                 time -= minutes * Timer.MINUTE;
             } else if(time >= Timer.SECOND) {
                 int seconds = (int)(time / Timer.SECOND);
                 timeString = timeString.concat(seconds + (seconds == 1 ? " second" : " seconds"));
                 time -= seconds * Timer.SECOND;
             }
         }
         return timeString;
     }
 
     /**
      * Use up some the players VIP daily item spawns.
      * @param amount The amount of spawns to use up.
      * @return The number of spawns remaining.
      */
     public int useVIPSpawns(int amount) {
         vipSpawns -= amount;
         return vipSpawns;
     }
 
     /**
      * Set the players back warp point to their current location.
      */
     public void setBackPoint() {
         back = getLocation();
     }
 
     public void setBackPoint(Location location) {
         back = location;
     }
 
     /**
      * Toggle this players game mode.
      * @return <code>true</code> if the players game mode is now set to creative, <code>false</code> otherwise.
      */
     public boolean toggleGameMode() {
         if(getGameMode().equals(GameMode.SURVIVAL)) {
             setGameMode(GameMode.CREATIVE);
             return true;
         } else {
             setGameMode(GameMode.SURVIVAL);
             return false;
         }
     }
 
     /**
      * Get a players monetary value.
      * @return A players monetary value.
      */
     public int getMoney() {
         return money;
     }
 
     /**
      * Get the chat room this player is currently in.
      * @return The players chat room.
      */
     public ChatRoom getChatRoom() {
         return chatRoom;
     }
 
     /**
      * Take a player out of jail and perform all the necessary operations.
      * @throws IOException
      */
     public void release() {
         jailTime = 0;
         jailSentence = 0;
         if(!quietTeleport(UDSPlugin.getWarps().get("jailout"))) {
             BufferedWriter out;
             try {
                 out = new BufferedWriter(new FileWriter(UDSPlugin.TICKET_PATH, true));
                 out.write("No jail out warp point has been placed. Use '/setwarp jailout' to do this.");
                 out.close();
             } catch (IOException ex) {
                 Logger.getLogger(SaveablePlayer.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     /**
      * Put a player in jail and perform all the necessary operations.
      * @param sentence Time in minutes to jail the player.
      * @param bail Bail to set.
      */
     public void jail(long sentence, int bail) throws IOException {
         getWorld().strikeLightningEffect(getLocation());
         if(!quietTeleport(UDSPlugin.getWarps().get("jailin"))) {
             BufferedWriter out = new BufferedWriter(new FileWriter(UDSPlugin.TICKET_PATH, true));
             out.write("No jail in warp point has been placed. Use '/setwarp jailin' to do this.");
             out.close();
         }
         jailTime = System.currentTimeMillis();
         jailSentence = sentence * Timer.MINUTE;
         this.bail = bail;
         sendMessage(Color.MESSAGE + "You have been jailed for " + sentence + " minutes.");
         if(bail != 0) {
             sendMessage(Color.MESSAGE + "If you can afford it, use /paybail to get out early for " + bail + " " + Config.CURRENCIES + ".");
         }
     }
 
     /**
      * Get the first region that the player is currently inside.
      * @param type Optional type of region to check, <code>null</code> to search all regions.
      * @return The first region the player is in, <code>null</code> otherwise.
      */
     public Region getCurrentRegion(Region.RegionType type) {
         if(type == Region.RegionType.CITY) {
             for(Region region : UDSPlugin.getCities().values()) {
                 if(getLocation().toVector().isInAABB(region.getV1(), region.getV2())) {
                     return region;
                 }
             }
         } else if(type == Region.RegionType.SHOP) {
             for(Region region : UDSPlugin.getShops().values()) {
                 if(getLocation().toVector().isInAABB(region.getV1(), region.getV2())) {
                     return region;
                 }
             }
         }
         return null;
     }
 
     /**
      * Demote a player.
      * @return Players new rank, <code>null</code> if no change.
      */
     public PlayerRank demote() {
         PlayerRank newRank = PlayerRank.getBelow(rank);
         if(newRank != null) {
             rank = newRank;
         }
         return newRank;
     }
 
     /**
      * Promote a player.
      * @return Players new rank, <code>null</code> if no change.
      */
     public PlayerRank promote() {
         PlayerRank newRank = PlayerRank.getAbove(rank);
         if(newRank != null) {
             rank = newRank;
         }
         return newRank;
     }
 
     /**
      * Get the players current chat channel.
      * @return The players chat channel.
      */
     public Channel getChannel() {
         return channel;
     }
 
     /**
      * Get this players saved checkpoint.
      * @return This players checkpoint.
      */
     public Location getCheckPoint() {
         return checkPoint;
     }
 
     public void setCheckPoint(Location location) {
         checkPoint = location;
     }
 
     /**
      * Check if a player is engaged in a duel with another player.
      * @return <code>true</code> if the player is engaged in a duel, <code>false</code> otherwise.
      */
     public boolean isDuelling() {
         return challenger != null;
     }
 
     public void setChallenger(SaveablePlayer challenger) {
         this.challenger = challenger;
     }
 
     public SaveablePlayer getChallenger() {
         return challenger;
     }
 
     /**
      * Check if a player can build in a given location.
      * @param player
      * @return
      */
     public boolean canBuildHere(Location location) {
         boolean contained = false;
         for(Region region : UDSPlugin.getRegions().values()) {
             if(location.toVector().isInAABB(region.getV1(), region.getV2())) {
                if(((region.getRank() != null && rank.compareTo(region.getRank()) >= 0)) || region.isOwner(this) || region.hasMember(this)) {
                     return true;
                 }
                 contained = true;
             }
         }
         return !contained;
     }
 
     /**
      * Give a player an item stack, any items that don't fit in the inventory are dropped at the players feet.
      * @param item Item to give the player.
      */
     public void giveAndDrop(ItemStack item) {
         HashMap<Integer, ItemStack> drops = getInventory().addItem(item);
         for(ItemStack drop : drops.values()) {
             getWorld().dropItemNaturally(getLocation(), drop);
         }
     }
 
     /**
      * Add a player to a list of players this player is ignoring.
      * @param player Player to ignore.
      * @return <code>true</code> if this player was not already being ignored, <code>false</code> otherwise.
      */
     public boolean ignorePlayer(Player player) {
         return ignoredPlayers.add(player.getName());
     }
 
     /**
      * Remove a player from a list of players this player is ignoring.
      * @param player Player to stop ignoring.
      * @return <code>true</code> if this player was being ignored, <code>false</code> otherwise.
      */
     public boolean unignorePlayer(Player player) {
         return ignoredPlayers.remove(player.getName());
     }
 
     /**
      * Check to see if this player is ignoring a player.
      * @param player Player to check.
      * @return <code>true</code> if this player is ignoring that player, <code>false</code> otherwise.
      */
     public boolean isIgnoringPlayer(Player player) {
         return ignoredPlayers.contains(player.getName());
     }
 
     /**
      * Get the last time this player caused damage to another player.
      * @return The last time this player did damage to another player.
      */
     public long getLastDamageCaused() {
         return lastDamageCaused;
     }
 
     /**
      * Set the last time this player caused damage to another player to now.
      */
     public void setLastDamageCaused() {
         lastDamageCaused = System.currentTimeMillis();
     }
 
     /**
      * Get this players total bounty.
      * @return Players bounty.
      */
     public int getBounty() {
         return bounty;
     }
 
     /**
      * Set the bounty on a player.
      * @param bounty Bounty to set.
      */
     public void setBounty(int bounty) {
         this.bounty = bounty;
     }
 
     /**
      * Add a bounty to this player.
      * @param bounty New bounty.
      */
     public void addBounty(int bounty) {
         this.bounty += bounty;
     }
 
     /**
      * Get the last recorded location of the player.
      * @return The last recorded location of the player.
      */
     public Location getBack() {
         return back;
     }
 
     /**
      * Get a player current rank.
      * @return Player rank.
      */
     public PlayerRank getRank() {
         return rank;
     }
 
     /**
      * Set the player rank.
      * @param rank The rank to set.
      */
     public void setRank(PlayerRank rank) {
         this.rank = rank;
     }
 
     /**
      * Check to see if a player belongs to a clan.
      * @return <code>true</code> if a player is in a clan, <code>false</code> otherwise.
      */
     public boolean isInClan() {
         return clan != null;
     }
 
     /**
      * Teleport to the point indicated by a warp.
      * @param warp Warp to teleport to.
      */
     public void teleport(Warp warp) {
         teleport(warp.getLocation());
     }
 
     /**
      * Get the name of the clan the player is a member of.
      * @return Clan name.
      */
     public Clan getClan() {
         return clan;
     }
 
     /**
      * Storage of nick name kept in extension for offline access.
      * @return Player nick name.
      */
     @Override
     public String getDisplayName() {
         return nick;
     }
 
     /**
      * Toggle the players chat channel.
      * @param channel Channel to toggle.
      * @return <code>true</code> if channel was toggled on, <code>false</code> if channel switched back to public.
      */
     public boolean toggleChannel(Channel channel) {
         if(this.channel.equals(channel)) {
             this.channel = Channel.PUBLIC;
             return false;
         } else {
             this.channel = channel;
             return true;
         }
     }
 
     /**
      * Check whether this player has a lockdown pass.
      * @return Has player got lockdown pass.
      */
     public boolean hasLockdownPass() {
         return lockdownPass;
     }
 
     /**
      * Get the time when this player rented VIP status.
      * @return When VIP was rented.
      */
     public long getVIPTime() {
         return vipTime;
     }
 
     /**
      * Set when a player rented VIP status.
      * @param time Time to set.
      */
     public void setVIPTime(long time) {
         vipTime = time;
     }
 
     /**
      * Get the number of free item spawns this player has left.
      * @return Number of spawns left.
      */
     public int getVIPSpawns() {
         return vipSpawns;
     }
 
     /**
      * Set the number of free VIP item spawns a player has remaining.
      * @param spawns Number of spawns to set.
      */
     public void setVIPSpawns(int spawns) {
         vipSpawns = spawns;
     }
 
     /**
      * Get the time that this player was put in jail.
      * @return Players jail time.
      */
     public long getJailTime() {
         return jailTime;
     }
 
     /**
      * Set when a player was put in jail.
      * @param time Jail time.
      */
     public void setJailTime(long time) {
         jailTime = time;
     }
 
     /**
      * Get how long this player was sentenced to jail for.
      * @return Players sentence.
      */
     public long getJailSentence() {
         return jailSentence;
     }
 
     /**
      * Set the length of a players jail sentence.
      * @param sentence Length of sentence.
      */
     public void setJailSentence(long sentence) {
         jailSentence = sentence;
     }
 
     /**
      * Check if a player is currently in jail.
      * @return <code>true</code> if a player is in jail, <code>false</code> otherwise.
      */
     public boolean isJailed() {
         return (jailTime > 0);
     }
 
     /**
      * Check if a player currently has god mode enabled.
      * @return God mode setting.
      */
     public boolean hasGodMode() {
         return godMode;
     }
 
     /**
      * Toggle a players god mode.
      * @return Players current god mode setting.
      */
     public boolean toggleGodMode() {
         godMode ^= true;
         return godMode;
     }
 
     /**
      * Set a players god mode.
      * @param mode God mode setting.
      */
     public void setGodMode(boolean mode) {
         godMode = mode;
     }
 
     /**
      * Send a message in a particular channel.
      * @param channel Channel to send message in.
      * @param message Message to send.
      */
     public void chat(Channel channel, String message) {
         Channel temp = this.channel;
         this.channel = channel;
         chat(message);
         this.channel = temp;
     }
 
     /**
      * Check if a player can afford to pay some value.
      * @param price Price to pay.
      * @return <code>true</code> if player has enough money, <code>false</code> otherwise.
      */
     public boolean canAfford(int price) {
         return (money >= price);
     }
 
     public void setWager(int wager) {
         this.wager = wager;
     }
 
     /**
      * Debit the players account the amount passed.
      * @param amount Amount to debit.
      */
     public void debit(int amount) {
         money -= amount;
     }
 
     /**
      * Credit the players account the amount passed.
      * @param amount Amount to credit.
      */
     public void credit(int amount) {
         money += amount;
     }
 
     /**
      * Set a players account.
      * @param amount Amount to set.
      */
     public void setMoney(int amount) {
         money = amount;
     }
 
     /**
      * Check if a player has a rank.
      * @param rank Rank to check.
      * @return <code>true</code> if player has rank, <code>false</code> otherwise.
      */
     public boolean hasRank(PlayerRank rank) {
         if(this.rank.compareTo(rank) >= 0) {
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Teleport a player but reserve pitch and yaw of player.
      * @param location Location to teleport to.
      */
     public void move(Location location) {
         Location destination = location;
         destination.setPitch(getLocation().getPitch());
         destination.setYaw(getLocation().getYaw());
         teleport(destination);
     }
 
     /**
      * Teleport a player but fail quietly if location is <code>null</code>.
      * @param location Location to teleport player to.
      * @return <code>true</code> if location is not <code>null</code>, <code>false</code> otherwise.
      */
     public boolean quietTeleport(Location location) {
         if(location == null) {
             return false;
         } else {
             base.teleport(location);
             return true;
         }
     }
 
     /**
      * Teleport a player but fail quietly if warp is <code>null</code>.
      * @param warp Warp to teleport player to.
      * @return <code>true</code> if warp is not <code>null</code>, <code>false</code> otherwise.
      */
     public boolean quietTeleport(Warp warp) {
         if(warp == null) {
             return false;
         } else {
             base.teleport(warp.getLocation());
             return true;
         }
     }
 
     /**
      * Check if this player has a permission.
      * @param perm The permission to check.
      * @return <code>true</code> if the player has the permission, <code>false</code> otherwise.
      */
     public boolean hasPermission(Perm perm) {
         if(perm.hereditary()) {
             return perm.getRank().compareTo(rank) <= 0;
         } else {
             return perm.getRank().equals(rank);
         }
     }
 
     /**
      * Storage of nick name kept in extension for offline access.
      * @param name Nick name to set.
      */
     @Override
     public void setDisplayName(String name) {
         nick = name;
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public String getPlayerListName() {
         return base.getPlayerListName();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setPlayerListName(String name) {
         base.setPlayerListName(name);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setCompassTarget(Location loc) {
         base.setCompassTarget(loc);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Location getCompassTarget() {
         return base.getCompassTarget();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public InetSocketAddress getAddress() {
         return base.getAddress();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void sendRawMessage(String message) {
         base.sendRawMessage(message);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void kickPlayer(String message) {
         base.kickPlayer(message);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void chat(String msg) {
         base.chat(msg);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean performCommand(String command) {
         return base.performCommand(command);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean isSneaking() {
         return base.isSneaking();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setSneaking(boolean sneak) {
         base.setSneaking(sneak);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean isSprinting() {
         return base.isSprinting();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setSprinting(boolean sprinting) {
         base.setSprinting(sprinting);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void saveData() {
         base.saveData();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void loadData() {
         base.loadData();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setSleepingIgnored(boolean isSleeping) {
         base.setSleepingIgnored(isSleeping);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean isSleepingIgnored() {
         return base.isSleepingIgnored();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void playNote(Location loc, byte instrument, byte note) {
         base.playNote(loc, instrument, note);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void playNote(Location loc, Instrument instrument, Note note) {
         base.playNote(loc, instrument, note);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void playSound(Location location, Sound sound, float volume, float pitch) {
         base.playSound(location, sound, volume, pitch);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void playEffect(Location loc, Effect effect, int data) {
         base.playEffect(loc, effect, data);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public <T> void playEffect(Location loc, Effect effect, T data) {
         base.playEffect(loc, effect, data);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void sendBlockChange(Location loc, Material material, byte data) {
         base.sendBlockChange(loc, material, data);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean sendChunkChange(Location loc, int sx, int sy, int sz, byte[] data) {
         return base.sendChunkChange(loc, sx, sy, sz, data);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void sendBlockChange(Location loc, int material, byte data) {
         base.sendBlockChange(loc, material, data);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void sendMap(MapView map) {
         base.sendMap(map);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void updateInventory() {
         base.updateInventory();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void awardAchievement(Achievement achievement) {
         base.awardAchievement(achievement);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void incrementStatistic(Statistic statistic) {
         base.incrementStatistic(statistic);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void incrementStatistic(Statistic statistic, int amount) {
         base.incrementStatistic(statistic, amount);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void incrementStatistic(Statistic statistic, Material material) {
         base.incrementStatistic(statistic, material);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void incrementStatistic(Statistic statistic, Material material, int amount) {
         base.incrementStatistic(statistic, material, amount);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setPlayerTime(long time, boolean relative) {
         base.setPlayerTime(time, relative);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public long getPlayerTime() {
         return base.getPlayerTime();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public long getPlayerTimeOffset() {
         return base.getPlayerTimeOffset();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean isPlayerTimeRelative() {
         return base.isPlayerTimeRelative();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void resetPlayerTime() {
         base.resetPlayerTime();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void giveExp(int amount) {
         base.giveExp(amount);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public float getExp() {
         return base.getExp();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setExp(float exp) {
         base.setExp(exp);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public int getLevel() {
         return base.getLevel();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setLevel(int level) {
         base.setLevel(level);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public int getTotalExperience() {
         return base.getTotalExperience();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setTotalExperience(int exp) {
         base.setTotalExperience(exp);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public float getExhaustion() {
         return base.getExhaustion();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setExhaustion(float value) {
         base.setExhaustion(value);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public float getSaturation() {
         return base.getSaturation();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setSaturation(float value) {
         base.setSaturation(value);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public int getFoodLevel() {
         return base.getFoodLevel();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setFoodLevel(int value) {
         base.setFoodLevel(value);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Location getBedSpawnLocation() {
         return base. getBedSpawnLocation();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setBedSpawnLocation(Location location) {
         base.setBedSpawnLocation(location);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean getAllowFlight() {
         return base.getAllowFlight();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setAllowFlight(boolean flight) {
         base.setAllowFlight(flight);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void hidePlayer(Player player) {
         base.hidePlayer(player);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void showPlayer(Player player) {
         base.showPlayer(player);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean canSee(Player player) {
         return base.canSee(player);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean isFlying() {
         return base.isFlying();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setFlying(boolean value) {
         base.setFlying(value);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setFlySpeed(float value) throws IllegalArgumentException {
         base.setFlySpeed(value);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setWalkSpeed(float value) throws IllegalArgumentException {
         base.setWalkSpeed(value);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public float getFlySpeed() {
         return base.getFlySpeed();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public float getWalkSpeed() {
         return base.getWalkSpeed();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public String getName() {
         return base == null ? name : base.getName();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public PlayerInventory getInventory() {
         return base.getInventory();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Inventory getEnderChest() {
         return base. getEnderChest();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean setWindowProperty(Property prop, int value) {
         return base.setWindowProperty(prop, value);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public InventoryView getOpenInventory() {
         return base.getOpenInventory();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public InventoryView openInventory(Inventory inventory) {
         return base.openInventory(inventory);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public InventoryView openWorkbench(Location location, boolean force) {
         return base.openWorkbench(location, force);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public InventoryView openEnchanting(Location location, boolean force) {
         return base.openEnchanting(location, force);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void openInventory(InventoryView inventory) {
         base.openInventory(inventory);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void closeInventory() {
         base.closeInventory();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public ItemStack getItemInHand() {
         return base.getItemInHand();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setItemInHand(ItemStack item) {
         base.setItemInHand(item);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public ItemStack getItemOnCursor() {
         return base.getItemOnCursor();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setItemOnCursor(ItemStack item) {
         base.setItemOnCursor(item);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean isSleeping() {
         return base.isSleeping();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public int getSleepTicks() {
         return base.getSleepTicks();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public GameMode getGameMode() {
         return base.getGameMode();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setGameMode(GameMode mode) {
         base.setGameMode(mode);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean isBlocking() {
         return base.isBlocking();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public int getExpToLevel() {
         return base.getExpToLevel();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public int getHealth() {
         return base.getHealth();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setHealth(int health) {
         base.setHealth(health);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public int getMaxHealth() {
         return base.getMaxHealth();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public double getEyeHeight() {
         return base.getEyeHeight();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public double getEyeHeight(boolean ignoreSneaking) {
         return base.getEyeHeight(ignoreSneaking);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Location getEyeLocation() {
         return base. getEyeLocation();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public List<Block> getLineOfSight(HashSet<Byte> transparent, int maxDistance) {
         return base.getLineOfSight(transparent, maxDistance);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Block getTargetBlock(HashSet<Byte> transparent, int maxDistance) {
         return base. getTargetBlock(transparent, maxDistance);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public List<Block> getLastTwoTargetBlocks(HashSet<Byte> transparent, int maxDistance) {
         return base.getLastTwoTargetBlocks(transparent, maxDistance);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Egg throwEgg() {
         return base. throwEgg();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Snowball throwSnowball() {
         return base. throwSnowball();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Arrow shootArrow() {
         return base. shootArrow();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public <T extends Projectile> T launchProjectile(Class<? extends T> projectile) {
         return base.launchProjectile(projectile);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public int getRemainingAir() {
         return base.getRemainingAir();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setRemainingAir(int ticks) {
         base.setRemainingAir(ticks);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public int getMaximumAir() {
         return base.getMaximumAir();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setMaximumAir(int ticks) {
         base.setMaximumAir(ticks);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void damage(int amount) {
         base.damage(amount);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void damage(int amount, Entity source) {
         base.damage(amount, source);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public int getMaximumNoDamageTicks() {
         return base.getMaximumNoDamageTicks();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setMaximumNoDamageTicks(int ticks) {
         base.setMaximumNoDamageTicks(ticks);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public int getLastDamage() {
         return base.getLastDamage();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setLastDamage(int damage) {
         base.setLastDamage(damage);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public int getNoDamageTicks() {
         return base.getNoDamageTicks();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setNoDamageTicks(int ticks) {
         base.setNoDamageTicks(ticks);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Player getKiller() {
         return base. getKiller();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean addPotionEffect(PotionEffect effect) {
         return base.addPotionEffect(effect);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean addPotionEffect(PotionEffect effect, boolean force) {
         return base.addPotionEffect(effect, force);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean addPotionEffects(Collection<PotionEffect> effects) {
         return base.addPotionEffects(effects);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean hasPotionEffect(PotionEffectType type) {
         return base.hasPotionEffect(type);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void removePotionEffect(PotionEffectType type) {
         base.removePotionEffect(type);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Collection<PotionEffect> getActivePotionEffects() {
         return base.getActivePotionEffects();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean hasLineOfSight(Entity other) {
         return base.hasLineOfSight(other);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Location getLocation() {
         return base. getLocation();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setVelocity(Vector velocity) {
         base.setVelocity(velocity);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Vector getVelocity() {
         return base. getVelocity();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public World getWorld() {
         return base. getWorld();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean teleport(Location location) {
         return base.teleport(location);
     }
 
     /**
      * @inheritDoc
      * @Override
      */
     @Override
     public boolean teleport(Location location, TeleportCause cause) {
         return base.teleport(location, cause);
     }
 
     /**
      *
      * @inheritDoc
      */
     @Override
     public boolean teleport(Entity destination) {
         return base.teleport(destination);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean teleport(Entity destination, TeleportCause cause) {
         return base.teleport(destination, cause);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public List<Entity> getNearbyEntities(double x, double y, double z) {
         return base.getNearbyEntities(x, y, z);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public int getEntityId() {
         return base.getEntityId();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public int getFireTicks() {
         return base.getFireTicks();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public int getMaxFireTicks() {
         return base.getMaxFireTicks();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setFireTicks(int ticks) {
         base.setFireTicks(ticks);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void remove() {
         base.remove();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean isDead() {
         return base.isDead();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean isValid() {
         return base.isValid();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Server getServer() {
         return base. getServer();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Entity getPassenger() {
         return base. getPassenger();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean setPassenger(Entity passenger) {
         return base.setPassenger(passenger);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean isEmpty() {
         return base.isEmpty();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean eject() {
         return base.eject();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public float getFallDistance() {
         return base.getFallDistance();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setFallDistance(float distance) {
         base.setFallDistance(distance);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setLastDamageCause(EntityDamageEvent event) {
         base.setLastDamageCause(event);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public EntityDamageEvent getLastDamageCause() {
         return base.getLastDamageCause();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public UUID getUniqueId() {
         return base.getUniqueId();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public int getTicksLived() {
         return base.getTicksLived();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setTicksLived(int value) {
         base.setTicksLived(value);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void playEffect(EntityEffect type) {
         base.playEffect(type);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public EntityType getType() {
         return base.getType();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean isInsideVehicle() {
         return base.isInsideVehicle();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean leaveVehicle() {
         return base.leaveVehicle();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Entity getVehicle() {
         return base. getVehicle();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
         base.setMetadata(metadataKey, newMetadataValue);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public List<MetadataValue> getMetadata(String metadataKey) {
         return base.getMetadata(metadataKey);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean hasMetadata(String metadataKey) {
         return base.hasMetadata(metadataKey);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void removeMetadata(String metadataKey, Plugin owningPlugin) {
         base.removeMetadata(metadataKey, owningPlugin);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean isPermissionSet(String name) {
         return base.isPermissionSet(name);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean isPermissionSet(Permission perm) {
         return base.isPermissionSet(perm);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean hasPermission(String name) {
         return base.hasPermission(name);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean hasPermission(Permission perm) {
         return base.hasPermission(perm);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
         return base.addAttachment(plugin, name, value);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public PermissionAttachment addAttachment(Plugin plugin) {
         return base.addAttachment(plugin);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
         return base.addAttachment(plugin, name, value, ticks);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
         return base.addAttachment(plugin, ticks);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void removeAttachment(PermissionAttachment attachment) {
         base.removeAttachment(attachment);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void recalculatePermissions() {
         base.recalculatePermissions();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Set<PermissionAttachmentInfo> getEffectivePermissions() {
         return base.getEffectivePermissions();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean isOp() {
         return base.isOp();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setOp(boolean value) {
         base.setOp(value);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean isConversing() {
         return base.isConversing();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void acceptConversationInput(String input) {
         base.acceptConversationInput(input);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean beginConversation(Conversation conversation) {
         return base.beginConversation(conversation);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void abandonConversation(Conversation conversation) {
         base.abandonConversation(conversation);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {
         base.abandonConversation(conversation, details);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void sendMessage(String message) {
         base.sendMessage(message);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void sendMessage(String[] messages) {
         base.sendMessage(messages);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean isOnline() {
         return base != null ? base.isOnline() : false;
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean isBanned() {
         return base.isBanned();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setBanned(boolean banned) {
         base.setBanned(banned);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean isWhitelisted() {
         return base.isWhitelisted();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setWhitelisted(boolean value) {
         base.setWhitelisted(value);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Player getPlayer() {
         return base.getPlayer();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public long getFirstPlayed() {
         return base.getFirstPlayed();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public long getLastPlayed() {
         return base.getLastPlayed();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean hasPlayedBefore() {
         return base.hasPlayedBefore();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Map<String, Object> serialize() {
         return base.serialize();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void sendPluginMessage(Plugin source, String channel, byte[] message) {
         base.sendPluginMessage(source, channel, message);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Set<String> getListeningPluginChannels() {
         return base.getListeningPluginChannels();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setBedSpawnLocation(Location lctn, boolean bln) {
         base.setBedSpawnLocation(lctn, bln);
     }
 
     @Override
     public void giveExpLevels(int amount) {
         base.giveExpLevels(amount);
     }
 }
