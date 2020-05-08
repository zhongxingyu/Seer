 package me.asofold.bukkit.contextmanager;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CMCommand implements CommandExecutor {
 	
 	/**
 	 * First is the label, rest aliases, for each array.	
 	 */
 	public static final String[][] presetCommandAliases = new String[][]{
 		// main commands
 		{"cmreload"},
 		{"mute", "cmmute"},
 		{"unmute", "demute", "cmunmute"},
 		{"muted"},
 		{"context", "cx"},
 		// sub commands:
 		{"channel", "chan", "ch", "c"},
 		{"world", "wor", "w"},
 		{"info", "?", "help", "hlp"},
 		{"reset", "clear", "clr", "cl", "res"},
 		{"ignore", "ign", "ig" , "i"},
 		{"all", "al", "a"},
 		
 		
 	};
 	
 	private ContextManager man;
 	
 	Map<String, String> commandAliases = new HashMap<String, String>();
 	
 	public CMCommand(ContextManager man){
 		this.man = man;
 		// map aliases to label.
 		for ( String[] ref : presetCommandAliases){
 			String label = ref[0];
 			for ( String n : ref){
 				commandAliases.put(n, label);
 			}
 		}
 	}
 	
 	/**
 	 * Get lower case version, possibly mapped from an abreviation.
 	 * @param input
 	 * @return
 	 */
 	public String getMappedCommandLabel(String input){
 		input = input.trim().toLowerCase();
 		String out = commandAliases.get(input);
 		if (out == null) return input;
 		else return out;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		man.lightChecks();
 		label = getMappedCommandLabel(label);
 		int len = args.length;
 		if ( label.equals("cmreload")){
 			if( !ContextManager.checkPerm(sender, "contextmanager.admin.cmd.reload")) return true;
 			man.loadSettings();
 			sender.sendMessage(ContextManager.plgLabel+" Settings reloaded");
 			return true;
 			
 		} 
 		else if ((len==1 || len==2 ) && label.equals("mute")){
 			if( !ContextManager.checkPerm(sender, "contextmanager.admin.cmd.mute")) return true;
 			int minutes = 0;
 			if ( len == 2){
 				try{
 					minutes = Integer.parseInt(args[1]);
 					if ( minutes <=0 ) throw new NumberFormatException();
 				} catch ( NumberFormatException e){
 					ContextManager.send(sender, ChatColor.DARK_RED+"Bad number for minutes: "+args[1]);
 				}
 			}
 			String name = args[0].trim().toLowerCase();
 			boolean known = ContextManager.isPlayerKnown(name);
 			
 			String end = ".";
 			Long ts = 0L;
 			if (minutes>0){
 				end = " for "+minutes+" minutes.";
 				ts = System.currentTimeMillis() + 60000L* (long)minutes;
 			}
 			man.muted.put(name, ts);
 			ContextManager.send(sender, ChatColor.YELLOW+ContextManager.plgLabel+" Muted "+(known?"":(ChatColor.RED+"unknown "+ChatColor.YELLOW))+"player '"+name+"'"+end);
 			return true;
 		} 
 		else if ((len==1 ) && label.equals("unmute")){
 			if( !ContextManager.checkPerm(sender, "contextmanager.admin.cmd.unmute")) return true;
 			if ( args[0].equals("*")){
 				man.muted.clear();
 				ContextManager.send(sender,ChatColor.YELLOW+ContextManager.plgLabel+" Cleared muted players list.");
 				return true;
 			}
 			String name = args[0].trim().toLowerCase();
 			Long ts = man.muted.remove(name);
 			if ( ts!=null ){
 				ContextManager.send(sender, ChatColor.YELLOW+ContextManager.plgLabel+" Removed from muted: "+name);
 			} else{
 				ContextManager.send(sender, ChatColor.GRAY+ContextManager.plgLabel+" Not in muted: "+name);
 			}
 			return true;
 		} 
 		else if (label.equals("muted")){
 			if( !ContextManager.checkPerm(sender, "contextmanager.admin.cmd.muted")) return true;
 			ContextManager.send(sender, ChatColor.YELLOW+ContextManager.plgLabel+" Muted: "+ChatColor.GRAY+ContextManager.join(man.muted.keySet(), ChatColor.DARK_GRAY+" | "+ChatColor.GRAY));
 			return true;
 		}
 		else if (label.equals("context")){
 			if (!ContextManager.checkPlayer(sender)) return true;
 			return contextCommand((Player) sender, args);
 		}
 		return false;
 	}
 	
 	/**
 	 * Handling a "context" command for a player.
 	 * @param player
 	 * @param args
 	 * @return
 	 */
 	private boolean contextCommand(Player player, String[] args) {
 		int len = args.length;
 		if (len == 0) return false; // send usage info.
 		String cmd = getMappedCommandLabel(args[0]);
 		// TODO: permissions
 		PlayerData data = man.getPlayerData(player.getName());
 		if (cmd.equals("reset")){
 			if (len == 1){
 				data.resetContexts();
 				ContextManager.send(player, ChatColor.YELLOW+ContextManager.plgLabel+" Contexts reset.");
 				return true;
 			} else if (len==2){
 				String target = getMappedCommandLabel(args[1]);
 				if (target.equals("ignore")){
 					data.resetIgnored();
 					ContextManager.send(player, ChatColor.YELLOW+ContextManager.plgLabel+" Ignored players reset.");
 					return true;
 				}
 				else if (target.equals("all")){
 					data.resetAll();
 					ContextManager.send(player, ChatColor.YELLOW+ContextManager.plgLabel+" Everything reset.");
 					return true;
 				}
 			}
 		}
		else if (cmd.equals("ignore") && len > 1){
 			for (int i = 1; i< args.length; i++){
 				String c = args[i].trim().toLowerCase();
 				if (c.isEmpty()) continue;
 				if (c.startsWith("-") && c.length()>1){
 					data.ignored.remove(c.substring(1));
 					continue;
 				}
 				else data.ignored.add(c);
 			}
 			player.sendMessage(ChatColor.DARK_GRAY+"[Ignored] "+ContextManager.join(data.ignored, " | "));
 			return true;
 		}
 		return false;
 	}
 
 }
