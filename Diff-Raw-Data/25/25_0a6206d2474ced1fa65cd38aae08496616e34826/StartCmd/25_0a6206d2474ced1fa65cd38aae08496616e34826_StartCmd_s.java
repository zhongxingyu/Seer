 package me.minebuilders.hg.commands;
 
 import me.minebuilders.hg.Game;
 import me.minebuilders.hg.HG;
 import me.minebuilders.hg.Util;
 
 import org.bukkit.ChatColor;
 
 public class StartCmd extends BaseCmd {
 
 	public StartCmd() {
 		forcePlayer = false;
 		cmdName = "forcestart";
 		forceInGame = false;
 		argLength = 2;
 		usage = "<&cgame&b>";
 	}
 
 	@Override
 	public boolean run() {
 		Game g = HG.manager.getGame(args[1]);
 		if (g != null) {
 			g.startPreGame();
			Util.scm(sender, "&3" + args[1] + " &b Is now starting!");
 		} else {
 			sender.sendMessage(ChatColor.RED + "This game does not exist!");
 		}
 		return true;
 	}
 }
