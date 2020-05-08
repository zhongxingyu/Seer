 package com.CMDbox.Commands;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.CMDbox.CMDbox;
 import com.CMDbox.Config.DefaultConfig;
 import com.CMDbox.Config.HomesFile;
 
 public class CommandSetHome implements CommandExecutor {
 	public CommandSetHome(CMDbox cmDbox) {
 		// TODO Auto-generated constructor stub
 	}
 
 	public boolean onCommand(CommandSender cs, Command cmd, String commandLabel, String[] args){
 		if(cs.hasPermission("CMDbox.sethome")){
 			if(cmd.getName().equalsIgnoreCase("sethome")){
 				String name = cs.getName();
 				if(DefaultConfig.DefaultConfig.getBoolean("players.Homes.Allow multiple homes")== true){
 					if(cmd instanceof Player){
 					    Player player = (Player)cmd;
					    	String cw = player.getWorld().getName();
					    	int x = player.getLocation().getBlockX();
					    	int y = player.getLocation().getBlockY();
					    	int z = player.getLocation().getBlockZ();
 					    		
					    	HomesFile.HomesConfig.addDefault(name + ".home.world", cw);
					    	HomesFile.HomesConfig.addDefault(name + ".home.x", x);
					    	HomesFile.HomesConfig.addDefault(name + ".home.y", y);
					    	HomesFile.HomesConfig.addDefault(name + ".home.z", z);
					    	HomesFile.saveHomes();
					    	cs.sendMessage("Your home is set!");
					    	return true;
 					    }
 				}else{
 					if(cmd instanceof Player){
 					    Player player = (Player)cmd;
 					    	if (HomesFile.HomesConfig.contains(name + ".home")){
 					    		cs.sendMessage("You already got a home!");
 					    	}else{
 					    		String cw = player.getWorld().getName();
 					    		int x = player.getLocation().getBlockX();
 					    		int y = player.getLocation().getBlockY();
 					    		int z = player.getLocation().getBlockZ();
 					    		
 					    		HomesFile.HomesConfig.addDefault(name + ".home.world", cw);
 					    		HomesFile.HomesConfig.addDefault(name + ".home.x", x);
 					    		HomesFile.HomesConfig.addDefault(name + ".home.y", y);
 					    		HomesFile.HomesConfig.addDefault(name + ".home.z", z);
 					    		HomesFile.saveHomes();
 					    		cs.sendMessage("Your home is set!");
 					    		return true;
 					    	}
 					    }
 				}
 			}
 		}
 		return false;
 		
 	}
 }
