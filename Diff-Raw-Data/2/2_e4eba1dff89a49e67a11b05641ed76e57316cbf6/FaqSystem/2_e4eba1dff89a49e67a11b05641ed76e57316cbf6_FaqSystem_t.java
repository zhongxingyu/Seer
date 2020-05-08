 /*
  * Copyright (C) 2013 Lord_Ralex
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
 
 import com.lordralex.ralexbot.EventHandler;
 import com.lordralex.ralexbot.RalexBot;
 import com.lordralex.ralexbot.api.EventField;
 import com.lordralex.ralexbot.api.EventType;
 import com.lordralex.ralexbot.api.Listener;
 import com.lordralex.ralexbot.api.events.CommandEvent;
 import com.lordralex.ralexbot.api.users.BotUser;
 import com.lordralex.ralexbot.settings.Settings;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import org.pircbotx.Colors;
 
 /**
  * @author Lord_Ralex
  * @version 1.0
  */
 public class FaqSystem extends Listener {
 
     private final Map<String, Database> databases = new ConcurrentHashMap<>();
     private int delay = 2;
     private final ScheduledExecutorService es = Executors.newScheduledThreadPool(3);
 
     @Override
     public void setup() {
         loadDatabases();
         delay = Settings.getGlobalSettings().getInt("faq-delay");
     }
 
     @Override
     @EventType(event = EventField.Command)
     public void runEvent(CommandEvent event) {
         if (event.getCommand().equalsIgnoreCase("refresh")) {
             loadDatabases();
             event.getSender().sendMessage("Updated local storage of all databases");
             return;
         } else {
             boolean allowExec = true;
             String cmdMethod = event.getCommand().toLowerCase();
             Database index = null;
             String[] dbNames = databases.keySet().toArray(new String[databases.size()]);
             for (String name : dbNames) {
                 if (event.getCommand().startsWith(name)) {
                     index = databases.get(name);
                     break;
                 } else if (event.getChannel() != null) {
                     if (event.getChannel().getName().startsWith("#" + name)) {
                         index = databases.get(name);
                         break;
                     }
                 }
             }
             if (index == null) {
                 allowExec = false;
             } else {
                 String master = index.getMaster();
                 if (master == null) {
                     allowExec = true;
                 } else {
                     List<String> users = event.getChannel().getUsers();
                     for (String u : users) {
                         if (u.equalsIgnoreCase(master)) {
                             allowExec = false;
                         }
                     }
                 }
             }
             if (!allowExec) {
                 return;
             }
             if (index == null) {
                 event.getSender().sendNotice("No database is selected");
                 return;
             }
             boolean databaseChanged = false;
             for (String dbName : dbNames) {
                 cmdMethod = cmdMethod.replace(dbName, "");
             }
             switch (cmdMethod) {
                 case ">": {
                     String target = event.getArgs()[0];
                     String channel = event.getChannel().getName();
                     String[] lines = index.getEntry(event.getArgs()[1].toLowerCase());
                     if (lines == null || lines.length == 0) {
                         event.getSender().sendNotice(index.getName() + " does not the factoid " + event.getArgs()[1] + " in the database");
                         return;
                     }
                     RunLaterThread thread = new RunLaterThread(event.getArgs()[1].toLowerCase(), target, channel, lines, false);
                     thread.setFuture(es.scheduleWithFixedDelay(thread, 1, delay, TimeUnit.SECONDS));
                 }
                 break;
                 case ">>": {
                     String target = event.getArgs()[0];
                     String channel = event.getChannel().getName();
                     String[] lines = index.getEntry(event.getArgs()[1].toLowerCase());
                     if (lines == null || lines.length == 0) {
                         event.getSender().sendNotice(index.getName() + " does not the factoid " + event.getArgs()[1] + " in the database");
                         return;
                     }
                     RunLaterThread thread = new RunLaterThread(event.getArgs()[1].toLowerCase(), target, channel, lines, true);
                     thread.setFuture(es.scheduleWithFixedDelay(thread, 1, delay, TimeUnit.SECONDS));
                 }
                 break;
                 case "<<": {
                     String target = event.getArgs()[0];
                     String channel = event.getChannel().getName();
                     String[] lines = index.getEntry(event.getArgs()[1].toLowerCase());
                     if (lines == null || lines.length == 0) {
                         event.getSender().sendNotice(index.getName() + " does not the factoid " + event.getArgs()[1] + " in the database");
                         return;
                     }
                     RunLaterThread thread = new RunLaterThread(event.getArgs()[1].toLowerCase(), target, channel, lines, true);
                     thread.setFuture(es.scheduleWithFixedDelay(thread, 1, delay, TimeUnit.SECONDS));
                 }
                 break;
                 case "<": {
                     String target = event.getSender().getNick();
                     String channel = event.getChannel().getName();
                     String[] lines = index.getEntry(event.getArgs()[0].toLowerCase());
                     if (lines == null || lines.length == 0) {
                         event.getSender().sendNotice(index.getName() + " does not the factoid " + event.getArgs()[0] + " in the database");
                         return;
                     }
                     RunLaterThread thread = new RunLaterThread(event.getArgs()[0].toLowerCase(), target, channel, lines, true);
                     thread.setFuture(es.scheduleWithFixedDelay(thread, 1, delay, TimeUnit.SECONDS));
                 }
                 break;
                 case "+": {
                     String loginName = event.getSender().isVerified();
                     if (loginName == null || !index.canEdit(loginName)) {
                         break;
                     }
                     if (index.isReadonly()) {
                         event.getSender().sendNotice("The " + index.getName() + " FAQ database is read-only");
                         break;
                     }
                     if (event.getArgs().length < 2) {
                         event.getSender().sendNotice("Command usage: " + EventHandler.getCommandPrefixes().get(0) + "+ [factoid] [message]");
                         break;
                     }
                     String message = "";
                     for (int i = 1; i < event.getArgs().length; i++) {
                         message += event.getArgs()[i];
                     }
                     message = message.trim();
                     String[] faq = message.split(";;");
                     index.setEntry(event.getArgs()[0].toLowerCase(), faq);
                     databaseChanged = true;
                     event.getSender().sendNotice("The factoid " + event.getArgs()[0].toLowerCase() + " has been added");
                 }
                 break;
                 case "-": {
                     String loginName = event.getSender().isVerified();
                     if (loginName == null || !index.canRemove(loginName)) {
                         break;
                     }
                     if (index.isReadonly()) {
                         event.getSender().sendNotice("The " + index.getName() + " FAQ database is read-only");
                         break;
                     }
                     if (event.getArgs().length != 1) {
                         event.getSender().sendNotice("Command usage: " + EventHandler.getCommandPrefixes().get(0) + "- [factoid]");
                         break;
                     }
                     if (index.removeEntry(event.getArgs()[0].toLowerCase())) {
                         databaseChanged = true;
                         event.getSender().sendNotice("The factoid " + event.getArgs()[0].toLowerCase() + " has been removed");
                     } else {
                         event.getSender().sendNotice("The factoid " + event.getArgs()[0].toLowerCase() + " does not exist");
                     }
                 }
                 break;
                 case "~": {
                     String loginName = event.getSender().isVerified();
                     if (loginName == null || !index.canEdit(loginName)) {
                         break;
                     }
                     if (index.isReadonly()) {
                         event.getSender().sendNotice("The " + index.getName() + " FAQ database is read-only");
                         break;
                     }
                     if (event.getArgs().length < 2) {
                         event.getSender().sendNotice("Command usage: " + EventHandler.getCommandPrefixes().get(0) + "+ [factoid] [message]");
                         break;
                     }
                     String message = "";
                     for (int i = 1; i < event.getArgs().length; i++) {
                         message += event.getArgs()[i];
                     }
                     message = message.trim();
                     String[] faq = message.split(";;");
                     index.setEntry(event.getArgs()[0].toLowerCase(), faq);
                     databaseChanged = true;
                     event.getSender().sendNotice("The factoid " + event.getArgs()[0].toLowerCase() + " has been modified");
                 }
                 break;
                 default: {
                     String target = null;
                     String channel = event.getChannel().getName();
                     String[] lines = index.getEntry(event.getArgs()[0].toLowerCase());
                     if (lines == null || lines.length == 0) {
                         event.getSender().sendNotice(index.getName() + " does not the factoid " + event.getArgs()[0] + " in the database");
                         return;
                     }
                     RunLaterThread thread = new RunLaterThread(event.getArgs()[0].toLowerCase(), target, channel, lines, false);
                     thread.setFuture(es.scheduleWithFixedDelay(thread, 1, delay, TimeUnit.SECONDS));
                 }
                 break;
             }
             if (databaseChanged) {
                 try {
                     saveDatabase(index);
                 } catch (IOException ex) {
                     RalexBot.getLogger().log(Level.SEVERE, "An error occured on saving the database " + index.getName(), ex);
                 }
             }
         }
     }
 
     @Override
     public String[] getAliases() {
         ArrayList<String> aliases = new ArrayList(Arrays.asList(new String[]{
             "faq",
             ">",
             "<",
             "<<",
             "refresh",
             "+",
             "-",
             "~"}));
         Set<String> keys;
         synchronized (databases) {
             keys = databases.keySet();
         }
         Iterator<String> it = keys.iterator();
         while (it.hasNext()) {
             String key = it.next();
             aliases.add(key + "");
             aliases.add(key + ">");
             aliases.add(key + "<<");
             aliases.add(key + "<");
             aliases.add(key + "+");
             aliases.add(key + "-");
             aliases.add(key + "~");
         }
         aliases.addAll(databases.keySet());
         return aliases.toArray(new String[aliases.size()]);
     }
 
     private void copyInputStream(InputStream in, FileOutputStream out) throws IOException {
         ReadableByteChannel rbc = Channels.newChannel(in);
         out.getChannel().transferFrom(rbc, 0, 1 << 24);
     }
 
     private synchronized void loadDatabases() {
         Settings settings = Settings.getGlobalSettings();
         List<String> databasesToLoad = settings.getStringList("faq-databases");
         for (String load : databasesToLoad) {
             try {
                 String name = load.split(" ")[0].toLowerCase();
                 String loadPath = load.split(" ")[1];
                 String savePath = load.split(" ")[2];
                 Database newDatabase = new Database(name, savePath);
                 if (load.split(" ").length <= 4) {
                     newDatabase.setReadonly(Boolean.parseBoolean(load.split(" ")[3]));
                 }
                 if (load.split(" ").length <= 5) {
                     newDatabase.setMaster(load.split(" ")[4]);
                 }
                 try {
                     if (!loadPath.equals(savePath)) {
                         InputStream reader = new URL(loadPath).openStream();
                         FileOutputStream writer = new FileOutputStream(new File(savePath));
                         copyInputStream(reader, writer);
                     }
                     BufferedReader filereader = new BufferedReader(new FileReader(savePath));
                     String line;
                     while ((line = filereader.readLine()) != null) {
                         if (line.contains("|")) {
                             String key = line.split("\\|")[0];
                             String value = line.split("\\|", 2)[1];
                             newDatabase.setEntry(key.toLowerCase(), value.split(";;"));
                         }
                     }
                 } catch (IOException ex) {
                     RalexBot.getLogger().log(Level.SEVERE, "There was an error", ex);
                 }
                 List<String> addable = settings.getStringList("faq-database-add-" + newDatabase.getName());
                 for (String n : addable) {
                     newDatabase.addAddable(n);
                 }
                 List<String> editable = settings.getStringList("faq-database-edit-" + newDatabase.getName());
                 for (String n : editable) {
                     newDatabase.addEditable(n);
                 }
                 List<String> removeable = settings.getStringList("faq-database-remove-" + newDatabase.getName());
                 for (String n : removeable) {
                     newDatabase.addRemoveable(n);
                 }
                 databases.put(name.toLowerCase(), newDatabase);
             } catch (Exception ex) {
                 RalexBot.getLogger().log(Level.SEVERE, "There was an error with this setting: " + load, ex);
             }
         }
     }
 
     private synchronized void saveDatabase(Database db) throws IOException {
         db.save();
     }
 
     private class RunLaterThread implements Runnable {
 
         private List<String> lines;
         private String channel;
         private String user;
         private boolean notice = false;
         private String name;
         private ScheduledFuture future = null;
 
         public RunLaterThread(String na, String u, String c, String[] l, boolean n) {
             lines = new ArrayList<>(Arrays.asList(l));
             channel = c;
             user = u;
             notice = n;
             name = na;
         }
 
         @Override
         public void run() {
             if (lines.isEmpty()) {
                 if (future != null) {
                     future.cancel(true);
                 }
                 return;
             }
             BotUser bot = BotUser.getBotUser();
            String message = lines.remove(0);
             if (user == null) {
                 message = Colors.BOLD + name.toLowerCase() + ": " + Colors.NORMAL + message;
             } else {
                 message = Colors.BOLD + user + ": " + Colors.NORMAL + "(" + name.toLowerCase() + ") " + message;
             }
             if (notice) {
                 bot.sendNotice(user, message);
             } else {
                 bot.sendMessage(channel, message);
             }
             if (lines.isEmpty()) {
                 if (future != null) {
                     future.cancel(true);
                 }
             }
         }
 
         public void setFuture(ScheduledFuture sf) {
             future = sf;
         }
     }
 
     private class Database {
 
         private final File fileLocation;
         private final Map<String, String[]> factoids = new ConcurrentHashMap<>();
         private String master = null;
         private boolean readOnly = false;
         private final String name;
         private final Set<String> add = new HashSet<>();
         private final Set<String> remove = new HashSet<>();
         private final Set<String> edit = new HashSet<>();
 
         public Database(String n, String filePath) {
             fileLocation = new File(filePath);
             name = n;
         }
 
         public Database(String n, File filePath) {
             fileLocation = filePath;
             name = n;
         }
 
         public String getName() {
             return name;
         }
 
         public void setMaster(String m) {
             master = m;
         }
 
         public String getMaster() {
             return master;
         }
 
         public void setReadonly(boolean newBool) {
             readOnly = newBool;
         }
 
         public boolean isReadonly() {
             return readOnly;
         }
 
         public String[] getEntry(String key) {
             String[] entry;
             synchronized (factoids) {
                 entry = factoids.get(key);
             }
             return entry;
         }
 
         public boolean setEntry(String key, String[] newEntry) {
             synchronized (factoids) {
                 factoids.put(key, newEntry);
             }
             return true;
         }
 
         public boolean removeEntry(String key) {
             boolean removed;
             synchronized (factoids) {
                 removed = factoids.remove(key) != null;
             }
             return removed;
         }
 
         public File getFile() {
             return fileLocation;
         }
 
         public synchronized void save() throws IOException {
             fileLocation.mkdirs();
             fileLocation.delete();
             try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileLocation))) {
                 synchronized (factoids) {
                     Set<String> keys = factoids.keySet();
                     for (String key : keys) {
                         String line = key + "|";
                         String[] parts = factoids.get(key);
                         for (int i = 0; i < parts.length; i++) {
                             if (i != 0) {
                                 line += ";;";
                             }
                             line += parts[i];
                             writer.write(line);
                             writer.newLine();
                         }
                     }
                 }
             }
         }
 
         public boolean canEdit(String login) {
             if (login == null) {
                 return false;
             }
             return edit.contains(login.toLowerCase());
         }
 
         public boolean canAdd(String login) {
             if (login == null) {
                 return false;
             }
             return add.contains(login.toLowerCase());
         }
 
         public boolean canRemove(String login) {
             if (login == null) {
                 return false;
             }
             return remove.contains(login.toLowerCase());
         }
 
         public void addAddable(String n) {
             add.add(n.toLowerCase());
         }
 
         public void addEditable(String n) {
             edit.add(n.toLowerCase());
         }
 
         public void addRemoveable(String n) {
             remove.add(n.toLowerCase());
         }
     }
 }
