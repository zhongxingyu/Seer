 package org.x3.mail;
 
 import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.x3.mail.permissions.MailPermissions;
 import org.x3.mail.util.Mailbox;
 
 public class SimpleMail extends JavaPlugin {
 
 	private HashMap<String, Mailbox> mail;
 	private MailPermissions perms;
 	private MailHandler mailHandler;
 	private Logger log;
 
 	@Override
 	public void onEnable() {
 		log = this.getLogger();
 		perms = new MailPermissions(this);
 		mailHandler = new MailHandler(new File(getDataFolder(), ".boxes"));
 		loadMailFromFile();
 		getConfig().options().copyDefaults(true);
 		saveConfig();
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
 
 	public MailPermissions getPerms() {
 		return perms;
 	}
 
 	public Boolean isAdmin(String who) {
 		List<String> admins = getConfig().getStringList("admins.list");
 		for (String s : admins) {
 			if (s.equalsIgnoreCase(who)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public Boolean adminsProtected() {
 		return getConfig().getBoolean("admins.protected");
 	}
 
 	public Mailbox getMailbox(String player) {
 		player = player.toLowerCase();
 		return mail.containsKey(player) ? mail.get(player)
 				: new Mailbox(player);
 	}
 
 	public void updateMailbox(Mailbox box) {
 		String owner = box.getOwner();
 		mail.remove(owner);
 		mail.put(owner, box);
 	}
 
 	private HashMap<String, Mailbox> loadMail() {
 		try {
 			return mailHandler.load();
		} catch (FileNotFoundException e) {
			log.warning("Mail file does not exist!");
 			log.info("Don't worry, we'll crack open a fresh one for you.");
 			return new HashMap<String, Mailbox>();
		} catch (ClassNotFoundException | IOException e) {
			log.log(Level.SEVERE, "Could not load mail from file!" + e);
			return new HashMap<String, Mailbox>();
 		}
 	}
 
 	public void loadMailFromFile() {
 		mail = loadMail();
 		saveMail();
 	}
 
 	public void saveMail() {
 		try {
 			mailHandler.save(mail);
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "Couldn't save mail!", e);
 		}
 	}
 
 	public HashMap<String, Mailbox> getMail() {
 		return mail;
 	}
 
 	public void addMailbox(String player, Mailbox mailbox) {
 		player = player.toLowerCase();
 		mail.put(player, mailbox);
 		saveMail();
 	}
 
 	public void addMailbox(String player) {
 		player = player.toLowerCase();
 		mail.put(player, new Mailbox(player));
 		saveMail();
 	}
 
 }
