 package com.theminequest.MineQuest.Frontend.Command;
 
 import java.lang.reflect.Method;
 import java.util.logging.Level;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.Utils.PropertiesFile;
 
 public abstract class CommandFrontend implements CommandExecutor {
 	
 	public PropertiesFile localization;
 	public String cmdname;
 	
 	public CommandFrontend(String name){
 		cmdname = name;
 		MineQuest.log("[CommandFrontend] Starting Command Frontend for \""+cmdname+"\"...");
 		localization = MineQuest.configuration.localizationConfig;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender arg0, Command arg1, String arg2,
 			String[] arg3) {
 		
 		Player player = null;
 		if (arg0 instanceof Player)
 			player = (Player)arg0;
		if ((player==null) && !allowConsole()){
 			MineQuest.log(Level.WARNING,"[CommandFrontend] No console use for \""+cmdname+"\"...");
 			return false;
 		}
 			
 		
 		if (arg3.length==0)
 			return help(player,arg3);
 
 		String cmd = arg3[0].toLowerCase();
 
 		String[] arguments = shrinkArray(arg3);
 
 		try {
 			Method m = this.getClass().getMethod(cmd, Player.class, String[].class);
 			return (Boolean)m.invoke(this, player, arguments);
 		} catch (Exception e) {
 			return false;
 		}
 	}
 	
 	private String[] shrinkArray(String[] array){
 		if (array.length<=1)
 			return new String[0];
 		String[] toreturn = new String[array.length-1];
 		for (int i=1; i<array.length; i++)
 			toreturn[i-1] = array[i];
 		return toreturn;
 	}
 	
 	public abstract Boolean help(Player p, String[] args);
 	public abstract boolean allowConsole();
 
 }
