 package com.jetfan.plugin.RegionBroadcast;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 public class MainCommandExecutor implements CommandExecutor{
 	private Main main;
 
 	public MainCommandExecutor(Main main) {
 		this.main=main;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		} else {
 			sender.sendMessage("Please use as a player.");
 		}
 		if(cmd.getName().equalsIgnoreCase("sayl")){
 			Location loc = player.getLocation();
 			Player[] playing = main.getServer().getOnlinePlayers();
 			if (args == null){
 				return true;
 			}
 			for(int i = 0; i<playing.length;i++){
 					int b = 0;
 					int x = 0, y = 0, z = 0;
 					boolean xt = false, yt = false, zt = false;
 					Location loc2 = playing[i].getLocation();
 					double range=100;
					if(args.length>=1){
 					try{
 					if(Double.valueOf(args[0]) != null){
 						range = Double.parseDouble(args[0]);
 						b=b+1;
 					}
 					} catch(NumberFormatException e){
 						
 					} catch(NullPointerException e){
 						
 					}
					}
 					boolean brea=true;
					if(args.length>=2){
 					try{
 							x = Integer.parseInt(args[1]);
 							b=b+1;
 							
 						} catch(NumberFormatException e){
 							
 					}
					}
					if(args.length>=3){
 					try{
 							y = Integer.parseInt(args[2]);
 							b=b+1;
 							
 						} catch(NumberFormatException e){
 							
 					} 
					}
					if(args.length>=4){
 					try{
 							z = Integer.parseInt(args[3]);
 							b=b+1;
 							
 						} catch(NumberFormatException e){
 							
 					} 
					}
 					if(x!=0){
 						xt=true;
 					}
 					if(y!=0){
 						yt=true;
 					}
 					if(z!=0){
 						zt=true;
 					}
 					if(xt==true&&yt==true&&zt==true){
 						brea=false;
 					} else {
 					}
 					if(brea==false){
 						loc=player.getWorld().getBlockAt(x, y, z).getLocation();
 						
 					}
 					String str = "&3[Local] &r"+player.getDisplayName()+": &7";
 					double distance;
					if(loc.getWorld().equals(loc2.getWorld())){
 					distance = loc.distance(loc2);
 					} else {
 						distance = range+1;
 					}
					if(!Double.isNaN(distance)){
						if(distance<=range){
 						for (int a = b;a<args.length;a++) {
 							str=str.concat(args[a]).concat(" ");
 							
 						}
 						str=ChatColor.translateAlternateColorCodes('&', str);
 						if(playing[i]!=player){
 						playing[i].sendMessage(str);
 						}
 						return true;
						}
 					}
 				
 				
 			}
 		}
 		return false;
 		} 
 
 }
