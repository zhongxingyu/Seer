 package net.betterverse.chatmanager.util;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 import net.betterverse.chatmanager.ChatManager;
 
 public class Configuration {
     private final ChatManager plugin;
     private final YamlFile file;
 
     public Configuration(ChatManager plugin) {
         this.plugin = plugin;
         this.file = new YamlFile(plugin, new File(plugin.getDataFolder(), "config.yml"), "config");
 
         load();
     }
 
     public long getAliasCooldown() {
         return file.getInt("alias-cooldown-hours") * 3600000;
     }
 
     public int getChatLimit() {
         return file.getInt("chat-limit.messages");
     }
 
     public long getChatLimitMillis() {
         return file.getInt("chat-limit.time-in-seconds") * 1000;
     }
 
     public String getChatLimitWarning() {
         return StringHelper.parseColors(file.getString("messages.chat-limit-warning").replace("<time>", String.valueOf(file.getInt("chat-limit.time-in-seconds"))));
     }
 
     public long getConsecutiveMessageTimeout() {
         return file.getInt("consecutive-message-timeout-seconds") * 20;
     }
 
     public String getConsecutiveMessageTimeoutNotification() {
         return StringHelper.parseColors(file.getString("messages.consecutive-message-timeout-notification"));
     }
 
     public String getConsecutiveMessageWarning() {
         return StringHelper.parseColors(file.getString("messages.consecutive-warning"));
     }
 
     public String getChatMessageFormat() {
         return file.getString("chat-format");
     }
 
     public String getMeCommandMessageFormat() {
         return file.getString("me-format");
     }
 
     public boolean isNonDefaultMessageFormat(String messageFormat) {
         if (messageFormat.equals(getMeCommandMessageFormat()))
             return true;
         return false;
     }
 
     public String parsedMessageFormat(String messageFormat, Player sender, String message) {
         return messageFormat.replace("<pex-prefix>", PermissionsEx.getUser(sender).getPrefix(sender.getWorld().getName()))
                 .replace("<prefix>", plugin.getPrefix(sender).isEmpty() ? "" : "&f[" + plugin.getPrefix(sender) + "&f]").replace("<nickname>", plugin.getAlias(sender)).replace("<message>", message);
     }
 
     public int getMaximumConsecutiveMessages() {
         return file.getInt("maximum-consecutive-messages");
     }
 
     public int getPrefixCost() {
         return file.getInt("prefix-cost-in-credits");
     }
 
     public boolean isValidString(String parse) {
         List<String> blacklistedColors = file.getStringList("color-code-blacklist");
         for (int i = 0; i < parse.length(); i++) {
             if (parse.charAt(i) == '&') {
                if (blacklistedColors.contains(String.valueOf(parse.charAt(i + 1)))) {
                    return false;
                 }
             }
         }
 
         return true;
     }
 
     public void sendWhisperMessages(CommandSender sender, CommandSender receiver, String message) {
         sender.sendMessage(StringHelper.parseColors(file.getString("whisper-format.send").replace("<sender>", sender.getName()).replace("<receiver>", receiver.getName()).replace("<message>", message)));
         receiver.sendMessage(StringHelper.parseColors(file.getString("whisper-format.receive").replace("<sender>", sender.getName()).replace("<receiver>", receiver.getName())
                 .replace("<message>", message)));
     }
 
     public String stripInvalidColorCodes(String parse) {
         List<String> blacklistedColors = file.getStringList("color-code-blacklist");
         for (int i = 0; i < parse.length(); i++) {
             if (parse.charAt(i) == '&') {
                 if (blacklistedColors.contains(String.valueOf(parse.charAt(i + 1)))) {
                     // Replace with spaces to maintain string length in loop, trim string later
                     parse.replaceFirst("&" + parse.charAt(i + 1), "  ");
                 }
             }
         }
 
         // Trim the result
         parse = parse.replace(" ", "");
 
         return parse;
     }
 
     public void load() {
         file.load();
 
         // Add defaults
         Map<String, Object> defaults = new HashMap<String, Object>();
         defaults.put("alias-cooldown-hours", 24);
         defaults.put("color-code-blacklist", Arrays.asList("l", "m", "n", "o"));
         defaults.put("chat-format", "<prefix><pex-prefix><nickname>:&f <message>");
         defaults.put("chat-limit.messages", 4);
         defaults.put("chat-limit.time-in-seconds", 30);
         defaults.put("consecutive-message-timeout-seconds", 30);
         defaults.put("maximum-consecutive-messages", 4);
         defaults.put("me-format", "<prefix><pex-prefix><nickname>&f <message>");
         defaults.put("messages.chat-limit-warning", "&cYou have sent too many messages within <time> seconds. Please be patient.");
         defaults.put("messages.consecutive-message-timeout-notification", "&aOkay, enough punishment. You can talk again.");
         defaults.put("messages.consecutive-warning", "&cYou have sent too many messages in a row. Please wait for someone else to chat before chatting again.");
         defaults.put("prefix-cost-in-credits", 10);
         defaults.put("whisper-format.receive", "&7<sender> whispered to you: <message>");
         defaults.put("whisper-format.send", "&7You whispered to <receiver>: <message>");
 
         for (Entry<String, Object> entry : defaults.entrySet()) {
             if (!file.containsKey(entry.getKey())) {
                 file.set(entry.getKey(), entry.getValue());
             }
         }
     }
 }
