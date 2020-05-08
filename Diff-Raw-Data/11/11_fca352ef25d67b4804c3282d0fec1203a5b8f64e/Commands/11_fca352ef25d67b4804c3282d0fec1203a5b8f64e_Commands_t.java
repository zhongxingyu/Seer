 package net.betterverse.unclaimed;
 
 import java.util.Random;
 import net.betterverse.communities.Communities;
 import net.betterverse.communities.community.WorldCoord;
 import net.betterverse.mychunks.bukkit.MyChunks;
 import net.betterverse.mychunks.bukkit.chunk.OwnedChunk;
 import net.betterverse.mychunks.bukkit.hooks.WorldguardManager;
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.entity.Player;
 
 public class Commands {
     
     private Main instance;
     
     public Commands(final Main instance) {
         this.instance = instance;
         PluginCommand quest = instance.getCommand("unclaimed");
         
         CommandExecutor commandExecutor = new CommandExecutor() {
             @Override
             public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                 if (sender instanceof Player) {
                     if (args.length == 0) {
                         sender.sendMessage(protectionStatus(((Player)sender).getLocation()));
                     } else {
                         if (args[0].equalsIgnoreCase("reload")) {
                             if (sender.hasPermission("unclaimed.reload")) instance.config.reload(instance);
                             else sender.sendMessage(Main.PREFIX + "You do not have permission to do this.");
                         } else if (args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tp")) {
                             if (sender.hasPermission("unclaimed.teleport")) TeleToUnclaimed((Player)sender);
                             else sender.sendMessage(Main.PREFIX + "You do not have permission to do this");
                         } else return false;
                     }
                 } else {
                     if (args.length > 0 && args[0].equalsIgnoreCase("reload")) instance.config.reload(instance);
                     else sender.sendMessage(Main.PREFIX + "Go home Console, you're drunk.");
                 }
                 
                 return true;
             }
         };
         
         quest.setExecutor(commandExecutor);
     }
     
     private String protectionStatus(Location l) {
         String response = Main.PREFIX + "WorldGuard: ";
         
         Chunk c = l.getChunk();
         
         //Worldguard checks
         if (isWorldGuarded(c)) response += "" + ChatColor.BOLD + ChatColor.RED + "PROTECTED." + ChatColor.RESET;
         else response += "" + ChatColor.GREEN + "Unprotected." + ChatColor.RESET;
         
         response += " MyChunks: ";
         
         //MyChunks checks
        if (isMyChunkd(l.getWorld().getName(), c)) response += "" + ChatColor.BOLD + ChatColor.RED + "PROTECTED." + ChatColor.RESET;
         else response += "" + ChatColor.GREEN + "Unprotected." + ChatColor.RESET;
         
         response += " Communities: ";
         
         //Finally, Communities checks
         if (isCommunitied(l)) response += "" + ChatColor.BOLD + ChatColor.RED + "PROTECTED." + ChatColor.RESET;
         else response += "" + ChatColor.GREEN + "Unprotected." + ChatColor.RESET;
         
         return response;
     }
     
     private boolean isWorldGuarded(Chunk c) {
         WorldguardManager wgm = new WorldguardManager(instance);
         return wgm.hasRegion(c);
     }
     
    private boolean isMyChunkd(String w, Chunk c) {
         MyChunks mc = (MyChunks)instance.getServer().getPluginManager().getPlugin("MyChunks");
        OwnedChunk oc = mc.getChunkManager().getOwnedChunk(w, c.getX(), c.getZ());
         
         return (oc != null);
     }
     
     private boolean isCommunitied(Location l) {
         Communities com = (Communities)instance.getServer().getPluginManager().getPlugin("Communities");
         return (com.isChunkOwned(WorldCoord.parseWorldCoord(l)));
     }
     
     private boolean isProtected(Location l) {
         Chunk c = l.getChunk();
         
        return ((isWorldGuarded(c)) || (isMyChunkd(l.getWorld().getName(), c)) || (isCommunitied(l)));
     }
     
     private void TeleToUnclaimed(Player p) {
         Random r = new Random();
         
         Location l;
         
         do {
             int x;
             int z;
             
             //Do a random. 50% chance of positive, 50% chance of negative.
             if (r.nextInt(101) <= 49)  x = r.nextInt(instance.config.maxX - 500) + 500;
             else x = (r.nextInt(instance.config.maxX - 500) - 500) * -1;
             
             //And again for Z.
             if (r.nextInt(101) <= 49)  z = r.nextInt(instance.config.maxZ - 500) + 500;
             else z = (r.nextInt(instance.config.maxZ - 500) - 500) * -1;
             
             l = new Location(p.getWorld(), x, p.getWorld().getHighestBlockAt(x, z).getY() + 2, z);
         } while (isProtected(l));
         
         p.teleport(l);
         p.sendMessage(Main.PREFIX + "Found a suitable location! Enjoy!");
     }
 
 }
