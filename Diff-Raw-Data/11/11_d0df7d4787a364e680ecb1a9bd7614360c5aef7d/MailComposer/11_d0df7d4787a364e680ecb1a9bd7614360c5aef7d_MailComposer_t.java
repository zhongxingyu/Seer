 package couk.MineCode.Composition;
 
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import couk.MineCode.Database.MailDatabase;
 import couk.MineCode.Database.MailItem;
 import couk.MineCode.Database.MailListStructure;
 import couk.MineCode.MineMail.MineMail;
 
 public class MailComposer implements Listener {
 
 	private static HashMap<String, MailItem> compositions = new HashMap<String, MailItem>();
 	public static final int lineLimit = 15;
 
 	public static boolean isComposing(String player) {
 		return compositions.containsKey(player);
 	}
 
 	public static void createComposition(String player, MailItem mi) {
 		compositions.put(player, mi);
 	}
 
 	public static void endComposition(String player) {
 		compositions.remove(player);
 	}
 
 	public static void sendComposition(String player) {
 		MailDatabase.sendMail(compositions.get(player));
 	}
 
	public MailComposer() {
		Bukkit.getServer().getPluginManager().registerEvents(this, MineMail.MineMail);
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onMailCompose(final PlayerChatEvent evt) {
 		Player p = evt.getPlayer();
 		if (compositions.containsKey(p.getName())) {
 			if (compositions.get(p.getName()).getContents().size() >= lineLimit) {
 				p.sendMessage(ChatColor.RED + "[MineMail] Line limit reached (15)!" + ChatColor.GREEN + " /m send");
 			} else {
 				compositions.get(p.getName()).addToContents(evt.getMessage());
 				p.sendMessage(ChatColor.GREEN + "[Added] --> " + ChatColor.GRAY + evt.getMessage());
 				evt.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onMailPaper(final PlayerInteractEvent evt) {
 		Player p = evt.getPlayer();
 		Action a = evt.getAction();
 
 		if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
 			if (p.getItemInHand().getType().equals(Material.PAPER)) {
 				if (!MailDatabase.doesPlayerHaveMailDB(p.getName())) {
 					p.sendMessage(ChatColor.RED + "[MineMail] You have no mail to read!");
 				} else {
 					MailListStructure mls = MailDatabase.loadMail(p.getName());
 					if (mls.getArraySize() < 1) {
 						p.sendMessage(ChatColor.RED + "[MineMail] You have no mail to read!");
 					} else {
 						mls.sendHeaders(p, 1);
 					}
 				}
 			}
 		}
 	}
 
 }
