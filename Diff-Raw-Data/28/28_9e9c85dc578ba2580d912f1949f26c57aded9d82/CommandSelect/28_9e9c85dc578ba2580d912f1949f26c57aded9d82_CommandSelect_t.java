 package me.naithantu.ArenaPVP.Commands;
 
 import me.naithantu.ArenaPVP.ArenaManager;
 import me.naithantu.ArenaPVP.ArenaPVP;
 import me.naithantu.ArenaPVP.Arena.Arena;
 import me.naithantu.ArenaPVP.Arena.ArenaExtras.ArenaValidator;
 
 import org.bukkit.command.CommandSender;
 
import java.io.File;

 public class CommandSelect extends AbstractCommand {
 
 	protected CommandSelect(CommandSender sender, String[] args, ArenaPVP plugin, ArenaManager arenaManager) {
 		super(sender, args, plugin, arenaManager);
 	}
 
 	@Override
 	public boolean handle() {
 		if(!testPermission(sender, "select") && !testPermission(sender, "mod")){
 			this.noPermission(sender);
 			return true;
 		}
 		
 		if(args.length == 0){
 			this.msg(sender, "Usage: /pvp select [arenaname]");
 			return true;
 		}
 		
 		String arenaName = args[0].toLowerCase();
 		
 		if(arenaManager.getArenas().containsKey(arenaName)){
 			this.msg(sender, "That arena is already loaded! You can unload it with /pvp stop [arenaname].");
 			return true;
 		}

        File file = new File(plugin.getDataFolder() + File.separator + "maps", arenaName + ".yml");
        if(!file.exists()){
            this.msg(sender, "That arena does not exist, use /pvp maps to see available arenas!");
            return true;
        }

 		Arena arena = new Arena(plugin, arenaManager, arenaName);
 		arenaManager.getArenas().put(arenaName, arena);
 		
 		this.msg(sender, "Loaded arena with name " + arena.getArenaName() + "!");
 		return true;
 	}
 }
