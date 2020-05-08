 package org.mcsg.double0negative.supercraftbros;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Vector;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.mcsg.double0negative.supercraftbros.commands.CreateArenaCommand;
 import org.mcsg.double0negative.supercraftbros.commands.DisableCommand;
 import org.mcsg.double0negative.supercraftbros.commands.EnableCommand;
 import org.mcsg.double0negative.supercraftbros.commands.LeaveCommand;
 import org.mcsg.double0negative.supercraftbros.commands.SetLobbyGameSpawn;
 import org.mcsg.double0negative.supercraftbros.commands.SetLobbySpawnCommand;
 import org.mcsg.double0negative.supercraftbros.commands.SetSpawnCommand;
 import org.mcsg.double0negative.supercraftbros.commands.StartCommand;
 import org.mcsg.double0negative.supercraftbros.commands.SubCommand;
 
 
 public class CommandHandler implements CommandExecutor
 {
 	private Plugin plugin;
 	private HashMap<String, SubCommand> commands;
 
 	public CommandHandler(Plugin plugin)
 	{
 		this.plugin = plugin;
 		commands = new HashMap<String, SubCommand>();
 		loadCommands();
 	}
 
 	private void loadCommands()
 	{
 		commands.put("createarena", new CreateArenaCommand());
 		commands.put("disable", new DisableCommand());
 		commands.put("enable", new EnableCommand());
 		commands.put("setlobbyspawn", new SetLobbySpawnCommand());
 		commands.put("setlobby", new SetLobbyGameSpawn());
 		commands.put("setspawn", new SetSpawnCommand());
 		commands.put("leave", new LeaveCommand());
 		commands.put("start", new StartCommand());
 
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd1, String commandLabel, String[] args){
 		String cmd = cmd1.getName();
 		PluginDescriptionFile pdfFile = plugin.getDescription();
 
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		else{
 			System.out.println("Only ingame players can use Super Craft Bros commands");
 			return true;
 		}
 
 
 		if(cmd.equalsIgnoreCase("scb")){ 
 			if(args == null || args.length < 1){
 				player.sendMessage(ChatColor.GOLD +""+ ChatColor.BOLD +"Super Craft Bros - Double0negative"+ChatColor.RESET+  ChatColor.YELLOW +" Version: "+ pdfFile.getVersion() );
 				//player.sendMessage(ChatColor.GOLD +"Type /scb help for help" );
 
 				return true;
 			}
 			if(args[0].equalsIgnoreCase("help")){
 				help(player);
 				return true;
 			}
 			String sub = args[0];
 
 			Vector<String> l  = new Vector<String>();
 			l.addAll(Arrays.asList(args));
 			l.remove(0);
 			args = (String[]) l.toArray(new String[0]);
 			if(!commands.containsKey(sub)){
 				player.sendMessage(ChatColor.RED+"Command dosent exist.");
 				//player.sendMessage(ChatColor.GOLD +"Type /scb help for help" );
 				return true;
 			}
 			try{
 
 				commands.get(sub).onCommand( player,  args);
			}catch(Exception e){e.printStackTrace(); player.sendMessage(ChatColor.RED+"An error occured while executing the command. Check the      console");                player.sendMessage(ChatColor.BLUE +"Type /scb help for help" );
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public void help(Player p){
 		/*
 		p.sendMessage("/scb <command> <args>");
 		for(SubCommand v: commands.values()){
 			p.sendMessage(ChatColor.AQUA +v.help(p));
 		}*/
 	}
 }
