 package me.reddy360.theholyflint.listeners;
 
 import me.reddy360.theholyflint.PluginMain;
 import net.minecraft.server.v1_4_6.Item;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.permissions.Permission;
 
 import de.bananaco.bpermissions.api.ApiLayer;
 import de.bananaco.bpermissions.api.util.CalculableType;
 
 public class PlayerListener implements Listener {
 	PluginMain pluginMain;
 	public PlayerListener(PluginMain pluginMain) {
 		this.pluginMain = pluginMain;
 	}
 	
 	@EventHandler
 	public void onPreLogin(PlayerLoginEvent e){
 		Player player = e.getPlayer();
 		if(pluginMain.getConfig().contains("banned." + player.getName())){
 			e.disallow(Result.KICK_BANNED, 
 					ChatColor.translateAlternateColorCodes('&', pluginMain.getConfig().getString("banned." + player.getName())));
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent e){
 		Player player = e.getPlayer();
 		if(!player.hasPlayedBefore()){
 			player.teleport(player.getWorld().getSpawnLocation());
 			e.setJoinMessage(ChatColor.DARK_GREEN + "Welcome " + ChatColor.BOLD + player.getName() + ChatColor.DARK_GREEN + " to The Holy Flint server!");
 			player.sendMessage(ChatColor.GREEN + "Welcome to The Holy Flint server! Do '/help' to get the help book!");
 			player.playEffect(player.getLocation(), Effect.RECORD_PLAY, 2267);//:)
 			final World world = player.getWorld();
 			final Block block = world.getBlockAt(3, 62, -5);
 			world.setTime(14000);
 			block.setType(Material.REDSTONE_TORCH_ON);
 			Bukkit.getScheduler().scheduleSyncDelayedTask(pluginMain, new Runnable(){
 				@Override
 				public void run() {
 					world.setTime(0L);
 					block.setType(Material.AIR);
 				}
 			}, 200L);
 		}else{
 			String groupName = ApiLayer.getGroups("world", CalculableType.USER, player.getName())[0];
 			e.setJoinMessage(getJoinMessage(groupName, player.getName()));
 		}
 	}
 	
 	private String getJoinMessage(String groupName, String playerName) {
 		if(groupName.equalsIgnoreCase("New")){
 			return null;
 		}else if(groupName.equalsIgnoreCase("Member")){
 			return ChatColor.GRAY + playerName + " joined!";
 		}else if(groupName.equalsIgnoreCase("VIP")){
 			return ChatColor.DARK_AQUA + playerName + ", a VIP joined!";
 		}else if(groupName.equalsIgnoreCase("Admin")){
 			return ChatColor.GOLD + playerName + ", an Admin joined!";
 		}else if(groupName.equalsIgnoreCase("AdminOffDuty")){
 			return ChatColor.GOLD + playerName + ", an Admin joined!";
 		}else{
 			return null;
 		}
 	}
 	@EventHandler
 	public void onPlayerLeave(PlayerQuitEvent e){
 		Player player = e.getPlayer();
 		e.setQuitMessage(getExitMessage(player.getName(), ApiLayer.getGroups("world", CalculableType.USER, player.getName())[0]));
 	}
 	String getExitMessage(String groupName, String playerName) {
 			if(groupName.equalsIgnoreCase("New")){
 				return null;
 			}else if(groupName.equalsIgnoreCase("Member")){
 				return ChatColor.GRAY + playerName + " left!";
 			}else if(groupName.equalsIgnoreCase("VIP")){
 				return ChatColor.DARK_AQUA + playerName + ", left!";
 			}else if(groupName.equalsIgnoreCase("Admin")){
 				return ChatColor.GOLD + playerName + ", left!";
 			}else if(groupName.equalsIgnoreCase("Adminoffduty")){
 				return ChatColor.GOLD + playerName + ", left!";
 			}else{
 				return null;
 			}
 		}
 	
 	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
 	public void onPlayerChat(AsyncPlayerChatEvent e){
 		if(pluginMain.mutedPlayers.contains(e.getPlayer().getName())){
 			e.getPlayer().sendMessage(ChatColor.DARK_RED + "YOU HAVE BEEN MUTED");
 			e.setCancelled(true);
 			return;
 		}
 		String groupPrefix = ApiLayer.getValue("world", CalculableType.USER, e.getPlayer().getName(), "prefix");
 		e.setFormat(ChatColor.GRAY + "[" + ChatColor.translateAlternateColorCodes('&', groupPrefix) 
 				+ ChatColor.GRAY + "] " + ChatColor.RESET + e.getPlayer().getName() + ": " + 
 		convertColours(e.getMessage(), pluginMain.pluginManager.getPermission("thf.colours"), e.getPlayer()));
 	}
 	private String convertColours(String message, Permission permission, Player player) {
 		if(player.hasPermission(permission)){
 			return ChatColor.translateAlternateColorCodes('&', message);
 		}else{
 			return message;
 		}
 	}
 	
 	
 	@EventHandler
 	public void onInteract(PlayerInteractEvent e){
 		Player player = e.getPlayer();
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK && player.hasPermission(pluginMain.pluginManager.getPermission("thf.position")) && 
 				(player.getItemInHand().getTypeId() == Item.BLAZE_ROD.id)){
 			Location location = e.getClickedBlock().getLocation();
 			String position = location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
 			player.sendMessage("Position Set: " + position);
 			PluginMain.setPos(player.getName(), position);
 		}
 	}
 
 }
