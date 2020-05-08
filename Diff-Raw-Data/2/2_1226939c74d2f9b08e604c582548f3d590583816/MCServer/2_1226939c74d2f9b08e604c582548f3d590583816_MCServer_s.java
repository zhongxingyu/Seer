 package com.hotmail.shinyclef.shinybridge;
 
 import me.mahoutsukaii.plugins.banreport.BanReport;
 import org.bukkit.*;
 import org.bukkit.block.Block;
 import org.bukkit.conversations.Conversation;
 import org.bukkit.conversations.ConversationAbandonedEvent;
 import org.bukkit.entity.*;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.inventory.*;
 import org.bukkit.map.MapView;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionAttachment;
 import org.bukkit.permissions.PermissionAttachmentInfo;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.util.Vector;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * User: Shinyclef
  * Date: 4/08/13
  * Time: 12:09 AM
  */
 
 public class MCServer extends ShinyBridge
 {
     private static ShinyBridge p;
     private static Server s;
     private static Logger bukkitLog;
     private static Logger pluginLog;
     private static Map<String, String> playerChatTagMap;
     private static ArrayList<String> bannedPlayers;
 
     private static List<String> commandWhiteList;
 
     public static void initialize(ShinyBridge plugin)
     {
         p = plugin;
         s = p.getServer();
 
         bukkitLog = Bukkit.getLogger();
         pluginLog = p.getLogger();
 
         playerChatTagMap = new HashMap<String, String>();
         commandWhiteList = new ArrayList<String>();
         reloadCommandWhiteList();
 
         //getBannedPlayers list from BanReport
         Plugin banReport = Bukkit.getPluginManager().getPlugin("BanReport");
         if (banReport != null)
         {
             bannedPlayers = ((BanReport)banReport).bannedNubs;
         }
     }
 
     public static void reloadCommandWhiteList()
     {
         //get latest config
         p.reloadConfig();
 
         //reset current list and generate temp list
         commandWhiteList.clear();
         List<String> temp = p.getConfig().getStringList("CommandWhiteList");
 
         //convert all to lowercase for final list
         for (String s : temp)
         {
             commandWhiteList.add(s.toLowerCase());
         }
     }
 
     public static synchronized void bukkitLog(Level level, String msg)
     {
         bukkitLog.log(level, msg);
     }
 
     public static synchronized void pluginLog(String msg)
     {
         pluginLog.info("R+: " + msg);
     }
 
     public static void addToPlayerChatTagMap(Player player)
     {
         String playerName = player.getName();
         Account.Rank chatRank = getChatRank(player);
         String rankTag = getColouredRankString(chatRank);
 
         //put it all together
         String fullChatTag = ChatColor.WHITE + "<" + rankTag + ChatColor.WHITE + playerName + "> ";
 
         //add it to the map
         playerChatTagMap.put(playerName, fullChatTag);
     }
 
     public static void removeFromPlayerChatTagMap(Player player)
     {
         if (!playerChatTagMap.containsKey(player.getName()))
         {
             return;
         }
 
         playerChatTagMap.remove(player.getName());
     }
 
     public static Account.Rank getRank(Player player)
     {
         //get highest rank
         Account.Rank rank;
         if (player.hasPermission("rolyd.admin"))
         {
             rank = Account.Rank.GM;
         }
         else if (player.hasPermission("rolyd.mod"))
         {
             rank = Account.Rank.MOD;
         }
         else if (player.hasPermission("rolyd.exp"))
         {
             rank = Account.Rank.EXPERT;
         }
         else if (player.hasPermission("rolyd.vip"))
         {
             rank = Account.Rank.VIP;
         }
         else
         {
             rank = Account.Rank.STANDARD;
         }
         return rank;
     }
 
     public static Account.Rank getRank(String permission)
     {
         String perm = permission.toLowerCase();
         if (perm.equals("rolyd.admin"))
         {
             return Account.Rank.GM;
         }
         else if (perm.equals("rolyd.mod"))
         {
             return Account.Rank.MOD;
         }
         else if (perm.equals("rolyd.exp"))
         {
             return Account.Rank.EXPERT;
         }
         else if (perm.equals("rolyd.vip"))
         {
             return Account.Rank.VIP;
         }
         else
         {
             return Account.Rank.STANDARD;
         }
     }
 
     public static Account.Rank getChatRank(Player player)
     {
         if (player.hasPermission("simpleprefix.admin"))
         {
             return Account.Rank.GM;
         }
         else if (player.hasPermission("simpleprefix.moderator"))
         {
             return Account.Rank.MOD;
         }
         else if (player.hasPermission("simpleprefix.Exp"))
         {
             return Account.Rank.EXPERT;
         }
         else if (player.hasPermission("simpleprefix.vip"))
         {
             return Account.Rank.VIP;
         }
         else
         {
             return Account.Rank.STANDARD;
         }
     }
 
     public static String getColouredRankString(Account.Rank rank)
     {
         String rankColour = getRankColour(rank);
 
         switch (rank)
         {
             case GM:
                 return rankColour + "[GM] ";
 
             case MOD:
                 return rankColour + "[Mod] ";
 
             case EXPERT:
                 return rankColour + "[Exp] ";
 
             case VIP:
                 return rankColour + "[VIP] ";
 
             default:
                 return "";
         }
     }
 
     public static String getRankColour(Account.Rank rank)
     {
         switch (rank)
         {
             case GM:
                 return ChatColor.RED + "";
 
             case MOD:
                 return ChatColor.GREEN + "";
 
             case EXPERT:
                 return ChatColor.AQUA + "";
 
             case VIP:
                return ChatColor.YELLOW + "";
 
             default:
                 if (ShinyBridge.DEV_BUILD)
                 {
                     pluginLog("WARNING! Default case triggered in MCServer.getRankColour.");
                 }
             return "";
         }
     }
 
     public static boolean isServerOnline(String playerName)
     {
         return (s.getOfflinePlayer(playerName).isOnline());
     }
 
     public static Set<String> getServerOnlinePlayerNamesSet()
     {
         Set<String> set = new HashSet<String>();
 
         Player[] players = s.getOnlinePlayers();
         for (Player p : players)
         {
             set.add(p.getName());
         }
         return set;
     }
 
     public static boolean isBanned(String playerName)
     {
         return bannedPlayers.contains(playerName.toLowerCase());
     }
 
     //Prefix '+' for logged in both, '-' for client only, and no prefix for server only.
     public static Set<String> getAllOnlinePlayerFormattedNamesSet()
     {
         //get server players set and logged in clients set
         Set<String> masterSet = getServerOnlinePlayerNamesSet();
         Set<String> clientSet = Account.getLoggedInClientUsernamesSet();
 
         //add clients to master. Prefix '+' for logged in both, '-' for client only, and no prefix for server only.
         for (String clientUser : clientSet)
         {
             if (masterSet.contains(clientUser))
             {
                 masterSet.remove(clientUser);
                 masterSet.add("+" + clientUser);
             }
             else
             {
                 masterSet.add("-" + clientUser);
             }
         }
 
         return masterSet;
     }
 
     public static Set<Player> getOnlinePlayersEverywhereSet()
     {
         Set<Player> recipients;
         Player[] serverPlayers = s.getOnlinePlayers();
         recipients = new HashSet<Player>(Arrays.asList(serverPlayers));
 
         for (String playerName : Account.getOnlineLcUsersClientMap().keySet())
         {
             recipients.add(Account.getAccountMap().get(playerName).getClientPlayer());
         }
 
         return recipients;
     }
 
     public static class ClientPlayer implements Player
     {
         private Account account;
 
         public ClientPlayer(Account account)
         {
             this.account = account;
         }
 
         @Override //IMPLEMENTED
         public String getDisplayName()
         {
             return account.getChatTag();
         }
 
         @Override
         public void setDisplayName(String s)
         {
         }
 
         @Override //IMPLEMENTED HALF
         public String getPlayerListName()
         {
             return account.getUserName();
         }
 
         @Override
         public void setPlayerListName(String s)
         {
         }
 
         @Override
         public void setCompassTarget(Location location)
         {
         }
 
         @Override
         public Location getCompassTarget()
         {
             return null;
         }
 
         @Override //IMPLEMENTED
         public InetSocketAddress getAddress()
         {
             Socket socket = NetClientConnection.getClientMap()
                     .get(account.getAssignedClientID()).getSocket();
             InetAddress inetAddress = socket.getInetAddress();
             int port = socket.getPort();
             return new InetSocketAddress(inetAddress, port);
         }
 
         @Override //IMPLEMENTED
         public void sendRawMessage(String s)
         {
             NetProtocol.sendToClient(account.getAssignedClientID(), s, true);
         }
 
         @Override
         public void kickPlayer(String s)
         {
         }
 
         @Override //IMPLEMENTED
         public void chat(String msg)
         {
             s.getScheduler().runTaskAsynchronously(p, new AsyncChat(this, msg));
         }
 
         @Override //IMPLEMENTED
         public boolean performCommand(String s)
         {
             MCServer.s.dispatchCommand(this, s);
             return true;
         }
 
         @Override
         public boolean isSneaking()
         {
             return false;
         }
 
         @Override
         public void setSneaking(boolean b)
         {
         }
 
         @Override
         public boolean isSprinting()
         {
             return false;
         }
 
         @Override
         public void setSprinting(boolean b)
         {
         }
 
         @Override
         public void saveData()
         {
         }
 
         @Override
         public void loadData()
         {
         }
 
         @Override
         public void setSleepingIgnored(boolean b)
         {
         }
 
         @Override
         public boolean isSleepingIgnored()
         {
             return false;
         }
 
         @Override
         public void playNote(Location location, byte b, byte b2)
         {
         }
 
         @Override
         public void playNote(Location location, Instrument instrument, Note note)
         {
         }
 
         @Override
         public void playSound(Location location, Sound sound, float v, float v2)
         {
         }
 
         @Override
         public void playSound(Location location, String s, float v, float v2)
         {
         }
 
         @Override
         public void playEffect(Location location, Effect effect, int i)
         {
         }
 
         @Override
         public <T> void playEffect(Location location, Effect effect, T t)
         {
         }
 
         @Override
         public void sendBlockChange(Location location, Material material, byte b)
         {
         }
 
         @Override
         public boolean sendChunkChange(Location location, int i, int i2, int i3, byte[] bytes)
         {
             return false;
         }
 
         @Override
         public void sendBlockChange(Location location, int i, byte b)
         {
         }
 
         @Override
         public void sendMap(MapView mapView)
         {
         }
 
         @Override
         public void updateInventory()
         {
         }
 
         @Override
         public void awardAchievement(Achievement achievement)
         {
         }
 
         @Override
         public void incrementStatistic(Statistic statistic)
         {
         }
 
         @Override
         public void incrementStatistic(Statistic statistic, int i)
         {
         }
 
         @Override
         public void incrementStatistic(Statistic statistic, Material material)
         {
         }
 
         @Override
         public void incrementStatistic(Statistic statistic, Material material, int i)
         {
         }
 
         @Override
         public void setPlayerTime(long l, boolean b)
         {
         }
 
         @Override
         public long getPlayerTime()
         {
             return 0;
         }
 
         @Override
         public long getPlayerTimeOffset()
         {
             return 0;
         }
 
         @Override
         public boolean isPlayerTimeRelative()
         {
             return false;
         }
 
         @Override
         public void resetPlayerTime()
         {
         }
 
         @Override
         public void setPlayerWeather(WeatherType weatherType)
         {
         }
 
         @Override
         public WeatherType getPlayerWeather()
         {
             return null;
         }
 
         @Override
         public void resetPlayerWeather()
         {
         }
 
         @Override
         public void giveExp(int i)
         {
         }
 
         @Override
         public void giveExpLevels(int i)
         {
         }
 
         @Override
         public float getExp()
         {
             return 0;
         }
 
         @Override
         public void setExp(float v)
         {
         }
 
         @Override
         public int getLevel()
         {
             return 0;
         }
 
         @Override
         public void setLevel(int i)
         {
         }
 
         @Override
         public int getTotalExperience()
         {
             return 0;
         }
 
         @Override
         public void setTotalExperience(int i)
         {
         }
 
         @Override
         public float getExhaustion()
         {
             return 0;
         }
 
         @Override
         public void setExhaustion(float v)
         {
         }
 
         @Override
         public float getSaturation()
         {
             return 0;
         }
 
         @Override
         public void setSaturation(float v)
         {
         }
 
         @Override
         public int getFoodLevel()
         {
             return 0;
         }
 
         @Override
         public void setFoodLevel(int i)
         {
         }
 
         @Override
         public Location getBedSpawnLocation()
         {
             return null;
         }
 
         @Override
         public void setBedSpawnLocation(Location location)
         {
         }
 
         @Override
         public void setBedSpawnLocation(Location location, boolean b)
         {
         }
 
         @Override
         public boolean getAllowFlight()
         {
             return false;
         }
 
         @Override
         public void setAllowFlight(boolean b)
         {
         }
 
         @Override
         public void hidePlayer(Player player)
         {
         }
 
         @Override
         public void showPlayer(Player player)
         {
         }
 
         @Override
         public boolean canSee(Player player)
         {
             return false;
         }
 
         @Override
         public boolean isOnGround()
         {
             return false;
         }
 
         @Override
         public boolean isFlying()
         {
             return false;
         }
 
         @Override
         public void setFlying(boolean b)
         {
         }
 
         @Override
         public void setFlySpeed(float v) throws IllegalArgumentException
         {
         }
 
         @Override
         public void setWalkSpeed(float v) throws IllegalArgumentException
         {
         }
 
         @Override
         public float getFlySpeed()
         {
             return 0;
         }
 
         @Override
         public float getWalkSpeed()
         {
             return 0;
         }
 
         @Override
         public void setTexturePack(String s)
         {
         }
 
         @Override
         public Scoreboard getScoreboard()
         {
             return null;
         }
 
         @Override
         public void setScoreboard(Scoreboard scoreboard) throws IllegalArgumentException, IllegalStateException
         {
         }
 
         @Override
         public boolean isHealthScaled()
         {
             return false;
         }
 
         @Override
         public void setHealthScaled(boolean b)
         {
         }
 
         @Override
         public void setHealthScale(double v) throws IllegalArgumentException
         {
         }
 
         @Override
         public double getHealthScale()
         {
             return 0;
         }
 
         @Override //IMPLEMENTED
         public void sendMessage(String s)
         {
             NetProtocol.sendToClient(account.getAssignedClientID(), ChatColor.WHITE + s, true);
         }
 
         @Override //IMPLEMENTED
         public void sendMessage(String[] strings)
         {
             String message = "";
             for (String s : strings)
             {
                 message = message + s + " ";
             }
             message = message.substring(0, message.length() - 1);
             sendMessage(message);
         }
 
         @Override
         public Map<String, Object> serialize()
         {
             return null;
         }
 
         @Override
         public boolean isConversing()
         {
             return false;
         }
 
         @Override
         public void acceptConversationInput(String s)
         {
         }
 
         @Override
         public boolean beginConversation(Conversation conversation)
         {
             return false;
         }
 
         @Override
         public void abandonConversation(Conversation conversation)
         {
         }
 
         @Override
         public void abandonConversation(Conversation conversation, ConversationAbandonedEvent conversationAbandonedEvent)
         {
         }
 
         @Override //IMPLEMENTED
         public String getName()
         {
             return account.getUserName();
         }
 
         @Override
         public PlayerInventory getInventory()
         {
             return null;
         }
 
         @Override
         public Inventory getEnderChest()
         {
             return null;
         }
 
         @Override
         public boolean setWindowProperty(InventoryView.Property property, int i)
         {
             return false;
         }
 
         @Override
         public InventoryView getOpenInventory()
         {
             return null;
         }
 
         @Override
         public InventoryView openInventory(Inventory itemStacks)
         {
             return null;
         }
 
         @Override
         public InventoryView openWorkbench(Location location, boolean b)
         {
             return null;
         }
 
         @Override
         public InventoryView openEnchanting(Location location, boolean b)
         {
             return null;
         }
 
         @Override
         public void openInventory(InventoryView inventoryView)
         {
         }
 
         @Override
         public void closeInventory()
         {
         }
 
         @Override
         public ItemStack getItemInHand()
         {
             return null;
         }
 
         @Override
         public void setItemInHand(ItemStack itemStack)
         {
         }
 
         @Override
         public ItemStack getItemOnCursor()
         {
             return null;
         }
 
         @Override
         public void setItemOnCursor(ItemStack itemStack)
         {
         }
 
         @Override
         public boolean isSleeping()
         {
             return false;
         }
 
         @Override
         public int getSleepTicks()
         {
             return 0;
         }
 
         @Override
         public GameMode getGameMode()
         {
             return null;
         }
 
         @Override
         public void setGameMode(GameMode gameMode)
         {
         }
 
         @Override
         public boolean isBlocking()
         {
             return false;
         }
 
         @Override
         public int getExpToLevel()
         {
             return 0;
         }
 
         @Override
         public double getEyeHeight()
         {
             return 0;
         }
 
         @Override
         public double getEyeHeight(boolean b)
         {
             return 0;
         }
 
         @Override
         public Location getEyeLocation()
         {
             return null;
         }
 
         @Override
         public List<Block> getLineOfSight(HashSet<Byte> bytes, int i)
         {
             return null;
         }
 
         @Override
         public Block getTargetBlock(HashSet<Byte> bytes, int i)
         {
             return null;
         }
 
         @Override
         public List<Block> getLastTwoTargetBlocks(HashSet<Byte> bytes, int i)
         {
             return null;
         }
 
         @Override
         public Egg throwEgg()
         {
             return null;
         }
 
         @Override
         public Snowball throwSnowball()
         {
             return null;
         }
 
         @Override
         public Arrow shootArrow()
         {
             return null;
         }
 
         @Override
         public <T extends Projectile> T launchProjectile(Class<? extends T> aClass)
         {
             return null;
         }
 
         @Override
         public int getRemainingAir()
         {
             return 0;
         }
 
         @Override
         public void setRemainingAir(int i)
         {
         }
 
         @Override
         public int getMaximumAir()
         {
             return 0;
         }
 
         @Override
         public void setMaximumAir(int i)
         {
         }
 
         @Override
         public int getMaximumNoDamageTicks()
         {
             return 0;
         }
 
         @Override
         public void setMaximumNoDamageTicks(int i)
         {
         }
 
         @Override
         public double getLastDamage()
         {
             return 0;
         }
 
         @Override
         public int _INVALID_getLastDamage()
         {
             return 0;
         }
 
         @Override
         public void setLastDamage(double v)
         {
         }
 
         @Override
         public void _INVALID_setLastDamage(int i)
         {
         }
 
         @Override
         public int getNoDamageTicks()
         {
             return 0;
         }
 
         @Override
         public void setNoDamageTicks(int i)
         {
         }
 
         @Override
         public Player getKiller()
         {
             return null;
         }
 
         @Override
         public boolean addPotionEffect(PotionEffect potionEffect)
         {
             return false;
         }
 
         @Override
         public boolean addPotionEffect(PotionEffect potionEffect, boolean b)
         {
             return false;
         }
 
         @Override
         public boolean addPotionEffects(Collection<PotionEffect> potionEffects)
         {
             return false;
         }
 
         @Override
         public boolean hasPotionEffect(PotionEffectType potionEffectType)
         {
             return false;
         }
 
         @Override
         public void removePotionEffect(PotionEffectType potionEffectType)
         {
         }
 
         @Override
         public Collection<PotionEffect> getActivePotionEffects()
         {
             return null;
         }
 
         @Override
         public boolean hasLineOfSight(Entity entity)
         {
             return false;
         }
 
         @Override
         public boolean getRemoveWhenFarAway()
         {
             return false;
         }
 
         @Override
         public void setRemoveWhenFarAway(boolean b)
         {
         }
 
         @Override
         public EntityEquipment getEquipment()
         {
             return null;
         }
 
         @Override
         public void setCanPickupItems(boolean b)
         {
         }
 
         @Override
         public boolean getCanPickupItems()
         {
             return false;
         }
 
         @Override
         public void setCustomName(String s)
         {
         }
 
         @Override
         public String getCustomName()
         {
             return null;
         }
 
         @Override
         public void setCustomNameVisible(boolean b)
         {
         }
 
         @Override
         public boolean isCustomNameVisible()
         {
             return false;
         }
 
         @Override
         public boolean isLeashed()
         {
             return false;
         }
 
         @Override
         public Entity getLeashHolder() throws IllegalStateException
         {
             return null;
         }
 
         @Override
         public boolean setLeashHolder(Entity entity)
         {
             return false;
         }
 
         @Override
         public void damage(double v)
         {
         }
 
         @Override
         public void _INVALID_damage(int i)
         {
         }
 
         @Override
         public void damage(double v, Entity entity)
         {
         }
 
         @Override
         public void _INVALID_damage(int i, Entity entity)
         {
         }
 
         @Override
         public double getHealth()
         {
             return 0;
         }
 
         @Override
         public int _INVALID_getHealth()
         {
             return 0;
         }
 
         @Override
         public void setHealth(double v)
         {
         }
 
         @Override
         public void _INVALID_setHealth(int i)
         {
         }
 
         @Override
         public double getMaxHealth()
         {
             return 0;
         }
 
         @Override
         public int _INVALID_getMaxHealth()
         {
             return 0;
         }
 
         @Override
         public void setMaxHealth(double v)
         {
         }
 
         @Override
         public void _INVALID_setMaxHealth(int i)
         {
         }
 
         @Override
         public void resetMaxHealth()
         {
         }
 
         @Override //IMPLEMENTED
         public Location getLocation()
         {
             return new Location(getWorld(), -10d, 66d, -21d);
         }
 
         @Override //IMPLEMENTED
         public Location getLocation(Location location)
         {
             return new Location(getWorld(), -10d, 66d, -21d);
         }
 
         @Override
         public void setVelocity(Vector vector)
         {
         }
 
         @Override
         public Vector getVelocity()
         {
             return null;
         }
 
         @Override //IMPLEMENTED
         public World getWorld()
         {
             return s.getWorld("world");
         }
 
         @Override
         public boolean teleport(Location location)
         {
             return false;
         }
 
         @Override
         public boolean teleport(Location location, PlayerTeleportEvent.TeleportCause teleportCause)
         {
             return false;
         }
 
         @Override
         public boolean teleport(Entity entity)
         {
             return false;
         }
 
         @Override
         public boolean teleport(Entity entity, PlayerTeleportEvent.TeleportCause teleportCause)
         {
             return false;
         }
 
         @Override
         public List<Entity> getNearbyEntities(double v, double v2, double v3)
         {
             return null;
         }
 
         @Override
         public int getEntityId()
         {
             return 0;
         }
 
         @Override
         public int getFireTicks()
         {
             return 0;
         }
 
         @Override
         public int getMaxFireTicks()
         {
             return 0;
         }
 
         @Override
         public void setFireTicks(int i)
         {
         }
 
         @Override
         public void remove()
         {
         }
 
         @Override
         public boolean isDead()
         {
             return false;
         }
 
         @Override
         public boolean isValid()
         {
             return false;
         }
 
         @Override
         public Server getServer()
         {
             return null;
         }
 
         @Override
         public Entity getPassenger()
         {
             return null;
         }
 
         @Override
         public boolean setPassenger(Entity entity)
         {
             return false;
         }
 
         @Override
         public boolean isEmpty()
         {
             return false;
         }
 
         @Override
         public boolean eject()
         {
             return false;
         }
 
         @Override
         public float getFallDistance()
         {
             return 0;
         }
 
         @Override
         public void setFallDistance(float v)
         {
         }
 
         @Override
         public void setLastDamageCause(EntityDamageEvent entityDamageEvent)
         {
         }
 
         @Override
         public EntityDamageEvent getLastDamageCause()
         {
             return null;
         }
 
         @Override
         public UUID getUniqueId()
         {
             return null;
         }
 
         @Override
         public int getTicksLived()
         {
             return 0;
         }
 
         @Override
         public void setTicksLived(int i)
         {
         }
 
         @Override
         public void playEffect(EntityEffect entityEffect)
         {
         }
 
         @Override
         public EntityType getType()
         {
             return null;
         }
 
         @Override
         public boolean isInsideVehicle()
         {
             return false;
         }
 
         @Override
         public boolean leaveVehicle()
         {
             return false;
         }
 
         @Override
         public Entity getVehicle()
         {
             return null;
         }
 
         @Override
         public void setMetadata(String s, MetadataValue metadataValue)
         {
         }
 
         @Override
         public List<MetadataValue> getMetadata(String s)
         {
             return null;
         }
 
         @Override
         public boolean hasMetadata(String s)
         {
             return false;
         }
 
         @Override
         public void removeMetadata(String s, Plugin plugin)
         {
         }
 
         @Override
         public boolean isOnline()
         {
             return false;
         }
 
         @Override
         public boolean isBanned()
         {
             return false;
         }
 
         @Override
         public void setBanned(boolean b)
         {
         }
 
         @Override
         public boolean isWhitelisted()
         {
             return false;
         }
 
         @Override
         public void setWhitelisted(boolean b)
         {
         }
 
         @Override
         public Player getPlayer()
         {
             return null;
         }
 
         @Override
         public long getFirstPlayed()
         {
             return 0;
         }
 
         @Override
         public long getLastPlayed()
         {
             return 0;
         }
 
         @Override
         public boolean hasPlayedBefore()
         {
             return false;
         }
 
         @Override
         public boolean isPermissionSet(String s)
         {
             return false;
         }
 
         @Override
         public boolean isPermissionSet(Permission permission)
         {
             return false;
         }
 
         @Override
         public boolean hasPermission(String s)
         {
             return false;
         }
 
         @Override
         public boolean hasPermission(Permission permission)
         {
             return false;
         }
 
         @Override
         public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b)
         {
             return null;
         }
 
         @Override
         public PermissionAttachment addAttachment(Plugin plugin)
         {
             return null;
         }
 
         @Override
         public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i)
         {
             return null;
         }
 
         @Override
         public PermissionAttachment addAttachment(Plugin plugin, int i)
         {
             return null;
         }
 
         @Override
         public void removeAttachment(PermissionAttachment permissionAttachment)
         {
         }
 
         @Override
         public void recalculatePermissions()
         {
         }
 
         @Override
         public Set<PermissionAttachmentInfo> getEffectivePermissions()
         {
             return null;
         }
 
         @Override
         public void sendPluginMessage(Plugin plugin, String s, byte[] bytes)
         {
         }
 
         @Override
         public Set<String> getListeningPluginChannels()
         {
             return null;
         }
 
         @Override
         public boolean isOp()
         {
             return false;
         }
 
         @Override
         public void setOp(boolean b)
         {
         }
     }
 
     private static class AsyncChat extends BukkitRunnable
     {
         Player player;
         String msg;
 
         private AsyncChat(Player player, String msg)
         {
             this.player = player;
             this.msg = msg;
         }
 
         @Override
         public void run()
         {
             AsyncPlayerChatEvent e = new AsyncPlayerChatEvent(false, player, msg, getOnlinePlayersEverywhereSet());
             s.getPluginManager().callEvent(e);
 
             if (e.isCancelled())
             {
                 return;
             }
 
             s.broadcastMessage(player.getDisplayName() + msg);
         }
     }
 
 
     // ---------- getters ---------- //
 
     public static Map<String, String> getPlayerChatTagMap()
     {
         return playerChatTagMap;
     }
 
     public static List<String> getCommandWhiteList()
     {
         return commandWhiteList;
     }
 }
