 package uk.co.quartzcraft.kingdoms.command;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import uk.co.quartzcraft.core.chat.ChatPhrase;
 import uk.co.quartzcraft.core.command.QSubCommand;
 import uk.co.quartzcraft.kingdoms.QuartzKingdoms;
 import uk.co.quartzcraft.kingdoms.entity.QKPlayer;
 import uk.co.quartzcraft.kingdoms.kingdom.Kingdom;
 import uk.co.quartzcraft.kingdoms.managers.ChunkManager;
 
 public class KingdomClaimSubCommand extends QSubCommand {
 
 	@Override
 	public String getPermission() {
 		return "QCK.Kingdom.claim";
 	}
 
 	@Override
 	public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		String playername = sender.toString();
 		String kingdomName = QKPlayer.getKingdom(playername);
 		
 		if(ChunkManager.claimChunk(playername)) {
 			sender.sendMessage(ChatPhrase.getPhrase("chunk_claimed_for_kingdom_yes") + ChatColor.WHITE + kingdomName);
 		} else {
			sender.sendMessage(ChatPhrase.getPhrase("chunk_claimed_for_kingdom_yes") + ChatColor.WHITE + kingdomName);
 		}
 		
 	}
 }
