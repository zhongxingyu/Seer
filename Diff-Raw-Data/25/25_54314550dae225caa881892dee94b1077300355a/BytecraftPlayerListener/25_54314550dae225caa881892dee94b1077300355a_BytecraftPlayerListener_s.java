 package info.bytecraft.listener;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 
 import info.bytecraft.Bytecraft;
 import info.bytecraft.api.BytecraftPlayer;
 import info.bytecraft.api.PaperLog;
 import info.bytecraft.api.PlayerBannedException;
 import info.bytecraft.api.Rank;
 import info.bytecraft.api.BytecraftPlayer.Flag;
 import info.bytecraft.database.DAOException;
 import info.bytecraft.database.IBlessDAO;
 import info.bytecraft.database.IContext;
 import info.bytecraft.database.ILogDAO;
 import info.bytecraft.zones.Zone;
 import info.bytecraft.api.TargetBlock;
 
 import org.apache.commons.lang.WordUtils;
 
 import static org.bukkit.ChatColor.*;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerAnimationEvent;
 import org.bukkit.event.player.PlayerAnimationType;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerToggleFlightEvent;
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
             if (!player.hasPlayedBefore()) {
                 player.teleport(plugin.getWorldSpawn("world"));
             }
 
         if (player.getRank() == Rank.NEWCOMER) {
             player.sendMessage(ChatColor.AQUA
                     + plugin.getConfig().getString("motd.new"));
             for (BytecraftPlayer other : plugin.getOnlinePlayers()) {
                 if (other.getRank().canMentor()) {
                     other.sendMessage(player.getDisplayName()
                             + ChatColor.AQUA
                             + " has joined as a newcomer, you should help them out!");
                 }
             }
         }
         else {
             player.sendMessage(ChatColor.AQUA
                     + plugin.getConfig().getString("motd.normal"));
         }
         
         player.setAllowFlight(player.hasFlag(Flag.NOBLE) || player.getRank().canFill());
         
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
         
         if(player.getRank().canVanish() && player.hasFlag(Flag.INVISIBLE)){
             event.setQuitMessage(null);
             plugin.removePlayer(player);
             return;
         }
         
         List<String> messages = plugin.getQuitMessages();
         String mess =
                 ChatColor.AQUA
                         + "- quit - "
                         + messages
                                 .get(new Random().nextInt(messages.size() - 1))
                                 .replace(
                                         "%s",
                                         player.getDisplayName()
                                                 + ChatColor.GRAY)
                                 .replaceAll("(?i)&([a-f0-9])", "\u00A7$1");
         
         
         event.setQuitMessage(mess);
         plugin.removePlayer(player);
     }
     
     @EventHandler
     public void onDamage(EntityDamageEvent event)
     {
         if (!(event.getEntity() instanceof Player))
             return;
         BytecraftPlayer player = plugin.getPlayer((Player) event.getEntity());
         
         if(event.getCause() == DamageCause.FALL){
             event.setCancelled(true);
             return;
         }else{
             event.setCancelled(player.getRank().isImmortal());
             return;
         }
     }
 
     @EventHandler
     public void onPvp(EntityDamageByEntityEvent event)
     {
         if (event.getDamager() instanceof Player) {
             BytecraftPlayer player =
                     plugin.getPlayer((Player) event.getDamager());
             if (event.getEntity() instanceof Player) {
                 if (player.getCurrentZone() == null
                         || !player.getCurrentZone().hasFlag(Zone.Flag.PVP)) {
                     event.setCancelled(true);
                     event.setDamage(0);
                     ((Player) event.getDamager()).sendMessage(ChatColor.RED
                             + "You are not in a pvp zone.");
                     return;
                 }
             }else{
                 event.setCancelled(player.getRank() == Rank.NEWCOMER);
             }
         }
     }
 
     @EventHandler
     public void onKick(PlayerKickEvent event)
     {
         event.setLeaveMessage(null);
         BytecraftPlayer player = plugin.getPlayer(event.getPlayer());
         plugin.removePlayer(player);
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
         BytecraftPlayer player = plugin.getPlayer(event.getEntity());
         if(player == null)return;
         List<String> deathMessages = plugin.getDeathMessages();
         String message = "";
         if(deathMessages.isEmpty()){
             message = ChatColor.GRAY + "- death - " + player.getDisplayName() + ChatColor.GRAY + " has died!";
         }else{
             message = ChatColor.GRAY + "- death - " + deathMessages.get(
                     new Random().nextInt(deathMessages.size() - 1)).replace("%s", player.getDisplayName() + ChatColor.GRAY)
                     .replaceAll("(?i)&([a-f0-9])", "\u00A7$1");
         }
         event.setDeathMessage(message);
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
     public void onFlight(PlayerToggleFlightEvent event)
     {
         BytecraftPlayer player = plugin.getPlayer(event.getPlayer());
         if(player == null)return;
         if(player.hasFlag(Flag.NOBLE) || player.getRank().canBuild()){
             event.setCancelled(false);
         }else{
             event.setCancelled(true);
         }
         
         if(player.hasFlag(Flag.SOFTWARNED) || player.hasFlag(Flag.HARDWARNED)){
             event.setCancelled(true);
         }
         
     }
     
     @EventHandler
    public void onCompass(PlayerAnimationEvent event)
     {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) {
             return;
         }
 
         BytecraftPlayer player = plugin.getPlayer(event.getPlayer());
         ItemStack heldItem = player.getItemInHand();
         if (heldItem.getType() != Material.COMPASS) {
             return;
         }
 
         World world = player.getWorld();
 
         if (player.hasFlag(Flag.LORD)) {
 
             float pitch = event.getPlayer().getLocation().getPitch();
             float yaw = event.getPlayer().getLocation().getYaw();
 
             TargetBlock targetCalc = new TargetBlock(event.getPlayer());
             Block target = targetCalc.getTargetBlock();
             
             if (target != null) {
 
                 for (int i = 0; i < 100; i++) {
 
                     int landingType =
                             world.getBlockAt(target.getX(), target.getY() + i,
                                     target.getZ()).getTypeId();
 
                     int landingAbove =
                             world.getBlockAt(target.getX(),
                                     target.getY() + i + 1, target.getZ())
                                     .getTypeId();
 
                     if (landingType == 0 && landingAbove == 0) {
                         Location loc = target.getLocation();
 
                         loc.setX(loc.getX() + .5);
                         loc.setZ(loc.getZ() + .5);
                         loc.setY(loc.getY() + i);
                         loc.setPitch(pitch);
                         loc.setYaw(yaw);
                         if (loc.getY() < 255) {
                             player.teleport(loc);
                         }
                         break;
                     }
                 }
             }
        } else {
            if (player.hasFlag(Flag.NOBLE)) {
 
             Block target = player.getDelegate().getTargetBlock(null, 300);
 
             Block b1 =
                     world.getBlockAt(new Location(player.getWorld(), target
                             .getX(), target.getY() + 1, target.getZ()));
 
             Block b2 =
                     world.getBlockAt(new Location(player.getWorld(), target
                             .getX(), target.getY() + 2, target.getZ()));
 
             int top = world.getHighestBlockYAt(target.getLocation());
             Location loc =
                     new Location(player.getWorld(), target.getX() + 0.5, top,
                             target.getZ() + 0.5, player.getLocation().getYaw(),
                             player.getLocation().getPitch());
             player.teleport(loc);
            }
         }
     }
     
 }
