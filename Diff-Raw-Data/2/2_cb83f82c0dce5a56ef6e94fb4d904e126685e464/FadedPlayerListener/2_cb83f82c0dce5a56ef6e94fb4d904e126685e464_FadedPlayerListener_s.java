 package me.lilfade.faded;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 import me.lilfade.faded.Fadedhttp;
 
 public class FadedPlayerListener implements Listener {
 	//gets new player event, and passes data to the webserver for pre-registration
 	@EventHandler(priority = EventPriority.HIGHEST)
     public void firstJoin(PlayerJoinEvent event) {
 		// Define our variables.
         Player player = event.getPlayer();
         //will be true if the player has played before
         Boolean b = player.hasPlayedBefore();
         //if they have lets setup the health and what not
         if(!b){
         	//could set players inital values here
             //player.setLevel(0);
             //player.setHealth(20);
             //player.setFoodLevel(1);
        	Fadedhttp.sendGetRequest("http://fadedgaming.co/serverenable.php", "cname="+player.getName());
         } else {
         	//do nothing ... for now
         }        
 	}
 }
