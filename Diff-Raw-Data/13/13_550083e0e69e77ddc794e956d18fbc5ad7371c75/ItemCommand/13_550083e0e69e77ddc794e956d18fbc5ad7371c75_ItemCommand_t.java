 package moatycraft.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class ItemCommand implements CommandExecutor {
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (!(sender instanceof Player)) {
             return false;
         }
         if (!sender.isOp()) {
             return false;
         }
         if (args.length > 2) {
             return false;
         }
        String[] split = null;
        if (args[0].endsWith(":")) {
            split[0] = args[0].substring(0, args[0].length() - 1);
             split[1] = "0";
        } else {
            split = args[0].split(":");
         }
         ItemStack stack = new ItemStack(Integer.valueOf(split[0]), Integer.valueOf(args[1]), Short.valueOf(split[1]));
         Player player = (Player) sender;
         player.getInventory().setItem(player.getInventory().firstEmpty(), stack);
        player.sendMessage(ChatColor.RED + "Enjoy your: " + stack.getType().name());
        return true;
     }
 }
