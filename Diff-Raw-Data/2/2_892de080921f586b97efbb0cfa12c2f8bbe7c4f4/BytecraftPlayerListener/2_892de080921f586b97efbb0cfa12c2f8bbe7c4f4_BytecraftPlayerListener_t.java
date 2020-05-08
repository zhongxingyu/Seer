 package info.bytecraft.listener;
 
 import java.util.HashMap;
 import java.util.List;
 
 import info.bytecraft.Bytecraft;
 import info.bytecraft.api.BytecraftPlayer;
 import info.bytecraft.api.ChestLog;
 import info.bytecraft.api.PaperLog;
 import info.bytecraft.api.PlayerBannedException;
 import info.bytecraft.api.Rank;
 import info.bytecraft.api.BytecraftPlayer.Flag;
 import info.bytecraft.database.DAOException;
 import info.bytecraft.database.IBlessDAO;
 import info.bytecraft.database.IContext;
 import info.bytecraft.database.ILogDAO;
 
 import org.apache.commons.lang.WordUtils;
 import org.bukkit.Bukkit;
 
 import static org.bukkit.ChatColor.*;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.google.common.collect.Maps;
 
 public class BytecraftPlayerListener implements Listener
 {
     private Bytecraft plugin;
 
     public BytecraftPlayerListener(Bytecraft bytecraft)
     {
         this.plugin = bytecraft;
     }
 
     @EventHandler
     public void onJoin(PlayerJoinEvent event)
     {
         event.setJoinMessage(null);
         BytecraftPlayer player = plugin.getPlayer(event.getPlayer());
         if(player == null)return;
         if ((player.getRank() == Rank.ELDER || player.getRank() == Rank.PRINCESS)) {
             if (player.hasFlag(Flag.INVISIBLE)) {
                 for (BytecraftPlayer other : plugin.getOnlinePlayers()) {
                     if (other == player) {
                         continue;
                     }
                     if (other.getRank() != Rank.ELDER
                             && other.getRank() != Rank.PRINCESS) {
                         other.hidePlayer(player.getDelegate());
                     }
                     else {
                         other.sendMessage(player.getDisplayName()
                                 + ChatColor.RED + " has joined invisible");
                     }
                 }
             }
             if (!player.hasFlag(Flag.SILENT_JOIN)) {
                 if (player.getCountry() != null
                         && !player.hasFlag(Flag.HIDDEN_LOCATION)) {
                     Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "Welcome "
                             + player.getDisplayName() + ChatColor.DARK_AQUA
                             + " from " + player.getCountry());
                 }
                 else {
                     Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "Welcome "
                             + player.getDisplayName() + ChatColor.DARK_AQUA
                             + " to Bytecraft ");
                 }
             }
         }
         else {
             if(player.getCountry() != null && !player.hasFlag(Flag.HIDDEN_LOCATION)){
                 Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "Welcome "
                         + player.getDisplayName() + ChatColor.DARK_AQUA
                         + " from " + player.getCountry());
             }else{
                 Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "Welcome "
                         + player.getDisplayName() + ChatColor.DARK_AQUA
                         + " to Bytecraft ");
             }
             if (!player.hasPlayedBefore()) {
                 player.teleport(plugin.getWorldSpawn("world"));
             }
             if (player.getRank() == Rank.NEWCOMER) {
                 player.sendMessage(ChatColor.AQUA + plugin.getConfig().getString("motd.new"));
                 for (BytecraftPlayer other : plugin.getOnlinePlayers()) {
                     if (other.isMentor()) {
                         other.sendMessage(player.getDisplayName()
                                 + ChatColor.AQUA
                                 + " has joined as a newcomer, you should help them out!");
                     }
                 }
             }else{
                player.sendMessage(ChatColor.AQUA + plugin.getConfig().getString("motd.normal"));
             }
         }
     }
 
     @EventHandler
     public void onLogin(PlayerLoginEvent event)
     {
         try {
             plugin.addPlayer(event.getPlayer(), event.getAddress());
         } catch (PlayerBannedException e) {
             event.disallow(Result.KICK_BANNED, e.getMessage());
         }
     }
 
     @EventHandler
     public void onQuit(PlayerQuitEvent event)
     {
         BytecraftPlayer player = plugin.getPlayer(event.getPlayer());
         if (player.hasFlag(Flag.SILENT_JOIN)) {
             event.setQuitMessage(null);
             plugin.removePlayer(player);
             return;
         }
         event.setQuitMessage(ChatColor.GRAY + "-QUIT- "
                 + plugin.getPlayer(event.getPlayer()).getDisplayName()
                 + ChatColor.AQUA + " has left the game");
         plugin.removePlayer(player);
         return;
     }
 
     @EventHandler
     public void onDamage(EntityDamageEvent event)
     {
         if (!(event.getEntity() instanceof Player))
             return;
         BytecraftPlayer player = plugin.getPlayer((Player) event.getEntity());
         event.setCancelled(player.isAdmin());
     }
 
     @EventHandler
     public void onPvp(EntityDamageByEntityEvent event)
     {
         if (event.getDamager() instanceof Player) {
             if (event.getEntity() instanceof Player) {
                 BytecraftPlayer player =
                         plugin.getPlayer((Player) event.getEntity());
                 if (player.getCurrentZone() == null
                         || !player.getCurrentZone().isPvp()) {
                     event.setCancelled(true);
                     event.setDamage(0);
                     ((Player) event.getDamager()).sendMessage(ChatColor.RED
                             + "You are not in a pvp zone.");
                     return;
                 }
             }
         }
     }
 
     @EventHandler
     public void onKick(PlayerKickEvent event)
     {
         event.setLeaveMessage(null);
     }
 
     private HashMap<Item, BytecraftPlayer> droppedItems = Maps.newHashMap();
 
     @EventHandler
     public void onDrop(PlayerDropItemEvent event)
     {
         BytecraftPlayer player = plugin.getPlayer(event.getPlayer());
         droppedItems.put(event.getItemDrop(), player);
     }
 
     @EventHandler
     public void onPickup(PlayerPickupItemEvent event)
     {
         if (!droppedItems.containsKey(event.getItem()))
             return;
         BytecraftPlayer player = plugin.getPlayer(event.getPlayer());
         BytecraftPlayer from = droppedItems.get(event.getItem());
         ItemStack stack = event.getItem().getItemStack();
 
         if (from != null && (from != player)) {
             player.sendMessage(ChatColor.YELLOW + "You got " + ChatColor.GOLD
                     + stack.getAmount() + " "
                     + stack.getType().toString().toLowerCase()
                     + ChatColor.YELLOW + " from " + from.getDisplayName() + ".");
             from.sendMessage(ChatColor.YELLOW + "You gave "
                     + player.getDisplayName() + ChatColor.GOLD + " "
                     + stack.getAmount() + " "
                     + stack.getType().name().toLowerCase().replace("_", " "));
             droppedItems.remove(event.getItem());
         }
     }
 
     @EventHandler
     public void onDeath(PlayerDeathEvent event)
     {
         event.setDeathMessage(null);
     }
 
     @EventHandler
     public void onRespawn(PlayerRespawnEvent event)
     {
         event.setRespawnLocation(plugin.getWorldSpawn(event.getPlayer().getWorld().getName()));
     }
 
     @EventHandler
     public void onCheck(PlayerInteractEvent event)
     {
         BytecraftPlayer player = plugin.getPlayer(event.getPlayer());
         if (player.getItemInHand().getType() != Material.PAPER)
             return;
         if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
             return;
         Block block = event.getClickedBlock();
         try (IContext ctx = plugin.createContext()) {
             ILogDAO dao = ctx.getLogDAO();
             for (PaperLog log : dao.getLogs(block)) {
                 player.sendMessage(ChatColor.GREEN + log.getPlayerName() + " "
                         + ChatColor.AQUA + log.getAction() + " "
                         + log.getMaterial() + ChatColor.GREEN + " at "
                         + log.getDate());
             }
             event.setCancelled(true);
             return;
         } catch (DAOException e) {
             throw new RuntimeException(e);
         }
     }
 
     @EventHandler
     public void onOpen(InventoryOpenEvent event)
     {
         if (!(event.getPlayer() instanceof Player))
             return;
         Player player = (Player) event.getPlayer();// For some reason its human
                                                    // entity
         if(!(event.getInventory().getHolder() instanceof Chest))return;
         
         Chest chest = (Chest)event.getInventory().getHolder();
         Block block = chest.getBlock();
         try(IContext ctx = plugin.createContext()){
             ILogDAO dao = ctx.getLogDAO();
             IBlessDAO bDao = ctx.getBlessDAO();
             if(bDao.isBlessed(block)){
                 if(player.getName().equalsIgnoreCase(bDao.getOwner(block))){
                     return;
                 }
             }
             ChestLog log = new ChestLog(player.getName(), chest.getLocation(), ChestLog.Action.OPEN);
             
             dao.insertChestLog(log);
         }catch(DAOException e){
             throw new RuntimeException(e);
         }
     }
     
     @EventHandler
     public void onCheckBlock(PlayerInteractEvent event)
     {
         if(event.getAction() != Action.RIGHT_CLICK_BLOCK)return;
         BytecraftPlayer player = plugin.getPlayer(event.getPlayer());
         if(player.getItemInHand().getType() != Material.BOOK)return;
         
         Block block = event.getClickedBlock();
         
         int x = block.getX();
         int y = block.getY();
         int z = block.getZ();
         
         String biome = WordUtils.capitalize(block.getBiome().name().replaceAll("_", " "));
         BytecraftPlayer owner = null;
         int blessId = 0;
         try(IContext ctx = plugin.createContext()){
             IBlessDAO dao = ctx.getBlessDAO();
             if(dao.isBlessed(block)){
                 owner = plugin.getPlayerOffline(dao.getOwner(block));
                 blessId = dao.getBlessId(block);
             }
         }catch(DAOException e){
             throw new RuntimeException(e);
         }
         
         player.sendMessage(DARK_AQUA + "========= Block Information =========");
         player.sendMessage(DARK_AQUA + "X: " + WHITE + x);
         player.sendMessage(DARK_AQUA + "Y: " + WHITE + y);
         player.sendMessage(DARK_AQUA + "Z: " + WHITE + z);
         player.sendMessage(DARK_AQUA + "Biome: " + WHITE + biome);
         
         if(owner != null){
             String name = owner.getRank().getColor() + owner.getName() + WHITE;
             player.sendMessage(DARK_AQUA + "Bless ID: " + WHITE + blessId);
             player.sendMessage(DARK_AQUA + "Owner: " + name);
         }
         player.sendMessage(DARK_AQUA + "=====================");
         event.setCancelled(true);
         event.setUseInteractedBlock(Event.Result.DENY);
         return;
     }
     
     @EventHandler
     public void onCheckChest(PlayerInteractEvent event)
     {
         if(event.getAction() != Action.RIGHT_CLICK_BLOCK)return;
         BytecraftPlayer player = plugin.getPlayer(event.getPlayer());
         if(player.getItemInHand().getType() != Material.BLAZE_ROD)return;
         
         Block block = event.getClickedBlock();
         if(block.getType() != Material.CHEST)return;
         
         try(IContext ctx = plugin.createContext()){
             ILogDAO dao = ctx.getLogDAO();
             List<ChestLog> logs = dao.getChestLogs(block);
             for(ChestLog log: logs){
                 BytecraftPlayer other = plugin.getBytecraftPlayerOffline(log.getPlayerName());
                 ChatColor color = other.getRank().getColor();
                 String name = color + other.getName();
                 player.sendMessage(name + ChatColor.WHITE + " opened at " + ChatColor.AQUA + log.getTimestamp());
             }
         }catch(DAOException e){
             throw new RuntimeException(e);
         }
         event.setCancelled(true);
         event.setUseInteractedBlock(Event.Result.DENY);
     }
 }
