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
		CRAFT(Config.getBoolean("Notify.NoCraft"), Config.getString("Messages.NoCraft")),
		BREW(Config.getBoolean("Notify.NoBrew"), Config.getString("Messages.NoBrew")),
		WEAR(Config.getBoolean("Notify.NoWear"), Config.getString("Messages.NoWear")),
		PICK_UP(Config.getBoolean("Notify.NoPickup"), Config.getString("Messages.NoPickup")),
		DROP(Config.getBoolean("Notify.NoDrop"), Config.getString("Messages.NoDrop")),
		INTERACT(Config.getBoolean("Notify.NoInteract"), Config.getString("Messages.NoInteract")),
		HOLD(Config.getBoolean("Notify.NoHold"), Config.getString("Messages.NoHold")),
		COOK(Config.getBoolean("Notify.NoCook"), Config.getString("Messages.NoCook")),
		BREAK(Config.getBoolean("Notify.NoBreak"), Config.getString("Messages.NoBreak")),
		PLACE(Config.getBoolean("Notify.NoPlace"), Config.getString("Messages.NoPlace")),
		OPEN(Config.getBoolean("Notify.NoOpen"), Config.getString("Messages.NoOpen")),
		HAVE(Config.getBoolean("Notify.NoHave"), Config.getString("Messages.NoHave")),
		ENCHANT(Config.getBoolean("NoEnchant"), Config.getString("Messages.NoEnchant"));
 		
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
 		msg = msg.replace("%n", offender.getDisplayName());
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
 		} else {
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
		return e.getType().getName().toLowerCase().replace("_", "");
 	}
 }
