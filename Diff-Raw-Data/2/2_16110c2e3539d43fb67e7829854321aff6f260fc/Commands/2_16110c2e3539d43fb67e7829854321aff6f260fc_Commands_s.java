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
 
 	private enum commandList { UNSERVERBAN, SERVERBAN, BAN, UNBAN, BANDURATION, LIVES, SERVERLIVES, USESERVERLIVES, MODLIVES, CONFIG, HARDCORE, MINHP, DIFFICULTY }
 	private static Logger log = Logger.getLogger("Minecraft");
 	
 	public static boolean ParseCommand(CommandSender sender, Command command, String commandLabel, String[] args){
 		String[] words = MiscFunctions.mergequotes(args);
 		int arglength = words.length;
 		if (arglength < 1) return false;
 		
 		boolean isplayer = (sender instanceof Player);
 		boolean bDur = false;
 
 		int i = 0; //default to 0 hours
 		String commandN = words[0].toUpperCase();
 		String playerN = "";
 		if (words.length>1){playerN = words[1];} else{ if(isplayer){playerN = ((Player)sender).getName();}else{playerN = "CONSOLE";}} 
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
 			if (arglength>2){
 				worldN = words[2];
 			}else{
 				worldN = "";
 			}
 		}
 		/*
 		if(arglength>=3){
 			if ((! MiscFunctions.sContainsInt(words[2]))&&(! WorldExists(worldN))){
 				sendMessage("Could not find world '" + worldN +"'", sender);
 				return true;
 			}
 		}
 		*/
 		Player playeron = null;
 		if (arglength>1){
 			if (player.isOnline()){
 				playeron = (Player)player;
 				worldN = playeron.getWorld().getName();
 			}else{
 				playeron = Bukkit.getPlayer(playerN);
 			}
 		}
 		
 		if (command.getLabel().equalsIgnoreCase("hcw")){
 			if (! enumContains(commandN)){ //if the chosen command is not recognized by the enum,
 				sendMessage("Unrecognized command: /hcw " +commandN, isplayer, sender);
 				return false;
 			}
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
 				if(!isInt(words[arglength-1],sender)) return true;
 				i = Integer.parseInt(words[arglength-1]);
 			}
 			
 			
 		switch (commandList.valueOf(commandN.toUpperCase())){ //ex hcw config <worldname> <Hardcore|BandDUration|Lives> <value of former statement >
 		case CONFIG:
 		switch (arglength){
 		case 4:
 			worldN = playerN;
 			if (!enumContains(words[2].toUpperCase())){sendMessage("unrecognized command "+ words[2],sender); return true;} 
 			switch (commandList.valueOf(words[2].toUpperCase())){
 				case HARDCORE:
 					if(!isBool(words[3],sender)) return true;
 					Config.setHc(worldN, Boolean.parseBoolean(words[3]));
 					sendMessage("World Hardcore: " + words[3],sender);
 					return true;
 				case BANDURATION:
 					if(!isDouble(words[3],sender)) return true;
 					Config.setBanL(worldN, Double.parseDouble(words[3]));
 					sendMessage("World Ban Duration: " + words[3],sender);
 					return true;
 				case LIVES:
 					if(!isInt(words[3],sender)) return true;
 					Config.setWorldLives(worldN, Integer.parseInt(words[3]));
 					sendMessage("World Life: " + words[3],sender);
 					return true;
 				case MINHP: 
 					if(!isInt(words[3],sender)) return true;
 					Config.setWorldMinHP(worldN, Integer.parseInt(words[3]));
 					sendMessage("World Min HP: " + words[3],sender);
 					return true;
 				default: return true;
 			}
 		case 3:
 			if (!enumContains(words[1].toUpperCase())){sendMessage("unrecognized command "+ words[1],sender); return true;}
 			switch (commandList.valueOf(words[1].toUpperCase())){
 				case USESERVERLIVES: 
 					if(!isInt(words[2],sender)) return true;
 					Config.setUseServerLives(Boolean.parseBoolean(words[2]));
 					sendMessage("Use Server Lives: " + words[2],sender);
 					return true;
 				case SERVERLIVES:
 					if(!isInt(words[2],sender)) return true;
 					Config.setServerLives(Integer.parseInt(words[2]));
 					sendMessage("Server Lives: " + words[2],sender);
 					return true;
 				default: return true;
 			}
 			default: return true;
 		}
 		case DIFFICULTY:
 		if (arglength==3){
 			worldN = playerN;
 			Config.setDif(worldN, words[2]);
 			sendMessage("World Difficulty: " + words[2],sender);
 			return true;
 		}
 		default: break;
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
 					case LIVES: //ex: /hcw Lives <player> <world> <integer>
 						if (player.isOnline()){
 							BanManager.setPlayerLives(playerN, worldN, i);
 							sendMessage(playerN+" Lives set to "+i+" in world "+ worldN,isplayer,sender);
 							return true;
 						}
 						return true;
 					case MODLIVES: //ex: /hcw Lives <player> <world> <integer>
 						if (player.isOnline()){
 							BanManager.setPlayerLives(playerN, worldN,BanManager.getPlayerLives(playerN,worldN)+i);
 							sendMessage(playerN+" Lives set to "+BanManager.getPlayerLives(playerN,worldN)+" in world "+ worldN,isplayer,sender);
 							return true;
 						}
 						return true;						
 					default: return false;
 					}
 				case 3: // if the wordlength is 3 and integer is present at end of list
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
 						case LIVES: //ex: /hcw Lives <player> <integer>
 							if (player.isOnline()){
 								BanManager.setPlayerLives(playerN, playeron.getWorld().getName(), i);
 								sendMessage(playerN+" Lives set to "+i+" in world "+ playeron.getWorld().getName(),isplayer,sender);
 							}else{
 								sendMessage("With this command, either the player must be online, or a world must be specified",isplayer,sender);
 							}
 							return true;
 						case MODLIVES: //ex: /hcw modlives <player> <integer>
 							if (player.isOnline()){
 								BanManager.setPlayerLives(playerN, playeron.getWorld().getName(), BanManager.getPlayerLives(playerN, playeron.getWorld().getName())+i);
 								sendMessage("Added "+i+" lives to player " + playerN+" in world "+ playeron.getWorld().getName(),isplayer,sender);
 							}else{
 								sendMessage("With this command, either the player must be online, or a world must be specified",isplayer,sender);
 							}
 							return true;
 						case SERVERLIVES: //ex: /hcw ServerLives <player> <integer>
 							BanManager.setSLives(player, i, hcw);
 							sendMessage(playerN+" Lives set to "+i+" on Server",isplayer,sender);
 							return true;
 							
 						default: return false;
 					}
 				case 2:
 					switch(commandList.valueOf(commandN)){
 					case LIVES: //ex /hcw lives <integer>
 						if(isplayer){
 							BanManager.setPlayerLives(((Player)sender).getName(),((Player)sender).getWorld().getName(),i);
 							sendMessage("Your lives set to " +i +" in world " + ((Player)sender).getWorld().getName(),isplayer,sender);
 						}else{
 							sendMessage("This command must be issued by a player, or a player must be specified",isplayer, sender);
 						}
 						return true;
 					case SERVERLIVES: //ex: /hcw ServerLives <integer>
 						if(isplayer){
 							BanManager.setSLives((Player)sender, i, hcw);
 							sendMessage("Your lives set to "+i+" on Server",isplayer,sender);
 						}else{
 							sendMessage("This command must be issued by a player, or a player must be specified",isplayer, sender);
 						}
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
 						case LIVES: //ex: /hcw Lives <player> <world>
 							i= BanManager.getPlayerLives(playerN, worldN);
 							sendMessage(playerN+" has "+i+" live(s) in world "+ worldN,isplayer,sender);
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
 				case LIVES: //ex: /hcw Lives <player>
 					if (player.isOnline()){
 						i= BanManager.getPlayerLives(playerN, ((Player)player).getWorld().getName());
 						sendMessage(playerN+" has "+i+" live(s) in world "+ ((Player)player).getWorld().getName(),isplayer,sender);
 					}else{
 						sendMessage("With this command, either the player must be online, or a world must be specified",isplayer,sender);
 					}
 					return true;
 				case SERVERLIVES: //ex: /hcw ServerLives <player>
 						sendMessage(playerN+"'s server lives: "+BanManager.getSLives(playerN),isplayer,sender);
 						return true;
 				default: return false;
 				}
 			case 1:
 				switch (commandList.valueOf(commandN)){
 				case LIVES:
 					if (!isplayer) {sendMessage("This command must be issued by a player, or a player must be specified",isplayer,sender); return true;}
 					sendMessage("Your have " + BanManager.getPlayerLives(((Player)sender).getName(),((Player)sender).getWorld().getName()) + " live(s) remaining in world " + ((Player)sender).getWorld().getName(),isplayer,sender);
 					return true;
 				case SERVERLIVES: //ex: /hcw ServerLives <player> 
 					if(isplayer){
 						sendMessage("Your Server lives: "+BanManager.getSLives(((Player)sender).getName()),isplayer,sender);
 					}else{
 						sendMessage("This command must be issued by a player, or a player must be specified",isplayer, sender);
 					}
 					return true;
 					
 				default: return false;
 				}
 			default:
 				return false;
 			}
 		}
 		return false;
 	}
 
 	/*private static boolean WorldExists(String worldN){
 		return (Bukkit.getWorld(worldN) != null); 
 	}
 	*/
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
 		if(!HardCoreWorlds.getPerm("commands",playerb,true)){
 			sendMessage("You do not have permission to access this command", sender);
 			return false;
 		}
 		return true;
 	}
 	
 	public static boolean enumContains(String command) {
 	    for (commandList c : commandList.values()) {
 	        if (c.name().equals(command)) {
 	            return true;
 	        }
 	    }
 	    return false;
 	}
 	
 	public static boolean isBool(String string, CommandSender sender){
 		try{
 			Boolean.parseBoolean(string);
 			return true;
 		}catch(NumberFormatException e){
 			sendMessage("This value must be True/False",sender);
 			return false;
 		}		
 	}
 	
 	public static boolean isInt(String string, CommandSender sender){
 		try{
 			Integer.parseInt(string);
 			return true;
 		}catch(NumberFormatException e){
 			sendMessage("This value must be an integer",sender);
 			return false;
 		}
 	}
 	
 	public static boolean isDouble(String string, CommandSender sender){
 		try{
 			Double.parseDouble(string);
 			return true;
 		}catch(NumberFormatException e){
 			sendMessage("This value must be a number",sender);
 			return false;
 		}
 	}
 	
 	
 }
