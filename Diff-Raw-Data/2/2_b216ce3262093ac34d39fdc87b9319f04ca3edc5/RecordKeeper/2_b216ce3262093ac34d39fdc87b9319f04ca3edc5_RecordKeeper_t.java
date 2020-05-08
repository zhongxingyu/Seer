 package edgruberman.bukkit.doorman;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 /** manages declaration history and display */
 public final class RecordKeeper {
 
     private final Plugin plugin;
     private final File configFile;
     private final FileConfiguration config;
     private final List<Message> history = new ArrayList<Message>();
 
     RecordKeeper(final Plugin plugin) {
         this.plugin = plugin;
         this.configFile = new File(plugin.getDataFolder(), "declarations.yml");
         this.config = YamlConfiguration.loadConfiguration(this.configFile);
         this.load(this.config);
     }
 
     private void load(final ConfigurationSection history) {
         for (final String setKey : history.getKeys(false)) {
             long set;
             try {
                 set = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz")).parse(setKey).getTime();
             } catch (final ParseException e) {
                 this.plugin.getLogger().warning("Unable to parse declaration set: " + setKey + "; " + e);
                 continue;
             }
 
             final String from = history.getString(setKey + ".from");
             final String text = history.getString(setKey + ".text");
 
             this.history.add(new Message(set, from, text));
         }
 
         Collections.sort(this.history, Message.NEWEST_FIRST);
     }
 
     public List<Message> getHistory() {
        return Collections.unmodifiableList(this.history);
     }
 
     public void add(final long set, final String from, final String text) {
         this.history.add(new Message(set, from, text));
         Collections.sort(this.history, Message.NEWEST_FIRST);
         this.save();
     }
 
     public void declare(final CommandSender recipient) {
         this.declare(recipient, this.history.get(0));
     }
 
     public void declare(final CommandSender recipient, final Message message) {
         final String name = (recipient instanceof Player ? ((Player) recipient).getDisplayName() : recipient.getName());
         Main.courier.send(recipient, "declaration", name, message.set, message.from, message.text, RecordKeeper.duration(System.currentTimeMillis() - message.set));
     }
 
     private void save() {
         for (final Message message : this.history) {
             final String key = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz")).format(new Date(message.set));
             this.config.set(key + ".from", message.from);
             this.config.set(key + ".text", message.text);
         }
         try {
             this.config.save(this.configFile);
         } catch (final IOException e) {
             this.plugin.getLogger().warning("Unable to save \"" + this.configFile + "\"; " + e);
         }
     }
 
     private static String duration(final long milliseconds) {
         final long totalSeconds = milliseconds / 1000;
         final long days = totalSeconds / 86400;
         final long hours = (totalSeconds % 86400) / 3600;
         final long minutes = ((totalSeconds % 86400) % 3600) / 60;
         final long seconds = totalSeconds % 60;
         final StringBuilder sb = new StringBuilder();
         if (days > 0)sb.append(Long.toString(days)).append("d");
         if (hours > 0) sb.append((sb.length() > 0) ? " " : "").append(Long.toString(hours)).append("h");
         if (minutes > 0) sb.append((sb.length() > 0) ? " " : "").append(Long.toString(minutes)).append("m");
         if (totalSeconds != 0 && totalSeconds < 60) sb.append((sb.length() > 0) ? " " : "").append(Long.toString(seconds)).append("s");
         if (totalSeconds == 0) return "<1s";
         return sb.toString();
     }
 
 
 
     private static class Message {
 
         static final NewestFirst NEWEST_FIRST = new NewestFirst();
 
         final Long set;
         final String from;
         final String text;
 
         Message(final long set, final String from, final String text) {
             this.set = set;
             this.from = from;
             this.text = text;
         }
 
         private static class NewestFirst implements Comparator<Message> {
 
             @Override
             public int compare(final Message o1, final Message o2) {
                 return o2.set.compareTo(o1.set);
             }
 
         }
 
     }
 
 }
