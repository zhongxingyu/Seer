 package com.beecub.execute;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import com.beecub.command.bPermissions;
 import com.beecub.glizer.glizer;
 import com.beecub.util.Language;
 import com.beecub.util.bChat;
 import com.beecub.util.bConfigManager;
 import de.upsj.glizer.APIRequest.NoteAddRequest;
 
 public class Ban {
 
 	public static boolean ban(String command, CommandSender sender,
 			String[] args) {
 		bChat.sendMessage(sender, Language.GetTranslated("ban.ban"));
 		return true;
 	}
 
 	public static boolean globalban(String command, CommandSender sender,
 			String[] args) {
 		if (sender == null) {
 			bChat.log("sender == null in globalban. Ban failed. Please report this error.");
 			return false;
 		}
 		if (bPermissions.checkPermission(sender, command)) {
 			if (args.length >= 2) {
 				String message = "";
 				String recipient = args[0];
 				for (int i = 1; i < args.length; i++) {
 					message += args[i] + " ";
 				}
 
 				if (message != null && message != "") {
 					bChat.sendMessage(sender, "&eBan enqueued");
 					glizer.queue.add(new NoteAddRequest(sender, recipient, message, NoteAddRequest.GlobalBan, 0, 0, null));
 				} else {
 					bChat.sendMessage(sender,
 							Language.GetTranslated("other.wrong_command_usage"));
 					bChat.sendMessage(sender,
 							"&6/globalban&e [playername] [message]");
 				}
 				return true;
 			}
 			bChat.sendMessage(sender,
 					Language.GetTranslated("other.wrong_command_usage"));
 			bChat.sendMessage(sender, "&6/globalban&e [playername] [message]");
 			return true;
 		}
 		return true;
 	}
 
 	public static boolean localBan(String command, CommandSender sender,
 			String[] args) {
 		if (sender == null) {
 			bChat.log("sender == null in localban. Ban failed. Please report this error.");
 			return false;
 		}
 		if (bPermissions.checkPermission(sender, command)) {
 			if (args.length >= 2) {
 				String message = "";
 				String recipient = args[0];
 				for (int i = 1; i < args.length; i++) {
 					message += args[i] + " ";
 				}
 
 				if (message != null && message != "") {
 					bChat.sendMessage(sender, "&eBan enqueued");
 					glizer.queue.add(new NoteAddRequest(sender, recipient, message, NoteAddRequest.LocalBan, 0, 0, null));
 				} else {
 					bChat.sendMessage(sender,
 							Language.GetTranslated("other.wrong_command_usage"));
 					bChat.sendMessage(sender,
 							"&6/globalban&e [playername] [message]");
 				}
 				return true;
 			}
 			bChat.sendMessage(sender,
 					Language.GetTranslated("other.wrong_command_usage"));
 			bChat.sendMessage(sender, "&6/localban&e [playername] [message]");
 			return true;
 		}
 		return true;
 	}
 
 	public static boolean forceBan(String command, CommandSender sender,
 			String[] args) {
 		if (sender == null) {
 			bChat.log("sender == null in forceban. Ban failed. Please report this error.");
 			return false;
 		}
 		if (bPermissions.checkPermission(sender, command)) {
 			if (args.length >= 2) {
 				String message = "";
 				String recipient = args[0];
 				for (int i = 1; i < args.length; i++) {
 					message += args[i] + " ";
 				}
 
 				if (message != null && message != "") {
 					bChat.sendMessage(sender, "&eBan enqueued");
 					glizer.queue.add(new NoteAddRequest(sender, recipient, message, NoteAddRequest.ForceBan, 0, 0, null));
 				} else {
 					bChat.sendMessage(sender,
 							Language.GetTranslated("other.wrong_command_usage"));
 					bChat.sendMessage(sender,
 							"&6/forceban&e [playername] [message]");
 				}
 				return true;
 			}
 			bChat.sendMessage(sender,
 					Language.GetTranslated("other.wrong_command_usage"));
 			bChat.sendMessage(sender, "&6/forceban&e [playername] [message]");
 			return true;
 		}
 		return true;
 	}
 
 	public static boolean tempban(String command, CommandSender sender,
 			String[] args) {
 		if (sender == null) {
 			bChat.log("sender == null in globalban. Ban failed. Please report this error.");
 			return false;
 		}
 
 		if (bPermissions.checkPermission(sender, command)) {
 			if (args.length >= 4) {
 				String message = "";
 				String recipient = args[0];
 				String stime = args[1];
 				String timetype = args[2];
 				double dtime;
 				try {
 					dtime = Double.valueOf(stime);
 				} catch (Exception e) {
 					bChat.sendMessage(sender, "&6Error, wrong time value");
 					return true;
 				}
 				if (timetype.equalsIgnoreCase("minutes")
 						|| timetype.equalsIgnoreCase("m")) {
 					dtime = dtime * 60;
 				} else if (timetype.equalsIgnoreCase("hours")
 						|| timetype.equalsIgnoreCase("h")) {
 					dtime = dtime * 60 * 60;
 				} else if (timetype.equalsIgnoreCase("days")
 						|| timetype.equalsIgnoreCase("d")) {
 					dtime = dtime * 60 * 60 * 24;
 				} else {
 					bChat.sendMessage(sender, "&6Error, wrong time value");
 					return true;
 				}
 				for (int i = 3; i < args.length; i++) {
 					message += args[i] + " ";
 				}
 
 				if (message != null && message != "") {
 					bChat.sendMessage(sender, "&eBan enqueued");
 					glizer.queue.add(new NoteAddRequest(sender, recipient, message, NoteAddRequest.TempBan, 0, (int) dtime, stime + timetype));
 				} else {
 					bChat.sendMessage(sender,
 							Language.GetTranslated("other.wrong_command_usage"));
 					bChat.sendMessage(sender,
 							"&6/tempban&e [playername] [time] [minutes|hours|days] [message]");
 				}
 				return true;
 			}
 			bChat.sendMessage(sender,
 					Language.GetTranslated("other.wrong_command_usage"));
 			bChat.sendMessage(sender,
 					"&6/tempban&e [playername] [time] [minutes|hours|days] [message]");
 			return true;
 		}
 		return true;
 	}
 
 	public static boolean unban(String command, CommandSender sender,
 			String[] args) {
 		if (sender == null) {
 			bChat.log("sender == null in globalban. Ban failed. Please report this error.");
 			return false;
 		}
 
 		if (bPermissions.checkPermission(sender, command)) {
 			if (args.length >= 2) {
 				String message = "";
 				String recipient = args[0];
 				
 				for (int i = 1; i < args.length; i++) {
 					message += args[i] + " ";
 				}
 				if (message != null && message != "") {
 					bChat.sendMessage(sender, "&eUnban enqueued");
 					glizer.queue.add(new NoteAddRequest(sender, recipient, message, NoteAddRequest.Unban, 0, 0, null));
 				} else {
 					bChat.sendMessage(sender,
 							Language.GetTranslated("other.wrong_command_usage"));
 					bChat.sendMessage(sender,
 							"&6/unban &e[Playername] [Reason]");
 				}
 				return true;
 			}
 			bChat.sendMessage(sender,
 					Language.GetTranslated("other.wrong_command_usage"));
 			bChat.sendMessage(sender, "&6/unban&e [playername] [message]");
 			return true;
 		}
 		return true;
 	}
 
 	public static void kick(String command, CommandSender sender,
 			String[] args, boolean silent) {
 		if (bPermissions.checkPermission(sender, command)) {
 			
 			if (args.length > 1) {
 				String recipient = args[0];
 				String message = "";
 				if (args.length >= 2) {
 					for (int i = 1; i < args.length; i++) {
 						message += args[i] + " ";
 					}
 				}
 
 				Player pPlayer = glizer.plugin.getServer().getPlayer(recipient);
 				if (pPlayer == null) {
 					bChat.sendMessage(sender, Language.GetTranslated(
 							"ban.kick_not_online", recipient));
 					return;
 				}
 				if (message.isEmpty()) {
 					message = "Kicked by Server";
 				}
 
				bChat.log(""+(silent?"KICK":"SKICK") + "%"+sender.getName()+";"+recipient+";"+pPlayer.getName()+";"+message);
 				if (bConfigManager.broadcastKick && !silent)
 
 					bChat.broadcastMessage(Language.GetTranslated(
 							"ban.kick_bc_1",
 							pPlayer.getName(),
 							(sender instanceof Player ? ("by " + ((Player) sender)
 									.getName()) : "from the server"), message));
 				pPlayer.kickPlayer(Language.GetTranslated(
 						"ban.kick_pm_1",
 						(sender instanceof Player ? ("by " + ((Player) sender)
 								.getName()) : "from the server"), message));
 
 			} else {
 				bChat.sendMessage(sender, "&6" + (silent ? "/skick" : "/kick")
 						+ " &e[playername] [reason]");
 			}
 		}
 	}
 }
