 package io.github.psgs.serverutils.commands;
 
 import io.github.psgs.serverutils.ServerUtils;
 import org.bukkit.*;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Firework;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.FireworkMeta;
 
 public class PartyCommand implements CommandExecutor {
 
     ServerUtils plugin;
 
     public PartyCommand(ServerUtils plugin) {
         this.plugin = plugin;
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 
         final String name = plugin.getConfig().getString("hubWorld");
         World w = Bukkit.getServer().getWorld(name);
 
         if (cmd.getName().equalsIgnoreCase("party")) {
             if (sender.isOp() || sender.hasPermission("utils.party")) {
                /*w.playEffect(Effect.STEP_SOUND, 79);

                FireworkMeta fireworkMeta = (FireworkMeta) (new ItemStack(
                        Material.FIREWORK)).getItemMeta();

                Firework firework = Bukkit.getWorld(name).spawnEntity(EntityType.FIREWORK);

                fireworkMeta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BURST)
                        .withColor(Color.RED).withColor(Color.WHITE)
                        .withColor(Color.BLUE).withTrail().build());
                firework.setFireworkMeta(fireworkMeta);*/
 
             } else {
                 sender.sendMessage(ChatColor.RED + "I don't think so!");
             }
         }
         return false;
     }
 }
