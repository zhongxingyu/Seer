 package me.comp.plugintut;
 
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class PlayerListener implements Listener {
 
 
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent e){
 
 		//define string, constants, and other.
 		String modsMessage = ChatColor.RED + "This string is for mod news";
 		String retModMessage = ChatColor.RED + "This string is for retired mod news";
 		Player player = e.getPlayer();
 		GameMode gamemode = player.getGameMode();
 		String buversio = Bukkit.getBukkitVersion();
 		//mod list string. 
 		boolean mod = player.getName().equalsIgnoreCase("bluedawn76");
 		mod = player.getName().equalsIgnoreCase("reapersheart");
 		mod = player.getName().equalsIgnoreCase("computerxpds");
 		mod = player.getName().equalsIgnoreCase("kirresson");
 		mod = player.getName().equalsIgnoreCase("telstar86");
 		mod = player.getName().equalsIgnoreCase("w00lly");
 		
 		//retired mod string.
 		boolean retmod = player.getName().equalsIgnoreCase("Veesters");
 		retmod = player.getName().equalsIgnoreCase("invigilium");
 		
 		
 		//fires messages when I join (I being computerxpds)
 		if(player.getName().equalsIgnoreCase("computerxpds")){
			player.sendMessage(ChatColor.RED + "Welcome to the server, comp!");
 			player.sendMessage(ChatColor.BLUE + "Your gamemode is " + gamemode);
 			player.sendMessage(ChatColor.GREEN + "Bukkit version is " + buversio);
 			player.sendMessage(ChatColor.AQUA + "The Current world is " + player.getWorld());
 			
 		}
 		//fires off when a mod joins.
 		if(mod){
 			player.sendMessage(ChatColor.RED + modsMessage);
 		}
 		
 		if(retmod){
 			player.sendMessage(ChatColor.RED + retModMessage);
 		}
 		
 	}
 
 }
