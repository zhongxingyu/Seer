 package com.deaboy.manhunt.commands;
 
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.deaboy.manhunt.Manhunt;
 import com.deaboy.manhunt.lobby.Lobby;
 import com.deaboy.manhunt.lobby.LobbyType;
 import com.deaboy.manhunt.map.Map;
 import com.deaboy.manhunt.map.Spawn;
 import com.deaboy.manhunt.map.SpawnType;
 import com.deaboy.manhunt.map.Zone;
 import com.deaboy.manhunt.map.ZoneFlag;
 
 public class CommandUtil
 {
 	//////// STANDARD COMMAND ERROR MESSAGES ////////
 	public static final String NO_PERMISSION = ChatColor.RED + "You don't have permission to do that.";
 	public static final String GAME_RUNNING = ChatColor.RED + "You can't do that while the game is running.";
 	public static final String NO_GAME_RUNNING = ChatColor.RED + "There are no Manhunt games running.";
 	public static final String IS_SERVER = ChatColor.RED + "The server cannot do that.";
 	public static final String WRONG_WORLD = ChatColor.RED + "The player must be in the manhunt world first!";
 	public static final String LOCKED = ChatColor.RED + "The teams are locked.";
 	public static final String INVALID_USAGE = ChatColor.RED + "Invalid usage.";
 	public static final String NO_WORLD_BY_NAME = ChatColor.RED + "There is no Manhunt world by that name!";
 	public static final String MAP_NOT_SELECTED = ChatColor.RED + "You must select a map first.\n"
 			+ ChatColor.GRAY + " Selecting a map: /mmap -s <mapname>\n"
 			+ ChatColor.GRAY + " Seeing registered maps: /mmap -list [all] [-page <page>]";
 	public static final String CORNER_PRIMARY_NOT_SELECTED = ChatColor.RED + "Select a corner using left-click with a wooden axe.";
 	public static final String CORNER_SECONDARY_NOT_SELECTED = ChatColor.RED + "Select a corner using right-click with a wooden axe.";
 	
 	
 	
 	
 	
 	//////// COMMAND TEMPLATES ////////
 	// Arguments
 	public static final ArgumentTemplate arg_help	= new ArgumentTemplate("help", ArgumentType.FLAG).addAlias("?").finalize_();
 	public static final ArgumentTemplate arg_info	= new ArgumentTemplate("info", ArgumentType.FLAG).addAlias("i").finalize_();
 	public static final ArgumentTemplate arg_select	= new ArgumentTemplate("select", ArgumentType.TEXT).addAlias("s").addAlias("sel").finalize_();
 	public static final ArgumentTemplate arg_list	= new ArgumentTemplate("list", ArgumentType.TEXT).addAlias("ls").finalize_();
 	public static final ArgumentTemplate arg_page	= new ArgumentTemplate("page", ArgumentType.TEXT).addAlias("pg").addAlias("p").finalize_();
 	public static final ArgumentTemplate arg_join	= new ArgumentTemplate("join", ArgumentType.TEXT).addAlias("jn").finalize_();
 	public static final ArgumentTemplate arg_leave	= new ArgumentTemplate("leave", ArgumentType.FLAG).addAlias("lv").finalize_();
 	public static final ArgumentTemplate arg_create	= new ArgumentTemplate("create", ArgumentType.TEXT).addAlias("cr").addAlias("new").finalize_();
 	public static final ArgumentTemplate arg_delete	= new ArgumentTemplate("delete", ArgumentType.FLAG).addAlias("del").addAlias("remove").addAlias("rm").finalize_();
 	public static final ArgumentTemplate arg_name	= new ArgumentTemplate("name", ArgumentType.TEXT).addAlias("n").addAlias("nm").finalize_();
 	public static final ArgumentTemplate arg_world	= new ArgumentTemplate("world", ArgumentType.TEXT).addAlias("w").addAlias("worlds").finalize_();
 	public static final ArgumentTemplate arg_close	= new ArgumentTemplate("close", ArgumentType.FLAG).finalize_();
 	public static final ArgumentTemplate arg_open	= new ArgumentTemplate("open", ArgumentType.FLAG).finalize_();
 	public static final ArgumentTemplate arg_lsmaps	= new ArgumentTemplate("listmaps", ArgumentType.TEXT).addAlias("listm").addAlias("lsmaps").addAlias("lsm").finalize_();
 	public static final ArgumentTemplate arg_addmap	= new ArgumentTemplate("addmap", ArgumentType.TEXT).addAlias("addm").finalize_();
 	public static final ArgumentTemplate arg_remmap	= new ArgumentTemplate("removemap", ArgumentType.TEXT).addAlias("remmap").addAlias("remm").finalize_();
 	public static final ArgumentTemplate arg_tp		= new ArgumentTemplate("teleport", ArgumentType.TEXT).addAlias("tp").finalize_();
 	public static final ArgumentTemplate arg_range	= new ArgumentTemplate("range", ArgumentType.TEXT).addAlias("r").finalize_();
 	public static final ArgumentTemplate arg_redefine		= new ArgumentTemplate("redefine", ArgumentType.FLAG).addAlias("define").addAlias("move").finalize_();
 	public static final ArgumentTemplate arg_zoneflags		= new ArgumentTemplate("flags", ArgumentType.CHECK).addAlias("flag").addAlias("fl");
 	public static final ArgumentTemplate arg_lobbytype		= new ArgumentTemplate("type", ArgumentType.RADIO).addAlias("t");
 	public static final ArgumentTemplate arg_pointtype		= new ArgumentTemplate("type", ArgumentType.RADIO).addAlias("t");
 	static {	// Fill parameters in certain arguments and finalize.
 		for (ZoneFlag flag : ZoneFlag.values())
 			arg_zoneflags.addParameter(flag.getName().toLowerCase());
 		arg_zoneflags.finalize_();
 		
 		for (LobbyType type : LobbyType.values())
 			arg_lobbytype.addParameter(type.getName().toLowerCase());
 		arg_lobbytype.finalize_();
 		
 		for (SpawnType type : SpawnType.values())
 			arg_pointtype.addParameter(type.getName().toLowerCase());
 		arg_pointtype.finalize_();
 	}
 	
 
 	private static HashMap<String, CommandTemplate> command_templates;
 	public static final CommandTemplate cmd_mlobby	= new CommandTemplate("mlobby")
 			.addAlias("lobby")
 			.addAlias("mhlobby")
 			.addArgument(arg_info)
 			.addArgument(arg_select)
 			.addArgument(arg_list)
 			.addArgument(arg_page)
 			.addArgument(arg_join)
 			.addArgument(arg_create)
 			.addArgument(arg_delete)
 			.addArgument(arg_lobbytype)
 			.addArgument(arg_name)
 			.addArgument(arg_open)
 			.addArgument(arg_close)
 			.addArgument(arg_lsmaps)
 			.addArgument(arg_addmap)
 			.addArgument(arg_remmap);
 	public static final CommandTemplate cmd_mmap	= new CommandTemplate("mmap")
 			.addAlias("map")
 			.addAlias("mhmap")
 			.addArgument(arg_info)
 			.addArgument(arg_select)
 			.addArgument(arg_list)
 			.addArgument(arg_world)
 			.addArgument(arg_create)
 			.addArgument(arg_delete)
 			.addArgument(arg_name);
 	public static final CommandTemplate cmd_mzone	= new CommandTemplate("mzone")
 			.addAlias("zone")
 			.addAlias("mhzone")
 			.addArgument(arg_info)
 			.addArgument(arg_select)
 			.addArgument(arg_list)
 			.addArgument(arg_page)
 			.addArgument(arg_create)
 			.addArgument(arg_delete)
 			.addArgument(arg_name)
 			.addArgument(arg_zoneflags)
 			.addArgument(arg_redefine);
 	public static final CommandTemplate cmd_mpoint	= new CommandTemplate("mpoint")
 			.addAlias("point")
 			.addAlias("mhpoint")
 			.addArgument(arg_info)
 			.addArgument(arg_select)
 			.addArgument(arg_list)
 			.addArgument(arg_page)
 			.addArgument(arg_create)
			.addArgument(arg_create)
 			.addArgument(arg_name)
 			.addArgument(arg_pointtype)
 			.addArgument(arg_redefine)
 			.addArgument(arg_range);
 	static {	// Store references to command templates in hashmap and add global arguments
 		command_templates = new HashMap<String, CommandTemplate>();
 		command_templates.put(cmd_mlobby.getName(), cmd_mlobby);
 		command_templates.put(cmd_mmap.getName(), cmd_mmap);
 		command_templates.put(cmd_mzone.getName(), cmd_mzone);
 		command_templates.put(cmd_mpoint.getName(), cmd_mpoint);
 		
 		for (CommandTemplate cmd : command_templates.values())
 			cmd.addArgument(arg_help).finalize_();
 	}
 	
 	
 	//////// PROPERTIES ////////
 	private HashMap<String, String> selected_maps;
 	private HashMap<String, Long> selected_lobbies;
 	private HashMap<String, String> selected_zones;
 	private HashMap<String, String> selected_points;
 	private HashMap<CommandSender, String> vcommands;
 	private HashMap<CommandSender, Boolean> verified;
 	
 	
 	
 	//---------------- Constructors ----------------//
 	public CommandUtil()
 	{
 		this.selected_maps = new HashMap<String, String>();
 		this.selected_lobbies = new HashMap<String, Long>();
 		this.selected_zones = new HashMap<String, String>();
 		this.selected_points = new HashMap<String, String>();
 		this.vcommands = new HashMap<CommandSender, String>();
 		this.verified = new HashMap<CommandSender, Boolean>();
 	}
 	
 	
 	
 	//---------------- Command Stuff ----------------//
 	public static Command parseCommand(org.bukkit.command.Command cmd, String[] arguments)
 	{
 		Command command;
 		
 		if (command_templates.containsKey(cmd.getName()))
 		{
 			command = Command.fromTemplate(command_templates.get(cmd.getName()), cmd.getLabel(), arguments);
 		}
 		else
 		{
 			command = null;
 		}
 		
 		return command;
 	}
 	
 	//---------------- Map Selection ----------------//
 	public static void setSelectedMap(CommandSender sender, Map map)
 	{
 		Manhunt.getCommandUtil().selected_maps.put(sender.getName(), map.getFullName());
 	}
 	public static Map getSelectedMap(CommandSender sender)
 	{
 		if (Manhunt.getCommandUtil().selected_maps.containsKey(sender.getName()))
 			return Manhunt.getMap(Manhunt.getCommandUtil().selected_maps.get(sender.getName()));
 		else
 			return null;
 	}
 	public static Map getSelectedMap(String name)
 	{
 		if (Manhunt.getCommandUtil().selected_maps.containsKey(name))
 			return Manhunt.getMap(Manhunt.getCommandUtil().selected_maps.get(name));
 		else
 			return null;
 	}
 	
 	
 	
 	//---------------- Lobby Selection ----------------//
 	public static void setSelectedLobby(CommandSender sender, Lobby lobby)
 	{
 		Manhunt.getCommandUtil().selected_lobbies.put(sender.getName(), lobby.getId());
 	}
 	public static Lobby getSelectedLobby(CommandSender sender)
 	{
 		return getSelectedLobby(sender.getName());
 	}
 	public static Lobby getSelectedLobby(String name)
 	{
 		if (Manhunt.getCommandUtil().selected_lobbies.containsKey(name))
 			return Manhunt.getLobby(Manhunt.getCommandUtil().selected_lobbies.get(name));
 		else
 			return null;
 	}
 	
 	
 	//---------------- Zone Selection -----------------//
 	public static void setSelectedZone(CommandSender sender, Zone zone)
 	{
 		Manhunt.getCommandUtil().selected_zones.put(sender.getName(), zone.getName());
 	}
 	public static Zone getSelectedZone(CommandSender sender)
 	{
 		return getSelectedZone(sender.getName());
 	}
 	public static Zone getSelectedZone(String name)
 	{
 		if (Manhunt.getCommandUtil().selected_zones.containsKey(name))
 			if (getSelectedMap(name) != null)
 				return getSelectedMap(name).getZone(Manhunt.getCommandUtil().selected_zones.get(name));
 			else
 				return null;
 		else
 			return null;
 	}
 	
 	
 	//---------------- Spawn Selection ----------------//
 	public static void setSelectedPoint(CommandSender sender, Spawn point)
 	{
 		Manhunt.getCommandUtil().selected_points.put(sender.getName(), point.getName());
 	}
 	public static Spawn getSelectedPoint(CommandSender sender)
 	{
 		return getSelectedPoint(sender.getName());
 	}
 	public static Spawn getSelectedPoint(String name)
 	{
 		if (Manhunt.getCommandUtil().selected_points.containsKey(name))
 			if (getSelectedMap(name) != null)
 				return getSelectedMap(name).getPoint(Manhunt.getCommandUtil().selected_points.get(name));
 			else
 				return null;
 		else
 			return null;
 	}
 	
 	
 	
 	//---------------- Verification ----------------//
 	public static void addVerifyCommand(CommandSender sender, String command, String message)
 	{
 		addVerifyCommand(sender, command);
 		sender.sendMessage(message);
 		sender.sendMessage(ChatColor.GRAY + "To confirm, type \"/mverify\" or \"/cancel\"");
 	}
 	public static void addVerifyCommand(CommandSender sender, String command)
 	{
 		Manhunt.getCommandUtil().vcommands.put(sender, command);
 		Manhunt.getCommandUtil().verified.put(sender, false);
 	}
 	public static boolean isVerified(CommandSender sender)
 	{
 		if (isVerifying(sender))
 			return Manhunt.getCommandUtil().verified.get(sender);
 		else
 			return false;
 	}
 	public static boolean isVerifying(CommandSender sender)
 	{
 		return (Manhunt.getCommandUtil().verified.containsKey(sender)
 				&& Manhunt.getCommandUtil().vcommands.containsKey(sender));
 	}
 	public static void executeCommand(CommandSender sender)
 	{
 		if (isVerifying(sender))
 		{
 			Manhunt.getCommandUtil().verified.put(sender,true);
 			Bukkit.dispatchCommand(sender, Manhunt.getCommandUtil().vcommands.get(sender));
 			cancelCommand(sender);
 		}
 	}
 	public static void cancelCommand(CommandSender sender)
 	{
 		if (Manhunt.getCommandUtil().vcommands.containsKey(sender))
 			Manhunt.getCommandUtil().vcommands.remove(sender);
 		if (Manhunt.getCommandUtil().verified.containsKey(sender))
 			Manhunt.getCommandUtil().verified.remove(sender);
 	}
 	public void deletePlayer(Player player)
 	{
 		if (selected_lobbies.containsKey(player.getName()))
 			selected_lobbies.remove(player.getName());
 		if (selected_maps.containsKey(player.getName()))
 			selected_maps.remove(player.getName());
 		if (vcommands.containsKey(player.getName()))
 			vcommands.remove(player.getName());
 		if (verified.containsKey(player.getName()))
 			verified.remove(player.getName());
 	}
 	
 	
 	
 }
