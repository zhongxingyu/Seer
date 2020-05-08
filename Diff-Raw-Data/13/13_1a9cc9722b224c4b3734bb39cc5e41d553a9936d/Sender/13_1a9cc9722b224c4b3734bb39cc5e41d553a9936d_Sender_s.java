 package com.gmail.zant95.LiveChat;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 public class Sender {
 	public static void main(Player sender, String msg, String type) {
 		if (MemStorage.mute.containsKey(sender.getName())) {
 			sender.sendMessage("\u00A7c" + MemStorage.locale.get("YOU_ARE_MUTED") + ".");
 		} else if (type == "public") {
 			publicchat(sender, msg);
 		} else if (type.startsWith("private")) {
 			Player target = sender.getServer().getPlayer(type.split(":")[1]);
 			privatechat(sender, target, msg);
 		} else if (type == "map") {
 			mapchat(sender, msg);
 		} else if (type == "local") {
 			localchat(sender, msg);
 		} else if (type == "admin") {
 			adminchat(sender, msg);
 		} else if (type == "emote") {
 			emotechat(sender, msg);
 		}
 	}
 	
 	private static void publicchat(Player sender, String msg) {
 		Player[] players = Bukkit.getServer().getOnlinePlayers();
 		for (Player target:players) {
 			if ((!Utils.isIgnored(sender, target) || LiveChat.perms.has(sender, "livechat.ignore.bypass")) && !MemStorage.publicignore.containsKey(target.getName())) {
 				target.sendMessage(Format.main(sender, msg, "public"));
 			}
 		}
 		Log.main("[" + sender.getName() + "] " + msg, "public");
 	}
 
 	private static void privatechat(Player sender, Player target, String msg) {
 		if (target == null) {
 			sender.sendMessage("\u00A7c" + MemStorage.locale.get("DISCONECTED_USER") + ".");
 		} else if (MemStorage.privateignore.containsKey(target.getName()) || !(!Utils.isIgnored(sender, target) || LiveChat.perms.has(sender, "livechat.ignore.bypass"))) {
 			sender.sendMessage("\u00A7c" + MemStorage.locale.get("YOU_ARE_IGNORED") + ".");
 		} else {
 			target.sendMessage(Format.privateTarget(sender, target, msg, "private"));
 			sender.sendMessage(Format.privateSender(sender, target, msg, "private"));
 			MemStorage.reply.put(target.getName(), sender.getName());
 			socialSpy(sender, target, msg);
 			Log.main("[" + sender.getName() + "->" + target.getName() + "] " + msg, "private");
 		}
 	}
 
 	private static void mapchat(Player sender, String msg) {
 		Player[] players = Bukkit.getServer().getOnlinePlayers();
 		for (Player target:players) {
 			if (target.getWorld() == sender.getWorld()) {
 				target.sendMessage(Format.main(sender, msg, "map"));
 			}
 		}
 		Log.main("[" + sender.getName() + "] " + msg, "map");
 	}
 
 	private static void localchat(Player sender, String msg) {
 		Boolean heard = false;
		Player[] players = sender.getWorld().getPlayers().toArray(new Player[0]);
 		for (Player target:players) {
			if ((!target.equals(sender) && target.getLocation().distance(sender.getLocation()) < MemStorage.plugin.getConfig().getInt("chat.local.radius") && (!Utils.isIgnored(sender, target) || LiveChat.perms.has(sender, "livechat.ignore.bypass"))) && !MemStorage.localignore.containsKey(target.getName())) {
				target.sendMessage(Format.main(sender, msg, "local"));
				heard = true;
 			}
 		}
 		if (heard) {
 			sender.sendMessage(Format.main(sender, msg, "local"));
 			Log.main("[" + sender.getName() + "] " + msg, "local");
 		} else {
 			sender.sendMessage("\u00A7c" + MemStorage.locale.get("NOBODY_HEAR") + ".");
 		}
 	}
 
 	private static void adminchat(Player sender, String msg) {
 		Player[] players = Bukkit.getServer().getOnlinePlayers();
 		for (Player target:players) {
 			if (LiveChat.perms.has(target, "livechat.admin") || LiveChat.perms.has(sender, "livechat.admin.chat") || target.isOp()) {
 				target.sendMessage(Format.main(sender, msg, "admin"));
 			}
 		}
 		Log.main("[" + sender.getName() + "] " + msg, "admin");
 	}
 
 	private static void emotechat(Player sender, String msg) {
 		Player[] players = Bukkit.getServer().getOnlinePlayers();
 		for (Player target:players) {
 			if (!Utils.isIgnored(sender, target) || LiveChat.perms.has(sender, "livechat.ignore.bypass")) {
 				target.sendMessage(Format.main(sender, msg, "emote"));
 			}
 		}
 		Log.main("[" + sender.getName() + "] " + msg, "emote");
 	}
 
 	private static void socialSpy(Player sender, Player target, String msg) {
 		String[] socialSpyList = MemStorage.plugin.getConfig().getString("chat.socialspy.players").replaceAll(" ", "").split(",");
 		for (int i = 0; i < socialSpyList.length; i++) {
 			Player socialSpyUser = null;
 			socialSpyUser = Bukkit.getServer().getPlayer(socialSpyList[i]);
 			if (socialSpyUser != sender && socialSpyUser != target && socialSpyUser != null) {
 				socialSpyUser.sendMessage(Format.withTarget(sender, target, msg, "socialspy"));
 			}
 		}
 	}
 }
