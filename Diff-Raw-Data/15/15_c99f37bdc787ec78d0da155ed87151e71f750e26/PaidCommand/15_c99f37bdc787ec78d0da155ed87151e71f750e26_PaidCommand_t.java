 
 import com.lordralex.ralexbot.RalexBot;
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
 import com.lordralex.ralexbot.api.Listener;
 import com.lordralex.ralexbot.api.events.CommandEvent;
 import com.lordralex.ralexbot.api.sender.Sender;
 import com.lordralex.ralexbot.settings.Settings;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.logging.Level;
 
 /*
  * Copyright (C) 2013 Laptop
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 /**
  * @version 1.0
  * @author Laptop
  */
 public class PaidCommand extends Listener {
 
     private final String HASPAID = "https://minecraft.net/haspaid.jsp?user={0}";
     private int MAX_NAMES;
     private ExecutorService es;
 
     @Override
     public void setup() {
         MAX_NAMES = Settings.getGlobalSettings().getInt("paid-name-limit");
         es = Executors.newSingleThreadExecutor();
     }
 
     @Override
    @EventType(event = EventField.Command)
     public void runEvent(CommandEvent event) {
         if (event.getArgs().length == 0) {
             return;
         }
         if (event.getArgs().length > MAX_NAMES) {
             event.getSender().sendNotice("I can only do " + MAX_NAMES + " lookups at once");
             return;
         }
         for (String name : event.getArgs()) {
             Sender target = event.getChannel();
             if (target == null) {
                 target = event.getSender();
             }
             es.submit(new Lookup(target, name));
         }
     }
 
    @Override
    public String[] getAliases() {
        return new String[]{
            "paid"
        };
    }

     private class Lookup implements Runnable {
 
         private String name;
         private Sender target;
 
         public Lookup(Sender tar, String n) {
             target = tar;
             name = n;
         }
 
         @Override
         public void run() {
             try {
                URL url = new URL(HASPAID.replace("{0}", name));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                 String reply = reader.readLine();
                 if (reply.equalsIgnoreCase("true")) {
                     target.sendMessage("The user '" + name + "' is a premium account");
                 } else {
                     target.sendMessage("The user '" + name + "' is NOT a premium account");
                 }
             } catch (Exception e) {
                 RalexBot.getLogger().log(Level.SEVERE, "An error occured on looking up " + name, e);
                 target.sendMessage("An error occured while looking to see if '" + name + "' has paid");
             }
         }
     }
 }
