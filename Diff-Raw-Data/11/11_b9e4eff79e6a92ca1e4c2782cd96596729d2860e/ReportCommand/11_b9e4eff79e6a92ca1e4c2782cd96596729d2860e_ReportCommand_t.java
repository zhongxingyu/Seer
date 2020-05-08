 package me.heldplayer.ModeratorGui;
 
 import java.util.List;
 
 import me.heldplayer.ModeratorGui.tables.Bans;
 import me.heldplayer.ModeratorGui.tables.Demotions;
 import me.heldplayer.ModeratorGui.tables.Issues;
 import me.heldplayer.ModeratorGui.tables.Lists;
 import me.heldplayer.ModeratorGui.tables.Promotions;
 import me.heldplayer.ModeratorGui.tables.Unbans;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class ReportCommand implements CommandExecutor {
 
 	private final ModeratorGui main;
 
 	public ReportCommand(ModeratorGui plugin) {
 		main = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
 		if (args.length <= 0) {
 			return false;
 		}
 
 		long timeStamp = System.currentTimeMillis();
 
 		if (args[0].equalsIgnoreCase("issue") && args.length > 2 && sender.hasPermission("moderatorgui.issue")) {
 			List<String> matchedNames = ModeratorGui.getPlayerMatches(args[1]);
 
 			String name = "";
 
 			if (matchedNames.size() == 1) {
 				name = matchedNames.get(0);
 			} else if (matchedNames.size() == 0) {
 				sender.sendMessage(ChatColor.RED + "No match found for '" + args[1] + "'");
 
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
 				String matches = "";
 
 				for (String matched : matchedNames) {
 					matches += ", " + matched;
 				}
 
 				matches = matches.replaceFirst(", ", "").trim();
 
 				sender.sendMessage(ChatColor.GRAY + matches);
 
 				return true;
 			}
 
 			String issue = "";
 
 			for (int i = 2; i < args.length; i++) {
 				issue += " " + args[i];
 			}
 
 			issue = issue.trim();
 
 			Issues issueRow = new Issues();
 			issueRow.setIssue(issue);
 			issueRow.setReported(name);
 			issueRow.setReporter(sender.getName());
 			issueRow.setTimestamp(timeStamp);
 
 			main.getDatabase().save(issueRow);
 
 			Issues created = main.getDatabase().find(Issues.class).where().eq("timestamp", timeStamp).findUnique();
 			report(created.getId(), ReportType.ISSUE, sender.getName(), name);
 
 			String reportString = main.formatReport(main.displayStrings[0], issueRow.getId(), issueRow.getReported(), issueRow.getReporter(), issueRow.getIssue(), issueRow.getTimestamp(), null, null);
 
 			main.getServer().broadcast(ChatColor.GRAY + sender.getName() + " reported a new issue.", "moderatorgui.viewreported");
 			main.getServer().broadcast(reportString, "moderatorgui.viewreported");
 
 			sender.sendMessage(ChatColor.GREEN + "Reported!");
 
 			return true;
 		}
 
 		if (args[0].equalsIgnoreCase("ban") && args.length > 2 && sender.hasPermission("moderatorgui.ban")) {
 			List<String> matchedNames = ModeratorGui.getPlayerMatches(args[1]);
 
 			String name = "";
 
 			if (matchedNames.size() == 1) {
 				name = matchedNames.get(0);
 			} else if (matchedNames.size() == 0) {
 				sender.sendMessage(ChatColor.RED + "No match found for '" + args[1] + "'");
 
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
 				String matches = "";
 
 				for (String matched : matchedNames) {
 					matches += ", " + matched;
 				}
 
 				matches = matches.replaceFirst(", ", "").trim();
 
 				sender.sendMessage(ChatColor.GRAY + matches);
 
 				return true;
 			}
 			String reason = "";
 
 			for (int i = 2; i < args.length; i++) {
 				reason += " " + args[i];
 			}
 
 			reason = reason.trim();
 
 			Bans banRow = new Bans();
 			banRow.setReason(reason);
 			banRow.setBanned(name);
 			banRow.setBanner(sender.getName());
 			banRow.setTimestamp(timeStamp);
 
 			main.getDatabase().save(banRow);
 
 			Bans created = main.getDatabase().find(Bans.class).where().eq("timestamp", timeStamp).findUnique();
 			report(created.getId(), ReportType.BAN, sender.getName(), name);
 
			String reportString = main.formatReport(main.displayStrings[4], banRow.getId(), banRow.getBanned(), banRow.getBanner(), banRow.getReason(), banRow.getTimestamp(), null, null);
 
 			main.getServer().broadcast(ChatColor.GRAY + sender.getName() + " reported a new ban.", "moderatorgui.viewreported");
 			main.getServer().broadcast(reportString, "moderatorgui.viewreported");
 
 			sender.sendMessage(ChatColor.GREEN + "Reported!");
 
 			return true;
 		}
 
 		if (args[0].equalsIgnoreCase("unban") && args.length > 2 && sender.hasPermission("moderatorgui.unban")) {
 			List<String> matchedNames = ModeratorGui.getPlayerMatches(args[1]);
 
 			String name = "";
 
 			if (matchedNames.size() == 1) {
 				name = matchedNames.get(0);
 			} else if (matchedNames.size() == 0) {
 				sender.sendMessage(ChatColor.RED + "No match found for '" + args[1] + "'");
 
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
 				String matches = "";
 
 				for (String matched : matchedNames) {
 					matches += ", " + matched;
 				}
 
 				matches = matches.replaceFirst(", ", "").trim();
 
 				sender.sendMessage(ChatColor.GRAY + matches);
 
 				return true;
 			}
 
 			String reason = "";
 
 			for (int i = 2; i < args.length; i++) {
 				reason += " " + args[i];
 			}
 
 			reason = reason.trim();
 
 			Unbans unbanRow = new Unbans();
 			unbanRow.setReason(reason);
 			unbanRow.setUnbanned(name);
 			unbanRow.setUnbanner(sender.getName());
 			unbanRow.setTimestamp(timeStamp);
 
 			main.getDatabase().save(unbanRow);
 
 			Unbans created = main.getDatabase().find(Unbans.class).where().eq("timestamp", timeStamp).findUnique();
 			report(created.getId(), ReportType.UNBAN, sender.getName(), name);
 
			String reportString = main.formatReport(main.displayStrings[5], unbanRow.getId(), unbanRow.getUnbanned(), unbanRow.getUnbanner(), unbanRow.getReason(), unbanRow.getTimestamp(), null, null);
 
 			main.getServer().broadcast(ChatColor.GRAY + sender.getName() + " reported a new unban.", "moderatorgui.viewreported");
 			main.getServer().broadcast(reportString, "moderatorgui.viewreported");
 
 			sender.sendMessage(ChatColor.GREEN + "Reported!");
 
 			return true;
 		}
 
 		if (args[0].equalsIgnoreCase("promote") && args.length > 4 && sender.hasPermission("moderatorgui.promote")) {
 			List<String> matchedNames = ModeratorGui.getPlayerMatches(args[1]);
 
 			String name = "";
 
 			if (matchedNames.size() == 1) {
 				name = matchedNames.get(0);
 			} else if (matchedNames.size() == 0) {
 				sender.sendMessage(ChatColor.RED + "No match found for '" + args[1] + "'");
 
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
 				String matches = "";
 
 				for (String matched : matchedNames) {
 					matches += ", " + matched;
 				}
 
 				matches = matches.replaceFirst(", ", "").trim();
 
 				sender.sendMessage(ChatColor.GRAY + matches);
 
 				return true;
 			}
 
 			List<String> matchedRanks1 = ModeratorGui.getRankMatches(args[2]);
 
 			String rank1 = "";
 
 			if (matchedRanks1.size() == 1) {
 				rank1 = matchedRanks1.get(0);
 			} else if (matchedRanks1.size() == 0) {
 				sender.sendMessage(ChatColor.RED + "No match found for '" + args[2] + "'");
 
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
 				String matches = "";
 
 				for (String matched : matchedRanks1) {
 					matches += ", " + matched;
 				}
 
 				matches = matches.replaceFirst(", ", "").trim();
 
 				sender.sendMessage(ChatColor.GRAY + matches);
 
 				return true;
 			}
 
 			List<String> matchedRanks2 = ModeratorGui.getRankMatches(args[3]);
 
 			String rank2 = "";
 
 			if (matchedRanks2.size() == 1) {
 				rank2 = matchedRanks2.get(0);
 			} else if (matchedRanks2.size() == 0) {
 				sender.sendMessage(ChatColor.RED + "No match found for '" + args[3] + "'");
 
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
 				String matches = "";
 
 				for (String matched : matchedRanks2) {
 					matches += ", " + matched;
 				}
 
 				matches = matches.replaceFirst(", ", "").trim();
 
 				sender.sendMessage(ChatColor.GRAY + matches);
 
 				return true;
 			}
 
 			String reason = "";
 
 			for (int i = 4; i < args.length; i++) {
 				reason += " " + args[i];
 			}
 
 			reason = reason.trim();
 
 			Promotions promotionRow = new Promotions();
 			promotionRow.setReason(reason);
 			promotionRow.setPromoted(name);
 			promotionRow.setPromoter(sender.getName());
 			promotionRow.setPrevRank(rank1);
 			promotionRow.setNewRank(rank2);
 			promotionRow.setTimestamp(timeStamp);
 
 			main.getDatabase().save(promotionRow);
 
 			Promotions created = main.getDatabase().find(Promotions.class).where().eq("timestamp", timeStamp).findUnique();
 			report(created.getId(), ReportType.PROMOTE, sender.getName(), name);
 
			String reportString = main.formatReport(main.displayStrings[2], promotionRow.getId(), promotionRow.getPromoted(), promotionRow.getPromoter(), promotionRow.getReason(), promotionRow.getTimestamp(), promotionRow.getPrevRank(), promotionRow.getNewRank());
 
 			main.getServer().broadcast(ChatColor.GRAY + sender.getName() + " reported a new promotion.", "moderatorgui.viewreported");
 			main.getServer().broadcast(reportString, "moderatorgui.viewreported");
 
 			sender.sendMessage(ChatColor.GREEN + "Reported!");
 
 			return true;
 		}
 
 		if (args[0].equalsIgnoreCase("demote") && args.length > 4 && sender.hasPermission("moderatorgui.demote")) {
 			List<String> matchedNames = ModeratorGui.getPlayerMatches(args[1]);
 
 			String name = "";
 
 			if (matchedNames.size() == 1) {
 				name = matchedNames.get(0);
 			} else if (matchedNames.size() == 0) {
 				sender.sendMessage(ChatColor.RED + "No match found for '" + args[1] + "'");
 
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
 				String matches = "";
 
 				for (String matched : matchedNames) {
 					matches += ", " + matched;
 				}
 
 				matches = matches.replaceFirst(", ", "").trim();
 
 				sender.sendMessage(ChatColor.GRAY + matches);
 
 				return true;
 			}
 
 			List<String> matchedRanks1 = ModeratorGui.getRankMatches(args[2]);
 
 			String rank1 = "";
 
 			if (matchedRanks1.size() == 1) {
 				rank1 = matchedRanks1.get(0);
 			} else if (matchedRanks1.size() == 0) {
 				sender.sendMessage(ChatColor.RED + "No match found for '" + args[2] + "'");
 
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
 				String matches = "";
 
 				for (String matched : matchedRanks1) {
 					matches += ", " + matched;
 				}
 
 				matches = matches.replaceFirst(", ", "").trim();
 
 				sender.sendMessage(ChatColor.GRAY + matches);
 
 				return true;
 			}
 
 			List<String> matchedRanks2 = ModeratorGui.getRankMatches(args[3]);
 
 			String rank2 = "";
 
 			if (matchedRanks2.size() == 1) {
 				rank2 = matchedRanks2.get(0);
 			} else if (matchedRanks2.size() == 0) {
 				sender.sendMessage(ChatColor.RED + "No match found for '" + args[3] + "'");
 
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
 				String matches = "";
 
 				for (String matched : matchedRanks2) {
 					matches += ", " + matched;
 				}
 
 				matches = matches.replaceFirst(", ", "").trim();
 
 				sender.sendMessage(ChatColor.GRAY + matches);
 
 				return true;
 			}
 
 			String reason = "";
 
 			for (int i = 4; i < args.length; i++) {
 				reason += " " + args[i];
 			}
 
 			reason = reason.trim();
 
 			Demotions demotionRow = new Demotions();
 			demotionRow.setReason(reason);
 			demotionRow.setDemoted(name);
 			demotionRow.setDemoter(sender.getName());
 			demotionRow.setPrevRank(rank1);
 			demotionRow.setNewRank(rank2);
 			demotionRow.setTimestamp(timeStamp);
 
 			main.getDatabase().save(demotionRow);
 
 			Demotions created = main.getDatabase().find(Demotions.class).where().eq("timestamp", timeStamp).findUnique();
 			report(created.getId(), ReportType.DEMOTE, sender.getName(), name);
 
 			sender.sendMessage(ChatColor.GREEN + "Reported!");
 
			String reportString = main.formatReport(main.displayStrings[3], demotionRow.getId(), demotionRow.getDemoted(), demotionRow.getDemoter(), demotionRow.getReason(), demotionRow.getTimestamp(), demotionRow.getPrevRank(), demotionRow.getNewRank());
 
 			main.getServer().broadcast(ChatColor.GRAY + sender.getName() + " reported a new demotion.", "moderatorgui.viewreported");
 			main.getServer().broadcast(reportString, "moderatorgui.viewreported");
 
 			return true;
 		}
 
 		if (args[0].equalsIgnoreCase("help")) {
 			if (sender.hasPermission("moderatorgui.issue"))
 				sender.sendMessage(ChatColor.GRAY + "/" + alias + " issue <playername> <issue>");
 			if (sender.hasPermission("moderatorgui.ban"))
 				sender.sendMessage(ChatColor.GRAY + "/" + alias + " ban <playername> <reason>");
 			if (sender.hasPermission("moderatorgui.unban"))
 				sender.sendMessage(ChatColor.GRAY + "/" + alias + " unban <playername> <reason>");
 			if (sender.hasPermission("moderatorgui.promote"))
 				sender.sendMessage(ChatColor.GRAY + "/" + alias + " promote <playername> <oldrank> <newrank> <reason>");
 			if (sender.hasPermission("moderatorgui.demote"))
 				sender.sendMessage(ChatColor.GRAY + "/" + alias + " demote <playername> <oldrank> <newrank> <reason>");
 
 			return true;
 		}
 
 		return false;
 	}
 
 	private void report(int id, ReportType type, String reporter, String target) {
 		Lists listRow = new Lists();
 
 		listRow.setReportId(id);
 		listRow.setType(type.getId());
 		listRow.setReporter(reporter);
 		listRow.setTarget(target);
 
 		main.getDatabase().save(listRow);
 	}
 }
