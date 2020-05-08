 package me.naithantu.ArenaPVP.Commands;
 
 import me.naithantu.ArenaPVP.ArenaManager;
 import me.naithantu.ArenaPVP.ArenaPVP;
 import me.naithantu.ArenaPVP.Arena.Arena;
 import me.naithantu.ArenaPVP.Arena.ArenaTeam;
 import me.naithantu.ArenaPVP.Storage.YamlStorage;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.entity.Player;
 
 public class CommandSaveInventory extends AbstractCommand {
 
 	protected CommandSaveInventory(CommandSender sender, String[] args, ArenaPVP plugin, ArenaManager arenaManager) {
 		super(sender, args, plugin, arenaManager);
 	}
 
 	@Override
 	public boolean handle() {
 		if (!testPermission(sender, "saveinventory") && !testPermission(sender, "mod")) {
 			this.noPermission(sender);
 			return true;
 		}
 
 		if (!(sender instanceof Player)) {
 			this.msg(sender, "That command can only be used in-game.");
 			return true;
 		}
 
 		Player player = (Player) sender;
 
 		if (arenaManager.getArenas().size() == 0) {
 			this.msg(sender, "There are currently no arenas that you can change.");
 			return true;
 		}
 
 		if (arenaManager.getArenas().size() > 1) {
 			// If there are several arenas, find out what arena players want to join.
 			if (args.length < 2) {
 				this.msg(sender, "There are currently several arenas to change, please specify the arena you want to change.");
 				this.msg(sender, "/pvp saveinventory <arenaname> <teamname>");
 				return true;
 			}
 
 			if (arenaManager.getArenas().containsKey(args[0])) {
 				Arena arena = arenaManager.getArenas().get(args[0]);
 				if(arena.getTeam(args[1]) != null){
 					saveInventory(arena, player, arena.getTeam(args[1]));
 				} else {
 					this.msg(sender, "No team with given name was found, type /pvp teams to see available teams.");
 				}
 			} else {
 				this.msg(sender, "No arena with given name was found, type /pvp arenas to see available arenas.");
 			}
 			return true;
 		} else {
 			//If there is only arena
 			Arena arena = arenaManager.getFirstArena();
 			if (args.length == 0) {
 				this.msg(sender, "Please specify the team you want to change.");
 				this.msg(sender, "/pvp saveinventory <teamname>");
 				return true;
 			}
 			
 			if(arena.getTeam(args[0]) != null){
 				saveInventory(arena, player, arena.getTeam(args[0]));
 			} else {
 				this.msg(sender, "No team with given name was found, type /pvp teams to see available teams.");
 			}
 		}
 		return true;
 	}
 
 	public void saveInventory(Arena arena, Player player, ArenaTeam team){
 		YamlStorage arenaStorage = arena.getArenaStorage();
 		Configuration arenaConfig = arenaStorage.getConfig();
		int teamNumber = arena.getTeams().indexOf(team);
 		arenaConfig.set("classes." + teamNumber + ".inventory", player.getInventory().getContents());
 		arenaConfig.set("classes." + teamNumber + ".armor", player.getInventory().getArmorContents());
 		arenaStorage.saveConfig();
 	}
 }
