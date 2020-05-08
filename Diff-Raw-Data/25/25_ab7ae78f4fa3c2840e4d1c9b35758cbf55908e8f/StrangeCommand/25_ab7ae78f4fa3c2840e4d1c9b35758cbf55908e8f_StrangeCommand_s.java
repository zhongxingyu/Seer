 package to.joe.strangeweapons;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.entity.Player;
 
 public class StrangeCommand implements CommandExecutor {
 
     StrangeWeapons plugin;
 
     public StrangeCommand(StrangeWeapons strange) {
         plugin = strange;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (!(sender instanceof Player)) {
             sender.sendMessage(ChatColor.RED + "Only players may use this command");
             return true;
         }
         Player player = (Player) sender;
         if (!player.getItemInHand().getType().equals(Material.AIR)) {
             NameableItem item = new NameableItem((CraftItemStack) player.getItemInHand());
             if (item.isStrange()) {
                 sender.sendMessage(ChatColor.RED + "That item is already strange");
                 return true;
             } else {
                 item.makeStrange();
                 item.setKills(0);
                 item.setName(ChatColor.GOLD + plugin.getWeaponName(0) + " " + plugin.toTitleCase(player.getItemInHand().getType().toString().toLowerCase().replaceAll("_", " ")));
                 item.setLore(new String[] { ChatColor.WHITE + "Kills: 0" });
                 sender.sendMessage(ChatColor.AQUA + "You never noticed it before, but the weapon you're holding looks awfully strange.");
                 return true;
             }
         } else {
             sender.sendMessage(ChatColor.RED + "I can't make strange air!");
             return true;
         }
     }
 
 }
