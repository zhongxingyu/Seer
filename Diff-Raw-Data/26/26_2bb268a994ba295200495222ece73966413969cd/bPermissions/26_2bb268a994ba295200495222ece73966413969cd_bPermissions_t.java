 package com.beecub.command;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.beecub.util.Language;
 import com.beecub.util.bChat;
 
 public class bPermissions {
 	public static boolean checkPermission(CommandSender sender, String command) {
 		if (sender instanceof Player) {
 			return checkPermission((Player) sender, command);
 		} else
 			return true;
 	}
 	
 	public static boolean checkPermission(Player player, String command)
 	{
 		if (scheckPermission(player, command))
 		{
 			return true;
 		}
 		bChat.sendMessageToPlayer(player, Language.GetTranslated("other.no_permissions"));
 		return false;
 	}
 
 	public static boolean scheckPermission(Player player, String command) {
 
 		/*String admin = "glizer.admin";
 		String moderator = "glizer.moderator";
 		String user = "glizer.user";*/
 
 		if (player.isOp()) {
 			return true;
 		}
 		if (command.equalsIgnoreCase("")) {
 			if (player.hasPermission("")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("glizer")) {
 			return true;
		} else if (command.equalsIgnoreCase("kick")) {
 			if (player.hasPermission("glizer.kick")) {
 				return true;
 			}
		} else if (command.equalsIgnoreCase("skick")) {
			if (player.hasPermission("glizer.skick")) {
				return true;
			}
 		}
 		else if (command.equalsIgnoreCase("ban")) {
 			if (player.hasPermission("glizer.ban.ban")) {
 				return true;
 			}
		} else if (command.equalsIgnoreCase("globalban") || command.equalsIgnoreCase("gban")) {
 			if (player.hasPermission("glizer.ban.globalban")) {
 				return true;
 			}
		} else if (command.equalsIgnoreCase("localban") || command.equalsIgnoreCase("lban")) {
 			if (player.hasPermission("glizer.ban.localban")) {
 				return true;
 			}
		} else if (command.equalsIgnoreCase("tempban") || command.equalsIgnoreCase("timeban")) {
 			if (player.hasPermission("glizer.ban.tempban")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("unban")) {
 			if (player.hasPermission("glizer.ban.unban")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("praise")) {
 			if (player.hasPermission("glizer.praise")) {
 				return true;
 			}
		} else if (command.equalsIgnoreCase("gpraise") || command.equalsIgnoreCase("globalpraise")) {
 				if (player.hasPermission("glizer.gpraise")) {
 					return true;
 				}
 		} else if (command.equalsIgnoreCase("note")) {
 			if (player.hasPermission("glizer.note.note")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("notes")) {
 			if (player.hasPermission("glizer.note.notes")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("warn")) {
 			if (player.hasPermission("glizer.warning.warn")) {
 				return true;
 			}
		} else if (command.equalsIgnoreCase("gwarn") || command.equalsIgnoreCase("globalwarn")) {
 			if (player.hasPermission("glizer.warning.gwarn")) {
 				return true;
 			}
		} else if (command.equalsIgnoreCase("tempwarn") || command.equalsIgnoreCase("timewarn")) {
 			if (player.hasPermission("glizer.warning.tempwarn")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("warnings") || command.equalsIgnoreCase("localwarnings")) {
 			if (player.hasPermission("glizer.warning.warnings")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("register")) {
 			if (player.hasPermission("glizer.other.register")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("comment")) {
 			if (player.hasPermission("glizer.comment.comment")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("onlinetime")) {
 			if (player.hasPermission("glizer.other.onlinetime")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("comments")) {
 			if (player.hasPermission("glizer.comment.comments")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("rateplayer")) {
 			if (player.hasPermission("glizer.rate.rateplayer")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("rateserver")) {
 			if (player.hasPermission("glizer.rate.rateserver")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("profile")) {
 			if (player.hasPermission("glizer.profile.profile")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("editprofile")) {
 			if (player.hasPermission("glizer.profile.editprofile")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("clearprofile")) {
 			if (player.hasPermission("glizer.profile.clearprofile")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("friends")) {
 			if (player.hasPermission("glizer.friends.friends")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("addfriend")) {
 			if (player.hasPermission("glizer.friends.addfriend")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("removefriend")) {
 			if (player.hasPermission("glizer.friends.removefriend")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("addwhitelist")) {
 			if (player.hasPermission("glizer.whitelist.add")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("removewhitelist")) {
 			if (player.hasPermission("glizer.whitelist.remove")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("listaccounts")) {
 			if (player.hasPermission("glizer.accounts.list")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("registerevent")
 				|| command.equalsIgnoreCase("unregisterevent")
 				|| command.equalsIgnoreCase("myevents")) {
 			if (player.hasPermission("glizer.event.register")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("eventlist")
 				|| command.equalsIgnoreCase("eventdetail")) {
 			return true;
 		} else if (command.equalsIgnoreCase("glizerreload")) {
 			if (player.hasPermission("glizer.reload")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("glizerhelp")) {
 			if (player.hasPermission("glizer.help")) {
 				return true;
 			}
 		} else if (command.equalsIgnoreCase("messagereputation")) {
 			if (player.hasPermission("glizer.reputation.message")) {
 				return true;
 			}
 		} else {
 			return false;
 		}
 		return false;
 	}
 }
