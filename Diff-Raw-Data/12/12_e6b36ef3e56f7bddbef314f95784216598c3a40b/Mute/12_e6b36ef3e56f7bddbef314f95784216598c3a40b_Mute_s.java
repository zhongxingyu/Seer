 package ru.dark32.chat;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 
 import ru.dark32.chat.chanels.BaseChanel;
 
 public class Mute implements IMute {
 	private final SimpleDateFormat	SDF			= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	final private int				chaneles	= ChanelRegister.getChanels();
 	final private String			needName;
 	final private String			muteMessage;
 	final private String			unmuteMessage;
 	final private String			canTHelp;
 	final private String			canTSeeSelf;
 	final private String			canTSeeAllMute;
 	final private String			canTSeeTargetMute;
 	final private String			signMoreOne;
 	final private String			canTUnmute;
 	final private String			canTMute;
 	final private String			noReason;
 	final private String			timeNotNum;
 	final private String			subCMDErr;
 	final private String			dataError;
 	final private String			muteSee;
 
 	public Mute(){
 		needName = getLoc("mute.needName");
 		muteMessage = getLoc("mute.muteMessage");
 		unmuteMessage = getLoc("mute.unmuteMessage");
 		canTHelp = getLoc("mute.canTHelp");
 		canTSeeSelf = getLoc("mute.canTSeeSelf");
 		canTSeeAllMute = getLoc("mute.canTSeeAllMute");
 		canTSeeTargetMute = getLoc("mute.canTSeeTargetMute");
 		signMoreOne = getLoc("mute.signMoreOne");
 		canTUnmute = getLoc("mute.canTUnmute");
 		canTMute = getLoc("mute.canTMute");
 		noReason = getLoc("mute.noReason");
 		timeNotNum = getLoc("mute.timeNotNum");
 		subCMDErr = getLoc("mute.subCMDErr");
 		dataError = getLoc("mute.dataError");
 		muteSee = getLoc("mute.muteSee");
 	}
 
 	private String getLoc(final String key ) {
 		return ChanelRegister.colorUTF8(Main.localeConfig.getString(key, key), 3);
 	}
 
 	private String getPlayerMuteString(final String playerName, final int chanel ) {
 		return playerName.toLowerCase(Locale.US) + ".mute." + ChanelRegister.getByIndex(chanel).getInnerName();
 	}
 
 	@Override
 	public boolean isMuted(final String playerName, final int chanel ) {
 		if (getTimeMute(playerName, chanel) > 0) {
 			return true;
 		} else {
 			unmute(playerName, chanel);
 			return false;
 		}
 	}
 
 	@Override
 	public void caseMute(final CommandSender sender, final String name, final int chanel, final int time,
 			final String reason ) {
 		if ("empty".equals(name)) {
 			sender.sendMessage(needName);
 			return;
 		}
 		for (int i = 0; i < chaneles; i++) {
 			if (chanel == i || chanel == -1) {
 				final Calendar cal = Calendar.getInstance();
 				cal.add(Calendar.SECOND, time);
 				Main.storage.set(getPlayerMuteString(name, i), SDF.format(cal.getTime()));
 				Main.storage.set(getPlayerMuteString(name, i) + "-reason", reason);
 			}
 		}
 		final String _chanelName = (chanel >= 0 && chanel < chaneles ? ChanelRegister.getByIndex(chanel).getName()
 				: "a");
 		if (time > 5) {
 			String msg = muteSee;
 			if (msg.contains("$name")) {
 				msg = msg.replace("$name", name);
 			}
 			if (msg.contains("$channel")) {
 				msg = msg.replace("$channel", ChanelRegister.getByIndex(chanel).getName());
 			}
 			if (msg.contains("$reason")) {
 				msg = msg.replace("$reason", reason);
 			}
 			msg = unParseTime(msg, time);
 			msg = Util.suffixLatter(msg);
			sender.sendMessage(msg);
 		} else {
 			System.out.println(name);
 			sender.sendMessage(unmuteMessage.replace("$name", name).replace("$channel", _chanelName));
 		}
 		save();
 
 	}
 
 	@Override
 	public void mute(final String[] args, final CommandSender sender ) {
 		final boolean hasHelp = Main.getPermissionsHandler().hasPermission(sender, Main.BASE_PERM + ".mute.help");
 		final boolean hasSee = Main.getPermissionsHandler().hasPermission(sender, Main.BASE_PERM + ".mute.see");
 		final boolean hasAll = Main.getPermissionsHandler().hasPermission(sender, Main.BASE_PERM + ".mute.all");
 		final boolean hasSeeSelf = Main.getPermissionsHandler()
 				.hasPermission(sender, Main.BASE_PERM + ".mute.see.self") || hasSee;
 		final boolean hasMute = Main.getPermissionsHandler().hasPermission(sender, Main.BASE_PERM + ".mute.mute");
 		final boolean hasUnMute = Main.getPermissionsHandler().hasPermission(sender, Main.BASE_PERM + ".mute.unmute");
 		if (args.length == 0) {
 			if (!hasHelp) {
 				sender.sendMessage(canTHelp);
 				return;
 			}
 			muteHelp(sender);
 		} else if (args.length == 1) {
 			final String par = args[0];
 			if ("see".equals(par)) {
 				if (!hasSeeSelf) {
 					sender.sendMessage(canTSeeSelf);
 					return;
 				}
 				seeSelf(sender);
 			} else if ("all".equals(par)) {
 				if (!hasAll) {
 					sender.sendMessage(canTSeeAllMute);
 					return;
 				}
 				seeAll(sender);
 			} else {
 				if (!hasSee) {
 					sender.sendMessage(canTSeeTargetMute);
 					return;
 				}
 				sender.sendMessage(ChatColor.GRAY + "$" + par + " :");
 				seeTarget(sender, par);
 			}
 		} else if (args.length == 2) {
 			final String nick = args[0];
 			if (args[1].equals("see")) {
 				if (nick.equals(sender.getName())) {
 					if (!hasSeeSelf) {
 						sender.sendMessage(canTSeeSelf);
 						return;
 					}
 					seeSelf(sender);
 				} else {
 					if (!hasSee) {
 						sender.sendMessage(canTSeeTargetMute);
 						return;
 					}
 					seeTarget(sender, nick);
 				}
 			} else {
 				sender.sendMessage(subCMDErr);
 			}
 		} else if (args.length >= 3) {
 			if (!hasMute) {
 				sender.sendMessage(canTMute);
 				return;
 			}
 			if (args[1].length() != 1) {
 				sender.sendMessage(signMoreOne.replace("$sign", args[1]));
 				return;
 			}
 			int time = 0;
			// try {
 			time = Util.timeParse(args[2]);
 			if (time == 0) {
 				sender.sendMessage(timeNotNum + args[2]);
 			}
 			if (time < 5 && !hasUnMute) {
 				sender.sendMessage(canTUnmute);
 				return;
 			}
 			final String reason = args.length >= 3 ? StringUtils.join(args, " ", 3, args.length) : noReason;
 			final String nick = args[0];
 			final int chanel = ChanelRegister.getIndexBySign(args[1].charAt(0));
 			caseMute(sender, nick, chanel, time, reason);
 		}
 
 	}
 
 	@Override
 	public void save() {
 		Set<String> list;
 		final ConfigurationSection cs = Main.storage.getRoot();
 		if (cs != null) {
 			list = cs.getKeys(false);
 			for (final String name : list) {
 				for (int i = 0; i < chaneles; i++) {
 					isMuted(name, i);
 				}
 			}
 		}
 		try {
 			Main.storage.save(Main.storageFile);
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void unmute(final String playerName, final int chanel ) {
 		for (int i = 0; i < chaneles; i++) {
 			if (chanel == i || chanel == -1) {
 				Main.storage.set(getPlayerMuteString(playerName, i), null);
 				Main.storage.set(getPlayerMuteString(playerName, i) + "-reason", null);
 			}
 		}
 
 	}
 
 	@Override
 	public long getTimeMute(final String playerName, final int chanel ) {
 		final String dateStr = Main.storage.getString(getPlayerMuteString(playerName, chanel));
 		if (dateStr != null) {
 			try {
 				final Date date = SDF.parse(dateStr);
 				return (date.getTime() - System.currentTimeMillis()) / 1000;
 			}
 			catch (ParseException e) {
 				Bukkit.getConsoleSender().sendMessage(getPlayerMuteString(playerName, chanel));
 				Bukkit.getConsoleSender().sendMessage(dataError + dateStr);
 			}
 		}
 		return -1;
 
 	}
 
 	private void muteHelp(final CommandSender sender ) {
 		final List<String> msg = new ArrayList<String>();
 		msg.addAll(ValueStorage.muteHelp);
 		for (final String s : msg) {
 			sender.sendMessage(ChanelRegister.colorUTF8(s, 3));
 		}
 	}
 
 	@Override
 	public void seeAll(final CommandSender sender ) {
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
 	public void seeSelf(final CommandSender sender ) {
 		seeTarget(sender, sender.getName());
 	}
 
 	@Override
 	public void seeTarget(final CommandSender sender, final String name ) {
 		for (int i = 0; i < chaneles; i++) {
 			final long time = getTimeMute(name, i);
 			final String reason = Main.storage.getString(getPlayerMuteString(name, i) + "-reason");
 			if (time > -1) {
 				String msg = muteSee;
 				if (msg.contains("$name")) {
 					msg = msg.replace("$name", name);
 				}
 				if (msg.contains("$channel")) {
 					msg = msg.replace("$channel", ChanelRegister.getByIndex(i).getName());
 				}
 				if (msg.contains("$reason")) {
 					msg = msg.replace("$reason", reason);
 				}
 				msg = unParseTime(msg, time);
 				msg = Util.suffixLatter(msg);
 				sender.sendMessage(msg);
 			}
 		}
 
 	}
 
 	public static String unParseTime(String msg, long time ) {
 		if (msg.contains("$time")) {
 			msg = msg.replace("$time", String.valueOf(time));
 		}
 		if (msg.contains("$data.day")) {
 			long day = time / Util.day;
 			msg = msg.replace("$data.day", Long.toString(day));
 		}
 		if (msg.contains("$data.hour")) {
 			long hour = (time % Util.day) / Util.hour;
 			msg = msg.replace("$data.hour", Long.toString(hour));
 		}
 		if (msg.contains("$data.minute")) {
 			long minute = (time % Util.hour) / Util.minute;
 			msg = msg.replace("$data.minute", Long.toString(minute));
 		}
 		if (msg.contains("$data.second")) {
 			long second = (time % Util.minute) / Util.secunde;
 			msg = msg.replace("$data.second", Long.toString(second));
 		}
 		return msg;
 	}
 }
