 package com.undeadscythes.udsplugin;
 
 import java.util.*;
 import org.apache.commons.lang.*;
 import org.bukkit.*;
 import org.bukkit.block.*;
 import org.bukkit.entity.*;
 import org.bukkit.inventory.*;
 
 /**
  * An extension of Minecraft players adding various fields and methods.
  * @author UndeadScythes
  */
 public class SaveablePlayer implements Saveable {
     private String name;
     private Player player;
     private String nick;
 
     private boolean hasGodMode = false;
     private boolean hasLockdownPass = false;
     private boolean isAfk = false;
     private boolean mustLoadItems = false;
     private boolean isShopping = false;
     private boolean isBuying = false;
     private boolean isHidden = false;
     private int wager = 0;
     private int powertoolId = 0;
     private int bounty = 0;
     private int money = 0;
     private int vipSpawns = 0;
     private int bail = 0;
     private long vipTime = 0;
     private long timeJailed = 0;
     private long jailSentence = 0;
     private long timeLogged = 0;
     private long timeAttacked = 0;
     private long timePrizeClaimed = 0;
     private String powertoolCmd = "";
     private Location backPoint = null;
     private Location checkPoint = null;
     private SaveablePlayer challenger = null;
     private SaveablePlayer whisperer = null;
     private ChatRoom chatRoom = null; // Is this field never modified?
     private ChatChannel chatChannel = ChatChannel.PUBLIC;
     private UUID selectedPet = null;
     private Clan clan = null;
     private PlayerRank rank = PlayerRank.DEFAULT;
     private ItemStack[] inventoryCopy = null;
     private ItemStack[] armorCopy = null;
     private final Inventory shoppingCart = Bukkit.createInventory(null, 36);
     private final LinkedList<Long> lastChatTimes = new LinkedList<Long>();
     private final Set<SaveablePlayer> ignoredPlayers = new HashSet<SaveablePlayer>();
 
     /**
      *
      * @param player
      */
     public SaveablePlayer(final Player player) {
         this.player = player;
         nick = player.getName();
         name = player.getName();
     }
 
     /**
      * Initialise an extended player from a string record.
      * @param record A line from a save file.
      */
     public SaveablePlayer(final String record) {
         final String[] recordSplit = record.split("\t");
         name = recordSplit[0];
         bounty = Integer.parseInt(recordSplit[1]);
         money = Integer.parseInt(recordSplit[2]);
         rank = PlayerRank.getByName(recordSplit[3]);
         vipTime = Long.parseLong(recordSplit[4]);
         vipSpawns = Integer.parseInt(recordSplit[5]);
         timeJailed = Long.parseLong(recordSplit[6]);
         jailSentence = Long.parseLong(recordSplit[7]);
         bail = Integer.parseInt(recordSplit[8]);
         nick = recordSplit[9];
         timeLogged = Long.parseLong(recordSplit[10]);
     }
 
     @Override
     public final String getRecord() {
         final List<String> record = new ArrayList<String>();
         record.add(name);
         record.add(Integer.toString(bounty));
         record.add(Integer.toString(money));
         record.add(rank.toString());
         record.add(Long.toString(vipTime));
         record.add(Integer.toString(vipSpawns));
         record.add(Long.toString(timeJailed));
         record.add(Long.toString(jailSentence));
         record.add(Integer.toString(bail));
         record.add(nick);
         record.add(Long.toString(timeLogged));
         return StringUtils.join(record.toArray(), "\t");
     }
 
     @Override
     public final String toString() {
         final String message = "Implicit Player.toString(). (" + Thread.currentThread().getStackTrace() + ")";
         Bukkit.getLogger().info(message); // Implicit .toString()
         return name;
     }
 
     /**
      * Warp an existing player with these extensions.
      * @param player Player to wrap.
      */
     public final void wrapPlayer(final Player player) {
         this.player = player;
         player.setDisplayName(nick);
     }
 
     /**
      * Increment logged time.
      * @param time Time to add.
      */
     public final void addTime(final long time) {
         timeLogged += time;
     }
 
     /**
      * Toggle this players lockdown pass.
      */
     public final void toggleLockdownPass() {
         hasLockdownPass ^= true;
     }
 
     /**
      * Get this players selected pet.
      * @return Pet UUID.
      */
     public final UUID getSelectedPet() {
         return selectedPet;
     }
 
     /**
      * Set this players selected pet.
      * @param id Pet UUID
      */
     public final void selectPet(final UUID id) {
         selectedPet = id;
     }
 
     /**
      * Check if this player currently shopping.
      * @return
      */
     public final boolean isShopping() {
         return isShopping;
     }
 
     /**
      * Set if this player is currently shopping.
      * @param isShopping
      */
     public final void setShopping(final boolean isShopping) {
         this.isShopping = isShopping;
     }
 
     /**
      * Set the player that this player is whispering with.
      * @param player Player whispering.
      */
     public void setWhisperer(final SaveablePlayer player) {
         whisperer = player;
     }
 
     /**
      * Toggle this players visibility.
      * @return If this player is now visible.
      */
     public boolean toggleHidden() {
         isHidden ^= true;
         return isHidden;
     }
 
     /**
      * Check if this player is hidden.
      * @return
      */
     public boolean isHidden() {
         return isHidden;
     }
 
     /**
      * Get this players duel wager.
      * @return Duel wager.
      */
     public int getWager() {
         return wager;
     }
 
     /**
      * Check if this player is buying an item.
      * @return
      */
     public final boolean isBuying() {
         return isBuying;
     }
 
     /**
      * Set if this player is buying an item.
      * @param isBuying
      */
     public final void setBuying(final boolean isBuying) {
         this.isBuying = isBuying;
     }
 
     /**
      * Check if this player is wearing scuba gear.
      * @return <code>true</code> if the player is wearing scuba gear, <code>false</code> otherwise.
      */
     public final boolean hasScuba() {
         if(player != null) {
             ItemStack helmet = player.getInventory().getHelmet();
             if(helmet != null && helmet.getType().equals(Material.GLASS)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Set the player that this player is whispering with.
      * @return Player whispering.
      */
     public final SaveablePlayer getWhisperer() {
         return whisperer;
     }
 
     /**
      *
      * @param search
      * @return
      */
     public final int countItems(final ItemStack search) {
         if(player == null) {
             return 0;
         } else {
             final ItemStack[] inventory = player.getInventory().getContents();
             int count = 0;
             for(int i = 0; i < inventory.length; i++) {
                 final ItemStack item = inventory[i];
                 if(item != null && item.getType() == search.getType() && item.getData().getData() == search.getData().getData()) {
                     count += item.getAmount();
                 }
             }
             return count;
         }
     }
 
     /**
      *
      * @return
      */
     public final int getPowertoolID() {
         return powertoolId;
     }
 
     /**
      *
      * @return
      */
     public final String getPowertool() {
         return powertoolCmd;
     }
 
     /**
      *
      */
     public final void claimPrize() {
         timePrizeClaimed = System.currentTimeMillis();
     }
 
     /**
      * 
      * @param vehicle
      */
     public final void setVehicle(final Entity vehicle) {
         vehicle.setPassenger(player);
     }
 
     /**
      *
      * @return
      */
     public final boolean hasClaimedPrize() {
         return (timePrizeClaimed + Timer.DAY > System.currentTimeMillis());
     }
 
     /**
      *
      * @return
      */
     public final Session forceSession() {
         Session session = UDSPlugin.getSessions().get(getName());
         if(session == null) {
             session = new Session();
             UDSPlugin.getSessions().put(getName(), session);
         }
         return session;
     }
 
     /**
      *
      * @param ID
      */
     public final void setPowertoolID(final int ID) {
         powertoolId = ID;
     }
 
     /**
      * Get this players current shopping list.
      * @return
      */
     public final Inventory getShoppingList() {
         return shoppingCart;
     }
 
     /**
      *
      * @param cmd
      */
     public final void setPowertool(final String cmd) {
         powertoolCmd = cmd;
     }
 
     /**
      *
      * @return
      */
     private Player getBase() {
         return player;
     }
 
     /**
      * Sets the base player reference to <code>null</code>.
      */
     public final void nullBase() {
         player = null;
     }
 
     /**
      * Get the players saved inventory.
      * @return The players saved inventory, <code>null</code> if none exists.
      */
     public final ItemStack[] getInventoryCopy() {
         return inventoryCopy == null ? null : inventoryCopy.clone();
     }
 
     /**
      *
      * @param location
      * @return
      */
     public final boolean isInShop(final Location location) {
         for(Region region : UDSPlugin.getRegions(RegionType.SHOP).values()) {
             if(location.toVector().isInAABB(region.getV1(), region.getV2())) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      *
      * @param clan
      */
     public final void setClan(final Clan clan) {
         this.clan = clan;
     }
 
     /**
      * Save this players inventory for later retrieval.
      */
     public final void saveInventory() {
         if(player != null) {
             inventoryCopy = player.getInventory().getContents();
         }
     }
 
     /**
      *
      */
     public final void saveItems() {
         saveInventory();
         saveArmor();
     }
 
     /**
      * Load this players armor.
      */
     public final void loadArmor() {
         if(player != null) {
             player.getInventory().setArmorContents(armorCopy);
             armorCopy = new ItemStack[0];
         }
     }
 
     /**
      *
      */
     public final void endChallenge() {
         wager = 0;
         challenger = null;
     }
 
     /**
      * Load this players inventory.
      */
     public final void loadInventory() {
         if(player != null) {
             player.getInventory().setContents(inventoryCopy);
             inventoryCopy = new ItemStack[0];
         }
     }
 
     /**
      * Save this players armor for later retrieval.
      */
     public final void saveArmor() {
         if(player != null) {
             armorCopy = player.getInventory().getArmorContents();
         }
     }
 
     /**
      * Get this players bail.
      * @return This players bail.
      */
     public final int getBail() {
         return bail;
     }
 
     /**
      *
      */
     public final void loadItems() {
         loadInventory();
         loadArmor();
     }
 
     /**
      * Update the chat times with a new value.
      * @return <code>true</code> if chat events are not occurring too frequently, <code>false</code> otherwise.
      */
     public final boolean newChat() {
         if(lastChatTimes.size() > 5) {
             lastChatTimes.removeFirst();
         }
         lastChatTimes.offerLast(System.currentTimeMillis());
         if(lastChatTimes.size() == 5 && lastChatTimes.getLast() - lastChatTimes.getFirst() < 3000) {
             return false;
         }
         return true;
     }
 
     /**
      *
      * @return
      */
     public final String getTimeLogged() {
         return timeToString(timeLogged);
     }
 
     /**
      *
      * @return
      */
     public final String getLastSeen() {
         if(player == null) {
             final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
             if(offlinePlayer == null) {
                 return "Unknown";
             } else {
                 return timeToString(System.currentTimeMillis() - Bukkit.getOfflinePlayer(name).getLastPlayed());
             }
         } else {
             return timeToString(System.currentTimeMillis() - player.getLastPlayed());
         }
     }
 
     /**
      * Construct an English reading string of the remaining VIP time.
      * @return English reading string.
      */
     public final String getVIPTimeString() {
         return timeToString(UDSPlugin.getConfigLong(ConfigRef.VIP_TIME) - System.currentTimeMillis() - getVIPTime());
     }
 
     /**
      *
      * @param time
      * @return
      */
     private String timeToString(final long time) {
         long timeRemaining = time;
         String timeString = "";
         if(timeRemaining >= Timer.DAY) {
             final int days = (int)(timeRemaining / Timer.DAY);
             timeString = timeString.concat(days + (days == 1 ? " day " : " days "));
             timeRemaining -= days * Timer.DAY;
         }
         if(timeRemaining >= Timer.HOUR) {
             final int hours = (int)(timeRemaining / Timer.HOUR);
             timeString = timeString.concat(hours + (hours == 1 ? " hour " : " hours "));
             timeRemaining -= hours * Timer.HOUR;
         }
         if(timeRemaining >= Timer.MINUTE) {
             final int minutes = (int)(timeRemaining / Timer.MINUTE);
             timeString = timeString.concat(minutes + (minutes == 1 ? " minute " : " minutes "));
             timeRemaining -= minutes * Timer.MINUTE;
         }
         if(timeRemaining >= Timer.SECOND) {
             final int seconds = (int)(timeRemaining / Timer.SECOND);
             timeString = timeString.concat(seconds + (seconds == 1 ? " second " : " seconds "));
             timeRemaining -= seconds * Timer.SECOND;
         }
         return timeString;
     }
 
     /**
      * Use up some the players VIP daily item spawns.
      * @param amount The amount of spawns to use up.
      * @return The number of spawns remaining.
      */
     public final int useVIPSpawns(final int amount) {
         vipSpawns -= amount;
         return vipSpawns;
     }
 
     /**
      * Set the players back warp point to their current location.
      */
     public final void setBackPoint() {
         if(player != null) {
             backPoint = player.getLocation();
         }
     }
 
     /**
      *
      * @param location
      */
     public final void setBackPoint(final Location location) {
         backPoint = location;
     }
 
     /**
      * Toggle this players game mode.
      * @return <code>true</code> if the players game mode is now set to creative, <code>false</code> otherwise.
      */
     public final boolean toggleGameMode() {
         if(player == null) {
             return false;
         } else {
             if(player.getGameMode().equals(GameMode.SURVIVAL)) {
                 player.setGameMode(GameMode.CREATIVE);
                 return true;
             } else {
                 player.setGameMode(GameMode.SURVIVAL);
                 return false;
             }
         }
     }
 
     /**
      * Get a players monetary value.
      * @return A players monetary value.
      */
     public final int getMoney() {
         return money;
     }
 
     /**
      * Get the chat room this player is currently in.
      * @return The players chat room.
      */
     public final ChatRoom getChatRoom() {
         return chatRoom;
     }
 
     /**
      * Take a player out of jail and perform all the necessary operations.
      */
     public final void release() {
         timeJailed = 0;
         jailSentence = 0;
         if(!quietTeleport(UDSPlugin.getWarps().get("jailout")) && !quietTeleport(UDSPlugin.getData().getSpawn())) {
             player.teleport(player.getWorld().getSpawnLocation());
         }
     }
 
     /**
      * Put a player in jail and perform all the necessary operations.
      * @param sentence Time in minutes to jail the player.
      * @param bail Bail to set.
      */
     public final void jail(final long sentence, final int bail) {
         timeJailed = System.currentTimeMillis();
         jailSentence = sentence * Timer.MINUTE;
         this.bail = bail;
     }
 
     /**
      * Get the first region that the player is currently inside.
      * @param type Optional type of region to check, <code>null</code> to search all regions.
      * @return The first region the player is in, <code>null</code> otherwise.
      */
     public final Region getCurrentRegion(final RegionType type) {
         if(player != null) {
             if(type == RegionType.CITY) {
                 for(Region region : UDSPlugin.getRegions(RegionType.CITY).values()) {
                     if(player.getLocation().toVector().isInAABB(region.getV1(), region.getV2())) {
                         return region;
                     }
                 }
             } else if(type == RegionType.SHOP) {
                for(Region region : UDSPlugin.getRegions(RegionType.CITY).values()) {
                     if(player.getLocation().toVector().isInAABB(region.getV1(), region.getV2())) {
                         return region;
                     }
                 }
             }
         }
         return null;
     }
 
     /**
      * Demote a player.
      * @return Players new rank, <code>null</code> if no change.
      */
     public final PlayerRank demote() {
         final PlayerRank newRank = PlayerRank.getBelow(rank);
         if(newRank != null) {
             rank = newRank;
         }
         return newRank;
     }
 
     /**
      * Promote a player.
      * @return Players new rank, <code>null</code> if no change.
      */
     public final PlayerRank promote() {
         final PlayerRank newRank = PlayerRank.getAbove(rank);
         if(newRank != null) {
             rank = newRank;
         }
         return newRank;
     }
 
     /**
      * Get the players current chat channel.
      * @return The players chat channel.
      */
     public final ChatChannel getChannel() {
         return chatChannel;
     }
 
     /**
      * Get this players saved checkpoint.
      * @return This players checkpoint.
      */
     public final Location getCheckPoint() {
         return checkPoint;
     }
 
     /**
      *
      * @param location
      */
     public final void setCheckPoint(final Location location) {
         checkPoint = location;
     }
 
     /**
      * Check if a player is engaged in a duel with another player.
      * @return <code>true</code> if the player is engaged in a duel, <code>false</code> otherwise.
      */
     public final boolean isDuelling() {
         return challenger != null;
     }
 
     /**
      *
      * @param challenger
      */
     public final void setChallenger(final SaveablePlayer challenger) {
         this.challenger = challenger;
     }
 
     /**
      * Hide a player from this player.
      * @param player Player this player have hidden.
      * @param hide
      */
     public final void hideFrom(final Player player, final boolean hide) {
         if(hide) {
             player.hidePlayer(this.player);
         } else {
             player.showPlayer(this.player);
         }
     }
 
     /**
      *
      * @return
      */
     public final SaveablePlayer getChallenger() {
         return challenger;
     }
 
     /**
      * Check if a player can build in a given location.
      * @param location
      * @return
      */
     public final boolean canBuildHere(final Location location) {
         boolean contained = false;
         for(Region region : UDSPlugin.getRegions(RegionType.GENERIC).values()) {
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
     public final void giveAndDrop(final ItemStack item) {
         if(player != null) {
             final Map<Integer, ItemStack> drops = player.getInventory().addItem(item);
             for(ItemStack drop : drops.values()) {
                 player.getWorld().dropItemNaturally(player.getLocation(), drop);
             }
         }
     }
 
     /**
      * Add a player to a list of players this player is ignoring.
      * @param player Player to ignore.
      * @return <code>true</code> if this player was not already being ignored, <code>false</code> otherwise.
      */
     public final boolean ignorePlayer(final SaveablePlayer player) {
         return ignoredPlayers.add(player);
     }
 
     /**
      * Remove a player from a list of players this player is ignoring.
      * @param player Player to stop ignoring.
      * @return <code>true</code> if this player was being ignored, <code>false</code> otherwise.
      */
     public final boolean unignorePlayer(final SaveablePlayer player) {
         return ignoredPlayers.remove(player);
     }
 
     /**
      * Check to see if this player is ignoring a player.
      * @param player Player to check.
      * @return <code>true</code> if this player is ignoring that player, <code>false</code> otherwise.
      */
     public final boolean isIgnoringPlayer(final SaveablePlayer player) {
         return ignoredPlayers.contains(player);
     }
 
     /**
      * Get the last time this player caused damage to another player.
      * @return The last time this player did damage to another player.
      */
     public final long getLastDamageCaused() {
         return timeAttacked;
     }
 
     /**
      * Get this players total bounty.
      * @return Players bounty.
      */
     public final int getBounty() {
         return bounty;
     }
 
     /**
      * Set the bounty on a player.
      * @param bounty Bounty to set.
      */
     public final void setBounty(final int bounty) {
         this.bounty = bounty;
     }
 
     /**
      * Add a bounty to this player.
      * @param bounty New bounty.
      */
     public final void addBounty(final int bounty) {
         this.bounty += bounty;
     }
 
     /**
      * Get the last recorded location of the player.
      * @return The last recorded location of the player.
      */
     public final Location getBack() {
         return backPoint;
     }
 
 //    /**
 //     * Get a player current rank.
 //     * @return Player rank.
 //     */
 //    public final PlayerRank getRank() {
 //        return rank;
 //    }
     
     public final boolean hasRank(final PlayerRank rank) {
         return this.rank.compareTo(rank) >= 0;
     }
     
     public final boolean isRank(final PlayerRank rank) {
         return this.rank.compareTo(rank) == 0;
     }
 
     /**
      * Set the player rank.
      * @param rank The rank to set.
      */
     public final void setRank(final PlayerRank rank) {
         this.rank = rank;
     }
 
     /**
      * Check to see if a player belongs to a clan.
      * @return <code>true</code> if a player is in a clan, <code>false</code> otherwise.
      */
     public final boolean isInClan() {
         return clan != null;
     }
 
     /**
      * Get the name of the clan the player is a member of.
      * @return Clan name.
      */
     public final Clan getClan() {
         return clan;
     }
 
     /**
      * Storage of nick name kept in extension for offline access.
      * @return Player nick name.
      */
     public final String getNick() {
         return nick;
     }
 
     /**
      * Toggle the players chat channel.
      * @param channel Channel to toggle.
      * @return <code>true</code> if channel was toggled on, <code>false</code> if channel switched back to public.
      */
     public final boolean toggleChannel(final ChatChannel channel) {
         if(this.chatChannel.equals(channel)) {
             this.chatChannel = ChatChannel.PUBLIC;
             return false;
         } else {
             this.chatChannel = channel;
             return true;
         }
     }
 
     /**
      * Check whether this player has a lockdown pass.
      * @return Has player got lockdown pass.
      */
     public final boolean hasLockdownPass() {
         return hasLockdownPass;
     }
 
     /**
      *
      * @param loadItems
      */
     public final void setLoadItems(final boolean loadItems) {
         this.mustLoadItems = loadItems;
     }
 
     /**
      *
      * @return
      */
     public final boolean hasLoadItems() {
         return mustLoadItems;
     }
 
     /**
      * Get the time when this player rented VIP status.
      * @return When VIP was rented.
      */
     public final long getVIPTime() {
         return vipTime;
     }
 
     /**
      * Set when a player rented VIP status.
      * @param time Time to set.
      */
     public final void setVIPTime(final long time) {
         vipTime = time;
     }
 
     /**
      * Get the number of free item spawns this player has left.
      * @return Number of spawns left.
      */
     public final int getVIPSpawns() {
         return vipSpawns;
     }
 
     /**
      * Set the number of free VIP item spawns a player has remaining.
      * @param spawns Number of spawns to set.
      */
     public final void setVIPSpawns(final int spawns) {
         vipSpawns = spawns;
     }
 
     /**
      * Get the time that this player was put in jail.
      * @return Players jail time.
      */
     public final long getJailTime() {
         return timeJailed;
     }
 
     /**
      * Set when a player was put in jail.
      * @param time Jail time.
      */
     public final void setJailTime(final long time) {
         timeJailed = time;
     }
 
     /**
      * Get how long this player was sentenced to jail for.
      * @return Players sentence.
      */
     public final long getJailSentence() {
         return jailSentence;
     }
 
     /**
      * Set the length of a players jail sentence.
      * @param sentence Length of sentence.
      */
     public final void setJailSentence(final long sentence) {
         jailSentence = sentence;
     }
 
     /**
      * Check if a player is currently in jail.
      * @return <code>true</code> if a player is in jail, <code>false</code> otherwise.
      */
     public final boolean isJailed() {
         return (timeJailed > 0);
     }
 
     /**
      * Check if a player currently has god mode enabled.
      * @return God mode setting.
      */
     public final boolean hasGodMode() {
         return hasGodMode;
     }
 
     /**
      * Toggle a players god mode.
      * @return Players current god mode setting.
      */
     public final boolean toggleGodMode() {
         hasGodMode ^= true;
         return hasGodMode;
     }
 
     /**
      * Set a players god mode.
      * @param mode God mode setting.
      */
     public final void setGodMode(final boolean mode) {
         hasGodMode = mode;
     }
 
     /**
      * Send a message in a particular channel.
      * @param channel Channel to send message in.
      * @param message Message to send.
      */
     public final void chat(final ChatChannel channel, final String message) {
         if(player != null) {
             final ChatChannel temp = this.chatChannel;
             this.chatChannel = channel;
             player.chat(message);
             this.chatChannel = temp;
         }
     }
 
     /**
      * Check if a player can afford to pay some value.
      * @param price Price to pay.
      * @return <code>true</code> if player has enough money, <code>false</code> otherwise.
      */
     public final boolean canAfford(final int price) {
         return money >= price || hasPermission(Perm.MIDAS);
     }
 
     /**
      *
      * @param wager
      */
     public final void setWager(final int wager) {
         this.wager = wager;
     }
 
     /**
      * Debit the players account the amount passed.
      * @param amount Amount to debit.
      */
     public final void debit(final int amount) {
         money -= amount;
     }
 
     /**
      * Credit the players account the amount passed.
      * @param amount Amount to credit.
      */
     public final void credit(final int amount) {
         money += amount;
     }
 
     /**
      * Set a players account.
      * @param amount Amount to set.
      */
     public final void setMoney(final int amount) {
         money = amount;
     }
 
     /**
      * Check if the player is marked as AFK.
      * @return Player is AFK.
      */
     public final boolean isAfk() {
         return isAfk;
     }
 
     /**
      * Toggle whether this player is marked as AFK.
      * @return Whether this player is now marked as AFK.
      */
     public final boolean toggleAfk() {
         isAfk ^= true;
         return isAfk;
     }
 
     /**
      * Teleport a player but reserve pitch and yaw of player.
      * @param location Location to teleport to.
      */
     public final void move(final Location location) {
         if(player != null) {
             final Location destination = location;
             destination.setPitch(player.getLocation().getPitch());
             destination.setYaw(player.getLocation().getYaw());
             player.teleport(destination);
         }
     }
 
     /**
      * Teleport a player but fail quietly if location is <code>null</code>.
      * @param location Location to teleport player to.
      * @return <code>true</code> if location is not <code>null</code>, <code>false</code> otherwise.
      */
     public final boolean quietTeleport(final Location location) {
         if(location == null) {
             return false;
         } else {
             if(player != null) {
                 player.teleport(location);
             }
             return true;
         }
     }
 
     /**
      * Teleport a player but fail quietly if warp or location are <code>null</code>.
      * @param warp Warp to teleport player to.
      * @return <code>true</code> if warp and location are not <code>null</code>, <code>false</code> otherwise.
      */
     public final boolean quietTeleport(final Warp warp) {
         if(warp == null) {
             return false;
         } else {
             if(player == null) {
                 return warp.getLocation() != null;
             } else {
                 return player.teleport(warp.getLocation());
             }
         }
     }
 
     /**
      * Check if this player has a permission.
      * @param perm The permission to check.
      * @return <code>true</code> if the player has the permission, <code>false</code> otherwise.
      */
     public final boolean hasPermission(final Perm perm) {
         if(perm.isHereditary()) {
             return perm.getRank().compareTo(rank) <= 0;
         } else {
             return perm.getRank().equals(rank);
         }
     }
 
     /**
      *
      * @param name
      */
     public final void setDisplayName(final String name) {
         nick = name;
     }
 
     /**
      *
      * @return
      */
     public final String getName() {
         return player == null ? name : player.getName();
     }
 
     /**
      *
      * @param message
      */
     public final void sendMessage(final String message) {
         if(player != null) {
             player.sendMessage(message);
         }
     }
 
     /**
      *
      * @return
      */
     public final boolean isOnline() {
         return player == null ? false : player.isOnline();
     }
 
     /**
      *
      * @param level
      */
     public final void setFoodLevel(final int level) {
         if(player != null) {
             player.setFoodLevel(level);
         }
     }
 
     /**
      *
      * @return
      */
     public final Location getLocation() {
         return player == null ? null : player.getLocation();
     }
 
     /**
      *
      * @return
      */
     public final World getWorld() {
         return player == null ? null : player.getWorld();
     }
 
     /**
      *
      * @return
      */
     public final long getLastPlayed() {
         return player == null ? Bukkit.getOfflinePlayer(name).getLastPlayed() : player.getLastPlayed();
     }
 
     /**
      *
      * @param message
      */
     public final void kickPlayer(final String message) {
         if(player != null) {
             player.kickPlayer(message);
         }
     }
 
     /**
      *
      * @param banned
      */
     public final void setBanned(final boolean banned) {
         if(player == null) {
             Bukkit.getOfflinePlayer(name).setBanned(banned);
         } else {
             player.setBanned(banned);
         }
     }
 
     /**
      *
      * @return
      */
     public final boolean isBanned() {
         return player == null ? Bukkit.getOfflinePlayer(name).isBanned() : player.isBanned();
     }
 
     /**
      *
      * @return
      */
     public final PlayerInventory getInventory() {
         return player == null ? null : player.getInventory();
     }
 
     /**
      *
      * @param location
      * @return
      */
     public final boolean teleport(final Location location) {
         return player == null? false : player.teleport(location);
     }
 
     /**
      *
      * @return
      */
     public final ItemStack getItemInHand() {
         return player == null ? null : player.getItemInHand();
     }
 
     /**
      *
      * @param command
      * @return
      */
     public final boolean performCommand(final String command) {
         return player == null ? false : player.performCommand(command);
     }
 
     /**
      *
      * @return
      */
     public final boolean isOp() {
         return player == null ? false : player.isOp();
     }
 
     /**
      *
      * @return
      */
     public final boolean isSneaking() {
         return player == null ? false : player.isSneaking();
     }
 
     /**
      *
      * @param item
      */
     public final void setItemInHand(final ItemStack item) {
         if(player != null) {
             player.setItemInHand(item);
         }
     }
 
     /**
      *
      * @param transparent
      * @param range
      * @return
      */
     public final Block getTargetBlock(final HashSet<Byte> transparent, final int range) {
         return player == null ? null : player.getTargetBlock(transparent, range);
     }
 
     /**
      *
      * @param transparent
      * @param range
      * @return
      */
     public final List<Block> getLastTwoTargetBlocks(final HashSet<Byte> transparent, final int range) {
         return player == null ? null : player.getLastTwoTargetBlocks(transparent, range);
     }
 
     /**
      *
      * @return
      */
     public final Player getKiller() {
         return player == null ? null : player.getKiller();
     }
 
     /**
      *
      * @return
      */
     public final GameMode getGameMode() {
         return player == null ? null : player.getGameMode();
     }
 
     /**
      *
      */
     @SuppressWarnings("deprecation")
     public final void updateInventory() {
         if(player != null) {
             player.updateInventory();
         }
     }
 
     /**
      *
      * @param target
      */
     public final void teleport(final SaveablePlayer target) {
         if(target != null) {
             player.teleport(target.getBase());
         }
     }
 
     /**
      *
      * @param entity
      */
     public final void teleportHere(final Entity entity) {
         if(player != null) {
             entity.teleport(player);
         }
     }
 
     /**
      *
      * @param pet
      */
     public final void setPet(final Tameable pet) {
         if(player != null) {
             pet.setOwner(player);
         }
     }
 
     /**
      *
      * @param levels
      */
     public final void giveExpLevels(final int levels) {
         if(player != null) {
             player.giveExpLevels(levels);
         }
     }
 
     /**
      *
      * @param exp
      */
     public final void setExp(final int exp) {
         if(player != null) {
             player.setExp(exp);
         }
     }
 
     /**
      *
      * @param level
      */
     public final void setLevel(final int level) {
         if(player != null) {
             player.setLevel(level);
         }
     }
 
     /**
      *
      * @return
      */
     public final int getLevel() {
         return player == null ? 0 : player.getLevel();
     }
 
     /**
      *
      * @return
      */
     public final double getMaxHealth() {
         return player == null ? 0 : player.getMaxHealth();
     }
 
     /**
      *
      * @param health
      */
     public final void setHealth(final double health) {
         if(player != null) {
             player.setHealth(health);
         }
     }
 
     /**
      *
      * @return
      */
     public final Location getBedSpawnLocation() {
         return player == null ? null : player.getBedSpawnLocation();
     }
 
     /**
      *
      * @param x
      * @param y
      * @param z
      * @return
      */
     public final List<Entity> getNearbyEntities(final double x, final double y, final double z) {
         return player == null ? null : player.getNearbyEntities(x, y, z);
     }
 
     /**
      *
      * @return
      */
     public final boolean isInsideVehicle() {
         return player == null ? false : player.isInsideVehicle();
     }
 
     /**
      *
      * @return
      */
     public final Entity getVehicle() {
         return player == null ? null : player.getVehicle();
     }
     
     public final ChatColor getRankColor() {
         return rank.getColor();
     }
     
     public final boolean outRanks(final SaveablePlayer player) {
         return !player.hasRank(rank);
     }
     
     public final String getRankName() {
         return rank.name();
     }
     
     public final boolean sameRank(final SaveablePlayer player) {
         return player.isRank(rank);
     }
     
     public final PlayerRank getRank() {
         return rank;
     }
 }
