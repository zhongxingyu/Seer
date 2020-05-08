 package to.joe.j2mc.fun.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.PlayerInventory;
 
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.j2mc.core.command.MasterCommand;
 import to.joe.j2mc.core.exceptions.BadPlayerMatchException;
 import to.joe.j2mc.fun.J2MC_Fun;
 
 public class ClearInventoryCommand extends MasterCommand {
 
     J2MC_Fun plugin;
 
     public ClearInventoryCommand(J2MC_Fun plugin) {
         super(plugin);
         this.plugin = plugin;
     }
 
     @Override
     public void exec(CommandSender sender, String commandName, String[] args, Player player, boolean isPlayer) {
         Player target = null;
         if (isPlayer && (args.length == 0)) {
             target = player;
             player.sendMessage(ChatColor.RED + "Inventory emptied");
             this.plugin.getLogger().info(ChatColor.RED + player.getName() + " emptied inventory");
         } else if ((args.length == 1) && (!isPlayer || J2MC_Manager.getPermissions().hasFlag(player.getName(), 'a'))) {
             try {
                 target = J2MC_Manager.getVisibility().getPlayer(args[0], null);
             } catch (final BadPlayerMatchException e) {
                 sender.sendMessage(ChatColor.RED + e.getMessage());
                 return;
             }
            this.plugin.getLogger().info(ChatColor.RED + player.getName() + " emptied inventory of " + target.getName());
         }
         if (target != null) {
             final PlayerInventory targetInventory = target.getInventory();
             targetInventory.clear(36);
             targetInventory.clear(37);
             targetInventory.clear(38);
             targetInventory.clear(39);
             targetInventory.clear();
         }
     }
 
 }
