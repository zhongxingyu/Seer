 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.listener
  * Created: 2012/12/31 3:18:20
  */
 package net.syamn.sakuracmd.listener;
 
 import static net.syamn.sakuracmd.storage.I18n._;
 
 import net.minecraft.server.v1_4_6.Packet62NamedSoundEffect;
 import net.syamn.sakuracmd.SCHelper;
 import net.syamn.sakuracmd.SakuraCmd;
 import net.syamn.sakuracmd.feature.GeoIP;
 import net.syamn.sakuracmd.manager.Worlds;
 import net.syamn.sakuracmd.permission.Perms;
 import net.syamn.sakuracmd.player.PlayerData;
 import net.syamn.sakuracmd.player.PlayerManager;
 import net.syamn.sakuracmd.player.Power;
 import net.syamn.sakuracmd.player.SakuraPlayer;
 import net.syamn.sakuracmd.storage.I18n;
 import net.syamn.sakuracmd.utils.plugin.SakuraCmdUtil;
 import net.syamn.sakuracmd.worker.AFKWorker;
 import net.syamn.sakuracmd.worker.InvisibleWorker;
 import net.syamn.utils.ItemUtil;
 import net.syamn.utils.Util;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
 /**
  * PlayerListener (PlayerListener.java)
  * @author syam(syamn)
  */
 public class PlayerListener implements Listener{
     private SakuraCmd plugin;
     public PlayerListener (final SakuraCmd plugin){
         this.plugin = plugin;
     }
     
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onPlayerInteract(final PlayerInteractEvent event){
         final Player player = event.getPlayer();
         AFKWorker.getInstance().updatePlayer(player);
     }
     
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onPlayerDamage(final EntityDamageEvent event){
         if (!(event.getEntity() instanceof Player)){
             return;
         }
         
         final Player player = (Player) event.getEntity();
         final SakuraPlayer sp = PlayerManager.getPlayer(player);
         if (sp.hasPower(Power.GODMODE)){
             event.setDamage(0);
             event.setCancelled(true);
         }
     }
     
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerChangedWorld(final PlayerChangedWorldEvent event){
         final Player player = event.getPlayer();
         final World world = player.getWorld();
         
         // messages
         if (world.getName().equalsIgnoreCase(Worlds.main_end)){
             Util.message(player, "&b ここは定期的にリセットされるエンドワールドです");
             Util.message(player, "&b メインワールドに戻るには &f/spawn &bコマンドを使ってください");
         }
         
         // Set survival as current gamemode for safety
         if (!player.getGameMode().equals(GameMode.SURVIVAL) && !Perms.TRUST.has(player)){
             player.setGameMode(GameMode.SURVIVAL);
         }
     }
     
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onPlayerMove(final PlayerMoveEvent event) {
         final Player player = event.getPlayer();
         final Location fromLoc = event.getFrom();
         final Location toLoc = event.getTo();
         
         AFKWorker.getInstance().updatePlayer(player);
         
         Location diffLoc = toLoc.clone().subtract(fromLoc);
         Location checkLoc = toLoc.clone().add(diffLoc.clone().multiply(3.0D));
         Block up = checkLoc.getBlock();
         Block down = up.getRelative(BlockFace.UP, 1);
         
         if (up.getType() == Material.DIAMOND_BLOCK || down.getType() == Material.DIAMOND_BLOCK) {
             Util.message(player, "&c Touch Diamond block!");
             
             final Vector dir = diffLoc.getDirection();
             Vector vect = new Vector((-(dir.getX())) * 5.0D, 2.0D, (-(dir.getZ())) * 5.0D);
             
             if (player.getVehicle() == null) {
                 player.setVelocity(vect);
             } else {
                 player.getVehicle().setVelocity(vect);
             }
         }
     }
     
     @EventHandler(priority = EventPriority.HIGH) // ignoreCancelled = true
     public void onPlayerRightClickWithItem(final PlayerInteractEvent event) {
         if (event.useItemInHand() == org.bukkit.event.Event.Result.DENY){
             return; // instead of ignoreCancelled = true
         }
         if (!(Action.RIGHT_CLICK_AIR.equals(event.getAction()) || Action.RIGHT_CLICK_BLOCK.equals(event.getAction()))) {
             return; // return if not right click
         }
         
         final Player player = event.getPlayer();
         final ItemStack is = player.getItemInHand();
         if (is == null || is.getType().equals(Material.AIR) || player.getWorld().getEnvironment().equals(Environment.THE_END)){
             return; // return if player not item in hand, or player on end environment
         }
 
         switch (is.getType()){
             // feather
             case FEATHER:
                 if (!player.getGameMode().equals(GameMode.CREATIVE)){
                     player.setItemInHand(ItemUtil.decrementItem(is, 1));
                 }
                 
                 player.setVelocity(player.getEyeLocation().getDirection().multiply(5));
                 
                 // TODO don't use CraftBukkit class here. Add this on SakuraLib and use it.
                 //final Location loc = player.getLocation();
                 //((CraftPlayer) player).getHandle().playerConnection.sendPacket(new Packet62NamedSoundEffect("note.harp", loc.getX(), loc.getY(), loc.getZ(), 0.8F, 1.0F));
                 break;
             // default - nop
             default: break;
         }
     }
     
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onPlayerFallOnSkyland(final PlayerMoveEvent event) {
         final Player player = event.getPlayer();
         
         if (player.getWorld().getName().equalsIgnoreCase(Worlds.skylands)) {
             final Location toLoc = event.getTo();
             if (toLoc.getY() <= -50.0D) { // -50以下
                 final World world = Bukkit.getWorld(Worlds.main_world);
                 if (world != null){
                     player.teleport(new Location(world, toLoc.getX(), 500.0D, toLoc.getZ()), TeleportCause.PLUGIN);
                 }
             }
         }
     }
     
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerJoin(final PlayerJoinEvent event){
         final Player player = event.getPlayer();
         final SakuraPlayer sp = PlayerManager.getPlayer(player);
         
         InvisibleWorker.getInstance().sendInvisibleOnJoin(player);
         
         String msg = _(((player.hasPlayedBefore()) ? "joinMessage" : "firstJoinMessage"), I18n.PLAYER, sp.getName());
         if (msg.length() < 1) msg = null;
         event.setJoinMessage(msg);
         
         // Auto vanish player if player has Invisible power
         if (sp.hasPower(Power.INVISIBLE)){
             InvisibleWorker.getInstance().vanish(player, true);
             Util.message(player, "&bあなたは透明モードが有効になっています！");
             event.setJoinMessage(null);
         }
         
         // Use GeoIP if enabled
         if (SCHelper.getInstance().getConfig().getUseGeoIP() && !Perms.HIDE_GEOIP.has(player)){
             msg = event.getJoinMessage();
             if (msg != null){
                 String geoStr = GeoIP.getInstance().getGeoIpString(player, SCHelper.getInstance().getConfig().getUseSimpleFormatOnJoin());
                 event.setJoinMessage(msg + Util.coloring("&7") + " (" + geoStr + ")");
             }
         }
         
         // Change TabColor
         SakuraCmdUtil.changeTabColor(player);
         
         // Run async
         plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
             @Override
             public void run(){
                 AFKWorker.getInstance().updateTimeStamp(player);
                 
                 PlayerData data = sp.getData();
                 data.updateLastConnection();
                 data.setLastIP(player.getAddress().getAddress().getHostAddress());
             }
         });
         
         // First join
         if (!player.hasPlayedBefore()) {
             final int unique = plugin.getServer().getOfflinePlayers().length;
             plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                 @Override
                 public void run() {
                     Util.broadcastMessage("&6現在のユニークビジター数: " + unique + " プレイヤー");
                 }
             }, 5L); // 0.25s after
         }
     }
     
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerLogin(final PlayerLoginEvent event){
         if (!event.getResult().equals(Result.ALLOWED)){
             return;
         }
         
         // Check Lockdown isEnabled
         if (plugin.getServerManager().isLockdown() && !Perms. LOCKDOWN_BYPASS.has(event.getPlayer())){
             event.disallow(Result.KICK_OTHER, _("serverLocked"));
         }
         
         // Add to players list
         PlayerManager.addPlayer(event.getPlayer());
     }
     
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerQuit(final PlayerQuitEvent event){
         final Player player = event.getPlayer();
         final SakuraPlayer sp = PlayerManager.getPlayer(player);
         
         // Messages
         String msg = _("quitMessage", I18n.PLAYER, sp.getName());
         if (msg.length() < 1) msg = null;
         event.setQuitMessage(msg);
         
         if (InvisibleWorker.getInstance().isInvisible(player)){
             InvisibleWorker.getInstance().onPlayerQuit(player);
             event.setQuitMessage(null); // hide message of vanished player
         }
         
         // Set survival as current gamemode for safety
         if (!player.getGameMode().equals(GameMode.SURVIVAL) && !Perms.TRUST.has(player)){
             player.setGameMode(GameMode.SURVIVAL);
         }
         
         PlayerManager.getPlayer(player).getData().updateLastDisconnect();
     }
     
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onPlayerKick(final PlayerKickEvent event) {
         event.setLeaveMessage(Util.coloring("&c[SakuraServer] &6" + event.getPlayer().getDisplayName() + " &aはKickされました: " + event.getReason()));
     }
 }
