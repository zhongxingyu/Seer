 package net.worldoftomorrow.noitem.util;
 
 import net.worldoftomorrow.noitem.Config;
 import net.worldoftomorrow.noitem.NoItem;
 import net.worldoftomorrow.noitem.permissions.Perm;
 import net.worldoftomorrow.noitem.permissions.PermMan;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class Messenger {
 	
 	public enum AlertType {
 		CRAFT(Config.getBoolean("Notify.Craft"), Config.getString("Messages.Craft")),
 		BREW(Config.getBoolean("Notify.Brew"), Config.getString("Messages.Brew")),
 		WEAR(Config.getBoolean("Notify.Wear"), Config.getString("Messages.Wear")),
 		PICK_UP(Config.getBoolean("Notify.Pickup"), Config.getString("Messages.Pickup")),
 		DROP(Config.getBoolean("Notify.Drop"), Config.getString("Messages.Drop")),
 		INTERACT(Config.getBoolean("Notify.Interact"), Config.getString("Messages.Interact")),
 		HOLD(Config.getBoolean("Notify.Hold"), Config.getString("Messages.Hold")),
 		COOK(Config.getBoolean("Notify.Cook"), Config.getString("Messages.Cook")),
 		BREAK(Config.getBoolean("Notify.Break"), Config.getString("Messages.Break")),
 		PLACE(Config.getBoolean("Notify.Place"), Config.getString("Messages.Place")),
 		OPEN(Config.getBoolean("Notify.Open"), Config.getString("Messages.Open")),
 		HAVE(Config.getBoolean("Notify.Have"), Config.getString("Messages.Have")),
 		ENCHANT(Config.getBoolean("Notify.Enchant"), Config.getString("Messages.Enchant")),
 		USE(Config.getBoolean("Notify.Use"), Config.getString("Messages.Use"));
 		
 		public final boolean notify;
 		public final String message;
 		
 		private AlertType(Boolean notify, String msg) {
 			this.notify = notify;
 			this.message = msg;
 		}
 	}
 	
 	public static void sendMessage(Player p, AlertType type, Object o) {
 		p.sendMessage(ChatColor.BLUE + parseMsg(p, type.message, o));
 	}
 	
 	public static void alertAdmins(Player offender, AlertType type, Object o) {
 		if(!Config.getBoolean("Notify.Admins")) return;
 		
 		PermMan perm = NoItem.getPermsManager();
 		String msg = ChatColor.RED + "[NI]" + ChatColor.BLUE + parseAdminMessage(offender, type, o);
 
 		for(Player p : Bukkit.getOnlinePlayers()) {
 			if(perm.has(p, Perm.ADMIN))
 				p.sendMessage(msg);
 		}
 	}
 	
 	private static String parseMsg(Player offender, String msg, Object o) {
 		msg = msg.replace("%n", offender.getName());
 		msg = msg.replace("%w", offender.getWorld().getName());
 		msg = msg.replace("%x", String.valueOf(offender.getLocation().getBlockX()));
 		msg = msg.replace("%y", String.valueOf(offender.getLocation().getBlockY()));
 		msg = msg.replace("%z", String.valueOf(offender.getLocation().getBlockZ()));
 		if(o instanceof ItemStack) {
 			msg = msg.replace("%i", Messenger.getStackName((ItemStack) o));
 		} else if (o instanceof Block) {
 			msg = msg.replace("%i", Messenger.getBlockName((Block) o));
 		} else if (o instanceof Entity) {
 			msg = msg.replace("%i", Messenger.getEntityName((Entity) o));
 		} else if (o instanceof String) {
 			msg = msg.replace("%i", (String) o);
 		} else if (o instanceof Integer) {
 			msg = msg.replace("%i", o.toString());
 		}else {
 			throw new UnsupportedOperationException("Invalid object given to parseMsg(): " + o.getClass().getSimpleName());
 		}
 		msg = ChatColor.translateAlternateColorCodes('&', msg);
 		return msg;
 	}
 	
 	/**
	 * Parse admin message in specific.
 	 * @param offender
 	 * @param type
 	 * @param o
 	 * @return
 	 */
 	private static String parseAdminMessage(Player offender, AlertType type, Object o) {
 		String msg = Config.getString("Messages.Admin");
 		msg = msg.replace("%t", type.name().toLowerCase().replace("_", " "));
 		msg = parseMsg(offender, msg, o);
 		return msg;
 	}
 	
 	public static String getStackName(ItemStack stack) {
 		return stack.getType().name().replace("_", "").toLowerCase();
 	}
 	
 	public static String getBlockName(Block b) {
 		return b.getType().name().replace("_", "").toLowerCase();
 	}
 	
 	public static String getEntityName(Entity e) {
 		return e.getType().name().replace("_", "").toLowerCase();
 	}
 }
