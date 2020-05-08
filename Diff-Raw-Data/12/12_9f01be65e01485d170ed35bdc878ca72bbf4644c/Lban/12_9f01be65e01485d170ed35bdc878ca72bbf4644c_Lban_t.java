 package com.cole2sworld.ColeBans.commands;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import com.cole2sworld.ColeBans.GlobalConf;
 import com.cole2sworld.ColeBans.LogManager;
 import com.cole2sworld.ColeBans.Main;
 import com.cole2sworld.ColeBans.LogManager.Type;
 import com.cole2sworld.ColeBans.framework.PermissionSet;
 import com.cole2sworld.ColeBans.framework.PlayerAlreadyBannedException;
 import com.cole2sworld.ColeBans.handlers.YAMLBanHandler;
 /**
  * The Local Ban command. Handles banning players through commands, but uses a seperate ban handler.
  *
  */
 public final class Lban implements CBCommand {
 	private static YamlConfiguration conf = new YamlConfiguration();
 	private static File confFile = new File("plugins/ColeBans/lbanlist.yml");
 	private static YAMLBanHandler handler;
 	@Override
 	public String run(String[] args, CommandSender admin) {
 		if (!(new PermissionSet(admin).canBan)) return ChatColor.RED+"You don't have permission to do that.";
 		String error = null;
 		if (args.length < 2) error = ChatColor.RED+"You must specify a player and reason.";
 		else {
 			String victim = args[0];
 			StringBuilder reasonBuilder = new StringBuilder();
 			reasonBuilder.append(args[1]);
 			for (int i = 2; i<args.length; i++) {
 				reasonBuilder.append(" ");
 				reasonBuilder.append(args[i]);
 			}
 			String reason = reasonBuilder.toString();
 			try {
 				handler.banPlayer(victim, reason, admin.getName());
 				Player playerObj = Main.instance.server.getPlayerExact(victim);
 				if (playerObj != null) {
 					playerObj.kickPlayer(ChatColor.valueOf(GlobalConf.banColor)+"BANNED: "+reason);
 					if (GlobalConf.fancyEffects) {
 						World world = playerObj.getWorld();
 						world.createExplosion(playerObj.getLocation(), 0);
 					}
 				}
 				if (GlobalConf.announceBansAndKicks) Main.instance.server.broadcastMessage(ChatColor.valueOf(GlobalConf.banColor)+victim+" was local banned! ["+reason+"]");
 				LogManager.addEntry(Type.LOCAL_BAN, admin.getName(), victim);
 			} catch (PlayerAlreadyBannedException e) {
 				error = ChatColor.DARK_RED+victim+" is already banned!";
 			}
 		}
 		return error;
 	}
 	static {
 		try {
			confFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
 			conf.load(confFile);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 		handler = new YAMLBanHandler(confFile, conf);
 	}
 }
