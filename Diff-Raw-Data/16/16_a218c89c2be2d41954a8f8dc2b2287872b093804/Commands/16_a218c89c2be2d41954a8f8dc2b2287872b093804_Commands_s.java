 package com.Cayviel.HardCoreWorlds;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class Commands {
 	private static HardCoreWorlds hcw;
 	Commands(HardCoreWorlds HCW){
 		hcw = HCW;
 	}
 
 	private enum commandList { UNSERVERBAN, SERVERBAN, BAN, UNBAN, BANDURATION }
 	private static Logger log = Logger.getLogger("Minecraft");
 	
 	public static boolean ParseCommand(CommandSender sender, Command command, String commandLabel, String[] args){
 		String[] words = MiscFunctions.mergequotes(args);
 		int arglength = words.length;
 		if (arglength < 2) return false;
 		
 		boolean isplayer = (sender instanceof Player);
 		boolean bDur = false;
 
 		int i = 0; //default to 0 hours
 		String commandN = words[0].toUpperCase();
 		String playerN = words[1];
 		String worldN;
 		
 		OfflinePlayer player = Bukkit.getOfflinePlayer(playerN);
 
 		if (arglength == 2){
 			if (!player.isOnline() && commandN == "BAN"){
 			sendMessage("With this command, either the player must be online, or a world must be specified, if you specified a world, check capitalization", sender);
 			return true;
 			}
 			if (commandN.equalsIgnoreCase("unban")){
 				sendMessage("With this command, a world must be specified, if you specified a world, check capitalization", sender);
 				return true;
 			}
 			if (player.isOnline()){
 				worldN = Bukkit.getPlayer(playerN).getWorld().getName();
 			}else{
 			if (isplayer){
 				worldN = ((Player)sender).getWorld().getName();
 			}else{
 				worldN = BanManager.getubWN();
 			}
 			}
 		}else{
 			worldN = words[2]; 
 		}
 		
 		if(arglength>=3){
 			if ((! MiscFunctions.sContainsInt(words[2]))&&(! WorldExists(worldN))){
 				sendMessage("Could not find world '" + worldN +"'", sender);
 				return true;
 			}
 		}
 		
 		if (command.getLabel().equalsIgnoreCase("hcw")){
 			
 			if (isplayer){
 				Player playerb = (Player)sender;
 				
 				if (HardCoreWorlds.OpCommands){ //if op commands are enabled
 					if(!playerb.isOp()){ //and the player is not op, check permissions
 						if(! commandPerms(playerb, sender)) return true;
 					}
 					//player is an op, so continue as normal
 				}else{//if op commands are not enabeled, check permissions
 					if(! commandPerms(playerb, sender)) return true;
 				}
 			}
 			
 			if (arglength > 4){
 				sendMessage("Too Many Arguments.  Try fewer words.  Place quotes around worlds with spaced names", sender);
 				return true;
 			}
 			if (MiscFunctions.sContainsInt(words[arglength-1])){
 				bDur = true;
 				i = Integer.parseInt(words[arglength-1]);
 			}
 			
 			if (bDur){	//if there is an integer at end of words
 				switch (arglength){
 				case 4: // if the wordlength is 4
 					switch (commandList.valueOf(commandN)){
 					case BAN://ex: /hcw ban <player> <world> <integer>
 						if (! BanManager.ban(playerN,worldN)){sendMessage(playerN + " cannot be banned on the unbannable world!", sender); return true;}
 						BanManager.ban(playerN,worldN);
 						BanManager.setBanDuration(playerN,worldN, i);
 						sendMessage(playerN+" banned on world "+worldN+" for "+i+" hours",sender);
 						return true;
 
 					case BANDURATION://ex: /hcw banduration <player> <world> <integer>
 						if(!BanManager.isBanned(playerN, worldN)) {sendMessage(playerN+" must be already banned to set the durationn",sender); return true;}
 						BanManager.setBanDuration(player.getName(),worldN,i);
 						sendMessage(playerN+" ban duration set in world '"+worldN+ "' for "+i+" hours",sender);
 						return true;
 						
 					default: return false;
 					}
 				case 3: // if the wordlength is 3 and integer is present at end of list
 					Player playeron = Bukkit.getPlayer(playerN); 
 					if (playeron != null){
 						worldN = playeron.getWorld().getName();
 					}else{
 						playeron = (Player)player;
 					}
 					switch (commandList.valueOf(commandN)){
 						case BAN: //ex: /hcw ban <player> <integer>
 							if (! BanManager.ban(playerN,worldN)){sendMessage(playerN + " cannot be banned on the unbannable world!", sender); return true;}
 							BanManager.ban(playerN,worldN);
 							BanManager.setBanDuration(playerN, worldN, i);
 							sendMessage(playerN +" banned on world "+worldN,sender);
 							return true;
 						case BANDURATION: //ex: /hcw banduration <player> <integer>
 							if(!BanManager.isBanned(playerN, worldN)) {sendMessage(playerN+" must be already banned to set the durationn",sender); return true;}
 							BanManager.setBanDuration(playerN,playeron.getWorld().getName(), i);
 							sendMessage(playerN+" ban duration set in world "+worldN+ "for "+i+" hours",sender);
 							return true;
 						case SERVERBAN: //ex: /hcw serverban <player> <integer>
 							sendMessage(playerN+" banned on server",isplayer,sender);
 							BanManager.serverBan(playeron,hcw,i);
 							return true;
 						default: return false;
 					}
 					
 				default: return false;
 				}
 			}					
 			//no integer is present at the end of list
 			switch (arglength){
 			case 3:// if wordlength is 3
 				switch (commandList.valueOf(commandN)) {
 						case BAN: //ex: /hcw ban <player> <world>
 							if (! BanManager.ban(playerN,worldN)){sendMessage(playerN + " cannot be banned on the unbannable world!", sender); return true;}
 							BanManager.ban(playerN,worldN);
 							sendMessage(playerN+" banned on world "+worldN,sender);
 							return true;
 						case UNBAN://ex: /hcw unban <player> <world>
 							BanManager.updateBan(playerN,worldN);
 							if (! BanManager.isBanned(playerN,worldN)){
 								sendMessage("Player "+playerN+" is already not banned in world "+worldN,sender);
 								return true;
 							}
 							BanManager.unBan(playerN,worldN);
 							sendMessage("Player "+playerN+" unbanned in world "+worldN,sender);
 							return true;
 						default:
 							return false;
 					}
 			case 2:
 				switch (commandList.valueOf(commandN)) {
 				case BAN: //ex: /hcw ban <player>
 					if (! BanManager.ban(playerN,worldN)){sendMessage(playerN + " cannot be banned on the unbannable world!", sender); return true;}
 					BanManager.ban(playerN,worldN);
 					sendMessage(playerN+" banned on world "+worldN,sender);
 					return true;
 				case SERVERBAN://ex: /hcw serverban <player>
 					sendMessage(playerN+" banned on server",sender);
 					BanManager.serverBan(player,hcw);
 					return true;
 				case UNSERVERBAN://ex: /hcw unserverban <player>
 					BanManager.unServerBan(playerN);
 					sendMessage(playerN+" unbanned on server",sender);
 					return true;
 				case UNBAN://ex: /hcw unban <player>
 					BanManager.updateBan(playerN,worldN);
 					if (! BanManager.isBanned(playerN,worldN)){
 						sendMessage("Player "+playerN+" is already not banned in world "+worldN,sender);
 						return true;
 					}
 					sendMessage("Player "+playerN+" unbanned in world "+worldN,sender);
 					BanManager.unBan(playerN,worldN);						
 					return true;
 				default:
 					return false;
 				}
 			}
			return false;
 		}
 		return false;
 	}
 
 	private static boolean WorldExists(String worldN){
 		return (Bukkit.getWorld(worldN) != null); 
 	}
 	
 	private static void sendMessage(String message, boolean isplayer, CommandSender sender){
 		if (isplayer){
 			Player player = (Player)sender;
 			player.sendMessage(message);
 		}else{
 			log.info("[HardCoreWorlds] " + message);
 		}
 	}
 	private static void sendMessage(String message, CommandSender sender){
 		if (sender instanceof Player){
 			Player player = (Player)sender;
 			player.sendMessage(message);
 		}else{
 			log.info("[HardCoreWorlds] " + message);
 		}
 	}
 	
 	private static boolean commandPerms(Player playerb, CommandSender sender){
 		if(!HardCoreWorlds.getPerm("ban.commands",playerb,true)){
 			sendMessage("You do not have permission to access this command", sender);
 			return false;
 		}
 		return true;
 	}
 	
 }
