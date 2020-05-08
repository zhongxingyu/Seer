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
        String[] split = args[0].split(":");
        if (split[1].equalsIgnoreCase("")) {
             split[1] = "0";
         }
         ItemStack stack = new ItemStack(Integer.valueOf(split[0]), Integer.valueOf(args[1]), Short.valueOf(split[1]));
         Player player = (Player) sender;
         player.getInventory().setItem(player.getInventory().firstEmpty(), stack);
        player.sendMessage(ChatColor.GREEN + "Enjoy your: " + stack.getType().name());
        throw new UnsupportedOperationException("Not supported yet.");
     }
 }
