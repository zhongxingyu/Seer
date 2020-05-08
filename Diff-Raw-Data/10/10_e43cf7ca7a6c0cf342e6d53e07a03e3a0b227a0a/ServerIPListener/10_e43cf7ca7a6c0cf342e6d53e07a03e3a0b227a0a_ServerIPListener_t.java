 
 import com.lordralex.ralexbot.api.EventField;
 import com.lordralex.ralexbot.api.EventType;
 import com.lordralex.ralexbot.api.Listener;
 import com.lordralex.ralexbot.api.Priority;
 import com.lordralex.ralexbot.api.Utils;
 import com.lordralex.ralexbot.api.events.CommandEvent;
 import com.lordralex.ralexbot.api.events.MessageEvent;
 import com.lordralex.ralexbot.api.events.PartEvent;
 import com.lordralex.ralexbot.api.events.QuitEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ServerIPListener extends Listener {
 
     protected PingServerCommand pingServer;
     protected List<String> triggered;
     protected List<String> ignorePeople;
 
     @Override
     public void setup() {
         pingServer = new PingServerCommand();
         triggered = new ArrayList<>();
         ignorePeople = new ArrayList<>();
     }
 
     @Override
     @EventType(event = EventField.Message, priority = Priority.HIGH)
     public void runEvent(MessageEvent event) {
         String channel = event.getChannel();
         String sender = event.getSender();
         String message = event.getMessage();
 
         boolean silence = false;
 
         if (ignorePeople.contains(sender.toLowerCase())) {
             return;
         }
 
         if (triggered.contains(sender.toLowerCase())) {
             silence = true;
         } else if (triggered.contains(event.getHostname().toLowerCase())) {
             silence = true;
         }
         String[] messageParts = message.split(" ");
         for (String part : messageParts) {
             if (isServer(part)) {
                 if (!silence) {
                     Utils.sendMessage(channel, "Please do not advertise servers here");
                     triggered.remove(sender.toLowerCase());
                     triggered.remove(event.getHostname().toLowerCase());
                     triggered.add(sender.toLowerCase());
                     triggered.add(event.getHostname().toLowerCase());
                 } else if (triggered.contains(sender.toLowerCase())) {
                     Utils.kick(sender, channel, "Server advertisement");
                     event.setCancelled(true);
                 }
                 break;
             }
         }
     }
 
     @Override
     @EventType(event = EventField.Part)
     public void runEvent(PartEvent event) {
         triggered.remove(event.getSender().toLowerCase());
     }
 
     @Override
     @EventType(event = EventField.Quit)
     public void runEvent(QuitEvent event) {
         triggered.remove(event.getSender().toLowerCase());
     }
 
     @Override
     @EventType(event = EventField.Command)
     public void runEvent(CommandEvent event) {
 
         if (event.getCommand().equalsIgnoreCase("ignoread")) {
             if (Utils.hasOP(event.getSender(), event.getChannel())) {
                 if (event.getArgs().length == 1) {
                    ignorePeople.add(event.getArgs()[0].toLowerCase());
                     Utils.sendMessage(event.getChannel(), "He will be ignored with IPs now");
                 }
             }
         } else if (event.getCommand().equalsIgnoreCase("unignoread")) {
             if (Utils.hasOP(event.getSender(), event.getChannel())) {
                 if (event.getArgs().length == 1) {
                    if (ignorePeople.remove(event.getArgs()[0].toLowerCase())) {
                         Utils.sendMessage(event.getChannel(), "He will be not ignored with IPs now");
                     }
                 }
             }
         } else if (event.getCommand().equalsIgnoreCase("reset")) {
             if (Utils.hasOP(event.getSender(), event.getChannel())) {
                 if (event.getArgs().length == 1) {
                    if (triggered.remove(event.getArgs()[0].toLowerCase())) {
                         Utils.sendMessage(event.getChannel(), "His counter was removed");
                     }
                 }
             }
         }
     }
 
     private boolean isServer(String testString) {
         String test = testString.toLowerCase().trim();
 
         String[] parts = split(test, ".");
         if (parts.length == 4) {
             if (parts[3].contains(":")) {
                 parts[3] = parts[3].split(":")[0];
             }
             for (int i = 0; i < 4; i++) {
                 try {
                     Integer.parseInt(parts[i]);
                 } catch (NumberFormatException e) {
                     return false;
                 }
             }
             return true;
         }
         return false;
     }
 
     private String[] split(String message, String lookFor) {
         List<String> parts = new ArrayList<>();
         String test = message.toString();
         while (test.contains(lookFor)) {
             int id = test.indexOf(lookFor);
             if (id == -1) {
                 break;
             }
             parts.add(test.substring(0, id));
             test = test.substring(id + 1);
         }
         parts.add(test);
 
         return parts.toArray(new String[0]);
     }
 }
