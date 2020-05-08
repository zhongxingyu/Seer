 package com.cole2sworld.ColeBans.commands;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 import com.cole2sworld.ColeBans.GlobalConf;
 import com.cole2sworld.ColeBans.Main;
 import com.cole2sworld.ColeBans.Util;
 import com.cole2sworld.ColeBans.framework.PermissionSet;
 import com.cole2sworld.ColeBans.handlers.BanHandler;
 
 public class Switch extends CBCommand {
 
 	@Override
 	public String run(String[] args, CommandSender admin) {
 		PermissionSet perm = new PermissionSet(admin);
 		if (!perm.canSwitch) return ChatColor.RED+"You don't have permission to do that."; 
 		if (args.length > 1) return ChatColor.RED+"The switch command must be used with only the destination handler as an argument";
 		else {
 			try {
 				BanHandler dest = Util.lookupHandler(args[0]);
 				Main.LOG.info("Starting conversion from "+Main.instance.banHandler.getClass().getSimpleName().replace(GlobalConf.Advanced.suffix, "")+" to "+args[0]);
 				admin.sendMessage(ChatColor.YELLOW+"Starting conversion...");
 				Main.instance.banHandler.convert(dest);
 				Main.instance.banHandler.onDisable();
 				GlobalConf.conf.set("settings.banHandler", args[0]);
				Main.instance.saveConfig();
 				Main.instance.banHandler = dest;
 				Main.LOG.info("Conversion succeeded!");
 				admin.sendMessage(ChatColor.GREEN+"Conversion succeeded!");
 			} catch (IllegalArgumentException e) {
 				return ChatColor.DARK_RED+"Given ban handler is wierdly implemented";
 			} catch (SecurityException e) {
 				return ChatColor.DARK_RED+"Plugin conflict!";
 			} catch (ClassCastException e) {
 				return ChatColor.DARK_RED+"Given ban handler is not actually a ban handler";
 			} catch (ClassNotFoundException e) {
 				return ChatColor.DARK_RED+"No such ban handler '"+args[0]+"' Make sure you got the caps right!";
 			} catch (IllegalAccessException e) {
 				return ChatColor.DARK_RED+"Given ban handler is wierdly implemented";
 			} catch (InvocationTargetException e) {
 				return ChatColor.DARK_RED+"Given ban handler is wierdly implemented";
 			} catch (NoSuchMethodException e) {
 				return ChatColor.DARK_RED+"Given ban handler is wierdly implemented";
 			}
 		}
 		return null;
 	}
 
 }
