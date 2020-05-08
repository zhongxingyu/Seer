 package com.beecub.execute;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.beecub.command.bPermissions;
 import com.beecub.glizer.glizer;
 import com.beecub.util.Language;
 import com.beecub.util.bChat;
 import com.beecub.util.bConfigManager;
 
 import de.upsj.glizer.APIRequest.LookupAccountsRequest;
 
 public class Other {
 
 	public static boolean glizerreload(String command, CommandSender sender,
 			String[] args) {
 		if (bPermissions.checkPermission(sender, command)) {
 			if (args.length <= 1) {
 				bConfigManager.reload();
 				Whitelist.getPlayers();
 				Backup.getPlayers();
 				Language.reloadFiles();
 				bChat.sendMessage(sender, "&6Glizer reload complete.");
 			} else {
 				bChat.sendMessage(sender,
 						Language.GetTranslated("other.wrong_command_usage"));
 
 			}
 			return true;
 		}
 		return true;
 	}
 	
 	public static boolean lookup(String command, CommandSender sender, String[] args)
 	{
 		if (bPermissions.checkPermission(sender, command))
 		{
 			if (args.length == 1)
 			{
 				glizer.queue.add(new LookupAccountsRequest(sender, args[0]));
 			}
 			else
 			{
 				bChat.sendMessage(sender, Language.GetTranslated("other.wrong_command_usage"));
 				bChat.sendMessage(sender, "&6/listaccounts&e [playername]");
 			}
 		}
 		return true;
 	}
 
 	public static boolean glizerhelp(String command, CommandSender sender,
 			String[] args) {
 		if (bPermissions.checkPermission(sender, command)) {
 			if (args.length == 1) {
 				try {
 					int page = Integer.valueOf(args[0]).intValue();
 					if ((page <= 0) || (page >= 10))
 						showHelpPage(sender, page);
 				} catch (Exception e) {
 					String topic = args[0];
 					showHelpTopic(sender, topic);
 					return true;
 				}
 			} else if (args.length == 0) {
 				bChat.sendMessage(sender, "&6Choose one of these help topics:");
 				bChat.sendMessage(sender, "&e/glizer help &6[Topic] ");
 				bChat.sendMessage(sender,
 						"&6Bans  Kicks   Warnings   Praise  Rating");
 				bChat.sendMessage(sender, "&6Profile   Friends   Comments");
 				bChat.sendMessage(sender, "&6Whitelist Onlinetime");
 				return true;
 			}
 			bChat.sendMessage(sender,
 					Language.GetTranslated("other.wrong_command_usage"));
 			return true;
 		}
 		return true;
 	}
 
 	public static boolean theanswertolifetheuniverseandeverything(
 			String command, Player player, String[] args) {
 		bChat.sendMessageToPlayer(player, "&442");
 		return true;
 	}
 
 	private static void showHelpPage(CommandSender sender, int page) {
 	}
 
 	private static boolean showHelpTopic(CommandSender sender, String topic) {
 		if (topic.equalsIgnoreCase("")) {
 			return true;
 		}
 		if (topic.equalsIgnoreCase("Kicks")) {
 			bChat.sendMessage(sender, "&6/kick&e [playername] [reason]");
 			return true;
 		}
 		if (topic.equalsIgnoreCase("Bans")) {
 			bChat.sendMessage(sender, "&6/globalban&e [playername] [reason]");
 			bChat.sendMessage(sender, "&6/localban&e [playername] [reason]");
 			bChat.sendMessage(sender,
 					"&6/tempban&e [playername] [time] [message]");
 			bChat.sendMessage(sender, "&6/unban&e [playername] [reason]");
 			return true;
 		}
		if (topic.equalsIgnoreCase("Onlietime")) {
 			bChat.sendMessage(sender, "&6/onlinetime&e [playername]");
 			return true;
 		}
 
 		if (topic.equalsIgnoreCase("Warnings")) {
 			bChat.sendMessage(sender, "&6/warnings&e [name]");
 			bChat.sendMessage(sender,
 					"&6/warn&e [name] [reputation| 1 to 80] [reason]");
 			bChat.sendMessage(
 					sender,
 					"&6/tempwarn&e [name] [reputation| 1 to 80] [TimeValue] [TimeUnit | minutes or hours or days] [reason]");
 			return true;
 		}
 		if (topic.equalsIgnoreCase("Rating")) {
 			bChat.sendMessage(sender,
 					"&6/rateserver&e [value|0 to 10 (10 is best)]");
 			return true;
 		}
 		if (topic.equalsIgnoreCase("Profile")) {
 			bChat.sendMessage(sender, "&6/profile&e [name]");
 			bChat.sendMessage(sender, "&6/editprofile&e [field] [value]");
 			bChat.sendMessage(sender,
 					"&6Available Fields:&e age | status | realname | more soon!");
 			return true;
 		}
 		if (topic.equalsIgnoreCase("Friends")) {
 			bChat.sendMessage(sender, "&6/friends&e [name]");
 			bChat.sendMessage(sender, "&6/addfriend&e [name]");
 			bChat.sendMessage(sender, "&6/removefriend&e [name]");
 			bChat.sendMessage(sender, "&6/friendsonline&e [name] &6- soon!");
 			return true;
 		}
 		if (topic.equalsIgnoreCase("Comments")) {
 			bChat.sendMessage(sender, "&6/comments&e [name]");
 			bChat.sendMessage(sender, "&6/comment&e [name] [text]");
 			return true;
 		}
 		if (topic.equalsIgnoreCase("Praise")) {
 			bChat.sendMessage(sender, "&6/praise&e [name] [Reputation]");
 			return true;
 		}
 
 		if (topic.equalsIgnoreCase("Whitelist")) {
 			bChat.sendMessage(sender, "&6/addwhitelist&e [name]");
 			bChat.sendMessage(sender, "&6/removewhitelist&e [name]");
 			return true;
 		}
 
 		glizerhelp("glizerhelp", sender, new String[0]);
 
 		return true;
 	}
 
 	public static void glizer(String command, CommandSender sender,
 			String[] args) {
 		if (args.length == 0) {
 			bChat.sendMessage(sender,
 					"&6Glizer (version " + glizer.pdfFile.getVersion() + ")");
 			bChat.sendMessage(sender,
 					Language.GetTranslated("other.see_help_menu"));
 			if (bPermissions.checkPermission(sender, "glizerreload")) {
 				bChat.sendMessage(sender,
 						Language.GetTranslated("other.reload_glizer"));
 			}
 
 		} else if (args[0].equals("reload")) {
 			glizerreload("glizerreload", sender, args);
 		} else if (args[0].equals("help")) {
 			if (args.length == 1)
 				glizerhelp("glizerhelp", sender, new String[0]);
 			else if (args.length == 2)
 				glizerhelp("glizerhelp", sender, new String[] { args[1] });
 		}/*
 		 * else if (args[0].equals("verify")) { if (args.length == 2)
 		 * glizerverify("glizerverify", sender, new String[] { args[1] }); else
 		 * { bChat.sendMessage(sender, "&e/glizer verify [name]"); return; } }
 		 */
 	}
 
 	public static void abuse(String command, CommandSender sender, String[] args) {
 		bChat.sendMessage(sender, "&6To report abuse please visit &eglizer.de");
 	}
 }
