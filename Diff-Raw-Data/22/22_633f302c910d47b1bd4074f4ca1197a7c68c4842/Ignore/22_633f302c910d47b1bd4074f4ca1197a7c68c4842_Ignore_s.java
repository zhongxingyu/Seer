 package ru.dark32.chat;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 
 public class Ignore implements IIgnore {
 	final private int	chaneles	= ChanelRegister.getChanels();
 	private String		message2;
 	private String		cantIgnorable;
 	private String		message;
 	private String		unIgnore;
 	private String		canTHelp;
 	private String		canTSeeSelf;
 	private String		canTSeeAll;
 	private String		signMoreOne;
 	private String		canTSeeTarget;
 	private String		canTUnIgnoreTarget;
 	private String		canTIgnore;
 	private String		noReason;
 	private String		canTIgnoreunIgnorable;
 
 	public Ignore(){
 		canTHelp = getLoc("ignore.canTHelp");
 		message2 = getLoc("ignore.Message2");
 		message = getLoc("ignore.Message");
 		cantIgnorable = getLoc("ignore.cantIgnorable");
 		unIgnore = getLoc("ignore.unIgnore");
 		canTSeeAll = getLoc("ignore.canTSeeAll");
 		canTSeeSelf = getLoc("ignore.canTSeeSelf");
 		canTIgnore = getLoc("ignore.canTIgnore");
 		signMoreOne = getLoc("ignore.signMoreOne");
 		canTSeeTarget = getLoc("ignore.canTSeeTarget");
 		canTUnIgnoreTarget = getLoc("ignore.canTUnIgnoreTarget");
 		noReason = getLoc("ignore.noReason");
 		canTIgnoreunIgnorable = getLoc("ignore.canTIgnoreunIgnorable");
 	}
 
 	private String getLoc(final String key ) {
 		return ChanelRegister.colorUTF8(Main.localeConfig.getString(key, key), 3);
 	}
 
 	private String getPlayerDeafString(final String playerName, final String targetName, final int chanel ) {
		return playerName + ".ignore." + ChanelRegister.getByIndex(chanel).getInnerName() + "." + targetName;
 	}
 
 	@Override
 	public boolean hasIgnore(CommandSender sender, String target, int chanel ) {
 		return Main.storage.getBoolean(getPlayerDeafString(sender.getName(), target, chanel));
 	}
 
 	@Override
 	public void caseIgnore(CommandSender sender, String target, int chanel, String reason ) {
 		if (hasntIgnorable(target)) {
 			sender.sendMessage(cantIgnorable.replace("$name", sender.getName()).replace("$ignore", target));
 		}
 
 		for (int i = 0; i < chaneles; i++) {
 			if (chanel == i || chanel == -1) {
 				Main.storage.set(getPlayerDeafString(sender.getName(), target, i), true);
 				Main.storage.set(getPlayerDeafString(sender.getName(), target, i) + "-reason", reason);
 			}
 		}
 		String chanelName = (chanel >= 0 && chanel < this.chaneles ? ChanelRegister.getByIndex(chanel).getName() : "a");
 		sender.sendMessage(message2.replace("$name", sender.getName()).replace("$channel", chanelName)
 				.replace("$reason", reason).replace("$ignore", target));
 	}
 
 	private void help(final CommandSender sender ) {
 		final List<String> msg = new ArrayList<String>();
 		msg.addAll(ValueStorage.deafHelp);
 		for (final String string : msg) {
 			sender.sendMessage(ChanelRegister.colorUTF8(string,3));
 		}
 	}
 
 	@Override
 	public void ignore(final String[] args, CommandSender sender ) {
 		final boolean hasHelp = Util.hasPermission(sender, Main.BASE_PERM + ".ignore.help");
 		final boolean hasSee = Util.hasPermission(sender, Main.BASE_PERM + ".ignore.see");
 		final boolean hasAll = Util.hasPermission(sender, Main.BASE_PERM + ".ignore.all");
 		final boolean hasSeeSelf = Util.hasPermission(sender, Main.BASE_PERM + ".ignore.see.self") || hasSee;
 		final boolean hasIgnore = Util.hasPermission(sender, Main.BASE_PERM + ".ignore.ignore");
 		final boolean hasUnIgnore = Util.hasPermission(sender, Main.BASE_PERM + ".ignore.unignore");
 		if (args.length == 0) {
 			if (!hasHelp) {
 				sender.sendMessage(canTHelp);
 				return;
 			}
 			help(sender);
 		} else if (args.length == 1) {
 			if (args[0].equalsIgnoreCase("see")) {
 				if (!hasSeeSelf) {
 					sender.sendMessage(canTSeeSelf);
 					return;
 				}
 				seeSelf(sender);
 			} else if (args[0].equalsIgnoreCase("all")) {
 				if (!hasAll) {
 					sender.sendMessage(canTSeeAll);
 					return;
 				}
 				seeAll(sender);
 			} else {
 				if (!hasSee) {
 					sender.sendMessage(canTSeeTarget);
 					return;
 				}
 				final String target = args[0];
 				sender.sendMessage(ChatColor.GRAY + "$" + target + " :");
 				seeTarget(sender, target);
 			}
 		} else if (args.length > 1) {
 			if (args[1].length() != 1) {
 				sender.sendMessage(signMoreOne.replace("$sign", args[0]));
 				return;
 			}
 			final int chanel = ChanelRegister.getIndexBySign(args[1].charAt(0));
 			if (args.length > 2 && args[2].equals("unignore")) {
 				if (!hasUnIgnore) {
 					sender.sendMessage(canTUnIgnoreTarget);
 					return;
 				}
 				final String target = args[0];
 				if (hasntIgnorable(target)) {
 					sender.sendMessage(canTIgnoreunIgnorable);
 					return;
 				}
 				caseUnIgnore(sender, target, chanel);
 				return;
 
 			}
 			final String reason = args.length >= 3 ? StringUtils.join(args, " ", 2, args.length) : noReason;
 			if (!hasIgnore) {
 				sender.sendMessage(canTIgnore);
 				return;
 			}
 			final String target = args[0];
 			caseIgnore(sender, target, chanel, reason);;
 			return;
 
 		}
 
 	}
 
 	@Override
 	public void seeTarget(CommandSender sender, String target ) {
 		final ConfigurationSection cs = Main.storage.getConfigurationSection(sender.getName() + ".ignore");
 		if (cs == null) {
 			return;
 		}
 		final Set<String> list = cs.getKeys(false);
 		for (final String ignoreName : list) {
 			for (int i = 0; i < chaneles; i++) {
 				final String reason = Main.storage
 						.getString(getPlayerDeafString(target, ignoreName, i) + "-reason", "");
 				final boolean isIgnore = this.hasIgnore(sender, target, i);
 				if (isIgnore) {
 					sender.sendMessage(message.replace("$name", ignoreName)
 							.replace("$channel", ChanelRegister.getByIndex(i).getName()).replace("$reason", reason));
 				}
 			}
 		}
 	}
 
 	@Override
 	public void seeSelf(CommandSender self ) {
 		seeTarget(self, self.getName());
 	}
 
 	@Override
 	public void seeAll(CommandSender sender ) {
 		final ConfigurationSection cs = Main.storage.getRoot();
 		sender.sendMessage(ChatColor.GRAY + "$===============all================");
 		if (cs == null) {
 			return;
 		}
 		final Set<String> list = cs.getKeys(false);
 		for (final String name : list) {
 			seeTarget(sender, name);
 		}
 
 	}
 
 	@Override
 	public void caseUnIgnore(CommandSender sender, String target, int chanel ) {
 		for (int i = 0; i < chaneles; i++) {
 			if (chanel == i || chanel == -1) {
 				Main.storage.set(getPlayerDeafString(sender.getName(), target, i), true);
 				Main.storage.set(getPlayerDeafString(sender.getName(), target, i) + "-reason", "");
 			}
 		}
 		String chanelName = (chanel >= 0 && chanel < this.chaneles ? ChanelRegister.getByIndex(chanel).getName() : "a");
 		sender.sendMessage(unIgnore.replace("$name", sender.getName()).replace("$channel", chanelName)
 				.replace("$ignore", target));
 
 	}
 
 	@Override
 	public boolean hasntIgnorable(String target ) {
 		return Util.hasPermission(target, Main.BASE_PERM + ".ignore.non");
 	}
 
 }
