 package com.gmail.zant95;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.command.CommandExecutor;
 
 public class CommandHandler implements CommandExecutor {
 	protected static CommandExecutor CommandExecutor;
 
 	LiveChat plugin;
 
 	public CommandHandler(LiveChat instance) {
 		plugin = instance;
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {	
 		String playerName = null;
 		String targetName = null;
 		Player target = null;
 		Player player = null;
 
 		if (!Utils.isConsole(sender)) {
 			player = (Player)sender;
 			playerName = player.getName();
 		}
 
 		if (command.getName().equalsIgnoreCase("msg")) {
 			if (Utils.isConsole(sender)) {
 				plugin.getLogger().info(MemStorage.locale.get("NOT_AS_CONSOLE") + ".");
 				return true;
 			}
 			if (LiveChat.perms.has(sender, "livechat.msg") || LiveChat.perms.has(sender, "livechat.admin") || sender.isOp()) {
 				if (args.length != 0) {
 					target = sender.getServer().getPlayer(args[0]);
 					if (target == null) {
 						sender.sendMessage("\u00A7c" + MemStorage.locale.get("PLAYER_NOT_FOUND") + ".");
 						return true;
 					}
 					targetName = target.getName();
 					if (args.length >= 2) {
 						if (targetName != playerName) {
 							Sender.privatechat(player, target, Utils.getMsg(args, 1, sender));
 							return true;
 						} else {
 							sender.sendMessage("\u00A7c" + MemStorage.locale.get("TALK_TO_YOURSELF") + ".");
 							return true;
 						}
 					}
 					if (playerName != targetName) {
 						MemStorage.speaker.put(playerName, targetName);
 						sender.sendMessage("\u00A7e" + MemStorage.locale.get("CONVERSATION_WITH") + " " + target.getDisplayName() + "\u00A7e.");
 						return true;
 					} else {
 						sender.sendMessage("\u00A7c" + MemStorage.locale.get("CONVERSATION_WITH_YOURSELF") + ".");
 						return true;
 					}
 				} else if (MemStorage.speaker.containsKey(playerName)) {
 					MemStorage.speaker.remove(sender.getName());
 					sender.sendMessage("\u00A7e" + MemStorage.locale.get("END_CONVERSATION") + ".");
 					return true;
 				} else {
 					sender.sendMessage("\u00A7c" + MemStorage.locale.get("ANY_CONVERSATION") + ".");
 					return true;
 				}
 			} else {
 				sender.sendMessage("\u00A7c" + MemStorage.locale.get("NOT_PERMISSION") + ".");
 				return true;
 			}
 		}
 
 		if (command.getName().equalsIgnoreCase("r")) {
 			if (Utils.isConsole(sender)) {
 				plugin.getLogger().info(MemStorage.locale.get("NOT_AS_CONSOLE") + ".");
 				return true;
 			}
 			if (LiveChat.perms.has(sender, "livechat.msg") || LiveChat.perms.has(sender, "livechat.admin") || sender.isOp()) {
 				if (MemStorage.reply.get(playerName) == null) {
 					sender.sendMessage("\u00A7c" + MemStorage.locale.get("NOBODY_REPLY") + ".");
 					return true;
 				} else if (args.length != 0) {
 					target = Bukkit.getServer().getPlayer(MemStorage.reply.get(playerName));
 					Sender.privatechat(player, target, Utils.getMsg(args, 0, sender));
 					return true;
 				} else {
 					((Player)sender).chat("/tell " + MemStorage.reply.get(playerName));
 					return true;
 				}
 			} else {
 				sender.sendMessage("\u00A7c" + MemStorage.locale.get("NOT_PERMISSION") + ".");
 				return true;
 			}
 		}
 
 		if (command.getName().equalsIgnoreCase("me")) {
 			if (Utils.isConsole(sender)) {
 				plugin.getLogger().info(MemStorage.locale.get("NOT_AS_CONSOLE") + ".");
 				return true;
 			}
 			if (LiveChat.perms.has(sender, "livechat.me") || LiveChat.perms.has(sender, "livechat.admin") || sender.isOp()) {
 				if (args.length == 0) {
 					sender.sendMessage("\u00A7c" + MemStorage.locale.get("EMOTE_USAGE"));
 					return true;
 				} else {
 					String msg = Utils.getMsg(args, 0, sender);
 					Sender.me(player, Format.main(player, msg, "emote"), msg);
 					return true;
 				}
 			} else {
 				sender.sendMessage("\u00A7c" + MemStorage.locale.get("NOT_PERMISSION") + ".");
 				return true;
 			}
 		}
 
 		if (command.getName().equalsIgnoreCase("local")) {
 			if (Utils.isConsole(sender)) {
 				plugin.getLogger().info(MemStorage.locale.get("NOT_AS_CONSOLE") + ".");
 				return true;
 			}
 			if (LiveChat.perms.has(sender, "livechat.local") || LiveChat.perms.has(sender, "livechat.admin") || sender.isOp()) {
 				if (args.length != 0) {
 					String msg = Utils.getMsg(args, 0, sender);
 					Sender.local(player, Format.main(player, msg, "local"), msg);
 					return true;
 
 				} else if (MemStorage.local.containsKey(playerName)) {
 					MemStorage.local.remove(playerName);
 					sender.sendMessage("\u00A7e" + MemStorage.locale.get("ENDED_LOCAL_CONVERSATION") + ".");
 					return true;
 				} else {
 					MemStorage.local.put(playerName, "");
 					sender.sendMessage("\u00A7e" + MemStorage.locale.get("STARTED_LOCAL_CONVERSATION") + ".");
 					return true;
 				}
 			} else {
 				sender.sendMessage("\u00A7c" + MemStorage.locale.get("NOT_PERMISSION") + ".");
 				return true;
 			}
 		}
 
 		if (command.getName().equalsIgnoreCase("mute")) {
 			if (LiveChat.perms.has(sender, "livechat.mute") || LiveChat.perms.has(sender, "livechat.admin") || sender.isOp()) {
 				if (args.length != 1) {
 					sender.sendMessage("\u00A7c" + MemStorage.locale.get("MUTE_USAGE"));
 					return true;	
 				} else {
 					target = sender.getServer().getPlayer(args[0]);
 					if (target == null) {
 						sender.sendMessage("\u00A7c" + MemStorage.locale.get("PLAYER_NOT_FOUND") + ".");
 						return true;
 					} else if (target.getName() == sender.getName()) {
 						sender.sendMessage("\u00A7c" + MemStorage.locale.get("NOT_WITH_YOURSELF") + ".");
 						return true;
 					} else if (MemStorage.mute.containsKey(target.getName())) {
 						MemStorage.mute.remove(target.getName());
 						sender.sendMessage(target.getDisplayName() + " \u00A7e" + MemStorage.locale.get("UNMUTED_PLAYER") + ".");
 						return true;
 					} else {
 						MemStorage.mute.put(target.getName(), "");
 						sender.sendMessage(target.getDisplayName() + " \u00A7e" + MemStorage.locale.get("MUTED_PLAYER") + ".");
 						return true;
 					}
 				}
 			} else {
 				sender.sendMessage("\u00A7c" + MemStorage.locale.get("NOT_PERMISSION") + ".");
 				return true;
 			}
 		}
 
 		if (command.getName().equalsIgnoreCase("block")) {
 			if (LiveChat.perms.has(sender, "livechat.block") || LiveChat.perms.has(sender, "livechat.admin") || sender.isOp()) {
 				if (args.length != 1) {
 					sender.sendMessage("\u00A7c" + MemStorage.locale.get("BLOCK_USAGE"));
 					return true;
 				} else {
 					target = sender.getServer().getPlayer(args[0]);
 					if (target == null) {
 						sender.sendMessage("\u00A7c" + MemStorage.locale.get("PLAYER_NOT_FOUND") + ".");
 						return true;
 					} else if (target.getName() == sender.getName()) {
 						sender.sendMessage("\u00A7c" + MemStorage.locale.get("NOT_WITH_YOURSELF") + ".");
 						return true;
 					} else if (MemStorage.block.containsKey(target.getName())) {
 						MemStorage.block.remove(target.getName());
 						sender.sendMessage(target.getDisplayName() + " \u00A7e" + MemStorage.locale.get("UNBLOCKED_PLAYER") + ".");
 						return true;
 					} else {
 						MemStorage.block.put(target.getName(), "");
 						sender.sendMessage(target.getDisplayName() + " \u00A7e" + MemStorage.locale.get("BLOCKED_PLAYER") + ".");
 						return true;
 					}
 				}
 			} else {
 				sender.sendMessage("\u00A7c" + MemStorage.locale.get("NOT_PERMISSION") + ".");
 				return true;
 			}
 		}
 
 		if (command.getName().equalsIgnoreCase("ignore")) {
 			if (Utils.isConsole(sender)) {
 				plugin.getLogger().info(MemStorage.locale.get("NOT_AS_CONSOLE") + ".");
 				return true;
 			}
 			if (LiveChat.perms.has(sender, "livechat.ignore") || LiveChat.perms.has(sender, "livechat.admin") || sender.isOp()) {
 				if (args.length != 1) {
 					sender.sendMessage("\u00A7c" + MemStorage.locale.get("IGNORE_USAGE"));
 					return true;
 				} else {
 					target = sender.getServer().getPlayer(args[0]);
 					if (target == null) {
 						sender.sendMessage("\u00A7c" + MemStorage.locale.get("PLAYER_NOT_FOUND") + ".");
 						return true;
 					} else if (target.getName() == sender.getName()) {
 						sender.sendMessage("\u00A7c" + MemStorage.locale.get("NOT_WITH_YOURSELF") + ".");
 						return true;
 					} else if (Utils.isIgnored(target, (Player)sender)) {
 						MemStorage.ignore.remove(sender.getName() + "." + target.getName());
 						sender.sendMessage(target.getDisplayName() + " \u00A7e" + MemStorage.locale.get("UNIGNORED_PLAYER") + ".");
 						return true;
 					} else {
 						MemStorage.ignore.put(sender.getName() + "." + target.getName(), "");
 						sender.sendMessage(target.getDisplayName() + " \u00A7e" + MemStorage.locale.get("IGNORED_PLAYER") + ".");
 						return true;
 					}
 				}
 			} else {
 				sender.sendMessage("\u00A7c" + MemStorage.locale.get("NOT_PERMISSION") + ".");
 				return true;
 			}
 		}
 
 		if (command.getName().equalsIgnoreCase("livechat")) {
 			if ((args.length == 1) && args[0].equalsIgnoreCase("reload")) {
 				if (LiveChat.perms.has(sender, "livechat.admin") || sender.isOp() || Utils.isConsole(sender)) {
 					plugin.reloadConfig();
					MemStorage.conf = plugin.getConfig();
 					sender.sendMessage("\u00A7e" + MemStorage.locale.get("RELOAD_CONFIG"));
 					return true;
 				} else {
 					sender.sendMessage("\u00A7c" + MemStorage.locale.get("NOT_PERMISSION") + ".");
 					return true;
 				}
 			} else {
 				sender.sendMessage("\u00A77-----------------------\u00A76Live\u00A79Chat\u00A77-----------------------");
 				sender.sendMessage("\u00A77 * \u00A76A complete chat suite by \u00A7e" + plugin.getDescription().getAuthors() + "\u00A76.");
 				sender.sendMessage("\u00A77 * \u00A76Version: \u00A7e" + plugin.getDescription().getVersion());
 				sender.sendMessage("\u00A77-----------------------------------------------------");
 				return true;
 			}
 		}
 
 		if (command.getName().equalsIgnoreCase("admin")) {
 			if (Utils.isConsole(sender)) {
 				plugin.getLogger().info(MemStorage.locale.get("NOT_AS_CONSOLE") + ".");
 				return true;
 			}
 			if (args.length != 0) {
 				String[] channelAdminList = MemStorage.plugin.getConfig().getString("channel-admin-players").replaceAll(" ", "").split(",");
 				if (Utils.containsIgnoreCase(channelAdminList, sender.getName())) {
 					for (int i = 0; i < channelAdminList.length; i++ ) {
 						Player channelAdminUser = null;
 						channelAdminUser = sender.getServer().getPlayer(channelAdminList[i]);
 						if (channelAdminUser != null) {
 							channelAdminUser.sendMessage(Format.main(player, Utils.getMsg(args, 0, sender), "channelAdmin"));
 						}
 					}
 					return true;
 				} else {
 					sender.sendMessage("\u00A7c" + MemStorage.locale.get("NOT_IN_CHANNEL_ADMIN") + ".");
 					return true;
 				}
 			} else {
 				sender.sendMessage("\u00A7c" + MemStorage.locale.get("CHANNEL_USAGE") + ".");
 				return true;
 			}
 		}
 		return false;
 	}
 }
