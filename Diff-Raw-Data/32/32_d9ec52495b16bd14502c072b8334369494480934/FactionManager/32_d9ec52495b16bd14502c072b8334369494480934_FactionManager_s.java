 package net.illusiononline.factioncore;
 
 import net.illusiononline.factioncore.hashmaps.*;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public class FactionManager {
 	
 	FactionCore plugin;
 	PermissionHashMap permissions;
 	PlayerConfirmationHashMap playerconfirm;
 	
 	public FactionManager(FactionCore plugin){
 		this.plugin = plugin;
 		playerconfirm = new PlayerConfirmationHashMap(plugin);
 		permissions = new PermissionHashMap();
 	}
 	
 	public boolean isFactionMember(String player, String faction){
 		String list[] = (FactionCore.getSqlManager().getListedUnit(faction, "member")).split(" ");
 		for (int i=0;i < list.length;i++){
 			if (list[i].trim().equalsIgnoreCase(player)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean isFactionAdmin(String player, String faction){
 		String list[] = (FactionCore.getSqlManager().getListedUnit(faction, "owner")+" "+FactionCore.getSqlManager().getListedUnit(faction, "admin")).split(" ");
 		for (int i=0;i < list.length;i++){
 			if (list[i].trim().equalsIgnoreCase(player)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean isFactionOwner(String player, String faction){
 		String list[] = (FactionCore.getSqlManager().getListedUnit(faction, "owner")).split(" ");
 		for (int i=0;i < list.length;i++){
 			if (list[i].trim().equalsIgnoreCase(player)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean isFactionAlly(String fac1, String fac2){
 		String list[] = (FactionCore.getSqlManager().getListedUnit(fac1, "ally")).split(" ");
 		for (int i=0;i < list.length;i++){
 			if (list[i].trim().equalsIgnoreCase(fac2)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean isFactionEnemy(String fac1, String fac2){
 		String list[] = (FactionCore.getSqlManager().getListedUnit(fac1, "enemy")).split(" ");
 		for (int i=0;i < list.length;i++){
 			if (list[i].trim().equalsIgnoreCase(fac2)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public void factionInfo(Player player, String name){
 		if (name == null) name = "";
 		String faction = name;
 		
 		if (name.equalsIgnoreCase(""))
 			faction = FactionCore.getSqlManager().getFactionbyMember(player.getName());
 		else 
 			faction = FactionCore.getSqlManager().getFaction(name);
 		
 		if (faction.equalsIgnoreCase("")){
 			player.sendMessage(ChatColor.RED+"Invalid faction!");
 			return;
 		}
 			
 		String owners = FactionCore.getSqlManager().getListedUnit(faction, "owner");
 		String admins = FactionCore.getSqlManager().getListedUnit(faction, "admins");
 		String members = FactionCore.getSqlManager().getListedUnit(faction, "member");
 		String allies = FactionCore.getSqlManager().getListedUnit(faction, "ally");
 		String enemies = FactionCore.getSqlManager().getListedUnit(faction, "enemy");
 		String balance = FactionCore.getSqlManager().getFlag(faction, "money");
 		String isopen = FactionCore.getSqlManager().getFlag(faction, "open");
 		if (isopen == null) isopen = ChatColor.RED+"false";
 		
 		player.sendMessage(ChatColor.GOLD+":|"+faction+"|:\n" +
 				ChatColor.AQUA+"Owner(s):  "+ChatColor.GREEN+owners+"\n" +
 				ChatColor.AQUA+"Admins:  "+ChatColor.GREEN+admins+"\n" +
 				ChatColor.AQUA+"Balance:  "+ChatColor.GREEN+balance+"\n" +
 				ChatColor.AQUA+"Allies:  "+ChatColor.BLUE+allies+"\n" +
 				ChatColor.AQUA+"Enemies:  "+ChatColor.RED+enemies+"\n" +
 				ChatColor.AQUA+"Open:  "+ChatColor.GREEN+isopen+"\n" +
 				ChatColor.AQUA+"Members:  "+ChatColor.GREEN+members);
 	}
 	
 	public boolean createFaction(Player player, String name){
 		if (name == null) name = "";
 		
 		if (player == null || name.equalsIgnoreCase("")) return false;
 		
 		if (!FactionCore.getSqlManager().getFactionbyMember(player.getName()).equalsIgnoreCase("")){
 			player.sendMessage(ChatColor.RED+"You must leave your faction before creating a new one!");
 			return false;
 		}
 		
 		String confirm = playerconfirm.getConfirmation(player.getName());
 		if (confirm != null){
 			if (confirm.equalsIgnoreCase("create "+name)) {
 				if (plugin.getEconomyIsPresent()){
 					int $e = plugin.getEconomySQLManager().getBalance(player.getName());
 					plugin.getEconomySQLManager().setBalance(player.getName(), $e-500);
 					player.sendMessage(ChatColor.AQUA+player.getName()+"'s Balance: "+($e-500));
 				}
 				boolean w = FactionCore.getSqlManager().newFaction(player.getName(), name);
 				if (w) {
 					playerconfirm.removeConfirmation(player.getName());
 					player.sendMessage(ChatColor.AQUA+"Faction "+name+" created!");
 					return true;
 				} else {
 					player.sendMessage(ChatColor.RED+"ERROR! Unable to create faction!");
 				}
 			}
 		}
 		
 		if (plugin.getEconomyIsPresent()){
 			int $e = plugin.getEconomySQLManager().getBalance(player.getName());
 			if ($e < 500) {
 				player.sendMessage(ChatColor.RED+"You need $500 to create a faction!");
 				return false;
 			}
 		}
 		if (FactionCore.getSqlManager().getFlag(name, "owner").equalsIgnoreCase("")){
 			if (name.length() < 5) {
 				player.sendMessage(ChatColor.RED+"Faction name must be longer than 5 characters!");
 				return false;
 			}
			if (containsIllegalCharacters(name)) {
				player.sendMessage(ChatColor.RED+"Faction name must not have illegal characters!");
 				return false;
 			}
 			player.sendMessage(ChatColor.GOLD+"Say \"/faction create "+name+"\" again to create the faction!\n" +
 					"It will cost $500!");
 			playerconfirm.addConfirmation(player.getName(), "create "+name);
 		}
 		return false;
 	} 
 	
 	public boolean disbandFaction(Player player,String name){
 		if (name == null) name = "";
 		
 		if (player == null && name.equalsIgnoreCase("")) return false;
 		
 		String faction = name;
 		
 		if (player == null){
 			boolean w = FactionCore.getSqlManager().deleteFaction(name);
 			if (w) {
 				plugin.getServer().getConsoleSender().sendMessage("Faction "+faction+" disbanded!");
 				return true;
 			} else
 				return false;
 		} else {
 			
 			if (name.equalsIgnoreCase(""))
 				faction = FactionCore.getSqlManager().getFactionbyMember(player.getName());
 			else 
 				faction = FactionCore.getSqlManager().getFaction(name);
 			
 			if (faction.equalsIgnoreCase("")){
 				player.sendMessage(ChatColor.RED+"Invalid faction!");
 				return false;
 			}
 			
 			String confirm = playerconfirm.getConfirmation(player.getName());
 			if (confirm != null){
 				if (confirm.equalsIgnoreCase("disband "+faction)) {
 					boolean w = FactionCore.getSqlManager().deleteFaction(faction);
 					if (w) {
 						playerconfirm.removeConfirmation(player.getName());
 						player.sendMessage(ChatColor.RED+"Faction "+faction+" disbanded!");
 						return true;
 					}
 				}
 			}
 			
 			if (isFactionOwner(player.getName(), faction) || player.hasPermission(permissions.getPermission("disbandother"))){
 				if (name.equalsIgnoreCase(""))
 					player.sendMessage(ChatColor.GOLD+"Say \"/faction disband\" again to disband the faction!");
 				else
 					player.sendMessage(ChatColor.GOLD+"Say \"/faction disband "+faction+"\" again to disband the faction!");
 				playerconfirm.addConfirmation(player.getName(), "disband "+faction);
 			} else {
 				player.sendMessage(ChatColor.RED+"You don't have permission to delete the faction!");
 			}
 		}
 		return false;
 	}
 	
 	public boolean joinFaction(Player player, String name){
 		if (name == null || player == null) return false;
 		
 		String faction = FactionCore.getSqlManager().getFaction(name);
 		
 		if (faction.equalsIgnoreCase("") || name.equalsIgnoreCase("")){
 			player.sendMessage(ChatColor.RED+"Invalid faction!");
 			return false;
 		}
 		
 		if (!FactionCore.getSqlManager().getFactionbyMember(player.getName()).equalsIgnoreCase("")){
 			player.sendMessage(ChatColor.RED+"You must leave your faction to join a new one!");
 			return false;
 		}
 		if (isFactionMember(player.getName(), faction)){
 			player.sendMessage(ChatColor.RED+"You are already part of this faction!");
 			return false;
 		}
 		
 		String confirm = playerconfirm.getConfirmation(player.getName());
 		if (confirm.equalsIgnoreCase("join "+faction)) {
 			boolean w = FactionCore.getSqlManager().addListedUnit(faction, "member", player.getName());
 			if (w) {
 				player.sendMessage(ChatColor.AQUA+"You have joined "+faction+"!");
 				Player players[] = plugin.getServer().getOnlinePlayers();
 				for (int i = 0;i <= players.length-1;i++){
 					if (isFactionMember(players[i].getName(), faction) && players[i] != player){
 						players[i].sendMessage(ChatColor.AQUA+player.getName()+" has joined your faction!");
 					}
 				}
 				return true;
 			}
 			return false;
 		}
 		
 		String isopen = FactionCore.getSqlManager().getFlag(faction, "open");
 		if (isopen.equalsIgnoreCase("true") || player.hasPermission(permissions.getPermission("joinoverride"))){
 			player.sendMessage(ChatColor.GOLD+"Say \"/faction join "+faction+"\" again to join the faction!");
 			playerconfirm.addConfirmation(player.getName(), "join "+faction);
 		} else {
 			player.sendMessage(ChatColor.RED+"Faction is closed!");
 		}
 		return false;
 	}
 	
 	public boolean inviteToFaction(Player player, String name){
 		if (player == null) return false;
 		Player target = plugin.getServer().getPlayer(name);
 		if (target == null) return false;
 		if (player.getName().equalsIgnoreCase(target.getName())) return false;
 		
 		String faction = FactionCore.getSqlManager().getFactionbyMember(player.getName());
 		if (faction.equalsIgnoreCase("")){
 			player.sendMessage(ChatColor.RED+"Invalid faction!");
 			return false;
 		}
 		
 		boolean can_invite = false;
 		if (FactionCore.getSqlManager().getFlag(faction, "open").equalsIgnoreCase("true"))
 			can_invite = true;
 		if (FactionCore.getSqlManager().getFlag(faction, "invite").equalsIgnoreCase("true"))
 			can_invite = true;
 		if (isFactionAdmin(player.getName(), faction))
 			can_invite = true;
 		
 		if (!FactionCore.getSqlManager().getFactionbyMember(target.getName()).equalsIgnoreCase("")){
 			player.sendMessage(ChatColor.RED+target.getName()+" is already in a faction!");
 			return false;
 		}
 		
 		if (can_invite) {
 			playerconfirm.addConfirmation(target.getName(), "join "+faction);
 			player.sendMessage(ChatColor.AQUA+"You have invited "+target.getName()+" to join your faction!");
 			target.sendMessage(ChatColor.AQUA+"You have been invited to join "+faction+"!\n" +
 								"Type \"/faction join "+faction+"\" to join the faction!");
 			return true;
 		} else {
 			player.sendMessage(ChatColor.RED+"You don't have permission to invite players!");
 		}
 		return false;
 	}
 	
 	public boolean kickFromFaction(Player player, String name){
 		if (player == null) return false;
 		Player target = plugin.getServer().getPlayer(name);
 		if (target != null) {
 			if (player.getName().equalsIgnoreCase(target.getName())) 
 				return false;
 		}
 		
 		String faction = FactionCore.getSqlManager().getFactionbyMember(player.getName());
 		if (faction.equalsIgnoreCase("")){
 			player.sendMessage(ChatColor.RED+"Invalid faction!");
 			return false;
 		}
 		String faction2 = FactionCore.getSqlManager().getFactionbyMember(name);
 		if (faction2.equalsIgnoreCase("")){
 			player.sendMessage(ChatColor.RED+"Invalid Player!");
 			return false;
 		}
 		if (!faction.equalsIgnoreCase(faction2)){
 			player.sendMessage(ChatColor.RED+"You are not in the same faction!");
 			return false;
 		}
 		
 		if (isFactionOwner(name, faction)) {
 			player.sendMessage(ChatColor.RED+"Cannot kick faction owner!");
 			return false;
 		}
 		
 		boolean can_kick = isFactionAdmin(player.getName(), faction);
 		
 		if (can_kick || player.hasPermission(permissions.getPermission("kickoverride"))) {
 			FactionCore.getSqlManager().removeListedUnit(faction, "admin", name);
 			FactionCore.getSqlManager().removeListedUnit(faction, "member", name);
 			player.sendMessage(ChatColor.AQUA+"You have kicked "+target.getName()+" from your faction!");
 			if (target != null)
 				target.sendMessage(ChatColor.RED+"You have been kicked from "+faction+"!");
 			return true;
 		} else {
 			player.sendMessage(ChatColor.RED+"You don't have permission to kick players!");
 		}
 		
 		return false;
 	}
 	
 	public boolean leaveFaction(Player player){
 		if (player == null) return false;
 		
 		String faction = FactionCore.getSqlManager().getFactionbyMember(player.getName());
 		
 		if (faction.equalsIgnoreCase("")){
 			player.sendMessage(ChatColor.RED+"Invalid faction!");
 			return false;
 		}
 		
 		String confirm = playerconfirm.getConfirmation(player.getName());
 		if (confirm.equalsIgnoreCase("leave "+faction)) {
 			boolean w = FactionCore.getSqlManager().removeListedUnit(faction, "member", player.getName());
 			if (w) {
 				player.sendMessage(ChatColor.RED+"You have left "+faction+"!");
 				Player players[] = plugin.getServer().getOnlinePlayers();
 				for (int i = 0;i < players.length;i++){
 					if (FactionCore.getSqlManager().getFactionbyMember(players[i].getName()).equalsIgnoreCase(faction)){
 						players[i].sendMessage(ChatColor.RED+player.getName()+"has left your faction!");
 					}
 				}
 				return true;
 			}
 			return false;
 		}
 		
 		if (isFactionOwner(player.getName(), faction)) {
 			player.sendMessage(ChatColor.RED+"You can not leave while you are an Owner!");
 			return false;
 		}
 		player.sendMessage(ChatColor.GOLD+"Say \"/faction leave\" again to leave the faction!");
 		playerconfirm.addConfirmation(player.getName(), "leave "+faction);
 		
 		return false;
 	}
 	
 	public boolean setHome(Player player, String name){
 		if (player == null) return false;
 		if (name == null) name = "";
 		
 		String faction = name;
 		
 		if (name.equalsIgnoreCase(""))
 			faction = FactionCore.getSqlManager().getFactionbyMember(player.getName());	
 		else
 			faction = FactionCore.getSqlManager().getFaction(name);
 		
 		if (faction.equalsIgnoreCase("")){
 			player.sendMessage(ChatColor.RED+"Invalid faction!");
 			return false;
 		}
 		
 		//Combine location into String
 		String loc = player.getLocation().getWorld().getName()+" "+
 				player.getLocation().getX()+" "+
 				player.getLocation().getY()+" "+
 				player.getLocation().getZ()+" "+
 				player.getLocation().getYaw()+" "+
 				player.getLocation().getPitch()+"";
 		
 		boolean can_set = isFactionAdmin(player.getName(), faction);
 		
 		if (can_set || player.hasPermission(permissions.getPermission("sethomeother"))) {
 			boolean w = FactionCore.getSqlManager().setFlag(faction, "home", loc);
 			if (w) {
 				player.sendMessage(ChatColor.AQUA+faction+"'s home set!");
 				return true;
 			} else 
 				player.sendMessage(ChatColor.RED+"Unable to set faction's home!");
 		} else {
 			player.sendMessage(ChatColor.RED+"You don't have permission to set the faction's home!");
 		}
 		return false;
 	}
 	
 	public boolean teleportHome(Player player, String name){
 		if (player == null) return false;
 		if (name == null) name = "";
 		
 		String faction = name;
 		
 		if (name.equalsIgnoreCase(""))
 			faction = FactionCore.getSqlManager().getFactionbyMember(player.getName());
 		else 
 			faction = FactionCore.getSqlManager().getFaction(name);
 		
 		if (faction.equalsIgnoreCase("")){
 			player.sendMessage(ChatColor.RED+"Invalid faction!");
 			return false;
 		}
 		
 		String loc[] = FactionCore.getSqlManager().getFlag(faction, "home").split(" ");
 		if (loc.length < 6) {
 			player.sendMessage(ChatColor.RED+faction+" does not have a home!");
 			return false;
 		}
 		if (plugin.getServer().getWorld(loc[0]) == null) {
 			player.sendMessage(ChatColor.RED+"Unable to define home!");
 			return false;
 		}
 		
 		/* Parse Values to Appropriate Forms */
 		double	a = 0, b = 0, c = 0;
 		float	y = 0, p = 0;
 		try {
 			a = Double.parseDouble(loc[1]);
 			b = Double.parseDouble(loc[2]);
 			c = Double.parseDouble(loc[3]);
 			y = Float.parseFloat(loc[4]);
 			p = Float.parseFloat(loc[5]);
 		} catch (NumberFormatException e) {
 			player.sendMessage(ChatColor.RED+"Unable to define home!");
 			plugin.getLogger().severe(e.getMessage());
 			return false;
 		}
 		/* End Parse */
 		
 		Location home = new Location(plugin.getServer().getWorld(loc[0]),a, b, c, y, p);
 		player.sendMessage(ChatColor.AQUA+"Going to "+faction+"'s home!");
 		player.teleport(home);
 		return true;
 	}
 	
 	public boolean clearHome(Player player, String name){
 		if (name == null) name = "";
 		
 		if (player == null && name.equalsIgnoreCase("")) return false;
 		
 		String faction = name;
 		
 		if (player == null){
 			boolean w = FactionCore.getSqlManager().clearFlag(faction, "home");
 			if (w) {
 				plugin.getServer().getConsoleSender().sendMessage("Faction "+faction+"'s home cleared!");
 				return true;
 			} else
 				return false;
 		} else {
 			
 			if (name.equalsIgnoreCase(""))
 				faction = FactionCore.getSqlManager().getFactionbyMember(player.getName());
 			else 
 				faction = FactionCore.getSqlManager().getFaction(name);
 			
 			if (faction.equalsIgnoreCase("")){
 				player.sendMessage(ChatColor.RED+"Invalid faction!");
 				return false;
 			}
 			
 			if (isFactionAdmin(player.getName(), faction) || player.hasPermission(permissions.getPermission("clearhomeother"))){
 				boolean w = FactionCore.getSqlManager().clearFlag(faction, "home");
 				if (w) {
 					player.sendMessage(ChatColor.AQUA+"Faction "+faction+"'s home cleared!");
 					return true;
 				} else {
 					player.sendMessage(ChatColor.RED+"Failed clear the faction's home!");
 				}
 			} else {
 				player.sendMessage(ChatColor.RED+"You don't have permission to clear the faction's home!");
 			}	
 		}
 		return false;
 	}
 
 	public boolean addAlly(Player player, String name){
 		if (name == null) name = "";
 		if (player == null || name.equalsIgnoreCase("")) return false;
 		
 		String faction = FactionCore.getSqlManager().getFactionbyMember(player.getName());
 		if (faction.equalsIgnoreCase("")){
 			player.sendMessage(ChatColor.RED+"You are not in a faction!");
 			
 
 		}
 		String faction2 = FactionCore.getSqlManager().getFaction(name);
 		if (faction2.equalsIgnoreCase("")){
 			player.sendMessage(ChatColor.RED+"Invalid faction!");
 		}
 		
 		if(isFactionEnemy(faction, faction2)){
 			player.sendMessage(ChatColor.RED+"Your faction is enemies with that faction!");
 			return false;
 		}
 		if(isFactionEnemy(faction2, faction)){
 			player.sendMessage(ChatColor.RED+"That faction is enemies with your faction!");
 			return false;
 		}
 		if(isFactionAlly(faction, faction2)){
 			player.sendMessage(ChatColor.RED+"Your faction is already allied with that faction!");
 			return false;
 		}
 		
 		boolean can_add = isFactionAdmin(player.getName(), faction2);
 		
 		if (can_add || player.hasPermission(permissions.getPermission("allyoverride"))){
 			
 			String confirm = playerconfirm.getConfirmation(player.getName());
 			if (confirm.equalsIgnoreCase("ally "+faction2)) {
 				boolean w = FactionCore.getSqlManager().addListedUnit(faction, "ally", faction2);
 				boolean w2 = FactionCore.getSqlManager().addListedUnit(faction2, "ally", faction);
 				if (w && w2) {
 					player.sendMessage(ChatColor.AQUA+"You have joined "+faction+"!");
 					Player players[] = plugin.getServer().getOnlinePlayers();
 					for (int i = 0;i < players.length;i++){
 						if (isFactionMember(players[i].getName(), faction) && players[i] != player){
 							players[i].sendMessage(ChatColor.AQUA+faction2+" is now your faction's ally!");
 						}
 						if (isFactionMember(players[i].getName(), faction2)){
 							players[i].sendMessage(ChatColor.AQUA+faction+" is now your faction's ally!");
 						}
 					}
 					return true;
 				}
 				return false;
 			}
 			
 			player.sendMessage(ChatColor.AQUA+"You have requested "+faction2+" to be your faction's ally!");
 
 			Player players[] = plugin.getServer().getOnlinePlayers();
 			for (int i = 0;i < players.length;i++){
 				if (isFactionAdmin(players[i].getName(), faction2)){
 					players[i].sendMessage(ChatColor.AQUA+faction+" has requested to be your faction's ally!");
 					player.sendMessage(ChatColor.GOLD+"Say \"/faction ally "+faction+"\" to ally their faction!");
 					playerconfirm.addConfirmation(player.getName(), "ally "+faction);
 				}
 			}
 			return true;
 		} else {
 			player.sendMessage(ChatColor.RED+"You don't have permission to add a faction ally!");
 		}
 		
 		return false;
 	}
 
 	public boolean removeAlly(Player player, String name){
 		if (name == null) name = "";
 		if (player == null || name.equalsIgnoreCase("")) return false;
 		
 		String faction = FactionCore.getSqlManager().getFactionbyMember(player.getName());
 		if (faction.equalsIgnoreCase("")){
 			player.sendMessage(ChatColor.RED+"You are not in a faction!");
 		}
 		String faction2 = FactionCore.getSqlManager().getFaction(name);
 		if (faction2.equalsIgnoreCase("")){
 			player.sendMessage(ChatColor.RED+"Invalid faction!");
 		}
 		if(!isFactionAlly(faction, faction2)){
 			player.sendMessage(ChatColor.RED+"Your faction isn't allied with that faction!");
 			return false;
 		}
 		
 		boolean can_remove = isFactionAdmin(player.getName(), faction2);
 		
 		if (can_remove || player.hasPermission(permissions.getPermission("allyoverride"))){
 			boolean w = FactionCore.getSqlManager().removeListedUnit(faction, "enemy", faction2);
 			if (w) {
 				player.sendMessage(ChatColor.AQUA+"You have remove "+faction2+" as your faction's ally!");
 	
 				Player players[] = plugin.getServer().getOnlinePlayers();
 				for (int i = 0;i < players.length;i++){
 					if (isFactionMember(players[i].getName(), faction) && players[i] != player){
 						players[i].sendMessage(ChatColor.AQUA+faction2+" is no longer your faction's ally!");
 					}
 					if (isFactionMember(players[i].getName(), faction2)){
 						players[i].sendMessage(ChatColor.RED+faction+" is no longer your faction's ally!");
 					}
 				}
 			} else {
 				player.sendMessage(ChatColor.RED+"Failed to remove faction ally!");
 			}
 		} else {
 			player.sendMessage(ChatColor.RED+"You don't have permission to add a faction ally!");
 		}
 		
 		return false;
 	}
 	
 	public boolean addEnemy(Player player, String name){
 		if (name == null) name = "";
 		if (player == null || name.equalsIgnoreCase("")) return false;
 		
 		String faction = FactionCore.getSqlManager().getFactionbyMember(player.getName());
 		if (faction.equalsIgnoreCase("")){
 			player.sendMessage(ChatColor.RED+"You are not in a faction!");
 		}
 		String faction2 = FactionCore.getSqlManager().getFaction(name);
 		if (faction2.equalsIgnoreCase("")){
 			player.sendMessage(ChatColor.RED+"Invalid faction!");
 		}
 		
 		if(isFactionAlly(faction, faction2)){
 			player.sendMessage(ChatColor.RED+"Your faction is allied with that faction!");
 			return false;
 		}
 		if(isFactionAlly(faction2, faction)){
 			player.sendMessage(ChatColor.RED+"That faction is allied with your faction!");
 			return false;
 		}
 		if(isFactionEnemy(faction, faction2)){
 			player.sendMessage(ChatColor.RED+"Your faction is already enemies with that faction!");
 			return false;
 		}
 		
 		boolean can_add = isFactionAdmin(player.getName(), faction2);
 		
 		if (can_add || player.hasPermission(permissions.getPermission("enemyoverride"))){
 			boolean w = FactionCore.getSqlManager().addListedUnit(faction, "enemy", faction2);
 			if (w) {
 				player.sendMessage(ChatColor.AQUA+faction2+" is now your faction's enemy!");
 				
 				Player players[] = plugin.getServer().getOnlinePlayers();
 				for (int i = 0;i < players.length;i++){
 					if (isFactionMember(players[i].getName(), faction) && players[i] != player){
 						players[i].sendMessage(ChatColor.AQUA+faction2+" is now your faction's enemy!");
 					}
 					if (isFactionMember(players[i].getName(), faction2)){
 						players[i].sendMessage(ChatColor.RED+faction+" has declared your faction as an enemy!");
 					}
 				}
 				return true;
 			} else {
 				player.sendMessage(ChatColor.RED+"Failed to add faction enemy!");
 			}
 		} else {
 			player.sendMessage(ChatColor.RED+"You don't have permission to add a faction enemy!");
 		}
 		return false;
 	}
 
 	public boolean removeEnemy(Player player, String name){
 		if (name == null) name = "";
 		if (player == null || name.equalsIgnoreCase("")) return false;
 		
 		String faction = FactionCore.getSqlManager().getFactionbyMember(player.getName());
 		if (faction.equalsIgnoreCase("")){
 			player.sendMessage(ChatColor.RED+"You are not in a faction!");
 		}
 		String faction2 = FactionCore.getSqlManager().getFaction(name);
 		if (faction2.equalsIgnoreCase("")){
 			player.sendMessage(ChatColor.RED+"Invalid faction!");
 		}
 		if(!isFactionEnemy(faction, faction2)){
 			player.sendMessage(ChatColor.RED+"Your faction isn't enemies with that faction!");
 			return false;
 		}
 		
 		boolean can_remove = isFactionAdmin(player.getName(), faction2);
 		
 		if (can_remove || player.hasPermission(permissions.getPermission("enemyoverride"))){
 			boolean w = FactionCore.getSqlManager().removeListedUnit(faction, "enemy", faction2);
 			if (w) {
 				player.sendMessage(ChatColor.AQUA+faction2+" is no longer your faction's enemy!");
 				
 				Player players[] = plugin.getServer().getOnlinePlayers();
 				for (int i = 0;i < players.length;i++){
 					if (isFactionMember(players[i].getName(), faction) && players[i] != player){
 						players[i].sendMessage(ChatColor.AQUA+faction2+" is no longer your faction's enemy!");
 					}
 					if (isFactionMember(players[i].getName(), faction2)){
 						players[i].sendMessage(ChatColor.RED+faction+" has undeclared your faction as an enemy!");
 					}
 				}
 				return true;
 			} else {
 				player.sendMessage(ChatColor.RED+"Failed to remove faction enemy!");
 			}
 		} else {
 			player.sendMessage(ChatColor.RED+"You don't have permission to remove a faction enemy!");
 		}
 		return false;
 	}
	
	private boolean containsIllegalCharacters(String string){
		String allowedcharacters[] = {
				"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r",
				"s","t","u","v","w","x","y","z","0","1","2","3","4","5","6","7","8","9"};
		String chars[] = string.split("");
		for (int i=0; i < chars.length; i++) {
			boolean allowed = false;
			for (int j=0; j < allowedcharacters.length; j++){
				if (chars[i].equalsIgnoreCase(allowedcharacters[j])){
					allowed = true;
					break;
				}
			}
			if (!allowed)
				return true;
		}
		return false;
	}
 }
