 package me.naithantu.ArenaPVP.Commands;
 
 import java.io.File;
 
 import me.naithantu.ArenaPVP.ArenaManager;
 import me.naithantu.ArenaPVP.ArenaPVP;
 import me.naithantu.ArenaPVP.Arena.Arena;
 import me.naithantu.ArenaPVP.Storage.YamlStorage;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.entity.Player;
 
 public class CommandCreate extends AbstractCommand {
 
 	protected CommandCreate(CommandSender sender, String[] args, ArenaPVP plugin, ArenaManager arenaManager) {
 		super(sender, args, plugin, arenaManager);
 	}
 
 	@Override
 	public boolean handle() {
 		if(!testPermission(sender, "create") && !testPermission(sender, "admin")){
 			this.noPermission(sender);
 			return true;
 		}
 		
 		if (!(sender instanceof Player)) {
 			this.msg(sender, "That command can only be used in-game.");
 			return true;
 		}
 		
 		if(args.length == 0){
 			this.msg(sender, "Usage: /pvp create [arenaname]");
 			return true;
 		}
 
 		Player player = (Player) sender;
 		
 		String arenaName = args[0].toLowerCase();
 				
		File file = new File(plugin.getDataFolder() + File.separator + "maps", arenaName);
 		if(file.exists()){
 			this.msg(sender, "An arena with that name already exists!");
 			return true;
 		}
 		
 		Arena arena = new Arena(plugin, arenaManager, arenaName);
 		YamlStorage arenaStorage = arena.getArenaStorage();
 		Configuration arenaConfig = arenaStorage.getConfig();
 		arenaConfig.set("nickname", arenaName);
 		arenaStorage.saveConfig();
 		arenaManager.getArenas().put(arenaName, arena);
 		
 		
 		
 		this.msg(sender, "Created a new arena with name " + arenaName + "!");
 		return true;
 	}
 }
