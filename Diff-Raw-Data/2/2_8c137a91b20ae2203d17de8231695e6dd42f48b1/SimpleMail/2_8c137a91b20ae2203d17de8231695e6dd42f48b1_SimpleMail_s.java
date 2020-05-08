 package org.x3.mail;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.x3.mail.util.Message;
 
 public class SimpleMail extends JavaPlugin {
 
 	HashMap<String, ArrayList<Message>> mail;
 	MailHandler mailHandler;
 	Logger log;
 
 	@Override
 	public void onEnable() {
 		log = this.getLogger();
 		mailHandler = new MailHandler(new File(getDataFolder(),
 				"simplemail.map"));
 		mail = new HashMap<String, ArrayList<Message>>();
 		this.getCommand("mail").setExecutor(new SMExecutor(this));
 		log.info("SimpleMail enabled.");
 	}
 
 	/**
 	 * Returns the result of JavaPlugin.getServer().getPlayer(name)
 	 * 
 	 * @param name
 	 *            Name of the player
 	 * @return The associated player, or null if not found.
 	 */
 	public Player getPlayer(String name) {
 		return getServer().getPlayer(name);
 	}
 
 	/**
 	 * Returns whether the specified player has unread mail.
 	 * 
 	 * @param player
 	 *            The player's name
 	 * @return Whether the player has unread mail.
 	 */
 	public boolean hasMail(String player) {
 		return mail.containsKey(player.toLowerCase());
 	}
 
 	/**
 	 * Returns the player's mail.
 	 * 
 	 * @param player
 	 *            Name of the player
 	 * @return Unread messages, or null if the player has no new mail.
 	 */
 	public ArrayList<Message> getMail(String player) {
 		return (mail.containsKey(player.toLowerCase())) ? mail.get(player
 				.toLowerCase()) : new ArrayList<Message>();
 	}
 
 	/**
 	 * Deletes all of the player's mail.
 	 * 
 	 * @param player
 	 *            Name of the player
 	 */
 	public void removeMail(String player) {
 		mail.remove(player.toLowerCase());
 	}
 
 	/**
 	 * Determines if the player is online.
 	 * 
 	 * @param player
 	 *            Name of the player
 	 * @return True if online, else false.
 	 */
 	public boolean isOnline(String player) {
 		for (Player p : getServer().getOnlinePlayers()) {
 			if (p.getName().equalsIgnoreCase(player)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Send a message. The target player is specified within the message.
 	 * 
 	 * @param message
 	 *            The message to send
 	 */
 	public void send(Message message) {
 		String recipient = message.getRecipient();
 		ArrayList<Message> playerMail = getMail(recipient);
 		if (hasMail(recipient)) {
 			mail.remove(recipient);
 		}
 		playerMail.add(message);
 		mail.put(recipient, playerMail);
 		if (recipient.equalsIgnoreCase("console")) {
			log.info(ChatColor.GREEN + "New mail for Console");
 		} else if (isOnline(recipient)) {
 			notifyPlayer(getServer().getPlayer(recipient));
 		}
 	}
 
 	/**
 	 * Notify the player that they have new messages.
 	 * 
 	 * @param player
 	 *            Player to notify
 	 */
 	public void notifyPlayer(Player player) {
 		player.sendMessage(ChatColor.GREEN
 				+ String.format("You have %s new message(s).",
 						getMail(player.getName()).size()));
 	}
 
 }
