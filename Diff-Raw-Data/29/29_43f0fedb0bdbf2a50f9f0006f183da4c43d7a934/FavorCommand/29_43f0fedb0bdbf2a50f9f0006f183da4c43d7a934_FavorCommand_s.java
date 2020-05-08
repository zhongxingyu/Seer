 package com.github.roburrito.bukkit.godly;
 
 /*TODO: FIX IMPORT LIST*/
 import java.util.List;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.github.roburrito.bukkit.godly.Godly;
import com.github.roburrito.bukkit.godly.listeners.FixedMetadataValue; 
import com.github.roburrito.bukkit.godly.listeners.MetadataValue; 
 import com.github.roburrito.bukkit.util.FavorMetadataMethods;
 import com.palmergames.bukkit.util.Colors;
 
 /*This class handles all commands beginning with "/favor"*/
 public class FavorCommand implements CommandExecutor {
  
 	/*variable and constructor only necessary if methods are used from the main class*/
 	private final Godly plugin;
 	private FavorMetadataMethods favorMethods;
  
 	public FavorCommand(Godly plugin) {
 		this.plugin = plugin;
 	}
  
 	/*TODO: add all commands listed here in Plugin.yml for Godly*/
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		Player player = null;
 		if(sender instanceof Player) {
 			player = (Player)sender;
 			
 			if(args.length == 0) {
 				printPlayersFavors(player);
 			}
 		}
 		/*Are there favor-based commands that servers can ask?*/
 		if(player == null) { 
 			sender.sendMessage(Colors.Rose + "Only players can asketh thy Gods for favors!");
 		}
 
 		return false;
 	}
 	
 	/*Lists the favor levels that the player has for each God and Goddess*/
 	public void printPlayersFavors(Player player) {
 		int rank, i = 0;
 		String godlyNames[] = favorMethods.godlyNames;
 		String Godly_Favors[] = favorMethods.godlyFavors;
 		
 		player.sendMessage("This is what each God/Goddess thinks of you:");
 		
 		for(String favor : Godly_Favors){
 			rank = favorMethods.getRank(player, favor);
 			/*TODO: Reformat message so that it doesn't flat out state rank, 
 			 * but rather elusively tells player what the God/Goddess thinks of them (use a switch).*/
 			player.sendMessage(godlyNames[i] + ": " + rank);
 			i++;
 		}
 	}
 	
 }
