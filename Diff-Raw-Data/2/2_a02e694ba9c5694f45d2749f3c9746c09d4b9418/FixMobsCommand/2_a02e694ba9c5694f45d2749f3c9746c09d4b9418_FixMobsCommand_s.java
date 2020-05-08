 package me.limebyte.endercraftessentials.commands;
 
 import me.limebyte.endercraftessentials.EndercraftEssentials;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Difficulty;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class FixMobsCommand implements CommandExecutor {
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args[0] == null) {
 			if (sender instanceof Player) {
 				Player player = (Player) sender;
 				fixMobs(player.getWorld());
 				player.sendMessage(ChatColor.GREEN + "Mobs Fixed!");
 			}
 		} else {
 			if (Bukkit.getWorld(args[0]) == null) {
 				sender.sendMessage(ChatColor.RED + "Invaild world!");
 			} else {
 				fixMobs(Bukkit.getWorld(args[0]));
 				sender.sendMessage(ChatColor.GREEN + "Mobs Fixed!");
 			}
 		}
 		return false;
 	}
 	
 	private void fixMobs(final World world) {
 		// Store current difficulty
 		final Difficulty worldDifficulty = world.getDifficulty();
 		
 		// Set it to peaceful
 		world.setDifficulty(Difficulty.PEACEFUL);
 		
 		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(EndercraftEssentials.getInstance(), new Runnable() {
 			   public void run() {
 				   // Set it back to the original difficulty
 				   world.setDifficulty(worldDifficulty);
 			   }
 		}, 20L);
 	}
 
 }
