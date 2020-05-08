 package me.naithantu.ArenaPVP.Commands;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import me.naithantu.ArenaPVP.ArenaManager;
 import me.naithantu.ArenaPVP.ArenaPVP;
 
 import org.bukkit.command.CommandSender;
 
 import com.google.common.base.Joiner;
 
 public class CommandMaps extends AbstractCommand {
 
 	protected CommandMaps(CommandSender sender, String[] args, ArenaPVP plugin, ArenaManager arenaManager) {
 		super(sender, args, plugin, arenaManager);
 	}
 
 	@Override
 	public boolean handle() {
 		if (!testPermission(sender, "maps") && !testPermission(sender, "mod")) {
 			this.noPermission(sender);
 			return true;
 		}
 
 		List<String> mapNames = new ArrayList<String>();
 		
 		File mapsFolder = new File(plugin.getDataFolder() + File.separator + "maps");
 		for (File map : mapsFolder.listFiles()) {
            String extension = "";

            int i = map.getName().lastIndexOf('.');
            if (i > 0) {
                extension = map.getName().substring(i+1);
            }

            System.out.println(map.getName() + " : " + extension);

			if(extension.equalsIgnoreCase("yml")){
                mapNames.add(map.getName().replaceFirst(".yml", ""));
            }

 		}
 		
 		this.msg(sender, "Available maps: " + Joiner.on(", ").join(mapNames));
 		return true;
 	}
 }
