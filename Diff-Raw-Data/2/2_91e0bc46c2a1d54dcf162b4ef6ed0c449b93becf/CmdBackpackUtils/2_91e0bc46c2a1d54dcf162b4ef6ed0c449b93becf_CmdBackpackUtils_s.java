 package me.darqy.backpacks.command;
 
 import java.util.HashMap;
 import java.util.Map;
 import me.darqy.backpacks.Backpack;
 import me.darqy.backpacks.BackpackManager;
 import me.darqy.backpacks.Backpacks;
 import me.darqy.backpacks.util.InventoryUtil;
 import me.darqy.backpacks.util.NMSUtil;
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class CmdBackpackUtils implements CommandExecutor  {
     
     private Backpacks plugin;
     private MagnetListener magnet = this.new MagnetListener();
     
     private static final String[] TOOLS = new String[] {
         "magnet", "chest", "rename", "empty",
     };
     
     private static final String MAGNET_USAGE = "magnet";
     private static final String CHEST_USAGE = "chest [put|take] [item|id|all]";
     private static final String RENAME_USAGE = "rename [new-pack]";
     private static final String EMPTY_USAGE = "empty";
     
     
     
     public CmdBackpackUtils(Backpacks instance) {
         this.plugin = instance;
         
         plugin.getServer().getPluginManager().registerEvents(magnet, plugin);
     }
 
     @Override
     public boolean onCommand(CommandSender s, Command c, String l, String[] args) {
         if (!(s instanceof Player)) {
             s.sendMessage(ChatColor.RED + "This command is only available to players");
             return true;
         }
         
         final Player p = (Player) s;
         final String player = p.getName();
                         
         if (args.length < 1) {
             handleHelp(c, p, null, l);
             return true;
         }
         
         if ("help".equalsIgnoreCase(args[0])) {
             handleHelp(c, p, args.length >= 2? args[1].toLowerCase(): null, l);
             return true;
         }
 
         final String action = getTool(args[0]);
         if (action == null) {
             handleHelp(c, p, args[0], l);
             return true;
         }
         
         if (!p.hasPermission("backpack.util." + action)) {
             s.sendMessage(ChatColor.RED + "You don't have permission.");
             return true;
         }
         
         BackpackManager manager = plugin.getManager(p.getWorld());
         if (manager == null) {
             s.sendMessage(ChatColor.RED + "Sorry, can't do that in this world.");
             return true;
         }
 
         String backpack = "default";
         String lArg = args[args.length - 1];
         if (lArg.startsWith("p:") || lArg.startsWith("P:")) {
             String[] parts = lArg.split(":");
            if (parts.length == 2) backpack = parts[0];
         }
 
         Backpack pack = manager.getBackpack(player, backpack);
         if (pack == null) {
             s.sendMessage(ChatColor.RED + "You don't have that backpack.");
             return true;
         }
         
         if ("magnet".equals(action)) {
             handleMagnet(p, pack, backpack);
         } else if ("chest".equals(action)) {
             if (args.length < 3) {
                 p.sendMessage(ChatColor.RED + "Not enough arguments.");
                 p.sendMessage(getUsage(c, ChatColor.YELLOW, l, CHEST_USAGE));
                 return true;
             }
             handleChestTransfer(p, pack, args[1], args[2]);
         } else if ("rename".equals(action)) {
             if (args.length < 3) {
                 p.sendMessage(ChatColor.RED + "Not enough arguments.");
                 p.sendMessage(getUsage(c, ChatColor.YELLOW, l, RENAME_USAGE)
                         .replace("(p:[backpack])", "[p:old-pack]"));
                 return true;
             }
             handleRename(p, manager, backpack, args[1].toLowerCase());
         } else if ("empty".equals(action)) {
             if (args.length < 2) {
                 p.sendMessage(ChatColor.RED + "Not enough arguments.");
                 p.sendMessage(getUsage(c, ChatColor.YELLOW, l, EMPTY_USAGE)
                         .replace("(p:[backpack])", "[p:old-pack]"));
                 return true;
             }
             handleEmpty(p, pack, backpack);
         }
 
         return true;
     }
     
     private void handleMagnet(Player p, Backpack pack, String backpack) {
         if (!magnet.magnetEnabled(p.getName())) {
             magnet.enableMagnet(p.getName(), pack);
             p.sendMessage(ChatColor.YELLOW + "Enabled magnet mode on your "
                     + "\"" + backpack + "\" backpack.");
             p.sendMessage(ChatColor.YELLOW + "Do this command again to disable it.");
         } else {
             magnet.disableMagnet(p.getName());
             p.sendMessage(ChatColor.YELLOW + "Magnet mode disabled.");
         }
     }
     
     private void handleChestTransfer(Player p, Backpack pack, String action, String item) {
         Block target = p.getTargetBlock(null, 5);
         if (!(target.getState() instanceof Chest)) {
             p.sendMessage(ChatColor.RED + "You must be looking at a chest to do that");
             return;
         }
         
         if (!plugin.checkProtection(p, target) && !p.hasPermission("backpack.util.chest.bypass")) {
             p.sendMessage(ChatColor.RED + "Sorry, you do not have access to that chest.");
             return;
         }
         
         Chest chest = (Chest) target.getState();
 
         Inventory from, to;
         if (action.equalsIgnoreCase("put")) {
             to = chest.getInventory();
             from = pack.getInventory();
         } else if (action.equalsIgnoreCase("take")) {
             to = pack.getInventory();
             from = chest.getInventory();
         } else {
             p.sendMessage(ChatColor.RED + "Error: " + action + ". Use \"put\" or \"take\"");
             return;
         }
 
         boolean success = InventoryUtil.transferItems(from, to, item);
         if (success) {
             p.sendMessage(ChatColor.YELLOW + "Items transfered!");
         } else {
             p.sendMessage(ChatColor.RED + "Transfer failed. Invalid item?");
         }
     }
     
     private void handleRename(Player p, BackpackManager mngr, String oldname, String newname) {
         if (mngr.hasBackpack(p.getName(), newname)) {
             p.sendMessage(ChatColor.RED + "The backpack you're trying to rename this to already exists.");
             return;
         }
         
         mngr.renameBackpack(p.getName(), oldname, newname);
         p.sendMessage(ChatColor.YELLOW + "Your \"" + oldname + "\" backpack is renamed to: \"" + newname + "\"");
     }
     
     private void handleEmpty(Player p, Backpack pack, String backpack) {
         pack.getInventory().clear();
         p.sendMessage(ChatColor.YELLOW + "Your \"" + backpack + "\" backpack is now empty!");
     }
     
     public void handleHelp(Command c, Player p, String action, String l) {
         if (action == null) {
             sendUtils(p, l);
         } else if ("magnet".equals(action)) {
             sendHelpText(p, "Usage: " + getUsage(c, ChatColor.RED, l, MAGNET_USAGE));
             sendHelpText(p, "Toggling magnet mode on a backpack allows it to"
                     + " collect items directly as you pick them up from the ground.");
             sendHelpText(p, ChatColor.GRAY + "---");
             sendHelpText(p, "- In order for an item to be collected by the backpack,"
                     + " your player inventory must also have space for the item");
             sendHelpText(p, "- When your backpack is full, you will be notified and"
                     + " magnet mode will be automatically disabled.");
             sendHelpText(p, ChatColor.GRAY + "---");
             sendHelpText(p, "Example usage:");
             sendHelpText(p, " - /" + l +" magnet - Enables magnet on your default backpack");
             sendHelpText(p, " - /" + l +" magnet p:collecter - Enables magnet on your \"collector\""
                     + " backpack");
             sendHelpText(p, " - /" + l +" magnet (after enabled) - disable magnet mode");
         } else if ("chest".equals(action)) {
             sendHelpText(p, "Usage: " + getUsage(c, ChatColor.RED, l, CHEST_USAGE));
             sendHelpText(p, " Transfers items from your pack into a chest, and"
                     + " vica versa");
             sendHelpText(p, ChatColor.GRAY + "---");
             sendHelpText(p, "- You must be looking at the chest you wish to transfer"
                     + " items with.");
             sendHelpText(p, ChatColor.GRAY + "---");
             sendHelpText(p, "Example usage:");
             sendHelpText(p, " - /" + l +" chest put stone - Transfers as much stone as possible"
                     + " from your \"default\" backpack into the chest");
             sendHelpText(p, " - /" + l +" chest take all p:random - Transfers as manys items as possible"
                     + " can from the chest to your \"random\" backpack");
         } else if ("rename".equals(action)) {
             sendHelpText(p, "Usage: " + getUsage(c, ChatColor.RED, l, RENAME_USAGE)
                     .replace("(p:[backpack])", "[p:old-pack]"));
             sendHelpText(p, " Rename a backpack");
             sendHelpText(p, ChatColor.GRAY + "---");
             sendHelpText(p, "Examples:");
             sendHelpText(p, " - /" + l +" rename diamonds p:mining - Rename your \"mining\" backpack to \"diamonds\"");
         } else if ("empty".equals(action)) {
             sendHelpText(p, "Usage: " + getUsage(c, ChatColor.RED, l, EMPTY_USAGE)
                     .replace("(p:[backpack])", "[p:old-pack]"));
             sendHelpText(p, " Empties a backpack");
             sendHelpText(p, ChatColor.GRAY + "---");
             sendHelpText(p, ChatColor.GOLD + "- WARNING: this operation cannot be undone!!"
                     + " You will lose the items in the pack forever");
             sendHelpText(p, ChatColor.GRAY + "---");
             sendHelpText(p, "Example usage:");
             sendHelpText(p, " - /" + l +" empty p:default - Empties your \"default\" backpack");
         } else {
             sendUtils(p, l);
         }
     }
     
     private static void sendHelpText(Player p, String message) {
         p.sendMessage(ChatColor.YELLOW + message);
     }
     
     private static String getTool(String filter) {
         for (String tool : TOOLS) {
             if (tool.equalsIgnoreCase(filter)) {
                 return tool;
             }
         }
         return null;
     }
     
     private static String getUsage(Command c, ChatColor color, String label, String usage) {
         return color + c.getUsage()
                 .replace("[action]", usage)
                 .replace("<command>", label);
     }
     
     private static void sendUtils(CommandSender sender, String l) {
             sender.sendMessage(ChatColor.YELLOW + "Unknown utility. Available utils: ");
             for (String tool : TOOLS) {
                 sender.sendMessage("- " + ChatColor.AQUA + tool);
             }
             sender.sendMessage(ChatColor.YELLOW + "Do " + ChatColor.RED
                     + "/" + l + " help [util] " + ChatColor.YELLOW + "for information and usage");
     }
             
     private class MagnetListener implements Listener {
         
         private Map<String, Backpack> magnets = new HashMap();
         
         private static final String backpackFull = 
                 "Your backpack is full! Disabled magnet mode.";
         
         public void enableMagnet(String player, Backpack pack) {
             magnets.put(player, pack);
         }
         
         public void disableMagnet(String player) {
             magnets.remove(player);
         }
         
         public boolean magnetEnabled(String player) {
             return magnets.containsKey(player);
         }
         
         @EventHandler(ignoreCancelled = true)
         public void randomPop(PlayerPickupItemEvent event) {
            final Player p = event.getPlayer();
             final Item item = event.getItem();
             final ItemStack itemstack = item.getItemStack();
             final String player = p.getName();
             
             if (magnetEnabled(player)) {
                 final Backpack pack = magnets.get(player);
                 int left = pack.pickup(itemstack);
                 
                 if (left > 0) {
                     p.sendMessage(ChatColor.RED + backpackFull);
                     disableMagnet(player);
                     
                     itemstack.setAmount(left);
                     item.setItemStack(itemstack);
                 } else {
                     NMSUtil.simulateItemPickup(p, item);
                     item.remove();
                 }
                 
                 event.setCancelled(true);
             }
         }
         
     }
     
 }
