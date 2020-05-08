 
 import com.vexsoftware.votifier.model.Vote;
 import com.vexsoftware.votifier.model.VoteListener;
 import de.bananaco.bpermissions.api.ApiLayer;
 import de.bananaco.bpermissions.api.CalculableType;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class Promoter
   implements VoteListener
 {
   public void voteMade(Vote vote)
   {
     String username = vote.getUsername();
 
     boolean haspermission = ApiLayer.hasPermission("world", CalculableType.USER, username, "essentials.afk");
 
     if (!haspermission) {
       //ApiLayer.setGroup("world", CalculableType.USER, username, "Member");//
       
       Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "pr " + username +" member");
 
       Player newbie = Bukkit.getServer().getPlayer(username);
      newbie.sendMessage(ChatColor.RED + "[Vote] " + ChatColor.WHITE + "You been promoted to " + ChatColor.GREEN + "Member");
     }
   }
 }
