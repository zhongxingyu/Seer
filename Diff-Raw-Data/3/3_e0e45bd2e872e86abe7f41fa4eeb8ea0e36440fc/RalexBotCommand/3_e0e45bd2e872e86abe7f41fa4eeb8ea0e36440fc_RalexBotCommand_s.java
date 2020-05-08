 
 import com.lordralex.ralexbot.RalexBot;
 import com.lordralex.ralexbot.api.EventField;
 import com.lordralex.ralexbot.api.EventType;
 import com.lordralex.ralexbot.api.Listener;
 import com.lordralex.ralexbot.api.events.CommandEvent;
 import com.lordralex.ralexbot.api.sender.Sender;
 import com.lordralex.ralexbot.api.users.BotUser;
 import org.pircbotx.PircBotX;
 
 public class RalexBotCommand extends Listener {
 
     @Override
     @EventType(event = EventField.Command)
     public void runEvent(CommandEvent event) {
         Sender target = event.getChannel();
         if (target == null) {
             target = event.getSender();
             if (target == null) {
                 return;
             }
            target.sendMessage("Hello. I am " + BotUser.getBotUser().getNick() + " " + RalexBot.VERSION + " using PircBotX " + PircBotX.VERSION);
         }
     }
 
     @Override
     public String[] getAliases() {
         return new String[]{
                     BotUser.getBotUser().getNick().toLowerCase(),
                     "version"
                 };
     }
 }
