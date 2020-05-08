 package me.corriekay.pppopp3.ponymanager;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import me.corriekay.pppopp3.Mane;
 import me.corriekay.pppopp3.events.JoinEvent;
 import me.corriekay.pppopp3.events.QuitEvent;
 import me.corriekay.pppopp3.ponyville.Pony;
 import me.corriekay.pppopp3.ponyville.Ponyville;
 import me.corriekay.pppopp3.utils.PSCmdExe;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.permissions.PermissionAttachment;
 
 public class PonyManager extends PSCmdExe {
 	
 	public static PonyManager ponyManager;
 	private HashMap<String,PonyGroup> groups = new HashMap<String,PonyGroup>();
 	private final FileConfiguration groupConfig = getNamedConfig("permissions.yml");
 	private final HashMap<String,HashSet<String>> groupsList = new HashMap<String,HashSet<String>>();
 	private final HashMap<String,PermissionAttachment> playerPerms = new HashMap<String,PermissionAttachment>();
 	private PermissionAttachment consolePerms= Bukkit.getConsoleSender().addAttachment(Mane.getInstance());
 	
 	public PonyManager(){
 		super("PonyManager",new String[]{"setgroup","list","groupaddperm","groupdelperm","useraddperm","userdelperm"});
 		ponyManager = this;
 		initialize();
 	}
 	private void initialize(){
 		groups.clear();
 		groupsList.clear();
 		for(String group : groupConfig.getConfigurationSection("groups").getKeys(false)){
 			groups.put(group, new PonyGroup(groupConfig,group));
 			groupsList.put(group,  new HashSet<String>());
 		}
 		calculateAllPerms();
 		ConsoleCommandSender ccs = Bukkit.getConsoleSender();
 		ccs.removeAttachment(consolePerms);
 		consolePerms = ccs.addAttachment(Mane.getInstance());
 		for(String perm : groupConfig.getStringList("consolePerms")){
 			boolean add = !perm.startsWith("-");
 			if(!add){
 				perm = perm.substring(1,perm.length());
 			}
 			consolePerms.setPermission(perm, add);
 		}
 	}
 	private void calculateAllPerms(){
 		for(Player player : Bukkit.getOnlinePlayers()){
 			calculatePerms(player,null);
 		}
 	}
 	private void calculatePerms(Player player, World w){
 		PermissionAttachment pa = playerPerms.get(player.getName());
 		if(pa!=null){
 			try{
 				player.removeAttachment(pa);
 			} catch (IllegalArgumentException e){
 				System.out.println("exception thrown: player does not have permission attachment.");
 			}
 		}
 		pa = player.addAttachment(Mane.getInstance());
 		Pony p = Ponyville.getPony(player);
 		PonyGroup group = groups.get(p.getGroup());
 		HashMap<String,Boolean> perms = null;
 		if(w==null){
 			perms = group.getPermissions(player.getWorld());
 		} else {
 			perms = group.getPermissions(w);
 		}
 		{
 			HashSet<String> negPerms = new HashSet<String>();
 			for(String perm : p.getPerms()){
 				if(perm.startsWith("-")){
 					negPerms.add(perm.substring(1,perm.length()));
 				} else {
 					perms.put(perm,true);
 				}
 			}
 			for(String negPerm : negPerms){
 				perms.put(negPerm,false);
 			}
 		}
 		for(String perm : perms.keySet()){
 			pa.setPermission(perm, perms.get(perm));
 		}
 		for(String g : groupsList.keySet()){
 			groupsList.get(g).remove(player.getName());
 		}
 		groupsList.get(group.getName()).add(player.getName());
 		playerPerms.put(player.getName(),pa);
 	}
 	@EventHandler
 	public void onJoin(JoinEvent event){
 		if (event.isJoining()) {
 			calculatePerms(event.getPlayer(),null);
 		}
 	}
 	@EventHandler
 	public void onQuit(QuitEvent event){
 		if(event.isQuitting()){
 			for(String g : groupsList.keySet()){
 				groupsList.get(g).remove(event.getPlayer().getName());
 			}
 			playerPerms.remove(event.getPlayer().getName());
 		}
 	}
 	@EventHandler
 	public void onTeleport(PlayerTeleportEvent event){
 		calculatePerms(event.getPlayer(),event.getTo().getWorld());
 	}
 	@SuppressWarnings({ "unchecked", "unused" })
 	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
 		if(cmd.getName().equals("manuadd")){
 			sendMessage(sender,"Command changed! new command is /setgroup <group>!");
 			return true;
 		}
 		if(cmd.getName().equals("list")){
 			int counter = 0;
 			for(Player p : Bukkit.getOnlinePlayers()){
 				//if(!InvisibilityHandler.ih.isHidden(p.getName())||sender.hasPermission("pppopp2.seehidden")){
 					counter++;
 				//}//TODO invisibility check
 			}
 			sendMessage(sender,"There are "+counter+" of "+Bukkit.getServer().getMaxPlayers()+" max players online!");
 			boolean invsee = sender.hasPermission("pppopp2.seehidden");
 			HashSet<String> admins = (HashSet<String>) groupsList.get("admin").clone();
 			HashSet<String> opony = (HashSet<String>) groupsList.get("opony").clone();
 			HashSet<String> pegasus = (HashSet<String>) groupsList.get("pegasus").clone();
 			HashSet<String> unicorn = (HashSet<String>) groupsList.get("unicorn").clone();
 			HashSet<String> donator = (HashSet<String>) groupsList.get("donator").clone();
 			HashSet<String> pony = (HashSet<String>) groupsList.get("pony").clone();
 			HashSet<String> filly = (HashSet<String>) groupsList.get("filly").clone();
 			if(!invsee){
 				removeHidden(admins);
 				removeHidden(opony);
 				removeHidden(pegasus);
 				removeHidden(unicorn);
 				removeHidden(donator);
 				removeHidden(pony);
 				removeHidden(filly);
 			}
 			if(admins.size()>0){
 				sender.sendMessage(createList(ChatColor.LIGHT_PURPLE+"Admins: ",admins));
 			}
 			if(opony.size()>0){
 				sender.sendMessage(createList(ChatColor.DARK_GREEN+"OPonies: ",opony));
 			}
 			if(pegasus.size()>0){
 				sender.sendMessage(createList(ChatColor.DARK_AQUA+"Pegasi: ",pegasus));
 			}
 			if(unicorn.size()>0){
 				sender.sendMessage(createList(ChatColor.DARK_PURPLE+"Unicorns: ", unicorn));
 			}
 			if(donator.size()>0){
 				sender.sendMessage(createList(ChatColor.GOLD+"Donators: ",donator));
 			}
 			if(pony.size()>0){
 				sender.sendMessage(createList(ChatColor.AQUA+"Ponies: ",pony));
 			}
 			if(filly.size()>0){
 				sender.sendMessage(createList(ChatColor.DARK_GRAY+"Fillies: ",filly));
 			}
 			return true;
 		}
 		String cmdn = cmd.getName();
 		if(args.length<2){
 			sendMessage(sender,notEnoughArgs);
 			return true;
 		}
 		if(cmdn.equals("setgroup")){
 			PonyGroup group = groups.get(args[1].toLowerCase());
 			if(group == null){
 				sendMessage(sender,"Hmm... I cant seem to find that group!");
 				return true;
 			}
 			String target = this.getSinglePlayer(args[0], sender);
 			if(target == null){
 				return true;
 			}
 			if(!group.canMoveTo(sender)){
 				sendMessage(sender,"Hey, you cant put them in that group! Whaddya try'na do, break the server??");
 				return true;
 			}
 			setGroup(target,group);
 			Bukkit.broadcast(pinkieSays+target+"+ was moved to group "+group.getName(),"pppopp3.pm.alertgroupchange");
 			//TODO RPA MESSAGE
 			return true;
 		}
 		if(cmdn.equals("groupaddperm")){
 			if(args.length < 3 ){
 				sendMessage(sender,notEnoughArgs);
 				return true;
 			}
 			PonyGroup group = groups.get(args[0]);
 			if(group == null){
 				sendMessage(sender,"Hmm... I cant seem to find that group!");
 				return true;
 			}
 			String path = "groups."+group.getName()+".";
 			if(args[2].equalsIgnoreCase("equestria")){
 				path += "worldPermissions.equestria";
 			} else if (args[2].equalsIgnoreCase("world")){
 				path+= "worldPermissions.world";
 			} else if (args[2].equalsIgnoreCase("badlands")){
 				path+="worldPermissions.badlands";
 			} else {
 				path+="globalPermissions";
 			}
 			List<String> perms = groupConfig.getStringList(path);
 			if(perms.contains(args[1])){
 				sendMessage(sender,"Oh you... That group already has that permission!");
 				return true;
 			}
 			perms.add(args[1]);
 			groupConfig.set(path,perms);
 			saveConfig();
 			initialize();
 			sendMessage(sender,"Yay! You added "+args[1]+" to group "+group.getName()+"!");
 			return true;
 		}
 		if(cmdn.equals("groupdelperm")){
 			if(args.length < 3 ){
 				sendMessage(sender,notEnoughArgs);
 				return true;
 			}
 			PonyGroup group = groups.get(args[0]);
 			if(group == null){
 				sendMessage(sender,"Hmm... I cant seem to find that group!");
 				return true;
 			}
 			String path = "groups."+group.getName()+".";
 			if(args[2].equalsIgnoreCase("equestria")){
 				path += "worldPermissions.equestria";
 			} else if (args[2].equalsIgnoreCase("world")){
 				path+= "worldPermissions.world";
 			} else if (args[2].equalsIgnoreCase("badlands")){
 				path+="worldPermissions.badlands";
 			} else {
 				path+="globalPermissions";
 			}
 			List<String> perms = groupConfig.getStringList(path);
 			if(!perms.contains(args[1])){
 				sendMessage(sender,"That group doesnt have that permission! What do you want me to do, make a DOUBLE NEGATIVE PERMISSION??");
 				return true;
 			}
 			perms.remove(args[1]);
 			groupConfig.set(path, perms);
 			saveConfig();
 			initialize();
 			sendMessage(sender,"Removed permission "+args[1]+" from group "+group.getName()+"!");
 			return true;
 		}
 		if(cmdn.equals("useraddperm")){
 			String target = getSinglePlayer(args[0],sender);
 			if(target == null){
 				return true;
 			}
 			Pony p = Ponyville.getOfflinePony(target);
 			ArrayList<String> perms = p.getPerms();
 			if(perms.contains(args[1])){
 				sendMessage(sender,"Huh. That user already has that permission! :D");
 				return true;
 			}
 			perms.add(args[1]);
 			p.setPerms(perms);
 			p.save();
 			sendMessage(sender,"You added "+args[1] +" to "+target +"'s permissions list!");
 			if(p.getPlayer().isOnline()){
 				Player player = (Player)p.getPlayer();
 				calculatePerms(player,null);
 			}
 			return true;
 		}
 		if(cmdn.equals("userdelperm")){
 			String target = getSinglePlayer(args[0],sender);
 			if(target == null){
 				return true;
 			}
 			Pony p = Ponyville.getOfflinePony(target);
 			ArrayList<String> perms = p.getPerms();
 			if(!perms.contains(args[1])){
 				sendMessage(sender,"Huh. That user doesnt have that permission anyways! :D");
 				return true;
 			}
 			perms.remove(args[1]);
 			p.setPerms(perms);
 			p.save();
 			sendMessage(sender,"You removed "+args[1] +" to "+target +"'s permissions list!");
 			if(p.getPlayer().isOnline()){
 				Player player = (Player)p.getPlayer();
 				calculatePerms(player,null);
 			}
 			return true;
 		}
 		return true;
 	}
 	private void setGroup(String target, PonyGroup group){
 		Pony p = Ponyville.getPony(target);
 		p.setGroup(group.getName());
 		p.save();
 		OfflinePlayer op = p.getPlayer();
 		if(op.isOnline()){
 			Player player = (Player)op;
 			sendMessage(player,"Yay! You're a pretty "+group.getName()+"!!!");
 			calculatePerms(player,null);
 		}
 	}
 	@SuppressWarnings("unused")
 	private void removeHidden(HashSet<String> removefrom){
 		HashSet<String> removeMe = new HashSet<String>();
 		for(String p : removefrom){
 			//if(InvisibilityHandler.ih.isHidden(p)){
 				//removeMe.add(p);
 			//}
 			//TODO implement invisibility
 		}
 		removefrom.removeAll(removeMe);
 	}
 	private String createList(String message, HashSet<String> players){
 		for(String p : players){
 			Player player = Bukkit.getPlayerExact(p);
 			message+=ChatColor.RED+player.getDisplayName()+ChatColor.WHITE+", ";
 		}
 		message = message.substring(0,message.length()-4);
 		return message;
 	}
 	private void saveConfig(){
 		saveNamedConfig("permissions.yml",groupConfig);
 	}
 	public static String getGroup(Player player){
 		return getGroup(player.getName());
 	}
 	public static String getGroup(String player){
 		Pony p = Ponyville.getOfflinePony(player);
 		return(p.getGroup());
 	}
	public void deactivate(){
		for(Player p : Bukkit.getOnlinePlayers()){
			PermissionAttachment pa = playerPerms.get(p.getName());
			if(pa!=null){
				try {
					p.removeAttachment(pa);
				} catch (Exception e) {
					continue;
				}
			}
		}
	}
 }
