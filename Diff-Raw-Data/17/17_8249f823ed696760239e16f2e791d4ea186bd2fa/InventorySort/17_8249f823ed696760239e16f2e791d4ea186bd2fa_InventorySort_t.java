 package Wolfwood.InventorySort;
 
 import com.nijiko.Messaging;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import java.io.File;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.block.*;
 
 /**
  * @version 0.1
  * @author Wolfwood
  */
 // this plugin need's the permission's jar and the bukkit snapshot jar
 public class InventorySort extends JavaPlugin {
 
     public static PermissionHandler Permissions = null;
     private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
     public static final Logger log = Logger.getLogger("Minecraft");
 
     public InventorySort(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder,
             File plugin, ClassLoader cLoader) {
         super(pluginLoader, instance, desc, folder, plugin, cLoader);
     }
 
     public void onEnable() {
         setupPermissions();
 
         PluginDescriptionFile pdfFile = this.getDescription();
         log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
     }
 
     public void onDisable() {
         PluginDescriptionFile pdfFile = this.getDescription();
         log.info(pdfFile.getName() + " v " + pdfFile.getVersion() + " is disabled!");
     }
 
     public boolean isDebugging(final Player player) {
         if (debugees.containsKey(player)) {
             return debugees.get(player) != null;
         } else {
             return false;
         }
     }
 
     public void setDebugging(final Player player, final boolean value) {
         debugees.put(player, value);
     }
 
     public void setupPermissions() {
         Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
 
 
         if (InventorySort.Permissions == null) {
             if (test != null) {
                 InventorySort.Permissions = ((Permissions) test).getHandler();
             } else {
                 log.info(Messaging.bracketize("Inventory Sort") + " Permission system not enabled. Disabling plugin.");
                 this.getServer().getPluginManager().disablePlugin(this);
             }
         }
     }
 
     // basically if it's the console sending the command it denies it.
     private boolean anonymousCheck(CommandSender sender) {
         if (!(sender instanceof Player)) {
             sender.sendMessage("Cannot execute that command, I don't know who you are!");
             return true;
         } else {
             return false;
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
         String[] trimmedArgs = args;
         String commandName = command.getName().toLowerCase();
 
         if (commandName.equals("sort")) {
             if (anonymousCheck(sender)) {
                 return false;
             }
             return sortPlyInv(sender, trimmedArgs);
         } else if (commandName.equals("sortchest")) {
             if (anonymousCheck(sender)) {
                 return false;
             }
             return sortChestInv(sender, trimmedArgs);
         }
         return false;
     }
 
     private boolean sortPlyInv(CommandSender sender, String[] split) {
         Player player = (Player) sender;
         int end = 36;
         int start = 0;
         if (split.length == 0) {
             return dispSortHelp(sender);
         } else if (split.length == 1) {
             String what = split[0];
             if (what.equalsIgnoreCase("all")) {
                 if (!InventorySort.Permissions.has(player, "iSort.basic.all")) {
                     sender.sendMessage(ChatColor.RED + "You do not have permission to run " + ChatColor.GREEN + "/sort all");
                     return true;
                 }
                 end = 36;
                 start = 0;
             } else if (what.equalsIgnoreCase("top")) {
                 if (!InventorySort.Permissions.has(player, "iSort.basic.top")) {
                     sender.sendMessage(ChatColor.RED + "You do not have permission to run " + ChatColor.GREEN + "/sort top");
                     return true;
                 }
                 end = 36;
                 start = 9;
             } else {
                 return dispSortHelp(sender);
             }
         } else if (split.length == 2 && InventorySort.Permissions.has(player, "iSort.basic.range")) {
             int j = 35;
             int k = 0;
             try {
                 j = Integer.valueOf(split[0]);
                 k = Integer.valueOf(split[1]);
             } catch (NumberFormatException e) {
                 sender.sendMessage("Those were not numbers!");
                 return dispSortHelp(sender);
             }
             if (j <= 35 && j >= 0 && k <= 35 && k >= 0) {
                 if (k > j) {
                     end = k + 1;
                     start = j;
                 } else if (j > k) {
                     end = j + 1;
                     start = k;
                 } else if (j == k) {
                     sender.sendMessage("You just want to slot " + j + " sorted? Ok, done.");
                     return true;
                 } else {
                     return dispSortHelp(sender); //should never get here
                 }
             } else {
                 sender.sendMessage(ChatColor.YELLOW + "Out of range error.");
                 return dispSortHelp(sender);
             }
         } else {
             sender.sendMessage(ChatColor.RED + "You do not have permission to run " + ChatColor.GREEN + "/sort <0-35> <0-35>");
             return true;
         }
        ItemStack[] x = player.getInventory().getContents();
        x = sortItemStack(x, start, end);
 
        player.getInventory().setContents(x);
         sender.sendMessage(ChatColor.GRAY + "Slots " + start + "-" + (end - 1) + " have been sorted!");
         return true;
     }
 
     private boolean sortChestInv(CommandSender sender, String[] split) {
         Player player = (Player) sender;
         if (!InventorySort.Permissions.has(player, "iSort.basic.chest")) {
             sender.sendMessage(ChatColor.RED + "You do not have permission to run " + ChatColor.GREEN + "/sortchest");
             return true;
         }
         TargetBlock hitBlox = new TargetBlock(player);
         Block target = hitBlox.getTargetBlock();
         int intX = target.getX();
         int intY = target.getY();
         int intZ = target.getZ();
         Block block = player.getWorld().getBlockAt(intX, intY, intZ);
         BlockState state = block.getState();
         if (state instanceof Chest) {
             Chest chest = (Chest) state;
             ItemStack[] x = chest.getInventory().getContents();
             x = sortItemStack(x);
             chest.getInventory().setContents(x);
             chest.update();
             sender.sendMessage(ChatColor.GRAY + "The chest has been sorted.");
         } else {
             sender.sendMessage("You are not looking at a Chest");
         }
         return true;
     }
 
     private ItemStack[] sortItemStack(ItemStack[] x) {
         return sortItemStack(x, 0, x.length);
     }
 
     private ItemStack[] sortItemStack(ItemStack[] x, int start, int end) {
        x = stackItems(x, start, end);
         boolean doMore = true;
        int n = end;
         while (doMore) {
            n--;
             doMore = false;  // assume this is our last pass over the array
            for (int i = start; i < n; i++) {
                 if (x[i].getTypeId() > x[i + 1].getTypeId()) {
                     // exchange elements
                     ItemStack temp = x[i];
                     x[i] = x[i + 1];
                     x[i + 1] = temp;
                     doMore = true;  // after an exchange, must look again
                 }
             }
         }
         return x;
     }
 
     private boolean dispSortHelp(CommandSender sender) {
         Player player = (Player) sender;
         if (InventorySort.Permissions.has(player, "iSort.basic.all")) {
             sender.sendMessage("Example: " + ChatColor.GREEN + "/sort all " + ChatColor.WHITE + "- sorts all slots");
         } else {
             sender.sendMessage(ChatColor.RED + "You do not have permission to run " + ChatColor.GREEN + "/sort all");
         }
         if (InventorySort.Permissions.has(player, "iSort.basic.top")) {
             sender.sendMessage("Example: " + ChatColor.GREEN + "/sort top " + ChatColor.WHITE + "- sorts slots 9 - 35");
         } else {
             sender.sendMessage(ChatColor.RED + "You do not have permission to run " + ChatColor.GREEN + "/sort top");
         }
         if (InventorySort.Permissions.has(player, "iSort.basic.range")) {
             sender.sendMessage("Example: " + ChatColor.GREEN + "/sort 9 35 " + ChatColor.WHITE + "- sorts slots 9 - 35");
             sender.sendMessage("Example: " + ChatColor.GREEN + "/sort 35 9 " + ChatColor.WHITE + "- sorts slots 9 - 35");
             sender.sendMessage("Example: " + ChatColor.GREEN + "/sort 10 15 " + ChatColor.WHITE + "- sorts slots 10 - 15");
         } else {
             sender.sendMessage(ChatColor.RED + "You do not have permission to run " + ChatColor.GREEN + "/sort <0-35> <0-35>");
         }
         return true;
     }
 
     private ItemStack[] stackItems(ItemStack[] items, int start, int end) {
         for (int i = start; i < end; i++) {
             ItemStack item = items[i];
 
             // Avoid infinite stacks and stacks with durability
             if (item == null || item.getAmount() <= 0
                     || ItemType.shouldNotStack(item.getTypeId())) {
                 continue;
             }
 
             // Ignore buckets
             if (item.getTypeId() >= 325 && item.getTypeId() <= 327) {
                 continue;
             }
 
             if (item.getAmount() < 64) {
                 int needed = 64 - item.getAmount(); // Number of needed items until 64
 
                 // Find another stack of the same type
                 for (int j = i + 1; j < end; j++) {
                     ItemStack item2 = items[j];
 
                     // Avoid infinite stacks and stacks with durability
                     if (item2 == null || item2.getAmount() <= 0
                             || ItemType.shouldNotStack(item.getTypeId())) {
                         continue;
                     }
 
                     // Same type?
                     // Blocks store their color in the damage value
                     if (item2.getTypeId() == item.getTypeId()
                             && (!ItemType.usesDamageValue(item.getTypeId())
                             || item.getDurability() == item2.getDurability())) {
                         // This stack won't fit in the parent stack
                         if (item2.getAmount() > needed) {
                             item.setAmount(64);
                             item2.setAmount(item2.getAmount() - needed);
                             break;
                             // This stack will
                         } else {
                             items[j] = null;
                             item.setAmount(item.getAmount() + item2.getAmount());
                             needed = 64 - item.getAmount();
                         }
 
                     }
                 }
             }
         }
 
         return items;
     }
 }
