 package au.com.mineauz.SkyQuest.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import au.com.mineauz.SkyQuest.SkyQuestPlugin;
 import au.com.mineauz.SkyQuest.pedestals.BlankQuestPedestal;
 import au.com.mineauz.SkyQuest.pedestals.Pedestals;
 import au.com.mineauz.SkyQuest.pedestals.SavePedestal;
 
 public class NewPedestalCommand implements ICommand{
 
 	@Override
 	public String getName() {
 		return "pedestal";
 	}
 
 	@Override
 	public String[] getAliases() {
 		return new String[] { "ped" };
 	}
 
 	@Override
 	public String getPermission() {
 		return "skyquest.pedestal.create";
 	}
 
 	@Override
 	public String getUsageString(String label) {
 		return "Creates a pedestal where you are looking";
 	}
 
 	@Override
 	public boolean canBeConsole() {
 		return false;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, String label, String[] args) {
 		
 		Player player = (Player)sender;
 		
 		Location target = player.getTargetBlock(null, 25).getLocation();
 		
 		if(target != null && target.getBlock().getType() != Material.AIR)
 		{
 			target.setY(target.getY() + 1);
 			if(args[0].equalsIgnoreCase("savepoint")){
 				Pedestals.addPedestal(new SavePedestal(target));
				SkyQuestPlugin.instance.saveData();
 			}
 			else if(args[0].equalsIgnoreCase("questpoint")){
 				Pedestals.addPedestal(new BlankQuestPedestal(target));
				SkyQuestPlugin.instance.saveData();
 			}
 			else{
 				player.sendMessage(ChatColor.RED + "Invalid pedestal type!");
 			}
 		}
 		else
 		{
 			player.sendMessage(ChatColor.RED + "No target block in range!");
 		}
 		return true;
 	}
 
 }
