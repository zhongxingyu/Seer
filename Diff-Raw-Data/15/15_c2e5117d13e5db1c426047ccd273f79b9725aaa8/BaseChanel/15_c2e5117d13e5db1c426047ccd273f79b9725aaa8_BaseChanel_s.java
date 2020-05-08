 package ru.dark32.chat.chanels;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Instrument;
 import org.bukkit.Note;
 import org.bukkit.Sound;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import ru.dark32.chat.ChanelRegister;
 import ru.dark32.chat.Main;
 import ru.dark32.chat.PEXHook;
 import ru.dark32.chat.SimpleClanHook;
 import ru.dark32.chat.Util;
 import ru.dark32.chat.ichanels.ETypeChanel;
 import ru.dark32.chat.ichanels.IChanel;
 
 public class BaseChanel implements IChanel {
 	final private String		colorize;
 	protected final int			COUNT_EXCLUDE	= 1;
 	protected final int			COUNT_INCLUDE	= -1;
 	protected final int			COUNT_OFF		= 0;
 	final private boolean		enable;
 	final private String		formatString;
 	final private int			index;
 	final private String		innerName;
 	final private boolean		isWorld;
 	final private String		listenerMessage;
 	final private int			listenerMessageEnable;
 	final private String		name;
 	final private boolean		needPerm;
 	final private String		noListenerMessage;
 	final private boolean		overAll;
 	final private boolean		pimkEnable;
 	final private Instrument	pimkInstrument;
 
 	final private Note			pimkNote;
 	final private char			prefix;
 	final private char			sign;
 	final private boolean		tabes;
 	private ETypeChanel			type;
 	private boolean				clanOnly;
 	private boolean				allyOnly;
 
 	public BaseChanel(String par_name ){
 		final String path_enable = "Chat." + par_name + ".enable";
 		final String path_name = "Chat." + par_name + ".name";
 		final String path_world = "Chat." + par_name + ".world";
 		final String path_prefix = "Chat." + par_name + ".prefix";
 		final String path_sign = "Chat." + par_name + ".sign";
 		final String path_format = "Chat." + par_name + ".format";
 		final String path_tab = "Chat." + par_name + ".tab";
 		final String path_listenerMessage = "Chat." + par_name + ".listenerMessage";
 		final String path_noListenerMessage = "Chat." + par_name + ".noListenerMessage";
 		final String path_isListenerMessage = "Chat." + par_name + ".isListenerMessage";
 		final String path_needPerm = "Chat." + par_name + ".needPerm";
 		final String path_pimk_note = "Chat." + par_name + ".pimk.note";
 		final String path_pimk_enable = "Chat." + par_name + ".pimk.enable";
 		final String path_pimk_instrument = "Chat." + par_name + ".pimk.instrument";
 		final String path_pimk_colorize = "Chat." + par_name + ".pimk.colorize";
 		final String path_overAll = "Chat." + par_name + ".overAll";
 		final String path_clan = "Chat." + par_name + ".clan";
 
 		this.index = ChanelRegister.getNextIndex();
 		this.innerName = par_name.toLowerCase(Locale.US);
 		this.enable = Main.chatConfig.getBoolean(path_enable, false);
 		this.name = Main.chatConfig.getString(path_name, path_name);
 		this.isWorld = Main.chatConfig.getBoolean(path_world, false);
 		this.prefix = Main.chatConfig.getString(path_prefix, path_prefix).charAt(0);
 		this.sign = Main.chatConfig.getString(path_sign, path_sign).charAt(0);
 		this.formatString = ChanelRegister.colorUTF8(Main.chatConfig.getString(path_format, path_format), 3);
 		this.tabes = Main.chatConfig.getBoolean(path_tab, true);
 		this.listenerMessage = ChanelRegister.colorUTF8(Main.chatConfig.getString(path_listenerMessage, ""), 3);
 		this.noListenerMessage = ChanelRegister.colorUTF8(Main.chatConfig.getString(path_noListenerMessage, ""), 3);
 		this.listenerMessageEnable = Main.chatConfig.getInt(path_isListenerMessage, 0);
 		this.needPerm = Main.chatConfig.getBoolean(path_needPerm, false);
 
 		// PIMK -->
 		String note = Main.chatConfig.getString(path_pimk_note, "1F#");
 		int octava = note.length() > 0 ? note.charAt(0) : 1;
 		Note.Tone tone = Note.Tone.F;
 		boolean sharped = false;
 		if (note.length() >= 2 && note.length() <= 3) {
 			char char0 = note.charAt(0);
 			char char1 = note.charAt(1);
 			octava = (char0 == '2') ? 2 : (char0 == '0' ? 0 : char0 == '1' ? 1 : 1);
 			tone = ('A' <= char1 && 'F' >= char1) ? Note.Tone.valueOf(String.valueOf(char1)) : Note.Tone.F;
 			sharped = (note.length() == 3 && note.charAt(1) == '#');
 		}
 		// <-- PIMK
 		this.pimkEnable = Main.chatConfig.getBoolean(path_pimk_enable, false);
 		this.pimkInstrument = Instrument.valueOf(Main.chatConfig.getString(path_pimk_instrument, "PIANO"));
 		this.pimkNote = new Note(octava, tone, sharped);
 		this.colorize = Main.chatConfig.getString(path_pimk_colorize, "@");
 		this.overAll = Main.chatConfig.getBoolean(path_overAll, true);
 		String clanly = Main.chatConfig.getString(path_clan, "none");
 		this.clanOnly = clanly.equalsIgnoreCase("clan");
 		this.allyOnly = clanly.equalsIgnoreCase("ally");
 	}
 
 	@Override
 	public boolean canSend(final Player sender, final String message ) {
 		return true;
 	}
 
 	void DEBUG(String message ) {
 		if (Main.DEBUG_MODE) {
 			DEBUG(message, Bukkit.getConsoleSender());
 		}
 	}
 
 	void DEBUG(String message, CommandSender sender ) {
 		if (Main.DEBUG_MODE) {
 			sender.sendMessage(message);
 		}
 	}
 
 	private String findMatch(String name, String[] message ) {
 		for (String s : message) {
 			if (name.startsWith(s)) {
 				return s;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public String format(final Player player, String msg ) {
 		if (Main.SCenable) {
 			// msg = SimpleClanHook.formatComplete(msg, player);
 		}
 		if (msg.contains("$suffix")) {
 			msg = msg.replace("$suffix", PEXHook.getSuffix(player.getName()));
 		}
 		if (msg.contains("$prefix")) {
 			msg = msg.replace("$prefix", PEXHook.getPreffix(player.getName()));
 		}
 		if (msg.contains("$p")) {
 			msg = msg.replace("$p", "%1$s");
 		}
 		if (msg.contains("$msg")) {
 			msg = msg.replace("$msg", "%2$s");
 		}
 		if (msg.contains("$id")) {
 			String iden = Integer.toHexString(player.getTicksLived() + player.getEntityId());
 
 			msg = msg.replace("$id", iden);
 		}
 		return msg;
 	}
 
 	@Override
 	public String getColorize() {
 		return colorize;
 	}
 
 	@Override
 	public String getFormat() {
 		return this.formatString;
 	}
 
 	@Override
 	public int getIndex() {
 		return this.index;
 	}
 
 	@Override
 	public String getInnerName() {
 		return innerName;
 	}
 
 	@Override
 	public String getListenerMessage(int count ) {
 		if (count > 0) {
 			if (listenerMessage.length() > 0 && listenerMessage.contains("$n")) {
 				return listenerMessage.replace("$n", String.valueOf(count));
 			}
 		} else {
 			if (noListenerMessage.length() > 0) {
 				return noListenerMessage;
 			}
 		}
 		return "";
 	}
 
 	@Override
 	public String getName() {
 		return this.name;
 	}
 
 	@Override
 	public Instrument getPimkInstrument() {
 		return pimkInstrument;
 	}
 
 	@Override
 	public Note getPimkNote() {
 		return pimkNote;
 	}
 
 	@Override
 	public char getPrefix() {
 		return this.prefix;
 	}
 
 	@Override
 	public Set<Player> getRecipients(final Player sender ) {
 		final Set<Player> recipients = new HashSet<Player>();
 		for (final Player recipient : Bukkit.getServer().getOnlinePlayers()) {
 			/*
 			 * if (!getClan() || !SimpleClanHook.equalClan(sender, recipient)) {
 			 * DEBUG("debug: hasn't in clan - " + recipient.getName(), sender);
 			 * continue; } else
 			 */
 			if (isRecipient(sender, recipient)) {
 				DEBUG("debug: isn't Recipient - " + recipient.getName(), sender);
 				continue;
 			} else {
 				recipients.add(recipient);
 			}
 		}
 		return recipients;
 	}
 
 	@Override
 	public char getSign() {
 		return this.sign;
 	}
 
 	@Override
 	public ETypeChanel getType() {
 		return this.type;
 	}
 
 	@Override
 	public boolean isEnable() {
 		return enable;
 	}
 
 	@Override
 	public int isListenerMessage() {
 		return listenerMessageEnable;
 	}
 
 	@Override
 	public boolean isNeedPerm() {
 		return needPerm;
 	}
 
 	@Override
 	public boolean isOverAll() {
 		return overAll;
 	}
 
 	@Override
 	public boolean isPimk() {
 		return pimkEnable;
 	}
 
 	/**
 	 * 
 	 * @param sender
 	 * @param recipient
 	 * @return Глухота, Слышит, Сам, В канале , Игнорируем<br>
 	 *         !isInChanel || isSelf || !isHear || isDeaf || hasIgnore || isClan
 	 *         || isAlly
 	 */
 	protected boolean isRecipient(final Player sender, final Player recipient ) {
 		final boolean isDeaf = !Main.getDeafStorage().isDeaf(recipient.getName(), getIndex());
 		final boolean isHear = !isNeedPerm()
 				|| Util.hasPermission(recipient, Main.BASE_PERM + "." + getInnerName() + ".say")
 				|| Util.hasPermission(recipient, Main.BASE_PERM + "." + getInnerName() + ".hear");
 		final boolean isSelf = (sender == recipient && isListenerMessage() == COUNT_INCLUDE);
 		final boolean isInChanel = isOverAll() || Util.getModeIndex(recipient.getName()) == getIndex();
 		final boolean hasIgnore = Main.getIgnoreStorage().hasIgnore(recipient.getName(), sender.getName(),
 				this.getIndex());
 		final boolean isClan = !this.isClan() || SimpleClanHook.equalClan(sender, recipient);
 		final boolean isAlly = !this.isAlly() || SimpleClanHook.equalAlly(sender, recipient);
 		final boolean allCond = isInChanel && !isSelf && isHear && isDeaf && !hasIgnore && isClan && isAlly;
 		DEBUG(recipient.getName());
 		DEBUG("isInChanel " + isInChanel + " isn'tSelf " + !isSelf + " isHear " + isHear);
 		DEBUG("hasDeaf " + isDeaf + " hasn'tIgnore " + !hasIgnore + " all " + allCond);
 		DEBUG("isClan" + isClan + " isAlly" + isAlly);
 		return !allCond;
 	}
 
 	@Override
 	public boolean isTabes() {
 		return tabes;
 	}
 
 	@Override
 	public boolean isWorldChat() {
 		return this.isWorld;
 	}
 
 	@Override
 	public String preformatMessage(final Player sender, final String message ) {
 		// return Util.randomRoll(message);
 		return message;
 	}
 
 	@SuppressWarnings("deprecation" )
 	@Override
 	public void preSend(final Player sender, final String message, final Set<Player> recipient ) {
 		// pimk
 		if (isPimk()) {
 			// FIX TO DO IT !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 			final String[] messageArr = message.replace(",", "").replace(".", "").replace(":", "").replace(";", "")
 					.replace("?", "").replace("!", "").replace("@", "").split(" ");
 			for (final Iterator<Player> iterator = recipient.iterator(); iterator.hasNext();) {
 				final Player player = iterator.next();
 				final String name = player.getName();
 				final String match = findMatch(name, messageArr);
 				if (match != null) {
 					player.playSound(player.getLocation(), Sound.valueOf("NOTE_" + getPimkInstrument()), 3f,
 							getPimkNote().getId());
 					sender.sendMessage(getListenerMessage(recipient.size())
 							+ format(sender, getFormat()).replace("%2$s", message.replace(match, name))
 									.replace("%1$s", sender.getName())
 									.replace(match, getColorize() + match + ChatColor.getLastColors(message)));
 					iterator.remove();
 				}
 			}
 		}
 		// отправляем число услышавших, если это включено
 		if (isListenerMessage() == COUNT_EXCLUDE) {
 			String str = getListenerMessage(recipient.size() - 1);
 			if (str.length() > 0) {
 				sender.sendMessage(str);
 			}
 		} else if (isListenerMessage() == COUNT_INCLUDE) {
 			sender.sendMessage(getListenerMessage(recipient.size())
 					+ String.format(format(sender, getFormat()), sender.getName(), message));
 		}
 	}
 
 	@Override
 	public void setType(final ETypeChanel type ) {
 		this.type = type;
 
 	}
 
 	@Override
 	public String toString() {
 		return super.toString() + ", index =>" + this.index + ", isWorld =>" + this.isWorld + ", name =>" + this.name
 				+ ", prefix =>" + this.prefix + ", sign =>" + this.sign + ", type =>" + this.type;
 	}
 
 	@Override
 	public boolean isClan() {
 		return clanOnly && Main.SCenable;
 	}
 
 	@Override
 	public Set<Player> getSpyRecipients(Player sender ) {
 		final Set<Player> recipients = new HashSet<Player>();
 		for (final Player recipient : Bukkit.getServer().getOnlinePlayers()) {
 			if (Util.hasPermission(recipient, Main.BASE_PERM + ".spy") && sender != recipient) {
 				DEBUG("debug: spy - " + recipient.getName(), sender);
 				recipients.add(recipient);
 				continue;
 			}
 		}
 		return recipients;
 	}
 
 	@Override
 	public boolean isAlly() {
 		return allyOnly && Main.SCenable;
 	}
 }
