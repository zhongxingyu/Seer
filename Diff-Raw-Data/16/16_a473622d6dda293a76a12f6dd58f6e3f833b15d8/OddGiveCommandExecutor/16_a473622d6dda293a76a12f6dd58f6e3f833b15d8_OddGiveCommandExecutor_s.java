 package info.somethingodd.bukkit.OddGive;
 
 import info.somethingodd.bukkit.OddItem.OddItem;
 import info.somethingodd.bukkit.OddItem.OddItemGroup;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class OddGiveCommandExecutor implements CommandExecutor {
 
     private OddGive oddGive = null;
 
     public OddGiveCommandExecutor(OddGive oddGive) {
         this.oddGive = oddGive;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (!(sender instanceof Player) && label.equals("i")) {
             sender.sendMessage(oddGive.logPrefix + "Try /give");
             return true;
         }
         if (sender instanceof Player) {
            oddGive.calculate((Player) sender);
             if (!sender.isOp() && ((!command.getName().equals("oddgive") && !sender.hasPermission("oddgive." + label)) || (command.getName().equals("oddgive") && !sender.hasPermission("oddgive." + args[0])))) {
                 sender.sendMessage(oddGive.logPrefix + "You are not worthy.");
                 return true;
             }
         }
         if (label.equals("give")) {
             return give(sender, args);
         } else if (label.equals("i")) {
             return i(sender, args);
         } else if (label.equals("i0")) {
             return i0(sender, args);
         }
         if (args.length == 1) {
             if (args[0].equals("list")) {
                 sender.sendMessage(oddGive.logPrefix + "You are " + (oddGive.blacklist ? "dis" : "") + "allowed these items: " + oddGive.lists.get(sender).toString());
                 return true;
             }
         }
         return false;
     }
 
     private boolean give(CommandSender sender, String[] args) {
         int i = 0;
         Set<Player> players = new HashSet<Player>();
         Set<ItemStack> items = new HashSet<ItemStack>();
         for (; i  < args.length; i++) {
             Player player = oddGive.getServer().getPlayer(args[i]);
             if (player == null) break;
             players.add(player);
         }
         for (; i < args.length; i++) {
             try {
                 ItemStack itemStack = OddItem.getItemStack(args[i]);
                 boolean deny = false;
                 for (ItemStack listItem : oddGive.lists.get(sender)) {
                     if (OddItem.compare(listItem, itemStack)) deny = true;
                 }
                 if (deny) {
                     sender.sendMessage(oddGive.logPrefix + "Not allowed: " + args[i]);
                     continue;
                 }
                 int amount = oddGive.defaultQuantity;
                 try {
                     amount = Integer.parseInt(args[i+1]);
                     i++;
                 } catch (Exception e) {
                 }
                 itemStack.setAmount(amount);
                 items.add(itemStack);
             } catch (IllegalArgumentException e) {
                 if (oddGive.kits.get(args[i]) == null) {
                     sender.sendMessage(oddGive.logPrefix + "Unknown item \"" + args[i] + "\" - did you mean \"" + e.getMessage() + "\"?");
                 } else {
                     OddItemGroup kit = oddGive.kits.get(args[i]);
                     for (ItemStack itemStack : kit) {
                         boolean deny = false;
                         for (ItemStack listItem : oddGive.lists.get(sender)) {
                             if (OddItem.compare(listItem, itemStack)) deny = true;
                         }
                         if (deny) {
                             sender.sendMessage(oddGive.logPrefix + "Not allowed: " + args[i]);
                             continue;
                         } else {
                             items.add(itemStack);
                         }
                     }
                 }
             }
         }
         for (ItemStack itemStack : items) {
             for (Player player : players) {
                 boolean deny = false;
                 for (ItemStack listItem : oddGive.lists.get(player)) {
                     if (!sender.hasPermission("oddgive.give.bypass") && OddItem.compare(listItem, itemStack)) {
                         deny = true;
                     }
                 }
                 if (deny) {
                     sender.sendMessage(oddGive.logPrefix + "Not allowed: " + OddItem.getAliases(itemStack.getTypeId() + ";" + itemStack.getDurability()).get(0));
                     continue;
                 }
                 player.getInventory().addItem(itemStack);
             }
         }
         return true;
     }
 
     private boolean i(CommandSender sender, String[] args) {
         HashSet<ItemStack> items = new HashSet<ItemStack>();
         for (int i = 0; i < args.length; i++) {
             try {
                 ItemStack itemStack = OddItem.getItemStack(args[i]);
                 boolean deny = false;
                 for (ItemStack listItem : oddGive.lists.get(sender)) {
                     if (OddItem.compare(listItem, itemStack)) {
                         deny = true;
                         break;
                     }
                 }
                 if (deny) {
                     sender.sendMessage(oddGive.logPrefix + "Not allowed: " + args[i]);
                     continue;
                 }
                 int amount = oddGive.defaultQuantity;
                 try {
                     amount = Integer.parseInt(args[i+1]);
                     i++;
                 } catch (Exception e) {
                 }
                 itemStack.setAmount(amount);
                 items.add(itemStack);
             } catch (IllegalArgumentException e) {
                 if (oddGive.kits.get(args[i]) == null) {
                     sender.sendMessage(oddGive.logPrefix + "Unknown item \"" + args[i] + "\" - did you mean \"" + e.getMessage() + "\"?");
                 } else {
                     OddItemGroup kit = oddGive.kits.get(args[i]);
                     for (ItemStack itemStack : kit) {
                         boolean deny = false;
                         for (ItemStack listItem : oddGive.lists.get(sender)) {
                             if (OddItem.compare(listItem, itemStack)) deny = true;
                         }
                         if (deny) {
                             sender.sendMessage(oddGive.logPrefix + "Not allowed: " + args[i]);
                             continue;
                         } else {
                             items.add(itemStack);
                         }
                     }
                 }
             }
         }
         for (ItemStack itemStack : items) {
             ((Player) sender).getInventory().addItem(itemStack);
         }
         return true;
     }
 
     private boolean i0(CommandSender sender, String[] args) {
         int i = 0;
         Set<Player> players = new HashSet<Player>();
         Set<ItemStack> items = new HashSet<ItemStack>();
         for (; i < args.length; i++) {
             Player player = oddGive.getServer().getPlayer(args[i]);
             if (player == null) break;
             players.add(player);
         }
         if (!players.isEmpty() && !sender.hasPermission("oddgive.i0.other")) {
             sender.sendMessage(oddGive.logPrefix + "Not allowed.");
             return true;
         }
         if (players.isEmpty()) players.add((Player) sender);
         for (; i < args.length; i++) {
             try {
                 ItemStack item = OddItem.getItemStack(args[i]);
                 int amount = -1; //2304;
                 try {
                     amount = Integer.parseInt(args[i + 1]);
                     i++;
                 } catch (Exception e) {
                 }
                 item.setAmount(amount);
                 items.add(item);
             } catch (IllegalArgumentException e) {
                 sender.sendMessage(oddGive.logPrefix + "Unknown item \"" + args[i] + "\" - did you mean \"" + e.getMessage() + "\"?");
             }
         }
         for (Player player : players) {
             if (items.isEmpty()) {
                 player.getInventory().clear();
             } else {
                 for (ItemStack itemStack : items) {
                     ItemStack[] inventory = player.getInventory().getContents();
                     int amount = itemStack.getAmount();
                     for (int j = 0; j < inventory.length; j++) {
                         if (inventory[j] == null) continue;
                         if (OddItem.compare(inventory[j], itemStack)) {
                             if (amount == -1) {
                                 player.getInventory().remove(inventory[j]);
                             } else {
                                 if (inventory[j].getAmount() > amount) {
                                     inventory[j].setAmount(inventory[j].getAmount() - amount);
                                     break;
                                 } else {
                                     amount -= inventory[j].getAmount();
                                     player.getInventory().remove(inventory[j]);
                                     if (amount == 0) break;
                                 }
                             }
                         }
                     }
                 }
             }
         }
         return true;
     }
 }
