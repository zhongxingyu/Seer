 import org.bukkit.event.Listener;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.ChatColor;
  
 public class PlayerListener implements Listener {
 	String[] devs = new String{"evelmidget38","Husky","MYCRAFTisbest", "ludo0777"};
       
       @EventHandler(priority = EventPriority.HIGHEST)
     public void PlayerJoin(final PlayerJoinEvent event) {
         Player p = event.getPlayer();
         boolean isdev = false;
         for(String dev:devs) {
         	if (p.getName().equals(dev)) {
         		Bukkit.broadcastMessage(ChatColor.BLUE + "[MyIsle] " + ChatColor.GOLD + p.getName() + " made MyIsle");
         		isdev=true;
         	}
         }
      if(!isdev) {
     	    	  Bukkit.broadcastMessage(ChatColor.BLUE + "[MyIsle]" + ChatColor.GREEN + p.getName() + "has joined as level <var>");
     	    	 ((Player) p).sendMessage(ChatColor.BLUE + "[MyIsle]" + ChatColor.GREEN + "This is a Isle server");
     	      }
    	if (voteMsg) return;{
     		((Player) p).sendMessage(ChatColor.BLUE + "[MyIsle]" + ChatColor.GREEN + "Vote for the server and get a larger isle");
     	}
 	}
     }
