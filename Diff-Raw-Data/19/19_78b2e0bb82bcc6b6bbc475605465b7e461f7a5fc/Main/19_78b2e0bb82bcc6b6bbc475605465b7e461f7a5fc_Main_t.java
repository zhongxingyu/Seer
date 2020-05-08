 package us.Crash.InvProt;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.player.PlayerEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.server.PluginEvent;
 import org.bukkit.event.server.ServerListener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class Main extends JavaPlugin implements Runnable {
 
 	public static HashMap<String, ItemStack[]> savedLists = new HashMap<String, ItemStack[]>();
 	public static ArrayList<String>respawnList = new ArrayList<String>();
 	public static ArrayList<String>userList = new ArrayList<String>();
 	public static Permissions permission = null;
 	
 	@Override
 	public void onDisable() {
 		
 		System.out.println("[InvProt] Inventory Protection disabled.");
 		
 	}
 
 	@Override
 	public void onEnable() {
 		
 		PListener playerListener = new PListener();
 		EListener entityListener = new EListener();
 		
 		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Event.Priority.Normal, this);
 		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
 		getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DEATH, entityListener, Event.Priority.Normal, this);
 		
		if(getServer().getPluginManager().getPlugin("Permissions") != null){
			
			System.out.println("[InvProt] Hooked onto Permissions plugin.");
			Main.permission = (Permissions)getServer().getPluginManager().getPlugin("Permissions");
			
		}
		
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, this, 0, 100);
 		
		System.out.println("[InvProt] Inventory Protection v0.2 enabled.");
 		
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command	command, String label, String[] args){
 		
 		if(!(sender instanceof Player))
 			return false;
 		
 		Player p = (Player)sender;
 		
 		if(command.getName().equalsIgnoreCase("invprot")){
 
 			if(permission == null){
 				
 				if(!userList.contains(p.getName())){
 					
 					userList.add(p.getName());
 					p.sendMessage(ChatColor.AQUA + "Your inventory is now protected!");
 					
 				}else{
 					
 					userList.remove(p.getName());
 					p.sendMessage(ChatColor.AQUA + "Inventory Protection is off!");
 					
 				}
 				
 				return true;
 				
 			} else {
 				
				if(permission.getHandler().permission(p, "invprot.protection")){
 					
 					if(!userList.contains(p.getName())){
 						
 						userList.add(p.getName());
 						p.sendMessage(ChatColor.AQUA + "Your inventory is now protected!");
 						
 					}else{
 						
 						userList.remove(p.getName());
 						p.sendMessage(ChatColor.AQUA + "Inventory Protection is off!");
 						
 					}
 
 					return true;
 					
 				} else {
 					
 					p.sendMessage(ChatColor.RED + "You can't use Inventory Protection!");
 					return false;
 					
 				}
 			
 			}
 			
 		}
 		
 		return false;
 		
 	}
 	
	@SuppressWarnings("deprecation")
 	@Override
 	public void run(){
 		
 		HashMap<String, ItemStack[]> playerList = savedLists;
 		Player[] players = getServer().getOnlinePlayers();
 		
 		for(String name : playerList.keySet()){
 			
 			Player p = null;
 			
 			for(int i = 0; i < players.length; i++){
 				
 				if(players[i].getName().equals(name))
 					p = players[i];
 				
 			}
 			
 			if(p != null && respawnList.contains(p.getName()) && p.getHealth() > 0){
 
 				p.getInventory().setContents(savedLists.get(p.getName()));
 				p.updateInventory();
 				respawnList.remove(p.getName());
 				savedLists.remove(p.getName());
 				
 			}
 
 		}
 		
 	}
 
 }
 
 class SListener extends ServerListener {
 	
 	@Override
 	public void onPluginEnabled(PluginEvent event){
 		
 		if(event.getPlugin().getDescription().equals("Permissions")){
 			
			System.out.println("[InvProt] Hooked onto Permissions plugin.");
 			Main.permission = (Permissions)event.getPlugin();
 			
 		}
 		
 	}
 	
 }
 
 class PListener extends PlayerListener {
 
 	@Override
 	public void onPlayerQuit(PlayerEvent event){
 		
 		if(Main.userList.contains(event.getPlayer().getName()))
 			if(Main.savedLists.containsKey(event.getPlayer().getName()))
 				Main.savedLists.remove(event.getPlayer().getName());
 		
 	}
 	
 	@Override
 	public void onPlayerRespawn(PlayerRespawnEvent event){
 		
 		if(Main.userList.contains(event.getPlayer().getName()))
 			if(Main.savedLists.containsKey(event.getPlayer().getName()))
 				Main.respawnList.add(event.getPlayer().getName());
 		
 	}
 	
 }
 
 class EListener extends EntityListener {
 	
 	public void onEntityDeath(EntityDeathEvent event){
 		
 		if(event.getEntity() instanceof Player){
 			
 			Player p = (Player)event.getEntity();
 			if(Main.userList.contains(p.getName())){
 			
 				Main.savedLists.put(p.getName(), p.getInventory().getContents());
 				event.getDrops().clear();
 				
 			}
 		
 		}
 		
 	}
 	
 }
