 /**
  * 
  */
 package ru.dark32.chat.chanels;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import ru.dark32.chat.ChanelRegister;
 import ru.dark32.chat.Main;
 import ru.dark32.chat.Util;
 import ru.dark32.chat.ichanels.IPersonalMessagesChanel;
 
 /**
  * @author Andrew
  * 
  */
 public class PersonalMessageChanel extends BaseChanel implements IPersonalMessagesChanel {
 
 	final private String	formatToSting;
 	final private String	formatSpyString;
 	final private String	formatFromString;
 	final private int		pmSearchNickMode;
 
 	public PersonalMessageChanel(String name ){
 		super(name);
 		final String path_formatTo = "Chat." + name + ".formatTo";
 		final String path_fotmatFrom = "Chat." + name + ".formatFrom";
 		final String path_formatSpy = "Chat." + name + ".formatSpy";
 		final String path_PMSNM = "Chat." + name + ".PMSearchNickMode";
 		this.formatToSting = ChanelRegister.colorUTF8(Main.chatConfig.getString(path_formatTo, path_formatTo), 3);
 		this.formatFromString = ChanelRegister
 				.colorUTF8(Main.chatConfig.getString(path_fotmatFrom, path_fotmatFrom), 3);
 		this.formatSpyString = ChanelRegister.colorUTF8(Main.chatConfig.getString(path_formatSpy, path_formatSpy), 3);
 		this.pmSearchNickMode = Main.chatConfig.getInt(path_PMSNM, 0);
 	}
 
 	/**
 	 * @return the pmSearchNickMode
 	 */
 	@Override
 	final public int getPmSearchNickMode() {
 		return pmSearchNickMode;
 	}
 
 	@Override
 	final public boolean hasNameTarget(final String raw ) {
 		return raw.length() > 1;
 	}
 
 	@Override
 	final public String getNameTarget(final String raw ) {
 		final int _ind = raw.indexOf(' ');
 		return raw.substring(1, (_ind == -1) ? raw.length() : _ind);
 	}
 
 	@Override
 	final public int hasMessage(final String raw ) {
 		return raw.indexOf(' ');
 	}
 
 	@Override
 	final public String getMessage(final String raw, final int _ind ) {
 		return _ind != -1 ? raw.substring(_ind + 1, raw.length()) : "";
 	}
 
 	@Override
 	final public Player getTargetByName(final String name ) {
 		switch (pmSearchNickMode) {
 			case -1:
 				return Bukkit.getServer().getPlayerExact(name);
 			case 0:
 				return Bukkit.getServer().getPlayer(name);
 			case 1:
 				return Util.getPlayerSoft(name);
 			default:
 				return Bukkit.getServer().getPlayer(name);
 		}
 	}
 
 	@Override
 	final public boolean hasTarget(final Player target ) {
 		return target != null;
 	}
 
 	@Override
 	public void sendMessage(final Player sender, final String raw ) {
 		if (!this.hasNameTarget(raw)) {
 			sender.sendMessage("Имя цели не указанно");
 			return;
 		}
 		final String nameTarget = this.getNameTarget(raw);
 		final Player target = this.getTargetByName(nameTarget);
 		if (!this.hasTarget(target)) {
 			sender.sendMessage("Цель не найдена");
 			return;
 		}
 		final int _ind = this.hasMessage(raw);
 		if (_ind < 0) {
 			sender.sendMessage("Сообщение не введено");
 			return;
 		}
 		final String messge = this.getMessage(raw, _ind); // извлекаем сообщение
 		// отсылаем цели
 		target.sendMessage(formatTo(sender, target, messge));
 		// отсылаем себе
 		responseSendMessage(sender, formatFrom(sender, target, messge));
 		// отсылаем прослушку
 		sendSpyMessage(sender, target, formatSpy(sender, target, messge));
 	}
 
 	@Override
 	final public void responseSendMessage(final Player sender, final String msg ) {
 		sender.sendMessage(msg);
 
 	}
 
 	@Override
 	final public void sendSpyMessage(final Player sender, final Player target, final String msg ) {
 		Bukkit.getConsoleSender().sendMessage(msg);
 
 	}
 
 	@Override
 	public Set<Player> getRecipients(final Player sender ) {
 		final Set<Player> recipients = new HashSet<Player>();
 		final String noSpy = Main.BASE_PERM + "." + this.getInnerName() + ".nospy";
 		for (final Player recipient : Bukkit.getServer().getOnlinePlayers()) {
 			if (!(Main.getPermissionsHandler().hasPermission(recipient, noSpy) || Main.getPermissionsHandler()
 					.hasPermission(sender, noSpy))
 					&& Main.getPermissionsHandler().hasPermission(recipient,
 							Main.BASE_PERM + "." + this.getInnerName() + ".pmspy") && !recipient.equals(sender)) {
 				recipients.add(recipient);
 				sender.sendMessage(recipient.getName());
 			}
 		}
 		return recipients;
 	}
 
 	@Override
 	final public String formatTo(final Player sender, final Player target, final String msg ) {
 		return format(sender, formatToSting).replace("%2$s", msg).replace("%1$s", sender.getName());
 	}
 
 	@Override
 	final public String formatFrom(final Player sender, final Player target, String msg ) {
 		if (msg.contains("$rsuffix")) {
 			msg = msg.replace("$rsuffix", Main.getPermissionsHandler().getSuffix(target));
 		}
 		if (msg.contains("$rprefix")) {
 			msg = msg.replace("$rprefix", Main.getPermissionsHandler().getPrefix(target));
 		}
 		if (msg.contains("$r")) {
 			msg = msg.replace("$r", target.getName());
 		}
 		return format(sender, formatFromString).replace("%2$s", msg).replace("%1$s", sender.getName());
 	}
 
 	@Override
 	final public String formatSpy(final Player sender, final Player target, String msg ) {
 		if (msg.contains("$rsuffix")) {
 			msg = msg.replace("$rsuffix", Main.getPermissionsHandler().getSuffix(target));
 		}
 		if (msg.contains("$rprefix")) {
 			msg = msg.replace("$rprefix", Main.getPermissionsHandler().getPrefix(target));
 		}
 		if (msg.contains("$r")) {
 			msg = msg.replace("$r", target.getName());
 		}
 		return format(sender, formatSpyString).replace("%1$s", sender.getName()).replace("%2$s", msg);
 	}
 
 	@Override
 	public void preSend(final Player sender, final String message, final Set<Player> recipient ) {
 		// отправляем сообщение цели
 		this.sendMessage(sender, message);
 
 	}
 
 	@Override
 	final public Set<Player> getSpyRecipients(Player sender ) {
		return null;
 	}
 }
