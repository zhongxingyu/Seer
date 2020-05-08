 package edgruberman.bukkit.simpletimestamp;
 
 import java.text.SimpleDateFormat;
import java.util.Calendar;
 
 import org.bukkit.event.player.PlayerChatEvent;
 
 public class PlayerListener extends org.bukkit.event.player.PlayerListener {
     
     private Main main;
     
     public PlayerListener(Main main) {
         this.main = main;
     }
     
     @Override
     public void onPlayerChat(PlayerChatEvent event) {
         
         // Default Format = "<%1$s> %2$s"
         String format = this.main.getConfiguration().getString("format")
             .replace(
                 "TIMESTAMP"
                , (new SimpleDateFormat(this.main.getConfiguration().getString("timestamp")).format(Calendar.getInstance().getTime()))
             )
             .replace(
                 "FORMAT"
                 , event.getFormat()
             )
         ;
         
         event.setFormat(format);
     }
 }
